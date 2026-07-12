package com.ashwathai.jump_droid

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
import kotlin.random.Random

class VoidEngineRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x; val ty = threat.y - cameraY
            val rot = threat.rotation
            val phase = threat.phase

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE91E63).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 500f
                ),
                radius = 500f,
                center = Offset(tx, ty)
            )

            val tearPath = Path().apply {
                val segs = 16
                moveTo(tx + 500f, ty)
                repeat(segs) {
                    val ta = ((it + 1) / segs.toFloat()) * 2f * PI.toFloat()
                    val seed = threat.instanceId.hashCode() + it + (gameTime / 150).toInt()
                    val rng = Random(seed)
                    val jitter = 480f + rng.nextFloat() * 40f
                    lineTo(tx + cos(ta) * jitter, ty + sin(ta) * jitter)
                }
                close()
            }
            drawPath(tearPath, Color(0xFFE91E63).copy(alpha = 0.08f), style = Stroke(width = 2f))

            rotate(rot, pivot = Offset(tx, ty)) {
                repeat(2) { g ->
                    val ghostRot = rot - 30f - g * 15f
                    rotate(ghostRot, pivot = Offset(tx, ty)) {
                        repeat(3) { i ->
                            rotate(i * 120f, pivot = Offset(tx, ty)) {
                                drawRect(Color(0xFF880E4F).copy(alpha = 0.1f / (g + 1)), topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f))
                            }
                        }
                    }
                }

                repeat(3) { i ->
                    rotate(i * 120f, pivot = Offset(tx, ty)) {
                        drawRect(Color(0xFF880E4F), topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f))
                        drawRect(Color.White, topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f), style = Stroke(width = 4f))
                        if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
                            drawCircle(Color.Magenta.copy(alpha = wpGlow), radius = 20f * wpGlow, center = Offset(tx, ty - 150f))
                        }
                    }
                }
            }

            val arcRate = if (phase == 3) 2 else 4
            if ((gameTime / 80) % arcRate == 0L) {
                drawLine(Color.White, Offset(tx, ty), Offset(tx + (Random.nextFloat() - 0.5f) * 600f, ty + (Random.nextFloat() - 0.5f) * 600f), strokeWidth = 5f)
            }

            if (phase == 3 && (threat.localTimer.toInt() % 60) > 55) {
                drawRect(Color(0xFFE91E63).copy(alpha = 0.08f), topLeft = Offset(0f, 0f), size = size)
            }

            if (phase >= 2 && (threat.localTimer.toInt() % 4 < 2)) {
                val shiftDir = if ((threat.localTimer.toInt() / 4) % 2 == 0) 1f else -1f
                repeat(3) { i ->
                    val ay = (gameTime / 2f + i * 200f) % size.height
                    val ax = if (shiftDir > 0) 100f else size.width - 100f
                    drawPath(
                        path = Path().apply {
                            moveTo(ax, ay)
                            lineTo(ax + 50f * shiftDir, ay + 30f)
                            lineTo(ax, ay + 60f)
                            close()
                        },
                        color = Color.Magenta.copy(alpha = 0.3f)
                    )
                }
                drawPath(
                    path = Path().apply {
                        moveTo(size.width / 2f + shiftDir * 200f, size.height / 2f)
                        lineTo(size.width / 2f - shiftDir * 100f, size.height / 2f - 60f)
                        lineTo(size.width / 2f - shiftDir * 100f, size.height / 2f + 60f)
                        close()
                    },
                    color = Color.Magenta.copy(alpha = 0.1f)
                )
            }
        }
    }
}
