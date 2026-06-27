package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiOrange
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.sin

class GravityRamRenderer : ThreatRenderer {
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
        val isCharging = threat.state == ThreatState.ACTIVE
        val chargeIntensity = if (isCharging) (sin(gameTime / 100f) * 0.3f + 0.7f) else 0.0f

        // Engine heat haze
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to SciFiRed.copy(alpha = 0.15f * alpha * (0.3f + chargeIntensity * 0.7f)),
                0.5f to SciFiOrange.copy(alpha = 0.08f * alpha * chargeIntensity),
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY + 15f),
                radius = 30f + chargeIntensity * 30f
            ),
            radius = 30f + chargeIntensity * 30f,
            center = Offset(centerX, centerY + 15f)
        )

        // Hull with stress gradient
        val hullColor = if (isCharging)
            Color(0xFF2A0000) else Color(0xFF1A1A1A)
        val stressColor = if (isCharging)
            SciFiRed else Color.DarkGray

        val hullPath = Path().apply {
            moveTo(centerX, centerY - 20f)
            lineTo(centerX + 25f, centerY + 20f)
            lineTo(centerX - 25f, centerY + 20f)
            close()
        }

        drawScope.drawPath(hullPath, hullColor.copy(alpha = alpha))

        // Hull stress lines (cracks that brighten during charge)
        if (isCharging) {
            val stressAlpha = chargeIntensity * alpha
            repeat(3) { i ->
                val lx = centerX + (i - 1) * 10f
                val ly1 = centerY - 10f + i * 5f
                val ly2 = centerY + 5f + i * 8f
                drawScope.drawLine(
                    color = SciFiRed.copy(alpha = stressAlpha * 0.4f),
                    start = Offset(lx, ly1),
                    end = Offset(lx + (i - 1) * 5f, ly2),
                    strokeWidth = 1.5f
                )
            }
        }

        // Hull outline
        drawScope.drawPath(
            hullPath,
            stressColor.copy(alpha = 0.6f * alpha),
            style = Stroke(width = 2f)
        )
        if (isCharging) {
            drawScope.drawPath(
                hullPath,
                SciFiRed.copy(alpha = 0.3f * chargeIntensity * alpha),
                style = Stroke(width = 1f)
            )
        }

        // Engine flare (grows during wind-up)
        val engineRadius = 6f + chargeIntensity * 8f
        val engineAlpha = (sin(gameTime / 150f) * 0.3f + 0.7f) * alpha * (0.5f + chargeIntensity * 0.5f)
        drawScope.drawCircle(
            color = SciFiRed.copy(alpha = engineAlpha),
            radius = engineRadius,
            center = Offset(centerX, centerY + 15f)
        )
        drawScope.drawCircle(
            color = SciFiOrange.copy(alpha = engineAlpha * 0.5f),
            radius = engineRadius * 0.6f,
            center = Offset(centerX, centerY + 15f)
        )
        drawScope.drawCircle(
            color = SciFiWhite.copy(alpha = engineAlpha * 0.2f),
            radius = engineRadius * 0.3f,
            center = Offset(centerX, centerY + 15f)
        )

        // Shockwave ring (expanding during charge)
        if (isCharging) {
            val shockPulse = (gameTime % 800) / 800f
            drawScope.drawCircle(
                color = SciFiRed.copy(alpha = 0.15f * alpha * (1f - shockPulse)),
                radius = 25f + shockPulse * 80f,
                center = Offset(centerX, centerY + 15f),
                style = Stroke(width = 3f - shockPulse * 2f)
            )
        }

        // Ground-scorch trail
        if (isCharging) {
            val trailAlpha = 0.08f * alpha * chargeIntensity
            drawScope.drawLine(
                color = SciFiRed.copy(alpha = trailAlpha),
                start = Offset(centerX, centerY + 30f),
                end = Offset(centerX, centerY + 120f),
                strokeWidth = 6f
            )
            drawScope.drawLine(
                color = SciFiOrange.copy(alpha = trailAlpha * 0.5f),
                start = Offset(centerX - 4f, centerY + 30f),
                end = Offset(centerX - 4f, centerY + 100f),
                strokeWidth = 3f
            )
            drawScope.drawLine(
                color = SciFiOrange.copy(alpha = trailAlpha * 0.5f),
                start = Offset(centerX + 4f, centerY + 30f),
                end = Offset(centerX + 4f, centerY + 100f),
                strokeWidth = 3f
            )
        }

        // Telegraphed dash line
        if (isCharging) {
            val lineAlpha = (sin(gameTime / 50f) * 0.5f + 0.5f) * 0.6f * alpha
            drawScope.drawLine(
                color = SciFiRed.copy(alpha = lineAlpha),
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY + 600f),
                strokeWidth = 4f
            )
            // Impact prediction crosshair at destination
            val destY = centerY + 600f
            val crossSize = 10f + sin(gameTime / 30f) * 3f
            drawScope.drawLine(
                color = SciFiRed.copy(alpha = lineAlpha * 0.5f),
                start = Offset(centerX - crossSize, destY),
                end = Offset(centerX + crossSize, destY),
                strokeWidth = 2f
            )
            drawScope.drawLine(
                color = SciFiRed.copy(alpha = lineAlpha * 0.5f),
                start = Offset(centerX, destY - crossSize),
                end = Offset(centerX, destY + crossSize),
                strokeWidth = 2f
            )
        }

        // Window port glow
        drawScope.drawCircle(
            color = SciFiOrange.copy(alpha = 0.3f * alpha * (sin(gameTime / 200f) * 0.3f + 0.7f)),
            radius = 3f,
            center = Offset(centerX, centerY - 5f)
        )
    }
}
