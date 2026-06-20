package com.example.jump_droid.missions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MissionManager(private val repository: MissionRepository) {

    private val allMissions = MissionRegistry.getAllMissions()

    private val _missionStates = mutableStateMapOf<String, MissionState>()
    private val _progress = mutableStateMapOf<String, Float>()

    var completionEvent by mutableStateOf<Mission?>(null)
        private set

    var isInitialized = false
        private set
    private var pendingStartRun = false

    suspend fun initialize() {
        val savedStates = repository.getAllStates(allMissions.map { it.id })
        _missionStates.clear()
        _progress.clear()
        allMissions.forEach { mission ->
            val saved = savedStates[mission.id]
            val defaultState = if (saved == null && mission.unlockCondition == null) MissionState.AVAILABLE else MissionState.LOCKED
            val state = saved ?: defaultState
            _missionStates[mission.id] = state
            _progress[mission.id] = repository.getProgress(mission.id)
        }
        checkInitialUnlocks()
        isInitialized = true
        if (pendingStartRun) { pendingStartRun = false; startRun() }
    }

    private suspend fun checkInitialUnlocks() {
        allMissions.forEach { mission ->
            val state = _missionStates[mission.id] ?: return@forEach
            if (state == MissionState.LOCKED) {
                checkUnlockCondition(mission)
            }
        }
    }

    fun startRun() {
        if (!isInitialized) { pendingStartRun = true; return }
        allMissions.forEach { mission ->
            val state = _missionStates[mission.id] ?: MissionState.LOCKED
            if (state == MissionState.AVAILABLE || state == MissionState.IN_PROGRESS) {
                _missionStates[mission.id] = MissionState.IN_PROGRESS
                val existing = _progress[mission.id] ?: 0f
                if (existing >= mission.objective.targetValue && mission.objective.targetValue > 0f) {
                    _missionStates[mission.id] = MissionState.COMPLETED
                } else {
                    _progress[mission.id] = 0f
                }
            }
        }
    }

    fun update(stats: GameStats): Mission? {
        allMissions.forEach { mission ->
            val state = _missionStates[mission.id]
            if (state != MissionState.IN_PROGRESS) return@forEach

            val progress = calculateProgress(mission, stats)
            _progress[mission.id] = progress

            if (progress >= mission.objective.targetValue && mission.objective.targetValue > 0f) {
                _missionStates[mission.id] = MissionState.COMPLETED
                completionEvent = mission
                return mission
            }
        }
        return null
    }

    private fun calculateProgress(mission: Mission, stats: GameStats): Float {
        return when (mission.objective.type) {
            ObjectiveType.TOTAL_FLIGHT_TIME -> stats.totalFlightTime
            ObjectiveType.TOTAL_PLATFORM_TIME -> stats.totalPlatformTime
            ObjectiveType.ZERO_HEAT_TIME -> stats.zeroHeatTime
            ObjectiveType.FUEL_PICKUPS_COLLECTED -> stats.fuelPickupsCollected.toFloat()
            ObjectiveType.MAX_COMBO -> stats.maxCombo.toFloat()
            ObjectiveType.BOSSES_DEFEATED -> stats.bossesDefeated.toFloat()
            ObjectiveType.CODEX_UNLOCKED -> stats.codexUnlocked.toFloat()
            ObjectiveType.MAX_ALTITUDE -> stats.maxAltitude
            ObjectiveType.MAX_MOMENTUM -> stats.maxMomentum
            ObjectiveType.HAZARD_HITS_SURVIVED -> stats.hazardHitsSurvived.toFloat()
            ObjectiveType.PERFECT_RUN_TIME -> stats.perfectRunTime
            ObjectiveType.ARTIFACTS_COLLECTED -> stats.artifactsCollected.toFloat()
            ObjectiveType.DASHES_PER_RUN -> stats.dashesPerRun.toFloat()
            ObjectiveType.COMBO_MAINTAIN_TIME -> {
                val threshold = mission.objective.comboThreshold ?: 0f
                if (stats.currentCombo >= threshold) stats.comboMaintainTime else 0f
            }
            ObjectiveType.OVERHEAT_COUNT -> stats.overheatCount.toFloat()
            ObjectiveType.NEAR_DEATH_RUN -> if (stats.wasNearDeath) 1f else 0f
            ObjectiveType.CONSECUTIVE_WINS -> stats.consecutiveWins.toFloat()
        }
    }

    fun endRun(scope: CoroutineScope) {
        val stateSnapshot = _missionStates.toMap()
        val progressSnapshot = _progress.toMap()
        scope.launch {
            stateSnapshot.forEach { (id, state) ->
                when (state) {
                    MissionState.IN_PROGRESS -> repository.saveMissionState(id, MissionState.AVAILABLE)
                    MissionState.COMPLETED -> repository.saveMissionState(id, MissionState.COMPLETED)
                    else -> {}
                }
                progressSnapshot[id]?.let { repository.saveProgress(id, it) }
            }
        }
        _missionStates.forEach { (id, state) ->
            if (state == MissionState.IN_PROGRESS) {
                _missionStates[id] = MissionState.AVAILABLE
            }
        }
        completionEvent = null
    }

    fun dismissCompletionEvent() {
        completionEvent = null
    }

    fun getVisibleMissions(): List<Mission> {
        return allMissions.filter { m ->
            val s = _missionStates[m.id]
            m.isHidden || s != MissionState.LOCKED
        }.sortedWith(compareBy<Mission> { it.tier.ordinal }.thenBy { it.category.name })
    }

    fun getMissionState(missionId: String): MissionState {
        return _missionStates[missionId] ?: MissionState.LOCKED
    }

    fun getProgress(missionId: String): Float {
        return _progress[missionId] ?: 0f
    }

    fun getProgressPercentage(missionId: String): Float {
        val mission = allMissions.find { it.id == missionId } ?: return 0f
        val progress = getProgress(missionId)
        return if (mission.objective.targetValue > 0f) {
            (progress / mission.objective.targetValue).coerceIn(0f, 1f)
        } else 0f
    }

    fun claimRewards(missionId: String, scope: CoroutineScope) {
        val current = _missionStates[missionId]
        if (current == MissionState.COMPLETED) {
            _missionStates[missionId] = MissionState.CLAIMED
            scope.launch { repository.saveMissionState(missionId, MissionState.CLAIMED) }
        } else if (current == MissionState.IN_PROGRESS) {
            val mission = allMissions.find { it.id == missionId } ?: return
            val pct = if (mission.objective.targetValue > 0f)
                (getProgress(missionId) / mission.objective.targetValue).coerceIn(0f, 1f) else 0f
            if (pct >= 1f) {
                _missionStates[missionId] = MissionState.CLAIMED
                scope.launch { repository.saveMissionState(missionId, MissionState.CLAIMED) }
            }
        }
    }

    fun getCompletedCount(): Int {
        return _missionStates.count { it.value == MissionState.COMPLETED || it.value == MissionState.CLAIMED }
    }

    fun getTotalVisibleCount(): Int {
        return getVisibleMissions().size
    }

    fun getCompletionPercentage(): Float {
        val total = getTotalVisibleCount()
        return if (total > 0) getCompletedCount().toFloat() / total else 0f
    }

    fun isMissionComplete(missionId: String): Boolean {
        val state = _missionStates[missionId]
        return state == MissionState.COMPLETED || state == MissionState.CLAIMED
    }

    fun getCompletedMissionIds(): Set<String> {
        return _missionStates.filter { it.value == MissionState.COMPLETED || it.value == MissionState.CLAIMED }.keys
    }

    fun hasUnclaimedRewards(): Boolean {
        return _missionStates.any { it.value == MissionState.COMPLETED }
    }

    fun hasNewlyUnlockedHidden(): Boolean {
        return _missionStates.any { (id, state) ->
            val mission = MissionRegistry.getMissionById(id) ?: return@any false
            mission.isHidden && state != MissionState.LOCKED && state != MissionState.CLAIMED
        }
    }

    private suspend fun checkUnlockCondition(mission: Mission) {
        val condition = mission.unlockCondition ?: return
        val unlocked = when (condition.type) {
            UnlockType.COMPLETE_MISSION -> {
                val prereqId = condition.missionId ?: return
                isMissionComplete(prereqId)
            }
            UnlockType.REACH_ALTITUDE -> {
                getPersistentMaxAltitude() >= condition.value
            }
            UnlockType.DEFEAT_BOSS -> {
                getPersistentBossesDefeated() >= condition.value.toInt()
            }
            UnlockType.UNLOCK_CODEX_ENTRY -> {
                getPersistentCodexUnlocked() >= condition.value.toInt()
            }
            UnlockType.REACH_BIOME -> {
                getPersistentMaxAltitude() >= 5000f
            }
            UnlockType.COLLECT_ARTIFACT -> {
                getPersistentArtifactsCollected() >= condition.value.toInt()
            }
        }
        if (unlocked) {
            _missionStates[mission.id] = MissionState.AVAILABLE
            repository.saveMissionState(mission.id, MissionState.AVAILABLE)
        }
    }

    private suspend fun getPersistentMaxAltitude(): Float {
        return repository.getProgress("_meta_max_altitude")
    }

    private suspend fun getPersistentBossesDefeated(): Int {
        return repository.getProgress("_meta_bosses").toInt()
    }

    private suspend fun getPersistentCodexUnlocked(): Int {
        return repository.getProgress("_meta_codex").toInt()
    }

    private suspend fun getPersistentArtifactsCollected(): Int {
        return repository.getProgress("_meta_artifacts").toInt()
    }

    fun saveMetaProgress(scope: CoroutineScope, stats: GameStats) {
        scope.launch {
            if (stats.maxAltitude > 0f) {
                val current = repository.getProgress("_meta_max_altitude")
                if (stats.maxAltitude > current) {
                    repository.saveProgress("_meta_max_altitude", stats.maxAltitude)
                }
            }
        }
    }
}
