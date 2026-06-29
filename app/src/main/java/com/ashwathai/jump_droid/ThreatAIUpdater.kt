package com.ashwathai.jump_droid

import kotlin.math.*
import kotlin.random.Random

fun ActiveThreat.updateAI(
    dt: Float,
    screenWidth: Float = 1000f,
    targetX: Float = 0f,
    targetY: Float = 0f,
    targetHeat: Float = 0f,
    targetOverheated: Boolean = false,
    powerUps: List<PowerUp> = emptyList(),
    activeThreats: List<ActiveThreat> = emptyList()
) {
    lifetime += dt
    
    // --- Life Cycle Calculation (Adjustment Run 1) ---
    val lifeRatio = (lifetime / duration).coerceIn(0f, 1f)
    if (definition.type == ThreatType.HAZARD) {
        phase = when {
            lifeRatio < 0.15f -> 1 
            lifeRatio < 0.85f -> 2
            else -> 3
        }
    }

    if (state == ThreatState.SPAWNING && lifetime > 0.5f) {
        state = ThreatState.ACTIVE
        
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

        if (definition.type == ThreatType.MINI_BOSS || definition.type == ThreatType.BOSS) {
            maxWeakPoints = when (definition.id) {
                "MINI_BOSS_COMMANDER" -> 3
                "BOSS_GATEKEEPER" -> 4
                "BOSS_STAR_EATER" -> 3
                "BOSS_LEVIATHAN" -> 3
                "BOSS_VOID_ENGINE" -> 2
                "BOSS_SIGNAL" -> 3
                "MINI_BOSS_THERMAL_HIVE" -> 2
                "MINI_BOSS_GRAVITY_ANCHOR" -> 2
                "MINI_BOSS_FORGER" -> 3
                "BOSS_ARCHITECT" -> 4
                "BOSS_ENTROPY_CORE" -> 4
                "BOSS_SINGULARITY" -> 4
                else -> 1
            }
            activeWeakPoints = maxWeakPoints
            wpDestroyedMask = 0
        }
    }

    if (!isTracking) {
        x += vx * dt
        y += vy * dt
    }

    if (state == ThreatState.ACTIVE) {
        when (definition.type) {
            ThreatType.HAZARD -> {
                localTimer += dt
                
                when (definition.id) {
                    "HAZ_LIGHTNING" -> {
                        if (lifetime > duration) {
                            if (phase != 4) { phase = 4; destructionTimer = 0f }
                            destructionTimer += dt
                            if (destructionTimer > 1.5f) { state = ThreatState.DESTROYED }
                        } else {
                            val cycleTime = 4f
                            val telegraphTime = 1.5f
                            val strikeTime = 0.5f
                            val t = lifetime % cycleTime
                            
                            val subPhase = if (t < telegraphTime) 1 else if (t < telegraphTime + strikeTime) 2 else 3
                            scanPulse = if (subPhase == 1) t / telegraphTime else if (subPhase == 2) 1.0f else 0f
                            
                            val dx = targetX - x
                            val dy = targetY - y
                            x += dx * 0.4f * dt
                            y += dy * 0.15f * dt
                        }
                    }
                    "HAZ_SOLAR_FLARE" -> {
                        y += 400f * dt
                        if (y > targetY + 1500f) state = ThreatState.DESTROYED
                    }
                    "HAZ_EMP" -> {
                        scanPulse += dt * 0.8f
                        if (scanPulse > 3.0f) state = ThreatState.DESTROYED
                    }
                    "HAZ_RADIATION" -> {
                        scanPulse = (sin(lifetime * 2f) * 0.5f + 0.5f)
                    }
                    "HAZ_TURBULENCE" -> {
                        scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                        x += vx * dt
                        y += vy * dt
                    }
                    "HAZ_DEBRIS" -> {
                        rotation += 60f * dt
                        x += vx * dt
                        y += vy * dt
                        if (y > targetY + 2000f) state = ThreatState.DESTROYED
                    }
                    "HAZ_GRAVITY" -> {
                        scanPulse = (scanPulse + dt * 0.5f) % 1.0f
                        x += (screenWidth / 2f - x) * 0.05f * dt
                        y += vy * 0.5f * dt
                    }
                    "HAZ_VOID_ANOMALY" -> {
                        scanPulse = (sin(lifetime * 1.5f) * 0.5f + 0.5f)
                        x += (screenWidth / 2f - x) * 0.03f * dt
                        y += vy * dt
                    }
                    "HAZ_CRYO_MIST" -> {
                        // Slowly drifts
                        x += vx * 0.5f * dt
                        y += vy * 0.5f * dt
                    }
                    "HAZ_MIRROR_SHARDS" -> {
                        rotation += 90f * dt
                        x += vx * dt
                        y += vy * dt
                    }
                    "HAZ_GRAVITY_SHEAR" -> {
                        // Pulsates
                        scanPulse = (sin(lifetime * 3f) * 0.5f + 0.5f)
                    }
                    else -> if (lifetime > duration) state = ThreatState.DESTROYED
                }
            }
            ThreatType.ENEMY -> {
                if (definition.id == "ENT_SCOUT_DRONE") {
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
                        
                        val dx = targetX - x
                        val dy = targetY - y
                        targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                        
                        if (transmissionProgress < 1f) {
                            transmissionProgress += dt / 5f
                            transmissionProgress = transmissionProgress.coerceIn(0f, 1f)
                            scanPulse = transmissionProgress
                        } else {
                            scanPulse = (scanPulse + dt * 6f) % 1.0f
                        }
                        
                        x += (targetX - x) * 0.2f * dt
                        y += (targetY - y) * 0.2f * dt
                        
                    } else {
                        if (isTracking) {
                            isTracking = false
                            transmissionProgress = 0f
                            firstDetectionShown = false
                            patrolTimer = 0f
                        }
                        
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
                    
                    if (!isTracking) {
                        vy = sin(lifetime * 3f) * 20f
                    } else {
                        vy = 0f
                    }
                }

                if (definition.id == "ENT_SWARM_BOTS") {
                    patrolTimer += dt
                    if (patrolTimer > 2f) {
                        val angle = (Random.nextFloat() * 2f * kotlin.math.PI.toFloat())
                        val speed = 100f + Random.nextFloat() * 100f
                        vx = kotlin.math.cos(angle) * speed
                        vy = kotlin.math.sin(angle) * speed
                        patrolTimer = 0f
                    }

                    if (x < 100f) vx = abs(vx)
                    if (x > screenWidth - 100f) vx = -abs(vx)
                    
                    x += vx * dt
                    y += vy * dt
                    
                    scanPulse = (sin(lifetime * 2f) * 0.5f + 0.5f)
                }

                if (definition.id == "ENT_HEAT_BAT") {
                    val dx = targetX - x
                    val dy = targetY - y
                    val dist = sqrt(dx*dx + dy*dy)
                    
                    val isDiving = targetHeat >= 70f || targetOverheated
                    val speed = if (isDiving) 350f else 120f
                    
                    if (dist > 5f) {
                        x += (dx / dist) * speed * dt
                        y += (dy / dist) * speed * dt
                    }
                }

                if (definition.id == "ENT_VOID_HARVESTER") {
                    val targetPu = powerUps.minByOrNull { sqrt((it.x - x).pow(2) + (it.y - y).pow(2)) }
                    if (targetPu != null) {
                        val dx = targetPu.x - x
                        val dy = targetPu.y - y
                        val dist = sqrt(dx*dx + dy*dy)
                        val speed = 250f
                        if (dist > 5f) {
                            x += (dx / dist) * speed * dt
                            y += (dy / dist) * speed * dt
                        }
                    } else {
                        // Drift near center
                        x += (screenWidth / 2f - x) * 0.5f * dt
                        y += vy * dt
                    }
                }

                if (definition.id == "ENT_PHASE_WRAITH") {
                    localTimer += dt
                    if (localTimer > 4f) {
                        // Teleport near player
                        x = targetX + (Random.nextFloat() - 0.5f) * 300f
                        y = targetY + (Random.nextFloat() - 0.5f) * 300f
                        localTimer = 0f
                    }
                    isMaterialized = targetOverheated
                }

                if (definition.id == "ENT_GRAVITY_RAM") {
                    localTimer += dt
                    val chargeTime = 2f
                    val dashTime = 1f
                    val totalCycle = chargeTime + dashTime
                    
                    val cyclePos = localTimer % totalCycle
                    if (cyclePos < chargeTime) {
                        // Tracking/Charging phase
                        x += (targetX - x) * 0.8f * dt
                        state = ThreatState.ACTIVE // We'll use ACTIVE for charging
                    } else {
                        // Dash phase
                        y += 800f * dt
                        if (y > targetY + 1000f) {
                            localTimer = 0f // Reset to start another charge
                            y = targetY - 600f // Reposition above
                        }
                    }
                }
            }
            else -> {}
        }

        if (definition.id == "MINI_BOSS_COMMANDER") {
            localTimer += dt
            gravityPulseTimer += dt
            jamCooldown -= dt
            projectileCooldown -= dt

            if (gravityPulseTimer > 2.5f) {
                gravityPulseTimer = 0f
                pulseAlpha = 1.0f
            }
            if (pulseAlpha > 0) pulseAlpha -= dt * 1.5f

            if (!isArrived) {
                arrivalTimer += dt
                vy = 150f
                if (arrivalTimer > arrivalDuration) {
                    isArrived = true
                    phase = 2
                    localTimer = 0f
                }
                return
            }

            if (activeWeakPoints <= 0 && phase < 5) { phase = 5; localTimer = 0f }

            when (phase) {
                2 -> {
                    isTracking = true
                    vy = sin(lifetime * 1.5f) * 50f
                    x += (targetX - x) * 0.7f * dt
                    y += (targetY - y) * 0.30f * dt
                    
                    val dx = targetX - x
                    val dy = targetY - y
                    targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                    scanPulse = (scanPulse + dt * 1.2f) % 1.0f 
                    
                    if (localTimer > 6f) {
                        phase = 3
                        localTimer = 0f
                    }
                }
                3 -> {
                    isTracking = true
                    vy = sin(lifetime * 2.5f) * 80f
                    x += (targetX - x) * 0.9f * dt
                    y += (targetY - y) * 0.30f * dt
                    scanPulse = (scanPulse + dt * 2.5f) % 1.0f
                    
                    val dx = targetX - x
                    val dy = targetY - y
                    targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                    
                    if (localTimer > 8f) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    isTracking = true
                    vy = sin(lifetime * 2.5f) * 70f
                    x += (targetX - x) * 0.9f * dt
                    y += (targetY - y) * 0.30f * dt
                    scanPulse = (scanPulse + dt * 3.0f) % 1.0f
                    
                    val dx = targetX - x
                    val dy = targetY - y
                    targetAngle = kotlin.math.atan2(dy, dx) * (180f / kotlin.math.PI.toFloat())
                    
                    if (localTimer > 15f) {
                        phase = 5
                        localTimer = 0f
                        isTracking = false
                    }
                }
                5 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

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

            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }

            when (phase) {
                2 -> {
                    rotation += (80f + (1f - activeWeakPoints.toFloat()/maxWeakPoints) * 80f) * dt
                    scanPulse = (scanPulse + dt * 0.8f) % 1.0f
                    x += (targetX - x) * 0.3f * dt
                    y += (targetY - y) * 0.40f * dt
                    if (localTimer > 10f || activeWeakPoints < maxWeakPoints) {
                        phase = 3
                        localTimer = 0f
                    }
                }
                3 -> {
                    val dir = if ((localTimer.toInt() / 3) % 2 == 0) 1f else -1f
                    rotation += (120f * dir) * dt
                    scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                    x += (targetX - x) * 0.5f * dt
                    y += (targetY - y) * 0.40f * dt
                    
                    if (localTimer > 12f || activeWeakPoints <= 0) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
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

            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }

            when (phase) {
                2 -> {
                    scanPulse = (scanPulse + dt * 0.5f) % 1.0f
                    x += (targetX - x) * 0.3f * dt
                    y += (targetY - y) * 0.3f * dt
                    if (localTimer > 12f || activeWeakPoints <= 0) {
                        phase = 3
                        localTimer = 0f
                    }
                }
                3 -> {
                    scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                    x += (targetX - x) * 0.6f * dt
                    y += (targetY - y) * 0.35f * dt
                    vy = sin(lifetime * 3f) * 150f
                    if (localTimer > 8f) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
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

            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }

            when (phase) {
                2 -> {
                    rotation += 240f * dt
                    val pulsePhase = sin(lifetime * 2f)
                    scanPulse = (pulsePhase * 0.5f + 0.5f)
                    x += (targetX - x) * 0.4f * dt
                    y += (targetY - y) * 0.40f * dt
                    if (localTimer > 12f || activeWeakPoints < maxWeakPoints) {
                        phase = 3
                        localTimer = 0f
                    }
                }
                3 -> {
                    rotation += 480f * dt
                    scanPulse = (scanPulse + dt * 3.0f) % 1.0f
                    val shift = sin(lifetime * 3f) * 2f
                    x += (targetX - x) * (0.5f + shift * 0.1f) * dt
                    y += (targetY - y) * 0.40f * dt
                    if (localTimer > 15f || activeWeakPoints <= 0) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
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

            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }

            when (phase) {
                2 -> {
                    x += vx * dt
                    vy = sin(lifetime * 1.2f) * 100f
                    y += (targetY - y) * 0.25f * dt
                    scanPulse = (sin(lifetime * 0.5f) * 0.5f + 0.5f) * 0.6f
                    if (x < 50f) vx = abs(vx)
                    if (x > screenWidth - 50f) vx = -abs(vx)
                    if (localTimer > 15f || activeWeakPoints < maxWeakPoints) {
                        phase = 3
                        localTimer = 0f
                    }
                }
                3 -> {
                    val speedMul = 1f + (1f - activeWeakPoints.toFloat() / maxWeakPoints) * 2f
                    x += vx * 3f * speedMul * dt
                    vy = sin(lifetime * 2.5f) * 200f
                    y += (targetY - y) * 0.25f * dt
                    scanPulse = (sin(lifetime * 3f) * 0.5f + 0.5f)
                    if (x < 20f) vx = abs(vx)
                    if (x > screenWidth - 20f) vx = -abs(vx)
                    if (localTimer > 15f || activeWeakPoints <= 0) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    vy = 0f
                    x += vx * 0.5f * dt
                    vx *= 0.95f
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

            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }

            when (phase) {
                2 -> {
                    scanPulse = (sin(lifetime * 5f) * 0.5f + 0.5f)
                    x += (targetX - x) * 0.15f * dt
                    y += (targetY - y) * 0.25f * dt
                    if (localTimer > 15f || activeWeakPoints <= 0) {
                        phase = 3
                        localTimer = 0f
                    }
                }
                3 -> {
                    scanPulse = (sin(lifetime * 8f) * 0.5f + 0.5f)
                    val fakeDrift = sin(lifetime * 2f) * 2f
                    x += (targetX - x) * (0.25f + fakeDrift * 0.05f) * dt
                    y += (targetY - y) * 0.25f * dt
                    vy = (sin(lifetime * 3f) * 150f)
                    if (localTimer > 20f) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

        if (definition.id == "MINI_BOSS_THERMAL_HIVE") {
            localTimer += dt
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
            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }

            when (phase) {
                2 -> {
                    x += (targetX - x) * 0.3f * dt
                    vy = sin(lifetime * 2f) * 40f
                    y += (targetY - y) * 0.30f * dt
                    if (targetHeat > 60f && threatSpawnCooldown <= 0f) {
                        threatSpawnCooldown = 3f
                    }
                    if (activeWeakPoints < maxWeakPoints || localTimer > 15f) phase = 3
                }
                3 -> {
                    x += (targetX - x) * 0.5f * dt
                    vy = sin(lifetime * 4f) * 80f
                    y += (targetY - y) * 0.30f * dt
                    if (activeWeakPoints <= 0) {
                        phase = 4
                        localTimer = 0f
                    }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

        if (definition.id == "MINI_BOSS_GRAVITY_ANCHOR") {
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
            localTimer += dt
            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }
            when (phase) {
                2 -> {
                    alertLevel = (localTimer / 10f).coerceIn(0f, 2f)
                    vy = 0f
                    scanPulse = (scanPulse + dt * 0.3f) % 1.0f
                    if (activeWeakPoints < maxWeakPoints || localTimer > 15f) { phase = 3; localTimer = 0f }
                }
                3 -> {
                    alertLevel = (2f + localTimer / 8f).coerceIn(0f, 3f)
                    vy = 0f
                    scanPulse = (scanPulse + dt * 1.0f) % 1.0f
                    if (activeWeakPoints <= 0) { phase = 4; localTimer = 0f }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

        if (definition.id == "MINI_BOSS_FORGER") {
            localTimer += dt
            jamCooldown -= dt
            if (!isArrived) {
                arrivalTimer += dt
                vy = 140f
                if (arrivalTimer > arrivalDuration) {
                    isArrived = true
                    phase = 2
                    localTimer = 0f
                }
                return
            }
            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }
            when (phase) {
                2 -> {
                    x += (targetX - x) * 0.4f * dt
                    y += (targetY - y) * 0.20f * dt
                    vy = sin(lifetime * 1.5f) * 30f
                    scanPulse = (scanPulse + dt * 0.5f) % 1.0f
                    if (activeWeakPoints < maxWeakPoints || localTimer > 12f) { phase = 3; localTimer = 0f }
                }
                3 -> {
                    x += (targetX - x) * 0.5f * dt
                    y += (targetY - y) * 0.20f * dt
                    vy = sin(lifetime * 2.5f) * 50f
                    scanPulse = (scanPulse + dt * 1.5f) % 1.0f
                    if (activeWeakPoints <= 0) { phase = 4; localTimer = 0f }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

        if (definition.id == "BOSS_ARCHITECT") {
            localTimer += dt
            threatSpawnCooldown -= dt
            projectileCooldown -= dt
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
            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }
            when (phase) {
                2 -> {
                    rotation += 30f * dt
                    scanPulse = (scanPulse + dt * 0.4f) % 1.0f
                    x += (targetX - x) * 0.3f * dt
                    y += (targetY - y) * 0.20f * dt
                    if (activeWeakPoints < maxWeakPoints || localTimer > 12f) { phase = 3; localTimer = 0f }
                }
                3 -> {
                    rotation += 60f * dt
                    scanPulse = (scanPulse + dt * 1.2f) % 1.0f
                    x += (targetX - x) * 0.5f * dt
                    y += (targetY - y) * 0.20f * dt
                    if (activeWeakPoints <= 0) { phase = 4; localTimer = 0f }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

        if (definition.id == "BOSS_ENTROPY_CORE") {
            localTimer += dt
            projectileCooldown -= dt
            if (!isArrived) {
                arrivalTimer += dt
                vy = 60f
                if (arrivalTimer > arrivalDuration) {
                    isArrived = true
                    phase = 2
                    localTimer = 0f
                }
                return
            }
            if (activeWeakPoints <= 0 && phase < 4) { phase = 4; localTimer = 0f }
            when (phase) {
                2 -> {
                    scanPulse = (sin(lifetime * 2.5f) * 0.5f + 0.5f)
                    x += (targetX - x) * 0.2f * dt
                    y += (targetY - y) * 0.20f * dt
                    if (activeWeakPoints < maxWeakPoints || localTimer > 14f) { phase = 3; localTimer = 0f }
                }
                3 -> {
                    scanPulse = (sin(lifetime * 4f) * 0.5f + 0.5f)
                    x += (targetX - x) * 0.35f * dt
                    y += (targetY - y) * 0.20f * dt
                    if (activeWeakPoints <= 0) { phase = 4; localTimer = 0f }
                }
                4 -> {
                    vy = 0f
                    vx *= 0.95f
                }
            }
        }

        if (definition.id == "BOSS_SINGULARITY") {
            localTimer += dt
            projectileCooldown -= dt
            if (!isArrived) {
                arrivalTimer += dt
                vy = 50f
                if (arrivalTimer > arrivalDuration) {
                    isArrived = true
                    phase = 2
                    localTimer = 0f
                }
                return
            }
            
            // Phase progression based on weak points remaining (4 layers)
            val remaining = activeWeakPoints
            val maxWp = maxWeakPoints
            
            when {
                remaining <= 0 -> { state = ThreatState.DESTROYED; return }
                remaining <= maxWp / 4 -> {
                    // Phase 4 — Event Horizon (<= 1 wp remaining)
                    if (phase != 4) { phase = 4; localTimer = 0f }
                }
                remaining <= maxWp / 2 -> {
                    // Phase 3 — Laser Grid (<= 2 wp remaining)
                    if (phase != 3) { phase = 3; localTimer = 0f }
                }
                remaining <= maxWp * 3 / 4 -> {
                    // Phase 2 — Photon Barrage (3 wp remaining)
                    if (phase != 2) { phase = 2; localTimer = 0f }
                }
            }

            // HUD Pull — intensifies with each phase
            val pullDuration = when (phase) { 4 -> 12f; 3 -> 10f; else -> 8f }
            val pullStrength = when (phase) { 4 -> 1.3f; 3 -> 1.2f; else -> 1.0f }
            val t = localTimer % pullDuration
            val basePull = if (t < pullDuration * 0.4f) (t / (pullDuration * 0.4f)) else if (t < pullDuration * 0.7f) 1.0f else max(0f, 1.0f - (t - pullDuration * 0.7f) / (pullDuration * 0.3f))
            hudPullFactor = (basePull * pullStrength).coerceIn(0f, 1.2f)
            
            // Movement: pursuit toward player, faster in later phases
            val driftSpeed = when (phase) { 4 -> 0.35f; 3 -> 0.25f; else -> 0.20f }
            x += (targetX - x) * driftSpeed * dt
            y += (targetY - y) * driftSpeed * dt
            
            // Rotation speeds up in later phases
            val rotSpeed = when (phase) { 4 -> 180f; 3 -> 135f; else -> 90f }
            rotation += rotSpeed * dt
            
            // Pulse telegraph for projectiles
            val pulseSpeed = when (phase) { 4 -> 12f; 3 -> 8f; else -> 5f }
            scanPulse = (sin(lifetime * pulseSpeed) * 0.5f + 0.5f)
            
            // Set projectile cooldown based on phase (actual firing happens in interaction handler)
            if (projectileCooldown <= 0f) {
                projectileCooldown = when (phase) {
                    4 -> 0.6f
                    3 -> 1.2f
                    else -> 2.0f
                }
            }
        }

        if (definition.id == "ENT_CLOUD_SKIMMER") {
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
            rotation += 15f * dt
            x += vx * dt
            y += vy * dt
            scanPulse = (scanPulse + dt * 0.4f) % 1.0f
        }

        if (definition.id == "ENT_STALKER") {
            val dx = targetX - x
            val dy = targetY - y
            val dist = kotlin.math.sqrt(dx*dx + dy*dy)
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
            val dist = kotlin.math.sqrt(dx*dx + dy*dy)
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
