package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

class SingularityRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player
    ) {
        val cx = threat.x
        val cy = threat.y - cameraY
        
        // 1. Shifting White Noise Core
        val corePulse = threat.scanPulse
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.8f * alpha),
            radius = 60f + corePulse * 20f,
            center = Offset(cx, cy)
        )
        
        // 2. Geometric Fragments
        repeat(12) { i ->
            val angle = (gameTime / 500f) + (i * 0.52f)
            val dist = 100f + sin(gameTime / 300f + i) * 30f
            val fx = cx + cos(angle) * dist
            val fy = cy + sin(angle) * dist
            
            drawScope.rotate(degrees = (gameTime / 10f) + i * 30f, pivot = Offset(fx, fy)) {
                drawScope.drawRect(
                    color = Color.White.copy(alpha = 0.6f * alpha),
                    topLeft = Offset(fx - 10f, fy - 10f),
                    size = Size(20f, 20f),
                    style = Stroke(width = 2f)
                )
            }
        }
        
        // 3. Reality Rifts (Random lines)
        repeat(5) { i ->
            val rx = cx + (kotlin.random.Random(gameTime + i).nextFloat() - 0.5f) * 400f
            val ry = cy + (kotlin.random.Random(gameTime + i + 100).nextFloat() - 0.5f) * 400f
            drawScope.drawLine(
                color = Color.White.copy(alpha = 0.3f * alpha),
                start = Offset(rx - 50f, ry),
                end = Offset(rx + 50f, ry),
                strokeWidth = 1f
            )
        }
    }
}
