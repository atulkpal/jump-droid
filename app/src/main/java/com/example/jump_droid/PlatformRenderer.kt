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
                PlatformType.NORMAL -> {
                    val isGhost = platform.isBreaking && platform.totalBreakTime < 0.5f
                    drawNormalPlatform(this, px, py, platform.width, currentZone, isGhost, gameTime)
                }
                PlatformType.MOVING -> drawMovingPlatform(this, px, py, platform.width, platform.speed, gameTime)
                PlatformType.BOOST -> drawBoostPlatform(this, px, py, platform.width, gameTime)
                PlatformType.ICE -> drawIcePlatform(this, px, py, platform.width)
                PlatformType.BREAKABLE -> drawBreakablePlatform(this, platform, py, gameTime)
                PlatformType.PHASE -> drawPhasePlatform(this, px, py, platform.width, gameTime)
                PlatformType.FUEL -> drawEnergyPlatform(this, px, py, platform.width, Color(0xFF4CAF50), "FUEL")
                PlatformType.COOLING -> drawEnergyPlatform(this, px, py, platform.width, Color(0xFF2196F3), "COOL")
                PlatformType.STABILITY -> drawEnergyPlatform(this, px, py, platform.width, Color(0xFFEEEEEE), "STAB")
                PlatformType.MAGNETIC -> drawMagneticPlatform(this, platform, py, gameTime)
            }

            if (platform.isJammed) {
                drawJammedEffect(this, px, py, platform.width, gameTime)
            }
        }
    }

    private fun drawJammedEffect(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val flash = (gameTime / 150) % 2 == 0L
        val color = if (flash) Color.Red.copy(alpha = 0.4f) else Color.Transparent
        drawScope.drawRect(color, Offset(x, y), Size(width, PLATFORM_HEIGHT))
        
        // Static glitches
        repeat(3) { i ->
            val rx = x + (kotlin.random.Random(gameTime + i).nextFloat() * width)
            val ry = y + (kotlin.random.Random(gameTime + i + 10).nextFloat() * PLATFORM_HEIGHT)
            drawScope.drawRect(Color.White.copy(alpha = 0.5f), Offset(rx, ry), Size(10f, 2f))
        }
    }

    private fun drawNormalPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, isGhost: Boolean = false, gameTime: Long = 0) {
        val baseColor = Color(0xFF9E9E9E) // Task 5: Gray
        
        val color = if (isGhost) {
            val alpha = (0.3f + (kotlin.math.sin(gameTime / 50f) * 0.2f)).coerceIn(0f, 1f)
            Color.White.copy(alpha = alpha)
        } else baseColor
        
        drawBase(drawScope, x, y, width, color)
    }

    private fun drawMovingPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, speed: Float, gameTime: Long) {
        val baseColor = Color(0xFFFFEB3B) // Task 5: Yellow
        drawBase(drawScope, x, y, width, baseColor)
        
        // Animated Chevrons
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
                drawScope.drawPath(path, Color.Black.copy(alpha = 0.2f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                curX += spacing
            }
        }
    }

    private fun drawBoostPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val baseColor = Color(0xFFFF9800) // Task 5: Orange
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
        
        val baseColor = Color(0xFFF44336) // Task 5: Red
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
                drawScope.drawRect(Color.White.copy(alpha = 0.2f), Offset(px, py), Size(width, PLATFORM_HEIGHT))
            }
        }
    }

    private fun drawPhasePlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val cycle = 4000L
        val progress = (gameTime % cycle) / cycle.toFloat()
        
        val alpha = when {
            progress < 0.4f -> 1.0f
            progress < 0.5f -> 1.0f - (progress - 0.4f) * 10f
            progress < 0.9f -> 0.0f
            else -> (progress - 0.9f) * 10f
        }
        
        val baseColor = Color(0xFF9C27B0) // Task 5: Purple
        drawBase(drawScope, x, y, width, baseColor.copy(alpha = alpha.coerceAtLeast(0.1f)))
        
        if (alpha > 0.1f) {
            repeat(2) { i ->
                val rand = kotlin.random.Random(gameTime / 200 + i)
                val sx = x + rand.nextFloat() * width
                val sy = y + rand.nextFloat() * PLATFORM_HEIGHT
                drawScope.drawCircle(Color.White.copy(alpha = 0.4f * alpha), radius = 3f, center = Offset(sx, sy))
            }
        }
    }

    private fun drawEnergyPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, label: String) {
        drawBase(drawScope, x, y, width, color)
        
        // Inner Glow
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(x + 5f, y + 5f),
            size = Size(width - 10f, PLATFORM_HEIGHT - 10f)
        )

        drawScope.drawContext.canvas.nativeCanvas.drawText(
            label,
            x + width / 2f,
            y + PLATFORM_HEIGHT / 2f + 10f,
            Paint().apply {
                this.color = android.graphics.Color.WHITE
                this.textSize = 24f
                this.textAlign = Paint.Align.CENTER
                this.typeface = Typeface.DEFAULT_BOLD
            }
        )
    }

    private fun drawMagneticPlatform(drawScope: DrawScope, platform: Platform, py: Float, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val baseColor = Color(0xFF00BCD4) // Task 5: Cyan
        drawBase(drawScope, px, py, width, baseColor)
        
        // Magnetic Field Visual (Energy Rings)
        val pulse = (gameTime % 2000) / 2000f
        repeat(3) { i ->
            val ringProgress = (pulse + i * 0.33f) % 1f
            val radius = 100f + ringProgress * 150f
            drawScope.drawCircle(
                color = baseColor.copy(alpha = 0.2f * (1f - ringProgress)),
                radius = radius,
                center = Offset(px + width / 2, py + PLATFORM_HEIGHT / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
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
