package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiOrange
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import kotlin.math.*
import kotlin.random.Random

class ZoneBackgroundRenderer {

    private val parallaxManager = ParallaxManager()
    private var assetLayersInitialized = false
    private var lastRenderedZone: AltitudeZone? = null

    // Multipliers for the 6-layer parallax system
    private val verticalMultipliers = listOf(0.02f, 0.05f, 0.12f, 0.25f, 0.45f, 0.75f)
    private val horizontalMultipliers = listOf(0.01f, 0.03f, 0.08f, 0.15f, 0.30f, 0.50f)

    // Performance Optimization: Pre-allocated noise for Singularity
    private val noiseRandom = Random(999)
    private val noisePoints = List(60) { Offset(noiseRandom.nextFloat(), noiseRandom.nextFloat()) }
    private val noiseSizes = List(60) { Size(4f + noiseRandom.nextFloat() * 8f, 2f + noiseRandom.nextFloat() * 4f) }
    private val noiseAlphas = List(60) { noiseRandom.nextFloat() * 0.08f }

    private val fragRandom = Random(1001)
    private val fragPoints = List(4) { Offset(fragRandom.nextFloat(), fragRandom.nextFloat()) }
    private val fragSizes = List(4) { 20f + fragRandom.nextFloat() * 30f }
    private val fragAlphas = List(4) { 0.2f + fragRandom.nextFloat() * 0.15f }

    // Aurora Path Caching
    private val auroraBands = List(5) { Path() }
    private var lastAuroraWidth = 0f
    private var lastAuroraHeight = 0f
    private var lastAuroraGameTimeStep = -1L

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

    private fun ensureAssetLayers(context: android.content.Context) {
        if (assetLayersInitialized) return
        
        AltitudeZone.entries.forEach { zone ->
            // Clear default procedural layers (stars, silhouettes, etc.)
            // so they don't overlap with the new asset packs.
            parallaxManager.clearLayers(zone)

            // Use specific multipliers for Earth (Zone 1) from the design pack
            val vSpeeds = if (zone == AltitudeZone.EARTH) {
                listOf(0.02f, 0.05f, 0.25f, 0.45f, 0.75f, 0.95f)
            } else {
                verticalMultipliers
            }

            val hSpeeds = if (zone == AltitudeZone.EARTH) {
                listOf(0.01f, 0.03f, 0.15f, 0.30f, 0.50f, 0.70f)
            } else {
                horizontalMultipliers
            }

            // Start from Layer 2 (index 1) as Layer 1 is the procedural gradient
            for (i in 1..5) {
                val resId = getAssetForZoneLayer(context, zone, i + 1)
                val bitmap = AssetManager.getBitmap(context, resId)
                
                parallaxManager.registerLayer(zone, BitmapParallaxLayer(
                    parallaxFactor = vSpeeds[i],
                    horizontalSpeedMultiplier = hSpeeds[i],
                    zIndex = -20 + i,
                    bitmap = bitmap
                ))
            }
        }
        assetLayersInitialized = true
    }

    private fun getAssetForZoneLayer(context: android.content.Context, zone: AltitudeZone, layerIndex: Int): Int {
        val zonePrefix = when(zone) {
            AltitudeZone.EARTH -> "bg_z1"
            AltitudeZone.CLOUD_LAYER -> "bg_z2"
            AltitudeZone.UPPER_ATMOSPHERE -> "bg_z3"
            AltitudeZone.ORBIT -> "bg_z4"
            AltitudeZone.THE_FOUNDRY -> "bg_z5"
            AltitudeZone.DEEP_SPACE -> "bg_z6"
            AltitudeZone.CHRONO_RIFT -> "bg_z7"
            AltitudeZone.VOID -> "bg_z8"
            AltitudeZone.THE_BEYOND -> "bg_z9"
            AltitudeZone.STELLAR_GATE -> "bg_z10"
            AltitudeZone.ANCIENT_CONSTRUCT -> "bg_z11"
            AltitudeZone.SINGULARITY -> "bg_z12"
        }

        // Specific suffixes from the Earth pack design
        val suffix = when(layerIndex) {
            1 -> "sky"
            2 -> "mountains"
            3 -> "clouds_mid"
            4 -> "islands"
            5 -> "clouds_near"
            6 -> "particles"
            else -> "layer_$layerIndex"
        }

        val resName = "${zonePrefix}_layer_${layerIndex}_$suffix"
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        
        // Fallback to game_icon if the specific asset is not yet added to the project
        return if (resId != 0) resId else R.drawable.game_icon
    }

