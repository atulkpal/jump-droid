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

object GravitonRenderer {
    fun draw(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val cx = px + width / 2
        val cy = py + PLATFORM_HEIGHT / 2
        val pulse = (gameTime % 1500) / 1500f

        // Event horizon glow ring
        val horizonPulse = sin(gameTime / 200f) * 0.15f + 0.85f
        drawScope.drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF1744).copy(alpha = 0.06f * horizonPulse),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = width * 0.8f
            ),
            topLeft = Offset(px - width * 0.3f, py - width * 0.3f),
            size = Size(width * 1.6f, PLATFORM_HEIGHT + width * 0.6f)
        )

        // Body: deep void black with reddened edges
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                0.0f to SciFiRed.copy(alpha = 0.15f * horizonPulse),
                0.15f to Color.Black,
                0.5f to Color(0xFF0A0000),
                0.85f to Color.Black,
                1.0f to SciFiRed.copy(alpha = 0.15f * horizonPulse)
            ),
            topLeft = Offset(px, py),
            size = Size(width, PLATFORM_HEIGHT)
        )

        // Red border frame
        drawScope.drawRect(
            color = SciFiRed.copy(alpha = 0.5f * horizonPulse),
            topLeft = Offset(px, py),
            size = Size(width, PLATFORM_HEIGHT),
            style = Stroke(width = 2f)
        )

        // Singularity lens flare — bright center
        val flareSize = 10f + sin(gameTime / 100f) * 4f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color.White.copy(alpha = 0.4f * horizonPulse),
                0.3f to SciFiRed.copy(alpha = 0.2f * horizonPulse),
                1.0f to Color.Transparent,
                center = Offset(cx, cy),
                radius = flareSize * 2f
            ),
            radius = flareSize * 2f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.3f * horizonPulse),
            radius = flareSize * 0.3f,
            center = Offset(cx, cy)
        )

        // Spacetime grid warp — bent horizontal lines
        repeat(3) { i ->
            val lineY = py + PLATFORM_HEIGHT * (i + 1) / 4f
            val gridPath = Path()
            gridPath.moveTo(px, lineY)
            for (step in 0..10) {
                val frac = step / 10f
                val gx = px + frac * width
                val warp = sin(frac * 6.28f * 0.5f - gameTime / 300f) * 4f * horizonPulse
                gridPath.lineTo(gx, lineY + warp)
            }
            drawScope.drawPath(
                path = gridPath,
                color = SciFiRed.copy(alpha = 0.15f * horizonPulse),
                style = Stroke(width = 1f)
            )
        }

        // Gravity well rings
        repeat(3) { i ->
            val ringProgress = (pulse + i * 0.33f) % 1f
            val radius = 20f + ringProgress * 150f
            drawScope.drawCircle(
                color = SciFiRed.copy(alpha = 0.3f * (1f - ringProgress) * horizonPulse),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 4f - ringProgress * 3f)
            )
        }

        // Tidal force stretched particles
        repeat(6) { i ->
            val ta = (gameTime / 400f + i * 1.04f) % 6.28f
            val td = 25f + sin(gameTime / 200f + i) * 10f
            val tx = cx + cos(ta) * td
            val ty = cy + sin(ta) * td * 0.4f
            val stretch = 1f + sin(gameTime / 100f + i * 2f) * 2f
            drawScope.drawRect(
                color = SciFiWhite.copy(alpha = 0.2f * horizonPulse * (sin(gameTime / 150f + i) * 0.5f + 0.5f)),
                topLeft = Offset(tx - stretch, ty - 1f),
                size = Size(stretch * 2f, 2f)
            )
        }

        // Accretion disk shimmer
        repeat(4) { i ->
            val aa = (gameTime / 250f + i * 1.57f) % 6.28f
            val ad = width * 0.3f + sin(gameTime / 300f + i) * 15f
            drawScope.drawCircle(
                color = SciFiRed.copy(alpha = 0.08f * horizonPulse * (sin(gameTime / 100f + i) * 0.5f + 0.5f)),
                radius = 2f + sin(gameTime / 80f + i) * 1.5f,
                center = Offset(cx + cos(aa) * ad, cy + sin(aa) * ad * 0.3f)
            )
        }
    }
}
