package com.ashwathai.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class Star(
    var x: Float, var y: Float, var speed: Float,
    val baseAlpha: Float, val twinklePhase: Float, val size: Float,
    val color: Color = Color.White
)

@Composable
fun StarfieldBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 60,
    speedRange: ClosedFloatingPointRange<Float> = 0.15f..0.55f,
    alphaRange: ClosedFloatingPointRange<Float> = 0.15f..0.55f,
    sizeRange: ClosedFloatingPointRange<Float> = 0.5f..2.0f,
    starColor: Color = Color.White,
    colors: List<Color>? = null
) {
    val stars = remember {
        List(starCount) { i ->
            val resolvedColor = colors?.getOrElse(i % colors.size) { starColor } ?: starColor
            Star(
                x = Random.nextFloat() * 2000f,
                y = Random.nextFloat() * 2000f,
                speed = speedRange.start + Random.nextFloat() * (speedRange.endInclusive - speedRange.start),
                baseAlpha = alphaRange.start + Random.nextFloat() * (alphaRange.endInclusive - alphaRange.start),
                twinklePhase = Random.nextFloat() * 6.28f,
                size = sizeRange.start + Random.nextFloat() * (sizeRange.endInclusive - sizeRange.start),
                color = resolvedColor
            )
        }
    }
    Canvas(modifier = modifier) {
        val ft = System.currentTimeMillis() / 100f
        val w = size.width
        val h = size.height
        stars.forEach { s ->
            s.y += s.speed
            if (s.y > h + 10) { s.y = -10f; s.x = Random.nextFloat() * w }
            val twinkle = sin(ft * 2f + s.twinklePhase) * 0.3f + 0.7f
            val alpha = s.baseAlpha * twinkle
            drawCircle(s.color.copy(alpha = alpha), radius = s.size, center = Offset(s.x, s.y))
        }
    }
}
