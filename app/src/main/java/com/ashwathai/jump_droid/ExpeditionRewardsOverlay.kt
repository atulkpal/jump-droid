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
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.gestures.awaitFirstDown
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
                        .height(320.dp)
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
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(isTop) {
        if (!isTop) offsetX.snapTo(0f)
    }

    Surface(
        modifier = modifier
            .width(320.dp)
            .height(200.dp)
            .offset(x = offsetX.value.dp)
            .graphicsLayer(
                rotationZ = offsetX.value / 15f,
                alpha = (1f - kotlin.math.abs(offsetX.value) / 500f).coerceIn(0f, 1f)
            )
            .pointerInput(isTop, isDismissed) {
                if (!isTop || isDismissed) return@pointerInput
                awaitPointerEventScope {
                    val down = awaitFirstDown()
                    while (true) {
                        val pointerEvent = awaitPointerEvent()
                        val change = pointerEvent.changes.first()
                        if (change.pressed) {
                            val dragAmount = change.position.x - change.previousPosition.x
                            scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                            change.consume()
                        } else {
                            if (kotlin.math.abs(offsetX.value) > 130f) {
                                isDismissed = true
                                scope.launch {
                                    offsetX.animateTo(if (offsetX.value > 0) 700f else -700f, tween(300))
                                    onDismiss()
                                }
                            } else {
                                scope.launch { offsetX.animateTo(0f, SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                            }
                            break
                        }
                    }
                }
            },
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
                    if (isTop) {
                        Text("FLICK TO SYNC", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black)
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
                
                if (event.loreText != null) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        "DATA RECOVERED // LOG SYNCED",
                        color = event.accentColor.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
