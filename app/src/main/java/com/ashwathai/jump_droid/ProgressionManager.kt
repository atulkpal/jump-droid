package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import java.util.Locale

// ArtifactRecord + AscensionRank declared in ArtifactManager.kt

/**
 * Manages permanent account progression, artifact collection, and ranks.
 */
class ProgressionManager(private val sharedPrefs: SharedPreferences) : ProgressionService {

    private val missionTracker = MissionTracker(sharedPrefs)
    private val statRecorder = StatRecorder(sharedPrefs)
    private val artifactManager = ArtifactManager(sharedPrefs)
    private val unlockService = UnlockService(sharedPrefs)

    companion object {
        private const val PROGRESS_PREFIX = "mission_progress_"
    }

    override val artifactsCollected: Map<String, ArtifactRecord>
        get() = artifactManager.artifactsCollected

    var ownedModuleIds by mutableStateOf<Set<String>>(emptySet())
        private set

    override val completedMissionIds: Set<String>
        get() = missionTracker.completedMissionIds

    override val claimedMissionIds: Set<String>
        get() = missionTracker.claimedMissionIds

    var onModuleUnlocked: ((Module) -> Unit)? = null

    var onLoreLogDiscovered: ((LoreLog) -> Unit)? = null

    var onBlueprintUnlocked: ((BlueprintType) -> Unit)? = null

    val currentRank: AscensionRank
        get() = artifactManager.currentRank

    val activeSetBonuses: List<ArtifactBonus>
        get() = artifactManager.activeSetBonuses

    val currentMasteryPoints: Int
        get() = artifactManager.currentMasteryPoints

    val nextRankThreshold: Int
        get() = artifactManager.nextRankThreshold

    var permanentMaxIntegrity by mutableFloatStateOf(Constants.BASE_INTEGRITY)
        private set

    var permanentMaxShield by mutableFloatStateOf(Constants.BASE_SHIELD)
        private set

    var ascensionPrestigeLevel by mutableIntStateOf(0)
        private set

    var totalCash by mutableIntStateOf(0)
        private set

    var unlockedTrailIds by mutableStateOf<Set<String>>(setOf("plasma_cyan"))
        private set

    var unlockedPaintIds by mutableStateOf<Set<String>>(setOf("stock"))
        private set

    var creditBalance by mutableIntStateOf(0)
        private set

    var maxCredits by mutableIntStateOf(10)
        private set

    var totalMiniBossesKilled by mutableIntStateOf(0)
        private set

    var totalBossesKilled by mutableIntStateOf(0)
        private set

    var unlockedMusicTracks by mutableStateOf<Set<String>>(emptySet())
        private set

    var isZenModeUnlocked by mutableStateOf(false)
        private set

    var isMultiplayerUnlocked by mutableStateOf(false)
        private set

    val statRecord get() = statRecorder

    val missionsCompleted: Int get() = missionTracker.completedMissionIds.size

    fun getCashBalance(): Int = totalCash

    fun spendCash(amount: Int): Boolean {
        if (totalCash >= amount) {
            totalCash -= amount
            statRecorder.recordCashSpent(amount)
            sharedPrefs.edit { putInt("total_cash", totalCash) }
            return true
        }
        return false
    }

    fun unlockTrail(id: String) {
        unlockedTrailIds = unlockedTrailIds + id
        sharedPrefs.edit { putStringSet("unlocked_trails", unlockedTrailIds) }
    }

    fun isTrailUnlocked(id: String): Boolean = unlockedTrailIds.contains(id)

    fun unlockPaint(id: String) {
        unlockedPaintIds = unlockedPaintIds + id
        sharedPrefs.edit { putStringSet("unlocked_paints", unlockedPaintIds) }
    }

    fun isPaintUnlocked(id: String): Boolean = unlockedPaintIds.contains(id)

    fun addCredits(amount: Int): Int {
        val added = minOf(amount, maxCredits - creditBalance)
        if (added > 0) {
            creditBalance += added
            sharedPrefs.edit { putInt("credit_balance", creditBalance) }
        }
        return added
    }

    fun spendCredit(): Boolean {
        if (creditBalance > 0) {
            creditBalance--
            sharedPrefs.edit { putInt("credit_balance", creditBalance) }
            return true
        }
        return false
    }

