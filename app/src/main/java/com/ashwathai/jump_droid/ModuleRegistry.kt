package com.ashwathai.jump_droid

import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.*

/**
 * Central registry for all modules.
 */
object ModuleRegistry {
    
    private val registry = mutableMapOf<String, Module>()

    init {
        // Hull
        register(ReinforcedHullModule())
        register(ImpactDampenersModule())
        register(SelfRepairMatrixModule())
        
        // Shield
        register(FastRechargeModule())
        register(EmergencyShieldModule())
        register(ReflectiveShieldModule())

        // Engine
        register(BurstThrustersModule())
        register(LongBurnThrustersModule())
        register(VectorThrustersModule())

        // Heat
        register(CoolingMatrixModule())
        register(ThermalBatteryModule())
        register(HeatSinkModule())

        // Utility
        register(SurveyScannerModule())
        register(ArtifactLocatorModule())
        register(ThreatScannerModule())
        register(AutoRepairDroneModule())
        register(EmergencyBeaconModule())

        // EPIC 11: Omega Modules
        register(VoidEngineModule())
        register(SingularityCoreModule())
    }

    private fun register(module: Module) {
        registry[module.id] = module
    }

    fun getById(id: String): Module? = registry[id]

    fun getAll(): List<Module> = registry.values.toList()
}

/** --- Hull Modules --- **/

class ReinforcedHullModule : Module {
    override val id = "MOD_HULL_REINFORCED"
    override val name = "Reinforced Hull"
    override val description = "Increases maximum hull integrity by 25%."
    override val category = ModuleCategory.HULL
    override val rarity = ModuleRarity.COMMON
    override val iconColor = SciFiGreen
    override val unlockRequirement = UnlockRequirement(UnlockType.ALTITUDE, value = 1000f)

    override fun onEquip(player: Player) {
        player.maxIntegrity += 25f
    }

    override fun onUnequip(player: Player) {
        player.maxIntegrity -= 25f
    }
}

class ImpactDampenersModule : Module {
    override val id = "MOD_HULL_DAMPENERS"
    override val name = "Impact Dampeners"
    override val description = "Reduces all incoming damage by 20%."
    override val category = ModuleCategory.HULL
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiGreen
    override val unlockRequirement = UnlockRequirement(UnlockType.SCORE, value = 5000f)

    override fun onDamageTaken(
        player: Player,
        amount: Float,
        onVisualFeedback: (Float, Float) -> Unit,
        onBurst: (Float, Float, Int, Color, Float) -> Unit
    ): Float {
        return amount * 0.8f
    }
}

class SelfRepairMatrixModule : Module {
    override val id = "MOD_HULL_REPAIR_MATRIX"
    override val name = "Self Repair Matrix"
    override val description = "Slowly repairs hull when out of combat."
    override val category = ModuleCategory.HULL
    override val rarity = ModuleRarity.EPIC
    override val iconColor = SciFiGreen
    override val unlockRequirement = UnlockRequirement(UnlockType.ARTIFACT, value = 5f)

    private val REPAIR_INTERVAL = 5f // seconds
    private val REPAIR_AMOUNT = 2f

    override fun onUpdate(player: Player, dt: Float, onSpawnPlatform: (Float, Float, PlatformType) -> Unit) {
        if (player.shieldRegenPauseTimer <= 0f && player.integrity < player.maxIntegrity) {
            val currentProgress = player.moduleCooldowns.getOrDefault(id, 0f)
            val nextProgress = currentProgress + dt
            
            if (nextProgress >= REPAIR_INTERVAL) {
                player.integrity = (player.integrity + REPAIR_AMOUNT).coerceAtMost(player.maxIntegrity)
                player.moduleCooldowns[id] = 0f
            } else {
                player.moduleCooldowns[id] = nextProgress
            }
        }
    }
}

/** --- Shield Modules --- **/

class FastRechargeModule : Module {
    override val id = "MOD_SHIELD_FAST_RECHARGE"
    override val name = "Fast Recharge"
    override val description = "Accelerates shield recovery after damage."
    override val category = ModuleCategory.SHIELD
    override val rarity = ModuleRarity.COMMON
    override val iconColor = SciFiCyan
    override val unlockRequirement = UnlockRequirement(UnlockType.DISCOVERY, target = "SHIELD_CAPSULE")

    override fun onUpdate(player: Player, dt: Float, onSpawnPlatform: (Float, Float, PlatformType) -> Unit) {
        if (player.shieldRegenPauseTimer > 0f) {
            player.shieldRegenPauseTimer -= dt * 0.5f // 50% faster recovery start
        }
    }
}

class EmergencyShieldModule : Module {
    override val id = "MOD_SHIELD_EMERGENCY"
    override val name = "Emergency Shield"
    override val description = "Instantly restores partial shields when hull is critical (60s cooldown)."
    override val category = ModuleCategory.SHIELD
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiCyan
    override val unlockRequirement = UnlockRequirement(UnlockType.SCORE, value = 10000f)

