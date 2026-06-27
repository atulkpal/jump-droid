package com.ashwathai.jump_droid

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import kotlin.random.Random

class SoundManager(private val appContext: Context) {

    private var soundPool: SoundPool? = null
    private val loadedSfx = mutableMapOf<String, Int>()
    private var musicPlayer: MediaPlayer? = null
    private var currentMusicResId: Int? = null
    
    private var currentZone: AltitudeZone? = null
    private var isBossMusicPlaying = false

    // Thrust Loop Handle
    private var thrustStreamId: Int = 0

    var sfxVolume = 0.7f
        set(value) {
            field = value.coerceIn(0f, 1f)
            if (thrustStreamId != 0) {
                val bias = sfxBias["sfx_thrust_loop"] ?: 1.0f
                soundPool?.setVolume(thrustStreamId, field * bias, field * bias)
            }
        }
    var musicVolume = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateMusicVolume()
        }
    var isMuted = false

    // --- Volume Balancing (Normalization) ---
    private val sfxBias = mapOf(
        "sfx_thrust_loop" to 0.35f,   // Engine is usually very loud
        "sfx_collect_item" to 0.8f,   // High-pitched sounds pierce more
        "sfx_hit_hull" to 1.0f,
        "sfx_hit_shield" to 0.9f,
        "sfx_impact_small_1" to 1.2f, // Boost weak impacts
        "sfx_impact_small_2" to 1.2f,
        "sfx_overheat_alarm" to 0.7f,
        "sfx_ui_click" to 0.6f,       // UI should be subtle
        "sfx_ui_confirm" to 0.8f,
        "sfx_cooling_vent" to 0.9f,
        "sfx_land_impact" to 0.85f
    )

    private val musicBias = mapOf(
        R.raw.bgm_menu to 0.7f,      // Menu music is often mixed loud
        R.raw.bgm_boss to 1.0f,      // Boss needs presence
        R.raw.bgm_singularity to 1.2f // Epic finale boost
    )

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(12)
            .setAudioAttributes(attrs)
            .build()

        loadAllSfx()
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
        if (isMuted) return
        val targetName = if (name == "sfx_impact_small") {
            if (Random.nextBoolean()) "sfx_impact_small_1" else "sfx_impact_small_2"
        } else {
            name
        }
        
        loadedSfx[targetName]?.let { id ->
            val bias = sfxBias[targetName] ?: 1.0f
            val vol = sfxVolume * bias
            val loopVal = if (loop) -1 else 0
            soundPool?.play(id, vol, vol, 1, loopVal, 1f)
        }
    }

    fun startThrust() {
        if (isMuted || thrustStreamId != 0) return
        loadedSfx["sfx_thrust_loop"]?.let { id ->
            val bias = sfxBias["sfx_thrust_loop"] ?: 0.4f
            val vol = sfxVolume * bias
            thrustStreamId = soundPool?.play(id, vol, vol, 1, -1, 1f) ?: 0
        }
    }

    fun stopThrust() {
        if (thrustStreamId != 0) {
            soundPool?.stop(thrustStreamId)
            thrustStreamId = 0
        }
    }

    fun playMusic(resId: Int) {
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) return
        stopMusic()
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
        musicPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        musicPlayer = null
        currentMusicResId = null
    }

    fun handleZoneChange(zone: AltitudeZone) {
        currentZone = zone
        if (!isBossMusicPlaying) {
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
            playMusic(R.raw.bgm_boss)
        } else {
            currentZone?.let { playZoneMusic(it) } ?: playMenuMusic()
        }
    }

    fun playMenuMusic() {
        isBossMusicPlaying = false
        playMusic(R.raw.bgm_menu)
    }

    fun release() {
        stopThrust()
        stopMusic()
        soundPool?.release()
        soundPool = null
    }
}
