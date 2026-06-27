package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.cos
import kotlin.math.sin

class CryoMistRenderer : ThreatRenderer {
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
        val radius = 120f
        val pulse = sin(gameTime / 1000f) * 0.1f + 0.9f
        val playerInside = (player.y - cameraY) in (centerY - radius)..(centerY + radius) &&
            player.x in (centerX - radius)..(centerX + radius)

        // Core mist field
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to SciFiCyan.copy(alpha = 0.3f * alpha * pulse),
                0.4f to SciFiCyan.copy(alpha = 0.15f * alpha),
                0.7f to Color(0xFFB0E0FF).copy(alpha = 0.08f * alpha),
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY),
                radius = radius * pulse
            ),
            radius = radius * pulse,
            center = Offset(centerX, centerY)
        )

        // Inner frost core
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color.White.copy(alpha = 0.2f * alpha * pulse),
                0.5f to SciFiCyan.copy(alpha = 0.1f * alpha),
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY),
                radius = radius * 0.4f
            ),
            radius = radius * 0.4f,
            center = Offset(centerX, centerY)
        )

        // Hoarfrost rim around edge
        val rimAngle = (gameTime / 2000f) % 6.28f
        repeat(8) { i ->
            val a = rimAngle + i * 0.78f
            val rimDist = radius * (0.85f + sin(gameTime / 800f + i) * 0.1f)
            val rx = centerX + cos(a) * rimDist
            val ry = centerY + sin(a) * rimDist
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.2f * alpha * (sin(gameTime / 400f + i) * 0.5f + 0.5f)),
                radius = 4f + sin(gameTime / 600f + i * 2f) * 2f,
                center = Offset(rx, ry)
            )
        }

        // Drifting ice crystals
        repeat(8) { i ->
            val angle = (gameTime / (500f + i * 50f)) + (i * 0.78f)
            val driftDist = radius * 0.7f + sin(gameTime / 1000f + i) * 20f
            val dx = centerX + cos(angle) * driftDist
            val dy = centerY + sin(angle * 1.3f) * driftDist * 0.5f + (gameTime / 200f * (0.2f + i * 0.05f)) % (radius * 0.5f)

            val crystalSize = 2f + sin(gameTime / 300f + i) * 1.5f
            val crystalPath = Path().apply {
                moveTo(dx, dy - crystalSize * 2f)
                lineTo(dx + crystalSize, dy - crystalSize * 0.5f)
                lineTo(dx + crystalSize * 2f, dy)
                lineTo(dx + crystalSize, dy + crystalSize * 0.5f)
                lineTo(dx, dy + crystalSize * 2f)
                lineTo(dx - crystalSize, dy + crystalSize * 0.5f)
                lineTo(dx - crystalSize * 2f, dy)
                lineTo(dx - crystalSize, dy - crystalSize * 0.5f)
                close()
            }
            drawScope.drawPath(
                path = crystalPath,
                color = SciFiWhite.copy(alpha = 0.35f * alpha * (sin(gameTime / 200f + i * 3f) * 0.3f + 0.7f)),
                style = Stroke(width = 1.5f)
            )
        }

        // Falling frost particles
        repeat(6) { i ->
            val px = centerX + sin(gameTime / 900f + i * 2.1f) * radius * 0.6f
            val py = centerY - radius * 0.6f + ((gameTime / 100f * (0.8f + i * 0.1f) + i * 30f) % (radius * 1.2f))
            val pAlpha = (0.3f - ((py - centerY + radius * 0.6f) / (radius * 1.2f)) * 0.2f) * alpha
            drawScope.drawCircle(
                color = Color.White.copy(alpha = pAlpha),
                radius = 1.5f + sin(gameTime / 200f + i) * 0.5f,
                center = Offset(px, py)
            )
        }

        // Player-triggered shatter sparks
        if (playerInside) {
            repeat(4) { i ->
                val sa = (gameTime / 100f + i * 1.57f) % 6.28f
                val sd = radius * 0.3f + sin(gameTime / 50f + i * 2f) * 10f
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.3f * alpha * (sin(gameTime / 30f + i) * 0.5f + 0.5f)),
                    radius = 2f + sin(gameTime / 40f + i) * 1.5f,
                    center = Offset(centerX + cos(sa) * sd, centerY + sin(sa) * sd)
                )
            }
        }

        // Vapor wisps
        repeat(3) { i ->
            val wAngle = (gameTime / 2000f + i * 2.09f) % 6.28f
            val wDist = radius * 0.4f + sin(gameTime / 1500f + i) * 30f
            val wx = centerX + cos(wAngle) * wDist
            val wy = centerY + sin(wAngle * 0.7f) * wDist * 0.5f

            val wispPath = Path().apply {
                moveTo(wx - 30f, wy)
                cubicTo(wx - 15f, wy - 15f, wx + 15f, wy + 10f, wx + 30f, wy - 5f)
                cubicTo(wx + 45f, wy - 20f, wx + 60f, wy + 5f, wx + 80f, wy)
            }
            drawScope.drawPath(
                path = wispPath,
                color = SciFiCyan.copy(alpha = 0.08f * alpha * (sin(gameTime / 600f + i) * 0.3f + 0.7f)),
                style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }

        // Outer frost ring — pulse when player is near
        if (playerInside) {
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.2f * alpha * (sin(gameTime / 200f) * 0.5f + 0.5f)),
                radius = radius * 1.15f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f)
            )
        }
    }
}
