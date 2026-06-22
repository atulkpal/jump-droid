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
    progressionManager: ProgressionManager,
    player: Player,
    onDismiss: () -> Unit
) {
    val allMissions = missionManager.getAllMissions()
    
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
                    
                    SummaryItem("CLAIMABLE", claimable.toString(), if (claimable > 0) SciFiGold else SciFiWhite.copy(alpha = 0.4f))
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
                    val currentMission = allMissions
                        .filter { it.category in track.categories && !it.isClaimed }
                        .sortedBy { it.tier.ordinal }
                        .firstOrNull() ?: allMissions
                        .filter { it.category in track.categories }
                        .sortedByDescending { it.tier.ordinal }
                        .firstOrNull()

                    if (currentMission != null) {
                        TrackRow(
                            track = track,
                            mission = currentMission,
                            onClaim = {
                                missionManager.claimMissionRewards(currentMission.id, progressionManager, player)
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
                            missionManager.claimMissionRewards(id, progressionManager, player)
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
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 7.sp, color = SciFiWhite.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(value, fontSize = 16.sp, color = color, fontWeight = FontWeight.Black)
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
                        fontSize = 8.sp,
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
                    val rewardText = mission.rewards.firstOrNull()?.let {
                        when (it) {
                            is MissionReward.Cash -> "+${it.amount} CASH"
                            is MissionReward.ModuleUnlock -> "MODULE UNLOCK"
                            is MissionReward.Artifact -> "ARTIFACT: ${it.discoveryType.title}"
                            is MissionReward.PowerUp -> "${it.type.name.replace("_", " ")}"
                            is MissionReward.Unlock -> "ROCKET: ${it.rocketType.title}"
                            is MissionReward.Achievement -> "ACHIEVEMENT"
                            else -> ""
                        }
                    } ?: ""
                    if (rewardText.isNotEmpty()) {
                        Text("REWARD: $rewardText", color = SciFiGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
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
    val claimable = missions.find { it.isCompleted && !it.isClaimed }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(enabled = claimable != null) { claimable?.let { onClaim(it.id) } },
        color = if (claimable != null) SciFiPurple.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.3f),
        shape = CardShape,
        border = BorderStroke(1.dp, if (claimable != null) SciFiPurple.copy(alpha = 0.6f) else SciFiPurple.copy(alpha = 0.2f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("📡", fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
            Column(Modifier.weight(1f)) {
                Text("HIDDEN SIGNALS", color = SciFiPurple, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("$count / $total SIGNALS RECOVERED", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 9.sp)
                if (claimable != null) {
                    Text("ENCRYPTION BROKEN - CLICK TO RECOVER DATA", color = SciFiGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
            if (claimable != null) {
                Box(
                    Modifier.size(width = 80.dp, height = 32.dp).background(SciFiGold, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CLAIM", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
