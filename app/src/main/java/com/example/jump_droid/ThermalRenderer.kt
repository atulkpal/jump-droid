package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin
import kotlin.random.Random

class ThermalRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val columnHeight = 500f
            val columnWidth = 80f
            val shimmer = sin(gameTime / 200f) * 10f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF6D00).copy(alpha = 0.3f * alpha), Color.Transparent),
                    center = Offset(tx, ty + columnHeight / 2),
                    radius = columnWidth * 2f
                ),
                radius = columnWidth * 2f,
                center = Offset(tx, ty + columnHeight / 2)
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFFF6D00).copy(alpha = 0.2f * alpha), Color(0xFFFFAB00).copy(alpha = 0.3f * alpha), Color.Transparent),
                    startY = ty - columnHeight / 2,
                    endY = ty + columnHeight / 2
                ),
                topLeft = Offset(tx - columnWidth / 2 + shimmer, ty - columnHeight / 2),
                size = Size(columnWidth, columnHeight)
            )

            repeat(8) { i ->
                val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 60).toInt()
                val rng = Random(seed)
                val px = tx + (rng.nextFloat() - 0.5f) * columnWidth * 0.8f + shimmer
                val py = ty + columnHeight / 2 - ((gameTime / 100f + i * 50f) % columnHeight)
                val particleAlpha = 1f - (ty + columnHeight / 2 - py) / columnHeight
                drawCircle(Color(0xFFFFAB00).copy(alpha = 0.5f * particleAlpha * alpha), radius = 2f + rng.nextFloat() * 3f, center = Offset(px, py))
            }

            repeat(3) { i ->
                val lineY = ty - columnHeight / 3 + i * (columnHeight / 3) + sin(gameTime / 150f + i * 2f) * 15f
                val lineX = tx - columnWidth / 2 + sin(gameTime / 100f + i) * 20f
                drawLine(Color.White.copy(alpha = 0.1f * alpha), Offset(lineX, lineY), Offset(lineX + columnWidth, lineY), strokeWidth = 1f)
            }
        }
    }
}
