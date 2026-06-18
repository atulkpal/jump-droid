package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.jump_droid.Constants.ROCKET_HEIGHT
import com.example.jump_droid.Constants.ROCKET_WIDTH
import com.example.jump_droid.ui.theme.*
import kotlin.math.abs
import kotlin.random.Random

/**
 * Handles the visual presentation of the player's rocket.
 * Decouples physics state from rendering details.
 */
class RocketRenderer {

    fun render(
        drawScope: DrawScope,
        player: Player,
        isThrusting: Boolean,
        thrustTarget: Offset,
        cameraY: Float,
        gameTime: Long
    ) {
        val rocketX = player.x
        val rocketY = player.y - cameraY
        
        // Calculate Tilt (Banking) based on horizontal velocity
        val maxTilt = 15f
        val tilt = (player.velocityX / 400f * maxTilt).coerceIn(-maxTilt, maxTilt)

        with(drawScope) {
            translate(rocketX, rocketY) {
                
                // --- CATASTROPHIC DESTRUCTION SEQUENCE (Task 3) ---
                if (player.destructionTimer > 0) {
                    val progress = player.destructionTimer // 0..2.5s
                    
                    if (progress < 0.5f) {
                        // PHASE 1: Hull Failure Flash
                        val flashAlpha = (kotlin.math.sin(gameTime / 30f) * 0.5f + 0.5f)
                        rotate(tilt) {
                            drawRocketBody(this, player)
                            drawRect(Color.White.copy(alpha = flashAlpha), topLeft = Offset(-ROCKET_WIDTH/2, -ROCKET_HEIGHT/2), size = Size(ROCKET_WIDTH, ROCKET_HEIGHT))
                        }
                    } else if (progress < 1.5f) {
                        // PHASE 2: Catastrophic Breakup
                        val breakProgress = (progress - 0.5f) // 0..1.0
                        
                        repeat(8) { i ->
                            val r = Random(i.toLong() * 100)
                            val angle = r.nextFloat() * 360f + breakProgress * 200f
                            val dist = breakProgress * 150f * (r.nextFloat() + 0.5f)
                            
                            rotate(angle, pivot = Offset.Zero) {
                                translate(dist, 0f) {
                                    // Mechanical Debris (Hull Plates, Engine pieces)
                                    when (i % 3) {
                                        0 -> drawRect(Color.Gray, size = Size(12f, 18f)) // Hull Plate
                                        1 -> drawCircle(Color.DarkGray, radius = 6f) // Gear/Bolt
                                        2 -> drawPath(Path().apply { moveTo(0f, 0f); lineTo(10f, 15f); lineTo(-5f, 10f); close() }, Color.Black) // Fragment
                                    }
                                    
                                    // Fire/Smoke trailing from fragments
                                    if (Random.nextFloat() < 0.6f) {
                                        drawCircle(
                                            color = if (Random.nextBoolean()) SciFiRed else SciFiGold,
                                            radius = 4f + Random.nextFloat() * 4f,
                                            center = Offset(-10f, 0f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Central Explosion Core
                        val explosionAlpha = (1.5f - progress).coerceIn(0f, 1f)
                        drawCircle(Color.White.copy(alpha = explosionAlpha), radius = 40f * (1f + breakProgress))
                        drawCircle(SciFiGold.copy(alpha = explosionAlpha * 0.7f), radius = 60f * (1f + breakProgress))
                    } else {
                        // PHASE 3: Loss of Control (Tumbling wreckage)
                        val tumbleProgress = (progress - 1.5f) // 0..1.0
                        rotate(gameTime / 2f) {
                            // Draw a charred, broken version of the body
                            drawRocketBody(this, player, overrideColor = Color.DarkGray)
                            
                            // Thick Smoke Trail
                            repeat(3) { i ->
                                val r = Random(gameTime + i)
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    radius = 10f + r.nextFloat() * 15f,
                                    center = Offset((r.nextFloat() - 0.5f) * 20f, 20f + tumbleProgress * 50f + i * 15f)
                                )
                            }
                            
                            // Occasional sparks
                            if (gameTime % 200 < 50) {
                                drawCircle(SciFiRed, 3f, Offset(Random.nextFloat() * 20f - 10f, Random.nextFloat() * 40f - 20f))
                            }
                        }
                    }
                    return@translate
                }

                // Apply Squash and Stretch
                scale(scaleX = 1f / player.squashStretch, scaleY = player.squashStretch, pivot = Offset(0f, ROCKET_HEIGHT / 2)) {
                    // Rotate around the physical bottom center
                    rotate(tilt, pivot = Offset(0f, ROCKET_HEIGHT / 2)) {
                        
                        // Invulnerability blink effect
                        val isVisible = if (player.invulnerabilityTimer > 0) (gameTime / 100) % 2 == 0L else true
                        
                        if (isVisible) {
                            // 1. Thruster Effects
                            if (isThrusting && player.fuel > 0) {
                                drawThrusterFlame(this, gameTime)
                                
                                val dx = thrustTarget.x - player.x
                                if (abs(dx) > 20f) {
                                    drawSideThruster(this, dx > 0, gameTime)
                                }
                            }

                            // 2. Rocket Body
                            drawRocketBody(this, player)

                            // 3. Status Overlays
                            drawAuras(this, player, gameTime)

                            // 4. EPIC 5: Survival Layers
                            drawSurvivalLayers(this, player, gameTime)
                        }
                    }
                }
                
                // Extra Re-entry/Spawn Protection Shield
                if (player.invulnerabilityTimer > 0) {
                    drawCircle(
                        color = SciFiWhite.copy(alpha = 0.3f),
                        radius = 50f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }
        }
    }

    private fun drawThrusterFlame(drawScope: DrawScope, gameTime: Long) {
        val random = Random(gameTime / 50)
        val flicker = random.nextFloat() * 15f
        
        val flamePath = Path().apply {
            moveTo(-10f, ROCKET_HEIGHT / 2)
            quadraticTo(0f, ROCKET_HEIGHT / 2 + 40f + flicker, 10f, ROCKET_HEIGHT / 2)
            close()
        }

        drawScope.drawPath(
            path = flamePath,
            brush = Brush.verticalGradient(
                colors = listOf(SciFiGold, SciFiRed.copy(alpha = 0f)),
                startY = ROCKET_HEIGHT / 2,
                endY = ROCKET_HEIGHT / 2 + 60f
            )
        )
        
        drawScope.drawPath(Path().apply {
            moveTo(-5f, ROCKET_HEIGHT / 2)
            quadraticTo(0f, ROCKET_HEIGHT / 2 + 20f + flicker * 0.5f, 5f, ROCKET_HEIGHT / 2)
            close()
        }, SciFiWhite.copy(alpha = 0.8f))
    }

    private fun drawSideThruster(drawScope: DrawScope, isRight: Boolean, gameTime: Long) {
        val random = Random(gameTime / 30)
        val flicker = random.nextFloat() * 10f
        val side = if (isRight) -1f else 1f
        
        val path = Path().apply {
            moveTo(side * (ROCKET_WIDTH / 2 - 5f), -5f)
            lineTo(side * (ROCKET_WIDTH / 2 + 10f + flicker), 0f)
            lineTo(side * (ROCKET_WIDTH / 2 - 5f), 5f)
            close()
        }
        
        drawScope.drawPath(path, SciFiCyan.copy(alpha = 0.6f))
    }

    private fun drawRocketBody(drawScope: DrawScope, player: Player, overrideColor: Color? = null) {
        val halfW = ROCKET_WIDTH / 2
        val halfH = ROCKET_HEIGHT / 2
        
        val heatRatio = (player.heat / Constants.MAX_HEAT).coerceIn(0f, 1f)
        val bodyBaseColor = when (player.rocketType) {
            RocketType.BALANCED -> SciFiWhite
            RocketType.SCOUT -> SciFiGold
            RocketType.TANK -> Color(0xFF455A64)
            RocketType.EXPERIMENTAL -> SciFiPurple
        }
        
        val currentColor = overrideColor ?: (if (player.isOverheated) SciFiRed 
                          else lerpColor(bodyBaseColor, SciFiRed, heatRatio * 0.7f))

        with(drawScope) {
            // Main Fuselage (fits exactly in hitbox)
            drawRect(
                color = currentColor,
                topLeft = Offset(-halfW + 5f, -halfH + 15f),
                size = Size(ROCKET_WIDTH - 10f, ROCKET_HEIGHT - 15f)
            )
            
            // Cockpit
            drawCircle(SciFiCyan.copy(alpha = 0.8f), radius = 7f, center = Offset(0f, -5f))

            // Nose Cone (fits exactly in hitbox)
            val nosePath = Path().apply {
                moveTo(-halfW + 5f, -halfH + 15f)
                lineTo(0f, -halfH)
                lineTo(halfW - 5f, -halfH + 15f)
                close()
            }
            drawPath(nosePath, Color.DarkGray)

            // Fins (Stay within halfW)
            val leftFin = Path().apply {
                moveTo(-halfW + 5f, 10f)
                lineTo(-halfW, halfH)
                lineTo(-halfW + 5f, halfH)
                close()
            }
            val rightFin = Path().apply {
                moveTo(halfW - 5f, 10f)
                lineTo(halfW, halfH)
                lineTo(halfW - 5f, halfH)
                close()
            }
            drawPath(leftFin, SciFiRed)
            drawPath(rightFin, SciFiRed)
        }
    }

    private fun drawAuras(drawScope: DrawScope, player: Player, gameTime: Long) {
        val pulse = (kotlin.math.sin(gameTime / 150f) * 0.1f) + 0.9f
        if (player.turboTimer > 0) {
            drawScope.drawCircle(SciFiCyan.copy(alpha = 0.2f), radius = 55f * pulse, center = Offset.Zero)
        }
        if (player.efficiencyTimer > 0) {
            drawScope.drawCircle(SciFiGreen.copy(alpha = 0.2f), radius = 55f * pulse, center = Offset.Zero)
        }
    }

    private fun drawSurvivalLayers(drawScope: DrawScope, player: Player, gameTime: Long) {
        with(drawScope) {
            // 1. SHIELD VISUAL REDESIGN V3: Energy Armor Plates (Task 1)
            if (player.shield > 0) {
                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)
                
                // Plate Count based on strength
                val plateCount = when {
                    shieldRatio >= 0.90f -> 8
                    shieldRatio >= 0.70f -> 6
                    shieldRatio >= 0.40f -> 4
                    shieldRatio >= 0.15f -> 2
                    else -> 1
                }

                val radius = 58f
                val rotationSpeed = 0.05f
                val instability = (1f - shieldRatio) * (1f - shieldRatio) * 20f
                val pulse = (kotlin.math.sin(gameTime / 150f) * 0.1f + 0.9f)
                val flicker = if (shieldRatio < 0.25f && (gameTime / 80 % 2 == 0L)) 0.4f else 1.0f

                val plateColor = SciFiCyan.copy(alpha = (0.5f + 0.3f * shieldRatio) * flicker)
                
                repeat(plateCount) { i ->
                    val r = Random(i.toLong() * 77)
                    val baseAngle = i * (360f / plateCount) + (gameTime * rotationSpeed)
                    val floatOffset = (kotlin.math.cos(gameTime / 200f + i) * 5f)
                    
                    rotate(baseAngle, pivot = Offset.Zero) {
                        // Floating Energy Armor Plate (Task 1)
                        // These are curved rectangles that feel like armor, not a ring
                        drawArc(
                            color = plateColor,
                            startAngle = -15f + (r.nextFloat() - 0.5f) * instability,
                            sweepAngle = 30f,
                            useCenter = false,
                            topLeft = Offset(-radius - floatOffset, -radius - floatOffset),
                            size = Size((radius + floatOffset) * 2 * pulse, (radius + floatOffset) * 2 * pulse),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 8f * shieldRatio.coerceAtLeast(0.3f),
                                cap = androidx.compose.ui.graphics.StrokeCap.Butt // Sharper edges than Round
                            )
                        )
                        
                        // Internal structural detail
                        drawArc(
                            color = SciFiWhite.copy(alpha = 0.6f * flicker),
                            startAngle = -10f,
                            sweepAngle = 20f,
                            useCenter = false,
                            topLeft = Offset(-radius - floatOffset + 2f, -radius - floatOffset + 2f),
                            size = Size((radius + floatOffset - 2f) * 2 * pulse, (radius + floatOffset - 2f) * 2 * pulse),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                        )

                        // Electrical discharge for low health (25% state)
                        if (shieldRatio < 0.3f && Random.nextFloat() < 0.15f) {
                            val boltX = radius + 10f
                            drawLine(
                                color = SciFiWhite,
                                start = Offset(boltX, -5f),
                                end = Offset(boltX + 15f * Random.nextFloat(), 5f),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }

            // 2. Integrity Damage (Battle Scars)
            if (player.integrity < player.maxIntegrity) {
                val damageRatio = 1f - (player.integrity / player.maxIntegrity)
                
                // Dark scorch marks on body
                repeat((damageRatio * 4).toInt().coerceAtLeast(1)) { i ->
                    val r = Random(i.toLong() * 500)
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = 4f + r.nextFloat() * 6f,
                        center = Offset((r.nextFloat() - 0.5f) * 20f, (r.nextFloat() - 0.5f) * 40f)
                    )
                }

                // Sparks for critical damage
                if (damageRatio > 0.5f && (gameTime / 100 % 3 == 0L)) {
                    val r = Random(gameTime)
                    drawCircle(
                        color = SciFiGold,
                        radius = 2f,
                        center = Offset((r.nextFloat() - 0.5f) * 25f, (r.nextFloat() - 0.5f) * 50f)
                    )
                }
            }
        }
    }

    private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = start.alpha + (end.alpha - start.alpha) * fraction
        )
    }
}
