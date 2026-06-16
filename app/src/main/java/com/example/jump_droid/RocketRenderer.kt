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

    private fun drawRocketBody(drawScope: DrawScope, player: Player) {
        val halfW = ROCKET_WIDTH / 2
        val halfH = ROCKET_HEIGHT / 2
        
        val heatRatio = (player.heat / Constants.MAX_HEAT).coerceIn(0f, 1f)
        val bodyBaseColor = when (player.rocketType) {
            RocketType.BALANCED -> SciFiWhite
            RocketType.SCOUT -> SciFiGold
            RocketType.TANK -> Color(0xFF455A64)
            RocketType.EXPERIMENTAL -> SciFiPurple
        }
        
        val currentColor = if (player.isOverheated) SciFiRed 
                          else lerpColor(bodyBaseColor, SciFiRed, heatRatio * 0.7f)

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

    private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = start.alpha + (end.alpha - start.alpha) * fraction
        )
    }
}
