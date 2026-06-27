package com.example.jump_droid

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.*

@Composable
fun LoadoutScreen(
    loadoutManager: LoadoutManager,
    progressionManager: ProgressionManager,
    missionManager: MissionManager,
    onNavigate: (GameState) -> Unit
) {
    var selectedSlot by remember { mutableStateOf(0) }
    val equippedIds = loadoutManager.equippedModuleIds
    val allModules = ModuleRegistry.getAll()

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Column(Modifier.padding(16.dp).safeDrawingPadding()) {
            Text(
                "SHIP LOADOUT",
                style = MaterialTheme.typography.headlineMedium,
                color = SciFiCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                "EQUIP OWNED MODULES TO AUGMENT PERFORMANCE",
                color = SciFiWhite.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(24.dp))

            // --- Slots ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                equippedIds.forEachIndexed { index, moduleId ->
                    val module = moduleId?.let { ModuleRegistry.getById(it) }
                    val isSelected = selectedSlot == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(if (isSelected) SciFiCyan.copy(alpha = 0.1f) else SciFiSurface, RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SciFiCyan else SciFiBorder.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedSlot = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (module != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(module.name.uppercase(), color = module.iconColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text(module.category.name, color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("UNEQUIP", color = SciFiRed, fontSize = 8.sp, modifier = Modifier.clickable {
                                    loadoutManager.unequipModule(index)
                                })
                            }
                        } else {
                            Text("SLOT ${index + 1}\nEMPTY", color = SciFiWhite.copy(alpha = 0.2f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("MODULE LIBRARY", color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                allModules.forEach { module ->
                    val isEquipped = equippedIds.contains(module.id)
                    val isUnlocked = loadoutManager.isModuleUnlocked(module, progressionManager, missionManager)
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(32.dp).background(
                                if (isUnlocked) module.iconColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f), 
                                RoundedCornerShape(4.dp)
                            ), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isUnlocked) module.category.name.take(1) else "🔒", 
                                    color = if (isUnlocked) module.iconColor else Color.Gray, 
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (isUnlocked) module.name else "LOCKED MODULE", 
                                    color = when {
                                        isEquipped -> SciFiWhite.copy(alpha = 0.3f)
                                        !isUnlocked -> Color.Gray
                                        else -> SciFiWhite
                                    }, 
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isUnlocked) module.description else "Requires: ${formatRequirement(module.unlockRequirement)}", 
                                    color = SciFiWhite.copy(alpha = 0.5f), 
                                    fontSize = 10.sp
                                )
                            }
                            if (isEquipped) {
                                Text("EQUIPPED", color = SciFiCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            } else if (!isUnlocked) {
                                Text("LOCKED", color = SciFiRed.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onNavigate(GameState.HANGAR) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.5f))
            ) {
                Text("BACK TO HANGAR", color = SciFiWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            GlobalAdBanner()
        }
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
