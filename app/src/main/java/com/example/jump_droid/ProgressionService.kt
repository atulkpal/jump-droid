package com.example.jump_droid

interface ProgressionService {
    val highScore: Int
    val lifetimeBossesDefeated: Int
    val artifactsCollected: Map<String, ArtifactRecord>
    val completedMissionIds: Set<String>
    val claimedMissionIds: Set<String>
    val lifetimeFlightTime: Float
    val lifetimePlatformTime: Float

    fun getTotalDiscoveries(): Int
    fun getMissionProgress(missionId: String): Int
    fun saveMissionProgress(missionId: String, progress: Int)
    fun grantReward(reward: MissionReward, player: Player)
    fun recordMissionClaim(missionId: String)
}
