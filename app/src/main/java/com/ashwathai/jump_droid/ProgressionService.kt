package com.ashwathai.jump_droid

interface ProgressionService {
    val highScore: Int
    val lifetimeBossesDefeated: Int
    val artifactsCollected: Map<String, ArtifactRecord>
    val completedMissionIds: Set<String>
    val claimedMissionIds: Set<String>
    val lifetimeFlightTime: Float
    val lifetimePlatformTime: Float
    val lifetimeHazards: Int
    val lifetimeArtifacts: Int
    val lifetimeLandings: Int

    fun getTotalDiscoveries(): Int
    fun getMissionProgress(missionId: String): Int
    fun saveMissionProgress(missionId: String, progress: Int)
    fun grantReward(reward: MissionReward, player: Player)
    fun recordMissionClaim(missionId: String)
    fun saveUnlockedMissionIds(ids: Set<String>)
    fun getUnlockedMissionIds(): Set<String>
    fun isDiscoveryUnlocked(discoveryName: String): Boolean
    fun saveDiscoveredLog(logId: String)
    fun getDiscoveredLogs(): Set<String>
    fun saveUnlockedBlueprint(id: String)
    fun getUnlockedBlueprints(): Set<String>
}
