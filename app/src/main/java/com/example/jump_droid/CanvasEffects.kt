package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.jump_droid.Constants.ROCKET_HEIGHT
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiRed
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun DrawScope.drawRealityDistortion(
    threats: List<ActiveThreat>,
    playerX: Float, playerY: Float,
    size: Size,
    currentZone: AltitudeZone = AltitudeZone.EARTH,
    altitude: Int = 0
) {
    if (currentZone == AltitudeZone.VOID) {
        val depthPulse = (kotlin.math.sin(altitude / 500f) * 0.02f + 0.03f).coerceIn(0f, 0.06f)
        drawRect(
            color = Color(0xFFFF1744).copy(alpha = depthPulse),
            size = size
        )
    }

    threats.find { threat: ActiveThreat -> threat.definition.id == "HAZ_VOID_ANOMALY" && threat.state == ThreatState.ACTIVE }?.let { anomaly ->
        val dx = (playerX - anomaly.x).toDouble()
        val dy = (playerY - anomaly.y).toDouble()
        val dist = sqrt(dx * dx + dy * dy).toFloat()
        if (dist < 1000f) {
            val intensity = (1f - dist / 1000f).coerceIn(0f, 1f)
            drawRect(
                color = Color(0xFFFF00FF).copy(alpha = 0.1f * intensity),
                size = size
            )
        }
    }
}

fun DrawScope.drawSpeedLines(
    velocityY: Float,
    screenWidth: Float, screenHeight: Float,
    currentZone: AltitudeZone = AltitudeZone.EARTH
) {
    val speedRatio = (kotlin.math.abs(velocityY) / 1200f).coerceIn(0f, 1f)
    if (speedRatio > 0.4f) {
        val lineColor = when (currentZone) {
            AltitudeZone.EARTH -> Color.White
            AltitudeZone.CLOUD_LAYER -> Color(0xFF80DEEA)
            AltitudeZone.UPPER_ATMOSPHERE -> Color(0xFFCE93D8)
            AltitudeZone.ORBIT -> SciFiGold
            AltitudeZone.DEEP_SPACE -> Color(0xFF64B5F6)
            AltitudeZone.THE_FOUNDRY -> Color(0xFFFF6D00)
            AltitudeZone.CHRONO_RIFT -> SciFiPurple
            AltitudeZone.VOID, AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE, AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY -> 
                SciFiRed
        }
        val count = when (currentZone) {
            AltitudeZone.EARTH, AltitudeZone.CLOUD_LAYER -> 8
            AltitudeZone.UPPER_ATMOSPHERE -> 6
            else -> 4
        }

        repeat(count) {
            val rx = kotlin.random.Random.nextFloat() * screenWidth
            val ry = kotlin.random.Random.nextFloat() * screenHeight
            val alpha = (speedRatio - 0.4f) * 0.3f
            drawLine(
                lineColor.copy(alpha = alpha),
                start = Offset(rx, ry),
                end = Offset(rx, ry + 60f * speedRatio),
                strokeWidth = 1f + 1f * speedRatio
            )
        }

        if (currentZone == AltitudeZone.EARTH || currentZone == AltitudeZone.CLOUD_LAYER) {
            val hAlpha = (speedRatio - 0.4f) * 0.12f
            repeat(4) {
                val rx = kotlin.random.Random.nextFloat() * screenWidth
                val ry = kotlin.random.Random.nextFloat() * screenHeight
                drawLine(
                    lineColor.copy(alpha = hAlpha),
                    start = Offset(rx, ry),
                    end = Offset(rx + 40f * speedRatio, ry),
                    strokeWidth = 1f
                )
            }
        }
    }
}

