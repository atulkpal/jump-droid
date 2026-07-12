package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
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
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

private data class CatDef(val label: String, val accent: Color)

@Composable
fun ArchiveScreen(
    sharedPrefs: SharedPreferences,
    discoveryManager: DiscoveryManager,
    progressionManager: ProgressionManager,
    onNavigate: (GameState) -> Unit
) {
    val cats = listOf(
        CatDef("BOSSES", SciFiRed),
        CatDef("ENEMIES", SciFiGold),
        CatDef("HAZARDS", SciFiCyan),
        CatDef("PLATFORMS", SciFiCyan),
        CatDef("ZONES", SciFiGreen),
        CatDef("POWER-UPS", SciFiGold),
        CatDef("MODULES", SciFiPurple),
        CatDef("LORE", SciFiPurple),
        CatDef("MECHANICS", SciFiCyan),
        CatDef("ARTIFACTS", SciFiPurple),
        CatDef("LOGS", SciFiGold),
        CatDef("ACHIEVEMENTS", SciFiGold)
    )
    var selectedCat by remember { mutableStateOf(cats[0]) }
    var selectedDetail by remember { mutableStateOf<EntityDetail?>(null) }
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.padding(16.dp).safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("DATA ARCHIVE", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                val discovered = DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) }
                Text("$discovered RECORDS", color = SciFiWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            val totalDiscovery = DiscoveryType.entries.size
            val totalLore = LoreLog.ALL_LOGS.size
            val totalAchieve = AchievementsList.size
            val overallTotal = totalDiscovery + totalLore + totalAchieve
            val overallFound = DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) } +
                LoreLog.ALL_LOGS.count { sharedPrefs.getBoolean("log_${it.id}", false) } +
                AchievementsList.count { sharedPrefs.getBoolean("achievement_${it.id}", false) }
            val overallPct = if (overallTotal > 0) (overallFound * 100) / overallTotal else 0
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SciFiSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ARCHIVE STATUS", style = MaterialTheme.typography.labelSmall, color = SciFiCyan)
                        Text("$overallFound / $overallTotal", style = MaterialTheme.typography.labelSmall, color = SciFiWhite)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { overallPct / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = SciFiCyan,
                        trackColor = SciFiSurface
                    )
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
                cats.forEach { cat ->
                    val isSelected = cat == selectedCat
                    Text(
                        text = cat.label,
                        modifier = Modifier
                            .clickable { selectedCat = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .background(if (isSelected) cat.accent.copy(alpha = 0.12f) else Color.Transparent, RoundedCornerShape(4.dp)),
                        color = if (isSelected) cat.accent else SciFiWhite.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                when (selectedCat.label) {
                    "LOGS" -> LoreLog.ALL_LOGS.forEach { log ->
                        val unlocked = sharedPrefs.getBoolean("log_${log.id}", false)
                        val title = if (unlocked) log.title else "SIGNAL LOST"
                        val desc = if (unlocked) log.text else "ENCRYPTED SIGNAL DETECTED AT ${log.unlockAltitude}m."
                        ArchiveCard(title, desc, if (unlocked) "CATEGORY: ${log.category.name}" else "", unlocked, selectedCat.accent, onClick = {
                            selectedDetail = EntityDetailRegistry.byDiscovery(DiscoveryType.LOG_GENERIC).copy(
                                id = log.id,
                                archiveRecord = if (unlocked) log.text else "█ SIGNAL LOST",
                                status = "RECORD // ${log.category.name}"
                            )
                        })
                    }
                    "ACHIEVEMENTS" -> AchievementsList.forEach { ach ->
                        val unlocked = sharedPrefs.getBoolean("achievement_${ach.id}", false)
                        ArchiveCard(
                            ach.title,
                            if (unlocked) ach.description else "DATA ENCRYPTED: REACH OBJECTIVE TO UNLOCK.",
                            "",
                            unlocked,
                            selectedCat.accent,
                            onClick = {
                                selectedDetail = EntityDetailRegistry.byDiscovery(DiscoveryType.ACHIEVEMENT_GENERIC).copy(
                                    id = ach.id,
                                    archiveRecord = if (unlocked) ach.description else "█ SIGNAL LOST"
                                )
                            }
                        )
                    }
                    "SETS" -> ArtifactSet.ALL_SETS.forEach { set ->
                        val foundCount = set.discoveries.count { progressionManager.isDiscoveryUnlocked(it.name) }
                        val isComplete = foundCount == set.discoveries.size
                        ArtifactSetCard(set, foundCount, isComplete)
                    }
                    else -> {
                        val entries = filterEntries(selectedCat.label, progressionManager)
                        entries.forEach { entry ->
                            val unlocked = discoveryManager.isDiscovered(entry)
                            val title = if (unlocked) entry.title else "UNKNOWN SIGNAL"
                            val desc = if (unlocked) entry.description else "DATA CORRUPTED — RECOVER DURING NEXT EXPEDITION."
                            val lore = if (unlocked) entry.lore else ""
                            val extraInfo = if (unlocked && entry.category == "ARTIFACTS") {
                                val record = progressionManager.artifactsCollected[entry.name]
                                if (record != null) "\n\nFIRST: ${record.firstDiscoveryDate}\nFOUND: ${record.timesFound}x\nMAX ALT: ${record.highestAltitude}m\nZONE: ${record.zoneFound}" else ""
                            } else ""
                            ArchiveCard(title, desc, lore + extraInfo, unlocked, selectedCat.accent, onClick = { selectedDetail = EntityDetailRegistry.byDiscovery(entry) })
                        }
                    }
                }
            }
            val totalEntries = filterEntries(selectedCat.label, progressionManager).size +
                if (selectedCat.label == "LOGS") LoreLog.ALL_LOGS.size else 0 +
                if (selectedCat.label == "ACHIEVEMENTS") AchievementsList.size else 0
            if (totalEntries > 0 && selectedCat.label != "ACHIEVEMENTS" && selectedCat.label != "LOGS" && selectedCat.label != "SETS") {
                val found = filterEntries(selectedCat.label, progressionManager).count { discoveryManager.isDiscovered(it) }
                val percent = (found * 100) / totalEntries
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = SciFiSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${selectedCat.label} DATABASE", style = MaterialTheme.typography.labelSmall, color = selectedCat.accent)
                            Text("$found / $totalEntries", style = MaterialTheme.typography.labelSmall, color = SciFiWhite)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = selectedCat.accent,
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
            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()
        }
        selectedDetail?.let { detail ->
            EntityDetailPopup(
                detail = detail,
                unlocked = discoveryManager.isDiscovered(detail.discoveryType),
                onDismiss = { selectedDetail = null }
            )
        }
    }
}
}

