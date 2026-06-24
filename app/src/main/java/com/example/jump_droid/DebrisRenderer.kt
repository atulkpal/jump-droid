package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DebrisRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val random = Random(threat.instanceId.hashCode())

            rotate(threat.rotation, pivot = Offset(tx, ty)) {
                val debrisColor = if (random.nextBoolean()) Color(0xFF757575) else Color(0xFF424242)

                val variant = abs(threat.instanceId.hashCode() % 3)
                val debrisPath = Path().apply {
                    when (variant) {
                        0 -> {
                            moveTo(tx - 50f, ty - 30f); lineTo(tx + 30f, ty - 50f)
                            lineTo(tx + 60f, ty - 10f); lineTo(tx + 40f, ty + 40f)
                            lineTo(tx - 20f, ty + 50f); lineTo(tx - 60f, ty + 20f)
                            close()
                        }
                        1 -> {
                            moveTo(tx - 40f, ty - 50f); lineTo(tx + 50f, ty - 30f)
                            lineTo(tx + 30f, ty + 10f); lineTo(tx + 50f, ty + 40f)
                            lineTo(tx - 10f, ty + 30f); lineTo(tx - 50f, ty + 10f)
                            close()
                        }
                        else -> {
                            moveTo(tx - 30f, ty - 60f); lineTo(tx + 40f, ty - 40f)
                            lineTo(tx + 50f, ty); lineTo(tx + 20f, ty + 50f)
                            lineTo(tx - 40f, ty + 30f); lineTo(tx - 60f, ty - 10f)
                            close()
                        }
                    }
                }
                drawPath(debrisPath, debrisColor.copy(alpha = alpha))
                drawPath(debrisPath, Color.White.copy(alpha = 0.1f * alpha), style = Stroke(width = 2f))

                val glowColor = Color(0xFFFF6D00)
                drawPath(debrisPath, glowColor.copy(alpha = 0.15f * alpha), style = Stroke(width = 4f))
            }

            repeat(3) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 30).toInt()
                val rng = Random(seed)
                val sx = tx + (rng.nextFloat() - 0.5f) * 80f
                val sy = ty + (rng.nextFloat() - 0.5f) * 80f
                drawCircle(Color(0xFFFFAB00).copy(alpha = 0.5f * alpha), radius = 2f, center = Offset(sx, sy))
            }

            repeat(2) { i ->
                val blurX = tx + (i + 1) * 15f * cos(threat.rotation * PI.toFloat() / 180f)
                val blurY = ty + (i + 1) * 15f * sin(threat.rotation * PI.toFloat() / 180f)
                drawCircle(Color.Gray.copy(alpha = 0.1f * alpha / (i + 1)), radius = 40f, center = Offset(blurX, blurY))
            }

            val ddx = player.x - tx
            val ddy = player.y - cameraY - ty
            if (ddx * ddx + ddy * ddy < 200f * 200f) {
                drawCircle(Color.Red.copy(alpha = 0.2f * alpha), radius = 60f + sin(gameTime / 100f) * 10f, center = Offset(tx, ty), style = Stroke(width = 3f))
            }
        }
    }
}
