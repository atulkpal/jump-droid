package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.sin

class CrosswindRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val bandCount = 4
            val bandSpacing = 120f
            val windDir = if ((threat.instanceId.hashCode() % 2) == 0) 1f else -1f

            repeat(bandCount) { i ->
                val bandY = ty - (bandCount / 2) * bandSpacing + i * bandSpacing + sin(gameTime / 800f + i) * 20f
                val bandStrength = 1f - (abs(i - bandCount / 2f) / (bandCount / 2f))
                val strokeW = 2f + bandStrength * 6f

                val bandPath = Path().apply {
                    moveTo(tx - 300f, bandY)
                    repeat(12) { seg ->
                        val segX = tx - 300f + seg * 50f
                        lineTo(segX, bandY + sin(segX / 80f + gameTime / 600f) * 8f * windDir)
                    }
                }
                drawPath(bandPath, Color.White.copy(alpha = 0.25f * bandStrength * alpha), style = Stroke(width = strokeW, cap = StrokeCap.Round))

                repeat(3) { a ->
                    val arrowX = tx - 200f + a * 200f
                    drawLine(Color.White.copy(alpha = 0.3f * bandStrength * alpha), Offset(arrowX, bandY), Offset(arrowX + 25f * windDir, bandY), strokeWidth = 3f)
                    drawLine(Color.White.copy(alpha = 0.3f * bandStrength * alpha), Offset(arrowX + 25f * windDir, bandY), Offset(arrowX + 15f * windDir, bandY - 6f), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = 0.3f * bandStrength * alpha), Offset(arrowX + 25f * windDir, bandY), Offset(arrowX + 15f * windDir, bandY + 6f), strokeWidth = 2f)
                }
            }
        }
    }
}
