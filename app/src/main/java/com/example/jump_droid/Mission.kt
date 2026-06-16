package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
    }
}