    fun cashToCredits(cashAmount: Int): Int {
        val cashPerCredit = getCurrentCreditRate()

        if (cashAmount < cashPerCredit) return 0
        val maxBuy = cashAmount / cashPerCredit
        val available = maxCredits - creditBalance
        val actualBuy = minOf(maxBuy, available)
        if (actualBuy > 0) {
            val totalSpent = actualBuy * cashPerCredit
            totalCash -= totalSpent
            statRecorder.recordCashSpent(totalSpent)
            creditBalance += actualBuy
            sharedPrefs.edit {
                putInt("total_cash", totalCash)
                putInt("credit_balance", creditBalance)
            }
        }
        return actualBuy
    }

    fun getCurrentCreditRate(): Int {
        val bosses = lifetimeBossesDefeated
        return when {
            bosses >= 15 -> 2000
            bosses >= 10 -> 1000
            bosses >= 5 -> 500
            else -> 250
        }
    }

    override var highScore by mutableIntStateOf(0)
        internal set

    override var highAltitude by mutableIntStateOf(0)
        private set

    // --- Lifetime Stats (Delegated) ---
    override val lifetimeFlightTime: Float get() = statRecorder.lifetimeFlightTime
    override val lifetimePlatformTime: Float get() = statRecorder.lifetimePlatformTime
    override val lifetimeHazards: Int get() = statRecorder.lifetimeHazards
    override val lifetimeArtifacts: Int get() = statRecorder.lifetimeArtifacts
    override val lifetimeLandings: Int get() = statRecorder.lifetimeLandings
    override val lifetimeBossesDefeated: Int get() = statRecorder.lifetimeBossesDefeated
    val lifetimeMissionsCompleted: Int get() = statRecorder.lifetimeMissionsCompleted

    init {
        loadProgression()
    }

    private fun loadProgression() {
        highScore = sharedPrefs.getInt("highScore", 0)
        highAltitude = sharedPrefs.getInt("highAltitude", 0)
        ascensionPrestigeLevel = sharedPrefs.getInt("ascension_prestige", 0)
        totalCash = sharedPrefs.getInt("total_cash", 0)
        unlockedTrailIds = sharedPrefs.getStringSet("unlocked_trails", setOf("plasma_cyan")) ?: setOf("plasma_cyan")
        unlockedPaintIds = sharedPrefs.getStringSet("unlocked_paints", setOf("stock")) ?: setOf("stock")
        creditBalance = sharedPrefs.getInt("credit_balance", 0)

        ownedModuleIds = sharedPrefs.getStringSet("owned_modules", emptySet()) ?: emptySet()

        permanentMaxIntegrity = sharedPrefs.getFloat("max_integrity", Constants.BASE_INTEGRITY)
        permanentMaxShield = sharedPrefs.getFloat("max_shield", Constants.BASE_SHIELD)

        totalMiniBossesKilled = sharedPrefs.getInt("total_mini_bosses", 0)
        totalBossesKilled = sharedPrefs.getInt("total_bosses", 0)
        unlockedMusicTracks = sharedPrefs.getStringSet("unlocked_music", emptySet()) ?: emptySet()
        isZenModeUnlocked = sharedPrefs.getBoolean("zen_unlocked", false)
        isMultiplayerUnlocked = sharedPrefs.getBoolean("multiplayer_unlocked", false)

        artifactManager.reevaluateSetBonuses()
    }

    fun reevaluateSetBonuses() {
        artifactManager.reevaluateSetBonuses()
    }

    // --- Set Bonus Calculation Helpers ---
    fun getFuelRegenMultiplier(): Float {
        return artifactManager.getFuelRegenMultiplier(ascensionPrestigeLevel)
    }

    fun getShieldRegenMultiplier(): Float {
        return artifactManager.getShieldRegenMultiplier(ascensionPrestigeLevel)
    }

    fun getHeatCooldownMultiplier(): Float {
        return artifactManager.getHeatCooldownMultiplier(ascensionPrestigeLevel)
    }

    fun getThrustMultiplier(): Float {
        return artifactManager.getThrustMultiplier(ascensionPrestigeLevel)
    }

    fun getHullBonusAmount(): Float {
        return artifactManager.getHullBonusAmount()
    }

