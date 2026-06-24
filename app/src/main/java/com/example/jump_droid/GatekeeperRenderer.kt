package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GatekeeperRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x; val ty = threat.y - cameraY
            val arrivalProgress = (threat.arrivalTimer / threat.arrivalDuration).coerceIn(0f, 1f)
            val phase = threat.phase

            drawRect(Color.Black.copy(alpha = 0.4f * arrivalProgress), topLeft = Offset(0f, 0f), size = size)

            repeat(2) { g ->
                val ghostAngle = threat.rotation - 30f - g * 20f
                rotate(ghostAngle, pivot = Offset(tx, ty)) {
                    drawCircle(Color.White.copy(alpha = 0.05f * (1f - g * 0.5f)), radius = 250f, center = Offset(tx, ty), style = Stroke(width = 15f))
                }
            }

            rotate(threat.rotation, pivot = Offset(tx, ty)) {
                drawCircle(Color.White.copy(alpha = 0.8f * arrivalProgress), radius = 250f, center = Offset(tx, ty), style = Stroke(width = 20f))

                repeat(4) { i ->
                    val isWpDestroyed = i >= threat.activeWeakPoints
                    rotate(i * 90f, pivot = Offset(tx, ty)) {
                        val safeColor = if (isWpDestroyed) Color.Red.copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.4f)
                        val dangerColor = Color.Red.copy(alpha = 0.5f)
                        drawArc(safeColor, startAngle = 45f, sweepAngle = 10f, useCenter = false, topLeft = Offset(tx - 250f, ty - 250f), size = Size(500f, 500f), style = Stroke(width = 80f))
                        drawArc(dangerColor, startAngle = 5f, sweepAngle = 80f, useCenter = false, topLeft = Offset(tx - 250f, ty - 250f), size = Size(500f, 500f), style = Stroke(width = 80f))
                        drawArc(Color.Red.copy(alpha = 0.7f), startAngle = 5f, sweepAngle = 10f, useCenter = false, topLeft = Offset(tx - 250f, ty - 250f), size = Size(500f, 500f), style = Stroke(width = 90f))

                        drawRect(Color(0xFFFF1744).copy(alpha = 0.15f), Offset(tx + 245f, ty - 300f), Size(10f, 600f))

                        if (!isWpDestroyed) {
                            val shieldAngle = (gameTime / 30f) % 360f
                            drawCircle(Color.Magenta, radius = 25f, center = Offset(tx + 250f, ty))
                            drawCircle(Color.White, radius = 10f, center = Offset(tx + 250f, ty))
                            drawArc(Color.Cyan.copy(alpha = 0.4f), startAngle = shieldAngle, sweepAngle = 120f, useCenter = false, topLeft = Offset(tx + 225f, ty - 25f), size = Size(50f, 50f), style = Stroke(width = 4f))
                        }
                    }
                }
            }

            val pdx = player.x - tx
            val pdy = player.y - cameraY - ty
            val pDist = sqrt(pdx * pdx + pdy * pdy)
            if (pDist > 150f && pDist < 350f && threat.activeWeakPoints > 0) {
                repeat(4) { i ->
                    val angle = atan2(pdy, pdx)
                    val forceX = tx + cos(angle) * 200f + (i - 1.5f) * 20f
                    val forceY = ty + sin(angle) * 200f
                    drawLine(Color(0xFFFFAB00).copy(alpha = 0.15f), Offset(forceX, forceY), Offset(player.x, player.y - cameraY), strokeWidth = 2f)
                }
            }

            val eyePulse = (sin(gameTime / 200f) * 0.2f + 0.8f)
            val eyeColor = if (phase == 3) Color.Yellow else Color.Red
            drawCircle(eyeColor.copy(alpha = 0.9f * eyePulse), radius = 40f * arrivalProgress, center = Offset(tx, ty))
            val lookAngle = atan2(pdy, pdx)
            val irisX = tx + cos(lookAngle) * 15f
            val irisY = ty + sin(lookAngle) * 15f
            drawCircle(Color.White, radius = 15f * arrivalProgress, center = Offset(tx, ty))
            drawCircle(Color.Black, radius = 8f * arrivalProgress, center = Offset(irisX, irisY))
        }
    }
}
