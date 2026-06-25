package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.jump_droid.ui.theme.SciFiGold
import kotlin.math.sin

class GravityShearRenderer : ThreatRenderer {
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
        val width = 150f
        val height = 200f
        
        // Upward shear arrows (top)
        repeat(3) { i ->
            val offset = (gameTime / 10f + i * 40f) % 80f
            val y = centerY - 100f + offset
            if (y in (centerY - 100f)..(centerY)) {
                val a = (1f - (centerY - y) / 100f) * alpha * 0.4f
                drawArrow(drawScope, centerX, y, true, a)
            }
        }
        
        // Downward shear arrows (bottom)
        repeat(3) { i ->
            val offset = (gameTime / 10f + i * 40f) % 80f
            val y = centerY + 100f - offset
            if (y in (centerY)..(centerY + 100f)) {
                val a = (1f - (y - centerY) / 100f) * alpha * 0.4f
                drawArrow(drawScope, centerX, y, false, a)
            }
        }
        
        // Mid-line
        drawScope.drawLine(
            color = Color.White.copy(alpha = 0.3f * alpha),
            start = Offset(centerX - 60f, centerY),
            end = Offset(centerX + 60f, centerY),
            strokeWidth = 2f
        )
    }
    
    private fun drawArrow(drawScope: DrawScope, x: Float, y: Float, up: Boolean, alpha: Float) {
        val size = 12f
        val dir = if (up) -1f else 1f
        drawScope.drawLine(
            color = SciFiGold.copy(alpha = alpha),
            start = Offset(x - size, y - size * dir),
            end = Offset(x, y),
            strokeWidth = 3f
        )
        drawScope.drawLine(
            color = SciFiGold.copy(alpha = alpha),
            start = Offset(x + size, y - size * dir),
            end = Offset(x, y),
            strokeWidth = 3f
        )
    }
}
