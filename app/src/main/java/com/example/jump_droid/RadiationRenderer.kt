package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class RadiationRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val pulse = threat.scanPulse
            val playerInZone = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty) < 300f * 300f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4A148C).copy(alpha = 0.15f * alpha), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 800f
                ),
                radius = 800f,
                center = Offset(tx, ty)
            )

            drawCircle(
                color = Color(0xFF4A148C).copy(alpha = 0.2f * alpha),
                radius = 350f + pulse * 20f,
                center = Offset(tx, ty),
                style = Stroke(width = 20f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6A1B9A).copy(alpha = 0.6f * alpha),
                        Color(0xFF2E7D32).copy(alpha = 0.3f * alpha),
                        Color.Transparent
                    ),
                    center = Offset(tx, ty),
                    radius = 400f
                ),
                radius = 400f,
                center = Offset(tx, ty)
            )

            if (playerInZone) {
                repeat(5) { i ->
                    val angle = (i / 5f) * 2f * PI.toFloat() + (gameTime / 300f)
                    val baseX = tx + cos(angle) * 250f
                    val baseY = ty + sin(angle) * 250f
                    val tipX = player.x
                    val tipY = player.y - cameraY
                    val tendrilPath = Path().apply {
                        moveTo(baseX, baseY)
                        cubicTo(baseX + (tipX - baseX) * 0.3f, baseY, tipX * 0.7f + baseX * 0.3f, tipY * 0.7f + baseY * 0.3f, tipX, tipY)
                    }
                    drawPath(tendrilPath, Color(0xFF00E676).copy(alpha = 0.2f * pulse * alpha), style = Stroke(width = 2f))
                }
            }

            repeat(12) { i ->
                val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 50).toInt()
                val rng = Random(seed)
                if (rng.nextFloat() < 0.3f) {
                    val gx = tx + (rng.nextFloat() - 0.5f) * 600f
                    val gy = ty + (rng.nextFloat() - 0.5f) * 600f
                    drawRect(Color(0xFF00E676).copy(alpha = 0.4f * alpha), Offset(gx, gy), Size(2f + rng.nextFloat() * 4f, 2f))
                }
            }

            repeat(8) { i ->
                val pRandom = Random(threat.instanceId.hashCode() + i + (gameTime / 100).toInt())
                val angle = (gameTime / 1000f) + i * (2f * PI.toFloat() / 8f)
                val px = tx + cos(angle.toDouble()).toFloat() * (150f + pRandom.nextFloat() * 150f)
                val py = ty + sin(angle.toDouble()).toFloat() * (150f + pRandom.nextFloat() * 150f)
                drawCircle(Color.Green.copy(alpha = 0.6f * alpha), 4f, Offset(px, py))
            }
        }
    }
}
