package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiOrange
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlin.math.cos
import kotlin.math.sin

class HeatBatRenderer : ThreatRenderer {
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
        val isHeatTriggered = player.heat >= 70f
        val heatFactor = (player.heat / 100f).coerceIn(0f, 1f)

        val wingFlap = sin(gameTime / 100f) * 20f
        val diveIntensity = if (isHeatTriggered) (sin(gameTime / 50f) * 0.3f + 0.7f) else 0.3f

        // Heat distortion haze
        val hazeSize = 60f + heatFactor * 40f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to SciFiRed.copy(alpha = 0.12f * alpha * heatFactor * diveIntensity),
                0.5f to SciFiOrange.copy(alpha = 0.06f * alpha * heatFactor * diveIntensity),
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY),
                radius = hazeSize
            ),
            radius = hazeSize,
            center = Offset(centerX, centerY)
        )

        // Ember trail
        if (isHeatTriggered) {
            repeat(5) { i ->
                val tAngle = (gameTime / 200f + i * 1.25f) % 6.28f
                val tDist = 20f + (gameTime / 50f * (0.5f + i * 0.1f)) % 50f
                val tx = centerX + cos(tAngle * 0.5f) * tDist * 0.5f
                val ty = centerY + wingFlap * 0.5f + tDist * 0.3f
                val tAlpha = (1f - (tDist / 50f)) * 0.5f * alpha
                drawScope.drawCircle(
                    color = SciFiOrange.copy(alpha = tAlpha),
                    radius = 2f - (tDist / 50f) * 1.5f,
                    center = Offset(tx, ty)
                )
            }
        }

        // Body with heat glow
        val bodyPath = Path().apply {
            moveTo(centerX, centerY - 10f)
            lineTo(centerX + 8f, centerY + 10f)
            lineTo(centerX - 8f, centerY + 10f)
            close()
        }

        val bodyColor = if (isHeatTriggered)
            Color(0xFF3D0000) else Color.Black
        drawScope.drawPath(bodyPath, bodyColor.copy(alpha = alpha))

        if (isHeatTriggered) {
            drawScope.drawPath(
                bodyPath,
                SciFiRed.copy(alpha = 0.3f * alpha * (sin(gameTime / 30f) * 0.5f + 0.5f)),
                style = Stroke(width = 1.5f)
            )
        }

        // Wings with thermal shimmer
        val wingPath = Path().apply {
            moveTo(centerX - 5f, centerY)
            lineTo(centerX - 30f - heatFactor * 10f, centerY - wingFlap)
            lineTo(centerX - 20f, centerY + 5f)
            close()
            moveTo(centerX + 5f, centerY)
            lineTo(centerX + 30f + heatFactor * 10f, centerY - wingFlap)
            lineTo(centerX + 20f, centerY + 5f)
            close()
        }

        val wingColor = if (isHeatTriggered)
            Color(0xFF2A0000) else Color(0xFF1A1A1A)
        drawScope.drawPath(wingPath, wingColor.copy(alpha = 0.8f * alpha))

        if (isHeatTriggered) {
            drawScope.drawPath(
                wingPath,
                SciFiRed.copy(alpha = 0.2f * alpha * (sin(gameTime / 40f) * 0.5f + 0.5f)),
                style = Stroke(width = 1f)
            )
        }

        // Wing-beat shadow
        val shadowAlpha = 0.08f * alpha * (sin(gameTime / 100f) * 0.5f + 0.5f)
        val shadowPath = Path().apply {
            moveTo(centerX - 5f, centerY + 20f)
            lineTo(centerX - 35f, centerY + 25f - wingFlap * 0.3f)
            lineTo(centerX + 35f, centerY + 25f - wingFlap * 0.3f)
            close()
        }
        drawScope.drawPath(shadowPath, Color.Black.copy(alpha = shadowAlpha))

        // Eyes
        val eyeGlow = if (isHeatTriggered)
            (sin(gameTime / 30f) * 0.3f + 0.7f) else 0.5f
        val eyeColor = if (isHeatTriggered) SciFiRed else Color.White

        drawScope.drawCircle(
            color = eyeColor.copy(alpha = alpha * eyeGlow),
            radius = if (isHeatTriggered) 3f else 2f,
            center = Offset(centerX - 3f, centerY - 2f)
        )
        drawScope.drawCircle(
            color = eyeColor.copy(alpha = alpha * eyeGlow),
            radius = if (isHeatTriggered) 3f else 2f,
            center = Offset(centerX + 3f, centerY - 2f)
        )

        // Heat-triggered outer glow ring
        if (isHeatTriggered) {
            drawScope.drawCircle(
                color = SciFiRed.copy(alpha = 0.15f * alpha * (sin(gameTime / 50f) * 0.5f + 0.5f)),
                radius = 25f + sin(gameTime / 60f) * 5f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )
        }
    }
}
