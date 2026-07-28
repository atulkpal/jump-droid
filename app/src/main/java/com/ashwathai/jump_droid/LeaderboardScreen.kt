package com.ashwathai.jump_droid

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    Box(Modifier.fillMaxSize().background(SciFiBackground).safeDrawingPadding()) {
        StarfieldBackground()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Text(
                "GLOBAL TERMINAL",
                style = MaterialTheme.typography.headlineMedium,
                color = SciFiCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            Spacer(Modifier.height(48.dp))

            // Lockdown Modal
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, SciFiOrange.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "Lockdown")
                    val pulse by infiniteTransition.animateFloat(0.6f, 1.2f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "IconPulse")
                    
                    Icon(
                        painter = painterResource(id = R.drawable.ic_station_trm),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).graphicsLayer(scaleX = pulse, scaleY = pulse),
                        tint = SciFiOrange
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        "TERMINAL OFFLINE",
                        style = MaterialTheme.typography.titleLarge,
                        color = SciFiOrange,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    
                    Text(
                        "GLOBAL UPLINK SEVERED // ERROR 404",
                        style = MaterialTheme.typography.labelSmall,
                        color = SciFiOrange.copy(alpha = 0.6f),
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "The Global Pilot Network is currently experiencing heavy interference. Terminal access is restricted until the next software sync.",
                        color = SciFiWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SciFiOrange.copy(alpha = 0.2f), contentColor = SciFiOrange),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SciFiOrange.copy(alpha = 0.4f))
                    ) {
                        Text("RETURN TO COMMAND", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            GlobalAdBanner()
        }
    }
}
