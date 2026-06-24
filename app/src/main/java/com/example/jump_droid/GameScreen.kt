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
    var totalPlatformLandings by remember { mutableIntStateOf(0) }
    var totalBossesDefeated by remember { mutableIntStateOf(0) }
    var totalArtifactsCollected by remember { mutableIntStateOf(0) }
    var totalDashes by remember { mutableIntStateOf(0) }
    var momentumValue by remember { mutableFloatStateOf(0f) }
    var comboMaintainTimer by remember { mutableFloatStateOf(0f) }
    var overheatCount by remember { mutableIntStateOf(0) }
    var wasNearDeath by remember { mutableStateOf(false) }
    var consecutiveWins by remember { mutableIntStateOf(0) }
    
    var gameState by remember { mutableStateOf(GameState.TITLE) }
    var previousState by remember { mutableStateOf(GameState.MAIN_MENU) }
    var activeDiscovery by remember { mutableStateOf<DiscoveryType?>(null) }
    var unlockedRocket by remember { mutableStateOf<RocketType?>(null) }
    var codexNotification by remember { mutableStateOf<DiscoveryType?>(null) }

    val discoveryManager = remember { DiscoveryManager(sharedPrefs) }
    val progressionManager = remember { ProgressionManager(sharedPrefs) }
    val missionManager = remember { MissionManager(progressionManager) }

    val threatManager = remember { 
        ThreatManager().apply {
            onThreatDestroyed = { def ->
                if (def.type == ThreatType.BOSS || def.type == ThreatType.MINI_BOSS) {
                    totalBossesDefeated++
                }
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

    SideEffect {
        missionManager.onMissionCompleted = { mission ->
            progressionManager.recordMissionCompletion(mission.id)
        }
    }

    var missionHintRotationTimer by remember { mutableFloatStateOf(0f) }
    var globalShowObjective by remember { mutableStateOf(false) }

    var gameTime by remember { mutableLongStateOf(0L) }
    var cameraY by remember { mutableFloatStateOf(0f) }
    var isThrusting by remember { mutableStateOf(value = false) }
    var thrustTarget by remember { mutableStateOf(Offset.Zero) }
    var screenShake by remember { mutableFloatStateOf(0f) }
    var impactFlashAlpha by remember { mutableFloatStateOf(0f) }
    var globalFogAlpha by remember { mutableFloatStateOf(0f) }
    
    var effectiveThrust by remember { mutableStateOf(false) }
    var effectiveTarget by remember { mutableStateOf(Offset.Zero) }

    // --- Developer / Cheat States ---
    var infiniteFuel by remember { mutableStateOf(false) }
    var disableHeat by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }

    val bossesSpawned = remember { mutableStateSetOf<String>() }

    val notificationManager = remember { NotificationManager() }
    val survivalManager = remember { SurvivalManager() }
    val encounterDirector = remember { EncounterDirector() }
    val projectileManager = remember { ProjectileManager() }
    val inputBufferManager = remember { InputBufferManager() }
    val loadoutManager = remember { LoadoutManager(sharedPrefs) }

    // Escalation & Major Warning State
    var majorWarningText by remember { mutableStateOf<String?>(null) }
    var majorWarningTimer by remember { mutableFloatStateOf(0f) }
    var escalationSpawnId by remember { mutableStateOf<String?>(null) }
    var escalationSpawnX by remember { mutableFloatStateOf(0f) }
    var escalationSpawnY by remember { mutableFloatStateOf(0f) }
    var escalationCountdown by remember { mutableFloatStateOf(0f) }

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

    fun checkUnlock(newScore: Int) {
        progressionManager.checkUnlocks(
            score = newScore,
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
            },
            onModuleUnlock = { module ->
                notificationManager.post("NEW MODULE: ${module.name.uppercase()}")
                floatingTextManager.add(FloatingText("MODULE UNLOCKED: ${module.name}", player.x, player.y - 250f, color = module.iconColor, isCritical = true))
            }
        )
    }

    fun saveHighScore(newScore: Int) {
        progressionManager.saveHighScore(newScore)
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
        player.maxIntegrity = progressionManager.permanentMaxIntegrity
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
                        threatManager.update(dt, cameraY, screenHeight, screenWidth, player.x, player.y, powerUpManager.powerUps)
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
                            if (player.velocityY < 0f) airborneTimer += sdt
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
                                                AltitudeZone.VOID -> "HAZ_VOID_ANOMALY" to "REALITY BREACH DETECTED"
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
                                if (platform.type == PlatformType.MAGNETIC) {
                                    val dx = player.x - (platform.x + platform.width / 2f)
                                    val dy = player.y - (platform.y + PLATFORM_HEIGHT / 2f)
                                    val distSq = dx*dx + dy*dy
                                    val radius = 250f
                                    if (distSq < radius * radius) {
                                        val dist = sqrt(distSq)
                                        val force = (1f - dist / radius) * 1200f
                                        player.velocityX -= (dx / dist) * force * sdt
                                        player.velocityY -= (dy / dist) * force * sdt
                                        
                                        // Heavy Feel (Damping)
                                        val damp = 1f - 0.15f * (1f - dist / radius)
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

                                val currentThrust = BASE_THRUST_POWER * player.rocketType.thrustMult * (if (player.turboTimer > 0) 1.2f else 1.0f) * thrustMult

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
                                player.heat = max(0f, player.heat - COOLING_RATE * coolingMult * sdt)
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
                                }
                                PowerUpType.HULL_REPAIR -> {
                                    player.integrity = min(player.maxIntegrity, player.integrity + 20f)
                                    spawnBurst(pu.x, pu.y, 30, SciFiGreen, 400f)
                                    notificationManager.post("HULL REPAIRED")
                                    floatingTextManager.add(FloatingText("+20 HULL", player.x, player.y - 120f, color = SciFiGreen))
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
                        // Task 1: Differentiate survival missions by ID prefix
                        missionManager.updateProgress(MissionType.SURVIVAL, absoluteValue = runDurationTimer.toInt()) {
                            it.id.startsWith("surv_time")
                        }

                        if (!player.isOnPlatform) {
                            airborneTimer += dt
                            // Task 1: Update Flight Time missions specifically
                            missionManager.updateProgress(MissionType.SURVIVAL, absoluteValue = airborneTimer.toInt()) {
                                it.id.startsWith("surv_air")
                            }
                        }

                        if (!player.isOverheated) {
                            noOverheatTimer += dt
                            // Task 4: Update Thermal missions specifically
                            missionManager.updateProgress(MissionType.SURVIVAL, absoluteValue = noOverheatTimer.toInt()) {
                                it.id.startsWith("surv_cool")
                            }
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
                            onShake = { screenShake = max(screenShake, it) }
                        )

                        if (!isThrusting) player.fuel = min(player.maxFuel, player.fuel + FUEL_RECHARGE_RATE * dt)
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
                            val random = Random(threat.instanceId.hashCode())
                            val lifeCycleAlpha = when (threat.phase) {
                                1 -> (threat.lifetime / (threat.duration * 0.15f)).coerceIn(0f, 1f)
                                3 -> (1f - (threat.lifetime - threat.duration * 0.85f) / (threat.duration * 0.15f)).coerceIn(0f, 1f)
                                else -> 1.0f
                            }
                            
                            val tx = threat.x
                            val ty = threat.y - cameraY

                            when (threat.definition.id) {
                                "HAZ_LIGHTNING" -> {
                                    val alpha = lifeCycleAlpha
                                    val scan = threat.scanPulse
                                    // Cumulonimbus anvil cloud shape
                                    val cloudPath = Path().apply {
                                        moveTo(tx - 160f, ty - 40f)
                                        cubicTo(tx - 180f, ty - 100f, tx - 120f, ty - 140f, tx - 40f, ty - 120f)
                                        cubicTo(tx - 20f, ty - 150f, tx + 30f, ty - 160f, tx + 80f, ty - 130f)
                                        cubicTo(tx + 150f, ty - 140f, tx + 190f, ty - 90f, tx + 170f, ty - 40f)
                                        cubicTo(tx + 200f, ty - 20f, tx + 190f, ty + 20f, tx + 150f, ty + 30f)
                                        lineTo(tx - 150f, ty + 30f)
                                        cubicTo(tx - 190f, ty + 20f, tx - 200f, ty - 20f, tx - 160f, ty - 40f)
                                        close()
                                    }
                                    drawPath(cloudPath, Color.DarkGray.copy(alpha = 0.85f * alpha))
                                    drawPath(cloudPath, Color(0xFF455A64).copy(alpha = 0.3f * alpha), style = Stroke(width = 2f))

                                    // Telegraph: cloud swells and brightens from yellow to white
                                    val telegraphBrightness = if (scan > 0 && scan < 1.0f && threat.phase == 2) scan else 0f
                                    val cloudGlowColor = if (telegraphBrightness > 0.5f) Color.White else Color.Yellow
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(cloudGlowColor.copy(alpha = 0.3f * alpha + telegraphBrightness * 0.4f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 180f + telegraphBrightness * 60f
                                        ),
                                        radius = 180f + telegraphBrightness * 60f,
                                        center = Offset(tx, ty)
                                    )

                                    // Electric arc particles crackling around cloud
                                    repeat(8) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 50).toInt()
                                        val rng = Random(seed)
                                        val arcAngle = rng.nextFloat() * 2f * PI.toFloat()
                                        val arcDist = 120f + rng.nextFloat() * 60f
                                        val ax = tx + cos(arcAngle) * arcDist
                                        val ay = ty + sin(arcAngle) * arcDist
                                        drawCircle(Color.Cyan.copy(alpha = 0.5f * alpha), radius = 2f, center = Offset(ax, ay))
                                    }

                                    // Strike zone telegraph: dashed circle on ground that shrinks
                                    if (scan > 0 && scan < 1.0f && threat.phase == 2) {
                                        val strikeRadius = 80f * (1f - scan) + 20f
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.2f * alpha),
                                            radius = strikeRadius + 30f,
                                            center = Offset(tx, ty + 250f),
                                            style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                                        )
                                        drawCircle(Color.Yellow.copy(alpha = 0.3f * alpha * (1f - scan)), radius = strikeRadius, center = Offset(tx, ty + 250f))
                                    }

                                    // Strike: branched fork lightning
                                    if (scan == 1.0f && threat.phase == 2) {
                                        val strikeBase = ty + 120f
                                        repeat(3) { branch ->
                                            val boltPath = Path().apply {
                                                moveTo(tx + (branch - 1) * 30f, ty + 30f)
                                                var curY = strikeBase
                                                var curX = tx + (branch - 1) * 30f
                                                val segments = 4 + branch
                                                repeat(segments) {
                                                    val nextY = curY + 80f
                                                    val nextX = curX + (Random.nextFloat() - 0.5f) * 120f
                                                    lineTo(nextX, nextY)
                                                    // Branch fork at midpoint
                                                    if (it == segments / 2) {
                                                        moveTo(nextX, nextY)
                                                        lineTo(nextX + (Random.nextFloat() - 0.5f) * 80f, nextY + 50f)
                                                        moveTo(nextX, nextY)
                                                    }
                                                    curX = nextX
                                                    curY = nextY
                                                }
                                            }
                                            drawPath(boltPath, Color.Yellow.copy(alpha = alpha * 0.8f), style = Stroke(width = 8f - branch, cap = StrokeCap.Round))
                                            drawPath(boltPath, Color.White.copy(alpha = alpha), style = Stroke(width = 3f, cap = StrokeCap.Round))
                                        }

                                        // Impact flash
                                        drawCircle(Color.White.copy(alpha = 0.7f * alpha), radius = 120f, center = Offset(tx, ty + 280f))
                                        drawCircle(Color.Yellow.copy(alpha = 0.4f * alpha), radius = 180f, center = Offset(tx, ty + 280f))
                                    }
                                }
                                "HAZ_DEBRIS" -> {
                                    val alpha = lifeCycleAlpha
                                    // Unified sharp-jagged debris shape
                                    rotate(threat.rotation, pivot = Offset(tx, ty)) {
                                        val debrisColor = if (random.nextBoolean()) Color(0xFF757575) else Color(0xFF424242)

                                        // Jagged debris polygon (different random variants for variety)
                                        val variant = abs(threat.instanceId.hashCode() % 3)
                                        val debrisPath = Path().apply {
                                            when (variant) {
                                                0 -> {
                                                    moveTo(tx - 50f, ty - 30f); lineTo(tx + 30f, ty - 50f)
                                                    lineTo(tx + 60f, ty - 10f); lineTo(tx + 40f, ty + 40f)
                                                    lineTo(tx - 20f, ty + 50f); lineTo(tx - 60f, ty + 20f)
                                                    close()
                                                }
                                                1 -> {
                                                    moveTo(tx - 40f, ty - 50f); lineTo(tx + 50f, ty - 30f)
                                                    lineTo(tx + 30f, ty + 10f); lineTo(tx + 50f, ty + 40f)
                                                    lineTo(tx - 10f, ty + 30f); lineTo(tx - 50f, ty + 10f)
                                                    close()
                                                }
                                                else -> {
                                                    moveTo(tx - 30f, ty - 60f); lineTo(tx + 40f, ty - 40f)
                                                    lineTo(tx + 50f, ty); lineTo(tx + 20f, ty + 50f)
                                                    lineTo(tx - 40f, ty + 30f); lineTo(tx - 60f, ty - 10f)
                                                    close()
                                                }
                                            }
                                        }
                                        drawPath(debrisPath, debrisColor.copy(alpha = alpha))
                                        drawPath(debrisPath, Color.White.copy(alpha = 0.1f * alpha), style = Stroke(width = 2f))

                                        // Orange glow on leading edge (danger highlight)
                                        val glowColor = Color(0xFFFF6D00)
                                        drawPath(debrisPath, glowColor.copy(alpha = 0.15f * alpha), style = Stroke(width = 4f))
                                    }

                                    // Spark particles from rotation
                                    repeat(3) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 30).toInt()
                                        val rng = Random(seed)
                                        val sx = tx + (rng.nextFloat() - 0.5f) * 80f
                                        val sy = ty + (rng.nextFloat() - 0.5f) * 80f
                                        drawCircle(Color(0xFFFFAB00).copy(alpha = 0.5f * alpha), radius = 2f, center = Offset(sx, sy))
                                    }

                                    // Speed blur ghost copies
                                    repeat(2) { i ->
                                        val blurX = tx + (i + 1) * 15f * cos(threat.rotation * PI.toFloat() / 180f)
                                        val blurY = ty + (i + 1) * 15f * sin(threat.rotation * PI.toFloat() / 180f)
                                        drawCircle(Color.Gray.copy(alpha = 0.1f * alpha / (i + 1)), radius = 40f, center = Offset(blurX, blurY))
                                    }

                                    // Danger pulse when player is on collision course
                                    val ddx = player.x - tx
                                    val ddy = player.y - cameraY - ty
                                    if (ddx * ddx + ddy * ddy < 200f * 200f) {
                                        drawCircle(Color.Red.copy(alpha = 0.2f * alpha), radius = 60f + sin(gameTime / 100f) * 10f, center = Offset(tx, ty), style = Stroke(width = 3f))
                                    }
                                }
                                "HAZ_RADIATION" -> {
                                    val alpha = lifeCycleAlpha
                                    val pulse = threat.scanPulse
                                    val playerInZone = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty) < 300f * 300f

                                    // Background purple aura (existing)
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFF4A148C).copy(alpha = 0.15f * alpha), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 800f
                                        ),
                                        radius = 800f,
                                        center = Offset(tx, ty)
                                    )
                                    // Distortion Boundary
                                    drawCircle(
                                        color = Color(0xFF4A148C).copy(alpha = 0.2f * alpha),
                                        radius = 350f + pulse * 20f,
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 20f)
                                    )
                                    // Pulsing Core
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF6A1B9A).copy(alpha = 0.6f * alpha), 
                                                Color(0xFF2E7D32).copy(alpha = 0.3f * alpha), 
                                                Color.Transparent
                                            ),
                                            center = Offset(tx, ty),
                                            radius = 400f
                                        ),
                                        radius = 400f,
                                        center = Offset(tx, ty)
                                    )

                                    // Energy-siphon tendrils reaching toward player when inside zone
                                    if (playerInZone) {
                                        repeat(5) { i ->
                                            val angle = (i / 5f) * 2f * PI.toFloat() + (gameTime / 300f)
                                            val baseX = tx + cos(angle) * 250f
                                            val baseY = ty + sin(angle) * 250f
                                            val tipX = player.x
                                            val tipY = player.y - cameraY
                                            val tendrilPath = Path().apply {
                                                moveTo(baseX, baseY)
                                                cubicTo(baseX + (tipX - baseX) * 0.3f, baseY, tipX * 0.7f + baseX * 0.3f, tipY * 0.7f + baseY * 0.3f, tipX, tipY)
                                            }
                                            drawPath(tendrilPath, Color(0xFF00E676).copy(alpha = 0.2f * pulse * alpha), style = Stroke(width = 2f))
                                        }
                                    }

                                    // Geiger-counter random burst rects
                                    repeat(12) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 50).toInt()
                                        val rng = Random(seed)
                                        if (rng.nextFloat() < 0.3f) {
                                            val gx = tx + (rng.nextFloat() - 0.5f) * 600f
                                            val gy = ty + (rng.nextFloat() - 0.5f) * 600f
                                            drawRect(Color(0xFF00E676).copy(alpha = 0.4f * alpha), Offset(gx, gy), Size(2f + rng.nextFloat() * 4f, 2f))
                                        }
                                    }

                                    // Floating particles (Radiation)
                                    repeat(8) { i ->
                                        val pRandom = Random(threat.instanceId.hashCode() + i + (gameTime / 100).toInt())
                                        val angle = (gameTime / 1000f) + i * (2f * PI / 8f)
                                        val px = tx + kotlin.math.cos(angle).toFloat() * (150f + pRandom.nextFloat() * 150f)
                                        val py = ty + kotlin.math.sin(angle).toFloat() * (150f + pRandom.nextFloat() * 150f)
                                        drawCircle(Color.Green.copy(alpha = 0.6f * alpha), 4f, Offset(px, py))
                                    }
                                }
                                "HAZ_SOLAR_FLARE" -> {
                                    val alpha = lifeCycleAlpha
                                    val flareHeight = 600f
                                    val flameTop = ty - flareHeight / 2 + sin(gameTime / 150f) * 20f

                                    // Wavy flame-front top edge
                                    val flamePath = Path().apply {
                                        moveTo(0f, flameTop)
                                        repeat(10) { seg ->
                                            val segX = seg * (screenWidth / 10f)
                                            val segY = flameTop + sin(segX / 60f + gameTime / 200f) * 25f
                                            lineTo(segX, segY)
                                        }
                                        lineTo(screenWidth, flameTop)
                                        lineTo(screenWidth, ty + flareHeight / 2)
                                        lineTo(0f, ty + flareHeight / 2)
                                        close()
                                    }
                                    drawPath(
                                        flamePath,
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xFFFFEA00).copy(alpha = 0.9f * alpha), Color(0xFFFF3D00).copy(alpha = 0.6f * alpha), Color.Transparent),
                                            startY = ty - flareHeight / 2,
                                            endY = ty + flareHeight / 2
                                        )
                                    )

                                    // Flame tongue tendrils reaching ahead of wave
                                    repeat(6) { i ->
                                        val tongueX = (gameTime / 2f + i * (screenWidth / 6f)) % screenWidth
                                        val tongueHeight = 40f + sin(gameTime / 300f + i) * 20f
                                        val tonguePath = Path().apply {
                                            moveTo(tongueX - 15f, flameTop)
                                            quadraticTo(tongueX, flameTop - tongueHeight, tongueX + 15f, flameTop)
                                        }
                                        drawPath(tonguePath, Color(0xFFFF3D00).copy(alpha = 0.5f * alpha), style = Stroke(width = 8f, cap = StrokeCap.Round))
                                        drawPath(tonguePath, Color(0xFFFFEA00).copy(alpha = 0.3f * alpha), style = Stroke(width = 3f, cap = StrokeCap.Round))
                                    }

                                    // Ember particles
                                    repeat(15) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 40).toInt()
                                        val rng = Random(seed)
                                        val ex = rng.nextFloat() * screenWidth
                                        val ey = flameTop + 50f + rng.nextFloat() * (flareHeight - 100f)
                                        drawCircle(Color(0xFFFF6D00).copy(alpha = 0.4f * alpha), radius = 1f + rng.nextFloat() * 3f, center = Offset(ex, ey))
                                    }

                                    // Screen flash effect when wave passes player Y
                                    val playerDistFromWave = abs(player.y - cameraY - (ty - flareHeight / 2))
                                    if (playerDistFromWave < 100f) {
                                        drawRect(Color.White.copy(alpha = 0.15f * (1f - playerDistFromWave / 100f)), topLeft = Offset(0f, 0f), size = size)
                                    }
                                }
                                "HAZ_TURBULENCE" -> {
                                    val alpha = lifeCycleAlpha
                                    val windDir = if (threat.instanceId.hashCode() % 2 == 0) 1f else -1f
                                    val strength = (sin(gameTime / 500f) * 0.3f + 0.7f)
                                    val streakCount = (10 + (strength * 30).toInt()).coerceAtMost(40)

                                    // Large wind direction arrow at center
                                    val arrowSize = 80f
                                    val arrowX = tx
                                    val arrowY = ty
                                    drawLine(Color.White.copy(alpha = 0.15f * alpha), Offset(arrowX - arrowSize * windDir, arrowY), Offset(arrowX + arrowSize * windDir, arrowY), strokeWidth = 10f, cap = StrokeCap.Round)
                                    // Arrowhead
                                    val arrowHeadPath = Path().apply {
                                        moveTo(arrowX + arrowSize * windDir, arrowY)
                                        lineTo(arrowX + (arrowSize - 30f) * windDir, arrowY - 20f)
                                        lineTo(arrowX + (arrowSize - 30f) * windDir, arrowY + 20f)
                                        close()
                                    }
                                    drawPath(arrowHeadPath, Color.White.copy(alpha = 0.15f * alpha))

                                    // Vortex swirl at center
                                    repeat(3) { spiral ->
                                        val spiralPath = Path().apply {
                                            var sx = tx
                                            var sy = ty
                                            repeat(20) { seg ->
                                                val t = seg / 20f
                                                val radius = 20f + t * 180f
                                                val angle = t * 4f * PI.toFloat() * windDir + spiral * 2.1f
                                                sx = tx + cos(angle) * radius
                                                sy = ty + sin(angle) * radius
                                                lineTo(sx, sy)
                                            }
                                        }
                                        drawPath(spiralPath, Color.White.copy(alpha = 0.08f * alpha), style = Stroke(width = 2f))
                                    }

                                    // Arrow-tipped streak clusters
                                    repeat(streakCount) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 80).toInt()
                                        val rng = Random(seed)
                                        val rx = tx + (rng.nextFloat() - 0.5f) * 800f
                                        val ry = ty + (rng.nextFloat() - 0.5f) * 800f
                                        val progress = (threat.scanPulse + rng.nextFloat()) % 1.0f
                                        val streakLen = 80f + strength * 80f
                                        val sx = rx + (progress * 300f * windDir)
                                        val streakAlpha = 0.3f * (1f - progress) * alpha * strength

                                        // Streak line
                                        drawLine(
                                            color = Color.White.copy(alpha = streakAlpha),
                                            start = Offset(sx, ry),
                                            end = Offset(sx + streakLen * windDir, ry),
                                            strokeWidth = 4f + strength * 4f,
                                            cap = StrokeCap.Round
                                        )
                                        // Arrow tip on streak
                                        if (progress > 0.5f && rng.nextFloat() < 0.3f) {
                                            val tipPath = Path().apply {
                                                moveTo(sx + streakLen * windDir, ry)
                                                lineTo(sx + (streakLen - 15f) * windDir, ry - 6f)
                                                lineTo(sx + (streakLen - 15f) * windDir, ry + 6f)
                                                close()
                                            }
                                            drawPath(tipPath, Color.White.copy(alpha = streakAlpha * 0.6f))
                                        }
                                    }
                                }
                                "HAZ_GRAVITY" -> {
                                    val alpha = lifeCycleAlpha
                                    val pulse = threat.scanPulse

                                    // Lensing Effect (existing)
                                    repeat(4) { i ->
                                        val radius = (100f + i * 150f + pulse * 100f)
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.15f * (1f - pulse) * alpha),
                                            radius = radius,
                                            center = Offset(tx, ty),
                                            style = Stroke(width = 2f + i)
                                        )
                                    }

                                    // Background star stretch (tidal distortion)
                                    repeat(20) { i ->
                                        val seed = threat.instanceId.hashCode() + i
                                        val rng = Random(seed)
                                        val angle = rng.nextFloat() * 2f * PI.toFloat()
                                        val dist = 200f + rng.nextFloat() * 400f
                                        val sx = tx + cos(angle) * dist
                                        val sy = ty + sin(angle) * dist + 150f * pulse / (dist / 100f)
                                        drawCircle(Color.White.copy(alpha = 0.2f * alpha * (1f - dist / 600f)), radius = 1f, center = Offset(sx, sy))
                                    }

                                    // 8 downward-pointing gravity arrows from core
                                    repeat(8) { i ->
                                        val angle = (i / 8f) * 2f * PI.toFloat()
                                        val arrowDist = 140f
                                        val ax = tx + cos(angle) * arrowDist
                                        val ay = ty + sin(angle) * arrowDist
                                        val arrowLen = 40f + pulse * 30f
                                        // Arrow line pointing downward from core radial position
                                        val gx = ax
                                        val gy = ay + arrowLen
                                        drawLine(Color.Cyan.copy(alpha = 0.2f * alpha), Offset(ax, ay), Offset(gx, gy + arrowLen), strokeWidth = 3f)
                                        // Arrowhead
                                        val head = Path().apply {
                                            moveTo(gx, gy + arrowLen)
                                            lineTo(gx - 8f, gy + arrowLen - 12f)
                                            lineTo(gx + 8f, gy + arrowLen - 12f)
                                            close()
                                        }
                                        drawPath(head, Color.Cyan.copy(alpha = 0.2f * alpha))
                                    }

                                    // Black Hole Core (existing)
                                    drawCircle(color = Color.Black.copy(alpha = 0.8f * alpha), radius = 60f, center = Offset(tx, ty))
                                    // Space Warping inner ring (existing)
                                    drawCircle(color = Color.Cyan.copy(alpha = 0.4f * alpha), radius = 70f + (kotlin.math.sin(gameTime / 100f) * 10f), center = Offset(tx, ty), style = Stroke(width = 4f))

                                    // Particles being sucked downward
                                    repeat(6) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 5 + (gameTime / 60).toInt()
                                        val rng = Random(seed)
                                        val sx = tx + (rng.nextFloat() - 0.5f) * 300f
                                        val sy = ty - 200f + (rng.nextFloat() * 400f)
                                        drawCircle(Color.Cyan.copy(alpha = 0.3f * alpha), radius = 2f, center = Offset(sx, sy))
                                        drawLine(Color.Cyan.copy(alpha = 0.15f * alpha), Offset(sx, sy), Offset(sx, sy + 30f), strokeWidth = 1f)
                                    }
                                }
                                "HAZ_EMP" -> {
                                    val alpha = lifeCycleAlpha
                                    val scanP = threat.scanPulse
                                    val ringRadius = scanP * 600f
                                    val ringAlpha = (1f - (scanP / 3.0f)).coerceIn(0f, 1f) * alpha

                                    // Main Shockwave drawn as shrinking arcs (remaining time indicator)
                                    val arcFraction = (1f - scanP / 3f).coerceIn(0f, 1f)
                                    drawArc(
                                        color = Color.Cyan.copy(alpha = ringAlpha),
                                        startAngle = 0f,
                                        sweepAngle = 360f * arcFraction,
                                        useCenter = false,
                                        topLeft = Offset(tx - ringRadius, ty - ringRadius),
                                        size = Size(ringRadius * 2, ringRadius * 2),
                                        style = Stroke(width = 15f)
                                    )
                                    // Secondary Echoes
                                    drawCircle(color = Color.White.copy(alpha = ringAlpha * 0.5f), radius = ringRadius * 0.9f, center = Offset(tx, ty), style = Stroke(width = 5f))

                                    // Electrical arcs jumping across ring circumference
                                    repeat(4) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 30).toInt()
                                        val rng = Random(seed)
                                        val arcAngle = rng.nextFloat() * 2f * PI.toFloat()
                                        val arcLen = 40f + rng.nextFloat() * 60f
                                        val ax = tx + cos(arcAngle) * ringRadius
                                        val ay = ty + sin(arcAngle) * ringRadius
                                        val bx = tx + cos(arcAngle + 0.2f) * (ringRadius + arcLen)
                                        val by = ty + sin(arcAngle + 0.2f) * (ringRadius + arcLen)
                                        val arcPath = Path().apply {
                                            moveTo(ax, ay)
                                            quadraticTo((ax + bx) / 2 + rng.nextFloat() * 30f, (ay + by) / 2 - 40f, bx, by)
                                        }
                                        drawPath(arcPath, Color.White.copy(alpha = ringAlpha * 0.6f), style = Stroke(width = 2f))
                                    }

                                    // Shield-broken X icon at ring edge
                                    val playerDist = sqrt((player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty))
                                    if (abs(playerDist - ringRadius) < 50f) {
                                        val xSize = 15f
                                        drawLine(Color.Red.copy(alpha = 0.6f), Offset(tx - xSize, ty - xSize), Offset(tx + xSize, ty + xSize), strokeWidth = 4f)
                                        drawLine(Color.Red.copy(alpha = 0.6f), Offset(tx + xSize, ty - xSize), Offset(tx - xSize, ty + xSize), strokeWidth = 4f)
                                    }

                                    // Spark trail behind ring edge
                                    repeat(8) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 40).toInt()
                                        val rng = Random(seed)
                                        val sa = rng.nextFloat() * 2f * PI.toFloat()
                                        val sr = ringRadius - 20f + rng.nextFloat() * 40f
                                        drawCircle(Color.Cyan.copy(alpha = 0.3f * ringAlpha), radius = 2f, center = Offset(tx + cos(sa) * sr, ty + sin(sa) * sr))
                                    }
                                }
                                "HAZ_GUST" -> {
                                    // Sudden directional gust
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val windDir = if ((threat.instanceId.hashCode() % 2) == 0) 1f else -1f
                                    val gustAlpha = (0.5f - abs(0.5f - threat.scanPulse)) * 2f * lifeCycleAlpha

                                    // Main directional arrow
                                    val arrowLen = 200f
                                    val arrowStartX = tx - arrowLen * 0.5f * windDir
                                    val arrowEndX = tx + arrowLen * 0.5f * windDir
                                    drawLine(Color.White.copy(alpha = gustAlpha * 0.6f), Offset(arrowStartX, ty), Offset(arrowEndX, ty), strokeWidth = 6f, cap = StrokeCap.Round)
                                    // Arrowhead
                                    val headPath = Path().apply {
                                        moveTo(arrowEndX, ty)
                                        lineTo(arrowEndX - 30f * windDir, ty - 15f)
                                        lineTo(arrowEndX - 30f * windDir, ty + 15f)
                                        close()
                                    }
                                    drawPath(headPath, Color.White.copy(alpha = gustAlpha * 0.6f))

                                    // Leaf/particle trail swept along gust path
                                    repeat(6) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 80).toInt()
                                        val rng = Random(seed)
                                        val px = tx + (rng.nextFloat() - 0.5f) * 150f * windDir
                                        val py = ty + (rng.nextFloat() - 0.5f) * 80f
                                        drawCircle(Color(0xFF81C784).copy(alpha = gustAlpha * 0.5f), radius = 2f + rng.nextFloat() * 3f, center = Offset(px, py))
                                    }

                                    // Screen ripple when gust is at peak
                                    if (threat.scanPulse > 0.4f && threat.scanPulse < 0.6f) {
                                        drawCircle(Color.White.copy(alpha = 0.1f), radius = 120f, center = Offset(tx, ty), style = Stroke(width = 2f))
                                    }
                                }
                                "HAZ_CROSSWIND" -> {
                                    // Horizontal wind bands at fixed altitudes
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val bandCount = 4
                                    val bandSpacing = 120f
                                    val windDir = if ((threat.instanceId.hashCode() % 2) == 0) 1f else -1f
                                    val alpha = lifeCycleAlpha

                                    repeat(bandCount) { i ->
                                        val bandY = ty - (bandCount / 2) * bandSpacing + i * bandSpacing + sin(gameTime / 800f + i) * 20f
                                        val bandStrength = 1f - (abs(i - bandCount / 2f) / (bandCount / 2f))
                                        val strokeW = 2f + bandStrength * 6f

                                        // Sine-wave wind band
                                        val bandPath = Path().apply {
                                            moveTo(tx - 300f, bandY)
                                            repeat(12) { seg ->
                                                val segX = tx - 300f + seg * 50f
                                                val segY = bandY + sin(segX / 80f + gameTime / 600f) * 8f * windDir
                                                lineTo(segX, bandY + sin(segX / 80f + gameTime / 600f) * 8f * windDir)
                                            }
                                        }
                                        drawPath(bandPath, Color.White.copy(alpha = 0.25f * bandStrength * alpha), style = Stroke(width = strokeW, cap = StrokeCap.Round))

                                        // Small direction arrows on each band
                                        repeat(3) { a ->
                                            val arrowX = tx - 200f + a * 200f
                                            drawLine(Color.White.copy(alpha = 0.3f * bandStrength * alpha), Offset(arrowX, bandY), Offset(arrowX + 25f * windDir, bandY), strokeWidth = 3f)
                                            drawLine(Color.White.copy(alpha = 0.3f * bandStrength * alpha), Offset(arrowX + 25f * windDir, bandY), Offset(arrowX + 15f * windDir, bandY - 6f), strokeWidth = 2f)
                                            drawLine(Color.White.copy(alpha = 0.3f * bandStrength * alpha), Offset(arrowX + 25f * windDir, bandY), Offset(arrowX + 15f * windDir, bandY + 6f), strokeWidth = 2f)
                                        }
                                    }
                                }
                                "HAZ_THERMAL" -> {
                                    // Rising heat column
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val columnHeight = 500f
                                    val columnWidth = 80f
                                    val alpha = lifeCycleAlpha
                                    val shimmer = sin(gameTime / 200f) * 10f

                                    // Warm glow at base
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFFFF6D00).copy(alpha = 0.3f * alpha), Color.Transparent),
                                            center = Offset(tx, ty + columnHeight / 2),
                                            radius = columnWidth * 2f
                                        ),
                                        radius = columnWidth * 2f,
                                        center = Offset(tx, ty + columnHeight / 2)
                                    )

                                    // Vertical heat column gradient
                                    drawRect(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xFFFF6D00).copy(alpha = 0.2f * alpha), Color(0xFFFFAB00).copy(alpha = 0.3f * alpha), Color.Transparent),
                                            startY = ty - columnHeight / 2,
                                            endY = ty + columnHeight / 2
                                        ),
                                        topLeft = Offset(tx - columnWidth / 2 + shimmer, ty - columnHeight / 2),
                                        size = Size(columnWidth, columnHeight)
                                    )

                                    // Upward particle stream
                                    repeat(8) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 60).toInt()
                                        val rng = Random(seed)
                                        val px = tx + (rng.nextFloat() - 0.5f) * columnWidth * 0.8f + shimmer
                                        val py = ty + columnHeight / 2 - ((gameTime / 100f + i * 50f) % columnHeight)
                                        val particleAlpha = 1f - (ty + columnHeight / 2 - py) / columnHeight
                                        drawCircle(Color(0xFFFFAB00).copy(alpha = 0.5f * particleAlpha * alpha), radius = 2f + rng.nextFloat() * 3f, center = Offset(px, py))
                                    }

                                    // Mirage distortion effect (offset copy of background elements not possible, so use shimmer lines)
                                    repeat(3) { i ->
                                        val lineY = ty - columnHeight / 3 + i * (columnHeight / 3) + sin(gameTime / 150f + i * 2f) * 15f
                                        val lineX = tx - columnWidth / 2 + sin(gameTime / 100f + i) * 20f
                                        drawLine(Color.White.copy(alpha = 0.1f * alpha), Offset(lineX, lineY), Offset(lineX + columnWidth, lineY), strokeWidth = 1f)
                                    }
                                }
                                "ENT_SCOUT_DRONE" -> {
                                    // Surveyor Probe — 4-state color palette
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val isTracking = threat.isTracking
                                    val isFleeing = threat.fleeTimer > 0f
                                    val transmitting = threat.transmissionProgress > 0f && threat.transmissionProgress < 1f

                                    // Determine state color
                                    val stateColor = when {
                                        isFleeing -> Color(0xFFFF6D00) // Orange - fleeing
                                        transmitting -> Color(0xFFE91E63) // Pink - transmitting
                                        isTracking -> Color(0xFFD32F2F) // Red - tracking
                                        threat.firstDetectionShown -> Color(0xFFFDD835) // Yellow - detected
                                        else -> Color(0xFF1976D2) // Blue - patrol
                                    }
                                    val stateName = when {
                                        isFleeing -> "FLEE"
                                        transmitting -> "TRANSMIT"
                                        isTracking -> "TRACK"
                                        threat.firstDetectionShown -> "DETECT"
                                        else -> "PATROL"
                                    }

                                    // Engine glow follows state color
                                    val flicker = Random(gameTime / 50).nextFloat() * 10f
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(stateColor.copy(alpha = 0.4f), Color.Transparent),
                                            center = Offset(tx, ty + 20f),
                                            radius = 30f + flicker
                                        ),
                                        radius = 30f + flicker,
                                        center = Offset(tx, ty + 20f)
                                    )

                                    // Detailed probe body (trapezoid + details)
                                    val bodyPath = Path().apply {
                                        moveTo(tx - 20f, ty - 15f)
                                        lineTo(tx + 20f, ty - 15f)
                                        lineTo(tx + 15f, ty + 15f)
                                        lineTo(tx - 15f, ty + 15f)
                                        close()
                                    }
                                    drawPath(bodyPath, stateColor.copy(alpha = 0.8f))
                                    drawPath(bodyPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 1f))

                                    // Antenna — extend during transmission
                                    val antLen = if (transmitting) 25f else 15f
                                    drawLine(Color.LightGray, Offset(tx - 12f, ty - 15f), Offset(tx - 18f, ty - 15f - antLen), strokeWidth = 2f)
                                    drawLine(Color.LightGray, Offset(tx + 12f, ty - 15f), Offset(tx + 18f, ty - 15f - antLen), strokeWidth = 2f)
                                    drawCircle(stateColor, radius = 3f, center = Offset(tx - 18f, ty - 15f - antLen))
                                    drawCircle(stateColor, radius = 3f, center = Offset(tx + 18f, ty - 15f - antLen))

                                    // Tracking beam
                                    if (isTracking) {
                                        val beamAngle = threat.targetAngle
                                        rotate(beamAngle, pivot = Offset(tx, ty)) {
                                            drawPath(
                                                path = Path().apply {
                                                    moveTo(tx + 10f, ty)
                                                    lineTo(tx + 250f, ty - 50f)
                                                    lineTo(tx + 250f, ty + 50f)
                                                    close()
                                                },
                                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                    colors = listOf(stateColor.copy(alpha = 0.3f * (1f - threat.scanPulse)), Color.Transparent),
                                                    startX = tx,
                                                    endX = tx + 250f
                                                )
                                            )
                                        }
                                    }

                                    // Eye pulsing
                                    val eyeRate = when {
                                        isFleeing -> 30f
                                        transmitting -> 50f
                                        isTracking -> 80f
                                        else -> 150f
                                    }
                                    val eyePulse = (sin(gameTime / eyeRate) * 0.5f + 0.5f)
                                    val eyeColor = if (isTracking || transmitting) Color.Red else Color.Cyan
                                    val eyeRadius = if (isTracking || transmitting) 12f else 8f
                                    drawCircle(eyeColor.copy(alpha = 0.6f + eyePulse * 0.4f), radius = eyeRadius, center = Offset(tx, ty))

                                    // Signal wave bars during transmission
                                    if (transmitting) {
                                        repeat(3) { i ->
                                            val barX = tx - 30f + i * 30f
                                            val barH = 8f + sin(gameTime / 100f + i * 2f) * 6f
                                            drawRect(stateColor.copy(alpha = 0.5f), Offset(barX, ty - 30f), Size(8f, barH))
                                        }
                                    }

                                    // Transmission visual rings (existing)
                                    if (isTracking && threat.transmissionProgress > 0f) {
                                        val prog = if (threat.transmissionProgress < 1f) threat.transmissionProgress else threat.scanPulse
                                        val baseRadius = 25f
                                        repeat(3) { i ->
                                            val offset = i * 0.33f
                                            val ringPhase = (prog + offset) % 1f
                                            val radius = ringPhase * 90f + baseRadius
                                            val alpha = (1f - ringPhase) * 0.5f
                                            drawCircle(
                                                color = Color(0xFFE91E63).copy(alpha = alpha),
                                                radius = radius,
                                                center = Offset(tx, ty),
                                                style = Stroke(width = 2f)
                                            )
                                        }
                                    }

                                    // Speed trail when fleeing
                                    if (isFleeing) {
                                        repeat(4) { i ->
                                            val trailAlpha = 0.3f * (1f - i / 4f)
                                            drawRect(stateColor.copy(alpha = trailAlpha), Offset(tx - 15f + (i + 1) * 8f, ty - 12f), Size(30f, 24f))
                                        }
                                    }
                                }
                                "ENT_SWARM_BOTS" -> {
                                    // Aerosol Swarm — full swarm feel
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 300f) * 0.2f + 0.8f)
                                    val playerInside = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty) < 150f * 150f

                                    // Swarm boundary ring
                                    val boundaryRadius = 100f + pulse * 30f
                                    drawCircle(Color.White.copy(alpha = 0.05f), radius = boundaryRadius, center = Offset(tx, ty), style = Stroke(width = 1f))

                                    // 35 particles with 3 queen bots
                                    val particleCount = 35
                                    repeat(particleCount) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 80).toInt()
                                        val rng = Random(seed)
                                        val baseR = 20f + rng.nextFloat() * 60f
                                        val baseAngle = (i / particleCount.toFloat()) * 2f * PI.toFloat() + (gameTime / 1000f)
                                        val ox = cos(baseAngle + rng.nextFloat() * 0.3f) * baseR * pulse
                                        val oy = sin(baseAngle * 1.3f + rng.nextFloat() * 0.3f) * baseR * pulse * 0.8f
                                        val px = tx + ox
                                        val py = ty + oy

                                        // Queen bots (every 12th particle)
                                        val isQueen = i % 12 == 0 && i > 0
                                        val radius = if (isQueen) 6f + rng.nextFloat() * 2f else 1f + rng.nextFloat() * 3f
                                        val color = if (isQueen) Color.Cyan else Color.White.copy(alpha = 0.3f)

                                        drawCircle(color = color, radius = radius, center = Offset(px, py))

                                        // Queen has red core
                                        if (isQueen) {
                                            drawCircle(Color.Red.copy(alpha = 0.5f), radius = 3f, center = Offset(px, py))
                                        }

                                        // Ghost trail behind each bot
                                        repeat(2) { t ->
                                            val trailX = px - ox * 0.3f * (t + 1)
                                            val trailY = py - oy * 0.3f * (t + 1)
                                            drawCircle(Color.White.copy(alpha = 0.08f / (t + 1)), radius = radius * 0.7f, center = Offset(trailX, trailY))
                                        }

                                        // Cyan spark (15% chance)
                                        if (rng.nextFloat() < 0.15f) {
                                            drawCircle(Color.Cyan.copy(alpha = 0.6f), radius = 1.5f, center = Offset(px, py))
                                        }
                                    }

                                    // Collision flash when player is inside swarm
                                    if (playerInside) {
                                        repeat(5) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 30).toInt()
                                            val rng = Random(seed)
                                            val fx = tx + (rng.nextFloat() - 0.5f) * 200f
                                            val fy = ty + (rng.nextFloat() - 0.5f) * 200f
                                            drawCircle(Color.White.copy(alpha = 0.4f), radius = 1f + rng.nextFloat() * 2f, center = Offset(fx, fy))
                                        }
                                    }
                                }
                                "ENT_CLOUD_SKIMMER" -> {
                                    // Sky Ray — friendly beneficial entity
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val distSq = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty)
                                    val isNear = distSq < 250f * 250f

                                    val dir = if (threat.vx > 0) 1f else -1f
                                    val wingSpan = 80f
                                    val flap = sin(gameTime / 600f) * 20f
                                    val friendlyColor = Color(0xFF00E676) // Teal/green
                                    val glowColor = Color(0xFF1DE9B6)

                                    // Friendly glow aura
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(glowColor.copy(alpha = 0.15f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 80f
                                        ),
                                        radius = 80f,
                                        center = Offset(tx, ty)
                                    )

                                    // Glow trail showing slipstream path
                                    if (isNear) {
                                        val trailPath = Path().apply {
                                            moveTo(tx - 80f * dir, ty)
                                            cubicTo(tx - 40f * dir, ty - 30f, tx + 40f * dir, ty - 50f, tx + 80f * dir, ty - 20f)
                                        }
                                        drawPath(trailPath, glowColor.copy(alpha = 0.2f), style = Stroke(width = 4f))

                                        repeat(3) { i ->
                                            val slipX = tx + (Random.nextFloat() - 0.5f) * 40f
                                            val slipY = ty + i * 20f
                                            drawLine(glowColor.copy(alpha = 0.4f), Offset(slipX, slipY), Offset(slipX, slipY - 80f), strokeWidth = 3f)
                                        }

                                        // Contact sparkle particles
                                        repeat(4) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 40).toInt()
                                            val rng = Random(seed)
                                            val sx = tx + (rng.nextFloat() - 0.5f) * 60f
                                            val sy = ty + (rng.nextFloat() - 0.5f) * 60f
                                            drawCircle(Color(0xFF69F0AE).copy(alpha = 0.5f), radius = 1f + rng.nextFloat() * 2f, center = Offset(sx, sy))
                                        }
                                    }

                                    // Wing body with upward arrow markings
                                    val rayPath = Path().apply {
                                        moveTo(tx - 40f * dir, ty)
                                        quadraticTo(tx, ty - 10f, tx + 40f * dir, ty)
                                        moveTo(tx, ty - 5f)
                                        lineTo(tx - 20f * dir, ty + flap)
                                        lineTo(tx - wingSpan * dir, ty + flap * 1.5f)
                                        lineTo(tx - 20f * dir, ty + 15f)
                                        close()
                                    }
                                    drawPath(rayPath, friendlyColor.copy(alpha = 0.4f))
                                    drawPath(rayPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 2f))

                                    // Upward arrow on each wing
                                    val arrowY = ty + flap * 0.5f
                                    val arrowX1 = tx - 50f * dir
                                    val arrowX2 = tx + 10f * dir
                                    drawLine(glowColor.copy(alpha = 0.3f), Offset(arrowX1, arrowY), Offset(arrowX2, arrowY), strokeWidth = 2f)
                                    drawLine(glowColor.copy(alpha = 0.3f), Offset(arrowX2, arrowY), Offset(arrowX2 - 10f * dir, arrowY - 8f), strokeWidth = 2f)
                                    drawLine(glowColor.copy(alpha = 0.3f), Offset(arrowX2, arrowY), Offset(arrowX2 - 10f * dir, arrowY + 8f), strokeWidth = 2f)

                                    // Trailing energy dots
                                    repeat(3) { i ->
                                        drawCircle(friendlyColor.copy(alpha = 0.2f), radius = 5f - i, center = Offset(tx - (60f + i * 20f) * dir, ty + sin(gameTime / 400f + i) * 10f))
                                    }
                                }
                                "HAZ_STORM" -> {
                                    // Render Static Discharge
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val r = 60f
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFF7E57C2).copy(alpha = 0.3f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = r
                                        ),
                                        radius = r,
                                        center = Offset(tx, ty)
                                    )
                                    if ((gameTime / 100) % 10 < 3L) {
                                        val randBolt = Random(gameTime / 100)
                                        repeat(2) {
                                            val bx = tx + (randBolt.nextFloat() - 0.5f) * r * 2
                                            val by = ty + (randBolt.nextFloat() - 0.5f) * r * 2
                                            drawLine(Color.White, start = Offset(tx, ty), end = Offset(bx, by), strokeWidth = 2f)
                                        }
                                    }
                                }
                                "ENT_ORBITAL_SENTRY" -> {
                                    // Defense Node — lock-on & fuel drain visible
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val rot = threat.rotation
                                    val isTracking = threat.isTracking

                                    // Octagonal turret chassis
                                    val octPath = Path().apply {
                                        moveTo(tx - 25f, ty - 10f)
                                        lineTo(tx - 10f, ty - 25f)
                                        lineTo(tx + 10f, ty - 25f)
                                        lineTo(tx + 25f, ty - 10f)
                                        lineTo(tx + 25f, ty + 10f)
                                        lineTo(tx + 10f, ty + 25f)
                                        lineTo(tx - 10f, ty + 25f)
                                        lineTo(tx - 25f, ty + 10f)
                                        close()
                                    }
                                    rotate(rot, pivot = Offset(tx, ty)) {
                                        drawPath(octPath, Color(0xFF37474F))
                                        drawPath(octPath, Color.Gray.copy(alpha = 0.5f), style = Stroke(width = 2f))
                                        drawCircle(Color.Cyan.copy(alpha = 0.3f), radius = 15f, center = Offset(tx, ty))

                                        // Gun barrel
                                        drawRect(Color(0xFF546E7A), Offset(tx - 4f, ty - 35f), Size(8f, 12f))
                                    }

                                    // Radar scan ring
                                    val pulseRadius = 150f * threat.scanPulse
                                    drawCircle(
                                        color = Color.Cyan.copy(alpha = 0.2f * (1f - threat.scanPulse)),
                                        radius = pulseRadius,
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 2f)
                                    )

                                    // Laser sight line when tracking
                                    if (isTracking) {
                                        val px = player.x
                                        val py = player.y - cameraY
                                        drawLine(Color.Red.copy(alpha = 0.3f), Offset(tx, ty), Offset(px, py), strokeWidth = 1f)

                                        // 4-bracket lock-on reticle at player position
                                        val bracketSize = 15f
                                        val gap = 5f
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py - bracketSize - gap), Offset(px - gap, py - bracketSize - gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py - bracketSize - gap), Offset(px - bracketSize - gap, py - gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py - bracketSize - gap), Offset(px + gap, py - bracketSize - gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py - bracketSize - gap), Offset(px + bracketSize + gap, py - gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py + bracketSize + gap), Offset(px - gap, py + bracketSize + gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px - bracketSize - gap, py + bracketSize + gap), Offset(px - bracketSize - gap, py + gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py + bracketSize + gap), Offset(px + gap, py + bracketSize + gap), strokeWidth = 2f)
                                        drawLine(Color.Red.copy(alpha = 0.5f), Offset(px + bracketSize + gap, py + bracketSize + gap), Offset(px + bracketSize + gap, py + gap), strokeWidth = 2f)

                                        // Fuel-drain stream (orange particles flowing from player to sentry)
                                        repeat(3) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 5 + (gameTime / 30).toInt()
                                            val rng = Random(seed)
                                            val t = rng.nextFloat()
                                            val sx = px + (tx - px) * t
                                            val sy = py + (ty - py) * t
                                            drawCircle(Color(0xFFFF6D00).copy(alpha = 0.5f * (1f - t)), radius = 2f, center = Offset(sx, sy))
                                        }
                                    }

                                    // Combo-freeze icon
                                    if (isTracking) {
                                        val iconX = tx + 40f
                                        val iconY = ty - 30f
                                        // Snowflake symbol
                                        rotate(gameTime / 10f, pivot = Offset(iconX, iconY)) {
                                            repeat(3) { i ->
                                                rotate(i * 60f, pivot = Offset(iconX, iconY)) {
                                                    drawLine(Color.Cyan.copy(alpha = 0.5f), Offset(iconX, iconY - 8f), Offset(iconX, iconY + 8f), strokeWidth = 2f)
                                                }
                                            }
                                        }
                                    }
                                }
                                "ENT_CORRUPTED_HULL" -> {
                                    // Derelict Echo — lootable salvage
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val rot = threat.rotation

                                    // Gold metallic shine
                                    val goldColor = Color(0xFFFF8F00)
                                    val goldShine = (sin(gameTime / 200f) * 0.3f + 0.7f)

                                    // Friendly beacon glow (alternating green/yellow)
                                    val beaconColor = if ((gameTime / 500) % 2 == 0L) Color(0xFF00E676) else Color(0xFFFFEA00)
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(beaconColor.copy(alpha = 0.2f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 80f
                                        ),
                                        radius = 80f,
                                        center = Offset(tx, ty)
                                    )

                                    rotate(rot, pivot = Offset(tx, ty)) {
                                        // Crate body
                                        drawRect(goldColor.copy(alpha = 0.6f), topLeft = Offset(tx - 15f, ty - 25f), size = Size(30f, 40f))
                                        drawRect(Color.White.copy(alpha = 0.2f), topLeft = Offset(tx - 15f, ty - 25f), size = Size(30f, 40f), style = Stroke(width = 1f))

                                        // Open lid (slightly ajar)
                                        drawLine(Color(0xFFFFAB00).copy(alpha = 0.8f), Offset(tx - 15f, ty - 25f), Offset(tx - 10f, ty - 35f), strokeWidth = 3f)
                                        drawLine(Color(0xFFFFAB00).copy(alpha = 0.8f), Offset(tx + 15f, ty - 25f), Offset(tx + 10f, ty - 35f), strokeWidth = 3f)

                                        // Power-up icon peeking out (small colored circle)
                                        drawCircle(Color.Cyan.copy(alpha = 0.6f * goldShine), radius = 6f, center = Offset(tx, ty - 18f))
                                    }

                                    // Gold sparkle particles
                                    repeat(3) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 60).toInt()
                                        val rng = Random(seed)
                                        val sx = tx + (rng.nextFloat() - 0.5f) * 60f
                                        val sy = ty + (rng.nextFloat() - 0.5f) * 60f
                                        drawCircle(Color(0xFFFFD54F).copy(alpha = 0.5f), radius = 1f + rng.nextFloat() * 2f, center = Offset(sx, sy))
                                    }

                                    // Attract flow lines
                                    repeat(3) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 80).toInt()
                                        val rng = Random(seed)
                                        val startX = tx + (rng.nextFloat() - 0.5f) * 200f
                                        val startY = ty + (rng.nextFloat() - 0.5f) * 200f
                                        drawLine(Color(0xFFFFD54F).copy(alpha = 0.15f), Offset(startX, startY), Offset(tx, ty), strokeWidth = 1f)
                                    }
                                }
                                "ENT_STALKER" -> {
                                    // Void Tracker — heat-seeking hunter drone (polished)
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val alertLevel = threat.alertLevel.coerceIn(0f, 1f)
                                    val bodyRadius = 15f + alertLevel * 10f
                                    val bodyColor = if (alertLevel > 0.5f) Color(0xFFFF6D00) else Color(0xFF880E4F)
                                    val heatAlpha = 0.3f + alertLevel * 0.5f
                                    val isTracking = threat.isTracking
                                    val px = player.x
                                    val py = player.y - cameraY

                                    // Engine glow
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFFFF6D00).copy(alpha = heatAlpha * 0.5f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = bodyRadius * 3f
                                        ),
                                        radius = bodyRadius * 3f,
                                        center = Offset(tx, ty)
                                    )

                                    // Targeting laser when tracking the player at high alert
                                    if (isTracking && alertLevel > 0.3f) {
                                        val laserAlpha = (sin(gameTime / 50f) * 0.3f + 0.7f) * alertLevel
                                        val laserEndX = px + (px - tx) * 3f
                                        val laserEndY = py + (py - ty) * 3f
                                        drawLine(Color(0xFFFF1744).copy(alpha = 0.15f * laserAlpha), Offset(tx, ty), Offset(laserEndX, laserEndY), strokeWidth = 1f)
                                        drawLine(Color.White.copy(alpha = 0.05f * laserAlpha), Offset(tx, ty), Offset(laserEndX, laserEndY), strokeWidth = 3f)
                                    }

                                    // MAX ALERT warning glow at full alert
                                    if (alertLevel > 0.9f) {
                                        val maxPulse = (sin(gameTime / 30f) * 0.5f + 0.5f)
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.08f * maxPulse), radius = bodyRadius * 5f, center = Offset(tx, ty), style = Stroke(width = 4f))
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.04f * maxPulse), radius = bodyRadius * 6f, center = Offset(tx, ty), style = Stroke(width = 2f))
                                    }

                                    // Triangular body with horizontal segmentation lines
                                    val bodyPath = Path().apply {
                                        moveTo(tx, ty - bodyRadius * 1.5f)
                                        lineTo(tx - bodyRadius * 1.2f, ty + bodyRadius * 0.8f)
                                        lineTo(tx + bodyRadius * 1.2f, ty + bodyRadius * 0.8f)
                                        close()
                                    }
                                    drawPath(bodyPath, bodyColor)
                                    drawPath(bodyPath, Color.White.copy(alpha = 0.3f * (0.5f + alertLevel * 0.5f)), style = Stroke(width = 2f))

                                    // Antenna spires on top
                                    repeat(2) { i ->
                                        val dir = if (i == 0) -1f else 1f
                                        val antX = tx + dir * bodyRadius * 0.4f
                                        val antTipY = ty - bodyRadius * 2.2f + sin(gameTime / 120f + i * 2f) * 4f
                                        drawLine(Color(0xFFFFAB00).copy(alpha = 0.3f + alertLevel * 0.4f), Offset(antX, ty - bodyRadius * 1.4f), Offset(antX, antTipY), strokeWidth = 2f)
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.5f + alertLevel * 0.4f), radius = 2f, center = Offset(antX, antTipY))
                                    }

                                    // Body segmentation glows (brighter with alert)
                                    repeat(2) { i ->
                                        val segY = ty - bodyRadius * 0.3f + i * (bodyRadius * 0.7f)
                                        drawLine(Color(0xFFFFAB00).copy(alpha = 0.2f + alertLevel * 0.4f), Offset(tx - bodyRadius * (0.6f - i * 0.3f), segY), Offset(tx + bodyRadius * (0.6f - i * 0.3f), segY), strokeWidth = 2f)
                                    }

                                    // Thermal shimmer lines above body
                                    repeat(3) { i ->
                                        val shimmerX = tx + (i - 1) * bodyRadius * 0.5f
                                        val shimmerY = ty - bodyRadius * 1.8f + sin(gameTime / 100f + i) * 5f
                                        drawLine(Color(0xFFFF6D00).copy(alpha = 0.15f + alertLevel * 0.25f), Offset(shimmerX - 10f, shimmerY), Offset(shimmerX + 10f, shimmerY), strokeWidth = 2f)
                                    }

                                    // Alert-level bar (top of body)
                                    drawRect(Color(0xFF1A1A1A).copy(alpha = 0.5f), Offset(tx - 15f, ty - bodyRadius * 1.8f), Size(30f, 4f))
                                    drawRect(Color(0xFFFF1744).copy(alpha = 0.7f), Offset(tx - 15f, ty - bodyRadius * 1.8f), Size(30f * alertLevel, 4f))

                                    // Scanning eye
                                    val eyePulse = (sin(gameTime / 100f) * 0.3f + 0.7f) * (0.3f + alertLevel * 0.7f)
                                    drawCircle(Color.White.copy(alpha = eyePulse), radius = 5f, center = Offset(tx, ty - 3f))
                                    drawCircle(Color(0xFFFF1744).copy(alpha = 0.6f + 0.4f * eyePulse), radius = 3f, center = Offset(tx, ty - 3f))

                                    // Heat trail particles intensify with alert
                                    if (alertLevel > 0.2f) {
                                        repeat((alertLevel * 6).toInt().coerceAtLeast(1)) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 100).toInt()
                                            val rng = Random(seed)
                                            val hpx = tx + (rng.nextFloat() - 0.5f) * 30f
                                            val hpy = ty + bodyRadius + rng.nextFloat() * 40f * alertLevel
                                            val particleAlpha = (1f - (hpy - ty - bodyRadius) / (40f * alertLevel)) * 0.6f
                                            drawCircle(Color(0xFFFF6D00).copy(alpha = particleAlpha), radius = 1f + rng.nextFloat() * 3f * alertLevel, center = Offset(hpx, hpy))
                                        }
                                    }

                                    // Scan rings
                                    val scanSpeed = 1f + alertLevel * 3f
                                    val scanPhase = (gameTime / 1000f * scanSpeed) % 1f
                                    drawCircle(Color(0xFFFF1744).copy(alpha = 0.15f * (1f - scanPhase)), radius = bodyRadius * 1.5f + 80f * scanPhase, center = Offset(tx, ty), style = Stroke(width = 2f))
                                    val scanPhase2 = (scanPhase + 0.5f) % 1f
                                    drawCircle(Color(0xFFFF6D00).copy(alpha = 0.1f * (1f - scanPhase2)), radius = bodyRadius * 1.5f + 80f * scanPhase2, center = Offset(tx, ty), style = Stroke(width = 1f))
                                }
                                "ENT_VOID_WHALE" -> {
                                    // Cosmic Leviathan — majestic ethereal behemoth (enhanced)
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 800f) * 0.15f + 0.85f)
                                    val tailSweep = sin(gameTime / 600f) * 20f
                                    val bodyRadius = 160f

                                    // Ethereal glow aura
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFF00BCD4).copy(alpha = 0.15f * pulse), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = bodyRadius * 2.5f
                                        ),
                                        radius = bodyRadius * 2.5f,
                                        center = Offset(tx, ty)
                                    )

                                    // Ambient floating particles around the whale
                                    repeat(12) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 13 + (gameTime / 300).toInt()
                                        val rng = Random(seed)
                                        val ax = tx + cos(gameTime / 500f + i * 1.2f) * (bodyRadius * 0.8f + rng.nextFloat() * bodyRadius)
                                        val ay = ty + sin(gameTime / 400f + i * 1.7f) * (bodyRadius * 0.4f + rng.nextFloat() * bodyRadius * 0.5f)
                                        val aColor = when (rng.nextInt(3)) { 0 -> Color.Cyan; 1 -> Color(0xFFCE93D8); else -> Color.White }
                                        drawCircle(aColor.copy(alpha = 0.06f * pulse), radius = 1f + rng.nextFloat() * 2f, center = Offset(ax, ay))
                                    }

                                    // Main body curve (whale silhouette)
                                    val bodyPath = Path().apply {
                                        moveTo(tx - bodyRadius * pulse, ty)
                                        cubicTo(tx - bodyRadius * 0.6f, ty - bodyRadius * 0.5f, tx + bodyRadius * 0.6f, ty - bodyRadius * 0.5f, tx + bodyRadius * pulse, ty)
                                        cubicTo(tx + bodyRadius * 0.6f, ty + bodyRadius * 0.25f, tx - bodyRadius * 0.6f, ty + bodyRadius * 0.25f, tx - bodyRadius * pulse, ty)
                                        close()
                                    }
                                    drawPath(bodyPath, Color(0xFF006064).copy(alpha = 0.3f * pulse))
                                    drawPath(bodyPath, Color.Cyan.copy(alpha = 0.2f * pulse), style = Stroke(width = 3f))

                                    // Dorsal ridge crest along top
                                    repeat(5) { i ->
                                        val rf = i.toFloat() / 4f
                                        val rx = tx + (rf - 0.3f) * bodyRadius * 1.4f
                                        val ry = ty - bodyRadius * 0.45f + sin(gameTime / 200f + i * 1.5f) * 5f
                                        val rh = 10f + rf * 15f
                                        drawLine(Color(0xFF00BCD4).copy(alpha = 0.2f * pulse), Offset(rx, ry), Offset(rx, ry - rh), strokeWidth = 3f)
                                        drawLine(Color.Cyan.copy(alpha = 0.1f * pulse), Offset(rx, ry), Offset(rx, ry - rh), strokeWidth = 1f)
                                    }

                                    // Ventral bioluminescent lines (underside glow)
                                    repeat(3) { i ->
                                        val vx = tx + (i - 1) * bodyRadius * 0.3f
                                        val vy = ty + bodyRadius * 0.15f
                                        val vLen = 20f + i * 8f
                                        val vPulse = (sin(gameTime / 400f + i * 1.8f) * 0.4f + 0.6f)
                                        drawLine(Color(0xFF00E5FF).copy(alpha = 0.15f * vPulse * pulse), Offset(vx - vLen, vy), Offset(vx + vLen, vy), strokeWidth = 2f)
                                    }

                                    // Glowing eye at the head
                                    val eyeX = tx + bodyRadius * 0.75f
                                    val eyeY = ty - bodyRadius * 0.15f
                                    val eyeGlow = (sin(gameTime / 200f) * 0.3f + 0.7f)
                                    drawCircle(Color(0xFFFF1744).copy(alpha = 0.1f * eyeGlow * pulse), radius = 12f, center = Offset(eyeX, eyeY))
                                    drawCircle(Color.White.copy(alpha = 0.6f * eyeGlow * pulse), radius = 6f, center = Offset(eyeX, eyeY))
                                    drawCircle(Color(0xFFFF1744).copy(alpha = 0.8f * eyeGlow * pulse), radius = 3f, center = Offset(eyeX, eyeY))

                                    // Tail fin with sweep animation
                                    val tailPath = Path().apply {
                                        moveTo(tx - bodyRadius * 0.9f, ty)
                                        lineTo(tx - bodyRadius * 1.4f, ty - bodyRadius * 0.4f + tailSweep)
                                        lineTo(tx - bodyRadius * 1.3f, ty + tailSweep * 0.5f)
                                        lineTo(tx - bodyRadius * 1.4f, ty + bodyRadius * 0.4f + tailSweep)
                                        close()
                                    }
                                    drawPath(tailPath, Color(0xFF00838F).copy(alpha = 0.25f * pulse))
                                    drawPath(tailPath, Color.Cyan.copy(alpha = 0.2f * pulse), style = Stroke(width = 2f))

                                    // Pectoral fins
                                    val finPath = Path().apply {
                                        moveTo(tx + bodyRadius * 0.2f, ty + bodyRadius * 0.2f)
                                        cubicTo(tx + bodyRadius * 0.5f, ty + bodyRadius * 0.6f, tx + bodyRadius * 0.3f, ty + bodyRadius * 0.7f, tx + bodyRadius * 0.1f, ty + bodyRadius * 0.3f)
                                        close()
                                    }
                                    drawPath(finPath, Color(0xFF00838F).copy(alpha = 0.15f * pulse))
                                    drawPath(finPath, Color.Cyan.copy(alpha = 0.1f * pulse), style = Stroke(width = 1f))

                                    // Nebula star-field body fill
                                    repeat(60) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 500).toInt()
                                        val rng = Random(seed)
                                        val nx = tx + (rng.nextFloat() - 0.5f) * bodyRadius * 1.6f
                                        val ny = ty + (rng.nextFloat() - 0.5f) * bodyRadius * 0.8f
                                        val starColor = when (rng.nextInt(4)) { 0 -> Color.Cyan; 1 -> Color(0xFFCE93D8); 2 -> Color(0xFF80D8FF); else -> Color.White }
                                        drawCircle(starColor.copy(alpha = 0.25f * rng.nextFloat() * pulse), radius = 1f + rng.nextFloat() * 2.5f, center = Offset(nx, ny))
                                    }

                                    // Bioluminescent skin dots
                                    repeat(8) { i ->
                                        val angleFrac = i.toFloat() / 8f
                                        val bx = tx + cos(angleFrac * PI.toFloat() * 2f) * bodyRadius * 0.7f
                                        val by = ty + sin(angleFrac * PI.toFloat() * 2f) * bodyRadius * 0.35f
                                        val starPulse = (sin(gameTime / 300f + i * 1.2f) * 0.5f + 0.5f)
                                        drawCircle(Color.Cyan.copy(alpha = 0.4f * starPulse * pulse), radius = 2f + starPulse * 3f, center = Offset(bx, by))
                                    }

                                    // Trailing star particles
                                    repeat(8) { i ->
                                        val trailPhase = ((gameTime / 2000f + i * 0.15f) % 1f)
                                        val dist = trailPhase * bodyRadius * 1.8f
                                        val offsetY = sin(trailPhase * PI.toFloat() * 2f + i) * 35f
                                        val tColor = when (i % 3) { 0 -> Color.Cyan; 1 -> Color(0xFFCE93D8); else -> Color.White }
                                        drawCircle(tColor.copy(alpha = (1f - trailPhase) * 0.35f * pulse), radius = 1f + (1f - trailPhase) * 3f, center = Offset(tx - dist * 0.7f, ty + offsetY))
                                    }

                                    // Slipstream direction arrows + lines when player is near
                                    val dx = player.x - tx
                                    val dy = player.y - cameraY - ty
                                    val near = dx * dx + dy * dy < 500f * 500f
                                    if (near) {
                                        val pushDir = if (threat.vx > 0) -1f else 1f
                                        // Direction arrow
                                        val arrX = tx + pushDir * bodyRadius * 0.5f
                                        drawLine(Color.Cyan.copy(alpha = 0.3f), Offset(arrX, ty), Offset(arrX + pushDir * 60f, ty), strokeWidth = 4f)
                                        val arrHead = Path().apply {
                                            moveTo(arrX + pushDir * 60f, ty)
                                            lineTo(arrX + pushDir * 45f, ty - 10f)
                                            lineTo(arrX + pushDir * 45f, ty + 10f)
                                            close()
                                        }
                                        drawPath(arrHead, Color.Cyan.copy(alpha = 0.3f))

                                        repeat(4) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 50).toInt()
                                            val rng = Random(seed)
                                            val sx = tx + (rng.nextFloat() - 0.5f) * bodyRadius
                                            val sy = ty + i * 30f - 60f
                                            drawLine(Color.Cyan.copy(alpha = 0.2f), Offset(sx, sy), Offset(sx, sy + 80f), strokeWidth = 2f)
                                        }

                                        // Void-wake lingering dots
                                        repeat(5) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 100).toInt()
                                            val rng = Random(seed)
                                            val wx = tx + (rng.nextFloat() - 0.5f) * 200f
                                            val wy = ty + (rng.nextFloat() - 0.5f) * 100f
                                            drawCircle(Color.White.copy(alpha = 0.08f), radius = 2f + rng.nextFloat() * 3f, center = Offset(wx, wy))
                                        }
                                    }
                                }
                                "ENT_VOID_WRAITH" -> {
                                    // Shadow Entity — phasing void horror (polished)
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val mat = threat.isMaterialized
                                    val visAlpha = if (mat) 1f else 0.03f

                                    // Danger aura ring when materialized
                                    if (mat) {
                                        val dangerPulse = (sin(gameTime / 80f) * 0.3f + 0.7f)
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.1f * dangerPulse), radius = 100f, center = Offset(tx, ty), style = Stroke(width = 3f))
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.05f * dangerPulse), radius = 120f, center = Offset(tx, ty), style = Stroke(width = 1f))
                                    }

                                    // State-indicator glow: purple when materialized, nearly invisible when phased
                                    val auraColor = if (mat) Color(0xFF4A148C) else Color(0xFF616161)
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(auraColor.copy(alpha = 0.15f * visAlpha), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 80f
                                        ),
                                        radius = 80f,
                                        center = Offset(tx, ty)
                                    )

                                    // Crackling energy when materialized only
                                    if (mat) {
                                        repeat(6) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 5 + (gameTime / 80).toInt()
                                            val rng = Random(seed)
                                            val ex = tx + (rng.nextFloat() - 0.5f) * 100f
                                            val ey = ty + (rng.nextFloat() - 0.5f) * 100f
                                            drawLine(Color(0xFFD500F9).copy(alpha = 0.4f), Offset(tx, ty), Offset(ex, ey), strokeWidth = 1f + rng.nextFloat() * 2f)
                                        }
                                    }

                                    // Full form when materialized
                                    if (mat) {
                                        val bodyAlpha = 0.7f
                                        val bodyColor = Color(0xFF1A1A2E).copy(alpha = bodyAlpha)
                                        val outlineColor = Color(0xFFD500F9).copy(alpha = bodyAlpha * 0.7f)
                                        val eyeIntensity = (sin(gameTime / 150f) * 0.3f + 0.7f)
                                        val px = player.x
                                        val py = player.y - cameraY

                                        // Head
                                        drawCircle(bodyColor, radius = 20f, center = Offset(tx, ty - 40f))
                                        drawCircle(outlineColor, radius = 20f, center = Offset(tx, ty - 40f), style = Stroke(width = 3f))

                                        // Eyes with pupil tracking
                                        val lookAngle = atan2(py - (ty - 42f), px - tx)
                                        val pupilOffset = 3f
                                        val lx = tx - 7f + cos(lookAngle) * pupilOffset
                                        val ly = ty - 42f + sin(lookAngle) * pupilOffset
                                        val rx = tx + 7f + cos(lookAngle) * pupilOffset
                                        val ry = ty - 42f + sin(lookAngle) * pupilOffset
                                        drawCircle(Color.White.copy(alpha = 0.8f * eyeIntensity), radius = 5f, center = Offset(tx - 7f, ty - 42f))
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.9f * eyeIntensity), radius = 3f, center = Offset(lx, ly))
                                        drawCircle(Color.White.copy(alpha = 0.8f * eyeIntensity), radius = 5f, center = Offset(tx + 7f, ty - 42f))
                                        drawCircle(Color(0xFFFF1744).copy(alpha = 0.9f * eyeIntensity), radius = 3f, center = Offset(rx, ry))

                                        // Torso
                                        val torsoPath = Path().apply {
                                            moveTo(tx - 25f, ty - 25f); lineTo(tx + 25f, ty - 25f)
                                            lineTo(tx + 20f, ty + 30f); lineTo(tx - 20f, ty + 30f); close()
                                        }
                                        drawPath(torsoPath, bodyColor)
                                        drawPath(torsoPath, outlineColor, style = Stroke(width = 2f))

                                        // Arms
                                        repeat(2) { i ->
                                            val armDir = if (i == 0) -1f else 1f
                                            drawLine(bodyColor, Offset(tx + armDir * 25f, ty - 15f), Offset(tx + armDir * 50f, ty + 30f), strokeWidth = 8f)
                                            drawLine(outlineColor, Offset(tx + armDir * 25f, ty - 15f), Offset(tx + armDir * 50f, ty + 30f), strokeWidth = 2f)
                                        }

                                        // Legs
                                        drawLine(bodyColor, Offset(tx - 12f, ty + 30f), Offset(tx - 20f, ty + 55f), strokeWidth = 7f)
                                        drawLine(bodyColor, Offset(tx + 12f, ty + 30f), Offset(tx + 20f, ty + 55f), strokeWidth = 7f)
                                        drawLine(outlineColor, Offset(tx - 12f, ty + 30f), Offset(tx - 20f, ty + 55f), strokeWidth = 2f)
                                        drawLine(outlineColor, Offset(tx + 12f, ty + 30f), Offset(tx + 20f, ty + 55f), strokeWidth = 2f)
                                    }
                                }
                                "HAZ_VOID_ANOMALY" -> {
                                    // Reality rift with inward pull
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 1000f) * 0.3f + 0.7f)
                                    val scan = threat.scanPulse

                                    // Deeper magenta aura (was pink)
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFF6A1B9A).copy(alpha = 0.5f * pulse), Color(0xFF4A148C).copy(alpha = 0.2f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 150f
                                        ),
                                        radius = 150f,
                                        center = Offset(tx, ty)
                                    )

                                    // Jagged space-tear rim
                                    val tearPath = Path().apply {
                                        val segments = 12
                                        moveTo(tx + 100f, ty)
                                        repeat(segments) {
                                            val angle = ((it + 1) / segments.toFloat()) * 2f * PI.toFloat()
                                            val seed = threat.instanceId.hashCode() + it + (gameTime / 200).toInt()
                                            val rng = Random(seed)
                                            val jitter = 90f + rng.nextFloat() * 20f
                                            lineTo(tx + cos(angle) * jitter, ty + sin(angle) * jitter)
                                        }
                                        close()
                                    }
                                    drawPath(tearPath, Color(0xFFAA00FF).copy(alpha = 0.15f * pulse), style = Stroke(width = 2f))

                                    // 3 expanding ring pulses (existing, enhanced)
                                    repeat(3) { i ->
                                        val ringPulse = (scan + i * 0.33f) % 1f
                                        drawCircle(
                                            color = Color(0xFFE1BEE7).copy(alpha = 0.15f * (1f - ringPulse)),
                                            radius = 20f + 130f * ringPulse,
                                            center = Offset(tx, ty),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                        )
                                    }

                                    // Inward-spiraling vortex particles
                                    repeat(10) { i ->
                                        val seed = threat.instanceId.hashCode() + i + (gameTime / 80).toInt()
                                        val rng = Random(seed)
                                        val angle = (gameTime / 400f + i * 0.63f) % (2f * PI.toFloat())
                                        val dist = 30f + ((gameTime / 60f + i * 30f) % 120f)
                                        val px = tx + cos(angle) * dist
                                        val py = ty + sin(angle) * dist
                                        drawCircle(Color(0xFFCE93D8).copy(alpha = 0.5f * pulse * (1f - dist / 150f)), radius = 2f + rng.nextFloat() * 2f, center = Offset(px, py))
                                    }

                                    // Tidal pull lines from anomaly to screen edges
                                    repeat(4) { i ->
                                        val pullAngle = (i / 4f) * 2f * PI.toFloat()
                                        val pullEnd = if (i % 2 == 0) Offset(if (i == 0) -100f else screenWidth + 100f, ty) else Offset(tx, if (i == 1) -100f else size.height + 100f)
                                        drawLine(Color(0xFFCE93D8).copy(alpha = 0.1f * pulse), Offset(tx, ty), pullEnd, strokeWidth = 2f)
                                    }
                                }
                                "MINI_BOSS_COMMANDER" -> {
                                    // Command Cruiser — phase-color shift + shield bubble
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val phase = threat.phase

                                    // Phase-color shift
                                    val hullColor = when {
                                        phase == 1 -> Color(0xFF263238)
                                        phase == 2 -> Color(0xFF1565C0) // Blue
                                        phase == 3 || phase == 4 -> Color(0xFFB71C1C) // Red
                                        else -> Color(0xFFE65100) // Orange (flee)
                                    }
                                    val engineGlowColor = when {
                                        phase >= 4 -> Color(0xFFFF6D00)
                                        phase >= 3 -> Color(0xFFFF1744)
                                        else -> Color.Cyan
                                    }

                                    // Shield bubble (when not all weak points destroyed)
                                    if (threat.activeWeakPoints > 0) {
                                        drawCircle(
                                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                colors = listOf(Color.Cyan.copy(alpha = 0.06f), Color.Transparent),
                                                center = Offset(tx, ty),
                                                radius = 200f
                                            ),
                                            radius = 200f,
                                            center = Offset(tx, ty)
                                        )
                                        drawCircle(Color.Cyan.copy(alpha = 0.1f), radius = 200f, center = Offset(tx, ty), style = Stroke(width = 2f))
                                    }

                                    // 1. Shadow (if arrival)
                                    if (threat.arrivalTimer < threat.arrivalDuration) {
                                        drawRect(Color.Black.copy(alpha = 0.2f), topLeft = Offset(0f, 0f), size = size)
                                    }

                                    // 2. Main Chassis
                                    drawRect(hullColor, topLeft = Offset(tx - 150f, ty - 60f), size = Size(300f, 120f))
                                    drawRect(Color.Gray.copy(alpha = 0.5f), topLeft = Offset(tx - 150f, ty - 60f), size = Size(300f, 120f), style = Stroke(width = 4f))

                                    // 3. Bridge / Command Tower
                                    drawRect(Color(0xFF37474F), topLeft = Offset(tx - 40f, ty - 100f), size = Size(80f, 40f))
                                    drawRect(engineGlowColor.copy(alpha = 0.3f), topLeft = Offset(tx - 30f, ty - 90f), size = Size(60f, 10f))

                                    // 4. Moving Antennae / Scanners
                                    repeat(2) { i ->
                                        val offset = if (i == 0) -120f else 120f
                                        val angle = (sin(gameTime / 500f + i) * 30f)
                                        rotate(angle, pivot = Offset(tx + offset, ty - 60f)) {
                                            drawLine(Color.Gray, Offset(tx + offset, ty - 60f), Offset(tx + offset, ty - 120f), strokeWidth = 3f)
                                            drawCircle(Color.Red, radius = 5f, center = Offset(tx + offset, ty - 120f))
                                        }
                                    }

                                    // 5. Radar Dish (Rotating)
                                    val radarAngle = (gameTime / 10f) % 360f
                                    rotate(radarAngle, pivot = Offset(tx + 60f, ty - 80f)) {
                                        drawArc(Color.Gray, 0f, 180f, true, topLeft = Offset(tx + 40f, ty - 100f), size = Size(40f, 40f))
                                    }

                                    // 6. Hull Lights (Flashing — faster in higher phases)
                                    val lightRate = if (phase >= 3) 200 else 500
                                    if ((gameTime / lightRate) % 2 == 0L) {
                                        drawCircle(Color.Yellow, radius = 4f, center = Offset(tx - 130f, ty - 40f))
                                        drawCircle(Color.Yellow, radius = 4f, center = Offset(tx + 130f, ty - 40f))
                                        drawCircle(Color.Yellow, radius = 4f, center = Offset(tx, ty - 40f))
                                    }

                                    // 7. Engines
                                    val engineFlicker = Random(gameTime / 50).nextFloat() * 15f
                                    repeat(3) { i ->
                                        val ex = tx - 100f + i * 100f
                                        drawCircle(
                                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                colors = listOf(engineGlowColor.copy(alpha = 0.5f), Color.Transparent),
                                                center = Offset(ex, ty + 60f),
                                                radius = 40f + engineFlicker
                                            ),
                                            radius = 40f + engineFlicker,
                                            center = Offset(ex, ty + 60f)
                                        )
                                    }

                                    // 8. Scanning Beams (Phase 3+)
                                    if (phase >= 3) {
                                        repeat(2) { i ->
                                            val offset = if (i == 0) -100f else 100f
                                            val angle = (sin(gameTime / 1000f + i) * 45f) + 90f
                                            rotate(angle, pivot = Offset(tx + offset, ty + 40f)) {
                                                drawPath(
                                                    path = Path().apply {
                                                        moveTo(tx + offset, ty + 40f)
                                                        lineTo(tx + offset + 400f, ty + 40f - 60f)
                                                        lineTo(tx + offset + 400f, ty + 40f + 60f)
                                                        close()
                                                    },
                                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(Color.Red.copy(alpha = 0.3f), Color.Transparent),
                                                        startX = tx + offset,
                                                        endX = tx + offset + 400f
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 9. Communication Arrays (Weak Points)
                                    repeat(threat.maxWeakPoints) { i ->
                                        val isDestroyed = i >= threat.activeWeakPoints
                                        val wx = tx - 80f + (i * 80f)
                                        val wy = ty - 40f
                                        if (!isDestroyed) {
                                            // Weak point with rotating beacon
                                            drawRect(Color.Magenta, topLeft = Offset(wx - 10f, wy - 10f), size = Size(20f, 20f))
                                            drawRect(Color.White, topLeft = Offset(wx - 10f, wy - 10f), size = Size(20f, 20f), style = Stroke(width = 2f))
                                            // Beacon rotation
                                            val beaconAngle = (gameTime / 20f + i * 120f) % 360f
                                            rotate(beaconAngle, pivot = Offset(wx, wy)) {
                                                drawLine(Color.White.copy(alpha = 0.5f), Offset(wx, wy), Offset(wx + 15f, wy), strokeWidth = 2f)
                                            }
                                        } else {
                                            if (Random.nextFloat() < 0.1f) {
                                                spawnBurst(wx, wy + cameraY, 5, SciFiBorder, 50f)
                                            }
                                        }
                                    }

                                    // Jam-wave ring (pulsing from cruiser)
                                    if (phase >= 3) {
                                        val jamPulse = (sin(gameTime / 400f) * 0.5f + 0.5f)
                                        drawCircle(Color(0xFF00BCD4).copy(alpha = 0.1f * jamPulse), radius = 100f + jamPulse * 200f, center = Offset(tx, ty), style = Stroke(width = 4f))
                                        drawCircle(Color.Cyan.copy(alpha = 0.05f * jamPulse), radius = 100f + jamPulse * 200f, center = Offset(tx, ty), style = Stroke(width = 2f))
                                    }

                                    // Gravity Pulse Visual (with debris particles)
                                    if (threat.pulseAlpha > 0) {
                                        val pulseScale = 1f - threat.pulseAlpha
                                        drawCircle(Color.White.copy(alpha = threat.pulseAlpha * 0.6f), radius = pulseScale * 1200f, center = Offset(tx, ty), style = Stroke(width = 8f))
                                        repeat(8) { i ->
                                            val seed = threat.instanceId.hashCode() + i + (gameTime / 50).toInt()
                                            val rng = Random(seed)
                                            val da = rng.nextFloat() * 2f * PI.toFloat()
                                            val dd = rng.nextFloat() * 600f
                                            drawCircle(Color.White.copy(alpha = 0.2f * threat.pulseAlpha), radius = 2f, center = Offset(tx + cos(da) * dd, ty + sin(da) * dd))
                                        }
                                    }
                                }
                                "BOSS_GATEKEEPER" -> {
                                    // Gatekeeper — safe gap clarity + afterimage rings
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val arrivalProgress = (threat.arrivalTimer / threat.arrivalDuration).coerceIn(0f, 1f)
                                    val phase = threat.phase

                                    // Dim background
                                    drawRect(Color.Black.copy(alpha = 0.4f * arrivalProgress), topLeft = Offset(0f, 0f), size = size)

                                    // Afterimage ghost rings from rotation
                                    repeat(2) { g ->
                                        val ghostAngle = threat.rotation - 30f - g * 20f
                                        rotate(ghostAngle, pivot = Offset(tx, ty)) {
                                            drawCircle(Color.White.copy(alpha = 0.05f * (1f - g * 0.5f)), radius = 250f, center = Offset(tx, ty), style = Stroke(width = 15f))
                                        }
                                    }

                                    rotate(threat.rotation, pivot = Offset(tx, ty)) {
                                        // Massive Orbital Ring
                                        drawCircle(Color.White.copy(alpha = 0.8f * arrivalProgress), radius = 250f, center = Offset(tx, ty), style = Stroke(width = 20f))

                                        // Safe Gaps with green/red coloring + solid barrier walls
                                        repeat(4) { i ->
                                            val isWpDestroyed = i >= threat.activeWeakPoints
                                            rotate(i * 90f, pivot = Offset(tx, ty)) {
                                                // Safe gap = green, danger arc = red
                                                val safeColor = if (isWpDestroyed) Color.Red.copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.4f)
                                                val dangerColor = Color.Red.copy(alpha = 0.5f)
                                                // Safe window (inner 50 degrees)
                                                drawArc(safeColor, startAngle = 45f, sweepAngle = 10f, useCenter = false, topLeft = Offset(tx - 250f, ty - 250f), size = Size(500f, 500f), style = Stroke(width = 80f))
                                                // Danger arc (outer 80 degrees)
                                                drawArc(dangerColor, startAngle = 5f, sweepAngle = 80f, useCenter = false, topLeft = Offset(tx - 250f, ty - 250f), size = Size(500f, 500f), style = Stroke(width = 80f))
                                                // High contrast unsafe edge
                                                drawArc(Color.Red.copy(alpha = 0.7f), startAngle = 5f, sweepAngle = 10f, useCenter = false, topLeft = Offset(tx - 250f, ty - 250f), size = Size(500f, 500f), style = Stroke(width = 90f))

                                                // Solid energy barrier walls
                                                drawRect(Color(0xFFFF1744).copy(alpha = 0.15f), Offset(tx + 245f, ty - 300f), Size(10f, 600f))

                                                // Weak Point Node with rotating shield
                                                if (!isWpDestroyed) {
                                                    val shieldAngle = (gameTime / 30f) % 360f
                                                    drawCircle(Color.Magenta, radius = 25f, center = Offset(tx + 250f, ty))
                                                    drawCircle(Color.White, radius = 10f, center = Offset(tx + 250f, ty))
                                                    // Rotating shield ring
                                                    drawArc(Color.Cyan.copy(alpha = 0.4f), startAngle = shieldAngle, sweepAngle = 120f, useCenter = false, topLeft = Offset(tx + 225f, ty - 25f), size = Size(50f, 50f), style = Stroke(width = 4f))
                                                }
                                            }
                                        }
                                    }

                                    // Push-back force lines from barrier toward player
                                    val pdx = player.x - tx
                                    val pdy = player.y - cameraY - ty
                                    val pDist = sqrt(pdx * pdx + pdy * pdy)
                                    if (pDist > 150f && pDist < 350f && threat.activeWeakPoints > 0) {
                                        repeat(4) { i ->
                                            val angle = atan2(pdy, pdx)
                                            val forceX = tx + cos(angle) * 200f + (i - 1.5f) * 20f
                                            val forceY = ty + sin(angle) * 200f
                                            drawLine(Color(0xFFFFAB00).copy(alpha = 0.15f), Offset(forceX, forceY), Offset(player.x, player.y - cameraY), strokeWidth = 2f)
                                        }
                                    }

                                    // Central Eye with iris tracking
                                    val eyePulse = (sin(gameTime / 200f) * 0.2f + 0.8f)
                                    val eyeColor = if (phase == 3) Color.Yellow else Color.Red
                                    drawCircle(eyeColor.copy(alpha = 0.9f * eyePulse), radius = 40f * arrivalProgress, center = Offset(tx, ty))
                                    // Iris tracks player
                                    val lookAngle = atan2(pdy, pdx)
                                    val irisX = tx + cos(lookAngle) * 15f
                                    val irisY = ty + sin(lookAngle) * 15f
                                    drawCircle(Color.White, radius = 15f * arrivalProgress, center = Offset(tx, ty))
                                    drawCircle(Color.Black, radius = 8f * arrivalProgress, center = Offset(irisX, irisY))
                                }
                                "BOSS_STAR_EATER" -> {
                                    // Star-Eater — power-up suction visible + dentition ring
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 400f) * 0.1f + 0.9f)
                                    val phase = threat.phase
                                    val auraRadius = if (phase == 3) 1000f else 800f

                                    // Sucking Aura
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color.Black, Color(0xFF6A1B9A).copy(alpha = 0.8f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = auraRadius
                                        ),
                                        radius = auraRadius,
                                        center = Offset(tx, ty)
                                    )

                                    // Energy Suction Particles
                                    repeat(15) { i ->
                                        val rand = Random(threat.instanceId.hashCode() + i)
                                        val angle = (gameTime / 10f + i * 24f) * (PI.toFloat() / 180f)
                                        val dist = ((gameTime / 5f + i * 100f) % auraRadius)
                                        val px = tx + cos(angle) * dist
                                        val py = ty + sin(angle) * dist
                                        drawCircle(Color.Magenta.copy(alpha = 0.4f), radius = 5f, center = Offset(px, py))
                                    }

                                    // After-image ghost copy
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFF6A1B9A).copy(alpha = 0.15f), Color.Transparent),
                                            center = Offset(tx - 50f, ty + 30f),
                                            radius = auraRadius * 0.8f
                                        ),
                                        radius = auraRadius * 0.8f,
                                        center = Offset(tx - 50f, ty + 30f)
                                    )

                                    // Power-up suction stream trails (toward nearest power-ups — simulated)
                                    repeat(4) { i ->
                                        val seed = threat.instanceId.hashCode() + i * 7 + (gameTime / 80).toInt()
                                        val rng = Random(seed)
                                        val sx = tx + (rng.nextFloat() - 0.5f) * 600f
                                        val sy = ty + (rng.nextFloat() - 0.5f) * 600f
                                        val streamPath = Path().apply {
                                            moveTo(sx, sy)
                                            cubicTo(sx, (sy + ty) * 0.5f, (sx + tx) * 0.5f, ty, tx, ty)
                                        }
                                        drawPath(streamPath, Color(0xFFBA68C8).copy(alpha = 0.15f), style = Stroke(width = 2f))
                                    }

                                    // Dark Core
                                    drawCircle(Color.Black, radius = 120f * pulse, center = Offset(tx, ty))

                                    // Dentition ring (energy teeth around maw)
                                    val toothCount = 16
                                    repeat(toothCount) { i ->
                                        val ta = (i / toothCount.toFloat()) * 2f * PI.toFloat() + (gameTime / 2000f)
                                        val innerR = 100f
                                        val outerR = 120f + pulse * 10f
                                        val tx1 = tx + cos(ta) * innerR
                                        val ty1 = ty + sin(ta) * innerR
                                        val tx2 = tx + cos(ta) * outerR
                                        val ty2 = ty + sin(ta) * outerR
                                        drawLine(Color(0xFFCE93D8).copy(alpha = 0.5f * pulse), Offset(tx1, ty1), Offset(tx2, ty2), strokeWidth = 4f * pulse)
                                    }

                                    // Core Eye (Weak Point)
                                    if (threat.activeWeakPoints > 0) {
                                        drawCircle(Color.Magenta.copy(alpha = 1.0f), radius = 50f * pulse, center = Offset(tx, ty))
                                        drawCircle(Color.White, radius = 15f, center = Offset(tx, ty))
                                    }

                                    // Tendrils — glow brighter when player is near
                                    val pDist = sqrt((player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty))
                                    val tendrilGlow = if (pDist < 400f) 1.0f else 0.5f
                                    repeat(12) { i ->
                                        val angle = i * 30f + sin(gameTime / 300f + i) * 40f
                                        rotate(angle, pivot = Offset(tx, ty)) {
                                            drawLine(
                                                if (phase == 3) Color.Red else Color(0xFFBA68C8).copy(alpha = tendrilGlow),
                                                Offset(tx + 80f, ty),
                                                Offset(tx + 400f, ty),
                                                strokeWidth = 15f * pulse * tendrilGlow
                                            )
                                        }
                                    }

                                    // Hunger-meter: core pulse rate increases (simulated via pulse speed variation)
                                    val hungerRate = 1f + (1f - pDist / 1000f).coerceIn(0f, 0.5f)
                                    drawCircle(Color(0xFFFF4081).copy(alpha = 0.1f * hungerRate), radius = 80f + pulse * 20f, center = Offset(tx, ty), style = Stroke(width = 3f))
                                }
                                "BOSS_VOID_ENGINE" -> {
                                    // Void Engine — gravity shift telegraph + inversion buildup
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val rot = threat.rotation
                                    val phase = threat.phase

                                    // Reality Warp Aura
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFFE91E63).copy(alpha = 0.3f), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 500f
                                        ),
                                        radius = 500f,
                                        center = Offset(tx, ty)
                                    )

                                    // Reality-tear rim (jagged edge)
                                    val tearPath = Path().apply {
                                        val segs = 16
                                        moveTo(tx + 500f, ty)
                                        repeat(segs) {
                                            val ta = ((it + 1) / segs.toFloat()) * 2f * PI.toFloat()
                                            val seed = threat.instanceId.hashCode() + it + (gameTime / 150).toInt()
                                            val rng = Random(seed)
                                            val jitter = 480f + rng.nextFloat() * 40f
                                            lineTo(tx + cos(ta) * jitter, ty + sin(ta) * jitter)
                                        }
                                        close()
                                    }
                                    drawPath(tearPath, Color(0xFFE91E63).copy(alpha = 0.08f), style = Stroke(width = 2f))

                                    rotate(rot, pivot = Offset(tx, ty)) {
                                        // Arm afterimages
                                        repeat(2) { g ->
                                            val ghostRot = rot - 30f - g * 15f
                                            rotate(ghostRot, pivot = Offset(tx, ty)) {
                                                repeat(3) { i ->
                                                    rotate(i * 120f, pivot = Offset(tx, ty)) {
                                                        drawRect(Color(0xFF880E4F).copy(alpha = 0.1f / (g + 1)), topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f))
                                                    }
                                                }
                                            }
                                        }

                                        repeat(3) { i ->
                                            rotate(i * 120f, pivot = Offset(tx, ty)) {
                                                drawRect(Color(0xFF880E4F), topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f))
                                                drawRect(Color.White, topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f), style = Stroke(width = 4f))
                                                if (i < threat.activeWeakPoints) {
                                                    drawCircle(Color.Magenta, radius = 20f, center = Offset(tx, ty - 150f))
                                                }
                                            }
                                        }
                                    }

                                    // Core instability arcs (frequency increases with damage)
                                    val arcRate = if (phase == 3) 2 else 4
                                    if ((gameTime / 80) % arcRate == 0L) {
                                        drawLine(Color.White, Offset(tx, ty), Offset(tx + (Random.nextFloat() - 0.5f) * 600f, ty + (Random.nextFloat() - 0.5f) * 600f), strokeWidth = 5f)
                                    }

                                    // Control-inversion buildup: screen tint shifts pink
                                    if (phase == 3 && (threat.localTimer.toInt() % 60) > 55) {
                                        drawRect(Color(0xFFE91E63).copy(alpha = 0.08f), topLeft = Offset(0f, 0f), size = size)
                                    }

                                    // Shift Direction Indicators (Large Arrows)
                                    if (phase >= 2 && (threat.localTimer.toInt() % 4 < 2)) {
                                        val shiftDir = if ((threat.localTimer.toInt() / 4) % 2 == 0) 1f else -1f
                                        // Screen-wide arrow indicators
                                        repeat(3) { i ->
                                            val ay = (gameTime / 2f + i * 200f) % screenHeight
                                            val ax = if (shiftDir > 0) 100f else screenWidth - 100f
                                            drawPath(
                                                path = Path().apply {
                                                    moveTo(ax, ay)
                                                    lineTo(ax + 50f * shiftDir, ay + 30f)
                                                    lineTo(ax, ay + 60f)
                                                    close()
                                                },
                                                color = Color.Magenta.copy(alpha = 0.3f)
                                            )
                                        }
                                        // Large background arrow
                                        drawPath(
                                            path = Path().apply {
                                                moveTo(screenWidth / 2f + shiftDir * 200f, screenHeight / 2f)
                                                lineTo(screenWidth / 2f - shiftDir * 100f, screenHeight / 2f - 60f)
                                                lineTo(screenWidth / 2f - shiftDir * 100f, screenHeight / 2f + 60f)
                                                close()
                                            },
                                            color = Color.Magenta.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                                "BOSS_LEVIATHAN" -> {
                                    // Leviathan — organic segments + directional slipstream
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val phase = threat.phase

                                    // Wall-pressure warning (red edge glow)
                                    val nearLeft = player.x < 100f
                                    val nearRight = player.x > screenWidth - 100f
                                    if (phase == 3 && (nearLeft || nearRight)) {
                                        val edgeX = if (nearLeft) 0f else screenWidth
                                        drawRect(Color.Red.copy(alpha = 0.15f), Offset(edgeX - if (nearLeft) 0f else 20f, 0f), Size(if (nearLeft) 20f else 20f, size.height))
                                    }

                                    repeat(6) { i ->
                                        val ox = sin(gameTime / 1000f - i * 0.5f) * 100f
                                        val oy = i * 60f
                                        val segmentPulse = (sin(gameTime / 500f + i) * 0.2f + 0.8f)
                                        val bodyColor = if (phase == 3) Color(0xFF1A237E) else Color(0xFF01579B)

                                        // Organic ellipse body + armor plate
                                        drawOval(bodyColor, topLeft = Offset(tx + ox - (60f - i * 8f) * segmentPulse, ty + oy - (45f - i * 6f) * segmentPulse), size = Size((120f - i * 16f) * segmentPulse, (90f - i * 12f) * segmentPulse))
                                        drawOval(Color.Cyan.copy(alpha = 0.2f), topLeft = Offset(tx + ox - (60f - i * 8f) * segmentPulse, ty + oy - (45f - i * 6f) * segmentPulse), size = Size((120f - i * 16f) * segmentPulse, (90f - i * 12f) * segmentPulse), style = Stroke(width = 2f))

                                        // Armor plate overlay
                                        drawOval(bodyColor.copy(alpha = 0.5f), topLeft = Offset(tx + ox - (30f - i * 4f) * segmentPulse, ty + oy - (10f - i * 2f) * segmentPulse), size = Size((60f - i * 8f) * segmentPulse, (20f - i * 3f) * segmentPulse))

                                        // Bioluminescent vein pattern
                                        val veinPath = Path().apply {
                                            moveTo(tx + ox - 20f + i * 3f, ty + oy - 15f)
                                            quadraticTo(tx + ox, ty + oy - 30f, tx + ox + 20f - i * 3f, ty + oy - 15f)
                                        }
                                        drawPath(veinPath, Color.Cyan.copy(alpha = 0.4f), style = Stroke(width = 2f))
                                        drawCircle(Color.Cyan.copy(alpha = 0.5f), radius = (40f - i * 5f) * segmentPulse, center = Offset(tx + ox, ty + oy), style = Stroke(width = 2f))

                                        // Directional slipstream arrow per segment
                                        val arrowDir = if (i % 2 == 0) 1f else -1f
                                        val arrX = tx + ox + arrowDir * 40f
                                        drawLine(Color.Cyan.copy(alpha = 0.3f), Offset(arrX, ty + oy), Offset(arrX + arrowDir * 25f, ty + oy), strokeWidth = 2f)
                                        val arrHead = Path().apply {
                                            moveTo(arrX + arrowDir * 25f, ty + oy)
                                            lineTo(arrX + arrowDir * 15f, ty + oy - 6f)
                                            lineTo(arrX + arrowDir * 15f, ty + oy + 6f)
                                            close()
                                        }
                                        drawPath(arrHead, Color.Cyan.copy(alpha = 0.3f))

                                        // Slipstream lines
                                        repeat(4) { j ->
                                            val windX = tx + ox + (Random.nextFloat() - 0.5f) * 60f
                                            val windY = ty + oy + 40f + (j * 40f)
                                            drawLine(Color.Cyan.copy(alpha = 0.4f), Offset(windX, windY), Offset(windX, windY + 60f), strokeWidth = 3f)
                                        }

                                        // Weak Points on even segments
                                        val wpIndex = i / 2
                                        if (i % 2 == 0 && wpIndex < threat.activeWeakPoints) {
                                            drawCircle(Color.Magenta, radius = 30f * segmentPulse, center = Offset(tx + ox, ty + oy))
                                            drawCircle(Color.White, radius = 10f, center = Offset(tx + ox, ty + oy))
                                        }

                                        // Head segment (segment 0) has glow eye
                                        if (i == 0) {
                                            drawCircle(Color(0xFFFF1744).copy(alpha = 0.6f), radius = 8f, center = Offset(tx + ox + 15f, ty + oy - 10f))
                                            drawCircle(Color.White.copy(alpha = 0.4f), radius = 4f, center = Offset(tx + ox + 15f, ty + oy - 10f))
                                        }

                                        // Tail whip telegraph (last 2 segments in phase 3 extend)
                                        if (phase == 3 && i >= 4) {
                                            val whipExtend = sin(gameTime / 200f + i) * 20f
                                            drawLine(Color.Red.copy(alpha = 0.3f), Offset(tx + ox + whipExtend, ty + oy), Offset(tx + ox + whipExtend * 2f, ty + oy + 20f), strokeWidth = 4f)
                                        }
                                    }
                                }
                                "BOSS_SIGNAL" -> {
                                    // The Signal — deception & corruption theme
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val phase = threat.phase
                                    val flicker = if (Random.nextFloat() < (if (phase == 3) 0.3f else 0.1f)) 0f else 1f

                                    // Static-noise TV ring
                                    drawCircle(Color(0xFF9E9E9E).copy(alpha = 0.04f), radius = 400f, center = Offset(tx, ty), style = Stroke(width = 60f))

                                    if (flicker > 0) {
                                        // Glitch rectangles (existing, enhanced)
                                        repeat(20) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 3 + (gameTime / 50).toInt()
                                            val rng = Random(seed)
                                            val rx = tx + (rng.nextFloat() - 0.5f) * 400f
                                            val ry = ty + (rng.nextFloat() - 0.5f) * 400f
                                            drawRect(
                                                if (phase == 3) Color.Red.copy(alpha = rng.nextFloat() * 0.3f) else Color.White.copy(alpha = rng.nextFloat() * 0.3f),
                                                topLeft = Offset(rx, ry),
                                                size = Size(rng.nextFloat() * 60f, rng.nextFloat() * 60f)
                                            )
                                        }

                                        // Screen-tear lines (horizontal offset bands)
                                        repeat(4) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 11 + (gameTime / 80).toInt()
                                            val rng = Random(seed)
                                            val tearY = ty - 300f + rng.nextFloat() * 600f
                                            val tearW = 20f + rng.nextFloat() * 60f
                                            drawRect(Color(0xFF212121).copy(alpha = 0.08f), Offset(tx - 200f + rng.nextFloat() * 100f, tearY), Size(tearW, 4f))
                                        }

                                        // Binary rain particles (0/1 characters as tiny rects)
                                        repeat(8) { i ->
                                            val seed = threat.instanceId.hashCode() + i * 13 + (gameTime / 60).toInt()
                                            val rng = Random(seed)
                                            val bx = tx + (rng.nextFloat() - 0.5f) * 300f
                                            val by = ty - 300f + ((gameTime / 40f + i * 80f) % 600f)
                                            drawRect(Color(0xFF00E676).copy(alpha = 0.1f), Offset(bx, by), Size(3f, 5f))
                                        }

                                        // Ghost platform preview (flickering platform outline)
                                        val ghostRng = Random(threat.instanceId.hashCode() + gameTime.toInt() / 100)
                                        if (phase >= 2 && ghostRng.nextFloat() < 0.3f) {
                                            val gx = tx + (ghostRng.nextFloat() - 0.5f) * 400f
                                            val gy = ty + (ghostRng.nextFloat() - 0.5f) * 400f
                                            drawRect(Color.White.copy(alpha = 0.08f), Offset(gx - 40f, gy - 5f), Size(80f, 10f), style = Stroke(width = 1f))
                                        }

                                        // Decoy Signal copies
                                        val decoyRng = Random(threat.instanceId.hashCode() + gameTime.toInt() / 150)
                                        if (phase == 3 && decoyRng.nextFloat() < 0.2f) {
                                            val dAngle = decoyRng.nextFloat() * 2f * PI.toFloat()
                                            val dDist = 100f + decoyRng.nextFloat() * 150f
                                            val dx = tx + cos(dAngle) * dDist
                                            val dy = ty + sin(dAngle) * dDist
                                            drawCircle(Color.Magenta.copy(alpha = 0.2f), radius = 30f, center = Offset(dx, dy))
                                            drawCircle(Color.White.copy(alpha = 0.1f), radius = 15f, center = Offset(dx, dy))
                                        }

                                        // Weak point (Glitching node)
                                        if (threat.activeWeakPoints > 0) {
                                            val wpPulse = (sin(gameTime / 100f) * 0.3f + 0.7f)
                                            drawCircle(Color.Magenta.copy(alpha = 0.6f * wpPulse), radius = 50f, center = Offset(tx, ty))
                                        }

                                        drawCircle(Color.White.copy(alpha = 0.1f * threat.scanPulse), radius = 600f, center = Offset(tx, ty))
                                    }
                                }
                            }
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
                    progressionManager = progressionManager,
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
                        gameTime = gameTime,
                        interferenceTimer = player.hudInterferenceTimer,
                        zone = altitudeManager.currentZone
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
                        gameTime = gameTime,
                        interferenceTimer = player.hudInterferenceTimer,
                        zone = altitudeManager.currentZone
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


