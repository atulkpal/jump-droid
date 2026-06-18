package com.example.jump_droid

import android.content.Context
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
    
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("highScore", 0)) }
    var highestYReached by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var score by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var breakableStreak by remember { mutableIntStateOf(0) }
    var phaseStreak by remember { mutableIntStateOf(0) }
    var magneticStreak by remember { mutableIntStateOf(0) }
    
    var runDurationTimer by remember { mutableFloatStateOf(0f) }
    var airborneTimer by remember { mutableFloatStateOf(0f) }
    var noOverheatTimer by remember { mutableFloatStateOf(0f) }
    
    var gameState by remember { mutableStateOf(GameState.TITLE) }
    var activeDiscovery by remember { mutableStateOf<DiscoveryType?>(null) }
    var unlockedRocket by remember { mutableStateOf<RocketType?>(null) }
    var codexNotification by remember { mutableStateOf<DiscoveryType?>(null) }

    val discoveryManager = remember { DiscoveryManager(sharedPrefs) }
    val progressionManager = remember { ProgressionManager(sharedPrefs) }
    val missionManager = remember { MissionManager() }
    val threatManager = remember { ThreatManager() }
    val comboManager = remember { ComboManager() }
    val missionCeremonies = remember { mutableStateMapOf<String, Float>() }
    val flyingRewards = remember { mutableStateListOf<FlyingReward>() }

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
    val powerUps = remember { mutableStateListOf<PowerUp>() }
    val landingEffects = remember { mutableStateListOf<LandingEffect>() }
    val particles = remember { mutableStateListOf<Particle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }

    var missionHintRotationTimer by remember { mutableFloatStateOf(0f) }
    var globalShowObjective by remember { mutableStateOf(false) }
    var newMissionOverlay by remember { mutableStateOf<Mission?>(null) }
    var newMissionOverlayTimer by remember { mutableFloatStateOf(0f) }

    var gameTime by remember { mutableLongStateOf(0L) }
    var powerUpSpawnTimer by remember { mutableFloatStateOf(0f) }
    var threatSpawnTimer by remember { mutableFloatStateOf(0f) }
    var cameraY by remember { mutableFloatStateOf(0f) }
    var isThrusting by remember { mutableStateOf(value = false) }
    var thrustTarget by remember { mutableStateOf(Offset.Zero) }
    var screenShake by remember { mutableFloatStateOf(0f) }
    var impactFlashAlpha by remember { mutableFloatStateOf(0f) }

    // --- Developer / Cheat States ---
    var infiniteFuel by remember { mutableStateOf(false) }
    var disableHeat by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }

    val bossesSpawned = remember { mutableStateSetOf<String>() }

    val notificationQueue = remember { mutableStateListOf<String>() }
    var notificationTimer by remember { mutableFloatStateOf(0f) }
    var activeNotification by remember { mutableStateOf<String?>(null) }
    var notificationAlpha by remember { mutableFloatStateOf(0f) }

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
            is ComboReward.PowerUp -> "${reward.type.name.replace("_", " ")}"
            is ComboReward.AltitudeBoost -> "ALTITUDE BOOST"
            is ComboReward.Artifact -> "ARTIFACT DISCOVERED"
        }

        // Task 4: Reward notifications near rocket
        floatingTexts.add(FloatingText(rewardName, player.x, player.y - 100f, color = rewardColor, isCritical = reward is ComboReward.Artifact))

        when (reward) {
            is ComboReward.Fuel -> {
                player.fuel = min(player.maxFuel, player.fuel + reward.amount)
                spawnBurst(player.x, player.y, 20, SciFiGreen, 200f)
            }
            is ComboReward.PowerUp -> {
                powerUps.add(PowerUp(player.x, player.y - 100f, reward.type, isMissionReward = true))
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
            if (!alreadyLanded) {
                // Task 1: Generic landing missions audit
                missionManager.updateProgress(MissionType.PLATFORMING) { it.id.startsWith("plat_land_") }
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
                    floatingTexts.add(FloatingText("FUEL RECHARGE", player.x, player.y - 100f, color = SciFiGreen))
                    checkDiscovery(DiscoveryType.FUEL_PLATFORM)
                }
                PlatformType.COOLING -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.heat = max(0f, player.heat - 30f)
                    spawnBurst(player.x, yTop, 20, SciFiCyan, 200f)
                    floatingTexts.add(FloatingText("ENGINES COOLED", player.x, player.y - 100f, color = SciFiCyan))
                    checkDiscovery(DiscoveryType.COOLING_PLATFORM)
                }
                PlatformType.STABILITY -> {
                    player.velocityY = LANDING_BOUNCE_VELOCITY
                    player.stabilityTimer = 10f
                    spawnBurst(player.x, yTop, 20, SciFiWhite, 200f)
                    floatingTexts.add(FloatingText("FLIGHT STABILIZED", player.x, player.y - 100f, color = SciFiWhite))
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
        RocketType.entries.forEach { type ->
            if (newScore >= type.unlockScore && !sharedPrefs.getBoolean("unlock_${type.name}", false)) {
                unlockedRocket = type
                gameState = GameState.UNLOCK
                checkDiscovery(type.discovery, forceTutorialState = false)
                sharedPrefs.edit { putBoolean("unlock_${type.name}", true) }
            }
        }

        if (newScore >= 0) checkDiscovery(player.rocketType.discovery)

        if (newScore >= 100) checkDiscovery(DiscoveryType.LORE_ASCENSION)
        if (newScore >= 5000) checkDiscovery(DiscoveryType.LORE_SIGNAL)
        if (newScore >= 10000) checkDiscovery(DiscoveryType.LORE_LOST_FLEET)
        if (newScore >= 20000) checkDiscovery(DiscoveryType.LORE_LOGS)

        AchievementsList.forEach { achievement ->
            if (!sharedPrefs.getBoolean("achievement_${achievement.id}", false)) {
                if (achievement.unlockCondition(newScore, player.maxComboReached, player.totalOverheats)) {
                    sharedPrefs.edit { putBoolean("achievement_${achievement.id}", true) }
                    floatingTexts.add(FloatingText("ACHIEVEMENT: ${achievement.title}", player.x, player.y - 200f, color = SciFiGold, isCritical = true))
                }
            }
        }
    }

    fun saveHighScore(newScore: Int) {
        if (newScore > highScore) {
            highScore = newScore
            sharedPrefs.edit { putInt("highScore", newScore) }
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
        player.heat = 0f
        player.isOverheated = false
        player.lastPlatform = spawnPlatform
        player.invulnerabilityTimer = 3.0f // 3 seconds of protection

        // Re-entry effect
        spawnBurst(player.x, player.y - 100f, 40, SciFiWhite, 300f)
        screenShake = 15f
        impactFlashAlpha = 1.0f
        floatingTexts.add(FloatingText("SYSTEM REBOOTED", player.x, player.y - 150f, color = SciFiCyan))

        continuesUsed++
        gameState = GameState.PLAYING
    }

    fun generatePlatform(lastY: Float): Platform {
        val difficulty = (score / 2000f).coerceIn(0f, 1f)
        val pWidth = (250f - (difficulty * 100f)).coerceAtLeast(100f)
        val gapY = 250f + (difficulty * 150f) + Random.nextFloat() * 100f
        val nextY = lastY - gapY
        val nextX = Random.nextFloat() * (screenWidth - pWidth)

        // Task 3: Adjusted Spawn Hierarchy for Sprint E Closure
        val type = when {
            score < 500 -> if (Random.nextFloat() < 0.2f) PlatformType.MOVING else PlatformType.NORMAL
            else -> {
                val rand = Random.nextFloat()
                when {
                    rand < 0.10f -> PlatformType.MOVING
                    rand < 0.22f -> PlatformType.ICE
                    rand < 0.35f -> PlatformType.BOOST
                    rand < 0.48f -> PlatformType.BREAKABLE
                    rand < 0.60f && phaseStreak < 2 -> PlatformType.PHASE 
                    rand < 0.70f && magneticStreak < 2 -> PlatformType.MAGNETIC
                    rand < 0.78f -> PlatformType.STABILITY
                    rand < 0.82f -> PlatformType.FUEL
                    rand < 0.86f -> PlatformType.COOLING
                    else -> PlatformType.NORMAL
                }
            }
        }

        if (type == PlatformType.BREAKABLE) breakableStreak++ else breakableStreak = 0
        if (type == PlatformType.PHASE) phaseStreak++ else phaseStreak = 0
        if (type == PlatformType.MAGNETIC) magneticStreak++ else magneticStreak = 0

        val isMoving = type == PlatformType.MOVING
        val speed = if (isMoving) (100f + (difficulty * 200f)) * (if (Random.nextBoolean()) 1f else -1f) else 0f
        // Task 4: Breakable Platform Tension Pass (20% faster)
        val totalBreakTime = if (type == PlatformType.BREAKABLE) 1.5f + Random.nextFloat() * 1.5f else 3f

        return Platform(nextX, nextY, pWidth, type, isMoving, speed, totalBreakTime)
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
        if (amount <= 0 || gameState != GameState.PLAYING) return

        // Visual Feedback
        screenShake = (12f + amount * 0.5f).coerceAtMost(40f)
        impactFlashAlpha = 0.7f

        var remainingDamage = amount

        // 1. Shield Absorption
        if (player.shield > 0) {
            val shieldDamage = min(player.shield, remainingDamage)
            player.shield -= shieldDamage
            remainingDamage -= shieldDamage

            // Shield Hit Feedback (Cyan)
            spawnBurst(player.x, player.y, 12, SciFiCyan, 450f)
            // Hook for sound: playShieldHitSound()
        }

        // 2. Integrity Damage
        if (remainingDamage > 0) {
            player.integrity = max(0f, player.integrity - remainingDamage)

            // Hull Hit Feedback (Red/Gold sparks)
            spawnBurst(player.x, player.y, 18, SciFiRed, 650f)
            spawnBurst(player.x, player.y, 8, SciFiGold, 400f)
            // Hook for sound: playHullHitSound()

            if (player.integrity <= 0) {
                gameState = GameState.GAMEOVER
                saveHighScore(score)
            }
        }

        // Pause Regen
        player.shieldRegenPauseTimer = Constants.SHIELD_REGEN_DELAY
    }

    fun unlockAll() {
        RocketType.entries.forEach { sharedPrefs.edit { putBoolean("unlock_${it.name}", true) } }
        DiscoveryType.entries.forEach { sharedPrefs.edit { putBoolean("discovery_$it", true) } }
        missionManager.updateProgress(MissionType.EXPLORATION, absoluteValue = score)
        missionManager.selectNextMission()

        // Handle Completed Missions (Rewards)
        missionManager.activeMissions.filter { it.isCompleted }.forEach { mission ->
            // Grant Reward
            when (val r = mission.reward) {
                is MissionReward.PowerUp -> {
                    val count = r.amount
                    repeat(count) {
                        powerUps.add(PowerUp(player.x, cameraY - 100f, r.type))
                    }
                }
                is MissionReward.Artifact -> {
                    checkDiscovery(r.discoveryType, forceTutorialState = false)
                }
                else -> {}
            }

            // Visuals
            floatingTexts.add(FloatingText("MISSION COMPLETE!", player.x, player.y - 150f, color = SciFiGreen, isCritical = true))
            spawnBurst(player.x, player.y - 100f, 30, SciFiGreen, 400f)
        }
        missionManager.selectNextMission()
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

        gameTime = 0L
        runDurationTimer = 0f
        airborneTimer = 0f
        noOverheatTimer = 0f

        powerUpSpawnTimer = 0f
        score = 0
        altitudeManager.updateAltitude(0)
        highestYReached = groundY
        cameraY = 0f
        continuesUsed = 0
        breakableStreak = 0
        phaseStreak = 0
        magneticStreak = 0
        platforms.clear()
        powerUps.clear()
        landingEffects.clear()
        particles.clear()
        threatManager.clear()
        missionManager.clear()
        threatSpawnTimer = 0f
        bossesSpawned.clear()
        notificationQueue.clear()
        activeNotification = null

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

                        // Update systems
                        discoveryManager.update(dt)
                        threatManager.update(dt, cameraY, screenHeight, screenWidth, player.x, player.y)
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
                                notificationQueue.add("COMBO MILESTONE: SURVIVAL DROP")
                                checkDiscovery(DiscoveryType.EFFICIENCY_SURVIVAL)
                            }
                            comboManager.immediateSurvivalRewards.clear()
                        }

                        if (comboManager.pendingReward != null && comboManager.pendingReward != prevPending) {
                            // Combo Ended with Reward (Task 5: Tier 3)
                            
                            // NEW COMBO HIGH CELEBRATION (Sprint E: Polish)
                            if (comboManager.isNewHighReached) {
                                floatingTexts.add(FloatingText("NEW COMBO HIGH!", player.x, player.y - 150f, color = SciFiGold, isCritical = true))
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
                            notificationQueue.add("COMBO COMPLETE: x${comboManager.lastFinalStreak}")
                            
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
                        if (activeNotification != null) {
                            notificationTimer -= dt
                            if (notificationTimer <= 0f) {
                                notificationAlpha -= dt * 2f
                                if (notificationAlpha <= 0f) {
                                    activeNotification = null
                                }
                            }
                        } else if (notificationQueue.isNotEmpty()) {
                            activeNotification = notificationQueue.removeAt(0)
                            notificationAlpha = 1f
                            notificationTimer = 2.0f // Show for 2 seconds
                        }

                        // --- Milestone Spawning (Boss Progression) ---
                        val bossMilestones = listOf(
                            "MINI_BOSS_COMMANDER" to 1500,
                            "BOSS_GATEKEEPER" to 4000,
                            "BOSS_LEVIATHAN" to 7000,
                            "BOSS_STAR_EATER" to 10000,
                            "BOSS_VOID_ENGINE" to 15000,
                            "BOSS_SIGNAL" to 18000
                        )

                        bossMilestones.forEach { (id, threshold) ->
                            if (score >= threshold && !bossesSpawned.contains(id)) {
                                bossesSpawned.add(id)
                                ThreatRegistry.getById(id)?.let { def ->
                                    threatManager.spawnThreat(def, screenWidth / 2f, cameraY - 600f)
                                    notificationQueue.add("!!! ${def.name.uppercase()} ARRIVING !!!")
                                    screenShake = 50f
                                    impactFlashAlpha = 1.0f
                                    
                                    val discovery = when(id) {
                                        "MINI_BOSS_COMMANDER" -> DiscoveryType.THREAT_SENTINEL
                                        "BOSS_GATEKEEPER" -> DiscoveryType.THREAT_GATEKEEPER
                                        "BOSS_LEVIATHAN" -> DiscoveryType.THREAT_LEVIATHAN
                                        "BOSS_STAR_EATER" -> DiscoveryType.THREAT_STAR_EATER
                                        "BOSS_VOID_ENGINE" -> DiscoveryType.THREAT_VOID_ENGINE
                                        "BOSS_SIGNAL" -> DiscoveryType.THREAT_SIGNAL
                                        else -> null
                                    }
                                    discovery?.let { checkDiscovery(it) }
                                }
                            }
                        }

                        // Threat Spawning Logic
                        threatSpawnTimer += dt
                        if (threatSpawnTimer > 3f) { // Check every 3 seconds
                            threatSpawnTimer = 0f
                            val activeThreats = threatManager.activeThreats
                            val eligible = ThreatRegistry.getEligibleThreats(score, altitudeManager.currentZone)

                            // Slow down spawning if boss is present
                            val bossPresent = activeThreats.any { it.definition.type == ThreatType.BOSS || it.definition.type == ThreatType.MINI_BOSS }
                            val spawnChanceMod = if (bossPresent) 0.3f else 1.0f

                            val currentZone = altitudeManager.currentZone

                            // 1. Environmental Hazards (Sprint B)
                            if (activeThreats.none { it.definition.type == ThreatType.HAZARD }) {
                                // Attempt to spawn one eligible hazard
                                val hazards = eligible.filter { it.type == ThreatType.HAZARD }.shuffled()
                                for (hazard in hazards) {
                                    // Apply weighting based on zone
                                    val weight = when (currentZone) {
                                        AltitudeZone.CLOUD_LAYER -> when (hazard.id) {
                                            "HAZ_LIGHTNING", "HAZ_TURBULENCE" -> 1.5f
                                            else -> 1.0f
                                        }
                                        AltitudeZone.UPPER_ATMOSPHERE -> 1.0f
                                        AltitudeZone.ORBIT -> when (hazard.id) {
                                            "HAZ_RADIATION", "HAZ_SOLAR_FLARE" -> 1.5f
                                            "HAZ_LIGHTNING" -> 0.3f
                                            else -> 1.0f
                                        }
                                        AltitudeZone.DEEP_SPACE -> when (hazard.id) {
                                            "HAZ_RADIATION", "HAZ_SOLAR_FLARE", "HAZ_EMP" -> 1.5f
                                            else -> 1.0f
                                        }
                                        AltitudeZone.VOID -> 2.0f
                                        else -> 1.0f
                                    }

                                    if (Random.nextFloat() < hazard.spawnRules.spawnChance * spawnChanceMod * weight) {
                                        val spawnX = Random.nextFloat() * screenWidth
                                        val spawnY = when (hazard.id) {
                                            "HAZ_SOLAR_FLARE" -> cameraY - 400f // Comes from above
                                            "HAZ_DEBRIS" -> cameraY - 200f
                                            else -> cameraY + Random.nextFloat() * screenHeight
                                        }
                                        
                                        // Specific init for some threats
                                        val vx = if (hazard.id == "HAZ_DEBRIS") (Random.nextFloat() - 0.5f) * 100f else 0f
                                        val vy = if (hazard.id == "HAZ_DEBRIS") 100f + Random.nextFloat() * 200f else 0f
                                        
                                        threatManager.spawnThreat(hazard, spawnX, spawnY, vx, vy)
                                        notificationQueue.add("${hazard.name.uppercase()} DETECTED")
                                        break // Only one major threat active in a local area
                                    }
                                }
                            }

                            // 2. Generic Enemies (Surveyor Probe, Swarm)
                            if (activeThreats.count { it.definition.id == "ENT_SCOUT_DRONE" } < 2) {
                                eligible.find { it.id == "ENT_SCOUT_DRONE" }?.let { probeDef ->
                                    if (Random.nextFloat() < probeDef.spawnRules.spawnChance * spawnChanceMod) {
                                        val spawnX = if (Random.nextBoolean()) -50f else screenWidth + 50f
                                        val vx = if (spawnX < 0) 150f else -150f
                                        threatManager.spawnThreat(probeDef, spawnX, cameraY + Random.nextFloat() * (screenHeight * 0.5f), vx = vx)
                                        notificationQueue.add("SURVEYOR PROBE DETECTED")
                                    }
                                }
                            }
                            if (activeThreats.none { it.definition.id == "ENT_SWARM_BOTS" }) {
                                eligible.find { it.id == "ENT_SWARM_BOTS" }?.let { swarmDef ->
                                    if (Random.nextFloat() < swarmDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(swarmDef, Random.nextFloat() * screenWidth, cameraY - 100f)
                                        notificationQueue.add("AEROSOL SWARM DETECTED")
                                    }
                                }
                            }

                            // 3. Zone-Specific Entities
                            if (currentZone == AltitudeZone.CLOUD_LAYER && activeThreats.none { it.definition.id == "ENT_CLOUD_SKIMMER" }) {
                                eligible.find { it.id == "ENT_CLOUD_SKIMMER" }?.let { rayDef ->
                                    if (Random.nextFloat() < rayDef.spawnRules.spawnChance * spawnChanceMod) {
                                        val dir = if (Random.nextBoolean()) 1f else -1f
                                        val spawnX = if (dir > 0) -200f else screenWidth + 200f
                                        threatManager.spawnThreat(rayDef, spawnX, cameraY + Random.nextFloat() * screenHeight, vx = dir * 50f)
                                    }
                                }
                            }
                            if (currentZone == AltitudeZone.ORBIT && activeThreats.none { it.definition.id == "ENT_ORBITAL_SENTRY" }) {
                                eligible.find { it.id == "ENT_ORBITAL_SENTRY" }?.let { sentryDef ->
                                    if (Random.nextFloat() < sentryDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(sentryDef, Random.nextFloat() * screenWidth, cameraY + 200f)
                                    }
                                }
                            }
                            if (currentZone == AltitudeZone.DEEP_SPACE && activeThreats.none { it.definition.id == "ENT_CORRUPTED_HULL" }) {
                                eligible.find { it.id == "ENT_CORRUPTED_HULL" }?.let { echoDef ->
                                    if (Random.nextFloat() < echoDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(echoDef, Random.nextFloat() * screenWidth, cameraY - 100f, vx = (Random.nextFloat() - 0.5f) * 30f, vy = 20f + Random.nextFloat() * 30f)
                                    }
                                }
                            }

                            // 4. Mini-Boss & Boss Spawning
                            if (activeThreats.none { it.definition.id == "MINI_BOSS_COMMANDER" }) {
                                eligible.find { it.id == "MINI_BOSS_COMMANDER" }?.let { bossDef ->
                                    if (Random.nextFloat() < bossDef.spawnRules.spawnChance) {
                                        threatManager.spawnThreat(bossDef, screenWidth / 2f, cameraY - 600f)
                                        notificationQueue.add("COMMAND CRUISER INBOUND")
                                        screenShake = 20f
                                        checkDiscovery(DiscoveryType.THREAT_SENTINEL)
                                    }
                                }
                            }


                            // Phase 3 Support: Boss Spawns reinforcements & Hazards
                            activeThreats.find { it.definition.id == "MINI_BOSS_COMMANDER" }?.let { boss ->
                                if (boss.phase == 3 || boss.phase == 4) {
                                    // Periodic reinforcement call
                                    if (Random.nextFloat() < 0.08f) { // Chance per 3s check
                                        val def = ThreatRegistry.getById("ENT_SCOUT_DRONE")
                                        def?.let {
                                            val side = if (Random.nextBoolean()) 1f else -1f
                                            val spawnX = if (side > 0) -100f else screenWidth + 100f
                                            threatManager.spawnThreat(it, spawnX, boss.y + 100f, vx = side * 200f)
                                            notificationQueue.add("REINFORCEMENTS INBOUND")
                                        }
                                    }
                                    // Boss-generated hazards
                                    if (Random.nextFloat() < 0.15f) {
                                        val id = "HAZ_TURBULENCE"
                                        ThreatRegistry.getById(id)?.let { threatManager.spawnThreat(it, boss.x, boss.y + 100f) }
                                    }
                                }
                            }
                        }

                        // (Legacy combo reset logic removed - now handled by ComboManager)

                        val textIterator = floatingTexts.iterator()
                        while (textIterator.hasNext()) {
                            val ft = textIterator.next()
                            ft.life -= dt
                            ft.y -= 50f * dt
                            if (ft.life <= 0) textIterator.remove()
                        }
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
                            threatManager.activeThreats.filter { it.state == ThreatState.ACTIVE }.forEach { threat ->
                                val dx = player.x - threat.x
                                val dy = player.y - threat.y
                                val distSq = dx * dx + dy * dy

                                when (threat.definition.id) {
                                    "HAZ_LIGHTNING" -> {
                                        if (distSq < 200f * 200f) {
                                            if (threat.phase == 2) { // Striking
                                                if (player.invulnerabilityTimer <= 0f) {
                                                    if (player.shield > 0) {
                                                        player.shield = max(0f, player.shield - 25f)
                                                        player.shieldRegenPauseTimer = 2f
                                                    } else {
                                                        player.integrity = max(0f, player.integrity - 10f)
                                                    }
                                                    player.invulnerabilityTimer = 0.5f
                                                    screenShake = 20f
                                                    impactFlashAlpha = 0.8f
                                                }
                                            }
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "HAZ_DEBRIS" -> {
                                        // Task 4: Physical Wreckage Collision (Hull Focus)
                                        if (distSq < 80f * 80f) {
                                            if (player.invulnerabilityTimer <= 0f) {
                                                // Debris deals more hull damage than energy damage
                                                if (player.shield > 0) {
                                                    player.shield = max(0f, player.shield - 10f)
                                                    player.integrity = max(0f, player.integrity - 5f)
                                                } else {
                                                    player.integrity = max(0f, player.integrity - 25f)
                                                }
                                                player.invulnerabilityTimer = 0.8f
                                                screenShake = 20f
                                                floatingTexts.add(FloatingText("HULL IMPACT", player.x, player.y - 100f, color = SciFiRed))
                                            }
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "HAZ_RADIATION" -> {
                                        if (distSq < 300f * 300f) {
                                            player.shield = max(0f, player.shield - 15f * sdt)
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "HAZ_SOLAR_FLARE" -> {
                                        if (abs(player.y - threat.y) < 100f) {
                                            player.heat = min(MAX_HEAT, player.heat + 120f * sdt)
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "HAZ_TURBULENCE" -> {
                                        // Task 4: Flight Control Force (No Direct Damage)
                                        if (distSq < 450f * 450f) {
                                            val dist = sqrt(distSq)
                                            val strength = (1f - dist / 450f)
                                            val windDir = if (threat.instanceId.hashCode() % 2 == 0) 1f else -1f
                                            player.velocityX += windDir * 1200f * strength * sdt
                                            player.velocityY += (Random.nextFloat() - 0.5f) * 600f * strength * sdt
                                            
                                            if (gameTime % 200 < 50) {
                                                particles.add(Particle(player.x, player.y, (Random.nextFloat()-0.5f)*100f, (Random.nextFloat()-0.5f)*100f, 0.5f, Color.White.copy(alpha=0.3f), 2f))
                                            }
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "HAZ_GRAVITY" -> {
                                        if (distSq < 500f * 500f) {
                                            val dist = sqrt(distSq)
                                            val strength = (1f - dist / 500f)
                                            player.velocityY += 1500f * strength * sdt 
                                            if (isThrusting) {
                                                player.fuel = max(0f, player.fuel - 10f * strength * sdt)
                                                player.velocityY += 500f * strength * sdt 
                                            }
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "HAZ_EMP" -> {
                                        val ringRadius = threat.scanPulse * 400f
                                        val dist = sqrt(distSq)
                                        if (abs(dist - ringRadius) < 50f) {
                                            player.shieldRegenPauseTimer = max(player.shieldRegenPauseTimer, 5f)
                                            threat.definition.discoveryType?.let { checkDiscovery(it) }
                                        }
                                    }
                                    "ENT_CLOUD_SKIMMER" -> { // Sky Ray: Lift Current
                                        if (distSq < 250f * 250f) {
                                            player.velocityY -= 1000f * sdt // "Ride the Ray"
                                        }
                                    }
                                    "ENT_ORBITAL_SENTRY" -> { // Orbital Sentry: Combo Freeze & Fuel Interference
                                        if (threat.isTracking && distSq < 400f * 400f) {
                                            player.comboFreezeTimer = 1.0f
                                            player.fuel = max(0f, player.fuel - 10f * sdt)
                                            if (!threat.hasInteracted) {
                                                threat.hasInteracted = true
                                                activeNotification = "LOCKED ON"
                                                notificationAlpha = 1.0f
                                            }
                                        } else {
                                            threat.hasInteracted = false
                                        }
                                    }
                                    "ENT_CORRUPTED_HULL" -> { // Derelict Echo: Salvage
                                        if (!threat.hasInteracted && distSq < 150f * 150f) {
                                            threat.hasInteracted = true
                                            activeNotification = "DISTRESS SIGNAL DETECTED"
                                            notificationAlpha = 1.0f
                                            val type = PowerUpType.entries.random()
                                            powerUps.add(PowerUp(threat.x, threat.y, type))
                                            floatingTexts.add(FloatingText("SALVAGE RECOVERED", player.x, player.y - 100f, color = Color.Green))
                                        }
                                    }
                                    "HAZ_VOID_ANOMALY" -> { // Void Anomaly: Gravitational Distortion
                                        if (distSq < 500f * 500f) {
                                            val dist = sqrt(distSq)
                                            val force = (1f - dist / 500f) * 1200f
                                            player.velocityX -= (dx / dist) * force * sdt
                                            player.velocityY -= (dy / dist) * force * sdt
                                            screenShake = max(screenShake, (1f - dist / 500f) * 15f)
                                            player.velocityX += (Random.nextFloat() - 0.5f) * 400f * sdt
                                            if (!threat.hasInteracted) {
                                                threat.hasInteracted = true
                                                activeNotification = "REALITY DISTORTION"
                                                notificationAlpha = 1.0f
                                            }
                                        } else {
                                            threat.hasInteracted = false
                                        }
                                    }
                                    "ENT_SCOUT_DRONE" -> { // Surveyor Probe: Reinforcement call
                                        if (threat.isTracking && threat.lifetime > 5.0f && Random.nextFloat() < 0.001f) {
                                            // Task 1: Offscreen reinforcements
                                            val currentDrones = threatManager.activeThreats.count { it.definition.id == "ENT_SCOUT_DRONE" }
                                            if (currentDrones < 4) {
                                                val side = if (Random.nextBoolean()) 1f else -1f
                                                val spawnX = if (side > 0) -100f else screenWidth + 100f
                                                threatManager.spawnThreat(threat.definition, spawnX, threat.y, vx = side * 150f)
                                                activeNotification = "REINFORCEMENTS INBOUND"
                                                notificationAlpha = 1.0f
                                            }
                                        }
                                    }
                                    "ENT_SWARM_BOTS" -> { // Aerosol Swarm: Turbulence
                                        if (distSq < 150f * 150f) {
                                            player.velocityX += (Random.nextFloat() - 0.5f) * 600f * sdt
                                            player.velocityY += (Random.nextFloat() - 0.5f) * 600f * sdt
                                        }
                                    }
                                    "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL" -> {
                                        // Generic Boss Collision & Weak Points
                                        if (threat.phase in 2..4) {
                                            // Handle Proximity Penalty (Collision with hull)
                                            if (distSq < 100f * 100f && player.invulnerabilityTimer <= 0f) {
                                                player.fuel = max(0f, player.fuel - 20f)
                                                player.heat = min(MAX_HEAT, player.heat + 30f)
                                                player.invulnerabilityTimer = 1.0f
                                                screenShake = 15f
                                                floatingTexts.add(FloatingText("HULL COLLISION", player.x, player.y - 100f, color = Color.Red))
                                            }

                                            // Weak Point Logic
                                            repeat(threat.maxWeakPoints) { i ->
                                                val isDestroyed = i >= threat.activeWeakPoints
                                                if (!isDestroyed) {
                                                    // Calculate weak point position based on boss type
                                                    val (wx, wy) = when (threat.definition.id) {
                                                        "MINI_BOSS_COMMANDER" -> Pair(threat.x - 80f + (i * 80f), threat.y - 40f)
                                                        "BOSS_GATEKEEPER" -> {
                                                            val angle = (threat.rotation + i * 90f) * (PI.toFloat() / 180f)
                                                            Pair(threat.x + cos(angle) * 250f, threat.y + sin(angle) * 250f)
                                                        }
                                                        "BOSS_STAR_EATER" -> Pair(threat.x, threat.y) // Center eye
                                                        "BOSS_LEVIATHAN" -> {
                                                            val ox = sin(threat.lifetime * 1000f - i * 0.5f) * 100f
                                                            Pair(threat.x + ox, threat.y + i * 60f)
                                                        }
                                                        "BOSS_VOID_ENGINE" -> {
                                                            val angle = (threat.rotation + i * 180f) * (PI.toFloat() / 180f)
                                                            Pair(threat.x + cos(angle) * 150f, threat.y + sin(angle) * 150f)
                                                        }
                                                        "BOSS_SIGNAL" -> Pair(threat.x + (Random(threat.instanceId.hashCode()).nextFloat()-0.5f)*200f, threat.y + (Random(threat.instanceId.hashCode()).nextFloat()-0.5f)*200f)
                                                        else -> Pair(threat.x, threat.y)
                                                    }

                                                    val ddx = player.x - wx
                                                    val ddy = player.y - wy
                                                    val hitDist = if (threat.definition.id == "BOSS_STAR_EATER") 80f else 50f

                                                    if (sqrt(ddx*ddx + ddy*ddy) < hitDist && player.invulnerabilityTimer <= 0f) {
                                                        threat.activeWeakPoints--
                                                        player.invulnerabilityTimer = 0.5f
                                                        player.velocityY = -400f // Bounce off
                                                        spawnBurst(wx, wy, 25, SciFiPurple, 300f)
                                                        screenShake = 20f
                                                        floatingTexts.add(FloatingText("WEAK POINT DESTROYED", player.x, player.y - 120f, color = SciFiPurple, isCritical = true))

                                                        if (threat.activeWeakPoints <= 0) {
                                                            threat.phase = 5 // Force retreat/destruction phase
                                                            score += 1000
                                                            floatingTexts.add(FloatingText("BOSS CRITICAL - RETREATING", player.x, player.y - 150f, color = SciFiCyan, isCritical = true))
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Boss-Specific Mechanics
                                        when (threat.definition.id) {
                                            "MINI_BOSS_COMMANDER" -> {
                                                if (threat.phase >= 3) {
                                                     if (distSq < 400f * 400f) {
                                                        player.fuel = max(0f, player.fuel - 5f * sdt) // Boss drain
                                                     }

                                                     // Mechanic 2: Gravity Pulse (AMPLIFIED)
                                                     if (threat.gravityPulseTimer < 0.6f) {
                                                         player.velocityY += 2500f * sdt
                                                         screenShake = max(screenShake, 15f)
                                                     }

                                                     // Mechanic 1: Platform Jammer (AGGRESSIVE)
                                                     if (threat.jamCooldown <= 0f) {
                                                         threat.jamCooldown = 1.2f
                                                         // Target 2 nearest platforms
                                                         platforms.sortedBy {
                                                             val dxP = it.x - player.x
                                                             val dyP = it.y - player.y
                                                             dxP*dxP + dyP*dyP
                                                         }.take(2).forEach {
                                                             it.isJammed = true
                                                             it.jamTimer = 2.0f
                                                         }
                                                     }
                                                }
                                            }
                                            "BOSS_GATEKEEPER" -> {
                                                val dist = sqrt(distSq)
                                                if (dist in 150f..350f) {
                                                    val angle = atan2(dy, dx) * (180f / PI.toFloat())
                                                    val relAngle = (angle - threat.rotation + 360f) % 360f
                                                    // Narrower safe gaps (50 degrees instead of 70)
                                                    val inGap = relAngle % 90f < 50f
                                                    if (!inGap) {
                                                        player.velocityX += (dx / dist) * 4000f * sdt // EXTREME REPULSE
                                                        player.heat += 100f * sdt
                                                        screenShake = max(screenShake, 5f)
                                                    }
                                                }
                                                if (threat.phase == 3 && dist < 600f) {
                                                    // Massive pull to center
                                                    player.velocityX -= (dx / dist) * 1500f * sdt
                                                    player.velocityY -= (dy / dist) * 1500f * sdt
                                                }
                                            }
                                            "BOSS_STAR_EATER" -> {
                                                val dist = sqrt(distSq)
                                                if (dist < 1000f) {
                                                    val suctionForce = (1f - dist / 1000f) * 3000f
                                                    player.velocityX -= (dx / dist) * suctionForce * sdt
                                                    player.velocityY -= (dy / dist) * suctionForce * sdt

                                                    if (dist < 200f) {
                                                        player.fuel = max(0f, player.fuel - 25f * sdt) // Violent energy drain
                                                    }
                                                }
                                                // Extreme resource pull
                                                powerUps.forEach { pu ->
                                                    val pdx = threat.x - pu.x
                                                    val pdy = threat.y - pu.y
                                                    val pdist = sqrt(pdx*pdx + pdy*pdy)
                                                    if (pdist < 1200f) {
                                                        pu.y += (pdy / pdist) * 800f * sdt
                                                        pu.x += (pdx / pdist) * 800f * sdt
                                                    }
                                                }
                                            }
                                            "BOSS_LEVIATHAN" -> {
                                                // Slipstreams (Propel the player violently)
                                                repeat(6) { i ->
                                                    val ox = sin(threat.lifetime * 1.5f - i * 0.5f) * 150f
                                                    val oy = i * 80f
                                                    val segX = threat.x + ox
                                                    val segY = threat.y + oy

                                                    val sdx = player.x - segX
                                                    val sdy = player.y - segY
                                                    if (sdx*sdx + sdy*sdy < 200f * 200f && sdy > 20f) {
                                                        player.velocityY -= 4500f * sdt // MASSIVE BOOST
                                                        if (Random.nextFloat() < 0.4f) {
                                                            spawnBurst(player.x, player.y + 50f, 10, SciFiCyan, 300f)
                                                        }
                                                    }
                                                }
                                            }
                                            "BOSS_VOID_ENGINE" -> {
                                                if (threat.phase >= 2 && (threat.localTimer.toInt() % 4 < 2)) {
                                                    // Reality Shift: EXTREME Horizontal gravity
                                                    val shiftDir = if ((threat.localTimer.toInt() / 4) % 2 == 0) 1f else -1f
                                                    player.velocityX += 4800f * shiftDir * sdt
                                                    screenShake = max(screenShake, 10f)
                                                    if (Random.nextFloat() < 0.3f) {
                                                        impactFlashAlpha = 0.4f
                                                    }
                                                }
                                            }
                                            "BOSS_SIGNAL" -> {
                                                val distSqSignal = distSq // rename or just use it
                                                if (distSqSignal < 800f * 800f) {
                                                    // Interference
                                                    if (Random.nextFloat() < 0.05f) {
                                                        notificationAlpha = 0.5f
                                                        activeNotification = "SIGNAL LOSS..."
                                                    }
                                                    // Fake fuel drain / Heat induction
                                                    if (threat.phase == 3) {
                                                        player.heat += 40f * sdt
                                                    }
                                                }
                                                // Mechanic: Ghost Platforms (DRAMATICALLY INCREASED)
                                                if (threat.phase >= 2 && (gameTime / 1000) % 2 == 0L) {
                                                    if (Random.nextFloat() < 0.15f && platforms.size < 40) {
                                                        platforms.add(Platform(
                                                            initialX = Random.nextFloat() * screenWidth,
                                                            y = cameraY + Random.nextFloat() * screenHeight,
                                                            width = 150f,
                                                            type = PlatformType.NORMAL,
                                                            isMoving = false,
                                                            initialSpeed = 0f,
                                                            totalBreakTime = 0.05f // Disappears instantly
                                                        ).apply { isBreaking = true })
                                                    }
                                                }
                                            }
                                        }

                                        // End encounter reward (ULTRA OBVIOUS) - Task 5: Tier 4
                                        if (threat.phase == 5 && !threat.hasInteracted) {
                                            threat.hasInteracted = true
                                            powerUps.add(PowerUp(player.x, cameraY + 200f, PowerUpType.ARTIFACT))
                                            floatingTexts.add(FloatingText("!!! ${threat.definition.name.uppercase()} DEFEATED !!!", player.x, player.y - 200f, color = SciFiCyan, isCritical = true))
                                            activeNotification = ">>> MISSION DATA RECOVERED <<<"
                                            notificationAlpha = 5.0f
                                            screenShake = 70f
                                            impactFlashAlpha = 1.0f
                                            spawnBurst(player.x, cameraY + 200f, 100, SciFiPurple, 1200f) // Massive purple burst
                                            
                                            // Task 5: Tier 4 Major Event Visuals
                                            repeat(8) {
                                                spawnBurst(player.x + (Random.nextFloat() - 0.5f) * 200f, 
                                                           player.y + (Random.nextFloat() - 0.5f) * 200f, 
                                                           50, SciFiWhite, 800f)
                                            }

                                            // Mission Hook
                                            if (threat.definition.id == "MINI_BOSS_COMMANDER") {
                                                missionManager.updateProgress(MissionType.BOSS)
                                            }
                                        }
                                    }
                                }
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
                            if (isThrusting && (player.fuel > 0f || infiniteFuel) && !player.isOverheated) {
                                val currentThrust = BASE_THRUST_POWER * player.rocketType.thrustMult * (if (player.turboTimer > 0) 1.2f else 1.0f)
                                val currentConsumption = if (infiniteFuel) 0f else BASE_FUEL_CONSUMPTION * player.rocketType.fuelMult * (if (player.efficiencyTimer > 0) 0.8f else 1.0f)

                                player.velocityY -= currentThrust * sdt
                                val dx = thrustTarget.x - player.x
                                val maxSteerDist = screenWidth / 3f
                                val steerMult = if (player.stabilityTimer > 0) 1.2f else 0.7f
                                val steerForce = (dx / maxSteerDist).coerceIn(-1f, 1f)
                                player.velocityX += steerForce * currentThrust * steerMult * sdt

                                player.fuel = max(0f, player.fuel - currentConsumption * sdt)
                                if (!disableHeat) {
                                    player.heat = min(MAX_HEAT, player.heat + HEAT_GENERATION_RATE * player.rocketType.heatMult * sdt)
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

                                if (player.heat > MAX_HEAT * 0.7f) checkDiscovery(DiscoveryType.HEAT_SYSTEM)
                                if (player.heat >= MAX_HEAT && !player.isOverheated) {
                                    player.isOverheated = true
                                    player.overheatTimer = OVERHEAT_COOLDOWN_TIME
                                    isThrusting = false
                                }
                            } else if (!player.isOverheated) {
                                player.heat = max(0f, player.heat - COOLING_RATE * sdt)
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

                                // Task 1: Phase Platform collision gating
                                if (platform.type == PlatformType.PHASE) {
                                    val cycle = 4000L
                                    val progress = (gameTime % cycle) / cycle.toFloat()
                                    // 0.42 to 0.92 is considered "passed through"
                                    if (progress > 0.42f && progress < 0.92f) return@forEach
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

                        powerUpSpawnTimer += dt
                        if (powerUpSpawnTimer > 20f) { // Task 0: Reduced frequency
                            val types = PowerUpType.entries.filter { it != PowerUpType.ARTIFACT }
                            val rand = Random.nextFloat()
                            val type = when {
                                rand < 0.05f -> PowerUpType.ARTIFACT
                                else -> types.random()
                            }
                            // Task 0: Higher spawn for visibility
                            powerUps.add(PowerUp(Random.nextFloat() * (screenWidth - 60f) + 30f, cameraY - 100f, type))
                            powerUpSpawnTimer = 0f
                        }

                        val powerUpIterator = powerUps.iterator()
                        while (powerUpIterator.hasNext()) {
                            val pu = powerUpIterator.next()
                            pu.life -= dt

                            if (pu.isMissionReward) {
                                // Mission Reward: Magnetic and fast
                                val dx = player.x - pu.x
                                val dy = player.y - pu.y
                                val distSq = dx*dx + dy*dy
                                val dist = sqrt(distSq)
                                val pull = 1000f * dt
                                pu.x += (dx / dist) * pull
                                pu.y += (dy / dist) * pull
                            } else {
                                // Normal Powerup: Task 1 logic
                                if (pu.hoverTimer > 0) {
                                    pu.hoverTimer -= dt
                                    // Slight hover bobbing
                                    pu.y += sin(gameTime / 200f) * 0.5f 
                                } else {
                                    // Task 1: Restore descent speed (200f) and add acceleration
                                    pu.velocityY += 300f * dt 
                                    pu.y += (200f + pu.velocityY) * dt
                                }

                                // Task 4: Magnetic Reward Interaction
                                platforms.forEach { plat ->
                                    if (plat.type == PlatformType.MAGNETIC) {
                                        val dx = pu.x - (plat.x + plat.width / 2f)
                                        val dy = pu.y - (plat.y + PLATFORM_HEIGHT / 2f)
                                        val distSq = dx * dx + dy * dy
                                        val radius = 250f
                                        if (distSq < radius * radius) {
                                            val dist = sqrt(distSq)
                                            val pull = 400f * dt * (1f - dist / radius)
                                            pu.x -= (dx / dist) * pull
                                            pu.y -= (dy / dist) * pull
                                        }
                                    }
                                }
                            }

                            if (abs(player.x - pu.x) < 80f && abs(player.y - pu.y) < 100f) { // Task 0: Increased radius
                                when (pu.type) {
                                    PowerUpType.FUEL_TANK -> {
                                        if (player.maxFuel < Constants.MAX_FUEL_CAPACITY_LIMIT) {
                                            player.maxFuel = min(Constants.MAX_FUEL_CAPACITY_LIMIT, player.maxFuel + 25f)
                                            player.fuel = player.maxFuel
                                            notificationQueue.add("FUEL CAPACITY UP!")
                                        } else {
                                            player.fuel = player.maxFuel
                                            notificationQueue.add("FUEL REFILLED")
                                        }
                                        spawnBurst(pu.x, pu.y, 30, SciFiGold, 300f)
                                        screenShake = 5f
                                        checkDiscovery(DiscoveryType.FUEL_TANK)
                                    }
                                    PowerUpType.TURBO_BOOSTER -> {
                                        player.turboTimer = 8f
                                        spawnBurst(pu.x, pu.y, 30, SciFiCyan, 300f)
                                        screenShake = 5f
                                        notificationQueue.add("TURBO ACTIVE!")
                                        checkDiscovery(DiscoveryType.TURBO_BOOSTER)
                                    }
                                    PowerUpType.EFFICIENCY_MODULE -> {
                                        player.efficiencyTimer = 8f
                                        spawnBurst(pu.x, pu.y, 30, SciFiGreen, 300f)
                                        screenShake = 5f
                                        notificationQueue.add("FUEL EFFICIENCY UP!")
                                        checkDiscovery(DiscoveryType.EFFICIENCY_MODULE)
                                    }
                                    PowerUpType.HEAT_SINK -> {
                                        player.heat = max(0f, player.heat - MAX_HEAT * 0.5f)
                                        player.isOverheated = false
                                        spawnBurst(pu.x, pu.y, 30, SciFiWhite, 300f)
                                        screenShake = 5f
                                        notificationQueue.add("ENGINES COOLED!")
                                        checkDiscovery(DiscoveryType.HEAT_SINK)
                                    }
                                    PowerUpType.SHIELD_CAPSULE -> {
                                        player.shield = min(player.maxShield, player.shield + 25f)
                                        spawnBurst(pu.x, pu.y, 30, SciFiCyan, 400f)
                                        notificationQueue.add("SHIELD RECHARGE")
                                        floatingTexts.add(FloatingText("+25 SHIELD", player.x, player.y - 120f, color = SciFiCyan))
                                    }
                                    PowerUpType.HULL_REPAIR -> {
                                        player.integrity = min(player.maxIntegrity, player.integrity + 20f)
                                        spawnBurst(pu.x, pu.y, 30, SciFiGreen, 400f)
                                        notificationQueue.add("HULL REPAIRED")
                                        floatingTexts.add(FloatingText("+20 HULL", player.x, player.y - 120f, color = SciFiGreen))
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
                                        notificationQueue.add("ARTIFACT RECOVERED!")
                                    }
                                    PowerUpType.ALTITUDE_BOOSTER -> {
                                        player.velocityY = -2500f // Massive boost
                                        spawnBurst(pu.x, pu.y, 40, SciFiWhite, 400f)
                                        screenShake = 10f
                                        notificationQueue.add("ALTITUDE BOOST!")
                                    }
                                }

                                if (pu.isMissionReward) {
                                    // Secondary celebration burst for mission rewards
                                    spawnBurst(player.x, player.y, 40, SciFiGold, 400f)
                                }

                                powerUpIterator.remove()
                            } else if (pu.y > cameraY + screenHeight + 200f || pu.life <= 0f) powerUpIterator.remove()
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
                                notificationQueue.add("MISSION RESET")
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

                        if (newMissionOverlayTimer > 0f) {
                            newMissionOverlayTimer -= dt
                            if (newMissionOverlayTimer <= 0f) newMissionOverlay = null
                        }

                        // Intro Card Trigger
                        missionManager.activeMissions.find { it.isNew }?.let {
                            it.isNew = false
                            newMissionOverlay = it
                            newMissionOverlayTimer = 3.0f
                        }

                        // Handle Completed Missions (Ceremony & Rewards)
                        missionManager.activeMissions.forEach { mission ->
                            if (mission.isCompleted) {
                                when (mission.ceremonyStage) {
                                    CeremonyStage.GLOW -> {
                                        // Wait a brief moment or start immediately
                                        mission.ceremonyStage = CeremonyStage.COMPLETED_TEXT
                                        screenShake = max(screenShake, 10f)
                                    }
                                    CeremonyStage.COMPLETED_TEXT -> {
                                        // Transition handled by UI but we can add logic here if needed
                                        // For now, move to spawn reward after a delay
                                        // We can use a dedicated timer per mission for precise stages if needed
                                    }
                                    CeremonyStage.REWARD_SPAWNED -> {
                                        // Reward is already in flyingRewards, waiting for it to finish
                                    }
                                    else -> {}
                                }
                            }
                        }

                        missionManager.activeMissions.filter { it.isCompleted && missionCeremonies[it.id] == null }.forEach { mission ->
                             missionCeremonies[mission.id] = 0f // Start ceremony
                        }

                        val ceremonyKeys = missionCeremonies.keys.toList()
                        ceremonyKeys.forEach { mid ->
                            val m = missionManager.activeMissions.find { it.id == mid }
                            if (m == null) { missionCeremonies.remove(mid); return@forEach }

                            val prevTime = missionCeremonies[mid] ?: 0f
                            val newTime = prevTime + dt
                            missionCeremonies[mid] = newTime

                            // Task 3 & 6: Card disintegration and Category Personality
                            if (newTime < 2.0f) {
                                val missionIdx = missionManager.activeMissions.indexOf(m)
                                if (missionIdx >= 0) {
                                    val totalMissions = missionManager.activeMissions.size
                                    val startX = (screenWidth - (totalMissions * 110f)) / 2f
                                    val cardX = startX + (missionIdx * 110f) + 55f
                                    val cardY = 100f * densityValue
                                    
                                    // Task 1: Increased density for visible fracturing
                                    val particleChance = if (newTime < 1.0f) 0.4f else 0.8f
                                    if (Random.nextFloat() < particleChance) {
                                        repeat(if (newTime > 1.5f) 5 else 2) {
                                            val pColor = when(m.type) {
                                                MissionType.EXPLORATION -> Color(0xFFFFD700) // Gold
                                                MissionType.PLATFORMING -> Color(0xFFFF5722) // Sparks/Orange
                                                MissionType.SURVIVAL -> Color(0xFF03A9F4) // Energy/Cyan
                                                MissionType.DISCOVERY -> Color(0xFF9C27B0) // Artifact/Purple
                                                MissionType.BOSS -> Color(0xFFFFC107) // Yellow/Shock
                                                else -> Color.White
                                            }
                                            particles.add(Particle(
                                                x = cardX + (Random.nextFloat() - 0.5f) * 110f,
                                                y = (cardY + cameraY) + (Random.nextFloat() - 0.5f) * 65f, // Account for cameraY when spawning from HUD
                                                vx = (Random.nextFloat() - 0.5f) * 80f,
                                                vy = (Random.nextFloat() - 0.5f) * 80f + 30f,
                                                life = 1.5f,
                                                color = pColor,
                                                size = 3f + Random.nextFloat() * 5f
                                            ))
                                        }
                                    }
                                }
                            }

                            // Stage transitions
                            if (prevTime < 1.0f && newTime >= 1.0f) {
                                m.ceremonyStage = CeremonyStage.COMPLETED_TEXT
                                
                                val missionIdx = missionManager.activeMissions.indexOf(m)
                                if (missionIdx >= 0) {
                                    val totalMissions = missionManager.activeMissions.size
                                    val startX = (screenWidth - (totalMissions * 110f)) / 2f
                                    val cardX = startX + (missionIdx * 110f) + 55f
                                    val cardY = 100f * densityValue
                                    
                                    // Task 4: Restore star/sparkle burst on mission completion
                                    repeat(40) {
                                        val angle = Random.nextFloat() * 2f * PI.toFloat()
                                        val speed = 150f + Random.nextFloat() * 300f
                                        particles.add(Particle(
                                            x = cardX, y = cardY + cameraY, // Account for cameraY when spawning from HUD
                                            vx = cos(angle) * speed, vy = sin(angle) * speed,
                                            life = 1.2f + Random.nextFloat() * 1.0f,
                                            color = if (Random.nextBoolean()) SciFiWhite else SciFiGold,
                                            size = 4f + Random.nextFloat() * 5f // Larger size triggers star rendering
                                        ))
                                    }
                                }

                                // Task 5: Hierarchy
                                val isSignificant = m.type == MissionType.DISCOVERY || m.type == MissionType.BOSS || m.id.contains("exp_void") || m.id == "surv_cool_2"
                                if (isSignificant) {
                                    screenShake = max(screenShake, 15f)
                                    impactFlashAlpha = max(impactFlashAlpha, 0.3f)
                                } else {
                                    screenShake = max(screenShake, 8f)
                                }
                            }
                            if (prevTime < 2.0f && newTime >= 2.0f) {
                                // Spawn Reward
                                val missionIdx = missionManager.activeMissions.indexOf(m)
                                val cardX = if (missionIdx >= 0) {
                                    val totalMissions = missionManager.activeMissions.size
                                    val startX = (screenWidth - (totalMissions * 110f)) / 2f
                                    startX + (missionIdx * 110f) + 55f
                                } else screenWidth / 2f

                                val comboReward = when (val r = m.reward) {
                                    is MissionReward.PowerUp -> ComboReward.PowerUp(r.type)
                                    is MissionReward.Artifact -> ComboReward.Artifact(r.discoveryType)
                                    else -> ComboReward.Fuel(50f)
                                }

                                flyingRewards.add(FlyingReward(
                                    type = comboReward,
                                    x = cardX,
                                    y = 100f * densityValue, // top area
                                    scale = 4.0f // Task 2: Increased scale
                                ))
                                m.ceremonyStage = CeremonyStage.REWARD_SPAWNED
                                spawnBurst(cardX, 100f * densityValue, 40, SciFiGold, 350f)
                            }
                            if (prevTime < 3.5f && newTime >= 3.5f) {
                                m.ceremonyStage = CeremonyStage.REPLACING
                                missionCeremonies.remove(mid)
                                
                                // Mission Synergy Bonus: Small progress to other active tracks
                                missionManager.activeMissions.filter { !it.isCompleted && it.id != m.id }.forEach { other ->
                                    when (other.type) {
                                        MissionType.PLATFORMING -> {
                                            other.currentProgress = min(other.targetValue, other.currentProgress + 1)
                                            other.checkCompletion()
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
                        }

                        // Cycle tracks: removes completed and adds new ones
                        missionManager.selectNextMission()

                        // EPIC 5: Survival System Updates
                        if (player.shieldRegenPauseTimer > 0) {
                            player.shieldRegenPauseTimer = max(0f, player.shieldRegenPauseTimer - dt)
                        } else if (player.shield < player.maxShield) {
                            player.shield = min(player.maxShield, player.shield + Constants.SHIELD_REGEN_RATE * dt)
                        }

                        // Task 3: Hull Failure & Destruction Sequence
                        if (player.integrity <= 0 && player.destructionTimer <= 0) {
                            player.destructionTimer = 0.01f // Start sequence
                            screenShake = 30f
                        }
                        
                        if (player.destructionTimer > 0) {
                            player.destructionTimer += dt
                            player.velocityX *= 0.95f
                            player.velocityY += 500f * dt // Loss of control, falling
                            
                            if (player.destructionTimer > 2.0f) {
                                gameState = GameState.GAMEOVER
                                saveHighScore(score)
                            }
                        }

                        // Emergency Warnings
                        if (player.shield > 0 && player.shield < player.maxShield * Constants.SURVIVAL_CRITICAL_THRESHOLD) {
                            if (gameTime % 3000 < 50) { // Throttled notification
                                notificationQueue.add("!!! SHIELD CRITICAL !!!")
                            }
                        }
                        if (player.integrity < player.maxIntegrity * Constants.SURVIVAL_CRITICAL_THRESHOLD) {
                            if (gameTime % 3000 < 50) { // Throttled notification
                                notificationQueue.add("!!! HULL CRITICAL !!!")
                            }
                        }

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

                    // Reality Distortion Visual Overlay
                    threatManager.activeThreats.find { it.definition.id == "HAZ_VOID_ANOMALY" && it.state == ThreatState.ACTIVE }?.let { anomaly ->
                        val dx = player.x - anomaly.x
                        val dy = player.y - anomaly.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 1000f) {
                            val intensity = (1f - dist / 1000f).coerceIn(0f, 1f)
                            drawRect(
                                color = Color(0xFFFF00FF).copy(alpha = 0.1f * intensity),
                                size = size
                            )
                        }
                    }

                    ambientManager.render(this, cameraY, gameTime)
                }

                // Speed lines
                val speedRatio = (abs(player.velocityY) / 1200f).coerceIn(0f, 1f)
                if (speedRatio > 0.4f) {
                    repeat(8) {
                        val rx = Random.nextFloat() * screenWidth
                        val ry = Random.nextFloat() * screenHeight
                        val alpha = (speedRatio - 0.4f) * 0.3f
                        drawLine(
                            Color.White.copy(alpha = alpha),
                            start = Offset(rx, ry),
                            end = Offset(rx, ry + 60f * speedRatio),
                            strokeWidth = 1f + 1f * speedRatio
                        )
                    }
                }

                if (gameState == GameState.PLAYING || gameState == GameState.GAMEOVER || gameState == GameState.TUTORIAL || gameState == GameState.PAUSED || gameState == GameState.HELP || gameState == GameState.UNLOCK) {
                    drawRect(Color(0xFF795548), topLeft = Offset(0f, groundY + (ROCKET_HEIGHT / 2) - cameraY), size = Size(screenWidth, screenHeight))

                    particles.forEach { p -> 
                        val alpha = (p.life / 1.0f).coerceIn(0f, 1f)
                        // Task 4: Render larger particles as stars for sparkle effect
                        if (p.size > 5f && (gameTime / 150) % 2L == 0L) {
                            val centerX = p.x
                            val centerY = p.y - cameraY
                            val s = p.size * 1.5f
                            drawLine(p.color.copy(alpha = alpha), Offset(centerX - s, centerY), Offset(centerX + s, centerY), strokeWidth = 2.5f)
                            drawLine(p.color.copy(alpha = alpha), Offset(centerX, centerY - s), Offset(centerX, centerY + s), strokeWidth = 2.5f)
                            // Diagonal sparkle bits for premium feel
                            val ds = s * 0.5f
                            drawLine(p.color.copy(alpha = alpha * 0.5f), Offset(centerX - ds, centerY - ds), Offset(centerX + ds, centerY + ds), strokeWidth = 1.5f)
                            drawLine(p.color.copy(alpha = alpha * 0.5f), Offset(centerX + ds, centerY - ds), Offset(centerX - ds, centerY + ds), strokeWidth = 1.5f)
                        } else {
                            drawCircle(p.color.copy(alpha = alpha), radius = p.size, center = Offset(p.x, p.y - cameraY)) 
                        }
                    }
                    landingEffects.forEach { effect ->
                        val progress = 1f - (effect.life / 0.5f).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color.Cyan.copy(alpha = 0.3f * (1f - progress)),
                            radius = 40f * progress,
                            center = Offset(effect.x, effect.y - cameraY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }

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
                    threatManager.activeThreats.forEach { threat ->
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
                                    // Massive Storm Cloud (Multiple layers for depth)
                                    repeat(3) { i ->
                                        val offX = (i - 1) * 80f
                                        val offY = if (i == 1) -20f else 20f
                                        drawCircle(
                                            color = Color.DarkGray.copy(alpha = 0.9f * alpha),
                                            radius = 160f - i * 20f,
                                            center = Offset(tx + offX, ty + offY)
                                        )
                                    }
                                    // Internal Glow
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color.Yellow.copy(alpha = 0.3f * alpha), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 200f
                                        ),
                                        radius = 200f,
                                        center = Offset(tx, ty)
                                    )

                                    // Strike logic (using scanPulse as sub-phase)
                                    if (threat.scanPulse > 0 && threat.scanPulse < 1.0f && threat.phase == 2) {
                                        // Telegraph: Pulse
                                        val tAlpha = (kotlin.math.sin(gameTime / 100f) * 0.3f + 0.4f) * alpha
                                        drawCircle(
                                            color = Color.Yellow.copy(alpha = tAlpha),
                                            radius = 180f * threat.scanPulse,
                                            center = Offset(tx, ty),
                                            style = Stroke(width = 4f)
                                        )
                                    } else if (threat.scanPulse == 1.0f && threat.phase == 2) {
                                        // Striking: Jagged Bolt
                                        val boltPath = Path().apply {
                                            moveTo(tx, ty)
                                            var curY = ty
                                            var curX = tx
                                            repeat(5) {
                                                val nextY = curY + 60f
                                                val nextX = curX + (Random.nextFloat() - 0.5f) * 100f
                                                lineTo(nextX, nextY)
                                                curX = nextX
                                                curY = nextY
                                            }
                                        }
                                        drawPath(boltPath, Color.Yellow.copy(alpha = alpha), style = Stroke(width = 12f, cap = StrokeCap.Round))
                                        drawPath(boltPath, Color.White.copy(alpha = alpha), style = Stroke(width = 4f, cap = StrokeCap.Round))
                                        
                                        // Impact Flash
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.6f * alpha),
                                            radius = 100f,
                                            center = Offset(tx, ty + 250f)
                                        )
                                    }
                                }
                                "HAZ_DEBRIS" -> {
                                    val alpha = lifeCycleAlpha
                                    val sizeMult = 2.0f // Task 4: Larger physical wreckage
                                    rotate(threat.rotation, pivot = Offset(tx, ty)) {
                                        val debrisColor = if (random.nextBoolean()) Color(0xFF757575) else Color(0xFF424242)
                                        
                                        // Task 4: Specific wreckage shapes (Satellite Panels, broken antennas)
                                        when (threat.instanceId.hashCode() % 3) {
                                            0 -> { // Solar Array Wing
                                                drawRect(Color.DarkGray.copy(alpha = alpha), Offset(tx - 60f, ty - 20f), Size(120f, 40f))
                                                drawRect(Color(0xFF3F51B5).copy(alpha = 0.4f * alpha), Offset(tx - 55f, ty - 15f), Size(110f, 30f), style = Stroke(width = 2f))
                                                drawLine(Color.White.copy(alpha = 0.2f * alpha), Offset(tx - 60f, ty), Offset(tx + 60f, ty))
                                            }
                                            1 -> { // Hull Plating
                                                val path = Path().apply {
                                                    moveTo(tx - 40f, ty - 40f)
                                                    lineTo(tx + 50f, ty - 30f)
                                                    lineTo(tx + 40f, ty + 50f)
                                                    lineTo(tx - 30f, ty + 40f)
                                                    close()
                                                }
                                                drawPath(path, debrisColor.copy(alpha = alpha))
                                                drawPath(path, Color.White.copy(alpha = 0.1f * alpha), style = Stroke(width = 2f))
                                            }
                                            else -> { // Structural Truss
                                                drawRect(debrisColor.copy(alpha = alpha), Offset(tx - 10f, ty - 60f), Size(20f, 120f))
                                                repeat(4) { j ->
                                                    drawLine(Color.White.copy(alpha = 0.3f * alpha), Offset(tx-10f, ty-60f+j*30f), Offset(tx+10f, ty-30f+j*30f))
                                                }
                                            }
                                        }
                                    }
                                }
                                "HAZ_RADIATION" -> {
                                    val alpha = lifeCycleAlpha
                                    val pulse = threat.scanPulse
                                    // Task 7: Move part of radiation to background feel
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
                                    // Massive Plasma Front
                                    val flareHeight = 600f // Task 3: Increased scale
                                    drawRect(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent, 
                                                Color(0xFFFFEA00).copy(alpha = 0.9f * alpha), 
                                                Color(0xFFFF3D00).copy(alpha = 0.6f * alpha), 
                                                Color.Transparent
                                            ),
                                            startY = ty - flareHeight / 2,
                                            endY = ty + flareHeight / 2
                                        ),
                                        topLeft = Offset(0f, ty - flareHeight / 2),
                                        size = Size(screenWidth, flareHeight)
                                    )
                                    // Arcs within flare
                                    repeat(8) { i ->
                                        val rx = (gameTime / 1.5f + i * 200f) % screenWidth
                                        drawArc(
                                            color = Color.White.copy(alpha = 0.3f * alpha),
                                            startAngle = 0f,
                                            sweepAngle = 180f,
                                            useCenter = false,
                                            topLeft = Offset(rx - 150f, ty - 80f),
                                            size = Size(300f, 160f),
                                            style = Stroke(width = 6f)
                                        )
                                    }
                                }
                                "HAZ_TURBULENCE" -> {
                                    val alpha = lifeCycleAlpha
                                    // Task 4: Atmospheric Distortion (Wind streaks - Force Focus)
                                    val windDir = if (threat.instanceId.hashCode() % 2 == 0) 1f else -1f
                                    
                                    // Background Pressure Waves (Task 7)
                                    repeat(3) { i ->
                                        val p = (threat.scanPulse + i * 0.33f) % 1.0f
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.05f * (1f - p) * alpha),
                                            radius = 500f * p,
                                            center = Offset(tx, ty),
                                            style = Stroke(width = 2f)
                                        )
                                    }

                                    repeat(25) { i ->
                                        val r = Random(threat.instanceId.hashCode() + i)
                                        val rx = tx + (r.nextFloat() - 0.5f) * 800f
                                        val ry = ty + (r.nextFloat() - 0.5f) * 800f
                                        val progress = (threat.scanPulse + r.nextFloat()) % 1.0f
                                        val lx = rx + (progress * 500f * windDir)
                                        
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.3f * (1f - progress) * alpha),
                                            start = Offset(lx, ry),
                                            end = Offset(lx + 120f * windDir, ry),
                                            strokeWidth = 8f, // Thicker, softer wind
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                                "HAZ_GRAVITY" -> {
                                    val alpha = lifeCycleAlpha
                                    val pulse = threat.scanPulse
                                    // Lensing Effect (Simulated with circles)
                                    repeat(4) { i ->
                                        val radius = (100f + i * 150f + pulse * 100f)
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.15f * (1f - pulse) * alpha),
                                            radius = radius,
                                            center = Offset(tx, ty),
                                            style = Stroke(width = 2f + i)
                                        )
                                    }
                                    // Black Hole Core
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.8f * alpha),
                                        radius = 60f,
                                        center = Offset(tx, ty)
                                    )
                                    // Space Warping (Inner Ring)
                                    drawCircle(
                                        color = Color.Cyan.copy(alpha = 0.4f * alpha),
                                        radius = 70f + (kotlin.math.sin(gameTime / 100f) * 10f),
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 4f)
                                    )
                                }
                                "HAZ_EMP" -> {
                                    val alpha = lifeCycleAlpha
                                    val ringRadius = threat.scanPulse * 600f // Larger shockwave
                                    val ringAlpha = (1f - (threat.scanPulse / 3.0f)).coerceIn(0f, 1f) * alpha
                                    
                                    // Main Shockwave
                                    drawCircle(
                                        color = Color.Cyan.copy(alpha = ringAlpha),
                                        radius = ringRadius,
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 15f)
                                    )
                                    // Secondary Echoes
                                    drawCircle(
                                        color = Color.White.copy(alpha = ringAlpha * 0.5f),
                                        radius = ringRadius * 0.9f,
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 5f)
                                    )
                                    // Electronic Interference (Glitches)
                                    repeat(5) { i ->
                                        val r = Random(gameTime / 100 + i)
                                        if (r.nextFloat() > 0.5f) {
                                            val gx = tx + (r.nextFloat() - 0.5f) * ringRadius * 2
                                            val gy = ty + (r.nextFloat() - 0.5f) * ringRadius * 2
                                            drawRect(Color.Cyan.copy(alpha = 0.4f * ringAlpha), Offset(gx, gy), Size(30f, 2f))
                                        }
                                    }
                                }
                                "HAZ_GUST", "HAZ_CROSSWIND", "HAZ_THERMAL" -> {
                                    // Compatibility
                                }
                                "ENT_SCOUT_DRONE" -> {
                                    // Render Surveyor Probe
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val isTracking = threat.isTracking

                                    val flicker = Random(gameTime / 50).nextFloat() * 10f
                                    val glowColor = if (isTracking) Color.Red else Color.White
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(glowColor.copy(alpha = 0.4f), Color.Transparent),
                                            center = Offset(tx, ty + 20f),
                                            radius = 30f + flicker
                                        ),
                                        radius = 30f + flicker,
                                        center = Offset(tx, ty + 20f)
                                    )

                                    drawRect(
                                        color = if (isTracking) Color(0xFFB71C1C) else Color(0xFF455A64),
                                        topLeft = Offset(tx - 20f, ty - 15f),
                                        size = Size(40f, 30f)
                                    )

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
                                                    colors = listOf(Color.Red.copy(alpha = 0.3f * (1f - threat.scanPulse)), Color.Transparent),
                                                    startX = tx,
                                                    endX = tx + 250f
                                                )
                                            )
                                        }
                                    }

                                    val eyePulse = (sin(gameTime / 150f) * 0.5f + 0.5f)
                                    val eyeColor = if (isTracking) Color.Red else Color.Cyan
                                    val eyeRadius = if (isTracking) 12f else 8f

                                    drawCircle(
                                        color = eyeColor.copy(alpha = 0.6f + eyePulse * 0.4f),
                                        radius = eyeRadius,
                                        center = Offset(tx, ty)
                                    )

                                    drawLine(
                                        color = Color.LightGray,
                                        start = Offset(tx - 20f, ty),
                                        end = Offset(tx - 35f, ty - 10f),
                                        strokeWidth = 3f
                                    )
                                    drawLine(
                                        color = Color.LightGray,
                                        start = Offset(tx + 20f, ty),
                                        end = Offset(tx + 35f, ty - 10f),
                                        strokeWidth = 3f
                                    )
                                }
                                "ENT_SWARM_BOTS" -> {
                                    // Render Aerosol Swarm
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 300f) * 0.2f + 0.8f)

                                    repeat(12) { i ->
                                        val randomPart = Random(threat.instanceId.hashCode() + i)
                                        val ox = (sin(gameTime / 500f + i) * 35f * pulse) + (randomPart.nextFloat() * 15f)
                                        val oy = (cos(gameTime / 400f + i * 1.5f) * 35f * pulse) + (randomPart.nextFloat() * 15f)

                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.3f),
                                            radius = 2f + randomPart.nextFloat() * 4f,
                                            center = Offset(tx + ox, ty + oy)
                                        )

                                        if (randomPart.nextFloat() < 0.05f) {
                                            drawCircle(
                                                color = Color.Cyan.copy(alpha = 0.6f),
                                                radius = 1f,
                                                center = Offset(tx + ox, ty + oy)
                                            )
                                        }
                                    }
                                }
                                "ENT_CLOUD_SKIMMER" -> {
                                    // Render Sky Ray
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val distSq = (player.x - tx) * (player.x - tx) + (player.y - cameraY - ty) * (player.y - cameraY - ty)
                                    val isNear = distSq < 250f * 250f

                                    val dir = if (threat.vx > 0) 1f else -1f
                                    val wingSpan = 80f
                                    val flap = sin(gameTime / 600f) * 20f

                                    if (isNear) {
                                        repeat(3) { i ->
                                            val slipX = tx + (Random.nextFloat() - 0.5f) * 40f
                                            val slipY = ty + i * 20f
                                            drawLine(
                                                color = Color.Cyan.copy(alpha = 0.3f),
                                                start = Offset(slipX, slipY),
                                                end = Offset(slipX, slipY - 60f),
                                                strokeWidth = 2f
                                            )
                                        }
                                    }

                                    val rayPath = Path().apply {
                                        moveTo(tx - 40f * dir, ty)
                                        quadraticTo(tx, ty - 10f, tx + 40f * dir, ty)
                                        moveTo(tx, ty - 5f)
                                        lineTo(tx - 20f * dir, ty + flap)
                                        lineTo(tx - wingSpan * dir, ty + flap * 1.5f)
                                        lineTo(tx - 20f * dir, ty + 15f)
                                        close()
                                    }
                                    drawPath(rayPath, Color(0xFFB3E5FC).copy(alpha = 0.4f))
                                    drawPath(rayPath, Color.White.copy(alpha = 0.2f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))

                                    repeat(3) { i ->
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.1f),
                                            radius = 5f - i,
                                            center = Offset(tx - (60f + i * 20f) * dir, ty + sin(gameTime / 400f + i) * 10f)
                                        )
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
                                    // Render Orbital Sentry
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val rot = threat.rotation
                                    rotate(rot, pivot = Offset(tx, ty)) {
                                        drawRect(Color(0xFF37474F), topLeft = Offset(tx - 25f, ty - 25f), size = Size(50f, 50f))
                                        drawRect(Color.Gray.copy(alpha = 0.5f), topLeft = Offset(tx - 25f, ty - 25f), size = Size(50f, 50f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                                        drawCircle(Color.Cyan.copy(alpha = 0.3f), radius = 15f, center = Offset(tx, ty))
                                    }
                                    val pulseRadius = 150f * threat.scanPulse
                                    drawCircle(
                                        color = Color.Cyan.copy(alpha = 0.2f * (1f - threat.scanPulse)),
                                        radius = pulseRadius,
                                        center = Offset(tx, ty),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                    )
                                }
                                "ENT_CORRUPTED_HULL" -> {
                                    // Render Derelict Echo
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    rotate(threat.rotation, pivot = Offset(tx, ty)) {
                                        drawRect(Color.DarkGray.copy(alpha = 0.4f), topLeft = Offset(tx - 15f, ty - 25f), size = Size(20f, 40f))
                                        drawRect(Color.DarkGray.copy(alpha = 0.3f), topLeft = Offset(tx + 5f, ty - 10f), size = Size(15f, 15f))
                                    }
                                    val pulseAlpha = 0.5f * (1f - threat.scanPulse)
                                    drawCircle(
                                        color = Color.Green.copy(alpha = pulseAlpha),
                                        radius = 5f + 40f * threat.scanPulse,
                                        center = Offset(tx, ty),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                                    )
                                }
                                "HAZ_VOID_ANOMALY" -> {
                                    // Render Void Anomaly
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 1000f) * 0.3f + 0.7f)
                                    drawCircle(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(Color(0xFFE91E63).copy(alpha = 0.4f * pulse), Color.Transparent),
                                            center = Offset(tx, ty),
                                            radius = 100f
                                        ),
                                        radius = 100f,
                                        center = Offset(tx, ty)
                                    )
                                    repeat(3) { i ->
                                        val ringPulse = (threat.scanPulse + i * 0.33f) % 1f
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.1f * (1f - ringPulse)),
                                            radius = 20f + 120f * ringPulse,
                                            center = Offset(tx, ty),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                                        )
                                    }
                                }
                                "MINI_BOSS_COMMANDER" -> {
                                    // Render Command Cruiser (Visual Identity Upgrade)
                                    val tx = threat.x
                                    val ty = threat.y - cameraY
                                    val phase = threat.phase

                                    // 1. Shadow (if arrival)
                                    if (threat.arrivalTimer < threat.arrivalDuration) {
                                        drawRect(Color.Black.copy(alpha = 0.2f), topLeft = Offset(0f, 0f), size = size)
                                    }

                                    // 2. Main Chassis
                                    drawRect(Color(0xFF263238), topLeft = Offset(tx - 150f, ty - 60f), size = Size(300f, 120f))
                                    drawRect(Color.Gray.copy(alpha = 0.5f), topLeft = Offset(tx - 150f, ty - 60f), size = Size(300f, 120f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))

                                    // 3. Bridge / Command Tower
                                    drawRect(Color(0xFF37474F), topLeft = Offset(tx - 40f, ty - 100f), size = Size(80f, 40f))
                                    drawRect(Color.Cyan.copy(alpha = 0.3f), topLeft = Offset(tx - 30f, ty - 90f), size = Size(60f, 10f))

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

                                    // 6. Hull Lights (Flashing)
                                    if ((gameTime / 500) % 2 == 0L) {
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
                                                colors = listOf(Color.Cyan.copy(alpha = 0.5f), Color.Transparent),
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
                                            drawRect(Color.Magenta, topLeft = Offset(wx - 10f, wy - 10f), size = Size(20f, 20f))
                                            drawRect(Color.White, topLeft = Offset(wx - 10f, wy - 10f), size = Size(20f, 20f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                                        } else {
                                            // Destroyed state debris
                                            if (Random.nextFloat() < 0.1f) {
                                                spawnBurst(wx, wy + cameraY, 5, SciFiBorder, 50f)
                                            }
                                        }
                                    }

                                    // Gravity Pulse Visual
                                    if (threat.pulseAlpha > 0) {
                                        val pulseScale = 1f - threat.pulseAlpha
                                        drawCircle(
                                            color = Color.White.copy(alpha = threat.pulseAlpha * 0.6f),
                                            radius = pulseScale * 1200f,
                                            center = Offset(tx, ty),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                                        )
                                    }
                                }
                                "BOSS_GATEKEEPER" -> {
                                    // Render Gatekeeper (Dramatic Arrival & Identity)
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val arrivalProgress = (threat.arrivalTimer / threat.arrivalDuration).coerceIn(0f, 1f)
                                    val phase = threat.phase

                                    // Dim background
                                    drawRect(Color.Black.copy(alpha = 0.4f * arrivalProgress), topLeft = Offset(0f, 0f), size = size)

                                    rotate(threat.rotation, pivot = Offset(tx, ty)) {
                                        // Massive Orbital Ring
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.8f * arrivalProgress),
                                            radius = 250f,
                                            center = Offset(tx, ty),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f)
                                        )
                                        // Safe Gaps (Visualized as massive energy barriers)
                                        repeat(4) { i ->
                                            val isWeakPointDestroyed = i >= threat.activeWeakPoints
                                            rotate(i * 90f, pivot = Offset(tx, ty)) {
                                                drawArc(
                                                    color = if (isWeakPointDestroyed) Color.Red.copy(alpha = 0.2f) else Color.Cyan.copy(alpha = 0.7f),
                                                    startAngle = 5f,
                                                    sweepAngle = 80f,
                                                    useCenter = false,
                                                    topLeft = Offset(tx - 250f, ty - 250f),
                                                    size = Size(500f, 500f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 80f)
                                                )
                                                // High contrast unsafe edge
                                                drawArc(
                                                    color = Color.Red.copy(alpha = 0.5f),
                                                    startAngle = 5f,
                                                    sweepAngle = 10f,
                                                    useCenter = false,
                                                    topLeft = Offset(tx - 250f, ty - 250f),
                                                    size = Size(500f, 500f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 90f)
                                                )
                                                // Weak Point Node
                                                if (!isWeakPointDestroyed) {
                                                    drawCircle(Color.Magenta, radius = 25f, center = Offset(tx + 250f, ty))
                                                    drawCircle(Color.White, radius = 10f, center = Offset(tx + 250f, ty))
                                                }
                                            }
                                        }
                                    }
                                    // Central Eye
                                    val eyePulse = (sin(gameTime / 200f) * 0.2f + 0.8f)
                                    val eyeColor = if (phase == 3) Color.Yellow else Color.Red
                                    drawCircle(eyeColor.copy(alpha = 0.9f * eyePulse), radius = 40f * arrivalProgress, center = Offset(tx, ty))
                                    drawCircle(Color.White, radius = 15f * arrivalProgress, center = Offset(tx, ty))
                                }
                                "BOSS_STAR_EATER" -> {
                                    // Render Star Eater
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val pulse = (sin(gameTime / 400f) * 0.1f + 0.9f)
                                    val phase = threat.phase

                                    // Sucking Aura (AMPLIFIED)
                                    val auraRadius = if (phase == 3) 1000f else 800f
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

                                    // Dark Core
                                    drawCircle(Color.Black, radius = 120f * pulse, center = Offset(tx, ty))

                                    // Core Eye (Weak Point)
                                    if (threat.activeWeakPoints > 0) {
                                        drawCircle(Color.Magenta.copy(alpha = 1.0f), radius = 50f * pulse, center = Offset(tx, ty))
                                        drawCircle(Color.White, radius = 15f, center = Offset(tx, ty))
                                    }

                                    // Tendrils (Now with suction physics appearance)
                                    repeat(12) { i ->
                                        val angle = i * 30f + sin(gameTime / 300f + i) * 40f
                                        rotate(angle, pivot = Offset(tx, ty)) {
                                            drawLine(
                                                if (phase == 3) Color.Red else Color(0xFFBA68C8),
                                                Offset(tx + 80f, ty),
                                                Offset(tx + 400f, ty),
                                                strokeWidth = 15f * pulse
                                            )
                                        }
                                    }
                                }
                                "BOSS_VOID_ENGINE" -> {
                                    // Render Void Engine
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

                                    rotate(rot, pivot = Offset(tx, ty)) {
                                        repeat(3) { i ->
                                            rotate(i * 120f, pivot = Offset(tx, ty)) {
                                                drawRect(Color(0xFF880E4F), topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f))
                                                drawRect(Color.White, topLeft = Offset(tx - 40f, ty - 200f), size = Size(80f, 400f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))

                                                // Weak Points on the arms
                                                if (i < threat.activeWeakPoints) {
                                                    drawCircle(Color.Magenta, radius = 20f, center = Offset(tx, ty - 150f))
                                                }
                                            }
                                        }
                                    }
                                    // Energy Arcs
                                    if ((gameTime / 80) % (if(phase == 3) 2 else 4) == 0L) {
                                        drawLine(Color.White, Offset(tx, ty), Offset(tx + (Random.nextFloat() - 0.5f) * 600f, ty + (Random.nextFloat() - 0.5f) * 600f), strokeWidth = 5f)
                                    }

                                    // Shift Direction Indicators (Large Arrows)
                                    if (threat.phase >= 2 && (threat.localTimer.toInt() % 4 < 2)) {
                                        val shiftDir = if ((threat.localTimer.toInt() / 4) % 2 == 0) 1f else -1f
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
                                    }
                                }
                                "BOSS_LEVIATHAN" -> {
                                    // Render Leviathan (Segmented body)
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val phase = threat.phase

                                    repeat(6) { i ->
                                        val ox = sin(gameTime / 1000f - i * 0.5f) * 100f
                                        val oy = i * 60f
                                        val segmentPulse = (sin(gameTime / 500f + i) * 0.2f + 0.8f)

                                        // Color shifts in phase 3
                                        val bodyColor = if (phase == 3) Color(0xFF1A237E) else Color(0xFF01579B)

                                        drawCircle(
                                            bodyColor,
                                            radius = (80f - i * 10f) * segmentPulse,
                                            center = Offset(tx + ox, ty + oy)
                                        )

                                        // Slipstream Wind Indicators (Behind each segment)
                                        repeat(4) { j ->
                                            val windX = tx + ox + (Random.nextFloat() - 0.5f) * 60f
                                            val windY = ty + oy + 40f + (j * 40f)
                                            drawLine(
                                                color = Color.Cyan.copy(alpha = 0.4f),
                                                start = Offset(windX, windY),
                                                end = Offset(windX, windY + 60f),
                                                strokeWidth = 3f
                                            )
                                        }

                                        // Weak Points (On segments 1, 3, 5)
                                        val wpIndex = i / 2
                                        if (i % 2 == 0 && wpIndex < threat.activeWeakPoints) {
                                            drawCircle(Color.Magenta, radius = 30f * segmentPulse, center = Offset(tx + ox, ty + oy))
                                            drawCircle(Color.White, radius = 10f, center = Offset(tx + ox, ty + oy))
                                        }

                                        // Bioluminescent Veins
                                        drawCircle(
                                            Color.Cyan.copy(alpha = 0.5f),
                                            radius = (40f - i * 5f) * segmentPulse,
                                            center = Offset(tx + ox, ty + oy),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                        )
                                    }
                                }
                                "BOSS_SIGNAL" -> {
                                    // Render The Signal
                                    val tx = threat.x; val ty = threat.y - cameraY
                                    val flicker = if (Random.nextFloat() < (if (threat.phase == 3) 0.3f else 0.1f)) 0f else 1f

                                    if (flicker > 0) {
                                        repeat(15) { i ->
                                            val rx = tx + (Random.nextFloat() - 0.5f) * 400f
                                            val ry = ty + (Random.nextFloat() - 0.5f) * 400f
                                            drawRect(
                                                if (threat.phase == 3) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f),
                                                topLeft = Offset(rx, ry),
                                                size = Size(Random.nextFloat() * 60f, Random.nextFloat() * 60f)
                                            )
                                        }

                                        // Weak point (Glitching node)
                                        if (threat.activeWeakPoints > 0) {
                                            drawCircle(Color.Magenta.copy(alpha = 0.6f), radius = 50f, center = Offset(tx, ty))
                                        }

                                        drawCircle(Color.White.copy(alpha = 0.1f * threat.scanPulse), radius = 600f, center = Offset(tx, ty))
                                    }
                                }
                            }
                            }
                        }
                    }

                    powerUps.forEach { pu ->
                        val baseColor = when (pu.type) {
                            PowerUpType.FUEL_TANK -> Color(0xFFE57373)
                            PowerUpType.TURBO_BOOSTER -> Color.Cyan
                            PowerUpType.EFFICIENCY_MODULE -> Color.Green
                            PowerUpType.HEAT_SINK -> Color.White
                            PowerUpType.ARTIFACT -> Color(0xFF9C27B0)
                            PowerUpType.ALTITUDE_BOOSTER -> Color.White
                            PowerUpType.SHIELD_CAPSULE -> SciFiCyan
                            PowerUpType.HULL_REPAIR -> SciFiGreen
                        }
                        if (pu.type == PowerUpType.ARTIFACT) { drawCircle(baseColor, radius = 15f, center = Offset(pu.x, pu.y - cameraY)); drawCircle(Color.White, radius = 5f, center = Offset(pu.x, pu.y - cameraY)) }
                        else if (pu.type == PowerUpType.SHIELD_CAPSULE || pu.type == PowerUpType.HULL_REPAIR) {
                            // Survival Capsules
                            drawCircle(baseColor, radius = 18f, center = Offset(pu.x, pu.y - cameraY))
                            drawCircle(Color.White.copy(alpha = 0.6f), radius = 22f, center = Offset(pu.x, pu.y - cameraY), style = Stroke(width = 2f))
                            if ((gameTime / 200) % 2 == 0L) {
                                drawCircle(Color.White, radius = 5f, center = Offset(pu.x, pu.y - cameraY))
                            }
                        }
                        else { drawRect(baseColor, topLeft = Offset(pu.x - 12f, pu.y - cameraY - 15f), size = Size(24f, 30f)); drawRect(Color.DarkGray, topLeft = Offset(pu.x - 6f, pu.y - cameraY - 20f), size = Size(12f, 5f)) }
                    }

                    // Render Flying Rewards
                    flyingRewards.forEach { fr ->
                        val curX = fr.x * (1f - fr.progress) + fr.targetX * fr.progress
                        val curY = fr.y * (1f - fr.progress) + fr.targetY * fr.progress

                        val baseColor = when (val t = fr.type) {
                            is ComboReward.Fuel -> Color.Green
                            is ComboReward.PowerUp -> when(t.type) {
                                PowerUpType.HULL_REPAIR -> SciFiGreen
                                PowerUpType.SHIELD_CAPSULE -> SciFiCyan
                                else -> SciFiCyan
                            }
                            is ComboReward.AltitudeBoost -> Color.White
                            is ComboReward.Artifact -> Color(0xFF9C27B0)
                        }

                        scale(fr.scale, pivot = Offset(curX, curY)) {
                            drawCircle(baseColor, radius = 15f, center = Offset(curX, curY))
                            drawCircle(Color.White, radius = 5f, center = Offset(curX, curY))
                        }
                    }

                    // Render Rocket via RocketRenderer
                    rocketRenderer.render(
                        drawScope = this,
                        player = player,
                        isThrusting = isThrusting,
                        thrustTarget = thrustTarget,
                        cameraY = cameraY,
                        gameTime = gameTime
                    )
                }

                if (impactFlashAlpha > 0) {
                    drawRect(
                        color = Color.White.copy(alpha = impactFlashAlpha),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f)
                    )
                }
            }

        // --- Screens ---

        when (gameState) {
            GameState.TITLE -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.safeDrawingPadding()) {
                        val infiniteTransition = rememberInfiniteTransition(label = "TitleTransition")
                        val glowAlpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "GlowAlpha")
                        val rocketOffset by infiniteTransition.animateFloat(0f, -20f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "RocketOffset")
                        Canvas(Modifier.size(100.dp).offset { androidx.compose.ui.unit.IntOffset(0, rocketOffset.dp.roundToPx()) }) {
                            val rx = size.width/2; val ry = size.height/2
                            drawRect(SciFiWhite.copy(alpha = 0.8f), topLeft = Offset(rx-10f, ry-15f), size = Size(20f, 40f))
                            drawPath(Path().apply { moveTo(rx-10f, ry-15f); lineTo(rx, ry-30f); lineTo(rx+10f, ry-15f); close() }, SciFiRed)
                            drawPath(Path().apply { moveTo(rx-5f, ry+25f); lineTo(rx, ry+40f+Random.nextFloat()*10f); lineTo(rx+5f, ry+25f); close() }, SciFiGold)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "JUMP DROID",
                            style = MaterialTheme.typography.displayLarge.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(SciFiCyan.copy(alpha = glowAlpha), Offset(0f, 0f), 20f),
                                letterSpacing = 8.sp
                            ),
                            color = SciFiWhite,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(80.dp))
                        Button(
                            onClick = { gameState = GameState.MAIN_MENU },
                            modifier = Modifier.width(240.dp).height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                        ) {
                            Text("INITIATE ASCENT", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("THE ASCENSION PROGRAM // EST. 1984", color = SciFiWhite.copy(alpha = 0.3f), letterSpacing = 1.sp, fontSize = 10.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("POWERED BY ASHWATH.AI // V1.2.0", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp)
                    }
                }
            }
            GameState.MAIN_MENU -> {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.safeDrawingPadding().width(280.dp)
                    ) {
                        Text(
                            text = "COMMAND CENTER",
                            style = MaterialTheme.typography.headlineLarge,
                            color = SciFiWhite,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                        Spacer(Modifier.height(48.dp))
                        
                        val menuButtons = listOf(
                            "LAUNCH" to { restartGame() },
                            "HANGAR" to { gameState = GameState.HANGAR },
                            "ARCHIVE" to { gameState = GameState.ARCHIVE },
                            "TERMINAL" to { gameState = GameState.LEADERBOARD },
                            "MISSION DATA" to { gameState = GameState.ABOUT },
                            "SETTINGS" to { gameState = GameState.SETTINGS }
                        )

                        menuButtons.forEach { (label, action) ->
                            Button(
                                onClick = action,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp), // Sharper corners for modern look
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SciFiSurface,
                                    contentColor = SciFiWhite
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = { (context as? android.app.Activity)?.finish() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SciFiRed.copy(alpha = 0.2f),
                                contentColor = SciFiRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.5f))
                        ) {
                            Text("ABORT MISSION", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                    }
                }
            }
            GameState.HANGAR -> {
                Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
                    Column(Modifier.padding(16.dp).safeDrawingPadding()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text("ROCKET HANGAR", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                Text(progressionManager.currentRank.title, color = SciFiGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val totalComp = progressionManager.getTotalCompletionPercentage()
                                Text("$totalComp% ARCHIVE COMPLETION", color = SciFiWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("${progressionManager.artifactsCollected.size} ARTIFACTS RECOVERED", color = SciFiPurple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // New: Hangar Progression Summary
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(SciFiSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, SciFiBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val (pFound, pTotal) = progressionManager.getCompletionStats("PLATFORMS")
                            val (zFound, zTotal) = progressionManager.getCompletionStats("AREAS")
                            val (aFound, aTotal) = progressionManager.getCompletionStats("ARTIFACTS")
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PLATFORMS", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                Text("$pFound/$pTotal", color = SciFiCyan, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ZONES", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                Text("$zFound/$zTotal", color = SciFiCyan, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ARTIFACTS", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                Text("$aFound/$aTotal", color = SciFiPurple, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { gameState = GameState.ARCHIVE }) {
                                Text("ARCHIVE", color = SciFiCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                Text("VIEW >", color = SciFiWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))

                        // EPIC 5: Survival Specs (Read-only for now)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(SciFiSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(1.dp, SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAX HULL", color = SciFiGreen.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("${progressionManager.permanentMaxIntegrity.toInt()}", color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAX SHIELD", color = SciFiCyan.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("${progressionManager.permanentMaxShield.toInt()}", color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("REGEN RATE", color = SciFiCyan.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Text("${Constants.SHIELD_REGEN_RATE.toInt()} U/S", color = SciFiWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            RocketType.entries.forEach { type ->
                                val unlocked = highScore >= type.unlockScore || sharedPrefs.getBoolean("unlock_${type.name}", false)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable(enabled = unlocked) { player.rocketType = type; gameState = GameState.MAIN_MENU }
                                        .border(
                                            width = 1.dp,
                                            color = if (player.rocketType == type) SciFiCyan else if (unlocked) SciFiBorder else SciFiBorder.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    color = if (player.rocketType == type) SciFiCyan.copy(alpha = 0.1f) else if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(Modifier.padding(20.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(type.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = if (unlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            if (!unlocked) Text("THRESHOLD: ${type.unlockScore}m", color = SciFiRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            else if (player.rocketType == type) Text("ACTIVE", color = SciFiCyan, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 2.sp)
                                        }
                                        if (unlocked) {
                                            Spacer(Modifier.height(12.dp))
                                            Text("THRUST: ${(type.thrustMult * 100).toInt()}% // FUEL: ${(type.fuelMult * 100).toInt()}% // THERMAL: ${(type.heatMult * 100).toInt()}%", color = SciFiCyan.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(4.dp))
                                            Text(type.discovery.description, color = SciFiWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { gameState = GameState.MAIN_MENU },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                        ) {
                            Text("BACK", color = SciFiWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            GameState.ARCHIVE -> {
                val categories = listOf("ROCKETS", "PLATFORMS", "POWERUPS", "AREAS", "THREATS", "ARTIFACTS", "LORE", "ACHIEVEMENTS")
                var selectedCat by remember { mutableStateOf("ROCKETS") }
                Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
                    Column(Modifier.padding(16.dp).safeDrawingPadding()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("DATA ARCHIVE", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            val discovered = DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) }
                            Text("$discovered RECORDS FOUND", color = SciFiWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
                            categories.forEach { cat -> 
                                Text(
                                    text = cat,
                                    modifier = Modifier
                                        .clickable { selectedCat = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .background(if (selectedCat == cat) SciFiCyan.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(4.dp)),
                                    color = if (selectedCat == cat) SciFiCyan else SciFiWhite.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            if (selectedCat == "ACHIEVEMENTS") {
                                AchievementsList.forEach { ach -> 
                                    val unlocked = sharedPrefs.getBoolean("achievement_${ach.id}", false)
                                    CodexCard(ach.title, if (unlocked) ach.description else "DATA ENCRYPTED: REACH OBJECTIVE TO UNLOCK.", "", unlocked) 
                                }
                            } else {
                                DiscoveryType.entries.filter { it.category == selectedCat }.forEach { entry ->
                                    val unlocked = discoveryManager.isDiscovered(entry)
                                    val title = if (unlocked || selectedCat != "THREATS") entry.title else "UNKNOWN SIGNAL"
                                    val desc = if (unlocked) entry.description else "DATA CORRUPTED: RECOVER DURING NEXT EXPEDITION."
                                    val lore = if (unlocked) entry.lore else ""
                                    
                                    if (unlocked && selectedCat == "ARTIFACTS") {
                                        val record = progressionManager.artifactsCollected[entry.name]
                                        val extraInfo = if (record != null) {
                                            "\n\nFIRST DISCOVERY: ${record.firstDiscoveryDate}\nRECOVERED: ${record.timesFound} TIMES\nHIGHEST ALT: ${record.highestAltitude}m\nZONE: ${record.zoneFound}"
                                        } else ""
                                        CodexCard(title, desc, lore + extraInfo, unlocked)
                                    } else {
                                        CodexCard(title, desc, lore, unlocked)
                                    }
                                }
                            }
                        }
                        
                        // Completion Summary
                        if (selectedCat != "ACHIEVEMENTS") {
                            val (found, total) = progressionManager.getCompletionStats(selectedCat)
                            val percent = if (total > 0) (found * 100) / total else 0
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                color = SciFiSurface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("$selectedCat DATABASE", style = MaterialTheme.typography.labelSmall, color = SciFiCyan)
                                        Text("$found / $total", style = MaterialTheme.typography.labelSmall, color = SciFiWhite)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = percent / 100f,
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = SciFiCyan,
                                        trackColor = SciFiSurface
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { gameState = GameState.MAIN_MENU },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                        ) {
                            Text("BACK", color = SciFiWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            GameState.SETTINGS -> {
                Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
                    Column(Modifier.padding(32.dp).safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SYSTEM SETTINGS", style = MaterialTheme.typography.headlineLarge, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(Modifier.height(48.dp))
                        Text("MASTER AUDIO", color = SciFiWhite.copy(alpha = 0.7f), letterSpacing = 1.sp)
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.width(200.dp).height(4.dp).background(SciFiSurface, CircleShape)) {
                            Box(Modifier.fillMaxWidth(0.8f).fillMaxHeight().background(SciFiCyan, CircleShape))
                        }
                        Spacer(Modifier.height(64.dp))
                        Button(
                            onClick = { sharedPrefs.edit { clear() }; highScore = 0; player.rocketType = RocketType.BALANCED; gameState = GameState.TITLE },
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiRed.copy(alpha = 0.2f), contentColor = SciFiRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("WIPE TELEMETRY DATA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { gameState = GameState.MAIN_MENU },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                        ) {
                            Text("RETURN", color = SciFiWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            GameState.ABOUT -> {
                Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
                    Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState()).safeDrawingPadding()) {
                        Text("MISSION DATA", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = "Jump Droid is an advanced vertical exploration simulator focusing on precision propulsion control and multi-layered atmospheric discovery.",
                            color = SciFiWhite.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(24.dp))
                        Text("SYSTEM ARCHITECTURE: JETPACK COMPOSE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text("CORE ENGINE: ASHWATH.AI PROTOTYPE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                        Spacer(Modifier.height(32.dp))
                        Text("PROTOCOL VERSION", style = MaterialTheme.typography.titleLarge, color = SciFiGold, fontWeight = FontWeight.Bold)
                        Text("V1.2.0 // ASCENSION EXPANSION", color = SciFiWhite.copy(alpha = 0.6f))
                        Spacer(Modifier.height(32.dp))
                        Text("PROJECT ROADMAP", style = MaterialTheme.typography.titleLarge, color = SciFiGold, fontWeight = FontWeight.Bold)
                        Text("• ADVANCED ENTITY AI\n• DEEP SPACE ANOMALIES\n• INTERSTELLAR TERMINAL", color = SciFiWhite.copy(alpha = 0.6f), lineHeight = 24.sp)
                        Spacer(Modifier.height(48.dp))
                        Button(
                            onClick = { gameState = GameState.MAIN_MENU },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                        ) {
                            Text("DISMISS", color = SciFiWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            GameState.LEADERBOARD -> {
                Box(Modifier.fillMaxSize().background(SciFiBackground).safeDrawingPadding(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(300.dp)) {
                        Text("GLOBAL TERMINAL", style = MaterialTheme.typography.headlineMedium, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(Modifier.height(48.dp))
                        Surface(
                            color = SciFiSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CONNECTION STATUS", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("OFFLINE", color = SciFiRed, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                                Spacer(Modifier.height(24.dp))
                                Text("Worldwide rankings currently unavailable due to atmospheric interference.", color = SciFiWhite.copy(alpha = 0.6f), textAlign = TextAlign.Center, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(48.dp))
                        Button(
                            onClick = { gameState = GameState.MAIN_MENU },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                        ) {
                            Text("DISCONNECT", color = SciFiWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            GameState.PLAYING, GameState.GAMEOVER, GameState.TUTORIAL, GameState.PAUSED, GameState.HELP, GameState.UNLOCK -> {
                Box(Modifier.fillMaxSize()) {
                    // Top-Right Utility Buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .statusBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { gameState = GameState.HELP; isThrusting = false },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = CircleShape
                        ) { Text("?", fontWeight = FontWeight.Bold) }

                        if (gameState == GameState.PLAYING) {
                            Button(
                                onClick = { gameState = GameState.PAUSED; isThrusting = false },
                                modifier = Modifier.size(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = CircleShape
                            ) { Text("||", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        }
                    }

                    // --- NEW REFACTORED HUD ---

                    // 1. TOP CENTER: Altitude
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                shadow = androidx.compose.ui.graphics.Shadow(SciFiCyan.copy(alpha = 0.3f), blurRadius = 15f)
                            ),
                            color = SciFiWhite
                        )
                        Text(
                            text = "ASCENSION DATA: BEST $highScore",
                            style = MaterialTheme.typography.labelSmall,
                            color = SciFiWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    // --- REFACTORED HUD (Adjustment Run 1) ---

                    // 1. TOP CENTER: Altitude
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                shadow = androidx.compose.ui.graphics.Shadow(SciFiCyan.copy(alpha = 0.3f), blurRadius = 15f)
                            ),
                            color = SciFiWhite
                        )
                        Text(
                            text = "ASCENSION DATA: BEST $highScore",
                            style = MaterialTheme.typography.labelSmall,
                            color = SciFiWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    // 2. LEFT SIDE: Resource Cluster (Stacked Vertically)
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .graphicsLayer(alpha = 0.85f), // Overall transparency
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Fuel Meter
                        val fuelGaugeHeight = (120f + (player.maxFuel - 100f) * 0.6f).coerceIn(100f, 250f).dp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "⛽",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (player.fuel < 20f) (gameTime / 200 % 2).toFloat() else 0.8f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(6.dp) // Narrowed by 50%
                                    .height(fuelGaugeHeight)
                                    .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                    .border(0.5.dp, SciFiBorder.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                val fuelRatio = (player.fuel / player.maxFuel).coerceIn(0f, 1f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))) }) {
                                        val fillHeight = size.height * fuelRatio
                                        val waveOffset = (gameTime / 300f) % (2 * PI.toFloat())
                                        val path = Path().apply {
                                            moveTo(0f, size.height - fillHeight)
                                            for (x in 0..size.width.toInt()) {
                                                val y = (size.height - fillHeight) + sin(x / 4f + waveOffset) * 2f
                                                lineTo(x.toFloat(), y)
                                            }
                                            lineTo(size.width, size.height)
                                            lineTo(0f, size.height)
                                            close()
                                        }
                                        drawPath(path = path, color = (if (player.fuel > player.maxFuel * 0.25f) SciFiGreen else SciFiRed).copy(alpha = 0.9f))
                                    }
                                }
                            }
                        }

                        // Heat Meter
                        val heatGaugeHeight = (120f + (player.maxHeat - 100f) * 0.6f).coerceIn(100f, 250f).dp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (player.isOverheated) "⚠️" else "🔥",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (player.isOverheated) (gameTime / 150 % 2).toFloat() else 0.8f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(6.dp) // Narrowed by 50%
                                    .height(heatGaugeHeight)
                                    .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                    .border(0.5.dp, SciFiBorder.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                val heatRatio = (player.heat / player.maxHeat).coerceIn(0f, 1f)
                                val heatColor = when {
                                    player.isOverheated -> SciFiRed
                                    heatRatio > 0.8f -> SciFiRed
                                    heatRatio > 0.5f -> SciFiGold
                                    else -> SciFiCyan
                                }
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val fillHeight = size.height * heatRatio
                                    drawRect(color = heatColor.copy(alpha = 0.9f), topLeft = Offset(0f, size.height - fillHeight), size = Size(size.width, fillHeight))
                                }
                            }
                        }
                    }

                    // 3. RIGHT SIDE: Survival Cluster (Stacked Vertically)
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .graphicsLayer(alpha = 0.85f), // Overall transparency
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Shield Meter
                        val shieldGaugeHeight = (120f + (player.maxShield - 50f) * 1.2f).coerceIn(100f, 250f).dp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val isShieldCritical = player.shield < player.maxShield * 0.25f
                            Text(
                                "🛡️",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (isShieldCritical) (gameTime / 200 % 2).toFloat() else 0.8f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(6.dp) // Narrowed by 50%
                                    .height(shieldGaugeHeight)
                                    .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                    .border(0.5.dp, (if (isShieldCritical) SciFiRed else SciFiBorder).copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(
                                        color = SciFiCyan.copy(alpha = 0.9f),
                                        topLeft = Offset(0f, size.height * (1f - shieldRatio)),
                                        size = Size(size.width, size.height * shieldRatio)
                                    )
                                }
                            }
                        }

                        // Integrity Meter (Hull)
                        val integrityGaugeHeight = (120f + (player.maxIntegrity - 100f) * 0.6f).coerceIn(100f, 250f).dp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val isHullCritical = player.integrity < player.maxIntegrity * 0.25f
                            Text(
                                "❤️",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (isHullCritical) (gameTime / 200 % 2).toFloat() else 0.8f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(6.dp) // Narrowed by 50%
                                    .height(integrityGaugeHeight)
                                    .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                    .border(0.5.dp, (if (isHullCritical) SciFiRed else SciFiBorder).copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                val integrityRatio = (player.integrity / player.maxIntegrity).coerceIn(0f, 1f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(
                                        color = SciFiGreen.copy(alpha = 0.9f),
                                        topLeft = Offset(0f, size.height * (1f - integrityRatio)),
                                        size = Size(size.width, size.height * integrityRatio)
                                    )
                                }
                            }
                        }
                    }

                    // 4. CENTER BELOW ALTITUDE: Progression HUD Layer
                    val isZoneCardVisible = discoveryManager.activeEvent is DiscoveryEvent.Zone
                    AnimatedVisibility(
                        visible = !isZoneCardVisible,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp) // Moved up from 80.dp
                            .statusBarsPadding(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Mission Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                            ) {
                                missionManager.activeMissions.forEach { activeMission ->
                                    val cardColor = when (activeMission.ceremonyStage) {
                                        CeremonyStage.GLOW -> SciFiCyan.copy(alpha = 0.6f)
                                        else -> SciFiSurface
                                    }

                                    AnimatedVisibility(
                                        visible = activeMission.ceremonyStage != CeremonyStage.REPLACING && 
                                                 activeMission.ceremonyStage != CeremonyStage.REWARD_SPAWNED,
                                        enter = slideInVertically { -it } + fadeIn(),
                                        exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f), // Dissolve feel
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Surface(
                                            color = cardColor,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.width(115.dp).height(65.dp).shadow(4.dp, RoundedCornerShape(12.dp)),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = if (activeMission.ceremonyStage == CeremonyStage.GLOW) SciFiWhite else SciFiBorder
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                if (activeMission.ceremonyStage == CeremonyStage.COMPLETED_TEXT || 
                                                    activeMission.ceremonyStage == CeremonyStage.REWARD_SPAWNED) {
                                                    Text(
                                                        "MISSION COMPLETE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SciFiGreen,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 8.sp,
                                                        letterSpacing = 1.sp
                                                    )
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = activeMission.type.toIcon(),
                                                            fontSize = 8.sp,
                                                            modifier = Modifier.padding(end = 4.dp)
                                                        )
                                                        
                                                        // Task 1: Prevent Overflow
                                                        AnimatedContent(
                                                            targetState = globalShowObjective,
                                                            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                                                            label = "MissionTextRotation"
                                                        ) { showObj ->
                                                            Text(
                                                                if (showObj) activeMission.description.uppercase() else activeMission.name.uppercase(),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = when(activeMission.type) {
                                                                    MissionType.EXPLORATION -> SciFiCyan
                                                                    MissionType.PLATFORMING -> SciFiGold
                                                                    else -> SciFiWhite
                                                                },
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 8.sp,
                                                                maxLines = 1,
                                                                textAlign = TextAlign.Center,
                                                                letterSpacing = 0.5.sp,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }

                                                val progressText = when(activeMission.type) {
                                                    MissionType.EXPLORATION -> "${(activeMission.currentProgress.toFloat() / activeMission.targetValue * 100).toInt()}%"
                                                    MissionType.SURVIVAL -> "${activeMission.currentProgress}s"
                                                    else -> "${activeMission.currentProgress}/${activeMission.targetValue}"
                                                }

                                                Text(
                                                    progressText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SciFiWhite.copy(alpha = 0.7f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Spacer(Modifier.height(4.dp))
                                                Box(Modifier.width(50.dp).height(2.dp).background(SciFiWhite.copy(alpha = 0.1f), CircleShape)) {
                                                    Box(
                                                        Modifier.fillMaxHeight()
                                                            .fillMaxWidth((activeMission.currentProgress.toFloat() / activeMission.targetValue).coerceIn(0f, 1f))
                                                            .background(
                                                                when(activeMission.type) {
                                                                    MissionType.EXPLORATION -> SciFiCyan
                                                                    MissionType.PLATFORMING -> SciFiGold
                                                                    else -> SciFiWhite
                                                                },
                                                                CircleShape
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // --- INTEGRATED COMBO HUD ---
                            Surface(
                                color = SciFiSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.9f).shadow(6.dp, RoundedCornerShape(12.dp)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            "BEST THIS RUN: x${comboManager.bestComboThisRun}",
                                            color = SciFiWhite.copy(alpha = 0.5f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 7.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            "COMBO x${comboManager.currentCombo}",
                                            color = if (comboManager.currentCombo >= comboManager.comboTarget) SciFiGold else SciFiWhite,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    // Timer Bar (Visual Emphasis)
                                    val timerRatio = if (comboManager.currentCombo > 0)
                                        (comboManager.comboTimeRemaining.toFloat() / comboManager.getWindowForCombo(comboManager.currentCombo)).coerceIn(0f, 1f)
                                        else 0f
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .background(SciFiWhite.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        val barColor = if (timerRatio > 0.3f) SciFiCyan else SciFiRed
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(timerRatio)
                                                .fillMaxHeight()
                                                .background(barColor, CircleShape)
                                        )
                                    }

                                    Text(
                                        "TARGET: x${comboManager.comboTarget}",
                                        color = SciFiWhite.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // P2: DEDICATED NOTIFICATION LAYER
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp), // Positioned above player but below HUD
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Threat Notifications
                            if (activeNotification != null) {
                                val isHighAlert = activeNotification!!.contains("!!!") || activeNotification!!.contains(">>>")
                                Text(
                                    text = activeNotification!!,
                                    modifier = Modifier.graphicsLayer(alpha = notificationAlpha).widthIn(max = screenWidth.dp * 0.9f),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 4.sp,
                                        shadow = androidx.compose.ui.graphics.Shadow(if (isHighAlert) SciFiRed.copy(alpha = 0.5f) else Color.Black, blurRadius = 15f)
                                    ),
                                    color = if (isHighAlert) SciFiRed else SciFiWhite,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // (Legacy combo HUD removed - now integrated above)

                    // Floating Combo Texts
                    floatingTexts.forEach { ft ->
                        val scale = if (ft.isCritical) 1.0f + (ft.life * 0.5f) else 1.0f
                        Text(
                            text = ft.text,
                            color = ft.color.copy(alpha = (ft.life/1.0f).coerceIn(0f, 1f)),
                            modifier = Modifier
                                .offset { androidx.compose.ui.unit.IntOffset((ft.x - 50f).toInt(), (ft.y - cameraY).toInt()) }
                                .graphicsLayer(scaleX = scale, scaleY = scale),
                            style = if (ft.isCritical) MaterialTheme.typography.headlineSmall.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                            ) else MaterialTheme.typography.labelLarge.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Cinematic Area Discovery Title Card
                    AnimatedVisibility(
                        visible = discoveryManager.activeEvent is DiscoveryEvent.Zone,
                        enter = fadeIn(tween(600)) + expandVertically(tween(500)) + scaleIn(tween(500), initialScale = 0.95f),
                        exit = fadeOut(tween(400)) + shrinkVertically(tween(400)) + scaleOut(tween(400), targetScale = 1.05f),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 180.dp)
                    ) {
                        val event = discoveryManager.activeEvent as? DiscoveryEvent.Zone
                        event?.let { zoneEvent ->
                            val zone = zoneEvent.zone
                            val titleText = when(zone) {
                                AltitudeZone.CLOUD_LAYER -> "CLOUD LAYER REACHED"
                                AltitudeZone.ORBIT -> "SPACE REACHED"
                                AltitudeZone.VOID -> "VOID ENTERED"
                                else -> zone.zoneName.uppercase()
                            }

                            val accentColor = when(zone) {
                                AltitudeZone.ORBIT -> SciFiGold
                                AltitudeZone.VOID -> SciFiRed
                                AltitudeZone.DEEP_SPACE -> SciFiPurple
                                else -> SciFiCyan
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(SciFiSurface, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 32.dp, vertical = 24.dp)
                                    .border(1.dp, SciFiBorder, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        shadow = androidx.compose.ui.graphics.Shadow(accentColor.copy(alpha = 0.5f), blurRadius = 20f),
                                        letterSpacing = 4.sp
                                    ),
                                    color = SciFiWhite,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = zone.subtitle.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = accentColor.copy(alpha = 0.8f),
                                    letterSpacing = 6.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (gameState == GameState.PAUSED) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).pointerInput(Unit) {}, contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.safeDrawingPadding().width(280.dp)
                        ) {
                            if (showDevMenu && DevConfig.CHEATS_ENABLED) {
                                Text("DEVELOPER OVERRIDE", color = SciFiGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(24.dp))
                                Row(Modifier.horizontalScroll(rememberScrollState())) {
                                    AltitudeZone.entries.forEach { zone ->
                                        Button(onClick = { jumpToZone(zone) }, Modifier.padding(4.dp), colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)) { Text(zone.name, fontSize = 10.sp) }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.horizontalScroll(rememberScrollState())) {
                                    listOf("ENT_SCOUT_DRONE", "ENT_SWARM_BOTS", "ENT_CLOUD_SKIMMER", "ENT_ORBITAL_SENTRY", "ENT_CORRUPTED_HULL", "HAZ_VOID_ANOMALY", "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL").forEach { id ->
                                        Button(onClick = { spawnDevThreat(id) }, Modifier.padding(4.dp), colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)) { Text(id.substringAfterLast("_"), fontSize = 10.sp) }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row {
                                    Button(onClick = { infiniteFuel = !infiniteFuel }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(infiniteFuel) SciFiGreen.copy(alpha = 0.3f) else SciFiSurface, contentColor = if(infiniteFuel) SciFiGreen else SciFiWhite)) { Text("INF FUEL", fontSize = 10.sp) }
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = { disableHeat = !disableHeat }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(disableHeat) SciFiGreen.copy(alpha = 0.3f) else SciFiSurface, contentColor = if(disableHeat) SciFiGreen else SciFiWhite)) { Text("NO HEAT", fontSize = 10.sp) }
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = { unlockAll() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface)) { Text("UNLOCK", fontSize = 10.sp) }
                                }
                                Spacer(Modifier.height(32.dp))
                                Button(onClick = { showDevMenu = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)) { Text("RETURN TO PAUSE", color = Color.Black, fontWeight = FontWeight.Bold) }
                            } else {
                                Text(
                                    text = "SYSTEMS STANDBY",
                                    color = SciFiWhite,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(48.dp))
                                
                                val pauseButtons = listOf(
                                    "RESUME" to { gameState = GameState.PLAYING },
                                    "RESTART RUN" to { restartGame() },
                                    "MAIN MENU" to { gameState = GameState.MAIN_MENU }
                                )

                                pauseButtons.forEach { (label, action) ->
                                    Button(
                                        onClick = action,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                                    ) {
                                        Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }

                                if (DevConfig.CHEATS_ENABLED) {
                                    Spacer(Modifier.height(24.dp))
                                    Button(
                                        onClick = { showDevMenu = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SciFiGold.copy(alpha = 0.5f))
                                    ) {
                                        Text("DEV OVERRIDE", fontSize = 12.sp, letterSpacing = 2.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (gameState == GameState.TUTORIAL && activeDiscovery != null) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable {}, contentAlignment = Alignment.Center) {
                        if (activeDiscovery!!.category == "ARTIFACTS") {
                            val infiniteTransition = rememberInfiniteTransition(label = "ArtifactGlow")
                            val glowScale by infiniteTransition.animateFloat(1f, 1.5f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "GlowScale")
                            Box(Modifier.size(300.dp).graphicsLayer(scaleX = glowScale, scaleY = glowScale).background(SciFiPurple.copy(alpha = 0.1f), CircleShape))
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SciFiSurface,
                            modifier = Modifier
                                .padding(24.dp)
                                .widthIn(max = 400.dp)
                                .safeDrawingPadding()
                                .shadow(20.dp, RoundedCornerShape(20.dp), spotColor = if (activeDiscovery!!.category == "ARTIFACTS") SciFiPurple else SciFiCyan)
                                .border(1.dp, if (activeDiscovery!!.category == "ARTIFACTS") SciFiPurple.copy(alpha = 0.5f) else SciFiBorder, RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val isLore = activeDiscovery!!.category == "LORE" || activeDiscovery!!.category == "ARTIFACTS"
                                val isArtifact = activeDiscovery!!.category == "ARTIFACTS"
                                Text(
                                    text = if (isArtifact) "ARTIFACT RECOVERED" else if (isLore) "INTEL RECOVERED" else "NEW DISCOVERY",
                                    color = if (isArtifact) SciFiPurple else if (isLore) SciFiPurple else SciFiCyan,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "DATABASE UPDATED",
                                    color = SciFiWhite.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    text = activeDiscovery!!.title.uppercase(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SciFiWhite,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = activeDiscovery!!.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = SciFiWhite.copy(alpha = 0.9f),
                                    lineHeight = 22.sp,
                                    maxLines = 4,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                
                                if (isArtifact) {
                                    Spacer(Modifier.height(24.dp))
                                    Text(
                                        "PERMANENT PROGRESS RECORDED",
                                        color = SciFiGold,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(Modifier.height(32.dp))
                                Button(
                                    onClick = { gameState = GameState.PLAYING; activeDiscovery = null },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isArtifact) SciFiPurple else SciFiCyan)
                                ) {
                                    Text("ACKNOWLEDGE", fontWeight = FontWeight.Bold, color = if (isArtifact) Color.White else Color.Black, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
                if (gameState == GameState.HELP) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable { gameState = GameState.PLAYING }, contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SciFiSurface,
                            modifier = Modifier.padding(32.dp).safeDrawingPadding().border(1.dp, SciFiBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState())) {
                                Text("SYSTEM LEGEND", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = SciFiCyan, letterSpacing = 2.sp)
                                
                                Spacer(Modifier.height(24.dp))
                                Text("PLATFORM TYPES", fontWeight = FontWeight.Bold, color = SciFiWhite, letterSpacing = 1.sp)
                                Spacer(Modifier.height(8.dp))
                                val pTypes = listOf("NORMAL" to SciFiGreen, "MOVING" to SciFiCyan, "BOOST" to SciFiGold, "ICE" to SciFiCyan, "BREAKABLE" to SciFiRed, "MAGNETIC" to SciFiPurple, "PHASE" to SciFiWhite)
                                pTypes.forEach { (label, color) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).background(color, CircleShape))
                                        Spacer(Modifier.width(12.dp))
                                        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }

                                Spacer(Modifier.height(24.dp))
                                Text("RESOURCE MODULES", fontWeight = FontWeight.Bold, color = SciFiWhite, letterSpacing = 1.sp)
                                Spacer(Modifier.height(8.dp))
                                val powerTypes = listOf("FUEL" to SciFiGreen, "TURBO" to SciFiCyan, "EFFICIENCY" to SciFiGreen, "HEAT SINK" to SciFiWhite, "ARTIFACT" to SciFiPurple)
                                powerTypes.forEach { (label, color) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                                        Spacer(Modifier.width(12.dp))
                                        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                                
                                Spacer(Modifier.height(32.dp))
                                Button(
                                    onClick = { gameState = GameState.PLAYING },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                                ) {
                                    Text("CLOSE ARCHIVE", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (gameState == GameState.UNLOCK && unlockedRocket != null) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable { gameState = GameState.PLAYING; unlockedRocket = null }, contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SciFiSurface,
                            modifier = Modifier.padding(32.dp).safeDrawingPadding().border(1.dp, SciFiBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NEW ASSET UNLOCKED", color = SciFiGold, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 12.sp)
                                Spacer(Modifier.height(16.dp))
                                Text(unlockedRocket!!.title.uppercase(), style = MaterialTheme.typography.headlineMedium, color = SciFiWhite, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                Spacer(Modifier.height(16.dp))
                                Text("Craft available for immediate deployment in the Hangar.", style = MaterialTheme.typography.bodyMedium, color = SciFiWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                                Spacer(Modifier.height(32.dp))
                                Button(
                                    onClick = { gameState = GameState.PLAYING; unlockedRocket = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                                ) {
                                    Text("CONFIRM", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (gameState == GameState.GAMEOVER) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .safeDrawingPadding()
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "COMMUNICATION LOST",
                                color = SciFiRed,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 28.sp, // Responsive adjust: specific size for guaranteed fit
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "TELEMETRY DATA ENDED",
                                color = SciFiRed.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelMedium,
                                letterSpacing = 4.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            
                            Spacer(Modifier.height(48.dp))
                            
                            Surface(
                                color = SciFiSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("FINAL ALTITUDE", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                                    Text("$score", color = SciFiWhite, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    Text("RECORD ALTITUDE", color = SciFiGold.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                                    Text("$highScore", color = SciFiGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    
                                    // New: Run Progression Summary
                                    Spacer(Modifier.height(24.dp))
                                    HorizontalDivider(color = SciFiBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("RANK", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                            Text(progressionManager.currentRank.title.split(" ").last(), color = SciFiGold, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("COLLECTION", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                            Text("${progressionManager.getTotalCompletionPercentage()}%", color = SciFiCyan, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val (found, _) = progressionManager.getCompletionStats("AREAS")
                                            Text("ZONES", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                                            Text("$found", color = SciFiCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(48.dp))

                            if (continuesUsed < 1) {
                                Button(
                                    onClick = { continueRun() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
                                ) {
                                    Text("RE-ESTABLISH LINK", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            Button(
                                onClick = { restartGame() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                            ) {
                                Text("NEW EXPEDITION", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = { gameState = GameState.MAIN_MENU },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SciFiWhite.copy(alpha = 0.5f))
                            ) {
                                Text("RETURN TO BASE", fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                            }
                        }
                    }
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
    MissionType.COMBO -> "🔥"
}

@Composable
fun PowerupBadge(label: String, color: Color, seconds: Int) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Text(
                text = "$label: ${seconds}s",
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp
            )
        }
    }
}

