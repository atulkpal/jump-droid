package com.ashwathai.jump_droid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*

data class CalibrationBuff(
    val id: String,
    val name: String,
    val description: String,
    val icon: String
)

val CALIBRATION_BUFFS = listOf(
    CalibrationBuff("BUFF_FUEL", "OPTIMIZED COMBUSTION", "+10% Fuel Efficiency", "\u26FD"),
    CalibrationBuff("BUFF_HEAT", "THERMAL OVERRIDE", "+15% Heat Dissipation", "\u2623"),
    CalibrationBuff("BUFF_SHIELD", "REINFORCED PLATING", "+20% Shield Capacity", "\u26E8")
)

@Composable
fun TechCalibrationOverlay(
    onBuffSelected: (CalibrationBuff) -> Unit,
    onDismiss: () -> Unit,
    analytics: GameAnalytics
) {
    val context = LocalContext.current

    Surface(
        color = Color.Black.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "DROID CALIBRATION",
                style = MaterialTheme.typography.headlineMedium,
                color = SciFiGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                "SELECT AN EXPEDITION ENHANCEMENT",
                style = MaterialTheme.typography.labelMedium,
                color = SciFiGold.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(32.dp))

            CALIBRATION_BUFFS.forEach { buff ->
                Button(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            AdManager.showRewardedAd(activity, analytics, onReward = {
                                onBuffSelected(buff)
                                analytics.logEvent("tech_calibration_activated", mapOf("buff_id" to buff.id))
                                onDismiss()
                            })
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = BorderStroke(1.dp, SciFiBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(buff.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(buff.name, fontWeight = FontWeight.Bold, color = SciFiWhite)
                            Text(buff.description, fontSize = 10.sp, color = SciFiCyan)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("[AD LINK]", color = SciFiGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = onDismiss) {
                Text("SKIP CALIBRATION", color = SciFiWhite.copy(alpha = 0.5f))
            }
        }
    }
}