    fun getGlobalEfficiencyMultiplier(): Float {
        return artifactManager.getGlobalEfficiencyMultiplier(ascensionPrestigeLevel)
    }

    fun recordArtifactDiscovery(type: DiscoveryType, altitude: Int, zone: AltitudeZone) {
        artifactManager.recordArtifactDiscovery(type, altitude, zone)
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

    fun syncModules(ids: Set<String>) {
        ownedModuleIds = ids
    }

    override fun saveMissionProgress(missionId: String, progress: Int) {
        missionTracker.saveMissionProgress(missionId, progress)
    }

    override fun getMissionProgress(missionId: String): Int {
        return missionTracker.getMissionProgress(missionId)
    }

    fun recordMissionCompletion(missionId: String) {
        missionTracker.recordMissionCompletion(missionId)
    }

    override fun recordMissionClaim(missionId: String) {
        missionTracker.recordMissionClaim(missionId)
    }

    override fun saveUnlockedMissionIds(ids: Set<String>) {
        missionTracker.saveUnlockedMissionIds(ids)
    }

    override fun getUnlockedMissionIds(): Set<String> {
        return missionTracker.getUnlockedMissionIds()
    }

    override fun isDiscoveryUnlocked(discoveryName: String): Boolean {
        return sharedPrefs.getBoolean("discovery_$discoveryName", false)
    }

    override fun saveDiscoveredLog(logId: String) {
        unlockService.saveDiscoveredLog(logId)
    }

    override fun getDiscoveredLogs(): Set<String> {
        return unlockService.getDiscoveredLogs()
    }

    override fun saveUnlockedBlueprint(id: String) {
        unlockService.saveUnlockedBlueprint(id)
    }

    override fun getUnlockedBlueprints(): Set<String> {
        return unlockService.getUnlockedBlueprints()
    }

    /**
     * Updates lifetime statistics from session results.
     */
    fun commitSessionStats(stats: GameStats) {
        statRecorder.commitSessionStats(stats, missionsCompleted)

        totalMiniBossesKilled += stats.miniBossesDefeated
        totalBossesKilled += stats.bossesDefeated

        sharedPrefs.edit {
            putInt("total_mini_bosses", totalMiniBossesKilled)
            putInt("total_bosses", totalBossesKilled)
        }

        checkMusicUnlocks()
        checkZenModeUnlock(stats)
        checkMultiplayerUnlock(stats)
    }

    private fun checkMusicUnlocks() {
        val newUnlocks = mutableSetOf<String>()

        // Boss BGM: 20 mini-bosses, 10 bosses
        if (totalMiniBossesKilled >= 20 && totalBossesKilled >= 10 && !unlockedMusicTracks.contains("bgm_boss")) {
            newUnlocks.add("bgm_boss")
        }

        if (newUnlocks.isNotEmpty()) {
            unlockedMusicTracks = unlockedMusicTracks + newUnlocks
            sharedPrefs.edit { putStringSet("unlocked_music", unlockedMusicTracks) }
        }
    }

    private fun checkZenModeUnlock(stats: GameStats) {
        if (isZenModeUnlocked) return

        // Requirements: 10,000m Altitude + 5 Bosses + 5 Combos of 15x
        val cumulativeAltitude = statRecorder.lifetimeAltitude
        val bosses = totalBossesKilled
        val combos = statRecorder.lifetimeCombosOver15

        if (cumulativeAltitude >= 10000 && bosses >= 5 && combos >= 5) {
            isZenModeUnlocked = true
            sharedPrefs.edit { putBoolean("zen_unlocked", true) }
        }
    }

    private fun checkMultiplayerUnlock(stats: GameStats) {
        if (!isZenModeUnlocked || isMultiplayerUnlocked) return
        
        // Requirements: 25,000m Altitude + 10 Bosses (incl. Singularity) + 5 Combos of 20x
        val cumulativeAltitude = statRecorder.lifetimeAltitude
        val bosses = totalBossesKilled
        val combos20 = statRecorder.lifetimeCombosOver20
        val singularityDefeated = statRecorder.uniqueBossesKilled.getOrDefault("BOSS_SINGULARITY", 0) > 0

        if (cumulativeAltitude >= 25000 && bosses >= 10 && combos20 >= 5 && singularityDefeated) {
            isMultiplayerUnlocked = true
            sharedPrefs.edit { putBoolean("multiplayer_unlocked", true) }
        }
    }

    fun getZenRequirements(): List<Triple<String, String, Float>> {
        val cumulativeAltitude = statRecorder.lifetimeAltitude
        val bosses = totalBossesKilled
        val combos = statRecorder.lifetimeCombosOver15
        
        return listOf(
            Triple("ALTITUDE", "${cumulativeAltitude}m / 10K", (cumulativeAltitude.toFloat() / 10000f).coerceIn(0f, 1f)),
            Triple("BOSSES", "$bosses / 5", (bosses.toFloat() / 5f).coerceIn(0f, 1f)),
            Triple("COMBOS (15x)", "$combos / 5", (combos.toFloat() / 5f).coerceIn(0f, 1f))
        )
    }

    fun getZenUnlockProgress(): Float {
        if (isZenModeUnlocked) return 1f
        val cumulativeAltitude = statRecorder.lifetimeAltitude
        val bosses = totalBossesKilled
        val combos = statRecorder.lifetimeCombosOver15
        
        val altProgress = (cumulativeAltitude.toFloat() / 10000f).coerceIn(0f, 1f)
        val bossProgress = (bosses.toFloat() / 5f).coerceIn(0f, 1f)
        val comboProgress = (combos.toFloat() / 5f).coerceIn(0f, 1f)
        
        return (altProgress + bossProgress + comboProgress) / 3f
    }

    fun getMultiplayerRequirements(): List<Triple<String, String, Float>> {
        val cumulativeAltitude = statRecorder.lifetimeAltitude
        val bosses = totalBossesKilled
        val combos20 = statRecorder.lifetimeCombosOver20
        val singularityDefeated = statRecorder.uniqueBossesKilled.getOrDefault("BOSS_SINGULARITY", 0) > 0
        
        return listOf(
            Triple("UPLINK ALTITUDE", "${cumulativeAltitude}m / 25K", (cumulativeAltitude.toFloat() / 25000f).coerceIn(0f, 1f)),
            Triple("TOTAL BOSSES", "$bosses / 10", (bosses.toFloat() / 10f).coerceIn(0f, 1f)),
            Triple("COMBOS (20x)", "$combos20 / 5", (combos20.toFloat() / 5f).coerceIn(0f, 1f)),
            Triple("SINGULARITY CORE", if (singularityDefeated) "ACQUIRED" else "MISSING", if (singularityDefeated) 1f else 0f)
        )
    }

    fun getMultiplayerUnlockProgress(): Float {
        if (isMultiplayerUnlocked) return 1f
        val cumulativeAltitude = statRecorder.lifetimeAltitude
        val bosses = totalBossesKilled
        val combos20 = statRecorder.lifetimeCombosOver20
        val singularityDefeated = statRecorder.uniqueBossesKilled.getOrDefault("BOSS_SINGULARITY", 0) > 0
        
        val altProgress = (cumulativeAltitude.toFloat() / 25000f).coerceIn(0f, 1f)
        val bossProgress = (bosses.toFloat() / 10f).coerceIn(0f, 1f)
        val comboProgress = (combos20.toFloat() / 5f).coerceIn(0f, 1f)
        val singularityProgress = if (singularityDefeated) 1f else 0f
        
        return (altProgress + bossProgress + comboProgress + singularityProgress) / 4f
    }

    fun unlockMusicTrack(resName: String) {
        if (!unlockedMusicTracks.contains(resName)) {
            unlockedMusicTracks = unlockedMusicTracks + resName
            sharedPrefs.edit { putStringSet("unlocked_music", unlockedMusicTracks) }
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
                totalCash += reward.amount
                sharedPrefs.edit { putInt("total_cash", totalCash) }
                android.util.Log.d("Progression", "Cash reward +${reward.amount} granted — balance=$totalCash")
            }
            is MissionReward.None -> {}
        }
    }

