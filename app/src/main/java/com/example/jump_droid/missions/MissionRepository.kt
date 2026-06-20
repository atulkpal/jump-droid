package com.example.jump_droid.missions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.missionDataStore: DataStore<Preferences> by preferencesDataStore(name = "missions")

class MissionRepository(private val context: Context) {

    private fun stateKey(id: String) = stringPreferencesKey("state_$id")
    private fun progressKey(id: String) = floatPreferencesKey("progress_$id")

    suspend fun saveMissionState(missionId: String, state: MissionState) {
        context.missionDataStore.edit { prefs ->
            prefs[stateKey(missionId)] = state.name
        }
    }

    suspend fun getMissionState(missionId: String): MissionState {
        return context.missionDataStore.data
            .map { prefs ->
                val name = prefs[stateKey(missionId)] ?: return@map MissionState.LOCKED
                try { MissionState.valueOf(name) } catch (_: Exception) { MissionState.LOCKED }
            }
            .first()
    }

    fun getMissionStateFlow(missionId: String): Flow<MissionState> {
        return context.missionDataStore.data.map { prefs ->
            val name = prefs[stateKey(missionId)] ?: return@map MissionState.LOCKED
            try { MissionState.valueOf(name) } catch (_: Exception) { MissionState.LOCKED }
        }
    }

    suspend fun saveProgress(missionId: String, progress: Float) {
        context.missionDataStore.edit { prefs ->
            prefs[progressKey(missionId)] = progress
        }
    }

    suspend fun getProgress(missionId: String): Float {
        return context.missionDataStore.data
            .map { prefs -> prefs[progressKey(missionId)] ?: 0f }
            .first()
    }

    fun getProgressFlow(missionId: String): Flow<Float> {
        return context.missionDataStore.data.map { prefs ->
            prefs[progressKey(missionId)] ?: 0f
        }
    }

    suspend fun saveAllStates(states: Map<String, MissionState>) {
        context.missionDataStore.edit { prefs ->
            states.forEach { (id, state) ->
                prefs[stateKey(id)] = state.name
            }
        }
    }

    suspend fun saveAllProgress(progress: Map<String, Float>) {
        context.missionDataStore.edit { prefs ->
            progress.forEach { (id, value) ->
                prefs[progressKey(id)] = value
            }
        }
    }

    suspend fun getAllStates(missionIds: List<String>): Map<String, MissionState> {
        val result = mutableMapOf<String, MissionState>()
        context.missionDataStore.data.first { prefs ->
            missionIds.forEach { id ->
                val name = prefs[stateKey(id)] ?: return@forEach
                try { result[id] = MissionState.valueOf(name) } catch (_: Exception) {}
            }
            true
        }
        return result
    }

    suspend fun resetMission(missionId: String) {
        context.missionDataStore.edit { prefs ->
            prefs.remove(stateKey(missionId))
            prefs.remove(progressKey(missionId))
        }
    }

    suspend fun resetAll() {
        context.missionDataStore.edit { it.clear() }
    }
}
