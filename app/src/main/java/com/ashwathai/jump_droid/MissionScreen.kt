package com.ashwathai.jump_droid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.sin

private val CardShape = RoundedCornerShape(10.dp)

private data class MissionTrack(
    val name: String,
    val iconRes: Int,
    val color: Color,
    val categories: List<MissionCategory>
)

private val MISSION_TRACKS = listOf(
    MissionTrack("Aeronautics", R.drawable.ic_track_aero, SciFiCyan, listOf(MissionCategory.FLIGHT_TIME, MissionCategory.NO_HEAT)),
    MissionTrack("Ground Support", R.drawable.ic_track_ground, SciFiWhite, listOf(MissionCategory.PLATFORM_STAY)),
    MissionTrack("Resource Mgmt", R.drawable.ic_track_resource, SciFiGreen, listOf(MissionCategory.FUEL_EFFICIENCY)),
    MissionTrack("Combo Mastery", R.drawable.ic_track_combo, SciFiGold, listOf(MissionCategory.COMBO_STREAK, MissionCategory.COMBO_PRO)),
    MissionTrack("Elite Combat", R.drawable.ic_track_combat, SciFiRed, listOf(MissionCategory.BOSS_SLAYER)),
    MissionTrack("Surveying", R.drawable.ic_track_survey, SciFiPurple, listOf(MissionCategory.DISCOVERY_HUNTER)),
    MissionTrack("Ascension Path", R.drawable.ic_track_climb, SciFiCyan, listOf(MissionCategory.ALTITUDE_CLIMBER)),
    MissionTrack("Kinetic Control", R.drawable.ic_track_kinetic, SciFiOrange, listOf(MissionCategory.MOMENTUM_MASTER, MissionCategory.BOOST_CHAMPION)),
    MissionTrack("Reinforcement", R.drawable.ic_track_defense, SciFiGreen, listOf(MissionCategory.HAZARD_SURVIVOR)),
    MissionTrack("Precision Flight", R.drawable.ic_track_precision, SciFiGold, listOf(MissionCategory.PERFECT_RUN)),
    MissionTrack("Archeology", R.drawable.ic_track_archeo, SciFiPurple, listOf(MissionCategory.COLLECTOR))
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
                    Text("MISSION LOG", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("JOURNAL: ASCENSION PATH", color = SciFiWhite.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                }
                IconButton(onClick = onDismiss) {
                    Text("\u2715", color = SciFiWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SciFiSurface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.1f))
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

            Spacer(Modifier.height(12.dp))

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                MISSION_TRACKS.forEachIndexed { index, track ->
                    val currentMission = missionManager.getBestMissionForTrack(allMissions, track.categories)
                    if (currentMission != null) {
                        TimelineNode(
                            track = track,
                            mission = currentMission,
                            isLast = index == MISSION_TRACKS.lastIndex,
                            onClaim = {
                                missionManager.claimMissionRewards(currentMission.id, player)
                                claimEffectAlpha = 1f
                                claimEffectText = "${track.name.uppercase()} \u2014 CLAIMED"
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

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

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = SciFiButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.5f))
            ) {
                Text("BACK TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(4.dp))
            GlobalAdBanner()

            if (claimEffectAlpha > 0f) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(SciFiGold.copy(alpha = claimEffectAlpha * 0.15f), RoundedCornerShape(100.dp))
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
private fun TimelineNode(
    track: MissionTrack,
    mission: Mission,
    isLast: Boolean,
    onClaim: () -> Unit
) {
    val isClaimable = mission.isCompleted && !mission.isClaimed
    val bgGlow by animateFloatAsState(targetValue = if (isClaimable) 1f else 0f, label = "bgGlow")
    val pulse = sin(System.nanoTime().toFloat() / 500_000_000f) * 0.3f + 0.7f

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Timeline line and dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp).fillMaxHeight()
        ) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(if (isClaimable) 14.dp else 10.dp)
                    .background(
                        if (mission.isClaimed) track.color.copy(alpha = 0.5f)
                        else if (isClaimable) track.color.copy(alpha = pulse)
                        else track.color.copy(alpha = 0.3f),
                        RoundedCornerShape(50)
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(track.color.copy(alpha = 0.15f))
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Track card
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .clickable(enabled = isClaimable) { onClaim() },
            color = if (isClaimable) SciFiGold.copy(alpha = 0.05f + 0.1f * bgGlow)
                    else if (mission.isClaimed) SciFiSurface.copy(alpha = 0.4f)
                    else SciFiSurface,
            shape = CardShape,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                when {
                    isClaimable -> SciFiGold.copy(alpha = 0.4f + 0.4f * bgGlow)
                    mission.isClaimed -> SciFiBorder.copy(alpha = 0.05f)
                    else -> track.color.copy(alpha = 0.2f)
                }
            )
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = track.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(track.name.uppercase(), color = track.color, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (mission.isClaimed) "DONE" else mission.tier.displayName,
                        color = if (mission.isClaimed) SciFiGreen else SciFiWhite.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    if (isClaimable) {
                        Text("CLAIM", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
                    } else if (mission.isClaimed) {
                        Text("MAXED", color = SciFiGreen, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (mission.isClaimed) "Track complete." else mission.description,
                    color = SciFiWhite.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!mission.isClaimed) {
                    Spacer(Modifier.height(6.dp))
                    val pct = (mission.currentProgress.toFloat() / mission.targetValue).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (mission.isCompleted) SciFiGreen else track.color,
                        trackColor = Color.White.copy(alpha = 0.05f)
                    )

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
                    val rewardDisplay = rewardTexts.joinToString(" + ")
                    if (rewardDisplay.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))
                        Text("NEXT: $rewardDisplay", color = SciFiGold.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
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
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("HIDDEN SIGNALS", color = SciFiPurple, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 6.dp))

        missions.forEach { mission ->
            val isClaimable = mission.isCompleted && !mission.isClaimed
            val bgGlow by animateFloatAsState(targetValue = if (isClaimable) 1f else 0f, label = "bgGlow")

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable(enabled = isClaimable) { onClaim(mission.id) },
                color = when {
                    isClaimable -> SciFiGold.copy(alpha = 0.05f + 0.1f * bgGlow)
                    !mission.isUnlocked -> Color.Black.copy(alpha = 0.4f)
                    else -> SciFiSurface
                },
                shape = CardShape,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        isClaimable -> SciFiGold.copy(alpha = 0.4f + 0.4f * bgGlow)
                        !mission.isUnlocked -> SciFiRed.copy(alpha = 0.2f)
                        else -> SciFiPurple.copy(alpha = 0.2f)
                    }
                )
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (mission.isUnlocked) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_track_survey),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(end = 12.dp)
                        )
                    } else {
                        Text("\uD83D\uDD12", fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        if (!mission.isUnlocked) {
                            GlitchText(text = "SIGNAL LOST", style = MaterialTheme.typography.labelLarge.copy(color = SciFiRed, fontWeight = FontWeight.Black, letterSpacing = 1.sp))
                            Text(mission.crypticHint, color = SciFiPurple.copy(alpha = 0.7f), fontSize = 9.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        } else {
                            Text(mission.name.uppercase(), color = SciFiPurple, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(
                                text = if (mission.isClaimed) "Decoded & archived." else mission.description,
                                color = SciFiWhite.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!mission.isClaimed) {
                                Spacer(Modifier.height(4.dp))
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
                            Modifier.padding(start = 12.dp).size(width = 72.dp, height = 28.dp)
                                .background(SciFiGold, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("RECOVER", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black) }
                    } else if (mission.isClaimed) {
                        Text("ARCHIVED", color = SciFiGreen, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
    }
}
