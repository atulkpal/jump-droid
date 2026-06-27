package com.example.jump_droid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.*

private val CardShape = RoundedCornerShape(8.dp)

private data class MissionTrack(
    val name: String,
    val icon: String,
    val categories: List<MissionCategory>
)

private val MISSION_TRACKS = listOf(
    MissionTrack("Aeronautics", "🚀", listOf(MissionCategory.FLIGHT_TIME, MissionCategory.NO_HEAT)),
    MissionTrack("Ground Support", "🏢", listOf(MissionCategory.PLATFORM_STAY)),
    MissionTrack("Resource Mgmt", "🔋", listOf(MissionCategory.FUEL_EFFICIENCY)),
    MissionTrack("Combo Mastery", "🔥", listOf(MissionCategory.COMBO_STREAK, MissionCategory.COMBO_PRO)),
    MissionTrack("Elite Combat", "⚔️", listOf(MissionCategory.BOSS_SLAYER)),
    MissionTrack("Surveying", "📡", listOf(MissionCategory.DISCOVERY_HUNTER)),
    MissionTrack("Ascension Path", "⛰️", listOf(MissionCategory.ALTITUDE_CLIMBER)),
    MissionTrack("Kinetic Control", "💨", listOf(MissionCategory.MOMENTUM_MASTER, MissionCategory.BOOST_CHAMPION)),
    MissionTrack("Reinforcement", "🛡️", listOf(MissionCategory.HAZARD_SURVIVOR)),
    MissionTrack("Precision Flight", "🎯", listOf(MissionCategory.PERFECT_RUN)),
    MissionTrack("Archeology", "🏺", listOf(MissionCategory.COLLECTOR))
)

