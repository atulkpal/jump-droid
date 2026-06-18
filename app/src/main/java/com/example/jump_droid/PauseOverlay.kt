package com.example.jump_droid

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun PauseOverlay(
    showDevMenu: Boolean,
    infiniteFuel: Boolean,
    disableHeat: Boolean,
    cheatsEnabled: Boolean,
    onToggleDevMenu: () -> Unit,
    onJumpToZone: (AltitudeZone) -> Unit,
    onSpawnDevThreat: (String) -> Unit,
    onToggleInfiniteFuel: () -> Unit,
    onToggleDisableHeat: () -> Unit,
    onUnlockAll: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).pointerInput(Unit) {}, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.safeDrawingPadding().width(280.dp)
        ) {
            if (showDevMenu && cheatsEnabled) {
                Text("DEVELOPER OVERRIDE", color = SciFiGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    AltitudeZone.entries.forEach { zone ->
                        Button(onClick = { onJumpToZone(zone) }, Modifier.padding(4.dp), colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)) { Text(zone.name, fontSize = 10.sp) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("ENT_SCOUT_DRONE", "ENT_SWARM_BOTS", "ENT_CLOUD_SKIMMER", "ENT_ORBITAL_SENTRY", "ENT_CORRUPTED_HULL", "HAZ_VOID_ANOMALY", "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL").forEach { id ->
                        Button(onClick = { onSpawnDevThreat(id) }, Modifier.padding(4.dp), colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)) { Text(id.substringAfterLast("_"), fontSize = 10.sp) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    Button(onClick = onToggleInfiniteFuel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(infiniteFuel) SciFiGreen.copy(alpha = 0.3f) else SciFiSurface, contentColor = if(infiniteFuel) SciFiGreen else SciFiWhite)) { Text("INF FUEL", fontSize = 10.sp) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onToggleDisableHeat, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(disableHeat) SciFiGreen.copy(alpha = 0.3f) else SciFiSurface, contentColor = if(disableHeat) SciFiGreen else SciFiWhite)) { Text("NO HEAT", fontSize = 10.sp) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onUnlockAll, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)) { Text("UNLOCK", fontSize = 10.sp) }
                }
                Spacer(Modifier.height(32.dp))
                Button(onClick = onToggleDevMenu, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)) { Text("RETURN TO PAUSE", color = Color.Black, fontWeight = FontWeight.Bold) }
            } else {
                Text(
                    text = "SYSTEMS STANDBY",
                    color = SciFiWhite,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(48.dp))

                val pauseButtons = listOf(
                    "RESUME" to onResume,
                    "RESTART RUN" to onRestart,
                    "MAIN MENU" to onMainMenu
                )

                pauseButtons.forEach { (label, action) ->
                    Button(
                        onClick = action,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                    ) {
                        Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (cheatsEnabled) {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onToggleDevMenu,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SciFiGold.copy(alpha = 0.5f))
                    ) {
                        Text("DEV OVERRIDE", fontSize = 12.sp, letterSpacing = 2.sp)
                    }
                }
            }
        }
    }
}
