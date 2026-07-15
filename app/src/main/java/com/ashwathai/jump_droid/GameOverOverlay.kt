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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.sin
import kotlin.random.Random
@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    progressionManager: ProgressionManager,
    continuesUsed: Int,
    isPremiumUser: Boolean = false,
    runBossesDefeated: Int = 0,
    bestComboThisRun: Int = 0,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
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
                text = "SIGNAL LOST AT ALTITUDE $score",
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
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINAL ALTITUDE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$score", color = SciFiWhite, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("RECORD ALTITUDE", color = SciFiGold.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$highScore", color = SciFiGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = SciFiBorder.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RANK", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text(progressionManager.currentRank.title.split(" ").last(), color = SciFiGold, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COLLECTION", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text("${progressionManager.getTotalCompletionPercentage()}%", color = SciFiCyan, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val (found, _) = progressionManager.getCompletionStats("AREAS")
                            Text("ZONES", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text("$found", color = SciFiCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            val earnedContinues = (runBossesDefeated / 5) + (bestComboThisRun / 15)
            val maxContinues = (if (isPremiumUser) 5 else 3) + earnedContinues
            val isFreeContinue = isPremiumUser && continuesUsed == 0
            val continuesRemaining = maxContinues - continuesUsed

            if (continuesUsed < maxContinues) {
                val context = LocalContext.current
                var retryCount by remember { mutableStateOf(0) }

                if (!isFreeContinue) {
                    LaunchedEffect(retryCount, continuesUsed) { RewardedAdHelper.load(context) }
                }

                val failureMessage = when (retryCount) {
                    0 -> null
                    1 -> "AD UNAVAILABLE — LINK WEAK"
                    2 -> "ONE ATTEMPT REMAINING"
                    else -> null
                }

                Button(
                    onClick = {
                        if (isFreeContinue) {
                            onContinue()
                        } else {
                            analytics.logAdClicked("rewarded", AdConfig.REWARDED_UNIT_ID)
                            RewardedAdHelper.show(context as Activity,
                                analytics = analytics,
                                onReward = onContinue,
                                onFailed = {
                                    if (retryCount >= 2) {
                                        onContinue()
                                    } else {
                                        retryCount++
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFreeContinue) SciFiGold else SciFiCyan
                    )
                ) {
                    if (isFreeContinue) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("FREE CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("[AD]", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                            Spacer(Modifier.padding(start = 8.dp))
                            Text(if (retryCount >= 2) "FORCED RELINK" else "RE-ESTABLISH LINK", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
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
                            putExtra(Intent.EXTRA_TEXT, "I reached altitude $score in Jump Droid! 🚀 Can you beat me?\nhttps://jump-droid.vercel.app")
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
