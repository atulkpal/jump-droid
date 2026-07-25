package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SignalRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x; val ty = threat.y - cameraY
            val phase = threat.phase
            val flicker = if (Random.nextFloat() < (if (phase == 3) 0.3f else 0.1f)) 0f else 1f

            drawCircle(Color(0xFF9E9E9E).copy(alpha = 0.04f), radius = 400f, center = Offset(tx, ty), style = Stroke(width = 60f))

            if (flicker > 0) {
                val glitchCount = if (phase == 3) 40 else 20
                val glitchAlpha = if (phase == 3) 0.5f else 0.3f
                val screenTearCount = if (phase == 3) 10 else 4
                val screenTearAlpha = if (phase == 3) 0.15f else 0.08f
                repeat(glitchCount) { i ->
                    val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 40).toInt()
                    val rng = Random(seed)
                    val rx = tx + (rng.nextFloat() - 0.5f) * 420f
                    val ry = ty + (rng.nextFloat() - 0.5f) * 420f
                    drawRect(
                        if (phase == 3) Color.Red.copy(alpha = rng.nextFloat() * glitchAlpha) else Color.White.copy(alpha = rng.nextFloat() * glitchAlpha),
                        topLeft = Offset(rx, ry),
                        size = Size(rng.nextFloat() * 70f, rng.nextFloat() * 70f)
                    )
                }

                repeat(screenTearCount) { i ->
                    val seed = threat.instanceId.hashCode() + i * 11 + (gameTime / 60).toInt()
                    val rng = Random(seed)
                    val tearY = ty - 350f + rng.nextFloat() * 700f
                    val tearW = 15f + rng.nextFloat() * 80f
                    val tearColor = if (phase == 3) Color(0xFF212121) else Color(0xFF212121)
                    drawRect(tearColor.copy(alpha = screenTearAlpha), Offset(tx - 200f + rng.nextFloat() * 150f, tearY), Size(tearW, 6f))
                    if (phase == 3 && rng.nextFloat() < 0.4f) {
                        drawRect(Color(0xFFFF1744).copy(alpha = 0.05f), Offset(tx - 180f + rng.nextFloat() * 120f, tearY - 2f), Size(tearW + 10f, 10f))
                    }
                }

                repeat(8) { i ->
                    val seed = threat.instanceId.hashCode() + i * 13 + (gameTime / 60).toInt()
                    val rng = Random(seed)
                    val bx = tx + (rng.nextFloat() - 0.5f) * 300f
                    val by = ty - 300f + ((gameTime / 40f + i * 80f) % 600f)
                    drawRect(Color(0xFF00E676).copy(alpha = 0.1f), Offset(bx, by), Size(3f, 5f))
                }

                val ghostRng = Random(threat.instanceId.hashCode() + gameTime.toInt() / 100)
                if (phase >= 2 && ghostRng.nextFloat() < 0.3f) {
                    val gx = tx + (ghostRng.nextFloat() - 0.5f) * 400f
                    val gy = ty + (ghostRng.nextFloat() - 0.5f) * 400f
                    drawRect(Color.White.copy(alpha = 0.08f), Offset(gx - 40f, gy - 5f), Size(80f, 10f), style = Stroke(width = 1f))
                }

                val decoyRng = Random(threat.instanceId.hashCode() + gameTime.toInt() / 150)
                if (phase == 3 && decoyRng.nextFloat() < 0.2f) {
                    val dAngle = decoyRng.nextFloat() * 2f * PI.toFloat()
                    val dDist = 100f + decoyRng.nextFloat() * 150f
                    val dx = tx + cos(dAngle) * dDist
                    val dy = ty + sin(dAngle) * dDist
                    drawCircle(Color.Magenta.copy(alpha = 0.2f), radius = 30f, center = Offset(dx, dy))
                    drawCircle(Color.White.copy(alpha = 0.1f), radius = 15f, center = Offset(dx, dy))
                }

                if (threat.activeWeakPoints > 0) {
                    val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
                    val wpPulse = (sin(gameTime / 100f) * 0.3f + 0.7f)
                    drawCircle(Color.Magenta.copy(alpha = 0.6f * wpPulse * wpGlow), radius = 50f, center = Offset(tx, ty))
                }

                drawCircle(Color.White.copy(alpha = 0.1f * threat.scanPulse), radius = 600f, center = Offset(tx, ty))
            }
        }
    }
}
