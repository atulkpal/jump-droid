package com.example.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import kotlin.math.sin
import kotlin.random.Random

private val zonePalette = mapOf(
    AltitudeZone.EARTH to Color(0xFF4CAF50),
    AltitudeZone.CLOUD_LAYER to Color(0xFF00BCD4),
    AltitudeZone.UPPER_ATMOSPHERE to Color(0xFF9C27B0),
    AltitudeZone.ORBIT to Color(0xFFFFD700),
    AltitudeZone.DEEP_SPACE to Color(0xFF673AB7),
    AltitudeZone.VOID to Color(0xFFD32F2F)
)

private data class PauseStar(
    var x: Float, var y: Float, var speed: Float,
    val baseAlpha: Float, val twinklePhase: Float, val size: Float
)

@Composable
fun PauseOverlay(
    showDevMenu: Boolean,
    infiniteFuel: Boolean,
    disableHeat: Boolean,
    infiniteShield: Boolean,
    invincibleHull: Boolean,
    cheatsEnabled: Boolean,
    onToggleDevMenu: () -> Unit,
    onJumpToZone: (AltitudeZone) -> Unit,
    onSpawnDevThreat: (String) -> Unit,
    onToggleInfiniteFuel: () -> Unit,
    onToggleDisableHeat: () -> Unit,
    onToggleInfiniteShield: () -> Unit,
    onToggleInvincibleHull: () -> Unit,
    onUnlockAll: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    zone: AltitudeZone = AltitudeZone.EARTH
) {
    val accent = zonePalette[zone] ?: SciFiCyan
    val infiniteTransition = rememberInfiniteTransition(label = "PauseTransition")
    val titleGlow by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "TitleGlow")

    val stars = remember {
        List(30) {
            PauseStar(
                x = Random.nextFloat() * 2000f,
                y = Random.nextFloat() * 2000f,
                speed = 0.15f + Random.nextFloat() * 0.4f,
                baseAlpha = 0.1f + Random.nextFloat() * 0.3f,
                twinklePhase = Random.nextFloat() * 6.28f,
                size = 0.5f + Random.nextFloat() * 1.5f
            )
        }
    }

    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).pointerInput(Unit) {}, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val ft = frameTime.value / 1000f
            val w = size.width
            val h = size.height

            stars.forEach { s ->
                s.y += s.speed
                if (s.y > h + 10) { s.y = -10f; s.x = Random.nextFloat() * w }
                val twinkle = sin(ft * 2f + s.twinklePhase) * 0.3f + 0.7f
                val alpha = s.baseAlpha * twinkle
                drawCircle(accent.copy(alpha = alpha), radius = s.size, center = Offset(s.x, s.y))
            }

            drawCircle(accent.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.8f, h * 0.2f))
            drawCircle(accent.copy(alpha = 0.02f), radius = 50f, center = Offset(w * 0.15f, h * 0.75f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.safeDrawingPadding().width(280.dp)
        ) {
            if (showDevMenu && cheatsEnabled) {
                Text("DEV MENU", color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("ZONES", color = Color.Gray, fontSize = 10.sp)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    AltitudeZone.entries.forEach { z ->
                        Button(onClick = { onJumpToZone(z) }, Modifier.padding(4.dp), colors = ButtonDefaults.buttonColors(containerColor = zonePalette[z]?.copy(alpha = 0.3f) ?: Color(0xFF333333), contentColor = Color.White)) { Text(z.name, fontSize = 10.sp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("ENTITIES", color = Color.Gray, fontSize = 10.sp)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("ENT_SCOUT_DRONE", "ENT_SWARM_BOTS", "ENT_CLOUD_SKIMMER", "ENT_ORBITAL_SENTRY", "ENT_CORRUPTED_HULL", "ENT_STALKER", "ENT_VOID_WHALE", "ENT_VOID_WRAITH", "HAZ_VOID_ANOMALY", "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL").forEach { id ->
                        Button(onClick = { onSpawnDevThreat(id) }, Modifier.padding(4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text(id.substringAfterLast("_"), fontSize = 10.sp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("TOGGLES", color = Color.Gray, fontSize = 10.sp)
                Row {
                    Button(onClick = onToggleInfiniteFuel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text(if (infiniteFuel) "FUEL:INF" else "FUEL", fontSize = 10.sp) }
                    Spacer(Modifier.width(4.dp))
                    Button(onClick = onToggleDisableHeat, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text(if (disableHeat) "HEAT:OFF" else "HEAT", fontSize = 10.sp) }
                    Spacer(Modifier.width(4.dp))
                    Button(onClick = onToggleInfiniteShield, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text(if (infiniteShield) "SHIELD:ON" else "SHIELD", fontSize = 10.sp) }
                    Spacer(Modifier.width(4.dp))
                    Button(onClick = onToggleInvincibleHull, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text(if (invincibleHull) "HULL:ON" else "HULL", fontSize = 10.sp) }
                    Spacer(Modifier.width(4.dp))
                    Button(onClick = onUnlockAll, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text("UNLOCK", fontSize = 10.sp) }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onToggleDevMenu, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)) { Text("CLOSE") }
            } else {
                Text(
                    text = "SYSTEMS STANDBY",
                    color = SciFiWhite,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(accent.copy(alpha = titleGlow * 0.4f), blurRadius = 14f)
                    ),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "// ${zone.zoneName.uppercase()} PAUSE",
                    color = accent.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp,
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
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = accent.copy(alpha = 0.5f))
                    ) {
                        Text("DEV OVERRIDE", fontSize = 12.sp, letterSpacing = 2.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("MISSION PAUSED // ${zone.zoneName.uppercase()} SECTOR", color = accent.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}
