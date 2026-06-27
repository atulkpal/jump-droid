package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class LightningRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val scan = threat.scanPulse

            // Cumulonimbus anvil cloud shape
            val cloudPath = Path().apply {
                moveTo(tx - 160f, ty - 40f)
                cubicTo(tx - 180f, ty - 100f, tx - 120f, ty - 140f, tx - 40f, ty - 120f)
                cubicTo(tx - 20f, ty - 150f, tx + 30f, ty - 160f, tx + 80f, ty - 130f)
                cubicTo(tx + 150f, ty - 140f, tx + 190f, ty - 90f, tx + 170f, ty - 40f)
                cubicTo(tx + 200f, ty - 20f, tx + 190f, ty + 20f, tx + 150f, ty + 30f)
                lineTo(tx - 150f, ty + 30f)
                cubicTo(tx - 190f, ty + 20f, tx - 200f, ty - 20f, tx - 160f, ty - 40f)
                close()
            }
            drawPath(cloudPath, Color.DarkGray.copy(alpha = 0.85f * alpha))
            drawPath(cloudPath, Color(0xFF455A64).copy(alpha = 0.3f * alpha), style = Stroke(width = 2f))

            // Telegraph: cloud swells and brightens from yellow to white
            val telegraphBrightness = if (scan > 0 && scan < 1.0f && threat.phase == 2) scan else 0f
            val cloudGlowColor = if (telegraphBrightness > 0.5f) Color.White else Color.Yellow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cloudGlowColor.copy(alpha = 0.3f * alpha + telegraphBrightness * 0.4f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = 180f + telegraphBrightness * 60f
                ),
                radius = 180f + telegraphBrightness * 60f,
                center = Offset(tx, ty)
            )

            // Electric arc particles crackling around cloud
            repeat(8) { i ->
                val seed = threat.instanceId.hashCode() + i + (gameTime / 50).toInt()
                val rng = Random(seed)
                val arcAngle = rng.nextFloat() * 2f * PI.toFloat()
                val arcDist = 120f + rng.nextFloat() * 60f
                val ax = tx + cos(arcAngle) * arcDist
                val ay = ty + sin(arcAngle) * arcDist
                drawCircle(Color.Cyan.copy(alpha = 0.5f * alpha), radius = 2f, center = Offset(ax, ay))
            }

            // Strike zone telegraph: dashed circle on ground that shrinks
            if (scan > 0 && scan < 1.0f && threat.phase == 2) {
                val strikeRadius = 80f * (1f - scan) + 20f
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f * alpha),
                    radius = strikeRadius + 30f,
                    center = Offset(tx, ty + 250f),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                )
                drawCircle(Color.Yellow.copy(alpha = 0.3f * alpha * (1f - scan)), radius = strikeRadius, center = Offset(tx, ty + 250f))
            }

            // Strike: branched fork lightning
            if (scan == 1.0f && threat.phase == 2) {
                val strikeBase = ty + 120f
                repeat(3) { branch ->
                    val boltPath = Path().apply {
                        moveTo(tx + (branch - 1) * 30f, ty + 30f)
                        var curY = strikeBase
                        var curX = tx + (branch - 1) * 30f
                        val segments = 4 + branch
                        repeat(segments) {
                            val nextY = curY + 80f
                            val nextX = curX + (Random.nextFloat() - 0.5f) * 120f
                            lineTo(nextX, nextY)
                            if (it == segments / 2) {
                                moveTo(nextX, nextY)
                                lineTo(nextX + (Random.nextFloat() - 0.5f) * 80f, nextY + 50f)
                                moveTo(nextX, nextY)
                            }
                            curX = nextX
                            curY = nextY
                        }
                    }
                    drawPath(boltPath, Color.Yellow.copy(alpha = alpha * 0.8f), style = Stroke(width = 8f - branch, cap = StrokeCap.Round))
                    drawPath(boltPath, Color.White.copy(alpha = alpha), style = Stroke(width = 3f, cap = StrokeCap.Round))
                }

                // Impact flash
                drawCircle(Color.White.copy(alpha = 0.7f * alpha), radius = 120f, center = Offset(tx, ty + 280f))
                drawCircle(Color.Yellow.copy(alpha = 0.4f * alpha), radius = 180f, center = Offset(tx, ty + 280f))
            }
        }
    }
}
