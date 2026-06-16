package com.example.jump_droid

/**
 * Architecture for mission rewards.
 * Supports artifacts, powerups, unlocks, and achievements.
 */
sealed class MissionReward {
    data class Artifact(val discoveryType: DiscoveryType) : MissionReward()
    data class PowerUp(val type: PowerUpType, val amount: Int = 1) : MissionReward()
    data class Unlock(val rocketType: RocketType) : MissionReward()
    data class Achievement(val id: String) : MissionReward()
    
    // Placeholder for future expansion
    object None : MissionReward()
}
