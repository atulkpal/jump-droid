package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

class MissionTracker(private val sharedPrefs: SharedPreferences) {
    companion object {
        private const val PROGRESS_PREFIX = "mission_progress_"
    }

    var completedMissionIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var claimedMissionIds by mutableStateOf<Set<String>>(emptySet())
        private set

    init {
        loadMissions()
    }

    private fun loadMissions() {
        completedMissionIds = sharedPrefs.getStringSet("completed_missions", emptySet()) ?: emptySet()
        claimedMissionIds = sharedPrefs.getStringSet("claimed_missions", emptySet()) ?: emptySet()
    }

    fun saveMissionProgress(missionId: String, progress: Int) {
        val prev = sharedPrefs.getInt("$PROGRESS_PREFIX$missionId", 0)
        if (progress != prev) {
            sharedPrefs.edit { putInt("$PROGRESS_PREFIX$missionId", progress) }
        }
    }

    fun getMissionProgress(missionId: String): Int {
        return sharedPrefs.getInt("$PROGRESS_PREFIX$missionId", 0)
    }

    fun recordMissionCompletion(missionId: String): Int {
        if (completedMissionIds.contains(missionId)) return completedMissionIds.size
        completedMissionIds = completedMissionIds + missionId
        sharedPrefs.edit {
            putStringSet("completed_missions", completedMissionIds)
            putInt("missions_completed", completedMissionIds.size)
        }
        return completedMissionIds.size
    }

    fun recordMissionClaim(missionId: String) {
        if (claimedMissionIds.contains(missionId)) return
        claimedMissionIds = claimedMissionIds + missionId
        sharedPrefs.edit { putStringSet("claimed_missions", claimedMissionIds) }
    }

    fun saveUnlockedMissionIds(ids: Set<String>) {
        sharedPrefs.edit { putStringSet("unlocked_missions", ids) }
    }

    fun getUnlockedMissionIds(): Set<String> {
        return sharedPrefs.getStringSet("unlocked_missions", emptySet()) ?: emptySet()
    }

    fun syncMissions(completed: Set<String>, claimed: Set<String>) {
        completedMissionIds = completed
        claimedMissionIds = claimed
    }

    fun clear() {
        completedMissionIds = emptySet()
        claimedMissionIds = emptySet()
    }
}