    private fun setupEarthLayers() {
        // 1. Distant Morning Bloom
        parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = -5,
            renderElement = { opacity, gameTime ->
                drawRect(
                    brush = Brush.verticalGradient(
                        0.7f to Color.Transparent,
                        1.0f to Color.White.copy(alpha = 0.15f * opacity)
                    ),
                    size = size
                )
            }
        ))

        // 2. Rugged Distant Mountains (Depth: 0.02)
        val mountainPathPoints = listOf(
            Offset(0f, 0.82f), Offset(0.08f, 0.78f), Offset(0.15f, 0.72f),
            Offset(0.22f, 0.76f), Offset(0.30f, 0.81f), Offset(0.38f, 0.74f),
            Offset(0.45f, 0.78f), Offset(0.52f, 0.68f), Offset(0.60f, 0.76f),
            Offset(0.68f, 0.82f), Offset(0.78f, 0.65f), Offset(0.85f, 0.77f),
            Offset(0.92f, 0.72f), Offset(0.98f, 0.78f), Offset(1f, 0.82f)
        )
        
        parallaxManager.registerLayer(AltitudeZone.EARTH, SilhouetteParallaxLayer(
            parallaxFactor = 0.02f,
            zIndex = 1,
            brush = Brush.linearGradient(
                0.0f to Color(0xFF78909C).copy(alpha = 0.5f),
                1.0f to Color(0xFF455A64).copy(alpha = 0.5f),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            ),
            pathPoints = mountainPathPoints,
            baseHeightPercent = 0.82f
        ))

        // 2.1 Ice Caps (Secondary pass on mountains)
        if (!DevConfig.QUALITY_LOW_END) {
            parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
                parallaxFactor = 0.02f,
                zIndex = 2,
                renderElement = { opacity, gameTime ->
                    val w = size.width
                    val h = size.height
                    mountainPathPoints.filter { it.y < 0.75f }.forEach { p ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.8f * opacity), Color.Transparent),
                                center = Offset(p.x * w, h * p.y),
                                radius = 15f
                            ),
                            radius = 15f, center = Offset(p.x * w, h * p.y)
                        )
                    }
                }
            ))
        }

        // 3. Flying Plane silhouette (Very slow horizontal drift)
        parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
            parallaxFactor = 0.04f,
            zIndex = 2,
            renderElement = { opacity, gameTime ->
                val planeX = (gameTime * 0.015f) % (size.width + 1000f) - 500f
                val planeY = size.height * 0.45f
                val planeColor = Color(0xFFECEFF1).copy(alpha = 0.3f * opacity)
                
                // Wing
                drawRect(planeColor, Offset(planeX - 15f, planeY), Size(30f, 4f))
                // Body
                drawRect(planeColor, Offset(planeX - 25f, planeY - 2f), Size(50f, 8f))
                // Tail
                drawRect(planeColor, Offset(planeX - 25f, planeY - 10f), Size(6f, 10f))
            }
        ))

        // 4. Distant City Skyline (Depth: 0.05)
        parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
            parallaxFactor = 0.05f,
            zIndex = 3,
            renderElement = { opacity, gameTime ->
                val w = size.width
                val h = size.height
                val baseY = h * 0.88f
                
                val cityLayout = listOf(
                    Triple(0.04f, 35f, 50f), Triple(0.08f, 40f, 90f), Triple(0.12f, 30f, 60f),
                    Triple(0.25f, 50f, 110f), Triple(0.32f, 45f, 80f), Triple(0.38f, 40f, 65f),
                    Triple(0.55f, 55f, 100f), Triple(0.62f, 40f, 120f), Triple(0.68f, 35f, 55f),
                    Triple(0.85f, 50f, 90f), Triple(0.92f, 30f, 70f)
                )
                
                cityLayout.forEachIndexed { i, (xPct, bW, bH) ->
                    val bx = w * xPct
                    val by = baseY - bH
                    val buildingColor = when (i % 3) {
                        0 -> Color(0xFF37474F) // Slate
                        1 -> Color(0xFF4E342E) // Brick
                        else -> Color(0xFF263238) // Steel
                    }.copy(alpha = 0.95f * opacity)
                    
                    // Main Building
                    drawRect(color = buildingColor, topLeft = Offset(bx, by), size = Size(bW, bH + 2000f))
                    
                    // Roof Detail (Antenna/Box)
                    if (bH > 80f) {
                        drawRect(buildingColor, Offset(bx + bW * 0.3f, by - 15f), Size(4f, 15f))
                        // Pulsing Aviation Light
                        val pulse = (sin(gameTime / 400f) * 0.5f + 0.5f)
                        drawCircle(Color.Red.copy(alpha = pulse * opacity), radius = 2.5f, center = Offset(bx + bW * 0.3f, by - 15f))
                    }
                    
                    // Windows
                    if (!DevConfig.QUALITY_LOW_END) {
                        val rows = (bH / 20f).toInt().coerceAtMost(6)
                        val cols = (bW / 15f).toInt().coerceAtMost(2)
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                if ((bx.toInt() + r + c) % 5 == 0) {
                                    drawRect(
                                        color = Color(0xFFFFD54F).copy(alpha = 0.25f * opacity),
                                        topLeft = Offset(bx + 8f + c * 14f, by + 15f + r * 20f),
                                        size = Size(3f, 4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ))

        // 5. Rural Hills (Depth: 0.15)
        parallaxManager.registerLayer(AltitudeZone.EARTH, SilhouetteParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 4,
            brush = Brush.verticalGradient(
                0.85f to Color(0xFF388E3C),
                1.0f to Color(0xFF1B5E20)
            ),
            pathPoints = listOf(
                Offset(0f, 0.93f), Offset(0.15f, 0.90f), Offset(0.3f, 0.85f),
                Offset(0.45f, 0.92f), Offset(0.6f, 0.95f), Offset(0.75f, 0.89f),
                Offset(0.88f, 0.94f), Offset(1f, 0.93f)
            ),
            baseHeightPercent = 0.93f
        ))

        // 5.1 Homesteads (Tiny rural life)
        if (!DevConfig.QUALITY_LOW_END) {
            parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
                parallaxFactor = 0.15f,
                zIndex = 4,
                renderElement = { opacity, gameTime ->
                    val w = size.width
                    val h = size.height
                    val baseY = h * 0.93f
                    listOf(0.2f, 0.55f, 0.85f).forEach { xPct ->
                        val bx = w * xPct
                        val houseColor = Color(0xFF5D4037).copy(alpha = 0.8f * opacity)
                        drawRect(houseColor, Offset(bx, baseY - 8f), Size(12f, 8f))
                        val roofPath = Path().apply {
                            moveTo(bx - 2f, baseY - 8f)
                            lineTo(bx + 6f, baseY - 14f)
                            lineTo(bx + 14f, baseY - 8f)
                            close()
                        }
                        drawPath(roofPath, Color(0xFF3E2723).copy(alpha = 0.9f * opacity))
                    }
                }
            ))
        }

        // 6. Road Traffic (Depth: 0.30)
        parallaxManager.registerLayer(AltitudeZone.EARTH, SingleObjectParallaxLayer(
            parallaxFactor = 0.30f,
            zIndex = 5,
            renderElement = { opacity, gameTime ->
                val roadY = size.height * 0.97f
                drawRect(color = Color(0xFF1A1A1A).copy(alpha = opacity), topLeft = Offset(0f, roadY), size = Size(size.width, 1000f))
                
                if (!DevConfig.QUALITY_LOW_END) {
                    val dashW = 30f; val dashG = 60f
                    val dashOff = (gameTime * 0.04f) % (dashW + dashG)
                    var x = -dashOff
                    while (x < size.width) {
                        drawRect(color = Color.White.copy(alpha = 0.12f * opacity), topLeft = Offset(x, roadY + 8f), size = Size(dashW, 1.2f))
                        x += dashW + dashG
                    }
                }
                
                repeat(if (DevConfig.QUALITY_LOW_END) 2 else 4) { i ->
                    val speed = 0.10f + (i * 0.05f)
                    val dir = if (i % 2 == 0) 1f else -1f
                    val laneY = if (dir > 0) 6f else 14f
                    
                    val cx = (gameTime * speed * dir + (i * 300f)) % (size.width + 200f)
                    val fx = if (cx < -100f) cx + size.width + 200f else cx
                    val color = if (dir > 0) Color(0xFFFFECB3) else Color(0xFFFF8A80)
                    
                    if (!DevConfig.QUALITY_LOW_END) {
                        // Bloom
                        drawCircle(
                            brush = Brush.radialGradient(listOf(color.copy(alpha = 0.3f * opacity), Color.Transparent), center = Offset(fx, roadY + laneY), radius = 8f),
                            radius = 8f, center = Offset(fx, roadY + laneY)
                        )
                    }
                    // Core
                    drawCircle(color = color.copy(alpha = 0.7f * opacity), radius = 2f, center = Offset(fx, roadY + laneY))
                }
            }
        ))

        // 7. Morning Air Particles
        parallaxManager.registerLayer(AltitudeZone.EARTH, RepeatingParallaxLayer(
            parallaxFactor = 0.50f,
            zIndex = 6,
            density = 3,
            seed = 999,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 3000f + random.nextInt(100)) * 20f
                drawCircle(color = Color.White.copy(alpha = 0.06f * opacity), radius = 1.5f + random.nextFloat() * 1.5f, center = Offset(x + drift, y))
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
                
                // Fluffy Cloud (Volumetric Pass)
                val c = Color(0xFF4A148C).copy(alpha = 0.20f * opacity)
                drawCircle(
                    brush = Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(cx, cy), radius = baseR),
                    radius = baseR, center = Offset(cx, cy)
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(cx - baseR * 0.4f, cy + baseR * 0.1f), radius = baseR * 0.7f),
                    radius = baseR * 0.7f, center = Offset(cx - baseR * 0.4f, cy + baseR * 0.1f)
                )
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
                val c = Color(0xFF6A1B9A).copy(alpha = 0.30f * opacity)
                
                // Cloud Body
                drawCircle(
                    brush = Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(cx, cy), radius = baseR),
                    radius = baseR, center = Offset(cx, cy)
                )
                
                // Internal Lightning Flash
                if (random.nextFloat() > 0.98f && (gameTime / 100) % 5 == 0L) {
                    drawCircle(Color.White.copy(alpha = 0.15f * opacity), radius = baseR * 0.8f, center = Offset(cx, cy))
                }
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
                val c = Color(0xFF8E24AA).copy(alpha = 0.40f * opacity)
                drawCircle(
                    brush = Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(cx, cy), radius = baseR),
                    radius = baseR, center = Offset(cx, cy)
                )
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

                    // Iridescent Dust in Upper Atmos
                    if (zone == AltitudeZone.UPPER_ATMOSPHERE && random.nextFloat() > 0.92f) {
                        val seedIdx = random.nextInt(100)
                        val dustPulse = (sin(gameTime / 400f + seedIdx) * 0.5f + 0.5f) * opacity
                        drawCircle(
                            color = if (seedIdx % 2 == 0) SciFiCyan.copy(alpha = dustPulse * 0.4f) else SciFiPurple.copy(alpha = dustPulse * 0.4f),
                            radius = 1.5f + random.nextFloat() * 2f,
                            center = Offset(x, y)
                        )
                    }

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
                    
                    // Textured Nebula pass
                    val colorBase = if (random.nextBoolean()) Color(0xFF4A00E0) else Color(0xFF8E2DE2)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorBase.copy(alpha = nebOpacity * opacity),
                                colorBase.copy(alpha = nebOpacity * 0.4f * opacity),
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

        // Orbit Curve (Sunlight Rim pass)
        parallaxManager.registerLayer(AltitudeZone.ORBIT, SingleObjectParallaxLayer(
            parallaxFactor = 0.02f,
            zIndex = -2,
            renderElement = { opacity, gameTime ->
                val glowAlpha = (sin(gameTime / 1000f) * 0.05f + 0.15f) * opacity
                
                // Rim Light
                drawCircle(
                    color = Color.White.copy(alpha = glowAlpha),
                    radius = size.width * 2f + 10f,
                    center = Offset(size.width / 2, size.height + size.width * 1.8f),
                    style = Stroke(width = 15f)
                )
                
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
        // Industrial machinery silhouettes (rim lit pass)
        parallaxManager.registerLayer(AltitudeZone.THE_FOUNDRY, RepeatingParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 1,
            density = 3,
            seed = 606,
            renderElement = { x, y, opacity, random, gameTime ->
                val w = 80f + random.nextFloat() * 120f
                val h = 200f + random.nextFloat() * 300f
                val col = Color(0xFF1A0A00).copy(alpha = 0.7f * opacity)
                
                // Main Shape
                drawRect(col, topLeft = Offset(x - w / 2, y - h / 2), size = Size(w, h))
                
                // Forge Glow (Internal)
                if (random.nextFloat() > 0.5f) {
                    val pulse = (sin(gameTime / 600f + random.nextInt(100)) * 0.3f + 0.7f)
                    drawRect(
                        color = SciFiOrange.copy(alpha = 0.4f * opacity * pulse),
                        topLeft = Offset(x - w / 4, y - h / 4),
                        size = Size(w / 2, h / 2)
                    )
                }
                
                val armW = w * 0.15f
                drawRect(col, topLeft = Offset(x - w / 2 - armW, y - h / 4), size = Size(armW, h / 2))
                drawRect(col, topLeft = Offset(x + w / 2, y - h / 4), size = Size(armW, h / 2))
            }
        ))

        // Spark particles (Heat Haze pass)
        parallaxManager.registerLayer(AltitudeZone.THE_FOUNDRY, RepeatingParallaxLayer(
            parallaxFactor = 0.3f,
            zIndex = 2,
            density = 5,
            seed = 707,
            renderElement = { x, y, opacity, random, gameTime ->
                val hazeX = x + sin(gameTime / 400f + y / 50f) * 20f // Horizontal displacement
                val sparkAlpha = (sin(gameTime / 100f + random.nextInt(100)) * 0.5f + 0.5f) * opacity
                drawCircle(
                    color = Color(0xFFFF6D00).copy(alpha = sparkAlpha * 0.6f),
                    radius = 2f + random.nextFloat() * 3f,
                    center = Offset(hazeX + (gameTime % 5000) / 50f, y)
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
        // Quantum Blur background
        parallaxManager.registerLayer(AltitudeZone.THE_BEYOND, RepeatingParallaxLayer(
            parallaxFactor = 0.05f,
            zIndex = -1,
            density = 4,
            seed = 808,
            renderElement = { x, y, opacity, random, gameTime ->
                val radius = 300f + random.nextFloat() * 500f
                val pulse = sin(gameTime / 2000f + random.nextFloat()) * 0.2f + 1f
                
                // Motion Blurred pass (drawn twice)
                repeat(2) { i ->
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.15f * opacity / (i+1)),
                                Color(0xFFD500F9).copy(alpha = 0.1f * opacity / (i+1)),
                                Color.Transparent
                            ),
                            center = Offset(x + (i * 20f * sin(gameTime/500f)), y),
                            radius = radius * pulse
                        ),
                        radius = radius * pulse,
                        center = Offset(x + (i * 20f * sin(gameTime/500f)), y)
                    )
                }
            }
        ))

        // Energy Streams (flow pass)
        parallaxManager.registerLayer(AltitudeZone.THE_BEYOND, RepeatingParallaxLayer(
            parallaxFactor = 0.3f,
            zIndex = 2,
            density = 8,
            seed = 810,
            renderElement = { x, y, opacity, random, gameTime ->
                val driftX = (gameTime / 2000f * 200f + x) % size.width
                val streamAlpha = (sin(gameTime / 800f + random.nextFloat() * 6f) * 0.3f + 0.5f) * opacity
                
                // Pulsing Energy Core
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = streamAlpha * 0.6f),
                    radius = 3f + random.nextFloat() * 2f,
                    center = Offset(driftX, y)
                )
                // Flow Trail
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = streamAlpha * 0.2f),
                    start = Offset(driftX - 40f, y),
                    end = Offset(driftX, y),
                    strokeWidth = 2f
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
            brush = SolidColor(Color.Black.copy(alpha = 0.6f)),
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

        // P2: Rotating gate arm silhouettes (rim lit pass)
        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, RepeatingParallaxLayer(
            parallaxFactor = 0.1f,
            zIndex = 3,
            density = 3,
            seed = 910,
            renderElement = { x, y, opacity, random, gameTime ->
                val armAngle = (gameTime / 5000f * 360f + random.nextFloat() * 180f) % 360f
                val armLen = 80f + random.nextFloat() * 40f
                val armW = 14f
                val cx = x
                val cy = y
                val rad = armAngle * (kotlin.math.PI.toFloat() / 180f)
                val ex = cx + cos(rad) * armLen
                val ey = cy + sin(rad) * armLen
                
                // Main Arm
                drawLine(
                    color = Color.Black.copy(alpha = 0.7f * opacity),
                    start = Offset(cx, cy),
                    end = Offset(ex, ey),
                    strokeWidth = armW
                )
                // Prismatic Rim
                drawLine(
                    color = Color(0xFFFFD700).copy(alpha = 0.3f * opacity),
                    start = Offset(cx, cy),
                    end = Offset(ex, ey),
                    strokeWidth = 2f
                )
            }
        ))

        // P2: Holographic Grid pass
        parallaxManager.registerLayer(AltitudeZone.STELLAR_GATE, SingleObjectParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = -3,
            renderElement = { opacity, gameTime ->
                val gridAlpha = (sin(gameTime / 1500f) * 0.05f + 0.1f) * opacity
                val spacing = 80f
                for (i in 0..(size.width / spacing).toInt()) {
                    drawLine(SciFiCyan.copy(alpha = gridAlpha), Offset(i * spacing, 0f), Offset(i * spacing, size.height), strokeWidth = 1f)
                }
                for (j in 0..(size.height / spacing).toInt()) {
                    drawLine(SciFiCyan.copy(alpha = gridAlpha), Offset(0f, j * spacing), Offset(size.width, j * spacing), strokeWidth = 1f)
                }
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
        // P3: Distant block structures (rim lit pass)
        parallaxManager.registerLayer(AltitudeZone.ANCIENT_CONSTRUCT, RepeatingParallaxLayer(
            parallaxFactor = 0.1f,
            zIndex = 1,
            density = 5,
            seed = 1010,
            renderElement = { x, y, opacity, random, gameTime ->
                val s = 150f + random.nextFloat() * 200f
                
                // Black Monolith
                drawRect(
                    color = Color.Black.copy(alpha = 0.9f * opacity),
                    topLeft = Offset(x - s / 2, y - s / 2),
                    size = Size(s, s)
                )
                // Cyan Circuit Line
                val lineY = y - s / 2 + (gameTime / 10f % s)
                drawRect(
                    color = SciFiCyan.copy(alpha = 0.4f * opacity),
                    topLeft = Offset(x - s / 2, lineY),
                    size = Size(s, 2f)
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
        // Time-dilation visual distortion (Pinch pass)
        parallaxManager.registerLayer(AltitudeZone.CHRONO_RIFT, RepeatingParallaxLayer(
            parallaxFactor = 0.0f,
            zIndex = 3,
            density = 2,
            seed = 1111,
            renderElement = { x, y, opacity, random, gameTime ->
                val phase = (gameTime / 2000f + random.nextFloat() * 6.28f) % 6.28f
                val waveX = x + sin(gameTime / 500f + y / 100f) * 50f
                val waveY = y + cos(gameTime / 600f + x / 80f) * 50f
                
                // Ripples with prismatic rim
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.12f * opacity),
                    radius = 60f + phase * 30f,
                    center = Offset(waveX, waveY),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color(0xFFD500F9).copy(alpha = 0.06f * opacity),
                    radius = 50f + phase * 30f,
                    center = Offset(waveX, waveY),
                    style = Stroke(width = 1f)
                )
            }
        ))

        // Ghost echoes (Previous silhouettes)
        parallaxManager.registerLayer(AltitudeZone.CHRONO_RIFT, RepeatingParallaxLayer(
            parallaxFactor = 0.15f,
            zIndex = 2,
            density = 3,
            seed = 1212,
            renderElement = { x, y, opacity, random, gameTime ->
                val ghostAlpha = (sin(gameTime / 300f + random.nextInt(100)) * 0.3f + 0.3f) * opacity
                val ghostW = 30f + random.nextFloat() * 40f
                val ghostH = 50f + random.nextFloat() * 60f
                
                // Rendered offset to simulate time trail
                repeat(2) { i ->
                    drawRect(
                        color = Color(0xFFCE93D8).copy(alpha = ghostAlpha * 0.1f / (i+1)),
                        topLeft = Offset(x - ghostW / 2 + (i * 10f), y - ghostH / 2 + (i * 5f)),
                        size = Size(ghostW, ghostH)
                    )
                }
            }
        ))
    }

    fun render(
        drawScope: DrawScope,
        altitude: Int,
        currentZone: AltitudeZone,
        cameraY: Float,
        gameTime: Long,
        context: android.content.Context? = null
    ) {
        with(drawScope) {
            val width = size.width
            val height = size.height
            if (width <= 1f || height <= 1f) return

            if (DevConfig.RENDER_MODE_ASSETS && context != null) {
                ensureAssetLayers(context)
                
                // Memory Management: Flush asset cache on major zone change
                if (lastRenderedZone != null && lastRenderedZone != currentZone) {
                    AssetManager.clearCache()
                }
                lastRenderedZone = currentZone
            }

            val progress = calculateZoneProgress(altitude, currentZone)

            // Background Gradients
            when (currentZone) {
                AltitudeZone.EARTH -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF0288D1), // Deep Sky Blue
                        middleStart = Color(0xFF4FC3F7), // Saturated Sky
                        bottomStart = Color(0xFF81D4FA), // Light Blue Horizon (No white)
                        topEnd = Color(0xFF0D47A1),
                        middleEnd = Color(0xFF1976D2),
                        bottomEnd = Color(0xFF42A5F5)
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
                    // P4: Composed white-noise background (Event Horizon pass)
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxDim = maxOf(size.width, size.height)
                    
                    // Event Horizon Halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to Color.Black,
                            0.3f to Color.DarkGray,
                            1.0f to Color.White,
                            center = Offset(cx, cy),
                            radius = maxDim * 0.8f
                        ),
                        radius = maxDim * 0.8f, center = Offset(cx, cy)
                    )
                    
                    // Static noise overlay (subtle grid) - OPTIMIZED: Using pre-calculated points
                    repeat(60) { i ->
                        val pt = noisePoints[i]
                        val nx = pt.x * size.width
                        val ny = (pt.y * size.height + gameTime / 200f) % size.height
                        drawRect(
                            color = Color(0xFF808080).copy(alpha = noiseAlphas[i]),
                            topLeft = Offset(nx, ny),
                            size = noiseSizes[i]
                        )
                    }
                    
                    // Geometric fragment debris (Reality Shatter pass)
                    repeat(4) { i ->
                        val pt = fragPoints[i]
                        val fx = (pt.x * size.width * 0.8f + size.width * 0.1f + sin(gameTime / 2000f + i * 2f) * 50f)
                        val fy = (pt.y * size.height * 0.6f + size.height * 0.1f + cos(gameTime / 2500f + i * 3f) * 30f)
                        val fSize = fragSizes[i]
                        
                        // Shatter Rotation
                        rotate(gameTime / 10f + i * 90f, pivot = Offset(fx, fy)) {
                            drawRect(
                                color = Color.White.copy(alpha = fragAlphas[i]),
                                topLeft = Offset(fx - fSize / 2, fy - fSize / 2),
                                size = Size(fSize, fSize * 0.3f)
                            )
                        }
                    }
                    // Intense radial center glow
                    val glowPulse = sin(gameTime / 800f) * 0.1f + 0.9f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.5f * glowPulse), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = 150f
                        ),
                        radius = 150f,
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

            // Transition Mist (Cross-fade between zones)
            if (progress > 0.01f && progress < 0.99f) {
                val mistAlpha = (0.3f * sin(progress * PI.toFloat())).coerceIn(0f, 1f)
                drawRect(
                    color = Color.White.copy(alpha = mistAlpha),
                    size = size
                )
            }

            if (currentZone == AltitudeZone.UPPER_ATMOSPHERE) {
                drawAtmosphericDust(this, width, height, gameTime, progress)
            }
        }
    }

    private fun drawAurora(drawScope: DrawScope, width: Float, height: Float, gameTime: Long, zoneProgress: Float) {
        val auroraAlpha = 0.08f * (1f - zoneProgress)
        if (auroraAlpha <= 0.001f) return

        // Optimization: Only update paths if resolution or time step changes significantly
        val currentTimeStep = gameTime / 16L // Roughly every frame at 60fps
        val needsUpdate = width != lastAuroraWidth || height != lastAuroraHeight || currentTimeStep != lastAuroraGameTimeStep
        
        if (needsUpdate) {
            lastAuroraWidth = width
            lastAuroraHeight = height
            lastAuroraGameTimeStep = currentTimeStep

            var bandY = height * 0.15f
            for (b in 0 until 5) {
                val path = auroraBands[b]
                path.reset()
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
                bandY += bandHeight * 0.6f
            }
        }

        drawScope.apply {
            for (b in 0 until 5) {
                val alpha = auroraAlpha * (1f - b.toFloat() / 5)
                drawPath(auroraBands[b], Color(0xFFD500F9).copy(alpha = alpha))
                drawPath(auroraBands[b], Color(0xFF00E5FF).copy(alpha = alpha * 0.5f))
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

        // Increased window for smoother early transitions (Earth -> Cloud)
        val maxWindow = if (currentZone == AltitudeZone.EARTH) 800f else 400f
        val transitionWindow = (range * 0.4f).coerceAtMost(maxWindow)
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
