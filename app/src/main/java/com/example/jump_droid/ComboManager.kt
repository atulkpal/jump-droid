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
    val breakRewards: List<ComboReward>,
    val survivalDrop: ComboReward? = null
)

data class FlyingReward(
    val type: ComboReward,
    var x: Float,
    var y: Float,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var scale: Float = 3.5f,
    var progress: Float = 0f // 0.0 to 1.0
)

class ComboManager {
    companion object {
        val TIERS = listOf(
            ComboTier(1, 5, 7, "BASIC", listOf(ComboReward.Fuel(50f)), ComboReward.PowerUp(PowerUpType.SHIELD_CAPSULE)),
            ComboTier(2, 8, 11, "IMPROVED", listOf(ComboReward.PowerUp(PowerUpType.TURBO_BOOSTER)), ComboReward.PowerUp(PowerUpType.SHIELD_CAPSULE)),
            ComboTier(3, 12, 15, "ADVANCED", listOf(ComboReward.AltitudeBoost), ComboReward.PowerUp(PowerUpType.HULL_REPAIR)),
            ComboTier(4, 16, 20, "ELITE", listOf(ComboReward.PowerUp(PowerUpType.ARTIFACT)), ComboReward.PowerUp(PowerUpType.HULL_REPAIR)),
            ComboTier(5, 21, 999, "LEGENDARY", listOf(ComboReward.Artifact(DiscoveryType.ART_RECORDER)))
        )
    }

    var currentCombo by mutableIntStateOf(0)
    var bestComboThisRun by mutableIntStateOf(0)
    var comboTimeRemaining by mutableLongStateOf(0L)
    var comboTarget by mutableIntStateOf(5)
    
    // Reward Presentation state
    var pendingReward by mutableStateOf<ComboReward?>(null)
    var lastFinalStreak by mutableIntStateOf(0)
    var showComboComplete by mutableStateOf(false)
    var comboCompleteTimer by mutableFloatStateOf(0f)
    var isNewHighReached by mutableStateOf(false)
    
    // Task 6: Survival Rewards
    val immediateSurvivalRewards = mutableListOf<ComboReward>()

    fun onLanding() {
        currentCombo++
        
        // Survival drops: check if we just entered a new tier
        TIERS.firstOrNull { currentCombo == it.minCombo }?.survivalDrop?.let {
            immediateSurvivalRewards.add(it)
        }
        // Post-legendary: every 5 combos past max tier
        if (currentCombo > (TIERS.lastOrNull()?.maxCombo ?: 25) && currentCombo % 5 == 0) {
            calculateReward(currentCombo)?.let { immediateSurvivalRewards.add(it) }
        }

        refreshTimer()
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
        isNewHighReached = false
        if (currentCombo > bestComboThisRun && currentCombo >= 5) {
            isNewHighReached = true
            lastFinalStreak = currentCombo
            showComboComplete = true
            comboCompleteTimer = 3.0f
            pendingReward = calculateReward(currentCombo)
            
            bestComboThisRun = currentCombo
            comboTarget = bestComboThisRun + 1
        }
        currentCombo = 0
    }

    private fun calculateReward(streak: Int): ComboReward? {
        val tier = TIERS.findLast { streak >= it.minCombo }
        return tier?.breakRewards?.randomOrNull()
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
        immediateSurvivalRewards.clear()
    }
}
