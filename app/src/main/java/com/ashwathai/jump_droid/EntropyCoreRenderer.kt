package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import kotlin.math.cos
import kotlin.math.sin

class EntropyCoreRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player,
        context: android.content.Context?
    ) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val pulse = threat.scanPulse
        val healthPct = threat.health / threat.definition.baseHealth
        val drainActive = player.heat > 50f || player.fuel < player.maxFuel * 0.5f

        // Dark aura
        drawScope.drawCircle(
            color = Color.Black.copy(alpha = 0.3f * alpha),
            radius = 140f,
            center = Offset(cx, cy)
        )

        // Outer energy field
        drawScope.drawCircle(
            color = SciFiRed.copy(alpha = (0.08f + pulse * 0.12f) * alpha),
            radius = 130f + pulse * 30f,
            center = Offset(cx, cy)
        )

        // D1: Plasma Siphon Beams (Pulling energy from edges)
        if (drainActive) {
            repeat(4) { side ->
                val startX = when(side) { 0 -> 0f; 1 -> drawScope.size.width; else -> cx }
                val startY = when(side) { 2 -> 0f; 3 -> drawScope.size.height; else -> cy }
                
                val beamPulse = sin((gameTime / 120f + side).toDouble()).toFloat() * 0.4f + 0.6f
                drawScope.drawLine(
                    color = SciFiRed.copy(alpha = 0.15f * beamPulse * alpha),
                    start = Offset(startX, startY),
                    end = Offset(cx, cy),
                    strokeWidth = 4f * beamPulse
                )
            }
        }

        // D2: Cooling Vents (Venting periodic steam particles)
        val steamAlpha = (sin((gameTime / 600f).toDouble()).toFloat() * 0.5f + 0.5f) * alpha
        repeat(8) { i ->
            val r = kotlin.random.Random(threat.instanceId.hashCode() + i + (gameTime / 1000).toInt())
            val steamDist = 140f + r.nextFloat() * 100f
            val steamAngle = i * 45f * (kotlin.math.PI.toFloat() / 180f)
            val sx = cx + cos(steamAngle) * steamDist
            val sy = cy + sin(steamAngle) * steamDist
            
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.12f * steamAlpha),
                radius = 10f + r.nextFloat() * 15f,
                center = Offset(sx, sy)
            )
        }

        // D3: Redline Core (Intensifying glow as pylons fall)
        val destroyedCount = threat.maxWeakPoints - threat.activeWeakPoints
        val redlineIntensity = 1f + (destroyedCount * 0.5f)
        val corePulse = (0.9f + pulse * 0.2f) * redlineIntensity
        
        val coreRadius = 80f * corePulse.coerceIn(0.5f, 2.5f)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SciFiRed.copy(alpha = 0.9f * alpha),
                    Color(0xFFFF6D00).copy(alpha = 0.6f * alpha),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = coreRadius * 1.8f
            ),
            radius = coreRadius * 1.8f,
            center = Offset(cx, cy)
        )
        
        drawScope.drawCircle(
            color = Color.Black.copy(alpha = alpha),
            radius = coreRadius * 0.7f,
            center = Offset(cx, cy)
        )

        // Inner core filament
        drawScope.drawCircle(
            color = Color.White.copy(alpha = (0.1f + pulse * 0.3f) * alpha),
            radius = coreRadius * 0.25f,
            center = Offset(cx, cy)
        )

        // Pylons (4) with visual destroyed state
        repeat(4) { i ->
            val angle = (i * 90f + sin(gameTime / 500f) * 10f) * (kotlin.math.PI.toFloat() / 180f)
            val dist = 150f + sin(gameTime / 300f + i * 1.2f) * 10f
            val px = cx + cos(angle) * dist
            val py = cy + sin(angle) * dist

            val isDestroyed = (threat.wpDestroyedMask and (1 shl i)) != 0
            val pylonColor = if (isDestroyed) Color.Gray.copy(alpha = 0.3f) else SciFiRed

            if (isDestroyed) {
                // Wreckage
                drawScope.drawRect(
                    color = pylonColor.copy(alpha = alpha),
                    topLeft = Offset(px - 15f, py - 15f),
                    size = androidx.compose.ui.geometry.Size(30f, 30f)
                )
                drawScope.drawLine(
                    color = Color.Gray.copy(alpha = 0.2f * alpha),
                    start = Offset(px - 10f, py - 10f),
                    end = Offset(px + 10f, py + 10f),
                    strokeWidth = 2f
                )
            } else {
                val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
                // Active pylon with glow
                drawScope.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(SciFiRed.copy(alpha = alpha * wpGlow), Color(0xFFFF6D00).copy(alpha = alpha * 0.7f * wpGlow)),
                        startY = py - 20f,
                        endY = py + 20f
                    ),
                    topLeft = Offset(px - 20f, py - 20f),
                    size = androidx.compose.ui.geometry.Size(40f, 40f)
                )
                drawScope.drawRect(
                    color = SciFiRed.copy(alpha = (0.3f + pulse * 0.3f) * alpha * wpGlow),
                    topLeft = Offset(px - 24f, py - 24f),
                    size = androidx.compose.ui.geometry.Size(48f, 48f),
                    style = Stroke(width = 2f)
                )

                // Energy beam to core
                val beamPulse = sin(gameTime / 200f + i * 1.5f) * 0.3f + 0.7f
                drawScope.drawLine(
                    color = SciFiRed.copy(alpha = (0.2f + pulse * 0.3f) * beamPulse * alpha),
                    start = Offset(cx, cy),
                    end = Offset(px, py),
                    strokeWidth = 2f + beamPulse * 2f
                )
            }
        }

        // System drain visual when active
        if (drainActive) {
            val drainPulse = sin(gameTime / 80f) * 0.5f + 0.5f
            repeat(6) { i ->
                val da = (i * 60f + gameTime / 60f) * (kotlin.math.PI.toFloat() / 180f)
                val dd = 100f + drainPulse * 40f
                drawScope.drawCircle(
                    color = SciFiRed.copy(alpha = 0.1f * (1f - drainPulse) * alpha),
                    radius = 6f,
                    center = Offset(cx + cos(da) * dd, cy + sin(da) * dd)
                )
            }
        }

        // Weakness pulse at low health
        if (healthPct < 0.3f) {
            val warnPulse = sin(gameTime / 150f) * 0.4f + 0.6f
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.15f * warnPulse * alpha),
                radius = 100f + warnPulse * 30f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )
        }
    }
}
