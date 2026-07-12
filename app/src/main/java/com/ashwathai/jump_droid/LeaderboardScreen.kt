package com.ashwathai.jump_droid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

@Composable
fun LeaderboardScreen(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(SciFiBackground).safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(300.dp)) {
            Text("GLOBAL TERMINAL", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(48.dp))
            Surface(
                color = SciFiSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CONNECTION STATUS", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("OFFLINE", color = SciFiRed, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Spacer(Modifier.height(24.dp))
                    Text("Worldwide rankings currently unavailable due to atmospheric interference.", color = SciFiWhite.copy(alpha = 0.6f), textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("DISCONNECT", color = SciFiWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()
        }
    }
}
