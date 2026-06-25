package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.jump_droid.Constants.PLATFORM_HEIGHT
import com.example.jump_droid.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

object FluxRenderer {
    fun draw(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val pulse = sin(gameTime / 150f) * 0.2f + 0.8f
        val cx = px + width / 2
        val cy = py + PLATFORM_HEIGHT / 2

        // Outer ambient glow
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    SciFiPurple.copy(alpha = 0.15f * pulse),
                    SciFiCyan.copy(alpha = 0.08f * pulse),
                    SciFiPurple.copy(alpha = 0.15f * pulse)
                )
            ),
            topLeft = Offset(px - 30f, py - 10f),
            size = Size(width + 60f, PLATFORM_HEIGHT + 20f)
        )

        // Core body with gradient
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                0.0f to SciFiPurple.copy(alpha = 0.5f),
                0.3f to SciFiPurple.copy(alpha = 0.7f * pulse),
                0.5f to Color(0xFF1A0033).copy(alpha = 0.8f * pulse),
                0.7f to SciFiPurple.copy(alpha = 0.7f * pulse),
                1.0f to SciFiPurple.copy(alpha = 0.5f)
            ),
            topLeft = Offset(px, py),
            size = Size(width, PLATFORM_HEIGHT)
        )

        // Portal edge glow
        drawScope.drawRect(
            color = SciFiCyan.copy(alpha = 0.7f * pulse),
            topLeft = Offset(px, py),
            size = Size(4f, PLATFORM_HEIGHT)
        )
        drawScope.drawRect(
            color = SciFiCyan.copy(alpha = 0.7f * pulse),
            topLeft = Offset(px + width - 4f, py),
            size = Size(4f, PLATFORM_HEIGHT)
        )

        // Swirling vortex core
        val spiralSteps = 12
        val spiralPath = Path()
        for (i in 0..spiralSteps) {
            val t = i.toFloat() / spiralSteps
            val angle = t * 6.28f * 2f - (gameTime / 300f)
            val radius = t * (width * 0.35f).coerceAtMost(50f)
            val sx = cx + cos(angle) * radius
            val sy = cy + sin(angle * 0.7f) * radius * 0.4f
            if (i == 0) spiralPath.moveTo(sx, sy) else spiralPath.lineTo(sx, sy)
        }
        drawScope.drawPath(
            path = spiralPath,
            color = SciFiWhite.copy(alpha = 0.25f * pulse),
            style = Stroke(width = 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Spatial distortion shimmer lines
        repeat(5) { i ->
            val shimmerX = px + ((gameTime / 8f + i * (width / 5f)) % width)
            val shimmerAlpha = (sin(gameTime / 200f + i * 1.3f) * 0.3f + 0.4f) * pulse
            drawScope.drawLine(
                color = SciFiWhite.copy(alpha = 0.15f * shimmerAlpha),
                start = Offset(shimmerX, py + 2f),
                end = Offset(shimmerX, py + PLATFORM_HEIGHT - 2f),
                strokeWidth = 1.5f
            )
        }

        // Internal flux lines
        repeat(3) { i ->
            val offset = (gameTime / 10f + i * (width / 3f)) % width
            val lineAlpha = (sin(gameTime / 100f + i * 2f) * 0.2f + 0.3f) * pulse
            drawScope.drawLine(
                color = SciFiWhite.copy(alpha = lineAlpha),
                start = Offset(px + offset, py + 2f),
                end = Offset(px + offset, py + PLATFORM_HEIGHT - 2f),
                strokeWidth = 1f
            )
        }

        // Vortex particles
        repeat(4) { i ->
            val pa = (gameTime / 200f + i * 1.57f) % 6.28f
            val pd = 15f + sin(gameTime / 150f + i) * 8f
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.3f * pulse * (sin(gameTime / 100f + i) * 0.5f + 0.5f)),
                radius = 2f,
                center = Offset(cx + cos(pa) * pd, cy + sin(pa) * pd * 0.6f)
            )
        }

        // Teleport-charge glow intensifier
        val chargePulse = (sin(gameTime / 80f) * 0.5f + 0.5f)
        drawScope.drawRect(
            color = SciFiCyan.copy(alpha = 0.1f * chargePulse * pulse),
            topLeft = Offset(px + 6f, py + 6f),
            size = Size(width - 12f, PLATFORM_HEIGHT - 12f),
            style = Stroke(width = 2f)
        )
    }
}
