package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class SolarFlareRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val flareHeight = 600f
            val flameTop = ty - flareHeight / 2 + sin(gameTime / 150f) * 20f

            val flamePath = Path().apply {
                moveTo(0f, flameTop)
                repeat(10) { seg ->
                    val segX = seg * (size.width / 10f)
                    val segY = flameTop + sin(segX / 60f + gameTime / 200f) * 25f
                    lineTo(segX, segY)
                }
                lineTo(size.width, flameTop)
                lineTo(size.width, ty + flareHeight / 2)
                lineTo(0f, ty + flareHeight / 2)
                close()
            }
            drawPath(
                flamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFFFEA00).copy(alpha = 0.9f * alpha), Color(0xFFFF3D00).copy(alpha = 0.6f * alpha), Color.Transparent),
                    startY = ty - flareHeight / 2,
                    endY = ty + flareHeight / 2
                )
            )

            repeat(6) { i ->
                val tongueX = (gameTime / 2f + i * (size.width / 6f)) % size.width
                val tongueHeight = 40f + sin(gameTime / 300f + i) * 20f
                val tonguePath = Path().apply {
                    moveTo(tongueX - 15f, flameTop)
                    quadraticTo(tongueX, flameTop - tongueHeight, tongueX + 15f, flameTop)
                }
                drawPath(tonguePath, Color(0xFFFF3D00).copy(alpha = 0.5f * alpha), style = Stroke(width = 8f, cap = StrokeCap.Round))
                drawPath(tonguePath, Color(0xFFFFEA00).copy(alpha = 0.3f * alpha), style = Stroke(width = 3f, cap = StrokeCap.Round))
            }

            repeat(15) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 40).toInt()
                val rng = Random(seed)
                val ex = rng.nextFloat() * size.width
                val ey = flameTop + 50f + rng.nextFloat() * (flareHeight - 100f)
                drawCircle(Color(0xFFFF6D00).copy(alpha = 0.4f * alpha), radius = 1f + rng.nextFloat() * 3f, center = Offset(ex, ey))
            }

            val playerDistFromWave = abs(player.y - cameraY - (ty - flareHeight / 2))
            if (playerDistFromWave < 100f) {
                drawRect(Color.White.copy(alpha = 0.15f * (1f - playerDistFromWave / 100f)), topLeft = Offset(0f, 0f), size = size)
            }
        }
    }
}
