package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.CornerRadius
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
        gameTime: Long,
        offsetOverride: Offset? = null
    ) {
        val rocketX = offsetOverride?.x ?: player.x
        val rocketY = offsetOverride?.y ?: (player.y - cameraY)

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
                                drawThrusterFlame(this, player, gameTime)

                                val dx = thrustTarget.x - player.x
                                if (abs(dx) > 20f) {
                                    drawSideThruster(this, dx > 0, gameTime)
                                }
                            }

                            drawRocketBody(this, player)
                            drawAuras(this, player, gameTime)
                            drawSurvivalLayers(this, player, gameTime)
                            drawModuleIndicators(this, player, gameTime)
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

    private fun drawModuleIndicators(drawScope: DrawScope, player: Player, gameTime: Long) {
        val modules = player.activeModules
        if (modules.isEmpty()) return
        val pulse = sin(gameTime / 300f) * 0.3f + 0.7f
        val bodyLeft = -ROCKET_WIDTH / 2 + 5f
        val bodyTop = -ROCKET_HEIGHT / 2 + 15f
        val bodyW = ROCKET_WIDTH - 10f
        val bodyH = ROCKET_HEIGHT - 15f

        modules.forEachIndexed { index, module ->
            val y = bodyTop + bodyH * (index + 1) / (modules.size + 1)
            val x = bodyLeft + bodyW + 8f
            drawScope.drawCircle(
                color = module.iconColor.copy(alpha = 0.6f * pulse),
                radius = 4f,
                center = Offset(x, y)
            )
            drawScope.drawCircle(
                color = module.iconColor.copy(alpha = 0.15f),
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }

    private fun drawThrusterFlame(drawScope: DrawScope, player: Player, gameTime: Long) {
        val random = Random(gameTime / 50)
        val flicker = random.nextFloat() * 15f
        val flickerInner = random.nextFloat() * 8f
        val nozzleY = ROCKET_HEIGHT / 2

        val heatRatio = (player.heat / Constants.MAX_HEAT).coerceIn(0f, 1f)

        val outerFlameColor = when {
            heatRatio > 0.8f -> SciFiRed
            heatRatio > 0.5f -> SciFiGold
            heatRatio > 0.2f -> Color(0xFFFF9800)
            else -> SciFiCyan
        }
        val innerFlameColor = when {
            heatRatio > 0.8f -> Color(0xFFFFEB3B)
            heatRatio > 0.5f -> Color.White
            heatRatio > 0.2f -> Color(0xFFB2EBF2)
            else -> Color.White
        }

        val outerLength = 50f + flicker + heatRatio * 20f
        val innerLength = 30f + flickerInner + heatRatio * 10f

        val outerFlame = Path().apply {
            moveTo(-14f, nozzleY - 2f)
            quadraticTo(0f, nozzleY + outerLength, 14f, nozzleY - 2f)
            close()
        }

        drawScope.drawPath(
            path = outerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(outerFlameColor, SciFiRed.copy(alpha = 0.0f)),
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
                colors = listOf(innerFlameColor, innerFlameColor.copy(alpha = 0.0f)),
                startY = nozzleY - 2f,
                endY = nozzleY + innerLength + 5f
            )
        )

        val engineMod = player.activeModules.firstOrNull { it.category == ModuleCategory.ENGINE && it.iconColor != Color.Black }
        if (engineMod != null) {
            val tintPath = Path().apply {
                moveTo(-14f, nozzleY - 2f)
                quadraticTo(0f, nozzleY + outerLength * 1.2f, 14f, nozzleY - 2f)
                close()
            }
            drawScope.drawPath(
                path = tintPath,
                brush = Brush.verticalGradient(
                    colors = listOf(engineMod.iconColor.copy(alpha = 0.4f), engineMod.iconColor.copy(alpha = 0.0f)),
                    startY = nozzleY - 2f,
                    endY = nozzleY + outerLength + 10f
                )
            )
        }

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
        val chassis = player.currentChassisIndex

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
            val bodyW = when (chassis) {
                2 -> ROCKET_WIDTH - 6f
                else -> ROCKET_WIDTH - 10f
            }
            val bodyLeft = -halfW + (ROCKET_WIDTH - bodyW) / 2
            val bodyTop = -halfH + 15f
            val bodyH = ROCKET_HEIGHT - 15f
            val bodyBottom = bodyTop + bodyH
            val bodyRight = bodyLeft + bodyW

            val accentColor = when (chassis) {
                1 -> SciFiCyan
                2 -> SciFiGold
                else -> currentColor
            }

            // Main Fuselage
            val fuselageShape = when (chassis) {
                1 -> Path().apply {
                    moveTo(bodyLeft + 3f, bodyTop)
                    lineTo(bodyLeft, bodyTop + bodyH * 0.15f)
                    lineTo(bodyLeft, bodyBottom)
                    lineTo(bodyRight, bodyBottom)
                    lineTo(bodyRight, bodyTop + bodyH * 0.15f)
                    lineTo(bodyRight - 3f, bodyTop)
                    close()
                }
                else -> null
            }
            if (fuselageShape != null) {
                drawPath(fuselageShape, currentColor)
            } else {
                drawRect(currentColor, topLeft = Offset(bodyLeft, bodyTop), size = Size(bodyW, bodyH))
            }

            // Engine Nozzle (wider for chassis 2)
            val nozzleW = if (chassis == 2) 18f else 14f
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

            // Chassis 1: lateral vents
            if (chassis == 1) {
                drawRect(SciFiCyan.copy(alpha = 0.3f), topLeft = Offset(bodyLeft - 2f, bodyTop + bodyH * 0.4f), size = Size(2f, 8f))
                drawRect(SciFiCyan.copy(alpha = 0.3f), topLeft = Offset(bodyRight, bodyTop + bodyH * 0.4f), size = Size(2f, 8f))
            }

            // Panel Lines with glow highlights
            val panelColor = Color.Black.copy(alpha = 0.15f)
            val energyLineColor = currentColor.copy(alpha = 0.08f)
            drawLine(panelColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.25f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.25f), strokeWidth = 1f)
            drawLine(energyLineColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.25f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.25f), strokeWidth = 2f)
            drawLine(panelColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.5f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.5f), strokeWidth = 1f)
            drawLine(energyLineColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.5f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.5f), strokeWidth = 2f)
            drawLine(panelColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.75f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.75f), strokeWidth = 1f)
            drawLine(energyLineColor, Offset(bodyLeft + 5f, bodyTop + bodyH * 0.75f), Offset(bodyRight - 5f, bodyTop + bodyH * 0.75f), strokeWidth = 2f)

            // Chassis 2: extra armor ridges
            if (chassis == 2) {
                val ridgeColor = Color(0xFF546E7A)
                drawLine(ridgeColor, Offset(bodyLeft, bodyTop + bodyH * 0.2f), Offset(bodyRight, bodyTop + bodyH * 0.2f), strokeWidth = 2f)
                drawLine(ridgeColor, Offset(bodyLeft, bodyTop + bodyH * 0.7f), Offset(bodyRight, bodyTop + bodyH * 0.7f), strokeWidth = 2f)
            }

            // Armor plates for hull modules (amplified on chassis 2)
            val hasHullMods = player.activeModules.any { it.category == ModuleCategory.HULL }
            if (hasHullMods) {
                val armorColor = if (chassis == 2) Color(0xFF546E7A) else Color(0xFF37474F)
                val plateCount = player.activeModules.count { it.category == ModuleCategory.HULL }.coerceAtMost(3)
                val plateWidth = if (chassis == 2) 5f else 3f
                repeat(plateCount) { i ->
                    val yo = bodyTop + bodyH * (i + 1) / (plateCount + 1)
                    drawRect(armorColor, topLeft = Offset(bodyLeft - plateWidth, yo - 4f), size = Size(plateWidth, 8f))
                    drawRect(armorColor, topLeft = Offset(bodyRight, yo - 4f), size = Size(plateWidth, 8f))
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(bodyLeft - plateWidth + 1f, yo - 3f), Offset(bodyLeft - plateWidth + 1f, yo + 3f), strokeWidth = 0.5f)
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(bodyRight + plateWidth - 1f, yo - 3f), Offset(bodyRight + plateWidth - 1f, yo + 3f), strokeWidth = 0.5f)
                }
            }

            // Body edge highlight (right side)
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(bodyRight - 1f, bodyTop + 2f),
                end = Offset(bodyRight - 1f, bodyBottom - 2f),
                strokeWidth = 1.5f
            )

            // Cockpit (differs by chassis)
            when (chassis) {
                1 -> {
                    drawCircle(SciFiCyan.copy(alpha = 0.8f), radius = 5f, center = Offset(0f, -3f))
                    drawCircle(SciFiCyan.copy(alpha = 0.15f), radius = 9f, center = Offset(0f, -3f))
                    drawLine(SciFiWhite.copy(alpha = 0.3f), Offset(-2f, -5f), Offset(2f, -3f), strokeWidth = 1f)
                }
                2 -> {
                    drawRoundRect(SciFiCyan.copy(alpha = 0.8f), topLeft = Offset(-5f, -8f), size = Size(10f, 6f), cornerRadius = CornerRadius(2f, 2f))
                    drawRoundRect(SciFiCyan.copy(alpha = 0.15f), topLeft = Offset(-8f, -10f), size = Size(16f, 10f), cornerRadius = CornerRadius(3f, 3f))
                }
                else -> {
                    drawCircle(SciFiCyan.copy(alpha = 0.8f), radius = 7f, center = Offset(0f, -5f))
                    drawCircle(SciFiCyan.copy(alpha = 0.15f), radius = 12f, center = Offset(0f, -5f))
                }
            }

            // Nose Cone (varies by chassis)
            when (chassis) {
                1 -> {
                    val nosePath = Path().apply {
                        moveTo(bodyLeft, bodyTop)
                        lineTo(0f, -halfH - 6f)
                        lineTo(bodyRight, bodyTop)
                        close()
                    }
                    drawPath(nosePath, Color.DarkGray)
                    val highlight = Path().apply {
                        moveTo(0f, -halfH - 3f)
                        lineTo(bodyLeft + 6f, bodyTop - 2f)
                        lineTo(bodyLeft + 10f, bodyTop + 4f)
                        close()
                    }
                    drawPath(highlight, Color.White.copy(alpha = 0.15f))
                }
                2 -> {
                    drawRoundRect(Color.DarkGray, topLeft = Offset(bodyLeft, bodyTop - 8f), size = Size(bodyW, bodyH * 0.25f + 8f), cornerRadius = CornerRadius(bodyW / 2, bodyW / 2))
                    drawCircle(Color.White.copy(alpha = 0.12f), radius = 6f, center = Offset(0f, -halfH + 8f))
                }
                else -> {
                    val nosePath = Path().apply {
                        moveTo(bodyLeft, bodyTop)
                        lineTo(0f, -halfH)
                        lineTo(bodyRight, bodyTop)
                        close()
                    }
                    drawPath(nosePath, Color.DarkGray)
                    drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, -halfH + 3f), Offset(bodyLeft + 8f, bodyTop - 2f), strokeWidth = 1f)
                }
            }

            // Chassis 1: sensor array stripe
            if (chassis == 1) {
                drawLine(SciFiCyan.copy(alpha = 0.2f), Offset(bodyLeft + 2f, bodyTop + 2f), Offset(bodyRight - 2f, bodyTop + 2f), strokeWidth = 1.5f)
            }

            // Fins (varies by chassis)
            when (chassis) {
                1 -> {
                    val leftFin = Path().apply {
                        moveTo(bodyLeft, 12f)
                        lineTo(-halfW - 6f, halfH)
                        lineTo(bodyLeft, halfH)
                        close()
                    }
                    val rightFin = Path().apply {
                        moveTo(bodyRight, 12f)
                        lineTo(halfW + 6f, halfH)
                        lineTo(bodyRight, halfH)
                        close()
                    }
                    drawPath(leftFin, SciFiRed)
                    drawPath(rightFin, SciFiRed)
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(-halfW - 4f, halfH - 4f), Offset(bodyLeft + 2f, 16f), strokeWidth = 1f)
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(halfW + 4f, halfH - 4f), Offset(bodyRight - 2f, 16f), strokeWidth = 1f)
                }
                2 -> {
                    val finColor = SciFiRed.copy(alpha = 0.8f)
                    val leftFin = Path().apply {
                        moveTo(bodyLeft, 8f)
                        lineTo(-halfW - 2f, halfH)
                        lineTo(bodyLeft + 2f, halfH)
                        close()
                    }
                    val rightFin = Path().apply {
                        moveTo(bodyRight, 8f)
                        lineTo(halfW + 2f, halfH)
                        lineTo(bodyRight - 2f, halfH)
                        close()
                    }
                    drawPath(leftFin, finColor)
                    drawPath(rightFin, finColor)
                    drawLine(Color(0xFF546E7A), Offset(bodyLeft, halfH - 6f), Offset(bodyLeft, 12f), strokeWidth = 2f)
                    drawLine(Color(0xFF546E7A), Offset(bodyRight, halfH - 6f), Offset(bodyRight, 12f), strokeWidth = 2f)
                }
                else -> {
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
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(-halfW + 2f, halfH - 4f), Offset(bodyLeft + 2f, 14f), strokeWidth = 1f)
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(halfW - 2f, halfH - 4f), Offset(bodyRight - 2f, 14f), strokeWidth = 1f)
                }
            }
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

            // Shield hit flash: expanding white ring with fade
            if (player.shieldHitTimer > 0) {
                val hitProgress = 1f - (player.shieldHitTimer / 0.3f)
                val hitAlpha = (1f - hitProgress) * 0.5f
                drawCircle(
                    color = Color.White.copy(alpha = hitAlpha),
                    radius = 50f + hitProgress * 30f,
                    style = Stroke(width = 3f * (1f - hitProgress) + 1f)
                )
                drawCircle(
                    color = SciFiCyan.copy(alpha = hitAlpha * 0.3f),
                    radius = 60f + hitProgress * 40f,
                    style = Stroke(width = 2f)
                )
            }

            if (player.shield > 0) {
                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)

                val plateCount = when {
                    shieldRatio >= 0.90f -> 8
                    shieldRatio >= 0.70f -> 6
                    shieldRatio >= 0.40f -> 4
                    shieldRatio >= 0.15f -> 2
                    else -> 1
                }

                val hasShieldMods = player.activeModules.any { it.category == ModuleCategory.SHIELD }
                val shieldBoost = if (hasShieldMods) 8f else 0f
                val alphaBoost = if (hasShieldMods) 0.2f else 0f
                val radius = 58f + shieldBoost
                val rotationSpeed = 0.05f
                val instability = (1f - shieldRatio) * (1f - shieldRatio) * 20f
                val pulse = (sin(gameTime / 150f) * 0.1f + 0.9f)
                val flicker = if (shieldRatio < 0.25f && (gameTime / 80 % 2 == 0L)) 0.4f else 1.0f

                val plateColor = SciFiCyan.copy(alpha = ((0.5f + 0.3f * shieldRatio) + alphaBoost).coerceAtMost(1f) * flicker)

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

                if (damageRatio > 0.5f) {
                    repeat((damageRatio * 3 + 1).toInt()) { i ->
                        val r = Random(i.toLong() * 500 + gameTime / 200)
                        val cx = (r.nextFloat() - 0.5f) * 22f
                        val cy = (r.nextFloat() - 0.5f) * 45f
                        val scorchSize = 4f + r.nextFloat() * 6f
                        drawCircle(
                            color = Color(0xFF3E2723).copy(alpha = 0.5f),
                            radius = scorchSize,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFF1A1A1A).copy(alpha = 0.3f),
                            radius = scorchSize * 0.6f,
                            center = Offset(cx + 1f, cy - 1f)
                        )
                    }
                }

                if (damageRatio > 0.75f && gameTime % 4 == 0L) {
                    val r = Random(gameTime / 2)
                    val sparkX = (r.nextFloat() - 0.5f) * 26f
                    val sparkY = (r.nextFloat() - 0.5f) * 50f
                    drawLine(
                        color = SciFiWhite,
                        start = Offset(sparkX, sparkY),
                        end = Offset(sparkX + (r.nextFloat() - 0.5f) * 8f, sparkY + (r.nextFloat() - 0.5f) * 8f),
                        strokeWidth = 1.5f
                    )
                    drawCircle(
                        color = SciFiGold,
                        radius = 2f,
                        center = Offset(sparkX, sparkY)
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
