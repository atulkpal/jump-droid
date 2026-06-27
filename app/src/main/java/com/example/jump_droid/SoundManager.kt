package com.example.jump_droid

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val appContext: Context) {

    private var soundPool: SoundPool? = null
    private val loadedSfx = mutableMapOf<String, Int>()
    private var musicPlayer: MediaPlayer? = null
    private var currentMusicResId: Int? = null
    private var thrustThread: Thread? = null
    private var isThrusting = false

    var sfxVolume = 0.7f
        set(value) { field = value.coerceIn(0f, 1f) }
    var musicVolume = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            musicPlayer?.setVolume(field, field)
        }
    var isMuted = true

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build()
        } else {
            soundPool = SoundPool(8, AudioManager.STREAM_MUSIC, 0)
        }
    }

    fun loadSfx(name: String, resId: Int) {
        soundPool?.let { pool ->
            pool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) loadedSfx[name]?.let { id ->
                    playSfx(name)
                }
            }
            val soundId = pool.load(appContext, resId, 1)
            loadedSfx[name] = soundId
        }
    }

    fun playSfx(name: String) {
        if (isMuted) return
        loadedSfx[name]?.let { id ->
            soundPool?.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
        } ?: playGenerated(name)
    }

    fun startThrust() {
        if (isMuted || isThrusting) return
        isThrusting = true
        thrustThread = Thread {
            val sampleRate = 22050
            val bufferSize = sampleRate / 10
            val track = AudioTrack(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build(),
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                bufferSize * 2, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            track.play()
            val buffer = ShortArray(bufferSize)
            var phase = 0f
            while (isThrusting && !Thread.interrupted()) {
                val vol = (sfxVolume * 0.3f).coerceIn(0f, 1f)
                for (i in buffer.indices) {
                    phase += 0.02f
                    val noise = sin(phase * 3.7f) * 0.5f + sin(phase * 7.1f) * 0.3f + sin(phase * 11.3f) * 0.2f
                    val rumble = (Math.random().toFloat() * 2f - 1f) * 0.3f
                    val sample = ((noise + rumble) * vol * Short.MAX_VALUE).toInt().toShort()
                    buffer[i] = sample
                }
                try { track.write(buffer, 0, bufferSize) } catch (_: Exception) { break }
            }
            track.stop(); track.release()
        }.apply { isDaemon = true; start() }
    }

    fun stopThrust() {
        isThrusting = false
        thrustThread?.interrupt()
        thrustThread = null
    }

    fun playMusic(resId: Int) {
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) return
        stopMusic()
        currentMusicResId = resId
        try {
            musicPlayer = MediaPlayer.create(appContext, resId).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } catch (_: Exception) {}
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
        currentMusicResId = null
    }

    fun handleZoneChange(zone: AltitudeZone) {
        if (isMuted) return
        // Placeholder for real music: play a distinct procedural tone for each zone transition
        val freq = when (zone) {
            AltitudeZone.EARTH -> 220f
            AltitudeZone.CLOUD_LAYER -> 261f
            AltitudeZone.UPPER_ATMOSPHERE -> 293f
            AltitudeZone.ORBIT -> 329f
            AltitudeZone.THE_FOUNDRY -> 349f
            AltitudeZone.DEEP_SPACE -> 392f
            AltitudeZone.CHRONO_RIFT -> 440f
            AltitudeZone.VOID -> 493f
            AltitudeZone.THE_BEYOND -> 523f
            AltitudeZone.STELLAR_GATE -> 587f
            AltitudeZone.ANCIENT_CONSTRUCT -> 659f
            AltitudeZone.SINGULARITY -> 698f
        }
        playGeneratedTone(freq, 2000, EnvelopeType.FADE_OUT)
    }

    private fun playGeneratedTone(freq: Float, durationMs: Int, envelope: EnvelopeType) {
        Thread {
            val sampleRate = 22050
            val numSamples = sampleRate * durationMs / 1000
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val env = when (envelope) {
                    EnvelopeType.FADE_OUT -> 1f - i.toFloat() / numSamples
                    EnvelopeType.QUICK_DECAY -> kotlin.math.exp(-t * 25f)
                    EnvelopeType.SUSTAIN -> 1f
                    EnvelopeType.CLICK -> if (i < numSamples / 4) 1f else 0f
                }
                val sample = (sin(2.0 * PI * freq * t).toFloat() * env * Short.MAX_VALUE * 0.2f).toInt().toShort()
                samples[i] = sample
            }
            try {
                val track = AudioTrack(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build(),
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    numSamples * 2, AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                track.write(samples, 0, numSamples)
                track.play()
                track.release()
            } catch (_: Exception) {}
        }.apply { isDaemon = true; start() }
    }

    fun release() {
        stopThrust()
        stopMusic()
        soundPool?.release()
        soundPool = null
    }

    private data class SfxSpec(val freq: Float, val durationMs: Int, val envelope: EnvelopeType)
    private enum class EnvelopeType { FADE_OUT, QUICK_DECAY, SUSTAIN, CLICK }

    private fun spec(name: String): SfxSpec = when (name) {
        "collect", "sfx_collect_item" -> SfxSpec(880f, 100, EnvelopeType.FADE_OUT)
        "land", "sfx_land_impact", "sfx_land_metal" -> SfxSpec(60f, 80, EnvelopeType.QUICK_DECAY)
        "sfx_land_ice" -> SfxSpec(1200f, 60, EnvelopeType.CLICK)
        "sfx_land_energy" -> SfxSpec(1000f, 150, EnvelopeType.FADE_OUT)
        "sfx_land_gravity" -> SfxSpec(40f, 200, EnvelopeType.SUSTAIN)
        "sfx_land_utility" -> SfxSpec(400f, 100, EnvelopeType.QUICK_DECAY)
        "sfx_land_fragile" -> SfxSpec(150f, 50, EnvelopeType.CLICK)
        "damage", "sfx_hit_hull" -> SfxSpec(200f, 150, EnvelopeType.FADE_OUT)
        "sfx_hit_shield" -> SfxSpec(600f, 120, EnvelopeType.FADE_OUT)
        "click", "sfx_ui_click" -> SfxSpec(1000f, 40, EnvelopeType.CLICK)
        "sfx_ui_confirm", "unlock", "sfx_fanfare_unlock" -> SfxSpec(1047f, 300, EnvelopeType.FADE_OUT)
        "boss" -> SfxSpec(150f, 500, EnvelopeType.SUSTAIN)
        "combo" -> SfxSpec(660f, 200, EnvelopeType.FADE_OUT)
        "overheat", "sfx_overheat_alarm", "sfx_hazard_emp" -> SfxSpec(400f, 300, EnvelopeType.SUSTAIN)
        "death", "sfx_boss_defeat" -> SfxSpec(80f, 600, EnvelopeType.FADE_OUT)
        "explosion", "sfx_hazard_lightning", "sfx_explosion_enemy" -> SfxSpec(50f, 400, EnvelopeType.QUICK_DECAY)
        "low_fuel", "sfx_alarm_low_fuel" -> SfxSpec(300f, 200, EnvelopeType.SUSTAIN)
        "sfx_alarm_critical" -> SfxSpec(150f, 400, EnvelopeType.SUSTAIN)
        "sfx_projectile_fire" -> SfxSpec(440f, 60, EnvelopeType.CLICK)
        "sfx_boss_weakpoint" -> SfxSpec(100f, 200, EnvelopeType.QUICK_DECAY)
        "sfx_fanfare_mission" -> SfxSpec(880f, 500, EnvelopeType.FADE_OUT)
        "sfx_data_scan" -> SfxSpec(1500f, 400, EnvelopeType.SUSTAIN)
        else -> SfxSpec(440f, 100, EnvelopeType.FADE_OUT)
    }

    private fun playGenerated(name: String) {
        val s = spec(name)
        Thread {
            val sampleRate = 22050
            val numSamples = sampleRate * s.durationMs / 1000
            val samples = ShortArray(numSamples)
            val noise = name in listOf("explosion", "death", "sfx_hazard_lightning", "sfx_boss_defeat", "sfx_explosion_enemy")

            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val envelope = when (s.envelope) {
                    EnvelopeType.FADE_OUT -> 1f - i.toFloat() / numSamples
                    EnvelopeType.QUICK_DECAY -> kotlin.math.exp(-t * 25f)
                    EnvelopeType.SUSTAIN -> 1f
                    EnvelopeType.CLICK -> if (i < numSamples / 4) 1f else 0f
                }
                val tone = if (noise) {
                    (Math.random().toFloat() * 2f - 1f) * 0.5f + sin(2.0 * PI * s.freq * t).toFloat() * 0.5f
                } else {
                    sin(2.0 * PI * s.freq * t).toFloat()
                }
                val sample = (tone * envelope * Short.MAX_VALUE * 0.5f).toInt().toShort()
                samples[i] = sample
            }

            try {
                val track = AudioTrack(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build(),
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    numSamples * 2, AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                track.write(samples, 0, numSamples)
                track.play()
                track.release()
            } catch (_: Exception) {}
        }.apply { isDaemon = true; start() }
    }
}
