package com.ashwathai.jump_droid

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
import androidx.compose.foundation.horizontalScroll
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
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiButtonShape
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
@Composable
fun HangarScreen(
    player: Player,
    highScore: Int,
    progressionManager: ProgressionManager,
    sharedPrefs: SharedPreferences,
    onNavigate: (GameState) -> Unit,
    soundManager: SoundManager? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HangarTransition")
    val accentPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "AccentPulse")
    val borderPulse by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "BorderPulse")

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 50, colors = listOf(SciFiCyan, SciFiPurple, SciFiGold))
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
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
                        .background(SciFiSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, SciFiBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val (pFound, pTotal) = progressionManager.getCompletionStats("PLATFORMS")
                    val (zFound, zTotal) = progressionManager.getCompletionStats("AREAS")
                    val (aFound, aTotal) = progressionManager.getCompletionStats("ARTIFACTS")

                    HangarNavWidget(Modifier.clickable { 
                        soundManager?.playSfx("sfx_ui_click")
                        onNavigate(GameState.ARCHIVE) 
                    }, "ARCHIVE", "VIEW >", SciFiCyan)
                    HangarNavWidget(Modifier.clickable { 
                        soundManager?.playSfx("sfx_ui_click")
                        onNavigate(GameState.LOADOUT) 
                    }, "LOADOUT", "EDIT >", SciFiPurple)
                    HangarNavWidget(Modifier.clickable { 
                        soundManager?.playSfx("sfx_ui_click")
                        onNavigate(GameState.MISSIONS) 
                    }, "MISSIONS", "${progressionManager.missionsCompleted} DONE >", SciFiGold)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .border(1.dp, SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val stats = listOf(
                        "MAX HULL" to "${progressionManager.permanentMaxIntegrity.toInt()}",
                        "MAX SHIELD" to "${progressionManager.permanentMaxShield.toInt()}",
                        "REGEN" to "${Constants.SHIELD_REGEN_RATE.toInt()} U/S"
                    )
                    stats.forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, color = SciFiWhite.copy(alpha = 0.5f), fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(value, color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
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
                                        Spacer(Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("TRAIT:", color = SciFiGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                            Spacer(Modifier.width(8.dp))
                                            Text(type.traitName.uppercase(), color = SciFiWhite, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                        Text(type.traitDescription, color = SciFiWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)

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

                val unlockedBlueprints = progressionManager.getUnlockedBlueprints()
                if (unlockedBlueprints.isNotEmpty()) {
                    Text("LOST TECHNOLOGY BLUEPRINTS", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BlueprintType.entries.forEach { type ->
                            if (unlockedBlueprints.contains(type.name)) {
                                BlueprintCard(type)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                Button(
                    onClick = { 
                        soundManager?.playSfx("sfx_ui_confirm")
                        onNavigate(GameState.MAIN_MENU) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = SciFiButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = BorderStroke(1.dp, SciFiBorder.copy(alpha = borderPulse))
                ) {
                    Text("RETURN TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                GlobalAdBanner()
                Spacer(Modifier.height(8.dp))
                Text("HANGAR // ROCKET SELECT // ${progressionManager.currentRank.title}", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun BlueprintCard(type: BlueprintType) {
    Surface(
        modifier = Modifier.size(width = 140.dp, height = 70.dp),
        color = SciFiSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SciFiGold.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(type.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(type.displayName.uppercase(), color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 9.sp, lineHeight = 11.sp)
                Text("ACQUIRED", color = SciFiGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun HangarNavWidget(modifier: Modifier, label: String, sublabel: String, color: Color) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(sublabel, color = SciFiWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
