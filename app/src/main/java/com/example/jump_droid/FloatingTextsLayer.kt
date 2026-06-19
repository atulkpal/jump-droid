package com.example.jump_droid

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset

@Composable
fun FloatingTextsLayer(
    texts: List<FloatingText>,
    cameraY: Float
) {
    texts.forEach { ft ->
        val scale = if (ft.isCritical) 1.0f + (ft.life * 0.5f * ft.scaleMultiplier) else 1.0f
        Text(
            text = ft.text,
            color = ft.color.copy(alpha = (ft.life / 1.0f).coerceIn(0f, 1f)),
            modifier = Modifier
                .offset { IntOffset((ft.x - 50f).toInt(), (ft.y - cameraY).toInt()) }
                .graphicsLayer(scaleX = scale, scaleY = scale),
            style = if (ft.isCritical) MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(ft.shadowColor, offset = Offset(0f, 0f), blurRadius = ft.shadowBlur)
            ) else MaterialTheme.typography.labelLarge.copy(
                shadow = Shadow(ft.shadowColor, offset = Offset(2f, 2f), blurRadius = ft.shadowBlur)
            ),
            fontWeight = FontWeight.Bold
        )
    }
}
