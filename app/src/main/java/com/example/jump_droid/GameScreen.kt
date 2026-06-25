package com.example.jump_droid

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random
import com.example.jump_droid.Constants.BASE_GRAVITY
import com.example.jump_droid.Constants.BASE_THRUST_POWER
import com.example.jump_droid.Constants.BASE_FUEL_CONSUMPTION
import com.example.jump_droid.Constants.FUEL_RECHARGE_RATE
import com.example.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
import com.example.jump_droid.Constants.HORIZONTAL_DAMPING
import com.example.jump_droid.Constants.AIR_FRICTION
import com.example.jump_droid.Constants.ROCKET_WIDTH
import com.example.jump_droid.Constants.ROCKET_HEIGHT
import com.example.jump_droid.Constants.PLATFORM_HEIGHT
import com.example.jump_droid.Constants.SCREEN_PADDING
import com.example.jump_droid.Constants.BASE_FUEL_CAPACITY
import com.example.jump_droid.Constants.MAX_HEAT
import com.example.jump_droid.Constants.HEAT_GENERATION_RATE
import com.example.jump_droid.Constants.COOLING_RATE
import com.example.jump_droid.Constants.OVERHEAT_COOLDOWN_TIME
import com.example.jump_droid.Constants.SHIELD_REGEN_RATE
import com.example.jump_droid.Constants.SHIELD_REGEN_DELAY
import com.example.jump_droid.Constants.SURVIVAL_CRITICAL_THRESHOLD
import com.example.jump_droid.ui.theme.*

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val densityValue = androidx.compose.ui.platform.LocalDensity.current.density
    val sharedPrefs = remember { context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE) }

    var gameState by remember { mutableStateOf(GameState.TITLE) }
    var previousState by remember { mutableStateOf(GameState.MAIN_MENU) }

    var highestYReached by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var score by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var runDurationTimer by remember { mutableFloatStateOf(0f) }
    var airborneTimer by remember { mutableFloatStateOf(0f) }
    var noOverheatTimer by remember { mutableFloatStateOf(0f) }
    var platformStayTimer by remember { mutableFloatStateOf(0f) }
    var perfectRunTimer by remember { mutableFloatStateOf(0f) }
    var hasTakenDamageThisRun by remember { mutableStateOf(false) }
    var totalHazardHits by remember { mutableIntStateOf(0) }
    var totalFuelPickups by remember { mutableIntStateOf(0) }
    var totalPowerUps by remember { mutableIntStateOf(0) }
    var totalPlatformLandings by remember { mutableIntStateOf(0) }
    var totalBossesDefeated by remember { mutableIntStateOf(0) }
    var totalArtifactsCollected by remember { mutableIntStateOf(0) }
    var totalDashes by remember { mutableIntStateOf(0) }
    var momentumValue by remember { mutableFloatStateOf(0f) }
    var comboMaintainTimer by remember { mutableFloatStateOf(0f) }
    var overheatCount by remember { mutableIntStateOf(0) }
    var wasNearDeath by remember { mutableStateOf(false) }
    var consecutiveWins by remember { mutableIntStateOf(0) }
    var activeDiscovery by remember { mutableStateOf<DiscoveryType?>(null) }
    var unlockedRocket by remember { mutableStateOf<RocketType?>(null) }
    var codexNotification by remember { mutableStateOf<DiscoveryType?>(null) }
    var signalDecodedMissionName by remember { mutableStateOf<String?>(null) }

    val discoveryManager = remember { DiscoveryManager(sharedPrefs) }
    val progressionManager = remember { ProgressionManager(sharedPrefs) }
    val missionManager = remember { MissionManager(progressionManager) }
    
    val threatManager = remember {
        ThreatManager().apply {
            onThreatDestroyed = { def ->
                if (def.type == ThreatType.BOSS || def.type == ThreatType.MINI_BOSS) totalBossesDefeated++
            }
        }
    }
    val comboManager = remember { ComboManager() }
    val flyingRewards = remember { mutableStateListOf<FlyingReward>() }
    val platformManager = remember { PlatformManager() }

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var groundY by remember { mutableFloatStateOf(0f) }
    val player = remember { Player(0f, 0f) }
    val altitudeManager = remember { AltitudeManager() }
    val backgroundRenderer = remember { ZoneBackgroundRenderer() }
    val ambientManager = remember { AmbientManager() }
    val rocketRenderer = remember { RocketRenderer() }
    val platformRenderer = remember { PlatformRenderer() }
    val platforms = remember { mutableStateListOf<Platform>() }
    val powerUpManager = remember { PowerUpManager() }
    val landingEffects = remember { mutableStateListOf<LandingEffect>() }
    val particles = remember { mutableStateListOf<Particle>() }
    val floatingTextManager = remember { FloatingTextManager() }

    var missionHintRotationTimer by remember { mutableFloatStateOf(0f) }
    var globalShowObjective by remember { mutableStateOf(false) }
    var gameTime by remember { mutableLongStateOf(0L) }
    var cameraY by remember { mutableFloatStateOf(0f) }
    var isThrusting by remember { mutableStateOf(false) }
    var thrustTarget by remember { mutableStateOf(Offset.Zero) }
    var screenShake by remember { mutableFloatStateOf(0f) }
    var impactFlashAlpha by remember { mutableFloatStateOf(0f) }
    var globalFogAlpha by remember { mutableFloatStateOf(0f) }
    var effectiveThrust by remember { mutableStateOf(false) }
    var effectiveTarget by remember { mutableStateOf(Offset.Zero) }
    var infiniteFuel by remember { mutableStateOf(false) }
    var disableHeat by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }

    val bossesSpawned = remember { mutableStateSetOf<String>() }
    val notificationManager = remember { NotificationManager() }

    SideEffect { 
        missionManager.onMissionCompleted = { progressionManager.recordMissionCompletion(it.id) }
        missionManager.onHiddenSignalRevealed = { signalDecodedMissionName = it.name }
        progressionManager.onModuleUnlocked = { module ->
            floatingTextManager.add(FloatingText(
                text = "MODULE UNLOCKED: ${module.name.uppercase()}",
                x = player.x,
                y = player.y - 100f,
                color = SciFiGold,
                isCritical = true
            ))
            screenShake = 15f
        }
        progressionManager.onLoreLogDiscovered = { log ->
            floatingTextManager.add(FloatingText(
                text = "LORE LOG RECOVERED: ${log.title.uppercase()}",
                x = player.x,
                y = player.y - 120f,
                color = SciFiWhite,
                isCritical = false
            ))
            notificationManager.post("SIGNAL ARCHIVED: ${log.category.name}")
            screenShake = 8f
        }
        progressionManager.onBlueprintUnlocked = { type ->
            floatingTextManager.add(FloatingText(
                text = "BLUEPRINT ACQUIRED: ${type.displayName.uppercase()}",
                x = player.x,
                y = player.y - 140f,
                color = SciFiGold,
                isCritical = true
            ))
            impactFlashAlpha = 0.4f
        }
    }

    val survivalManager = remember { SurvivalManager() }
    val encounterDirector = remember { EncounterDirector() }
    val projectileManager = remember { ProjectileManager() }
    val inputBufferManager = remember { InputBufferManager() }

    var majorWarningText by remember { mutableStateOf<String?>(null) }
    var majorWarningTimer by remember { mutableFloatStateOf(0f) }
    var escalationSpawnId by remember { mutableStateOf<String?>(null) }
    var escalationSpawnX by remember { mutableFloatStateOf(0f) }
    var escalationSpawnY by remember { mutableFloatStateOf(0f) }
    var escalationCountdown by remember { mutableFloatStateOf(0f) }

    val loadoutManager = remember { LoadoutManager(sharedPrefs) }

    LaunchedEffect(gameState) {
        if (gameState != GameState.PLAYING) {
            isThrusting = false
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED
                    isThrusting = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun spawnBurst(x: Float, y: Float, count: Int, color: Color, speed: Float = 100f) {
        repeat(count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val s = Random.nextFloat() * speed
            particles.add(Particle(
                x = x, y = y, vx = cos(angle) * s, vy = sin(angle) * s,
                life = 0.5f + Random.nextFloat() * 0.5f,
                color = color, size = 5f + Random.nextFloat() * 5f
            ))
        }
    }

    fun checkDiscovery(type: DiscoveryType, forceTutorialState: Boolean = true) {
        val isNew = discoveryManager.discover(type)
        
        if (type.category == "ARTIFACTS") {
            progressionManager.recordArtifactDiscovery(type, score, altitudeManager.currentZone)
            totalArtifactsCollected++
        }

        if (isNew) {
            progressionManager.updateRank()
            codexNotification = type
            activeDiscovery = type
            if (forceTutorialState) gameState = GameState.TUTORIAL

            // Mission Hook - General Discovery (Only count if it's an ARTIFACT discovery for generic goals)
            if (type.category == "ARTIFACTS") {
                missionManager.updateProgress(MissionType.DISCOVERY) { it.id.contains("art_find") }
            }

            // Mission Hook - Specific Discoveries
            missionManager.activeMissions.find { !it.isCompleted && it.type == MissionType.DISCOVERY }?.let { active ->
                val match = when(active.id) {
                    "disc_ray" -> type.title.contains("Sky Ray", ignoreCase = true)
                    "disc_echo" -> type.title.contains("Derelict Echo", ignoreCase = true)
                    "disc_anomaly" -> type.title.contains("Void Anomaly", ignoreCase = true)
                    else -> false
                }
                if (match) missionManager.completeMission(active.id)
            }
        }
    }

    fun handleRewardCollection(reward: ComboReward) {
        val rewardColor = when (val r = reward) {
            is ComboReward.Fuel -> SciFiGreen
            is ComboReward.PowerUp -> when(r.type) {
                PowerUpType.HULL_REPAIR -> SciFiGreen
                else -> SciFiCyan
            }
            is ComboReward.AltitudeBoost -> SciFiWhite
            is ComboReward.Artifact -> SciFiPurple
        }
        val rewardName = when (reward) {
            is ComboReward.Fuel -> "FUEL RECOVERED"
            is ComboReward.PowerUp -> reward.type.name.replace("_", " ")
            is ComboReward.AltitudeBoost -> "ALTITUDE BOOST"
            is ComboReward.Artifact -> "ARTIFACT DISCOVERED"
        }

        // Tactical feedback near rocket (Always shown)
        floatingTextManager.add(FloatingText(rewardName, player.x, player.y - 100f, color = rewardColor, isCritical = reward is ComboReward.Artifact))

        // High-priority notifications only
        if (reward is ComboReward.Artifact || reward is ComboReward.AltitudeBoost) {
            notificationManager.post(rewardName)
        }

        when (reward) {
            is ComboReward.Fuel -> {
                player.fuel = min(player.maxFuel, player.fuel + reward.amount)
                spawnBurst(player.x, player.y, 20, SciFiGreen, 200f)
            }
            is ComboReward.PowerUp -> {
                powerUpManager.add(player.x, player.y - 100f, reward.type, isMissionReward = true)
            }
            is ComboReward.AltitudeBoost -> {
                player.velocityY = -2500f
                spawnBurst(player.x, player.y + 50f, 40, SciFiWhite, 500f)
                screenShake = 20f
            }
            is ComboReward.Artifact -> {
                checkDiscovery(reward.discoveryType)
                spawnBurst(player.x, player.y, 30, SciFiPurple, 300f)
                impactFlashAlpha = 0.6f
            }
        }
    }

    fun checkUnlock(newScore: Int) {
        val stats = GameStats(
            totalFlightTime = airborneTimer,
            totalPlatformTime = platformStayTimer,
            zeroHeatTime = noOverheatTimer,
            fuelPickupsCollected = totalFuelPickups,
            powerUpsCollected = totalPowerUps,
            platformLandings = totalPlatformLandings,
            maxCombo = comboManager.bestComboThisRun,
            currentCombo = comboManager.currentCombo,
            comboMaintainTime = comboMaintainTimer,
            bossesDefeated = totalBossesDefeated,
            codexUnlocked = progressionManager.getTotalDiscoveries(),
            maxAltitude = newScore.toFloat(),
            maxMomentum = momentumValue,
            hazardHitsSurvived = totalHazardHits,
            perfectRunTime = perfectRunTimer,
            artifactsCollected = totalArtifactsCollected,
            dashesPerRun = totalDashes,
            overheatCount = player.totalOverheats,
            wasNearDeath = wasNearDeath,
            consecutiveWins = consecutiveWins
        )
        progressionManager.checkUnlocks(
            stats = stats,
            player = player,
            onRocketUnlock = { type ->
                unlockedRocket = type
                gameState = GameState.UNLOCK
            },
            onAchievementUnlock = { achievement ->
                floatingTextManager.add(FloatingText("ACHIEVEMENT: ${achievement.title}", player.x, player.y - 200f, color = SciFiGold, isCritical = true))
            },
            onLoreDiscovery = { type ->
                checkDiscovery(type, forceTutorialState = false)
            }
        )
    }

    fun saveHighScore(newScore: Int) {
        progressionManager.saveHighScore(newScore)
    }

    fun applyDamage(amount: Float) {
        if (amount > 0) {
            totalHazardHits++
            hasTakenDamageThisRun = true
        }
        survivalManager.applyDamage(
            amount = amount,
            player = player,
            isGameOver = gameState != GameState.PLAYING,
            onGameOver = {
                gameState = GameState.GAMEOVER
                saveHighScore(score)
                
                // Commit to Intelligence Network
                val stats = GameStats(
                    totalFlightTime = airborneTimer,
                    totalPlatformTime = platformStayTimer,
                    zeroHeatTime = noOverheatTimer,
                    fuelPickupsCollected = totalFuelPickups,
                    powerUpsCollected = totalPowerUps,
                    platformLandings = totalPlatformLandings,
                    maxCombo = comboManager.bestComboThisRun,
                    currentCombo = comboManager.currentCombo,
                    comboMaintainTime = comboMaintainTimer,
                    bossesDefeated = totalBossesDefeated,
                    codexUnlocked = progressionManager.getTotalDiscoveries(),
                    maxAltitude = score.toFloat(),
                    maxMomentum = momentumValue,
                    hazardHitsSurvived = totalHazardHits,
                    perfectRunTime = perfectRunTimer,
                    artifactsCollected = totalArtifactsCollected,
                    dashesPerRun = totalDashes,
                    overheatCount = player.totalOverheats,
                    wasNearDeath = wasNearDeath,
                    consecutiveWins = consecutiveWins
                )
                progressionManager.commitSessionStats(stats)
            },
            onVisualFeedback = { shake, flash ->
                screenShake = shake
                impactFlashAlpha = flash
            },
            onBurst = { x, y, count, color, speed ->
                spawnBurst(x, y, count, color, speed)
            }
        )
    }

    fun handleLanding(platform: Platform?, yTop: Float) {
        // Triggered EXACTLY once per unique landing event
        player.velocityY = 0f
        
        // Task 2: Platform Reuse Check
        val alreadyLanded = platform?.hasBeenLandedOn ?: false
        
        if (!alreadyLanded) {
            player.squashStretch = 0.8f // Squash on landing
            landingEffects.add(LandingEffect(player.x, yTop))
            spawnBurst(player.x, yTop, 15, SciFiBorder, 120f)
            platform?.hasBeenLandedOn = true
            
            // EPIC 10: Kinetic Battery landing effect
            if (player.kineticBatteryTimer > 0f) {
                player.fuel = min(player.maxFuel, player.fuel + 5f)
                player.shield = min(player.maxShield, player.shield + 2f)
                floatingTextManager.add(FloatingText("ENERGY RECOVERED", player.x, yTop - 60f, color = Color.White))
                spawnBurst(player.x, yTop, 20, Color.White, 200f)
            }
        }

        if (platform != null) {
            // EPIC 7: Module Landing Hook
            player.activeModules.forEach { it.onLanding(player, platform) }

            if (!alreadyLanded) {
                // Task 1: Generic landing missions audit
                missionManager.updateProgress(MissionType.PLATFORMING) { it.id.startsWith("plat_land_") }
                totalPlatformLandings++
            }

            when (platform.type) {
                PlatformType.BOOST -> { 
                    player.velocityY = -600f
                    checkDiscovery(DiscoveryType.BOOST_PLATFORM)
                    if (!alreadyLanded) {
                        spawnBurst(player.x, yTop, 25, SciFiGold, 400f)
                        screenShake = 10f
                        // Task 1: Boost specific mission audit
                        missionManager.updateProgress(MissionType.PLATFORMING) { it.id == "plat_boost" }
                    }
                }
                PlatformType.ICE -> { 
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.velocityX *= 0.98f
                    checkDiscovery(DiscoveryType.ICE_PLATFORM) 
                }
                PlatformType.MOVING -> { 
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.velocityX = platform.speed
                    checkDiscovery(DiscoveryType.MOVING_PLATFORM)
                    if (!alreadyLanded) {
                        // Task 1: Moving specific mission audit
                        missionManager.updateProgress(MissionType.PLATFORMING) { it.id == "plat_moving" }
                    }
                }
                PlatformType.FUEL -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.fuel = min(player.maxFuel, player.fuel + 50f)
                    spawnBurst(player.x, yTop, 20, SciFiGreen, 200f)
                    floatingTextManager.add(FloatingText("FUEL RECHARGE", player.x, player.y - 100f, color = SciFiGreen))
                    checkDiscovery(DiscoveryType.FUEL_PLATFORM)
                }
                PlatformType.COOLING -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.heat = max(0f, player.heat - 30f)
                    spawnBurst(player.x, yTop, 20, SciFiCyan, 200f)
                    floatingTextManager.add(FloatingText("ENGINES COOLED", player.x, player.y - 100f, color = SciFiCyan))
                    checkDiscovery(DiscoveryType.COOLING_PLATFORM)
                }
                PlatformType.STABILITY -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.stabilityTimer = 10f
                    spawnBurst(player.x, yTop, 20, SciFiWhite, 200f)
                    floatingTextManager.add(FloatingText("FLIGHT STABILIZED", player.x, player.y - 100f, color = SciFiWhite))
                    checkDiscovery(DiscoveryType.STABILITY_PLATFORM)
                }
                PlatformType.FLUX -> {
                    if (player.fluxCooldown <= 0f) {
                        val oldX = player.x
                        player.x = screenWidth - player.x
                        player.fluxCooldown = 0.2f
                        spawnBurst(oldX, yTop, 20, SciFiPurple, 200f)
                        spawnBurst(player.x, yTop, 30, SciFiPurple, 300f)
                        floatingTextManager.add(FloatingText("FLUX TELEPORT", player.x, player.y - 100f, color = SciFiPurple))
                    }
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                }
                PlatformType.GRAVITON -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    floatingTextManager.add(FloatingText("GRAVITY WELL", player.x, player.y - 100f, color = SciFiRed))
                }
                PlatformType.MAGNETIC -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    checkDiscovery(DiscoveryType.MAGNETIC_PLATFORM)
                }
                PlatformType.PHASE -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.velocityX *= HORIZONTAL_DAMPING
                    checkDiscovery(DiscoveryType.PHASE_PLATFORM)
                }
                PlatformType.BREAKABLE -> { 
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.velocityX *= HORIZONTAL_DAMPING
                    platform.isBreaking = true
                    checkDiscovery(DiscoveryType.BREAKABLE_PLATFORM)
                }
                PlatformType.CONVEYOR -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    checkDiscovery(DiscoveryType.CONVEYOR_PLATFORM)
                }
                PlatformType.MIMIC -> {
                    // Shatters immediately
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    platform.isBreaking = true
                    platform.crackTime = platform.totalBreakTime // Boom
                    player.integrity = max(0f, player.integrity - 15f)
                    spawnBurst(player.x, yTop, 30, Color.Red, 400f)
                    floatingTextManager.add(FloatingText("MIMIC TRAP!", player.x, player.y - 100f, color = Color.Red))
                    checkDiscovery(DiscoveryType.MIMIC_PLATFORM)
                }
                else -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.velocityX *= HORIZONTAL_DAMPING
                    if (platform.isTrapPlatform) platform.isBreaking = true
                    checkDiscovery(DiscoveryType.NORMAL_PLATFORM)
                }
            }
        } else {
            // Ground landing
            player.velocityY = -player.velocityY * 0.2f
            if (abs(player.velocityY) < 50f) player.velocityY = 0f
            player.combo = 0
            player.lastLandingTime = 0L
            player.lastPlatform = null
        }
        
        // Combo Logic
        if (platform != null && platform != player.lastPlatform) {
            if (player.comboFreezeTimer <= 0f) {
                if (!alreadyLanded) {
                    comboManager.onLanding()
                    player.combo = comboManager.currentCombo // Keep synced if needed by other systems
                }
            }
            player.lastLandingTime = gameTime
            player.lastPlatform = platform
        }
    }


    LaunchedEffect(Unit) {
        altitudeManager.onZoneChanged = { newZone ->
            discoveryManager.discoverZone(newZone)

            // Task 1: Zone Progression Celebration Effects
            when (newZone) {
                AltitudeZone.CLOUD_LAYER -> {
                    spawnBurst(player.x, player.y, 50, SciFiCyan, 600f)
                    screenShake = 10f
                }
                AltitudeZone.UPPER_ATMOSPHERE -> {
                    impactFlashAlpha = 0.4f
                }
                AltitudeZone.ORBIT -> {
                    impactFlashAlpha = 1.0f
                    screenShake = 20f
                    spawnBurst(player.x, player.y, 100, SciFiCyan, 800f)
                }
                AltitudeZone.DEEP_SPACE -> {
                    impactFlashAlpha = 0.6f
                    spawnBurst(player.x, player.y, 60, SciFiPurple, 500f)
                }
                AltitudeZone.VOID -> {
                    impactFlashAlpha = 0.8f // High impact flash
                    screenShake = 15f
                }
                else -> {}
            }
        }
    }

    fun continueRun() {
        if (continuesUsed >= 1) return

        // Find the lowest visible platform to respawn on (Excluding Phase Platforms for safety)
        val visibleBottom = cameraY + screenHeight
        val visibleTop = cameraY
        val visiblePlatforms = platforms.filter { 
            it.y in visibleTop..visibleBottom && it.type != PlatformType.PHASE 
        }

        val spawnPlatform = if (visiblePlatforms.isNotEmpty()) {
            visiblePlatforms.maxByOrNull { it.y }
        } else {
            null
        }

        if (spawnPlatform != null) {
            player.x = spawnPlatform.x + spawnPlatform.width / 2f
            player.y = spawnPlatform.y - ROCKET_HEIGHT / 2f
        } else {
            player.x = screenWidth / 2f
            player.y = groundY
            cameraY = 0f
        }

        player.velocityX = 0f
        player.velocityY = 0f
        player.fuel = player.maxFuel * 0.5f // Restore 50% fuel
        player.maxIntegrity = progressionManager.permanentMaxIntegrity + progressionManager.getHullBonusAmount()
        player.integrity = player.maxIntegrity * 0.5f // Restore 50% integrity
        player.shield = player.maxShield * 0.5f // Restore 50% shield
        player.destructionTimer = 0f
        player.heat = 0f
        player.isOverheated = false
        player.lastPlatform = spawnPlatform
        player.invulnerabilityTimer = 3.0f // 3 seconds of protection

        // Re-entry effect
        spawnBurst(player.x, player.y - 100f, 40, SciFiWhite, 300f)
        screenShake = 15f
        impactFlashAlpha = 1.0f
        floatingTextManager.add(FloatingText("SYSTEM REBOOTED", player.x, player.y - 150f, color = SciFiCyan))

        continuesUsed++
        gameState = GameState.PLAYING
    }

    fun generatePlatform(lastY: Float): Platform {
        return platformManager.generate(score, screenWidth, lastY)
    }

    // --- Developer Cheats ---
    fun jumpToZone(zone: AltitudeZone) {
        score = zone.threshold
        highestYReached = groundY - score * 10f
        player.y = highestYReached
        cameraY = player.y - screenHeight * 0.5f
        player.velocityX = 0f
        player.velocityY = 0f
        platforms.clear()
        var currentY = player.y - 250f
        repeat(15) {
            generatePlatform(currentY).let {
                platforms.add(it)
                currentY = it.y
            }
        }
        altitudeManager.updateAltitude(score)
        gameState = GameState.PLAYING
    }

    fun spawnDevThreat(id: String) {
        ThreatRegistry.getById(id)?.let { def ->
            threatManager.spawnThreat(def, player.x, cameraY - 100f)
        }
        gameState = GameState.PLAYING
    }

    fun spawnDevPowerUp(type: PowerUpType) {
        powerUpManager.add(player.x, cameraY - 200f, type)
        gameState = GameState.PLAYING
    }

    fun spawnDevPlatform(type: PlatformType) {
        platforms.add(Platform(player.x, cameraY - 200f, 150f, type))
        gameState = GameState.PLAYING
    }

    fun unlockAll() {
        RocketType.entries.forEach { sharedPrefs.edit { putBoolean("unlock_${it.name}", true) } }
        DiscoveryType.entries.forEach { sharedPrefs.edit { putBoolean("discovery_$it", true) } }
        missionManager.updateProgress(MissionType.EXPLORATION, absoluteValue = score)
        missionManager.selectNextMission()

        // Handle Completed Missions (Rewards)
        missionManager.activeMissions.filter { it.isCompleted }.forEach { mission ->
            // Grant Reward
            mission.rewards.forEach { reward ->
                when (reward) {
                    is MissionReward.PowerUp -> {
                        repeat(reward.amount) {
                            powerUpManager.add(player.x, cameraY - 100f, reward.type)
                        }
                    }
                    is MissionReward.Artifact -> {
                        checkDiscovery(reward.discoveryType, forceTutorialState = false)
                    }
                    else -> {}
                }
            }

            // Visuals
            floatingTextManager.add(FloatingText("MISSION COMPLETE!", player.x, player.y - 150f, color = SciFiGreen, isCritical = true))
            spawnBurst(player.x, player.y - 100f, 30, SciFiGreen, 400f)
        }
        missionManager.selectNextMission()
    }

    fun spawnEscalationThreat(id: String, droneX: Float, droneY: Float) {
        val spawnY = droneY - 100f
        when (id) {
            "HAZ_LIGHTNING" -> {
                ThreatRegistry.getById(id)?.let { def ->
                    val offsetX = if (Random.nextBoolean()) -400f else 400f
                    threatManager.spawnThreat(def, player.x + offsetX, spawnY - 100f, vy = 60f)
                }
            }
            "HAZ_TURBULENCE" -> {
                val side = if (player.x > screenWidth / 2f) -1f else 1f
                val sx = if (side > 0) -200f else screenWidth + 200f
                ThreatRegistry.getById(id)?.let { def ->
                    threatManager.spawnThreat(def, sx, spawnY, vx = side * 250f, vy = Random.nextFloat() * 100f)
                }
            }
            "HAZ_EMP" -> {
                ThreatRegistry.getById(id)?.let { def ->
                    threatManager.spawnThreat(def, droneX, spawnY)
                }
            }
            "HAZ_GRAVITY" -> {
                ThreatRegistry.getById(id)?.let { def ->
                    val gx = player.x + (Random.nextFloat() - 0.5f) * 400f
                    threatManager.spawnThreat(def, gx, droneY - 50f, vy = 40f)
                }
            }
            "HAZ_VOID_ANOMALY" -> {
                val offsetX = if (Random.nextBoolean()) -350f else 350f
                ThreatRegistry.getById(id)?.let { def ->
                    threatManager.spawnThreat(def, player.x + offsetX, spawnY, vy = 80f)
                }
            }
        }
    }

    fun restartGame() {
        if (screenWidth <= 0f) return
        player.x = screenWidth / 2f
        player.y = groundY
        player.velocityX = 0f
        player.velocityY = 0f
        player.maxFuel = BASE_FUEL_CAPACITY * player.rocketType.fuelMult
        player.fuel = player.maxFuel
        
        // EPIC 5: Survival Initialization
        player.maxIntegrity = progressionManager.permanentMaxIntegrity + progressionManager.getHullBonusAmount()
        player.integrity = player.maxIntegrity
        player.maxShield = progressionManager.permanentMaxShield
        player.shield = player.maxShield
        player.shieldRegenPauseTimer = 0f

        player.heat = 0f
        player.isOverheated = false
        player.lastPlatform = null
        player.combo = 0
        player.turboTimer = 0f
        player.efficiencyTimer = 0f
        player.stabilityTimer = 0f
        player.infiniteShield = false
        player.invincibleHull = false
        player.activeTether = null
        player.inputLatency = 0f

        // EPIC 7: Module Initialization
        player.activeModules.clear()
        player.moduleCooldowns.clear()

        // Native Traits Initialization
        player.discoveryRangeMultiplier = if (player.rocketType == RocketType.BALANCED) 1.2f else 1.0f

        loadoutManager.getActiveModules(progressionManager).forEach {
            player.activeModules.add(it)
            it.onEquip(player)
        }

        gameTime = 0L
        runDurationTimer = 0f
        airborneTimer = 0f
        noOverheatTimer = 0f
        platformStayTimer = 0f
        perfectRunTimer = 0f
        hasTakenDamageThisRun = false
        totalHazardHits = 0
        totalFuelPickups = 0
        totalPowerUps = 0
        totalPlatformLandings = 0
        totalBossesDefeated = 0
        totalArtifactsCollected = 0
        totalDashes = 0
        momentumValue = 0f
        comboMaintainTimer = 0f
        overheatCount = 0
        wasNearDeath = false

        powerUpManager.spawnTimer = 0f
        missionManager.clearCeremonies()
        globalFogAlpha = 0f
        effectiveThrust = false
        effectiveTarget = Offset.Zero
        score = 0
        altitudeManager.updateAltitude(0)
        highestYReached = groundY
        cameraY = 0f
        continuesUsed = 0
        platforms.clear()
        powerUpManager.powerUps.clear()
        landingEffects.clear()
        particles.clear()
        threatManager.clear()
        projectileManager.clear()
        inputBufferManager.clear()
        missionManager.clear()
        bossesSpawned.clear()
        majorWarningText = null
        majorWarningTimer = 0f
        escalationSpawnId = null
        escalationCountdown = 0f
        notificationManager.clear()
        floatingTextManager.clear()
        platformManager.reset()

        comboManager.reset()
        flyingRewards.clear()

        missionManager.selectNextMission()

        var lastY = groundY - 250f
        repeat(15) {
            generatePlatform(lastY).let {
                platforms.add(it)
                lastY = it.y
            }
        }
        gameState = GameState.PLAYING
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (gameState == GameState.PLAYING) {
                        isThrusting = true
                        thrustTarget = down.position
                        player.squashStretch = 1.2f // Stretch on takeoff
                        spawnBurst(player.x, player.y + ROCKET_HEIGHT / 2, 10, SciFiWhite.copy(alpha = 0.5f), 50f)
                    }
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyDown = event.changes.any { it.pressed }
                        if (!anyDown) {
                            isThrusting = false
                            break
                        }
                        thrustTarget = event.changes.firstOrNull { it.pressed }?.position ?: thrustTarget
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        LaunchedEffect(maxWidth, maxHeight) {
            screenWidth = with(density) { maxWidth.toPx() }
            screenHeight = with(density) { maxHeight.toPx() }
            groundY = screenHeight - ROCKET_HEIGHT - 50f
            if (gameState == GameState.PLAYING) restartGame()
        }

        LaunchedEffect(gameState) {
            if (gameState == GameState.PLAYING) {
                var lastFrameTime = 0L
                while (gameState == GameState.PLAYING) {
                    withFrameNanos { currentTime ->
                        if (lastFrameTime == 0L) { lastFrameTime = currentTime; return@withFrameNanos }
                        val dt = (currentTime - lastFrameTime) / 1_000_000_000f
                        lastFrameTime = currentTime
                        if (screenWidth <= 0f) return@withFrameNanos

                        // EPIC 7: Module Update Hook
                        player.activeModules.forEach { module ->
                            module.onUpdate(player, dt, onSpawnPlatform = { x, y, type ->
                                if (platforms.size < 50) {
                                    platforms.add(Platform(x, y, 150f, type))
                                }
                            })
                        }

                        // Update systems
                        inputBufferManager.recordInput(isThrusting, thrustTarget, gameTime)
                        val (frameEffThrust, frameEffTarget) = inputBufferManager.getEffectiveThrust(gameTime, player.inputLatency)
                        effectiveThrust = frameEffThrust
                        effectiveTarget = frameEffTarget

                        discoveryManager.update(dt)
                        threatManager.update(dt, cameraY, screenHeight, screenWidth, player, powerUpManager.powerUps)
                        projectileManager.update(dt)
                        ambientManager.update(dt, cameraY, screenWidth, screenHeight, altitudeManager.currentZone)

                        // --- COMBO SYSTEM UPDATE ---
                        val prevPending = comboManager.pendingReward
                        comboManager.update(dt)
                        
                        // Task 6: Survival Rewards Milestones
                        if (comboManager.immediateSurvivalRewards.isNotEmpty()) {
                            comboManager.immediateSurvivalRewards.forEach { reward ->
                                flyingRewards.add(FlyingReward(
                                    type = reward,
                                    x = player.x,
                                    y = player.y - cameraY,
                                    targetX = screenWidth - 60f,
                                    targetY = screenHeight / 2f,
                                    scale = 4.0f
                                ))
                                notificationManager.post("COMBO MILESTONE: SURVIVAL DROP")
                                checkDiscovery(DiscoveryType.EFFICIENCY_SURVIVAL)
                            }
                            comboManager.immediateSurvivalRewards.clear()
                        }

                        if (comboManager.pendingReward != null && comboManager.pendingReward != prevPending) {
                            // Combo Ended with Reward (Task 5: Tier 3)
                            
                            // NEW COMBO HIGH CELEBRATION (Sprint E: Polish)
                            if (comboManager.isNewHighReached) {
                                floatingTextManager.add(FloatingText("NEW COMBO HIGH!", player.x, player.y - 150f, color = SciFiGold, isCritical = true))
                                screenShake = max(screenShake, 35f)
                                impactFlashAlpha = max(impactFlashAlpha, 0.7f)
                                
                                // Large energy burst
                                repeat(50) {
                                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                                    val s = 400f + Random.nextFloat() * 400f
                                    particles.add(Particle(
                                        x = player.x, y = player.y, vx = cos(angle) * s, vy = sin(angle) * s,
                                        life = 1.5f, color = SciFiGold, size = 6f
                                    ))
                                }
                            }

                            flyingRewards.add(FlyingReward(
                                type = comboManager.pendingReward!!,
                                x = screenWidth - 60f,
                                y = screenHeight / 2f + (120f * densityValue),
                                scale = 3.5f // Task 2: Increased scale
                            ))
                            notificationManager.post("COMBO COMPLETE: x${comboManager.lastFinalStreak}")
                            
                            // Visual pay-off for combo completion
                            screenShake = max(screenShake, 25f)
                            impactFlashAlpha = max(impactFlashAlpha, 0.5f)
                            repeat(30) {
                                val angle = Random.nextFloat() * 2f * PI.toFloat()
                                particles.add(Particle(
                                    x = screenWidth - 60f, y = screenHeight / 2f + (120f * densityValue),
                                    vx = cos(angle) * 450f, vy = sin(angle) * 450f,
                                    life = 1.2f, color = SciFiCyan, size = 5f
                                ))
                            }
                        }

                        // Update flying rewards
                        val rewardIter = flyingRewards.iterator()
                        while (rewardIter.hasNext()) {
                            val fr = rewardIter.next()
                            fr.progress += dt * 1.0f // 1 second flight
                            if (fr.progress >= 1.0f) {
                                handleRewardCollection(fr.type)
                                rewardIter.remove()
                            } else {
                                fr.scale = 2.0f * (1.0f - fr.progress) + 1.0f * fr.progress
                                fr.targetX = player.x
                                fr.targetY = player.y - cameraY
                            }
                        }

                        gameTime += (dt * 1000).toLong()
                        if (screenShake > 0) screenShake = max(0f, screenShake - dt * 30f)
                        if (impactFlashAlpha > 0) impactFlashAlpha = max(0f, impactFlashAlpha - dt * 4f)

                        // Set per-frame state
                        val wasOverheatedBefore = player.isOverheated

                        // Notification Queue Processing
                        notificationManager.update(dt)

                        // --- Encounter System Update (AI Director) ---
                        encounterDirector.update(
                            dt = dt,
                            score = score,
                            currentZone = altitudeManager.currentZone,
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            cameraY = cameraY,
                            playerX = player.x,
                            playerY = player.y,
                            bossesSpawned = bossesSpawned,
                            threatManager = threatManager,
                            notificationManager = notificationManager,
                            onDiscovery = { checkDiscovery(it) },
                            onVisualFeedback = { shake, flash ->
                                screenShake = max(screenShake, shake)
                                impactFlashAlpha = max(impactFlashAlpha, flash)
                            }
                        )

                        // (Legacy combo reset logic removed - now handled by ComboManager)

                        floatingTextManager.update(dt)
                        val effectIterator = landingEffects.iterator()
                        while (effectIterator.hasNext()) {
                            val effect = effectIterator.next()
                            effect.life -= dt
                            if (effect.life <= 0) effectIterator.remove()
                        }
                        val particleIterator = particles.iterator()
                        while (particleIterator.hasNext()) {
                            val p = particleIterator.next()
                            p.life -= dt
                            if (p.life <= 0) { particleIterator.remove() } else {
                                p.x += p.vx * dt
                                p.y += p.vy * dt
                                p.vy += 500f * dt
                            }
                        }

                        if (player.turboTimer > 0) player.turboTimer = max(0f, player.turboTimer - dt)
                        if (player.efficiencyTimer > 0) player.efficiencyTimer = max(0f, player.efficiencyTimer - dt)
                        if (player.stabilityTimer > 0) player.stabilityTimer = max(0f, player.stabilityTimer - dt)
                        if (player.fluxCooldown > 0) player.fluxCooldown = max(0f, player.fluxCooldown - dt)
                        if (player.kineticBatteryTimer > 0) player.kineticBatteryTimer = max(0f, player.kineticBatteryTimer - dt)
                        if (player.magneticSiphonTimer > 0) player.magneticSiphonTimer = max(0f, player.magneticSiphonTimer - dt)
                        if (player.overdriveTimer > 0) {
                            player.overdriveTimer = max(0f, player.overdriveTimer - dt)
                            // Overdrive penalty: integrity damage over time
                            player.integrity = max(0f, player.integrity - 2f * dt)
                        }
                        if (player.invulnerabilityTimer > 0) player.invulnerabilityTimer = max(0f, player.invulnerabilityTimer - dt)
                        if (player.comboFreezeTimer > 0) player.comboFreezeTimer = max(0f, player.comboFreezeTimer - dt)
                        if (player.controlInversionTimer > 0) player.controlInversionTimer = max(0f, player.controlInversionTimer - dt)
                        if (player.hudInterferenceTimer > 0) player.hudInterferenceTimer = max(0f, player.hudInterferenceTimer - dt)

                        // --- Intelligence Network (Real-time Stats) ---
                        runDurationTimer += dt
                        if (comboManager.currentCombo >= 20) comboMaintainTimer += dt // Threshold based on lowest combo pro mission
                        
                        momentumValue = max(momentumValue, abs(player.velocityY))

                        val stats = GameStats(
                            totalFlightTime = airborneTimer,
                            totalPlatformTime = platformStayTimer,
                            zeroHeatTime = noOverheatTimer,
                            fuelPickupsCollected = totalFuelPickups,
                            powerUpsCollected = totalPowerUps,
                            platformLandings = totalPlatformLandings,
                            maxCombo = comboManager.bestComboThisRun,
                            currentCombo = comboManager.currentCombo,
                            comboMaintainTime = comboMaintainTimer,
                            bossesDefeated = totalBossesDefeated,
                            codexUnlocked = progressionManager.getTotalDiscoveries(),
                            maxAltitude = score.toFloat(),
                            maxMomentum = momentumValue,
                            hazardHitsSurvived = totalHazardHits,
                            perfectRunTime = perfectRunTimer,
                            artifactsCollected = totalArtifactsCollected,
                            dashesPerRun = totalDashes,
                            overheatCount = player.totalOverheats,
                            wasNearDeath = wasNearDeath,
                            consecutiveWins = consecutiveWins
                        )
                        missionManager.updateProgressAll(stats)
                        missionManager.checkUnlocks()

                        // Major warning timer
                        if (majorWarningTimer > 0f) {
                            majorWarningTimer -= dt
                            if (majorWarningTimer <= 0f) majorWarningText = null
                        }
                        // Escalation countdown — delay between message and threat spawn
                        if (escalationCountdown > 0f && escalationSpawnId != null) {
                            escalationCountdown -= dt
                            if (escalationCountdown <= 0f) {
                                spawnEscalationThreat(escalationSpawnId!!, escalationSpawnX, escalationSpawnY)
                                escalationSpawnId = null
                            }
                        }

                        // Visual squash/stretch lerp back to 1.0
                        player.squashStretch += (1.0f - player.squashStretch) * 10f * dt

                        if (player.isOverheated) {
                            player.overheatTimer -= dt
                            if (player.overheatTimer <= 0) {
                                player.isOverheated = false
                                player.heat = 0f
                                player.totalOverheats++
                            }
                        }

                        // Sub-stepped physics for collision reliability
                        val subSteps = 4
                        val sdt = dt / subSteps
                        repeat(subSteps) {
                            player.isOnPlatform = false // Reset per substep for accurate airborne tracking

                            // --- Intelligence Network Tracking ---
                            if (player.heat == 0f) noOverheatTimer += sdt
                            if (!hasTakenDamageThisRun) perfectRunTimer += sdt

                            player.activeTether?.applyPhysics(player, sdt, cameraY)

                            // a. Update platforms
                            platforms.forEach { p ->
                                if (p.isMoving) {
                                    p.x += p.speed * sdt
                                    if (p.x < 0) { p.x = 0f; p.speed *= -1 }
                                    else if (p.x + p.width > screenWidth) { p.x = screenWidth - p.width; p.speed *= -1 }
                                }
                                if (p.isBreaking) p.crackTime += sdt
                                if (p.isJammed) {
                                    p.jamTimer -= sdt
                                    if (p.jamTimer <= 0) p.isJammed = false
                                }
                            }

                            // b. Player Physics
                            // ... (Wind and Threat code unchanged)
                            
                            // Reset per-substep platform flag
                            // (remnants removed)

                            // ... (Wait, I need to make sure I don't break the wind/threat code)

                            // c. Threat Interactions (Proximity Effects)
                            projectileManager.processPlayerCollision(player) { projectile ->
                                applyDamage(projectile.damage)
                                floatingTextManager.add(FloatingText("PROJECTILE IMPACT", player.x, player.y, color = Color.Red))
                            }

                            threatManager.activeThreats.toList().forEach { threat ->
                                threat.processInteraction(
                                    player = player,
                                    sdt = sdt,
                                    isThrusting = isThrusting,
                                    platforms = platforms,
                                    powerUps = powerUpManager.powerUps,
                                    gameTime = gameTime,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                    cameraY = cameraY,
                                    onVisualFeedback = { shake, flash ->
                                        screenShake = max(screenShake, shake)
                                        impactFlashAlpha = max(impactFlashAlpha, flash)
                                    },
                                    onNotification = { msg, alpha ->
                                        if (alpha != null) notificationManager.showImmediately(msg, alpha)
                                        else notificationManager.showImmediately(msg)
                                    },
                                    onMajorWarning = { msg, duration ->
                                        majorWarningText = msg
                                        majorWarningTimer = duration
                                    },
                                    onFloatingText = { msg, x, y, color, critical, life ->
                                        floatingTextManager.add(FloatingText(msg, x, y, life = life, color = color, isCritical = critical))
                                    },
                                    onDiscovery = { checkDiscovery(it) },
                                    onBurst = { x, y, count, color, speed ->
                                        spawnBurst(x, y, count, color, speed)
                                    },
                                    onScoreUpdate = { score += it },
                                    onMissionProgress = { type ->
                                        missionManager.updateProgress(type)
                                    },
                                    onSpawnGhostPlatform = { x, y ->
                                        if (platforms.size < 40) {
                                            platforms.add(Platform(
                                                initialX = x,
                                                y = y,
                                                width = 150f,
                                                type = PlatformType.NORMAL,
                                                isMoving = false,
                                                initialSpeed = 0f,
                                                totalBreakTime = 0.3f // Breaks on touch
                                            ).apply { isTrapPlatform = true })
                                        }
                                    },
                                    onSpawnReinforcements = {},
                                    onAnchoredText = { floatingTextManager.add(it) },
                                    onEscalationEvent = { threatX, threatY, source ->
                                        if (escalationCountdown <= 0f) {
                                            val zone = altitudeManager.currentZone
                                            val (threatId, msg) = when (zone) {
                                                AltitudeZone.EARTH -> "ENT_SCOUT_DRONE" to "SUMMONING REINFORCEMENTS"
                                                AltitudeZone.CLOUD_LAYER -> "HAZ_LIGHTNING" to "SUMMONING STORM"
                                                AltitudeZone.UPPER_ATMOSPHERE -> "HAZ_TURBULENCE" to "SUMMONING TURBULENCE"
                                                AltitudeZone.ORBIT -> "HAZ_EMP" to "ACTIVATING DEFENSE PROTOCOL"
                                                AltitudeZone.DEEP_SPACE -> "HAZ_GRAVITY" to "SUMMONING ANOMALY"
                                                AltitudeZone.THE_FOUNDRY -> "HAZ_DEBRIS" to "ACTIVATING MANUFACTURING PROTOCOLS"
                                                AltitudeZone.CHRONO_RIFT -> "HAZ_VOID_ANOMALY" to "TEMPORAL DISTORTION DETECTED"
                                                AltitudeZone.VOID, AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE, AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY -> 
                                                    "HAZ_VOID_ANOMALY" to "REALITY BREACH DETECTED"
                                            }
                                            floatingTextManager.add(FloatingText(msg, threatX, threatY - 30f, life = 2.5f, color = SciFiRed, isCritical = false, sourceThreat = source, anchorOffsetY = -30f, shadowColor = SciFiRed, shadowBlur = 25f))
                                            escalationSpawnId = threatId
                                            escalationSpawnX = threatX
                                            escalationSpawnY = threatY
                                            escalationCountdown = 1.5f
                                        }
                                    },
                                    activeThreats = threatManager.activeThreats,
                                    onSpawnProjectile = { x, y, vx, vy, type, owner, damage, color, size, life ->
                                        projectileManager.spawn(x, y, vx, vy, type, owner, damage, color, size, life)
                                    },
                                    onSpawnThreat = { id, x, y, vx, vy ->
                                        ThreatRegistry.getById(id)?.let { def ->
                                            threatManager.spawnThreat(def, x, y, vx, vy)
                                        }
                                    }
                                )
                            }

                            // Task 3: Magnetic Platforms (Proximity Gravity)
                            platforms.forEach { platform ->
                                if (platform.type == PlatformType.MAGNETIC || platform.type == PlatformType.GRAVITON) {
                                    val dx = player.x - (platform.x + platform.width / 2f)
                                    val dy = player.y - (platform.y + PLATFORM_HEIGHT / 2f)
                                    val distSq = dx*dx + dy*dy
                                    val isGraviton = platform.type == PlatformType.GRAVITON
                                    val radius = if (isGraviton) 180f else 250f
                                    
                                    if (distSq < radius * radius) {
                                        val dist = sqrt(distSq)
                                        val force = (1f - dist / radius) * (if (isGraviton) 3000f else 1200f)
                                        player.velocityX -= (dx / dist) * force * sdt
                                        player.velocityY -= (dy / dist) * force * sdt
                                        
                                        // Heavy Feel (Damping)
                                        val dampBase = if (isGraviton) 0.25f else 0.15f
                                        val damp = 1f - dampBase * (1f - dist / radius)
                                        player.velocityX *= damp
                                        player.velocityY *= damp
                                    }
                                }
                            }

                            // d. Player Physics
                            val canThrustNormal = effectiveThrust && (player.fuel > 0f || infiniteFuel) && !player.isOverheated
                            val canSteerPrototype = effectiveThrust && player.isOverheated && player.rocketType == RocketType.EXPERIMENTAL
                            
                            if (canThrustNormal || canSteerPrototype) {
                                // EPIC 7: Module Thrust/Fuel/Heat Hook
                                var thrustMult = 1.0f
                                var fuelMult = 1.0f
                                var steerMult = 1.0f
                                var heatGenMult = 1.0f

                                player.activeModules.forEach {
                                    thrustMult *= it.onThrust(player, sdt)
                                    fuelMult *= it.onFuelConsume(player, sdt)
                                    steerMult *= it.onSteer(player, sdt)
                                }

                                val currentThrust = BASE_THRUST_POWER * player.rocketType.thrustMult * 
                                    (if (player.turboTimer > 0) 1.2f else 1.0f) * 
                                    (if (player.overdriveTimer > 0) 2.0f else 1.0f) *
                                    thrustMult * progressionManager.getThrustMultiplier()

                                if (canThrustNormal) {
                                    player.velocityY -= currentThrust * sdt
                                    val currentConsumption = if (infiniteFuel) 0f else BASE_FUEL_CONSUMPTION * player.rocketType.fuelMult * (if (player.efficiencyTimer > 0) 0.8f else 1.0f) * fuelMult

                                    player.fuel = max(0f, player.fuel - currentConsumption * sdt)
                                    if (!disableHeat) {
                                        player.activeModules.forEach {
                                            heatGenMult *= it.onHeatChange(player, player.heat)
                                        }
                                        player.heat = min(MAX_HEAT, player.heat + HEAT_GENERATION_RATE * player.rocketType.heatMult * heatGenMult * sdt)
                                    }

                                    // Thrust trail particles
                                    if (Random.nextFloat() < 0.4f) {
                                        particles.add(Particle(
                                            x = player.x + (Random.nextFloat() - 0.5f) * 15f,
                                            y = player.y + ROCKET_HEIGHT / 2,
                                            vx = (Random.nextFloat() - 0.5f) * 40f,
                                            vy = 100f + Random.nextFloat() * 150f,
                                            life = 0.3f + Random.nextFloat() * 0.3f,
                                            color = if (player.turboTimer > 0) Color.Cyan else Color(0xFFFF9800),
                                            size = 3f + Random.nextFloat() * 5f
                                        ))
                                    }
                                }

                                // Steering Authority (Common to both normal and prototype)
                                val dx = effectiveTarget.x - player.x
                                val maxSteerDist = screenWidth / 3f
                                val baseSteerMult = if (player.stabilityTimer > 0) 1.2f else 0.7f
                                val steerForce = (dx / maxSteerDist).coerceIn(-1f, 1f) * (if (player.controlInversionTimer > 0f) -1f else 1f)
                                player.velocityX += steerForce * currentThrust * baseSteerMult * steerMult * sdt

                                if (canThrustNormal) {
                                    if (player.heat > MAX_HEAT * 0.7f) checkDiscovery(DiscoveryType.HEAT_SYSTEM)
                                    if (player.heat >= MAX_HEAT && !player.isOverheated) {
                                        player.isOverheated = true
                                        player.overheatTimer = OVERHEAT_COOLDOWN_TIME
                                        isThrusting = false
                                    }
                                }
                            } else if (!player.isOverheated) {
                                var coolingMult = 1.0f
                                player.activeModules.forEach {
                                    coolingMult *= it.onCooling(player, sdt)
                                }
                                player.heat = max(0f, player.heat - COOLING_RATE * coolingMult * progressionManager.getHeatCooldownMultiplier() * sdt)
                            }

                            player.velocityY += BASE_GRAVITY * sdt
                            val oldX = player.x
                            val oldY = player.y
                            player.x += player.velocityX * sdt
                            player.y += player.velocityY * sdt

                            // c. Collision Resolution
                            val rHalfW = ROCKET_WIDTH / 2
                            val rHalfH = ROCKET_HEIGHT / 2

                            platforms.forEach { platform ->
                                if (platform.isBreaking && platform.crackTime > platform.totalBreakTime) return@forEach

                                if (platform.type == PlatformType.PHASE) {
                                    val cycle = 4000L
                                    val progress = (gameTime % cycle) / cycle.toFloat()
                                    // Pass-through when platform is not fully solid (matches visual alpha)
                                    if (progress >= 0.4f) return@forEach
                                }

                                val pLeft = platform.x
                                val pRight = platform.x + platform.width
                                val pTop = platform.y
                                val pBottom = platform.y + PLATFORM_HEIGHT

                                val rLeft = player.x - rHalfW
                                val rRight = player.x + rHalfW
                                val rTop = player.y - rHalfH
                                val rBottom = player.y + rHalfH

                                // Check for overlap (Fixed for resting contact)
                                if (rRight > pLeft && rLeft < pRight && rBottom >= pTop && rTop < pBottom) {

                                    val prevBottom = oldY + rHalfH
                                    val prevTop = oldY - rHalfH
                                    val prevLeft = oldX - rHalfW
                                    val prevRight = oldX + rHalfW

                                    // Resolve by the side that hit first (using state)
                                    val wasAbove = prevBottom <= pTop + 5f
                                    val wasBelow = prevTop >= pBottom - 5f
                                    val wasLeft = prevRight <= pLeft + 5f
                                    val wasRight = prevLeft >= pRight - 5f

                                    if (wasAbove && player.velocityY >= 0) {
                                        player.isOnPlatform = true
                                        // Task 3: Inherit platform movement
                                        if (platform.isMoving) {
                                            player.x += platform.speed * sdt
                                        }
                                        if (platform.type == PlatformType.CONVEYOR) {
                                            player.x += 150f * sdt
                                        }
                                        
                                        if (platform != player.lastPlatform) {
                                            handleLanding(platform, pTop)
                                        } else {
                                            // Regular landing stabilization without event trigger
                                            player.y = pTop - rHalfH
                                            if (player.velocityY > 0) player.velocityY = 0f
                                        }
                                        spawnBurst(player.x, pTop, 10, SciFiBorder, 100f)
                                    }
                                    else if (wasBelow && player.velocityY < 0) {
                                        player.y = pBottom + rHalfH
                                        player.velocityY = -player.velocityY * 0.5f
                                    }
                                    else if (wasLeft) {
                                        player.x = pLeft - rHalfW
                                        player.velocityX = -abs(player.velocityX) * 0.5f
                                    }
                                    else if (wasRight) {
                                        player.x = pRight + rHalfW
                                        player.velocityX = abs(player.velocityX) * 0.5f
                                    } else {
                                        // Final Fallback: Shallowest Penetration
                                        val penL = rRight - pLeft
                                        val penR = pRight - rLeft
                                        val penT = rBottom - pTop
                                        val penB = pBottom - rTop
                                        when (minOf(penL, penR, penT, penB)) {
                                            penT -> {
                                                player.y = pTop - rHalfH
                                                player.isOnPlatform = true
                                                if (player.velocityY > 0) player.velocityY = 0f
                                            }
                                            penB -> { player.y = pBottom + rHalfH; if (player.velocityY < 0) player.velocityY = -player.velocityY * 0.5f }
                                            penL -> { player.x = pLeft - rHalfW; player.velocityX = -abs(player.velocityX) * 0.5f }
                                            penR -> { player.x = pRight + rHalfW; player.velocityX = abs(player.velocityX) * 0.5f }
                                        }
                                    }
                                }
                            }

                            // Ground collision
                            if (player.y > groundY) {
                                player.isOnPlatform = true
                                player.y = groundY
                                if (player.lastPlatform != null) {
                                    handleLanding(null, groundY)
                                } else {
                                    // Stabilize on ground if already there
                                    player.velocityY = -player.velocityY * 0.2f
                                    if (abs(player.velocityY) < 50f) player.velocityY = 0f
                                }
                            }

                            // --- Intelligence Network (Platform tracking) ---
                            if (player.isOnPlatform) platformStayTimer += sdt
                        }

                        // Trigger Overheat Visuals if it just happened
                        if (player.isOverheated && !wasOverheatedBefore) {
                            screenShake = 15f
                            impactFlashAlpha = 1.0f
                            checkDiscovery(DiscoveryType.OVERHEAT_SYSTEM)
                        }

                        // Post-physics friction and bounds
                        player.velocityX *= AIR_FRICTION
                        player.velocityY *= AIR_FRICTION

                        if (player.x < SCREEN_PADDING) { player.x = SCREEN_PADDING; player.velocityX = -player.velocityX * 0.5f }
                        if (player.x > screenWidth - SCREEN_PADDING) { player.x = screenWidth - SCREEN_PADDING; player.velocityX = -player.velocityX * 0.5f }

                        if (player.y - (ROCKET_HEIGHT / 2) > cameraY + screenHeight) { gameState = GameState.GAMEOVER; saveHighScore(score) }

                        // Cleanup broken platforms
                        val platformIter = platforms.iterator()
                        while (platformIter.hasNext()) {
                            val p = platformIter.next()
                            if (p.isBreaking && p.crackTime > p.totalBreakTime) {
                                spawnBurst(p.x + p.width / 2, p.y, 25, Color(0xFFFF9800), 200f)
                                platformIter.remove()
                            }
                        }

                        powerUpManager.updateAutoSpawn(dt, screenWidth, cameraY)
                        powerUpManager.updateMovement(dt, gameTime, player, platforms, cameraY, screenHeight)

                        for (pu in powerUpManager.checkCollection(player)) {
                            totalPowerUps++
                            when (pu.type) {
                                PowerUpType.FUEL_TANK -> {
                                    if (player.maxFuel < Constants.MAX_FUEL_CAPACITY_LIMIT) {
                                        player.maxFuel = min(Constants.MAX_FUEL_CAPACITY_LIMIT, player.maxFuel + 25f)
                                        player.fuel = player.maxFuel
                                        notificationManager.post("FUEL CAPACITY UP!")
                                    } else {
                                        player.fuel = player.maxFuel
                                        notificationManager.post("FUEL REFILLED")
                                    }
                                    totalFuelPickups++
                                    spawnBurst(pu.x, pu.y, 30, SciFiGold, 300f)
                                    screenShake = 5f
                                    checkDiscovery(DiscoveryType.FUEL_TANK)
                                }
                                PowerUpType.TURBO_BOOSTER -> {
                                    player.turboTimer = 8f
                                    spawnBurst(pu.x, pu.y, 30, SciFiCyan, 300f)
                                    screenShake = 5f
                                    notificationManager.post("TURBO ACTIVE!")
                                    checkDiscovery(DiscoveryType.TURBO_BOOSTER)
                                }
                                PowerUpType.EFFICIENCY_MODULE -> {
                                    player.efficiencyTimer = 8f
                                    spawnBurst(pu.x, pu.y, 30, SciFiGreen, 300f)
                                    screenShake = 5f
                                    notificationManager.post("FUEL EFFICIENCY UP!")
                                    checkDiscovery(DiscoveryType.EFFICIENCY_MODULE)
                                }
                                PowerUpType.HEAT_SINK -> {
                                    player.heat = max(0f, player.heat - MAX_HEAT * 0.5f)
                                    player.isOverheated = false
                                    spawnBurst(pu.x, pu.y, 30, SciFiWhite, 300f)
                                    screenShake = 5f
                                    notificationManager.post("ENGINES COOLED!")
                                    checkDiscovery(DiscoveryType.HEAT_SINK)
                                }
                                PowerUpType.SHIELD_CAPSULE -> {
                                    player.shield = min(player.maxShield, player.shield + 25f)
                                    spawnBurst(pu.x, pu.y, 30, SciFiCyan, 400f)
                                    notificationManager.post("SHIELD RECHARGE")
                                    floatingTextManager.add(FloatingText("+25 SHIELD", player.x, player.y - 120f, color = SciFiCyan))
                                    checkDiscovery(DiscoveryType.SHIELD_CAPSULE)
                                }
                                PowerUpType.HULL_REPAIR -> {
                                    player.integrity = min(player.maxIntegrity, player.integrity + 20f)
                                    spawnBurst(pu.x, pu.y, 30, SciFiGreen, 400f)
                                    notificationManager.post("HULL REPAIRED")
                                    floatingTextManager.add(FloatingText("+20 HULL", player.x, player.y - 120f, color = SciFiGreen))
                                    checkDiscovery(DiscoveryType.HULL_REPAIR)
                                }
                                PowerUpType.KINETIC_BATTERY -> {
                                    player.kineticBatteryTimer = 15f
                                    spawnBurst(pu.x, pu.y, 30, Color.White, 300f)
                                    notificationManager.post("KINETIC BATTERY ACTIVE")
                                    checkDiscovery(DiscoveryType.KINETIC_BATTERY)
                                }
                                PowerUpType.MAGNETIC_SIPHON -> {
                                    player.magneticSiphonTimer = 20f
                                    spawnBurst(pu.x, pu.y, 30, Color.Magenta, 300f)
                                    notificationManager.post("MAGNETIC SIPHON ACTIVE")
                                    checkDiscovery(DiscoveryType.MAGNETIC_SIPHON)
                                }
                                PowerUpType.OVERDRIVE_MODULE -> {
                                    player.overdriveTimer = 10f
                                    spawnBurst(pu.x, pu.y, 30, Color.Red, 400f)
                                    notificationManager.post("OVERDRIVE ENGAGED!")
                                    checkDiscovery(DiscoveryType.OVERDRIVE_MODULE)
                                }
                                PowerUpType.ARTIFACT -> {
                                    val artifact = listOf(
                                        DiscoveryType.ART_RECORDER, DiscoveryType.ART_ALLOY,
                                        DiscoveryType.ART_BEACON, DiscoveryType.ART_DRONE
                                    ).random()
                                    checkDiscovery(artifact)
                                    spawnBurst(pu.x, pu.y, 50, SciFiPurple, 400f)
                                    screenShake = 10f
                                    impactFlashAlpha = 0.6f
                                    notificationManager.post("ARTIFACT RECOVERED!")

                                    player.activeModules.forEach { it.onArtifactCollected(player) }
                                }
                                PowerUpType.ALTITUDE_BOOSTER -> {
                                    player.velocityY = -2500f
                                    spawnBurst(pu.x, pu.y, 40, SciFiWhite, 400f)
                                    screenShake = 10f
                                    notificationManager.post("ALTITUDE BOOST!")
                                }
                            }

                            if (pu.isMissionReward) {
                                spawnBurst(player.x, player.y, 40, SciFiGold, 400f)
                            }

                        }

                        if (platforms.isNotEmpty()) {
                            val highestPlatformY = platforms.minOf { it.y }
                            if (highestPlatformY > cameraY - screenHeight) {
                                var lastY = highestPlatformY
                                repeat(5) { generatePlatform(lastY).let { platforms.add(it); lastY = it.y } }
                            }
                        }
                        platforms.removeAll { it.y > cameraY + screenHeight + 600f }

                        // --- Mission System Per-Frame Updates ---
                        runDurationTimer += dt
                        
                        if (!player.isOnPlatform) {
                            airborneTimer += dt
                        }

                        if (!player.isOverheated) {
                            noOverheatTimer += dt
                        } else {
                            if (noOverheatTimer > 0f) {
                                // Task 4: Targeted reset for heat missions
                                missionManager.resetProgress(MissionType.SURVIVAL) { it.id.startsWith("surv_cool") }
                                notificationManager.post("MISSION RESET")
                            }
                            noOverheatTimer = 0f
                        }

                        missionManager.updateProgress(MissionType.EXPLORATION, absoluteValue = score)

                        // --- Mission UX System Per-Frame ---
                        missionHintRotationTimer += dt
                        if (missionHintRotationTimer > 5f) {
                            missionHintRotationTimer = 0f
                            globalShowObjective = !globalShowObjective
                        }

                        // Handle Completed Missions (Ceremony & Rewards)
                        missionManager.activeMissions.forEach { mission ->
                            if (mission.isCompleted && !missionManager.isInCeremony(mission.id)) {
                                missionManager.startCeremony(mission.id)
                                notificationManager.post("MISSION COMPLETE: ${mission.name.uppercase()}")
                                
                                // One-time burst at player
                                spawnBurst(player.x, player.y, 40, SciFiGreen, 350f)
                                screenShake = max(screenShake, 12f)
                                impactFlashAlpha = max(impactFlashAlpha, 0.4f)
                            }
                        }

                        for (mid in missionManager.updateCeremonies(dt)) {
                            val m = missionManager.activeMissions.find { it.id == mid } ?: continue
                            m.ceremonyStage = CeremonyStage.REPLACING

                            // Mission Synergy Bonus: Small progress to other active tracks
                            missionManager.activeMissions.filter { !it.isCompleted && it.id != m.id }.forEach { other ->
                                when (other.type) {
                                    MissionType.PLATFORMING -> {
                                        other.currentProgress = min(other.targetValue, other.currentProgress + 1)
                                        if (other.checkCompletion()) {
                                            other.isCompleted = true
                                            other.ceremonyStage = CeremonyStage.GLOW
                                        }
                                    }
                                    MissionType.SURVIVAL -> {
                                        val bonus = if (other.id.startsWith("surv_air")) 2f else 5f
                                        if (other.id.startsWith("surv_air")) airborneTimer += bonus
                                        else noOverheatTimer += bonus
                                    }
                                    else -> {}
                                }
                            }
                        }

                        // Cycle tracks: removes completed missions after their replacement ceremony stage
                        // and fills any empty mission slots.
                        missionManager.selectNextMission()

                        // --- Survival System Update ---
                        survivalManager.update(
                            dt = dt,
                            player = player,
                            gameTime = gameTime,
                            notificationManager = notificationManager,
                            onGameOver = {
                                gameState = GameState.GAMEOVER
                                saveHighScore(score)
                            },
                            onShake = { screenShake = max(screenShake, it) },
                            shieldRegenMultiplier = progressionManager.getShieldRegenMultiplier()
                        )

                        if (!isThrusting) player.fuel = min(player.maxFuel, player.fuel + FUEL_RECHARGE_RATE * progressionManager.getFuelRegenMultiplier() * dt)
                        if (player.y < highestYReached) {
                            highestYReached = player.y
                            val newScore = ((groundY - highestYReached) / 10f).toInt()
                            if (newScore > score) {
                                score = newScore
                                altitudeManager.updateAltitude(score)
                                checkUnlock(score)
                            }
                        }
                        if (player.y < cameraY + screenHeight * 0.4f) cameraY = player.y - screenHeight * 0.4f
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val shakeX = if (screenShake > 0) (Random.nextFloat() - 0.5f) * screenShake else 0f
            val shakeY = if (screenShake > 0) (Random.nextFloat() - 0.5f) * screenShake else 0f

            translate(shakeX, shakeY) {
                if (gameState == GameState.TITLE || gameState == GameState.MAIN_MENU || gameState == GameState.HANGAR ||
                    gameState == GameState.ARCHIVE || gameState == GameState.SETTINGS || gameState == GameState.ABOUT ||
                    gameState == GameState.LEADERBOARD) {
                    backgroundRenderer.renderTitle(this)
                } else {
                    backgroundRenderer.render(
                        drawScope = this,
                        altitude = score,
                        currentZone = altitudeManager.currentZone,
                        cameraY = cameraY,
                        gameTime = gameTime
                    )

                    drawRealityDistortion(
                        threats = threatManager.activeThreats,
                        playerX = player.x, playerY = player.y,
                        size = size,
                        currentZone = altitudeManager.currentZone,
                        altitude = score
                    )

                    ambientManager.render(this, cameraY, gameTime)
                }

                drawSpeedLines(
                    velocityY = player.velocityY,
                    screenWidth = screenWidth, screenHeight = screenHeight,
                    currentZone = altitudeManager.currentZone
                )

                if (gameState == GameState.PLAYING || gameState == GameState.GAMEOVER || gameState == GameState.TUTORIAL || gameState == GameState.PAUSED || gameState == GameState.HELP || gameState == GameState.UNLOCK) {
                    drawGround(
                        groundY = groundY, cameraY = cameraY,
                        screenWidth = screenWidth, screenHeight = screenHeight,
                        currentZone = altitudeManager.currentZone
                    )

                    drawParticles(
                        particles = particles.toList(),
                        cameraY = cameraY,
                        gameTime = gameTime
                    )
                    drawLandingEffects(
                        effects = landingEffects.toList(),
                        cameraY = cameraY,
                        currentZone = altitudeManager.currentZone
                    )

                    drawProjectiles(
                        projectiles = projectileManager.projectiles.toList(),
                        cameraY = cameraY,
                        gameTime = gameTime
                    )

                    drawTether(
                        tether = player.activeTether,
                        playerX = player.x,
                        playerY = player.y,
                        cameraY = cameraY,
                        gameTime = gameTime
                    )

                    platforms.forEach { platform ->
                        platformRenderer.render(
                            drawScope = this,
                            platform = platform,
                            currentZone = altitudeManager.currentZone,
                            cameraY = cameraY,
                            gameTime = gameTime
                        )
                    }

                    // Render Threats
                    threatManager.activeThreats.toList().forEach { threat ->
                        if (threat.state == ThreatState.ACTIVE) {
                            val lifeCycleAlpha = when (threat.phase) {
                                1 -> (threat.lifetime / (threat.duration * 0.15f)).coerceIn(0f, 1f)
                                3 -> (1f - (threat.lifetime - threat.duration * 0.85f) / (threat.duration * 0.15f)).coerceIn(0f, 1f)
                                else -> 1.0f
                            }
                            
                            val tx = threat.x
                            val ty = threat.y - cameraY

                            ThreatRendererRegistry.forId(threat.definition.id)?.render(
                                this, threat, cameraY, lifeCycleAlpha, gameTime, player
                            )
                            }
                        }
                    }

                    drawPowerUps(
                        powerUps = powerUpManager.powerUps.toList(),
                        cameraY = cameraY,
                        gameTime = gameTime
                    )

                    drawFlyingRewards(
                        rewards = flyingRewards.toList()
                    )

                    // EPIC 7: Module Visual Hook
                    player.activeModules.forEach { module ->
                        module.onDraw(
                            drawScope = this,
                            player = player,
                            cameraY = cameraY,
                            gameTime = gameTime,
                            activeThreats = threatManager.activeThreats.toList(),
                            powerUps = powerUpManager.powerUps.toList(),
                            platforms = platforms.toList()
                        )
                    }

                    // Render Rocket via RocketRenderer
                    rocketRenderer.render(
                        drawScope = this,
                        player = player,
                        isThrusting = effectiveThrust,
                        thrustTarget = effectiveTarget,
                        cameraY = cameraY,
                        gameTime = gameTime
                    )

                    drawVisualObstruction(
                        fogAlpha = globalFogAlpha,
                        playerX = player.x,
                        playerY = player.y,
                        cameraY = cameraY,
                        size = size
                    )
                }

                drawImpactFlash(
                    alpha = impactFlashAlpha,
                    size = size
                )
            }

        // --- Screens ---

        when (gameState) {
            GameState.TITLE -> {
                TitleScreen(onNavigate = { gameState = it })
            }
            GameState.MAIN_MENU -> {
                MainMenuScreen(
                    onLaunch = { restartGame() },
                    onNavigate = { 
                        previousState = GameState.MAIN_MENU
                        gameState = it 
                    },
                    onExit = { (context as? android.app.Activity)?.finish() }
                )
            }
            GameState.HANGAR -> {
                HangarScreen(
                    player = player,
                    highScore = progressionManager.highScore,
                    progressionManager = progressionManager,
                    sharedPrefs = sharedPrefs,
                    onNavigate = { 
                        previousState = GameState.HANGAR
                        gameState = it 
                    }
                )
            }
            GameState.LOADOUT -> {
                LoadoutScreen(
                    loadoutManager = loadoutManager,
                    progressionManager = progressionManager,
                    missionManager = missionManager,
                    onNavigate = { gameState = it }
                )
            }
            GameState.ARCHIVE -> {
                ArchiveScreen(
                    sharedPrefs = sharedPrefs,
                    discoveryManager = discoveryManager,
                    progressionManager = progressionManager,
                    onNavigate = { gameState = it }
                )
            }
            GameState.SETTINGS -> {
                SettingsScreen(
                    sharedPrefs = sharedPrefs,
                    onWipeData = { progressionManager.wipeData(); player.rocketType = RocketType.BALANCED; gameState = GameState.TITLE },
                    onReturn = { gameState = GameState.MAIN_MENU }
                )
            }
            GameState.ABOUT -> {
                AboutScreen(onDismiss = { gameState = GameState.MAIN_MENU })
            }
            GameState.MISSIONS -> {
                MissionScreen(
                    missionManager = missionManager,
                    player = player,
                    onDismiss = { gameState = previousState }
                )
            }
            GameState.LEADERBOARD -> {
                LeaderboardScreen(onDismiss = { gameState = GameState.MAIN_MENU })
            }
            GameState.PLAYING, GameState.GAMEOVER, GameState.TUTORIAL, GameState.PAUSED, GameState.HELP, GameState.UNLOCK -> {
                Box(Modifier.fillMaxSize()) {
                    TopRightUtilityButtons(
                        modifier = Modifier.align(Alignment.TopEnd),
                        gameState = gameState,
                        onHelp = { gameState = GameState.HELP; isThrusting = false },
                        onPause = { gameState = GameState.PAUSED; isThrusting = false }
                    )

                    // --- HUD WIDGETS ---
                    AltitudeDisplay(
                        modifier = Modifier.align(Alignment.TopCenter),
                        score = score, highScore = progressionManager.highScore,
                        zone = altitudeManager.currentZone
                    )

                    LeftGauges(
                        modifier = Modifier.align(Alignment.CenterStart),
                        fuel = player.fuel, maxFuel = player.maxFuel,
                        heat = player.heat, maxHeat = player.maxHeat, isOverheated = player.isOverheated,
                        hud = HudContext(gameTime = gameTime, interferenceTimer = player.hudInterferenceTimer, zone = altitudeManager.currentZone)
                    )

                    ComboDisplay(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .statusBarsPadding(),
                        currentCombo = comboManager.currentCombo,
                        comboTimeRemaining = comboManager.comboTimeRemaining,
                        getWindowForCombo = { comboManager.getWindowForCombo(it) },
                        zone = altitudeManager.currentZone
                    )

                    RightGauges(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        shield = player.shield, maxShield = player.maxShield,
                        integrity = player.integrity, maxIntegrity = player.maxIntegrity,
                        hud = HudContext(gameTime = gameTime, interferenceTimer = player.hudInterferenceTimer, zone = altitudeManager.currentZone)
                    )

                    // 4. CENTER BELOW ALTITUDE: Progression HUD Layer

                    // NOTIFICATION LAYER
                    NotificationLayer(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 240.dp),
                        activeNotification = notificationManager.active,
                        notificationAlpha = notificationManager.alpha,
                        screenWidth = screenWidth,
                        zone = altitudeManager.currentZone
                    )

                    // MAJOR WARNING (center-screen, reserved for bosses/rocket/zone/critical events)
                    if (majorWarningText != null) {
                        val warnAlpha = (majorWarningTimer / 2f).coerceIn(0f, 1f)
                        Text(
                            text = majorWarningText!!,
                            modifier = Modifier.align(Alignment.Center).graphicsLayer(alpha = warnAlpha),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            ),
                            color = SciFiRed,
                            textAlign = TextAlign.Center
                        )
                    }

                    FloatingTextsLayer(
                        texts = floatingTextManager.texts,
                        cameraY = cameraY
                    )

                    // ZONE DISCOVERY CARD
                    AnimatedVisibility(
                        visible = discoveryManager.activeEvent is DiscoveryEvent.Zone,
                        enter = fadeIn(tween(600)) + expandVertically(tween(500)) + scaleIn(tween(500), initialScale = 0.95f),
                        exit = fadeOut(tween(400)) + shrinkVertically(tween(400)) + scaleOut(tween(400), targetScale = 1.05f),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 180.dp)
                    ) {
                        ZoneDiscoveryCard(activeEvent = discoveryManager.activeEvent, score = score)
                    }
                }

                if (gameState == GameState.PAUSED) {
                    PauseOverlay(
                        showDevMenu = showDevMenu,
                        infiniteFuel = infiniteFuel,
                        disableHeat = disableHeat,
                        infiniteShield = player.infiniteShield,
                        invincibleHull = player.invincibleHull,
                        cheatsEnabled = DevConfig.CHEATS_ENABLED,
                        onToggleDevMenu = { showDevMenu = !showDevMenu },
                        onJumpToZone = { jumpToZone(it) },
                        onSpawnDevThreat = { spawnDevThreat(it) },
                        onSpawnDevPowerUp = { spawnDevPowerUp(it) },
                        onSpawnDevPlatform = { spawnDevPlatform(it) },
                        onToggleInfiniteFuel = { infiniteFuel = !infiniteFuel },
                        onToggleDisableHeat = { disableHeat = !disableHeat },
                        onToggleInfiniteShield = { player.infiniteShield = !player.infiniteShield },
                        onToggleInvincibleHull = { player.invincibleHull = !player.invincibleHull },
                        onUnlockAll = { unlockAll() },
                        onResume = { gameState = GameState.PLAYING },
                        onRestart = { restartGame() },
                        onMainMenu = { gameState = GameState.MAIN_MENU },
                        zone = altitudeManager.currentZone
                    )
                }

                if (gameState == GameState.TUTORIAL && activeDiscovery != null) {
                    TutorialOverlay(
                        activeDiscovery = activeDiscovery,
                        onAcknowledge = { gameState = GameState.PLAYING; activeDiscovery = null }
                    )
                }
                if (gameState == GameState.HELP) {
                    HelpOverlay(onDismiss = { gameState = GameState.PLAYING })
                }
                if (gameState == GameState.UNLOCK && unlockedRocket != null) {
                    UnlockOverlay(
                        unlockedRocket = unlockedRocket,
                        onConfirm = { gameState = GameState.PLAYING; unlockedRocket = null }
                    )
                }
                if (gameState == GameState.GAMEOVER) {
                    GameOverOverlay(
                        score = score,
                        highScore = progressionManager.highScore,
                        progressionManager = progressionManager,
                        continuesUsed = continuesUsed,
                        onContinue = { continueRun() },
                        onRestart = { restartGame() },
                        onMainMenu = { gameState = GameState.MAIN_MENU }
                    )
                }

                signalDecodedMissionName?.let { name ->
                    SignalDecodedOverlay(
                        missionName = name,
                        onDismiss = { signalDecodedMissionName = null }
                    )
                }
            }
        }
    }
}

fun MissionType.toIcon(): String = when(this) {
    MissionType.EXPLORATION -> "🚀"
    MissionType.PLATFORMING -> "🧱"
    MissionType.SURVIVAL -> "❄️"
    MissionType.DISCOVERY -> "📡"
    MissionType.BOSS -> "⚠️"
}


