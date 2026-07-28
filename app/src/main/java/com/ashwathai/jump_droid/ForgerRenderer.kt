package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ForgerRenderer : ThreatRenderer {
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
        val isJamming = threat.jamCooldown > 0f
        val pulse = sin(gameTime / 200f) * 0.05f + 0.95f

        // D1: Holographic Blueprint Projection (Flickering grid background)
        if (isJamming) {
            val bpAlpha = (0.05f + kotlin.random.Random(gameTime / 100).nextFloat() * 0.05f) * alpha
            val bpSize = 300f
            repeat(10) { i ->
                val lineX = cx - bpSize/2 + i * (bpSize/10f)
                drawScope.drawLine(SciFiCyan.copy(alpha = bpAlpha), Offset(lineX, cy - bpSize/2), Offset(lineX, cy + bpSize/2), strokeWidth = 1f)
                val lineY = cy - bpSize/2 + i * (bpSize/10f)
                drawScope.drawLine(SciFiCyan.copy(alpha = bpAlpha), Offset(cx - bpSize/2, lineY), Offset(cx + bpSize/2, lineY), strokeWidth = 1f)
            }
        }

        // Fabrication Body with horizontal gradient
        drawScope.drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF2A2A2A).copy(alpha = alpha),
                    Color(0xFF455A64).copy(alpha = alpha), // Higher specularity
                    Color(0xFF2A2A2A).copy(alpha = alpha)
                ),
                startX = cx - 45f,
                endX = cx + 45f
            ),
            topLeft = Offset(cx - 40f, cy - 25f),
            size = Size(80f, 50f)
        )

        // D2: Multi-Jointed Assembly Arms (animated movement)
        repeat(2) { side ->
            val isRight = side == 1
            val dir = if (isRight) 1f else -1f
            val baseArmX = cx + dir * 40f
            val armPulse = sin((gameTime / 350f + side * kotlin.math.PI.toFloat()).toDouble()).toFloat()
            
            // Shoulder Joint
            val shoulderY = cy - 10f
            val jointAngle = armPulse * 30f
            val upperArmLen = 30f
            val elbowX = baseArmX + cos((jointAngle * PI.toFloat() / 180f)) * upperArmLen * dir
            val elbowY = shoulderY + sin((jointAngle * PI.toFloat() / 180f)) * upperArmLen
            
            drawScope.drawLine(Color.Gray.copy(alpha = 0.8f * alpha), Offset(baseArmX, shoulderY), Offset(elbowX, elbowY), strokeWidth = 5f)
            
            // Elbow Joint to Claw
            val wristAngle = (jointAngle + 45f) * (if (isJamming) 2f else 1f)
            val lowerArmLen = 25f
            val clawX = elbowX + cos((wristAngle * PI.toFloat() / 180f)) * lowerArmLen * dir
            val clawY = elbowY + sin((wristAngle * PI.toFloat() / 180f)) * lowerArmLen
            
            drawScope.drawLine(Color.DarkGray.copy(alpha = 0.8f * alpha), Offset(elbowX, elbowY), Offset(clawX, clawY), strokeWidth = 4f)
            
            // Claw Head (cyan glow)
            drawScope.drawCircle(SciFiCyan.copy(alpha = 0.6f * alpha), 5f, Offset(clawX, clawY))
        }

        // D3: Fabrication Flash (High-intensity strobe when jamming)
        if (isJamming) {
            val strobe = if ((gameTime / 40) % 2 == 0L) 0.8f else 0.2f
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SciFiCyan.copy(alpha = 0.3f * strobe * alpha), Color.Transparent),
                    center = Offset(cx, cy + 10f),
                    radius = 120f
                ),
                radius = 120f,
                center = Offset(cx, cy + 10f)
            )
        }

        // Jam sparks when converting platforms
        if (isJamming) {
            val sparkIntensity = threat.jamCooldown / 3f
            repeat(8) { i ->
                val sa = (i * 45f + gameTime / 40f * i) * (kotlin.math.PI.toFloat() / 180f)
                val sd = 30f + (kotlin.random.Random(gameTime + i).nextFloat() * 80f * sparkIntensity)
                val sx = cx + kotlin.math.cos(sa) * sd
                val sy = cy + kotlin.math.sin(sa) * sd
                val sparkColor = if (i % 2 == 0) SciFiCyan else SciFiOrange
                drawScope.drawRect(
                    color = sparkColor.copy(alpha = (0.6f * sparkIntensity * (1f - sd / 120f)) * alpha),
                    topLeft = Offset(sx - 3f, sy - 3f),
                    size = Size(6f, 6f)
                )
            }

            // Central flash during jam
            val flashPulse = sin(gameTime / 60f) * 0.5f + 0.5f
            drawScope.drawRect(
                color = SciFiOrange.copy(alpha = 0.2f * flashPulse * alpha),
                topLeft = Offset(cx - 30f, cy - 10f),
                size = Size(60f, 20f)
            )
        } else {
            // Idle indicator light
            val idlePulse = sin(gameTime / 400f) * 0.3f + 0.7f
            drawScope.drawCircle(
                color = SciFiCyan.copy(alpha = 0.3f * idlePulse * alpha),
                radius = 4f,
                center = Offset(cx, cy + 28f)
            )
        }

        // Weak point indicators
        if (threat.activeWeakPoints > 0) {
            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
            val wpPulse = sin(gameTime / 200f) * 0.3f + 0.7f
            repeat(3) { i ->
                if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                val wx = cx + (i - 1) * 60f
                drawScope.drawCircle(Color.Magenta.copy(alpha = 0.8f * wpPulse * wpGlow * alpha), radius = 8f * wpPulse, center = Offset(wx, cy))
                drawScope.drawCircle(Color.White.copy(alpha = 0.5f * wpGlow * alpha), radius = 3f, center = Offset(wx, cy))
                }
            }
        }
    }
}
