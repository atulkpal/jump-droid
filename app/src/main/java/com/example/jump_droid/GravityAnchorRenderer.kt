package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.jump_droid.ui.theme.SciFiGold
import kotlin.math.sin

class GravityAnchorRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val intensity = threat.alertLevel / 3f

        // Gravity distortion aura
        drawScope.drawCircle(
            color = SciFiGold.copy(alpha = 0.04f * intensity * alpha),
            radius = 200f,
            center = Offset(cx, cy)
        )

        // Anchor Base with metallic gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4A4A4A).copy(alpha = alpha),
                    Color.DarkGray.copy(alpha = alpha),
                    Color(0xFF1A1A1A).copy(alpha = alpha)
                ),
                startY = cy - 30f,
                endY = cy + 30f
            ),
            topLeft = Offset(cx - 30f, cy - 30f),
            size = androidx.compose.ui.geometry.Size(60f, 60f)
        )

        // Base border glow
        drawScope.drawRect(
            color = SciFiGold.copy(alpha = 0.3f * intensity * alpha),
            topLeft = Offset(cx - 32f, cy - 32f),
            size = androidx.compose.ui.geometry.Size(64f, 64f),
            style = Stroke(width = 2f)
        )

        // Core glow
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SciFiGold.copy(alpha = 0.5f * intensity * alpha),
                    SciFiGold.copy(alpha = 0f)
                ),
                center = Offset(cx, cy),
                radius = 30f
            ),
            radius = 30f,
            center = Offset(cx, cy)
        )

        // Expanding gravity rings (intensity-reactive)
        val pulse = (gameTime % 1200) / 1200f
        val ringCount = when {
            intensity > 0.8f -> 4
            intensity > 0.5f -> 3
            else -> 2
        }
        repeat(ringCount) { i ->
            val ringProgress = (pulse + i * (1f / ringCount)) % 1f
            val maxRadius = 60f + ringProgress * 250f * intensity
            val ringAlpha = 0.5f * (1f - ringProgress) * intensity * alpha

            drawScope.drawCircle(
                color = SciFiGold.copy(alpha = ringAlpha),
                radius = maxRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2f + (1f - ringProgress) * 3f)
            )

            // Secondary ring shimmer
            drawScope.drawCircle(
                color = Color.White.copy(alpha = ringAlpha * 0.3f),
                radius = maxRadius * 0.9f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
        }

        // Tidal force particles — attracted toward anchor
        repeat(8) { i ->
            val angle = (i * 45f + gameTime / 200f) * (kotlin.math.PI.toFloat() / 180f)
            val dist = 40f + (1f - pulse) * 180f * intensity + sin(gameTime / 300f + i) * 20f
            val px = cx + kotlin.math.cos(angle) * dist
            val py = cy + kotlin.math.sin(angle) * dist
            drawScope.drawCircle(
                color = SciFiGold.copy(alpha = (0.15f * intensity * (1f - dist / 250f)) * alpha),
                radius = 2f + intensity * 2f,
                center = Offset(px, py)
            )
        }

        // Warning glow at max intensity
        if (intensity > 0.9f) {
            val warnPulse = sin(gameTime / 150f) * 0.3f + 0.7f
            drawScope.drawCircle(
                color = SciFiGold.copy(alpha = 0.2f * warnPulse * alpha),
                radius = 80f + warnPulse * 20f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )
        }
    }
}
