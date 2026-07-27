package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HangarScreen(
    player: Player,
    highScore: Int,
    progressionManager: ProgressionManager,
    loadoutManager: LoadoutManager,
    missionManager: MissionManager,
    sharedPrefs: SharedPreferences,
    onNavigate: (GameState) -> Unit,
    soundManager: SoundManager? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "COSMETICS")
    val infiniteTransition = rememberInfiniteTransition(label = "HangarTransition")

    LaunchedEffect(loadoutManager.equippedModuleIds.toList()) {
        player.activeModules.clear()
        loadoutManager.getActiveModules(progressionManager).forEach { module ->
            player.activeModules.add(module)
        }
    }
    val accentPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "AccentPulse")
    val borderPulse by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "BorderPulse")

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 50, colors = listOf(SciFiCyan, SciFiPurple, SciFiGold))
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(SciFiGold.copy(alpha = 0.03f), radius = 60f, center = Offset(w * 0.15f, h * 0.2f))
                drawCircle(SciFiPurple.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.85f, h * 0.8f))
            }

            Column(Modifier.padding(16.dp).safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_btn_hangar),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("ROCKET HANGAR", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text(progressionManager.currentRank.title, color = SciFiGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                    TextButton(onClick = { onNavigate(GameState.MAIN_MENU) }) {
                        Text("\u2715", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 18.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = SciFiCyan,
                    divider = { HorizontalDivider(color = SciFiBorder.copy(alpha = 0.1f), thickness = 0.5.dp) }
                ) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    label,
                                    color = if (selectedTab == index) SciFiCyan else SciFiWhite.copy(alpha = 0.4f),
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> OverviewTab(player, loadoutManager, progressionManager, missionManager, highScore, sharedPrefs, accentPulse, borderPulse, onNavigate, soundManager)
                    1 -> CosmeticsTab(player, progressionManager, highScore, sharedPrefs)
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    player: Player,
    loadoutManager: LoadoutManager,
    progressionManager: ProgressionManager,
    missionManager: MissionManager,
    highScore: Int,
    sharedPrefs: SharedPreferences,
    accentPulse: Float,
    borderPulse: Float,
    onNavigate: (GameState) -> Unit,
    soundManager: SoundManager?
) {
    val rocketRenderer = remember { RocketRenderer() }
    var showModulePicker by remember { mutableStateOf(false) }
    var pickerCategory by remember { mutableStateOf<ModuleCategory?>(null) }
    var pickerSlotIndex by remember { mutableIntStateOf(0) }

    fun resetPicker() { showModulePicker = false; pickerCategory = null }

    val bobTransition = rememberInfiniteTransition(label = "RocketBob")
    val bobY by bobTransition.animateFloat(-3f, 3f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "Bob")
    val currentGameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            currentGameTime.value = System.currentTimeMillis()
            kotlinx.coroutines.delay(50L)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            // Large detailed rocket preview
            Box(
                Modifier.fillMaxWidth().height(300.dp).padding(8.dp)
                    .background(SciFiSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .border(2.dp, SciFiCyan.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val gt = currentGameTime.value
                    val driftX = sin(gt / 3200f * 6.283185f) * 12f
                    val cx = size.width / 2
                    val cy = size.height / 2 - 22f

                    drawRect(Brush.verticalGradient(listOf(Color(0xFF07101E), Color(0xFF101E32), Color(0xFF07101E))), size = size)

                    val leftPanel = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.23f, 0f)
                        lineTo(size.width * 0.16f, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    val rightPanel = Path().apply {
                        moveTo(size.width, 0f)
                        lineTo(size.width * 0.77f, 0f)
                        lineTo(size.width * 0.84f, size.height)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(leftPanel, Color.Black.copy(alpha = 0.12f))
                    drawPath(rightPanel, Color.Black.copy(alpha = 0.16f))
                    drawLine(SciFiCyan.copy(alpha = 0.11f), Offset(size.width * 0.23f, 0f), Offset(size.width * 0.16f, size.height), strokeWidth = 1.2f)
                    drawLine(SciFiCyan.copy(alpha = 0.08f), Offset(size.width * 0.77f, 0f), Offset(size.width * 0.84f, size.height), strokeWidth = 1.2f)

                    val beam = Path().apply {
                        moveTo(size.width * 0.04f, size.height * 0.18f)
                        lineTo(size.width * 0.35f, size.height * 0.31f)
                        lineTo(size.width * 0.23f, size.height * 0.68f)
                        lineTo(size.width * 0.02f, size.height * 0.54f)
                        close()
                    }
                    drawPath(beam, SciFiCyan.copy(alpha = 0.045f))

                    translate(cx, cy + bobY) {
                        val padY = 88f
                        val pad = Path().apply {
                            moveTo(-78f, padY + 18f)
                            lineTo(78f, padY + 18f)
                            lineTo(52f, padY - 5f)
                            lineTo(-52f, padY - 5f)
                            close()
                        }
                        drawOval(Color.Black.copy(alpha = 0.52f), topLeft = Offset(-70f, padY - 18f), size = Size(140f, 24f))
                        drawPath(pad, Color(0xFF07101E).copy(alpha = 0.96f))
                        drawLine(SciFiCyan.copy(alpha = 0.68f), Offset(-52f, padY - 5f), Offset(52f, padY - 5f), strokeWidth = 2.4f)
                        drawLine(SciFiGold.copy(alpha = 0.30f), Offset(-64f, padY + 9f), Offset(64f, padY + 9f), strokeWidth = 1.4f)

                        val glowPulse = 0.20f + sin(gt / 1200f) * 0.05f
                        drawOval(Color(0xFFFF7628).copy(alpha = glowPulse), topLeft = Offset(-22f, 52f), size = Size(44f, 42f))
                        drawOval(SciFiCyan.copy(alpha = 0.13f), topLeft = Offset(-30f, 64f), size = Size(60f, 20f))

                        translate(driftX, 0f) {
                            scale(4.1f, 4.1f, pivot = Offset.Zero) {
                                rocketRenderer.render(this, player, false, Offset.Zero, 0f, gt, offsetOverride = Offset.Zero, isPreview = true)
                            }
                        }

                        val flamePulse = 0.85f + sin(gt / 180f) * 0.15f
                        val outerFlame = Path().apply {
                            moveTo(-13f, 36f)
                            quadraticTo(0f, 72f + flamePulse * 5f, 13f, 36f)
                            quadraticTo(0f, 48f, -13f, 36f)
                            close()
                        }
                        val innerFlame = Path().apply {
                            moveTo(-6f, 40f)
                            quadraticTo(0f, 61f + flamePulse * 4f, 6f, 40f)
                            quadraticTo(0f, 49f, -6f, 40f)
                            close()
                        }
                        drawPath(outerFlame, SciFiCyan.copy(alpha = 0.75f))
                        drawPath(innerFlame, Color(0xFFFFF2A6).copy(alpha = 0.85f))

                        repeat(9) { i ->
                            val seed = i * 149L + gt / 70
                            val rng = kotlin.random.Random(seed)
                            val progress = ((gt / 70f + i * 11f) % 100f) / 100f
                            val px = sin(gt / 420f + i * 1.6f) * 10f
                            val py = 48f + progress * 40f
                            val alpha = (1f - progress) * 0.65f
                            val emberSize = 2f + rng.nextFloat() * 3f * (1f - progress)
                            val emberColor = if (i % 3 == 0) SciFiCyan else Color(0xFFFF9A2A)
                            drawCircle(emberColor.copy(alpha = alpha), radius = emberSize, center = Offset(px, py))
                            drawCircle(SciFiWhite.copy(alpha = alpha * 0.35f), radius = emberSize * 0.45f, center = Offset(px - 0.8f, py - 0.5f))
                        }

                        repeat(3) { i ->
                            val progress = ((gt / 4200f + i * 0.33f) % 1f)
                            val side = if (i % 2 == 0) -1f else 1f
                            val px = side * (28f + progress * 18f)
                            val py = 66f + progress * 18f
                            val alpha = (1f - progress) * 0.18f
                            val puffSize = 8f + progress * 12f
                            drawCircle(Color.White.copy(alpha = alpha), radius = puffSize, center = Offset(px, py))
                            drawCircle(SciFiCyan.copy(alpha = alpha * 0.45f), radius = puffSize * 0.65f, center = Offset(px + side * 3f, py - 2f))
                        }
                    }
                }

                // Inspection label
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 9.dp).padding(horizontal = 16.dp),
                    color = Color.Black.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.18f))
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${player.rocketType.title.uppercase()} · ${player.rocketType.chassisVariants.getOrNull(player.currentChassisIndex)?.name?.uppercase() ?: "STOCK"}",
                            color = SciFiWhite.copy(alpha = 0.55f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        val equippedCount = loadoutManager.equippedModuleIds.count { it != null }
                        Text(
                            "POWER ON · MODULES $equippedCount/2",
                            color = SciFiCyan.copy(alpha = 0.38f),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.6.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

        Spacer(Modifier.height(6.dp))

        // Rocket type selector
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RocketType.entries.forEach { type ->
                val unlocked = highScore >= type.unlockScore || sharedPrefs.getBoolean("unlock_${type.name}", false)
                val isActive = player.rocketType == type
                Surface(
                    modifier = Modifier.weight(1f)
                        .clickable(enabled = unlocked) {
                            if (isActive) return@clickable
                            player.rocketType = type
                            player.currentChassisIndex = 0
                        }
                        .border(1.dp, if (isActive) SciFiCyan else if (unlocked) SciFiBorder.copy(alpha = 0.2f) else SciFiBorder.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                    color = if (isActive) SciFiCyan.copy(alpha = 0.08f) else if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(4.dp))
                        Text(type.title, color = if (unlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!unlocked) Text("${type.unlockScore}m", color = SciFiRed, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        else if (isActive) Text("ACTIVE", color = SciFiCyan, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Split layout: left info card + right PentagonChart
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Top) {
            Surface(Modifier.weight(0.58f), color = SciFiSurface.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(10.dp)) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(player.rocketType.title.uppercase(), color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("ACTIVE", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 7.sp, letterSpacing = 2.sp)
                    }
                    Divider(color = SciFiBorder.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Text(player.rocketType.traitName.uppercase(), color = SciFiGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(player.rocketType.traitDescription, color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)

                    // ⚔ PERFORMANCE (chassis-adjusted)
                    Spacer(Modifier.height(6.dp))
                    Text("\u2694 PERFORMANCE", color = SciFiCyan, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.height(3.dp))
                    val maxPerf = 1.5f
                    val currentThrust = player.rocketType.chassisThrustMult(player.currentChassisIndex)
                    val currentFuel = player.rocketType.chassisFuelMult(player.currentChassisIndex)
                    val currentHeat = player.rocketType.chassisHeatMult(player.currentChassisIndex)
                    val currentIntegrity = player.rocketType.chassisIntegrityMult(player.currentChassisIndex)
                    val currentManeuver = player.rocketType.chassisSteerMult(player.currentChassisIndex)

                    StatBar("THRUST", "${(currentThrust * 100).toInt()}%", currentThrust / maxPerf, SciFiGold, R.drawable.ic_stat_thrust)
                    StatBar("FUEL", "${(currentFuel * 100).toInt()}%", currentFuel / maxPerf, SciFiGreen, R.drawable.ic_hud_fuel)
                    StatBar("THERMAL", "${(1.0f / currentHeat * 100).toInt()}%", (1.0f / currentHeat) / maxPerf, SciFiRed, R.drawable.ic_hud_heat)
                    StatBar("INTEGRITY", "${(currentIntegrity * 100).toInt()}%", currentIntegrity / maxPerf, SciFiGold, R.drawable.ic_hud_hull)
                    StatBar("MANEUVER", "${(currentManeuver * 100).toInt()}%", currentManeuver / maxPerf, SciFiCyan, R.drawable.ic_cat_utility)

                    // CHASSIS picker
                    Spacer(Modifier.height(4.dp))
                    Text("CHASSIS", color = SciFiWhite.copy(alpha = 0.35f), fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(Modifier.height(3.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        player.rocketType.chassisVariants.forEachIndexed { idx, variant ->
                            val isSelected = player.currentChassisIndex == idx
                            Surface(
                                modifier = Modifier.weight(1f)
                                    .clickable { player.currentChassisIndex = idx }
                                    .border(1.dp, if (isSelected) SciFiCyan else SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                color = if (isSelected) SciFiCyan.copy(alpha = 0.08f) else SciFiSurface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(Modifier.padding(vertical = 4.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(variant.name, color = if (isSelected) SciFiCyan else SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 7.sp)
                                    val offsets = listOf(
                                        if (variant.thrustOffset != 0f) "T${if (variant.thrustOffset > 0) "+" else ""}${(variant.thrustOffset * 100).toInt()}%" else "",
                                        if (variant.fuelOffset != 0f) "F${if (variant.fuelOffset > 0) "+" else ""}${(variant.fuelOffset * 100).toInt()}%" else "",
                                        if (variant.heatOffset != 0f) "H${if (variant.heatOffset > 0) "+" else ""}${(variant.heatOffset * 100).toInt()}%" else ""
                                    ).filter { it.isNotEmpty() }.joinToString(" ")
                                    Text(offsets.ifEmpty { "baseline" }, color = SciFiWhite.copy(alpha = 0.3f), fontSize = 6.sp)
                                }
                            }
                        }
                    }

                    // 🛡 DURATIONALS (permanent)
                    Spacer(Modifier.height(6.dp))
                    Divider(color = SciFiBorder.copy(alpha = 0.15f), thickness = 0.5.dp)
                    Spacer(Modifier.height(4.dp))
                    Text("\uD83D\uDEE1 DURATIONALS", color = SciFiCyan, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.height(3.dp))
                    val maxInt = 800f
                    StatBar("HULL", "${progressionManager.permanentMaxIntegrity.toInt()}", progressionManager.permanentMaxIntegrity / maxInt, SciFiGold, R.drawable.ic_hud_hull)
                    StatBar("SHIELD", "${progressionManager.permanentMaxShield.toInt()}", progressionManager.permanentMaxShield / maxInt, SciFiCyan, R.drawable.ic_hud_shield)
                }
            }
            Spacer(Modifier.width(6.dp))
            PentagonChart(
                stats = RocketStats(
                    thrust = player.rocketType.chassisThrustMult(player.currentChassisIndex),
                    fuel = player.rocketType.chassisFuelMult(player.currentChassisIndex),
                    thermal = 1.0f / player.rocketType.chassisHeatMult(player.currentChassisIndex),
                    integrity = player.rocketType.chassisIntegrityMult(player.currentChassisIndex),
                    maneuverability = player.rocketType.chassisSteerMult(player.currentChassisIndex)
                ),
                color = typeColor(player.rocketType),
                modifier = Modifier.weight(0.42f).height(140.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        // 2 module slots
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            loadoutManager.equippedModuleIds.forEachIndexed { index, moduleId ->
                val module = moduleId?.let { ModuleRegistry.getById(it) }
                Box(
                    modifier = Modifier.weight(1f).height(58.dp)
                        .background(SciFiSurface, RoundedCornerShape(10.dp))
                        .border(1.dp, if (module != null) module.iconColor.copy(alpha = 0.4f) else SciFiBorder.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable { showModulePicker = true; pickerSlotIndex = index },
                    contentAlignment = Alignment.Center
                ) {
                    if (module != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Box(Modifier.size(22.dp).background(module.iconColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = categoryIconRes(module.category)),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(module.name.uppercase(), color = module.iconColor, fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(module.category.name, color = SciFiWhite.copy(alpha = 0.3f), fontSize = 7.sp)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Box(Modifier.size(22.dp).background(SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Text("+", color = SciFiWhite.copy(alpha = 0.2f), fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("SLOT ${index + 1}", color = SciFiWhite.copy(alpha = 0.2f), fontSize = 9.sp)
                                Text("TAP TO EQUIP", color = SciFiCyan.copy(alpha = 0.3f), fontSize = 7.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                soundManager?.playSfx("sfx_ui_confirm")
                onNavigate(GameState.MAIN_MENU)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
            border = BorderStroke(1.dp, SciFiCyan.copy(alpha = borderPulse))
        ) {
            Text("RETURN TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(4.dp))
        GlobalAdBanner()
        Spacer(Modifier.height(8.dp))
    }

    // Module picker popup
    if (showModulePicker) {
        ModulePickerPopup(
            loadoutManager = loadoutManager,
            progressionManager = progressionManager,
            missionManager = missionManager,
            selectedCategory = pickerCategory,
            slotIndex = pickerSlotIndex,
            onSelectCategory = { pickerCategory = it },
            onSelectModule = { moduleId ->
                loadoutManager.equipModule(moduleId, pickerSlotIndex)
                resetPicker()
            },
            onDismiss = { resetPicker() }
        )
    }
    }
}

@Composable
private fun ModulePickerPopup(
    loadoutManager: LoadoutManager,
    progressionManager: ProgressionManager,
    missionManager: MissionManager,
    selectedCategory: ModuleCategory?,
    slotIndex: Int,
    onSelectCategory: (ModuleCategory?) -> Unit,
    onSelectModule: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
        Surface(
            Modifier.fillMaxWidth(0.9f).wrapContentHeight().verticalScroll(rememberScrollState()),
            color = SciFiBackground,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                // Header
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedCategory == null) "SLOT ${slotIndex + 1} — SELECT CATEGORY" else selectedCategory.name, color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text("\u2715", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 18.sp, modifier = Modifier.clickable(onClick = onDismiss))
                }
                Spacer(Modifier.height(12.dp))

                if (selectedCategory == null) {
                    val otherSlotIndex = 1 - slotIndex
                    val otherSlotModuleId = loadoutManager.equippedModuleIds.getOrNull(otherSlotIndex)
                    val otherSlotModule = otherSlotModuleId?.let { ModuleRegistry.getById(it) }
                    val otherSlotCategory = otherSlotModule?.category
                    // Category grid
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ModuleCategory.entries.forEach { cat ->
                            val catBlocked = cat == otherSlotCategory
                            Surface(
                                modifier = Modifier.weight(1f)
                                    .clickable { onSelectCategory(cat) }
                                    .border(1.dp, categoryColor(cat).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                color = categoryColor(cat).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(Modifier.padding(8.dp).fillMaxWidth()) {
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(categoryIcon(cat), fontSize = 20.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(cat.name, color = categoryColor(cat), fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (catBlocked) {
                                        Text("S${otherSlotIndex + 1}", color = categoryColor(cat), fontSize = 7.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    // Slot-only unequip section
                    val currentModId = loadoutManager.equippedModuleIds.getOrNull(slotIndex)
                    val currentMod = currentModId?.let { ModuleRegistry.getById(it) }
                    if (currentMod != null) {
                        Text("EQUIPPED", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                                .border(1.dp, currentMod.iconColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            color = currentMod.iconColor.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp).background(currentMod.iconColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = categoryIconRes(currentMod.category)),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(currentMod.name.uppercase(), color = currentMod.iconColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(currentMod.description, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text("UNEQUIP", color = SciFiRed, fontWeight = FontWeight.Black, fontSize = 7.sp, modifier = Modifier.clickable { loadoutManager.unequipModule(slotIndex); onDismiss() })
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)
                    ) { Text("CANCEL", color = SciFiWhite.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                } else {
                    // Module list for selected category
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("\u25C0 BACK", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 9.sp, modifier = Modifier.clickable { onSelectCategory(null) })
                        Spacer(Modifier.weight(1f))
                        Text("${selectedCategory.name} MODULES", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(8.dp))

                    val equippedIds = loadoutManager.equippedModuleIds
                    ModuleRegistry.getAll().filter { it.category == selectedCategory }.forEach { module ->
                        val equippedSlot = equippedIds.indexOf(module.id)
                        val isEquipped = equippedSlot == slotIndex
                        val isEquippedOther = equippedSlot == 1 - slotIndex && equippedSlot >= 0
                        val isUnlocked = loadoutManager.isModuleUnlocked(module, progressionManager, missionManager)

                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                .clickable(isUnlocked && !isEquipped && !isEquippedOther) { onSelectModule(module.id) },
                            color = when { isEquipped || isEquippedOther -> SciFiSurface.copy(alpha = 0.3f); !isUnlocked -> Color.Black.copy(alpha = 0.3f); else -> SciFiSurface },
                            shape = RoundedCornerShape(8.dp),
                            border = if (isUnlocked && !isEquipped && !isEquippedOther) BorderStroke(1.dp, categoryColor(selectedCategory).copy(alpha = 0.15f)) else null
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp).background(if (isUnlocked) module.iconColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                    if (isUnlocked) {
                                        Image(
                                            painter = painterResource(id = categoryIconRes(module.category)),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text("\uD83D\uDD12", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (isUnlocked) module.name else "LOCKED MODULE", color = when { isEquipped -> SciFiWhite.copy(alpha = 0.3f); isEquippedOther -> SciFiWhite.copy(alpha = 0.4f); !isUnlocked -> Color.Gray; else -> SciFiWhite }, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(if (isUnlocked) module.description else formatRequirement(module.unlockRequirement), color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                if (isEquipped) Text("EQPD", color = SciFiCyan, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                else if (isEquippedOther) Text("S${equippedSlot + 1}", color = categoryColor(selectedCategory), fontSize = 7.sp, fontWeight = FontWeight.Black)
                                else if (!isUnlocked) Text("LOCKED", color = SciFiRed.copy(alpha = 0.4f), fontSize = 6.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun categoryColor(category: ModuleCategory): Color = when (category) {
    ModuleCategory.HULL -> Color(0xFF78909C)
    ModuleCategory.SHIELD -> SciFiCyan
    ModuleCategory.ENGINE -> SciFiGold
    ModuleCategory.HEAT -> SciFiRed
    ModuleCategory.UTILITY -> SciFiPurple
}

private fun categoryIcon(category: ModuleCategory): String = when (category) {
    ModuleCategory.HULL -> "\u2B1C"
    ModuleCategory.SHIELD -> "\uD83D\uDEE1"
    ModuleCategory.ENGINE -> "\uD83D\uDD25"
    ModuleCategory.HEAT -> "\u2744"
    ModuleCategory.UTILITY -> "\uD83D\uDD0D"
}

private fun categoryIconRes(category: ModuleCategory): Int = when (category) {
    ModuleCategory.HULL -> R.drawable.ic_cat_hull
    ModuleCategory.SHIELD -> R.drawable.ic_cat_shield
    ModuleCategory.ENGINE -> R.drawable.ic_cat_engine
    ModuleCategory.HEAT -> R.drawable.ic_cat_heat
    ModuleCategory.UTILITY -> R.drawable.ic_cat_utility
}

@Composable
private fun CosmeticsTab(
    player: Player,
    progressionManager: ProgressionManager,
    highScore: Int,
    sharedPrefs: SharedPreferences
) {
    LaunchedEffect(Unit) {
        player.equippedTrailIndex = sharedPrefs.getInt("equipped_trail", 0)
        player.equippedPaintIndex = sharedPrefs.getInt("equipped_paint", 0)
    }

    val totalChassis = RocketType.entries.sumOf { it.chassisVariants.size }
    val unlockedChassis = RocketType.entries.sumOf { cls ->
        if (highScore >= cls.unlockScore || sharedPrefs.getBoolean("unlock_${cls.name}", false))
            cls.chassisVariants.size
        else 0
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("FLEET COLLECTION", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                Text("${unlockedChassis}/${totalChassis} — Chassis Mastery", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 9.sp)
            }
            Text("${(unlockedChassis.toFloat() / totalChassis * 100).toInt()}%", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }

        // Mastery progress bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_premium_star),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.weight(1f).height(4.dp).background(SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
            ) {
                Box(
                    Modifier.fillMaxWidth(fraction = unlockedChassis.toFloat() / totalChassis).fillMaxHeight()
                        .background(SciFiGold, RoundedCornerShape(2.dp))
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Engine Trails
        Text("ENGINE TRAILS", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EngineTrailRegistry.trails.forEachIndexed { idx, trail ->
                val isSelected = player.equippedTrailIndex == idx
                val isUnlocked = progressionManager.isTrailUnlocked(trail.id)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (isSelected) trail.trailColor else if (isUnlocked) SciFiBorder.copy(alpha = 0.15f) else SciFiBorder.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable(enabled = isUnlocked) {
                            player.equippedTrailIndex = idx
                            sharedPrefs.edit().putInt("equipped_trail", idx).commit()
                        },
                    color = if (isSelected) trail.trailColor.copy(alpha = 0.1f) else if (isUnlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(Modifier.size(20.dp)) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            drawCircle(if (isUnlocked) trail.glowColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f), radius = size.minDimension / 2)
                            drawCircle(if (isUnlocked) trail.trailColor else Color.Gray, radius = 4f, center = Offset(cx, cy))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(trail.name, color = if (isSelected) trail.trailColor else if (isUnlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (isSelected) "ACTIVE" else if (isUnlocked) "SELECT" else "\uD83D\uDD12", color = if (isSelected) trail.trailColor.copy(alpha = 0.6f) else SciFiWhite.copy(alpha = 0.1f), fontSize = 6.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("PAINT SCHEMES", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PaintRegistry.paints.forEachIndexed { idx, paint ->
                val isSelected = player.equippedPaintIndex == idx
                val isUnlocked = progressionManager.isPaintUnlocked(paint.id)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (isSelected) paint.accentColor else if (isUnlocked) SciFiBorder.copy(alpha = 0.15f) else SciFiBorder.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable(enabled = isUnlocked) {
                            player.equippedPaintIndex = idx
                            sharedPrefs.edit().putInt("equipped_paint", idx).commit()
                        },
                    color = if (isSelected) paint.hullColor.copy(alpha = 0.15f) else if (isUnlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(20.dp).background(if (isUnlocked) paint.hullColor else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, if (isUnlocked) paint.accentColor.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(paint.name, color = if (isSelected) paint.accentColor else if (isUnlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (isSelected) "ACTIVE" else if (isUnlocked) "SELECT" else "\uD83D\uDD12", color = if (isSelected) paint.accentColor.copy(alpha = 0.6f) else SciFiWhite.copy(alpha = 0.1f), fontSize = 6.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            RocketType.entries.forEach { cls ->
                val clsUnlocked = highScore >= cls.unlockScore || sharedPrefs.getBoolean("unlock_${cls.name}", false)
                Spacer(Modifier.height(4.dp))
                Text(cls.title.uppercase(), color = if (clsUnlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cls.chassisVariants.forEachIndexed { idx, variant ->
                        val owned = clsUnlocked
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (owned) SciFiBorder.copy(alpha = 0.3f) else SciFiBorder.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                ),
                            color = if (owned) SciFiSurface else SciFiSurface.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Canvas(Modifier.size(44.dp).padding(2.dp)) {
                                    val cx = size.width / 2
                                    val cy = size.height / 2
                                    val s = 1.2f
                                    val alpha = if (owned) 1f else 0.3f

                                    val bw = 10f * s
                                    val bh = 24f * s
                                    val bodyL = cx - bw / 2
                                    val bodyT = cy - bh / 2

                                    when (idx) {
                                        1 -> {
                                            val nose = Path().apply {
                                                moveTo(bodyL, bodyT)
                                                lineTo(cx, bodyT - 12f * s)
                                                lineTo(bodyL + bw, bodyT)
                                                close()
                                            }
                                            drawPath(nose, Color.DarkGray.copy(alpha = alpha))
                                            drawRect(SciFiWhite.copy(alpha = 0.6f * alpha), topLeft = Offset(bodyL, bodyT), size = Size(bw, bh))
                                            val fin = Path().apply {
                                                moveTo(bodyL, bodyT + 8f * s)
                                                lineTo(cx - bw / 2 - 4f * s, cy + bh / 2)
                                                lineTo(bodyL + 2f, cy + bh / 2)
                                                close()
                                            }
                                            drawPath(fin, SciFiRed.copy(alpha = alpha))
                                            val finR = Path().apply {
                                                moveTo(bodyL + bw, bodyT + 8f * s)
                                                lineTo(cx + bw / 2 + 4f * s, cy + bh / 2)
                                                lineTo(bodyL + bw - 2f, cy + bh / 2)
                                                close()
                                            }
                                            drawPath(finR, SciFiRed.copy(alpha = alpha))
                                        }
                                        2 -> {
                                            drawRoundRect(Color.DarkGray.copy(alpha = alpha), topLeft = Offset(bodyL, bodyT - 4f * s), size = Size(bw, bh * 0.3f + 4f * s), cornerRadius = CornerRadius(bw / 2, bw / 2))
                                            drawRect(SciFiWhite.copy(alpha = 0.6f * alpha), topLeft = Offset(bodyL, bodyT), size = Size(bw, bh))
                                            val fin = Path().apply {
                                                moveTo(bodyL, bodyT + 6f * s)
                                                lineTo(cx - bw / 2 - 2f * s, cy + bh / 2)
                                                lineTo(bodyL + 2f, cy + bh / 2)
                                                close()
                                            }
                                            drawPath(fin, SciFiRed.copy(alpha = alpha * 0.8f))
                                            val finR = Path().apply {
                                                moveTo(bodyL + bw, bodyT + 6f * s)
                                                lineTo(cx + bw / 2 + 2f * s, cy + bh / 2)
                                                lineTo(bodyL + bw - 2f, cy + bh / 2)
                                                close()
                                            }
                                            drawPath(finR, SciFiRed.copy(alpha = alpha * 0.8f))
                                        }
                                        else -> {
                                            drawRect(SciFiWhite.copy(alpha = 0.6f * alpha), topLeft = Offset(bodyL, bodyT), size = Size(bw, bh))
                                            val nose = Path().apply {
                                                moveTo(bodyL, bodyT)
                                                lineTo(cx, bodyT - 10f * s)
                                                lineTo(bodyL + bw, bodyT)
                                                close()
                                            }
                                            drawPath(nose, Color.DarkGray.copy(alpha = alpha))
                                            val fin = Path().apply {
                                                moveTo(bodyL, bodyT + 6f * s)
                                                lineTo(cx - bw / 2, cy + bh / 2)
                                                lineTo(bodyL, cy + bh / 2)
                                                close()
                                            }
                                            drawPath(fin, SciFiRed.copy(alpha = alpha))
                                            val finR = Path().apply {
                                                moveTo(bodyL + bw, bodyT + 6f * s)
                                                lineTo(cx + bw / 2, cy + bh / 2)
                                                lineTo(bodyL + bw, cy + bh / 2)
                                                close()
                                            }
                                            drawPath(finR, SciFiRed.copy(alpha = alpha))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(variant.name, color = if (owned) SciFiWhite else SciFiWhite.copy(alpha = 0.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    if (owned) "OWNED" else "\uD83D\uDD12",
                                    color = if (owned) SciFiGreen.copy(alpha = 0.6f) else SciFiWhite.copy(alpha = 0.15f),
                                    fontSize = 6.sp, fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        GlobalAdBanner()
    }
}

@Composable
private fun StatBar(label: String, value: String, fraction: Float, color: Color, iconRes: Int? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(label, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(48.dp))
        Box(Modifier.weight(1f).height(6.dp).background(SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(3.dp))) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(color, RoundedCornerShape(3.dp)))
        }
        Spacer(Modifier.width(6.dp))
        Text(value, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp))
    }
}

private fun formatRequirement(req: UnlockRequirement): String {
    return when (req.type) {
        UnlockType.SCORE -> "${req.value.toInt()} Score"
        UnlockType.ALTITUDE -> "${req.value.toInt()}m Altitude"
        UnlockType.ARTIFACT -> "${req.value.toInt()} Artifacts"
        UnlockType.DISCOVERY -> "Discovery of ${req.target}"
        UnlockType.MISSION, UnlockType.MISSION_COMPLETE -> "Mission '${req.target}'"
        UnlockType.ARTIFACT_SET -> "Artifact Set '${req.target}'"
    }
}
