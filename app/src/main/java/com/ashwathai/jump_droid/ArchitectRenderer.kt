package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import kotlin.math.cos
import kotlin.math.sin

class ArchitectRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player,
        context: android.content.Context?
    ) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val healthPct = threat.health / threat.definition.baseHealth
        val dangerGlow = (1f - healthPct) * 0.8f

        drawScope.rotate(threat.rotation, pivot = Offset(cx, cy)) {
            val pulse = sin((gameTime / 250f).toDouble()).toFloat() * 0.15f + 1f
            val baseSize = 65f * pulse

            // D1: Fractal Unfolding (Nested scaled geometric layers)
            repeat(4) { layer ->
                val layerScale = 1f - (layer * 0.2f)
                val layerPulse = (sin((gameTime / (400f + layer * 100f)).toDouble()).toFloat() * 0.1f + 0.9f)
                val s = baseSize * layerScale * layerPulse
                val layerRot = if (layer % 2 == 0) gameTime / 10f else -gameTime / 8f
                
                drawScope.rotate(layerRot, pivot = Offset(cx, cy)) {
                    val p = Path().apply {
                        moveTo(cx, cy - s)
                        lineTo(cx + s, cy)
                        lineTo(cx, cy + s)
                        lineTo(cx - s, cy)
                        close()
                    }
                    drawScope.drawPath(p, Color.Black.copy(alpha = alpha * (1f - layer * 0.2f)))
                    drawScope.drawPath(p, SciFiCyan.copy(alpha = 0.6f * alpha * (1f - layer * 0.2f)), style = Stroke(width = 3f - layer * 0.5f))
                }
            }

            // D2: Energy Conduits (Lightning-bolt links between orbiting sub-structures)
            repeat(4) { i ->
                val angle = (i * 90f + gameTime / 60f) * (kotlin.math.PI.toFloat() / 180f)
                val dist = 100f + sin((gameTime / 350f + i * 1.5f).toDouble()).toFloat() * 20f
                val dx = cos(angle) * dist
                val dy = sin(angle) * dist
                
                // Drawing orbiting diamond
                val s = 14f
                val subPath = Path().apply {
                    moveTo(cx + dx, cy + dy - s)
                    lineTo(cx + dx + s, cy + dy)
                    lineTo(cx + dx, cy + dy + s)
                    lineTo(cx + dx - s, cy + dy)
                    close()
                }
                drawScope.drawPath(subPath, SciFiCyan.copy(alpha = 0.5f * alpha), style = Stroke(width = 2.5f))
                
                // Conduits to neighbors
                val nextAngle = ((i + 1) * 90f + gameTime / 60f) * (kotlin.math.PI.toFloat() / 180f)
                val nx = cos(nextAngle) * dist
                val ny = sin(nextAngle) * dist
                
                if (gameTime % 100 < 50) { // Flickering conduit
                    drawScope.drawLine(
                        color = SciFiCyan.copy(alpha = 0.3f * alpha),
                        start = Offset(cx + dx, cy + dy),
                        end = Offset(cx + nx, cy + ny),
                        strokeWidth = 1.5f
                    )
                }
            }

            // D3: Sharp Glowing Edge Highlights (Geometric purity)
            repeat(4) { i ->
                val angle = i * 90f * (kotlin.math.PI.toFloat() / 180f)
                val dx = cos(angle) * baseSize * 1.1f
                val dy = sin(angle) * baseSize * 1.1f
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.4f * alpha),
                    radius = 3f,
                    center = Offset(cx + dx, cy + dy)
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

        // Weak point indicators — orbiting magenta diamonds
        if (threat.activeWeakPoints > 0) {
            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
            val wpPulse = sin(gameTime / 200f) * 0.3f + 0.7f
            repeat(4) { i ->
                if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                val angle = (i * 90f + gameTime / 400f) * (kotlin.math.PI.toFloat() / 180f)
                val dist = 95f + sin(gameTime / 500f + i * 1.5f) * 10f
                val wx = cx + cos(angle) * dist
                val wy = cy + sin(angle) * dist
                val ws = 6f * wpPulse
                val wpPath = Path().apply {
                    moveTo(wx, wy - ws)
                    lineTo(wx + ws, wy)
                    lineTo(wx, wy + ws)
                    lineTo(wx - ws, wy)
                    close()
                }
                drawScope.drawPath(wpPath, Color.Magenta.copy(alpha = 0.7f * wpGlow * alpha))
                drawScope.drawPath(wpPath, Color.White.copy(alpha = 0.3f * wpGlow * alpha), style = Stroke(width = 2f))
                }
            }
        }
    }
}
