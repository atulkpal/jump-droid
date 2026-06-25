package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.jump_droid.ui.theme.SciFiCyan
import kotlin.math.cos
import kotlin.math.sin

class ArchitectRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val healthPct = threat.health / threat.definition.baseHealth
        val dangerGlow = (1f - healthPct) * 0.8f

        drawScope.rotate(threat.rotation, pivot = Offset(cx, cy)) {
            val pulse = sin(gameTime / 300f) * 0.15f + 1f
            val size = 60f * pulse

            // Outer glow
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.08f * alpha),
                radius = size * 2.5f,
                center = Offset(cx, cy)
            )

            // Rotating geometric core diamond
            val path = Path().apply {
                moveTo(cx, cy - size)
                lineTo(cx + size, cy)
                lineTo(cx, cy + size)
                lineTo(cx - size, cy)
                close()
            }
            drawScope.drawPath(path, Color.Black.copy(alpha = alpha))
            drawScope.drawPath(path, SciFiCyan.copy(alpha = 0.5f * alpha), style = Stroke(width = 3f))

            // Inner diamond gradient
            val innerPath = Path().apply {
                moveTo(cx, cy - size * 0.6f)
                lineTo(cx + size * 0.6f, cy)
                lineTo(cx, cy + size * 0.6f)
                lineTo(cx - size * 0.6f, cy)
                close()
            }
            drawScope.drawPath(
                innerPath,
                Brush.radialGradient(
                    colors = listOf(
                        SciFiCyan.copy(alpha = 0.6f * alpha * dangerGlow),
                        Color.Black.copy(alpha = 0f)
                    ),
                    center = Offset(cx, cy),
                    radius = size * 0.6f
                )
            )

            // Sub-structures — rotating smaller diamonds at corners
            repeat(4) { i ->
                val angle = (i * 90f + gameTime / 50f) * (kotlin.math.PI.toFloat() / 180f)
                val dist = 85f + sin(gameTime / 400f + i * 1.5f) * 15f
                val dx = cos(angle) * dist
                val dy = sin(angle) * dist

                val subPath = Path().apply {
                    val s = 12f
                    moveTo(cx + dx, cy + dy - s)
                    lineTo(cx + dx + s, cy + dy)
                    lineTo(cx + dx, cy + dy + s)
                    lineTo(cx + dx - s, cy + dy)
                    close()
                }
                drawScope.drawPath(subPath, SciFiCyan.copy(alpha = 0.3f * alpha), style = Stroke(width = 2f))
                drawScope.drawPath(subPath, SciFiCyan.copy(alpha = 0.15f * alpha))
            }

            // Connecting energy lines
            repeat(4) { i ->
                val angle = i * 90f * (kotlin.math.PI.toFloat() / 180f)
                val dx = cos(angle) * size * 0.8f
                val dy = sin(angle) * size * 0.8f
                drawScope.drawLine(
                    color = SciFiCyan.copy(alpha = 0.2f * alpha * (0.5f + 0.5f * sin(gameTime / 200f + i))),
                    start = Offset(cx + dx, cy + dy),
                    end = Offset(cx + dx * 1.8f, cy + dy * 1.8f),
                    strokeWidth = 1.5f
                )
            }
        }

        // Danger pulse when low health
        if (healthPct < 0.4f) {
            val alarmPulse = sin(gameTime / 100f) * 0.3f + 0.7f
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.15f * alarmPulse * alpha),
                radius = 120f + alarmPulse * 40f,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )
        }
    }
}