fun DrawScope.drawGround(
    groundY: Float, cameraY: Float,
    screenWidth: Float, screenHeight: Float,
    currentZone: AltitudeZone = AltitudeZone.EARTH,
    zoneProgress: Float = 0f
) {
    if (currentZone.ordinal >= AltitudeZone.ORBIT.ordinal) return

    val groundColor = when (currentZone) {
        AltitudeZone.EARTH -> {
            val green = androidx.compose.ui.graphics.lerp(Color(0xFF795548), Color(0xFF558B2F), zoneProgress * 0.3f)
            green
        }
        AltitudeZone.CLOUD_LAYER -> Color(0xFF546E7A)
        AltitudeZone.UPPER_ATMOSPHERE -> Color(0xFF1A0033)
        else -> Color(0xFF795548)
    }

    val baseY = groundY + (ROCKET_HEIGHT / 2) - cameraY
    val fadeAlpha = if (currentZone == AltitudeZone.UPPER_ATMOSPHERE) (1f - zoneProgress).coerceIn(0f, 1f) else 1f

    drawRect(
        groundColor.copy(alpha = fadeAlpha),
        topLeft = Offset(0f, baseY),
        size = Size(screenWidth, screenHeight)
    )
}

fun DrawScope.drawParticles(
    particles: List<Particle>,
    cameraY: Float,
    gameTime: Long
) {
    particles.forEach { p ->
        val alpha = (p.life / 1.0f).coerceIn(0f, 1f)
        val centerX = p.x
        val centerY = p.y - cameraY
        val sizeMult = (0.5f + alpha * 0.5f)
        val currentSize = p.size * sizeMult

        if (p.life < 0.9f) {
            drawCircle(
                color = p.color.copy(alpha = alpha * 0.2f),
                radius = currentSize * 2.5f,
                center = Offset(centerX, centerY)
            )
        }

        if (currentSize > 3f && (gameTime / 120) % 2L == 0L) {
            val s = currentSize * 1.5f
            val angle = kotlin.math.atan2(p.vy, p.vx)
            val cosA = kotlin.math.cos(angle)
            val sinA = kotlin.math.sin(angle)
            val cx = centerX
            val cy = centerY
            drawLine(p.color.copy(alpha = alpha), Offset(cx - s * cosA, cy - s * sinA), Offset(cx + s * cosA, cy + s * sinA), strokeWidth = 2.5f)
            drawLine(p.color.copy(alpha = alpha), Offset(cx + s * sinA, cy - s * cosA), Offset(cx - s * sinA, cy + s * cosA), strokeWidth = 2.5f)
            val ds = s * 0.4f
            drawLine(p.color.copy(alpha = alpha * 0.4f), Offset(cx - ds * cosA - ds * sinA, cy - ds * sinA + ds * cosA), Offset(cx + ds * cosA + ds * sinA, cy + ds * sinA - ds * cosA), strokeWidth = 1.5f)
            drawLine(p.color.copy(alpha = alpha * 0.4f), Offset(cx + ds * cosA - ds * sinA, cy + ds * sinA + ds * cosA), Offset(cx - ds * cosA + ds * sinA, cy - ds * sinA - ds * cosA), strokeWidth = 1.5f)
        } else {
            drawCircle(p.color.copy(alpha = alpha), radius = currentSize, center = Offset(centerX, centerY))
        }
    }
}

fun DrawScope.drawLandingEffects(
    effects: List<LandingEffect>,
    cameraY: Float,
    currentZone: AltitudeZone = AltitudeZone.EARTH
) {
    val ringColor = when (currentZone) {
        AltitudeZone.EARTH -> Color.Cyan
        AltitudeZone.CLOUD_LAYER -> Color(0xFF80DEEA)
        AltitudeZone.UPPER_ATMOSPHERE -> SciFiPurple
        AltitudeZone.ORBIT -> SciFiGold
            AltitudeZone.DEEP_SPACE -> Color(0xFF64B5F6)
            AltitudeZone.THE_FOUNDRY -> Color(0xFFFF6D00)
            AltitudeZone.CHRONO_RIFT -> SciFiPurple
            AltitudeZone.VOID, AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE, AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY -> 
            SciFiRed
    }

    effects.forEach { effect ->
        val progress = 1f - (effect.life / 0.5f).coerceIn(0f, 1f)
        drawCircle(
            color = ringColor.copy(alpha = 0.3f * (1f - progress)),
            radius = 40f * progress,
            center = Offset(effect.x, effect.y - cameraY),
            style = Stroke(width = 2f)
        )
    }
}

