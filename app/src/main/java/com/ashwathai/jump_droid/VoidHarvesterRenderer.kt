package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VoidHarvesterRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player
    ) {
        val centerX = threat.x
        val centerY = threat.y - cameraY
        val pulse = (sin(gameTime / 400f) * 0.2f + 0.8f)

        // Ambient field
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to SciFiPurple.copy(alpha = 0.1f * alpha * pulse),
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY),
                radius = 45f
            ),
            radius = 45f,
            center = Offset(centerX, centerY)
        )

        // Core glow
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to SciFiWhite.copy(alpha = 0.3f * alpha * pulse),
                0.6f to SciFiPurple.copy(alpha = 0.2f * alpha * pulse),
                1.0f to Color(0xFF1A0033).copy(alpha = 0.4f * alpha),
                center = Offset(centerX, centerY),
                radius = 15f
            ),
            radius = 15f,
            center = Offset(centerX, centerY)
        )

        // Body
        drawScope.drawCircle(
            color = Color.DarkGray.copy(alpha = alpha),
            radius = 15f,
            center = Offset(centerX, centerY)
        )
        drawScope.drawCircle(
            color = SciFiPurple.copy(alpha = 0.4f * alpha * pulse),
            radius = 18f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        // Eye (single tracking lens)
        val lensPulse = (sin(gameTime / 200f) * 0.3f + 0.7f)
        drawScope.drawCircle(
            color = SciFiCyan.copy(alpha = 0.5f * alpha * lensPulse),
            radius = 4f,
            center = Offset(centerX, centerY - 2f)
        )
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.7f * alpha * lensPulse),
            radius = 2f,
            center = Offset(centerX, centerY - 2f)
        )

        // Segmented tentacles
        repeat(5) { i ->
            val baseAngle = (gameTime / 400f) + (i * 1.25f)
            val segments = 4
            var segX = centerX
            var segY = centerY
            val tentaclePath = Path()

            tentaclePath.moveTo(segX, segY)
            for (s in 0 until segments) {
                val t = (s + 1).toFloat() / segments
                val a = baseAngle + sin(gameTime / 200f + i + s * 0.5f) * 0.5f
                val len = 10f * (1f - t * 0.3f)
                val jitter = sin(gameTime / 150f + i * 2f + s) * 3f
                segX += cos(a) * len + jitter * 0.5f
                segY += sin(a) * len + jitter
                tentaclePath.lineTo(segX, segY)

                // Joint dot
                drawScope.drawCircle(
                    color = SciFiPurple.copy(alpha = 0.3f * alpha * (1f - t * 0.5f)),
                    radius = 2f * (1f - t * 0.3f),
                    center = Offset(segX, segY)
                )
            }
            tentaclePath.lineTo(segX, segY)

            drawScope.drawPath(
                path = tentaclePath,
                color = SciFiPurple.copy(alpha = 0.5f * alpha),
                style = Stroke(width = 3f)
            )
            // Inner tentacle line
            drawScope.drawPath(
                path = tentaclePath,
                color = SciFiCyan.copy(alpha = 0.15f * alpha),
                style = Stroke(width = 1.5f)
            )
        }

        // Detection pulse — ripples outward when player is near
        val dx = player.x - centerX
        val dy = (player.y - cameraY) - centerY
        val distToPlayer = sqrt(dx * dx + dy * dy)
        if (distToPlayer < 250f) {
            val detectionAlpha = (1f - distToPlayer / 250f) * 0.3f * alpha
            val pulsePhase = (gameTime / 800f) % 1f
            drawScope.drawCircle(
                color = SciFiPurple.copy(alpha = detectionAlpha * (1f - pulsePhase)),
                radius = 20f + pulsePhase * 60f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f - pulsePhase * 2f)
            )
        }

        // Harvest burst (when near player with low resources)
        if (player.fuel < 30f && distToPlayer < 100f) {
            repeat(4) { i ->
                val ba = (gameTime / 80f + i * 1.57f) % 6.28f
                val bd = 15f + sin(gameTime / 60f + i) * 5f
                drawScope.drawCircle(
                    color = SciFiPurple.copy(alpha = 0.3f * alpha * (sin(gameTime / 40f + i) * 0.5f + 0.5f)),
                    radius = 3f,
                    center = Offset(centerX + cos(ba) * bd, centerY + sin(ba) * bd)
                )
            }
        }
    }
}
