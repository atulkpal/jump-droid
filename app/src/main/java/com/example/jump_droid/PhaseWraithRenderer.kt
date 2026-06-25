package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiWhite
import com.example.jump_droid.ui.theme.SciFiPurple
import kotlin.math.cos
import kotlin.math.sin

class PhaseWraithRenderer : ThreatRenderer {
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

        val isVulnerable = player.isOverheated
        val flicker = if (!isVulnerable) (sin(gameTime / 100f) * 0.5f + 0.5f) else 1.0f
        val renderAlpha = alpha * flicker * (if (isVulnerable) 1.0f else 0.4f)

        // Ambient phase distortion
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                0.0f to SciFiCyan.copy(alpha = 0.08f * renderAlpha),
                0.6f to SciFiPurple.copy(alpha = 0.04f * renderAlpha),
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY),
                radius = 60f
            ),
            radius = 60f,
            center = Offset(centerX, centerY)
        )

        // Afterimage ghosts
        if (!isVulnerable) {
            repeat(3) { i ->
                val ghostPhase = (gameTime / 300f + i * 0.5f) % 1f
                val ghostX = centerX + sin(gameTime / 400f + i * 2f) * (ghostPhase * 30f)
                val ghostY = centerY + sin(gameTime / 300f + i * 3f) * (ghostPhase * 15f)
                val ghostAlpha = (1f - ghostPhase) * 0.15f * alpha

                val ghostPath = Path().apply {
                    moveTo(ghostX, ghostY - 25f)
                    lineTo(ghostX + 15f, ghostY)
                    lineTo(ghostX, ghostY + 30f)
                    lineTo(ghostX - 15f, ghostY)
                    close()
                }
                drawScope.drawPath(
                    path = ghostPath,
                    color = SciFiCyan.copy(alpha = ghostAlpha),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // Phase-transition ring
        val ringPulse = (sin(gameTime / 200f) * 0.5f + 0.5f)
        drawScope.drawCircle(
            color = SciFiCyan.copy(alpha = 0.2f * renderAlpha * ringPulse),
            radius = 35f + ringPulse * 10f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )
        drawScope.drawCircle(
            color = SciFiWhite.copy(alpha = 0.1f * renderAlpha * (1f - ringPulse)),
            radius = 35f + (1f - ringPulse) * 15f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1f)
        )

        // Ethereal humanoid silhouette
        val bodyPath = Path().apply {
            moveTo(centerX, centerY - 25f)
            lineTo(centerX + 15f, centerY)
            lineTo(centerX, centerY + 30f)
            lineTo(centerX - 15f, centerY)
            close()
        }

        val bodyColor = if (isVulnerable)
            SciFiWhite else SciFiCyan

        drawScope.drawPath(
            path = bodyPath,
            color = bodyColor.copy(alpha = renderAlpha)
        )
        drawScope.drawPath(
            path = bodyPath,
            color = Color.White.copy(alpha = 0.3f * renderAlpha),
            style = Stroke(width = 2f)
        )

        // Vulnerability glow
        if (isVulnerable) {
            val vAlpha = (sin(gameTime / 50f) * 0.3f + 0.7f) * alpha
            drawScope.drawPath(
                path = bodyPath,
                color = Color.White.copy(alpha = vAlpha * 0.3f),
                style = Stroke(width = 4f)
            )
            // Bright core
            drawScope.drawCircle(
                color = SciFiWhite.copy(alpha = 0.4f * vAlpha),
                radius = 8f,
                center = Offset(centerX, centerY - 5f)
            )
        }

        // Head/crown detail
        drawScope.drawCircle(
            color = SciFiWhite.copy(alpha = 0.4f * renderAlpha),
            radius = 3f,
            center = Offset(centerX, centerY - 25f)
        )

        // Whisper particles
        repeat(4) { i ->
            val px = centerX + sin(gameTime / 500f + i * 1.8f) * 40f
            val py = centerY + cos(gameTime / 400f + i * 2.2f) * 30f + sin(gameTime / 300f + i) * 10f
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.12f * renderAlpha * (sin(gameTime / 200f + i) * 0.5f + 0.5f)),
                radius = 1.5f,
                center = Offset(px, py)
            )
        }
    }
}
