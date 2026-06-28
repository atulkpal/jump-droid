package com.ashwathai.jump_droid

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.core.content.edit
import kotlin.random.Random

class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private val sharedPrefs: SharedPreferences = appContext.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)
    
    private var soundPool: SoundPool? = null
    private val loadedSfx = mutableMapOf<String, Int>()
    private val loadingStatus = mutableMapOf<Int, Boolean>()
    
    private var musicPlayer: MediaPlayer? = null
    private var currentMusicResId: Int? = null
    
    private var currentZone: AltitudeZone? = null
    private var isBossMusicPlaying = false

    // Thrust Loop Handle
    private var thrustStreamId: Int = 0

    var sfxVolume = 0.8f
        set(value) {
            field = value.coerceIn(0f, 1f)
            if (thrustStreamId != 0) {
                val bias = sfxBias["sfx_thrust_loop"] ?: 1.0f
                soundPool?.setVolume(thrustStreamId, field * bias, field * bias)
            }
        }
    var musicVolume = 0.6f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateMusicVolume()
        }

    private var _isMuted = sharedPrefs.getBoolean("is_muted", false)
    var isMuted: Boolean
        get() = _isMuted
        set(value) {
            _isMuted = value
            sharedPrefs.edit { putBoolean("is_muted", value) }
            applyMuteState(value)
        }

    private val sfxBias = mapOf(
        "sfx_thrust_loop" to 0.45f,
        "sfx_collect_item" to 0.85f,
        "sfx_hit_hull" to 1.0f,
        "sfx_hit_shield" to 0.95f,
        "sfx_impact_small_1" to 1.3f,
        "sfx_impact_small_2" to 1.3f,
        "sfx_overheat_alarm" to 0.75f,
        "sfx_ui_click" to 0.7f,
        "sfx_ui_confirm" to 0.9f,
        "sfx_cooling_vent" to 0.95f,
        "sfx_land_impact" to 0.9f
    )

    private val musicBias = mapOf(
        R.raw.bgm_menu to 0.8f,
        R.raw.bgm_boss to 1.0f,
        R.raw.bgm_singularity to 1.2f
    )

    init {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA) // Changed to MEDIA for better compatibility
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            
            soundPool = SoundPool.Builder()
                .setMaxStreams(15)
                .setAudioAttributes(attrs)
                .build()
        } catch (e: Exception) {
            // Legacy fallback
            @Suppress("DEPRECATION")
            soundPool = SoundPool(15, AudioManager.STREAM_MUSIC, 0)
        }
            
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadingStatus[sampleId] = true
            }
        }

        loadAllSfx()
        applyMuteState(_isMuted)
    }

    private fun applyMuteState(muted: Boolean) {
        if (muted) {
            stopMusicInternal()
            stopThrust()
            soundPool?.autoPause()
        } else {
            soundPool?.autoResume()
            if (isBossMusicPlaying) {
                playBossMusic()
            } else {
                currentZone?.let { playZoneMusic(it) } ?: playMenuMusic()
            }
        }
    }

    private fun loadAllSfx() {
        load("sfx_ui_click", R.raw.sfx_ui_click)
        load("sfx_ui_confirm", R.raw.sfx_ui_confirm)
        load("sfx_ui_back", R.raw.sfx_ui_back)
        load("sfx_fanfare_unlock", R.raw.sfx_fanfare_unlock)
        load("sfx_fanfare_mission", R.raw.sfx_fanfare_mission)
        load("sfx_gameover", R.raw.sfx_gameover)
        load("sfx_data_scan", R.raw.sfx_data_scan)
        load("sfx_hit_hull", R.raw.sfx_hit_hull)
        load("sfx_hit_shield", R.raw.sfx_hit_shield)
        load("sfx_collect_item", R.raw.sfx_collect_item)
        load("sfx_projectile_fire", R.raw.sfx_projectile_fire)
        load("sfx_explosion_enemy", R.raw.sfx_explosion_enemy)
        load("sfx_boss_weakpoint", R.raw.sfx_boss_weakpoint)
        load("sfx_boss_defeat", R.raw.sfx_boss_defeat)
        load("sfx_land_impact", R.raw.sfx_land_impact)
        load("sfx_land_metal", R.raw.sfx_land_impact)
        load("sfx_land_ice", R.raw.sfx_land_ice)
        load("sfx_land_energy", R.raw.sfx_land_energy)
        load("sfx_land_gravity", R.raw.sfx_land_gravity)
        load("sfx_land_utility", R.raw.sfx_land_utility)
        load("sfx_land_fragile", R.raw.sfx_land_fragile)
        load("sfx_thrust_loop", R.raw.sfx_thrust_loop)
        load("sfx_cooling_vent", R.raw.sfx_cooling_vent)
        load("sfx_hazard_lightning", R.raw.sfx_hazard_lightning)
        load("sfx_hazard_emp", R.raw.sfx_hazard_emp)
        load("sfx_hazard_void", R.raw.sfx_hazard_void)
        load("sfx_hazard_wind", R.raw.sfx_hazard_wind)
        load("sfx_hazard_radiation", R.raw.sfx_hazard_radiation)
        load("sfx_overheat_alarm", R.raw.sfx_overheat_alarm)
        load("sfx_alarm_low_fuel", R.raw.sfx_alarm_low_fuel)
        load("sfx_alarm_critical", R.raw.sfx_alarm_critical)
        load("sfx_impact_small_1", R.raw.sfx_impact_small_1)
        load("sfx_impact_small_2", R.raw.sfx_impact_small_2)
    }

    private fun load(name: String, resId: Int) {
        soundPool?.let { pool ->
            val id = pool.load(appContext, resId, 1)
            loadedSfx[name] = id
        }
    }

    fun playSfx(name: String, loop: Boolean = false) {
        if (_isMuted) return
        val targetName = if (name == "sfx_impact_small") {
            if (Random.nextBoolean()) "sfx_impact_small_1" else "sfx_impact_small_2"
        } else {
            name
        }
        
        loadedSfx[targetName]?.let { id ->
            if (loadingStatus[id] == true) {
                val bias = sfxBias[targetName] ?: 1.0f
                val vol = sfxVolume * bias
                val loopVal = if (loop) -1 else 0
                soundPool?.play(id, vol, vol, 1, loopVal, 1f)
            }
        }
    }

    fun startThrust() {
        if (_isMuted || thrustStreamId != 0) return
        loadedSfx["sfx_thrust_loop"]?.let { id ->
            if (loadingStatus[id] == true) {
                val bias = sfxBias["sfx_thrust_loop"] ?: 0.5f
                val vol = sfxVolume * bias
                thrustStreamId = soundPool?.play(id, vol, vol, 1, -1, 1f) ?: 0
            }
        }
    }

    fun stopThrust() {
        if (thrustStreamId != 0) {
            soundPool?.stop(thrustStreamId)
            thrustStreamId = 0
        }
    }

    fun playMusic(resId: Int) {
        if (_isMuted) {
            currentMusicResId = resId
            return
        }
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) return
        stopMusicInternal()
        currentMusicResId = resId
        try {
            musicPlayer = MediaPlayer.create(appContext, resId).apply {
                isLooping = true
                val bias = musicBias[resId] ?: 1.0f
                setVolume(musicVolume * bias, musicVolume * bias)
                start()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing music: ${e.message}")
        }
    }

    private fun updateMusicVolume() {
        musicPlayer?.let { player ->
            val bias = musicBias[currentMusicResId] ?: 1.0f
            player.setVolume(musicVolume * bias, musicVolume * bias)
        }
    }

    fun stopMusic() {
        stopMusicInternal()
        currentMusicResId = null
    }

    private fun stopMusicInternal() {
        musicPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {}
            release()
        }
        musicPlayer = null
    }

    fun handleZoneChange(zone: AltitudeZone) {
        currentZone = zone
        if (!isBossMusicPlaying && !_isMuted) {
            playZoneMusic(zone)
        }
    }

    fun playZoneMusic(zone: AltitudeZone) {
        val resId = when (zone) {
            AltitudeZone.EARTH -> R.raw.bgm_earth
            AltitudeZone.CLOUD_LAYER -> R.raw.bgm_clouds
            AltitudeZone.UPPER_ATMOSPHERE -> R.raw.bgm_atmosphere
            AltitudeZone.ORBIT -> R.raw.bgm_orbit
            AltitudeZone.THE_FOUNDRY -> R.raw.bgm_foundry
            AltitudeZone.DEEP_SPACE -> R.raw.bgm_space
            AltitudeZone.CHRONO_RIFT -> R.raw.bgm_chrono
            AltitudeZone.VOID -> R.raw.bgm_void
            AltitudeZone.THE_BEYOND -> R.raw.bgm_beyond
            AltitudeZone.STELLAR_GATE -> R.raw.bgm_gate
            AltitudeZone.ANCIENT_CONSTRUCT -> R.raw.bgm_construct
            AltitudeZone.SINGULARITY -> R.raw.bgm_singularity
        }
        playMusic(resId)
    }

    fun setBossActive(active: Boolean) {
        if (isBossMusicPlaying == active) return
        isBossMusicPlaying = active
        if (active) {
            playBossMusic()
        } else {
            if (!_isMuted) {
                currentZone?.let { playZoneMusic(it) } ?: playMenuMusic()
            }
        }
    }

    fun playBossMusic() {
        playMusic(R.raw.bgm_boss)
    }

    fun playMenuMusic() {
        isBossMusicPlaying = false
        playMusic(R.raw.bgm_menu)
    }

    fun pauseAll() {
        try {
            if (musicPlayer?.isPlaying == true) {
                musicPlayer?.pause()
            }
            soundPool?.autoPause()
            stopThrust()
        } catch (_: Exception) {}
    }

    fun resumeAll() {
        if (_isMuted) return
        try {
            musicPlayer?.start()
            soundPool?.autoResume()
        } catch (_: Exception) {}
    }

    fun release() {
        stopThrust()
        stopMusicInternal()
        soundPool?.release()
        soundPool = null
    }
}