    private val COOLDOWN = 60f
    private val TRIGGER_THRESHOLD = 0.2f // 20% Hull
    private val RESTORE_AMOUNT = 25f

    override fun onDamageTaken(
        player: Player,
        amount: Float,
        onVisualFeedback: (Float, Float) -> Unit,
        onBurst: (Float, Float, Int, Color, Float) -> Unit
    ): Float {
        val cd = player.moduleCooldowns.getOrDefault(id, 0f)
        if (cd <= 0f && (player.integrity / player.maxIntegrity) < TRIGGER_THRESHOLD) {
            player.shield = max(player.shield, RESTORE_AMOUNT)
            player.moduleCooldowns[id] = COOLDOWN
            onVisualFeedback(20f, 0.9f)
            onBurst(player.x, player.y, 20, SciFiCyan, 500f)
        }
        return amount
    }

    override fun onUpdate(player: Player, dt: Float, onSpawnPlatform: (Float, Float, PlatformType) -> Unit) {
        val cd = player.moduleCooldowns.getOrDefault(id, 0f)
        if (cd > 0f) {
            player.moduleCooldowns[id] = max(0f, cd - dt)
        }
    }
}

class ReflectiveShieldModule : Module {
    override val id = "MOD_SHIELD_REFLECTIVE"
    override val name = "Reflective Shield"
    override val description = "Triggers a shockwave on shield impact."
    override val category = ModuleCategory.SHIELD
    override val rarity = ModuleRarity.EPIC
    override val iconColor = SciFiCyan
    override val unlockRequirement = UnlockRequirement(UnlockType.MISSION, target = "flight_time_2")

    override fun onShieldHit(
        player: Player,
        amount: Float,
        onVisualFeedback: (Float, Float) -> Unit,
        onBurst: (Float, Float, Int, Color, Float) -> Unit
    ) {
        if (amount > 5f) {
            onVisualFeedback(10f, 0.3f)
            onBurst(player.x, player.y, 15, SciFiPurple, 300f)
        }
    }
}

/** --- Engine Modules --- **/

class BurstThrustersModule : Module {
    override val id = "MOD_ENGINE_BURST"
    override val name = "Burst Thrusters"
    override val description = "Massive initial acceleration but generates 50% more heat."
    override val category = ModuleCategory.ENGINE
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiRed
    override val unlockRequirement = UnlockRequirement(UnlockType.SCORE, value = 7500f)

    override fun onThrust(player: Player, dt: Float): Float = 1.3f
    override fun onHeatChange(player: Player, currentHeat: Float): Float = 1.5f
}

class LongBurnThrustersModule : Module {
    override val id = "MOD_ENGINE_LONG_BURN"
    override val name = "Long Burn Thrusters"
    override val description = "Improved fuel efficiency and sustain at the cost of peak power."
    override val category = ModuleCategory.ENGINE
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiGold
    override val unlockRequirement = UnlockRequirement(UnlockType.ALTITUDE, value = 5000f)

    override fun onThrust(player: Player, dt: Float): Float = 0.85f
    override fun onFuelConsume(player: Player, dt: Float): Float = 0.7f
}

class VectorThrustersModule : Module {
    override val id = "MOD_ENGINE_VECTOR"
    override val name = "Vector Thrusters"
    override val description = "Advanced steering authority for precision maneuvers."
    override val category = ModuleCategory.ENGINE
    override val rarity = ModuleRarity.EPIC
    override val iconColor = SciFiCyan
    override val unlockRequirement = UnlockRequirement(UnlockType.MISSION, target = "momentum_master_2")

    override fun onSteer(player: Player, dt: Float): Float = 1.6f
}

/** --- Heat Modules --- **/

class CoolingMatrixModule : Module {
    override val id = "MOD_HEAT_COOLING_MATRIX"
    override val name = "Cooling Matrix"
    override val description = "Improves passive engine cooling rate by 50%."
    override val category = ModuleCategory.HEAT
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiWhite
    override val unlockRequirement = UnlockRequirement(UnlockType.SCORE, value = 12000f)

    override fun onCooling(player: Player, dt: Float): Float = 1.5f
}

class ThermalBatteryModule : Module {
    override val id = "MOD_HEAT_BATTERY"
    override val name = "Thermal Battery"
    override val description = "Increases maximum engine heat capacity by 40 units."
    override val category = ModuleCategory.HEAT
    override val rarity = ModuleRarity.EPIC
    override val iconColor = SciFiRed
    override val unlockRequirement = UnlockRequirement(UnlockType.ARTIFACT, value = 8f)

    override fun onEquip(player: Player) {
        player.maxHeat += 40f
    }

