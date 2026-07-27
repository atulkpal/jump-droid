package com.ashwathai.jump_droid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

@Composable
fun LeaderboardScreen(
    leaderboardManager: LeaderboardManager,
    progressionManager: ProgressionManager,
    cloudSyncManager: CloudSyncManager? = null,
    onDismiss: () -> Unit
) {
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var playerRank by remember { mutableStateOf(0) }
    var totalPlayers by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        cloudSyncManager?.syncAll()
        // Ensure local high score is on the leaderboard when engaging with terminal
        leaderboardManager.submitScore(progressionManager.highScore, progressionManager.highScore)
        
        entries = leaderboardManager.getTopScores()
        val rankInfo = leaderboardManager.getPlayerRank()
        playerRank = rankInfo.first
        totalPlayers = rankInfo.second
        loading = false
    }

    Box(Modifier.fillMaxSize().background(SciFiBackground).safeDrawingPadding()) {
        StarfieldBackground()
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "GLOBAL TERMINAL",
                style = MaterialTheme.typography.headlineMedium,
                color = SciFiCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
            )

            if (leaderboardManager.isOnline()) {
                Text(
                    "$totalPlayers PILOTS IN ORBIT",
                    color = SciFiWhite.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                if (playerRank > 0) {
                    val myScore = entries.find { it.isPlayer }?.highScore ?: 0
                    Text(
                        "YOUR RANK: #$playerRank ($myScore pts)",
                        color = SciFiGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    "OFFLINE \u2014 Sign in to access global rankings",
                    color = SciFiRed.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (loading) {
                    Text(
                        "CONNECTING...",
                        color = SciFiCyan.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (entries.isEmpty()) {
                    Text(
                        if (leaderboardManager.isOnline()) "No scores yet. Launch a run!" else "Sign in to see global rankings.",
                        color = SciFiWhite.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                } else {
                    entries.forEach { entry ->
                        LeaderboardRow(entry)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("DISCONNECT", color = SciFiWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val bg = if (entry.isPlayer) SciFiGold.copy(alpha = 0.08f) else SciFiSurface
    val accent = if (entry.isPlayer) SciFiGold else SciFiBorder
    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, accent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rankColor = when (entry.rank) {
                1 -> SciFiGold
                2 -> SciFiCyan.copy(alpha = 0.8f)
                3 -> SciFiRed.copy(alpha = 0.7f)
                else -> SciFiWhite.copy(alpha = 0.5f)
            }
            Text(
                "#${entry.rank}",
                color = rankColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.width(40.dp)
            )
            Text(
                entry.displayName,
                color = if (entry.isPlayer) SciFiGold else SciFiWhite,
                fontWeight = if (entry.isPlayer) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${entry.highScore}",
                color = SciFiCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
