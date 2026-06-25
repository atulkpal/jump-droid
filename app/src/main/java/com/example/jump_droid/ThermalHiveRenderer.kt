package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.jump_droid.ui.theme.SciFiOrange
import com.example.jump_droid.ui.theme.SciFiRed
import kotlin.math.cos
import kotlin.math.sin

class ThermalHiveRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val pulse = sin(gameTime / 200f) * 0.1f + 0.9f
        val heatDanger = player.heat > 60f

        // Heat haze aura
        drawScope.drawCircle(
            color = SciFiOrange.copy(alpha = (if (heatDanger) 0.15f else 0.05f) * alpha),
            radius = 80f * pulse,
            center = Offset(cx, cy)
        )

        // Outer glow ring (heat-reactive)
        val glowRadius = 55f * pulse
        val glowColor = if (heatDanger) SciFiRed else SciFiOrange
        val glowAlpha = if (heatDanger) 0.7f else 0.3f

        drawScope.drawCircle(
            color = glowColor.copy(alpha = glowAlpha * alpha),
            radius = glowRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 4f)
        )

        // Second glow ring
        drawScope.drawCircle(
            color = glowColor.copy(alpha = (glowAlpha * 0.5f) * alpha),
            radius = glowRadius + 15f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        // Black organic core with gradient
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A0000).copy(alpha = alpha),
                    Color.Black.copy(alpha = alpha),
                    Color.Black.copy(alpha = alpha * 0.5f)
                ),
                center = Offset(cx, cy),
                radius = 45f * pulse
            ),
            radius = 45f * pulse,
            center = Offset(cx, cy)
        )

        // Internal heat veins
        repeat(3) { i ->
            val va = (i * 120f + gameTime / 100f) * (kotlin.math.PI.toFloat() / 180f)
            val vd = 20f + sin(gameTime / 400f + i * 2f) * 10f
            val vx = cx + cos(va) * vd
            val vy = cy + sin(va) * vd
            drawScope.drawCircle(
                color = glowColor.copy(alpha = (0.3f + 0.3f * sin(gameTime / 150f + i)) * alpha),
                radius = 4f + sin(gameTime / 200f + i * 1.3f) * 2f,
                center = Offset(vx, vy)
            )
        }

        // Swarm particles — orbiting, heat-reactive density
        val particleCount = if (heatDanger) 12 else 6
        val particleSpeed = if (heatDanger) 80f else 150f
        repeat(particleCount) { i ->
            val angle = (gameTime / particleSpeed) + (i * (kotlin.math.PI.toFloat() * 2f / particleCount))
            val dist = 28f + sin(gameTime / 300f + i * 1.7f) * 12f
            val px = cx + cos(angle) * dist
            val py = cy + sin(angle) * dist
            val pSize = if (heatDanger) 3f else 2f
            val pColor = if (heatDanger) SciFiRed else SciFiOrange
            drawScope.drawCircle(
                color = pColor.copy(alpha = (0.4f + 0.4f * sin(gameTime / 100f + i * 2.1f)) * alpha),
                radius = pSize,
                center = Offset(px, py)
            )
        }

        // Swarm spawn VFX when heat is high
        if (heatDanger) {
            val spawnPulse = sin(gameTime / 50f) * 0.5f + 0.5f
            if (spawnPulse > 0.8f) {
                repeat(3) { i ->
                    val sa = (i * 120f + gameTime / 30f) * (kotlin.math.PI.toFloat() / 180f)
                    val sd = 60f + spawnPulse * 30f
                    drawScope.drawCircle(
                        color = SciFiRed.copy(alpha = (0.3f * (1f - spawnPulse)) * alpha),
                        radius = 3f + spawnPulse * 5f,
                        center = Offset(cx + cos(sa) * sd, cy + sin(sa) * sd)
                    )
                }
            }
        }
    }
}
