package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.random.Random

class GustRenderer : ThreatRenderer {
    override fun render(drawScope: DrawScope, threat: ActiveThreat, cameraY: Float, alpha: Float, gameTime: Long, player: Player) {
        with(drawScope) {
            val tx = threat.x
            val ty = threat.y - cameraY
            val windDir = if ((threat.instanceId.hashCode() % 2) == 0) 1f else -1f
            val gustAlpha = (0.5f - abs(0.5f - threat.scanPulse)) * 2f * alpha

            val arrowLen = 200f
            val arrowStartX = tx - arrowLen * 0.5f * windDir
            val arrowEndX = tx + arrowLen * 0.5f * windDir
            drawLine(Color.White.copy(alpha = gustAlpha * 0.6f), Offset(arrowStartX, ty), Offset(arrowEndX, ty), strokeWidth = 6f, cap = StrokeCap.Round)

            val headPath = Path().apply {
                moveTo(arrowEndX, ty)
                lineTo(arrowEndX - 30f * windDir, ty - 15f)
                lineTo(arrowEndX - 30f * windDir, ty + 15f)
                close()
            }
            drawPath(headPath, Color.White.copy(alpha = gustAlpha * 0.6f))

            repeat(6) { i ->
                val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 80).toInt()
                val rng = Random(seed)
                val px = tx + (rng.nextFloat() - 0.5f) * 150f * windDir
                val py = ty + (rng.nextFloat() - 0.5f) * 80f
                drawCircle(Color(0xFF81C784).copy(alpha = gustAlpha * 0.5f), radius = 2f + rng.nextFloat() * 3f, center = Offset(px, py))
            }

            if (threat.scanPulse > 0.4f && threat.scanPulse < 0.6f) {
                drawCircle(Color.White.copy(alpha = 0.1f), radius = 120f, center = Offset(tx, ty), style = Stroke(width = 2f))
            }
        }
    }
}
