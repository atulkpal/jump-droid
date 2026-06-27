package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

class OrbitalSentryRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val rot = threat.rotation
            val isTracking = threat.isTracking

            val octPath = Path().apply {
                moveTo(tx - 25f, ty - 10f)
                lineTo(tx - 10f, ty - 25f)
                lineTo(tx + 10f, ty - 25f)
                lineTo(tx + 25f, ty - 10f)
                lineTo(tx + 25f, ty + 10f)
                lineTo(tx + 10f, ty + 25f)
                lineTo(tx - 10f, ty + 25f)
                lineTo(tx - 25f, ty + 10f)
                close()
            }
            rotate(rot, pivot = Offset(tx, ty)) {
                drawPath(octPath, Color(0xFF37474F))
                drawPath(octPath, Color.Gray.copy(alpha = 0.5f), style = Stroke(width = 2f))
                drawCircle(Color.Cyan.copy(alpha = 0.3f), radius = 15f, center = Offset(tx, ty))

                drawRect(Color(0xFF546E7A), Offset(tx - 4f, ty - 35f), Size(8f, 12f))
            }

            val pulseRadius = 150f * threat.scanPulse
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.2f * (1f - threat.scanPulse)),
                radius = pulseRadius,
                center = Offset(tx, ty),
                style = Stroke(width = 2f)
            )

            if (isTracking) {
                val px = player.x
                val py = player.y - cameraY
                drawLine(Color.Red.copy(alpha = 0.3f), Offset(tx, ty), Offset(px, py), strokeWidth = 1f)

                val bracketSize = 15f
                val gap = 5f
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py - bracketSize - gap), Offset(px - gap, py - bracketSize - gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py - bracketSize - gap), Offset(px - bracketSize - gap, py - gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py - bracketSize - gap), Offset(px + gap, py - bracketSize - gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py - bracketSize - gap), Offset(px + bracketSize + gap, py - gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py + bracketSize + gap), Offset(px - gap, py + bracketSize + gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py + bracketSize + gap), Offset(px - bracketSize - gap, py + gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py + bracketSize + gap), Offset(px + gap, py + bracketSize + gap), strokeWidth = 2f)
                drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py + bracketSize + gap), Offset(px + bracketSize + gap, py + gap), strokeWidth = 2f)

                repeat(3) { i ->
                    val seed = threat.instanceId.hashCode() + i * 5 + (gameTime / 30).toInt()
                    val rng = Random(seed)
                    val t = rng.nextFloat()
                    val sx = px + (tx - px) * t
                    val sy = py + (ty - py) * t
                    drawCircle(Color(0xFFFF6D00).copy(alpha = 0.5f * (1f - t)), radius = 2f, center = Offset(sx, sy))
                }
            }

            if (isTracking) {
                val iconX = tx + 40f
                val iconY = ty - 30f
                rotate(gameTime / 10f, pivot = Offset(iconX, iconY)) {
                    repeat(3) { i ->
                        rotate(i * 60f, pivot = Offset(iconX, iconY)) {
                            drawLine(Color.Cyan.copy(alpha = 0.5f), Offset(iconX, iconY - 8f), Offset(iconX, iconY + 8f), strokeWidth = 2f)
                        }
                    }
                }
            }
        }
    }
}
