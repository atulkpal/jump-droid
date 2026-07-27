package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.*
import kotlin.random.Random

interface ParallaxLayer {
    val parallaxFactor: Float
    val zIndex: Int

    fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long)
}

/**
 * Renders a bitmap that tiles infinitely horizontally and vertically.
 */
class BitmapParallaxLayer(
    override val parallaxFactor: Float,
    val horizontalSpeedMultiplier: Float,
    override val zIndex: Int,
    private val bitmap: ImageBitmap
) : ParallaxLayer {

    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0f) return
        val canvasWidth = drawScope.size.width
        val canvasHeight = drawScope.size.height
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // Calculate offsets based on camera and time
        // Vertical offset is tied to camera altitude (negated to move background down as player moves up)
        val offsetY = (-cameraY * parallaxFactor) % bitmapHeight
        // Horizontal offset is tied to game time for a constant "wind" or player movement effect
        val offsetX = (-gameTime * horizontalSpeedMultiplier * 0.1f) % bitmapWidth

        // Normalize offsets to positive
        val startX = if (offsetX < 0) offsetX + bitmapWidth else offsetX
        val startY = if (offsetY < 0) offsetY + bitmapHeight else offsetY

        // Tile horizontally and vertically to cover the screen
        var currentY = -startY
        while (currentY < canvasHeight) {
            var currentX = -startX
            while (currentX < canvasWidth) {
                drawScope.drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(currentX.toInt(), currentY.toInt()),
                    dstSize = IntSize(bitmapWidth, bitmapHeight),
                    alpha = opacity
                )
                currentX += bitmapWidth
            }
            currentY += bitmapHeight
        }
    }
}

class RepeatingParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val density: Int,
    private val seed: Int,
    private val renderElement: DrawScope.(x: Float, y: Float, opacity: Float, random: Random, gameTime: Long) -> Unit
) : ParallaxLayer {

    var densityMultiplier: Float = 1.0f

    private var cachedPoints: List<Offset>? = null
    private var lastCachedSize: androidx.compose.ui.geometry.Size? = null

    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0f) return
        val width = drawScope.size.width
        val height = drawScope.size.height
        val effectiveDensity = (density * densityMultiplier).toInt().coerceAtLeast(1)

        // Optimization: Cache points to avoid Random calls in hot loop
        if (cachedPoints == null || lastCachedSize != drawScope.size || cachedPoints!!.size != effectiveDensity) {
            val random = Random(seed)
            cachedPoints = List(effectiveDensity) {
                Offset(random.nextFloat() * width, random.nextFloat() * height)
            }
            lastCachedSize = drawScope.size
        }

        val randomForTwinkle = Random(seed) // Still need a random for twinkle/flicker if elements use it

        cachedPoints?.forEach { point ->
            // Negated cameraY: ensure background moves DOWN as player moves UP
            val virtualY = (point.y + (-cameraY * parallaxFactor)) % height
            val finalY = if (virtualY < 0) virtualY + height else virtualY

            if (point.x.isFinite() && finalY.isFinite()) {
                drawScope.renderElement(point.x, finalY, opacity, randomForTwinkle, gameTime)
            }
        }
    }
}

class SilhouetteParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val brush: Brush,
    private val pathPoints: List<Offset>,
    private val baseHeightPercent: Float
) : ParallaxLayer {

    private var cachedPath: androidx.compose.ui.graphics.Path? = null
    private var lastCachedSize: androidx.compose.ui.geometry.Size? = null

    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0.001f) return
        val width = drawScope.size.width
        val height = drawScope.size.height

        // Optimization: Cache Path object to avoid re-calculating path every frame
        if (cachedPath == null || lastCachedSize != drawScope.size) {
            cachedPath = androidx.compose.ui.graphics.Path().apply {
                val floorY = height + 5000f
                moveTo(0f, floorY)
                lineTo(0f, height * baseHeightPercent)
                pathPoints.forEach { p ->
                    lineTo(p.x * width, height * (baseHeightPercent - (baseHeightPercent - p.y)))
                }
                lineTo(width, height * baseHeightPercent)
                lineTo(width, floorY)
                close()
            }
            lastCachedSize = drawScope.size
        }

        // NEGATED cameraY: As camera goes UP (-Y), background moves DOWN (+Y)
        drawScope.translate(top = -cameraY * parallaxFactor) {
            cachedPath?.let { drawScope.drawPath(it, brush, alpha = opacity) }
        }
    }
}

class HazeParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val brush: Brush
) : ParallaxLayer {
    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0.001f) return
        val w = drawScope.size.width
        val h = drawScope.size.height
        // Over-draw vertically in both directions to prevent gaps. Negated cameraY.
        drawScope.translate(top = -cameraY * parallaxFactor) {
            drawScope.drawRect(
                brush = brush,
                topLeft = Offset(0f, -5000f),
                size = androidx.compose.ui.geometry.Size(w, h + 10000f),
                alpha = opacity
            )
        }
    }
}

class SingleObjectParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val renderElement: DrawScope.(opacity: Float, gameTime: Long) -> Unit
) : ParallaxLayer {
    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0f) return
        // Negated cameraY: Background moves DOWN as player moves UP
        drawScope.translate(top = -cameraY * parallaxFactor) {
            renderElement(opacity, gameTime)
        }
    }
}

class ParallaxManager {
    private val layersByZone = mutableMapOf<AltitudeZone, MutableList<ParallaxLayer>>()
    private var densityMultiplier = 1.0f
    private var lastFrameGameTime = 0L
    private var stableFrames = 0

    fun registerLayer(zone: AltitudeZone, layer: ParallaxLayer) {
        val list = layersByZone.getOrPut(zone) { mutableListOf() }
        list.add(layer)
        list.sortBy { it.zIndex }
    }

    /**
     * Clears all registered layers for a specific zone.
     * Useful when switching from procedural to asset-based sets.
     */
    fun clearLayers(zone: AltitudeZone) {
        layersByZone[zone]?.clear()
    }

    fun render(drawScope: DrawScope, cameraY: Float, currentZone: AltitudeZone, zoneProgress: Float, gameTime: Long) {
        val delta = if (lastFrameGameTime > 0) gameTime - lastFrameGameTime else 0L
        lastFrameGameTime = gameTime

        if (delta > 33L) {
            densityMultiplier = (densityMultiplier - 0.1f).coerceIn(0.3f, 1.0f)
            stableFrames = 0
        } else if (delta < 16L) {
            stableFrames++
            if (stableFrames > 30) {
                densityMultiplier = (densityMultiplier + 0.05f).coerceIn(0.3f, 1.0f)
            }
        } else {
            stableFrames = 0
        }

        applyDensityMultiplier(layersByZone[currentZone])
        layersByZone[currentZone]?.forEach { layer ->
            if (1f - zoneProgress > 0.001f) {
                layer.render(drawScope, cameraY, 1f - zoneProgress, gameTime)
            }
        }

        if (zoneProgress > 0.001f) {
            val nextZoneOrdinal = currentZone.ordinal + 1
            if (nextZoneOrdinal < AltitudeZone.entries.size) {
                val nextZone = AltitudeZone.entries[nextZoneOrdinal]
                applyDensityMultiplier(layersByZone[nextZone])
                layersByZone[nextZone]?.forEach { layer ->
                    layer.render(drawScope, cameraY, zoneProgress, gameTime)
                }
            }
        }
    }

    private fun applyDensityMultiplier(layers: List<ParallaxLayer>?) {
        layers?.forEach { layer ->
            (layer as? RepeatingParallaxLayer)?.densityMultiplier = densityMultiplier
        }
    }
}
