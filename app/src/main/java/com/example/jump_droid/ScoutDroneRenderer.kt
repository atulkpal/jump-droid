package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

class ScoutDroneRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val isTracking = threat.isTracking
            val isFleeing = threat.fleeTimer > 0f
            val transmitting = threat.transmissionProgress > 0f && threat.transmissionProgress < 1f

            val stateColor = when {
                isFleeing -> Color(0xFFFF6D00)
                transmitting -> Color(0xFFE91E63)
                isTracking -> Color(0xFFD32F2F)
                threat.firstDetectionShown -> Color(0xFFFDD835)
                else -> Color(0xFF1976D2)
            }
            val stateName = when {
                isFleeing -> "FLEE"
                transmitting -> "TRANSMIT"
                isTracking -> "TRACK"
                threat.firstDetectionShown -> "DETECT"
                else -> "PATROL"
            }

            val flicker = Random(gameTime / 50).nextFloat() * 10f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(stateColor.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(tx, ty + 20f),
                    radius = 30f + flicker
                ),
                radius = 30f + flicker,
                center = Offset(tx, ty + 20f)
            )

            val bodyPath = Path().apply {
                moveTo(tx - 20f, ty - 15f)
                lineTo(tx + 20f, ty - 15f)
                lineTo(tx + 15f, ty + 15f)
                lineTo(tx - 15f, ty + 15f)
                close()
            }
            drawPath(bodyPath, stateColor.copy(alpha = 0.8f))
            drawPath(bodyPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 1f))

            val antLen = if (transmitting) 25f else 15f
            drawLine(Color.LightGray, Offset(tx - 12f, ty - 15f), Offset(tx - 18f, ty - 15f - antLen), strokeWidth = 2f)
            drawLine(Color.LightGray, Offset(tx + 12f, ty - 15f), Offset(tx + 18f, ty - 15f - antLen), strokeWidth = 2f)
            drawCircle(stateColor, radius = 3f, center = Offset(tx - 18f, ty - 15f - antLen))
            drawCircle(stateColor, radius = 3f, center = Offset(tx + 18f, ty - 15f - antLen))

            if (isTracking) {
                val beamAngle = threat.targetAngle
                rotate(beamAngle, pivot = Offset(tx, ty)) {
                    drawPath(
                        path = Path().apply {
                            moveTo(tx + 10f, ty)
                            lineTo(tx + 250f, ty - 50f)
                            lineTo(tx + 250f, ty + 50f)
                            close()
                        },
                        brush = Brush.horizontalGradient(
                            colors = listOf(stateColor.copy(alpha = 0.3f * (1f - threat.scanPulse)), Color.Transparent),
                            startX = tx,
                            endX = tx + 250f
                        )
                    )
                }
            }

            val eyeRate = when {
                isFleeing -> 30f
                transmitting -> 50f
                isTracking -> 80f
                else -> 150f
            }
            val eyePulse = (sin(gameTime / eyeRate) * 0.5f + 0.5f)
            val eyeColor = if (isTracking || transmitting) Color.Red else Color.Cyan
            val eyeRadius = if (isTracking || transmitting) 12f else 8f
            drawCircle(eyeColor.copy(alpha = 0.6f + eyePulse * 0.4f), radius = eyeRadius, center = Offset(tx, ty))

            if (transmitting) {
                repeat(3) { i ->
                    val barX = tx - 30f + i * 30f
                    val barH = 8f + sin(gameTime / 100f + i * 2f) * 6f
                    drawRect(stateColor.copy(alpha = 0.5f), Offset(barX, ty - 30f), Size(8f, barH))
                }
            }

            if (isTracking && threat.transmissionProgress > 0f) {
                val prog = if (threat.transmissionProgress < 1f) threat.transmissionProgress else threat.scanPulse
                val baseRadius = 25f
                repeat(3) { i ->
                    val offset = i * 0.33f
                    val ringPhase = (prog + offset) % 1f
                    val radius = ringPhase * 90f + baseRadius
                    val ringAlpha = (1f - ringPhase) * 0.5f
                    drawCircle(
                        color = Color(0xFFE91E63).copy(alpha = ringAlpha),
                        radius = radius,
                        center = Offset(tx, ty),
                        style = Stroke(width = 2f)
                    )
                }
            }

            if (isFleeing) {
                repeat(4) { i ->
                    val trailAlpha = 0.3f * (1f - i / 4f)
                    drawRect(stateColor.copy(alpha = trailAlpha), Offset(tx - 15f + (i + 1) * 8f, ty - 12f), Size(30f, 24f))
                }
            }
        }
    }
}
