package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.random.Random

class ZoneBackgroundRenderer {

    fun render(
        drawScope: DrawScope,
        altitude: Int,
        currentZone: AltitudeZone,
        cameraY: Float
    ) {
        with(drawScope) {
            val width = size.width
            val height = size.height
            if (width <= 1f || height <= 1f) return

            val progress = calculateZoneProgress(altitude, currentZone)
            
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
                    drawEarthLayers(progress, cameraY, width, height)
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
                    drawCloudLayerLayers(progress, cameraY, width, height)
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
                    drawUpperAtmosphereLayers(progress, cameraY, width, height)
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
                    drawOrbitLayers(progress, cameraY, width, height)
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
                    drawDeepSpaceLayers(progress, cameraY, width, height)
                }
                AltitudeZone.VOID -> {
                    drawRect(Color.Black)
                    drawVoidLayers(cameraY, width, height)
                }
            }
        }
    }

    fun renderTitle(drawScope: DrawScope) {
        with(drawScope) {
            val width = size.width
            val height = size.height
            if (width <= 1f || height <= 1f) return
            
            val time = (System.currentTimeMillis() % 100000) / 1000f

            // Mysterious deep space gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF000411), Color(0xFF0D001A), Color(0xFF1A0033))
                )
            )
            
            val starRandom = Random(42)

            // Subtle Nebula layers (Restored with safety)
            repeat(2) { i ->
                val nx = (starRandom.nextFloat() * width + time * 1.5f) % width
                val ny = starRandom.nextFloat() * height
                
                // Ensure coordinates are valid and non-NaN
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
            
            // Stars field (Restored animation)
            repeat(120) {
                val sx = (starRandom.nextFloat() * width + time * 4f) % width
                val sy = starRandom.nextFloat() * height
                val brightness = (0.15f + starRandom.nextFloat() * 0.5f).coerceIn(0f, 1f)
                drawCircle(Color.White.copy(alpha = brightness), radius = 1.1f, center = Offset(sx, sy))
            }

            // Distant mountain silhouettes
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
        
        val transitionWindow = (range * 0.4f).coerceAtMost(300f) // Slightly wider transition but capped
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

    private fun DrawScope.drawEarthLayers(
        progress: Float,
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        val mountainAlpha = (0.4f * (1f - progress)).coerceIn(0f, 1f)
        if (mountainAlpha > 0.05f) {
            val mountainColor = Color(0xFF1A0033).copy(alpha = mountainAlpha)
            val mountainPath = Path().apply {
                moveTo(0f, screenHeight * 0.85f)
                lineTo(screenWidth * 0.25f, screenHeight * 0.7f)
                lineTo(screenWidth * 0.5f, screenHeight * 0.9f)
                lineTo(screenWidth * 0.75f, screenHeight * 0.65f)
                lineTo(screenWidth, screenHeight * 0.95f)
                lineTo(screenWidth, screenHeight)
                lineTo(0f, screenHeight)
                close()
            }
            translate(top = -cameraY * 0.05f) {
                drawPath(mountainPath, color = mountainColor)
            }
        }
        drawDriftingClouds(cameraY, screenWidth, screenHeight, density = 4, opacity = 0.25f * (1f - progress))
    }

    private fun DrawScope.drawCloudLayerLayers(
        progress: Float,
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.03f * (1f - progress)),
            radius = screenWidth * 1.2f,
            center = Offset(screenWidth * 0.9f, -cameraY * 0.02f)
        )
        drawDriftingClouds(cameraY, screenWidth, screenHeight, density = 12, opacity = 0.45f * (1f - progress))
        drawStars(cameraY, screenWidth, screenHeight, density = 20, alpha = progress * 0.3f)
    }

    private fun DrawScope.drawUpperAtmosphereLayers(
        progress: Float,
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        drawStars(cameraY, screenWidth, screenHeight, density = 40, alpha = 0.3f + progress * 0.4f)
        drawNebulae(cameraY, screenWidth, screenHeight, opacity = 0.05f * progress)
    }

    private fun DrawScope.drawOrbitLayers(
        progress: Float,
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        drawStars(cameraY, screenWidth, screenHeight, density = 80, alpha = 0.7f + progress * 0.3f)
        drawNebulae(cameraY, screenWidth, screenHeight, opacity = 0.05f + 0.05f * progress)
        
        // Distant Earth Curve at the bottom
        val curveAlpha = (0.2f * (1f - progress)).coerceIn(0f, 1f)
        if (curveAlpha > 0f) {
            drawCircle(
                color = Color(0xFF2196F3).copy(alpha = curveAlpha),
                radius = screenWidth * 2f,
                center = Offset(screenWidth / 2, screenHeight + screenWidth * 1.8f - cameraY * 0.05f)
            )
        }
    }

    private fun DrawScope.drawDeepSpaceLayers(
        progress: Float,
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        drawStars(cameraY, screenWidth, screenHeight, density = 100, alpha = 1.0f - progress * 0.5f)
        drawNebulae(cameraY, screenWidth, screenHeight, opacity = 0.1f * (1f - progress))
    }

    private fun DrawScope.drawVoidLayers(
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        drawStars(cameraY, screenWidth, screenHeight, density = 30, alpha = 0.3f)
    }

    private fun DrawScope.drawStars(
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float,
        density: Int,
        alpha: Float
    ) {
        val op = alpha.coerceIn(0f, 1f)
        if (op <= 0.01f) return
        
        val starRandom = Random(42)
        repeat(density) { i ->
            val parallaxFactor = 0.01f + (i * 0.005f)
            val sx = starRandom.nextFloat() * screenWidth
            val sy = (starRandom.nextFloat() * screenHeight - (cameraY * parallaxFactor)) % screenHeight
            val finalSy = if (sy < 0) sy + screenHeight else sy
            
            val brightness = (0.3f + starRandom.nextFloat() * 0.7f) * op
            if (sx.isFinite() && finalSy.isFinite()) {
                drawCircle(
                    color = Color.White.copy(alpha = brightness),
                    radius = 0.8f + starRandom.nextFloat() * 1.2f,
                    center = Offset(sx, finalSy)
                )
            }
        }
    }

    private fun DrawScope.drawNebulae(
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float,
        opacity: Float
    ) {
        val op = opacity.coerceIn(0f, 1f)
        if (op <= 0.01f) return
        
        val nebRandom = Random(123)
        repeat(2) { i ->
            val parallaxFactor = 0.02f + i * 0.01f
            val nx = nebRandom.nextFloat() * screenWidth
            val ny = (nebRandom.nextFloat() * screenHeight - (cameraY * parallaxFactor)) % screenHeight
            val finalNy = if (ny < 0) ny + screenHeight else ny
            
            if (nx.isFinite() && finalNy.isFinite()) {
                val center = Offset(nx, finalNy)
                val radius = 400f + nebRandom.nextFloat() * 400f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (i == 0) Color(0xFF4A00E0).copy(alpha = op) else Color(0xFF8E2DE2).copy(alpha = op * 0.8f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
        }
    }

    private fun DrawScope.drawDriftingClouds(
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float,
        density: Int,
        opacity: Float
    ) {
        val op = opacity.coerceIn(0f, 1f)
        if (op <= 0f) return
        val cloudRandom = Random(42)
        repeat(density) { i ->
            val parallaxFactor = 0.12f + (i * 0.03f)
            val cx = (cloudRandom.nextFloat() * screenWidth + (System.currentTimeMillis() / 250f * (i + 1) % screenWidth)) % screenWidth
            val cy = (cloudRandom.nextFloat() * screenHeight + (-cameraY * parallaxFactor)) % screenHeight
            if (cx.isFinite() && cy.isFinite()) {
                drawCircle(
                    color = Color.White.copy(alpha = op),
                    radius = 100f + cloudRandom.nextFloat() * 150f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
