package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import kotlin.math.cos
import kotlin.math.sin

class MirrorShardsRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player
    ) {
        val centerX = threat.x
        val centerY = threat.y - cameraY
        val inversionActive = player.controlInversionTimer > 0f

        // Ambient glow field
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SciFiPurple.copy(alpha = 0.08f * alpha),
                    SciFiCyan.copy(alpha = 0.04f * alpha),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = 100f
            ),
            radius = 100f,
            center = Offset(centerX, centerY)
        )

        // Fragment trail particles
        repeat(4) { i ->
            val tAngle = (gameTime / 300f + i * 0.5f + sin(gameTime / 1000f + i) * 0.3f) % 6.28f
            val tDist = 60f + sin(gameTime / 500f + i * 2f) * 15f
            val tx = centerX + cos(tAngle) * tDist
            val ty = centerY + sin(tAngle * 0.8f) * tDist * 0.6f
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.15f * alpha * (sin(gameTime / 200f + i) * 0.5f + 0.5f)),
                radius = 1.5f,
                center = Offset(tx, ty)
            )
        }

        repeat(6) { i ->
            val angle = (gameTime / 800f) + (i * 1.04f)
            val dist = 40f + sin(gameTime / 400f + i) * 10f
            val x = centerX + cos(angle) * dist
            val y = centerY + sin(angle) * dist

            val shardSize = 15f + sin(gameTime / 300f + i * 1.5f) * 3f
            val rotation = sin(gameTime / 200f + i) * 0.2f

            val path = Path().apply {
                val cx = x
                val cy = y
                val cosR = cos(rotation)
                val sinR = sin(rotation)
                val pts = arrayOf(
                    Offset(0f, -shardSize),
                    Offset(shardSize * 0.7f, shardSize * 0.3f),
                    Offset(-shardSize * 0.7f, shardSize * 0.3f)
                )
                val tPts = pts.map { p ->
                    Offset(
                        cx + p.x * cosR - p.y * sinR,
                        cy + p.x * sinR + p.y * cosR
                    )
                }
                moveTo(tPts[0].x, tPts[0].y)
                lineTo(tPts[1].x, tPts[1].y)
                lineTo(tPts[2].x, tPts[2].y)
                close()
            }

            val baseAlpha = (sin(gameTime / 300f + i * 2f) * 0.3f + 0.7f) * alpha

            // Glass fill
            drawScope.drawPath(
                path = path,
                color = if (inversionActive)
                    SciFiPurple.copy(alpha = 0.4f * baseAlpha)
                else
                    SciFiPurple.copy(alpha = 0.25f * baseAlpha)
            )

            // Edge glow
            drawScope.drawPath(
                path = path,
                color = SciFiWhite.copy(alpha = 0.3f * baseAlpha),
                style = Stroke(width = 2f)
            )

            // Specular highlight sweep
            val highlightPhase = ((gameTime / 2000f + i * 0.166f) % 1f)
            val highlightY = y - shardSize * 0.5f + highlightPhase * shardSize
            val hlStart = Offset(
                x - shardSize * 0.5f * (1f - highlightPhase),
                highlightY
            )
            val hlEnd = Offset(
                x + shardSize * 0.5f * (1f - highlightPhase),
                highlightY
            )
            drawScope.drawLine(
                color = Color.White.copy(alpha = 0.4f * (1f - highlightPhase) * alpha),
                start = hlStart,
                end = hlEnd,
                strokeWidth = 2f
            )

            // Refraction lines
            repeat(2) { j ->
                val rPhase = (gameTime / 300f + i + j * 0.3f) % 1f
                val rx = x + (j - 0.5f) * shardSize * 0.4f
                val ry1 = y - shardSize * 0.3f
                val ry2 = y + shardSize * 0.2f
                drawScope.drawLine(
                    color = SciFiCyan.copy(alpha = 0.12f * alpha * (1f - rPhase)),
                    start = Offset(rx, ry1),
                    end = Offset(rx, ry2),
                    strokeWidth = 1.5f
                )
            }
        }

        // Shatter-threshold flash when inversion is about to trigger
        if (player.controlInversionTimer > 0f) {
            val flashAlpha = (sin(gameTime / 50f) * 0.5f + 0.5f) * 0.3f * alpha
            drawScope.drawCircle(
                color = Color.White.copy(alpha = flashAlpha),
                radius = 35f + sin(gameTime / 30f) * 5f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )
        }

        // Central core
        drawScope.drawCircle(
            color = SciFiWhite.copy(alpha = 0.3f * alpha * (sin(gameTime / 400f) * 0.3f + 0.7f)),
            radius = 3f,
            center = Offset(centerX, centerY)
        )
    }
}
