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

class VoidAnomalyRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val pulse = (sin(gameTime / 1000f) * 0.3f + 0.7f)
            val scan = threat.scanPulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6A1B9A).copy(alpha = 0.5f * pulse), Color(0xFF4A148C).copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 150f
                ),
                radius = 150f,
                center = Offset(tx, ty)
            )

            val tearPath = Path().apply {
                val segments = 12
                moveTo(tx + 100f, ty)
                repeat(segments) {
                    val angle = ((it + 1) / segments.toFloat()) * 2f * PI.toFloat()
                    val seed = threat.instanceId.hashCode() + it + (gameTime / 200).toInt()
                    val rng = Random(seed)
                    val jitter = 90f + rng.nextFloat() * 20f
                    lineTo(tx + cos(angle) * jitter, ty + sin(angle) * jitter)
                }
                close()
            }
            drawPath(tearPath, Color(0xFFAA00FF).copy(alpha = 0.15f * pulse), style = Stroke(width = 2f))

            repeat(3) { i ->
                val ringPulse = (scan + i * 0.33f) % 1f
                drawCircle(
                    color = Color(0xFFE1BEE7).copy(alpha = 0.15f * (1f - ringPulse)),
                    radius = 20f + 130f * ringPulse,
                    center = Offset(tx, ty),
                    style = Stroke(width = 2f)
                )
            }

            repeat(10) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 80).toInt()
                val rng = Random(seed)
                val angle = (gameTime / 400f + i * 0.63f) % (2f * PI.toFloat())
                val dist = 30f + ((gameTime / 60f + i * 30f) % 120f)
                val px = tx + cos(angle) * dist
                val py = ty + sin(angle) * dist
                drawCircle(Color(0xFFCE93D8).copy(alpha = 0.5f * pulse * (1f - dist / 150f)), radius = 2f + rng.nextFloat() * 2f, center = Offset(px, py))
            }

            repeat(4) { i ->
                val pullAngle = (i / 4f) * 2f * PI.toFloat()
                val pullEnd = if (i % 2 == 0) Offset(if (i == 0) -100f else size.width + 100f, ty) else Offset(tx, if (i == 1) -100f else size.height + 100f)
                drawLine(Color(0xFFCE93D8).copy(alpha = 0.1f * pulse), Offset(tx, ty), pullEnd, strokeWidth = 2f)
            }
        }
    }
}
