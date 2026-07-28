package com.ashwathai.jump_droid

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AscensionInsignia(
    rank: AscensionRank,
    modifier: Modifier = Modifier,
    insigniaSize: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "InsigniaGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "GlowPulse"
    )
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "Rotate"
    )

    val insigniaColor = when (rank) {
        AscensionRank.EXPLORER_I -> Color(0xFFCD7F32) // Bronze
        AscensionRank.EXPLORER_II -> Color(0xFFC0C0C0) // Silver
        AscensionRank.EXPLORER_III -> SciFiGold
        AscensionRank.EXPLORER_IV -> SciFiCyan
        AscensionRank.EXPLORER_V -> SciFiPurple
    }

    Box(modifier = modifier.size(insigniaSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(insigniaSize)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val radius = size.minDimension / 2 * 0.8f

            // Background Glow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(insigniaColor.copy(alpha = 0.3f * glowPulse), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius * 1.5f
                )
            )

            rotate(if (rank.level >= 3) rotateAngle else 0f, pivot = Offset(cx, cy)) {
                when (rank) {
                    AscensionRank.EXPLORER_I -> {
                        val path = createPolygonPath(cx, cy, 3, radius)
                        drawPath(path, insigniaColor, style = Stroke(width = 2.dp.toPx()))
                        drawPath(path, insigniaColor.copy(alpha = 0.2f))
                    }
                    AscensionRank.EXPLORER_II -> {
                        val path = createPolygonPath(cx, cy, 4, radius)
                        drawPath(path, insigniaColor, style = Stroke(width = 2.5.dp.toPx()))
                        drawPath(path, insigniaColor.copy(alpha = 0.2f))
                        drawCircle(insigniaColor.copy(alpha = 0.4f), radius * 0.4f, center = Offset(cx, cy))
                    }
                    AscensionRank.EXPLORER_III -> {
                        val path = createPolygonPath(cx, cy, 5, radius)
                        drawPath(path, insigniaColor, style = Stroke(width = 3.dp.toPx()))
                        val starPath = createStarPath(cx, cy, 5, radius * 0.6f, radius * 0.3f)
                        drawPath(starPath, insigniaColor)
                    }
                    AscensionRank.EXPLORER_IV -> {
                        val path = createPolygonPath(cx, cy, 6, radius)
                        drawPath(path, insigniaColor, style = Stroke(width = 3.dp.toPx()))
                        drawCircle(insigniaColor.copy(alpha = 0.15f), radius * 0.7f, style = Stroke(width = 1.dp.toPx()))
                        drawCircle(insigniaColor.copy(alpha = 0.3f), radius * 0.4f, style = Stroke(width = 2.dp.toPx()))
                        repeat(6) { i ->
                            val angle = i * PI.toFloat() / 3f
                            drawCircle(insigniaColor, 3f, Offset(cx + radius * cos(angle), cy + radius * sin(angle)))
                        }
                    }
                    AscensionRank.EXPLORER_V -> {
                        val path = createStarPath(cx, cy, 8, radius, radius * 0.5f)
                        drawPath(path, insigniaColor, style = Stroke(width = 4.dp.toPx()))
                        drawPath(path, insigniaColor.copy(alpha = 0.25f))
                        
                        rotate(-rotateAngle * 2, pivot = Offset(cx, cy)) {
                            val innerPath = createPolygonPath(cx, cy, 4, radius * 0.4f)
                            drawPath(innerPath, SciFiWhite.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))
                        }
                        
                        drawCircle(SciFiWhite, 4f, Offset(cx, cy))
                    }
                }
            }
        }
    }
}

private fun createPolygonPath(cx: Float, cy: Float, sides: Int, radius: Float): Path {
    val path = Path()
    val angleStep = 2 * PI.toFloat() / sides
    for (i in 0 until sides) {
        val angle = -PI.toFloat() / 2 + i * angleStep
        val x = cx + radius * cos(angle)
        val y = cy + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun createStarPath(cx: Float, cy: Float, points: Int, outerRadius: Float, innerRadius: Float): Path {
    val path = Path()
    val angleStep = PI.toFloat() / points
    for (i in 0 until 2 * points) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = -PI.toFloat() / 2 + i * angleStep
        val x = cx + radius * cos(angle)
        val y = cy + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
