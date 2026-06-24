package com.example.jump_droid

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf

/**
 * Simplified Mission Manager.
 * Maintains 3 active mission tracks and tracks completion history.
 * Objective: Simple, Reliable, Trustworthy.
 */
class MissionManager(private val progressionManager: ProgressionManager) {
    // Current active missions being tracked (max 3)
    val activeMissions = mutableStateListOf<Mission>()

    // Lifetime tracking for all missions (The Intelligence Network)
    private val allMissionInstances = mutableStateMapOf<String, Mission>()

    var onMissionCompleted: ((Mission) -> Unit)? = null
    
    // Tracks IDs of missions completed during this session to prevent repetition
    private val completedIdsInRun = mutableSetOf<String>()

    // Ceremony timers: mission ID → elapsed time
    private val ceremonyTimers = mutableMapOf<String, Float>()

    fun startCeremony(missionId: String) {
        ceremonyTimers[missionId] = 0f
    }

    fun isInCeremony(missionId: String): Boolean = ceremonyTimers.containsKey(missionId)

    fun updateCeremonies(dt: Float): List<String> {
        val ended = mutableListOf<String>()
        ceremonyTimers.keys.toList().forEach { mid ->
            val newTime = (ceremonyTimers[mid] ?: 0f) + dt
            ceremonyTimers[mid] = newTime
            if (newTime >= 3.0f) {
                ceremonyTimers.remove(mid)
                ended.add(mid)
            }
        }
        return ended
    }

    fun clearCeremonies() {
        ceremonyTimers.clear()
    }

    /**
     * Audits unlock conditions for all missions.
     * Called whenever progression state changes.
     */
    fun checkUnlocks() {
        allMissionInstances.values.forEach { mission ->
            val condition = mission.unlockCondition ?: return@forEach
            
            val isUnlocked = when (condition.type) {
                MissionUnlockType.COMPLETE_MISSION -> {
                    val prereqId = condition.missionId ?: ""
                    allMissionInstances[prereqId]?.isCompleted == true
                }
                MissionUnlockType.REACH_ALTITUDE -> {
                    progressionManager.highScore >= condition.value
                }
                MissionUnlockType.DEFEAT_BOSS -> {
                    progressionManager.lifetimeBossesDefeated >= condition.value
                }
                MissionUnlockType.UNLOCK_CODEX_ENTRY -> {
                    progressionManager.getTotalDiscoveries() >= condition.value
                }
                MissionUnlockType.REACH_BIOME -> {
                    // Approximate by altitude for now
                    progressionManager.highScore >= 5000f
                }
                MissionUnlockType.COLLECT_ARTIFACT -> {
                    progressionManager.artifactsCollected.size >= condition.value
                }
            }

            if (isUnlocked) {
                mission.isUnlocked = true
            }
        }
    }

    init {
        // Initialize instances for all known templates
        MissionRegistry.getAllTemplates().forEach { template ->
            val mission = Mission(
                id = template.id,
                name = template.name,
                description = template.description,
                type = template.type,
                category = template.category,
                tier = template.tier,
                targetValue = template.targetValue,
                rewards = template.rewards,
                unlockCondition = template.unlockCondition,
                icon = template.icon,
                isHidden = template.isHidden,
                initialProgress = progressionManager.getMissionProgress(template.id)
            )

            // Sync with persistent state
            mission.isCompleted = progressionManager.completedMissionIds.contains(mission.id)
            mission.isClaimed = progressionManager.claimedMissionIds.contains(mission.id)

            allMissionInstances[template.id] = mission
        }
    }

    /**
     * Re-syncs mission states and progress with progression manager.
     */
    fun syncState() {
        allMissionInstances.values.forEach { mission ->
            mission.isCompleted = progressionManager.completedMissionIds.contains(mission.id)
            mission.isClaimed = progressionManager.claimedMissionIds.contains(mission.id)
            val savedProgress = progressionManager.getMissionProgress(mission.id)
            if (savedProgress > mission.currentProgress) {
                mission.currentProgress = savedProgress
            }
        }
    }

