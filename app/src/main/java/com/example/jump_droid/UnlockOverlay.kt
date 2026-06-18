package com.example.jump_droid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun UnlockOverlay(
    unlockedRocket: RocketType?,
    onConfirm: () -> Unit
) {
    if (unlockedRocket == null) return

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable { onConfirm() }, contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SciFiSurface,
            modifier = Modifier.padding(32.dp).safeDrawingPadding().border(1.dp, SciFiBorder, RoundedCornerShape(16.dp))
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NEW ASSET UNLOCKED", color = SciFiGold, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Text(unlockedRocket.title.uppercase(), style = MaterialTheme.typography.headlineMedium, color = SciFiWhite, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                Text("Craft available for immediate deployment in the Hangar.", style = MaterialTheme.typography.bodyMedium, color = SciFiWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                ) {
                    Text("CONFIRM", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
