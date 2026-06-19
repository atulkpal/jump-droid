package com.example.jump_droid

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import com.example.jump_droid.Constants.PLATFORM_HEIGHT
import com.example.jump_droid.ui.theme.*
import kotlin.math.ceil
import kotlin.math.sin

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
                    drawNormalPlatform(this, px, py, platform.width, currentZone, isGhost, platform.isTrapPlatform, gameTime)
                }
                PlatformType.MOVING -> drawMovingPlatform(this, px, py, platform.width, platform.speed, currentZone, gameTime)
                PlatformType.BOOST -> drawBoostPlatform(this, px, py, platform.width, currentZone, gameTime)
                PlatformType.ICE -> drawIcePlatform(this, px, py, platform.width, currentZone, gameTime)
                PlatformType.BREAKABLE -> drawBreakablePlatform(this, platform, py, currentZone, gameTime)
                PlatformType.PHASE -> drawPhasePlatform(this, px, py, platform.width, currentZone, gameTime)
                PlatformType.FUEL -> drawEnergyPlatform(this, px, py, platform.width, SciFiGreen, "FUEL", currentZone, gameTime)
                PlatformType.COOLING -> drawEnergyPlatform(this, px, py, platform.width, SciFiCyan, "COOL", currentZone, gameTime)
                PlatformType.STABILITY -> drawEnergyPlatform(this, px, py, platform.width, SciFiWhite, "STAB", currentZone, gameTime)
                PlatformType.MAGNETIC -> drawMagneticPlatform(this, platform, py, currentZone, gameTime)
            }

            if (platform.isJammed) {
                drawJammedEffect(this, px, py, platform.width, gameTime)
            }
        }
    }

    private fun adjustForZone(color: Color, zone: AltitudeZone): Color {
        return when (zone) {
            AltitudeZone.CLOUD_LAYER -> color.copy(alpha = (color.alpha * 1.6f).coerceIn(0f, 1f))
            AltitudeZone.EARTH -> color
            AltitudeZone.UPPER_ATMOSPHERE -> color.copy(alpha = (color.alpha * 1.3f).coerceIn(0f, 1f))
            AltitudeZone.ORBIT -> color.copy(alpha = (color.alpha * 1.4f).coerceIn(0f, 1f))
            AltitudeZone.DEEP_SPACE -> color.copy(alpha = (color.alpha * 1.5f).coerceIn(0f, 1f))
            AltitudeZone.VOID -> color.copy(alpha = (color.alpha * 1.6f).coerceIn(0f, 1f))
        }
    }

    private fun drawZoneEmissiveGlow(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, color: Color, gameTime: Long) {
        if (zone.ordinal >= AltitudeZone.UPPER_ATMOSPHERE.ordinal) {
            val pulse = sin(gameTime / 100f) * 0.2f + 1.0f
            val glowAlpha = when (zone) {
                AltitudeZone.UPPER_ATMOSPHERE -> 0.12f
                AltitudeZone.ORBIT -> 0.18f
                AltitudeZone.DEEP_SPACE -> 0.22f
                AltitudeZone.VOID -> 0.28f
                else -> 0f
            } * pulse

            drawScope.drawRect(
                color = color.copy(alpha = glowAlpha),
                topLeft = Offset(x - 4f, y - 4f),
                size = Size(width + 8f, PLATFORM_HEIGHT + 8f),
                style = Stroke(width = 3f)
            )
            drawScope.drawRect(
                color = color.copy(alpha = glowAlpha * 0.5f),
                topLeft = Offset(x - 8f, y - 8f),
                size = Size(width + 16f, PLATFORM_HEIGHT + 16f),
                style = Stroke(width = 2f)
            )
        }
    }

    private fun drawJammedEffect(drawScope: DrawScope, x: Float, y: Float, width: Float, gameTime: Long) {
        val flash = (gameTime / 150) % 2 == 0L
        val color = if (flash) SciFiRed.copy(alpha = 0.4f) else Color.Transparent
        drawScope.drawRect(color, Offset(x, y), Size(width, PLATFORM_HEIGHT))

        repeat(3) { i ->
            val rx = x + (kotlin.random.Random(gameTime + i).nextFloat() * width)
            val ry = y + (kotlin.random.Random(gameTime + i + 10).nextFloat() * PLATFORM_HEIGHT)
            drawScope.drawRect(SciFiWhite.copy(alpha = 0.5f), Offset(rx, ry), Size(10f, 2f))
        }
    }

    private fun drawNormalPlatform(
        drawScope: DrawScope, x: Float, y: Float, width: Float,
        zone: AltitudeZone, isGhost: Boolean = false, isTrapPlatform: Boolean = false, gameTime: Long = 0
    ) {
        val baseColor = adjustForZone(SciFiWhite.copy(alpha = 0.5f), zone)

        val color = if (isGhost) {
            val alpha = (0.3f + (sin(gameTime / 50f) * 0.2f)).coerceIn(0f, 1f)
            SciFiRed.copy(alpha = alpha * 1.5f)
        } else if (isTrapPlatform) {
            val warnAlpha = (0.08f + sin(gameTime / 120f) * 0.06f).coerceIn(0f, 1f)
            drawScope.drawRect(
                color = SciFiRed.copy(alpha = warnAlpha),
                topLeft = Offset(x, y),
                size = Size(width, PLATFORM_HEIGHT),
                style = Stroke(width = 3f)
            )
            baseColor
        } else baseColor

        drawBase(drawScope, x, y, width, color, zone, gameTime)
        if (!isGhost && !isTrapPlatform) {
            drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiWhite, gameTime)
        }
    }

    private fun drawMovingPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, speed: Float, zone: AltitudeZone, gameTime: Long) {
        val baseColor = adjustForZone(SciFiCyan, zone)
        drawBase(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiCyan, gameTime)

        drawScope.clipRect(left = x + 4f, top = y, right = x + width - 4f, bottom = y + PLATFORM_HEIGHT) {
            val spacing = 30f
            val direction = if (speed > 0) 1 else -1
            val offset = (gameTime / 8f * direction) % spacing

            var curX = x + offset - spacing
            while (curX < x + width + spacing) {
                val path = Path().apply {
                    moveTo(curX, y + 4f)
                    lineTo(curX + (direction * 12f), y + PLATFORM_HEIGHT / 2)
                    lineTo(curX, y + PLATFORM_HEIGHT - 4f)
                }
                drawScope.drawPath(path, Color.White.copy(alpha = 0.4f), style = Stroke(width = 4f))
                curX += spacing
            }
        }
    }

    private fun drawBoostPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, gameTime: Long) {
        val baseColor = adjustForZone(SciFiGold, zone)
        drawBase(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiGold, gameTime)

        val pulse = (sin(gameTime / 80f) * 0.5f + 0.5f)
        val glowSize = 5f + pulse * 10f

        drawScope.drawRect(
            brush = Brush.verticalGradient(listOf(SciFiGold.copy(alpha = 0.4f * pulse), Color.Transparent), startY = y, endY = y - glowSize),
            topLeft = Offset(x, y - glowSize),
            size = Size(width, glowSize)
        )

        val arrowAlpha = 0.6f + pulse * 0.4f
        val arrowWidth = 20f
        val centerX = x + width / 2

        val arrowPath = Path().apply {
            moveTo(centerX - arrowWidth, y + PLATFORM_HEIGHT - 4f)
            lineTo(centerX, y + 4f)
            lineTo(centerX + arrowWidth, y + PLATFORM_HEIGHT - 4f)
        }
        drawScope.drawPath(arrowPath, SciFiWhite.copy(alpha = arrowAlpha), style = Stroke(width = 5f))
    }

    private fun drawIcePlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, gameTime: Long) {
        val baseColor = adjustForZone(SciFiCyan.copy(alpha = 0.3f), zone)
        drawBase(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiCyan, gameTime)

        val shimmer = sin(gameTime / 60f) * 0.15f + 0.4f
        drawScope.drawRect(
            brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = shimmer), Color.Transparent), startY = y, endY = y + PLATFORM_HEIGHT),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )

        val crystalAlpha = (sin(gameTime / 80f) * 0.2f + 0.4f).coerceIn(0f, 1f)
        repeat(4) { i ->
            val rx = x + (i * (width / 4f)) + 10f
            val wobble = sin(gameTime / 100f + i * 1.5f) * 3f
            drawScope.drawLine(
                SciFiWhite.copy(alpha = crystalAlpha),
                Offset(rx + wobble, y + 2f),
                Offset(rx - 5f + wobble, y + PLATFORM_HEIGHT - 2f),
                strokeWidth = 1f
            )
        }

        val sparkleAlpha = (sin(gameTime / 40f) * 0.3f + 0.5f).coerceIn(0f, 1f)
        val sparkleX = x + ((gameTime / 7) % (width.toInt() - 20) + 10).toFloat()
        val sparkleY = y + ((gameTime / 11) % (PLATFORM_HEIGHT.toInt() - 6) + 3).toFloat()
        drawScope.drawCircle(Color.White.copy(alpha = sparkleAlpha), radius = 2f, center = Offset(sparkleX, sparkleY))
    }

    private fun drawBreakablePlatform(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val progress = if (platform.isBreaking) (platform.crackTime / platform.totalBreakTime).coerceIn(0f, 1f) else 0f

        val baseColor = adjustForZone(SciFiRed, zone)
        drawBase(drawScope, px, py, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, px, py, width, zone, SciFiRed, gameTime)

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

    private fun drawPhasePlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, gameTime: Long) {
        val cycle = 4000L
        val progress = (gameTime % cycle) / cycle.toFloat()

        val alpha = when {
            progress < 0.4f -> 1.0f
            progress < 0.5f -> 1.0f - (progress - 0.4f) * 10f
            progress < 0.9f -> 0.0f
            else -> (progress - 0.9f) * 10f
        }

        val baseColor = adjustForZone(SciFiPurple, zone)
        drawBase(drawScope, x, y, width, baseColor.copy(alpha = alpha.coerceAtLeast(0.1f)), zone, gameTime)
        if (alpha > 0.5f) {
            drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiPurple, gameTime)
        }

        if (alpha > 0.1f) {
            drawScope.clipRect(left = x, top = y, right = x + width, bottom = y + PLATFORM_HEIGHT) {
                val scanY = (gameTime / 5) % PLATFORM_HEIGHT.toInt()
                drawScope.drawLine(SciFiWhite.copy(alpha = 0.3f * alpha), Offset(x, y + scanY), Offset(x + width, y + scanY), strokeWidth = 1f)
            }
        }
    }

    private fun drawEnergyPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, label: String, zone: AltitudeZone, gameTime: Long) {
        val pulse = sin(gameTime / 90f) * 0.15f + 0.85f
        val baseColor = adjustForZone(color, zone)
        drawBase(drawScope, x, y, width, baseColor.copy(alpha = baseColor.alpha * pulse), zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, color, gameTime)

        drawScope.drawRect(
            color = color.copy(alpha = 0.4f * pulse),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = Stroke(width = 4f)
        )

        val ds = 6f
        val decalPulse = (sin(gameTime / 70f + 1f) * 0.2f + 0.8f).coerceIn(0f, 1f)
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x, y), Size(ds, ds))
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x + width - ds, y), Size(ds, ds))
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x, y + PLATFORM_HEIGHT - ds), Size(ds, ds))
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x + width - ds, y + PLATFORM_HEIGHT - ds), Size(ds, ds))

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

    private fun drawMagneticPlatform(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val baseColor = adjustForZone(SciFiPurple, zone)
        drawBase(drawScope, px, py, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, px, py, width, zone, SciFiPurple, gameTime)

        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(px + width / 4, py + 4f),
            size = Size(width / 2, PLATFORM_HEIGHT - 8f)
        )

        val pulse = (gameTime % 2500) / 2500f
        repeat(4) { i ->
            val ringProgress = (pulse + i * 0.25f) % 1f
            val radius = 80f + ringProgress * 180f
            drawScope.drawCircle(
                color = baseColor.copy(alpha = 0.3f * (1f - ringProgress)),
                radius = radius,
                center = Offset(px + width / 2, py + PLATFORM_HEIGHT / 2),
                style = Stroke(width = 3f - ringProgress * 2f)
            )
        }
    }

    private fun drawBase(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, zone: AltitudeZone = AltitudeZone.EARTH, gameTime: Long = 0) {
        drawScope.drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )

        drawScope.drawLine(
            color = Color.Black.copy(alpha = 0.3f),
            start = Offset(x + 10f, y + PLATFORM_HEIGHT / 2f),
            end = Offset(x + width - 10f, y + PLATFORM_HEIGHT / 2f),
            strokeWidth = 2f
        )

        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(x, y),
            size = Size(width, 2f)
        )

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

        val glowAlpha = if (zone.ordinal >= AltitudeZone.UPPER_ATMOSPHERE.ordinal) 0.35f else 0.2f
        drawScope.drawRect(
            color = color.copy(alpha = glowAlpha),
            topLeft = Offset(x - 2f, y - 2f),
            size = Size(width + 4f, PLATFORM_HEIGHT + 4f),
            style = Stroke(width = 1f)
        )

        drawScope.drawRect(
            color = SciFiBorder,
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = Stroke(width = 1.5f)
        )
    }
}
