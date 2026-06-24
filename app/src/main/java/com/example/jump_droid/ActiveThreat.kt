package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Represents a live instance of a threat during gameplay.
 */
class ActiveThreat(
    val instanceId: String,
    val definition: ThreatDefinition,
    initialX: Float,
    initialY: Float,
    initialVx: Float = 0f,
    initialVy: Float = 0f
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)
    var vx by mutableFloatStateOf(initialVx)
    var vy by mutableFloatStateOf(initialVy)
    
    var state by mutableStateOf(ThreatState.SPAWNING)
    var lifetime by mutableFloatStateOf(0f)
    var health by mutableFloatStateOf(definition.baseHealth)
    var difficultyMultiplier by mutableFloatStateOf(1f)
    var duration by mutableFloatStateOf(10f) // Default duration for temporary threats
    var patrolTimer by mutableFloatStateOf(0f)
    
    // Tracking/Awareness State
    var isTracking by mutableStateOf(false)
    var targetAngle by mutableFloatStateOf(0f)
    var scanPulse by mutableFloatStateOf(0f)
    var rotation by mutableFloatStateOf(0f)
    var hasInteracted by mutableStateOf(false)
    var phase by mutableIntStateOf(1)
    var alertLevel by mutableFloatStateOf(0f)
    var localTimer by mutableFloatStateOf(0f)
    var hasReinforced by mutableStateOf(false)
    var fleeTimer by mutableFloatStateOf(0f)
    var transmissionProgress by mutableFloatStateOf(0f)
    var firstDetectionShown by mutableStateOf(false)
    var isMaterialized by mutableStateOf(true)
    
    // Boss Mechanic States
    var gravityPulseTimer by mutableFloatStateOf(0f)
    var pulseAlpha by mutableFloatStateOf(0f)
    var jamCooldown by mutableFloatStateOf(0f)
    var bossRewardDropped by mutableStateOf(false)
    var shakeCooldown by mutableFloatStateOf(0f)
    var projectileCooldown by mutableFloatStateOf(0f)
    var threatSpawnCooldown by mutableFloatStateOf(0f)
    
    // Weak Point System
    var maxWeakPoints by mutableIntStateOf(0)
    var activeWeakPoints by mutableIntStateOf(0)
    
    // Arrival Logic
    var arrivalTimer by mutableFloatStateOf(0f)
    val arrivalDuration = 5f
    var isArrived by mutableStateOf(false)

    /**
     * Updates the threat's internal state.
     */
    fun update(
        dt: Float, 
        screenWidth: Float = 1000f, 
        targetX: Float = 0f, 
        targetY: Float = 0f,
        powerUps: List<PowerUp> = emptyList(),
        activeThreats: List<ActiveThreat> = emptyList()
    ) {
        updateAI(dt, screenWidth, targetX, targetY, powerUps, activeThreats)
    }

    /**
     * Processes interaction logic between this threat and the player.
     * Should be called from the physics sub-step loop.
     */
    fun processInteraction(
        player: Player,
        sdt: Float,
        isThrusting: Boolean,
        platforms: List<Platform>,
        powerUps: MutableList<PowerUp>,
        gameTime: Long,
        screenWidth: Float,
        screenHeight: Float,
        cameraY: Float,
        onVisualFeedback: (shake: Float, flash: Float) -> Unit,
        onNotification: (String, Float?) -> Unit,
        onMajorWarning: (String, Float) -> Unit,
        onFloatingText: (String, Float, Float, Color, Boolean, Float) -> Unit,
        onDiscovery: (DiscoveryType) -> Unit,
        onBurst: (x: Float, y: Float, count: Int, color: Color, speed: Float) -> Unit,
        onScoreUpdate: (Int) -> Unit,
        onMissionProgress: (MissionType) -> Unit,
        onSpawnGhostPlatform: (x: Float, y: Float) -> Unit,
        onSpawnReinforcements: () -> Unit,
        onAnchoredText: (FloatingText) -> Unit,
        onEscalationEvent: (x: Float, y: Float, source: ActiveThreat) -> Unit,
        activeThreats: List<ActiveThreat> = emptyList(),
        onSpawnProjectile: (x: Float, y: Float, vx: Float, vy: Float, type: ProjectileType, owner: ProjectileOwner, damage: Float, color: Color, size: Float, life: Float) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
        onSpawnThreat: (id: String, x: Float, y: Float, vx: Float, vy: Float) -> Unit = { _, _, _, _, _ -> }
    ) {
        processInteractionHandler(
            player = player, sdt = sdt, isThrusting = isThrusting,
            platforms = platforms, powerUps = powerUps, gameTime = gameTime,
            screenWidth = screenWidth, screenHeight = screenHeight, cameraY = cameraY,
            onVisualFeedback = onVisualFeedback, onNotification = onNotification,
            onMajorWarning = onMajorWarning, onFloatingText = onFloatingText,
            onDiscovery = onDiscovery, onBurst = onBurst, onScoreUpdate = onScoreUpdate,
            onMissionProgress = onMissionProgress, onSpawnGhostPlatform = onSpawnGhostPlatform,
            onSpawnReinforcements = onSpawnReinforcements, onAnchoredText = onAnchoredText,
            onEscalationEvent = onEscalationEvent, activeThreats = activeThreats,
            onSpawnProjectile = onSpawnProjectile, onSpawnThreat = onSpawnThreat
        )
    }
}
