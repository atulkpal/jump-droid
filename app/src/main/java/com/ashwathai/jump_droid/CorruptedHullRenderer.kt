package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

class CorruptedHullRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val rot = threat.rotation

            val goldColor = Color(0xFFFF8F00)
            val goldShine = (sin(gameTime / 200f) * 0.3f + 0.7f)

            val beaconColor = if ((gameTime / 500) % 2 == 0L) Color(0xFF00E676) else Color(0xFFFFEA00)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(beaconColor.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 80f
                ),
                radius = 80f,
                center = Offset(tx, ty)
            )

            rotate(rot, pivot = Offset(tx, ty)) {
                drawRect(goldColor.copy(alpha = 0.6f), topLeft = Offset(tx - 15f, ty - 25f), size = Size(30f, 40f))
                drawRect(Color.White.copy(alpha = 0.2f), topLeft = Offset(tx - 15f, ty - 25f), size = Size(30f, 40f), style = Stroke(width = 1f))

                drawLine(Color(0xFFFFAB00).copy(alpha = 0.8f), Offset(tx - 15f, ty - 25f), Offset(tx - 10f, ty - 35f), strokeWidth = 3f)
                drawLine(Color(0xFFFFAB00).copy(alpha = 0.8f), Offset(tx + 15f, ty - 25f), Offset(tx + 10f, ty - 35f), strokeWidth = 3f)

                drawCircle(Color.Cyan.copy(alpha = 0.6f * goldShine), radius = 6f, center = Offset(tx, ty - 18f))
            }

            repeat(3) { i ->
                val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 60).toInt()
                val rng = Random(seed)
                val sx = tx + (rng.nextFloat() - 0.5f) * 60f
                val sy = ty + (rng.nextFloat() - 0.5f) * 60f
                drawCircle(Color(0xFFFFD54F).copy(alpha = 0.5f), radius = 1f + rng.nextFloat() * 2f, center = Offset(sx, sy))
            }

            repeat(3) { i ->
                val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 80).toInt()
                val rng = Random(seed)
                val startX = tx + (rng.nextFloat() - 0.5f) * 200f
                val startY = ty + (rng.nextFloat() - 0.5f) * 200f
                drawLine(Color(0xFFFFD54F).copy(alpha = 0.15f), Offset(startX, startY), Offset(tx, ty), strokeWidth = 1f)
            }
        }
    }
}
