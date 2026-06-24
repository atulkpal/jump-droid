package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.example.jump_droid.Constants.PLATFORM_HEIGHT
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class PowerUpManager {
    val powerUps = mutableStateListOf<PowerUp>()
    var spawnTimer by mutableFloatStateOf(0f)

    fun add(x: Float, y: Float, type: PowerUpType, isMissionReward: Boolean = false) {
        powerUps.add(PowerUp(x, y, type, isMissionReward = isMissionReward))
    }

    fun updateAutoSpawn(dt: Float, screenWidth: Float, cameraY: Float) {
        spawnTimer += dt
        if (spawnTimer > 20f) {
            val types = PowerUpType.entries.filter { it != PowerUpType.ARTIFACT }
            val rand = Random.nextFloat()
            val type = when {
                rand < 0.05f -> PowerUpType.ARTIFACT
                else -> types.random()
            }
            powerUps.add(PowerUp(Random.nextFloat() * (screenWidth - 60f) + 30f, cameraY - 100f, type))
            spawnTimer = 0f
        }
    }

    fun updateMovement(dt: Float, gameTime: Long, player: Player, platforms: List<Platform>, cameraY: Float, screenHeight: Float) {
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val pu = iterator.next()
            pu.life -= dt

            if (pu.isMissionReward) {
                val dx = player.x - pu.x
                val dy = player.y - pu.y
                val distSq = dx * dx + dy * dy
                if (distSq > 0f) {
                    val dist = sqrt(distSq)
                    val pull = 1000f * dt
                    pu.x += (dx / dist) * pull
                    pu.y += (dy / dist) * pull
                }
            } else {
                if (pu.hoverTimer > 0) {
                    pu.hoverTimer -= dt
                    pu.y += sin(gameTime / 200f) * 0.5f
                } else {
                    pu.velocityY += 300f * dt
                    pu.y += (200f + pu.velocityY) * dt
                }

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

            if (pu.y > cameraY + screenHeight + 200f || pu.life <= 0f) {
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
