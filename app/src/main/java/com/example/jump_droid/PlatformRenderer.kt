package com.example.jump_droid

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import com.example.jump_droid.Constants.PLATFORM_HEIGHT
import kotlin.math.ceil

/**
 * Handles the visual presentation of all platform types.
 * Translates gameplay intent into consistent visual language.
 */
class PlatformRenderer {

    fun render(
        drawScope: DrawScope,
        platform: Platform,
        currentZone: AltitudeZone,
        cameraY: Float,
        gameTime: Long
    ) {
        val px = platform.x
        val py = platform.y - cameraY
        
        with(drawScope) {
            when (platform.type) {
                PlatformType.NORMAL -> drawNormalPlatform(this, px, py, platform.width, currentZone)
                PlatformType.MOVING -> drawMovingPlatform(this, px, py, platform.width, platform.speed, gameTime)
                PlatformType.BOOST -> drawBoostPlatform(this, px, py, platform.width, gameTime)
                PlatformType.ICE -> drawIcePlatform(this, px, py, platform.width)
                PlatformType.BREAKABLE -> drawBreakablePlatform(this, platform, py, gameTime)
            }
        }
    }

    private fun drawNormalPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone) {
        val baseColor = when (zone) {
            AltitudeZone.EARTH -> Color(0xFF4CAF50)
            AltitudeZone.CLOUD_LAYER -> Color(0xFF81C784)
            AltitudeZone.UPPER_ATMOSPHERE -> Color(0xFFB0BEC5)
            AltitudeZone.ORBIT -> Color(0xFF90A4AE)
            AltitudeZone.DEEP_SPACE -> Color(0xFF607D8B)
            AltitudeZone.VOID -> Color(0xFF37474F)
        }
        drawBase(drawScope, x, y, width, baseColor)
    }

    private fun drawMovingPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, speed: Float, gameTime: Long) {
        val baseColor = Color(0xFF546E7A) // Industrial Steel
        drawBase(drawScope, x, y, width, baseColor)
        
        // Animated Chevrons indicating motion
        drawScope.clipRect(left = x, top = y, right = x + width, bottom = y + PLATFORM_HEIGHT) {
            val spacing = 40f
            val offset = (gameTime / 10f * (if (speed > 0) 1 else -1)) % spacing
            
            var curX = x + offset - spacing
            while (curX < x + width + spacing) {
                val path = Path().apply {
                    moveTo(curX, y + 5f)
                    lineTo(curX + (if (speed > 0) 10f else -10f), y + PLATFORM_HEIGHT / 2)
                    lineTo(curX, y + PLATFORM_HEIGHT - 5f)
                }
                drawScope.drawPath(path, Color.White.copy(alpha = 0.3f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                curX += spacing
            }
        }
    }

    private fun drawBoostPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val baseColor = Color(0xFFFFC107) // Amber/Gold
        drawBase(drawScope, x, y, width, baseColor)
        
        // Upward Pulsing Arrows
        val pulse = (kotlin.math.sin(gameTime / 100f) * 0.5f + 0.5f)
        val arrowAlpha = 0.4f + pulse * 0.4f
        
        val arrowPath = Path().apply {
            moveTo(x + width / 2 - 15f, y + PLATFORM_HEIGHT - 5f)
            lineTo(x + width / 2, y + 5f)
            lineTo(x + width / 2 + 15f, y + PLATFORM_HEIGHT - 5f)
        }
        drawScope.drawPath(arrowPath, Color.White.copy(alpha = arrowAlpha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }

    private fun drawIcePlatform(drawScope: DrawScope, x: Float, y: Float, width: Float) {
        val baseColor = Color(0xFFB2EBF2) // Frosty Blue
        drawBase(drawScope, x, y, width, baseColor)
        
        // Crystalline Shards
        drawScope.drawLine(Color.White.copy(alpha = 0.6f), Offset(x + 5f, y + 5f), Offset(x + 20f, y + 10f), strokeWidth = 2f)
        drawScope.drawLine(Color.White.copy(alpha = 0.4f), Offset(x + width - 15f, y + 12f), Offset(x + width - 5f, y + 5f), strokeWidth = 1f)
        drawScope.drawRect(Color.White.copy(alpha = 0.3f), Offset(x, y), Size(width, 4f)) // Top glare
    }

    private fun drawBreakablePlatform(drawScope: DrawScope, platform: Platform, py: Float, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val progress = if (platform.isBreaking) (platform.crackTime / platform.totalBreakTime).coerceIn(0f, 1f) else 0f
        
        val baseColor = Color(0xFFFF9800) // Hazard Orange
        drawBase(drawScope, px, py, width, baseColor)
        
        // Structural Damage (Cracks)
        val crackCount = (progress * 6).toInt() + 1
        repeat(crackCount) { i ->
            val startX = px + (width / (crackCount + 1)) * (i + 1)
            drawScope.drawLine(
                color = Color.Black.copy(alpha = 0.6f + progress * 0.4f),
                start = Offset(startX, py),
                end = Offset(startX + (if (i % 2 == 0) 12f else -12f), py + PLATFORM_HEIGHT),
                strokeWidth = 2f + progress * 3f
            )
        }
        
        // Hazard Stripes if breaking
        if (platform.isBreaking) {
            val flash = (gameTime / 150) % 2 == 0L
            if (flash) {
                drawScope.drawRect(Color.Red.copy(alpha = 0.2f), Offset(px, py), Size(width, PLATFORM_HEIGHT))
            }
        }

        // Countdown Text
        val remaining = ceil(platform.totalBreakTime - platform.crackTime).toInt()
        val text = if (platform.isBreaking) {
            if (remaining > 0) remaining.toString() else "!"
        } else {
            "?"
        }
        
        val textColor = if (platform.isBreaking) android.graphics.Color.RED else android.graphics.Color.YELLOW
        val textSize = if (platform.isBreaking) 40f else 30f
        
        drawScope.drawContext.canvas.nativeCanvas.drawText(
            text,
            px + width / 2f,
            py - 10f,
            Paint().apply {
                this.color = textColor
                this.textSize = textSize
                this.textAlign = Paint.Align.CENTER
                this.typeface = Typeface.DEFAULT_BOLD
                if (platform.isBreaking) this.setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
            }
        )
    }

    /**
     * Shared rendering for all platforms to ensure cohesive feel.
     */
    private fun drawBase(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color) {
        // Main volume
        drawScope.drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )
        
        // Bevel/Highlight (Top)
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(x, y),
            size = Size(width, 3f)
        )
        
        // Bevel/Shadow (Bottom)
        drawScope.drawRect(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(x, y + PLATFORM_HEIGHT - 3f),
            size = Size(width, 3f)
        )
        
        // Subtle Border
        drawScope.drawRect(
            color = Color.Black.copy(alpha = 0.1f),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )
    }
}
