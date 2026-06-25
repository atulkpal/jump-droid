package com.example.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val zonePalette = mapOf(
    AltitudeZone.EARTH to Color(0xFF4CAF50),
    AltitudeZone.CLOUD_LAYER to Color(0xFF00BCD4),
    AltitudeZone.UPPER_ATMOSPHERE to Color(0xFF9C27B0),
    AltitudeZone.ORBIT to Color(0xFFFFD700),
    AltitudeZone.DEEP_SPACE to Color(0xFF673AB7),
    AltitudeZone.VOID to Color(0xFFD32F2F)
)

private enum class DevCategory { THREATS, POWERUPS, PLATFORMS }

private val categoryLabels = mapOf(
    DevCategory.THREATS to "Threats",
    DevCategory.POWERUPS to "Power-Ups",
    DevCategory.PLATFORMS to "Platforms"
)

private fun threatLabel(id: String): String {
    val prefix = id.substringBefore("_")
    val name = id.substringAfter("_").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    return when (prefix) {
        "HAZ" -> "[H] $name"
        "ENT" -> "[E] $name"
        "MINI" -> "[M] ${id.substringAfterLast("_").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}"
        "BOSS" -> "[B] ${id.substringAfterLast("_").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}"
        else -> id
    }
}

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
    onSpawnDevPowerUp: (PowerUpType) -> Unit,
    onSpawnDevPlatform: (PlatformType) -> Unit,
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

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).pointerInput(Unit) {}, contentAlignment = Alignment.Center) {
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 30, alphaRange = 0.1f..0.4f, starColor = accent)
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(accent.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.8f, h * 0.2f))
            drawCircle(accent.copy(alpha = 0.02f), radius = 50f, center = Offset(w * 0.15f, h * 0.75f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.safeDrawingPadding().width(300.dp)
        ) {
            if (showDevMenu && cheatsEnabled) {
                DevMenuContent(
                    zone = zone,
                    infiniteFuel = infiniteFuel,
                    disableHeat = disableHeat,
                    infiniteShield = infiniteShield,
                    invincibleHull = invincibleHull,
                    onJumpToZone = onJumpToZone,
                    onSpawnDevThreat = onSpawnDevThreat,
                    onSpawnDevPowerUp = onSpawnDevPowerUp,
                    onSpawnDevPlatform = onSpawnDevPlatform,
                    onToggleInfiniteFuel = onToggleInfiniteFuel,
                    onToggleDisableHeat = onToggleDisableHeat,
                    onToggleInfiniteShield = onToggleInfiniteShield,
                    onToggleInvincibleHull = onToggleInvincibleHull,
                    onUnlockAll = onUnlockAll,
                    onClose = onToggleDevMenu
                )
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

@Composable
private fun DevMenuContent(
    zone: AltitudeZone,
    infiniteFuel: Boolean,
    disableHeat: Boolean,
    infiniteShield: Boolean,
    invincibleHull: Boolean,
    onJumpToZone: (AltitudeZone) -> Unit,
    onSpawnDevThreat: (String) -> Unit,
    onSpawnDevPowerUp: (PowerUpType) -> Unit,
    onSpawnDevPlatform: (PlatformType) -> Unit,
    onToggleInfiniteFuel: () -> Unit,
    onToggleDisableHeat: () -> Unit,
    onToggleInfiniteShield: () -> Unit,
    onToggleInvincibleHull: () -> Unit,
    onUnlockAll: () -> Unit,
    onClose: () -> Unit
) {
    var zoneExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var itemExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(DevCategory.THREATS) }
    var selectedThreatId by remember { mutableStateOf<String?>(null) }
    var selectedPowerUp by remember { mutableStateOf<PowerUpType?>(null) }
    var selectedPlatform by remember { mutableStateOf<PlatformType?>(null) }

    val allThreats = remember { ThreatRegistry.getAll().sortedBy { it.id } }
    val allPowerUps = remember { PowerUpType.entries }
    val allPlatforms = remember { PlatformType.entries }

    Text("DEV MENU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(10.dp))

    // Zone dropdown
    DropdownField(
        label = "Zone",
        displayText = zone.name,
        expanded = zoneExpanded,
        onToggle = { zoneExpanded = !zoneExpanded },
        onDismiss = { zoneExpanded = false }
    ) {
        AltitudeZone.entries.forEach { z ->
            MenuItem(
                text = z.name,
                color = zonePalette[z] ?: Color.White,
                onClick = { onJumpToZone(z); zoneExpanded = false }
            )
        }
    }

    Spacer(Modifier.height(6.dp))

    // Category dropdown
    DropdownField(
        label = "Type",
        displayText = categoryLabels[selectedCategory] ?: "",
        expanded = categoryExpanded,
        onToggle = { categoryExpanded = !categoryExpanded },
        onDismiss = { categoryExpanded = false }
    ) {
        DevCategory.entries.forEach { cat ->
            MenuItem(
                text = categoryLabels[cat] ?: "",
                onClick = { selectedCategory = cat; categoryExpanded = false; itemExpanded = false }
            )
        }
    }

    Spacer(Modifier.height(6.dp))

    // Item dropdown + spawn button
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (selectedCategory) {
            DevCategory.THREATS -> {
                DropdownField(
                    label = "Entity",
                    displayText = selectedThreatId?.let { threatLabel(it) } ?: "Pick...",
                    expanded = itemExpanded,
                    onToggle = { itemExpanded = !itemExpanded },
                    onDismiss = { itemExpanded = false },
                    modifier = Modifier.weight(1f)
                ) {
                    var lastPrefix = ""
                    allThreats.forEach { t ->
                        val prefix = t.id.substringBefore("_")
                        if (prefix != lastPrefix) {
                            lastPrefix = prefix
                            MenuItem(
                                text = when (prefix) {
                                    "HAZ" -> "── HAZARDS ──"
                                    "ENT" -> "── ENEMIES ──"
                                    "MINI" -> "── MINI-BOSSES ──"
                                    "BOSS" -> "── BOSSES ──"
                                    else -> "── OTHER ──"
                                },
                                fontSize = 10.sp,
                                color = Color.Gray,
                                enabled = false,
                                onClick = {}
                            )
                        }
                        MenuItem(
                            text = threatLabel(t.id),
                            fontSize = 12.sp,
                            onClick = { selectedThreatId = t.id; itemExpanded = false }
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                val canSpawn = selectedThreatId != null
                Button(
                    onClick = { selectedThreatId?.let { onSpawnDevThreat(it) } },
                    enabled = canSpawn,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (canSpawn) SciFiRed.copy(alpha = 0.6f) else Color(0xFF333333)),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("GO", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }

            DevCategory.POWERUPS -> {
                DropdownField(
                    label = "Power-Up",
                    displayText = selectedPowerUp?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Pick...",
                    expanded = itemExpanded,
                    onToggle = { itemExpanded = !itemExpanded },
                    onDismiss = { itemExpanded = false },
                    modifier = Modifier.weight(1f)
                ) {
                    allPowerUps.forEach { p ->
                        MenuItem(
                            text = p.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            onClick = { selectedPowerUp = p; itemExpanded = false }
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                val canSpawn = selectedPowerUp != null
                Button(
                    onClick = { selectedPowerUp?.let { onSpawnDevPowerUp(it) } },
                    enabled = canSpawn,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (canSpawn) SciFiGreen.copy(alpha = 0.6f) else Color(0xFF333333)),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("GO", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }

            DevCategory.PLATFORMS -> {
                DropdownField(
                    label = "Platform",
                    displayText = selectedPlatform?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Pick...",
                    expanded = itemExpanded,
                    onToggle = { itemExpanded = !itemExpanded },
                    onDismiss = { itemExpanded = false },
                    modifier = Modifier.weight(1f)
                ) {
                    allPlatforms.forEach { p ->
                        MenuItem(
                            text = p.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            onClick = { selectedPlatform = p; itemExpanded = false }
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                val canSpawn = selectedPlatform != null
                Button(
                    onClick = { selectedPlatform?.let { onSpawnDevPlatform(it) } },
                    enabled = canSpawn,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (canSpawn) SciFiGold.copy(alpha = 0.6f) else Color(0xFF333333)),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("GO", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Toggles — compact chip row
    Text("CHEATS", color = Color.Gray, fontSize = 10.sp)
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.weight(1f)) { CompactToggle(active = infiniteFuel, label = "FUEL:INF", color = SciFiCyan, onClick = onToggleInfiniteFuel) }
        Box(Modifier.weight(1f)) { CompactToggle(active = disableHeat, label = "HEAT:OFF", color = SciFiRed, onClick = onToggleDisableHeat) }
        Box(Modifier.weight(1f)) { CompactToggle(active = infiniteShield, label = "SHIELD:∞", color = SciFiGreen, onClick = onToggleInfiniteShield) }
        Box(Modifier.weight(1f)) { CompactToggle(active = invincibleHull, label = "HULL:∞", color = SciFiGold, onClick = onToggleInvincibleHull) }
    }
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Button(
            onClick = onUnlockAll,
            modifier = Modifier.weight(1f).height(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
            shape = RoundedCornerShape(4.dp)
        ) { Text("UNLOCK ALL", fontSize = 10.sp) }
        Button(
            onClick = onClose,
            modifier = Modifier.weight(1f).height(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
            shape = RoundedCornerShape(4.dp)
        ) { Text("CLOSE", fontSize = 10.sp) }
    }
}

@Composable
private fun DropdownField(
    label: String,
    displayText: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFF555555)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(displayText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("▼", fontSize = 9.sp, color = Color.Gray)
            }
        }
        // Custom dropdown panel (avoids Popup placement issues)
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF555555), RoundedCornerShape(6.dp))
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun CompactToggle(
    active: Boolean,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) color.copy(alpha = 0.5f) else Color(0xFF222222),
            contentColor = if (active) Color.White else Color(0xFF888888)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    color: Color = Color.White,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = if (enabled) color else color.copy(alpha = 0.4f)
        )
    }
}
