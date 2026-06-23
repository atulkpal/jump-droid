package com.example.jump_droid

import androidx.compose.runtime.mutableStateListOf

/**
 * Simplified Mission Manager.
 * Maintains 3 active mission tracks and tracks completion history.
 * Objective: Simple, Reliable, Trustworthy.
 */
class MissionManager {
    // Current active missions being tracked (max 3)
    val activeMissions = mutableStateListOf<Mission>()

    var onMissionCompleted: ((Mission) -> Unit)? = null
    
    // Tracks IDs of missions completed during this session to prevent repetition
    private val completedIdsInRun = mutableSetOf<String>()

    /**
     * Activates a mission template as a fresh instance.
     */
    fun activateMission(missionId: String) {
        MissionRegistry.getById(missionId)?.let { template ->
            if (activeMissions.none { it.id == missionId }) {
                val newInstance = Mission(
                    id = template.id,
                    name = template.name,
                    description = template.description,
                    type = template.type,
                    targetValue = template.targetValue,
                    reward = template.reward,
                    initialProgress = 0
                )
                activeMissions.add(newInstance)
                android.util.Log.d("MissionTruth", "MISSION ACTIVATED: ${newInstance.id} (Category: ${newInstance.type})")
            }
        }
    }

    /**
     * Updates progress for missions of a specific type.
     * Uses absoluteValue for metrics like Altitude (Exploration) or Timers (Survival).
     */
    fun updateProgress(type: MissionType, increment: Int = 1, absoluteValue: Int? = null, predicate: ((Mission) -> Boolean)? = null) {
        activeMissions.filter { it.type == type && !it.isCompleted && (predicate?.invoke(it) ?: true) }.forEach { mission ->
            val before = mission.currentProgress
            if (absoluteValue != null) {
                // Absolute metrics (like altitude) only move forward
                if (absoluteValue > mission.currentProgress) {
                    mission.currentProgress = absoluteValue
                }
            } else {
                mission.currentProgress += increment
            }
            
            if (before != mission.currentProgress) {
                android.util.Log.d("MissionTruth", "MISSION PROGRESS: ${mission.id} $before -> ${mission.currentProgress} (Target: ${mission.targetValue}) [Source: $type]")
            }

            if (mission.checkCompletion()) {
                completedIdsInRun.add(mission.id)
                onMissionCompleted?.invoke(mission)
                android.util.Log.i("MissionTruth", "MISSION COMPLETED: ${mission.id}")
            }
        }
    }

    /**
     * Explicitly completes a mission.
     */
    fun completeMission(missionId: String) {
        activeMissions.find { it.id == missionId }?.let {
            if (!it.isCompleted) {
                it.isCompleted = true
                completedIdsInRun.add(it.id)
                onMissionCompleted?.invoke(it)
                android.util.Log.i("MissionTruth", "MISSION EXPLICITLY COMPLETED: ${it.id}")
            }
        }
    }

    /**
     * Resets progress for active missions of a specific type (e.g., survival resets).
     */
    fun resetProgress(type: MissionType, predicate: ((Mission) -> Boolean)? = null) {
        activeMissions.filter { it.type == type && !it.isCompleted && (predicate?.invoke(it) ?: true) }.forEach {
            it.reset()
            android.util.Log.d("MissionTruth", "MISSION RESET: ${it.id}")
        }
    }

    /**
     * Strictly maintains one mission from each core track.
     */
    fun selectNextMission() {
        // 1. Remove missions only after they finished their replacement ceremony stage
        activeMissions.removeAll { it.ceremonyStage == CeremonyStage.REPLACING }

        // 2. Ensure each track is filled
        val coreTracks = listOf(MissionType.EXPLORATION, MissionType.PLATFORMING, MissionType.SURVIVAL)
        coreTracks.forEach { type ->
            if (activeMissions.none { it.type == type }) {
                val nextTemplate = MissionRegistry.getAllTemplates().find { template ->
                    template.type == type && !completedIdsInRun.contains(template.id)
                }
                
                nextTemplate?.let { 
                    android.util.Log.d("MissionTruth", "TRACK REFILL: Category $type -> Selecting ${it.id}")
                    activateMission(it.id) 
                }
            }
        }
    }

    /**
     * Resets the entire system for a new game run.
     */
    fun clear() {
        activeMissions.clear()
        completedIdsInRun.clear()
        android.util.Log.d("MissionTruth", "MISSION SYSTEM CLEARED")
    }
}
