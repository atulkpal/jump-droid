package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiOrange
import kotlin.math.sin

class ForgerRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val isJamming = threat.jamCooldown > 0f
        val pulse = sin(gameTime / 200f) * 0.05f + 0.95f

        // Fabricator Body with horizontal gradient
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF2A2A2A).copy(alpha = alpha),
                    Color(0xFF1A1A1A).copy(alpha = alpha),
                    Color(0xFF2A2A2A).copy(alpha = alpha)
                ),
                startX = cx - 40f,
                endX = cx + 40f
            ),
            topLeft = Offset(cx - 40f, cy - 20f),
            size = Size(80f, 40f)
        )

        // Body edge glow
        drawScope.drawRect(
            color = SciFiCyan.copy(alpha = 0.2f * alpha),
            topLeft = Offset(cx - 42f, cy - 22f),
            size = Size(84f, 44f)
        )

        // Scanning light (processing)
        val scanPos = sin(gameTime / 80f) * 35f
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    SciFiCyan.copy(alpha = 0f),
                    SciFiCyan.copy(alpha = 0.8f * alpha),
                    SciFiCyan.copy(alpha = 0f)
                ),
                startX = cx + scanPos - 15f,
                endX = cx + scanPos + 15f
            ),
            topLeft = Offset(cx + scanPos - 15f, cy - 18f),
            size = Size(30f, 36f)
        )

        // Scan light center line
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.6f * alpha),
            topLeft = Offset(cx + scanPos - 2f, cy - 16f),
            size = Size(4f, 32f)
        )

        // Assembly arms (animated)
        repeat(2) { side ->
            val armOffset = sin(gameTime / 300f + side * kotlin.math.PI.toFloat()) * 25f
            val armX = cx + (if (side == 0) -45f else 45f) + armOffset
            drawScope.drawLine(
                color = Color.Gray.copy(alpha = 0.5f * alpha),
                start = Offset(armX, cy - 30f),
                end = Offset(armX, cy + 30f),
                strokeWidth = 3f
            )
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.3f * alpha),
                radius = 3f,
                center = Offset(armX, cy - 30f)
            )
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.3f * alpha),
                radius = 3f,
                center = Offset(armX, cy + 30f)
            )
        }

        // Jam sparks when converting platforms
        if (isJamming) {
            val sparkIntensity = threat.jamCooldown / 3f
            repeat(8) { i ->
                val sa = (i * 45f + gameTime / 40f * i) * (kotlin.math.PI.toFloat() / 180f)
                val sd = 30f + (kotlin.random.Random(gameTime + i).nextFloat() * 80f * sparkIntensity)
                val sx = cx + kotlin.math.cos(sa) * sd
                val sy = cy + kotlin.math.sin(sa) * sd
                val sparkColor = if (i % 2 == 0) SciFiCyan else SciFiOrange
                drawScope.drawRect(
                    color = sparkColor.copy(alpha = (0.6f * sparkIntensity * (1f - sd / 120f)) * alpha),
                    topLeft = Offset(sx - 3f, sy - 3f),
                    size = Size(6f, 6f)
                )
            }

            // Central flash during jam
            val flashPulse = sin(gameTime / 60f) * 0.5f + 0.5f
            drawScope.drawRect(
                color = SciFiOrange.copy(alpha = 0.2f * flashPulse * alpha),
                topLeft = Offset(cx - 30f, cy - 10f),
                size = Size(60f, 20f)
            )
        } else {
            // Idle indicator light
            val idlePulse = sin(gameTime / 400f) * 0.3f + 0.7f
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.3f * idlePulse * alpha),
                radius = 4f,
                center = Offset(cx, cy + 28f)
            )
        }

        // Weak point indicators
        if (threat.activeWeakPoints > 0) {
            val wpPulse = sin(gameTime / 200f) * 0.3f + 0.7f
            repeat(3) { i ->
                if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                val wx = cx + (i - 1) * 60f
                drawScope.drawCircle(Color.Magenta.copy(alpha = 0.8f * wpPulse * alpha), radius = 8f * wpPulse, center = Offset(wx, cy))
                drawScope.drawCircle(Color.White.copy(alpha = 0.5f * alpha), radius = 3f, center = Offset(wx, cy))
                }
            }
        }
    }
}
