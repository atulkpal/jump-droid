package com.ashwathai.jump_droid

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

private enum class TerminalTab { LOCAL, GLOBAL }

@Composable
fun LeaderboardScreen(
    leaderboardManager: LeaderboardManager,
    progressionManager: ProgressionManager,
    cloudSyncManager: CloudSyncManager? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    
    var selectedTab by remember { mutableStateOf(TerminalTab.LOCAL) }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var myRank by remember { mutableStateOf(0 to 0) }
    
    val isOnline = leaderboardManager.isOnline()
    val isSignedIn = leaderboardManager.loginManager.isSignedIn

    LaunchedEffect(selectedTab) {
        if (selectedTab == TerminalTab.GLOBAL && isOnline) {
            isLoading = true
            entries = leaderboardManager.getTopScores()
            myRank = leaderboardManager.getPlayerRank()
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize().background(SciFiBackground).safeDrawingPadding()) {
        StarfieldBackground()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "FLEET TERMINAL",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SciFiCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "PILOT TELEMETRY // DATA RELAY",
                        color = SciFiCyan.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // UPLINK STATUS HUD
                Surface(
                    color = (if (isOnline) SciFiGreen else SciFiRed).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, (if (isOnline) SciFiGreen else SciFiRed).copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(if (isOnline) SciFiGreen else SciFiRed, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) "UPLINK: ACTIVE" else "UPLINK: SEVERED",
                            color = if (isOnline) SciFiGreen else SciFiRed,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tab Selector
            Row(Modifier.fillMaxWidth().height(40.dp)) {
                TerminalTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isSelected) SciFiCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .drawBehind {
                                if (isSelected) {
                                    drawLine(SciFiCyan, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name + " TELEMETRY",
                            color = if (isSelected) SciFiCyan else SciFiWhite.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (selectedTab == TerminalTab.LOCAL) {
                LocalTelemetryContent(progressionManager, Modifier.weight(1f))
            } else {
                GlobalTerminalContent(entries, isLoading, isSignedIn, isOnline, myRank, leaderboardManager, progressionManager, activity, Modifier.weight(1f))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("DISCONNECT TERMINAL", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()
        }
    }
}

@Composable
private fun LocalTelemetryContent(progressionManager: ProgressionManager, modifier: Modifier = Modifier) {
    val stats = progressionManager.statRecord
    val infiniteTransition = rememberInfiniteTransition(label = "TelemetryLife")
    
    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50L)
            frameTime.value += 50
        }
    }
    val ft = frameTime.value / 1000f

    // Radar Sweep Animation
    val radarProgress by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "Radar")
    
    // Header Glitch
    val glitchVal by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(2000)), label = "Glitch")
    val isGlitching = (glitchVal * 100).toInt() % 15 == 0

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            Text(
                "PILOT COMMAND CENTER // SECTOR: LOCAL",
                color = if (isGlitching) SciFiGold else SciFiCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .offset(x = if (isGlitching) 2.dp else 0.dp)
                    .graphicsLayer(alpha = if (isGlitching) 0.7f else 1f)
            )
        }

        // 2x4 MAIN TELEMETRY GRID
        val totalDistStr = if (stats.lifetimeAltitude >= 10000) {
            String.format(java.util.Locale.US, "%.1fkm", stats.lifetimeAltitude / 1000f)
        } else {
            "${stats.lifetimeAltitude}m"
        }

        Box(Modifier.fillMaxWidth()) {
            // Radar Background Effect
            Canvas(Modifier.matchParentSize().graphicsLayer(alpha = 0.15f)) {
                val lineY = size.height * radarProgress
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, SciFiCyan, Color.Transparent),
                        startY = lineY - 20f,
                        endY = lineY + 20f
                    ),
                    size = Size(size.width, 40f),
                    topLeft = Offset(0f, lineY - 20f)
                )
            }

            TelemetryGrid(
                listOf(
                    TelemetryItem("BEST ASCENT", "${progressionManager.highAltitude}m", SciFiCyan),
                    TelemetryItem("TOTAL DISTANCE", totalDistStr, SciFiGreen),
                    TelemetryItem("EXPEDITIONS", "${stats.totalRuns}", SciFiGold),
                    TelemetryItem("MAX SCORE", "${progressionManager.highScore}", SciFiPurple),
                    TelemetryItem("MAX COMBO", "${stats.maxComboEver}x", SciFiGreen),
                    TelemetryItem("ARTIFACTS", "${stats.lifetimeArtifacts}", SciFiGold),
                    TelemetryItem("CASH EARNED", "${stats.lifetimeCashEarned} JC", SciFiGold),
                    TelemetryItem("PERFECT LANDINGS", "${stats.perfectLandings}", SciFiCyan)
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        // HISTORICAL LOG (TOP 3)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "HISTORICAL EXPEDITION LOG",
                    color = SciFiWhite.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                stats.topRuns.take(3).forEachIndexed { index, score ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "0${index + 1}",
                                color = if (score > 0) SciFiGold else SciFiWhite.copy(alpha = 0.2f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (score > 0) "PILOT MASTERY" else "NO DATA",
                                color = SciFiWhite.copy(alpha = if (score > 0) 0.8f else 0.2f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            if (score > 0) "${score}" else "---",
                            color = if (score > 0) SciFiWhite else SciFiWhite.copy(alpha = 0.2f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (index < 2) HorizontalDivider(color = SciFiBorder.copy(alpha = 0.1f), thickness = 0.5.dp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        // BOSS DISCOVERY PROGRESS
        val allBossesList = remember { ThreatRegistry.getAll().filter { it.type == ThreatType.BOSS || it.type == ThreatType.MINI_BOSS } }
        val totalBosses = allBossesList.size
        val encounteredCount = allBossesList.count { 
            stats.uniqueBossesKilled.containsKey(it.id) || 
            stats.uniqueBossesEscaped.containsKey(it.id) || 
            stats.killedByBossMap.containsKey(it.id) 
        }
        val discoveryProgress = encounteredCount.toFloat() / totalBosses.toFloat()
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SciFiSurface.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("THREAT DISCOVERY PROGRESS", color = SciFiPurple, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text("$encounteredCount / $totalBosses", color = SciFiPurple, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(4.dp)) {
                    LinearProgressIndicator(
                        progress = { discoveryProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = SciFiPurple,
                        trackColor = SciFiPurple.copy(alpha = 0.1f)
                    )
                    // Scanning Particle
                    Canvas(Modifier.fillMaxSize()) {
                        val particleX = size.width * discoveryProgress * ((sin(ft * 4f) * 0.5f + 0.5f))
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(particleX, size.height / 2),
                            alpha = 0.8f
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        // THREAT NEUTRALIZATION LOG (Detailed Table)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "THREAT NEUTRALIZATION LOG",
                    color = SciFiCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                
                // Table Header
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("THREAT IDENTIFIER", Modifier.weight(1.5f), color = SciFiWhite.copy(alpha = 0.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text("SLAYED", Modifier.weight(1f), color = SciFiWhite.copy(alpha = 0.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("ESCAPED", Modifier.weight(1f), color = SciFiWhite.copy(alpha = 0.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("DEFEATS", Modifier.weight(1f), color = SciFiWhite.copy(alpha = 0.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }

                val allBosses = ThreatRegistry.getAll().filter { it.type == ThreatType.BOSS || it.type == ThreatType.MINI_BOSS }
                val encounteredBosses = allBosses.filter { def ->
                    stats.uniqueBossesKilled.containsKey(def.id) || 
                    stats.uniqueBossesEscaped.containsKey(def.id) || 
                    stats.killedByBossMap.containsKey(def.id)
                }

                if (encounteredBosses.isEmpty()) {
                    Text(
                        "NO DATA ENCOUNTERED",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        color = SciFiWhite.copy(alpha = 0.2f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                encounteredBosses.forEach { def ->
                    val kills = stats.uniqueBossesKilled.getOrDefault(def.id, 0)
                    val escapes = stats.uniqueBossesEscaped.getOrDefault(def.id, 0)
                    val defeats = stats.killedByBossMap.getOrDefault(def.id, 0)
                    
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = def.name.uppercase(),
                            modifier = Modifier.weight(1.5f),
                            color = SciFiWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "x$kills",
                            modifier = Modifier.weight(1f),
                            color = if (kills > 0) SciFiGold else SciFiWhite.copy(alpha = 0.1f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "x$escapes",
                            modifier = Modifier.weight(1f),
                            color = if (escapes > 0) SciFiPurple else SciFiWhite.copy(alpha = 0.1f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "x$defeats",
                            modifier = Modifier.weight(1f),
                            color = if (defeats > 0) SciFiRed else SciFiWhite.copy(alpha = 0.1f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    HorizontalDivider(color = SciFiBorder.copy(alpha = 0.05f), thickness = 0.5.dp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

private data class TelemetryItem(val label: String, val value: String, val color: Color)

@Composable
private fun TelemetryGrid(items: List<TelemetryItem>) {
    Column(Modifier.fillMaxWidth()) {
        for (i in items.indices step 2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items.getOrNull(i)?.let { TelemetryCard(it, Modifier.weight(1f), index = i) }
                items.getOrNull(i + 1)?.let { TelemetryCard(it, Modifier.weight(1f), index = i + 1) }
            }
            if (i < items.size - 2) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TelemetryCard(item: TelemetryItem, modifier: Modifier = Modifier, index: Int = 0) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100L * index)
        visible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.height(56.dp),
            color = SciFiSurface.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, item.color.copy(alpha = 0.15f))
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(item.label, color = item.color.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text(item.value, color = SciFiWhite, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun GlobalTerminalContent(
    entries: List<LeaderboardEntry>,
    isLoading: Boolean,
    isSignedIn: Boolean,
    isOnline: Boolean,
    myRank: Pair<Int, Int>,
    leaderboardManager: LeaderboardManager,
    progressionManager: ProgressionManager,
    activity: Activity?,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SciFiCyan)
            }
        } else if (!isSignedIn) {
            // REQUIRED CTA: ESTABLISH PILOT ID
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, SciFiRed.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_station_trm),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SciFiRed
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "FLEET SYNC UNAVAILABLE",
                            style = MaterialTheme.typography.titleMedium,
                            color = SciFiRed,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "ESTABLISH PILOT ID TO VIEW GLOBAL TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = SciFiRed.copy(alpha = 0.6f),
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Unauthorized access restricted. Connect to the fleet network to synchronize your flight data with other pilots.",
                            color = SciFiWhite.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = {
                                activity?.let {
                                    leaderboardManager.loginManager.triggerPlayGamesSignIn()
                                    it.startActivityForResult(leaderboardManager.loginManager.getSignInIntent(), 9001) // Simplified for now
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiRed.copy(alpha = 0.2f), contentColor = SciFiRed),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SciFiRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("ESTABLISH PILOT UPLINK", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        } else if (entries.isEmpty()) {
            // LOCAL TELEMETRY FALLBACK
            Column(Modifier.fillMaxSize()) {
                Text(
                    "FETCHING CLOUD DATA...",
                    color = SciFiWhite.copy(alpha = 0.3f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SciFiCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCAL",
                            color = SciFiCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(40.dp)
                        )
                        Text(
                            text = (leaderboardManager.loginManager.displayName ?: "YOU").uppercase(),
                            color = SciFiWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${progressionManager.highScore}m",
                            color = SciFiGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Text(
                    "NO REMOTE PILOTS DETECTED IN SECTOR",
                    color = SciFiWhite.copy(alpha = 0.2f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Leaderboard List
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
            ) {
                Column {
                    // Table Header
                    Row(Modifier.fillMaxWidth().background(SciFiSurface).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("RANK", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(40.dp))
                        Text("PILOT", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("ALTITUDE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
                    }
                    
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(entries) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .background(if (entry.isPlayer) SciFiCyan.copy(alpha = 0.1f) else Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${entry.rank}",
                                    color = when(entry.rank) { 1 -> SciFiGold; 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32); else -> SciFiWhite.copy(alpha = 0.6f) },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    text = entry.displayName.uppercase(),
                                    color = if (entry.isPlayer) SciFiCyan else SciFiWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${entry.highScore}m",
                                    color = SciFiGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                            HorizontalDivider(color = SciFiBorder.copy(alpha = 0.1f))
                        }
                    }
                }
            }
            
            // Footer: My Rank
            if (myRank.first > 0) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SciFiCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("YOUR POSITION:", color = SciFiCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("#${myRank.first} OF ${myRank.second}", color = SciFiWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
