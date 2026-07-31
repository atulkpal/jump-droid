package com.ashwathai.jump_droid

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MultiplayerScreen(
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val multiplayerManager = engine.multiplayerManager
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var broadcastText by remember { mutableStateOf("") }
    
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Column(
            Modifier
                .padding(24.dp)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "MULTIPLAYER HUB",
                style = MaterialTheme.typography.headlineMedium,
                color = SciFiCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Text(
                if (engine.loginManager.isSignedIn) "CONNECTED AS: ${engine.loginManager.displayName?.uppercase()}" else "UNAUTHORIZED PILOT // OFFLINE",
                style = MaterialTheme.typography.labelSmall,
                color = (if (engine.loginManager.isSignedIn) SciFiCyan else SciFiRed).copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
            
            Text(
                "DATABASE STATUS: ${multiplayerManager.connectionStatus}",
                style = MaterialTheme.typography.labelSmall,
                color = if (multiplayerManager.connectionStatus == "STABLE") SciFiGreen.copy(alpha = 0.4f) else SciFiRed.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
            
            if (!engine.loginManager.isSignedIn) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { engine.loginManager.triggerPlayGamesSignIn() },
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiRed.copy(alpha = 0.2f), contentColor = SciFiRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.5f))
                ) {
                    Text("CONNECT PILOT ID (AUTH REQUIRED)", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // --- Phase 0: Broadcast Test ---
            SectionHeader("GLOBAL BROADCAST TEST")
            Card(
                colors = CardDefaults.cardColors(containerColor = SciFiSurface.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Broadcast a real-time message to every pilot currently online. This uses the Realtime Database sync layer.",
                        color = SciFiWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = broadcastText,
                        onValueChange = { broadcastText = it },
                        label = { Text("Enter Frequency Message") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SciFiWhite,
                            unfocusedTextColor = SciFiWhite,
                            focusedBorderColor = SciFiCyan,
                            unfocusedBorderColor = SciFiCyan.copy(alpha = 0.3f)
                        )
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            if (broadcastText.isNotBlank()) {
                                multiplayerManager.sendBroadcast(broadcastText)
                                broadcastText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TRANSMIT SIGNAL", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Received Broadcasts List
            multiplayerManager.broadcastMessages.forEach { broadcast ->
                key(broadcast.timestamp) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SciFiPurple.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiPurple.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(SciFiPurple, RoundedCornerShape(4.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "INCOMING FROM: ${broadcast.senderName.uppercase()}",
                                    color = SciFiPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    broadcast.message,
                                    color = SciFiWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // --- Phase 1: Matchmaking ---
            SectionHeader("VS. BATTLE (ROOMS)")
            
            var joinCode by remember { mutableStateOf("") }
            var isJoining by remember { mutableStateOf(false) }

            if (multiplayerManager.currentRoom == null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { coroutineScope.launch { multiplayerManager.createRoom() } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiGold.copy(alpha = 0.2f), contentColor = SciFiGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiGold.copy(alpha = 0.4f))
                        ) {
                            Text("HOST ROOM", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    HorizontalDivider(color = SciFiWhite.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { if (it.length <= 6) joinCode = it },
                            label = { Text("6-Digit Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SciFiWhite,
                                unfocusedTextColor = SciFiWhite,
                                focusedBorderColor = SciFiCyan,
                                unfocusedBorderColor = SciFiCyan.copy(alpha = 0.3f)
                            )
                        )
                        Button(
                            onClick = { 
                                isJoining = true
                                coroutineScope.launch { 
                                    val success = multiplayerManager.joinRoom(joinCode)
                                    isJoining = false
                                    if (!success) {
                                        android.widget.Toast.makeText(context, "Room not found or full", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = joinCode.length == 6 && !isJoining,
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("JOIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LobbyView(multiplayerManager.currentRoom!!) {
                    // Start game logic will go here
                }
            }
            
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("RETURN TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(
            title,
            color = SciFiWhite.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(SciFiCyan.copy(alpha = 0.2f)))
    }
}

@Composable
fun LobbyView(room: MultiplayerRoom, onStart: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SciFiSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ROOM CODE", color = SciFiGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                room.code,
                color = SciFiWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp
            )
            
            Spacer(Modifier.height(24.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PlayerSlot(room.hostName, "HOST", true)
                Text("VS", color = SciFiGold, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Black)
                PlayerSlot(room.guestName ?: "WAITING...", "GUEST", room.guestId != null)
            }
            
            Spacer(Modifier.height(32.dp))
            
            if (room.status == RoomStatus.STARTING) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiGreen)
                ) {
                    Text("START MISSION", fontWeight = FontWeight.Black)
                }
            } else {
                Text(
                    "WAITING FOR OPPONENT...",
                    color = SciFiWhite.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlayerSlot(name: String, label: String, isReady: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(60.dp)
                .background(if (isReady) SciFiCyan.copy(alpha = 0.2f) else SciFiSurface, RoundedCornerShape(30.dp))
                .border(1.dp, if (isReady) SciFiCyan else SciFiWhite.copy(alpha = 0.2f), RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isReady) "\u2713" else "?",
                color = if (isReady) SciFiCyan else SciFiWhite.copy(alpha = 0.2f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = SciFiWhite.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(name, color = SciFiWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
