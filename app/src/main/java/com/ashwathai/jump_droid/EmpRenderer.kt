package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class EmpRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val scanP = threat.scanPulse
            val ringRadius = scanP * 600f
            val ringAlpha = (1f - (scanP / 3.0f)).coerceIn(0f, 1f) * alpha

            val arcFraction = (1f - scanP / 3f).coerceIn(0f, 1f)
            drawArc(
                color = Color.Cyan.copy(alpha = ringAlpha),
                startAngle = 0f,
                sweepAngle = 360f * arcFraction,
                useCenter = false,
                topLeft = Offset(tx - ringRadius, ty - ringRadius),
                size = Size(ringRadius * 2, ringRadius * 2),
                style = Stroke(width = 15f)
            )
            drawCircle(color = Color.White.copy(alpha = ringAlpha * 0.5f), radius = ringRadius * 0.9f, center = Offset(tx, ty), style = Stroke(width = 5f))

            repeat(4) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 30).toInt()
                val rng = Random(seed)
                val arcAngle = rng.nextFloat() * 2f * PI.toFloat()
                val arcLen = 40f + rng.nextFloat() * 60f
                val ax = tx + cos(arcAngle) * ringRadius
                val ay = ty + sin(arcAngle) * ringRadius
                val bx = tx + cos(arcAngle + 0.2f) * (ringRadius + arcLen)
                val by = ty + sin(arcAngle + 0.2f) * (ringRadius + arcLen)
                val arcPath = Path().apply {
                    moveTo(ax, ay)
                    quadraticTo((ax + bx) / 2 + rng.nextFloat() * 30f, (ay + by) / 2 - 40f, bx, by)
                }
                drawPath(arcPath, Color.White.copy(alpha = ringAlpha * 0.6f), style = Stroke(width = 2f))
            }

            val playerDist = sqrt((player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty))
            if (abs(playerDist - ringRadius) < 50f) {
                val xSize = 15f
                drawLine(Color.Red.copy(alpha = 0.6f), Offset(tx - xSize, ty - xSize), Offset(tx + xSize, ty + xSize), strokeWidth = 4f)
                drawLine(Color.Red.copy(alpha = 0.6f), Offset(tx + xSize, ty - xSize), Offset(tx - xSize, ty + xSize), strokeWidth = 4f)
            }

            repeat(8) { i ->
                val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 40).toInt()
                val rng = Random(seed)
                val sa = rng.nextFloat() * 2f * PI.toFloat()
                val sr = ringRadius - 20f + rng.nextFloat() * 40f
                drawCircle(Color.Cyan.copy(alpha = 0.3f * ringAlpha), radius = 2f, center = Offset(tx + cos(sa) * sr, ty + sin(sa) * sr))
            }
        }
    }
}
