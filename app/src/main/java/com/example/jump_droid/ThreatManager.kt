package com.example.jump_droid

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

/**
 * Central runtime manager for all active threat instances.
 */
class ThreatManager {
    // List of active threat instances
    val activeThreats = mutableStateListOf<ActiveThreat>()

    /**
     * Updates all active threats and handles cleanup.
     */
    fun update(
        dt: Float, 
        cameraY: Float, 
        screenHeight: Float, 
        screenWidth: Float, 
        targetX: Float = 0f, 
        targetY: Float = 0f,
        powerUps: List<PowerUp> = emptyList()
    ) {
        val iterator = activeThreats.iterator()
        while (iterator.hasNext()) {
            val threat = iterator.next()
            threat.update(dt, screenWidth, targetX, targetY, powerUps, activeThreats)

            // Cleanup logic: Remove if destroyed or way off-screen
            val isWayOffScreen = (threat.y > cameraY + screenHeight + 1000f) || (threat.y < cameraY - 2000f)
            if (threat.state == ThreatState.DESTROYED || isWayOffScreen) {
                iterator.remove()
            }
        }
    }

    /**
     * Spawns a new threat instance based on a definition.
     */
    fun spawnThreat(definition: ThreatDefinition, x: Float, y: Float, vx: Float = 0f, vy: Float = 0f): ActiveThreat {
        val instance = ActiveThreat(
            instanceId = UUID.randomUUID().toString(),
            definition = definition,
            initialX = x,
            initialY = y,
            initialVx = vx,
            initialVy = vy,
        )
        activeThreats.add(instance)
        return instance
    }

    /**
     * Clears all active threats.
     */
    fun clear() {
        activeThreats.clear()
    }
}
