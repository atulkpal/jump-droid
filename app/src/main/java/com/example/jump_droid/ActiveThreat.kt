package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

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
        lifetime += dt
        
        // --- Life Cycle Calculation (Adjustment Run 1) ---
        val lifeRatio = (lifetime / duration).coerceIn(0f, 1f)
        // Note: phase is used here for Life Cycle (1: Formation, 2: Active, 3: Dissipation)
        // For hazards, we use this 1-3 cycle. For enemies/bosses, they have their own phase logic.
        if (definition.type == ThreatType.HAZARD) {
            phase = when {
                lifeRatio < 0.15f -> 1 
                lifeRatio < 0.85f -> 2
                else -> 3
            }
        }

        if (state == ThreatState.SPAWNING && lifetime > 0.5f) {
            state = ThreatState.ACTIVE
            
            // Initialize threat-specific durations (Adjustment Run 1)
            duration = when (definition.id) {
                "HAZ_LIGHTNING" -> 8f + (Random.nextFloat() * 4f)
                "HAZ_DEBRIS" -> 25f
                "HAZ_RADIATION" -> 20f
                "HAZ_SOLAR_FLARE" -> 15f
                "HAZ_TURBULENCE" -> 18f
                "HAZ_GRAVITY" -> 22f
                "HAZ_EMP" -> 15f
                "HAZ_VOID_ANOMALY" -> 15f
                else -> duration
            }

            // Initialize boss-specific stats
            if (definition.type == ThreatType.MINI_BOSS || definition.type == ThreatType.BOSS) {
                maxWeakPoints = when (definition.id) {
                    "MINI_BOSS_COMMANDER" -> 3
                    "BOSS_GATEKEEPER" -> 4
                    "BOSS_STAR_EATER" -> 1
                    "BOSS_LEVIATHAN" -> 3
                    "BOSS_VOID_ENGINE" -> 2
                    "BOSS_SIGNAL" -> 1
                    else -> 1
                }
                activeWeakPoints = maxWeakPoints
            }
        }

        // Basic movement logic
        if (!isTracking) {
            x += vx * dt
            y += vy * dt
        }

        // Specific AI behaviors
        if (state == ThreatState.ACTIVE) {
            when (definition.type) {
                ThreatType.HAZARD -> {
                    localTimer += dt
                    
                    // Cleanup based on duration
                    if (lifetime > duration) {
                        state = ThreatState.DESTROYED
                    }

                    when (definition.id) {
                        "HAZ_LIGHTNING" -> {
                            // Strike cycle: Telegraph -> Strike -> Wait
                            val cycleTime = 4f
                            val telegraphTime = 1.5f
                            val strikeTime = 0.5f
                            val t = lifetime % cycleTime
                            
                            // Visual Sub-Phase (within the Active phase)
                            val subPhase = if (t < telegraphTime) 1 else if (t < telegraphTime + strikeTime) 2 else 3
                            scanPulse = if (subPhase == 1) t / telegraphTime else if (subPhase == 2) 1.0f else 0f
                            
                            // Limited Pursuit Behavior (Adjustment Run 1)
                            // Drift toward player horizontal and follow altitude slightly
                            val dx = targetX - x
                            val dy = targetY - y
                            x += dx * 0.4f * dt
                            y += dy * 0.15f * dt // Lazy follow vertical
                        }
                        "HAZ_SOLAR_FLARE" -> {
                            // Moving wave from top to bottom
                            y += 400f * dt
                            if (y > targetY + 1500f) state = ThreatState.DESTROYED
                        }
                        "HAZ_EMP" -> {
                            // Expanding ring
                            scanPulse += dt * 0.8f
                            if (scanPulse > 3.0f) state = ThreatState.DESTROYED
                        }
                        "HAZ_RADIATION" -> {
                            // Pulsing area
                            scanPulse = (kotlin.math.sin(lifetime * 2f) * 0.5f + 0.5f)
                        }
                        "HAZ_TURBULENCE" -> {
                            // Drifting front (Weather-like movement)
                            scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                            x += vx * dt
                            y += vy * dt
                        }
                        "HAZ_DEBRIS" -> {
                            // Tumble and drift (Natural drifting only)
                            rotation += 60f * dt
                            x += vx * dt
                            y += vy * dt
                            if (y > targetY + 2000f) state = ThreatState.DESTROYED
                        }
                        "HAZ_GRAVITY" -> {
                            // Slow drifting anomaly
                            scanPulse = (scanPulse + dt * 0.5f) % 1.0f
                            x += (screenWidth / 2f - x) * 0.05f * dt // Drift toward center
                            y += vy * 0.5f * dt
                        }
                        "HAZ_VOID_ANOMALY" -> {
                            // Pulsing reality rift — slow center drift
                            scanPulse = (kotlin.math.sin(lifetime * 1.5f) * 0.5f + 0.5f)
                            x += (screenWidth / 2f - x) * 0.03f * dt
                            y += vy * dt
                        }
                    }
                }
                ThreatType.ENEMY -> {
                    // Awareness logic for Surveyor Probe
                    if (definition.id == "ENT_SCOUT_DRONE") {
                        // Flee behavior after transmission
                        if (hasReinforced) {
                            fleeTimer -= dt
                            vy = -400f
                            x += (x - targetX) * 2f * dt
                            scanPulse = (scanPulse + dt * 8f) % 1.0f
                            if (fleeTimer <= 0f) state = ThreatState.DESTROYED
                            return
                        }

                        val dx_dist = targetX - x
                        val dy_dist = targetY - y
                        val distToTarget = kotlin.math.sqrt(dx_dist * dx_dist + dy_dist * dy_dist)
                        
                        val detectionRange = 400f
                        
                        if (distToTarget < detectionRange) {
                            if (!isTracking) {
                                isTracking = true
                                state = ThreatState.ACTIVE
                            }
                            
                            // Calculate angle to player
                            val dx = targetX - x
                            val dy = targetY - y
                            targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                            
                            // Transmission countdown while tracking
                            if (transmissionProgress < 1f) {
                                transmissionProgress += dt / 5f // 5-second transmission
                                transmissionProgress = transmissionProgress.coerceIn(0f, 1f)
                                // scanPulse reflects transmission progress (sustained, not cycling)
                                scanPulse = transmissionProgress
                            } else {
                                // Transmission complete — rapid alert pulse
                                scanPulse = (scanPulse + dt * 6f) % 1.0f
                            }
                            
                            // Slow move toward player
                            x += (targetX - x) * 0.2f * dt
                            y += (targetY - y) * 0.2f * dt
                            
                        } else {
                            if (isTracking) {
                                isTracking = false
                                transmissionProgress = 0f
                                firstDetectionShown = false
                                patrolTimer = 0f
                            }
                            
                            // Horizontal patrol
                            patrolTimer += dt
                            if (patrolTimer > 3f) {
                                vx = -vx
                                patrolTimer = 0f
                            }
                            
                            if (x < 50f && vx < 0) vx = abs(vx)
                            if (x > screenWidth - 50f && vx > 0) vx = -abs(vx)
                            
                            x += vx * dt
                            y += vy * dt
                        }
                        
                        // Hover oscillation
                        if (!isTracking) {
                            vy = kotlin.math.sin(lifetime * 3f) * 20f
                        } else {
                            vy = 0f
                        }
                    }

                    if (definition.id == "ENT_SWARM_BOTS") {
                        // Wander logic: Change direction periodically
                        patrolTimer += dt
                        if (patrolTimer > 2f) {
                            val angle = (kotlin.random.Random.nextFloat() * 2f * kotlin.math.PI.toFloat())
                            val speed = 100f + kotlin.random.Random.nextFloat() * 100f
                            vx = kotlin.math.cos(angle) * speed
                            vy = kotlin.math.sin(angle) * speed
                            patrolTimer = 0f
                        }

                        // Boundary avoidance
                        if (x < 100f) vx = abs(vx)
                        if (x > screenWidth - 100f) vx = -abs(vx)
                        
                        // Swarm center movement
                        x += vx * dt
                        y += vy * dt
                        
                        // Swarm Surge: periodically expand and contract
                        scanPulse = (kotlin.math.sin(lifetime * 2f) * 0.5f + 0.5f)
                    }
                }
                else -> {}
            }

                    if (definition.id == "MINI_BOSS_COMMANDER") {
                        localTimer += dt
                        gravityPulseTimer += dt
                        jamCooldown -= dt
                        projectileCooldown -= dt

                        // Gravity Pulse Ring Effect logic - INCREASED FREQUENCY
                        if (gravityPulseTimer > 2.5f) {
                            gravityPulseTimer = 0f
                            pulseAlpha = 1.0f
                        }
                        if (pulseAlpha > 0) pulseAlpha -= dt * 1.5f

                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 150f // Dramatic descent
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // Observation & Area Denial
                                isTracking = true
                                vy = kotlin.math.sin(lifetime * 1.5f) * 50f 
                                x += (targetX - x) * 0.7f * dt 
                                
                                val dx = targetX - x
                                val dy = targetY - y
                                targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                                scanPulse = (scanPulse + dt * 1.2f) % 1.0f 
                                
                                if (localTimer > 6f) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Pressure: Hazard Support
                                isTracking = true
                                vy = kotlin.math.sin(lifetime * 2.5f) * 80f
                                x += (targetX - x) * 0.9f * dt
                                scanPulse = (scanPulse + dt * 2.5f) % 1.0f
                                
                                val dx = targetX - x
                                val dy = targetY - y
                                targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                                
                                if (localTimer > 8f) {
                                    phase = 4 // Final Pursuit
                                    localTimer = 0f
                                }
                            }
                            4 -> { // Final Pursuit
                                isTracking = true
                                vy = kotlin.math.sin(lifetime * 2.5f) * 70f
                                x += (targetX - x) * 0.9f * dt // Very aggressive tracking
                                scanPulse = (scanPulse + dt * 3.0f) % 1.0f // Ultra fast scan
                                
                                val dx = targetX - x
                                val dy = targetY - y
                                targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                                
                                if (localTimer > 15f) {
                                    phase = 5 // Departure
                                    localTimer = 0f
                                    isTracking = false
                                }
                            }
                            5 -> { // Departure
                                vy = -500f // Escape rapidly
                                if (y < targetY - 2500f) {
                                    state = ThreatState.DESTROYED
                                }
                            }
                        }
                    }

                    // --- Boss Roster Expansion Support (Framework) ---
                    if (definition.id == "BOSS_GATEKEEPER") {
                        localTimer += dt
                        projectileCooldown -= dt
                        threatSpawnCooldown -= dt
                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 80f
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // Standard Rotation
                                rotation += (80f + (1f - activeWeakPoints.toFloat()/maxWeakPoints) * 80f) * dt
                                scanPulse = (scanPulse + dt * 0.8f) % 1.0f
                                x += (targetX - x) * 0.3f * dt 
                                if (localTimer > 10f || activeWeakPoints < maxWeakPoints) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Oscillating Rotation & Pull
                                val dir = if ((localTimer.toInt() / 3) % 2 == 0) 1f else -1f
                                rotation += (120f * dir) * dt
                                scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                                x += (targetX - x) * 0.5f * dt
                                
                                if (localTimer > 12f || activeWeakPoints <= 0) {
                                    phase = 4
                                    localTimer = 0f
                                }
                            }
                            4 -> { // Departure
                                vy = -400f
                                if (y < targetY - 2000f) state = ThreatState.DESTROYED
                            }
                        }
                    }
                    if (definition.id == "BOSS_STAR_EATER") {
                        localTimer += dt
                        projectileCooldown -= dt
                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 100f
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // Resource Sucking
                                scanPulse = (scanPulse + dt * 0.5f) % 1.0f
                                // Sucking pull logic parameters
                                x += (targetX - x) * 0.3f * dt
                                y += (targetY - y) * 0.3f * dt
                                if (localTimer > 12f || activeWeakPoints <= 0) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Starvation Frenzy
                                scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                                x += (targetX - x) * 0.6f * dt
                                vy = kotlin.math.sin(lifetime * 3f) * 150f
                                if (localTimer > 8f) {
                                    phase = 4
                                    localTimer = 0f
                                }
                            }
                            4 -> { // Departure
                                vy = -600f
                                if (y < targetY - 2000f) state = ThreatState.DESTROYED
                            }
                        }
                    }
                    if (definition.id == "BOSS_VOID_ENGINE") {
                        localTimer += dt
                        shakeCooldown = max(0f, shakeCooldown - dt)
                        projectileCooldown -= dt
                        threatSpawnCooldown -= dt
                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 120f
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // Reality Warping
                                rotation += 240f * dt
                                val pulsePhase = sin(lifetime * 2f)
                                scanPulse = (pulsePhase * 0.5f + 0.5f)
                                x += (targetX - x) * 0.2f * dt
                                if (localTimer > 12f || activeWeakPoints < maxWeakPoints) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Gravity Destabilization
                                rotation += 480f * dt
                                scanPulse = (scanPulse + dt * 3.0f) % 1.0f
                                val shift = sin(lifetime * 3f) * 2f
                                x += (targetX - x) * (0.5f + shift * 0.1f) * dt
                                if (localTimer > 15f || activeWeakPoints <= 0) {
                                    phase = 4
                                    localTimer = 0f
                                }
                            }
                            4 -> { // Departure
                                vy = -700f
                                if (y < targetY - 2500f) state = ThreatState.DESTROYED
                            }
                        }
                    }
                    if (definition.id == "BOSS_LEVIATHAN") {
                        localTimer += dt
                        projectileCooldown -= dt
                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 90f
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                                vx = 100f
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // Majestic Glide
                                x += vx * dt
                                vy = kotlin.math.sin(lifetime * 1.2f) * 100f
                                scanPulse = (sin(lifetime * 0.5f) * 0.5f + 0.5f) * 0.6f
                                if (x < 50f) vx = abs(vx)
                                if (x > screenWidth - 50f) vx = -abs(vx)
                                if (localTimer > 15f || activeWeakPoints < maxWeakPoints) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Aggressive Thrash
                                val speedMul = 1f + (1f - activeWeakPoints.toFloat() / maxWeakPoints) * 2f
                                x += vx * 3f * speedMul * dt
                                vy = kotlin.math.sin(lifetime * 2.5f) * 200f
                                scanPulse = (sin(lifetime * 3f) * 0.5f + 0.5f)
                                if (x < 20f) vx = abs(vx)
                                if (x > screenWidth - 20f) vx = -abs(vx)
                                if (localTimer > 15f || activeWeakPoints <= 0) {
                                    phase = 4
                                    localTimer = 0f
                                }
                            }
                            4 -> { // Departure
                                vy = -500f
                                x += vx * 0.5f * dt
                                if (y < targetY - 2000f) state = ThreatState.DESTROYED
                            }
                        }
                    }
                    if (definition.id == "BOSS_SIGNAL") {
                        localTimer += dt
                        projectileCooldown -= dt
                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 110f
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // HUD Interference
                                scanPulse = (sin(lifetime * 5f) * 0.5f + 0.5f)
                                x += (targetX - x) * 0.05f * dt
                                if (localTimer > 15f || activeWeakPoints <= 0) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Hallucinations
                                scanPulse = (sin(lifetime * 8f) * 0.5f + 0.5f)
                                val fakeDrift = sin(lifetime * 2f) * 2f
                                x += (targetX - x) * (0.2f + fakeDrift * 0.05f) * dt
                                vy = (kotlin.math.sin(lifetime * 3f) * 150f)
                                if (localTimer > 20f) {
                                    phase = 4
                                    localTimer = 0f
                                }
                            }
                            4 -> { // Departure
                                vy = -800f
                                if (y < targetY - 2500f) state = ThreatState.DESTROYED
                            }
                        }
                    }

                    if (definition.id == "ENT_CLOUD_SKIMMER") {
                        // Sky Ray: slow majestic glide
                        // Spawns on one side, glides to other
                        if (x < -200f || x > screenWidth + 200f) {
                            state = ThreatState.DESTROYED
                        }
                    }

                    if (definition.id == "ENT_ORBITAL_SENTRY") {
                        rotation += 20f * dt
                        scanPulse = (scanPulse + dt * 0.3f) % 1.0f
                        projectileCooldown -= dt
                    }

                    if (definition.id == "ENT_CORRUPTED_HULL") {
                        // Derelict Echo: Drifting and tumbling
                        rotation += 15f * dt
                        x += vx * dt
                        y += vy * dt
                        
                        // Periodic signal pulse
                        scanPulse = (scanPulse + dt * 0.4f) % 1.0f
                    }

                    if (definition.id == "ENT_STALKER") {
                        val dx = targetX - x
                        val dy = targetY - y
                        val dist = sqrt(dx*dx + dy*dy)
                        if (dist < 600f) {
                            if (!isTracking) isTracking = true
                            val huntSpeed = 120f + alertLevel * 120f
                            val dirX = if (dist > 0f) dx / dist else 0f
                            val dirY = if (dist > 0f) dy / dist else 0f
                            vx = dirX * huntSpeed
                            vy = dirY * huntSpeed
                            x += vx * dt
                            y += vy * dt
                            alertLevel = min(1f, alertLevel + dt * 0.8f)
                            scanPulse = (scanPulse + dt * (1f + alertLevel * 2f)) % 1.0f
                            projectileCooldown -= dt
                        } else {
                            isTracking = false
                            alertLevel = max(0f, alertLevel - dt * 0.3f)
                            x += vx * dt
                            y += vy * dt
                            scanPulse = (sin(lifetime * 0.5f) * 0.5f + 0.5f) * 0.3f
                        }
                        if (dist > 1200f) isTracking = false
                    }

                    if (definition.id == "ENT_VOID_WRAITH") {
                        localTimer += dt
                        val phaseDuration = 5f
                        val visibleDuration = 3f
                        val cyclePos = localTimer % phaseDuration
                        isMaterialized = cyclePos < visibleDuration
                        val dx = targetX - x
                        val dy = targetY - y
                        val dist = sqrt(dx*dx + dy*dy)
                        val speed = if (isMaterialized) 120f else 40f
                        val dirX = if (dist > 0f) dx / dist else 0f
                        val dirY = if (dist > 0f) dy / dist else 0f
                        x += dirX * speed * dt
                        y += dirY * speed * dt
                        scanPulse = (scanPulse + dt * (if (isMaterialized) 3f else 0.5f)) % 1.0f
                    }

                    if (definition.id == "ENT_VOID_WHALE") {
                        if (x < -300f) x = screenWidth + 300f
                        if (x > screenWidth + 300f) x = -300f
                        x += vx * dt
                        vy = sin(lifetime * 0.3f) * 15f
                        y += vy * dt
                        scanPulse = (sin(lifetime * 0.5f) * 0.5f + 0.5f) * 0.8f + 0.2f
                    }
        }
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
        if (state != ThreatState.ACTIVE) return

        val dx = player.x - x
        val dy = player.y - y
        val distSq = dx * dx + dy * dy

        // EPIC 7: Survey Scanner support (Earlier discovery)
        definition.discoveryType?.let { discoveryType ->
            val baseDiscDist = 500f
            val discDist = baseDiscDist * player.discoveryRangeMultiplier
            if (distSq < discDist * discDist) {
                onDiscovery(discoveryType)
            }
        }

        when (definition.id) {
            "HAZ_LIGHTNING" -> {
                if (distSq < 200f * 200f) {
                    if (phase == 2) { // Striking
                        if (player.invulnerabilityTimer <= 0f) {
                            if (player.shield > 0 && !player.infiniteShield) {
                                player.shield = max(0f, player.shield - 25f)
                                player.shieldRegenPauseTimer = 2f
                            } else if (!player.invincibleHull) {
                                player.integrity = max(0f, player.integrity - 10f)
                            }
                            player.invulnerabilityTimer = 0.5f
                            onVisualFeedback(20f, 0.8f)
                        }
                    }
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "HAZ_DEBRIS" -> {
                if (distSq < 80f * 80f) {
                    if (player.invulnerabilityTimer <= 0f) {
                        if (player.shield > 0 && !player.infiniteShield) {
                            player.shield = max(0f, player.shield - 10f)
                            player.integrity = max(0f, player.integrity - 5f)
                        } else if (player.shield > 0 && player.infiniteShield) {
                            if (!player.invincibleHull) player.integrity = max(0f, player.integrity - 5f)
                        } else if (!player.invincibleHull) {
                            player.integrity = max(0f, player.integrity - 25f)
                        }
                        player.invulnerabilityTimer = 0.8f
                        onVisualFeedback(20f, 0f)
                        onFloatingText("HULL IMPACT", player.x, player.y, Color.Red, false, 1.0f)
                    }
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "HAZ_RADIATION" -> {
                if (distSq < 300f * 300f) {
                    if (!player.infiniteShield) player.shield = max(0f, player.shield - 15f * sdt)
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "HAZ_SOLAR_FLARE" -> {
                if (abs(player.y - y) < 100f) {
                    player.heat = min(Constants.MAX_HEAT, player.heat + 120f * sdt)
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "HAZ_TURBULENCE" -> {
                if (distSq < 450f * 450f) {
                    val dist = sqrt(distSq)
                    val strength = (1f - dist / 450f)
                    val windDir = if (instanceId.hashCode() % 2 == 0) 1f else -1f
                    player.velocityX += windDir * 1200f * strength * sdt
                    player.velocityY += (Random.nextFloat() - 0.5f) * 600f * strength * sdt
                    
                    if (gameTime % 200 < 50) {
                        onBurst(player.x, player.y, 1, Color.White.copy(alpha=0.3f), 100f)
                    }
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "HAZ_GRAVITY" -> {
                if (distSq < 500f * 500f) {
                    val dist = sqrt(distSq)
                    val strength = (1f - dist / 500f)
                    player.velocityY += 1500f * strength * sdt 
                    if (isThrusting) {
                        player.fuel = max(0f, player.fuel - 10f * strength * sdt)
                        player.velocityY += 500f * strength * sdt 
                    }
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "HAZ_EMP" -> {
                val ringRadius = scanPulse * 400f
                val dist = sqrt(distSq)
                if (abs(dist - ringRadius) < 50f) {
                    player.shieldRegenPauseTimer = max(player.shieldRegenPauseTimer, 5f)
                    definition.discoveryType?.let { onDiscovery(it) }
                }
            }
            "ENT_CLOUD_SKIMMER" -> {
                if (distSq < 250f * 250f) {
                    player.velocityY -= 1000f * sdt
                }
            }
            "ENT_ORBITAL_SENTRY" -> {
                if (isTracking && distSq < 400f * 400f) {
                    player.comboFreezeTimer = 1.0f
                    player.fuel = max(0f, player.fuel - 10f * sdt)
                    if (!hasInteracted) {
                        hasInteracted = true
                        onNotification("LOCKED ON", null)
                    }
                    if (projectileCooldown <= 0f) {
                        val ang = atan2(dy, dx)
                        onSpawnProjectile(x, y, cos(ang) * 400f, sin(ang) * 400f, ProjectileType.BOLT, ProjectileOwner.THREAT, 8f, Color(0xFFFF9800), 6f, 3f)
                        projectileCooldown = 2.0f
                    }
                } else {
                    hasInteracted = false
                }
            }
            "ENT_CORRUPTED_HULL" -> {
                if (!hasInteracted && distSq < 150f * 150f) {
                    hasInteracted = true
                    onNotification("DISTRESS SIGNAL DETECTED", null)
                    val type = PowerUpType.entries.random()
                    powerUps.add(PowerUp(x, y, type))
                    onFloatingText("SALVAGE RECOVERED", player.x, player.y, Color.Green, false, 1.0f)
                }
            }
            "HAZ_VOID_ANOMALY" -> {
                if (distSq < 500f * 500f) {
                    val dist = sqrt(distSq)
                    val force = (1f - dist / 500f) * 1200f
                    player.velocityX -= (dx / dist) * force * sdt
                    player.velocityY -= (dy / dist) * force * sdt
                    onVisualFeedback((1f - dist / 500f) * 15f, 0f)
                    player.velocityX += (Random.nextFloat() - 0.5f) * 400f * sdt
                    if (!hasInteracted) {
                        hasInteracted = true
                        onNotification("REALITY DISTORTION", null)
                    }
                } else {
                    hasInteracted = false
                }
            }
            "ENT_SCOUT_DRONE" -> {
                if (isTracking && !firstDetectionShown) {
                    firstDetectionShown = true
                    onAnchoredText(FloatingText("HUMAN PRESENCE DETECTED", x, y - 60f, life = 2.5f, color = Color.White, isCritical = false, sourceThreat = this, anchorOffsetY = -60f, shadowColor = Color(0x80FF1744), shadowBlur = 20f))
                }
                if (isTracking && transmissionProgress >= 1f && !hasReinforced) {
                    onAnchoredText(FloatingText("SIGNAL TRANSMITTED", x, y - 60f, life = 2.5f, color = Color(0xFFFF1744), isCritical = false, sourceThreat = this, anchorOffsetY = -60f, shadowColor = Color(0xFFFF1744), shadowBlur = 25f))
                    onEscalationEvent(x, y, this)
                    hasReinforced = true
                    fleeTimer = 3f
                    onBurst(x, y, 20, Color(0xFFE91E63), 300f)
                }
            }
            "ENT_SWARM_BOTS" -> {
                if (distSq < 150f * 150f) {
                    player.velocityX += (Random.nextFloat() - 0.5f) * 600f * sdt
                    player.velocityY += (Random.nextFloat() - 0.5f) * 600f * sdt
                }
            }
            "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL" -> {
                if (phase in 2..4) {
                    if (distSq < 100f * 100f && player.invulnerabilityTimer <= 0f) {
                        player.fuel = max(0f, player.fuel - 20f)
                        player.heat = min(Constants.MAX_HEAT, player.heat + 30f)
                        player.invulnerabilityTimer = 1.0f
                        onVisualFeedback(15f, 0f)
                        onFloatingText("HULL COLLISION", player.x, player.y, Color.Red, false, 1.0f)
                    }

                    repeat(maxWeakPoints) { i ->
                        val isDestroyed = i >= activeWeakPoints
                        if (!isDestroyed) {
                            val (wx, wy) = when (definition.id) {
                                "MINI_BOSS_COMMANDER" -> Pair(x - 80f + (i * 80f), y - 40f)
                                "BOSS_GATEKEEPER" -> {
                                    val angle = (rotation + i * 90f) * (PI.toFloat() / 180f)
                                    Pair(x + cos(angle) * 250f, y + sin(angle) * 250f)
                                }
                                "BOSS_STAR_EATER" -> Pair(x, y)
                                "BOSS_LEVIATHAN" -> {
                                    val ox = sin(lifetime * 1.5f - i * 0.5f) * 100f
                                    Pair(x + ox, y + i * 60f)
                                }
                                "BOSS_VOID_ENGINE" -> {
                                    val angle = (rotation + i * 180f) * (PI.toFloat() / 180f)
                                    Pair(x + cos(angle) * 150f, y + sin(angle) * 150f)
                                }
                                "BOSS_SIGNAL" -> Pair(x + (Random.nextFloat()-0.5f)*200f, y + (Random.nextFloat()-0.5f)*200f)
                                else -> Pair(x, y)
                            }

                            val ddx = player.x - wx
                            val ddy = player.y - wy
                            val hitDist = if (definition.id == "BOSS_STAR_EATER") 80f else {
                                if (player.rocketType == RocketType.SCOUT) 70f else 50f
                            }

                            if (sqrt(ddx*ddx + ddy*ddy) < hitDist && player.invulnerabilityTimer <= 0f) {
                                activeWeakPoints--
                                player.invulnerabilityTimer = 0.5f
                                player.velocityY = -400f
                                onBurst(wx, wy, 25, Color(0xFF9C27B0), 300f) // SciFiPurple
                                onVisualFeedback(20f, 0f)
                                onFloatingText("WEAK POINT DESTROYED", player.x, player.y, Color(0xFF9C27B0), true, 1.0f)

                                // Trait: Kinetic Mass (Heavy)
                                if (player.rocketType == RocketType.TANK) {
                                    onBurst(wx, wy, 60, Color.White, 800f)
                                    onVisualFeedback(40f, 0.6f)
                                }

                                if (activeWeakPoints <= 0) {
                                    phase = 5 
                                    onScoreUpdate(1000)
                                    onFloatingText("BOSS CRITICAL - RETREATING", player.x, player.y, Color.Cyan, true, 1.0f)
                                }
                            }
                        }
                    }
                }

                when (definition.id) {
                    "MINI_BOSS_COMMANDER" -> {
                        if (phase >= 3) {
                             if (distSq < 400f * 400f) {
                                player.fuel = max(0f, player.fuel - 5f * sdt)
                             }
                             if (gravityPulseTimer < 0.6f) {
                                 player.velocityY += 2500f * sdt
                                 onVisualFeedback(15f, 0f)
                             }
                             if (jamCooldown <= 0f) {
                                 platforms.toList().sortedBy {
                                     val dxP = it.x - player.x
                                     val dyP = it.y - player.y
                                     dxP*dxP + dyP*dyP
                                 }.take(2).forEach {
                                     it.isJammed = true
                                     it.jamTimer = 2.0f
                                 }
                                 jamCooldown = 1.2f
                             }
                               if (projectileCooldown <= 0f) {
                                   val baseAngle = atan2(dy, dx)
                                  onSpawnProjectile(x, y, cos(baseAngle) * 600f, sin(baseAngle) * 600f, ProjectileType.BOLT, ProjectileOwner.THREAT, 20f, Color(0xFFFF1744), 8f, 3f)
                                  onSpawnProjectile(x, y, cos(baseAngle + 0.3f) * 600f, sin(baseAngle + 0.3f) * 600f, ProjectileType.BOLT, ProjectileOwner.THREAT, 20f, Color(0xFFFF1744), 8f, 3f)
                                  onSpawnProjectile(x, y, cos(baseAngle - 0.3f) * 600f, sin(baseAngle - 0.3f) * 600f, ProjectileType.BOLT, ProjectileOwner.THREAT, 20f, Color(0xFFFF1744), 8f, 3f)
                                  projectileCooldown = 1.5f
                              }
                        }
                    }
                    "BOSS_GATEKEEPER" -> {
                        val dist = sqrt(distSq)
                        if (phase == 2 && projectileCooldown <= 0f) {
                            val baseAngle = atan2(dy, dx)
                            for (i in -1..1) {
                                val a = baseAngle + i * 0.35f
                                onSpawnProjectile(x, y, cos(a) * 500f, sin(a) * 500f, ProjectileType.BOLT, ProjectileOwner.THREAT, 15f, Color(0xFFFF9800), 6f, 4f)
                            }
                            projectileCooldown = 2.0f
                        }
                        if (dist in 150f..350f) {
                            val angle = atan2(dy, dx) * (180f / PI.toFloat())
                            val relAngle = (angle - rotation + 360f) % 360f
                            val inGap = relAngle % 90f < 50f
                            if (!inGap) {
                                player.velocityX += (dx / dist) * 4000f * sdt
                                player.heat += 100f * sdt
                                onVisualFeedback(5f, 0f)
                            }
                        }
                        if (phase == 3 && dist < 600f) {
                            player.velocityX -= (dx / dist) * 1500f * sdt
                            player.velocityY -= (dy / dist) * 1500f * sdt
                        }
                        if (phase == 3 && threatSpawnCooldown <= 0f) {
                            onSpawnThreat("ENT_SCOUT_DRONE", x + Random.nextFloat() * 200f - 100f, y + 200f, 0f, -100f)
                            onSpawnThreat("ENT_SCOUT_DRONE", x + Random.nextFloat() * 200f - 100f, y + 300f, 0f, -150f)
                            threatSpawnCooldown = 4.0f
                        }
                    }
                    "BOSS_STAR_EATER" -> {
                        val dist = sqrt(distSq)
                        if (dist < 1000f) {
                            val suctionForce = (1f - dist / 1000f) * 3000f
                            player.velocityX -= (dx / dist) * suctionForce * sdt
                            player.velocityY -= (dy / dist) * suctionForce * sdt

                            if (dist < 200f) {
                                player.fuel = max(0f, player.fuel - 25f * sdt)
                            }
                        }
                        powerUps.forEach { pu ->
                            val pdx = x - pu.x
                            val pdy = y - pu.y
                            val pdist = sqrt(pdx*pdx + pdy*pdy)
                            if (pdist < 1200f) {
                                pu.y += (pdy / pdist) * 800f * sdt
                                pu.x += (pdx / pdist) * 800f * sdt
                            }
                        }
                        if (phase == 2 && projectileCooldown <= 0f) {
                            onSpawnProjectile(x, y - 30f, cos(atan2(dy, dx)) * 300f, sin(atan2(dy, dx)) * 300f, ProjectileType.WAVE, ProjectileOwner.THREAT, 12f, Color(0xFFE040FB), 20f, 5f)
                            projectileCooldown = 4.0f
                        }
                        if (phase == 3 && projectileCooldown <= 0f) {
                            val ang = atan2(dy, dx)
                            onSpawnProjectile(x, y - 30f, cos(ang) * 400f, sin(ang) * 400f, ProjectileType.MISSILE, ProjectileOwner.THREAT, 15f, Color(0xFF9C27B0), 10f, 4f)
                            projectileCooldown = 2.5f
                        }
                    }
                    "BOSS_LEVIATHAN" -> {
                        val speedMul = if (phase == 3) 1.5f else 1.0f
                        repeat(6) { i ->
                            val ox = sin(lifetime * 1.5f - i * 0.5f) * 150f
                            val oy = i * 80f
                            val segX = x + ox
                            val segY = y + oy

                            val sdx = player.x - segX
                            val sdy = player.y - segY
                            if (sdx*sdx + sdy*sdy < 300f * 300f) {
                                val tailMul = if (i >= 4) 3f else 1f
                                val forceStrength = (1f - sqrt(sdx*sdx + sdy*sdy) / 300f) * 3000f * speedMul * tailMul
                                player.velocityX += (sdx / max(1f, sqrt(sdx*sdx + sdy*sdy))) * forceStrength * sdt
                                if (sdy > 20f && sdx*sdx + sdy*sdy < 200f * 200f) {
                                    player.velocityY -= 4500f * sdt * tailMul
                                    if (Random.nextFloat() < 0.4f) {
                                        onBurst(player.x, player.y + 50f, 10, Color.Cyan, 300f)
                                    }
                                }
                            }
                        }
                        if (phase == 2) {
                            val shrinkMargin = 100f + (1f - activeWeakPoints.toFloat() / maxWeakPoints) * 100f
                            if (player.x < shrinkMargin) player.velocityX += 3000f * sdt
                            if (player.x > screenWidth - shrinkMargin) player.velocityX -= 3000f * sdt
                        }
                        if (phase == 3 && (player.x < 50f || player.x > screenWidth - 50f)) {
                            player.velocityY -= 100000f * sdt
                        }
                        if (phase == 3) {
                            val dxMaw = player.x - x
                            val dyMaw = player.y - y
                            if (dxMaw*dxMaw + dyMaw*dyMaw < 80f * 80f) {
                                player.heat = min(Constants.MAX_HEAT, player.heat + 100f * sdt)
                                onVisualFeedback(25f, 0.4f)
                                if (Random.nextFloat() < 0.02f) {
                                    onFloatingText("MAW CORE", player.x, player.y, Color.Cyan, true, 1.0f)
                                }
                            }
                        }
                        if (phase >= 2 && projectileCooldown <= 0f) {
                            val ang = atan2(dy, dx)
                            if (phase == 2) {
                                onSpawnProjectile(x, y + 40f, cos(ang) * 500f, sin(ang) * 500f, ProjectileType.BOLT, ProjectileOwner.THREAT, 10f, Color(0xFF00BCD4), 8f, 4f)
                                projectileCooldown = 3.0f
                            }
                            if (phase == 3) {
                                onSpawnProjectile(x, y + 40f, cos(ang) * 600f, sin(ang) * 600f, ProjectileType.BEAM, ProjectileOwner.THREAT, 15f, Color(0xFF00BCD4), 6f, 3f)
                                projectileCooldown = 2.0f
                            }
                        }
                    }
                    "BOSS_VOID_ENGINE" -> {
                        if (phase >= 2) {
                            if (phase == 2) {
                                val shiftStrength = 4800f
                                val shiftDir = if (sin(lifetime * 2f) > 0f) 1f else -1f
                                player.velocityX += shiftStrength * shiftDir * sdt
                                if (shakeCooldown <= 0f) {
                                    onVisualFeedback(10f, 0.4f)
                                    shakeCooldown = 0.5f
                                }
                                if (threatSpawnCooldown <= 0f) {
                                    onSpawnThreat("HAZ_VOID_ANOMALY", x + Random.nextFloat() * 300f - 150f, y + 100f, 0f, -50f)
                                    threatSpawnCooldown = 5.0f
                                }
                            }
                            if (phase == 3) {
                                player.velocityY += 4800f * sdt
                                if (shakeCooldown <= 0f) {
                                    onVisualFeedback(10f, 0.4f)
                                    shakeCooldown = 0.5f
                                }
                                if (Random.nextFloat() < 0.012f) {
                                    onFloatingText("GRAVITY FLUX", player.x, player.y - 60f, Color(0xFFFF1744), true, 1.5f)
                                    onVisualFeedback(20f, 0.6f)
                                    onBurst(player.x, player.y, 30, Color(0xFFFF1744), 500f)
                                    player.controlInversionTimer = 2.5f
                                    onFloatingText("CONTROL INVERTED", player.x, player.y, Color(0xFFE91E63), true, 1.0f)
                                }
                            }
                        }
                        if (phase == 2 && projectileCooldown <= 0f) {
                            onSpawnProjectile(x, y, 0f, 200f, ProjectileType.WAVE, ProjectileOwner.THREAT, 8f, Color(0xFFE91E63), 25f, 4f)
                            projectileCooldown = 3.5f
                        }
                        if (phase == 3 && projectileCooldown <= 0f) {
                            val ang = atan2(dy, dx)
                            for (i in -1..1) {
                                val a = ang + i * 0.35f
                                onSpawnProjectile(x, y, cos(a) * 500f, sin(a) * 500f, ProjectileType.BOLT, ProjectileOwner.THREAT, 12f, Color(0xFFE91E63), 8f, 3f)
                            }
                            projectileCooldown = 2.0f
                        }
                    }
                    "BOSS_SIGNAL" -> {
                        if (phase == 3 && activeWeakPoints <= 0) {
                            phase = 4
                            localTimer = 0f
                            onFloatingText("OVERLOAD INITIATED", x, y - 80f, Color.Cyan, true, 2.0f)
                        }
                        if (distSq < 800f * 800f) {
                            if (phase == 2) {
                                if (Random.nextFloat() < 0.05f) {
                                    onNotification("SIGNAL LOSS...", 0.5f)
                                }
                                if (Random.nextFloat() < (0.01f + localTimer * 0.002f)) {
                                    player.hudInterferenceTimer = 2.5f + localTimer * 0.3f
                                }
                            }
                            if (phase == 3) {
                                if (Random.nextFloat() < 0.1f) {
                                    onNotification("SIGNAL LOSS...", 0.5f)
                                }
                                player.heat += 40f * sdt
                                player.velocityX *= (1f - sdt)
                                player.velocityY *= (1f - sdt)
                                health = min(definition.baseHealth, health + 20f * sdt)
                                if (Random.nextFloat() < 0.01f) {
                                    onFloatingText("MIRAGE", player.x, player.y - 60f, Color(0xFFE91E63), true, 1.0f)
                                }
                            }
                            if (phase == 4) {
                                player.velocityY += 4000f * sdt
                                player.velocityX *= (1f - sdt * 2f)
                                onVisualFeedback(15f, 0.3f)
                            }
                        }
                        if (phase == 2 && projectileCooldown <= 0f) {
                            val jitterX = (Random.nextFloat() - 0.5f) * 40f
                            val jitterY = (Random.nextFloat() - 0.5f) * 40f
                            val ang = atan2(dy + jitterY, dx + jitterX)
                            onSpawnProjectile(x + jitterX, y + jitterY, cos(ang) * 450f, sin(ang) * 450f, ProjectileType.BOLT, ProjectileOwner.THREAT, 10f, Color(0xFFFF1744), 7f, 4f)
                            projectileCooldown = 3.0f
                        }
                        if (phase == 3 && projectileCooldown <= 0f) {
                            val ang = atan2(dy, dx)
                            onSpawnProjectile(x, y, cos(ang) * 550f, sin(ang) * 550f, ProjectileType.BEAM, ProjectileOwner.THREAT, 12f, Color(0xFFFF1744), 5f, 2.5f)
                            projectileCooldown = 1.5f
                        }
                        if (phase >= 2 && phase <= 3) {
                            val ghostRate = if (phase == 3) 0.25f else 0.15f
                            if (Random.nextFloat() < ghostRate) {
                                onSpawnGhostPlatform(Random.nextFloat() * screenWidth, cameraY + Random.nextFloat() * screenHeight)
                            }
                        }
                    }
                    "ENT_STALKER" -> {
                        if (isTracking && distSq < 120f * 120f && player.invulnerabilityTimer <= 0f) {
                            val dmg = 10f + alertLevel * 20f
                            player.heat = min(Constants.MAX_HEAT, player.heat + dmg)
                            player.invulnerabilityTimer = 0.8f
                            onVisualFeedback(10f + alertLevel * 15f, 0.2f)
                            onBurst(x, y, 5, Color(0xFFFF1744), 200f)
                        }
                        if (alertLevel > 0.5f && projectileCooldown <= 0f) {
                            val ang = atan2(dy, dx)
                            onSpawnProjectile(x, y, cos(ang) * 500f, sin(ang) * 500f, ProjectileType.BOLT, ProjectileOwner.THREAT, 8f, Color(0xFFFF1744), 5f, 3f)
                            projectileCooldown = 1.5f
                        }
                    }
                    "ENT_VOID_WRAITH" -> {
                        if (isMaterialized && distSq < 100f * 100f && player.invulnerabilityTimer <= 0f) {
                            if (!player.invincibleHull) player.integrity = max(0f, player.integrity - 15f)
                            player.fuel = max(0f, player.fuel - 30f)
                            player.invulnerabilityTimer = 1.5f
                            onVisualFeedback(25f, 0.6f)
                            onBurst(x, y, 15, Color(0xFFD500F9), 400f)
                        }
                    }
                    "ENT_VOID_WHALE" -> {
                        val dist = sqrt(distSq)
                        if (dist < 500f) {
                            val force = (1f - dist / 500f) * 30000f
                            val side = if (vx > 0f) -1f else 1f
                            player.velocityX += side * force * sdt
                            if (dist < 200f) {
                                player.velocityY -= 45000f * sdt
                                onVisualFeedback(5f, 0f)
                            }
                        }
                    }
                }

                if (phase == 5 && !hasInteracted) {
                    hasInteracted = true
                    powerUps.add(PowerUp(player.x, cameraY + 200f, PowerUpType.ARTIFACT))
                    onFloatingText("!!! ${definition.name.uppercase()} DEFEATED !!!", player.x, player.y, Color.Cyan, true, 1.0f)
                    onNotification(">>> MISSION DATA RECOVERED <<<", 5.0f)
                    onVisualFeedback(70f, 1.0f)
                    onBurst(player.x, cameraY + 200f, 100, Color(0xFF9C27B0), 1200f)
                    
                    repeat(8) {
                        onBurst(player.x + (Random.nextFloat() - 0.5f) * 200f, 
                                   player.y + (Random.nextFloat() - 0.5f) * 200f, 
                                   50, Color.White, 800f)
                    }

                    if (definition.id == "MINI_BOSS_COMMANDER") {
                        onMissionProgress(MissionType.BOSS)
                    }
                }
            }
        }
    }
}
