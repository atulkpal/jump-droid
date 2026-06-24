package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GravityRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val pulse = threat.scanPulse

            repeat(4) { i ->
                val radius = (100f + i * 150f + pulse * 100f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f * (1f - pulse) * alpha),
                    radius = radius,
                    center = Offset(tx, ty),
                    style = Stroke(width = 2f + i)
                )
            }

            repeat(20) { i ->
                val seed = threat.instanceId.hashCode() + i
                val rng = Random(seed)
                val angle = rng.nextFloat() * 2f * PI.toFloat()
                val dist = 200f + rng.nextFloat() * 400f
                val sx = tx + cos(angle) * dist
                val sy = ty + sin(angle) * dist + 150f * pulse / (dist / 100f)
                drawCircle(Color.White.copy(alpha = 0.2f * alpha * (1f - dist / 600f)), radius = 1f, center = Offset(sx, sy))
            }

            repeat(8) { i ->
                val angle = (i / 8f) * 2f * PI.toFloat()
                val arrowDist = 140f
                val ax = tx + cos(angle) * arrowDist
                val ay = ty + sin(angle) * arrowDist
                val arrowLen = 40f + pulse * 30f
                val gx = ax
                val gy = ay + arrowLen
                drawLine(Color.Cyan.copy(alpha = 0.2f * alpha), Offset(ax, ay), Offset(gx, gy + arrowLen), strokeWidth = 3f)
                val head = Path().apply {
                    moveTo(gx, gy + arrowLen)
                    lineTo(gx - 8f, gy + arrowLen - 12f)
                    lineTo(gx + 8f, gy + arrowLen - 12f)
                    close()
                }
                drawPath(head, Color.Cyan.copy(alpha = 0.2f * alpha))
            }

            drawCircle(color = Color.Black.copy(alpha = 0.8f * alpha), radius = 60f, center = Offset(tx, ty))
            drawCircle(color = Color.Cyan.copy(alpha = 0.4f * alpha), radius = 70f + (sin(gameTime / 100f) * 10f), center = Offset(tx, ty), style = Stroke(width = 4f))

            repeat(6) { i ->
                val seed = threat.instanceId.hashCode() + i * 5 + (gameTime / 60).toInt()
                val rng = Random(seed)
                val sx = tx + (rng.nextFloat() - 0.5f) * 300f
                val sy = ty - 200f + (rng.nextFloat() * 400f)
                drawCircle(Color.Cyan.copy(alpha = 0.3f * alpha), radius = 2f, center = Offset(sx, sy))
                drawLine(Color.Cyan.copy(alpha = 0.15f * alpha), Offset(sx, sy), Offset(sx, sy + 30f), strokeWidth = 1f)
            }
        }
    }
}