fun DrawScope.drawPowerUps(
    powerUps: List<PowerUp>,
    cameraY: Float,
    gameTime: Long
) {
    powerUps.forEach { pu ->
        val px = pu.x
        val py = pu.y - cameraY
        val glowPulse = kotlin.math.sin(gameTime / 120f) * 0.15f + 0.85f
        val baseColor = when (pu.type) {
            PowerUpType.FUEL_TANK -> Color(0xFFE57373)
            PowerUpType.TURBO_BOOSTER -> Color.Cyan
            PowerUpType.EFFICIENCY_MODULE -> Color.Green
            PowerUpType.HEAT_SINK -> Color.White
            PowerUpType.ARTIFACT -> Color(0xFF9C27B0)
            PowerUpType.ALTITUDE_BOOSTER -> Color.White
            PowerUpType.SHIELD_CAPSULE -> SciFiCyan
            PowerUpType.HULL_REPAIR -> SciFiGreen
            PowerUpType.KINETIC_BATTERY -> Color.White
            PowerUpType.MAGNETIC_SIPHON -> Color.Magenta
            PowerUpType.OVERDRIVE_MODULE -> Color.Red
        }

        // D1: Hexagonal background plate
        val plateRadius = 28f
        val hexPath = Path().apply {
            moveTo(px + plateRadius * cos(0f), py + plateRadius * sin(0f))
            for (i in 1..6) {
                val angle = i * (kotlin.math.PI.toFloat() / 3f)
                lineTo(px + plateRadius * cos(angle), py + plateRadius * sin(angle))
            }
            close()
        }
        drawPath(hexPath, Color(0xFF1A1A2E).copy(alpha = 0.7f))
        drawPath(hexPath, baseColor.copy(alpha = 0.6f), style = Stroke(width = 2f))

        // D2: Enhanced glow
        drawCircle(
            color = baseColor.copy(alpha = 0.35f * glowPulse),
            radius = 32f,
            center = Offset(px, py)
        )

        when (pu.type) {
            PowerUpType.FUEL_TANK -> {
                drawCircle(baseColor, radius = 16f, center = Offset(px, py + 2f))
                val tipPath = Path().apply {
                    moveTo(px - 8f, py + 14f)
                    lineTo(px, py + 22f)
                    lineTo(px + 8f, py + 14f)
                    close()
                }
                drawPath(tipPath, baseColor)
                // D5: Fuel drip particles
                val drip = (gameTime / 300) % 3L
                if (drip == 0L) drawCircle(baseColor.copy(alpha = 0.5f), radius = 3f, center = Offset(px - 4f, py + 24f))
                if (drip == 1L) drawCircle(baseColor.copy(alpha = 0.5f), radius = 3f, center = Offset(px + 4f, py + 24f))
            }
            PowerUpType.TURBO_BOOSTER -> {
                rotate((gameTime / 50f) % 360f, pivot = Offset(px, py)) {
                    val boltPath = Path().apply {
                        moveTo(px, py - 14f)
                        lineTo(px - 8f, py - 2f)
                        lineTo(px - 3f, py - 2f)
                        lineTo(px - 6f, py + 14f)
                        lineTo(px + 6f, py)
                        lineTo(px + 2f, py)
                        close()
                    }
                    drawPath(boltPath, baseColor)
                }
            }
            PowerUpType.EFFICIENCY_MODULE -> {
                rotate((gameTime / 25f) % 360f, pivot = Offset(px, py)) {
                    drawCircle(baseColor, radius = 14f, center = Offset(px, py))
                    repeat(4) { i ->
                        val a = i * 90f
                        rotate(a, pivot = Offset(px, py)) {
                            drawRect(baseColor, topLeft = Offset(px - 4f, py - 18f), size = Size(8f, 10f))
                        }
                    }
                }
                drawCircle(Color.White.copy(alpha = 0.6f), radius = 7f, center = Offset(px, py))
                // D5: Trail
                repeat(2) { i ->
                    val tAngle = (gameTime / 25f + i * 180f) % 360f
                    val tRad = tAngle * (kotlin.math.PI.toFloat() / 180f)
                    drawCircle(baseColor.copy(alpha = 0.2f), radius = 4f, center = Offset(px + cos(tRad) * 22f, py + sin(tRad) * 22f))
                }
            }
            PowerUpType.HEAT_SINK -> {
                drawRect(baseColor, topLeft = Offset(px - 14f, py - 16f), size = Size(28f, 32f), style = Stroke(width = 3f))
                // D5: Sequential fin pulse
                repeat(3) { i ->
                    val fy = py - 8f + i * 8f
                    val finAlpha = ((sin(gameTime / 200f + i * 2f) * 0.3f + 0.7f))
                    drawLine(baseColor.copy(alpha = finAlpha), start = Offset(px - 18f, fy), end = Offset(px + 18f, fy), strokeWidth = 3f)
                }
            }
            PowerUpType.ARTIFACT -> {
                drawCircle(baseColor, radius = 20f, center = Offset(px, py))
                drawCircle(Color.White, radius = 7f, center = Offset(px, py))
                // D5: Alternating glow colors
                val artGlow = Color(0xFFFFD700).copy(alpha = (sin(gameTime / 150f) * 0.3f + 0.5f))
                drawCircle(artGlow, radius = 5f, center = Offset(px - 5f, py - 3f))
                drawCircle(Color.White.copy(alpha = (sin(gameTime / 200f + 1f) * 0.3f + 0.5f)), radius = 4f, center = Offset(px + 4f, py + 3f))
            }
            PowerUpType.ALTITUDE_BOOSTER -> {
                drawRect(baseColor, topLeft = Offset(px - 12f, py - 16f), size = Size(24f, 32f))
                val upPath = Path().apply {
                    moveTo(px, py - 22f)
                    lineTo(px - 7f, py - 14f)
                    lineTo(px + 7f, py - 14f)
                    close()
                }
                drawPath(upPath, baseColor)
            }
            PowerUpType.SHIELD_CAPSULE -> {
                drawCircle(baseColor, radius = 20f, center = Offset(px, py))
                val angle = (gameTime / 30f) % 360f
                rotate(angle, pivot = Offset(px, py)) {
                    drawCircle(Color.White.copy(alpha = 0.5f), radius = 26f, center = Offset(px, py), style = Stroke(width = 2.5f))
                }
                drawCircle(Color.White.copy(alpha = 0.6f), radius = 7f, center = Offset(px, py))
            }
            PowerUpType.HULL_REPAIR -> {
                drawCircle(baseColor, radius = 20f, center = Offset(px, py))
                // D5: Rotating cross-hair
                val crossAngle = (gameTime / 80f) % 360f
                rotate(crossAngle, pivot = Offset(px, py)) {
                    drawLine(Color.White.copy(alpha = 0.8f), start = Offset(px - 9f, py), end = Offset(px + 9f, py), strokeWidth = 3f)
                    drawLine(Color.White.copy(alpha = 0.8f), start = Offset(px, py - 9f), end = Offset(px, py + 9f), strokeWidth = 3f)
                }
            }
            PowerUpType.KINETIC_BATTERY -> {
                drawCircle(baseColor, radius = 14f, center = Offset(px, py), style = Stroke(width = 2.5f))
                // D5: Throb
                val throb = sin(gameTime / 150f) * 3f + 6f
                drawCircle(baseColor, radius = throb, center = Offset(px, py))
            }
            PowerUpType.MAGNETIC_SIPHON -> {
                drawCircle(baseColor, radius = 12f, center = Offset(px, py))
                // D5: Expand/contract ring
                val ringSize = sin(gameTime / 200f) * 4f + 18f
                drawCircle(baseColor, radius = ringSize, center = Offset(px, py), style = Stroke(width = 2.5f))
            }
            PowerUpType.OVERDRIVE_MODULE -> {
                // D5: Pulsing red/white triangle
                val pulseColor = if ((gameTime / 100) % 2 == 0L) baseColor else Color.White.copy(alpha = 0.8f)
                val path = Path().apply {
                    moveTo(px - 14f, py + 14f)
                    lineTo(px, py - 14f)
                    lineTo(px + 14f, py + 14f)
                    close()
                }
                drawPath(path, pulseColor)
            }
        }
    }
}

