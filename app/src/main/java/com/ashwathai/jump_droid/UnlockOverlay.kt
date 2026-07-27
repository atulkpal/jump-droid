package com.ashwathai.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*

@Composable
fun UnlockOverlay(
    unlockEvent: UnlockEvent,
    onConfirm: () -> Unit
) {
    val glowTransition = rememberInfiniteTransition(label = "UnlockGlow")
    val glowAlpha by glowTransition.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "GlowAlpha")
    val glowRadius by glowTransition.animateFloat(100f, 180f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "GlowRadius")

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable { onConfirm() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            drawCircle(unlockEvent.accentColor.copy(alpha = glowAlpha * 0.06f), radius = glowRadius * 1.5f, center = Offset(cx, cy))
            drawCircle(unlockEvent.accentColor.copy(alpha = glowAlpha * 0.04f), radius = glowRadius * 2.5f, center = Offset(cx, cy))
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SciFiSurface,
            modifier = Modifier.padding(24.dp).safeDrawingPadding()
                .border(1.dp, unlockEvent.accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            Column(
                Modifier.padding(28.dp).widthIn(max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("UNLOCKED", color = unlockEvent.accentColor, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 4.sp)

                Spacer(Modifier.height(16.dp))

                when (unlockEvent) {
                    is UnlockEvent.Rocket -> {
                        val rocketRenderer = remember { RocketRenderer() }
                        val previewPlayer = remember {
                            Player(0f, 0f).apply { rocketType = unlockEvent.type }
                        }
                        Box(Modifier.size(160.dp).background(SciFiBackground.copy(alpha = 0.5f), RoundedCornerShape(10.dp)).border(0.5.dp, unlockEvent.accentColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(80.dp).align(Alignment.Center)) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                scale(2.5f, pivot = center) {
                                    rocketRenderer.render(this, previewPlayer, false, Offset.Zero, 0f, 0L, offsetOverride = center)
                                }
                            }
                        }
                    }
                    is UnlockEvent.Module -> {
                        Box(Modifier.size(60.dp).background(unlockEvent.accentColor.copy(alpha = 0.15f), RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                            Text(unlockEvent.module.category.name.take(1), color = unlockEvent.accentColor, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        }
                    }
                    is UnlockEvent.Achievement -> {
                        Box(Modifier.size(60.dp).background(SciFiGold.copy(alpha = 0.15f), RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(36.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width / 2, 0f)
                                    lineTo(size.width * 0.65f, size.height * 0.35f)
                                    lineTo(size.width, size.height * 0.4f)
                                    lineTo(size.width * 0.75f, size.height * 0.65f)
                                    lineTo(size.width * 0.8f, size.height)
                                    lineTo(size.width / 2, size.height * 0.85f)
                                    lineTo(size.width * 0.2f, size.height)
                                    lineTo(size.width * 0.25f, size.height * 0.65f)
                                    lineTo(0f, size.height * 0.4f)
                                    lineTo(size.width * 0.35f, size.height * 0.35f)
                                    close()
                                }
                                drawPath(path, SciFiGold)
                            }
                        }
                    }
                    is UnlockEvent.Mission -> {
                        Box(Modifier.size(60.dp).background(SciFiCyan.copy(alpha = 0.15f), RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                            Text("\uD83D\uDCCD", fontSize = 28.sp)
                        }
                    }
                    is UnlockEvent.Discovery -> {
                        Box(Modifier.size(60.dp).background(SciFiPurple.copy(alpha = 0.15f), RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                            Text("\uD83D\uDD0D", fontSize = 28.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(unlockEvent.title, style = MaterialTheme.typography.headlineSmall, color = SciFiWhite, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, letterSpacing = 1.sp)

                Spacer(Modifier.height(10.dp))

                Text(unlockEvent.description, style = MaterialTheme.typography.bodyMedium, color = SciFiWhite.copy(alpha = 0.6f), textAlign = TextAlign.Center)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = unlockEvent.accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(Modifier.height(6.dp))

                Text(unlockEvent.destinationLabel, color = unlockEvent.accentColor.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}
