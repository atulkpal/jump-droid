package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.random.Random

class LeviathanRenderer : ThreatRenderer {
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
            val phase = threat.phase

            val nearLeft = player.x < 100f
            val nearRight = player.x > size.width - 100f
            if (phase == 3 && (nearLeft || nearRight)) {
                val edgeX = if (nearLeft) 0f else size.width
                drawRect(Color.Red.copy(alpha = 0.15f), Offset(edgeX - if (nearLeft) 0f else 20f, 0f), Size(if (nearLeft) 20f else 20f, size.height))
            }

            val bioPulse = (sin((gameTime / 350f).toDouble()).toFloat() * 0.4f + 0.6f)
            val bioGlowColor = if (phase == 3) Color(0xFFFF1744) else Color(0xFF00E5FF)
            val secondaryGlow = if (phase == 3) Color(0xFFFF8A80) else Color(0xFFB2EBF2)
            
            repeat(8) { i -> // Increased segment count for smoother tail
                val ox = sin((gameTime / 1000f - i * 0.4f).toDouble()).toFloat() * 120f
                val oy = i * 70f
                val segmentPulse = (sin((gameTime / 450f + i).toDouble()).toFloat() * 0.25f + 0.75f) * bioPulse
                val bodyColor = if (phase == 3) Color(0xFF1A237E) else Color(0xFF01579B)
                val lerpedBioColor = if (i % 2 == 0) bioGlowColor else secondaryGlow

                // D1: Body Segments (Enhanced Shadow & Glow)
                drawOval(
                    color = bodyColor,
                    topLeft = Offset(tx + ox - (70f - i * 8f) * segmentPulse, ty + oy - (50f - i * 6f) * segmentPulse),
                    size = Size((140f - i * 16f) * segmentPulse, (100f - i * 12f) * segmentPulse)
                )
                
                // D2: Bio-luminescent Rims
                drawOval(
                    color = lerpedBioColor.copy(alpha = 0.2f * bioPulse),
                    topLeft = Offset(tx + ox - (70f - i * 8f + 8f) * segmentPulse, ty + oy - (50f - i * 6f + 8f) * segmentPulse),
                    size = Size((140f - i * 16f + 16f) * segmentPulse, (100f - i * 12f + 16f) * segmentPulse),
                    style = Stroke(width = 5f)
                )
                drawOval(
                    color = lerpedBioColor.copy(alpha = 0.5f * bioPulse),
                    topLeft = Offset(tx + ox - (70f - i * 8f) * segmentPulse, ty + oy - (50f - i * 6f) * segmentPulse),
                    size = Size((140f - i * 16f) * segmentPulse, (100f - i * 12f) * segmentPulse),
                    style = Stroke(width = 2.5f)
                )

                // D3: Vein Network
                val veinPath = Path().apply {
                    moveTo(tx + ox - 25f + i * 3f, ty + oy - 20f)
                    quadraticTo(tx + ox, ty + oy - 40f, tx + ox + 25f - i * 3f, ty + oy - 20f)
                }
                drawPath(veinPath, secondaryGlow.copy(alpha = 0.5f * bioPulse), style = Stroke(width = 2f))

                // D4: Pulsing Circular Nodes
                drawCircle(
                    color = lerpedBioColor.copy(alpha = 0.4f * segmentPulse),
                    radius = (45f - i * 5f) * segmentPulse,
                    center = Offset(tx + ox, ty + oy),
                    style = Stroke(width = 2.5f)
                )

                val arrowDir = if (i % 2 == 0) 1f else -1f
                val arrX = tx + ox + arrowDir * 40f
                drawLine(Color.Cyan.copy(alpha = 0.3f), Offset(arrX, ty + oy), Offset(arrX + arrowDir * 25f, ty + oy), strokeWidth = 2f)
                val arrHead = Path().apply {
                    moveTo(arrX + arrowDir * 25f, ty + oy)
                    lineTo(arrX + arrowDir * 15f, ty + oy - 6f)
                    lineTo(arrX + arrowDir * 15f, ty + oy + 6f)
                    close()
                }
                drawPath(arrHead, Color.Cyan.copy(alpha = 0.3f))

                repeat(4) { j ->
                    val windX = tx + ox + (Random.nextFloat() - 0.5f) * 60f
                    val windY = ty + oy + 40f + (j * 40f)
                    drawLine(Color.Cyan.copy(alpha = 0.4f), Offset(windX, windY), Offset(windX, windY + 60f), strokeWidth = 3f)
                }

                val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
                val wpIndex = i / 2
                if (i % 2 == 0 && (threat.wpDestroyedMask and (1 shl wpIndex)) == 0) {
                    drawCircle(Color.Magenta.copy(alpha = wpGlow), radius = 30f * segmentPulse * wpGlow, center = Offset(tx + ox, ty + oy))
                    drawCircle(Color.White.copy(alpha = wpGlow), radius = 10f, center = Offset(tx + ox, ty + oy))
                }

                if (i == 0) {
                    drawCircle(Color(0xFFFF1744).copy(alpha = 0.6f), radius = 8f, center = Offset(tx + ox + 15f, ty + oy - 10f))
                    drawCircle(Color.White.copy(alpha = 0.4f), radius = 4f, center = Offset(tx + ox + 15f, ty + oy - 10f))
                }

                if (phase == 3 && i >= 4) {
                    val whipExtend = sin(gameTime / 200f + i) * 20f
                    drawLine(Color.Red.copy(alpha = 0.3f), Offset(tx + ox + whipExtend, ty + oy), Offset(tx + ox + whipExtend * 2f, ty + oy + 20f), strokeWidth = 4f)
                }
            }
        }
    }
}
