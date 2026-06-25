package com.example.jump_droid

import android.util.Log

/**
 * Central evaluator for module and blueprint unlock requirements.
 */
object UnlockEngine {

    fun evaluate(
        req: UnlockRequirement,
        progression: ProgressionManager,
        missionManager: MissionManager
    ): Boolean {
        // For now, LogicOp is ignored as most requirements are single. 
        // LogicOp.AND/OR will be handled when complex nested requirements are added in Task 2.1 extension.
        
        return when (req.type) {
            UnlockType.SCORE -> {
                progression.highScore >= req.value
            }
            UnlockType.ALTITUDE -> {
                progression.highScore >= req.value
            }
            UnlockType.ARTIFACT -> {
                progression.lifetimeArtifacts >= req.value
            }
            UnlockType.DISCOVERY -> {
                // target should be the name of the DiscoveryType
                progression.isDiscoveryUnlocked(req.target)
            }
            UnlockType.MISSION, UnlockType.MISSION_COMPLETE -> {
                // target should be the mission ID or achievement ID
                progression.completedMissionIds.contains(req.target) || progression.isAchievementUnlocked(req.target)
            }
            UnlockType.ARTIFACT_SET -> {
                // To be implemented in Sprint 9.3
                false 
            }
        }
    }
}
