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

class CommanderRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val phase = threat.phase

            val hullColor = when {
                phase == 1 -> Color(0xFF263238)
                phase == 2 -> Color(0xFF1565C0)
                phase == 3 || phase == 4 -> Color(0xFFB71C1C)
                else -> Color(0xFFE65100)
            }
            val engineGlowColor = when {
                phase >= 4 -> Color(0xFFFF6D00)
                phase >= 3 -> Color(0xFFFF1744)
                else -> Color.Cyan
            }

            if (threat.activeWeakPoints > 0) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Cyan.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(tx, ty),
                        radius = 200f
                    ),
                    radius = 200f,
                    center = Offset(tx, ty)
                )
                drawCircle(Color.Cyan.copy(alpha = 0.1f), radius = 200f, center = Offset(tx, ty), style = Stroke(width = 2f))
            }

            if (threat.arrivalTimer < threat.arrivalDuration) {
                drawRect(Color.Black.copy(alpha = 0.2f), topLeft = Offset(0f, 0f), size = size)
            }

            drawRect(hullColor, topLeft = Offset(tx - 150f, ty - 60f), size = Size(300f, 120f))
            drawRect(Color.Gray.copy(alpha = 0.5f), topLeft = Offset(tx - 150f, ty - 60f), size = Size(300f, 120f), style = Stroke(width = 4f))

            drawRect(Color(0xFF37474F), topLeft = Offset(tx - 40f, ty - 100f), size = Size(80f, 40f))
            drawRect(engineGlowColor.copy(alpha = 0.3f), topLeft = Offset(tx - 30f, ty - 90f), size = Size(60f, 10f))

            repeat(2) { i ->
                val offset = if (i == 0) -120f else 120f
                val angle = (sin(gameTime / 500f + i) * 30f)
                rotate(angle, pivot = Offset(tx + offset, ty - 60f)) {
                    drawLine(Color.Gray, Offset(tx + offset, ty - 60f), Offset(tx + offset, ty - 120f), strokeWidth = 3f)
                    drawCircle(Color.Red, radius = 5f, center = Offset(tx + offset, ty - 120f))
                }
            }

            val radarAngle = (gameTime / 10f) % 360f
            rotate(radarAngle, pivot = Offset(tx + 60f, ty - 80f)) {
                drawArc(Color.Gray, 0f, 180f, true, topLeft = Offset(tx + 40f, ty - 100f), size = Size(40f, 40f))
            }

            val lightRate = if (phase >= 3) 200 else 500
            if ((gameTime / lightRate) % 2 == 0L) {
                drawCircle(Color.Yellow, radius = 4f, center = Offset(tx - 130f, ty - 40f))
                drawCircle(Color.Yellow, radius = 4f, center = Offset(tx + 130f, ty - 40f))
                drawCircle(Color.Yellow, radius = 4f, center = Offset(tx, ty - 40f))
            }

            val engineFlicker = Random(gameTime / 50).nextFloat() * 15f
            repeat(3) { i ->
                val ex = tx - 100f + i * 100f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(engineGlowColor.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(ex, ty + 60f),
                        radius = 40f + engineFlicker
                    ),
                    radius = 40f + engineFlicker,
                    center = Offset(ex, ty + 60f)
                )
            }

            if (phase >= 3) {
                repeat(2) { i ->
                    val offset = if (i == 0) -100f else 100f
                    val angle = (sin(gameTime / 1000f + i) * 45f) + 90f
                    rotate(angle, pivot = Offset(tx + offset, ty + 40f)) {
                        drawPath(
                            path = Path().apply {
                                moveTo(tx + offset, ty + 40f)
                                lineTo(tx + offset + 400f, ty + 40f - 60f)
                                lineTo(tx + offset + 400f, ty + 40f + 60f)
                                close()
                            },
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Red.copy(alpha = 0.3f), Color.Transparent),
                                startX = tx + offset,
                                endX = tx + offset + 400f
                            )
                        )
                    }
                }
            }

            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
            repeat(threat.maxWeakPoints) { i ->
                val isDestroyed = (threat.wpDestroyedMask and (1 shl i)) != 0
                val wx = tx - 80f + (i * 80f)
                val wy = ty - 40f
                if (!isDestroyed) {
                    drawRect(Color.Magenta.copy(alpha = wpGlow), topLeft = Offset(wx - 10f, wy - 10f), size = Size(20f, 20f))
                    drawRect(Color.White.copy(alpha = 0.5f * wpGlow), topLeft = Offset(wx - 10f, wy - 10f), size = Size(20f, 20f), style = Stroke(width = 2f))
                    val beaconAngle = (gameTime / 20f + i * 120f) % 360f
                    rotate(beaconAngle, pivot = Offset(wx, wy)) {
                        drawLine(Color.White.copy(alpha = 0.5f * wpGlow), Offset(wx, wy), Offset(wx + 15f, wy), strokeWidth = 2f)
                    }
                }
            }

            if (phase >= 3) {
                val jamPulse = (sin(gameTime / 400f) * 0.5f + 0.5f)
                drawCircle(Color(0xFF00BCD4).copy(alpha = 0.1f * jamPulse), radius = 100f + jamPulse * 200f, center = Offset(tx, ty), style = Stroke(width = 4f))
                drawCircle(Color.Cyan.copy(alpha = 0.05f * jamPulse), radius = 100f + jamPulse * 200f, center = Offset(tx, ty), style = Stroke(width = 2f))
            }

            if (threat.pulseAlpha > 0) {
                val pulseScale = 1f - threat.pulseAlpha
                drawCircle(Color.White.copy(alpha = threat.pulseAlpha * 0.6f), radius = pulseScale * 1200f, center = Offset(tx, ty), style = Stroke(width = 8f))
                repeat(8) { i ->
                    val seed = threat.instanceId.hashCode() + i + (gameTime / 50).toInt()
                    val rng = Random(seed)
                    val da = rng.nextFloat() * 2f * PI.toFloat()
                    val dd = rng.nextFloat() * 600f
                    drawCircle(Color.White.copy(alpha = 0.2f * threat.pulseAlpha), radius = 2f, center = Offset(tx + cos(da) * dd, ty + sin(da) * dd))
                }
            }
        }
    }
}
