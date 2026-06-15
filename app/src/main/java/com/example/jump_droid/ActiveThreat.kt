package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

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
    var duration by mutableFloatStateOf(5f) // Default duration for temporary threats
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
    
    // Boss Mechanic States
    var gravityPulseTimer by mutableFloatStateOf(0f)
    var pulseAlpha by mutableFloatStateOf(0f)
    var jamCooldown by mutableFloatStateOf(0f)
    var bossRewardDropped by mutableStateOf(false)
    
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
    fun update(dt: Float, screenWidth: Float = 1000f, targetX: Float = 0f, targetY: Float = 0f) {
        lifetime += dt
        
        if (state == ThreatState.SPAWNING && lifetime > 0.5f) {
            state = ThreatState.ACTIVE
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
                    if (lifetime > duration) {
                        state = ThreatState.DESTROYED
                    }

                    if (definition.id == "HAZ_STORM") {
                        // Static Discharge: slow drifting storm pocket
                        x += vx * 0.5f * dt
                        y += vy * 0.5f * dt
                    }

                    if (definition.id == "HAZ_VOID_ANOMALY" || definition.id == "HAZ_VOID_TEAR") {
                        // Void Anomaly: slow pulsing drift
                        scanPulse = (scanPulse + dt * 0.5f) % 1.0f
                    }
                }
                ThreatType.ENEMY -> {
                    // Awareness logic for Surveyor Probe
                    if (definition.id == "ENT_SCOUT_DRONE") {
                        val dx_dist = targetX - x
                        val dy_dist = targetY - y
                        val distToTarget = kotlin.math.sqrt(dx_dist * dx_dist + dy_dist * dy_dist)
                        
                        val detectionRange = 400f
                        
                        if (distToTarget < detectionRange) {
                            if (!isTracking) {
                                isTracking = true
                                state = ThreatState.ACTIVE // Ensure state is correct
                            }
                            
                            // Calculate angle to player
                            val dx = targetX - x
                            val dy = targetY - y
                            targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                            
                            // Scan animation pulse
                            scanPulse = (scanPulse + dt * 2f) % 1.0f
                            
                            // Slow move toward player (optional, keep subtle)
                            x += (targetX - x) * 0.2f * dt
                            y += (targetY - y) * 0.2f * dt
                            
                        } else {
                            if (isTracking) {
                                isTracking = false
                                patrolTimer = 0f // Reset patrol
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
                            y += vy * dt // Still apply hover oscillation below
                        }
                        
                        // Hover oscillation
                        if (!isTracking) {
                            vy = kotlin.math.sin(lifetime * 3f) * 20f
                        } else {
                            vy = 0f // Steady while scanning
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

                    if (definition.id == "MINI_BOSS_COMMANDER") {
                        localTimer += dt
                        gravityPulseTimer += dt
                        jamCooldown -= dt

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
                                scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                                x += (targetX - x) * 0.2f * dt
                                if (localTimer > 12f || activeWeakPoints < maxWeakPoints) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Gravity Destabilization
                                rotation += 480f * dt
                                scanPulse = (scanPulse + dt * 3.0f) % 1.0f
                                x += (targetX - x) * 0.5f * dt
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
                        if (!isArrived) {
                            arrivalTimer += dt
                            vy = 90f
                            if (arrivalTimer > arrivalDuration) {
                                isArrived = true
                                phase = 2
                                localTimer = 0f
                                vx = 100f // Start swimming sideways
                            }
                            return
                        }

                        when (phase) {
                            2 -> { // Majestic Glide
                                x += vx * dt
                                vy = kotlin.math.sin(lifetime * 1.2f) * 100f
                                if (x < 50f) vx = abs(vx)
                                if (x > screenWidth - 50f) vx = -abs(vx)
                                if (localTimer > 15f || activeWeakPoints < maxWeakPoints) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Aggressive Thrash
                                x += vx * 3f * dt
                                vy = kotlin.math.sin(lifetime * 2.5f) * 200f
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
                                scanPulse = (scanPulse + dt * 2.5f) % 1.0f
                                x += (targetX - x) * 0.05f * dt
                                if (localTimer > 15f || activeWeakPoints <= 0) {
                                    phase = 3
                                    localTimer = 0f
                                }
                            }
                            3 -> { // Hallucinations
                                scanPulse = (scanPulse + dt * 5.0f) % 1.0f
                                x += (targetX - x) * 0.2f * dt
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
                        // Orbital Sentry: Stationary rotation
                        rotation += 20f * dt
                        
                        // Periodic "Radar Scan" pulse
                        scanPulse = (scanPulse + dt * 0.3f) % 1.0f
                    }

                    if (definition.id == "ENT_CORRUPTED_HULL") {
                        // Derelict Echo: Drifting and tumbling
                        rotation += 15f * dt
                        x += vx * dt
                        y += vy * dt
                        
                        // Periodic signal pulse
                        scanPulse = (scanPulse + dt * 0.4f) % 1.0f
                    }
                }
                else -> {}
            }
        }
    }
}
