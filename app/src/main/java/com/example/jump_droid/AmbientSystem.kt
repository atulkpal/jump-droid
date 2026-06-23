package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.sin
import kotlin.random.Random

enum class AmbientType {
    BIRD, AIRCRAFT, BALLOON, // Earth
    CLOUD_WISP, WEATHER_BALLOON, // Clouds
    RESEARCH_BALLOON, CONTRAIL, // Upper Atmos
    SATELLITE, DEBRIS, PROBE, // Orbit
    ASTEROID, DERELICT, ANCIENT_STRUCTURE, // Space
    ANOMALY, DISTORTED_SHAPE // Void
}

class AmbientObject(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val parallaxFactor: Float,
    val type: AmbientType,
    val scale: Float,
    val color: Color,
    val seed: Int = Random.nextInt()
) {
    var life = 1.0f
    var rotation = 0f
    var rotationSpeed = (Random.nextFloat() - 0.5f) * 50f

    fun update(dt: Float) {
        x += vx * dt
        y += vy * dt
        rotation += rotationSpeed * dt
    }
}

class AmbientManager {
    private val activeObjects = mutableListOf<AmbientObject>()
    private var spawnTimer = 0f
    private val random = Random(System.currentTimeMillis())

    fun update(dt: Float, cameraY: Float, screenWidth: Float, screenHeight: Float, currentZone: AltitudeZone) {
        // Update existing objects
        val iterator = activeObjects.iterator()
        while (iterator.hasNext()) {
            val obj = iterator.next()
            obj.update(dt)

            // Calculate relative Y position considering parallax
            val relativeY = obj.y - (cameraY * obj.parallaxFactor)
            
            // Remove if way off screen
            if (relativeY > screenHeight + 500f || relativeY < -1000f || 
                obj.x < -500f || obj.x > screenWidth + 500f) {
                iterator.remove()
            }
        }

        // Spawning logic
        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            // Randomize next spawn timer (objects should be rare)
            spawnTimer = 5f + random.nextFloat() * 15f 
            
            if (activeObjects.size < 10) {
                spawnAmbientObject(screenWidth, cameraY, currentZone)
            }
        }
    }

    private fun spawnAmbientObject(screenWidth: Float, cameraY: Float, zone: AltitudeZone) {
        val type = when (zone) {
            AltitudeZone.EARTH -> listOf(AmbientType.BIRD, AmbientType.AIRCRAFT, AmbientType.BALLOON).random()
            AltitudeZone.CLOUD_LAYER -> listOf(AmbientType.CLOUD_WISP, AmbientType.WEATHER_BALLOON).random()
            AltitudeZone.UPPER_ATMOSPHERE -> listOf(AmbientType.RESEARCH_BALLOON, AmbientType.CONTRAIL).random()
            AltitudeZone.ORBIT -> listOf(AmbientType.SATELLITE, AmbientType.DEBRIS, AmbientType.PROBE).random()
            AltitudeZone.DEEP_SPACE -> listOf(AmbientType.ASTEROID, AmbientType.DERELICT, AmbientType.ANCIENT_STRUCTURE).random()
            AltitudeZone.VOID -> listOf(AmbientType.ANOMALY, AmbientType.DISTORTED_SHAPE).random()
        }

        val parallax = 0.1f + random.nextFloat() * 0.4f
        val vx = (if (random.nextBoolean()) 1f else -1f) * (20f + random.nextFloat() * 40f)
        val vy = (random.nextFloat() - 0.5f) * 10f
        
        // Spawn slightly ahead of camera
        val spawnX = if (vx > 0) -100f else screenWidth + 100f
        val spawnY = (cameraY * parallax) + (random.nextFloat() * 800f) - 200f

        val ambientBase = when (zone) {
            AltitudeZone.EARTH -> Color.White
            AltitudeZone.CLOUD_LAYER -> Color(0xFF80DEEA)
            AltitudeZone.UPPER_ATMOSPHERE -> Color(0xFFCE93D8)
            AltitudeZone.ORBIT -> Color(0xFFFFD700)
            AltitudeZone.DEEP_SPACE -> Color(0xFF64B5F6)
            AltitudeZone.VOID -> Color(0xFFEF9A9A)
        }

        activeObjects.add(AmbientObject(
            x = spawnX,
            y = spawnY,
            vx = vx,
            vy = vy,
            parallaxFactor = parallax,
            type = type,
            scale = 0.5f + random.nextFloat() * 1.5f,
            color = ambientBase.copy(alpha = 0.3f + random.nextFloat() * 0.5f)
        ))
    }

    fun render(drawScope: DrawScope, cameraY: Float, gameTime: Long) {
        activeObjects.forEach { obj ->
            val relY = obj.y - (cameraY * obj.parallaxFactor)
            drawScope.translate(left = obj.x, top = relY) {
                drawAmbientType(this, obj, gameTime)
            }
        }
    }

    private fun drawAmbientType(drawScope: DrawScope, obj: AmbientObject, gameTime: Long) {
        val s = obj.scale
        val c = obj.color
        val r = obj.seed
        
        with(drawScope) {
            rotate(obj.rotation) {
                when (obj.type) {
                    AmbientType.BIRD -> {
                        val wingFold = sin(gameTime / 100f) * 10f
                        val path = Path().apply {
                            moveTo(-10f * s, 0f)
                            quadraticTo(0f, - wingFold * s, 10f * s, 0f)
                            moveTo(-10f * s, 2f * s)
                            quadraticTo(0f, 2f * s - wingFold * s, 10f * s, 2f * s)
                        }
                        drawPath(path, c, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    }
                    AmbientType.AIRCRAFT -> {
                        drawRect(c, topLeft = Offset(-15f * s, -5f * s), size = Size(30f * s, 10f * s))
                        drawRect(c, topLeft = Offset(-5f * s, -15f * s), size = Size(10f * s, 30f * s))
                    }
                    AmbientType.BALLOON -> {
                        drawCircle(c, radius = 15f * s)
                        drawLine(c, start = Offset(0f, 15f * s), end = Offset(0f, 25f * s))
                        drawRect(c, topLeft = Offset(-5f * s, 25f * s), size = Size(10f * s, 8f * s))
                    }
                    AmbientType.CLOUD_WISP -> {
                        drawCircle(c.copy(alpha = c.alpha * 0.5f), radius = 30f * s, center = Offset(0f, 0f))
                        drawCircle(c.copy(alpha = c.alpha * 0.5f), radius = 20f * s, center = Offset(20f * s, 5f * s))
                    }
                    AmbientType.WEATHER_BALLOON, AmbientType.RESEARCH_BALLOON -> {
                        drawCircle(c, radius = 12f * s)
                        drawLine(c, start = Offset(0f, 12f * s), end = Offset(0f, 20f * s))
                        drawRect(c, topLeft = Offset(-3f * s, 20f * s), size = Size(6f * s, 6f * s))
                    }
                    AmbientType.CONTRAIL -> {
                        drawLine(c.copy(alpha = 0.2f), start = Offset(-50f * s, 0f), end = Offset(50f * s, 0f), strokeWidth = 2f * s)
                    }
                    AmbientType.SATELLITE -> {
                        drawRect(c, topLeft = Offset(-8f * s, -8f * s), size = Size(16f * s, 16f * s))
                        drawRect(Color.Cyan.copy(alpha = 0.4f), topLeft = Offset(-20f * s, -4f * s), size = Size(12f * s, 8f * s))
                        drawRect(Color.Cyan.copy(alpha = 0.4f), topLeft = Offset(8f * s, -4f * s), size = Size(12f * s, 8f * s))
                    }
                    AmbientType.DEBRIS -> {
                        drawRect(c, topLeft = Offset(-4f * s, -4f * s), size = Size(8f * s, 8f * s))
                    }
                    AmbientType.PROBE -> {
                        drawCircle(c, radius = 6f * s)
                        repeat(3) { i ->
                            rotate(i * 120f) {
                                drawLine(c, start = Offset(0f, -6f * s), end = Offset(0f, -15f * s))
                            }
                        }
                    }
                    AmbientType.ASTEROID -> {
                        val path = Path().apply {
                            moveTo(10f * s, 0f)
                            lineTo(7f * s, 8f * s)
                            lineTo(-2f * s, 10f * s)
                            lineTo(-9f * s, 3f * s)
                            lineTo(-6f * s, -7f * s)
                            lineTo(4f * s, -9f * s)
                            close()
                        }
                        drawPath(path, Color.Gray.copy(alpha = c.alpha))
                    }
                    AmbientType.DERELICT, AmbientType.ANCIENT_STRUCTURE -> {
                        drawRect(c, topLeft = Offset(-20f * s, -10f * s), size = Size(40f * s, 20f * s))
                        drawRect(c, topLeft = Offset(-5f * s, -20f * s), size = Size(10f * s, 40f * s))
                    }
                    AmbientType.ANOMALY, AmbientType.DISTORTED_SHAPE -> {
                        repeat(4) { i ->
                            rotate(i * 45f + (gameTime / 200f)) {
                                drawRect(c.copy(alpha = 0.2f), topLeft = Offset(-15f * s, -15f * s), size = Size(30f * s, 30f * s))
                            }
                        }
                    }
                }
            }
        }
    }
}
