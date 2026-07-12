package com.ashwathai.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.ashwathai.jump_droid.Constants.PLATFORM_HEIGHT
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class PowerUpManager {
    val powerUps = mutableStateListOf<PowerUp>()
    var spawnTimer by mutableFloatStateOf(0f)

    companion object {
        private val BOSS_EXCLUSION_RADIUS = 200f
        private val HAZARD_EXCLUSION_RADIUS = 250f
        private val DESPAWN_TIME = 8.0f
    }

    fun add(x: Float, y: Float, type: PowerUpType, isMissionReward: Boolean = false) {
        powerUps.add(PowerUp(x, y, type, isMissionReward = isMissionReward))
    }

    fun updateAutoSpawn(
        dt: Float,
        screenWidth: Float,
        cameraY: Float,
        screenHeight: Float,
        activeThreats: List<ActiveThreat>,
        platforms: List<Platform>
    ) {
        spawnTimer += dt
        if (spawnTimer > 20f) {
            val types = PowerUpType.entries.filter { it != PowerUpType.ARTIFACT }
            val rand = Random.nextFloat()
            val type = when {
                rand < 0.05f -> PowerUpType.ARTIFACT
                else -> types.random()
            }

            // Find a valid spawn position above camera top with dead zone avoidance
            var spawnX: Float
            var spawnY: Float
            var valid = false
            var attempts = 0
            while (!valid && attempts < 20) {
                spawnX = Random.nextFloat() * (screenWidth - 60f) + 30f
                spawnY = cameraY - Random.nextFloat() * 400f - 50f

                val safe = activeThreats.all { threat ->
                    val dx = spawnX - threat.x
                    val dy = spawnY - threat.y
                    val distSq = dx * dx + dy * dy
                    val exclusionRadius = if (threat.definition.type == ThreatType.BOSS ||
                        threat.definition.type == ThreatType.MINI_BOSS
                    ) BOSS_EXCLUSION_RADIUS else HAZARD_EXCLUSION_RADIUS
                    distSq > exclusionRadius * exclusionRadius
                } && platforms.none { p ->
                    p.type == PlatformType.BREAKABLE && spawnX > p.x && spawnX < p.x + p.width &&
                            abs(spawnY - p.y) < 150f
                }

                if (safe) {
                    valid = true
                    powerUps.add(PowerUp(spawnX, spawnY, type))
                }
                attempts++
            }
            if (!valid) {
                powerUps.add(PowerUp(
                    Random.nextFloat() * (screenWidth - 60f) + 30f,
                    cameraY - 100f,
                    type
                ))
            }
            spawnTimer = 0f
        }
    }

    fun updateMovement(dt: Float, gameTime: Long, player: Player, platforms: List<Platform>, cameraY: Float, screenHeight: Float) {
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val pu = iterator.next()

            if (pu.isMissionReward || player.magneticSiphonTimer > 0f) {
                val dx = player.x - pu.x
                val dy = player.y - pu.y
                val distSq = dx * dx + dy * dy
                if (distSq > 0f) {
                    val dist = sqrt(distSq)
                    val pull = (if (player.magneticSiphonTimer > 0f) 1500f else 1000f) * dt
                    pu.x += (dx / dist) * pull
                    pu.y += (dy / dist) * pull
                }
            } else {
                // Floating hover — no falling-drop
                pu.y += sin(gameTime / 200f + pu.x) * 0.8f * dt * 60f

                // Despawn timer
                pu.despawnTimer -= dt
                pu.glowPulseSpeed = 1.0f + (1.0f - pu.despawnTimer / DESPAWN_TIME) * 4.0f

                // Magnetic platform attraction
                for (plat in platforms) {
                    if (plat.type == PlatformType.MAGNETIC) {
                        val dx = pu.x - (plat.x + plat.width / 2f)
                        val dy = pu.y - (plat.y + PLATFORM_HEIGHT / 2f)
                        val distSq = dx * dx + dy * dy
                        val radius = 250f
                        if (distSq < radius * radius) {
                            val dist = sqrt(distSq)
                            val pull = 400f * dt * (1f - dist / radius)
                            pu.x -= (dx / dist) * pull
                            pu.y -= (dy / dist) * pull
                        }
                    }
                }
            }

            if (pu.y > cameraY + screenHeight + 200f || pu.despawnTimer <= 0f) {
                iterator.remove()
            }
        }
    }

    fun checkCollection(player: Player): List<PowerUp> {
        val collected = mutableListOf<PowerUp>()
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val pu = iterator.next()
            if (abs(player.x - pu.x) < 80f && abs(player.y - pu.y) < 100f) {
                collected.add(pu)
                iterator.remove()
            }
        }
        return collected
    }
}