    override fun onUnequip(player: Player) {
        player.maxHeat -= 40f
    }
}

class HeatSinkModule : Module {
    override val id = "MOD_HEAT_SINK"
    override val name = "Heat Sink"
    override val description = "Instantly vents 30 heat upon landing on any platform."
    override val category = ModuleCategory.HEAT
    override val rarity = ModuleRarity.EPIC
    override val iconColor = SciFiCyan
    override val unlockRequirement = UnlockRequirement(UnlockType.DISCOVERY, target = "HEAT_SINK")

    override fun onLanding(player: Player, platform: Platform) {
        player.heat = max(0f, player.heat - 30f)
    }
}

/** --- Utility Modules --- **/

class SurveyScannerModule : Module {
    override val id = "MOD_UTIL_SCANNER"
    override val name = "Survey Scanner"
    override val description = "Doubles the range at which discoveries are revealed."
    override val category = ModuleCategory.UTILITY
    override val rarity = ModuleRarity.COMMON
    override val iconColor = SciFiCyan
    override val unlockRequirement = UnlockRequirement(UnlockType.ALTITUDE, value = 2000f)

    override fun onEquip(player: Player) {
        player.discoveryRangeMultiplier = 2.0f
    }

    override fun onUnequip(player: Player) {
        player.discoveryRangeMultiplier = 1.0f
    }

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        // Visual pulse effect
        val pulse = (gameTime % 2000) / 2000f
        drawScope.drawCircle(
            color = SciFiCyan.copy(alpha = 0.1f * (1f - pulse)),
            radius = 500f * player.discoveryRangeMultiplier * pulse,
            center = Offset(player.x, player.y - cameraY),
            style = Stroke(width = 2f)
        )
    }
}

