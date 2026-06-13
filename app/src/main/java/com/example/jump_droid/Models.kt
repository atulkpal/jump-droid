package com.example.jump_droid

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class GameState {
    TITLE, MAIN_MENU, HANGAR, CODEX, ABOUT, LEADERBOARD, PLAYING, GAMEOVER, TUTORIAL, SETTINGS, PAUSED, HELP, UNLOCK
}

enum class PowerUpType {
    FUEL_TANK, TURBO_BOOSTER, EFFICIENCY_MODULE, HEAT_SINK, ARTIFACT
}

enum class DiscoveryType(val title: String, val description: String, val lore: String, val category: String) {
    // Platforms
    NORMAL_PLATFORM("Standard Platform", "Reliable landing platform.", "The backbone of the Ascension Program's infrastructure.", "PLATFORMS"),
    MOVING_PLATFORM("Moving Platform", "Moves horizontally.", "Early prototypes often malfunctioned, leading to their current oscillating behavior.", "PLATFORMS"),
    ICE_PLATFORM("Ice Platform", "Very slippery.", "Condensation from high-altitude clouds flash-freezes on these surfaces.", "PLATFORMS"),
    BOOST_PLATFORM("Boost Platform", "Launches your rocket higher.", "Utilizes kinetic energy recovery systems to provide an extra push.", "PLATFORMS"),
    BREAKABLE_PLATFORM("Breakable Platform", "Collapses shortly after landing.", "Constructed from lightweight, temporary alloys meant for rapid deployment.", "PLATFORMS"),
    
    // Powerups
    FUEL_TANK("Fuel Tank", "Increases maximum fuel capacity.", "Standardized liquid oxygen tanks recovered from previous expeditions.", "POWERUPS"),
    TURBO_BOOSTER("Turbo Booster", "Increases thrust power.", "An experimental injector that temporarily overrides engine safety limits.", "POWERUPS"),
    EFFICIENCY_MODULE("Efficiency Module", "Reduces fuel consumption.", "Optimizes the fuel-to-air ratio for thinner atmospheric conditions.", "POWERUPS"),
    HEAT_SINK("Heat Sink", "Instantly removes heat.", "Advanced thermal paste and cooling fins designed for vacuum operation.", "POWERUPS"),
    
    // Mechanics
    HEAT_SYSTEM("Engine Heat", "Using thrusters generates heat.", "Atmospheric friction and engine stress must be managed carefully.", "MECHANICS"),
    OVERHEAT_SYSTEM("Overheated", "Engines shut down temporarily.", "Safety protocols engage to prevent catastrophic engine failure.", "MECHANICS"),
    
    // Areas
    AREA_EARTH("Earth", "Humanity's home.", "The final familiar place before the ascent begins.", "AREAS"),
    AREA_CLOUDS("Cloud Layer", "A realm above the weather.", "Most pilots never reach this altitude. The air is crisp and the view is infinite.", "AREAS"),
    AREA_ATMOSPHERE("Upper Atmosphere", "The edge between sky and space.", "A place of silence and isolation. The sky turns a deep, bruised purple.", "AREAS"),
    AREA_ORBIT("Orbit", "Where Earth becomes a distant world.", "The first true step into the unknown. Silence is absolute here.", "AREAS"),
    AREA_SPACE("Deep Space", "Beyond established routes.", "Signals become strange. Maps become unreliable. You are truly alone.", "AREAS"),
    AREA_VOID("The Void", "A region that should not exist.", "Sensors report impossible readings. The stars themselves seem distorted.", "AREAS"),

    // Rockets
    ROCKET_BALANCED("Balanced Rocket", "Standard exploration craft.", "Reliable and stable, it has carried many pilots on their first ascent.", "ROCKETS"),
    ROCKET_SCOUT("Scout Rocket", "High thrust, low fuel.", "Designed for rapid surveying, it sacrifices endurance for speed.", "ROCKETS"),
    ROCKET_TANK("Tank Rocket", "Massive fuel, low maneuverability.", "Built for long-range expeditions where fuel stations are scarce.", "ROCKETS"),
    ROCKET_EXPERIMENTAL("Experimental Rocket", "Exceptional thrust, high heat.", "A dangerous prototype that pushes the boundaries of engine technology.", "ROCKETS"),

