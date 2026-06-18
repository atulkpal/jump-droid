package com.example.jump_droid

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    progressionManager: ProgressionManager,
    continuesUsed: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "COMMUNICATION LOST",
                color = SciFiRed,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "TELEMETRY DATA ENDED",
                color = SciFiRed.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 4.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )

            Spacer(Modifier.height(48.dp))

            Surface(
                color = SciFiSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SciFiBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINAL ALTITUDE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$score", color = SciFiWhite, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("RECORD ALTITUDE", color = SciFiGold.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("$highScore", color = SciFiGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = SciFiBorder.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RANK", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text(progressionManager.currentRank.title.split(" ").last(), color = SciFiGold, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COLLECTION", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text("${progressionManager.getTotalCompletionPercentage()}%", color = SciFiCyan, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val (found, _) = progressionManager.getCompletionStats("AREAS")
                            Text("ZONES", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text("$found", color = SciFiCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            if (continuesUsed < 1) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                ) {
                    Text("RE-ESTABLISH LINK", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("NEW EXPEDITION", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onMainMenu,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SciFiWhite.copy(alpha = 0.5f))
            ) {
                Text("RETURN TO BASE", fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
            }
        }
    }
}