    /**
     * Updates progress for ALL missions based on current game state.
     * This is the heart of the Intelligence Network.
     */
    fun updateProgressAll(stats: GameStats) {
        allMissionInstances.values.forEach { mission ->
            if (mission.isCompleted) return@forEach

            val progress = calculateProgress(mission, stats)
            val before = mission.currentProgress
            
            // Only move forward (especially for altitude/cumulative stats)
            if (progress.toInt() > before) {
                mission.currentProgress = progress.toInt()
                progressionManager.saveMissionProgress(mission.id, mission.currentProgress)
                
                // If this mission is also in activeHUD, it will update automatically via state
                if (mission.checkCompletion()) {
                    mission.ceremonyStage = CeremonyStage.GLOW
                    completedIdsInRun.add(mission.id)
                    onMissionCompleted?.invoke(mission)
                    android.util.Log.i("IntelligenceNetwork", "MISSION COMPLETED: ${mission.id}")
                }
            }
        }
    }

    private fun calculateProgress(mission: Mission, stats: GameStats): Float {
        return when (mission.category) {
            MissionCategory.FLIGHT_TIME -> progressionManager.lifetimeFlightTime + stats.totalFlightTime
            MissionCategory.PLATFORM_STAY -> progressionManager.lifetimePlatformTime + stats.totalPlatformTime
            MissionCategory.NO_HEAT -> stats.zeroHeatTime
            MissionCategory.FUEL_EFFICIENCY -> stats.fuelPickupsCollected.toFloat()
            MissionCategory.COMBO_STREAK -> stats.maxCombo.toFloat()
            MissionCategory.BOSS_SLAYER -> stats.bossesDefeated.toFloat()
            MissionCategory.DISCOVERY_HUNTER -> stats.codexUnlocked.toFloat()
            MissionCategory.ALTITUDE_CLIMBER -> stats.maxAltitude
            MissionCategory.MOMENTUM_MASTER -> stats.maxMomentum
            MissionCategory.HAZARD_SURVIVOR -> stats.hazardHitsSurvived.toFloat()
            MissionCategory.PERFECT_RUN -> stats.perfectRunTime
            MissionCategory.COLLECTOR -> stats.artifactsCollected.toFloat()
            MissionCategory.BOOST_CHAMPION -> stats.dashesPerRun.toFloat()
            MissionCategory.COMBO_PRO -> stats.comboMaintainTime
        }
    }

    /**
     * Activates a mission template as a fresh instance into the HUD.
     */
    fun activateMission(missionId: String) {
        val template = allMissionInstances[missionId]
        if (template != null && activeMissions.none { it.id == missionId }) {
            activeMissions.add(template)
            android.util.Log.d("MissionHUD", "MISSION ACTIVATED TO HUD: ${template.id}")
        }
    }

    /**
     * Finalizes a mission and grants all associated rewards.
     */
    fun claimMissionRewards(missionId: String, progressionManager: ProgressionManager, player: Player) {
        val mission = allMissionInstances[missionId] ?: return
        if (mission.isCompleted && !mission.isClaimed) {
            mission.rewards.forEach { reward ->
                progressionManager.grantReward(reward, player)
            }
            mission.isClaimed = true
            progressionManager.saveMissionProgress(mission.id, mission.currentProgress)
            progressionManager.recordMissionClaim(mission.id)
            android.util.Log.i("MissionManager", "MISSION REWARDS CLAIMED: ${mission.id}")
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
                progressionManager.saveMissionProgress(mission.id, mission.currentProgress)
                android.util.Log.d("MissionTruth", "MISSION PROGRESS: ${mission.id} $before -> ${mission.currentProgress} (Target: ${mission.targetValue}) [Source: $type]")
            }

            if (mission.checkCompletion()) {
                mission.ceremonyStage = CeremonyStage.GLOW
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
            progressionManager.saveMissionProgress(it.id, 0)
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
                    template.type == type &&
                    !completedIdsInRun.contains(template.id) &&
                    allMissionInstances[template.id]?.isCompleted != true
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

    /**
     * Returns all mission instances for the grid UI.
     */
    fun getAllMissions(): List<Mission> = allMissionInstances.values.toList()
}
