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
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player,
        context: android.content.Context?
    ) {
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

            // D1: Engine Overhaul (Long flickering exhaust trails)
            val engineFlicker = Random(gameTime / 40).nextFloat() * 20f
            val exhaustLen = 120f + (if (phase >= 3) 80f else 40f) + engineFlicker
            repeat(3) { i ->
                val ex = tx - 100f + i * 100f
                val ey = ty + 60f
                
                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(engineGlowColor.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(ex, ey),
                        radius = 45f + engineFlicker
                    ),
                    radius = 45f + engineFlicker,
                    center = Offset(ex, ey)
                )
                
                // Trail path
                val trailPath = Path().apply {
                    moveTo(ex - 15f, ey)
                    quadraticTo(ex, ey + exhaustLen, ex + 15f, ey)
                    close()
                }
                drawPath(
                    path = trailPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(engineGlowColor.copy(alpha = 0.7f), Color.Transparent),
                        startY = ey,
                        endY = ey + exhaustLen
                    )
                )
            }

            // D2: Animated Sensor Arrays & Turrets
            repeat(2) { i ->
                val offset = if (i == 0) -120f else 120f
                val arrayAngle = (sin((gameTime / 600f + i).toDouble()).toFloat() * 35f)
                val extendY = if (phase >= 3) 20f else 0f
                
                rotate(arrayAngle, pivot = Offset(tx + offset, ty - 60f)) {
                    drawLine(Color.Gray, Offset(tx + offset, ty - 60f), Offset(tx + offset, ty - 130f - extendY), strokeWidth = 4f)
                    drawCircle(Color.Red.copy(alpha = 0.8f), radius = 6f, center = Offset(tx + offset, ty - 130f - extendY))
                    
                    // Rotating Turret Pods
                    val turretAngle = (gameTime / 15f) % 360f
                    rotate(turretAngle, pivot = Offset(tx + offset, ty - 90f)) {
                        drawRect(Color(0xFF455A64), Offset(tx + offset - 10f, ty - 100f), Size(20f, 20f))
                        drawLine(Color.DarkGray, Offset(tx + offset, ty - 100f), Offset(tx + offset, ty - 115f), strokeWidth = 3f)
                    }
                }
            }

            // D3: Tactical Jam Pulse (Hexagonal distortion wave)
            if (phase >= 3) {
                val jamPulse = (sin((gameTime / 450f).toDouble()).toFloat() * 0.5f + 0.5f)
                val pulseRadius = 150f + jamPulse * 250f
                
                // Drawing 6 segments of a hexagon
                val hexPath = Path().apply {
                    repeat(6) { i ->
                        val ang = i * 60f * (PI.toFloat() / 180f)
                        val px = tx + pulseRadius * cos(ang)
                        val py = ty + pulseRadius * sin(ang)
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(hexPath, Color.Cyan.copy(alpha = 0.15f * (1f - jamPulse)), style = Stroke(width = 6f))
                drawPath(hexPath, Color.White.copy(alpha = 0.1f * (1f - jamPulse)), style = Stroke(width = 2f))
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
