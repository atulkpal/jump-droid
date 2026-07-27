package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
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

class ArtifactManager(private val sharedPrefs: SharedPreferences) {
    var artifactsCollected by mutableStateOf<Map<String, ArtifactRecord>>(emptyMap())
        private set

    var activeSetBonuses by mutableStateOf<List<ArtifactBonus>>(emptyList())
        private set

    var currentRank by mutableStateOf(AscensionRank.EXPLORER_I)
        private set

    var currentMasteryPoints by mutableIntStateOf(0)
        private set

    var nextRankThreshold by mutableIntStateOf(50)
        private set

    init {
        loadArtifacts()
    }

    private fun loadArtifacts() {
        val artifactTypes = DiscoveryType.entries.filter { it.category == "ARTIFACTS" }
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
        reevaluateSetBonuses()
        updateRank()
    }

    fun reevaluateSetBonuses() {
        val active = mutableListOf<ArtifactBonus>()
        ArtifactSet.ALL_SETS.forEach { set ->
            val complete = set.discoveries.all { isDiscoveryUnlocked(it.name) }
            if (complete) {
                active.add(set.bonus)
            }
        }
        activeSetBonuses = active
    }

    private fun isDiscoveryUnlocked(discoveryName: String): Boolean {
        return sharedPrefs.getBoolean("discovery_$discoveryName", false)
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

    fun updateRank() {
        reevaluateSetBonuses()
        val totalDiscoveries = DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) }
        val artifactCount = artifactsCollected.size
        val areaTypes = DiscoveryType.entries.filter { it.name.startsWith("AREA_") }
        val zoneCount = areaTypes.count { sharedPrefs.getBoolean("discovery_${it.name}", false) }
        
        currentMasteryPoints = totalDiscoveries + (artifactCount * 3) + (zoneCount * 5)
        
        val thresholds = listOf(50, 150, 300, 500)
        
        currentRank = when {
            currentMasteryPoints >= thresholds[3] -> AscensionRank.EXPLORER_V
            currentMasteryPoints >= thresholds[2] -> AscensionRank.EXPLORER_IV
            currentMasteryPoints >= thresholds[1] -> AscensionRank.EXPLORER_III
            currentMasteryPoints >= thresholds[0] -> AscensionRank.EXPLORER_II
            else -> AscensionRank.EXPLORER_I
        }
        
        nextRankThreshold = when (currentRank) {
            AscensionRank.EXPLORER_I -> thresholds[0]
            AscensionRank.EXPLORER_II -> thresholds[1]
            AscensionRank.EXPLORER_III -> thresholds[2]
            AscensionRank.EXPLORER_IV -> thresholds[3]
            AscensionRank.EXPLORER_V -> 1000 // Elite Cap
        }
    }

    fun getCompletionStats(category: String): Pair<Int, Int> {
        val allInCategory = DiscoveryType.entries.filter { it.category == category }
        val discoveredCount = allInCategory.count { sharedPrefs.getBoolean("discovery_$it", false) }
        return discoveredCount to allInCategory.size
    }

    fun getTotalCompletionPercentage(): Int {
        val filtered = DiscoveryType.entries.filter { 
            it.category != "LOGS" && it.category != "ACHIEVEMENTS" && it.category != "THREATS"
        }
        val total = filtered.size
        val discovered = filtered.count { sharedPrefs.getBoolean("discovery_${it.name}", false) }
        return if (total > 0) (discovered * 100) / total else 0
    }

    // --- Multipliers ---
    fun getFuelRegenMultiplier(prestigeLevel: Int): Float {
        var mult = 1.0f
        activeSetBonuses.forEach { if (it is ArtifactBonus.FuelRegen) mult *= it.multiplier }
        return mult * getGlobalEfficiencyMultiplier(prestigeLevel)
    }

    fun getShieldRegenMultiplier(prestigeLevel: Int): Float {
        var mult = 1.0f
        activeSetBonuses.forEach { if (it is ArtifactBonus.ShieldRegen) mult *= it.multiplier }
        return mult * getGlobalEfficiencyMultiplier(prestigeLevel)
    }

    fun getHeatCooldownMultiplier(prestigeLevel: Int): Float {
        var mult = 1.0f
        activeSetBonuses.forEach { if (it is ArtifactBonus.HeatCooldown) mult *= it.multiplier }
        return mult * getGlobalEfficiencyMultiplier(prestigeLevel)
    }

    fun getThrustMultiplier(prestigeLevel: Int): Float {
        var mult = 1.0f
        activeSetBonuses.forEach { if (it is ArtifactBonus.ThrustBoost) mult *= it.multiplier }
        return mult * getGlobalEfficiencyMultiplier(prestigeLevel)
    }

    fun getHullBonusAmount(): Float {
        var bonus = 0f
        activeSetBonuses.forEach { if (it is ArtifactBonus.HullBoost) bonus += it.amount }
        return bonus
    }

    fun getGlobalEfficiencyMultiplier(prestigeLevel: Int): Float {
        var mult = 1.0f + (prestigeLevel * 0.1f)
        activeSetBonuses.forEach { if (it is ArtifactBonus.GlobalEfficiency) mult *= it.multiplier }
        return mult
    }

    fun getTotalDiscoveries(): Int {
        return DiscoveryType.entries.count { isDiscoveryUnlocked(it.name) }
    }

    fun clear() {
        artifactsCollected = emptyMap()
        activeSetBonuses = emptyList()
        currentRank = AscensionRank.EXPLORER_I
    }
}

private fun String.capitalize(locale: Locale): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
