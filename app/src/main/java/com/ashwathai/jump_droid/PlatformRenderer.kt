package com.ashwathai.jump_droid

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
import com.ashwathai.jump_droid.Constants.PLATFORM_HEIGHT
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
                PlatformType.STABILITY -> drawEnergyPlatform(this, px, py, platform.width, SciFiCyan, "SHLD", currentZone, gameTime)
                PlatformType.MAGNETIC -> drawMagneticPlatform(this, platform, py, currentZone, gameTime)
                PlatformType.FLUX -> FluxRenderer.draw(this, platform, py, currentZone, gameTime)
                PlatformType.GRAVITON -> GravitonRenderer.draw(this, platform, py, currentZone, gameTime)
                PlatformType.CONVEYOR -> drawConveyorPlatform(this, platform, py, currentZone, gameTime)
                PlatformType.MIMIC -> drawMimicPlatform(this, px, py, platform.width, currentZone, gameTime)
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
            AltitudeZone.THE_FOUNDRY -> color.copy(alpha = (color.alpha * 1.6f).coerceIn(0f, 1f))
            AltitudeZone.CHRONO_RIFT -> color.copy(alpha = (color.alpha * 1.6f).coerceIn(0f, 1f))
            AltitudeZone.VOID, AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE, AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY ->
                color.copy(alpha = (color.alpha * 1.6f).coerceIn(0f, 1f))
        }
    }

    private fun zoneTint(zone: AltitudeZone): Color = when (zone) {
        AltitudeZone.EARTH -> Color(0xFF795548)
        AltitudeZone.CLOUD_LAYER -> Color(0xFF80DEEA)
        AltitudeZone.THE_FOUNDRY -> Color(0xFFFF6D00)
        AltitudeZone.CHRONO_RIFT -> SciFiPurple
        AltitudeZone.VOID -> SciFiRed
        else -> SciFiWhite
    }

    private fun drawZoneEmissiveGlow(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, color: Color, gameTime: Long) {
        if (zone.ordinal >= AltitudeZone.UPPER_ATMOSPHERE.ordinal) {
            val pulse = sin(gameTime / 100f) * 0.2f + 1.0f
            val glowAlpha = when (zone) {
                AltitudeZone.UPPER_ATMOSPHERE -> 0.12f
                AltitudeZone.ORBIT -> 0.18f
                AltitudeZone.DEEP_SPACE -> 0.22f
                AltitudeZone.THE_FOUNDRY -> 0.28f
                AltitudeZone.CHRONO_RIFT -> 0.28f
                AltitudeZone.VOID, AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE, AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY -> 0.28f
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
            val rx = x + (Random(gameTime + i).nextFloat() * width)
            val ry = y + (Random(gameTime + i + 10).nextFloat() * PLATFORM_HEIGHT)
            drawScope.drawRect(SciFiWhite.copy(alpha = 0.5f), Offset(rx, ry), Size(10f, 2f))
        }
    }

    private fun drawCornerBrackets(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, alpha: Float, size: Float = 8f) {
        val a = alpha.coerceIn(0f, 1f)
        // Top-left
        drawScope.drawLine(color.copy(alpha = a), Offset(x, y + size), Offset(x, y), strokeWidth = 2f)
        drawScope.drawLine(color.copy(alpha = a), Offset(x, y), Offset(x + size, y), strokeWidth = 2f)
        // Top-right
        drawScope.drawLine(color.copy(alpha = a), Offset(x + width - size, y), Offset(x + width, y), strokeWidth = 2f)
        drawScope.drawLine(color.copy(alpha = a), Offset(x + width, y), Offset(x + width, y + size), strokeWidth = 2f)
        // Bottom-left
        drawScope.drawLine(color.copy(alpha = a), Offset(x, y + PLATFORM_HEIGHT - size), Offset(x, y + PLATFORM_HEIGHT), strokeWidth = 2f)
        drawScope.drawLine(color.copy(alpha = a), Offset(x, y + PLATFORM_HEIGHT), Offset(x + size, y + PLATFORM_HEIGHT), strokeWidth = 2f)
        // Bottom-right
        drawScope.drawLine(color.copy(alpha = a), Offset(x + width - size, y + PLATFORM_HEIGHT), Offset(x + width, y + PLATFORM_HEIGHT), strokeWidth = 2f)
        drawScope.drawLine(color.copy(alpha = a), Offset(x + width, y + PLATFORM_HEIGHT - size), Offset(x + width, y + PLATFORM_HEIGHT), strokeWidth = 2f)
    }

    private fun drawNormalPlatform(
        drawScope: DrawScope, x: Float, y: Float, width: Float,
        zone: AltitudeZone, isGhost: Boolean = false, isTrapPlatform: Boolean = false, gameTime: Long = 0
    ) {
        val tint = if (isGhost) SciFiRed else zoneTint(zone)
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

        drawCore(drawScope, x, y, width, color, zone, gameTime)

        if (!isGhost && !isTrapPlatform) {
            drawZoneEmissiveGlow(drawScope, x, y, width, zone, tint, gameTime)
            drawCornerBrackets(drawScope, x, y, width, SciFiWhite, 0.3f, 10f)

            // Holographic grid overlay
            val gridAlpha = 0.08f + sin(gameTime / 300f) * 0.04f
            drawScope.clipRect(left = x + 6f, top = y + 4f, right = x + width - 6f, bottom = y + PLATFORM_HEIGHT - 4f) {
                val gridSpacing = 20f
                var gx = x + 6f
                while (gx < x + width - 6f) {
                    drawScope.drawLine(tint.copy(alpha = gridAlpha), Offset(gx, y + 4f), Offset(gx, y + PLATFORM_HEIGHT - 4f), strokeWidth = 0.5f)
                    gx += gridSpacing
                }
                var gy = y + 4f
                while (gy < y + PLATFORM_HEIGHT - 4f) {
                    drawScope.drawLine(tint.copy(alpha = gridAlpha), Offset(x + 6f, gy), Offset(x + width - 6f, gy), strokeWidth = 0.5f)
                    gy += gridSpacing
                }
            }
        }
    }

    private fun drawMovingPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, speed: Float, zone: AltitudeZone, gameTime: Long) {
        val baseColor = adjustForZone(SciFiCyan, zone)
        drawCore(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiCyan, gameTime)
        drawCornerBrackets(drawScope, x, y, width, SciFiCyan, 0.4f, 8f)

        val dir = if (speed > 0) 1 else -1
        val speedFactor = (kotlin.math.abs(speed) / 200f).coerceIn(0.5f, 2f)

        // Directional energy streaks
        drawScope.clipRect(left = x + 4f, top = y, right = x + width - 4f, bottom = y + PLATFORM_HEIGHT) {
            val spacing = 30f
            val offset = (gameTime / 8f * dir * speedFactor) % spacing
            var curX = x + offset - spacing
            while (curX < x + width + spacing) {
                val path = Path().apply {
                    moveTo(curX, y + 4f)
                    lineTo(curX + (dir * 12f), y + PLATFORM_HEIGHT / 2)
                    lineTo(curX, y + PLATFORM_HEIGHT - 4f)
                }
                drawScope.drawPath(path, Color.White.copy(alpha = 0.4f), style = Stroke(width = 4f))
                curX += spacing
            }

            // Trailing glow particles
            val trailEdge = if (dir > 0) x + width - 6f else x + 6f
            repeat(3) { i ->
                val tp = ((gameTime / 80f + i * 0.33f) % 1f)
                val tx = trailEdge + dir * tp * 30f
                val ty = y + PLATFORM_HEIGHT / 2 + sin(gameTime / 100f + i) * 8f
                drawScope.drawCircle(
                    color = SciFiCyan.copy(alpha = 0.25f * (1f - tp) * speedFactor),
                    radius = 2f * (1f - tp),
                    center = Offset(tx, ty)
                )
            }
        }
    }

    private fun drawBoostPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, gameTime: Long) {
        val baseColor = adjustForZone(SciFiGold, zone)
        drawCore(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiGold, gameTime)
        drawCornerBrackets(drawScope, x, y, width, SciFiGold, 0.5f, 8f)

        val pulse = (sin(gameTime / 80f) * 0.5f + 0.5f)
        val glowSize = 5f + pulse * 10f

        // Top thrust-cone gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(SciFiGold.copy(alpha = 0.5f * pulse), Color.Transparent),
                startY = y, endY = y - glowSize
            ),
            topLeft = Offset(x, y - glowSize),
            size = Size(width, glowSize)
        )
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.3f * pulse), Color.Transparent),
                startY = y, endY = y - glowSize * 0.5f
            ),
            topLeft = Offset(x, y - glowSize * 0.5f),
            size = Size(width, glowSize * 0.5f)
        )

        // Arrow
        val arrowAlpha = 0.6f + pulse * 0.4f
        val arrowWidth = 20f
        val centerX = x + width / 2
        val arrowPath = Path().apply {
            moveTo(centerX - arrowWidth, y + PLATFORM_HEIGHT - 4f)
            lineTo(centerX, y + 4f)
            lineTo(centerX + arrowWidth, y + PLATFORM_HEIGHT - 4f)
        }
        drawScope.drawPath(arrowPath, SciFiWhite.copy(alpha = arrowAlpha), style = Stroke(width = 5f))

        // Rising spark particles
        repeat(4) { i ->
            val sx = x + (width * 0.15f) + ((gameTime / 50f + i * (width / 4f)) % (width * 0.7f))
            val sy = y + PLATFORM_HEIGHT - 4f - ((gameTime / 100f * (0.5f + i * 0.2f) + i * 20f) % 60f)
            drawScope.drawCircle(
                color = SciFiGold.copy(alpha = 0.4f * (1f - (y + PLATFORM_HEIGHT - sy) / 60f) * pulse),
                radius = 1.5f,
                center = Offset(sx, sy)
            )
        }

        // Compression ring on landing indicator
        val ringPulse = (sin(gameTime / 200f) * 0.5f + 0.5f)
        drawScope.drawCircle(
            color = SciFiGold.copy(alpha = 0.12f * ringPulse),
            radius = width * 0.3f + ringPulse * 20f,
            center = Offset(centerX, y + PLATFORM_HEIGHT / 2),
            style = Stroke(width = 2f)
        )
    }

    private fun drawIcePlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, gameTime: Long) {
        val baseColor = adjustForZone(SciFiCyan.copy(alpha = 0.3f), zone)
        drawCore(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiCyan, gameTime)
        drawCornerBrackets(drawScope, x, y, width, SciFiCyan, 0.3f, 8f)

        // Polarized light sweep
        val sweepPhase = (gameTime / 2000f) % 1f
        val sweepX = x + sweepPhase * width
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startX = sweepX - width * 0.2f,
                endX = sweepX + width * 0.2f
            ),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )

        // Hexagonal frost pattern (simplified as angled cross-hatch)
        val frostAlpha = (sin(gameTime / 200f) * 0.1f + 0.2f).coerceIn(0f, 1f)
        drawScope.clipRect(left = x + 4f, top = y + 2f, right = x + width - 4f, bottom = y + PLATFORM_HEIGHT - 2f) {
            val hSpacing = 25f
            var hx = x + 4f
            while (hx < x + width - 4f) {
                val wobble = sin(gameTime / 300f + hx / 50f) * 3f
                drawScope.drawLine(
                    SciFiWhite.copy(alpha = frostAlpha),
                    Offset(hx + wobble, y + 2f),
                    Offset(hx + 10f + wobble, y + PLATFORM_HEIGHT - 2f),
                    strokeWidth = 0.5f
                )
                drawScope.drawLine(
                    SciFiWhite.copy(alpha = frostAlpha),
                    Offset(hx + 10f + wobble, y + 2f),
                    Offset(hx + wobble, y + PLATFORM_HEIGHT - 2f),
                    strokeWidth = 0.5f
                )
                hx += hSpacing
            }
        }

        // Icicle spikes along bottom edge
        val spikeAlpha = (sin(gameTime / 150f) * 0.2f + 0.3f).coerceIn(0f, 1f)
        repeat((width / 25f).toInt() + 1) { i ->
            val sx = x + i * 25f + sin(gameTime / 200f + i) * 4f
            val spikeH = 6f + sin(gameTime / 100f + i * 2f) * 3f
            val spikePath = Path().apply {
                moveTo(sx - 3f, y + PLATFORM_HEIGHT)
                lineTo(sx, y + PLATFORM_HEIGHT + spikeH)
                lineTo(sx + 3f, y + PLATFORM_HEIGHT)
                close()
            }
            drawScope.drawPath(spikePath, SciFiWhite.copy(alpha = spikeAlpha))
        }

        // Sparkle
        val sparkleAlpha = (sin(gameTime / 40f) * 0.3f + 0.5f).coerceIn(0f, 1f)
        val sparkleX = x + ((gameTime / 7) % (width.toInt() - 20) + 10).toFloat()
        val sparkleY = y + ((gameTime / 11) % (PLATFORM_HEIGHT.toInt() - 6) + 3).toFloat()
        drawScope.drawCircle(Color.White.copy(alpha = sparkleAlpha), radius = 2f, center = Offset(sparkleX, sparkleY))
    }

    private fun drawBreakablePlatform(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val progress = if (platform.isBreaking) (platform.crackTime / platform.totalBreakTime).coerceIn(0f, 1f) else 0f
        val pulse = sin(gameTime / 100f) * 0.2f + 0.8f

        val baseColor = adjustForZone(SciFiRed, zone)
        drawCore(drawScope, px, py, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, px, py, width, zone, SciFiRed, gameTime)

        // Warning hologram edges
        val warnAlpha = 0.15f + (sin(gameTime / 50f) * 0.1f + 0.1f) * (1f - progress)
        drawScope.drawRect(
            color = SciFiRed.copy(alpha = warnAlpha),
            topLeft = Offset(px - 3f, py - 3f),
            size = Size(width + 6f, PLATFORM_HEIGHT + 6f),
            style = Stroke(width = 2f)
        )

        // Glowing fracture network
        val crackCount = (progress * 10).toInt() + 1
        repeat(crackCount) { i ->
            val startX = px + (width / (crackCount + 1)) * (i + 1)
            val branchOffset = if (i % 2 == 0) 15f else -15f
            val crackBrightness = 0.5f + progress * 0.5f + sin(gameTime / 30f + i) * 0.1f

            // Main crack
            drawScope.drawLine(
                color = Color.White.copy(alpha = crackBrightness),
                start = Offset(startX, py),
                end = Offset(startX + branchOffset, py + PLATFORM_HEIGHT),
                strokeWidth = 2f + progress * 4f
            )

            // Branch cracks
            if (progress > 0.3f) {
                repeat(2) { j ->
                    val frac = 0.3f + j * 0.3f
                    val bx = startX + branchOffset * frac
                    val by = py + PLATFORM_HEIGHT * frac
                    drawScope.drawLine(
                        color = Color.White.copy(alpha = crackBrightness * 0.6f),
                        start = Offset(bx, by),
                        end = Offset(bx + branchOffset * 0.5f + 10f * j, by - 8f),
                        strokeWidth = 1.5f
                    )
                }
            }

            // Debris particles around cracks
            if (progress > 0.5f) {
                repeat(2) { j ->
                    val seed = platform.hashCode() + i * 7 + j + (gameTime / 100).toInt()
                    val rng = Random(seed)
                    val dx = startX + (rng.nextFloat() - 0.5f) * 30f
                    val dy = py + rng.nextFloat() * PLATFORM_HEIGHT
                    drawScope.drawCircle(
                        color = SciFiRed.copy(alpha = (1f - progress) * 0.5f * pulse),
                        radius = 1f + rng.nextFloat() * 2f,
                        center = Offset(dx, dy)
                    )
                }
            }
        }

        drawCornerBrackets(drawScope, px, py, width, SciFiRed, (1f - progress).coerceIn(0.2f, 0.6f), 8f)
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
        drawCore(drawScope, x, y, width, baseColor.copy(alpha = alpha.coerceAtLeast(0.1f)), zone, gameTime)

        // Phase-shift ripple ring
        if (alpha < 0.5f && alpha > 0.05f) {
            val rippleRadius = width * 0.4f + (1f - alpha) * width * 0.3f
            drawScope.drawCircle(
                color = SciFiPurple.copy(alpha = (1f - alpha) * 0.3f),
                radius = rippleRadius,
                center = Offset(x + width / 2, y + PLATFORM_HEIGHT / 2),
                style = Stroke(width = 3f)
            )
        }

        if (alpha > 0.5f) {
            drawZoneEmissiveGlow(drawScope, x, y, width, zone, SciFiPurple, gameTime)
            drawCornerBrackets(drawScope, x, y, width, SciFiPurple, 0.4f, 8f)
        }

        if (alpha > 0.1f) {
            drawScope.clipRect(left = x, top = y, right = x + width, bottom = y + PLATFORM_HEIGHT) {
                // Scan line
                val scanY = (gameTime / 5) % PLATFORM_HEIGHT.toInt()
                drawScope.drawLine(SciFiWhite.copy(alpha = 0.3f * alpha), Offset(x, y + scanY), Offset(x + width, y + scanY), strokeWidth = 1f)

                // Digital noise
                if (alpha > 0.6f) {
                    val noiseAlpha = (sin(gameTime / 50f) * 0.5f + 0.5f) * 0.08f * alpha
                    repeat(3) { i ->
                        val nx = x + ((gameTime / 30f + i * (width / 3f)) % width)
                        val ny = y + ((gameTime / 40f + i * 20f) % PLATFORM_HEIGHT)
                        drawScope.drawRect(
                            color = Color.White.copy(alpha = noiseAlpha),
                            topLeft = Offset(nx, ny),
                            size = Size(8f + sin(gameTime / 60f + i) * 3f, 2f)
                        )
                    }
                }

                // Glitch displacement during fade
                if (alpha < 0.8f && alpha > 0.2f) {
                    val glitchIntensity = (1f - alpha.coerceIn(0.2f, 0.8f)) * 0.5f
                    repeat(2) { i ->
                        val gy = y + ((gameTime / 80f + i * 50f) % PLATFORM_HEIGHT)
                        val gOffset = 4f + sin(gameTime / 30f + i) * 4f
                        drawScope.drawRect(
                            color = SciFiPurple.copy(alpha = glitchIntensity * 0.2f),
                            topLeft = Offset(x + gOffset, gy),
                            size = Size(width - gOffset * 2f, 3f)
                        )
                    }
                }
            }
        }
    }

    private fun drawEnergyPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, label: String, zone: AltitudeZone, gameTime: Long) {
        val pulse = sin(gameTime / 90f) * 0.15f + 0.85f
        val baseColor = adjustForZone(color, zone)
        drawCore(drawScope, x, y, width, baseColor.copy(alpha = baseColor.alpha * pulse), zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, color, gameTime)
        drawCornerBrackets(drawScope, x, y, width, color, 0.5f * pulse, 8f)

        // Pulsing inner border
        drawScope.drawRect(
            color = color.copy(alpha = 0.4f * pulse),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = Stroke(width = 4f)
        )

        // Corner decals
        val ds = 6f
        val decalPulse = (sin(gameTime / 70f + 1f) * 0.2f + 0.8f).coerceIn(0f, 1f)
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x, y), Size(ds, ds))
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x + width - ds, y), Size(ds, ds))
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x, y + PLATFORM_HEIGHT - ds), Size(ds, ds))
        drawScope.drawRect(SciFiWhite.copy(alpha = decalPulse), Offset(x + width - ds, y + PLATFORM_HEIGHT - ds), Size(ds, ds))

        // Resource flow lines — particles flowing inward
        val flowCount = 3
        repeat(flowCount) { i ->
            val frac = ((gameTime / 200f + i * 0.33f) % 1f)
            val fx = x + frac * width
            val fy = y + PLATFORM_HEIGHT / 2 + sin(gameTime / 100f + i * 2f) * 6f
            drawScope.drawCircle(
                color = SciFiWhite.copy(alpha = 0.3f * (1f - kotlin.math.abs(frac - 0.5f) * 2f) * pulse),
                radius = 1.5f,
                center = Offset(fx, fy)
            )
        }

        // Label icon
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
                this.setShadowLayer(4f, 0f, 0f, android.graphics.Color.argb(128, 255, 255, 255))
            }
        )
    }

    private fun drawMagneticPlatform(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val cx = px + width / 2
        val cy = py + PLATFORM_HEIGHT / 2
        val baseColor = adjustForZone(SciFiPurple, zone)
        drawCore(drawScope, px, py, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, px, py, width, zone, SciFiPurple, gameTime)
        drawCornerBrackets(drawScope, px, py, width, SciFiPurple, 0.5f, 8f)

        // Core
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(px + width / 4, py + 4f),
            size = Size(width / 2, PLATFORM_HEIGHT - 8f)
        )

        // Magnetic field lines (arcs from center)
        repeat(4) { i ->
            val angle = (i * 90f + sin(gameTime / 200f + i) * 20f)
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val endX = cx + kotlin.math.cos(rad) * width * 0.6f
            val endY = cy + kotlin.math.sin(rad) * PLATFORM_HEIGHT * 0.8f
            val fieldPath = Path().apply {
                moveTo(cx, cy)
                cubicTo(
                    cx + (endX - cx) * 0.3f, cy - 20f,
                    cx + (endX - cx) * 0.7f, endY - 15f,
                    endX, endY
                )
            }
            drawScope.drawPath(
                path = fieldPath,
                color = SciFiPurple.copy(alpha = 0.15f * (sin(gameTime / 100f + i) * 0.5f + 0.5f)),
                style = Stroke(width = 1.5f)
            )
        }

        // Expanding ring pulses
        val pulse = (gameTime % 2500) / 2500f
        repeat(4) { i ->
            val ringProgress = (pulse + i * 0.25f) % 1f
            val radius = 80f + ringProgress * 180f
            drawScope.drawCircle(
                color = baseColor.copy(alpha = 0.3f * (1f - ringProgress)),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3f - ringProgress * 2f)
            )
        }

        // Ferrous particle attraction
        repeat(3) { i ->
            val pa = (gameTime / 300f + i * 2.09f) % 6.28f
            val pd = 10f + sin(gameTime / 200f + i) * 5f
            drawScope.drawCircle(
                color = SciFiWhite.copy(alpha = 0.2f * (sin(gameTime / 100f + i) * 0.5f + 0.5f)),
                radius = 1.5f,
                center = Offset(cx + kotlin.math.cos(pa) * pd, cy + kotlin.math.sin(pa) * pd)
            )
        }
    }

    private fun drawConveyorPlatform(drawScope: DrawScope, platform: Platform, py: Float, zone: AltitudeZone, gameTime: Long) {
        val px = platform.x
        val width = platform.width
        val pulse = sin(gameTime / 150f) * 0.1f + 0.9f

        // Dark metallic base
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF1A1A1A),
                    Color(0xFF2A2A2A),
                    Color(0xFF1A1A1A)
                )
            ),
            topLeft = Offset(px, py),
            size = Size(width, PLATFORM_HEIGHT)
        )

        // Industrial warning stripes on edges
        val warnCount = 4
        val warnSpacing = 6f
        val warnAlpha = 0.25f + (sin(gameTime / 200f) * 0.1f + 0.1f)
        // Left edge
        repeat(warnCount) { i ->
            if (i % 2 == 0) {
                drawScope.drawRect(
                    SciFiRed.copy(alpha = warnAlpha),
                    Offset(px, py + i * warnSpacing),
                    Size(4f, warnSpacing)
                )
            }
        }
        // Right edge
        repeat(warnCount) { i ->
            if (i % 2 == 0) {
                drawScope.drawRect(
                    SciFiRed.copy(alpha = warnAlpha),
                    Offset(px + width - 4f, py + i * warnSpacing),
                    Size(4f, warnSpacing)
                )
            }
        }

        // Roller segments (animated horizontal bars)
        val rollerSpacing = 18f
        val rollerOffset = (gameTime / 8f) % rollerSpacing
        repeat((width / rollerSpacing).toInt() + 2) { i ->
            val rx = px + i * rollerSpacing - rollerOffset
            drawScope.drawRect(
                color = Color(0xFF3A3A3A).copy(alpha = 0.6f),
                topLeft = Offset(rx, py + 2f),
                size = Size(3f, PLATFORM_HEIGHT - 4f)
            )
            drawScope.drawRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(rx + 1f, py + 2f),
                size = Size(1f, PLATFORM_HEIGHT - 4f)
            )
        }

        // Moving belt diagonal lines
        drawScope.clipRect(left = px + 6f, top = py + 2f, right = px + width - 6f, bottom = py + PLATFORM_HEIGHT - 2f) {
            val spacing = 20f
            val offset = (gameTime / 10f) % spacing
            repeat((width / spacing).toInt() + 2) { i ->
                val lineX = px + i * spacing - offset
                val beltPath = Path().apply {
                    moveTo(lineX, py + 2f)
                    lineTo(lineX + 8f, py + PLATFORM_HEIGHT - 2f)
                }
                drawScope.drawPath(
                    path = beltPath,
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(width = 2f)
                )
            }
        }

        // Gear teeth at ends
        val gearPulse = sin(gameTime / 80f) * 0.2f + 0.8f
        drawScope.drawRect(
            color = SciFiRed.copy(alpha = 0.1f * gearPulse),
            topLeft = Offset(px, py),
            size = Size(4f, PLATFORM_HEIGHT)
        )
        drawScope.drawRect(
            color = SciFiRed.copy(alpha = 0.1f * gearPulse),
            topLeft = Offset(px + width - 4f, py),
            size = Size(4f, PLATFORM_HEIGHT)
        )

        // Speed-matched glow (simulated)
        val speedGlow = (sin(gameTime / 100f) * 0.3f + 0.7f) * pulse
        drawScope.drawRect(
            color = Color(0xFFFF6D00).copy(alpha = 0.08f * speedGlow),
            topLeft = Offset(px, py),
            size = Size(width, PLATFORM_HEIGHT),
            style = Stroke(width = 1f)
        )
    }

    private fun drawMimicPlatform(drawScope: DrawScope, x: Float, y: Float, width: Float, zone: AltitudeZone, gameTime: Long) {
        val isGlitching = (gameTime / 1000) % 5 == 0L
        val baseColor = adjustForZone(SciFiWhite.copy(alpha = 0.5f), zone)

        drawCore(drawScope, x, y, width, baseColor, zone, gameTime)
        drawZoneEmissiveGlow(drawScope, x, y, width, zone, zoneTint(zone), gameTime)

        if (isGlitching) {
            val glitchDuration = gameTime % 100

            // Reality-tear fracture
            if (glitchDuration < 60) {
                val tearAlpha = (1f - glitchDuration / 60f) * 0.5f
                repeat(3) { i ->
                    val tearPath = Path().apply {
                        val tx = x + (width / 4f) * (i + 1)
                        moveTo(tx, y)
                        lineTo(tx + sin(gameTime / 50f + i) * 20f, y + PLATFORM_HEIGHT * 0.5f)
                        lineTo(tx + cos(gameTime / 30f + i) * 15f, y + PLATFORM_HEIGHT)
                        close()
                    }
                    drawScope.drawPath(
                        path = tearPath,
                        color = Color.Black.copy(alpha = tearAlpha)
                    )
                    drawScope.drawPath(
                        path = tearPath,
                        color = SciFiRed.copy(alpha = tearAlpha * 0.5f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // Static interference bars
            if (glitchDuration < 40) {
                val staticAlpha = (1f - glitchDuration / 40f) * 0.3f
                repeat(4) { i ->
                    val sy = y + (Random(gameTime + i).nextFloat() * PLATFORM_HEIGHT)
                    val sh = 2f + Random(gameTime + i + 10).nextFloat() * 4f
                    drawScope.drawRect(
                        color = SciFiWhite.copy(alpha = staticAlpha),
                        topLeft = Offset(x, sy),
                        size = Size(width, sh)
                    )
                }
            }

            // Red glitch flash
            if (glitchDuration < 50) {
                val flashAlpha = (1f - glitchDuration / 50f) * 0.3f
                drawScope.drawRect(
                    color = SciFiRed.copy(alpha = flashAlpha),
                    topLeft = Offset(x, y),
                    size = Size(width, PLATFORM_HEIGHT)
                )
            }

            // Wrong-perspective shadow offset
            if (glitchDuration < 30) {
                val shadowOffset = 8f + sin(gameTime / 20f) * 6f
                drawScope.drawRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(x - shadowOffset, y + shadowOffset),
                    size = Size(width, PLATFORM_HEIGHT)
                )
            }
        } else {
            drawCornerBrackets(drawScope, x, y, width, SciFiWhite, 0.2f, 10f)
        }
    }

    private fun drawCore(drawScope: DrawScope, x: Float, y: Float, width: Float, color: Color, zone: AltitudeZone = AltitudeZone.EARTH, gameTime: Long = 0) {
        // Gradient body
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                0.0f to color.copy(alpha = color.alpha * 0.7f),
                0.1f to color,
                0.9f to color,
                1.0f to color.copy(alpha = color.alpha * 0.7f)
            ),
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT)
        )

        // Top edge line
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(x, y),
            size = Size(width, 2f)
        )

        // Side accents
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

        // Center horizontal detail line
        drawScope.drawLine(
            color = Color.Black.copy(alpha = 0.2f),
            start = Offset(x + 10f, y + PLATFORM_HEIGHT / 2f),
            end = Offset(x + width - 10f, y + PLATFORM_HEIGHT / 2f),
            strokeWidth = 1.5f
        )

        // Outer emissive border
        val glowAlpha = if (zone.ordinal >= AltitudeZone.UPPER_ATMOSPHERE.ordinal) 0.35f else 0.2f
        drawScope.drawRect(
            color = color.copy(alpha = glowAlpha),
            topLeft = Offset(x - 2f, y - 2f),
            size = Size(width + 4f, PLATFORM_HEIGHT + 4f),
            style = Stroke(width = 1f)
        )

        // Sci-Fi border
        drawScope.drawRect(
            color = SciFiBorder,
            topLeft = Offset(x, y),
            size = Size(width, PLATFORM_HEIGHT),
            style = Stroke(width = 1.5f)
        )
    }
}
