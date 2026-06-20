package com.example.jump_droid

import android.content.SharedPreferences
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBackground
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HangarScreen(
    player: Player,
    highScore: Int,
    progressionManager: ProgressionManager,
    sharedPrefs: SharedPreferences,
    onNavigate: (GameState) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HangarTransition")
    val accentPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "AccentPulse")

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                repeat(30) {
                    val x = Random.nextFloat() * w
                    val y = Random.nextFloat() * h
                    drawCircle(SciFiCyan.copy(alpha = 0.04f), radius = 0.5f + Random.nextFloat(), center = Offset(x, y))
                }
                drawCircle(SciFiGold.copy(alpha = 0.03f), radius = 60f, center = Offset(w * 0.15f, h * 0.2f))
                drawCircle(SciFiPurple.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.85f, h * 0.8f))
            }

            Column(Modifier.padding(16.dp).safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("ROCKET HANGAR", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(progressionManager.currentRank.title, color = SciFiGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val totalComp = progressionManager.getTotalCompletionPercentage()
                        Text("$totalComp% ARCHIVE COMPLETION", color = SciFiWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("${progressionManager.artifactsCollected.size} ARTIFACTS RECOVERED", color = SciFiPurple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(SciFiSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, SciFiBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val (pFound, pTotal) = progressionManager.getCompletionStats("PLATFORMS")
                    val (zFound, zTotal) = progressionManager.getCompletionStats("AREAS")
                    val (aFound, aTotal) = progressionManager.getCompletionStats("ARTIFACTS")

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PLATFORMS", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                        Text("$pFound/$pTotal", color = SciFiCyan, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ZONES", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                        Text("$zFound/$zTotal", color = SciFiCyan, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ARTIFACTS", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                        Text("$aFound/$aTotal", color = SciFiPurple, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigate(GameState.ARCHIVE) }) {
                        Text("ARCHIVE", color = SciFiCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Text("VIEW >", color = SciFiWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(SciFiSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(1.dp, SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MAX HULL", color = SciFiGreen.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Text("${progressionManager.permanentMaxIntegrity.toInt()}", color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MAX SHIELD", color = SciFiCyan.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Text("${progressionManager.permanentMaxShield.toInt()}", color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REGEN RATE", color = SciFiCyan.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Text("${Constants.SHIELD_REGEN_RATE.toInt()} U/S", color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    RocketType.entries.forEach { type ->
                        val unlocked = highScore >= type.unlockScore || sharedPrefs.getBoolean("unlock_${type.name}", false)
                        val isActive = player.rocketType == type
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable(enabled = unlocked) { player.rocketType = type; onNavigate(GameState.MAIN_MENU) }
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) SciFiCyan else if (unlocked) SciFiBorder else SciFiBorder.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (isActive) SciFiCyan.copy(alpha = 0.1f) else if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Canvas(Modifier.size(48.dp).padding(end = 16.dp)) {
                                    val cx = size.width / 2
                                    val cy = size.height / 2
                                    val bodyW = 12f
                                    val bodyH = 30f
                                    drawRoundRect(
                                        if (unlocked) SciFiWhite.copy(alpha = 0.8f) else SciFiWhite.copy(alpha = 0.3f),
                                        topLeft = Offset(cx - bodyW / 2, cy - bodyH / 2),
                                        size = Size(bodyW, bodyH),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                                    )
                                    drawPath(
                                        Path().apply { moveTo(cx - bodyW / 2, cy - bodyH / 2); lineTo(cx, cy - bodyH / 2 - 12f); lineTo(cx + bodyW / 2, cy - bodyH / 2); close() },
                                        if (unlocked) SciFiRed else SciFiRed.copy(alpha = 0.3f)
                                    )
                                    drawPath(
                                        Path().apply { moveTo(cx - 3f, cy + bodyH / 2); lineTo(cx, cy + bodyH / 2 + 10f); lineTo(cx + 3f, cy + bodyH / 2); close() },
                                        if (unlocked) SciFiGold.copy(alpha = 0.7f) else SciFiGold.copy(alpha = 0.2f)
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(type.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = if (unlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        if (!unlocked) Text("THRESHOLD: ${type.unlockScore}m", color = SciFiRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        else if (isActive) Text("ACTIVE", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 2.sp)
                                    }
                                    if (unlocked) {
                                        Spacer(Modifier.height(12.dp))
                                        Text("THRUST: ${(type.thrustMult * 100).toInt()}% // FUEL: ${(type.fuelMult * 100).toInt()}% // THERMAL: ${(type.heatMult * 100).toInt()}%", color = SciFiCyan.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(type.discovery.description, color = SciFiWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onNavigate(GameState.MAIN_MENU) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = BorderStroke(1.dp, SciFiBorder)
                ) {
                    Text("BACK", color = SciFiWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
