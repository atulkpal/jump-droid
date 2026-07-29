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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*

@Composable
fun LeaderboardScreen(
    leaderboardManager: LeaderboardManager,
    progressionManager: ProgressionManager,
    cloudSyncManager: CloudSyncManager? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var myRank by remember { mutableStateOf(0 to 0) }
    
    val isOnline = leaderboardManager.isOnline()
    val isSignedIn = leaderboardManager.loginManager.isSignedIn

    LaunchedEffect(Unit) {
        if (isOnline) {
            entries = leaderboardManager.getTopScores()
            myRank = leaderboardManager.getPlayerRank()
        }
        isLoading = false
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
                        "GLOBAL TERMINAL",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SciFiCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "CONNECTED PILOTS // LIVE TELEMETRY",
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

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SciFiCyan)
                }
            } else if (!isSignedIn) {
                // REQUIRED CTA: ESTABLISH PILOT ID
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
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
                Column(Modifier.weight(1f)) {
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
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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
                Spacer(Modifier.height(16.dp))
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
