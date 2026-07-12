package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class VoidWraithRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val mat = threat.isMaterialized
            val visAlpha = if (mat) 1f else 0.03f

            if (mat) {
                val dangerPulse = (sin(gameTime / 80f) * 0.3f + 0.7f)
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.1f * dangerPulse), radius = 100f, center = Offset(tx, ty), style = Stroke(width = 3f))
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.05f * dangerPulse), radius = 120f, center = Offset(tx, ty), style = Stroke(width = 1f))
            }

            val auraColor = if (mat) Color(0xFF4A148C) else Color(0xFF616161)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = 0.15f * visAlpha), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 80f
                ),
                radius = 80f,
                center = Offset(tx, ty)
            )

            if (mat) {
                repeat(6) { i ->
                    val seed = threat.instanceId.hashCode() + i * 5 + (gameTime / 80).toInt()
                    val rng = Random(seed)
                    val ex = tx + (rng.nextFloat() - 0.5f) * 100f
                    val ey = ty + (rng.nextFloat() - 0.5f) * 100f
                    drawLine(Color(0xFFD500F9).copy(alpha = 0.4f), Offset(tx, ty), Offset(ex, ey), strokeWidth = 1f + rng.nextFloat() * 2f)
                }
            }

            if (mat) {
                val bodyAlpha = 0.7f
                val bodyColor = Color(0xFF1A1A2E).copy(alpha = bodyAlpha)
                val outlineColor = Color(0xFFD500F9).copy(alpha = bodyAlpha * 0.7f)
                val eyeIntensity = (sin(gameTime / 150f) * 0.3f + 0.7f)
                val px = player.x
                val py = player.y - cameraY

                drawCircle(bodyColor, radius = 20f, center = Offset(tx, ty - 40f))
                drawCircle(outlineColor, radius = 20f, center = Offset(tx, ty - 40f), style = Stroke(width = 3f))

                val lookAngle = atan2(py - (ty - 42f), px - tx)
                val pupilOffset = 3f
                val lx = tx - 7f + cos(lookAngle) * pupilOffset
                val ly = ty - 42f + sin(lookAngle) * pupilOffset
                val rx = tx + 7f + cos(lookAngle) * pupilOffset
                val ry = ty - 42f + sin(lookAngle) * pupilOffset
                drawCircle(Color.White.copy(alpha = 0.8f * eyeIntensity), radius = 5f, center = Offset(tx - 7f, ty - 42f))
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.9f * eyeIntensity), radius = 3f, center = Offset(lx, ly))
                drawCircle(Color.White.copy(alpha = 0.8f * eyeIntensity), radius = 5f, center = Offset(tx + 7f, ty - 42f))
                drawCircle(Color(0xFFFF1744).copy(alpha = 0.9f * eyeIntensity), radius = 3f, center = Offset(rx, ry))

                val torsoPath = Path().apply {
                    moveTo(tx - 25f, ty - 25f); lineTo(tx + 25f, ty - 25f)
                    lineTo(tx + 20f, ty + 30f); lineTo(tx - 20f, ty + 30f); close()
                }
                drawPath(torsoPath, bodyColor)
                drawPath(torsoPath, outlineColor, style = Stroke(width = 2f))

                repeat(2) { i ->
                    val armDir = if (i == 0) -1f else 1f
                    drawLine(bodyColor, Offset(tx + armDir * 25f, ty - 15f), Offset(tx + armDir * 50f, ty + 30f), strokeWidth = 8f)
                    drawLine(outlineColor, Offset(tx + armDir * 25f, ty - 15f), Offset(tx + armDir * 50f, ty + 30f), strokeWidth = 2f)
                }

                drawLine(bodyColor, Offset(tx - 12f, ty + 30f), Offset(tx - 20f, ty + 55f), strokeWidth = 7f)
                drawLine(bodyColor, Offset(tx + 12f, ty + 30f), Offset(tx + 20f, ty + 55f), strokeWidth = 7f)
                drawLine(outlineColor, Offset(tx - 12f, ty + 30f), Offset(tx - 20f, ty + 55f), strokeWidth = 2f)
                drawLine(outlineColor, Offset(tx + 12f, ty + 30f), Offset(tx + 20f, ty + 55f), strokeWidth = 2f)
            }
        }
    }
}
