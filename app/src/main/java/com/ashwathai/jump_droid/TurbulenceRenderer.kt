package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TurbulenceRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val windDir = if (threat.instanceId.hashCode() % 2 == 0) 1f else -1f
            val strength = (sin(gameTime / 500f) * 0.3f + 0.7f)
            val streakCount = (10 + (strength * 30).toInt()).coerceAtMost(40)

            val arrowSize = 80f
            val arrowX = tx
            val arrowY = ty
            drawLine(Color.White.copy(alpha = 0.15f * alpha), Offset(arrowX - arrowSize * windDir, arrowY), Offset(arrowX + arrowSize * windDir, arrowY), strokeWidth = 10f, cap = StrokeCap.Round)

            val arrowHeadPath = Path().apply {
                moveTo(arrowX + arrowSize * windDir, arrowY)
                lineTo(arrowX + (arrowSize - 30f) * windDir, arrowY - 20f)
                lineTo(arrowX + (arrowSize - 30f) * windDir, arrowY + 20f)
                close()
            }
            drawPath(arrowHeadPath, Color.White.copy(alpha = 0.15f * alpha))

            repeat(3) { spiral ->
                val spiralPath = Path().apply {
                    var sx = tx
                    var sy = ty
                    repeat(20) { seg ->
                        val t = seg / 20f
                        val radius = 20f + t * 180f
                        val angle = t * 4f * PI.toFloat() * windDir + spiral * 2.1f
                        sx = tx + cos(angle) * radius
                        sy = ty + sin(angle) * radius
                        lineTo(sx, sy)
                    }
                }
                drawPath(spiralPath, Color.White.copy(alpha = 0.08f * alpha), style = Stroke(width = 2f))
            }

            repeat(streakCount) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 80).toInt()
                val rng = Random(seed)
                val rx = tx + (rng.nextFloat() - 0.5f) * 800f
                val ry = ty + (rng.nextFloat() - 0.5f) * 800f
                val progress = (threat.scanPulse + rng.nextFloat()) % 1.0f
                val streakLen = 80f + strength * 80f
                val sx = rx + (progress * 300f * windDir)
                val streakAlpha = 0.3f * (1f - progress) * alpha * strength

                drawLine(
                    color = Color.White.copy(alpha = streakAlpha),
                    start = Offset(sx, ry),
                    end = Offset(sx + streakLen * windDir, ry),
                    strokeWidth = 4f + strength * 4f,
                    cap = StrokeCap.Round
                )

                if (progress > 0.5f && rng.nextFloat() < 0.3f) {
                    val tipPath = Path().apply {
                        moveTo(sx + streakLen * windDir, ry)
                        lineTo(sx + (streakLen - 15f) * windDir, ry - 6f)
                        lineTo(sx + (streakLen - 15f) * windDir, ry + 6f)
                        close()
                    }
                    drawPath(tipPath, Color.White.copy(alpha = streakAlpha * 0.6f))
                }
            }
        }
    }
}
