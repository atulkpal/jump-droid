package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class MissionCategory {
    FLIGHT_TIME, PLATFORM_STAY, NO_HEAT, FUEL_EFFICIENCY, COMBO_STREAK,
    BOSS_SLAYER, DISCOVERY_HUNTER, ALTITUDE_CLIMBER, MOMENTUM_MASTER,
    HAZARD_SURVIVOR, PERFECT_RUN, COLLECTOR, BOOST_CHAMPION, COMBO_PRO
}

enum class MissionTier(val displayName: String) {
    TIER_1("ROOKIE"), TIER_2("EXPERIENCED"), TIER_3("MASTER"), TIER_4("GOD")
}

enum class ObjectiveType {
    TOTAL_FLIGHT_TIME, TOTAL_PLATFORM_TIME, ZERO_HEAT_TIME,
    FUEL_PICKUPS_COLLECTED, MAX_COMBO, BOSSES_DEFEATED,
    CODEX_UNLOCKED, MAX_ALTITUDE, MAX_MOMENTUM,
    HAZARD_HITS_SURVIVED, PERFECT_RUN_TIME, ARTIFACTS_COLLECTED,
    DASHES_PER_RUN, COMBO_MAINTAIN_TIME,
    OVERHEAT_COUNT, NEAR_DEATH_RUN, CONSECUTIVE_WINS
}

enum class MissionUnlockType {
    REACH_ALTITUDE, DEFEAT_BOSS, COMPLETE_MISSION,
    UNLOCK_CODEX_ENTRY, REACH_BIOME, COLLECT_ARTIFACT
}

data class MissionUnlockCondition(
    val type: MissionUnlockType,
    val value: Float,
    val missionId: String? = null
)

enum class CeremonyStage {
    NONE,
    GLOW,
    REPLACING
}

/**
 * Enhanced data model for a Mission.
 * Migrated from feature/mission-system and aligned with EPIC 7.
 */
class Mission(
    val id: String,
    val name: String,
    val description: String,
    val type: MissionType,
    val category: MissionCategory,
    val tier: MissionTier,
    val targetValue: Int,
    val rewards: List<MissionReward>,
    val unlockCondition: MissionUnlockCondition? = null,
    val icon: String = "",
    val isHidden: Boolean = false,
    initialProgress: Int = 0
) {
    var currentProgress by mutableIntStateOf(initialProgress)
    var isUnlocked by mutableStateOf(unlockCondition == null)
    var isCompleted by mutableStateOf(false)
    var isClaimed by mutableStateOf(false)

    // UI Presentation State
    var ceremonyStage by mutableStateOf(CeremonyStage.NONE)

    /**
     * Checks if the mission goal has been met.
     */
    fun checkCompletion(): Boolean {
        if (!isCompleted && currentProgress >= targetValue) {
            isCompleted = true
            return true
        }
        return false
    }

    /**
     * Resets mission progress.
     */
    fun reset() {
        currentProgress = 0
        isCompleted = false
        isClaimed = false
        ceremonyStage = CeremonyStage.NONE
    }
}