@Composable
fun MissionScreen(
    missionManager: MissionManager,
    player: Player,
    onDismiss: () -> Unit
) {
    val allMissions = missionManager.getAllMissions()
    var claimEffectAlpha by remember { mutableStateOf(0f) }
    var claimEffectText by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        missionManager.syncState()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SciFiBackground) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "MISSION LOG",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SciFiCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "OPERATIONAL STATUS: ACTIVE",
                        color = SciFiWhite.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Text("✕", color = SciFiWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Dashboard Summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SciFiSurface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.1f))
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    val claimable = allMissions.count { it.isCompleted && !it.isClaimed }
                    val completed = allMissions.count { it.isClaimed }
                    val total = allMissions.size
                    
                    val hiddenClaimable = allMissions.count { it.isHidden && it.isCompleted && !it.isClaimed }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SummaryItem("CLAIMABLE", claimable.toString(), if (claimable > 0) SciFiGold else SciFiWhite.copy(alpha = 0.4f))
                        if (hiddenClaimable > 0) {
                            Spacer(Modifier.width(4.dp))
                            Text("($hiddenClaimable HIDDEN)", color = SciFiPurple.copy(alpha = 0.6f), fontSize = 8.sp)
                        }
                    }
                    SummaryItem("TOTAL COMP", "${(completed * 100 / total)}%", SciFiGreen)
                    SummaryItem("SIGNALS", allMissions.count { it.isHidden && it.isUnlocked }.toString(), SciFiPurple)
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(MISSION_TRACKS) { track ->
                    val currentMission = missionManager.getBestMissionForTrack(allMissions, track.categories)

                    if (currentMission != null) {
                        TrackRow(
                            track = track,
                            mission = currentMission,
                            onClaim = {
                                missionManager.claimMissionRewards(currentMission.id, player)
                                claimEffectAlpha = 1f
                                claimEffectText = "${track.name.uppercase()} — CLAIMED"
                            }
                        )
                    }
                }
                
                item {
                    HiddenSignalsCard(
                        count = allMissions.count { it.isHidden && it.isUnlocked },
                        total = allMissions.count { it.isHidden },
                        missions = allMissions.filter { it.isHidden },
                        onClaim = { id ->
                            missionManager.claimMissionRewards(id, player)
                            claimEffectAlpha = 1f
                            claimEffectText = "SIGNAL RECOVERED"
                        }
                    )
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = SciFiButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.5f))
            ) {
                Text("BACK TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()

            // Claim celebration overlay
            if (claimEffectAlpha > 0f) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                SciFiGold.copy(alpha = claimEffectAlpha * 0.15f),
                                RoundedCornerShape(100.dp)
                            )
                    )
                    Text(
                        text = claimEffectText,
                        color = SciFiGold.copy(alpha = claimEffectAlpha),
                        fontSize = (14 + 6 * claimEffectAlpha).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }
                LaunchedEffect(claimEffectAlpha) {
                    kotlinx.coroutines.delay(800)
                    claimEffectAlpha = 0f
                    claimEffectText = ""
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = SciFiWhite.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TrackRow(
    track: MissionTrack,
    mission: Mission,
    onClaim: () -> Unit
) {
    val isClaimable = mission.isCompleted && !mission.isClaimed
    val bgGlow by animateFloatAsState(targetValue = if (isClaimable) 1f else 0f, label = "bgGlow")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClaimable) { onClaim() },
        color = if (isClaimable) SciFiGold.copy(alpha = 0.05f + 0.1f * bgGlow) else SciFiSurface,
        shape = CardShape,
        border = BorderStroke(1.dp, if (isClaimable) SciFiGold.copy(alpha = 0.4f + 0.4f * bgGlow) else SciFiBorder.copy(alpha = 0.1f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(track.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
            
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(track.name.uppercase(), color = SciFiCyan, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (mission.isClaimed) "MAXED" else mission.tier.displayName,
                        color = if (mission.isClaimed) SciFiGreen else SciFiWhite.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = if (mission.isClaimed) "All track objectives finalized." else mission.description,
                    color = SciFiWhite.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!mission.isClaimed) {
                    Spacer(Modifier.height(4.dp))
                    val rewardTexts = mission.rewards.mapNotNull { reward ->
                        when (reward) {
                            is MissionReward.Cash -> "+${reward.amount} CASH"
                            is MissionReward.ModuleUnlock -> "MODULE"
                            is MissionReward.Artifact -> "ARTIFACT"
                            is MissionReward.PowerUp -> reward.type.name.replace("_", " ")
                            is MissionReward.Unlock -> "ROCKET"
                            is MissionReward.Achievement -> "ACHIEVEMENT"
                            is MissionReward.None -> null
                        }
                    }
                    val extraCount = mission.rewards.count { it !is MissionReward.None } - rewardTexts.size
                    val rewardDisplay = buildString {
                        append(rewardTexts.joinToString(" + "))
                        if (extraCount > 0) append(" +${extraCount} MORE")
                    }
                    if (rewardDisplay.isNotEmpty()) {
                        Text("REWARD: $rewardDisplay", color = SciFiGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(Modifier.height(6.dp))
                    val pct = (mission.currentProgress.toFloat() / mission.targetValue).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (mission.isCompleted) SciFiGreen else SciFiCyan,
                        trackColor = Color.White.copy(alpha = 0.05f)
                    )
                }
            }

            if (isClaimable) {
                Box(
                    Modifier
                        .padding(start = 16.dp)
                        .size(width = 80.dp, height = 32.dp)
                        .background(SciFiGold, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CLAIM", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            } else if (mission.isClaimed) {
                Text(
                    text = "DONE",
                    color = SciFiGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun HiddenSignalsCard(
    count: Int,
    total: Int,
    missions: List<Mission>,
    onClaim: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            "HIDDEN SIGNALS",
            color = SciFiPurple,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        missions.forEach { mission ->
            val isClaimable = mission.isCompleted && !mission.isClaimed
            val bgGlow by animateFloatAsState(targetValue = if (isClaimable) 1f else 0f, label = "bgGlow")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = isClaimable) { onClaim(mission.id) },
                color = when {
                    isClaimable -> SciFiGold.copy(alpha = 0.05f + 0.1f * bgGlow)
                    !mission.isUnlocked -> Color.Black.copy(alpha = 0.4f)
                    else -> SciFiSurface
                },
                shape = CardShape,
                border = BorderStroke(
                    1.dp,
                    when {
                        isClaimable -> SciFiGold.copy(alpha = 0.4f + 0.4f * bgGlow)
                        !mission.isUnlocked -> SciFiRed.copy(alpha = 0.2f)
                        else -> SciFiPurple.copy(alpha = 0.2f)
                    }
                )
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (mission.isUnlocked) "📡" else "🔒",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Column(Modifier.weight(1f)) {
                        if (!mission.isUnlocked) {
                            GlitchText(
                                text = "SIGNAL LOST",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = SciFiRed,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = mission.crypticHint,
                                color = SciFiPurple.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            Text(
                                mission.name.uppercase(),
                                color = SciFiPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (mission.isClaimed) "SIGNAL DECODED & ARCHIVED" else mission.description,
                                color = SciFiWhite.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (!mission.isClaimed) {
                                Spacer(Modifier.height(6.dp))
                                val pct = (mission.currentProgress.toFloat() / mission.targetValue).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                                    color = if (mission.isCompleted) SciFiGreen else SciFiPurple,
                                    trackColor = Color.White.copy(alpha = 0.05f)
                                )
                            }
                        }
                    }

                    if (isClaimable) {
                        Box(
                            Modifier
                                .padding(start = 16.dp)
                                .size(width = 80.dp, height = 32.dp)
                                .background(SciFiGold, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RECOVER", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    } else if (mission.isClaimed) {
                        Text(
                            text = "ARCHIVED",
                            color = SciFiGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
