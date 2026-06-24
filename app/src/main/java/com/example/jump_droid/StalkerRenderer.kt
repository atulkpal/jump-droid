package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.random.Random

class StalkerRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val alertLevel = threat.alertLevel.coerceIn(0f, 1f)
            val bodyRadius = 15f + alertLevel * 10f
            val bodyColor = if (alertLevel > 0.5f) Color(0xFFFF6D00) else Color(0xFF880E4F)
            val heatAlpha = 0.3f + alertLevel * 0.5f
            val isTracking = threat.isTracking
            val px = player.x
            val py = player.y - cameraY

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF6D00).copy(alpha = heatAlpha * 0.5f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = bodyRadius * 3f
                ),
                radius = bodyRadius * 3f,
                center = Offset(tx, ty)
            )

            if (isTracking && alertLevel > 0.3f) {
                val laserAlpha = (sin(gameTime / 50f) * 0.3f + 0.7f) * alertLevel
                val laserEndX = px + (px - tx) * 3f
                val laserEndY = py + (py - ty) * 3f
                drawLine(Color(0xFFFF1744).copy(alpha = 0.15f * laserAlpha), Offset(tx, ty), Offset(laserEndX, laserEndY), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.05f * laserAlpha), Offset(tx, ty), Offset(laserEndX, laserEndY), strokeWidth = 3f)
            }

            if (alertLevel > 0.9f) {
                val maxPulse = (sin(gameTime / 30f) * 0.5f + 0.5f)
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.08f * maxPulse), radius = bodyRadius * 5f, center = Offset(tx, ty), style = Stroke(width = 4f))
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.04f * maxPulse), radius = bodyRadius * 6f, center = Offset(tx, ty), style = Stroke(width = 2f))
            }

            val bodyPath = Path().apply {
                moveTo(tx, ty - bodyRadius * 1.5f)
                lineTo(tx - bodyRadius * 1.2f, ty + bodyRadius * 0.8f)
                lineTo(tx + bodyRadius * 1.2f, ty + bodyRadius * 0.8f)
                close()
            }
            drawPath(bodyPath, bodyColor)
            drawPath(bodyPath, Color.White.copy(alpha = 0.3f * (0.5f + alertLevel * 0.5f)), style = Stroke(width = 2f))

            repeat(2) { i ->
                val dir = if (i == 0) -1f else 1f
                val antX = tx + dir * bodyRadius * 0.4f
                val antTipY = ty - bodyRadius * 2.2f + sin(gameTime / 120f + i * 2f) * 4f
                drawLine(Color(0xFFFFAB00).copy(alpha = 0.3f + alertLevel * 0.4f), Offset(antX, ty - bodyRadius * 1.4f), Offset(antX, antTipY), strokeWidth = 2f)
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.5f + alertLevel * 0.4f), radius = 2f, center = Offset(antX, antTipY))
            }

            repeat(2) { i ->
                val segY = ty - bodyRadius * 0.3f + i * (bodyRadius * 0.7f)
                drawLine(Color(0xFFFFAB00).copy(alpha = 0.2f + alertLevel * 0.4f), Offset(tx - bodyRadius * (0.6f - i * 0.3f), segY), Offset(tx + bodyRadius * (0.6f - i * 0.3f), segY), strokeWidth = 2f)
            }

            repeat(3) { i ->
                val shimmerX = tx + (i - 1) * bodyRadius * 0.5f
                val shimmerY = ty - bodyRadius * 1.8f + sin(gameTime / 100f + i) * 5f
                drawLine(Color(0xFFFF6D00).copy(alpha = 0.15f + alertLevel * 0.25f), Offset(shimmerX - 10f, shimmerY), Offset(shimmerX + 10f, shimmerY), strokeWidth = 2f)
            }

            drawRect(Color(0xFF1A1A1A).copy(alpha = 0.5f), Offset(tx - 15f, ty - bodyRadius * 1.8f), Size(30f, 4f))
            drawRect(Color(0xFFFF1744).copy(alpha = 0.7f), Offset(tx - 15f, ty - bodyRadius * 1.8f), Size(30f * alertLevel, 4f))

            val eyePulse = (sin(gameTime / 100f) * 0.3f + 0.7f) * (0.3f + alertLevel * 0.7f)
            drawCircle(Color.White.copy(alpha = eyePulse), radius = 5f, center = Offset(tx, ty - 3f))
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.6f + 0.4f * eyePulse), radius = 3f, center = Offset(tx, ty - 3f))

            if (alertLevel > 0.2f) {
                repeat((alertLevel * 6).toInt().coerceAtLeast(1)) { i ->
                    val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 100).toInt()
                    val rng = Random(seed)
                    val hpx = tx + (rng.nextFloat() - 0.5f) * 30f
                    val hpy = ty + bodyRadius + rng.nextFloat() * 40f * alertLevel
                    val particleAlpha = (1f - (hpy - ty - bodyRadius) / (40f * alertLevel)) * 0.6f
                    drawCircle(Color(0xFFFF6D00).copy(alpha = particleAlpha), radius = 1f + rng.nextFloat() * 3f * alertLevel, center = Offset(hpx, hpy))
                }
            }

            val scanSpeed = 1f + alertLevel * 3f
            val scanPhase = (gameTime / 1000f * scanSpeed) % 1f
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.15f * (1f - scanPhase)), radius = bodyRadius * 1.5f + 80f * scanPhase, center = Offset(tx, ty), style = Stroke(width = 2f))
            val scanPhase2 = (scanPhase + 0.5f) % 1f
            drawCircle(Color(0xFFFF6D00).copy(alpha = 0.1f * (1f - scanPhase2)), radius = bodyRadius * 1.5f + 80f * scanPhase2, center = Offset(tx, ty), style = Stroke(width = 1f))
        }
    }
}
