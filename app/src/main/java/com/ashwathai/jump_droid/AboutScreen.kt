package com.ashwathai.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiButtonShape
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
@Composable
fun AboutScreen(onDismiss: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 40, alphaRange = 0.15f..0.55f, starColor = SciFiCyan)
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(SciFiGold.copy(alpha = 0.03f), radius = 50f, center = Offset(w * 0.2f, h * 0.3f))
            }

            Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState()).safeDrawingPadding()) {
                Text(
                    "SYSTEM PROTOCOL",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(SciFiCyan.copy(alpha = 0.4f), blurRadius = 12f)
                    ),
                    color = SciFiCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Jump Droid is an advanced vertical exploration simulator focusing on precision propulsion control and multi-layered atmospheric discovery.\n\nProject developed to explore modular physics and tiered progression systems in reactive UI environments.",
                    color = SciFiWhite.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
                Spacer(Modifier.height(24.dp))
                Text("SYSTEM ARCHITECTURE: JETPACK COMPOSE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 12.sp, letterSpacing = 1.sp)
                Text("CORE ENGINE: ASHWATH.AI PROTOTYPE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(32.dp))
                Text("PROTOCOL VERSION", style = MaterialTheme.typography.titleLarge, color = SciFiGold, fontWeight = FontWeight.Bold)
                Text("V1.2.0 // ASCENSION EXPANSION", color = SciFiWhite.copy(alpha = 0.6f), letterSpacing = 1.sp)
                Spacer(Modifier.height(32.dp))
                Text("PROJECT ROADMAP", style = MaterialTheme.typography.titleLarge, color = SciFiGold, fontWeight = FontWeight.Bold)
                Text("• ADVANCED ENTITY AI\n• DEEP SPACE ANOMALIES\n• INTERSTELLAR TERMINAL\n• FLEET EXPANSION", color = SciFiWhite.copy(alpha = 0.6f), lineHeight = 24.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = SciFiButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                ) {
                    Text("DISMISS", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text("POWERED BY ASHWATH.AI // V1.2.0", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                GlobalAdBanner()
            }
        }
    }
}
