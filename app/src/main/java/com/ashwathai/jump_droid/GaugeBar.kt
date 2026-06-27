package com.ashwathai.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import kotlin.math.sin

private const val GAUGE_WIDTH = 10f
private const val GAUGE_BAR_RADIUS = 4f

@Composable
fun GaugeBar(
    modifier: Modifier = Modifier,
    value: Float,
    color: Color,
    gameTime: Long,
    isInterfered: Boolean,
    noiseVal: Float,
    gaugeHeight: Dp,
    interferencePhase: Float = 0f,
    onDrawFill: DrawScope.(fillAlpha: Float, fillHeight: Float) -> Unit,
    onDrawExtra: DrawScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(GAUGE_WIDTH.dp)
            .height(gaugeHeight)
            .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(GAUGE_BAR_RADIUS.dp))
            .border(0.5.dp, color.copy(alpha = 0.2f), RoundedCornerShape(GAUGE_BAR_RADIUS.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackRect = Rect(Offset.Zero, size)
            clipPath(Path().apply { addRoundRect(RoundRect(trackRect, CornerRadius(GAUGE_BAR_RADIUS.dp.toPx()))) }) {
                if (noiseVal > 0.15f) {
                    val fillAlpha = if (isInterfered) (0.3f + noiseVal * 0.6f) else 0.9f
                    val fillHeight = size.height * value
                    onDrawFill(fillAlpha, fillHeight)
                }
                if (isInterfered) {
                    repeat(4) {
                        val ny = (sin(gameTime / 80.0 + it * 2.0 + interferencePhase) * 0.5 + 0.5) * size.height
                        drawLine(Color.White.copy(alpha = 0.3f * noiseVal), Offset(0f, ny.toFloat()), Offset(size.width, ny.toFloat()), strokeWidth = 1f)
                    }
                }
                onDrawExtra()
                val segCount = 10
                for (i in 1 until segCount) {
                    val sy = size.height * (i.toFloat() / segCount)
                    drawLine(color.copy(alpha = 0.1f), Offset(0f, sy), Offset(size.width, sy), strokeWidth = 0.5f)
                }
            }
        }
    }
}
