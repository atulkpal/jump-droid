package com.ashwathai.jump_droid

/**
 * Real-time statistics for the current run.
 * Feeds the Intelligence Network for mission progress and permanent account stats.
 */
data class GameStats(
    val totalFlightTime: Float = 0f,
    val totalPlatformTime: Float = 0f,
    val zeroHeatTime: Float = 0f,
    val fuelPickupsCollected: Int = 0,
    val powerUpsCollected: Int = 0,
    val platformLandings: Int = 0,
    val maxCombo: Int = 0,
    val currentCombo: Int = 0,
    val comboMaintainTime: Float = 0f,
    val bossesDefeated: Int = 0,
    val codexUnlocked: Int = 0,
    val maxAltitude: Float = 0f,
    val maxMomentum: Float = 0f,
    val hazardHitsSurvived: Int = 0,
    val perfectRunTime: Float = 0f,
    val artifactsCollected: Int = 0,
    val dashesPerRun: Int = 0,
    val overheatCount: Int = 0,
    val wasNearDeath: Boolean = false,
    val consecutiveWins: Int = 0
)