    // Lore
    LORE_ASCENSION("The Ascension Program", "A multinational effort.", "Records indicate the program started after the 'Great Signal' was received.", "LORE"),
    LORE_SIGNAL("The First Signal", "A repeating transmission.", "It originated from the Void, carrying a sequence that matched no known language.", "LORE"),
    LORE_LOST_FLEET("The Lost Fleet", "Records of disappeared vessels.", "Multiple rockets vanished during the initial push into the Void.", "LORE"),
    LORE_LOGS("The Ascension Logs", "Recovered fragments.", "Encrypted data logs suggesting the signal might be coming from within the planet.", "LORE"),
    
    // Artifacts
    ART_RECORDER("Flight Recorder", "A damaged recording unit.", "Contains garbled audio of a pilot describing 'structures in the clouds'.", "ARTIFACTS"),
    ART_ALLOY("Unknown Alloy", "A fragment of material.", "Its molecular structure suggests it was grown, not forged.", "ARTIFACTS"),
    ART_BEACON("Encrypted Beacon", "A signaling device.", "Found drifting in orbit, it continuously broadcasts a single prime number.", "ARTIFACTS"),
    ART_DRONE("Drone Core", "An automated survey unit.", "The AI within seems to be stuck in a loop, repeating 'The Void calls'.", "ARTIFACTS"),

    // Threats
    THREAT_SENTINEL("The Sentinel", "Autonomous defense platform.", "A relic of an unknown civilization tasked with guarding the orbit.", "THREATS"),
    THREAT_VOID_ENGINE("The Void Engine", "An impossible object.", "A massive structure drifting in the Void that seems to warp space around it.", "THREATS")
}

enum class RocketType(
    val title: String,
    val thrustMult: Float,
    val fuelMult: Float,
    val heatMult: Float,
    val unlockScore: Int,
    val discovery: DiscoveryType
) {
    BALANCED("Balanced", 1.0f, 1.0f, 1.0f, 0, DiscoveryType.ROCKET_BALANCED),
    SCOUT("Scout", 1.25f, 0.7f, 0.9f, 2000, DiscoveryType.ROCKET_SCOUT),
    TANK("Tank", 0.85f, 1.5f, 0.8f, 5000, DiscoveryType.ROCKET_TANK),
    EXPERIMENTAL("Experimental", 1.5f, 1.0f, 1.4f, 10000, DiscoveryType.ROCKET_EXPERIMENTAL)
}

data class Mission(
    val id: String,
    val title: String,
    val goal: Int,
    var progress: Int = 0,
    var isCompleted: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlockCondition: (Int, Int, Int) -> Boolean // score, combo, overheat count
)

class PowerUp(
    val x: Float,
    var y: Float,
    val type: PowerUpType = PowerUpType.FUEL_TANK
)

class LandingEffect(
    val x: Float,
    val y: Float,
    var life: Float = 0.5f
)

class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    var life: Float = 1.0f,
    val color: Color = Color.White,
    val isCritical: Boolean = false
)

class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color,
    val size: Float
)

class Player(
    initialX: Float,
    initialY: Float
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)
    var velocityX by mutableFloatStateOf(0f)
    var velocityY by mutableFloatStateOf(0f)
    var fuel by mutableFloatStateOf(100f) // Will be set on restart
    
    var rocketType by mutableStateOf(RocketType.BALANCED)
    var maxFuel by mutableFloatStateOf(100f)
    
    var heat by mutableFloatStateOf(0f)
    var isOverheated by mutableStateOf(false)
    var overheatTimer by mutableFloatStateOf(0f)
    var totalOverheats by mutableIntStateOf(0)
    
    var turboTimer by mutableFloatStateOf(0f)
    var efficiencyTimer by mutableFloatStateOf(0f)
    
    var combo by mutableIntStateOf(0)
    var maxComboReached by mutableIntStateOf(0)
    var lastLandingTime by mutableLongStateOf(0L)
    var lastPlatform: Platform? = null

    // Visual Feedback State
    var squashStretch by mutableFloatStateOf(1.0f)
    var invulnerabilityTimer by mutableFloatStateOf(0f)
}
