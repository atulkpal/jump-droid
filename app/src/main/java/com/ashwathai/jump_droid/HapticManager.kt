package com.ashwathai.jump_droid

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class HapticManager(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val isEnabled: Boolean 
        get() = sharedPrefs.getBoolean("haptic_enabled", true)

    fun vibrate(type: HapticType) {
        if (!isEnabled || !vibrator.hasVibrator()) {
            return
        }

        // Increased durations significantly to ensure they are felt on all hardware
        when (type) {
            HapticType.IMPACT_LIGHT -> playEffect(longArrayOf(0, 30), intArrayOf(0, 180), -1)
            HapticType.IMPACT_MEDIUM -> playEffect(longArrayOf(0, 50), intArrayOf(0, 220), -1)
            HapticType.IMPACT_HEAVY -> playEffect(longArrayOf(0, 100), intArrayOf(0, 255), -1)
            HapticType.SUCCESS -> playEffect(longArrayOf(0, 50, 60, 50), intArrayOf(0, 200, 0, 255), -1)
            HapticType.WARNING -> playEffect(longArrayOf(0, 300), intArrayOf(0, 150), -1)
            HapticType.EXPLOSION -> playEffect(longArrayOf(0, 500), intArrayOf(0, 255), -1)
            HapticType.TICK -> playEffect(longArrayOf(0, 20), intArrayOf(0, 120), -1)
        }
    }

    private fun playEffect(timings: LongArray, amplitudes: IntArray, repeat: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, repeat)
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                vibrator.vibrate(effect, attrs)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings[1])
            }
        } catch (e: Exception) {
            Log.e("HapticManager", "Error playing haptic effect: ${e.message}")
        }
    }

    enum class HapticType {
        IMPACT_LIGHT, IMPACT_MEDIUM, IMPACT_HEAVY, SUCCESS, WARNING, EXPLOSION, TICK
    }
}
