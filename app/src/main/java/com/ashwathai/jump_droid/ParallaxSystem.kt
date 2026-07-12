package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.random.Random

interface ParallaxLayer {
    val parallaxFactor: Float
    val zIndex: Int

    fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long)
}

class RepeatingParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val density: Int,
    private val seed: Int,
    private val renderElement: DrawScope.(x: Float, y: Float, opacity: Float, random: Random, gameTime: Long) -> Unit
) : ParallaxLayer {

    var densityMultiplier: Float = 1.0f

    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0f) return
        val random = Random(seed)
        val width = drawScope.size.width
        val height = drawScope.size.height
        val effectiveDensity = (density * densityMultiplier).toInt().coerceAtLeast(1)

        repeat(effectiveDensity) { i ->
            val rx = random.nextFloat() * width
            val virtualY = (random.nextFloat() * height - (cameraY * parallaxFactor)) % height
            val finalY = if (virtualY < 0) virtualY + height else virtualY

            if (rx.isFinite() && finalY.isFinite()) {
                drawScope.renderElement(rx, finalY, opacity, random, gameTime)
            }
        }
    }
}

class SilhouetteParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val color: Color,
    private val pathPoints: List<Offset>,
    private val baseHeightPercent: Float
) : ParallaxLayer {

    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0f) return
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

class SingleObjectParallaxLayer(
    override val parallaxFactor: Float,
    override val zIndex: Int,
    private val renderElement: DrawScope.(opacity: Float, gameTime: Long) -> Unit
) : ParallaxLayer {
    override fun render(drawScope: DrawScope, cameraY: Float, opacity: Float, gameTime: Long) {
        if (opacity <= 0f) return
        drawScope.translate(top = -cameraY * parallaxFactor) {
            renderElement(opacity, gameTime)
        }
    }
}

class ParallaxManager {
    private val layersByZone = mutableMapOf<AltitudeZone, List<ParallaxLayer>>()
    private var densityMultiplier = 1.0f
    private var lastFrameGameTime = 0L
    private var stableFrames = 0

    fun registerLayer(zone: AltitudeZone, layer: ParallaxLayer) {
        val list = layersByZone.getOrPut(zone) { mutableListOf() } as MutableList
        list.add(layer)
        list.sortBy { it.zIndex }
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
            layer.render(drawScope, cameraY, 1f - zoneProgress, gameTime)
        }

        if (zoneProgress > 0f) {
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
