package com.ashwathai.jump_droid

/**
 * Architecture for mission rewards.
 * Supports artifacts, powerups, modules, and achievements.
 * Migrated to support feature/mission-system rewards.
 */
sealed class MissionReward {
    data class Artifact(val discoveryType: DiscoveryType) : MissionReward()
    data class PowerUp(val type: PowerUpType, val amount: Int = 1) : MissionReward()
    data class Unlock(val rocketType: RocketType) : MissionReward()
    data class Achievement(val id: String) : MissionReward()
    data class ModuleUnlock(val moduleId: String) : MissionReward()
    data class Cash(val amount: Int) : MissionReward()
    
    object None : MissionReward()
}
