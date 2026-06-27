package com.example.jump_droid

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    var isEnabled: Boolean = true

    fun vibrate(type: HapticType) {
        if (!isEnabled || !vibrator.hasVibrator()) return

        when (type) {
            HapticType.IMPACT_LIGHT -> playEffect(longArrayOf(0, 10), intArrayOf(0, 150), -1)
            HapticType.IMPACT_MEDIUM -> playEffect(longArrayOf(0, 25), intArrayOf(0, 200), -1)
            HapticType.IMPACT_HEAVY -> playEffect(longArrayOf(0, 50), intArrayOf(0, 255), -1)
            HapticType.SUCCESS -> playEffect(longArrayOf(0, 20, 50, 20), intArrayOf(0, 180, 0, 255), -1)
            HapticType.WARNING -> playEffect(longArrayOf(0, 100), intArrayOf(0, 120), -1)
            HapticType.EXPLOSION -> playEffect(longArrayOf(0, 200), intArrayOf(0, 255), -1)
            HapticType.TICK -> playEffect(longArrayOf(0, 5), intArrayOf(0, 100), -1)
        }
    }

    private fun playEffect(timings: LongArray, amplitudes: IntArray, repeat: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings[1])
        }
    }

    enum class HapticType {
        IMPACT_LIGHT, IMPACT_MEDIUM, IMPACT_HEAVY, SUCCESS, WARNING, EXPLOSION, TICK
    }
}
