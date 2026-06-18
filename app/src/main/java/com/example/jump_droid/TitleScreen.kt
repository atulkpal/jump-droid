package com.example.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun TitleScreen(onNavigate: (GameState) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.safeDrawingPadding()) {
            val infiniteTransition = rememberInfiniteTransition(label = "TitleTransition")
            val glowAlpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "GlowAlpha")
            val rocketOffset by infiniteTransition.animateFloat(0f, -20f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "RocketOffset")
            Canvas(Modifier.size(100.dp).offset { IntOffset(0, rocketOffset.dp.roundToPx()) }) {
                val rx = size.width / 2; val ry = size.height / 2
                drawRect(SciFiWhite.copy(alpha = 0.8f), topLeft = Offset(rx - 10f, ry - 15f), size = Size(20f, 40f))
                drawPath(Path().apply { moveTo(rx - 10f, ry - 15f); lineTo(rx, ry - 30f); lineTo(rx + 10f, ry - 15f); close() }, SciFiRed)
                drawPath(Path().apply { moveTo(rx - 5f, ry + 25f); lineTo(rx, ry + 40f + Random.nextFloat() * 10f); lineTo(rx + 5f, ry + 25f); close() }, SciFiGold)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "JUMP DROID",
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = Shadow(SciFiCyan.copy(alpha = glowAlpha), Offset(0f, 0f), 20f),
                    letterSpacing = 8.sp
                ),
                color = SciFiWhite,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(80.dp))
            Button(
                onClick = { onNavigate(GameState.MAIN_MENU) },
                modifier = Modifier.width(240.dp).height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
            ) {
                Text("INITIATE ASCENT", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("THE ASCENSION PROGRAM // EST. 1984", color = SciFiWhite.copy(alpha = 0.3f), letterSpacing = 1.sp, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Text("POWERED BY ASHWATH.AI // V1.2.0", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp)
        }
    }
}
