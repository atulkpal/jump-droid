package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class CeremonyStage {
    NONE,
    GLOW,
    COMPLETED_TEXT,
    REWARD_SPAWNED,
    REPLACING
}

/**
 * Simplified data model for a mission.
 * Tracks progress and completion status.
 */
class Mission(
    val id: String,
    val name: String,
    val description: String,
    val type: MissionType,
    val targetValue: Int,
    val reward: MissionReward = MissionReward.None,
    initialProgress: Int = 0
) {
    var currentProgress by mutableIntStateOf(initialProgress)
    var isCompleted by mutableStateOf(false)

    // UI Presentation State
    var ceremonyStage by mutableStateOf(CeremonyStage.NONE)
    var isNew by mutableStateOf(true) // For introduction card

    /**
     * Checks if the mission goal has been met.
     */
    fun checkCompletion(): Boolean {
        if (!isCompleted && currentProgress >= targetValue) {
            isCompleted = true
            ceremonyStage = CeremonyStage.GLOW
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
        ceremonyStage = CeremonyStage.NONE
    }
}
