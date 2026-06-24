package com.example.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ArtifactRecord(
    val name: String,
    val firstDiscoveryDate: String,
    val timesFound: Int,
    val highestAltitude: Int,
    val zoneFound: String
)

enum class AscensionRank(val title: String, val level: Int) {
    EXPLORER_I("Explorer Rank I", 1),
    EXPLORER_II("Explorer Rank II", 2),
    EXPLORER_III("Explorer Rank III", 3),
    EXPLORER_IV("Explorer Rank IV", 4),
    EXPLORER_V("Explorer Rank V", 5)
}

/**
 * Manages permanent account progression, artifact collection, and ranks.
 */
class ProgressionManager(private val sharedPrefs: SharedPreferences) : ProgressionService {

    companion object {
        private const val PROGRESS_PREFIX = "mission_progress_"
    }

    override var artifactsCollected by mutableStateOf<Map<String, ArtifactRecord>>(emptyMap())
        private set

    var ownedModuleIds by mutableStateOf<Set<String>>(emptySet())
        private set

    override var completedMissionIds by mutableStateOf<Set<String>>(emptySet())
        private set

    override var claimedMissionIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var currentRank by mutableStateOf(AscensionRank.EXPLORER_I)
        private set

    var permanentMaxIntegrity by mutableFloatStateOf(Constants.BASE_INTEGRITY)
        private set

    var permanentMaxShield by mutableFloatStateOf(Constants.BASE_SHIELD)
        private set

    val missionsCompleted: Int get() = completedMissionIds.size

    override var highScore by mutableIntStateOf(0)
        internal set

    // --- Lifetime Stats (Intelligence Network) ---
    override var lifetimeFlightTime by mutableFloatStateOf(0f)
        private set
    override var lifetimePlatformTime by mutableFloatStateOf(0f)
        private set
    override var lifetimeBossesDefeated by mutableIntStateOf(0)
        private set
    var lifetimeArtifactsCollected by mutableIntStateOf(0)
        private set
    var lifetimeHazardHitsSurvived by mutableIntStateOf(0)
        private set
    var lifetimeMissionsCompleted by mutableIntStateOf(0)
        private set

    init {
        loadProgression()
    }

    private fun loadProgression() {
        highScore = sharedPrefs.getInt("highScore", 0)
        
        // Load Lifetime Stats
        lifetimeFlightTime = sharedPrefs.getFloat("stat_lifetime_flight_time", 0f)
        lifetimePlatformTime = sharedPrefs.getFloat("stat_lifetime_platform_time", 0f)
        lifetimeBossesDefeated = sharedPrefs.getInt("stat_lifetime_bosses", 0)
        lifetimeArtifactsCollected = sharedPrefs.getInt("stat_lifetime_artifacts", 0)
        lifetimeHazardHitsSurvived = sharedPrefs.getInt("stat_lifetime_hazards", 0)
        lifetimeMissionsCompleted = sharedPrefs.getInt("missions_completed", 0)

        val artifactTypes = DiscoveryType.values().filter { it.category == "ARTIFACTS" }
        val loadedArtifacts = mutableMapOf<String, ArtifactRecord>()
        
        artifactTypes.forEach { type ->
            val keyBase = "art_${type.name}"
            if (sharedPrefs.contains("${keyBase}_date")) {
                loadedArtifacts[type.name] = ArtifactRecord(
                    name = type.title,
                    firstDiscoveryDate = sharedPrefs.getString("${keyBase}_date", "") ?: "",
                    timesFound = sharedPrefs.getInt("${keyBase}_count", 0),
                    highestAltitude = sharedPrefs.getInt("${keyBase}_alt", 0),
                    zoneFound = sharedPrefs.getString("${keyBase}_zone", "") ?: ""
                )
            }
        }
        artifactsCollected = loadedArtifacts
        
        ownedModuleIds = sharedPrefs.getStringSet("owned_modules", emptySet()) ?: emptySet()
        completedMissionIds = sharedPrefs.getStringSet("completed_missions", emptySet()) ?: emptySet()
        claimedMissionIds = sharedPrefs.getStringSet("claimed_missions", emptySet()) ?: emptySet()

        permanentMaxIntegrity = sharedPrefs.getFloat("max_integrity", Constants.BASE_INTEGRITY)
        permanentMaxShield = sharedPrefs.getFloat("max_shield", Constants.BASE_SHIELD)

        updateRank()
    }

