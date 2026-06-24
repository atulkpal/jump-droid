package com.example.jump_droid

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.jump_droid.ui.theme.SciFiRed
import kotlin.random.Random

/**
 * Composable that renders text with a glitch effect.
 * Randomly flickers characters and overlays subtle scanline glitches.
 */
@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current.copy(color = SciFiRed)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlitchTransition")
    
    // Drive flicker at ~8Hz (125ms per cycle)
    val flickerTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(125, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlitchTrigger"
    )

    val glitchedText = remember(text, flickerTrigger) {
        val chars = text.toCharArray()
        if (text.isNotEmpty() && Random.nextFloat() > 0.3f) {
            // Randomly flicker 2-3 characters
            val count = Random.nextInt(1, 3).coerceAtMost(text.length)
            repeat(count) {
                val idx = Random.nextInt(text.length)
                chars[idx] = listOf('?', '#', '*', '!', '0', 'X', '$', '%', '&').random()
            }
        }
        String(chars)
    }

    Box(modifier = modifier.drawWithContent {
        drawContent()
        // Subtle scanline glitches
        if (Random.nextFloat() > 0.7f) {
            val y = Random.nextFloat() * size.height
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
        
        if (Random.nextFloat() > 0.95f) {
             drawRect(
                color = SciFiRed.copy(alpha = 0.05f),
                topLeft = Offset(0f, Random.nextFloat() * size.height),
                size = androidx.compose.ui.geometry.Size(size.width, 10f)
            )
        }
    }) {
        Text(
            text = glitchedText,
            style = style
        )
    }
}
