package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
    var lifetimeCombosOver15 by mutableIntStateOf(0)
        private set
    var lifetimeCombosOver20 by mutableIntStateOf(0)
        private set
    var lifetimeCombosOver50 by mutableIntStateOf(0)
        private set
    var bossesEscaped by mutableIntStateOf(0)
        private set
    var lifetimeContinuesUsed by mutableIntStateOf(0)
        private set
    var lifetimeCashEarned by mutableIntStateOf(0)
        private set
    var lifetimeCashSpent by mutableIntStateOf(0)
        private set
    var nearDeathEscapes by mutableIntStateOf(0)
        private set
    var perfectLandings by mutableIntStateOf(0)
        private set
    var totalRuns by mutableIntStateOf(0)
        private set

    // Zen Mode Stats
    var zenTopRuns = mutableStateListOf<Int>(0, 0, 0)
    var zenMaxCombo by mutableIntStateOf(0)
        private set

    // Multiplayer Stats
    var mpGamesPlayed by mutableIntStateOf(0)
        private set
    var mpWins by mutableIntStateOf(0)
        private set
    var mpLosses by mutableIntStateOf(0)
        private set

    var topRuns = mutableStateListOf<Int>(0, 0, 0)
    var uniqueBossesKilled = mutableStateMapOf<String, Int>()
    var uniqueBossesEscaped = mutableStateMapOf<String, Int>()
    var killedByBossMap = mutableStateMapOf<String, Int>()

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
        
        lifetimeCombosOver15 = sharedPrefs.getInt("stat_combos_15", 0)
        lifetimeCombosOver20 = sharedPrefs.getInt("stat_combos_20", 0)
        lifetimeCombosOver50 = sharedPrefs.getInt("stat_combos_50", 0)
        bossesEscaped = sharedPrefs.getInt("stat_bosses_escaped", 0)
        lifetimeContinuesUsed = sharedPrefs.getInt("stat_continues_used", 0)
        lifetimeCashEarned = sharedPrefs.getInt("stat_cash_earned", 0)
        lifetimeCashSpent = sharedPrefs.getInt("stat_cash_spent", 0)
        nearDeathEscapes = sharedPrefs.getInt("stat_near_death_escapes", 0)
        perfectLandings = sharedPrefs.getInt("stat_perfect_landings", 0)
        totalRuns = sharedPrefs.getInt("stat_total_runs", 0)
        
        // Zen Mode
        zenTopRuns.clear()
        zenTopRuns.add(sharedPrefs.getInt("zen_run_1", 0))
        zenTopRuns.add(sharedPrefs.getInt("zen_run_2", 0))
        zenTopRuns.add(sharedPrefs.getInt("zen_run_3", 0))
        zenMaxCombo = sharedPrefs.getInt("zen_max_combo", 0)
        
        // Multiplayer
        mpGamesPlayed = sharedPrefs.getInt("mp_games", 0)
        mpWins = sharedPrefs.getInt("mp_wins", 0)
        mpLosses = sharedPrefs.getInt("mp_losses", 0)

        topRuns.clear()
        topRuns.add(sharedPrefs.getInt("top_run_1", 0))
        topRuns.add(sharedPrefs.getInt("top_run_2", 0))
        topRuns.add(sharedPrefs.getInt("top_run_3", 0))

        uniqueBossesKilled.clear()
        uniqueBossesEscaped.clear()
        killedByBossMap.clear()
        
        sharedPrefs.all.forEach { (key, value) ->
            when {
                key.startsWith("boss_kills_") -> uniqueBossesKilled[key.removePrefix("boss_kills_")] = (value as? Int) ?: 0
                key.startsWith("boss_escapes_") -> uniqueBossesEscaped[key.removePrefix("boss_escapes_")] = (value as? Int) ?: 0
                key.startsWith("boss_killed_player_") -> killedByBossMap[key.removePrefix("boss_killed_player_")] = (value as? Int) ?: 0
            }
        }

        // Statistical Self-Correction (Migration logic)
        val bestRun = topRuns.firstOrNull() ?: 0
        if (lifetimeAltitude < bestRun && bestRun > 0) {
            lifetimeAltitude = topRuns.sum()
        }
        if (totalRuns == 0 && bestRun > 0) {
            totalRuns = topRuns.count { it > 0 }
        }
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
        missions: Int,
        runs: Int = 0
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
        if (runs > 0) totalRuns = runs
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
        totalRuns++
        
        // New stats
        lifetimeCombosOver15 += stats.combosOver15
        if (stats.maxCombo >= 20) lifetimeCombosOver20++
        if (stats.maxCombo >= 50) lifetimeCombosOver50++
        lifetimeContinuesUsed += stats.continuesUsed
        
        lifetimeCashEarned += stats.totalScore / 10 
        if (stats.wasNearDeath) nearDeathEscapes++

        // Update Top Runs
        val newTop = (topRuns + stats.totalScore).sortedDescending().take(3)
        topRuns.clear()
        topRuns.addAll(newTop)

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
            
            putInt("stat_combos_15", lifetimeCombosOver15)
            putInt("stat_combos_20", lifetimeCombosOver20)
            putInt("stat_combos_50", lifetimeCombosOver50)
            putInt("stat_bosses_escaped", bossesEscaped)
            putInt("stat_continues_used", lifetimeContinuesUsed)
            putInt("stat_cash_earned", lifetimeCashEarned)
            putInt("stat_cash_spent", lifetimeCashSpent)
            putInt("stat_near_death_escapes", nearDeathEscapes)
            putInt("stat_perfect_landings", perfectLandings)
            putInt("stat_total_runs", totalRuns)
            
            topRuns.forEachIndexed { index, score ->
                putInt("top_run_${index + 1}", score)
            }
        }
    }

    fun recordBossKill(bossId: String) {
        val current = uniqueBossesKilled.getOrDefault(bossId, 0)
        uniqueBossesKilled[bossId] = current + 1
        sharedPrefs.edit {
            putInt("boss_kills_$bossId", current + 1)
        }
    }

    fun recordBossEscape(bossId: String) {
        bossesEscaped++
        val current = uniqueBossesEscaped.getOrDefault(bossId, 0)
        uniqueBossesEscaped[bossId] = current + 1
        sharedPrefs.edit { 
            putInt("stat_bosses_escaped", bossesEscaped)
            putInt("boss_escapes_$bossId", current + 1)
        }
    }
    
    fun recordKilledByBoss(bossId: String) {
        val current = killedByBossMap.getOrDefault(bossId, 0)
        killedByBossMap[bossId] = current + 1
        sharedPrefs.edit {
            putInt("boss_killed_player_$bossId", current + 1)
        }
    }
    
    fun recordCashSpent(amount: Int) {
        lifetimeCashSpent += amount
        sharedPrefs.edit { putInt("stat_cash_spent", lifetimeCashSpent) }
    }

    fun recordPerfectLanding() {
        perfectLandings++
        sharedPrefs.edit { putInt("stat_perfect_landings", perfectLandings) }
    }
    
    fun recordContinuesUsed(count: Int) {
        lifetimeContinuesUsed += count
        sharedPrefs.edit { putInt("stat_continues_used", lifetimeContinuesUsed) }
    }

    fun commitZenSession(score: Int, maxCombo: Int, altitude: Int) {
        val newTop = (zenTopRuns + score).sortedDescending().take(3)
        zenTopRuns.clear()
        zenTopRuns.addAll(newTop)
        if (maxCombo > zenMaxCombo) {
            zenMaxCombo = maxCombo
        }
        
        // Also contribute to global lifetime stats
        totalRuns++
        lifetimeAltitude += altitude
        if (maxCombo >= 50) lifetimeCombosOver50++
        
        sharedPrefs.edit {
            putInt("zen_run_1", zenTopRuns[0])
            putInt("zen_run_2", zenTopRuns[1])
            putInt("zen_run_3", zenTopRuns[2])
            putInt("zen_max_combo", zenMaxCombo)
            putInt("stat_total_runs", totalRuns)
            putInt("stat_lifetime_altitude", lifetimeAltitude)
            putInt("stat_combos_50", lifetimeCombosOver50)
        }
    }

    fun recordMpResult(won: Boolean) {
        mpGamesPlayed++
        if (won) mpWins++ else mpLosses++
        
        sharedPrefs.edit {
            putInt("mp_games", mpGamesPlayed)
            putInt("mp_wins", mpWins)
            putInt("mp_losses", mpLosses)
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
        
        lifetimeCombosOver15 = 0
        lifetimeCombosOver20 = 0
        lifetimeCombosOver50 = 0
        bossesEscaped = 0
        lifetimeContinuesUsed = 0
        lifetimeCashEarned = 0
        lifetimeCashSpent = 0
        nearDeathEscapes = 0
        perfectLandings = 0
        totalRuns = 0
        
        zenTopRuns.fill(0)
        zenMaxCombo = 0
        mpGamesPlayed = 0
        mpWins = 0
        mpLosses = 0

        topRuns.fill(0)
        uniqueBossesKilled.clear()
        uniqueBossesEscaped.clear()
        killedByBossMap.clear()
    }
}