fun DrawScope.drawFlyingRewards(
    rewards: List<FlyingReward>
) {
    rewards.forEach { fr ->
        val curX = fr.x * (1f - fr.progress) + fr.targetX * fr.progress
        val curY = fr.y * (1f - fr.progress) + fr.targetY * fr.progress

        val baseColor = when (val t = fr.type) {
            is ComboReward.Fuel -> Color.Green
            is ComboReward.PowerUp -> when (t.type) {
                PowerUpType.HULL_REPAIR -> SciFiGreen
                PowerUpType.SHIELD_CAPSULE -> SciFiCyan
                else -> SciFiCyan
            }
            is ComboReward.AltitudeBoost -> Color.White
            is ComboReward.Artifact -> Color(0xFF9C27B0)
        }

        scale(fr.scale, pivot = Offset(curX, curY)) {
            drawCircle(baseColor, radius = 15f, center = Offset(curX, curY))
            drawCircle(Color.White, radius = 5f, center = Offset(curX, curY))
        }
    }
}

fun DrawScope.drawProjectiles(
    projectiles: List<Projectile>,
    cameraY: Float,
    gameTime: Long
) {
    projectiles.forEach { p ->
        val tx = p.x
        val ty = p.y - cameraY

        when (p.type) {
            ProjectileType.BOLT -> {
                val mag = sqrt(p.vx * p.vx + p.vy * p.vy + 0.001f)
                val unitX = p.vx / mag
                val unitY = p.vy / mag
                val trailLen = 5
                for (i in trailLen downTo 1) {
                    val tAlpha = 0.15f * (1f - i.toFloat() / trailLen)
                    drawCircle(
                        color = p.color.copy(alpha = tAlpha),
                        radius = p.size * (0.6f + 0.4f * i.toFloat() / trailLen),
                        center = Offset(tx - unitX * i * 6f, ty - unitY * i * 6f)
                    )
                }
                drawCircle(
                    color = p.color.copy(alpha = 0.8f),
                    radius = p.size,
                    center = Offset(tx, ty)
                )
                drawCircle(
                    color = Color.White,
                    radius = p.size * 0.5f,
                    center = Offset(tx, ty)
                )
                val glowAlpha = (kotlin.math.sin(gameTime / 100f) * 0.3f + 0.3f)
                drawCircle(
                    color = p.color.copy(alpha = glowAlpha),
                    radius = p.size * 3f,
                    center = Offset(tx, ty)
                )
            }
            ProjectileType.MISSILE -> {
                val mag = sqrt(p.vx * p.vx + p.vy * p.vy + 0.001f)
                val unitX = p.vx / mag
                val unitY = p.vy / mag
                val bodyW = p.size * 2f
                val bodyH = p.size

                drawRect(
                    color = p.color,
                    topLeft = Offset(tx - bodyW / 2, ty - bodyH / 2),
                    size = Size(bodyW, bodyH)
                )
                drawRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(tx - bodyW / 2, ty - bodyH / 4),
                    size = Size(bodyW * 0.6f, bodyH / 2)
                )

                for (i in 1..4) {
                    val t = i * 5
                    val smokeAlpha = 0.15f * (1f - i / 5f)
                    val smokeR = (3f + i * 2f) * (0.8f + kotlin.math.sin(gameTime / 50f + i) * 0.2f)
                    drawCircle(
                        color = Color(0xFF607D8B).copy(alpha = smokeAlpha),
                        radius = smokeR,
                        center = Offset(tx - unitX * t * 3f + (kotlin.math.sin(gameTime / 30f + i) * 4f), ty - unitY * t * 3f + (kotlin.math.cos(gameTime / 40f + i) * 4f))
                    )
                }

                val flicker = (gameTime / 50) % 2 == 0L
                drawCircle(
                    color = if (flicker) SciFiGold else SciFiRed.copy(alpha = 0.8f),
                    radius = p.size * 0.6f,
                    center = Offset(tx - unitX * bodyW / 2, ty - unitY * bodyH / 2)
                )
            }
            ProjectileType.BEAM -> {
                val length = 40f
                val mag = sqrt(p.vx * p.vx + p.vy * p.vy + 0.001f)
                val unitX = p.vx / mag
                val unitY = p.vy / mag
                val glowPulse = kotlin.math.sin(gameTime / 60f) * 0.3f + 0.7f

                drawLine(
                    color = p.color.copy(alpha = 0.2f * glowPulse),
                    start = Offset(tx, ty),
                    end = Offset(tx + unitX * length, ty + unitY * length),
                    strokeWidth = p.size * 3f
                )
                drawLine(
                    color = p.color.copy(alpha = 0.6f),
                    start = Offset(tx, ty),
                    end = Offset(tx + unitX * length, ty + unitY * length),
                    strokeWidth = p.size
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(tx, ty),
                    end = Offset(tx + unitX * length, ty + unitY * length),
                    strokeWidth = p.size * 0.3f
                )
            }
            ProjectileType.WAVE -> {
                val waveAlpha = 0.3f + kotlin.math.sin(gameTime / 80f) * 0.1f
                drawCircle(
                    color = p.color.copy(alpha = waveAlpha),
                    radius = p.size,
                    center = Offset(tx, ty),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = p.color.copy(alpha = waveAlpha * 0.5f),
                    radius = p.size * 1.2f,
                    center = Offset(tx, ty),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

fun DrawScope.drawVisualObstruction(
    fogAlpha: Float,
    playerX: Float,
    playerY: Float,
    cameraY: Float,
    size: Size,
    fogColor: Color = Color.Black
) {
    if (!DevConfig.ENABLE_VISUAL_OBSTRUCTION || fogAlpha <= 0f) return

    val maxDim = if (size.width > size.height) size.width else size.height
    drawRect(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            0.0f to Color.Transparent,
            0.15f to fogColor.copy(alpha = fogAlpha * 0.3f),
            0.4f to fogColor.copy(alpha = fogAlpha),
            center = Offset(playerX, playerY - cameraY),
            radius = maxDim * 1.5f
        ),
        size = size
    )
}

fun DrawScope.drawTether(
    tether: Tether?,
    playerX: Float,
    playerY: Float,
    cameraY: Float,
    gameTime: Long
) {
    if (tether == null || !tether.isActive || !DevConfig.ENABLE_TETHER_PHYSICS) return

    val px = playerX
    val py = playerY - cameraY
    val ax = tether.anchorX
    val ay = tether.anchorY

    val color = Color.Cyan
    val alpha = 0.6f + (kotlin.math.sin(gameTime / 50f) * 0.2f)

    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(px, py),
        end = Offset(ax, ay),
        strokeWidth = 3f
    )

    repeat(3) { i ->
        val seed = gameTime / 100 + i
        val rng = kotlin.random.Random(seed)
        val segments = 5
        var lastX = px
        var lastY = py

        for (j in 1..segments) {
            val progress = j.toFloat() / segments
            val targetX = px + (ax - px) * progress
            val targetY = py + (ay - py) * progress

            val offsetX = (rng.nextFloat() - 0.5f) * 20f
            val offsetY = (rng.nextFloat() - 0.5f) * 20f

            drawLine(
                color = Color.White.copy(alpha = alpha * 0.5f),
                start = Offset(lastX, lastY),
                end = Offset(targetX + offsetX, targetY + offsetY),
                strokeWidth = 1.5f
            )
            lastX = targetX + offsetX
            lastY = targetY + offsetY
        }
        drawLine(Color.White.copy(alpha = alpha * 0.5f), Offset(lastX, lastY), Offset(ax, ay), strokeWidth = 1.5f)
    }
}

fun DrawScope.drawImpactFlash(
    alpha: Float,
    size: Size
) {
    if (alpha > 0) {
        drawRect(
            color = Color.White.copy(alpha = alpha),
            style = Stroke(width = 20f)
        )
    }
}
