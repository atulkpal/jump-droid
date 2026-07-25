package com.ashwathai.jump_droid

import android.content.SharedPreferences
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.roundToInt

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
    val tabs = listOf("OVERVIEW", "ROCKETS", "MODULES", "COSMETICS")
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
                    Column {
                        Text("ROCKET HANGAR", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(progressionManager.currentRank.title, color = SciFiGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
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
                    0 -> OverviewTab(player, loadoutManager, progressionManager, accentPulse, borderPulse, onNavigate, soundManager)
                    1 -> RocketsTab(player, highScore, sharedPrefs, borderPulse, onNavigate, soundManager)
                    2 -> ModulesTab(player, loadoutManager, progressionManager, missionManager, soundManager)
                    3 -> CosmeticsTab(player, highScore, sharedPrefs)
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
    accentPulse: Float,
    borderPulse: Float,
    onNavigate: (GameState) -> Unit,
    soundManager: SoundManager?
) {
    val rocketRenderer = remember { RocketRenderer() }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .background(SciFiSurface, RoundedCornerShape(16.dp))
                .border(1.dp, SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                scale(2.8f, 2.8f, pivot = Offset(w / 2, h / 2)) {
                    rocketRenderer.render(this, player, false, Offset.Zero, 0f, 0L, offsetOverride = Offset.Zero)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SciFiSurface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(player.rocketType.title.uppercase(), color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("ACTIVE", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 2.sp)
                }
                Text(player.rocketType.traitName.uppercase(), color = SciFiGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(player.rocketType.traitDescription, color = SciFiWhite.copy(alpha = 0.5f), fontSize = 9.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            loadoutManager.equippedModuleIds.forEachIndexed { index, moduleId ->
                val module = moduleId?.let { ModuleRegistry.getById(it) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .background(SciFiSurface, RoundedCornerShape(10.dp))
                        .border(1.dp, if (module != null) module.iconColor.copy(alpha = 0.4f) else SciFiBorder.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (module != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(module.name.uppercase(), color = module.iconColor, fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.Center)
                            Text(module.category.name, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 7.sp)
                        }
                    } else {
                        Text("SLOT ${index + 1}\nEMPTY", color = SciFiWhite.copy(alpha = 0.2f), textAlign = TextAlign.Center, fontSize = 9.sp, lineHeight = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val stats = listOf(
                "HULL" to "${progressionManager.permanentMaxIntegrity.toInt()}",
                "SHIELD" to "${progressionManager.permanentMaxShield.toInt()}",
                "FUEL" to "${player.rocketType.fuelMult.times(100).toInt()}%",
                "HEAT" to "${player.rocketType.heatMult.times(100).toInt()}%"
            )
            stats.forEach { (label, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(label, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(value, color = SciFiWhite, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                soundManager?.playSfx("sfx_ui_confirm")
                onNavigate(GameState.MAIN_MENU)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
            border = BorderStroke(1.dp, SciFiCyan.copy(alpha = borderPulse))
        ) {
            Text("RETURN TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(4.dp))
        GlobalAdBanner()
    }
}

@Composable
private fun RocketsTab(
    player: Player,
    highScore: Int,
    sharedPrefs: SharedPreferences,
    borderPulse: Float,
    onNavigate: (GameState) -> Unit,
    soundManager: SoundManager?
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        RocketType.entries.forEach { type ->
            val unlocked = highScore >= type.unlockScore || sharedPrefs.getBoolean("unlock_${type.name}", false)
            val isActive = player.rocketType == type
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = unlocked) {
                        player.rocketType = type
                        player.currentChassisIndex = sharedPrefs.getInt("chassis_${type.name}", 0)
                    }
                    .border(
                        width = 1.dp,
                        color = if (isActive) SciFiCyan else if (unlocked) SciFiBorder else SciFiBorder.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                color = if (isActive) SciFiCyan.copy(alpha = 0.1f) else if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(44.dp).padding(end = 12.dp)) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val bodyW = 12f
                        val bodyH = 28f
                        drawRoundRect(
                            if (unlocked) SciFiWhite.copy(alpha = 0.8f) else SciFiWhite.copy(alpha = 0.3f),
                            topLeft = Offset(cx - bodyW / 2, cy - bodyH / 2),
                            size = Size(bodyW, bodyH),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                        drawPath(
                            Path().apply { moveTo(cx - bodyW / 2, cy - bodyH / 2); lineTo(cx, cy - bodyH / 2 - 12f); lineTo(cx + bodyW / 2, cy - bodyH / 2); close() },
                            if (unlocked) SciFiRed else SciFiRed.copy(alpha = 0.3f)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(type.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = if (unlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            if (!unlocked) Text("${type.unlockScore}m", color = SciFiRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            else if (isActive) Text("ACTIVE", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 2.sp)
                        }
                        if (unlocked) {
                            Text("${type.traitName.uppercase()}", color = SciFiGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(type.traitDescription, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp)
                            Spacer(Modifier.height(6.dp))
                            val cIdx = if (isActive) player.currentChassisIndex else 0
                            Text("THRUST: ${(type.chassisThrustMult(cIdx) * 100).toInt()}%  FUEL: ${(type.chassisFuelMult(cIdx) * 100).toInt()}%  THERMAL: ${(type.chassisHeatMult(cIdx) * 100).toInt()}%", color = SciFiCyan.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Chassis picker for the selected (active) class
            if (isActive && unlocked) {
                Spacer(Modifier.height(4.dp))
                Text("CHASSIS VARIANTS", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    type.chassisVariants.forEachIndexed { idx, variant ->
                        val isSelected = player.currentChassisIndex == idx
                        val borderClr = if (isSelected) SciFiCyan else SciFiBorder.copy(alpha = 0.2f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    player.currentChassisIndex = idx
                                    sharedPrefs.edit().putInt("chassis_${type.name}", idx).commit()
                                }
                                .border(1.dp, borderClr, RoundedCornerShape(8.dp)),
                            color = if (isSelected) SciFiCyan.copy(alpha = 0.08f) else SciFiSurface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                Modifier.padding(8.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(variant.name, color = if (isSelected) SciFiCyan else SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                Spacer(Modifier.height(2.dp))
                                Text("T:${(type.chassisThrustMult(idx) * 100).toInt()}% F:${(type.chassisFuelMult(idx) * 100).toInt()}% H:${(type.chassisHeatMult(idx) * 100).toInt()}%", color = SciFiWhite.copy(alpha = 0.35f), fontSize = 7.sp)
                                if (isSelected) {
                                    Spacer(Modifier.height(2.dp))
                                    Text("EQUIPPED", color = SciFiCyan, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("STAT COMPARISON", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.height(4.dp))
        PentagonChart(
            rocketType = player.rocketType,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
        Spacer(Modifier.height(8.dp))
        StatLegend(
            rocketType = player.rocketType,
            allTypes = RocketType.entries,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        GlobalAdBanner()
    }
}

@Composable
private fun ModulesTab(
    player: Player,
    loadoutManager: LoadoutManager,
    progressionManager: ProgressionManager,
    missionManager: MissionManager,
    soundManager: SoundManager?
) {
    var selectedSlot by remember { mutableIntStateOf(0) }
    val equippedIds = loadoutManager.equippedModuleIds
    val allModules = ModuleRegistry.getAll()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            equippedIds.forEachIndexed { index, moduleId ->
                val module = moduleId?.let { ModuleRegistry.getById(it) }
                val isSelected = selectedSlot == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .background(if (isSelected) SciFiCyan.copy(alpha = 0.1f) else SciFiSurface, RoundedCornerShape(10.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SciFiCyan else SciFiBorder.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedSlot = index },
                    contentAlignment = Alignment.Center
                ) {
                    if (module != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(module.name.uppercase(), color = module.iconColor, fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.Center)
                            Text(module.category.name, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 7.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("UNEQUIP", color = SciFiRed, fontSize = 7.sp, modifier = Modifier.clickable {
                                loadoutManager.unequipModule(index)
                            })
                        }
                    } else {
                        Text("SLOT ${index + 1}\nEMPTY", color = SciFiWhite.copy(alpha = 0.2f), textAlign = TextAlign.Center, fontSize = 9.sp, lineHeight = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("MODULE LIBRARY", color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            allModules.forEach { module ->
                val isEquipped = equippedIds.contains(module.id)
                val isUnlocked = loadoutManager.isModuleUnlocked(module, progressionManager, missionManager)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable(isUnlocked && !isEquipped) {
                            loadoutManager.equipModule(module.id, selectedSlot)
                        },
                    color = when {
                        isEquipped -> SciFiSurface.copy(alpha = 0.5f)
                        !isUnlocked -> Color.Black.copy(alpha = 0.3f)
                        else -> SciFiSurface
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = if (isUnlocked && !isEquipped) BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.1f)) else null
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(
                            if (isUnlocked) module.iconColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        ), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isUnlocked) module.category.name.take(1) else "\uD83D\uDD12",
                                color = if (isUnlocked) module.iconColor else Color.Gray,
                                fontWeight = FontWeight.Black, fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (isUnlocked) module.name else "LOCKED MODULE",
                                color = when {
                                    isEquipped -> SciFiWhite.copy(alpha = 0.3f)
                                    !isUnlocked -> Color.Gray
                                    else -> SciFiWhite
                                },
                                fontWeight = FontWeight.Bold, fontSize = 11.sp
                            )
                            Text(
                                text = if (isUnlocked) module.description else formatRequirement(module.unlockRequirement),
                                color = SciFiWhite.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                        }
                        if (isEquipped) {
                            Text("EQUIPPED", color = SciFiCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        } else if (!isUnlocked) {
                            Text("LOCKED", color = SciFiRed.copy(alpha = 0.5f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
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
private fun CosmeticsTab(
    player: Player,
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
        Box(
            Modifier.fillMaxWidth().height(4.dp).background(SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
        ) {
            Box(
                Modifier.fillMaxWidth(fraction = unlockedChassis.toFloat() / totalChassis).fillMaxHeight()
                    .background(SciFiGold, RoundedCornerShape(2.dp))
            )
        }

        Spacer(Modifier.height(12.dp))

        // Engine Trails
        Text("ENGINE TRAILS", color = SciFiGold, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EngineTrailRegistry.trails.forEachIndexed { idx, trail ->
                val isSelected = player.equippedTrailIndex == idx
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (isSelected) trail.trailColor else SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .clickable {
                            player.equippedTrailIndex = idx
                            sharedPrefs.edit().putInt("equipped_trail", idx).commit()
                        },
                    color = if (isSelected) trail.trailColor.copy(alpha = 0.1f) else SciFiSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(Modifier.size(20.dp)) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            drawCircle(trail.glowColor.copy(alpha = 0.2f), radius = size.minDimension / 2)
                            drawCircle(trail.trailColor, radius = 4f, center = Offset(cx, cy))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(trail.name, color = if (isSelected) trail.trailColor else SciFiWhite, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (isSelected) "ACTIVE" else "SELECT", color = if (isSelected) trail.trailColor.copy(alpha = 0.6f) else SciFiWhite.copy(alpha = 0.2f), fontSize = 6.sp, fontWeight = FontWeight.Black)
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
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (isSelected) paint.accentColor else SciFiBorder.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .clickable {
                            player.equippedPaintIndex = idx
                            sharedPrefs.edit().putInt("equipped_paint", idx).commit()
                        },
                    color = if (isSelected) paint.hullColor.copy(alpha = 0.15f) else SciFiSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(20.dp).background(paint.hullColor, RoundedCornerShape(4.dp))
                                .border(1.dp, paint.accentColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(paint.name, color = if (isSelected) paint.accentColor else SciFiWhite, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (isSelected) "ACTIVE" else "SELECT", color = if (isSelected) paint.accentColor.copy(alpha = 0.6f) else SciFiWhite.copy(alpha = 0.2f), fontSize = 6.sp, fontWeight = FontWeight.Black)
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
