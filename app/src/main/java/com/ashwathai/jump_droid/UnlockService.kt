package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.core.content.edit

class UnlockService(private val sharedPrefs: SharedPreferences) {

    fun checkModuleUnlocks(
        progressionManager: ProgressionManager,
        missionManager: MissionManager,
        onModuleUnlocked: ((Module) -> Unit)?,
        onBlueprintUnlocked: ((BlueprintType) -> Unit)?
    ) {
        ModuleRegistry.getAll().forEach { module ->
            if (!progressionManager.isModuleOwned(module.id)) {
                if (UnlockEngine.evaluate(module.unlockRequirement, progressionManager, missionManager)) {
                    if (progressionManager.grantModule(module.id)) {
                        onModuleUnlocked?.invoke(module)
                        android.util.Log.i("ProgressionManager", "NEW MODULE UNLOCKED: ${module.name}")
                    }
                }
            }
        }

        BlueprintRegistry.ALL_BLUEPRINTS.forEach { (type, req) ->
            if (!sharedPrefs.getBoolean("blueprint_${type.name}", false)) {
                if (UnlockEngine.evaluate(req, progressionManager, missionManager)) {
                    saveUnlockedBlueprint(type.name)
                    onBlueprintUnlocked?.invoke(type)
                    android.util.Log.i("ProgressionManager", "BLUEPRINT ACQUIRED: ${type.displayName}")
                }
            }
        }
    }

    fun evaluateLoreLogs(altitude: Int, onLoreLogDiscovered: ((LoreLog) -> Unit)?) {
        LoreLog.ALL_LOGS.forEach { log ->
            if (!sharedPrefs.getBoolean("log_${log.id}", false)) {
                if (altitude >= log.unlockAltitude) {
                    saveDiscoveredLog(log.id)
                    onLoreLogDiscovered?.invoke(log)
                    android.util.Log.i("ProgressionManager", "LORE LOG RECOVERED: ${log.title}")
                }
            }
        }
    }

    fun checkUnlocks(
        stats: GameStats,
        player: Player,
        onRocketUnlock: (RocketType) -> Unit,
        onAchievementUnlock: (Achievement) -> Unit,
        onLoreDiscovery: (DiscoveryType) -> Unit,
        onLoreLogDiscovered: ((LoreLog) -> Unit)?
    ) {
        val score = stats.totalScore
        val altitude = stats.maxAltitudeMeters
        evaluateLoreLogs(altitude, onLoreLogDiscovered)
        
        // 1. Rocket Unlocks
        RocketType.entries.forEach { type ->
            if (score >= type.unlockScore && !sharedPrefs.getBoolean("unlock_${type.name}", false)) {
                sharedPrefs.edit { putBoolean("unlock_${type.name}", true) }
                onRocketUnlock(type)
            }
        }

        // 3. Lore Discoveries (Altitude-based for distance milestones)
        if (altitude >= 0) onLoreDiscovery(player.rocketType.discovery)
        if (altitude >= 100) onLoreDiscovery(DiscoveryType.LORE_ASCENSION)
        if (altitude >= 5000) onLoreDiscovery(DiscoveryType.LORE_SIGNAL)
        if (altitude >= 10000) onLoreDiscovery(DiscoveryType.LORE_LOST_FLEET)
        if (altitude >= 20000) onLoreDiscovery(DiscoveryType.LORE_LOGS)

        // 3. Achievements
        AchievementsList.forEach { achievement ->
            if (!sharedPrefs.getBoolean("achievement_${achievement.id}", false)) {
                if (achievement.unlockCondition(stats)) {
                    sharedPrefs.edit { putBoolean("achievement_${achievement.id}", true) }
                    onAchievementUnlock(achievement)
                }
            }
        }
    }

    fun saveUnlockedBlueprint(id: String) {
        sharedPrefs.edit { putBoolean("blueprint_$id", true) }
    }

    fun getUnlockedBlueprints(): Set<String> {
        val unlocked = mutableSetOf<String>()
        BlueprintType.entries.forEach { blueprint ->
            if (sharedPrefs.getBoolean("blueprint_${blueprint.name}", false)) {
                unlocked.add(blueprint.name)
            }
        }
        return unlocked
    }

    fun saveDiscoveredLog(logId: String) {
        sharedPrefs.edit { putBoolean("log_$logId", true) }
    }

    fun getDiscoveredLogs(): Set<String> {
        val discovered = mutableSetOf<String>()
        LoreLog.ALL_LOGS.forEach { log ->
            if (sharedPrefs.getBoolean("log_${log.id}", false)) {
                discovered.add(log.id)
            }
        }
        return discovered
    }
}
