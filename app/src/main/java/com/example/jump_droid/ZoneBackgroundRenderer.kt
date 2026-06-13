package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*
import kotlin.random.Random

class ZoneBackgroundRenderer {

    private val parallaxManager = ParallaxManager()

    init {
        setupEarthLayers()
        setupCloudLayers()
        setupSpaceLayers()
    }

    private fun setupEarthLayers() {
        // Distant Mountains (10%)
        parallaxManager.registerLayer(AltitudeZone.EARTH, SilhouetteParallaxLayer(
            parallaxFactor = 0.10f,
            zIndex = 1,
            color = Color(0xFF1A0033).copy(alpha = 0.4f),
            pathPoints = listOf(
                Offset(0f, 0.85f), Offset(0.25f, 0.7f), Offset(0.5f, 0.9f), 
                Offset(0.75f, 0.65f), Offset(1f, 0.95f)
            ),
            baseHeightPercent = 0.85f
        ))

        // Near Hills (25%)
        parallaxManager.registerLayer(AltitudeZone.EARTH, SilhouetteParallaxLayer(
            parallaxFactor = 0.25f,
            zIndex = 2,
            color = Color(0xFF0D47A1).copy(alpha = 0.3f),
            pathPoints = listOf(
                Offset(0f, 0.92f), Offset(0.2f, 0.88f), Offset(0.4f, 0.95f), 
                Offset(0.6f, 0.9f), Offset(0.8f, 0.94f), Offset(1f, 0.9f)
            ),
            baseHeightPercent = 0.92f
        ))

        // Earth Clouds (45%)
        parallaxManager.registerLayer(AltitudeZone.EARTH, RepeatingParallaxLayer(
            parallaxFactor = 0.45f,
            zIndex = 3,
            density = 4,
            seed = 42,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 2000f + random.nextInt(100)) * 50f
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f * opacity),
                    radius = 100f + random.nextFloat() * 150f,
                    center = Offset(x + drift, y)
                )
            }
        ))
    }

    private fun setupCloudLayers() {
        // Far Clouds
        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.2f,
            zIndex = 1,
            density = 6,
            seed = 101,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 3000f + random.nextInt(100)) * 30f
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f * opacity),
                    radius = 150f + random.nextFloat() * 200f,
                    center = Offset(x + drift, y)
                )
            }
        ))

        // Medium Clouds
        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.4f,
            zIndex = 2,
            density = 8,
            seed = 202,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 2500f + random.nextInt(100)) * 40f
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f * opacity),
                    radius = 100f + random.nextFloat() * 150f,
                    center = Offset(x + drift, y)
                )
            }
        ))

        // Near Clouds
        parallaxManager.registerLayer(AltitudeZone.CLOUD_LAYER, RepeatingParallaxLayer(
            parallaxFactor = 0.6f,
            zIndex = 3,
            density = 5,
            seed = 303,
            renderElement = { x, y, opacity, random, gameTime ->
                val drift = sin(gameTime / 2000f + random.nextInt(100)) * 60f
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f * opacity),
                    radius = 80f + random.nextFloat() * 100f,
                    center = Offset(x + drift, y)
                )
            }
        ))
    }

    private fun setupSpaceLayers() {
        // Common Stars for space zones
        listOf(AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID).forEach { zone ->
            val starDensity = when(zone) {
                AltitudeZone.UPPER_ATMOSPHERE -> 40
                AltitudeZone.ORBIT -> 80
                AltitudeZone.DEEP_SPACE -> 100
                AltitudeZone.VOID -> 30
                else -> 0
            }
            
            parallaxManager.registerLayer(zone, RepeatingParallaxLayer(
                parallaxFactor = 0.05f,
                zIndex = 0,
                density = starDensity,
                seed = zone.ordinal,
                renderElement = { x, y, opacity, random, gameTime ->
                    val twinkle = (sin(gameTime / 500f + random.nextInt(100)) * 0.4f + 0.6f)
                    val brightness = (0.3f + random.nextFloat() * 0.7f) * opacity * twinkle
                    drawCircle(Color.White.copy(alpha = brightness), radius = 0.8f + random.nextFloat() * 1.2f, center = Offset(x, y))
                }
            ))

            // Nebulae for deep space
            if (zone == AltitudeZone.DEEP_SPACE || zone == AltitudeZone.ORBIT || zone == AltitudeZone.UPPER_ATMOSPHERE) {
                val nebOpacity = when(zone) {
                    AltitudeZone.UPPER_ATMOSPHERE -> 0.05f
                    AltitudeZone.ORBIT -> 0.08f
                    AltitudeZone.DEEP_SPACE -> 0.1f
                    else -> 0f
                }
                parallaxManager.registerLayer(zone, RepeatingParallaxLayer(
                    parallaxFactor = 0.08f,
                    zIndex = -1, // Behind stars
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
        }

        // Orbit Curve
        parallaxManager.registerLayer(AltitudeZone.ORBIT, SingleObjectParallaxLayer(
            parallaxFactor = 0.02f,
            zIndex = -2,
            renderElement = { opacity, gameTime ->
                drawCircle(
                    color = Color(0xFF2196F3).copy(alpha = 0.2f * opacity),
                    radius = size.width * 2f,
                    center = Offset(size.width / 2, size.height + size.width * 1.8f)
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
                        topStart = Color(0xFF1A237E),
                        middleStart = Color(0xFF2196F3),
                        bottomStart = Color(0xFFBBDEFB),
                        topEnd = Color(0xFF42A5F5),
                        middleEnd = Color(0xFF90CAF9),
                        bottomEnd = Color(0xFFE3F2FD)
                    )
                }
                AltitudeZone.CLOUD_LAYER -> {
                    drawInterpolatedBackground(
                        progress = progress,
                        topStart = Color(0xFF42A5F5),
                        middleStart = Color(0xFF90CAF9),
                        bottomStart = Color(0xFFE3F2FD),
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
                AltitudeZone.VOID -> {
                    drawRect(Color.Black)
                }
            }

            // Render Parallax Layers
            parallaxManager.render(this, cameraY, currentZone, progress, gameTime)
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
