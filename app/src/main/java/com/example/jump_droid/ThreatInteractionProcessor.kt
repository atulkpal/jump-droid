package com.example.jump_droid

import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

fun ActiveThreat.processInteractionHandler(
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
                if (phase == 2) {
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
                            onBurst(wx, wy, 25, Color(0xFF9C27B0), 300f)
                            onVisualFeedback(20f, 0f)
                            onFloatingText("WEAK POINT DESTROYED", player.x, player.y, Color(0xFF9C27B0), true, 1.0f)

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