    fun recordArtifactDiscovery(type: DiscoveryType, altitude: Int, zone: AltitudeZone) {
        val name = type.name
        val existing = artifactsCollected[name]
        
        val newRecord = if (existing == null) {
            ArtifactRecord(
                name = type.title,
                firstDiscoveryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                timesFound = 1,
                highestAltitude = altitude,
                zoneFound = zone.name.lowercase().replace("_", " ").capitalize(Locale.getDefault())
            )
        } else {
            existing.copy(
                timesFound = existing.timesFound + 1,
                highestAltitude = maxOf(existing.highestAltitude, altitude)
            )
        }
        
        val keyBase = "art_${name}"
        sharedPrefs.edit {
            putString("${keyBase}_date", newRecord.firstDiscoveryDate)
            putInt("${keyBase}_count", newRecord.timesFound)
            putInt("${keyBase}_alt", newRecord.highestAltitude)
            putString("${keyBase}_zone", newRecord.zoneFound)
        }
        
        artifactsCollected = artifactsCollected + (name to newRecord)
        updateRank()
    }

    fun grantModule(moduleId: String): Boolean {
        if (ownedModuleIds.contains(moduleId)) return false
        ownedModuleIds = ownedModuleIds + moduleId
        sharedPrefs.edit {
            putStringSet("owned_modules", ownedModuleIds)
        }
        return true
    }

    fun isModuleOwned(moduleId: String): Boolean {
        return ownedModuleIds.contains(moduleId)
    }

    override fun saveMissionProgress(missionId: String, progress: Int) {
        val prev = sharedPrefs.getInt("$PROGRESS_PREFIX$missionId", 0)
        if (progress != prev) {
            sharedPrefs.edit { putInt("$PROGRESS_PREFIX$missionId", progress) }
        }
    }

    override fun getMissionProgress(missionId: String): Int {
        return sharedPrefs.getInt("$PROGRESS_PREFIX$missionId", 0)
    }

    fun recordMissionCompletion(missionId: String) {
        if (completedMissionIds.contains(missionId)) return
        completedMissionIds = completedMissionIds + missionId
        sharedPrefs.edit {
            putStringSet("completed_missions", completedMissionIds)
            putInt("missions_completed", completedMissionIds.size)
        }
        
        lifetimeMissionsCompleted = completedMissionIds.size
    }

    override fun recordMissionClaim(missionId: String) {
        if (claimedMissionIds.contains(missionId)) return
        claimedMissionIds = claimedMissionIds + missionId
        sharedPrefs.edit { putStringSet("claimed_missions", claimedMissionIds) }
    }

    /**
     * Updates lifetime statistics from session results.
     */
    fun commitSessionStats(stats: GameStats) {
        lifetimeFlightTime += stats.totalFlightTime
        lifetimePlatformTime += stats.totalPlatformTime
        lifetimeBossesDefeated += stats.bossesDefeated
        lifetimeArtifactsCollected += stats.artifactsCollected
        lifetimeHazardHitsSurvived += stats.hazardHitsSurvived
        
        sharedPrefs.edit {
            putFloat("stat_lifetime_flight_time", lifetimeFlightTime)
            putFloat("stat_lifetime_platform_time", lifetimePlatformTime)
            putInt("stat_lifetime_bosses", lifetimeBossesDefeated)
            putInt("stat_lifetime_artifacts", lifetimeArtifactsCollected)
            putInt("stat_lifetime_hazards", lifetimeHazardHitsSurvived)
            putInt("missions_completed", lifetimeMissionsCompleted)
        }
    }

    /**
     * Grants a mission reward to the player's permanent account.
     */
    override fun grantReward(reward: MissionReward, player: Player) {
        when (reward) {
            is MissionReward.Artifact -> {
                // Record discovery (Altitude/Zone approximated)
                recordArtifactDiscovery(reward.discoveryType, 0, AltitudeZone.EARTH)
            }
            is MissionReward.PowerUp -> {
                // Instantly grant in-run benefit (handled in GameScreen completion callback usually)
                // For Phase 3, we focus on permanent progression
            }
            is MissionReward.Unlock -> {
                sharedPrefs.edit { putBoolean("unlock_${reward.rocketType.name}", true) }
            }
            is MissionReward.Achievement -> {
                sharedPrefs.edit { putBoolean("achievement_${reward.id}", true) }
            }
            is MissionReward.ModuleUnlock -> {
                grantModule(reward.moduleId)
            }
            is MissionReward.Cash -> {
                // Cash system placeholder
            }
            is MissionReward.None -> {}
        }
    }

    fun updateRank() {
        val totalDiscoveries = DiscoveryType.values().count { sharedPrefs.getBoolean("discovery_$it", false) }
        val artifactCount = artifactsCollected.size
        val zoneCount = AltitudeZone.values().count { sharedPrefs.getBoolean("discovery_AREA_${it.name}", false) }
        
        // Progression score calculation
        val score = totalDiscoveries + (artifactCount * 3) + (zoneCount * 5)
        
        currentRank = when {
            score >= 60 -> AscensionRank.EXPLORER_V
            score >= 40 -> AscensionRank.EXPLORER_IV
            score >= 25 -> AscensionRank.EXPLORER_III
            score >= 10 -> AscensionRank.EXPLORER_II
            else -> AscensionRank.EXPLORER_I
        }
    }
    
