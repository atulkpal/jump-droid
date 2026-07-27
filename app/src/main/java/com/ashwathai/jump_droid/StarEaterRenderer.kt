package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class StarEaterRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player,
        context: android.content.Context?
    ) {
        with(drawScope) {
            val tx = threat.x; val ty = threat.y - cameraY
            val pulse = (sin(gameTime / 400f) * 0.1f + 0.9f)
            val phase = threat.phase
            val auraRadius = if (phase == 3) 1000f else 800f
            
            val pDist = sqrt(((player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty)).toDouble()).toFloat()
            val tendrilGlow = if (pDist < 400f) 1.0f else 0.5f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black, Color(0xFF6A1B9A).copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(tx, ty),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(tx, ty)
            )

            repeat(15) { i ->
                val rand = Random(threat.instanceId.hashCode() + i)
                val angle = (gameTime / 10f + i * 24f) * (PI.toFloat() / 180f)
                val dist = ((gameTime / 5f + i * 100f) % auraRadius)
                val px = tx + cos(angle) * dist
                val py = ty + sin(angle) * dist
                drawCircle(Color.Magenta.copy(alpha = 0.4f), radius = 5f, center = Offset(px, py))
            }

            // D1: Swirling Accretion Disk (High density light and shadow)
            val diskAngle = (gameTime / 15f) % 360f
            rotate(diskAngle, pivot = Offset(tx, ty)) {
                repeat(4) { i ->
                    val dRadius = auraRadius * (0.4f + i * 0.15f)
                    drawCircle(
                        color = Color(0xFF9C27B0).copy(alpha = 0.08f * (1f - i * 0.2f)),
                        radius = dRadius,
                        center = Offset(tx, ty),
                        style = Stroke(width = 40f + i * 20f)
                    )
                }
                
                // Orbiting light motes in the disk
                repeat(25) { i ->
                    val r = Random(threat.instanceId.hashCode() + i)
                    val ang = (gameTime / 8f + i * 14.4f) * (PI.toFloat() / 180f)
                    val dist = (auraRadius * 0.3f) + (r.nextFloat() * auraRadius * 0.6f)
                    drawCircle(
                        color = if (i % 2 == 0) Color.White.copy(alpha = 0.6f) else Color.Magenta.copy(alpha = 0.4f),
                        radius = 2f + r.nextFloat() * 4f,
                        center = Offset(tx + cos(ang) * dist, ty + sin(ang) * dist)
                    )
                }
            }

            // D2: Gravity Lens Distortion (Central warp)
            val lensPulse = (sin((gameTime / 250f).toDouble()).toFloat() * 0.1f + 0.9f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.Black,
                    0.5f to Color.Black.copy(alpha = 0.9f),
                    0.8f to Color(0xFF4A148C).copy(alpha = 0.3f),
                    1.0f to Color.Transparent,
                    center = Offset(tx, ty),
                    radius = 150f * lensPulse
                ),
                radius = 150f * lensPulse,
                center = Offset(tx, ty)
            )

            // D3: Liquid Hunger Tendrils (reaching shadow threads)
            repeat(14) { i ->
                val baseAngle = i * (360f / 14f) + (sin((gameTime / 800f + i).toDouble()).toFloat() * 20f)
                val tLen = 350f + (sin((gameTime / 300f + i).toDouble()).toFloat() * 60f)
                val tThickness = 12f * pulse * tendrilGlow
                
                rotate(baseAngle, pivot = Offset(tx, ty)) {
                    val tPath = Path().apply {
                        moveTo(tx + 80f, ty)
                        quadraticTo(tx + 200f + sin((gameTime / 150f).toDouble()).toFloat() * 30f, ty + cos((gameTime / 150f).toDouble()).toFloat() * 40f, tx + tLen, ty)
                    }
                    drawPath(
                        path = tPath,
                        color = if (phase == 3) Color.Red.copy(alpha = 0.7f) else Color(0xFF6A1B9A).copy(alpha = tendrilGlow * 0.8f),
                        style = Stroke(width = tThickness, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Tendril tip glow
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f * tendrilGlow),
                        radius = 6f,
                        center = Offset(tx + tLen, ty)
                    )
                }
            }

            drawCircle(Color.Black, radius = 120f * pulse, center = Offset(tx, ty))

            val hungerRate = 1f + (1f - pDist / 1000f).coerceIn(0f, 0.5f)
            drawCircle(Color(0xFFFF4081).copy(alpha = 0.1f * hungerRate), radius = 80f + pulse * 20f, center = Offset(tx, ty), style = Stroke(width = 3f))
        }
    }
}
