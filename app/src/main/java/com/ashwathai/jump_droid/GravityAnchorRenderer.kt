package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import kotlin.math.PI
import kotlin.math.sin

class GravityAnchorRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player,
        context: android.content.Context?
    ) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val intensity = threat.alertLevel / 3f

        // Gravity distortion aura
        drawScope.drawCircle(
            color = SciFiGold.copy(alpha = 0.04f * intensity * alpha),
            radius = 200f,
            center = Offset(cx, cy)
        )

        // D1: Metallic Shimmer (Specularity pass on anchor base)
        val glintPos = (sin((gameTime / 400f).toDouble()).toFloat() * 30f)
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4A4A4A).copy(alpha = alpha),
                    Color(0xFFE0E0E0).copy(alpha = 0.8f * alpha), // Glint
                    Color(0xFF1A1A1A).copy(alpha = alpha)
                ),
                startY = cy - 30f + glintPos,
                endY = cy + 30f + glintPos
            ),
            topLeft = Offset(cx - 30f, cy - 30f),
            size = androidx.compose.ui.geometry.Size(60f, 60f)
        )

        // D2: Expanding Gravity Waves (Variable thickness & alpha)
        val wavePulse = (gameTime % 1500) / 1500f
        repeat(4) { i ->
            val waveProgress = (wavePulse + i * 0.25f) % 1f
            val waveRadius = 40f + waveProgress * 300f * intensity
            val waveAlpha = 0.6f * (1f - waveProgress) * intensity * alpha
            
            drawScope.drawCircle(
                color = SciFiGold.copy(alpha = waveAlpha),
                radius = waveRadius,
                center = Offset(cx, cy),
                style = Stroke(width = (2f + (1f - waveProgress) * 6f).coerceAtLeast(1f))
            )
            // Energy arcs on the waves
            if (intensity > 0.6f) {
                drawScope.drawArc(
                    color = Color.White.copy(alpha = waveAlpha * 0.5f),
                    startAngle = (gameTime / 10f + i * 90f) % 360f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(cx - waveRadius, cy - waveRadius),
                    size = androidx.compose.ui.geometry.Size(waveRadius * 2, waveRadius * 2),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // D3: Tidal Force Particles (accelerating toward core)
        repeat(12) { i ->
            val r = kotlin.random.Random(threat.instanceId.hashCode() + i)
            val startAngle = i * (360f / 12f) + (gameTime / 300f)
            val pProgress = (wavePulse + r.nextFloat()) % 1f
            val dist = 300f * (1f - pProgress) // Move TOWARD center
            val px = cx + kotlin.math.cos(startAngle * (PI.toFloat() / 180f)) * dist
            val py = cy + kotlin.math.sin(startAngle * (PI.toFloat() / 180f)) * dist
            
            val pAlpha = (0.2f + 0.3f * pProgress) * intensity * alpha
            drawScope.drawCircle(
                color = SciFiGold.copy(alpha = pAlpha),
                radius = 1.5f + (1f - pProgress) * 4f,
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

        // Weak point indicators
        if (threat.activeWeakPoints > 0) {
            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
            val wpPulse = sin(gameTime / 200f) * 0.3f + 0.7f
            repeat(2) { i ->
                if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                val wy = cy + (if (i == 0) -50f else 50f)
                drawScope.drawCircle(Color.Magenta.copy(alpha = 0.8f * wpPulse * wpGlow * alpha), radius = 10f * wpPulse, center = Offset(cx, wy))
                drawScope.drawCircle(Color.White.copy(alpha = 0.5f * wpGlow * alpha), radius = 4f, center = Offset(cx, wy))
                }
            }
        }
    }
}
