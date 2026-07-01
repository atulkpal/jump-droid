package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*
import kotlin.random.Random

class ZoneBackgroundRenderer {

    private val parallaxManager = ParallaxManager()

    init {
        setupEarthLayers()
        setupCloudLayers()
        setupSpaceLayers()
        setupFoundryLayers()
        setupBeyondLayers()
        setupGateLayers()
        setupConstructLayers()
        setupChronoRiftLayers()
    }

    private fun setupEarthLayers() {
        // Stars (night sky)
        parallaxManager.registerLayer(AltitudeZone.EARTH, RepeatingParallaxLayer(
            parallaxFactor = 0.03f,
            zIndex = 0,
            density = 30,
            seed = 999,
            renderElement = { x, y, opacity, random, gameTime ->
                val twinkle = (sin(gameTime / 600f + random.nextInt(100)) * 0.4f + 0.6f)
                val brightness = (0.3f + random.nextFloat() * 0.7f) * opacity * twinkle
                drawCircle(
                    color = Color.White.copy(alpha = brightness),
                    radius = 0.8f + random.nextFloat() * 1.5f,
                    center = Offset(x, y)
                )
            }
        ))

        parallaxManager.registerLayer(AltitudeZone.EARTH, SilhouetteParallaxLayer(
            parallaxFactor = 0.10f,
            zIndex = 1,
            color = Color(0xFF0D0020).copy(alpha = 0.6f),
            pathPoints = listOf(
                Offset(0f, 0.85f), Offset(0.25f, 0.7f), Offset(0.5f, 0.9f),
                Offset(0.75f, 0.65f), Offset(1f, 0.95f)
            ),
            baseHeightPercent = 0.85f
        ))

        parallaxManager.registerLayer(AltitudeZone.EARTH, SilhouetteParallaxLayer(
            parallaxFactor = 0.25f,
            zIndex = 2,
            color = Color(0xFF0A1A0A).copy(alpha = 0.5f),
            pathPoints = listOf(
                Offset(0f, 0.92f), Offset(0.2f, 0.88f), Offset(0.4f, 0.95f),
                Offset(0.6f, 0.9f), Offset(0.8f, 0.94f), Offset(1f, 0.9f)
            ),
            baseHeightPercent = 0.92f
        ))

        parallaxManager.registerLayer(AltitudeZone.EARTH, RepeatingParallaxLayer(
            parallaxFactor = 0.45f,
            zIndex = 3,
            density = 4,
            seed = 42,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 2000f + random.nextInt(100)) * 50f
                drawCircle(
                    color = Color(0xFF90CAF9).copy(alpha = 0.10f * opacity),
                    radius = 100f + random.nextFloat() * 150f,
                    center = Offset(x + drift, y)
                )
            }
        ))

        parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = 5,
            renderElement = { opacity, gameTime ->
                val fade = (1f - opacity).coerceIn(0f, 1f)
                val moonAlpha = (1f - fade).coerceIn(0f, 1f) * 0.5f
                // Moon glow
                drawCircle(
                    color = Color(0xFFB0BEC5).copy(alpha = moonAlpha * 0.2f),
                    radius = 80f,
                    center = Offset(size.width - 100f, 80f)
                )
                // Moon body
                drawCircle(
                    color = Color(0xFFE0E0E0).copy(alpha = moonAlpha),
                    radius = 30f,
                    center = Offset(size.width - 100f, 80f)
                )
                // Moon crater detail
                drawCircle(
                    color = Color(0xFF9E9E9E).copy(alpha = moonAlpha * 0.5f),
                    radius = 6f,
                    center = Offset(size.width - 110f, 75f)
                )
                drawCircle(
                    color = Color(0xFF9E9E9E).copy(alpha = moonAlpha * 0.4f),
                    radius = 4f,
                    center = Offset(size.width - 90f, 85f)
                )
            }
        ))
    }

    private fun setupCloudLayers() {
        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.2f,
            zIndex = 1,
            density = 6,
            seed = 101,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 3000f + random.nextInt(100)) * 30f
                val cx = x + drift
                val cy = y
                val baseR = 150f + random.nextFloat() * 200f
                val c = Color(0xFF4A148C).copy(alpha = 0.25f * opacity)
                drawOval(c, Offset(cx - baseR * 0.6f, cy - baseR * 0.2f), Size(baseR * 1.2f, baseR * 0.8f))
                drawOval(c, Offset(cx - baseR * 0.8f, cy - baseR * 0.3f), Size(baseR * 0.8f, baseR * 0.7f))
                drawOval(c, Offset(cx + baseR * 0.4f, cy - baseR * 0.25f), Size(baseR * 0.9f, baseR * 0.75f))
                drawOval(c, Offset(cx - baseR * 0.1f, cy - baseR * 0.5f), Size(baseR * 0.7f, baseR * 0.6f))
            }
        ))

        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.4f,
            zIndex = 2,
            density = 8,
            seed = 202,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 2500f + random.nextInt(100)) * 40f
                val cx = x + drift
                val cy = y
                val baseR = 100f + random.nextFloat() * 150f
                val c = Color(0xFF6A1B9A).copy(alpha = 0.35f * opacity)
                drawOval(c, Offset(cx - baseR * 0.6f, cy - baseR * 0.2f), Size(baseR * 1.2f, baseR * 0.8f))
                drawOval(c, Offset(cx - baseR * 0.8f, cy - baseR * 0.3f), Size(baseR * 0.8f, baseR * 0.7f))
                drawOval(c, Offset(cx + baseR * 0.4f, cy - baseR * 0.25f), Size(baseR * 0.9f, baseR * 0.75f))
                drawOval(c, Offset(cx - baseR * 0.1f, cy - baseR * 0.5f), Size(baseR * 0.7f, baseR * 0.6f))
            }
        ))

        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.6f,
            zIndex = 3,
            density = 5,
            seed = 303,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 2000f + random.nextInt(100)) * 60f
                val cx = x + drift
                val cy = y
                val baseR = 80f + random.nextFloat() * 100f
                val c = Color(0xFF8E24AA).copy(alpha = 0.5f * opacity)
                drawOval(c, Offset(cx - baseR * 0.6f, cy - baseR * 0.2f), Size(baseR * 1.2f, baseR * 0.8f))
                drawOval(c, Offset(cx - baseR * 0.8f, cy - baseR * 0.3f), Size(baseR * 0.8f, baseR * 0.7f))
                drawOval(c, Offset(cx + baseR * 0.4f, cy - baseR * 0.25f), Size(baseR * 0.9f, baseR * 0.75f))
                drawOval(c, Offset(cx - baseR * 0.1f, cy - baseR * 0.5f), Size(baseR * 0.7f, baseR * 0.6f))
            }
        ))

        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.75f,
            zIndex = 4,
            density = 6,
            seed = 404,
            renderElement = { x, y, opacity, random, gameTime ->
                drawLine(
                    color = Color(0xFF80DEEA).copy(alpha = 0.15f * opacity),
                    start = Offset(x, y),
                    end = Offset(x + 80f + random.nextFloat() * 120f, y),
                    strokeWidth = 1.5f
                )
            }
        ))
    }

    private fun setupSpaceLayers() {
        AltitudeZone.entries.filter { it.ordinal >= AltitudeZone.UPPER_ATMOSPHERE.ordinal }.forEach { zone ->
            val starCount = when(zone) {
                AltitudeZone.UPPER_ATMOSPHERE -> 40
                AltitudeZone.ORBIT -> 80
                AltitudeZone.DEEP_SPACE -> 100
                AltitudeZone.THE_FOUNDRY -> 60
                AltitudeZone.CHRONO_RIFT -> 25
                AltitudeZone.VOID -> 30
                else -> 0
            }

            parallaxManager.registerLayer(zone, RepeatingParallaxLayer(
                parallaxFactor = 0.05f,
                zIndex = 0,
                density = starCount,
                seed = zone.ordinal,
                renderElement = { x, y, opacity, random, gameTime ->
                    val twinkle = (sin(gameTime / 500f + random.nextInt(100)) * 0.4f + 0.6f)
                    val brightness = (0.3f + random.nextFloat() * 0.7f) * opacity * twinkle

                    val starColor = when (zone) {
                        AltitudeZone.THE_FOUNDRY -> {
                            if (random.nextFloat() > 0.6f) Color(1f, 0.5f + random.nextFloat() * 0.3f, 0f)
                            else Color.White
                        }
                        AltitudeZone.CHRONO_RIFT -> {
                            if (random.nextFloat() > 0.5f) Color(0.3f + random.nextFloat() * 0.7f, 0.5f + random.nextFloat() * 0.5f, 1f)
                            else Color.White
                        }
                        AltitudeZone.VOID -> {
                            val rTint = 0.7f + random.nextFloat() * 0.3f
                            Color(rTint, 0.3f + random.nextFloat() * 0.3f, 0.2f + random.nextFloat() * 0.2f)
                        }
                        AltitudeZone.DEEP_SPACE -> {
                            if (random.nextFloat() > 0.7f) {
                                Color(0.5f + random.nextFloat() * 0.5f, 0.5f + random.nextFloat() * 0.5f, 1f)
                            } else Color.White
                        }
                        else -> Color.White
                    }

                    drawCircle(starColor.copy(alpha = brightness), radius = 0.8f + random.nextFloat() * 1.2f, center = Offset(x, y))

                    if (zone == AltitudeZone.ORBIT && brightness > 0.6f && random.nextFloat() > 0.85f) {
                        val flareAlpha = brightness * 0.3f
                        drawLine(starColor.copy(alpha = flareAlpha), Offset(x - 6f, y), Offset(x + 6f, y), strokeWidth = 1.5f)
                        drawLine(starColor.copy(alpha = flareAlpha), Offset(x, y - 6f), Offset(x, y + 6f), strokeWidth = 1.5f)
                    }
                }
            ))
        }

        // Nebulae
        AltitudeZone.entries.filter { it.ordinal >= AltitudeZone.UPPER_ATMOSPHERE.ordinal && it.ordinal <= AltitudeZone.DEEP_SPACE.ordinal }.forEach { zone ->
            val nebOpacity = when(zone) {
                AltitudeZone.UPPER_ATMOSPHERE -> 0.05f
                AltitudeZone.ORBIT -> 0.08f
                AltitudeZone.DEEP_SPACE -> 0.1f
                else -> 0f
            }

            parallaxManager.registerLayer(zone, RepeatingParallaxLayer(
                parallaxFactor = 0.08f,
                zIndex = -1,
                density = 2,
                seed = zone.ordinal + 50,
                renderElement = { x, y, opacity, random, gameTime ->
                    val radius = 400f + random.nextFloat() * 400f
                    val pulse = sin(gameTime / 4000f + random.nextInt(1000)) * 0.1f + 1.0f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (random.nextBoolean()) Color(0xFF4A00E0).copy(alpha = nebOpacity * opacity) else Color(0xFF8E2DE2).copy(alpha = nebOpacity * 0.8f * opacity),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = radius * pulse
                        ),
                        radius = radius * pulse,
                        center = Offset(x, y)
                    )
                }
            ))
        }

        // Blue nebula for Deep Space
        parallaxManager.registerLayer(AltitudeZone.DEEP_SPACE, RepeatingParallaxLayer(
            parallaxFactor = 0.06f,
            zIndex = -1,
            density = 1,
            seed = 99,
            renderElement = { x, y, opacity, random, gameTime ->
                val radius = 500f + random.nextFloat() * 300f
                val pulse = sin(gameTime / 5000f) * 0.1f + 1.0f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2196F3).copy(alpha = 0.06f * opacity),
                            Color.Transparent
                        ),
                        center = Offset(x, y),
                        radius = radius * pulse
                    ),
                    radius = radius * pulse,
                    center = Offset(x, y)
                )
            }
        ))

        // Orbit Curve
        parallaxManager.registerLayer(AltitudeZone.ORBIT, SingleObjectParallaxLayer(
            parallaxFactor = 0.02f,
            zIndex = -2,
            renderElement = { opacity, gameTime ->
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.12f * opacity),
                    radius = size.width * 2f,
                    center = Offset(size.width / 2, size.height + size.width * 1.8f)
                )
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.05f * opacity),
                    radius = size.width * 2f + 40f,
                    center = Offset(size.width / 2, size.height + size.width * 1.8f),
                    style = Stroke(width = 3f)
                )
            }
        ))

        // Distant planet in Orbit
        parallaxManager.registerLayer(AltitudeZone.ORBIT, SingleObjectParallaxLayer(
            parallaxFactor = 0.01f,
            zIndex = -1,
            renderElement = { opacity, gameTime ->
                val drift = sin(gameTime / 8000f) * 100f
                drawCircle(
                    color = Color(0xFF78909C).copy(alpha = 0.3f * opacity),
                    radius = 25f,
                    center = Offset(size.width * 0.8f + drift, size.height * 0.2f)
                )
            }
        ))

        // Golden debris glow in Orbit
        parallaxManager.registerLayer(AltitudeZone.ORBIT, RepeatingParallaxLayer(
            parallaxFactor = 0.03f,
            zIndex = -1,
            density = 8,
            seed = 505,
            renderElement = { x, y, opacity, random, gameTime ->
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.08f * opacity),
                    radius = 2f + random.nextFloat() * 3f,
                    center = Offset(x, y)
                )
            }
        ))

        // Derelict structures in Deep Space
        parallaxManager.registerLayer(AltitudeZone.DEEP_SPACE, RepeatingParallaxLayer(
            parallaxFactor = 0.02f,
            zIndex = 1,
            density = 3,
            seed = 606,
            renderElement = { x, y, opacity, random, gameTime ->
                val w = 40f + random.nextFloat() * 60f
                val h = 15f + random.nextFloat() * 25f
                val col = Color(0xFF455A64).copy(alpha = 0.2f * opacity)
                drawRect(col, topLeft = Offset(x - w / 2, y - h / 2), size = androidx.compose.ui.geometry.Size(w, h))
                val armW = w * 0.3f
                drawRect(col, topLeft = Offset(x - w / 2 - armW, y - h / 4), size = androidx.compose.ui.geometry.Size(armW, h / 2))
                drawRect(col, topLeft = Offset(x + w / 2, y - h / 4), size = androidx.compose.ui.geometry.Size(armW, h / 2))
            }
        ))

        // Galaxy swirl in Deep Space
        parallaxManager.registerLayer(AltitudeZone.DEEP_SPACE, SingleObjectParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = -2,
            renderElement = { opacity, gameTime ->
                val cx = size.width * 0.15f
                val cy = size.height * 0.1f
                val pulse = sin(gameTime / 6000f) * 0.1f + 1.0f
                val gAlpha = 0.04f * opacity * pulse
                drawCircle(Color(0xFFE0E0FF).copy(alpha = gAlpha), radius = 15f, center = Offset(cx, cy))
                drawCircle(Color(0xFFE0E0FF).copy(alpha = gAlpha * 0.5f), radius = 25f, center = Offset(cx, cy), style = Stroke(width = 2f))
                drawCircle(Color(0xFFE0E0FF).copy(alpha = gAlpha * 0.3f), radius = 35f, center = Offset(cx, cy), style = Stroke(width = 1f))
            }
        ))

        // Void distortion ripples
        parallaxManager.registerLayer(AltitudeZone.VOID, RepeatingParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = 1,
            density = 3,
            seed = 707,
            renderElement = { x, y, opacity, random, gameTime ->
                val phase = (gameTime / 3000f + random.nextFloat() * 6.28f) % 6.28f
                val rippleRadius = 30f + phase * 20f
                drawCircle(
                    color = Color(0xFFFF4444).copy(alpha = 0.03f * opacity),
                    radius = rippleRadius,
                    center = Offset(x, y),
                    style = Stroke(width = 1f)
                )
            }
        ))
    }

    private fun setupFoundryLayers() {
        // Industrial machinery silhouettes
        parallaxManager.registerLayer(AltitudeZone.THE_FOUNDRY, RepeatingParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 1,
            density = 3,
            seed = 606,
            renderElement = { x, y, opacity, random, gameTime ->
                val w = 80f + random.nextFloat() * 120f
                val h = 200f + random.nextFloat() * 300f
                val col = Color(0xFF1A0A00).copy(alpha = 0.7f * opacity)
                drawRect(col, topLeft = Offset(x - w / 2, y - h / 2), size = Size(w, h))
                val armW = w * 0.15f
                drawRect(col, topLeft = Offset(x - w / 2 - armW, y - h / 4), size = Size(armW, h / 2))
                drawRect(col, topLeft = Offset(x + w / 2, y - h / 4), size = Size(armW, h / 2))
            }
        ))

        // Spark particles
        parallaxManager.registerLayer(AltitudeZone.THE_FOUNDRY, RepeatingParallaxLayer(
            parallaxFactor = 0.3f,
            zIndex = 2,
            density = 5,
            seed = 707,
            renderElement = { x, y, opacity, random, gameTime ->
                val sparkAlpha = (sin(gameTime / 100f + random.nextInt(100)) * 0.5f + 0.5f) * opacity
                drawCircle(
                    color = Color(0xFFFF6D00).copy(alpha = sparkAlpha * 0.6f),
                    radius = 2f + random.nextFloat() * 3f,
                    center = Offset(x + (gameTime % 5000) / 50f, y)
                )
            }
        ))

        // Laser grid lines
        parallaxManager.registerLayer(AltitudeZone.THE_FOUNDRY, RepeatingParallaxLayer(
            parallaxFactor = 0.5f,
            zIndex = 3,
            density = 2,
            seed = 808,
            renderElement = { x, y, opacity, random, gameTime ->
                val beamAlpha = (sin(gameTime / 200f + x / 100f) * 0.3f + 0.3f) * opacity
                drawRect(
                    color = Color(0xFFFF1744).copy(alpha = beamAlpha * 0.3f),
                    topLeft = Offset(x, 0f),
                    size = Size(2f, size.height)
                )
            }
        ))
    }

    private fun setupBeyondLayers() {
        parallaxManager.registerLayer(AltitudeZone.THE_BEYOND, RepeatingParallaxLayer(
            parallaxFactor = 0.05f,
            zIndex = -1,
            density = 4,
            seed = 808,
            renderElement = { x, y, opacity, random, gameTime ->
                val radius = 300f + random.nextFloat() * 500f
                val pulse = sin(gameTime / 2000f + random.nextFloat()) * 0.2f + 1f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.15f * opacity),
                            Color(0xFFD500F9).copy(alpha = 0.1f * opacity),
                            Color.Transparent
                        ),
                        center = Offset(x, y),
                        radius = radius * pulse
                    ),
                    radius = radius * pulse,
                    center = Offset(x, y)
                )
            }
        ))

        // P1: Floating platform silhouettes
        parallaxManager.registerLayer(AltitudeZone.THE_BEYOND, RepeatingParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 1,
            density = 4,
            seed = 809,
            renderElement = { x, y, opacity, random, gameTime ->
                val w = 80f + random.nextFloat() * 100f
                val h = 12f + random.nextFloat() * 8f
                val col = Color(0xFF0A0020).copy(alpha = 0.6f * opacity)
                drawRect(col, topLeft = Offset(x - w / 2, y - h / 2), size = Size(w, h))
                drawRect(col.copy(alpha = 0.15f * opacity), topLeft = Offset(x - w / 2, y - h / 2 - 2f), size = Size(w, 2f))
            }
        ))

        // P1: Cyan energy stream particles
        parallaxManager.registerLayer(AltitudeZone.THE_BEYOND, RepeatingParallaxLayer(
            parallaxFactor = 0.3f,
            zIndex = 2,
            density = 8,
            seed = 810,
            renderElement = { x, y, opacity, random, gameTime ->
                val driftX = (gameTime / 2000f * 100f + x) % size.width
                val streamAlpha = (sin(gameTime / 800f + random.nextFloat() * 6f) * 0.3f + 0.5f) * opacity
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = streamAlpha * 0.4f),
                    radius = 2f + random.nextFloat() * 2f,
                    center = Offset(driftX, y)
                )
            }
        ))

        // P1: Distant ring structure
        parallaxManager.registerLayer(AltitudeZone.THE_BEYOND, SingleObjectParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = -2,
            renderElement = { opacity, gameTime ->
                val cx = size.width * 0.5f
                val cy = size.height * 0.2f
                val ringPulse = sin(gameTime / 3000f) * 0.15f + 0.85f
                val ringAlpha = 0.15f * opacity * ringPulse
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = ringAlpha),
                    radius = 120f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color(0xFFD500F9).copy(alpha = ringAlpha * 0.4f),
                    radius = 130f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )
            }
        ))
    }

    private fun setupGateLayers() {
        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, SilhouetteParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 1,
            color = Color.Black.copy(alpha = 0.6f),
            pathPoints = listOf(
                Offset(0f, 0.4f), Offset(0.3f, 0.4f), Offset(0.3f, 0.1f),
                Offset(0.7f, 0.1f), Offset(0.7f, 0.4f), Offset(1f, 0.4f)
            ),
            baseHeightPercent = 0.1f
        ))

        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, RepeatingParallaxLayer(
            parallaxFactor = 0.3f,
            zIndex = 2,
            density = 3,
            seed = 909,
            renderElement = { x, y, opacity, random, gameTime ->
                val w = 200f
                val h = 400f
                drawRect(
                    color = Color(0xFFFFD700).copy(alpha = 0.1f * opacity),
                    topLeft = Offset(x - w / 2, y - h / 2),
                    size = Size(w, h),
                    style = Stroke(width = 2f)
                )
            }
        ))

        // P2: Rotating gate arm silhouettes
        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, RepeatingParallaxLayer(
            parallaxFactor = 0.1f,
            zIndex = 3,
            density = 3,
            seed = 910,
            renderElement = { x, y, opacity, random, gameTime ->
                val armAngle = (gameTime / 5000f * 360f + random.nextFloat() * 180f) % 360f
                val armLen = 60f + random.nextFloat() * 40f
                val armW = 12f
                val cx = x
                val cy = y
                val rad = armAngle * (kotlin.math.PI.toFloat() / 180f)
                val ex = cx + cos(rad) * armLen
                val ey = cy + sin(rad) * armLen
                drawLine(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f * opacity),
                    start = Offset(cx, cy),
                    end = Offset(ex, ey),
                    strokeWidth = armW
                )
            }
        ))

        // P2: Star concentration near gate
        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, RepeatingParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = -1,
            density = 40,
            seed = 911,
            renderElement = { x, y, opacity, random, gameTime ->
                val twinkle = (sin(gameTime / 400f + random.nextFloat() * 6f) * 0.3f + 0.7f)
                val brightness = 0.4f * opacity * twinkle
                drawCircle(Color.White.copy(alpha = brightness), radius = 1f + random.nextFloat(), center = Offset(x, y))
            }
        ))

        // P2: Golden particle stream through gate
        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, RepeatingParallaxLayer(
            parallaxFactor = 0.2f,
            zIndex = 4,
            density = 6,
            seed = 912,
            renderElement = { x, y, opacity, random, gameTime ->
                val driftY = (gameTime / 3000f * 200f + y) % size.height
                val driftX = x + sin(gameTime / 1000f + y / 50f) * 30f
                val pAlpha = (sin(gameTime / 600f + random.nextFloat() * 6f) * 0.3f + 0.5f) * opacity
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = pAlpha * 0.3f),
                    radius = 2f + random.nextFloat() * 2f,
                    center = Offset(driftX, driftY)
                )
            }
        ))
    }

    private fun setupConstructLayers() {
        // P3: Distant block structures on horizon
        parallaxManager.registerLayer(AltitudeZone.ANCIENT_CONSTRUCT, RepeatingParallaxLayer(
            parallaxFactor = 0.05f,
            zIndex = 0,
            density = 2,
            seed = 1011,
            renderElement = { x, y, opacity, random, gameTime ->
                val w = 200f + random.nextFloat() * 200f
                val h = 40f + random.nextFloat() * 60f
                drawRect(
                    color = Color(0xFF0A0A0A).copy(alpha = 0.5f * opacity),
                    topLeft = Offset(x - w / 2, y - h / 2),
                    size = Size(w, h)
                )
            }
        ))

        parallaxManager.registerLayer(AltitudeZone.ANCIENT_CONSTRUCT, RepeatingParallaxLayer(
            parallaxFactor = 0.1f,
            zIndex = 1,
            density = 5,
            seed = 1010,
            renderElement = { x, y, opacity, random, gameTime ->
                val size = 150f + random.nextFloat() * 200f
                drawRect(
                    color = Color.Black.copy(alpha = 0.8f * opacity),
                    topLeft = Offset(x - size / 2, y - size / 2),
                    size = Size(size, size)
                )
            }
        ))

        // P3: Floating monolith silhouettes
        parallaxManager.registerLayer(AltitudeZone.ANCIENT_CONSTRUCT, RepeatingParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 2,
            density = 3,
            seed = 1012,
            renderElement = { x, y, opacity, random, gameTime ->
                val mw = 30f + random.nextFloat() * 20f
                val mh = 80f + random.nextFloat() * 100f
                val col = Color(0xFF1A1A2E).copy(alpha = 0.7f * opacity)
                drawRect(col, topLeft = Offset(x - mw / 2, y - mh / 2), size = Size(mw, mh))
                drawRect(Color(0xFF00E5FF).copy(alpha = 0.1f * opacity), topLeft = Offset(x - mw / 2, y - mh / 2), size = Size(2f, mh))
            }
        ))

        // P3: Green energy motes
        parallaxManager.registerLayer(AltitudeZone.ANCIENT_CONSTRUCT, RepeatingParallaxLayer(
            parallaxFactor = 0.3f,
            zIndex = 3,
            density = 8,
            seed = 1013,
            renderElement = { x, y, opacity, random, gameTime ->
                val moteAlpha = (sin(gameTime / 700f + random.nextFloat() * 6f) * 0.3f + 0.5f) * opacity
                drawCircle(
                    color = Color(0xFF69F0AE).copy(alpha = moteAlpha * 0.3f),
                    radius = 2f + random.nextFloat() * 2f,
                    center = Offset(x + sin(gameTime / 1500f + random.nextFloat()) * 20f, y)
                )
            }
        ))
    }

    private fun setupChronoRiftLayers() {
        // Time-dilation visual distortion
        parallaxManager.registerLayer(AltitudeZone.CHRONO_RIFT, RepeatingParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = 3,
            density = 2,
            seed = 1111,
            renderElement = { x, y, opacity, random, gameTime ->
                val phase = (gameTime / 2000f + random.nextFloat() * 6.28f) % 6.28f
                val waveX = x + sin(gameTime / 500f + y / 100f) * 50f
                val waveY = y + cos(gameTime / 600f + x / 80f) * 50f
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.08f * opacity),
                    radius = 60f + phase * 30f,
                    center = Offset(waveX, waveY),
                    style = Stroke(width = 1f)
                )
            }
        ))

        // Ghost echoes
        parallaxManager.registerLayer(AltitudeZone.CHRONO_RIFT, RepeatingParallaxLayer(
            parallaxFactor = 0.2f,
            zIndex = 2,
            density = 2,
            seed = 1212,
            renderElement = { x, y, opacity, random, gameTime ->
                val ghostAlpha = (sin(gameTime / 300f + random.nextInt(100)) * 0.3f + 0.3f) * opacity
                val ghostW = 30f + random.nextFloat() * 40f
                val ghostH = 50f + random.nextFloat() * 60f
                drawRect(
                    color = Color(0xFFCE93D8).copy(alpha = ghostAlpha * 0.2f),
                    topLeft = Offset(x - ghostW / 2, y - ghostH / 2),
                    size = Size(ghostW, ghostH)
                )
            }
        ))
    }

    fun render(
        drawScope: DrawScope,
        altitude: Int,
        currentZone: AltitudeZone,
        cameraY: Float,
        gameTime: Long
    ) {
        with(drawScope) {
            val width = size.width
            val height = size.height
            if (width <= 1f || height <= 1f) return

            val progress = calculateZoneProgress(altitude, currentZone)

            // Background Gradients
            when (currentZone) {
                AltitudeZone.EARTH -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF1A0033),
                        middleStart = Color(0xFFBF360C),
                        bottomStart = Color(0xFF33691E),
                        topEnd = Color(0xFF1A0033),
                        middleEnd = Color(0xFF0D001A),
                        bottomEnd = Color(0xFF0D3311)
                    )
                }
                AltitudeZone.CLOUD_LAYER -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF1A0033),
                        middleStart = Color(0xFF0D001A),
                        bottomStart = Color(0xFF1A1A3E),
                        topEnd = Color(0xFF0D001A),
                        middleEnd = Color(0xFF1A0033),
                        bottomEnd = Color(0xFF311B92)
                    )
                }
                AltitudeZone.UPPER_ATMOSPHERE -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF0D001A),
                        middleStart = Color(0xFF1A0033),
                        bottomStart = Color(0xFF311B92),
                        topEnd = Color(0xFF000411),
                        middleEnd = Color(0xFF0D001A),
                        bottomEnd = Color(0xFF1A0033)
                    )
                }
                AltitudeZone.ORBIT -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF000411),
                        middleStart = Color(0xFF0D001A),
                        bottomStart = Color(0xFF1A0033),
                        topEnd = Color.Black,
                        middleEnd = Color.Black,
                        bottomEnd = Color(0xFF0D001A)
                    )
                }
                AltitudeZone.THE_FOUNDRY -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF1A0A00),
                        middleStart = Color(0xFF3E1A00),
                        bottomStart = Color(0xFF0D001A),
                        topEnd = Color(0xFF000000),
                        middleEnd = Color(0xFF1A0000),
                        bottomEnd = Color(0xFF0D001A)
                    )
                }
                AltitudeZone.DEEP_SPACE -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color.Black,
                        middleStart = Color.Black,
                        bottomStart = Color(0xFF0D001A),
                        topEnd = Color.Black,
                        middleEnd = Color.Black,
                        bottomEnd = Color.Black
                    )
                }
                AltitudeZone.CHRONO_RIFT -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF0D001A),
                        middleStart = Color(0xFF1A0033),
                        bottomStart = Color(0xFF311B92),
                        topEnd = Color(0xFF000000),
                        middleEnd = Color(0xFF0D001A),
                        bottomEnd = Color(0xFF1A0033)
                    )
                }
                AltitudeZone.VOID -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color.Black,
                        middleStart = Color.Black,
                        bottomStart = Color.Black,
                        topEnd = Color(0xFF001219),
                        middleEnd = Color(0xFF005F73),
                        bottomEnd = Color(0xFF0A9396)
                    )
                }
                AltitudeZone.THE_BEYOND -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF001219),
                        middleStart = Color(0xFF005F73),
                        bottomStart = Color(0xFF0A9396),
                        topEnd = Color(0xFF000000),
                        middleEnd = Color(0xFF310E68),
                        bottomEnd = Color(0xFF5F0A87)
                    )
                }
                AltitudeZone.STELLAR_GATE -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF000000),
                        middleStart = Color(0xFF310E68),
                        bottomStart = Color(0xFF5F0A87),
                        topEnd = Color(0xFF1B1B1B),
                        middleEnd = Color(0xFF0D0D0D),
                        bottomEnd = Color(0xFF000000)
                    )
                }
                AltitudeZone.ANCIENT_CONSTRUCT -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF1B1B1B),
                        middleStart = Color(0xFF0D0D0D),
                        bottomStart = Color(0xFF000000),
                        topEnd = Color(0xFFFFFFFF),
                        middleEnd = Color(0xFF808080),
                        bottomEnd = Color(0xFF000000)
                    )
                }
                AltitudeZone.SINGULARITY -> {
                    // P4: Composed white-noise background
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxDim = maxOf(size.width, size.height)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFE0E0E0), Color(0xFFB0B0B0)),
                            center = Offset(cx, cy),
                            radius = maxDim * 0.8f
                        )
                    )
                    // Static noise overlay (subtle grid)
                    val noiseSeed = Random(999)
                    repeat(60) {
                        val nx = noiseSeed.nextFloat() * size.width
                        val ny = (noiseSeed.nextFloat() * size.height + gameTime / 200f) % size.height
                        drawRect(
                            color = Color(0xFF808080).copy(alpha = noiseSeed.nextFloat() * 0.08f),
                            topLeft = Offset(nx, ny),
                            size = Size(4f + noiseSeed.nextFloat() * 8f, 2f + noiseSeed.nextFloat() * 4f)
                        )
                    }
                    // Geometric fragment debris
                    val fragSeed = Random(1001)
                    repeat(4) {
                        val fx = (fragSeed.nextFloat() * size.width * 0.8f + size.width * 0.1f + sin(gameTime / 2000f + it * 2f) * 50f)
                        val fy = (fragSeed.nextFloat() * size.height * 0.6f + size.height * 0.1f + cos(gameTime / 2500f + it * 3f) * 30f)
                        val fSize = 20f + fragSeed.nextFloat() * 30f
                        val angle = (gameTime / (3000f + it * 500f) * 60f) % 360f
                        drawRect(
                            color = Color.White.copy(alpha = 0.2f + fragSeed.nextFloat() * 0.15f),
                            topLeft = Offset(fx - fSize / 2, fy - fSize / 2),
                            size = Size(fSize, fSize * 0.3f)
                        )
                    }
                    // Intense radial center glow
                    val glowPulse = sin(gameTime / 800f) * 0.1f + 0.9f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.4f * glowPulse), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = 100f
                        ),
                        radius = 100f,
                        center = Offset(cx, cy)
                    )
                }
            }

            // Zone-specific effects
            if (currentZone == AltitudeZone.CLOUD_LAYER && (gameTime % 8000) < 200) {
                val flashAlpha = if ((gameTime % 200) < 50) 0.12f else 0.0f
                drawRect(Color.White.copy(alpha = flashAlpha))
            }

            if (currentZone == AltitudeZone.UPPER_ATMOSPHERE) {
                drawAurora(this, width, height, gameTime, progress)
            }

            if (currentZone == AltitudeZone.THE_FOUNDRY) {
                val heatPulse = sin(gameTime / 2000f) * 0.02f + 0.04f
                drawRect(
                    brush = Brush.radialGradient(
                        0.5f to Color.Transparent,
                        1.0f to Color(0xFFFF6D00).copy(alpha = heatPulse)
                    )
                )
            }

            if (currentZone == AltitudeZone.CHRONO_RIFT) {
                val glitchPulse = sin(gameTime / 800f) * 0.04f + 0.06f
                drawRect(
                    color = Color(0xFF00E5FF).copy(alpha = glitchPulse)
                )
            }

            if (currentZone == AltitudeZone.VOID) {
                val pulse = sin(gameTime / 1500f) * 0.03f + 0.06f
                drawRect(
                    brush = Brush.radialGradient(
                        0.6f to Color.Transparent,
                        1.0f to Color(0xFFFF1744).copy(alpha = pulse)
                    )
                )
            }

            parallaxManager.render(this, cameraY, currentZone, progress, gameTime)

            if (currentZone == AltitudeZone.UPPER_ATMOSPHERE) {
                drawAtmosphericDust(this, width, height, gameTime, progress)
            }
        }
    }

    private fun drawAurora(drawScope: DrawScope, width: Float, height: Float, gameTime: Long, zoneProgress: Float) {
        val auroraAlpha = 0.08f * (1f - zoneProgress)
        if (auroraAlpha <= 0.001f) return

        drawScope.apply {
            val bands = 5
            var bandY = height * 0.15f
            for (b in 0 until bands) {
                val path = Path()
                val phase = gameTime / 3000f + b * 1.2f
                val freq = 0.01f + b * 0.002f
                val amp = 30f + b * 10f
                val bandHeight = 80f + b * 20f
                val steps = 20

                path.moveTo(0f, bandY)
                for (i in 0..steps) {
                    val px = (i.toFloat() / steps) * width
                    val py = bandY + sin(px * freq + phase) * amp + sin(px * freq * 2.5f + phase * 1.5f) * amp * 0.4f
                    path.lineTo(px, py)
                }
                path.lineTo(width, bandY + bandHeight)
                for (i in steps downTo 0) {
                    val px = (i.toFloat() / steps) * width
                    val py = bandY + bandHeight + sin(px * freq + phase + 1f) * amp * 0.6f + sin(px * freq * 2.5f + phase * 1.5f + 1f) * amp * 0.25f
                    path.lineTo(px, py)
                }
                path.close()

                val alpha = auroraAlpha * (1f - b.toFloat() / bands)

                drawPath(path, Color(0xFFD500F9).copy(alpha = alpha))
                drawPath(path, Color(0xFF00E5FF).copy(alpha = alpha * 0.5f))

                bandY += bandHeight * 0.6f
            }
        }
    }

    private fun drawAtmosphericDust(drawScope: DrawScope, width: Float, height: Float, gameTime: Long, zoneProgress: Float) {
        val dustAlpha = 0.15f * (1f - zoneProgress)
        if (dustAlpha <= 0.001f) return

        drawScope.apply {
            val count = 15
            val seed = Random(888)
            for (i in 0 until count) {
                val dx = seed.nextFloat() * width
                val dy = (seed.nextFloat() * height + (gameTime / 200f) * (0.5f + seed.nextFloat())) % height
                val drift = sin(gameTime / 3000f + seed.nextFloat() * 10f) * 30f
                drawCircle(
                    Color.White.copy(alpha = 0.06f * dustAlpha * (0.5f + seed.nextFloat())),
                    radius = 1f + seed.nextFloat() * 2f,
                    center = Offset(dx + drift, dy)
                )
            }
        }
    }

    fun renderTitle(drawScope: DrawScope) {
        with(drawScope) {
            val width = size.width
            val height = size.height
            if (width <= 1f || height <= 1f) return

            val time = (System.currentTimeMillis() % 100000) / 1000f

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF000411), Color(0xFF0D001A), Color(0xFF1A0033))
                )
            )

            val starRandom = Random(42)

            repeat(2) { i ->
                val nx = (starRandom.nextFloat() * width + time * 1.5f) % width
                val ny = starRandom.nextFloat() * height

                if (nx.isFinite() && ny.isFinite()) {
                    val centerOffset = Offset(nx, ny)
                    val r = 500f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if(i == 0) Color(0xFF4A00E0).copy(alpha = 0.12f) else Color(0xFF8E2DE2).copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = r
                        ),
                        radius = r,
                        center = centerOffset
                    )
                }
            }

            repeat(120) {
                val sx = (starRandom.nextFloat() * width + time * 4f) % width
                val sy = starRandom.nextFloat() * height
                val brightness = (0.15f + starRandom.nextFloat() * 0.5f).coerceIn(0f, 1f)
                drawCircle(Color.White.copy(alpha = brightness), radius = 1.1f, center = Offset(sx, sy))
            }

            val mPath = Path().apply {
                moveTo(0f, height * 0.88f)
                lineTo(width * 0.25f, height * 0.78f)
                lineTo(width * 0.5f, height * 0.92f)
                lineTo(width * 0.75f, height * 0.74f)
                lineTo(width, height * 0.95f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(mPath, Color(0xFF020005).copy(alpha = 0.8f))
        }
    }

    private fun calculateZoneProgress(altitude: Int, currentZone: AltitudeZone): Float {
        val nextZoneOrdinal = currentZone.ordinal + 1
        if (nextZoneOrdinal >= AltitudeZone.entries.size) return 0f

        val nextThreshold = AltitudeZone.entries[nextZoneOrdinal].threshold
        val range = nextThreshold - currentZone.threshold
        if (range <= 0) return 0f

        val transitionWindow = (range * 0.4f).coerceAtMost(300f)
        val transitionStart = nextThreshold - transitionWindow

        if (altitude < transitionStart) return 0f
        return ((altitude - transitionStart) / transitionWindow).coerceIn(0f, 1f)
    }

    private fun DrawScope.drawInterpolatedBackground(
        progress: Float,
        topStart: Color, middleStart: Color, bottomStart: Color,
        topEnd: Color, middleEnd: Color, bottomEnd: Color
    ) {
        val top = lerpColor(topStart, topEnd, progress)
        val middle = lerpColor(middleStart, middleEnd, progress)
        val bottom = lerpColor(bottomStart, bottomEnd, progress)

        drawRect(
            brush = Brush.verticalGradient(
                0.0f to top,
                0.5f to middle,
                1.0f to bottom
            )
        )
    }

    private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = start.red + (end.red - start.red) * f,
            green = start.green + (end.green - start.green) * f,
            blue = start.blue + (end.blue - start.blue) * f,
            alpha = start.alpha + (end.alpha - start.alpha) * f
        )
    }
}
