package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SwarmBotsRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val pulse = (sin(gameTime / 300f) * 0.2f + 0.8f)
            val playerInside = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty) < 150f * 150f

            val boundaryRadius = 100f + pulse * 30f
            drawCircle(Color.White.copy(alpha = 0.05f), radius = boundaryRadius, center = Offset(tx, ty), style = Stroke(width = 1f))

            val particleCount = 35
            repeat(particleCount) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 80).toInt()
                val rng = Random(seed)
                val baseR = 20f + rng.nextFloat() * 60f
                val baseAngle = (i / particleCount.toFloat()) * 2f * PI.toFloat() + (gameTime / 1000f)
                val ox = cos(baseAngle + rng.nextFloat() * 0.3f) * baseR * pulse
                val oy = sin(baseAngle * 1.3f + rng.nextFloat() * 0.3f) * baseR * pulse * 0.8f
                val px = tx + ox
                val py = ty + oy

                val isQueen = i % 12 == 0 && i > 0
                val radius = if (isQueen) 6f + rng.nextFloat() * 2f else 1f + rng.nextFloat() * 3f
                val color = if (isQueen) Color.Cyan else Color.White.copy(alpha = 0.3f)

                drawCircle(color = color, radius = radius, center = Offset(px, py))

                if (isQueen) {
                    drawCircle(Color.Red.copy(alpha = 0.5f), radius = 3f, center = Offset(px, py))
                }

                repeat(2) { t ->
                    val trailX = px - ox * 0.3f * (t + 1)
                    val trailY = py - oy * 0.3f * (t + 1)
                    drawCircle(Color.White.copy(alpha = 0.08f / (t + 1)), radius = radius * 0.7f, center = Offset(trailX, trailY))
                }

                if (rng.nextFloat() < 0.15f) {
                    drawCircle(Color.Cyan.copy(alpha = 0.6f), radius = 1.5f, center = Offset(px, py))
                }
            }

            if (playerInside) {
                repeat(5) { i ->
                    val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 30).toInt()
                    val rng = Random(seed)
                    val fx = tx + (rng.nextFloat() - 0.5f) * 200f
                    val fy = ty + (rng.nextFloat() - 0.5f) * 200f
                    drawCircle(Color.White.copy(alpha = 0.4f), radius = 1f + rng.nextFloat() * 2f, center = Offset(fx, fy))
                }
            }
        }
    }
}
