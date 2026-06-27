package com.example.jump_droid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun HelpOverlay(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SciFiSurface,
            modifier = Modifier.padding(32.dp).safeDrawingPadding().border(1.dp, SciFiBorder, RoundedCornerShape(16.dp))
        ) {
            Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState())) {
                Text("SYSTEM LEGEND", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = SciFiCyan, letterSpacing = 2.sp)

                Spacer(Modifier.height(24.dp))
                Text("PLATFORM TYPES", fontWeight = FontWeight.Bold, color = SciFiWhite, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                val pTypes = listOf("NORMAL" to SciFiGreen, "MOVING" to SciFiCyan, "BOOST" to SciFiGold, "ICE" to SciFiCyan, "BREAKABLE" to SciFiRed, "MAGNETIC" to SciFiPurple, "PHASE" to SciFiWhite)
                pTypes.forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(color, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(24.dp))
                Text("RESOURCE MODULES", fontWeight = FontWeight.Bold, color = SciFiWhite, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                val powerTypes = listOf("FUEL" to SciFiGreen, "TURBO" to SciFiCyan, "EFFICIENCY" to SciFiGreen, "HEAT SINK" to SciFiWhite, "ARTIFACT" to SciFiPurple)
                powerTypes.forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(12.dp))
                        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                ) {
                    Text("CLOSE ARCHIVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                GlobalAdBanner()
            }
        }
    }
}