    fun updateRank() {
        artifactManager.updateRank()
    }
    
    fun getCompletionStats(category: String): Pair<Int, Int> {
        return artifactManager.getCompletionStats(category)
    }

    fun getTotalCompletionPercentage(): Int {
        return artifactManager.getTotalCompletionPercentage()
    }

    override fun getTotalDiscoveries(): Int {
        return artifactManager.getTotalDiscoveries()
    }
    
    /**
     * Returns all progression data as a flat map for cloud sync.
     */
    fun getCloudData(): Map<String, Any> = buildMap {
        put("highScore", highScore)
        put("highAltitude", highAltitude)
        put("totalCash", totalCash)
        put("unlockedTrailIds", unlockedTrailIds.toList())
        put("unlockedPaintIds", unlockedPaintIds.toList())
        put("creditBalance", creditBalance)
        put("ascensionPrestigeLevel", ascensionPrestigeLevel)
        put("permanentMaxIntegrity", permanentMaxIntegrity.toDouble())
        put("permanentMaxShield", permanentMaxShield.toDouble())
        put("lifetimeFlightTime", lifetimeFlightTime.toDouble())
        put("lifetimePlatformTime", lifetimePlatformTime.toDouble())
        put("lifetimeBossesDefeated", lifetimeBossesDefeated)
        put("lifetimeHazards", lifetimeHazards)
        put("lifetimeArtifacts", lifetimeArtifacts)
        put("lifetimeLandings", lifetimeLandings)
        put("lifetimeMissionsCompleted", lifetimeMissionsCompleted)
        put("ownedModuleIds", ownedModuleIds.toList())
        put("completedMissionIds", completedMissionIds.toList())
        put("claimedMissionIds", claimedMissionIds.toList())
    }