class ArtifactLocatorModule : Module {
    override val id = "MOD_UTIL_LOCATOR"
    override val name = "Artifact Locator"
    override val description = "Provides directional guidance to nearby artifacts."
    override val category = ModuleCategory.UTILITY
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiPurple
    override val unlockRequirement = UnlockRequirement(UnlockType.ARTIFACT, value = 3f)

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        powerUps.filter { it.type == PowerUpType.ARTIFACT }.forEach { artifact ->
            val dx = artifact.x - player.x
            val dy = artifact.y - player.y
            val dist = sqrt(dx*dx + dy*dy)
            
            if (dist < 1500f) {
                val angle = atan2(dy, dx)
                val indicatorDist = 60f + (sin(gameTime / 100f) * 5f)
                val x = player.x + cos(angle) * indicatorDist
                val y = (player.y - cameraY) + sin(angle) * indicatorDist
                
                drawScope.drawCircle(
                    color = SciFiPurple.copy(alpha = 0.6f * (1f - dist/1500f)),
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

class ThreatScannerModule : Module {
    override val id = "MOD_UTIL_THREAT_SCAN"
    override val name = "Threat Scanner"
    override val description = "Displays detailed tactical data for nearby threats."
    override val category = ModuleCategory.UTILITY
    override val rarity = ModuleRarity.RARE
    override val iconColor = SciFiRed
    override val unlockRequirement = UnlockRequirement(UnlockType.MISSION, target = "boss_slayer_1")

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        activeThreats.forEach { threat ->
            val dx = threat.x - player.x
            val dy = threat.y - player.y
            val dist = sqrt(dx*dx + dy*dy)
            
            if (dist < 600f) {
                val alpha = 0.8f * (1f - dist/600f)
                val tx = threat.x
                val ty = threat.y - cameraY - 40f
                
                // HP Bar
                val barW = 40f
                val barH = 4f
                val hpRatio = threat.health / threat.definition.baseHealth
                
                drawScope.drawRect(
                    color = Color.Gray.copy(alpha = alpha * 0.3f),
                    topLeft = Offset(tx - barW/2, ty),
                    size = Size(barW, barH)
                )
                drawScope.drawRect(
                    color = SciFiRed.copy(alpha = alpha),
                    topLeft = Offset(tx - barW/2, ty),
                    size = Size(barW * hpRatio, barH)
                )
            }
        }
    }
}

class AutoRepairDroneModule : Module {
    override val id = "MOD_SUPPORT_REPAIR_DRONE"
    override val name = "Auto Repair Drone"
    override val description = "Slowly repairs hull integrity when out of combat."
    override val category = ModuleCategory.UTILITY
    override val rarity = ModuleRarity.EPIC
    override val iconColor = SciFiGreen
    override val unlockRequirement = UnlockRequirement(UnlockType.SCORE, value = 15000f)

    private val REPAIR_INTERVAL = 8f
    private val REPAIR_AMOUNT = 1f

    override fun onUpdate(player: Player, dt: Float, onSpawnPlatform: (Float, Float, PlatformType) -> Unit) {
        if (player.shieldRegenPauseTimer <= 0f && player.integrity < player.maxIntegrity) {
            val cd = player.moduleCooldowns.getOrDefault(id, 0f)
            val next = cd + dt
            if (next >= REPAIR_INTERVAL) {
                player.integrity = min(player.maxIntegrity, player.integrity + REPAIR_AMOUNT)
                player.moduleCooldowns[id] = 0f
            } else {
                player.moduleCooldowns[id] = next
            }
        }
    }

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        if (player.integrity < player.maxIntegrity) {
            val orbitRadius = 40f
            val orbitSpeed = gameTime / 500f
            val dx = cos(orbitSpeed) * orbitRadius
            val dy = sin(orbitSpeed) * orbitRadius
            
            drawScope.drawCircle(
                color = SciFiGreen.copy(alpha = 0.8f),
                radius = 4f,
                center = Offset(player.x + dx, player.y - cameraY + dy)
            )
            // Connection line
            drawScope.drawLine(
                color = SciFiGreen.copy(alpha = 0.2f),
                start = Offset(player.x, player.y - cameraY),
                end = Offset(player.x + dx, player.y - cameraY + dy),
                strokeWidth = 1f
            )
        }
    }
}

class EmergencyBeaconModule : Module {
    override val id = "MOD_SUPPORT_BEACON"
    override val name = "Emergency Beacon"
    override val description = "Spawns a rescue platform when fuel is critical (120s cooldown)."
    override val category = ModuleCategory.UTILITY
    override val rarity = ModuleRarity.LEGENDARY
    override val iconColor = SciFiGold
    override val unlockRequirement = UnlockRequirement(UnlockType.ALTITUDE, value = 10000f)

    private val COOLDOWN = 120f
    private val FUEL_THRESHOLD = 15f

    override fun onUpdate(player: Player, dt: Float, onSpawnPlatform: (Float, Float, PlatformType) -> Unit) {
        val cd = player.moduleCooldowns.getOrDefault(id, 0f)
        if (cd > 0f) {
            player.moduleCooldowns[id] = max(0f, cd - dt)
        } else if (player.fuel < FUEL_THRESHOLD && player.velocityY > 0f) {
            // Trigger emergency platform spawn
            onSpawnPlatform(player.x - 75f, player.y + 150f, PlatformType.FUEL)
            player.moduleCooldowns[id] = COOLDOWN
        }
    }

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        val cd = player.moduleCooldowns.getOrDefault(id, 0f)
        if (cd <= 0f && player.fuel < FUEL_THRESHOLD * 2f) {
            val blink = (gameTime % 500 < 250)
            if (blink) {
                drawScope.drawCircle(
                    color = SciFiGold.copy(alpha = 0.5f),
                    radius = 20f,
                    center = Offset(player.x, player.y - cameraY - 60f),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

/** --- EPIC 11: Omega Modules --- **/

class VoidEngineModule : Module {
    override val id = "MOD_VOID_ENGINE"
    override val name = "Void Engine"
    override val description = "Draws energy from spatial folding. Provides infinite fuel."
    override val category = ModuleCategory.ENGINE
    override val rarity = ModuleRarity.LEGENDARY
    override val iconColor = Color.Black
    override val unlockRequirement = UnlockRequirement(UnlockType.MISSION_COMPLETE, target = "infinite_ascent")

    override fun onFuelConsume(player: Player, dt: Float): Float = 0f

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        repeat(3) { i ->
            val angle = (gameTime / 100f) + i * 2.09f
            val dx = cos(angle) * 15f
            val dy = sin(angle) * 15f + 40f
            drawScope.drawCircle(Color.Black.copy(alpha = 0.4f), radius = 5f, center = Offset(player.x + dx, player.y - cameraY + dy))
        }
    }
}

class SingularityCoreModule : Module {
    override val id = "MOD_SINGULARITY_CORE"
    override val name = "Singularity Core"
    override val description = "Miniaturized gravitational anchor. Perfect flight stability."
    override val category = ModuleCategory.UTILITY
    override val rarity = ModuleRarity.LEGENDARY
    override val iconColor = Color.White
    override val unlockRequirement = UnlockRequirement(UnlockType.MISSION_COMPLETE, target = "infinite_ascent")

    override fun onEquip(player: Player) {
        player.stabilityTimer = Float.MAX_VALUE
    }

    override fun onUnequip(player: Player) {
        player.stabilityTimer = 0f
    }

    override fun onDraw(drawScope: DrawScope, player: Player, cameraY: Float, gameTime: Long, activeThreats: List<ActiveThreat>, powerUps: List<PowerUp>, platforms: List<Platform>) {
        val pulse = (sin(gameTime / 50f) * 0.5f + 0.5f)
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.2f * pulse),
            radius = 60f,
            center = Offset(player.x, player.y - cameraY),
            style = Stroke(width = 2f)
        )
    }
}
