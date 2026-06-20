package com.example.jump_droid.missions.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.jump_droid.missions.Mission
import com.example.jump_droid.missions.MissionManager
import com.example.jump_droid.missions.MissionState
import com.example.jump_droid.missions.MissionTier
import com.example.jump_droid.ui.theme.SciFiBackground
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun MissionScreen(
    missionManager: MissionManager,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }

    val visibleMissions = remember(refreshKey) { missionManager.getVisibleMissions() }
    val completedCount = missionManager.getCompletedCount()
    val totalVisible = missionManager.getTotalVisibleCount()

    Column(modifier = Modifier.fillMaxSize().background(SciFiBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Missions", color = SciFiWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onDismiss) {
                Text("Close", color = SciFiCyan, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                "$completedCount / $totalVisible Complete",
                color = Color(0xFF9E9E9E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(12.dp))
        val overallPct = if (totalVisible > 0) completedCount.toFloat() / totalVisible else 0f
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(Color(0x1AFFFFFF))
        ) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(overallPct)
                .clip(RoundedCornerShape(3.dp)).background(Color(0xFF2196F3)))
        }
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
        ) {
            items(visibleMissions, key = { it.id }) { mission ->
                val state = missionManager.getMissionState(mission.id)
                val progress = missionManager.getProgressPercentage(mission.id)
                val rawProgress = missionManager.getProgress(mission.id)
                val isClaimable = state != MissionState.CLAIMED && (
                    state == MissionState.COMPLETED ||
                    (state == MissionState.IN_PROGRESS && progress > 0.99f) ||
                    (rawProgress >= mission.objective.targetValue)
                )
                MissionCard(mission, state, progress, isClaimable, onClaim = {
                    missionManager.claimRewards(mission.id, scope)
                    refreshKey++
                })
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: Mission,
    state: MissionState,
    progress: Float,
    isClaimable: Boolean,
    onClaim: () -> Unit
) {
    val isHiddenLocked = mission.isHidden && state == MissionState.LOCKED
    val isLocked = state == MissionState.LOCKED && !isHiddenLocked
    val tierColor = when (mission.tier) {
        MissionTier.TIER_1 -> SciFiGreen
        MissionTier.TIER_2 -> SciFiCyan
        MissionTier.TIER_3 -> Color(0xFF9C27B0)
        MissionTier.TIER_4 -> SciFiGold
    }

    val showClaim = isClaimable
    val bgGlow by animateFloatAsState(targetValue = if (showClaim) 1f else 0f, label = "bgGlow")

    CardContent(
        mission = mission,
        state = state,
        progress = progress,
        tierColor = tierColor,
        isHiddenLocked = isHiddenLocked,
        isLocked = isLocked,
        showClaim = showClaim,
        bgGlow = bgGlow,
        onClick = { if (!isHiddenLocked && !isLocked) onClaim() },
        cardShape = CardShape
    )
}

@Composable
private fun CardContent(
    mission: Mission,
    state: MissionState,
    progress: Float,
    tierColor: Color,
    isHiddenLocked: Boolean,
    isLocked: Boolean,
    showClaim: Boolean,
    bgGlow: Float,
    onClick: () -> Unit,
    cardShape: RoundedCornerShape
) {
    val bgColor = when {
        isHiddenLocked -> Color(0xFF0A0E14)
        showClaim -> tierColor.copy(alpha = bgGlow * 0.08f + 0.04f)
        state == MissionState.CLAIMED -> tierColor.copy(alpha = 0.04f)
        else -> Color(0xCC1A1A2E)
    }
    val borderMod = when {
        isHiddenLocked -> Modifier.border(0.5.dp, SciFiRed.copy(alpha = 0.25f), cardShape)
        isLocked -> Modifier.border(0.5.dp, Color(0x33FFFFFF), cardShape)
        showClaim -> Modifier.border(1.5.dp, tierColor.copy(alpha = bgGlow * 0.4f + 0.3f), cardShape)
        state == MissionState.CLAIMED -> Modifier.border(0.5.dp, tierColor.copy(alpha = 0.15f), cardShape)
        else -> Modifier.border(0.5.dp, Color(0x26FFFFFF), cardShape)
    }

    Column(
        modifier = Modifier
            .aspectRatio(0.7f)
            .clip(cardShape)
            .background(bgColor)
            .then(borderMod)
            .clickable(enabled = !isHiddenLocked && !isLocked) { onClick() }
            .padding(12.dp)
    ) {
        // Top row: icon left + tier pill right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(if (mission.icon.isNotEmpty()) mission.icon else "\u2708\uFE0F", fontSize = 22.sp)
            if (!isHiddenLocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isLocked) Color(0x1AFFFFFF) else tierColor.copy(alpha = 0.1f))
                        .border(0.5.dp, if (isLocked) Color(0x33FFFFFF) else tierColor.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isLocked) "LOCKED" else mission.tier.displayName,
                        color = if (isLocked) Color(0xFF9E9E9E) else tierColor.copy(alpha = 0.85f),
                        fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Mission name
        val nameColor = when {
            isHiddenLocked -> SciFiRed.copy(alpha = 0.7f)
            isLocked -> Color(0xFF9E9E9E)
            state == MissionState.CLAIMED -> tierColor.copy(alpha = 0.5f)
            else -> SciFiWhite
        }
        Text(
            mission.name,
            color = nameColor,
            fontSize = 14.sp,
            fontWeight = if (showClaim) FontWeight.Black else FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (isHiddenLocked) {
            Spacer(Modifier.height(4.dp))
            Text("SIGNAL LOST", color = SciFiRed.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }

        Spacer(Modifier.weight(1f))

        if (!isHiddenLocked && !isLocked) {
            // Progress bar
            val barColor = when {
                showClaim || state == MissionState.CLAIMED -> tierColor
                progress > 0.7f -> SciFiGold
                progress > 0f -> SciFiCyan
                else -> tierColor.copy(alpha = 0.2f)
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(Color(0x1AFFFFFF))
            ) {
                Box(modifier = Modifier.fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp)).background(barColor))
            }

            Spacer(Modifier.height(6.dp))

            if (showClaim) {
                // Full-width CLAIM button
                Box(
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tierColor.copy(alpha = 0.2f))
                        .border(0.5.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CLAIM ${(progress * 100).toInt()}%",
                        color = tierColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Bottom row: % left + state text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = if (progress == 0f) Color(0x669E9E9E) else Color(0xFF9E9E9E),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        when (state) {
                            MissionState.CLAIMED -> "\u2713 Done"
                            MissionState.IN_PROGRESS -> "\u25CF Active"
                            else -> ""
                        },
                        color = when (state) {
                            MissionState.CLAIMED -> tierColor.copy(alpha = 0.5f)
                            MissionState.IN_PROGRESS -> SciFiCyan
                            else -> Color(0x669E9E9E)
                        },
                        fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isLocked) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("\uD83D\uDD12", fontSize = 20.sp)
            }
        }
    }
}
