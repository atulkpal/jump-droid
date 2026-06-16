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
import com.example.jump_droid.ui.theme.*
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
                PlatformType.FUEL -> drawEnergyPlatform(this, px, py, platform.width, SciFiGreen, "FUEL")
                PlatformType.COOLING -> drawEnergyPlatform(this, px, py, platform.width, SciFiCyan, "COOL")
                PlatformType.STABILITY -> drawEnergyPlatform(this, px, py, platform.width, SciFiWhite, "STAB")
                PlatformType.MAGNETIC -> drawMagneticPlatform(this, platform, py, gameTime)
            }

            if (platform.isJammed) {
                drawJammedEffect(this, px, py, platform.width, gameTime)
            }
        }
    }

    private fun drawJammedEffect(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val flash = (gameTime / 150) % 2 == 0L
        val color = if (flash) SciFiRed.copy(alpha = 0.4f) else Color.Transparent
        drawScope.drawRect(color, Offset(x, y), Size(width, PLATFORM_HEIGHT))
        
        // Static glitches
        repeat(3) { i ->
            val rx = x + (kotlin.random.Random(gameTime + i).nextFloat() * width)
            val ry = y + (kotlin.random.Random(gameTime + i + 10).nextFloat() * PLATFORM_HEIGHT)
            drawScope.drawRect(SciFiWhite.copy(alpha = 0.5f), Offset(rx, ry), Size(10f, 2f))
        }
    }

    private fun drawNormalPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, isGhost: Boolean = false, gameTime: Long = 0) {
        val baseColor = SciFiWhite.copy(alpha = 0.5f)
        
        val color = if (isGhost) {
            val alpha = (0.3f + (kotlin.math.sin(gameTime / 50f) * 0.2f)).coerceIn(0f, 1f)
            SciFiWhite.copy(alpha = alpha)
        } else baseColor
        
        drawBase(drawScope, x, y, width, color)
    }

    private fun drawMovingPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, speed: Float, gameTime: Long) {
        val baseColor = SciFiCyan
        drawBase(drawScope, x, y, width, baseColor)
        
        // Animated Chevrons - Enhanced Contrast
        drawScope.clipRect(left = x + 4f, top = y, right = x + width - 4f, bottom = y + PLATFORM_HEIGHT) {
            val spacing = 30f
            val offset = (gameTime / 8f * (if (speed > 0) 1 else -1)) % spacing
            
            var curX = x + offset - spacing
            while (curX < x + width + spacing) {
                val path = Path().apply {
                    moveTo(curX, y + 4f)
                    lineTo(curX + (if (speed > 0) 12f else -12f), y + PLATFORM_HEIGHT / 2)
                    lineTo(curX, y + PLATFORM_HEIGHT - 4f)
                }
                drawScope.drawPath(path, Color.White.copy(alpha = 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                curX += spacing
            }
        }
    }

    private fun drawBoostPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val baseColor = SciFiGold
        drawBase(drawScope, x, y, width, baseColor)
        
        // High-Intensity Pulse
        val pulse = (kotlin.math.sin(gameTime / 80f) * 0.5f + 0.5f)
        val glowSize = 5f + pulse * 10f
        
        // Top Glow
        drawScope.drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(SciFiGold.copy(alpha = 0.4f * pulse), Color.Transparent), startY = y, endY = y - glowSize),
            topLeft = Offset(x, y - glowSize),
            size = Size(width, glowSize)
        )
        
        // Boost Indicator (Centralised)
        val arrowAlpha = 0.6f + pulse * 0.4f
        val arrowWidth = 20f
        val centerX = x + width / 2
        
        val arrowPath = Path().apply {
            moveTo(centerX - arrowWidth, y + PLATFORM_HEIGHT - 4f)
            lineTo(centerX, y + 4f)
            lineTo(centerX + arrowWidth, y + PLATFORM_HEIGHT - 4f)
        }
        drawScope.drawPath(arrowPath, SciFiWhite.copy(alpha = arrowAlpha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
    }

    private fun drawIcePlatform(drawScope: DrawScope, x: Float, y: Float, width: Float) {
        val baseColor = SciFiCyan.copy(alpha = 0.3f)
        drawBase(drawScope, x, y, width, baseColor)
        
        // Surface Sheen / Gloss
        drawScope.drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent), startY = y, endY = y + PLATFORM_HEIGHT),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )
        
        // Crystalline Internal Detail
        repeat(4) { i ->
            val rx = x + (i * (width / 4f)) + 10f
            drawScope.drawLine(SciFiWhite.copy(alpha = 0.5f), Offset(rx, y + 2f), Offset(rx - 5f, y + PLATFORM_HEIGHT - 2f), strokeWidth = 1f)
        }
    }

    private fun drawBreakablePlatform(drawScope: DrawScope, platform: Platform, py: Float, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val progress = if (platform.isBreaking) (platform.crackTime / platform.totalBreakTime).coerceIn(0f, 1f) else 0f
        
        val baseColor = SciFiRed
        drawBase(drawScope, px, py, width, baseColor)
        
        // Warning Stripes
        drawScope.clipRect(left = px, top = py, right = px + width, bottom = py + PLATFORM_HEIGHT) {
            val stripeW = 15f
            repeat((width / stripeW).toInt() + 1) { i ->
                if (i % 2 == 0) {
                    val path = Path().apply {
                        moveTo(px + i * stripeW, py)
                        lineTo(px + (i + 1) * stripeW, py)
                        lineTo(px + i * stripeW + 10f, py + PLATFORM_HEIGHT)
                        lineTo(px + (i - 1) * stripeW + 10f, py + PLATFORM_HEIGHT)
                        close()
                    }
                    drawScope.drawPath(path, Color.Black.copy(alpha = 0.2f))
                }
            }
        }
        
        // Structural Damage (Cracks) - More dramatic
        val crackCount = (progress * 8).toInt() + 1
        repeat(crackCount) { i ->
            val startX = px + (width / (crackCount + 1)) * (i + 1)
            drawScope.drawLine(
                color = SciFiWhite.copy(alpha = 0.7f + progress * 0.3f),
                start = Offset(startX, py),
                end = Offset(startX + (if (i % 2 == 0) 15f else -15f), py + PLATFORM_HEIGHT),
                strokeWidth = 2f + progress * 4f
            )
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
        
        val baseColor = SciFiPurple
        drawBase(drawScope, x, y, width, baseColor.copy(alpha = alpha.coerceAtLeast(0.1f)))
        
        // Digital "Noise" / Scanlines
        if (alpha > 0.1f) {
            drawScope.clipRect(left = x, top = y, right = x + width, bottom = y + PLATFORM_HEIGHT) {
                val scanY = (gameTime / 5) % PLATFORM_HEIGHT.toInt()
                drawScope.drawLine(SciFiWhite.copy(alpha = 0.3f * alpha), Offset(x, y + scanY), Offset(x + width, y + scanY), strokeWidth = 1f)
            }
        }
    }

    private fun drawEnergyPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, label: String) {
        drawBase(drawScope, x, y, width, color)
        
        // Outer Glow Frame
        drawScope.drawRect(
            color = color.copy(alpha = 0.4f),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
        
        // Tech Decals (Corners)
        val ds = 6f
        drawScope.drawRect(SciFiWhite, Offset(x, y), Size(ds, ds))
        drawScope.drawRect(SciFiWhite, Offset(x + width - ds, y), Size(ds, ds))
        drawScope.drawRect(SciFiWhite, Offset(x, y + PLATFORM_HEIGHT - ds), Size(ds, ds))
        drawScope.drawRect(SciFiWhite, Offset(x + width - ds, y + PLATFORM_HEIGHT - ds), Size(ds, ds))

        drawScope.drawContext.canvas.nativeCanvas.drawText(
            label,
            x + width / 2f,
            y + PLATFORM_HEIGHT / 2f + 8f,
            Paint().apply {
                this.color = android.graphics.Color.WHITE
                this.textSize = 22f
                this.textAlign = Paint.Align.CENTER
                this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                this.letterSpacing = 0.2f
            }
        )
    }

    private fun drawMagneticPlatform(drawScope: DrawScope, platform: Platform, py: Float, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val baseColor = SciFiPurple
        drawBase(drawScope, px, py, width, baseColor)
        
        // Magnetic Core
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(px + width / 4, py + 4f),
            size = Size(width / 2, PLATFORM_HEIGHT - 8f)
        )
        
        // Enhanced Magnetic Field Visual
        val pulse = (gameTime % 2500) / 2500f
        repeat(4) { i ->
            val ringProgress = (pulse + i * 0.25f) % 1f
            val radius = 80f + ringProgress * 180f
            drawScope.drawCircle(
                color = baseColor.copy(alpha = 0.3f * (1f - ringProgress)),
                radius = radius,
                center = Offset(px + width / 2, py + PLATFORM_HEIGHT / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f - ringProgress * 2f)
            )
        }
    }

    /**
     * Shared rendering for all platforms to ensure cohesive feel.
     * Sprint E Refinement: Bold, Crisp, Premium.
     */
    private fun drawBase(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color) {
        // Main volume with slight gradient or depth
        drawScope.drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )
        
        // Inner detail line (Sci-Fi groove)
        drawScope.drawLine(
            color = Color.Black.copy(alpha = 0.3f),
            start = Offset(x + 10f, y + PLATFORM_HEIGHT / 2f),
            end = Offset(x + width - 10f, y + PLATFORM_HEIGHT / 2f),
            strokeWidth = 2f
        )
        
        // Premium Bevel/Highlight (Top)
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(x, y),
            size = Size(width, 2f)
        )
        
        // Side Accents (Bold silhouettes)
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(x, y),
            size = Size(4f, PLATFORM_HEIGHT)
        )
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(x + width - 4f, y),
            size = Size(4f, PLATFORM_HEIGHT)
        )
        
        // Subtle Border Glow
        drawScope.drawRect(
            color = color.copy(alpha = 0.2f),
            topLeft = Offset(x - 2f, y - 2f),
            size = Size(width + 4f, PLATFORM_HEIGHT + 4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )
        
        // Outer Crisp Border
        drawScope.drawRect(
            color = SciFiBorder,
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}
