package com.example.jump_droid

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class GameState {
    TITLE, MAIN_MENU, HANGAR, CODEX, ABOUT, LEADERBOARD, PLAYING, GAMEOVER, TUTORIAL, SETTINGS
}

enum class PowerUpType {
    FUEL_TANK, TURBO_BOOSTER, EFFICIENCY_MODULE, HEAT_SINK, SHIELD, FUEL_CONVERTER, OVERDRIVE, GRAVITY_STABILIZER, MAGNET, SCANNER, BEACON, ASCENSION_CORE, ARTIFACT, REPAIR_KIT
}

enum class DiscoveryType(val title: String, val description: String, val category: String) {
    // Platforms
    NORMAL_PLATFORM("Standard Platform", "Reliable landing platform.\nNo special effects.", "PLATFORMS"),
    MOVING_PLATFORM("Moving Platform", "Moves horizontally.\nTime your landing carefully.", "PLATFORMS"),
    ICE_PLATFORM("Ice Platform", "Very slippery.\nMomentum is preserved.", "PLATFORMS"),
    BOOST_PLATFORM("Boost Platform", "Launches your rocket much higher.", "PLATFORMS"),
    BREAKABLE_PLATFORM("Breakable Platform", "Collapses shortly after landing.\nKeep moving.", "PLATFORMS"),
    
    // Powerups
    FUEL_TANK("Fuel Tank", "Increases maximum fuel capacity.", "POWERUPS"),
    TURBO_BOOSTER("Turbo Booster", "Temporarily increases thrust power.", "POWERUPS"),
    EFFICIENCY_MODULE("Efficiency Module", "Reduces fuel consumption temporarily.", "POWERUPS"),
    HEAT_SINK("Heat Sink", "Instantly removes 50% heat.", "POWERUPS"),
    GUARDIAN_SHIELD("Guardian Shield", "Blocks one projectile.", "POWERUPS"),
    FUEL_CONVERTER("Fuel Converter", "Heat becomes fuel.", "POWERUPS"),
    OVERDRIVE_CORE("Overdrive Core", "Massive thrust increase. Heat generation doubled.", "POWERUPS"),
    GRAVITY_STABILIZER("Gravity Stabilizer", "Reduces gravity.", "POWERUPS"),
    MAGNET_DRONE("Magnet Drone", "Attracts nearby powerups.", "POWERUPS"),
    ARTIFACT_SCANNER("Artifact Scanner", "Highlights nearby artifacts.", "POWERUPS"),
    GUARDIAN_BEACON("Guardian Beacon", "Guardian support active.", "POWERUPS"),
    ASCENSION_CORE("Ascension Core", "Unlimited fuel and cooling.", "POWERUPS"),
    
    // Mechanics
    HEAT_SYSTEM("Engine Heat", "Using thrusters generates heat.\nAllow the engine to cool or risk overheating.", "MECHANICS"),
    OVERHEAT_SYSTEM("Overheated", "Your engines have shut down temporarily.\nUse platforms while waiting for cooldown.", "MECHANICS"),
    
    // Areas
    AREA_EARTH("Earth", "Starting region. Blue sky. ideal for training.", "AREAS"),
    AREA_CLOUDS("Cloud Layer", "Visibility decreases. Cloud density increases.", "AREAS"),
    AREA_ATMOSPHERE("Upper Atmosphere", "The sky darkens. Stars become visible.", "AREAS"),
    AREA_ORBIT("Orbit", "Boundary between atmosphere and space. Debris fields.", "AREAS"),
    AREA_SPACE("Deep Space", "The frontier. No atmosphere. Only distance.", "AREAS"),
    AREA_VOID("The Void", "Beyond Deep Space. Distorted stars. Unknown phenomena.", "AREAS"),

    // Rockets
    ROCKET_BALANCED("Balanced Rocket", "The standard exploration craft. Reliable. Stable.", "ROCKETS"),
    ROCKET_SCOUT("Scout Rocket", "High thrust, low fuel reserves.", "ROCKETS"),
    ROCKET_TANK("Tank Rocket", "Massive fuel reserves, sluggish maneuverability.", "ROCKETS"),
    ROCKET_EXPERIMENTAL("Experimental Rocket", "Exceptional thrust, rapid overheating.", "ROCKETS"),

    // Lore
    LORE_ASCENSION("The Ascension Program", "A multinational effort to reach an unidentified object detected beyond the solar system.", "LORE"),
    LORE_SIGNAL("The First Signal", "A repeating transmission detected beyond Deep Space.", "LORE"),
    LORE_LOST_FLEET("The Lost Fleet", "Records suggest multiple rockets disappeared during the original mission.", "LORE"),
    LORE_LOGS("The Ascension Logs", "Recovered fragments from the first expedition.", "LORE"),

    // Artifacts
    ART_RECORDER("Flight Recorder", "A damaged recording unit from a previous mission.", "ARTIFACTS"),
    ART_ALLOY("Unknown Alloy", "A fragment of material not found on Earth.", "ARTIFACTS"),
    ART_BEACON("Encrypted Beacon", "A short-range signaling device of unknown manufacture.", "ARTIFACTS"),
    ART_DRONE("Drone Core", "The processor from an automated survey unit.", "ARTIFACTS"),
    ART_CRYSTAL("Navigation Crystal", "A crystal used in advanced navigation systems.", "ARTIFACTS"),
    ART_CELL("Ancient Power Cell", "A powerful and old energy source.", "ARTIFACTS"),
    ART_VOID("Void Fragment", "A mysterious shard from the Void.", "ARTIFACTS"),
    ART_RELIC("Guardian Relic", "A relic left behind by the Guardians.", "ARTIFACTS"),

    // Threats
    THREAT_SENTINEL("The Sentinel", "A massive autonomous defense platform.", "THREATS"),
    THREAT_COLLECTOR("The Collector", "Believed to harvest abandoned technology.", "THREATS"),
    THREAT_VOID_ENGINE("The Void Engine", "An impossible object drifting beyond known space.", "THREATS"),
    THREAT_HUNGER("The Hunger", "Massive platform-consuming organism.", "THREATS"),
    THREAT_SIEGEBREAKER("The Siegebreaker", "Ancient Ascension war machine.", "THREATS"),
    THREAT_FROSTMAW("The Frostmaw", "Converts platforms into ice.", "THREATS"),
    THREAT_STORM_SHEPHERD("The Storm Shepherd", "Creates wind zones.", "THREATS")
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
    val color: Color = Color.White
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
}
