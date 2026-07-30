package com.ashwathai.jump_droid

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ashwathai.jump_droid.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ExpeditionRewardsOverlay(
    pendingUnlocks: List<UnlockEvent>,
    progressionManager: ProgressionManager,
    sessionStats: GameStats,
    onClaimReward: (UnlockEvent) -> Unit,
    onAllClaimed: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var showSummary by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "EXPEDITION DATA RECOVERED",
                color = SciFiCyan,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "REVIEWING ACQUIRED INTELLIGENCE",
                color = SciFiWhite.copy(alpha = 0.5f),
                fontSize = 10.sp,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(32.dp))

            if (currentIndex >= pendingUnlocks.size) {
                // --- SESSION SUMMARY SCREEN ---
                SessionSummary(progressionManager, sessionStats, onAllClaimed)
            } else {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Use a key to ensure proper state resetting between cards
                    key(currentIndex) {
                        // Show up to 3 cards in the stack
                        val endIdx = minOf(currentIndex + 2, pendingUnlocks.size - 1)
                        for (i in (endIdx downTo currentIndex)) {
                            val event = pendingUnlocks[i]
                            val isTop = i == currentIndex

                            RewardCardLarge(
                                event = event,
                                isTop = isTop,
                                onDismiss = {
                                    onClaimReward(event)
                                    currentIndex++
                                },
                                modifier = Modifier.zIndex((pendingUnlocks.size - i).toFloat())
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                Text(
                    "ITEM ${currentIndex + 1} OF ${pendingUnlocks.size}",
                    color = SciFiWhite.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun SessionSummary(
    progressionManager: ProgressionManager,
    sessionStats: GameStats,
    onFinish: () -> Unit
) {
    var isExiting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val exitOffset by animateFloatAsState(
        targetValue = if (isExiting) -1000f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SummaryExit"
    )

    Surface(
        color = SciFiSurface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SciFiBorder),
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = exitOffset.dp)
            .graphicsLayer(alpha = if (isExiting) 0f else 1f)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "SESSION SUMMARY",
                color = SciFiGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryStat("ALTITUDE", "${sessionStats.maxAltitudeMeters}m", SciFiCyan)
                SummaryStat("BOSSES", "${sessionStats.bossesDefeated}", SciFiGold)
                SummaryStat("SCORE", "${sessionStats.totalScore}", SciFiGreen)
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = SciFiBorder.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // LORE COMPLETION BAR
            val lorePercent = progressionManager.getTotalCompletionPercentage()
            Text(
                "LORE SYNC STATUS: $lorePercent%",
                color = SciFiPurple,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { lorePercent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = SciFiPurple,
                trackColor = SciFiPurple.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
            Text(
                "ARCHIVE ENTRIES REMAIN LOCKED IN THE VOID",
                color = SciFiWhite.copy(alpha = 0.3f),
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ZEN MODE PROGRESS
            if (!progressionManager.isZenModeUnlocked) {
                val zenProgress = progressionManager.getZenUnlockProgress()
                Text(
                    "ZEN MODE CALIBRATION",
                    color = SciFiCyan.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { zenProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = SciFiCyan,
                    trackColor = SciFiCyan.copy(alpha = 0.1f)
                )
                Text(
                    "REMAINING: ${(100 - zenProgress * 100).toInt()}%",
                    color = SciFiCyan.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    "ZEN MODE: CALIBRATED",
                    color = SciFiGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    scope.launch {
                        isExiting = true
                        delay(500)
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SciFiGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("PROCEED TO DEBRIEF", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, letterSpacing = 1.sp)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RewardCardLarge(
    event: UnlockEvent,
    isTop: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val ft = rememberInfiniteTransition(label = "SyncFT").animateFloat(0f, 1000f, infiniteRepeatable(tween(100000)), label = "FT").value

    val syncAlpha by animateFloatAsState(
        targetValue = if (isSyncing) 0f else 1f,
        animationSpec = if (isSyncing) tween(800, easing = FastOutSlowInEasing) else snap(),
        label = "SyncAlpha"
    )
    val syncScale by animateFloatAsState(
        targetValue = if (isSyncing) 1.5f else 1f,
        animationSpec = if (isSyncing) tween(800, easing = LinearOutSlowInEasing) else snap(),
        label = "SyncScale"
    )

    Box(
        modifier = modifier
            .width(320.dp)
            .height(260.dp), // Increased to accommodate button
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .height(200.dp)
                .graphicsLayer(
                    alpha = syncAlpha,
                    scaleX = syncScale,
                    scaleY = syncScale,
                    rotationX = if (isSyncing) 10f else 0f
                ),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, event.accentColor.copy(alpha = 0.8f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Insignia Watermark
                event.insigniaRes?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp)
                            .graphicsLayer(alpha = 0.2f),
                        colorFilter = ColorFilter.tint(event.accentColor)
                    )
                }

                // Binary Dissolution Effect
                if (isSyncing) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))) {
                        Text(
                            "SYNCING...",
                            modifier = Modifier.align(Alignment.Center),
                            color = event.accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                    }
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        repeat(20) { i ->
                            val rx = Random(i.toLong()).nextFloat() * w
                            val ry = Random(i.toLong() + 100).nextFloat() * h
                            val paint = android.graphics.Paint().apply {
                                color = event.accentColor.copy(alpha = syncAlpha).toArgb()
                                textSize = 20f
                                typeface = android.graphics.Typeface.MONOSPACE
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                if (Random.nextBoolean()) "1" else "0",
                                rx, ry, paint
                            )
                        }
                    }
                }

                Column(Modifier.padding(20.dp).fillMaxSize()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = when (event) {
                                is UnlockEvent.Mission -> "MISSION"
                                is UnlockEvent.Module -> "MODULE"
                                is UnlockEvent.Rocket -> "BLUEPRINT"
                                is UnlockEvent.Achievement -> "ACHIEVEMENT"
                                is UnlockEvent.Discovery -> "SIGNAL"
                            },
                            color = event.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )

                        // REWARD VALUE BADGE
                        event.rewardValue?.let { value ->
                            Surface(
                                color = event.accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, event.accentColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = value,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = event.accentColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = event.title,
                        color = SciFiWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    val displayDesc = event.loreText.takeIf { !it.isNullOrBlank() } ?: event.description
                    Text(
                        text = displayDesc,
                        color = SciFiWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 4,
                        fontStyle = if (event.loreText != null) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                    )
                }
            }
        }

        if (isTop && !isSyncing) {
            Button(
                onClick = {
                    scope.launch {
                        isSyncing = true
                        delay(800)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = event.accentColor.copy(alpha = 0.2f), contentColor = event.accentColor),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, event.accentColor.copy(alpha = 0.5f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val blink = if ((ft * 4).toInt() % 2 == 0) 1f else 0.4f
                    Box(Modifier.size(8.dp).graphicsLayer(alpha = blink).background(event.accentColor, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Text("ARCHIVE INTELLIGENCE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
        }
    }
}
