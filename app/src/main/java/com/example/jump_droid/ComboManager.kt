package com.example.jump_droid

import androidx.compose.runtime.*
import kotlin.math.max

sealed class ComboReward {
    data class Fuel(val amount: Float) : ComboReward()
    data class PowerUp(val type: PowerUpType) : ComboReward()
    object AltitudeBoost : ComboReward()
    data class Artifact(val discoveryType: DiscoveryType) : ComboReward()
}

data class ComboTier(
    val tierLevel: Int,
    val minCombo: Int,
    val maxCombo: Int,
    val name: String,
    val rewards: List<ComboReward>
)

data class FlyingReward(
    val type: ComboReward,
    var x: Float,
    var y: Float,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var scale: Float = 2.0f,
    var progress: Float = 0f // 0.0 to 1.0
)

class ComboManager {
    var currentCombo by mutableIntStateOf(0)
    var bestComboThisRun by mutableIntStateOf(0)
    var comboTimeRemaining by mutableLongStateOf(0L)
    var comboTarget by mutableIntStateOf(5)
    
    // Reward Presentation state
    var pendingReward by mutableStateOf<ComboReward?>(null)
    var lastFinalStreak by mutableIntStateOf(0)
    var showComboComplete by mutableStateOf(false)
    var comboCompleteTimer by mutableFloatStateOf(0f)

    private val tiers = listOf(
        ComboTier(1, 5, 7, "BASIC", listOf(ComboReward.Fuel(50f))),
        ComboTier(2, 8, 11, "IMPROVED", listOf(ComboReward.PowerUp(PowerUpType.TURBO_BOOSTER))),
        ComboTier(3, 12, 15, "ADVANCED", listOf(ComboReward.AltitudeBoost)),
        ComboTier(4, 16, 20, "ELITE", listOf(ComboReward.PowerUp(PowerUpType.ARTIFACT))),
        ComboTier(5, 21, 999, "LEGENDARY", listOf(ComboReward.Artifact(DiscoveryType.ART_RECORDER)))
    )

    fun onLanding() {
        currentCombo++
        refreshTimer()
        
        if (currentCombo > bestComboThisRun) {
            bestComboThisRun = currentCombo
            // New combo high reached
        }
        
        if (currentCombo >= comboTarget) {
            // Target reached
            comboTarget = bestComboThisRun + 1
        }
    }

    private fun refreshTimer() {
        comboTimeRemaining = getWindowForCombo(currentCombo)
    }

    fun update(dt: Float) {
        if (currentCombo > 0) {
            comboTimeRemaining = max(0L, comboTimeRemaining - (dt * 1000).toLong())
            if (comboTimeRemaining <= 0L) {
                breakCombo()
            }
        }
        
        if (showComboComplete) {
            comboCompleteTimer -= dt
            if (comboCompleteTimer <= 0f) {
                showComboComplete = false
            }
        }
    }

    private fun breakCombo() {
        if (currentCombo >= 5) {
            lastFinalStreak = currentCombo
            showComboComplete = true
            comboCompleteTimer = 3.0f
            pendingReward = calculateReward(currentCombo)
        }
        currentCombo = 0
        // Reset target if it was missed? The instructions say:
        // "Once the player reaches Combo x5: Target changes. The new target becomes: Beat your current combo high."
        // So target stays at best + 1 until hit.
    }

    private fun calculateReward(streak: Int): ComboReward? {
        val tier = tiers.findLast { streak >= it.minCombo }
        return tier?.rewards?.randomOrNull()
    }

    fun getWindowForCombo(combo: Int): Long {
        return when {
            combo >= 16 -> 2000L
            combo >= 11 -> 3000L
            combo >= 6 -> 3500L
            else -> 4000L
        }
    }

    fun reset() {
        currentCombo = 0
        bestComboThisRun = 0
        comboTarget = 5
        comboTimeRemaining = 0L
        pendingReward = null
        showComboComplete = false
    }
}
