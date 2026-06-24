package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class StarEaterRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x; val ty = threat.y - cameraY
            val pulse = (sin(gameTime / 400f) * 0.1f + 0.9f)
            val phase = threat.phase
            val auraRadius = if (phase == 3) 1000f else 800f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black, Color(0xFF6A1B9A).copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(tx, ty)
            )

            repeat(15) { i ->
                val rand = Random(threat.instanceId.hashCode() + i)
                val angle = (gameTime / 10f + i * 24f) * (PI.toFloat() / 180f)
                val dist = ((gameTime / 5f + i * 100f) % auraRadius)
                val px = tx + cos(angle) * dist
                val py = ty + sin(angle) * dist
                drawCircle(Color.Magenta.copy(alpha = 0.4f), radius = 5f, center = Offset(px, py))
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6A1B9A).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(tx - 50f, ty + 30f),
                    radius = auraRadius * 0.8f
                ),
                radius = auraRadius * 0.8f,
                center = Offset(tx - 50f, ty + 30f)
            )

            repeat(4) { i ->
                val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 80).toInt()
                val rng = Random(seed)
                val sx = tx + (rng.nextFloat() - 0.5f) * 600f
                val sy = ty + (rng.nextFloat() - 0.5f) * 600f
                val streamPath = Path().apply {
                    moveTo(sx, sy)
                    cubicTo(sx, (sy + ty) * 0.5f, (sx + tx) * 0.5f, ty, tx, ty)
                }
                drawPath(streamPath, Color(0xFFBA68C8).copy(alpha = 0.15f), style = Stroke(width = 2f))
            }

            drawCircle(Color.Black, radius = 120f * pulse, center = Offset(tx, ty))

            val toothCount = 16
            repeat(toothCount) { i ->
                val ta = (i / toothCount.toFloat()) * 2f * PI.toFloat() + (gameTime / 2000f)
                val innerR = 100f
                val outerR = 120f + pulse * 10f
                val tx1 = tx + cos(ta) * innerR
                val ty1 = ty + sin(ta) * innerR
                val tx2 = tx + cos(ta) * outerR
                val ty2 = ty + sin(ta) * outerR
                drawLine(Color(0xFFCE93D8).copy(alpha = 0.5f * pulse), Offset(tx1, ty1), Offset(tx2, ty2), strokeWidth = 4f * pulse)
            }

            if (threat.activeWeakPoints > 0) {
                drawCircle(Color.Magenta.copy(alpha = 1.0f), radius = 50f * pulse, center = Offset(tx, ty))
                drawCircle(Color.White, radius = 15f, center = Offset(tx, ty))
            }

            val pDist = sqrt((player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty))
            val tendrilGlow = if (pDist < 400f) 1.0f else 0.5f
            repeat(12) { i ->
                val angle = i * 30f + sin(gameTime / 300f + i) * 40f
                rotate(angle, pivot = Offset(tx, ty)) {
                    drawLine(
                        if (phase == 3) Color.Red else Color(0xFFBA68C8).copy(alpha = tendrilGlow),
                        Offset(tx + 80f, ty),
                        Offset(tx + 400f, ty),
                        strokeWidth = 15f * pulse * tendrilGlow
                    )
                }
            }

            val hungerRate = 1f + (1f - pDist / 1000f).coerceIn(0f, 0.5f)
            drawCircle(Color(0xFFFF4081).copy(alpha = 0.1f * hungerRate), radius = 80f + pulse * 20f, center = Offset(tx, ty), style = Stroke(width = 3f))
        }
    }
}
