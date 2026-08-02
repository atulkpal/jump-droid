package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
    soundManager: SoundManager? = null,
    hapticManager: HapticManager? = null
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
                    TextButton(onClick = { 
                        soundManager?.playSfx("sfx_ui_back")
                        hapticManager?.vibrate(HapticManager.HapticType.TICK)
                        onNavigate(GameState.MAIN_MENU) 
                    }) {
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
                            onClick = { 
                                soundManager?.playSfx("sfx_ui_click")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                selectedTab = index 
                            },
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

                Box(Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> OverviewTab(player, loadoutManager, progressionManager, missionManager, highScore, sharedPrefs, accentPulse, borderPulse, onNavigate, soundManager, hapticManager)
                        1 -> CosmeticsTab()
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                NativeIntegratedAd()
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
    soundManager: SoundManager?,
    hapticManager: HapticManager?
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
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Large detailed rocket preview
            Box(
                Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 8.dp, vertical = 4.dp)
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
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Rocket type selector
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RocketType.entries.forEach { type ->
                    val unlocked = highScore >= type.unlockScore || sharedPrefs.getBoolean("unlock_${type.name}", false)
                    val isActive = player.rocketType == type
                    Surface(
                        modifier = Modifier.weight(1f)
                            .clickable(enabled = unlocked) {
                                if (isActive) return@clickable
                                soundManager?.playSfx("sfx_ui_click")
                                hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                player.rocketType = type
                                player.currentChassisIndex = 0
                            }
                            .border(1.dp, if (isActive) SciFiCyan else if (unlocked) SciFiBorder.copy(alpha = 0.2f) else SciFiBorder.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        color = if (isActive) SciFiCyan.copy(alpha = 0.08f) else if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(vertical = 4.dp, horizontal = 2.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(type.title, color = if (unlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (!unlocked) Text("${type.unlockScore}m", color = SciFiRed, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                            else if (isActive) Text("ACTIVE", color = SciFiCyan, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Console: Chassis & Stats
            Surface(Modifier.fillMaxWidth().padding(horizontal = 8.dp), color = SciFiSurface.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.1f))) {
                Column(Modifier.padding(12.dp)) {
                    // Internal Chassis Selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CHASSIS VARIANT", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.width(70.dp))
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            player.rocketType.chassisVariants.forEachIndexed { index, chassis ->
                                val isActive = player.currentChassisIndex == index
                                Surface(
                                    modifier = Modifier.weight(1f).height(24.dp)
                                        .clickable {
                                            if (isActive) return@clickable
                                            soundManager?.playSfx("sfx_ui_click")
                                            hapticManager?.vibrate(HapticManager.HapticType.TICK)
                                            player.currentChassisIndex = index
                                        }
                                        .border(0.5.dp, if (isActive) SciFiCyan else SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                                    color = if (isActive) SciFiCyan.copy(alpha = 0.12f) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(chassis.name.uppercase(), color = if (isActive) SciFiCyan else SciFiWhite.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SciFiBorder.copy(alpha = 0.1f))
                    
                    val currentThrust = player.rocketType.chassisThrustMult(player.currentChassisIndex)
                    val currentFuel = player.rocketType.chassisFuelMult(player.currentChassisIndex)
                    val currentHeat = player.rocketType.chassisHeatMult(player.currentChassisIndex)
                    val currentIntegrity = player.rocketType.chassisIntegrityMult(player.currentChassisIndex)
                    val currentSteer = player.rocketType.chassisSteerMult(player.currentChassisIndex)

                    val rocketStats = RocketStats(
                        thrust = currentThrust,
                        fuel = currentFuel,
                        thermal = (1.0f / currentHeat).coerceIn(0.1f, 2.0f),
                        integrity = currentIntegrity,
                        maneuverability = currentSteer
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            StatBar("THRUST", "${(currentThrust * 100).toInt()}%", currentThrust / 1.5f, SciFiGold)
                            Spacer(Modifier.height(5.dp))
                            StatBar("FUEL", "${(currentFuel * 100).toInt()}%", currentFuel / 1.5f, SciFiGreen)
                            Spacer(Modifier.height(5.dp))
                            StatBar("THERMAL", "${(rocketStats.thermal * 100).toInt()}%", rocketStats.thermal / 1.5f, SciFiRed)
                            Spacer(Modifier.height(5.dp))
                            StatBar("HULL", "${(rocketStats.integrity * 100).toInt()}%", rocketStats.integrity / 2.0f, SciFiCyan)
                            Spacer(Modifier.height(5.dp))
                            StatBar("STEERING", "${(rocketStats.maneuverability * 100).toInt()}%", rocketStats.maneuverability / 1.5f, SciFiPurple)
                        }
                        Spacer(Modifier.width(20.dp))
                        PentagonChart(
                            stats = rocketStats,
                            color = typeColor(player.rocketType),
                            modifier = Modifier.size(110.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Module slots
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                loadoutManager.equippedModuleIds.forEachIndexed { index, moduleId ->
                    val module = moduleId?.let { ModuleRegistry.getById(it) }
                    Surface(
                        modifier = Modifier.weight(1f).height(46.dp)
                            .clickable { showModulePicker = true; pickerSlotIndex = index }
                            .border(1.dp, if (module != null) module.iconColor.copy(alpha = 0.4f) else SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        color = SciFiSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            if (module != null) {
                                Icon(painter = painterResource(id = categoryIconRes(module.category)), contentDescription = null, modifier = Modifier.size(14.dp), tint = module.iconColor)
                                Spacer(Modifier.width(6.dp))
                                Text(module.name.uppercase(), color = module.iconColor, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            } else {
                                Text("SLOT ${index+1}", color = SciFiWhite.copy(alpha = 0.2f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    soundManager?.playSfx("sfx_ui_confirm")
                    onNavigate(GameState.CALIBRATION)
                },
                modifier = Modifier.fillMaxWidth(0.9f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiGold, contentColor = Color.Black)
            ) {
                Text("START EXPEDITION", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    soundManager?.playSfx("sfx_ui_confirm")
                    onNavigate(GameState.MAIN_MENU)
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = BorderStroke(1.dp, SciFiCyan.copy(alpha = borderPulse))
            ) {
                Text("BACK TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp)
            }
        }

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
private fun CosmeticsTab() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).padding(24.dp),
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, SciFiPurple.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "Maintenance")
                val rotation by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "GearRotation")
                
                Icon(
                    painter = painterResource(id = R.drawable.ic_station_sys),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).graphicsLayer(rotationZ = rotation),
                    tint = SciFiPurple
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text("SYSTEM UPGRADE", style = MaterialTheme.typography.titleLarge, color = SciFiPurple, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text("COSMETIC PROTOCOLS: OFFLINE", style = MaterialTheme.typography.labelSmall, color = SciFiPurple.copy(alpha = 0.6f), letterSpacing = 2.sp)
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Ship paint and exhaust customizer is currently undergoing hardware recalibration for production release.",
                    color = SciFiWhite.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp
                )
                
                Spacer(Modifier.height(32.dp))
                
                Surface(
                    color = SciFiPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SciFiPurple.copy(alpha = 0.3f))
                ) {
                    Text("STATUS: RECONFIGURING // v2.2.3 READY", color = SciFiPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedCategory == null) "SLOT ${slotIndex + 1} — SELECT CATEGORY" else selectedCategory.name, color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text("\u2715", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 18.sp, modifier = Modifier.clickable(onClick = onDismiss))
                }
                Spacer(Modifier.height(12.dp))

                if (selectedCategory == null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ModuleCategory.entries.forEach { cat ->
                            Surface(
                                modifier = Modifier.weight(1f).clickable { onSelectCategory(cat) }.border(1.dp, categoryColor(cat).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                color = categoryColor(cat).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(Modifier.padding(8.dp).fillMaxWidth()) {
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(categoryIcon(cat), fontSize = 20.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(cat.name, color = categoryColor(cat), fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    val currentModId = loadoutManager.equippedModuleIds.getOrNull(slotIndex)
                    val currentMod = currentModId?.let { ModuleRegistry.getById(it) }
                    if (currentMod != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, currentMod.iconColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            color = currentMod.iconColor.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp).background(currentMod.iconColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                    Image(painter = painterResource(id = categoryIconRes(currentMod.category)), contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(currentMod.name.uppercase(), color = currentMod.iconColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Text("UNEQUIP", color = SciFiRed, fontWeight = FontWeight.Black, fontSize = 7.sp, modifier = Modifier.clickable { loadoutManager.unequipModule(slotIndex); onDismiss() })
                            }
                        }
                    }
                } else {
                    val equippedIds = loadoutManager.equippedModuleIds
                    ModuleRegistry.getAll().filter { it.category == selectedCategory }.forEach { module ->
                        val equippedSlot = equippedIds.indexOf(module.id)
                        val isEquipped = equippedSlot == slotIndex
                        val isEquippedOther = equippedSlot == 1 - slotIndex && equippedSlot >= 0
                        val isUnlocked = loadoutManager.isModuleUnlocked(module, progressionManager, missionManager)

                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(isUnlocked && !isEquipped && !isEquippedOther) { onSelectModule(module.id) },
                            color = when { isEquipped || isEquippedOther -> SciFiSurface.copy(alpha = 0.3f); !isUnlocked -> Color.Black.copy(alpha = 0.3f); else -> SciFiSurface },
                            shape = RoundedCornerShape(8.dp),
                            border = if (isUnlocked && !isEquipped && !isEquippedOther) BorderStroke(1.dp, categoryColor(selectedCategory).copy(alpha = 0.15f)) else null
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp).background(if (isUnlocked) module.iconColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                    if (isUnlocked) Image(painter = painterResource(id = categoryIconRes(module.category)), contentDescription = null, modifier = Modifier.size(16.dp))
                                    else Text("\uD83D\uDD12", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (isUnlocked) module.name else "LOCKED MODULE", color = if (isUnlocked) SciFiWhite else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(if (isUnlocked) module.description else formatRequirement(module.unlockRequirement), color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBar(label: String, value: String, fraction: Float, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(48.dp))
        Box(Modifier.weight(1f).height(6.dp).background(SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(3.dp))) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(color, RoundedCornerShape(3.dp)))
        }
        Spacer(Modifier.width(6.dp))
        Text(value, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

private fun formatRequirement(req: UnlockRequirement): String = when (req.type) {
    UnlockType.SCORE -> "${req.value.toInt()} Score"
    UnlockType.ALTITUDE -> "${req.value.toInt()}m Altitude"
    UnlockType.ARTIFACT -> "${req.value.toInt()} Artifacts"
    UnlockType.DISCOVERY -> "Discovery of ${req.target}"
    UnlockType.MISSION, UnlockType.MISSION_COMPLETE -> "Mission '${req.target}'"
    UnlockType.ARTIFACT_SET -> "Artifact Set '${req.target}'"
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
