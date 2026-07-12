package com.ashwathai.jump_droid

import androidx.compose.ui.graphics.Color

enum class ModuleCategory {
    HULL, SHIELD, ENGINE, HEAT, UTILITY
}

enum class ModuleRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

enum class LogicOp { AND, OR }

/**
 * Enhanced requirement system for Dynamic Unlocks.
 */
data class UnlockRequirement(
    val type: UnlockType,
    val target: String = "",
    val value: Float = 1f,
    val operator: LogicOp = LogicOp.AND
)

enum class UnlockType {
    SCORE, ALTITUDE, DISCOVERY, MISSION, ARTIFACT, ARTIFACT_SET, MISSION_COMPLETE
}

/**
 * Interface for all Rocket Modules.
 * Each module can hook into game events to modify player state or behavior.
 */
interface Module {
    val id: String
    val name: String
    val description: String
    val category: ModuleCategory
    val rarity: ModuleRarity
    val iconColor: Color

    // Phase A: Unlock Metadata
    val unlockRequirement: UnlockRequirement

    // --- Life Cycle & Hooks ---

    /**
     * Called once when the module is equipped or when the player state is initialized.
     */
    fun onEquip(player: Player) {}

    /**
     * Called once when the module is unequipped.
     */
    fun onUnequip(player: Player) {}

    /**
     * Hook into damage calculation. Returns the modified damage amount.
     */
    fun onDamageTaken(
        player: Player,
        amount: Float,
        onVisualFeedback: (shake: Float, flash: Float) -> Unit = { _, _ -> },
        onBurst: (x: Float, y: Float, count: Int, color: Color, speed: Float) -> Unit = { _, _, _, _, _ -> }
    ): Float = amount

    /**
     * Hook into shield hit event.
     */
    fun onShieldHit(
        player: Player,
        amount: Float,
        onVisualFeedback: (shake: Float, flash: Float) -> Unit = { _, _ -> },
        onBurst: (x: Float, y: Float, count: Int, color: Color, speed: Float) -> Unit = { _, _, _, _, _ -> }
    ) {}

    /**
     * Hook into the thrusting loop.
     * Returns a multiplier for thrust power.
     */
    fun onThrust(player: Player, dt: Float): Float = 1.0f

    /**
     * Hook into fuel consumption.
     * Returns a multiplier for fuel usage.
     */
    fun onFuelConsume(player: Player, dt: Float): Float = 1.0f

    /**
     * Hook into steering.
     * Returns a multiplier for steering authority.
     */
    fun onSteer(player: Player, dt: Float): Float = 1.0f

    /**
     * Hook into the heat changes.
     * Returns a multiplier for heat generation.
     */
    fun onHeatChange(player: Player, currentHeat: Float): Float = 1.0f

    /**
     * Hook into the cooling loop.
     * Returns a multiplier for cooling rate.
     */
    fun onCooling(player: Player, dt: Float): Float = 1.0f

    /**
     * Hook into landing events.
     */
    fun onLanding(player: Player, platform: Platform) {}

    /**
     * Hook into artifact discovery.
     */
    fun onArtifactCollected(player: Player) {}

    /**
     * Hook into the per-frame update loop.
     */
    fun onUpdate(
        player: Player,
        dt: Float,
        onSpawnPlatform: (x: Float, y: Float, type: PlatformType) -> Unit = { _, _, _ -> }
    ) {}

    /**
     * Hook into the rendering loop.
     * Allows modules to draw to the Canvas.
     */
    fun onDraw(
        drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
        player: Player,
        cameraY: Float,
        gameTime: Long,
        activeThreats: List<ActiveThreat>,
        powerUps: List<PowerUp>,
        platforms: List<Platform>
    ) {}
}