    /**
     * Merges cloud data into local progression. "Keep highest" for competitive stats,
     * union for collections, last-write-wins for settings.
     */
    fun applyCloudData(data: Map<String, Any>) {
        highScore = maxOf(highScore, (data["highScore"] as? Long)?.toInt() ?: 0)
        highAltitude = maxOf(highAltitude, (data["highAltitude"] as? Long)?.toInt() ?: 0)
        totalCash = maxOf(totalCash, (data["totalCash"] as? Long)?.toInt() ?: 0)
        
        val cloudTrails = (data["unlockedTrailIds"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
        if (cloudTrails.isNotEmpty()) unlockedTrailIds = unlockedTrailIds union cloudTrails

        val cloudPaints = (data["unlockedPaintIds"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
        if (cloudPaints.isNotEmpty()) unlockedPaintIds = unlockedPaintIds union cloudPaints

        creditBalance = maxOf(creditBalance, (data["creditBalance"] as? Long)?.toInt() ?: 0)
        ascensionPrestigeLevel = maxOf(ascensionPrestigeLevel, (data["ascensionPrestigeLevel"] as? Long)?.toInt() ?: 0)
        permanentMaxIntegrity = maxOf(permanentMaxIntegrity, (data["permanentMaxIntegrity"] as? Double)?.toFloat() ?: 0f)
        permanentMaxShield = maxOf(permanentMaxShield, (data["permanentMaxShield"] as? Double)?.toFloat() ?: 0f)
        
        statRecorder.syncStats(
            flightTime = maxOf(lifetimeFlightTime, (data["lifetimeFlightTime"] as? Double)?.toFloat() ?: 0f),
            platformTime = maxOf(lifetimePlatformTime, (data["lifetimePlatformTime"] as? Double)?.toFloat() ?: 0f),
            bosses = maxOf(lifetimeBossesDefeated, (data["lifetimeBossesDefeated"] as? Long)?.toInt() ?: 0),
            hazards = maxOf(lifetimeHazards, (data["lifetimeHazards"] as? Long)?.toInt() ?: 0),
            artifacts = maxOf(lifetimeArtifacts, (data["lifetimeArtifacts"] as? Long)?.toInt() ?: 0),
            landings = maxOf(lifetimeLandings, (data["lifetimeLandings"] as? Long)?.toInt() ?: 0),
            altitude = maxOf(statRecorder.lifetimeAltitude, (data["lifetimeAltitude"] as? Long)?.toInt() ?: 0),
            maxCombo = maxOf(statRecorder.maxComboEver, (data["maxComboEver"] as? Long)?.toInt() ?: 0),
            missions = maxOf(lifetimeMissionsCompleted, (data["lifetimeMissionsCompleted"] as? Long)?.toInt() ?: 0)
        )

        val cloudModules = (data["ownedModuleIds"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
        if (cloudModules.isNotEmpty()) ownedModuleIds = ownedModuleIds union cloudModules

        missionTracker.syncMissions(
            completed = completedMissionIds union ((data["completedMissionIds"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()),
            claimed = claimedMissionIds union ((data["claimedMissionIds"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet())
        )

        persistCloudSync()
    }

    private fun persistCloudSync() {
        sharedPrefs.edit {
            putInt("highScore", highScore)
            putInt("highAltitude", highAltitude)
            putInt("total_cash", totalCash)
            putStringSet("unlocked_trails", unlockedTrailIds)
            putStringSet("unlocked_paints", unlockedPaintIds)
            putInt("credit_balance", creditBalance)
            putInt("ascension_prestige", ascensionPrestigeLevel)
            putFloat("max_integrity", permanentMaxIntegrity)
            putFloat("max_shield", permanentMaxShield)
            putFloat("stat_lifetime_flight_time", lifetimeFlightTime)
            putFloat("stat_lifetime_platform_time", lifetimePlatformTime)
            putInt("stat_lifetime_bosses", lifetimeBossesDefeated)
            putInt("stat_lifetime_hazards", lifetimeHazards)
            putInt("stat_lifetime_artifacts", lifetimeArtifacts)
            putInt("stat_lifetime_landings", lifetimeLandings)
            putInt("stat_lifetime_altitude", statRecorder.lifetimeAltitude)
            putInt("stat_max_combo", statRecorder.maxComboEver)
            putInt("missions_completed", lifetimeMissionsCompleted)
            putStringSet("owned_modules", ownedModuleIds)
            putStringSet("completed_missions", completedMissionIds)
            putStringSet("claimed_missions", claimedMissionIds)
        }
    }

    /**
     * Wipes all progression data.
     * If isFactoryReset is false, preserves premium status.
     */
    fun wipeData(isFactoryReset: Boolean = false) {
        val wasPremium = if (!isFactoryReset) sharedPrefs.getBoolean("premium_user", false) else false
        
        sharedPrefs.edit { clear() }
        
        if (wasPremium) {
            sharedPrefs.edit { putBoolean("premium_user", true) }
        }

        highScore = 0
        highAltitude = 0
        ownedModuleIds = emptySet()
        totalCash = 0
        creditBalance = 0
        ascensionPrestigeLevel = 0
        unlockedTrailIds = setOf("plasma_cyan")
        unlockedPaintIds = setOf("stock")
        
        permanentMaxIntegrity = Constants.BASE_INTEGRITY
        permanentMaxShield = Constants.BASE_SHIELD
        
        missionTracker.clear()
        statRecorder.clear()
        artifactManager.clear()
        
        // Re-evaluate rank immediately so MP becomes 0
        updateRank()
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
     * Persists a new high altitude if it exceeds the current one.
     */
    fun saveHighAltitude(newAltitude: Int): Boolean {
        if (newAltitude > highAltitude) {
            highAltitude = newAltitude
            sharedPrefs.edit { putInt("highAltitude", newAltitude) }
            return true
        }
        return false
    }

    /**
     * Re-evaluates all module unlocks.
     */
    fun checkModuleUnlocks(missionManager: MissionManager) {
        unlockService.checkModuleUnlocks(this, missionManager, onModuleUnlocked, onBlueprintUnlocked)
    }

    fun evaluateLoreLogs(altitude: Int) {
        unlockService.evaluateLoreLogs(altitude, onLoreLogDiscovered)
    }

    /**
     * Audits achievements and rocket unlocks based on current run stats.
     */
    fun checkUnlocks(
        stats: GameStats,
        player: Player,
        onRocketUnlock: (RocketType) -> Unit,
        onAchievementUnlock: (Achievement) -> Unit,
        onLoreDiscovery: (DiscoveryType) -> Unit
    ) {
        unlockService.checkUnlocks(stats, player, onRocketUnlock, onAchievementUnlock, onLoreDiscovery, onLoreLogDiscovered)
    }

    fun isAchievementUnlocked(id: String): Boolean {
        return sharedPrefs.getBoolean("achievement_$id", false)
    }
}

private fun String.capitalize(locale: Locale): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