    fun getCompletionStats(category: String): Pair<Int, Int> {
        val allInCategory = DiscoveryType.values().filter { it.category == category }
        val discoveredCount = allInCategory.count { sharedPrefs.getBoolean("discovery_$it", false) }
        return discoveredCount to allInCategory.size
    }

    fun getTotalCompletionPercentage(): Int {
        val total = DiscoveryType.values().size
        val discovered = DiscoveryType.values().count { sharedPrefs.getBoolean("discovery_$it", false) }
        return if (total > 0) (discovered * 100) / total else 0
    }

    override fun getTotalDiscoveries(): Int {
        return DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) }
    }

    /**
     * Wipes all progression data.
     */
    fun wipeData() {
        sharedPrefs.edit { clear() }
        highScore = 0
        artifactsCollected = emptyMap()
        ownedModuleIds = emptySet()
        completedMissionIds = emptySet()
        claimedMissionIds = emptySet()
        lifetimeFlightTime = 0f
        lifetimePlatformTime = 0f
        lifetimeBossesDefeated = 0
        lifetimeArtifactsCollected = 0
        lifetimeHazardHitsSurvived = 0
        lifetimeMissionsCompleted = 0
        currentRank = AscensionRank.EXPLORER_I
        permanentMaxIntegrity = Constants.BASE_INTEGRITY
        permanentMaxShield = Constants.BASE_SHIELD
    }

    /**
     * Persists a new high score if it exceeds the current one.
     */
    fun saveHighScore(newScore: Int): Boolean {
        if (newScore > highScore) {
            highScore = newScore
            sharedPrefs.edit { putInt("highScore", newScore) }
            return true
        }
        return false
    }

    /**
     * Audits achievements and rocket unlocks based on current run stats.
     */
    fun checkUnlocks(
        score: Int,
        player: Player,
        onRocketUnlock: (RocketType) -> Unit,
        onAchievementUnlock: (Achievement) -> Unit,
        onLoreDiscovery: (DiscoveryType) -> Unit,
        onModuleUnlock: (Module) -> Unit
    ) {
        // 1. Rocket Unlocks
        RocketType.entries.forEach { type ->
            if (score >= type.unlockScore && !sharedPrefs.getBoolean("unlock_${type.name}", false)) {
                sharedPrefs.edit { putBoolean("unlock_${type.name}", true) }
                onRocketUnlock(type)
            }
        }

        // 2. Module Auto-Unlocks
        ModuleRegistry.getAll().forEach { module ->
            if (!isModuleOwned(module.id)) {
                val req = module.unlockRequirement
                val met = when (req.type) {
                    UnlockType.SCORE -> score >= req.threshold
                    UnlockType.ALTITUDE -> score >= req.threshold
                    UnlockType.ARTIFACT -> artifactsCollected.size >= req.threshold
                    UnlockType.DISCOVERY -> {
                        val totalDisc = DiscoveryType.values().count { sharedPrefs.getBoolean("discovery_$it", false) }
                        if (module.id == "MOD_SHIELD_FAST_RECHARGE") {
                            sharedPrefs.getBoolean("discovery_SHIELD_CAPSULE", false)
                        } else {
                            totalDisc >= req.threshold
                        }
                    }
                    UnlockType.MISSION -> missionsCompleted >= req.threshold
                }

                if (met) {
                    grantModule(module.id)
                    onModuleUnlock(module)
                }
            }
        }

        // 3. Lore Discoveries (Score-based)
        if (score >= 0) onLoreDiscovery(player.rocketType.discovery)
        if (score >= 100) onLoreDiscovery(DiscoveryType.LORE_ASCENSION)
        if (score >= 5000) onLoreDiscovery(DiscoveryType.LORE_SIGNAL)
        if (score >= 10000) onLoreDiscovery(DiscoveryType.LORE_LOST_FLEET)
        if (score >= 20000) onLoreDiscovery(DiscoveryType.LORE_LOGS)

        // 3. Achievements
        AchievementsList.forEach { achievement ->
            if (!sharedPrefs.getBoolean("achievement_${achievement.id}", false)) {
                if (achievement.unlockCondition(score, player.maxComboReached, player.totalOverheats)) {
                    sharedPrefs.edit { putBoolean("achievement_${achievement.id}", true) }
                    onAchievementUnlock(achievement)
                }
            }
        }
    }
}

private fun String.capitalize(locale: Locale): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
