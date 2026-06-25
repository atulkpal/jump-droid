package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiWhite
import com.example.jump_droid.ui.theme.SciFiBorder
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    val sharedPrefs: android.content.SharedPreferences
) {
    // --- Managers ---
    val player = Player(0f, 0f)
    val altitudeManager = AltitudeManager()
    val discoveryManager = DiscoveryManager(sharedPrefs)
    val progressionManager = ProgressionManager(sharedPrefs)
    val missionManager = MissionManager(progressionManager)
    val threatManager = ThreatManager()
    val comboManager = ComboManager()
    val platformManager = PlatformManager()
    val ambientManager = AmbientManager()
    val encounterDirector = EncounterDirector()
    val projectileManager = ProjectileManager()
    val inputBufferManager = InputBufferManager()
    val notificationManager = NotificationManager()
    val survivalManager = SurvivalManager()
    val powerUpManager = PowerUpManager()
    val floatingTextManager = FloatingTextManager()

    val platforms = mutableStateListOf<Platform>()
    val flyingRewards = mutableStateListOf<FlyingReward>()
    val particles = mutableStateListOf<Particle>()
    val landingEffects = mutableStateListOf<LandingEffect>()

    // --- Game State ---
    var highestYReached by mutableFloatStateOf(Float.MAX_VALUE)
    var score by mutableIntStateOf(0)
    var continuesUsed by mutableIntStateOf(0)
    var runDurationTimer by mutableFloatStateOf(0f)
    var airborneTimer by mutableFloatStateOf(0f)
    var noOverheatTimer by mutableFloatStateOf(0f)
    var platformStayTimer by mutableFloatStateOf(0f)
    var perfectRunTimer by mutableFloatStateOf(0f)
    var hasTakenDamageThisRun by mutableStateOf(false)
    var totalHazardHits by mutableIntStateOf(0)
    var totalFuelPickups by mutableIntStateOf(0)
    var totalPowerUps by mutableIntStateOf(0)
    var totalPlatformLandings by mutableIntStateOf(0)
    var totalBossesDefeated by mutableIntStateOf(0)
    var totalArtifactsCollected by mutableIntStateOf(0)
    var totalDashes by mutableIntStateOf(0)
    var momentumValue by mutableFloatStateOf(0f)
    var comboMaintainTimer by mutableFloatStateOf(0f)
    var overheatCount by mutableIntStateOf(0)
    var wasNearDeath by mutableStateOf(false)
    var consecutiveWins by mutableIntStateOf(0)
    var gameState by mutableStateOf(GameState.TITLE)
    var previousState by mutableStateOf(GameState.MAIN_MENU)
    var activeDiscovery by mutableStateOf<DiscoveryType?>(null)
    var unlockedRocket by mutableStateOf<RocketType?>(null)
    var codexNotification by mutableStateOf<DiscoveryType?>(null)
    var screenWidth by mutableFloatStateOf(0f)
    var screenHeight by mutableFloatStateOf(0f)
    var groundY by mutableFloatStateOf(0f)
    var gameTime by mutableLongStateOf(0L)
    var cameraY by mutableFloatStateOf(0f)
    var isThrusting by mutableStateOf(false)
    var thrustTarget by mutableStateOf(Offset.Zero)
    var screenShake by mutableFloatStateOf(0f)
    var impactFlashAlpha by mutableFloatStateOf(0f)
    var globalFogAlpha by mutableFloatStateOf(0f)
    var effectiveThrust by mutableStateOf(false)
    var effectiveTarget by mutableStateOf(Offset.Zero)
    var infiniteFuel by mutableStateOf(false)
    var disableHeat by mutableStateOf(false)
    var showDevMenu by mutableStateOf(false)
    val bossesSpawned = mutableStateSetOf<String>()
    var majorWarningText by mutableStateOf<String?>(null)
    var majorWarningTimer by mutableFloatStateOf(0f)
    var escalationSpawnId by mutableStateOf<String?>(null)
    var escalationSpawnX by mutableFloatStateOf(0f)
    var escalationSpawnY by mutableFloatStateOf(0f)
    var escalationCountdown by mutableFloatStateOf(0f)
    var missionHintRotationTimer by mutableFloatStateOf(0f)
    var globalShowObjective by mutableStateOf(false)

    var baseAltitude by mutableFloatStateOf(0f) // EPIC 11: Shift amount for origin reset
    var baseDifficultyMultiplier by mutableFloatStateOf(1.0f) // EPIC 11: For Prestige

    // --- Callbacks (set by GameScreen) ---
    var onSpawnBurst: ((x: Float, y: Float, count: Int, color: Color, speed: Float) -> Unit)? = null
    var onCheckDiscovery: ((type: DiscoveryType, forceTutorialState: Boolean) -> Unit)? = null
    var onSaveHighScore: ((score: Int) -> Unit)? = null
    var onEscalationState: ((id: String?, x: Float, y: Float, countdown: Float) -> Unit)? = null
    var onScreenShake: ((amount: Float) -> Unit)? = null
    var onImpactFlash: ((alpha: Float) -> Unit)? = null
    var onSetBossesSpawned: ((String) -> Unit)? = null

    var lastFrameTime = 0L

    /**
     * EPIC 11: Origin Reset Logic
     * Normalizes coordinates to 0 to prevent floating-point jitter at extreme altitudes.
     * Clears existing world objects to prepare for the final encounter.
     */
    fun triggerAscensionOriginReset() {
        baseAltitude += abs(cameraY)
        val shiftAmount = cameraY
        cameraY = 0f
        player.y -= shiftAmount
        
        // Clear world objects for the Singularity encounter
        platforms.clear()
        particles.clear()
        landingEffects.clear()
        flyingRewards.clear()
        projectileManager.clear()
        powerUpManager.powerUps.clear()
        threatManager.clear()
        
        // Ground is no longer relevant in Point Zero, but we shift it to keep logic consistent
        groundY -= shiftAmount
    }
}
