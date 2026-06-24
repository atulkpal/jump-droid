package com.example.jump_droid

import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBackground
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun ArchiveScreen(
    sharedPrefs: SharedPreferences,
    discoveryManager: DiscoveryManager,
    progressionManager: ProgressionManager,
    onNavigate: (GameState) -> Unit
) {
    val categories = listOf("ROCKETS", "PLATFORMS", "POWERUPS", "AREAS", "THREATS", "ARTIFACTS", "SETS", "LOGS", "LORE", "ACHIEVEMENTS")
    var selectedCat by remember { mutableStateOf("ROCKETS") }
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Column(Modifier.padding(16.dp).safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("DATA ARCHIVE", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                val discovered = DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) }
                Text("$discovered RECORDS FOUND", color = SciFiWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
                categories.forEach { cat ->
                    Text(
                        text = cat,
                        modifier = Modifier
                            .clickable { selectedCat = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .background(if (selectedCat == cat) SciFiCyan.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(4.dp)),
                        color = if (selectedCat == cat) SciFiCyan else SciFiWhite.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (selectedCat == "ACHIEVEMENTS") {
                    AchievementsList.forEach { ach ->
                        val unlocked = sharedPrefs.getBoolean("achievement_${ach.id}", false)
                        CodexCard(ach.title, if (unlocked) ach.description else "DATA ENCRYPTED: REACH OBJECTIVE TO UNLOCK.", "", unlocked)
                    }
                } else if (selectedCat == "SETS") {
                    ArtifactSet.ALL_SETS.forEach { set ->
                        val foundCount = set.discoveries.count { progressionManager.isDiscoveryUnlocked(it.name) }
                        val isComplete = foundCount == set.discoveries.size
                        ArtifactSetCard(set, foundCount, isComplete)
                    }
                } else if (selectedCat == "LOGS") {
                    LoreLog.ALL_LOGS.forEach { log ->
                        val unlocked = sharedPrefs.getBoolean("log_${log.id}", false)
                        val title = if (unlocked) log.title else "SIGNAL LOST"
                        val desc = if (unlocked) log.text else "ENCRYPTED SIGNAL DETECTED AT ${log.unlockAltitude}m."
                        CodexCard(title, desc, if (unlocked) "CATEGORY: ${log.category.name}" else "", unlocked)
                    }
                } else {
                    DiscoveryType.entries.filter { it.category == selectedCat }.forEach { entry ->
                        val unlocked = discoveryManager.isDiscovered(entry)
                        val title = if (unlocked || selectedCat != "THREATS") entry.title else "UNKNOWN SIGNAL"
                        val desc = if (unlocked) entry.description else "DATA CORRUPTED: RECOVER DURING NEXT EXPEDITION."
                        val lore = if (unlocked) entry.lore else ""

                        if (unlocked && selectedCat == "ARTIFACTS") {
                            val record = progressionManager.artifactsCollected[entry.name]
                            val extraInfo = if (record != null) {
                                "\n\nFIRST DISCOVERY: ${record.firstDiscoveryDate}\nRECOVERED: ${record.timesFound} TIMES\nHIGHEST ALT: ${record.highestAltitude}m\nZONE: ${record.zoneFound}"
                            } else ""
                            CodexCard(title, desc, lore + extraInfo, unlocked)
                        } else {
                            CodexCard(title, desc, lore, unlocked)
                        }
                    }
                }
            }

            if (selectedCat != "ACHIEVEMENTS") {
                val (found, total) = progressionManager.getCompletionStats(selectedCat)
                val percent = if (total > 0) (found * 100) / total else 0
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = SciFiSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$selectedCat DATABASE", style = MaterialTheme.typography.labelSmall, color = SciFiCyan)
                            Text("$found / $total", style = MaterialTheme.typography.labelSmall, color = SciFiWhite)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = percent / 100f,
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = SciFiCyan,
                            trackColor = SciFiSurface
                        )
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

@Composable
private fun ArtifactSetCard(set: ArtifactSet, found: Int, complete: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        color = if (complete) SciFiPurple.copy(alpha = 0.15f) else SciFiSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (complete) SciFiPurple else SciFiBorder.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(set.name.uppercase(), color = if (complete) SciFiPurple else SciFiWhite, fontWeight = FontWeight.Black, fontSize = 12.sp)
                if (complete) {
                    Surface(color = com.example.jump_droid.ui.theme.SciFiGreen, shape = RoundedCornerShape(4.dp)) {
                        Text("SET ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Text("$found / ${set.discoveries.size}", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "BONUS: ${set.bonus.description}",
                color = if (complete) com.example.jump_droid.ui.theme.SciFiGold else SciFiWhite.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = found.toFloat() / set.discoveries.size,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (complete) com.example.jump_droid.ui.theme.SciFiGreen else SciFiPurple,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}
