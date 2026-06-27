package com.ashwathai.jump_droid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import kotlin.math.sin

@Composable
fun FloatingTextsLayer(
    texts: List<FloatingText>,
    cameraY: Float
) {
    texts.forEach { ft ->
        val age = 1.0f - ft.life
        val alpha = (ft.life / 1.0f).coerceIn(0f, 1f)
        val isCritical = ft.isCritical

        val textScale = if (isCritical) {
            val pop = if (age < 0.2f) 1.0f + (0.2f - age) * 1.5f else 1.0f
            pop * (1.0f + ft.life * 0.5f * ft.scaleMultiplier)
        } else 1.0f

        val driftX = if (isCritical) sin(ft.life * 8f) * 4f else 0f

        Box(
            modifier = Modifier
                .offset { IntOffset(
                    (ft.x - 50f + driftX).toInt(),
                    (ft.y - cameraY - (if (isCritical) 15f else 5f)).toInt()
                ) }
                .width(IntrinsicSize.Max)
                .graphicsLayer(
                    scaleX = textScale,
                    scaleY = textScale,
                    alpha = alpha
                )
                .then(
                    if (isCritical) Modifier.drawBehind {
                        val tw = size.width + 16f
                        val th = size.height + 8f
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.5f * alpha),
                            topLeft = Offset(-8f, -4f),
                            size = androidx.compose.ui.geometry.Size(tw, th),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = ft.shadowColor.copy(alpha = 0.2f * alpha),
                            topLeft = Offset(-8f, -4f),
                            size = androidx.compose.ui.geometry.Size(tw, th),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
                        )
                    } else Modifier
                )
        ) {
            Text(
                text = ft.text,
                color = ft.color.copy(alpha = alpha),
                style = if (isCritical) MaterialTheme.typography.titleSmall.copy(
                    shadow = Shadow(ft.shadowColor, offset = Offset(0f, 0f), blurRadius = ft.shadowBlur * textScale)
                ) else MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(ft.shadowColor, offset = Offset(1f, 1f), blurRadius = ft.shadowBlur.coerceIn(0f, 6f))
                ),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
