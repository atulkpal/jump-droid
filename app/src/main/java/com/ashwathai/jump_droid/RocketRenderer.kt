package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.ashwathai.jump_droid.Constants.ROCKET_HEIGHT
import com.ashwathai.jump_droid.Constants.ROCKET_WIDTH
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

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

        val maxTilt = 15f
        val tilt = (player.velocityX / 400f * maxTilt).coerceIn(-maxTilt, maxTilt)

        with(drawScope) {
            translate(rocketX, rocketY) {

                if (player.destructionTimer > 0) {
                    val progress = player.destructionTimer

                    if (progress < 0.5f) {
                        val flashAlpha = (sin(gameTime / 30f) * 0.5f + 0.5f)
                        rotate(tilt) {
                            drawRocketBody(this, player)
                            drawRect(Color.White.copy(alpha = flashAlpha), topLeft = Offset(-ROCKET_WIDTH/2, -ROCKET_HEIGHT/2), size = Size(ROCKET_WIDTH, ROCKET_HEIGHT))
                        }
                    } else if (progress < 1.5f) {
                        val breakProgress = (progress - 0.5f)

                        repeat(10) { i ->
                            val r = Random(i.toLong() * 100)
                            val angle = r.nextFloat() * 360f + breakProgress * 200f
                            val dist = breakProgress * 150f * (r.nextFloat() + 0.5f)

                            rotate(angle, pivot = Offset.Zero) {
                                translate(dist, 0f) {
                                    when (i % 3) {
                                        0 -> drawRect(Color.Gray, size = Size(12f, 18f))
                                        1 -> drawCircle(Color.DarkGray, radius = 6f)
                                        2 -> drawPath(Path().apply { moveTo(0f, 0f); lineTo(10f, 15f); lineTo(-5f, 10f); close() }, Color.Black)
                                    }

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

                        val explosionAlpha = (1.5f - progress).coerceIn(0f, 1f)
                        drawCircle(Color.White.copy(alpha = explosionAlpha), radius = 40f * (1f + breakProgress))
                        drawCircle(SciFiGold.copy(alpha = explosionAlpha * 0.7f), radius = 60f * (1f + breakProgress))
                    } else {
                        val tumbleProgress = (progress - 1.5f)
                        rotate(gameTime / 2f) {
                            drawRocketBody(this, player, overrideColor = Color.DarkGray)

                            repeat(3) { i ->
                                val r = Random(gameTime + i)
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    radius = 10f + r.nextFloat() * 15f,
                                    center = Offset((r.nextFloat() - 0.5f) * 20f, 20f + tumbleProgress * 50f + i * 15f)
                                )
                            }

                            if (gameTime % 200 < 50) {
                                drawCircle(SciFiRed, 3f, Offset(Random.nextFloat() * 20f - 10f, Random.nextFloat() * 40f - 20f))
                            }
                        }
                    }
                    return@translate
                }

                scale(scaleX = 1f / player.squashStretch, scaleY = player.squashStretch, pivot = Offset(0f, ROCKET_HEIGHT / 2)) {
                    rotate(tilt, pivot = Offset(0f, ROCKET_HEIGHT / 2)) {

                        val isVisible = if (player.invulnerabilityTimer > 0) (gameTime / 100) % 2 == 0L else true

                        if (isVisible) {
                            if (isThrusting && player.fuel > 0) {
                                drawThrusterFlame(this, gameTime)

                                val dx = thrustTarget.x - player.x
                                if (abs(dx) > 20f) {
                                    drawSideThruster(this, dx > 0, gameTime)
                                }
                            }

                            drawRocketBody(this, player)
                            drawAuras(this, player, gameTime)
                            drawSurvivalLayers(this, player, gameTime)
                        }
                    }
                }

                if (player.invulnerabilityTimer > 0) {
                    drawCircle(
                        color = SciFiWhite.copy(alpha = 0.3f),
                        radius = 50f,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }

    private fun drawThrusterFlame(drawScope: DrawScope, gameTime: Long) {
        val random = Random(gameTime / 50)
        val flicker = random.nextFloat() * 15f
        val flickerInner = random.nextFloat() * 8f
        val nozzleY = ROCKET_HEIGHT / 2

        val outerLength = 50f + flicker
        val innerLength = 30f + flickerInner

        val outerFlame = Path().apply {
            moveTo(-14f, nozzleY - 2f)
            quadraticTo(0f, nozzleY + outerLength, 14f, nozzleY - 2f)
            close()
        }

        drawScope.drawPath(
            path = outerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(SciFiGold, SciFiRed.copy(alpha = 0.0f)),
                startY = nozzleY - 2f,
                endY = nozzleY + outerLength + 10f
            )
        )

        val innerFlame = Path().apply {
            moveTo(-6f, nozzleY - 2f)
            quadraticTo(0f, nozzleY + innerLength, 6f, nozzleY - 2f)
            close()
        }

        drawScope.drawPath(
            path = innerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, Color(0xFF80DEEA).copy(alpha = 0.0f)),
                startY = nozzleY - 2f,
                endY = nozzleY + innerLength + 5f
            )
        )

        val afterburnerAlpha = 0.6f + sin(gameTime / 30f) * 0.15f
        drawScope.drawCircle(
            color = Color.White.copy(alpha = afterburnerAlpha * 0.5f),
            radius = 11f,
            center = Offset(0f, nozzleY)
        )
        drawScope.drawCircle(
            color = SciFiGold.copy(alpha = afterburnerAlpha * 0.3f),
            radius = 16f,
            center = Offset(0f, nozzleY)
        )

        if ((gameTime / 80) % 3 == 0L) {
            val diamondY = nozzleY + 8f + (gameTime / 80 % 3) * 10f
            val mach = Path().apply {
                moveTo(0f, diamondY)
                lineTo(4f, diamondY + 6f)
                lineTo(0f, diamondY + 12f)
                lineTo(-4f, diamondY + 6f)
                close()
            }
            drawScope.drawPath(mach, Color.White.copy(alpha = 0.3f))
        }
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
            val bodyLeft = -halfW + 5f
            val bodyTop = -halfH + 15f
            val bodyW = ROCKET_WIDTH - 10f
            val bodyH = ROCKET_HEIGHT - 15f
            val bodyBottom = bodyTop + bodyH
            val bodyRight = bodyLeft + bodyW

            // Main Fuselage
            drawRect(
                color = currentColor,
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyW, bodyH)
            )

            // Engine Nozzle
            val nozzleW = 14f
            val nozzleH = 6f
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(-nozzleW / 2, bodyBottom - 2f),
                size = Size(nozzleW, nozzleH)
            )
            drawRect(
                color = Color(0xFF37474F),
                topLeft = Offset(-nozzleW / 2 + 2f, bodyBottom - 1f),
                size = Size(nozzleW - 4f, 2f)
            )

            // Panel Lines
            val panelColor = Color.Black.copy(alpha = 0.15f)
            drawLine(panelColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.25f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.25f), strokeWidth = 1f)
            drawLine(panelColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.5f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.5f), strokeWidth = 1f)
            drawLine(panelColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.75f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.75f), strokeWidth = 1f)

            // Body edge highlight (right side)
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(bodyRight - 1f, bodyTop + 2f),
                end = Offset(bodyRight - 1f, bodyBottom - 2f),
                strokeWidth = 1.5f
            )

            // Cockpit
            drawCircle(SciFiCyan.copy(alpha = 0.8f), radius = 7f, center = Offset(0f, -5f))
            // Cockpit glow
            drawCircle(SciFiCyan.copy(alpha = 0.15f), radius = 12f, center = Offset(0f, -5f))

            // Nose Cone
            val nosePath = Path().apply {
                moveTo(bodyLeft, bodyTop)
                lineTo(0f, -halfH)
                lineTo(bodyRight, bodyTop)
                close()
            }
            drawPath(nosePath, Color.DarkGray)
            // Nose cone highlight
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(0f, -halfH + 3f),
                end = Offset(bodyLeft + 8f, bodyTop - 2f),
                strokeWidth = 1f
            )

            // Fins
            val leftFin = Path().apply {
                moveTo(bodyLeft, 10f)
                lineTo(-halfW, halfH)
                lineTo(bodyLeft, halfH)
                close()
            }
            val rightFin = Path().apply {
                moveTo(bodyRight, 10f)
                lineTo(halfW, halfH)
                lineTo(bodyRight, halfH)
                close()
            }
            drawPath(leftFin, SciFiRed)
            drawPath(rightFin, SciFiRed)
            // Fin highlights
            drawLine(Color.White.copy(alpha = 0.15f), Offset(-halfW + 2f, halfH - 4f), Offset(bodyLeft + 2f, 14f), strokeWidth = 1f)
            drawLine(Color.White.copy(alpha = 0.15f), Offset(halfW - 2f, halfH - 4f), Offset(bodyRight - 2f, 14f), strokeWidth = 1f)
        }
    }

    private fun drawAuras(drawScope: DrawScope, player: Player, gameTime: Long) {
        val pulse = (sin(gameTime / 150f) * 0.1f) + 0.9f
        if (player.turboTimer > 0) {
            drawScope.drawCircle(SciFiCyan.copy(alpha = 0.2f), radius = 55f * pulse, center = Offset.Zero)
        }
        if (player.efficiencyTimer > 0) {
            drawScope.drawCircle(SciFiGreen.copy(alpha = 0.2f), radius = 55f * pulse, center = Offset.Zero)
        }
    }

    private fun drawSurvivalLayers(drawScope: DrawScope, player: Player, gameTime: Long) {
        with(drawScope) {
            if (player.shield > 0) {
                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)

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
                val pulse = (sin(gameTime / 150f) * 0.1f + 0.9f)
                val flicker = if (shieldRatio < 0.25f && (gameTime / 80 % 2 == 0L)) 0.4f else 1.0f

                val plateColor = SciFiCyan.copy(alpha = (0.5f + 0.3f * shieldRatio) * flicker)

                repeat(plateCount) { i ->
                    val r = Random(i.toLong() * 77)
                    val baseAngle = i * (360f / plateCount) + (gameTime * rotationSpeed)
                    val floatOffset = (kotlin.math.cos(gameTime / 200f + i) * 5f)

                    rotate(baseAngle, pivot = Offset.Zero) {
                        drawArc(
                            color = plateColor,
                            startAngle = -15f + (r.nextFloat() - 0.5f) * instability,
                            sweepAngle = 30f,
                            useCenter = false,
                            topLeft = Offset(-radius - floatOffset, -radius - floatOffset),
                            size = Size((radius + floatOffset) * 2 * pulse, (radius + floatOffset) * 2 * pulse),
                            style = Stroke(
                                width = 8f * shieldRatio.coerceAtLeast(0.3f),
                                cap = androidx.compose.ui.graphics.StrokeCap.Butt
                            )
                        )

                        drawArc(
                            color = SciFiWhite.copy(alpha = 0.6f * flicker),
                            startAngle = -10f,
                            sweepAngle = 20f,
                            useCenter = false,
                            topLeft = Offset(-radius - floatOffset + 2f, -radius - floatOffset + 2f),
                            size = Size((radius + floatOffset - 2f) * 2 * pulse, (radius + floatOffset - 2f) * 2 * pulse),
                            style = Stroke(width = 1.5f)
                        )

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

            if (player.integrity < player.maxIntegrity) {
                val damageRatio = 1f - (player.integrity / player.maxIntegrity)

                repeat((damageRatio * 4).toInt().coerceAtLeast(1)) { i ->
                    val r = Random(i.toLong() * 500)
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = 4f + r.nextFloat() * 6f,
                        center = Offset((r.nextFloat() - 0.5f) * 20f, (r.nextFloat() - 0.5f) * 40f)
                    )
                }

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
