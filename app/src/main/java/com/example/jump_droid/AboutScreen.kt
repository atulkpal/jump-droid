package com.example.jump_droid

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBackground
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun AboutScreen(onDismiss: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState()).safeDrawingPadding()) {
            Text("MISSION DATA", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Jump Droid is an advanced vertical exploration simulator focusing on precision propulsion control and multi-layered atmospheric discovery.",
                color = SciFiWhite.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(24.dp))
            Text("SYSTEM ARCHITECTURE: JETPACK COMPOSE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 12.sp)
            Text("CORE ENGINE: ASHWATH.AI PROTOTYPE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 12.sp)
            Spacer(Modifier.height(32.dp))
            Text("PROTOCOL VERSION", style = MaterialTheme.typography.titleLarge, color = SciFiGold, fontWeight = FontWeight.Bold)
            Text("V1.2.0 // ASCENSION EXPANSION", color = SciFiWhite.copy(alpha = 0.6f))
            Spacer(Modifier.height(32.dp))
            Text("PROJECT ROADMAP", style = MaterialTheme.typography.titleLarge, color = SciFiGold, fontWeight = FontWeight.Bold)
            Text("• ADVANCED ENTITY AI\n• DEEP SPACE ANOMALIES\n• INTERSTELLAR TERMINAL", color = SciFiWhite.copy(alpha = 0.6f), lineHeight = 24.sp)
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
            ) {
                Text("DISMISS", color = SciFiWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}
