package com.example.jump_droid

import android.content.Context
import androidx.core.content.edit
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
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

val AchievementsList = listOf(
    Achievement("first_launch", "First Launch", "Reach 100 score.") { s, _, _ -> s >= 100 },
    Achievement("sky_breaker", "Sky Breaker", "Reach Cloud Layer.") { s, _, _ -> s >= 500 },
    Achievement("orbital_pilot", "Orbital Pilot", "Reach Orbit.") { s, _, _ -> s >= 4000 },
    Achievement("deep_space", "Deep Space Explorer", "Reach Deep Space.") { s, _, _ -> s >= 8000 },
    Achievement("combo_master", "Combo Master", "Achieve Combo x10.") { _, c, _ -> c >= 10 },
    Achievement("thermal_survivor", "Thermal Survivor", "Recover from overheating 25 times.") { _, _, o -> o >= 25 },
)

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE) }
    
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("highScore", 0)) }
    var highestYReached by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var score by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var breakableStreak by remember { mutableIntStateOf(0) }
    
    var gameState by remember { mutableStateOf(GameState.TITLE) }
    var activeDiscovery by remember { mutableStateOf<DiscoveryType?>(null) }
    var unlockedRocket by remember { mutableStateOf<RocketType?>(null) }
    var codexNotification by remember { mutableStateOf<DiscoveryType?>(null) }

    val discoveryManager = remember { DiscoveryManager(sharedPrefs) }
    val threatManager = remember { ThreatManager() }

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

    val missions = remember { mutableStateListOf(
        Mission("alt1000", "Reach 1000 altitude", 1000),
        Mission("collect5", "Collect 5 fuel tanks", 5),
        Mission("land10moving", "Land on 10 moving platforms", 10)
    )}

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

    fun checkDiscovery(type: DiscoveryType, forceTutorialState: Boolean = true) {
        if (discoveryManager.discover(type)) {
            codexNotification = type
            activeDiscovery = type
            if (forceTutorialState) gameState = GameState.TUTORIAL
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
                    floatingTexts.add(FloatingText("ACHIEVEMENT: ${achievement.title}", player.x, player.y - 200f, color = Color.Cyan, isCritical = true))
                }
            }
        }
    }

    fun updateMission(id: String, increment: Int = 1) {
        missions.find { it.id == id && !it.isCompleted }?.let { mission ->
            mission.progress += increment
            if (mission.progress >= mission.goal) {
                mission.isCompleted = true
                floatingTexts.add(FloatingText("MISSION COMPLETE!", player.x, player.y - 150f, color = Color.Green, isCritical = true))
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
                    spawnBurst(player.x, player.y, 50, Color.Cyan, 600f)
                    screenShake = 10f
                }
                AltitudeZone.UPPER_ATMOSPHERE -> {
                    impactFlashAlpha = 0.4f
                }
                AltitudeZone.ORBIT -> {
                    impactFlashAlpha = 1.0f
                    screenShake = 20f
                    spawnBurst(player.x, player.y, 100, Color.Cyan, 800f)
                }
                AltitudeZone.DEEP_SPACE -> {
                    impactFlashAlpha = 0.6f
                    spawnBurst(player.x, player.y, 60, Color(0xFF9C27B0), 500f)
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
        
        // Find the lowest visible platform to respawn on
        val visibleBottom = cameraY + screenHeight
        val visibleTop = cameraY
        val visiblePlatforms = platforms.filter { it.y in visibleTop..visibleBottom }
        
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
        spawnBurst(player.x, player.y - 100f, 40, Color.White, 300f)
        screenShake = 15f
        impactFlashAlpha = 1.0f
        floatingTexts.add(FloatingText("SYSTEM REBOOTED", player.x, player.y - 150f, color = Color.Cyan))
        
        continuesUsed++
        gameState = GameState.PLAYING
    }

    fun generatePlatform(lastY: Float): Platform {
        val difficulty = (score / 2000f).coerceIn(0f, 1f)
        val pWidth = (250f - (difficulty * 100f)).coerceAtLeast(100f)
        val gapY = 250f + (difficulty * 150f) + Random.nextFloat() * 100f
        val nextY = lastY - gapY
        val nextX = Random.nextFloat() * (screenWidth - pWidth)
        
        val type = when {
            score < 500 -> if (Random.nextFloat() < 0.2f) PlatformType.MOVING else PlatformType.NORMAL
            score < 1500 -> when {
                Random.nextFloat() < 0.2f -> PlatformType.MOVING
                Random.nextFloat() < 0.3f -> PlatformType.ICE
                Random.nextFloat() < 0.4f -> PlatformType.BOOST
                else -> PlatformType.NORMAL
            }
            else -> when {
                Random.nextFloat() < 0.2f -> PlatformType.MOVING
                Random.nextFloat() < 0.4f -> PlatformType.ICE
                Random.nextFloat() < 0.5f -> PlatformType.BOOST
                (Random.nextFloat() < 0.6f && breakableStreak < 3) -> PlatformType.BREAKABLE
                else -> PlatformType.NORMAL
            }
        }
        
        if (type == PlatformType.BREAKABLE) breakableStreak++ else breakableStreak = 0

        val isMoving = type == PlatformType.MOVING
        val speed = if (isMoving) (100f + (difficulty * 200f)) * (if (Random.nextBoolean()) 1f else -1f) else 0f
        val totalBreakTime = if (type == PlatformType.BREAKABLE) 2f + Random.nextFloat() * 2f else 3f
        
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

    fun unlockAll() {
        RocketType.entries.forEach { sharedPrefs.edit { putBoolean("unlock_${it.name}", true) } }
        DiscoveryType.entries.forEach { sharedPrefs.edit { putBoolean("discovery_$it", true) } }
    }

    fun restartGame() {
        if (screenWidth <= 0f) return
        player.x = screenWidth / 2f
        player.y = groundY
        player.velocityX = 0f
        player.velocityY = 0f
        player.maxFuel = BASE_FUEL_CAPACITY * player.rocketType.fuelMult
        player.fuel = player.maxFuel
        player.heat = 0f
        player.isOverheated = false
        player.lastPlatform = null
        player.combo = 0
        player.turboTimer = 0f
        player.efficiencyTimer = 0f
        
        gameTime = 0L
        powerUpSpawnTimer = 0f
        score = 0
        altitudeManager.updateAltitude(0)
        highestYReached = groundY
        cameraY = 0f
        continuesUsed = 0
        breakableStreak = 0
        platforms.clear()
        powerUps.clear()
        landingEffects.clear()
        particles.clear()
        threatManager.clear()
        threatSpawnTimer = 0f
        bossesSpawned.clear()
        
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
                        spawnBurst(player.x, player.y + ROCKET_HEIGHT / 2, 10, Color.LightGray, 50f)
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
                        gameTime += (dt * 1000).toLong()
                        if (screenShake > 0) screenShake = max(0f, screenShake - dt * 30f)
                        if (impactFlashAlpha > 0) impactFlashAlpha = max(0f, impactFlashAlpha - dt * 4f)
                        if (notificationAlpha > 0) notificationAlpha = max(0f, notificationAlpha - dt * 0.5f)
                        if (notificationAlpha <= 0) activeNotification = null

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
                                    activeNotification = "!!! ${def.name.uppercase()} ARRIVING !!!"
                                    notificationAlpha = 3.0f
                                    screenShake = 50f
                                    impactFlashAlpha = 1.0f
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
                            val bossPresent = activeThreats.any { it.definition.id == "MINI_BOSS_COMMANDER" }
                            val spawnChanceMod = if (bossPresent) 0.3f else 1.0f

                            // 1. Earth Zone Hazards (Air Instability)
                            if (altitudeManager.currentZone == AltitudeZone.EARTH && activeThreats.none { it.definition.type == ThreatType.HAZARD }) {
                                val instabilityIds = listOf("HAZ_GUST", "HAZ_CROSSWIND", "HAZ_THERMAL")
                                val instabilityDef = eligible.filter { it.id in instabilityIds }.randomOrNull()
                                instabilityDef?.let { def ->
                                    if (Random.nextFloat() < def.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(def, 0f, cameraY + 200f)
                                        activeNotification = when (def.id) {
                                            "HAZ_GUST" -> "DOWNWARD DRAFT"
                                            "HAZ_THERMAL" -> "UPWARD DRAFT"
                                            "HAZ_CROSSWIND" -> if (Random.nextBoolean()) "LEFTWARD CROSSWIND" else "RIGHTWARD CROSSWIND"
                                            else -> "AIR INSTABILITY DETECTED"
                                        }
                                        notificationAlpha = 1.0f
                                    }
                                }
                            }

                            // 2. Cloud Layer Elements
                            if (altitudeManager.currentZone == AltitudeZone.CLOUD_LAYER) {
                                // Sky Ray
                                if (activeThreats.none { it.definition.id == "ENT_CLOUD_SKIMMER" }) {
                                    eligible.find { it.id == "ENT_CLOUD_SKIMMER" }?.let { rayDef ->
                                        if (Random.nextFloat() < rayDef.spawnRules.spawnChance * spawnChanceMod) {
                                            val dir = if (Random.nextBoolean()) 1f else -1f
                                            val spawnX = if (dir > 0) -200f else screenWidth + 200f
                                            threatManager.spawnThreat(rayDef, spawnX, cameraY + Random.nextFloat() * screenHeight, vx = dir * 50f)
                                        }
                                    }
                                }
                                // Static Discharge
                                if (activeThreats.none { it.definition.id == "HAZ_STORM" }) {
                                    eligible.find { it.id == "HAZ_STORM" }?.let { stormDef ->
                                        if (Random.nextFloat() < stormDef.spawnRules.spawnChance * spawnChanceMod) {
                                            threatManager.spawnThreat(stormDef, Random.nextFloat() * screenWidth, cameraY - 100f, vx = (Random.nextFloat() - 0.5f) * 40f)
                                        }
                                    }
                                }
                            }

                            // 3. Space Elements (Orbital Sentry)
                            if (altitudeManager.currentZone == AltitudeZone.ORBIT && activeThreats.none { it.definition.id == "ENT_ORBITAL_SENTRY" }) {
                                eligible.find { it.id == "ENT_ORBITAL_SENTRY" }?.let { sentryDef ->
                                    if (Random.nextFloat() < sentryDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(sentryDef, Random.nextFloat() * screenWidth, cameraY + 200f)
                                    }
                                }
                            }

                            // 4. Deep Space Elements (Derelict Echo)
                            if (altitudeManager.currentZone == AltitudeZone.DEEP_SPACE && activeThreats.none { it.definition.id == "ENT_CORRUPTED_HULL" }) {
                                eligible.find { it.id == "ENT_CORRUPTED_HULL" }?.let { echoDef ->
                                    if (Random.nextFloat() < echoDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(echoDef, Random.nextFloat() * screenWidth, cameraY - 100f, vx = (Random.nextFloat() - 0.5f) * 30f, vy = 20f + Random.nextFloat() * 30f)
                                    }
                                }
                            }

                            // 5. Void Elements (Void Anomaly)
                            if (altitudeManager.currentZone == AltitudeZone.VOID && activeThreats.none { it.definition.id == "HAZ_VOID_ANOMALY" }) {
                                eligible.find { it.id == "HAZ_VOID_ANOMALY" }?.let { anomalyDef ->
                                    if (Random.nextFloat() < anomalyDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(anomalyDef, Random.nextFloat() * screenWidth, cameraY + screenHeight * 0.3f)
                                    }
                                }
                            }

                            // 6. Generic Enemies (Surveyor Probe, Swarm)
                            // Surveyor Probe
                            if (activeThreats.count { it.definition.id == "ENT_SCOUT_DRONE" } < 2) {
                                eligible.find { it.id == "ENT_SCOUT_DRONE" }?.let { probeDef ->
                                    if (Random.nextFloat() < probeDef.spawnRules.spawnChance * spawnChanceMod) {
                                        val spawnX = if (Random.nextBoolean()) -50f else screenWidth + 50f
                                        val vx = if (spawnX < 0) 150f else -150f
                                        threatManager.spawnThreat(probeDef, spawnX, cameraY + Random.nextFloat() * (screenHeight * 0.5f), vx = vx)
                                        activeNotification = "SURVEYOR PROBE DETECTED"
                                        notificationAlpha = 1.0f
                                    }
                                }
                            }
                            // Swarm
                            if (activeThreats.none { it.definition.id == "ENT_SWARM_BOTS" }) {
                                eligible.find { it.id == "ENT_SWARM_BOTS" }?.let { swarmDef ->
                                    if (Random.nextFloat() < swarmDef.spawnRules.spawnChance * spawnChanceMod) {
                                        threatManager.spawnThreat(swarmDef, Random.nextFloat() * screenWidth, cameraY - 100f)
                                        activeNotification = "AEROSOL SWARM DETECTED"
                                        notificationAlpha = 1.0f
                                    }
                                }
                            }
                            
                            // 7. Mini-Boss: Command Cruiser
                            if (activeThreats.none { it.definition.id == "MINI_BOSS_COMMANDER" }) {
                                eligible.find { it.id == "MINI_BOSS_COMMANDER" }?.let { bossDef ->
                                    if (Random.nextFloat() < bossDef.spawnRules.spawnChance) {
                                        threatManager.spawnThreat(bossDef, screenWidth / 2f, cameraY - 600f)
                                        activeNotification = "COMMAND CRUISER INBOUND"
                                        notificationAlpha = 1.0f
                                        screenShake = 20f
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
                                            activeNotification = "REINFORCEMENTS INBOUND"
                                            notificationAlpha = 1.0f
                                        }
                                    }
                                    // Boss-generated hazards
                                    if (Random.nextFloat() < 0.15f) {
                                        val id = if (Random.nextBoolean()) "HAZ_GUST" else "HAZ_CROSSWIND"
                                        ThreatRegistry.getById(id)?.let { threatManager.spawnThreat(it, boss.x, boss.y + 100f) }
                                    }
                                }
                            }
                        }

                        // Reset combo if window expires
                        if (player.combo > 0 && gameTime - player.lastLandingTime > 2000L) {
                            if (player.combo > 1) {
                                floatingTexts.add(FloatingText("COMBO LOST", player.x, player.y - 100f, color = Color.Red))
                            }
                            player.combo = 0
                        }

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
                        if (player.invulnerabilityTimer > 0) player.invulnerabilityTimer = max(0f, player.invulnerabilityTimer - dt)
                        if (player.comboFreezeTimer > 0) player.comboFreezeTimer = max(0f, player.comboFreezeTimer - dt)

                        // Visual squash/stretch lerp back to 1.0
                        player.squashStretch += (1.0f - player.squashStretch) * 10f * dt

                        val wasOverheatedBefore = player.isOverheated
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
                            val activeWind = threatManager.activeThreats.find { 
                                it.definition.id in listOf("HAZ_GUST", "HAZ_CROSSWIND", "HAZ_THERMAL") && 
                                it.state == ThreatState.ACTIVE 
                            }
                            
                            activeWind?.let { wind ->
                                when (wind.definition.id) {
                                    "HAZ_GUST" -> {
                                        player.velocityY += 1500f * sdt
                                        if (Random.nextFloat() < 0.15f) {
                                            particles.add(Particle(
                                                x = player.x + (Random.nextFloat() - 0.5f) * 60f,
                                                y = player.y - 100f,
                                                vx = (Random.nextFloat() - 0.5f) * 20f,
                                                vy = 500f + Random.nextFloat() * 500f,
                                                life = 0.5f,
                                                color = Color.White.copy(alpha = 0.3f),
                                                size = 2f
                                            ))
                                        }
                                    }
                                    "HAZ_CROSSWIND" -> {
                                        val windDir = if (Random(wind.instanceId.hashCode()).nextBoolean()) 1f else -1f
                                        player.velocityX += 800f * windDir * sdt
                                        if (Random.nextFloat() < 0.1f) {
                                            particles.add(Particle(
                                                x = player.x - 100f * windDir,
                                                y = player.y + (Random.nextFloat() - 0.5f) * 60f,
                                                vx = 400f * windDir,
                                                vy = (Random.nextFloat() - 0.5f) * 20f,
                                                life = 0.5f,
                                                color = Color.White.copy(alpha = 0.3f),
                                                size = 2f
                                            ))
                                        }
                                    }
                                    "HAZ_THERMAL" -> {
                                        player.velocityY -= 1000f * sdt
                                        if (Random.nextFloat() < 0.15f) {
                                            particles.add(Particle(
                                                x = player.x + (Random.nextFloat() - 0.5f) * 60f,
                                                y = player.y + 100f,
                                                vx = (Random.nextFloat() - 0.5f) * 20f,
                                                vy = -400f - Random.nextFloat() * 400f,
                                                life = 0.5f,
                                                color = Color.White.copy(alpha = 0.3f),
                                                size = 2f
                                            ))
                                        }
                                    }
                                }
                            }

                            // c. Threat Interactions (Proximity Effects)
                            threatManager.activeThreats.filter { it.state == ThreatState.ACTIVE }.forEach { threat ->
                                val dx = player.x - threat.x
                                val dy = player.y - threat.y
                                val distSq = dx * dx + dy * dy
                                
                                when (threat.definition.id) {
                                    "ENT_CLOUD_SKIMMER" -> { // Sky Ray: Lift Current
                                        if (distSq < 250f * 250f) {
                                            player.velocityY -= 1000f * sdt // "Ride the Ray"
                                        }
                                    }
                                    "HAZ_STORM" -> { // Static Discharge: Heat Induction
                                        if (distSq < 150f * 150f) {
                                            player.heat = min(MAX_HEAT, player.heat + 70f * sdt)
                                            // Lightning Flash
                                            if (threat.localTimer <= 0f && Random.nextFloat() < 0.005f) {
                                                threat.localTimer = 1.0f 
                                                impactFlashAlpha = 1.0f
                                                screenShake = 15f
                                                player.velocityY += 300f // Thrust instability
                                            }
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
                                                        spawnBurst(wx, wy, 25, Color.Magenta, 300f)
                                                        screenShake = 20f
                                                        floatingTexts.add(FloatingText("WEAK POINT DESTROYED", player.x, player.y - 120f, color = Color.Magenta, isCritical = true))
                                                        
                                                        if (threat.activeWeakPoints <= 0) {
                                                            threat.phase = 5 // Force retreat/destruction phase
                                                            score += 1000
                                                            floatingTexts.add(FloatingText("BOSS CRITICAL - RETREATING", player.x, player.y - 150f, color = Color.Cyan, isCritical = true))
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
                                                            spawnBurst(player.x, player.y + 50f, 10, Color.Cyan, 300f)
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

                                        // End encounter reward (ULTRA OBVIOUS)
                                        if (threat.phase == 5 && !threat.hasInteracted) {
                                            threat.hasInteracted = true
                                            powerUps.add(PowerUp(player.x, cameraY + 200f, PowerUpType.ARTIFACT))
                                            floatingTexts.add(FloatingText("!!! ${threat.definition.name.uppercase()} DEFEATED !!!", player.x, player.y - 200f, color = Color.Cyan, isCritical = true))
                                            activeNotification = ">>> MISSION DATA RECOVERED <<<"
                                            notificationAlpha = 5.0f
                                            screenShake = 60f
                                            impactFlashAlpha = 1.0f
                                            spawnBurst(player.x, cameraY + 200f, 100, Color(0xFF9C27B0), 1000f) // Massive purple burst
                                        }
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
                                val steerForce = (dx / maxSteerDist).coerceIn(-1f, 1f)
                                player.velocityX += steerForce * currentThrust * 0.7f * sdt
                                
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
                                
                                val pLeft = platform.x
                                val pRight = platform.x + platform.width
                                val pTop = platform.y
                                val pBottom = platform.y + PLATFORM_HEIGHT

                                val rLeft = player.x - rHalfW
                                val rRight = player.x + rHalfW
                                val rTop = player.y - rHalfH
                                val rBottom = player.y + rHalfH

                                // Check for overlap
                                if (rRight > pLeft && rLeft < pRight && rBottom > pTop && rTop < pBottom) {
                                    
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
                                        player.y = pTop - rHalfH
                                        if (player.velocityY > 0) player.velocityY = 0f
                                        player.squashStretch = 0.8f // Squash on landing
                                        
                                        when (platform.type) {
                                            PlatformType.BOOST -> { 
                                                player.velocityY = -600f
                                                spawnBurst(player.x, pTop, 25, Color.Yellow, 400f)
                                                screenShake = 10f
                                            }
                                            PlatformType.ICE -> { player.velocityY = LANDING_BOUNCE_VELOCITY; player.velocityX *= 0.98f; checkDiscovery(DiscoveryType.ICE_PLATFORM) }
                                            PlatformType.MOVING -> { 
                                                player.velocityY = LANDING_BOUNCE_VELOCITY
                                                player.velocityX = platform.speed
                                                checkDiscovery(DiscoveryType.MOVING_PLATFORM)
                                                updateMission("land10moving")
                                            }
                                            PlatformType.BREAKABLE -> { player.velocityY = LANDING_BOUNCE_VELOCITY; player.velocityX *= HORIZONTAL_DAMPING; platform.isBreaking = true; checkDiscovery(DiscoveryType.BREAKABLE_PLATFORM) }
                                            else -> { player.velocityY = LANDING_BOUNCE_VELOCITY; player.velocityX *= HORIZONTAL_DAMPING; checkDiscovery(DiscoveryType.NORMAL_PLATFORM) }
                                        }
                                        landingEffects.add(LandingEffect(player.x, pTop))
                                        
                                        // --- Combo Logic ---
                                        val comboWindow = 2000L
                                        if (platform != player.lastPlatform) {
                                            if (gameTime - player.lastLandingTime < comboWindow) {
                                                if (player.comboFreezeTimer <= 0f) {
                                                    player.combo++
                                                    if (player.combo > player.maxComboReached) player.maxComboReached = player.combo
                                                } else {
                                                    floatingTexts.add(FloatingText("COMBO FROZEN", player.x, player.y - 120f, color = Color.Yellow))
                                                }
                                                
                                                val comboBonus = when {
                                                    player.combo >= 10 -> { 
                                                        score += 500
                                                        player.fuel = min(player.maxFuel, player.fuel + 15f)
                                                        player.heat = max(0f, player.heat - MAX_HEAT * 0.15f)
                                                        spawnBurst(player.x, pTop, 40, Color.Cyan, 500f)
                                                        screenShake = 15f
                                                        impactFlashAlpha = 0.8f
                                                        "MEGA COMBO! +500" 
                                                    }
                                                    player.combo >= 5 -> { 
                                                        score += 200
                                                        player.fuel = min(player.maxFuel, player.fuel + 8f)
                                                        spawnBurst(player.x, pTop, 25, Color.Yellow, 300f)
                                                        screenShake = 8f
                                                        "SUPER COMBO! +200" 
                                                    }
                                                    player.combo >= 3 -> { 
                                                        score += 100
                                                        spawnBurst(player.x, pTop, 20, Color.White, 200f)
                                                        "COMBO x${player.combo} +100" 
                                                    }
                                                    else -> { score += 50; "COMBO x${player.combo}" }
                                                }
                                                val textColor = when { player.combo >= 10 -> Color.Cyan; player.combo >= 5 -> Color.Yellow; else -> Color.White }
                                                floatingTexts.add(
                                                    FloatingText(
                                                        text = comboBonus, 
                                                        x = player.x, 
                                                        y = player.y - 120f, 
                                                        color = textColor,
                                                        isCritical = player.combo >= 5
                                                    )
                                                )
                                            } else { player.combo = 1 }
                                            player.lastLandingTime = gameTime
                                            player.lastPlatform = platform
                                        }
                                        spawnBurst(player.x, pTop, 10, Color.Gray, 100f)
                                    } 
                                    else if ((wasBelow) && (player.velocityY < 0)) {
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
                                            penT -> { player.y = pTop - rHalfH; if (player.velocityY > 0) player.velocityY = 0f }
                                            penB -> { player.y = pBottom + rHalfH; if (player.velocityY < 0) player.velocityY = -player.velocityY * 0.5f }
                                            penL -> { player.x = pLeft - rHalfW; player.velocityX = -abs(player.velocityX) * 0.5f }
                                            penR -> { player.x = pRight + rHalfW; player.velocityX = abs(player.velocityX) * 0.5f }
                                        }
                                    }
                                }
                            }

                            // Ground collision
                            if (player.y > groundY) {
                                player.y = groundY
                                player.velocityY = -player.velocityY * 0.2f
                                if (abs(player.velocityY) < 50f) player.velocityY = 0f
                                player.combo = 0
                                player.lastLandingTime = 0L
                                player.lastPlatform = null
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
                        if (powerUpSpawnTimer > 10f) {
                            val types = PowerUpType.entries.filter { it != PowerUpType.ARTIFACT }
                            val rand = Random.nextFloat()
                            val type = when {
                                rand < 0.05f -> PowerUpType.ARTIFACT
                                else -> types.random()
                            }
                            powerUps.add(PowerUp(Random.nextFloat() * (screenWidth - 60f) + 30f, cameraY - 200f, type))
                            powerUpSpawnTimer = 0f
                        }

                        val powerUpIterator = powerUps.iterator()
                        while (powerUpIterator.hasNext()) {
                            val pu = powerUpIterator.next()
                            pu.y += 200f * dt
                            if (abs(player.x - pu.x) < 50f && abs(player.y - pu.y) < 70f) {
                                when (pu.type) {
                                    PowerUpType.FUEL_TANK -> { 
                                        player.maxFuel = min(250f, player.maxFuel + 25f)
                                        player.fuel = player.maxFuel
                                        spawnBurst(pu.x, pu.y, 30, Color.Yellow, 300f)
                                        screenShake = 5f
                                        floatingTexts.add(FloatingText("FUEL CAPACITY UP!", player.x, player.y - 150f, color = Color.Yellow, isCritical = true))
                                        checkDiscovery(DiscoveryType.FUEL_TANK)
                                        updateMission("collect5")
                                    }
                                    PowerUpType.TURBO_BOOSTER -> { 
                                        player.turboTimer = 8f
                                        spawnBurst(pu.x, pu.y, 30, Color.Cyan, 300f)
                                        screenShake = 5f
                                        floatingTexts.add(FloatingText("TURBO ACTIVE!", player.x, player.y - 150f, color = Color.Cyan, isCritical = true))
                                        checkDiscovery(DiscoveryType.TURBO_BOOSTER) 
                                    }
                                    PowerUpType.EFFICIENCY_MODULE -> { 
                                        player.efficiencyTimer = 8f
                                        spawnBurst(pu.x, pu.y, 30, Color.Green, 300f)
                                        screenShake = 5f
                                        floatingTexts.add(FloatingText("FUEL EFFICIENCY UP!", player.x, player.y - 150f, color = Color.Green, isCritical = true))
                                        checkDiscovery(DiscoveryType.EFFICIENCY_MODULE) 
                                    }
                                    PowerUpType.HEAT_SINK -> {
                                        player.heat = max(0f, player.heat - MAX_HEAT * 0.5f)
                                        player.isOverheated = false
                                        spawnBurst(pu.x, pu.y, 30, Color.White, 300f)
                                        screenShake = 5f
                                        floatingTexts.add(FloatingText("ENGINES COOLED!", player.x, player.y - 150f, color = Color.White, isCritical = true))
                                        checkDiscovery(DiscoveryType.HEAT_SINK)
                                    }
                                    PowerUpType.ARTIFACT -> {
                                        val artifact = listOf(
                                            DiscoveryType.ART_RECORDER, DiscoveryType.ART_ALLOY, 
                                            DiscoveryType.ART_BEACON, DiscoveryType.ART_DRONE
                                        ).random()
                                        checkDiscovery(artifact)
                                        spawnBurst(pu.x, pu.y, 50, Color(0xFF9C27B0), 400f)
                                        screenShake = 10f
                                        impactFlashAlpha = 0.6f
                                        floatingTexts.add(FloatingText("ARTIFACT RECOVERED!", player.x, player.y - 150f, color = Color(0xFF9C27B0), isCritical = true))
                                    }
                                }
                                powerUpIterator.remove()
                            } else if (pu.y > cameraY + screenHeight + 200f) powerUpIterator.remove()
                        }

                        if (platforms.isNotEmpty()) {
                            val highestPlatformY = platforms.minOf { it.y }
                            if (highestPlatformY > cameraY - screenHeight) {
                                var lastY = highestPlatformY
                                repeat(5) { generatePlatform(lastY).let { platforms.add(it); lastY = it.y } }
                            }
                        }
                        platforms.removeAll { it.y > cameraY + screenHeight + 600f }

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
                    gameState == GameState.CODEX || gameState == GameState.SETTINGS || gameState == GameState.ABOUT || 
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

                    particles.forEach { p -> drawCircle(p.color.copy(alpha = (p.life/1.0f).coerceIn(0f, 1f)), radius = p.size, center = Offset(p.x, p.y - cameraY)) }
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
                            when (threat.definition.id) {
                                "HAZ_GUST" -> {
                                    // Render wind streaks for downdraft
                                    repeat(5) {
                                        val rx = random.nextFloat() * screenWidth
                                        val ry = (gameTime / 2f + random.nextFloat() * screenHeight) % screenHeight
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.15f),
                                            start = Offset(rx, ry),
                                            end = Offset(rx, ry + 150f),
                                            strokeWidth = 2f
                                        )
                                    }
                                }
                                "HAZ_CROSSWIND" -> {
                                    // Render horizontal streaks for crosswind
                                    val windDir = if (random.nextBoolean()) 1f else -1f
                                    repeat(5) {
                                        val rx = (gameTime * (windDir * 0.5f) + random.nextFloat() * screenWidth) % screenWidth
                                        val finalX = if (rx < 0) rx + screenWidth else rx
                                        val ry = random.nextFloat() * screenHeight
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.15f),
                                            start = Offset(finalX, ry),
                                            end = Offset(finalX + (150f * windDir), ry),
                                            strokeWidth = 2f
                                        )
                                    }
                                }
                                "HAZ_THERMAL" -> {
                                    // Render upward streaks for thermal
                                    repeat(5) {
                                        val rx = random.nextFloat() * screenWidth
                                        val ry = (-gameTime / 2f + random.nextFloat() * screenHeight) % screenHeight
                                        val finalY = if (ry < 0) ry + screenHeight else ry
                                        drawLine(
                                            color = Color.Cyan.copy(alpha = 0.15f),
                                            start = Offset(rx, finalY),
                                            end = Offset(rx, finalY + 150f),
                                            strokeWidth = 2f
                                        )
                                    }
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
                                                spawnBurst(wx, wy + cameraY, 5, Color.Gray, 50f)
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
                        }
                        if (pu.type == PowerUpType.ARTIFACT) { drawCircle(baseColor, radius = 15f, center = Offset(pu.x, pu.y - cameraY)); drawCircle(Color.White, radius = 5f, center = Offset(pu.x, pu.y - cameraY)) }
                        else { drawRect(baseColor, topLeft = Offset(pu.x - 12f, pu.y - cameraY - 15f), size = Size(24f, 30f)); drawRect(Color.DarkGray, topLeft = Offset(pu.x - 6f, pu.y - cameraY - 20f), size = Size(12f, 5f)) }
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
                            drawRect(Color.Gray, topLeft = Offset(rx-10f, ry-15f), size = Size(20f, 40f))
                            drawPath(Path().apply { moveTo(rx-10f, ry-15f); lineTo(rx, ry-30f); lineTo(rx+10f, ry-15f); close() }, Color.Red)
                            drawPath(Path().apply { moveTo(rx-5f, ry+25f); lineTo(rx, ry+40f+Random.nextFloat()*10f); lineTo(rx+5f, ry+25f); close() }, Color.Yellow)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("JUMP DROID", style = MaterialTheme.typography.displayLarge.copy( shadow = androidx.compose.ui.graphics.Shadow(Color.Cyan.copy(alpha = glowAlpha), Offset(0f, 0f), 20f) ), color = Color.White, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(80.dp))
                        Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.width(220.dp)) { Text("ASCEND") }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("The Ascension Program • Established 1984", color = Color.White.copy(alpha = 0.5f))
                        Text("Powered by AshwathAI", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
            GameState.MAIN_MENU -> {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.safeDrawingPadding()) {
                        Text("COMMAND CENTER", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { restartGame() }, Modifier.width(200.dp)) { Text("LAUNCH") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { gameState = GameState.HANGAR }, Modifier.width(200.dp)) { Text("HANGAR") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { gameState = GameState.CODEX }, Modifier.width(200.dp)) { Text("CODEX") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { gameState = GameState.LEADERBOARD }, Modifier.width(200.dp)) { Text("TERMINAL") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { gameState = GameState.ABOUT }, Modifier.width(200.dp)) { Text("MISSION DATA") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { gameState = GameState.SETTINGS }, Modifier.width(200.dp)) { Text("SETTINGS") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { (context as? android.app.Activity)?.finish() }, Modifier.width(200.dp)) { Text("ABORT") }
                    }
                }
            }
            GameState.HANGAR -> {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0D001A)) {
                    Column(Modifier.padding(16.dp).safeDrawingPadding()) {
                        Text("ROCKET HANGAR", style = MaterialTheme.typography.headlineMedium, color = Color.Cyan)
                        Spacer(Modifier.height(16.dp))
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            RocketType.entries.forEach { type ->
                                val unlocked = highScore >= type.unlockScore || sharedPrefs.getBoolean("unlock_${type.name}", false)
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(enabled = unlocked) { player.rocketType = type; gameState = GameState.MAIN_MENU },
                                    color = if (player.rocketType == type) Color(0xFF1A237E) else if (unlocked) Color.DarkGray else Color.Black.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (player.rocketType == type) androidx.compose.foundation.BorderStroke(2.dp, Color.Cyan) else null
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(type.title, style = MaterialTheme.typography.titleLarge, color = if (unlocked) Color.White else Color.Gray)
                                            if (!unlocked) Text("Locked: ${type.unlockScore}m", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                                            else if (player.rocketType == type) Text("ACTIVE", color = Color.Cyan, fontWeight = FontWeight.Bold)
                                        }
                                        if (unlocked) {
                                            Spacer(Modifier.height(8.dp))
                                            Text("Fuel: ${(type.fuelMult * 100).toInt()}% • Thrust: ${(type.thrustMult * 100).toInt()}% • Heat: ${(type.heatMult * 100).toInt()}%", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                            Text(type.discovery.description, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                        Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.fillMaxWidth()) { Text("BACK") }
                    }
                }
            }
            GameState.CODEX -> {
                val categories = listOf("ROCKETS", "PLATFORMS", "POWERUPS", "AREAS", "THREATS", "ARTIFACTS", "LORE", "ACHIEVEMENTS")
                var selectedCat by remember { mutableStateOf("ROCKETS") }
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0D001A)) {
                    Column(Modifier.padding(16.dp).safeDrawingPadding()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CODEX", style = MaterialTheme.typography.headlineMedium, color = Color.Cyan)
                            val discovered = DiscoveryType.entries.count { sharedPrefs.getBoolean("discovery_$it", false) }
                            Text("$discovered Found", color = Color.Gray)
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                            categories.forEach { cat -> Text(cat, Modifier.clickable { selectedCat = cat }.padding(8.dp), color = if (selectedCat == cat) Color.Yellow else Color.Gray) }
                        }
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            if (selectedCat == "ACHIEVEMENTS") {
                                AchievementsList.forEach { ach -> val unlocked = sharedPrefs.getBoolean("achievement_${ach.id}", false); CodexCard(ach.title, if (unlocked) ach.description else "???", "", unlocked) }
                            } else {
                                DiscoveryType.entries.filter { it.category == selectedCat }.forEach { entry ->
                                    val unlocked = discoveryManager.isDiscovered(entry)
                                    val title = if (unlocked || selectedCat != "THREATS") entry.title else "UNKNOWN SIGNAL"
                                    val desc = if (unlocked) entry.description else "Locked: Encounter during expedition to unlock."
                                    val lore = if (unlocked) entry.lore else ""
                                    CodexCard(title, desc, lore, unlocked)
                                }
                            }
                        }
                        Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.fillMaxWidth()) { Text("BACK") }
                    }
                }
            }
            GameState.SETTINGS -> {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0D001A)) {
                    Column(Modifier.padding(32.dp).safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SETTINGS", style = MaterialTheme.typography.headlineLarge, color = Color.Cyan)
                        Spacer(Modifier.height(48.dp))
                        Text("Master Volume", color = Color.White); Spacer(Modifier.height(8.dp)); Box(Modifier.width(200.dp).height(10.dp).background(Color.Gray))
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { sharedPrefs.edit { clear() }; highScore = 0; player.rocketType = RocketType.BALANCED; gameState = GameState.TITLE }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RESET ALL PROGRESS") }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.fillMaxWidth()) { Text("BACK") }
                    }
                }
            }
            GameState.ABOUT -> {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0D001A)) {
                    Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState()).safeDrawingPadding()) {
                        Text("MISSION DATA", style = MaterialTheme.typography.headlineMedium, color = Color.Cyan); Spacer(Modifier.height(24.dp))
                        Text("Jump Droid is a vertical exploration game focused on precision rocket control and discovery.", color = Color.White)
                        Spacer(Modifier.height(16.dp)); Text("Built using Android Studio and Jetpack Compose. Powered by Ashwath.AI.", color = Color.White)
                        Spacer(Modifier.height(24.dp)); Text("Version: 1.1.0 (Ascension Update)", color = Color.Gray)
                        Spacer(Modifier.height(24.dp)); Text("ROADMAP", style = MaterialTheme.typography.titleLarge, color = Color.Yellow)
                        Text("• Hostile Entities & Hazards\n• Deep Space Boss Encounters\n• Global Leaderboards", color = Color.Gray)
                        Spacer(Modifier.height(32.dp)); Button(onClick = { gameState = GameState.MAIN_MENU }) { Text("BACK") }
                    }
                }
            }
            GameState.LEADERBOARD -> {
                Box(Modifier.fillMaxSize().background(Color(0xFF0D001A)).safeDrawingPadding(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TERMINAL", style = MaterialTheme.typography.headlineMedium, color = Color.Cyan); Spacer(Modifier.height(32.dp))
                        Text("Worldwide rankings coming soon.", color = Color.White); Spacer(Modifier.height(32.dp))
                        Button(onClick = { gameState = GameState.MAIN_MENU }) { Text("BACK") }
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

                    // Modern Compact HUD Panel
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .statusBarsPadding(),
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = score.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "BEST: $highScore",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray.copy(alpha = 0.7f),
                                fontSize = 9.sp
                            )
                            
                            Spacer(Modifier.height(6.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Compact Fuel Bar
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.width(70.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f), shape = CircleShape)) {
                                        Box(Modifier.fillMaxHeight().fillMaxWidth(player.fuel / player.maxFuel).background(if (player.fuel > player.maxFuel * 0.2f) Color.Green else Color.Red, shape = CircleShape))
                                    }
                                    Text("FUEL", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = Color.White.copy(alpha = 0.5f))
                                }
                                
                                // Compact Heat Bar
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.width(70.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f), shape = CircleShape)) {
                                        val heatColor = when { player.isOverheated -> Color.Red; player.heat > MAX_HEAT * 0.8f -> Color.Red; player.heat > MAX_HEAT * 0.4f -> Color.Yellow; else -> Color.Green }
                                        Box(Modifier.fillMaxHeight().fillMaxWidth(player.heat / MAX_HEAT).background(heatColor, shape = CircleShape)) 
                                    }
                                    Text(if (player.isOverheated) "OVERHEAT" else "HEAT", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = if(player.isOverheated) Color.Red else Color.White.copy(alpha = 0.5f))
                                }
                            }

                            // Compact Combo Display
                            if (player.combo > 0) {
                                val comboRemaining = (2000L - (gameTime - player.lastLandingTime)).coerceAtLeast(0L) / 2000f
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "COMBO x${player.combo}",
                                    color = if (player.combo >= 10) Color.Cyan else if (player.combo >= 5) Color.Yellow else Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                                Box(Modifier.width(60.dp).height(2.dp).background(Color.White.copy(alpha = 0.1f), shape = CircleShape)) {
                                    Box(Modifier.fillMaxHeight().fillMaxWidth(comboRemaining).background(Color.White))
                                }
                            }
                        }
                    }

                    // HUD-independent Active Powerup Indicators (Top Left)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (player.turboTimer > 0) {
                            PowerupBadge("TURBO", Color.Cyan, player.turboTimer.toInt())
                        }
                        if (player.efficiencyTimer > 0) {
                            PowerupBadge("EFFICIENCY", Color.Green, player.efficiencyTimer.toInt())
                        }
                    }

                    // Task 8: Subtle Notification below HUD
                    if (activeNotification != null) {
                        Text(
                            text = activeNotification!!,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 130.dp)
                                .statusBarsPadding()
                                .graphicsLayer(alpha = notificationAlpha),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

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
                                AltitudeZone.ORBIT -> Color.Yellow
                                AltitudeZone.VOID -> Color.Red
                                AltitudeZone.DEEP_SPACE -> Color(0xFF9C27B0)
                                else -> Color.Cyan
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = titleText,
                                    style = if (zone == AltitudeZone.ORBIT) 
                                        MaterialTheme.typography.headlineMedium.copy(shadow = androidx.compose.ui.graphics.Shadow(Color.Yellow, blurRadius = 15f))
                                        else MaterialTheme.typography.headlineMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = zone.subtitle.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accentColor.copy(alpha = 0.8f),
                                    letterSpacing = 3.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (gameState == GameState.PAUSED) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).pointerInput(Unit) {}, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.safeDrawingPadding()) {
                            if (showDevMenu && DevConfig.CHEATS_ENABLED) {
                                Text("DEVELOPER MENU", color = Color.Yellow, style = MaterialTheme.typography.headlineMedium)
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.horizontalScroll(rememberScrollState())) {
                                    AltitudeZone.entries.forEach { zone ->
                                        Button(onClick = { jumpToZone(zone) }, Modifier.padding(4.dp)) { Text(zone.name) }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.horizontalScroll(rememberScrollState())) {
                                    listOf("ENT_SCOUT_DRONE", "ENT_SWARM_BOTS", "ENT_CLOUD_SKIMMER", "ENT_ORBITAL_SENTRY", "ENT_CORRUPTED_HULL", "HAZ_VOID_ANOMALY", "MINI_BOSS_COMMANDER", "BOSS_GATEKEEPER", "BOSS_STAR_EATER", "BOSS_VOID_ENGINE", "BOSS_LEVIATHAN", "BOSS_SIGNAL").forEach { id ->
                                        Button(onClick = { spawnDevThreat(id) }, Modifier.padding(4.dp)) { Text(id.substringAfterLast("_")) }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    Button(onClick = { infiniteFuel = !infiniteFuel }, colors = ButtonDefaults.buttonColors(containerColor = if(infiniteFuel) Color.Green else Color.Gray)) { Text("INF FUEL") }
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = { disableHeat = !disableHeat }, colors = ButtonDefaults.buttonColors(containerColor = if(disableHeat) Color.Green else Color.Gray)) { Text("NO HEAT") }
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = { unlockAll() }) { Text("UNLOCK ALL") }
                                }
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { showDevMenu = false }) { Text("BACK TO PAUSE") }
                            } else {
                                Text("SYSTEM PAUSED", color = Color.White, style = MaterialTheme.typography.displayMedium)
                                Spacer(Modifier.height(32.dp))
                                Button(onClick = { gameState = GameState.PLAYING }, Modifier.width(200.dp)) { Text("RESUME") }
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { restartGame() }, Modifier.width(200.dp)) { Text("RESTART RUN") }
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.width(200.dp)) { Text("MAIN MENU") }
                                if (DevConfig.CHEATS_ENABLED) {
                                    Spacer(Modifier.height(32.dp))
                                    Button(onClick = { showDevMenu = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("DEV MENU") }
                                }
                            }
                        }
                    }
                }

                if (gameState == GameState.TUTORIAL && activeDiscovery != null) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable {}, contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(32.dp).safeDrawingPadding()) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                val isLore = activeDiscovery!!.category == "LORE" || activeDiscovery!!.category == "ARTIFACTS"
                                Text(if (isLore) "INTEL RECOVERED" else "NEW DISCOVERY!", color = if (isLore) Color(0xFF9C27B0) else Color.Blue, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp)); Text(activeDiscovery!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp)); Text(activeDiscovery!!.description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(24.dp)); Button(onClick = { gameState = GameState.PLAYING; activeDiscovery = null }) { Text("Got It") }
                            }
                        }
                    }
                }
                if (gameState == GameState.HELP) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { gameState = GameState.PLAYING }, contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(32.dp).safeDrawingPadding()) {
                            Column(Modifier.padding(24.dp)) {
                                Text("Legend", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(16.dp)); Text("Platforms", fontWeight = FontWeight.Bold)
                                Text("Green: Normal", color = Color(0xFF4CAF50)); Text("Blue: Moving", color = Color(0xFF2196F3)); Text("Yellow: Boost", color = Color(0xFFFBC02D)); Text("Cyan: Ice", color = Color(0xFF00BCD4)); Text("Orange: Breakable", color = Color(0xFFFF9800))
                                Spacer(Modifier.height(16.dp)); Text("Powerups", fontWeight = FontWeight.Bold)
                                Text("Red: Fuel Tank", color = Color(0xFFE57373)); Text("Cyan: Turbo Booster", color = Color(0xFF00BCD4)); Text("Green: Efficiency Module", color = Color(0xFF4CAF50)); Text("White: Heat Sink", color = Color.White); Text("Purple: Artifact", color = Color(0xFF9C27B0))
                                Spacer(Modifier.height(24.dp)); Button(onClick = { gameState = GameState.PLAYING }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Close") }
                            }
                        }
                    }
                }
                if (gameState == GameState.UNLOCK && unlockedRocket != null) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { gameState = GameState.PLAYING; unlockedRocket = null }, contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(32.dp).safeDrawingPadding()) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("New Rocket Unlocked!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp)); Text(unlockedRocket!!.title, style = MaterialTheme.typography.headlineMedium, color = Color.Red, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(16.dp)); Text("Available in the Hangar.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(24.dp)); Button(onClick = { gameState = GameState.PLAYING; unlockedRocket = null }) { Text("Awesome") }
                            }
                        }
                    }
                }
                if (gameState == GameState.GAMEOVER) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.safeDrawingPadding()) {
                            Text("GAME OVER", color = Color.White, style = MaterialTheme.typography.displayMedium)
                            Text("Score: $score", color = Color.White, style = MaterialTheme.typography.headlineSmall); Text("Best: $highScore", color = Color.Yellow, style = MaterialTheme.typography.headlineSmall)

                            Spacer(Modifier.height(32.dp))

                            if (continuesUsed < 1) {
                                Button(onClick = { continueRun() }, Modifier.width(200.dp)) { Text("CONTINUE") }
                                Spacer(Modifier.height(12.dp))
                            }

                            Button(onClick = { restartGame() }, Modifier.width(200.dp)) { Text("RESTART") }
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.width(200.dp)) { Text("MAIN MENU") }
                        }
                    }
                }
            }
        }
    }
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

@Composable
fun CodexCard(title: String, description: String, lore: String, unlocked: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = if (unlocked) Color.DarkGray else Color.DarkGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (unlocked) Color.Yellow else Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.LightGray, textAlign = TextAlign.Start)
            if (unlocked && lore.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(lore, style = MaterialTheme.typography.bodySmall, color = Color.Cyan.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}
