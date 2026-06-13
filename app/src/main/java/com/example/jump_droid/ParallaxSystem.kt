package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.random.Random

/**
 * Represents a single layer in the parallax system.
 */
interface ParallaxLayer {
    val parallaxFactor: Float // 0f = fixed, 1f = moves with camera
    val zIndex: Int
    
    fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long)
}

/**
 * A layer that repeats a simple shape or sprite across the screen.
 */
class RepeatingParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val density: Int,
    private val seed: Int,
    private val renderElement: DrawScope.(x: Float, y: Float, opacity: Float, random: Random, gameTime: Long) -> Unit
) : ParallaxLayer {

    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0.01f) return
        val random = Random(seed)
        val width = drawScope.size.width
        val height = drawScope.size.height
        
        repeat(density) { i ->
            val rx = random.nextFloat() * width
            val virtualY = (random.nextFloat() * height - (cameraY * parallaxFactor)) % height
            val finalY = if (virtualY < 0) virtualY + height else virtualY
            
            if (rx.isFinite() && finalY.isFinite()) {
                drawScope.renderElement(rx, finalY, opacity, random, gameTime)
            }
        }
    }
}

/**
 * A layer for static silhouettes like mountains or hills.
 */
class SilhouetteParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val color: Color,
    private val pathPoints: List<Offset>, // Normalized points (0..1)
    private val baseHeightPercent: Float
) : ParallaxLayer {
    
    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0.01f) return
        val width = drawScope.size.width
        val height = drawScope.size.height
        
        val path = androidx.compose.ui.graphics.Path().apply {
            val first = pathPoints.first()
            moveTo(first.x * width, height * baseHeightPercent)
            pathPoints.drop(1).forEach { p ->
                lineTo(p.x * width, height * (baseHeightPercent - (baseHeightPercent - p.y)))
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        drawScope.translate(top = -cameraY * parallaxFactor) {
            drawPath(path, color.copy(alpha = color.alpha * opacity))
        }
    }
}

/**
 * A layer for a single, unique element like the Sun or a planet.
 */
class SingleObjectParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val renderElement: DrawScope.(opacity: Float, gameTime: Long) -> Unit
) : ParallaxLayer {
    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0.01f) return
        drawScope.translate(top = -cameraY * parallaxFactor) {
            renderElement(opacity, gameTime)
        }
    }
}

/**
 * Manages multiple parallax layers and their transitions.
 */
class ParallaxManager {
    private val layersByZone = mutableMapOf<AltitudeZone, List<ParallaxLayer>>()

    fun registerLayer(zone: AltitudeZone, layer: ParallaxLayer) {
        val list = layersByZone.getOrPut(zone) { mutableListOf() } as MutableList
        list.add(layer)
        list.sortBy { it.zIndex }
    }

    fun render(drawScope: DrawScope, cameraY: Float, currentZone: AltitudeZone, zoneProgress: Float, gameTime: Long) {
        // Render current zone layers
        layersByZone[currentZone]?.forEach { layer ->
            layer.render(drawScope, cameraY, 1f - zoneProgress, gameTime)
        }
        
        // Render next zone layers if transitioning
        if (zoneProgress > 0f) {
            val nextZoneOrdinal = currentZone.ordinal + 1
            if (nextZoneOrdinal < AltitudeZone.entries.size) {
                val nextZone = AltitudeZone.entries[nextZoneOrdinal]
                layersByZone[nextZone]?.forEach { layer ->
                    layer.render(drawScope, cameraY, zoneProgress, gameTime)
                }
            }
        }
    }
}
