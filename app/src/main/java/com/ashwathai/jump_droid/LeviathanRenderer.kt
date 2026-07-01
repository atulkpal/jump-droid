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
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x; val ty = threat.y - cameraY
            val phase = threat.phase

            val nearLeft = player.x < 100f
            val nearRight = player.x > size.width - 100f
            if (phase == 3 && (nearLeft || nearRight)) {
                val edgeX = if (nearLeft) 0f else size.width
                drawRect(Color.Red.copy(alpha = 0.15f), Offset(edgeX - if (nearLeft) 0f else 20f, 0f), Size(if (nearLeft) 20f else 20f, size.height))
            }

            repeat(6) { i ->
                val ox = sin(gameTime / 1000f - i * 0.5f) * 100f
                val oy = i * 60f
                val segmentPulse = (sin(gameTime / 500f + i) * 0.2f + 0.8f)
                val bodyColor = if (phase == 3) Color(0xFF1A237E) else Color(0xFF01579B)

                drawOval(bodyColor, topLeft = Offset(tx + ox - (60f - i * 8f) * segmentPulse, ty + oy - (45f - i * 6f) * segmentPulse), size = Size((120f - i * 16f) * segmentPulse, (90f - i * 12f) * segmentPulse))
                drawOval(Color.Cyan.copy(alpha = 0.2f), topLeft = Offset(tx + ox - (60f - i * 8f) * segmentPulse, ty + oy - (45f - i * 6f) * segmentPulse), size = Size((120f - i * 16f) * segmentPulse, (90f - i * 12f) * segmentPulse), style = Stroke(width = 2f))

                drawOval(bodyColor.copy(alpha = 0.5f), topLeft = Offset(tx + ox - (30f - i * 4f) * segmentPulse, ty + oy - (10f - i * 2f) * segmentPulse), size = Size((60f - i * 8f) * segmentPulse, (20f - i * 3f) * segmentPulse))

                val veinPath = Path().apply {
                    moveTo(tx + ox - 20f + i * 3f, ty + oy - 15f)
                    quadraticTo(tx + ox, ty + oy - 30f, tx + ox + 20f - i * 3f, ty + oy - 15f)
                }
                drawPath(veinPath, Color.Cyan.copy(alpha = 0.4f), style = Stroke(width = 2f))
                drawCircle(Color.Cyan.copy(alpha = 0.5f), radius = (40f - i * 5f) * segmentPulse, center = Offset(tx + ox, ty + oy), style = Stroke(width = 2f))

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
