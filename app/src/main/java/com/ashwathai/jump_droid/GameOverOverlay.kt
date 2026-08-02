package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
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

private data class FlyingUiReward(
    val id: Long,
    val color: Color,
    val isCredit: Boolean
)

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
    isZenMode: Boolean = false,
    purchaseManager: PurchaseManager? = null,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    val analytics = LocalAnalytics.current
    val infiniteTransition = rememberInfiniteTransition(label = "GameOverTransition")
    val glitchOffset by infiniteTransition.animateFloat(0f, 3f, infiniteRepeatable(tween(200), RepeatMode.Reverse), label = "GlitchOffset")
    val borderPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "BorderPulse")
    val titleGlow by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "TitleGlow")

    var startAnims by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnims = true }

    val frameTime = remember { mutableStateOf(0L) }
    val flyingUiRewards = remember { mutableStateListOf<FlyingUiReward>() }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }

    val context = LocalContext.current
    var adRetryCount by remember { mutableStateOf(0) }
    LaunchedEffect(adRetryCount, continuesUsed) {
        if (!isPremiumUser) {
            RewardedAdHelper.load(context)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 50, alphaRange = 0.1f..0.4f, starColor = if (isZenMode) SciFiPurple else Color(0xFFD32F2F))
        
        // --- TOP CURRENCY HUD (Hidden in Zen) ---
        if (!isZenMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrencyBadge(label = "CREDITS", value = progressionManager.creditBalance.toString(), color = SciFiGold)
                CurrencyBadge(label = "CASH", value = progressionManager.totalCash.toString(), color = SciFiGreen)
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            val ft = frameTime.value / 1000f
            val w = size.width
            val h = size.height

            repeat(20) {
                val x = Random.nextFloat() * w + sin(ft * 2f + it * 1.3f) * 2f
                val y = Random.nextFloat() * h
                drawCircle((if (isZenMode) SciFiPurple else SciFiRed).copy(alpha = 0.06f), radius = 0.5f + Random.nextFloat(), center = Offset(x, y))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            val titleAnim by animateFloatAsState(if (startAnims) 0f else -100f, tween(600, easing = FastOutSlowInEasing), label = "TitleAnim")
            
            Column(Modifier.offset(y = titleAnim.dp).graphicsLayer(alpha = if (startAnims) 1f else 0f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isZenMode) "ZEN EXPEDITION ENDED" else "COMMUNICATION LOST",
                    color = if (isZenMode) SciFiPurple else SciFiRed,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (isZenMode) 22.sp else 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        shadow = Shadow((if (isZenMode) SciFiPurple else SciFiRed).copy(alpha = titleGlow * 0.5f), blurRadius = 16f)
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
                    text = if (isZenMode) "FLIGHT PROTOCOL COMPLETED" else "TELEMETRY DATA ENDED",
                    color = (if (isZenMode) SciFiPurple else SciFiRed).copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isZenMode) "TOTAL DISTANCE: ${altitude}m" else "SIGNAL LOST AT ${altitude}m",
                color = (if (isZenMode) SciFiPurple else SciFiRed).copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(Modifier.height(16.dp))

            val contentAnim by animateFloatAsState(if (startAnims) 1f else 0.8f, tween(800, 200, FastOutSlowInEasing), label = "ContentAnim")
            val contentAlpha by animateFloatAsState(if (startAnims) 1f else 0f, tween(800, 200), label = "ContentAlpha")

            Surface(
                color = SciFiSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, (if (isZenMode) SciFiPurple else SciFiBorder).copy(alpha = borderPulse)),
                modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = contentAnim, scaleY = contentAnim, alpha = contentAlpha)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val isNewRecord = score > highScore && highScore > 0
                    
                    Text(if (isZenMode) "ZEN SCORE" else "TOTAL SCORE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text(
                        "$score",
                        color = if (isNewRecord) SciFiGold else SciFiWhite,
                        style = MaterialTheme.typography.displayMedium.copy(
                            shadow = if (isNewRecord) Shadow(SciFiGold.copy(alpha = titleGlow), blurRadius = 25f) else null
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isNewRecord) {
                        Text("NEW PERSONAL BEST", color = SciFiGold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
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
                    Text(if (isZenMode) "ZEN RECORD" else "RECORD SCORE", color = SciFiGold.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$highScore", color = SciFiGold, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(12.dp))
                    Text("TELEMETRY PROCESSED BY ASHWATH.AI", color = SciFiWhite.copy(alpha = 0.15f), fontSize = 7.sp, letterSpacing = 1.sp)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = (if (isZenMode) SciFiPurple else SciFiBorder).copy(alpha = 0.3f), thickness = 1.dp)
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

            Spacer(Modifier.height(16.dp))

            val buttonsAnim by animateFloatAsState(if (startAnims) 0f else 100f, tween(800, 400, FastOutSlowInEasing), label = "ButtonsAnim")
            
            Column(Modifier.offset(y = buttonsAnim.dp).graphicsLayer(alpha = if (startAnims) 1f else 0f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isZenMode) {
                    val earnedContinues = (runBossesDefeated / 5) + (bestComboThisRun / 15)
                    val maxContinues = (if (isPremiumUser) 5 else 3) + earnedContinues
                    val isFreeContinue = isPremiumUser && continuesUsed == 0
                    val continuesRemaining = maxContinues - continuesUsed

                    if (continuesUsed < maxContinues) {
                        val scope = rememberCoroutineScope()
                        var isAdLoading by remember { mutableStateOf(false) }
                        val hasCredits = progressionManager.creditBalance > 0

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
                                                    if (adRetryCount >= 2) {
                                                        isAdLoading = false
                                                        onContinue()
                                                    } else {
                                                        adRetryCount++
                                                        RewardedAdHelper.load(context)
                                                        scope.launch {
                                                            delay(1000)
                                                            RewardedAdHelper.show(activity,
                                                                analytics = analytics,
                                                                onReward = { isAdLoading = false; onContinue() },
                                                                onFailed = { 
                                                                    if (adRetryCount >= 2) {
                                                                        isAdLoading = false
                                                                        onContinue()
                                                                    } else {
                                                                        adRetryCount++
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
                                            onContinue()
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
                                    Text("FREE CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("[AD]", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                                        Spacer(Modifier.padding(start = 8.dp))
                                        Text("WATCH AD TO CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }

                        if (continuesRemaining > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Continue ${continuesUsed + 1} of $maxContinues",
                                    color = SciFiWhite.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (!isPremiumUser) {
                                    Spacer(Modifier.width(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { showUpgradeDialog = true }
                                    ) {
                                        Text(
                                            text = "(GO PREMIUM FOR 5)",
                                            color = SciFiGold.copy(alpha = 0.6f),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        if (purchaseManager?.hasOffer == true) {
                                            Spacer(Modifier.width(6.dp))
                                            DiscountFlyer(
                                                text = purchaseManager.offerText,
                                                urgencyText = purchaseManager.offerExpiryText
                                            )
                                        }
                                    }
                                }
                            }
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
                }

                // Credit management row (Hidden in Zen)
                if (!isZenMode) {
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
                                        onReward = { progressionManager.addCredits(1) },
                                        onFailed = {}
                                    )
                                }
                            },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan.copy(alpha = 0.2f), contentColor = SciFiCyan),
                            border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.4f))
                        ) {
                            Text("+1 CREDIT [AD]", fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // NEW EXPEDITION BUTTON
                var isRestartAdLoading by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        if (isZenMode && !isPremiumUser) {
                            isRestartAdLoading = true
                            val activity = context.findActivity()
                            if (activity != null) {
                                RewardedAdHelper.show(activity,
                                    analytics = analytics,
                                    onReward = {
                                        isRestartAdLoading = false
                                        onRestart()
                                    },
                                    onFailed = {
                                        isRestartAdLoading = false
                                        onRestart() // Fallback to free for now to avoid blocking
                                    }
                                )
                            } else {
                                onRestart()
                            }
                        } else {
                            onRestart()
                        }
                    },
                    enabled = !isRestartAdLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isZenMode) SciFiPurple.copy(alpha = 0.2f) else SciFiSurface),
                    border = BorderStroke(1.dp, if (isZenMode) SciFiPurple.copy(alpha = 0.5f) else SciFiBorder)
                ) {
                    if (isRestartAdLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SciFiWhite, strokeWidth = 2.dp)
                    } else {
                        val label = if (isZenMode) "RE-DEPLOY ZEN MODE" else "NEW EXPEDITION"
                        val adHint = if (isZenMode && !isPremiumUser) " [AD]" else ""
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$label$adHint", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            if (isZenMode && !isPremiumUser) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showUpgradeDialog = true }
                                ) {
                                    Text(
                                        "REMOVE ADS WITH PREMIUM",
                                        color = SciFiGold.copy(alpha = 0.5f),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    if (purchaseManager?.hasOffer == true) {
                                        Spacer(Modifier.width(6.dp))
                                        DiscountFlyer(
                                            text = purchaseManager.offerText,
                                            urgencyText = purchaseManager.offerExpiryText
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                }
                Spacer(Modifier.height(8.dp))
                GlobalAdBanner()
            }
        }

        if (showUpgradeDialog) {
            EliteUpgradeDialog(
                purchaseManager = purchaseManager,
                onDismiss = { showUpgradeDialog = false }
            )
        }

        // --- FLYING REWARD ANIMATIONS ---
        flyingUiRewards.forEach { fr ->
            var animProgress by remember { mutableStateOf(0f) }
            LaunchedEffect(fr.id) {
                animate(0f, 1f, animationSpec = tween(800, easing = FastOutSlowInEasing)) { value, _ ->
                    animProgress = value
                }
                flyingUiRewards.remove(fr)
            }
            
            val startX = 0.dp
            val startY = 100.dp
            val targetX = if (fr.isCredit) (-140).dp else 140.dp
            val targetY = (-300).dp
            
            Box(
                modifier = Modifier
                    .offset(
                        x = startX + (targetX - startX) * animProgress,
                        y = startY + (targetY - startY) * animProgress
                    )
                    .size(16.dp)
                    .background(fr.color, CircleShape)
                    .graphicsLayer(
                        scaleX = 1f + animProgress,
                        scaleY = 1f + animProgress,
                        alpha = 1f - animProgress
                    )
                    .zIndex(100f)
            )
        }
    }
}

@Composable
private fun CurrencyBadge(label: String, value: String, color: Color) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = color.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.width(8.dp))
            Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
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
