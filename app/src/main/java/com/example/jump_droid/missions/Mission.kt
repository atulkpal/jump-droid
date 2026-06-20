package com.example.jump_droid.missions

enum class MissionCategory {
    FLIGHT_TIME, PLATFORM_STAY, NO_HEAT, FUEL_EFFICIENCY, COMBO_STREAK,
    BOSS_SLAYER, DISCOVERY_HUNTER, ALTITUDE_CLIMBER, MOMENTUM_MASTER,
    HAZARD_SURVIVOR, PERFECT_RUN, COLLECTOR, BOOST_CHAMPION, COMBO_PRO
}

enum class MissionTier(val displayName: String) {
    TIER_1("ROOKIE"), TIER_2("EXPERIENCED"), TIER_3("MASTER"), TIER_4("GOD")
}

enum class MissionState {
    LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED, CLAIMED
}

enum class ObjectiveType {
    TOTAL_FLIGHT_TIME, TOTAL_PLATFORM_TIME, ZERO_HEAT_TIME,
    FUEL_PICKUPS_COLLECTED, MAX_COMBO, BOSSES_DEFEATED,
    CODEX_UNLOCKED, MAX_ALTITUDE, MAX_MOMENTUM,
    HAZARD_HITS_SURVIVED, PERFECT_RUN_TIME, ARTIFACTS_COLLECTED,
    DASHES_PER_RUN, COMBO_MAINTAIN_TIME,
    OVERHEAT_COUNT, NEAR_DEATH_RUN, CONSECUTIVE_WINS
}

enum class UnlockType {
    REACH_ALTITUDE, DEFEAT_BOSS, COMPLETE_MISSION,
    UNLOCK_CODEX_ENTRY, REACH_BIOME, COLLECT_ARTIFACT
}

data class MissionObjective(
    val type: ObjectiveType,
    val targetValue: Float,
    val unit: String = "",
    val comboThreshold: Float? = null
)

data class Rewards(
    val cash: Int,
    val codexEntry: String? = null,
    val powerup: String? = null,
    val artifact: String? = null,
    val rocketSkin: String? = null,
    val unlockable: String? = null
)

data class UnlockCondition(
    val type: UnlockType,
    val value: Float,
    val missionId: String? = null
)

data class GameStats(
    val totalFlightTime: Float = 0f,
    val totalPlatformTime: Float = 0f,
    val zeroHeatTime: Float = 0f,
    val fuelPickupsCollected: Int = 0,
    val maxCombo: Int = 0,
    val currentCombo: Int = 0,
    val comboMaintainTime: Float = 0f,
    val bossesDefeated: Int = 0,
    val codexUnlocked: Int = 0,
    val maxAltitude: Float = 0f,
    val maxMomentum: Float = 0f,
    val hazardHitsSurvived: Int = 0,
    val perfectRunTime: Float = 0f,
    val artifactsCollected: Int = 0,
    val dashesPerRun: Int = 0,
    val overheatCount: Int = 0,
    val wasNearDeath: Boolean = false,
    val consecutiveWins: Int = 0
)

data class Mission(
    val id: String,
    val category: MissionCategory,
    val tier: MissionTier,
    val name: String,
    val description: String,
    val objective: MissionObjective,
    val rewards: Rewards,
    val unlockCondition: UnlockCondition? = null,
    val icon: String = "",
    val isHidden: Boolean = false
)
