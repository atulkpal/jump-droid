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
    onSpawnThreat: (id: String, x: Float, y: Float, vx: Float, vy: Float) -> Unit = { _, _, _, _, _ -> },
    onDamage: (amount: Float) -> Unit = {},
    onPlaySfx: (String) -> Unit = {},
    onVibrate: (HapticManager.HapticType) -> Unit = {}
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
                        onDamage(25f)
                        player.invulnerabilityTimer = 0.5f
                        onVisualFeedback(20f, 0.8f)
                        onPlaySfx("sfx_hazard_lightning")
                        onVibrate(HapticManager.HapticType.IMPACT_MEDIUM)
                    }
                }
                definition.discoveryType?.let { onDiscovery(it) }
            }
        }
        "HAZ_DEBRIS" -> {
            if (distSq < 80f * 80f) {
                if (player.invulnerabilityTimer <= 0f) {
                    onDamage(25f)
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
                onPlaySfx("sfx_hazard_emp")
                onVibrate(HapticManager.HapticType.IMPACT_LIGHT)
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
                    onPlaySfx("sfx_projectile_fire")
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
        "HAZ_CRYO_MIST" -> {
            if (distSq < 150f * 150f) {
                // Freeze heat bar - we'll approximate this by damping any changes 
                // but for a strict "freeze", we'd need to store the entry heat.
                // For now, let's just strongly cool it or keep it static.
                player.heat = player.heat // This doesn't actually do anything without external state.
                // We'll use a specific status flag if we had one, but let's just 
                // spam a vent effect to counteract generation.
                player.heat = max(0f, player.heat - 80f * sdt) 
                if (gameTime % 500 < 50) {
                    onFloatingText("THERMAL LOCK", player.x, player.y, Color.Cyan, false, 0.5f)
                }
            }
        }
        "HAZ_MIRROR_SHARDS" -> {
            if (distSq < 200f * 200f) {
                player.controlInversionTimer = 0.5f
                if (gameTime % 1000 < 50) {
                    onNotification("AXIS INVERTED", 0.5f)
                }
            }
        }
        "HAZ_GRAVITY_SHEAR" -> {
            if (distSq < 300f * 300f) {
                val dist = sqrt(distSq)
                val strength = (1f - dist / 300f) * 2500f
                if (dy < 0) { // Player is above center
                    player.velocityY -= strength * sdt // Push UP
                } else { // Player is below center
                    player.velocityY += strength * sdt // Pull DOWN
                }
            }
        }
        "ENT_HEAT_BAT" -> {
            if (distSq < 80f * 80f && player.invulnerabilityTimer <= 0f) {
                val damage = if (player.heat >= 70f) 20f else 10f
                onDamage(damage)
                player.invulnerabilityTimer = 1.0f
                onVisualFeedback(15f, 0.2f)
                onBurst(x, y, 10, Color.Black, 300f)
            }
        }
        "ENT_VOID_HARVESTER" -> {
            val iter = powerUps.iterator()
            while (iter.hasNext()) {
                val pu = iter.next()
                val pdx = pu.x - x
                val pdy = pu.y - y
                val dSq = pdx * pdx + pdy * pdy
                if (dSq < 2500f) {
                    onBurst(pu.x, pu.y, 15, Color.Magenta, 200f)
                    iter.remove()
                    onFloatingText("RESOURCE CONSUMED", x, y, Color.Magenta, true, 1.0f)
                    health = min(definition.baseHealth * 2f, health + 20f)
                }
            }
            if (distSq < 100f * 100f && player.invulnerabilityTimer <= 0f) {
                player.fuel = max(0f, player.fuel - 15f)
                player.invulnerabilityTimer = 0.8f
                onVisualFeedback(10f, 0f)
            }
        }
        "ENT_PHASE_WRAITH" -> {
            if (isMaterialized && distSq < 100f * 100f && player.invulnerabilityTimer <= 0f) {
                onDamage(15f)
                player.invulnerabilityTimer = 1.2f
                onVisualFeedback(20f, 0.5f)
                onBurst(x, y, 20, Color.Cyan, 400f)
            }
        }
        "ENT_GRAVITY_RAM" -> {
            if (distSq < 120f * 120f && player.invulnerabilityTimer <= 0f) {
                player.velocityX += (if (dx > 0) 1f else -1f) * 2000f
                player.velocityY += 3000f // Massive downward knockback
                onDamage(25f)
                player.invulnerabilityTimer = 1.5f
                onVisualFeedback(40f, 0.8f)
                onBurst(player.x, player.y, 30, Color.Red, 800f)
                onFloatingText("KINETIC IMPACT", player.x, player.y, Color.Red, true, 1.5f)
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
        "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL", 
        "MINI_BOSS_THERMAL_HIVE", "MINI_BOSS_GRAVITY_ANCHOR", "MINI_BOSS_FORGER", "BOSS_ARCHITECT", "BOSS_ENTROPY_CORE" -> {
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
                            "BOSS_STAR_EATER" -> Pair(x + cos(lifetime * 2f + i) * 100f, y + sin(lifetime * 2f + i) * 100f)
                            "BOSS_LEVIATHAN" -> {
                                val ox = sin(lifetime * 1.5f - i * 0.5f) * 100f
                                Pair(x + ox, y + i * 60f)
                            }
                            "BOSS_VOID_ENGINE" -> {
                                val angle = (rotation + i * 180f) * (PI.toFloat() / 180f)
                                Pair(x + cos(angle) * 150f, y + sin(angle) * 150f)
                            }
                            "BOSS_SIGNAL" -> Pair(x + (Random.nextFloat()-0.5f)*200f, y + (Random.nextFloat()-0.5f)*200f)
                            "MINI_BOSS_THERMAL_HIVE" -> Pair(x + (if (i == 0) -60f else 60f), y + 20f)
                            "MINI_BOSS_GRAVITY_ANCHOR" -> Pair(x, y)
                            "MINI_BOSS_FORGER" -> Pair(x + (i - 1) * 70f, y - 30f)
                            "BOSS_ARCHITECT" -> {
                                val angle = (rotation + i * 90f) * (PI.toFloat() / 180f)
                                Pair(x + cos(angle) * 200f, y + sin(angle) * 200f)
                            }
                            "BOSS_ENTROPY_CORE" -> {
                                val angle = (i * 90f) * (PI.toFloat() / 180f)
                                Pair(x + cos(angle) * 180f, y + sin(angle) * 180f)
                            }
                            else -> Pair(x, y)
                        }

                        val ddx = player.x - wx
                        val ddy = player.y - wy
                        val hitDist = if (definition.id == "BOSS_STAR_EATER") 120f else {
                            if (player.rocketType == RocketType.SCOUT) 70f else 50f
                        }

                        if (sqrt(ddx*ddx + ddy*ddy) < hitDist && player.invulnerabilityTimer <= 0f) {
                            activeWeakPoints--
                            player.invulnerabilityTimer = 0.5f
                            player.velocityY = -400f
                            onBurst(wx, wy, 25, Color(0xFF9C27B0), 300f)
                            onVisualFeedback(20f, 0f)
                            onPlaySfx("sfx_boss_weakpoint")
                            onVibrate(HapticManager.HapticType.SUCCESS)
                            onFloatingText("WEAK POINT DESTROYED", player.x, player.y, Color(0xFF9C27B0), true, 1.0f)

                            if (player.rocketType == RocketType.TANK) {
                                onBurst(wx, wy, 60, Color.White, 800f)
                                onVisualFeedback(40f, 0.6f)
                            }

                            if (activeWeakPoints <= 0) {
                                phase = when (definition.id) {
                                    "MINI_BOSS_COMMANDER" -> 5
                                    else -> 4
                                }
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
                              onPlaySfx("sfx_projectile_fire")
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
                        onPlaySfx("sfx_projectile_fire")
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
                        val suctionForce = (1f - dist / 1000f) * 1500f
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
                        onPlaySfx("sfx_projectile_fire")
                        projectileCooldown = 4.0f
                    }
                    if (phase == 3 && projectileCooldown <= 0f) {
                        val ang = atan2(dy, dx)
                        onSpawnProjectile(x, y - 30f, cos(ang) * 400f, sin(ang) * 400f, ProjectileType.MISSILE, ProjectileOwner.THREAT, 15f, Color(0xFF9C27B0), 10f, 4f)
                        onPlaySfx("sfx_projectile_fire")
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
                            onPlaySfx("sfx_projectile_fire")
                            projectileCooldown = 3.0f
                        }
                        if (phase == 3) {
                            onSpawnProjectile(x, y + 40f, cos(ang) * 600f, sin(ang) * 600f, ProjectileType.BEAM, ProjectileOwner.THREAT, 15f, Color(0xFF00BCD4), 6f, 3f)
                            onPlaySfx("sfx_projectile_fire")
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
                        onPlaySfx("sfx_projectile_fire")
                        projectileCooldown = 3.5f
                    }
                    if (phase == 3 && projectileCooldown <= 0f) {
                        val ang = atan2(dy, dx)
                        for (i in -1..1) {
                            val a = ang + i * 0.35f
                            onSpawnProjectile(x, y, cos(a) * 500f, sin(a) * 500f, ProjectileType.BOLT, ProjectileOwner.THREAT, 12f, Color(0xFFE91E63), 8f, 3f)
                        }
                        onPlaySfx("sfx_projectile_fire")
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
                        onPlaySfx("sfx_projectile_fire")
                        projectileCooldown = 3.0f
                    }
                    if (phase == 3 && projectileCooldown <= 0f) {
                        val ang = atan2(dy, dx)
                        onSpawnProjectile(x, y, cos(ang) * 550f, sin(ang) * 550f, ProjectileType.BEAM, ProjectileOwner.THREAT, 12f, Color(0xFFFF1744), 5f, 2.5f)
                        onPlaySfx("sfx_projectile_fire")
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
                        onPlaySfx("sfx_projectile_fire")
                        projectileCooldown = 1.5f
                    }
                }
                "ENT_VOID_WRAITH" -> {
                    if (isMaterialized && distSq < 100f * 100f && player.invulnerabilityTimer <= 0f) {
                        onDamage(15f)
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
                "MINI_BOSS_THERMAL_HIVE" -> {
                    if (phase == 2 && player.heat > 60f && threatSpawnCooldown <= 0f) {
                        onSpawnThreat("ENT_SWARM_BOTS", x, y, 200f * (if (Random.nextBoolean()) 1f else -1f), 100f)
                        threatSpawnCooldown = 3.5f
                        onFloatingText("HIVE ALERT", x, y, Color.Red, false, 0.8f)
                    }
                }
                "MINI_BOSS_GRAVITY_ANCHOR" -> {
                    val dist = sqrt(distSq)
                    if (dist < 1000f) {
                        val anchorStrength = (1f - dist / 1000f) * 4000f * alertLevel
                        player.velocityY += anchorStrength * sdt
                        if (gameTime % 500 < 50) onVisualFeedback(2f * alertLevel, 0f)
                    }
                }
                "MINI_BOSS_FORGER" -> {
                    if (phase == 2 && jamCooldown <= 0f) {
                        platforms.filter { 
                            val dxP = it.x - x
                            val dyP = it.y - y
                            dxP*dxP + dyP*dyP < 600f * 600f && it.type == PlatformType.NORMAL
                        }.take(2).forEach { 
                            // In a real impl, we'd need to change type.
                            // For simplicity, let's just use jam mechanism as "Conversion"
                            it.isJammed = true
                            it.jamTimer = 4.0f
                        }
                        jamCooldown = 5f
                        onFloatingText("FABRICATING HAZARDS", x, y, Color.White, false, 1f)
                    }
                }
                "BOSS_ARCHITECT" -> {
                    if (phase >= 2 && projectileCooldown <= 0f) {
                        val ang = atan2(dy, dx)
                        val r = rotation * (PI.toFloat() / 180f)
                        if (phase == 2) {
                            onSpawnProjectile(x, y, cos(r) * 400f, sin(r) * 400f, ProjectileType.BOLT, ProjectileOwner.THREAT, 12f, Color(0xFF00E5FF), 7f, 4f)
                            onSpawnProjectile(x, y, cos(r + PI.toFloat()) * 400f, sin(r + PI.toFloat()) * 400f, ProjectileType.BOLT, ProjectileOwner.THREAT, 12f, Color(0xFF00E5FF), 7f, 4f)
                            onPlaySfx("sfx_projectile_fire")
                            projectileCooldown = 2.5f
                        }
                        if (phase >= 3) {
                            repeat(4) { i ->
                                val a = r + i * (PI.toFloat() / 2f)
                                onSpawnProjectile(x, y, cos(a) * 500f, sin(a) * 500f, ProjectileType.WAVE, ProjectileOwner.THREAT, 10f, Color(0xFF00E5FF), 12f, 3f)
                            }
                            onPlaySfx("sfx_projectile_fire")
                            projectileCooldown = 3.5f
                        }
                    }
                    if (phase == 2 && threatSpawnCooldown <= 0f) {
                        val iter = (platforms as MutableList).iterator()
                        while (iter.hasNext()) {
                            val p = iter.next()
                            val dxP = p.x - x
                            val dyP = p.y - y
                            if (dxP*dxP + dyP*dyP < 400f * 400f) {
                                onBurst(p.x + p.width/2, p.y, 10, Color.White, 150f)
                                iter.remove()
                            }
                        }
                        threatSpawnCooldown = 6f
                        onFloatingText("STABILIZING REALITY", x, y, Color.Cyan, true, 1.5f)
                    }
                }
                "BOSS_ENTROPY_CORE" -> {
                    if (phase >= 2 && projectileCooldown <= 0f) {
                        val ang = atan2(dy, dx)
                        val alivePylons = max(1, activeWeakPoints)
                        val cooldownBase = 1.5f + (maxWeakPoints - alivePylons) * 0.5f
                        repeat(alivePylons) { i ->
                            val pa = (i * (kotlin.math.PI.toFloat() * 2f / maxWeakPoints.toFloat())) + rotation * (PI.toFloat() / 180f)
                            val pd = 150f
                            val px = x + cos(pa) * pd
                            val py = y + sin(pa) * pd
                            val pAng = atan2(player.y - py, player.x - px)
                            onSpawnProjectile(px, py, cos(pAng) * 400f, sin(pAng) * 400f, ProjectileType.BOLT, ProjectileOwner.THREAT, 8f, Color(0xFFFF1744), 6f, 3f)
                        }
                        onPlaySfx("sfx_projectile_fire")
                        projectileCooldown = cooldownBase
                    }
                    if (phase == 2) {
                        val drainDist = 1200f
                        val dist = sqrt(distSq)
                        if (dist < drainDist) {
                            val factor = (1f - dist / drainDist)
                            player.fuel = max(0f, player.fuel - 10f * factor * sdt)
                            player.shield = max(0f, player.shield - 5f * factor * sdt)
                            player.heat = min(Constants.MAX_HEAT, player.heat + 20f * factor * sdt)
                            if (gameTime % 800 < 50) onFloatingText("ENTROPY DRAIN", player.x, player.y - 100f, Color.Red, false, 0.5f)
                        }
                    }
                }
            }

            if (activeWeakPoints <= 0 && maxWeakPoints > 0 && !bossRewardDropped) {
                if (!hasInteracted) {
                    hasInteracted = true
                    destructionTimer = 0f
                    bossRewardDropped = true
                    powerUps.add(PowerUp(player.x, cameraY + 200f, PowerUpType.ARTIFACT))
                    onFloatingText("!!! ${definition.name.uppercase()} DEFEATED !!!", player.x, player.y, Color.Cyan, true, 1.0f)
                    onNotification(">>> MISSION DATA RECOVERED <<<", 5.0f)
                    onScoreUpdate(1000)
                }

                destructionTimer += sdt
                val t = destructionTimer
                val debrisColor = when (definition.id) {
                    "BOSS_GATEKEEPER" -> Color(0xFFFF9800)
                    "BOSS_STAR_EATER" -> Color(0xFFE040FB)
                    "BOSS_LEVIATHAN" -> Color(0xFF00BCD4)
                    "BOSS_VOID_ENGINE" -> Color(0xFFE91E63)
                    "BOSS_SIGNAL" -> Color(0xFFFF1744)
                    "MINI_BOSS_THERMAL_HIVE" -> Color(0xFFFF6D00)
                    "MINI_BOSS_GRAVITY_ANCHOR" -> Color(0xFF9C27B0)
                    "MINI_BOSS_FORGER" -> Color(0xFF4CAF50)
                    "BOSS_ARCHITECT" -> Color(0xFF00E5FF)
                    "BOSS_ENTROPY_CORE" -> Color(0xFFFF1744)
                    "BOSS_SINGULARITY" -> Color(0xFFD500F9)
                    else -> Color(0xFF9C27B0)
                }

                // Progressive bursts
                if (t < 0.3f) {
                    if (t - sdt < 0.1f) {
                        onBurst(x, y - 50f, 25, debrisColor, 300f)
                    }
                } else if (t < 0.6f) {
                    if (t - sdt < 0.3f) {
                        onBurst(x + (Random.nextFloat() - 0.5f) * 100f, y + (Random.nextFloat() - 0.5f) * 100f, 30, Color.White, 400f)
                        onBurst(x - 50f, y + 50f, 20, debrisColor, 350f)
                    }
                } else if (t < 0.9f) {
                    if (t - sdt < 0.6f) {
                        repeat(2) {
                            onBurst(x + (Random.nextFloat() - 0.5f) * 150f, y + (Random.nextFloat() - 0.5f) * 150f, 35, debrisColor, 500f)
                        }
                        onBurst(x, y, 20, Color.White, 600f)
                    }
                } else if (t < 1.2f) {
                    if (t - sdt < 0.9f) {
                        repeat(3) {
                            onBurst(x + (Random.nextFloat() - 0.5f) * 200f, y + (Random.nextFloat() - 0.5f) * 200f, 40, debrisColor.copy(alpha = 0.8f), 600f)
                        }
                        onBurst(x, y + 50f, 30, Color.White, 700f)
                    }
                }

                if (t >= 1.5f) {
                    // Final large explosion
                    onBurst(x, y, 100, debrisColor, 1200f)
                    repeat(4) {
                        onBurst(x + (Random.nextFloat() - 0.5f) * 250f, y + (Random.nextFloat() - 0.5f) * 250f, 50, Color.White, 900f)
                    }
                    onVisualFeedback(70f, 1.0f)
                    onPlaySfx("sfx_boss_defeat")
                    onVibrate(HapticManager.HapticType.EXPLOSION)
                    state = ThreatState.DESTROYED

                    if (definition.id == "MINI_BOSS_COMMANDER") {
                        onMissionProgress(MissionType.BOSS)
                    }
                }
            }
        }
    }
}
