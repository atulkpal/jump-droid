package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

class StormRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val r = 60f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7E57C2).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = r
                ),
                radius = r,
                center = Offset(tx, ty)
            )
            if ((gameTime / 100) % 10 < 3L) {
                val randBolt = Random(gameTime / 100)
                repeat(2) {
                    val bx = tx + (randBolt.nextFloat() - 0.5f) * r * 2
                    val by = ty + (randBolt.nextFloat() - 0.5f) * r * 2
                    drawLine(Color.White, start = Offset(tx, ty), end = Offset(bx, by), strokeWidth = 2f)
                }
            }
        }
    }
}
