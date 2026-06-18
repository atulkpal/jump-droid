package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.example.jump_droid.Constants.ROCKET_HEIGHT
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGreen
import kotlin.math.sqrt

fun DrawScope.drawRealityDistortion(
    threats: List<ActiveThreat>,
    playerX: Float, playerY: Float,
    size: Size
) {
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
    screenWidth: Float, screenHeight: Float
) {
    val speedRatio = (kotlin.math.abs(velocityY) / 1200f).coerceIn(0f, 1f)
    if (speedRatio > 0.4f) {
        repeat(8) {
            val rx = kotlin.random.Random.nextFloat() * screenWidth
            val ry = kotlin.random.Random.nextFloat() * screenHeight
            val alpha = (speedRatio - 0.4f) * 0.3f
            drawLine(
                Color.White.copy(alpha = alpha),
                start = Offset(rx, ry),
                end = Offset(rx, ry + 60f * speedRatio),
                strokeWidth = 1f + 1f * speedRatio
            )
        }
    }
}

fun DrawScope.drawGround(
    groundY: Float, cameraY: Float,
    screenWidth: Float, screenHeight: Float
) {
    drawRect(
        Color(0xFF795548),
        topLeft = Offset(0f, groundY + (ROCKET_HEIGHT / 2) - cameraY),
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
        if (p.size > 5f && (gameTime / 150) % 2L == 0L) {
            val centerX = p.x
            val centerY = p.y - cameraY
            val s = p.size * 1.5f
            drawLine(p.color.copy(alpha = alpha), Offset(centerX - s, centerY), Offset(centerX + s, centerY), strokeWidth = 2.5f)
            drawLine(p.color.copy(alpha = alpha), Offset(centerX, centerY - s), Offset(centerX, centerY + s), strokeWidth = 2.5f)
            val ds = s * 0.5f
            drawLine(p.color.copy(alpha = alpha * 0.5f), Offset(centerX - ds, centerY - ds), Offset(centerX + ds, centerY + ds), strokeWidth = 1.5f)
            drawLine(p.color.copy(alpha = alpha * 0.5f), Offset(centerX + ds, centerY - ds), Offset(centerX - ds, centerY + ds), strokeWidth = 1.5f)
        } else {
            drawCircle(p.color.copy(alpha = alpha), radius = p.size, center = Offset(p.x, p.y - cameraY))
        }
    }
}

fun DrawScope.drawLandingEffects(
    effects: List<LandingEffect>,
    cameraY: Float
) {
    effects.forEach { effect ->
        val progress = 1f - (effect.life / 0.5f).coerceIn(0f, 1f)
        drawCircle(
            color = Color.Cyan.copy(alpha = 0.3f * (1f - progress)),
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
        val baseColor = when (pu.type) {
            PowerUpType.FUEL_TANK -> Color(0xFFE57373)
            PowerUpType.TURBO_BOOSTER -> Color.Cyan
            PowerUpType.EFFICIENCY_MODULE -> Color.Green
            PowerUpType.HEAT_SINK -> Color.White
            PowerUpType.ARTIFACT -> Color(0xFF9C27B0)
            PowerUpType.ALTITUDE_BOOSTER -> Color.White
            PowerUpType.SHIELD_CAPSULE -> SciFiCyan
            PowerUpType.HULL_REPAIR -> SciFiGreen
        }
        if (pu.type == PowerUpType.ARTIFACT) {
            drawCircle(baseColor, radius = 15f, center = Offset(pu.x, pu.y - cameraY))
            drawCircle(Color.White, radius = 5f, center = Offset(pu.x, pu.y - cameraY))
        } else if (pu.type == PowerUpType.SHIELD_CAPSULE || pu.type == PowerUpType.HULL_REPAIR) {
            drawCircle(baseColor, radius = 18f, center = Offset(pu.x, pu.y - cameraY))
            drawCircle(Color.White.copy(alpha = 0.6f), radius = 22f, center = Offset(pu.x, pu.y - cameraY), style = Stroke(width = 2f))
            if ((gameTime / 200) % 2 == 0L) {
                drawCircle(Color.White, radius = 5f, center = Offset(pu.x, pu.y - cameraY))
            }
        } else {
            drawRect(baseColor, topLeft = Offset(pu.x - 12f, pu.y - cameraY - 15f), size = Size(24f, 30f))
            drawRect(Color.DarkGray, topLeft = Offset(pu.x - 6f, pu.y - cameraY - 20f), size = Size(12f, 5f))
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
