package com.example.jump_droid

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun MissionRow(
    missions: List<Mission>,
    globalShowObjective: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        missions.toList().forEach { activeMission ->
            val cardColor = when (activeMission.ceremonyStage) {
                CeremonyStage.GLOW -> SciFiCyan.copy(alpha = 0.6f)
                else -> SciFiSurface
            }

            AnimatedVisibility(
                visible = activeMission.ceremonyStage != CeremonyStage.REPLACING &&
                        activeMission.ceremonyStage != CeremonyStage.REWARD_SPAWNED,
                enter = slideInVertically { -it } + fadeIn(),
                exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Surface(
                    color = cardColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(115.dp).height(65.dp).shadow(4.dp, RoundedCornerShape(12.dp)),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (activeMission.ceremonyStage == CeremonyStage.GLOW) SciFiWhite else SciFiBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (activeMission.ceremonyStage == CeremonyStage.COMPLETED_TEXT ||
                            activeMission.ceremonyStage == CeremonyStage.REWARD_SPAWNED) {
                            Text(
                                "MISSION COMPLETE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SciFiGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeMission.type.toIcon(),
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                AnimatedContent(
                                    targetState = globalShowObjective,
                                    transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                                    label = "MissionTextRotation"
                                ) { showObj ->
                                    Text(
                                        if (showObj) activeMission.description.uppercase() else activeMission.name.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (activeMission.type) {
                                            MissionType.EXPLORATION -> SciFiCyan
                                            MissionType.PLATFORMING -> SciFiGold
                                            else -> SciFiWhite
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 0.5.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        val progressText = when (activeMission.type) {
                            MissionType.EXPLORATION -> "${(activeMission.currentProgress.toFloat() / activeMission.targetValue * 100).toInt()}%"
                            MissionType.SURVIVAL -> "${activeMission.currentProgress}s"
                            else -> "${activeMission.currentProgress}/${activeMission.targetValue}"
                        }

                        Text(
                            progressText,
                            style = MaterialTheme.typography.labelSmall,
                            color = SciFiWhite.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.width(50.dp).height(2.dp).background(SciFiWhite.copy(alpha = 0.1f), CircleShape)) {
                            Box(
                                Modifier.fillMaxHeight()
                                    .fillMaxWidth((activeMission.currentProgress.toFloat() / activeMission.targetValue).coerceIn(0f, 1f))
                                    .background(
                                        when (activeMission.type) {
                                            MissionType.EXPLORATION -> SciFiCyan
                                            MissionType.PLATFORMING -> SciFiGold
                                            else -> SciFiWhite
                                        },
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
