package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random
@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    altitude: Int,
    altitudePoints: Int,
    platformPoints: Int,
    bossPoints: Int,
    comboPoints: Int,
    progressionManager: ProgressionManager,
    continuesUsed: Int,
    isPremiumUser: Boolean = false,
    runBossesDefeated: Int = 0,
    bestComboThisRun: Int = 0,
    pendingUnlocks: List<UnlockEvent> = emptyList(),
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    onClaimRewards: () -> Unit = {}
) {
    val analytics = LocalAnalytics.current
    val infiniteTransition = rememberInfiniteTransition(label = "GameOverTransition")
    val glitchOffset by infiniteTransition.animateFloat(0f, 3f, infiniteRepeatable(tween(200), RepeatMode.Reverse), label = "GlitchOffset")
    val borderPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "BorderPulse")
    val titleGlow by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "TitleGlow")

    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 50, alphaRange = 0.1f..0.4f, starColor = Color(0xFFD32F2F))
        Canvas(Modifier.fillMaxSize()) {
            val ft = frameTime.value / 1000f
            val w = size.width
            val h = size.height

            // Glitch particles overlay
            repeat(20) {
                val x = Random.nextFloat() * w + sin(ft * 2f + it * 1.3f) * 2f
                val y = Random.nextFloat() * h
                drawCircle(SciFiRed.copy(alpha = 0.06f), radius = 0.5f + Random.nextFloat(), center = Offset(x, y))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "COMMUNICATION LOST",
                color = SciFiRed,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    shadow = Shadow(SciFiRed.copy(alpha = titleGlow * 0.5f), blurRadius = 16f)
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.offset(
                    x = (glitchOffset * (if (sin(glitchOffset.toDouble()) > 0.5) 1 else -1)).dp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "TELEMETRY DATA ENDED",
                color = SciFiRed.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SIGNAL LOST AT ${altitude}m",
                color = SciFiRed.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(Modifier.height(36.dp))

            Surface(
                color = SciFiSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SciFiBorder.copy(alpha = borderPulse)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL SCORE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$score", color = SciFiWhite, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreBreakdownItem("ALTITUDE", altitudePoints, SciFiCyan)
                        ScoreBreakdownItem("BOSSES", bossPoints, SciFiGold)
                        ScoreBreakdownItem("PLATFORMS", platformPoints, SciFiGreen)
                        ScoreBreakdownItem("COMBOS", comboPoints, SciFiPurple)
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("RECORD SCORE", color = SciFiGold.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$highScore", color = SciFiGold, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = SciFiBorder.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RANK", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            AscensionInsignia(rank = progressionManager.currentRank, insigniaSize = 28.dp)
                            Text(progressionManager.currentRank.title.split(" ").last(), color = SciFiGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MASTERY", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text("${progressionManager.currentMasteryPoints}", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("POINTS", color = SciFiCyan.copy(alpha = 0.5f), fontSize = 7.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COLLECTION", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text("${progressionManager.getTotalCompletionPercentage()}%", color = SciFiCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FLIGHT LOG (Achievements/Missions)
            if (pendingUnlocks.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("FLIGHT LOG — EXPEDITION DATA ACQUIRED", color = SciFiCyan, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    items(pendingUnlocks) { event ->
                        Surface(
                            color = SciFiSurface,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, event.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier.width(160.dp).height(120.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = when(event) {
                                            is UnlockEvent.Mission -> "MISSION"
                                            is UnlockEvent.Module -> "MODULE"
                                            is UnlockEvent.Rocket -> "BLUEPRINT"
                                            is UnlockEvent.Achievement -> "ACHIEVEMENT"
                                            is UnlockEvent.Discovery -> "SIGNAL"
                                        },
                                        color = event.accentColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (event is UnlockEvent.Mission) {
                                        Text(event.mission.tier.displayName, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp)
                                    }
                                }
                                Text(
                                    text = event.title,
                                    color = SciFiWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = event.description,
                                    color = SciFiWhite.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    lineHeight = 10.sp,
                                    maxLines = 2
                                )
                                if (event is UnlockEvent.Mission && event.mission.tier != MissionTier.TIER_4) {
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "NEXT GOAL: ${MissionTier.entries[event.mission.tier.ordinal + 1].displayName}",
                                        color = event.accentColor.copy(alpha = 0.6f),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onClaimRewards,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiGreen.copy(alpha = 0.2f), contentColor = SciFiGreen),
                    border = BorderStroke(1.dp, SciFiGreen.copy(alpha = 0.4f))
                ) {
                    Text("CLAIM ALL LOG REWARDS", fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(36.dp))

            val earnedContinues = (runBossesDefeated / 5) + (bestComboThisRun / 15)
            val maxContinues = (if (isPremiumUser) 5 else 3) + earnedContinues
            val isFreeContinue = isPremiumUser && continuesUsed == 0
            val continuesRemaining = maxContinues - continuesUsed

            if (continuesUsed < maxContinues) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var retryCount by remember { mutableStateOf(0) }
                var isAdLoading by remember { mutableStateOf(false) }
                val hasCredits = progressionManager.creditBalance > 0

                if (!isFreeContinue && !hasCredits) {
                    LaunchedEffect(retryCount, continuesUsed) { RewardedAdHelper.load(context) }
                }

                val failureMessage = if (!hasCredits && !isFreeContinue) {
                    when (retryCount) {
                        0 -> null
                        1 -> "AD UNAVAILABLE — LINK WEAK"
                        2 -> "ONE ATTEMPT REMAINING"
                        else -> null
                    }
                } else null

                // Continue via credit (PRIMARY path)
                if (hasCredits) {
                    Button(
                        onClick = {
                            if (progressionManager.spendCredit()) {
                                onContinue()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SciFiGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("CONTINUE (1 CREDIT)", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${progressionManager.creditBalance - 1} left",
                                color = Color.Black.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Continue via watch ad (FALLBACK when no credits)
                if (!hasCredits) {
                    Button(
                        onClick = {
                            if (isFreeContinue) {
                                onContinue()
                            } else {
                                isAdLoading = true
                                analytics.logAdClicked("rewarded", AdConfig.REWARDED_UNIT_ID)
                                val activity = context.findActivity()
                                if (activity != null) {
                                    RewardedAdHelper.show(activity,
                                        analytics = analytics,
                                        onReward = {
                                            isAdLoading = false
                                            onContinue()
                                        },
                                        onFailed = {
                                            if (retryCount >= 2) {
                                                isAdLoading = false
                                                onContinue() // Grant free reward
                                            } else {
                                                retryCount++
                                                // Automatic retry logic
                                                RewardedAdHelper.load(context)
                                                // Keep spinner visible and try showing again after a short delay
                                                scope.launch {
                                                    delay(1000)
                                                    RewardedAdHelper.show(activity,
                                                        analytics = analytics,
                                                        onReward = { isAdLoading = false; onContinue() },
                                                        onFailed = { 
                                                            if (retryCount >= 2) {
                                                                isAdLoading = false
                                                                onContinue()
                                                            } else {
                                                                retryCount++
                                                                isAdLoading = false
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    isAdLoading = false
                                    onContinue() // Emergency fallback
                                }
                            }
                        },
                        enabled = !isAdLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFreeContinue) SciFiGold else SciFiCyan
                        )
                    ) {
                        if (isAdLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else if (isFreeContinue) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("FREE CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("[AD]", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                                Spacer(Modifier.padding(start = 8.dp))
                                Text(if (retryCount >= 2) "FORCED RELINK" else "WATCH AD TO CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                    }
                }

                if (continuesRemaining > 0) {
                    Text(
                        text = "Continue ${continuesUsed + 1} of $maxContinues",
                        color = SciFiWhite.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (failureMessage != null) {
                    Text(
                        text = failureMessage,
                        color = SciFiRed.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                val nextBossTarget = ((runBossesDefeated / 5) + 1) * 5
                val nextComboTarget = ((bestComboThisRun / 15) + 1) * 15
                Text(
                    text = "Bosses: $runBossesDefeated/$nextBossTarget  ·  Best Combo: $bestComboThisRun/$nextComboTarget",
                    color = SciFiWhite.copy(alpha = 0.25f),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            // Credit management row
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                val creditContext = LocalContext.current
                Text(
                    text = "CREDITS: ${progressionManager.creditBalance}",
                    color = SciFiGold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        analytics.logAdClicked("rewarded", AdConfig.REWARDED_UNIT_ID)
                        val activity = creditContext.findActivity()
                        if (activity != null) {
                            RewardedAdHelper.show(activity,
                                analytics = analytics,
                                onReward = {
                                    progressionManager.addCredits(1)
                                },
                                onFailed = {}
                            )
                        } else {
                            // Fallback if activity not found
                            progressionManager.addCredits(1)
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SciFiCyan.copy(alpha = 0.2f),
                        contentColor = SciFiCyan
                    ),
                    border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.4f))
                ) {
                    Text("+1 CREDIT [AD]", fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp)
                }
                if (progressionManager.totalCash >= 100 && progressionManager.creditBalance < progressionManager.maxCredits) {
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            progressionManager.cashToCredits(100)
                        },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SciFiGold.copy(alpha = 0.15f),
                            contentColor = SciFiGold
                        ),
                        border = BorderStroke(1.dp, SciFiGold.copy(alpha = 0.3f))
                    ) {
                        Text("100 CASH → 1 CREDIT", fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("NEW EXPEDITION", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onMainMenu,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SciFiWhite.copy(alpha = 0.5f))
                ) {
                    Text("RETURN TO BASE", fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                }
                val shareContext = LocalContext.current
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "I scored $score points in Jump Droid! 🚀 Can you beat me?\nhttps://jump-droid.vercel.app")
                        }
                        shareContext.startActivity(Intent.createChooser(intent, "Share Jump Droid"))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("⇧", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()
        }
    }
}

@Composable
private fun ScoreBreakdownItem(label: String, points: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 7.sp, letterSpacing = 1.sp)
        Text("+$points", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}