private fun filterEntries(cat: String, pm: ProgressionManager): List<DiscoveryType> {
    return when (cat) {
        "BOSSES" -> DiscoveryType.entries.filter { it.name.startsWith("THREAT_") }
        "ENEMIES" -> DiscoveryType.entries.filter { it.name.startsWith("ENEMY_") }
        "HAZARDS" -> DiscoveryType.entries.filter { it.name.startsWith("HAZARD_") }
        "PLATFORMS" -> DiscoveryType.entries.filter { it.category == "PLATFORMS" }
        "ZONES" -> DiscoveryType.entries.filter { it.category == "AREAS" }
        "POWER-UPS" -> DiscoveryType.entries.filter { it.category == "POWERUPS" }
        "MODULES" -> DiscoveryType.entries.filter { it.category == "ROCKETS" }
        "LORE" -> DiscoveryType.entries.filter { it.category == "LORE" }
        "MECHANICS" -> DiscoveryType.entries.filter { it.category == "MECHANICS" }
        "ARTIFACTS" -> DiscoveryType.entries.filter { it.category == "ARTIFACTS" }
        else -> emptyList()
    }
}

@Composable
private fun ArchiveCard(
    title: String,
    description: String,
    meta: String,
    unlocked: Boolean,
    accent: Color = SciFiCyan,
    onClick: (() -> Unit)? = null
) {
    val bg = if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.4f)
    val borderC = if (unlocked) accent.copy(alpha = 0.4f) else SciFiBorder.copy(alpha = 0.15f)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderC)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (unlocked) {
                    Box(Modifier.size(6.dp).background(accent, CircleShape))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = title.uppercase(),
                    color = if (unlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f),
                    fontWeight = if (unlocked) FontWeight.Black else FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            if (!unlocked) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    color = SciFiWhite.copy(alpha = 0.25f),
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                if (description.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(description, color = SciFiWhite.copy(alpha = 0.7f), fontSize = 10.sp)
                }
                if (meta.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(meta, color = accent.copy(alpha = 0.5f), fontSize = 8.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun ArtifactSetCard(set: ArtifactSet, found: Int, complete: Boolean) {
    val accent = if (complete) SciFiGreen else SciFiPurple
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
                    Surface(color = SciFiGreen, shape = RoundedCornerShape(4.dp)) {
                        Text("SET ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Text("$found / ${set.discoveries.size}", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "BONUS: ${set.bonus.description}",
                color = if (complete) SciFiGold else SciFiWhite.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { found.toFloat() / set.discoveries.size },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (complete) SciFiGreen else accent,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}
