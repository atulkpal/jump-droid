package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

class StatRecorder(private val sharedPrefs: SharedPreferences) {
    var lifetimeFlightTime by mutableFloatStateOf(0f)
        private set
    var lifetimePlatformTime by mutableFloatStateOf(0f)
        private set
    var lifetimeHazards by mutableIntStateOf(0)
        private set
    var lifetimeArtifacts by mutableIntStateOf(0)
        private set
    var lifetimeLandings by mutableIntStateOf(0)
        private set
    var lifetimeAltitude by mutableIntStateOf(0)
        private set
    var maxComboEver by mutableIntStateOf(0)
        private set
    var lifetimeBossesDefeated by mutableIntStateOf(0)
        private set
    var lifetimeMissionsCompleted by mutableIntStateOf(0)
        private set

    init {
        loadStats()
    }

    private fun loadStats() {
        lifetimeFlightTime = sharedPrefs.getFloat("stat_lifetime_flight_time", 0f)
        lifetimePlatformTime = sharedPrefs.getFloat("stat_lifetime_platform_time", 0f)
        lifetimeBossesDefeated = sharedPrefs.getInt("stat_lifetime_bosses", 0)
        lifetimeHazards = sharedPrefs.getInt("stat_lifetime_hazards", 0)
        lifetimeArtifacts = sharedPrefs.getInt("stat_lifetime_artifacts", 0)
        lifetimeLandings = sharedPrefs.getInt("stat_lifetime_landings", 0)
        lifetimeAltitude = sharedPrefs.getInt("stat_lifetime_altitude", 0)
        maxComboEver = sharedPrefs.getInt("stat_max_combo", 0)
        lifetimeMissionsCompleted = sharedPrefs.getInt("missions_completed", 0)
    }

    fun syncStats(
        flightTime: Float,
        platformTime: Float,
        bosses: Int,
        hazards: Int,
        artifacts: Int,
        landings: Int,
        altitude: Int,
        maxCombo: Int,
        missions: Int
    ) {
        lifetimeFlightTime = flightTime
        lifetimePlatformTime = platformTime
        lifetimeBossesDefeated = bosses
        lifetimeHazards = hazards
        lifetimeArtifacts = artifacts
        lifetimeLandings = landings
        lifetimeAltitude = altitude
        maxComboEver = maxCombo
        lifetimeMissionsCompleted = missions
    }

    fun commitSessionStats(stats: GameStats, missionsCompleted: Int) {
        lifetimeFlightTime += stats.totalFlightTime
        lifetimePlatformTime += stats.totalPlatformTime
        lifetimeBossesDefeated += stats.bossesDefeated
        lifetimeHazards += stats.hazardHitsSurvived
        lifetimeArtifacts += stats.artifactsCollected
        lifetimeLandings += stats.platformLandings
        lifetimeAltitude += stats.maxAltitudeMeters
        maxComboEver = maxOf(maxComboEver, stats.maxCombo)
        lifetimeMissionsCompleted = missionsCompleted
        
        sharedPrefs.edit {
            putFloat("stat_lifetime_flight_time", lifetimeFlightTime)
            putFloat("stat_lifetime_platform_time", lifetimePlatformTime)
            putInt("stat_lifetime_bosses", lifetimeBossesDefeated)
            putInt("stat_lifetime_hazards", lifetimeHazards)
            putInt("stat_lifetime_artifacts", lifetimeArtifacts)
            putInt("stat_lifetime_landings", lifetimeLandings)
            putInt("stat_lifetime_altitude", lifetimeAltitude)
            putInt("stat_max_combo", maxComboEver)
            putInt("missions_completed", lifetimeMissionsCompleted)
        }
    }

    fun clear() {
        lifetimeFlightTime = 0f
        lifetimePlatformTime = 0f
        lifetimeHazards = 0
        lifetimeArtifacts = 0
        lifetimeLandings = 0
        lifetimeAltitude = 0
        maxComboEver = 0
        lifetimeBossesDefeated = 0
        lifetimeMissionsCompleted = 0
    }
}
