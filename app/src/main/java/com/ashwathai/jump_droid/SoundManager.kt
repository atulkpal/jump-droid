package com.ashwathai.jump_droid

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.random.Random

class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private val sharedPrefs: SharedPreferences = appContext.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main)

    private var soundPool: SoundPool? = null
    private val loadedSfx = mutableMapOf<String, Int>()

    // Dual MediaPlayer for crossfade
    private var musicPlayerA: MediaPlayer? = null
    private var musicPlayerB: MediaPlayer? = null
    private var currentMusicResId: Int? = null
    private var crossfadeJob: Job? = null

    private var currentZone: AltitudeZone? = null
    private var isBossMusicPlaying = false

    // Thrust Loop Handle
    private var thrustStreamId: Int = 0

    // Managed loop streams (alarms, ambient, etc.)
    private val loopStreams = mutableMapOf<String, Int>()
    private val activeLoopNames = mutableSetOf<String>()
    private var ambientLoop: String? = null

    // Ducking
    private var duckJob: Job? = null
    private var duckSfxOriginal = 0.9f
    private var duckMusicOriginal = 0.45f
    private var isDucked = false

    var sfxVolume = 0.9f
        set(value) {
            field = value.coerceIn(0f, 1f)
            if (thrustStreamId != 0) {
                val bias = sfxBias["sfx_thrust_loop"] ?: 1.0f
                soundPool?.setVolume(thrustStreamId, field * bias, field * bias)
            }
            // Update loop volumes too
            loopStreams.forEach { (name, streamId) ->
                val bias = sfxBias[name] ?: 1.0f
                soundPool?.setVolume(streamId, field * bias, field * bias)
            }
        }
    var musicVolume = 0.45f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateMusicVolume()
        }

    private var _isMuted by mutableStateOf(sharedPrefs.getBoolean("is_muted", false))
    var isMuted: Boolean
        get() = _isMuted
        set(value) {
            Log.d("SoundManager", "Mute state changed to $value")
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
        "sfx_ui_click" to 0.78f,
        "sfx_ui_confirm" to 0.9f,
        "sfx_cooling_vent" to 0.95f,
        "sfx_land_impact" to 0.9f,
        "sfx_alarm_critical" to 0.8f,
        "sfx_alarm_low_fuel" to 0.8f,
        "sfx_hazard_wind" to 0.5f,
        "sfx_hazard_void" to 0.5f
    )

    private val musicBias = mapOf(
        R.raw.bgm_menu to 0.8f,
        R.raw.bgm_boss to 1.0f,
        R.raw.bgm_singularity to 1.2f
    )

    init {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(15)
                .setAudioAttributes(attrs)
                .build()
        } catch (e: Exception) {
            @Suppress("DEPRECATION")
            soundPool = SoundPool(15, AudioManager.STREAM_MUSIC, 0)
        }

        loadAllSfx()
        applyMuteState(_isMuted)
        Log.d("SoundManager", "Initialized — sfx=$sfxVolume music=$musicVolume muted=$_isMuted")
    }

    private fun applyMuteState(muted: Boolean) {
        if (muted) {
            stopMusicInternal()
            stopAllLoops()
            stopThrust()
            soundPool?.autoPause()
        } else {
            soundPool?.autoResume()
            // Restart ambient loop
            currentZone?.let {
                ambientLoop = null
                startAmbientForZone(it)
            }
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
        load("sfx_land_impact", R.raw.sfx_land_metal)
        load("sfx_land_metal", R.raw.sfx_land_metal)
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
            val bias = sfxBias[targetName] ?: 1.0f
            val vol = (sfxVolume * bias).coerceIn(0f, 1f)
            val loopVal = if (loop) -1 else 0
            soundPool?.play(id, vol, vol, 1, loopVal, 1f)
        }
    }

    // --- Loop Management ---

    fun startLoop(name: String) {
        if (_isMuted) return
        if (activeLoopNames.contains(name)) return // prevent duplicates
        loadedSfx[name]?.let { id ->
            val bias = sfxBias[name] ?: 1.0f
            val vol = (sfxVolume * bias).coerceIn(0f, 1f)
            val streamId = soundPool?.play(id, vol, vol, 1, -1, 1f) ?: return
            loopStreams[name] = streamId
            activeLoopNames.add(name)
            Log.d("SoundManager", "startLoop: $name (stream=$streamId)")
        }
    }

    fun stopLoop(name: String) {
        loopStreams.remove(name)?.let { streamId ->
            soundPool?.stop(streamId)
            activeLoopNames.remove(name)
            Log.d("SoundManager", "stopLoop: $name (stream=$streamId)")
        }
    }

    fun stopAllLoops() {
        loopStreams.keys.toList().forEach { stopLoop(it) }
        ambientLoop = null
    }

    fun isLoopActive(name: String): Boolean = activeLoopNames.contains(name)

    // --- Ambient Zone Sounds ---

    private fun getAmbientForZone(zone: AltitudeZone): String? {
        return when (zone) {
            AltitudeZone.EARTH, AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE,
            AltitudeZone.ORBIT, AltitudeZone.THE_FOUNDRY -> "sfx_hazard_wind"
            AltitudeZone.VOID, AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE,
            AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY -> "sfx_hazard_void"
            AltitudeZone.DEEP_SPACE, AltitudeZone.CHRONO_RIFT -> null
        }
    }

    private fun startAmbientForZone(zone: AltitudeZone) {
        val newAmbient = getAmbientForZone(zone)
        if (newAmbient == ambientLoop) return
        // Stop previous ambient
        ambientLoop?.let { stopLoop(it) }
        ambientLoop = null
        // Start new ambient
        if (newAmbient != null) {
            startLoop(newAmbient)
            ambientLoop = newAmbient
            Log.d("SoundManager", "Ambient started: $newAmbient for zone $zone")
        }
    }

    // --- Ducking ---

    fun duck(durationMs: Long) {
        if (isDucked) return
        duckSfxOriginal = sfxVolume
        duckMusicOriginal = musicVolume
        sfxVolume = (sfxVolume * 0.8f).coerceIn(0f, 1f)
        musicVolume = (musicVolume * 0.8f).coerceIn(0f, 1f)
        isDucked = true
        Log.d("SoundManager", "Duck started — sfx: $duckSfxOriginal->$sfxVolume music: $duckMusicOriginal->$musicVolume for ${durationMs}ms")

        duckJob?.cancel()
        duckJob = scope.launch {
            delay(durationMs)
            sfxVolume = duckSfxOriginal
            musicVolume = duckMusicOriginal
            isDucked = false
            duckJob = null
            Log.d("SoundManager", "Duck ended — restored sfx=$sfxVolume music=$musicVolume")
        }
    }

    // --- Game Over Fade ---

    fun fadeOutAndPlayGameOver(onGameOverSfx: () -> Unit) {
        Log.d("SoundManager", "Game over fade-out started")
        scope.launch {
            // Fade out current music player
            val activePlayer = getActiveMusicPlayer()
            activePlayer?.let { player ->
                if (player.isPlaying) {
                    val bias = musicBias[currentMusicResId] ?: 1.0f
                    val startVol = (musicVolume * bias).coerceIn(0f, 1f)
                    val steps = 12
                    repeat(steps) {
                        val progress = (it + 1).toFloat() / steps
                        player.setVolume(startVol * (1f - progress), startVol * (1f - progress))
                        delay(33)
                    }
                    player.stop()
                    Log.d("SoundManager", "Music faded out for game over")
                }
            }
            // Stop all loops
            stopAllLoops()
            stopThrust()
            // Play game over SFX
            onGameOverSfx()
        }
    }

    private fun getActiveMusicPlayer(): MediaPlayer? {
        return musicPlayerA ?: musicPlayerB
    }

    // --- Thrust ---

    fun startThrust() {
        if (_isMuted || thrustStreamId != 0) return
        loadedSfx["sfx_thrust_loop"]?.let { id ->
            val bias = sfxBias["sfx_thrust_loop"] ?: 0.5f
            val vol = (sfxVolume * bias).coerceIn(0f, 1f)
            thrustStreamId = soundPool?.play(id, vol, vol, 1, -1, 1f) ?: 0
            Log.d("SoundManager", "Thrust started (stream=$thrustStreamId)")
        }
    }

    fun stopThrust() {
        if (thrustStreamId != 0) {
            soundPool?.stop(thrustStreamId)
            Log.d("SoundManager", "Thrust stopped (stream=$thrustStreamId)")
            thrustStreamId = 0
        }
    }

    // --- Music System (with crossfade) ---

    fun playMusic(resId: Int) {
        if (_isMuted) {
            currentMusicResId = resId
            return
        }
        if (currentMusicResId == resId && isMusicPlaying()) return

        val oldPlayer = getActiveMusicPlayer()
        currentMusicResId = resId

        try {
            val newPlayer = MediaPlayer.create(appContext, resId).apply {
                isLooping = true
                start()
            }
            // Assign new player to an available slot
            if (musicPlayerA == null || musicPlayerA == oldPlayer) {
                musicPlayerA = newPlayer
            } else {
                musicPlayerB = newPlayer
            }

            val bias = musicBias[resId] ?: 1.0f
            val targetVol = (musicVolume * bias).coerceIn(0f, 1f)

            if (oldPlayer?.isPlaying == true) {
                // Crossfade: ramp new up, old down over 600ms async
                newPlayer.setVolume(0f, 0f)
                crossfadeJob?.cancel()
                crossfadeJob = scope.launch {
                    val oldBias = musicBias[resId] ?: 1.0f
                    val oldVol = (musicVolume * oldBias).coerceIn(0f, 1f)
                    val steps = 20
                    repeat(steps) {
                        val t = (it + 1).toFloat() / steps
                        newPlayer.setVolume(targetVol * t, targetVol * t)
                        oldPlayer.setVolume(oldVol * (1f - t), oldVol * (1f - t))
                        delay(30)
                    }
                    oldPlayer.stop()
                    oldPlayer.release()
                    if (musicPlayerA == oldPlayer) musicPlayerA = null
                    if (musicPlayerB == oldPlayer) musicPlayerB = null
                    Log.d("SoundManager", "Crossfade complete: new track=$resId")
                    crossfadeJob = null
                }
            } else {
                newPlayer.setVolume(targetVol, targetVol)
                Log.d("SoundManager", "Music started: resId=$resId vol=$targetVol")
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error during music play: ${e.message}")
        }
    }

    private fun isMusicPlaying(): Boolean {
        return (musicPlayerA?.isPlaying == true) || (musicPlayerB?.isPlaying == true)
    }

    private fun updateMusicVolume() {
        val bias = musicBias[currentMusicResId] ?: 1.0f
        val vol = (musicVolume * bias).coerceIn(0f, 1f)
        musicPlayerA?.setVolume(vol, vol)
        musicPlayerB?.setVolume(vol, vol)
    }

    fun stopMusic() {
        stopMusicInternal()
        currentMusicResId = null
    }

    private fun stopMusicInternal() {
        crossfadeJob?.cancel()
        crossfadeJob = null
        listOf(musicPlayerA, musicPlayerB).forEach { player ->
            player?.apply {
                try {
                    if (isPlaying) stop()
                } catch (_: Exception) {}
                release()
            }
        }
        musicPlayerA = null
        musicPlayerB = null
    }

    fun handleZoneChange(zone: AltitudeZone) {
        Log.d("SoundManager", "Zone change: $zone")
        currentZone = zone
        startAmbientForZone(zone)
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
            listOf(musicPlayerA, musicPlayerB).forEach { player ->
                if (player?.isPlaying == true) player.pause()
            }
            soundPool?.autoPause()
            stopThrust()
        } catch (_: Exception) {}
    }

    fun resumeAll() {
        if (_isMuted) return
        try {
            listOf(musicPlayerA, musicPlayerB).forEach { player ->
                player?.start()
            }
            soundPool?.autoResume()
        } catch (_: Exception) {}
    }

    fun release() {
        scope.cancel()
        stopAllLoops()
        stopThrust()
        stopMusicInternal()
        soundPool?.release()
        soundPool = null
        Log.d("SoundManager", "Released all resources")
    }
}
