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
                
                // --- Destruction Sequence Rendering ---
                if (player.destructionTimer > 0) {
                    val progress = player.destructionTimer // 0..2 seconds
                    val flash = (kotlin.math.sin(gameTime / 50f) * 0.5f + 0.5f)
                    
                    if (progress < 0.5f) {
                        // Phase 1: Damage Flash
                        drawRocketBody(this, player, overrideColor = Color.White.copy(alpha = flash))
                    } else if (progress < 1.5f) {
                        // Phase 2: Breaking Apart
                        val breakProgress = (progress - 0.5f)
                        repeat(5) { i ->
                            val r = Random(i.toLong())
                            rotate(r.nextFloat() * 360f + breakProgress * 100f, pivot = Offset(0f, 0f)) {
                                translate(breakProgress * 100f * (r.nextFloat() + 0.5f), 0f) {
                                    drawRect(
                                        color = SciFiWhite.copy(alpha = (1.5f - progress)),
                                        size = Size(20f, 20f)
                                    )
                                }
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
            // 1. Shield Layer (Adjustment Run 2: Segmented Energy Cage)
            if (player.shield > 0) {
                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)
                
                // Segment Count based on strength
                val segmentCount = when {
                    shieldRatio >= 0.85f -> 8
                    shieldRatio >= 0.65f -> 6
                    shieldRatio >= 0.45f -> 4
                    shieldRatio >= 0.25f -> 2
                    else -> 1
                }
                
                val radius = 55f
                val instability = (1f - shieldRatio) * 15f
                val flicker = if (shieldRatio < 0.3f && (gameTime / 100 % 2 == 0L)) 0.3f else 1.0f
                
                val color = SciFiCyan.copy(alpha = (0.4f + 0.4f * shieldRatio) * flicker)
                
                repeat(segmentCount) { i ->
                    val r = Random(i.toLong())
                    val baseAngle = i * (360f / segmentCount) + (gameTime / 20f)
                    val offsetAngle = (r.nextFloat() - 0.5f) * instability
                    
                    rotate(baseAngle + offsetAngle, pivot = Offset.Zero) {
                        // Curved energy segments
                        drawArc(
                            color = color,
                            startAngle = -20f,
                            sweepAngle = 40f,
                            useCenter = false,
                            topLeft = Offset(-radius, -radius),
                            size = Size(radius * 2, radius * 2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 4f * shieldRatio.coerceAtLeast(0.5f),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        
                        // Internal highlight line
                        drawArc(
                            color = SciFiWhite.copy(alpha = 0.4f * shieldRatio * flicker),
                            startAngle = -15f,
                            sweepAngle = 30f,
                            useCenter = false,
                            topLeft = Offset(-radius + 3f, -radius + 3f),
                            size = Size((radius - 3f) * 2, (radius - 3f) * 2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                        )
                        
                        // Critical sparks
                        if (shieldRatio < 0.25f && Random.nextFloat() < 0.1f) {
                            drawCircle(SciFiWhite, 2f, Offset(radius, 0f))
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
