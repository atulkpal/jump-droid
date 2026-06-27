package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.random.Random

class CloudSkimmerRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val distSq = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty)
            val isNear = distSq < 250f * 250f

            val dir = if (threat.vx > 0) 1f else -1f
            val wingSpan = 80f
            val flap = sin(gameTime / 600f) * 20f
            val friendlyColor = Color(0xFF00E676)
            val glowColor = Color(0xFF1DE9B6)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 80f
                ),
                radius = 80f,
                center = Offset(tx, ty)
            )

            if (isNear) {
                val trailPath = Path().apply {
                    moveTo(tx - 80f * dir, ty)
                    cubicTo(tx - 40f * dir, ty - 30f, tx + 40f * dir, ty - 50f, tx + 80f * dir, ty - 20f)
                }
                drawPath(trailPath, glowColor.copy(alpha = 0.2f), style = Stroke(width = 4f))

                repeat(3) { i ->
                    val slipX = tx + (Random.nextFloat() - 0.5f) * 40f
                    val slipY = ty + i * 20f
                    drawLine(glowColor.copy(alpha = 0.4f), Offset(slipX, slipY), Offset(slipX, slipY - 80f), strokeWidth = 3f)
                }

                repeat(4) { i ->
                    val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 40).toInt()
                    val rng = Random(seed)
                    val sx = tx + (rng.nextFloat() - 0.5f) * 60f
                    val sy = ty + (rng.nextFloat() - 0.5f) * 60f
                    drawCircle(Color(0xFF69F0AE).copy(alpha = 0.5f), radius = 1f + rng.nextFloat() * 2f, center = Offset(sx, sy))
                }
            }

            val rayPath = Path().apply {
                moveTo(tx - 40f * dir, ty)
                quadraticTo(tx, ty - 10f, tx + 40f * dir, ty)
                moveTo(tx, ty - 5f)
                lineTo(tx - 20f * dir, ty + flap)
                lineTo(tx - wingSpan * dir, ty + flap * 1.5f)
                lineTo(tx - 20f * dir, ty + 15f)
                close()
            }
            drawPath(rayPath, friendlyColor.copy(alpha = 0.4f))
            drawPath(rayPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 2f))

            val arrowY = ty + flap * 0.5f
            val arrowX1 = tx - 50f * dir
            val arrowX2 = tx + 10f * dir
            drawLine(glowColor.copy(alpha = 0.3f), Offset(arrowX1, arrowY), Offset(arrowX2, arrowY), strokeWidth = 2f)
            drawLine(glowColor.copy(alpha = 0.3f), Offset(arrowX2, arrowY), Offset(arrowX2 - 10f * dir, arrowY - 8f), strokeWidth = 2f)
            drawLine(glowColor.copy(alpha = 0.3f), Offset(arrowX2, arrowY), Offset(arrowX2 - 10f * dir, arrowY + 8f), strokeWidth = 2f)

            repeat(3) { i ->
                drawCircle(friendlyColor.copy(alpha = 0.2f), radius = 5f - i, center = Offset(tx - (60f + i * 20f) * dir, ty + sin(gameTime / 400f + i) * 10f))
            }
        }
    }
}
