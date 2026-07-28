package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ashwathai.jump_droid.ui.theme.SciFiOrange
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import kotlin.math.cos
import kotlin.math.sin

class ThermalHiveRenderer : ThreatRenderer {
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
        val pulse = sin(gameTime / 200f) * 0.1f + 0.9f
        val heatDanger = player.heat > 60f

        // D1: Thermal Haze Overlay (Centered on hive)
        val hazeIntensity = if (heatDanger) 0.12f else 0.04f
        val hazeRadius = 400f * pulse
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SciFiOrange.copy(alpha = hazeIntensity * alpha), Color.Transparent),
                center = Offset(cx, cy),
                radius = hazeRadius
            ),
            radius = hazeRadius,
            center = Offset(cx, cy)
        )

        // D2: Vascular Internal Veins (breathing intensity)
        val veinAlpha = (if (heatDanger) 0.6f else 0.3f) * alpha
        repeat(5) { i ->
            val va = (i * 72f + gameTime / 80f) * (kotlin.math.PI.toFloat() / 180f)
            val vPulse = sin((gameTime / 150f + i).toDouble()).toFloat() * 10f
            val vd = 15f + vPulse
            val vx = cx + cos(va) * vd
            val vy = cy + sin(va) * vd
            drawScope.drawCircle(
                color = if (heatDanger) SciFiRed.copy(alpha = veinAlpha) else SciFiOrange.copy(alpha = veinAlpha),
                radius = 5f + sin((gameTime / 100f + i).toDouble()).toFloat() * 2f,
                center = Offset(vx, vy)
            )
        }

        // D3: Swarm Particles (Significantly increased density)
        val particleCount = if (heatDanger) 24 else 12
        val particleSpeed = if (heatDanger) 60f else 120f
        repeat(particleCount) { i ->
            val r = kotlin.random.Random(threat.instanceId.hashCode() + i)
            val angle = (gameTime / (particleSpeed + r.nextFloat() * 20f)) + (i * (kotlin.math.PI.toFloat() * 2f / particleCount))
            val dist = 30f + sin((gameTime / 250f + i).toDouble()).toFloat() * 20f + r.nextFloat() * 15f
            val px = cx + cos(angle) * dist
            val py = cy + sin(angle) * dist
            val pSize = if (heatDanger) 4f else 2.5f
            val pColor = if (heatDanger) SciFiRed else SciFiOrange
            
            drawScope.drawCircle(
                color = pColor.copy(alpha = (0.5f + 0.4f * sin((gameTime / 90f + i * 1.5f).toDouble()).toFloat()) * alpha),
                radius = pSize,
                center = Offset(px, py)
            )
            // Add tiny trail to particles
            drawScope.drawCircle(
                color = pColor.copy(alpha = 0.2f * alpha),
                radius = pSize * 0.7f,
                center = Offset(px - cos(angle) * 5f, py - sin(angle) * 5f)
            )
        }

        // Swarm spawn VFX when heat is high
        if (heatDanger) {
            val spawnPulse = sin(gameTime / 50f) * 0.5f + 0.5f
            if (spawnPulse > 0.8f) {
                repeat(3) { i ->
                    val sa = (i * 120f + gameTime / 30f) * (kotlin.math.PI.toFloat() / 180f)
                    val sd = 60f + spawnPulse * 30f
                    drawScope.drawCircle(
                        color = SciFiRed.copy(alpha = (0.3f * (1f - spawnPulse)) * alpha),
                        radius = 3f + spawnPulse * 5f,
                        center = Offset(cx + cos(sa) * sd, cy + sin(sa) * sd)
                    )
                }
            }
        }

        // Weak point indicators
        if (threat.activeWeakPoints > 0) {
            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
            val wpPulse = sin(gameTime / 200f) * 0.3f + 0.7f
            repeat(2) { i ->
                if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                val wx = cx + (if (i == 0) -60f else 60f)
                val wy = cy + 20f
                drawScope.drawCircle(Color.Magenta.copy(alpha = 0.8f * wpPulse * wpGlow * alpha), radius = 12f * wpPulse, center = Offset(wx, wy))
                drawScope.drawCircle(Color.White.copy(alpha = 0.5f * wpGlow * alpha), radius = 5f, center = Offset(wx, wy))
                }
            }
        }
    }
}
