package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class VoidWhaleRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val pulse = (sin(gameTime / 800f) * 0.15f + 0.85f)
            val tailSweep = sin(gameTime / 600f) * 20f
            val bodyRadius = 160f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00BCD4).copy(alpha = 0.15f * pulse), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = bodyRadius * 2.5f
                ),
                radius = bodyRadius * 2.5f,
                center = Offset(tx, ty)
            )

            repeat(12) { i ->
                val seed = threat.instanceId.hashCode() + i * 13 + (gameTime / 300).toInt()
                val rng = Random(seed)
                val ax = tx + cos(gameTime / 500f + i * 1.2f) * (bodyRadius * 0.8f + rng.nextFloat() * bodyRadius)
                val ay = ty + sin(gameTime / 400f + i * 1.7f) * (bodyRadius * 0.4f + rng.nextFloat() * bodyRadius * 0.5f)
                val aColor = when (rng.nextInt(3)) { 0 -> Color.Cyan; 1 -> Color(0xFFCE93D8); else -> Color.White }
                drawCircle(aColor.copy(alpha = 0.06f * pulse), radius = 1f + rng.nextFloat() * 2f, center = Offset(ax, ay))
            }

            val bodyPath = Path().apply {
                moveTo(tx - bodyRadius * pulse, ty)
                cubicTo(tx - bodyRadius * 0.6f, ty - bodyRadius * 0.5f, tx + bodyRadius * 0.6f, ty - bodyRadius * 0.5f, tx + bodyRadius * pulse, ty)
                cubicTo(tx + bodyRadius * 0.6f, ty + bodyRadius * 0.25f, tx - bodyRadius * 0.6f, ty + bodyRadius * 0.25f, tx - bodyRadius * pulse, ty)
                close()
            }
            drawPath(bodyPath, Color(0xFF006064).copy(alpha = 0.3f * pulse))
            drawPath(bodyPath, Color.Cyan.copy(alpha = 0.2f * pulse), style = Stroke(width = 3f))

            repeat(5) { i ->
                val rf = i.toFloat() / 4f
                val rx = tx + (rf - 0.3f) * bodyRadius * 1.4f
                val ry = ty - bodyRadius * 0.45f + sin(gameTime / 200f + i * 1.5f) * 5f
                val rh = 10f + rf * 15f
                drawLine(Color(0xFF00BCD4).copy(alpha = 0.2f * pulse), Offset(rx, ry), Offset(rx, ry - rh), strokeWidth = 3f)
                drawLine(Color.Cyan.copy(alpha = 0.1f * pulse), Offset(rx, ry), Offset(rx, ry - rh), strokeWidth = 1f)
            }

            repeat(3) { i ->
                val vx = tx + (i - 1) * bodyRadius * 0.3f
                val vy = ty + bodyRadius * 0.15f
                val vLen = 20f + i * 8f
                val vPulse = (sin(gameTime / 400f + i * 1.8f) * 0.4f + 0.6f)
                drawLine(Color(0xFF00E5FF).copy(alpha = 0.15f * vPulse * pulse), Offset(vx - vLen, vy), Offset(vx + vLen, vy), strokeWidth = 2f)
            }

            val eyeX = tx + bodyRadius * 0.75f
            val eyeY = ty - bodyRadius * 0.15f
            val eyeGlow = (sin(gameTime / 200f) * 0.3f + 0.7f)
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.1f * eyeGlow * pulse), radius = 12f, center = Offset(eyeX, eyeY))
            drawCircle(Color.White.copy(alpha = 0.6f * eyeGlow * pulse), radius = 6f, center = Offset(eyeX, eyeY))
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.8f * eyeGlow * pulse), radius = 3f, center = Offset(eyeX, eyeY))

            val tailPath = Path().apply {
                moveTo(tx - bodyRadius * 0.9f, ty)
                lineTo(tx - bodyRadius * 1.4f, ty - bodyRadius * 0.4f + tailSweep)
                lineTo(tx - bodyRadius * 1.3f, ty + tailSweep * 0.5f)
                lineTo(tx - bodyRadius * 1.4f, ty + bodyRadius * 0.4f + tailSweep)
                close()
            }
            drawPath(tailPath, Color(0xFF00838F).copy(alpha = 0.25f * pulse))
            drawPath(tailPath, Color.Cyan.copy(alpha = 0.2f * pulse), style = Stroke(width = 2f))

            val finPath = Path().apply {
                moveTo(tx + bodyRadius * 0.2f, ty + bodyRadius * 0.2f)
                cubicTo(tx + bodyRadius * 0.5f, ty + bodyRadius * 0.6f, tx + bodyRadius * 0.3f, ty + bodyRadius * 0.7f, tx + bodyRadius * 0.1f, ty + bodyRadius * 0.3f)
                close()
            }
            drawPath(finPath, Color(0xFF00838F).copy(alpha = 0.15f * pulse))
            drawPath(finPath, Color.Cyan.copy(alpha = 0.1f * pulse), style = Stroke(width = 1f))

            repeat(60) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 500).toInt()
                val rng = Random(seed)
                val nx = tx + (rng.nextFloat() - 0.5f) * bodyRadius * 1.6f
                val ny = ty + (rng.nextFloat() - 0.5f) * bodyRadius * 0.8f
                val starColor = when (rng.nextInt(4)) { 0 -> Color.Cyan; 1 -> Color(0xFFCE93D8); 2 -> Color(0xFF80D8FF); else -> Color.White }
                drawCircle(starColor.copy(alpha = 0.25f * rng.nextFloat() * pulse), radius = 1f + rng.nextFloat() * 2.5f, center = Offset(nx, ny))
            }

            repeat(8) { i ->
                val angleFrac = i.toFloat() / 8f
                val bx = tx + cos(angleFrac * PI.toFloat() * 2f) * bodyRadius * 0.7f
                val by = ty + sin(angleFrac * PI.toFloat() * 2f) * bodyRadius * 0.35f
                val starPulse = (sin(gameTime / 300f + i * 1.2f) * 0.5f + 0.5f)
                drawCircle(Color.Cyan.copy(alpha = 0.4f * starPulse * pulse), radius = 2f + starPulse * 3f, center = Offset(bx, by))
            }

            repeat(8) { i ->
                val trailPhase = ((gameTime / 2000f + i * 0.15f) % 1f)
                val dist = trailPhase * bodyRadius * 1.8f
                val offsetY = sin(trailPhase * PI.toFloat() * 2f + i) * 35f
                val tColor = when (i % 3) { 0 -> Color.Cyan; 1 -> Color(0xFFCE93D8); else -> Color.White }
                drawCircle(tColor.copy(alpha = (1f - trailPhase) * 0.35f * pulse), radius = 1f + (1f - trailPhase) * 3f, center = Offset(tx - dist * 0.7f, ty + offsetY))
            }

            val dx = player.x - tx
            val dy = player.y - cameraY - ty
            val near = dx * dx + dy * dy < 500f * 500f
            if (near) {
                val pushDir = if (threat.vx > 0) -1f else 1f
                val arrX = tx + pushDir * bodyRadius * 0.5f
                drawLine(Color.Cyan.copy(alpha = 0.3f), Offset(arrX, ty), Offset(arrX + pushDir * 60f, ty), strokeWidth = 4f)
                val arrHead = Path().apply {
                    moveTo(arrX + pushDir * 60f, ty)
                    lineTo(arrX + pushDir * 45f, ty - 10f)
                    lineTo(arrX + pushDir * 45f, ty + 10f)
                    close()
                }
                drawPath(arrHead, Color.Cyan.copy(alpha = 0.3f))

                repeat(4) { i ->
                    val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 50).toInt()
                    val rng = Random(seed)
                    val sx = tx + (rng.nextFloat() - 0.5f) * bodyRadius
                    val sy = ty + i * 30f - 60f
                    drawLine(Color.Cyan.copy(alpha = 0.2f), Offset(sx, sy), Offset(sx, sy + 80f), strokeWidth = 2f)
                }

                repeat(5) { i ->
                    val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 100).toInt()
                    val rng = Random(seed)
                    val wx = tx + (rng.nextFloat() - 0.5f) * 200f
                    val wy = ty + (rng.nextFloat() - 0.5f) * 100f
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = 2f + rng.nextFloat() * 3f, center = Offset(wx, wy))
                }
            }
        }
    }
}
