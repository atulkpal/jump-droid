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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
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
import com.example.jump_droid.Constants.MOVING_PLATFORM_CHANCE
import com.example.jump_droid.Constants.BASE_FUEL_CAPACITY
import com.example.jump_droid.Constants.MAX_HEAT
import com.example.jump_droid.Constants.HEAT_GENERATION_RATE
import com.example.jump_droid.Constants.COOLING_RATE
import com.example.jump_droid.Constants.OVERHEAT_COOLDOWN_TIME

val AchievementsList = listOf(
    Achievement("first_launch", "First Launch", "Reach 100 score.") { s, _, _ -> s >= 100 },
    Achievement("sky_breaker", "Sky Breaker", "Reach Cloud Layer.") { s, _, _ -> s >= 1000 },
    Achievement("orbital_pilot", "Orbital Pilot", "Reach Orbit.") { s, _, _ -> s >= 5000 },
    Achievement("deep_space", "Deep Space Explorer", "Reach Deep Space.") { s, _, _ -> s >= 10000 },
    Achievement("combo_master", "Combo Master", "Achieve Combo x10.") { _, c, _ -> c >= 10 },
    Achievement("thermal_survivor", "Thermal Survivor", "Recover from overheating 25 times.") { _, _, o -> o >= 25 }
)

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE) }
    
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("highScore", 0)) }
    var highestYReached by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var score by remember { mutableIntStateOf(0) }
    
    var gameState by remember { mutableStateOf(GameState.TITLE) }
    var activeDiscovery by remember { mutableStateOf<DiscoveryType?>(null) }
    var showHelp by remember { mutableStateOf(false) }
    var unlockedRocket by remember { mutableStateOf<RocketType?>(null) }
    var codexNotification by remember { mutableStateOf<DiscoveryType?>(null) }

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var groundY by remember { mutableFloatStateOf(0f) }

    val player = remember { Player(0f, 0f) }
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

    var cameraY by remember { mutableFloatStateOf(0f) }
    var isThrusting by remember { mutableStateOf(false) }
    var thrustTarget by remember { mutableStateOf(Offset.Zero) }

    fun checkDiscovery(type: DiscoveryType) {
        if (!sharedPrefs.getBoolean("discovery_$type", false)) {
            codexNotification = type
            if (type == DiscoveryType.HEAT_SYSTEM || type == DiscoveryType.OVERHEAT_SYSTEM || type.category == "PLATFORMS" || type.category == "POWERUPS" || type.category == "ARTIFACTS") {
                activeDiscovery = type
                gameState = GameState.TUTORIAL
            }
            sharedPrefs.edit { putBoolean("discovery_$type", true) }
        }
    }

    fun checkUnlock(newScore: Int) {
        RocketType.entries.forEach { type ->
            if (newScore >= type.unlockScore && !sharedPrefs.getBoolean("unlock_${type.name}", false)) {
                unlockedRocket = type
                checkDiscovery(type.discovery)
                sharedPrefs.edit { putBoolean("unlock_${type.name}", true) }
            }
        }
        
        if (newScore >= 15000) checkDiscovery(DiscoveryType.AREA_VOID)
        else if (newScore >= 10000) checkDiscovery(DiscoveryType.AREA_SPACE)
        else if (newScore >= 5000) checkDiscovery(DiscoveryType.AREA_ORBIT)
        else if (newScore >= 2500) checkDiscovery(DiscoveryType.AREA_ATMOSPHERE)
        else if (newScore >= 1000) checkDiscovery(DiscoveryType.AREA_CLOUDS)
        else if (newScore >= 0) checkDiscovery(DiscoveryType.AREA_EARTH)
        
        if (newScore >= 0) checkDiscovery(player.rocketType.discovery)

        if (newScore >= 100) checkDiscovery(DiscoveryType.LORE_ASCENSION)
        if (newScore >= 3000) checkDiscovery(DiscoveryType.LORE_SIGNAL)
        if (newScore >= 7000) checkDiscovery(DiscoveryType.LORE_LOST_FLEET)
        if (newScore >= 12000) checkDiscovery(DiscoveryType.LORE_LOGS)

        AchievementsList.forEach { achievement ->
            if (!sharedPrefs.getBoolean("achievement_${achievement.id}", false)) {
                if (achievement.unlockCondition(newScore, player.maxComboReached, player.totalOverheats)) {
                    sharedPrefs.edit { putBoolean("achievement_${achievement.id}", true) }
                    floatingTexts.add(FloatingText("ACHIEVEMENT: ${achievement.title}", player.x, player.y - 200f, color = Color.Cyan))
                }
            }
        }
    }

    fun updateMission(id: String, increment: Int = 1) {
        missions.find { it.id == id && !it.isCompleted }?.let { mission ->
            mission.progress += increment
            if (mission.progress >= mission.goal) {
                mission.isCompleted = true
                floatingTexts.add(FloatingText("MISSION COMPLETE!", player.x, player.y - 150f, color = Color.Green))
            }
        }
    }

    fun saveHighScore(newScore: Int) {
        if (newScore > highScore) {
            highScore = newScore
            sharedPrefs.edit { putInt("highScore", newScore) }
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
                Random.nextFloat() < 0.6f -> PlatformType.BREAKABLE
                else -> PlatformType.NORMAL
            }
        }
        
        val isMoving = type == PlatformType.MOVING
        val speed = if (isMoving) (100f + (difficulty * 200f)) * (if (Random.nextBoolean()) 1f else -1f) else 0f
        
        return Platform(nextX, nextY, pWidth, type, isMoving, speed)
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
        
        score = 0
        highestYReached = groundY
        cameraY = 0f
        platforms.clear()
        powerUps.clear()
        landingEffects.clear()
        particles.clear()
        
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
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        when {
                            score > 15000 -> Color(0xFF000000) // Void
                            score > 10000 -> Color(0xFF0D001A) // Deep Space
                            score > 5000 -> Color(0xFF000000)  // Orbit
                            score > 2500 -> Color(0xFF1A237E)  // Upper Atmos
                            score > 1000 -> Color(0xFF42A5F5)  // Cloud Layer
                            else -> Color(0xFF2196F3)         // Earth
                        },
                        when {
                            score > 15000 -> Color(0xFF0D001A)
                            score > 10000 -> Color(0xFF1A0033)
                            score > 5000 -> Color(0xFF1A237E)
                            else -> Color(0xFF90CAF9)
                        }
                    )
                )
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (gameState == GameState.PLAYING) {
                        isThrusting = true
                        thrustTarget = down.position
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
                var powerUpSpawnTimer = 0f
                while (gameState == GameState.PLAYING) {
                    withFrameNanos { currentTime ->
                        if (lastFrameTime == 0L) { lastFrameTime = currentTime; return@withFrameNanos }
                        val dt = (currentTime - lastFrameTime) / 1_000_000_000f
                        lastFrameTime = currentTime
                        if (screenWidth <= 0f) return@withFrameNanos

                        // Update systems
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

                        val platformIterator = platforms.iterator()
                        while (platformIterator.hasNext()) {
                            val p = platformIterator.next()
                            if (p.isMoving) {
                                p.x += p.speed * dt
                                if (p.x < 0) { p.x = 0f; p.speed *= -1 }
                                else if (p.x + p.width > screenWidth) { p.x = screenWidth - p.width; p.speed *= -1 }
                            }
                            if (p.isBreaking) {
                                p.crackTime += dt
                                if (p.crackTime > 0.3f) platformIterator.remove()
                            }
                        }

                        if (player.turboTimer > 0) player.turboTimer = max(0f, player.turboTimer - dt)
                        if (player.efficiencyTimer > 0) player.efficiencyTimer = max(0f, player.efficiencyTimer - dt)

                        if (player.isOverheated) {
                            player.overheatTimer -= dt
                            if (player.overheatTimer <= 0) { 
                                player.isOverheated = false
                                player.heat = 0f 
                                player.totalOverheats++
                            }
                        }

                        if (isThrusting && player.fuel > 0f && !player.isOverheated) {
                            val currentThrust = BASE_THRUST_POWER * player.rocketType.thrustMult * (if (player.turboTimer > 0) 1.2f else 1.0f)
                            val currentConsumption = BASE_FUEL_CONSUMPTION * player.rocketType.fuelMult * (if (player.efficiencyTimer > 0) 0.8f else 1.0f)
                            
                            // Vertical: Constant upward thrust while touching
                            player.velocityY -= currentThrust * dt
                            
                            // Horizontal: Steering based on finger position relative to rocket
                            val dx = thrustTarget.x - player.x
                            val maxSteerDist = screenWidth / 3f 
                            val steerForce = (dx / maxSteerDist).coerceIn(-1f, 1f)
                            player.velocityX += steerForce * currentThrust * 0.7f * dt
                            
                            player.fuel = max(0f, player.fuel - currentConsumption * dt)
                            
                            player.heat = min(MAX_HEAT, player.heat + HEAT_GENERATION_RATE * player.rocketType.heatMult * dt)
                            if (player.heat > MAX_HEAT * 0.7f) checkDiscovery(DiscoveryType.HEAT_SYSTEM)
                            if (player.heat >= MAX_HEAT) {
                                player.isOverheated = true
                                player.overheatTimer = OVERHEAT_COOLDOWN_TIME
                                isThrusting = false
                                checkDiscovery(DiscoveryType.OVERHEAT_SYSTEM)
                            }
                        } else if (!player.isOverheated) {
                            player.heat = max(0f, player.heat - COOLING_RATE * dt)
                        }

                        player.velocityY += BASE_GRAVITY * dt
                        player.x += player.velocityX * dt
                        player.y += player.velocityY * dt
                        player.velocityX *= AIR_FRICTION
                        player.velocityY *= AIR_FRICTION

                        if (player.x < SCREEN_PADDING) { player.x = SCREEN_PADDING; player.velocityX = -player.velocityX * 0.5f }
                        if (player.x > screenWidth - SCREEN_PADDING) { player.x = screenWidth - SCREEN_PADDING; player.velocityX = -player.velocityX * 0.5f }

                        val rHalfW = ROCKET_WIDTH / 2
                        val rHalfH = ROCKET_HEIGHT / 2
                        val rBottom = player.y + rHalfH

                        platforms.forEach { platform ->
                            val pLeft = platform.x
                            val pRight = platform.x + platform.width
                            val pTop = platform.y
                            val pBottom = platform.y + PLATFORM_HEIGHT

                            if (player.x + rHalfW > pLeft && player.x - rHalfW < pRight && rBottom > pTop && player.y - rHalfH < pBottom) {
                                val prevX = player.x - player.velocityX * dt
                                val prevY = player.y - player.velocityY * dt
                                val prevBottom = prevY + rHalfH
                                val prevTop = prevY - rHalfH
                                val prevLeft = prevX - rHalfW
                                val prevRight = prevX + rHalfW

                                if (prevBottom <= pTop) {
                                    player.y = pTop - rHalfH
                                    when (platform.type) {
                                        PlatformType.BOOST -> { player.velocityY = -600f; spawnBurst(player.x, pTop, 20, Color.Yellow, 300f); checkDiscovery(DiscoveryType.BOOST_PLATFORM) }
                                        PlatformType.ICE -> { player.velocityY = LANDING_BOUNCE_VELOCITY; player.velocityX *= 0.98f; checkDiscovery(DiscoveryType.ICE_PLATFORM) }
                                        PlatformType.MOVING -> { 
                                            player.velocityY = LANDING_BOUNCE_VELOCITY; 
                                            player.velocityX = platform.speed; 
                                            checkDiscovery(DiscoveryType.MOVING_PLATFORM)
                                            updateMission("land10moving")
                                        }
                                        PlatformType.BREAKABLE -> { player.velocityY = LANDING_BOUNCE_VELOCITY; player.velocityX *= HORIZONTAL_DAMPING; platform.isBreaking = true; checkDiscovery(DiscoveryType.BREAKABLE_PLATFORM) }
                                        else -> { player.velocityY = LANDING_BOUNCE_VELOCITY; player.velocityX *= HORIZONTAL_DAMPING; checkDiscovery(DiscoveryType.NORMAL_PLATFORM) }
                                    }
                                    landingEffects.add(LandingEffect(player.x, pTop))
                                    spawnBurst(player.x, pTop, 10, Color.Gray, 100f)
                                    val now = System.currentTimeMillis()
                                    if (platform != player.lastPlatform && now - player.lastLandingTime < 1500) {
                                        player.combo++
                                        if (player.combo > player.maxComboReached) player.maxComboReached = player.combo
                                        if (player.combo > 1) {
                                            floatingTexts.add(FloatingText("COMBO x${player.combo}", player.x, player.y - 100f, color = Color.Yellow))
                                            score += 10 * player.combo
                                        }
                                    } else if (platform != player.lastPlatform) player.combo = 1
                                    if (platform != player.lastPlatform) { player.lastLandingTime = now; player.lastPlatform = platform }
                                } else if (prevTop >= pBottom) {
                                    player.y = pBottom + rHalfH
                                    if (player.velocityY < 0) player.velocityY = -player.velocityY * 0.5f 
                                } else if (prevRight <= pLeft) {
                                    player.x = pLeft - rHalfW
                                    player.velocityX = -abs(player.velocityX) * 0.5f
                                } else if (prevLeft >= pRight) {
                                    player.x = pRight + rHalfW
                                    player.velocityX = abs(player.velocityX) * 0.5f
                                }
                            }
                        }

                        if (player.y > groundY) {
                            player.y = groundY
                            player.velocityY = -player.velocityY * 0.2f
                            if (abs(player.velocityY) < 50f) player.velocityY = 0f
                            player.combo = 0; player.lastPlatform = null
                        }

                        if (player.y - (ROCKET_HEIGHT / 2) > cameraY + screenHeight) { gameState = GameState.GAMEOVER; saveHighScore(score) }

                        powerUpSpawnTimer += dt
                        if (powerUpSpawnTimer > 10f) {
                            val types = PowerUpType.entries.filter { it != PowerUpType.REPAIR_KIT }
                            val rand = Random.nextFloat()
                            val type = when {
                                rand < 0.05f -> PowerUpType.ARTIFACT
                                else -> types.filter { it != PowerUpType.ARTIFACT }.random()
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
                                        player.maxFuel = min(250f, player.maxFuel + 25f); 
                                        player.fuel = player.maxFuel; 
                                        spawnBurst(pu.x, pu.y, 20, Color.Yellow, 200f); 
                                        checkDiscovery(DiscoveryType.FUEL_TANK)
                                        updateMission("collect5")
                                    }
                                    PowerUpType.TURBO_BOOSTER -> { player.turboTimer = 8f; spawnBurst(pu.x, pu.y, 20, Color.Cyan, 200f); checkDiscovery(DiscoveryType.TURBO_BOOSTER) }
                                    PowerUpType.EFFICIENCY_MODULE -> { player.efficiencyTimer = 8f; spawnBurst(pu.x, pu.y, 20, Color.Green, 200f); checkDiscovery(DiscoveryType.EFFICIENCY_MODULE) }
                                    PowerUpType.ARTIFACT -> {
                                        val artifact = listOf(
                                            DiscoveryType.ART_RECORDER, DiscoveryType.ART_ALLOY, 
                                            DiscoveryType.ART_BEACON, DiscoveryType.ART_DRONE
                                        ).random()
                                        checkDiscovery(artifact)
                                        spawnBurst(pu.x, pu.y, 30, Color(0xFF9C27B0), 250f)
                                        floatingTexts.add(FloatingText("ARTIFACT RECOVERED!", player.x, player.y - 150f, color = Color(0xFF9C27B0)))
                                    }
                                    else -> {}
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
                                checkUnlock(score)
                            }
                        }
                        if (player.y < cameraY + screenHeight * 0.4f) cameraY = player.y - screenHeight * 0.4f
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (gameState == GameState.TITLE || gameState == GameState.MAIN_MENU || gameState == GameState.HANGAR) {
                val time = (System.currentTimeMillis() % 100000) / 1000f
                drawRect(Color(0xFF0D001A))
                val starRandom = Random(42)
                repeat(100) {
                    val sx = (starRandom.nextFloat() * size.width + time * 5f) % size.width
                    val sy = starRandom.nextFloat() * size.height
                    drawCircle(Color.White.copy(alpha = 0.3f), radius = 1.5f, center = Offset(sx, sy))
                }
                val mPath = Path().apply {
                    moveTo(0f, size.height * 0.8f); lineTo(size.width * 0.2f, size.height * 0.7f); lineTo(size.width * 0.5f, size.height * 0.85f); lineTo(size.width * 0.8f, size.height * 0.65f); lineTo(size.width, size.height * 0.9f); lineTo(size.width, size.height); lineTo(0f, size.height); close()
                }
                drawPath(mPath, Color(0xFF1A0033).copy(alpha = 0.5f))
            }

            if (gameState == GameState.PLAYING || gameState == GameState.GAMEOVER || gameState == GameState.TUTORIAL) {
                if (score > 2500) {
                    val starRandom = Random(42)
                    repeat(if (score > 15000) 150 else 50) {
                        val sx = starRandom.nextFloat() * size.width
                        val sy = (starRandom.nextFloat() * size.height + (cameraY * (if (score > 15000) 0.05f else 0.1f))) % size.height
                        val color = if (score > 15000) { listOf(Color.White, Color.Cyan, Color.Magenta).random(starRandom).copy(alpha = 0.6f) } else Color.White.copy(alpha = 0.5f)
                        drawCircle(color, radius = if (score > 15000) 3f else 2f, center = Offset(sx, sy))
                    }
                }
                
                drawRect(Color(0xFF795548), topLeft = Offset(0f, groundY + (ROCKET_HEIGHT / 2) - cameraY), size = Size(screenWidth, screenHeight))

                particles.forEach { p -> drawCircle(p.color.copy(alpha = (p.life/1.0f).coerceIn(0f, 1f)), radius = p.size, center = Offset(p.x, p.y - cameraY)) }
                landingEffects.forEach { effect -> drawCircle(Color.Gray.copy(alpha = (effect.life/0.5f).coerceIn(0f, 0.5f)), radius = 30f * (1f - effect.life/0.5f), center = Offset(effect.x, effect.y - cameraY)) }

                platforms.forEach { platform ->
                    val color = when (platform.type) {
                        PlatformType.NORMAL -> if (score < 1000) Color(0xFF4CAF50) else if (score < 5000) Color(0xFF81C784) else Color(0xFFB0BEC5)
                        PlatformType.MOVING -> Color(0xFF2196F3)
                        PlatformType.BOOST -> Color(0xFFFFEB3B)
                        PlatformType.ICE -> Color(0xFF00BCD4)
                        PlatformType.BREAKABLE -> Color(0xFFFF9800)
                    }
                    drawRect(color, topLeft = Offset(platform.x, platform.y - cameraY), size = Size(platform.width, PLATFORM_HEIGHT))
                    if (platform.isBreaking) drawLine(Color.Black, start = Offset(platform.x, platform.y - cameraY), end = Offset(platform.x + platform.width * (platform.crackTime / 0.3f), platform.y + PLATFORM_HEIGHT - cameraY), strokeWidth = 2f)
                    if (platform.type == PlatformType.MOVING) { drawLine(Color.White.copy(alpha = 0.3f), start = Offset(0f, platform.y + PLATFORM_HEIGHT/2 - cameraY), end = Offset(screenWidth, platform.y + PLATFORM_HEIGHT/2 - cameraY), strokeWidth = 1f) }
                }

                powerUps.forEach { pu ->
                    val baseColor = when (pu.type) {
                        PowerUpType.FUEL_TANK -> Color(0xFFE57373); PowerUpType.TURBO_BOOSTER -> Color.Cyan; PowerUpType.EFFICIENCY_MODULE -> Color.Green; PowerUpType.ARTIFACT -> Color(0xFF9C27B0); else -> Color.White
                    }
                    if (pu.type == PowerUpType.ARTIFACT) { drawCircle(baseColor, radius = 15f, center = Offset(pu.x, pu.y - cameraY)); drawCircle(Color.White, radius = 5f, center = Offset(pu.x, pu.y - cameraY)) }
                    else { drawRect(baseColor, topLeft = Offset(pu.x - 12f, pu.y - cameraY - 15f), size = Size(24f, 30f)); drawRect(Color.DarkGray, topLeft = Offset(pu.x - 6f, pu.y - cameraY - 20f), size = Size(12f, 5f)) }
                }

                val rocketX = player.x
                val rocketY = player.y - cameraY
                
                if (isThrusting && player.fuel > 0 && gameState == GameState.PLAYING) {
                    val flamePath = Path().apply { moveTo(rocketX - 10f, rocketY + (ROCKET_HEIGHT / 2)); lineTo(rocketX, rocketY + (ROCKET_HEIGHT / 2) + 40f + Random.nextFloat() * 20f); lineTo(rocketX + 10f, rocketY + (ROCKET_HEIGHT / 2)); close() }
                    drawPath(flamePath, Color(0xFFFF9800))
                    if (thrustTarget.x > rocketX + 10f) { val sideFlame = Path().apply { moveTo(rocketX - (ROCKET_WIDTH / 2), rocketY + 10f); lineTo(rocketX - (ROCKET_WIDTH / 2) - 20f, rocketY + 15f); lineTo(rocketX - (ROCKET_WIDTH / 2), rocketY + 20f); close() }; drawPath(sideFlame, Color(0xFFFF9800)) }
                    else if (thrustTarget.x < rocketX - 10f) { val sideFlame = Path().apply { moveTo(rocketX + (ROCKET_WIDTH / 2), rocketY + 10f); lineTo(rocketX + (ROCKET_WIDTH / 2) + 20f, rocketY + 15f); lineTo(rocketX + (ROCKET_WIDTH / 2), rocketY + 20f); close() }; drawPath(sideFlame, Color(0xFFFF9800)) }
                }

                val heatRatio = player.heat / MAX_HEAT
                val bodyColor = if (player.isOverheated) Color.Red else Color.Gray.copy(red = 0.5f + (heatRatio * 0.5f), green = 0.5f * (1f - heatRatio), blue = 0.5f * (1f - heatRatio))
                if (player.turboTimer > 0) drawCircle(Color.Cyan.copy(alpha = 0.3f), radius = 60f, center = Offset(rocketX, rocketY))
                if (player.efficiencyTimer > 0) drawCircle(Color.Green.copy(alpha = 0.3f), radius = 55f, center = Offset(rocketX, rocketY))
                drawRect(bodyColor, topLeft = Offset(rocketX - (ROCKET_WIDTH / 2), rocketY - (ROCKET_HEIGHT / 2) + 5f), size = Size(ROCKET_WIDTH, ROCKET_HEIGHT - 5f))
                drawCircle(Color.Cyan.copy(alpha = 0.8f), radius = 8f, center = Offset(rocketX, rocketY - 10f))
                val nosePath = Path().apply { moveTo(rocketX - (ROCKET_WIDTH / 2), rocketY - (ROCKET_HEIGHT / 2) + 5f); lineTo(rocketX, rocketY - (ROCKET_HEIGHT / 2) - 25f); lineTo(rocketX + (ROCKET_WIDTH / 2), rocketY - (ROCKET_HEIGHT / 2) + 5f); close() }
                drawPath(nosePath, Color.Red)
                val leftFin = Path().apply { moveTo(rocketX - (ROCKET_WIDTH / 2), rocketY + 10f); lineTo(rocketX - (ROCKET_WIDTH / 2) - 20f, rocketY + (ROCKET_HEIGHT / 2)); lineTo(rocketX - (ROCKET_WIDTH / 2), rocketY + (ROCKET_HEIGHT / 2)); close() }
                drawPath(leftFin, Color.Red)
                val rightFin = Path().apply { moveTo(rocketX + (ROCKET_WIDTH / 2), rocketY + 10f); lineTo(rocketX + (ROCKET_WIDTH / 2) + 20f, rocketY + (ROCKET_HEIGHT / 2)); lineTo(rocketX + (ROCKET_WIDTH / 2), rocketY + (ROCKET_HEIGHT / 2)); close() }
                drawPath(rightFin, Color.Red)
            }
        }

        // --- Screens ---

        when (gameState) {
            GameState.TITLE -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val glowAlpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse))
                        val rocketOffset by infiniteTransition.animateFloat(0f, -20f, infiniteRepeatable(tween(2000), RepeatMode.Reverse))
                        Canvas(Modifier.size(100.dp).offset(y = rocketOffset.dp)) {
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
                    Text("The Ascension Program • Established 1984", Modifier.align(Alignment.BottomCenter).padding(32.dp), color = Color.White.copy(alpha = 0.5f))
                    Text("Powered by AshwathAI", Modifier.align(Alignment.BottomCenter).padding(15.dp), color = Color.White.copy(alpha = 0.5f))
                }
            }
            GameState.MAIN_MENU -> {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Column(Modifier.padding(16.dp)) {
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
                    Column(Modifier.padding(16.dp)) {
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
                                AchievementsList.forEach { ach -> val unlocked = sharedPrefs.getBoolean("achievement_${ach.id}", false); CodexCard(ach.title, if (unlocked) ach.description else "???", unlocked) }
                            } else {
                                DiscoveryType.entries.filter { it.category == selectedCat }.forEach { entry ->
                                    val unlocked = sharedPrefs.getBoolean("discovery_$entry", false)
                                    val title = if (unlocked || selectedCat != "THREATS") entry.title else "UNKNOWN SIGNAL"
                                    val desc = if (unlocked) entry.description else "Locked: Encounter during expedition to unlock."
                                    CodexCard(title, desc, unlocked)
                                }
                            }
                        }
                        Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.fillMaxWidth()) { Text("BACK") }
                    }
                }
            }
            GameState.SETTINGS -> {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0D001A)) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SETTINGS", style = MaterialTheme.typography.headlineLarge, color = Color.Cyan)
                        Spacer(Modifier.height(48.dp))
                        Text("Master Volume", color = Color.White); Spacer(Modifier.height(8.dp)); Box(Modifier.width(200.dp).height(10.dp).background(Color.Gray))
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { sharedPrefs.edit { clear() }; highScore = 0; player.rocketType = RocketType.BALANCED; gameState = GameState.TITLE }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RESET ALL PROGRESS") }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = { gameState = GameState.MAIN_MENU }, Modifier.fillMaxWidth()) { Text("BACK") }
                    }
                }
            }
            GameState.ABOUT -> {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0D001A)) {
                    Column(Modifier.padding(32.dp).verticalScroll(rememberScrollState())) {
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
                Box(Modifier.fillMaxSize().background(Color(0xFF0D001A)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TERMINAL", style = MaterialTheme.typography.headlineMedium, color = Color.Cyan); Spacer(Modifier.height(32.dp))
                        Text("Worldwide rankings coming soon.", color = Color.White); Spacer(Modifier.height(32.dp))
                        Button(onClick = { gameState = GameState.MAIN_MENU }) { Text("BACK") }
                    }
                }
            }
            GameState.PLAYING, GameState.GAMEOVER, GameState.TUTORIAL -> {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Score: $score", style = MaterialTheme.typography.headlineMedium, color = Color.Black); Spacer(Modifier.width(16.dp)); Button(onClick = { showHelp = true }, modifier = Modifier.size(40.dp), contentPadding = PaddingValues(0.dp)) { Text("?") } }
                    Text("Best: $highScore", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray); Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(200.dp).height(20.dp).background(Color.LightGray, shape = RoundedCornerShape(4.dp))) { Box(Modifier.fillMaxHeight().fillMaxWidth(player.fuel / player.maxFuel).background(if (player.fuel > player.maxFuel * 0.2f) Color.Green else Color.Red, shape = RoundedCornerShape(4.dp))) }
                    Text("Rocket Fuel (${player.maxFuel.toInt()}L)", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray); Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(200.dp).height(10.dp).background(Color.LightGray, shape = RoundedCornerShape(4.dp))) { val heatColor = when { player.isOverheated -> Color.Red; player.heat > MAX_HEAT * 0.8f -> Color.Red; player.heat > MAX_HEAT * 0.4f -> Color.Yellow; else -> Color.Green }; Box(Modifier.fillMaxHeight().fillMaxWidth(player.heat / MAX_HEAT).background(heatColor, shape = RoundedCornerShape(4.dp))) }
                    if (player.isOverheated) Text("OVERHEATED!", color = Color.Red, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    else Text("Engine Temperature", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                }
                floatingTexts.forEach { ft -> Text(ft.text, color = ft.color.copy(alpha = (ft.life/1.0f).coerceIn(0f, 1f)), modifier = Modifier.offset { androidx.compose.ui.unit.IntOffset((ft.x - 50f).toInt(), (ft.y - cameraY).toInt()) }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
                if (gameState == GameState.TUTORIAL && activeDiscovery != null) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable {}, contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(32.dp)) {
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
                if (showHelp) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showHelp = false }, contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(32.dp)) {
                            Column(Modifier.padding(24.dp)) {
                                Text("Legend", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(16.dp)); Text("Platforms", fontWeight = FontWeight.Bold)
                                Text("Green: Normal", color = Color(0xFF4CAF50)); Text("Blue: Moving", color = Color(0xFF2196F3)); Text("Yellow: Boost", color = Color(0xFFFBC02D)); Text("Cyan: Ice", color = Color(0xFF00BCD4)); Text("Orange: Breakable", color = Color(0xFFFF9800))
                                Spacer(Modifier.height(16.dp)); Text("Powerups", fontWeight = FontWeight.Bold)
                                Text("Red: Fuel Tank", color = Color(0xFFE57373)); Text("Cyan: Turbo Booster", color = Color(0xFF00BCD4)); Text("Green: Efficiency Module", color = Color(0xFF4CAF50)); Text("Purple: Artifact", color = Color(0xFF9C27B0))
                                Spacer(Modifier.height(24.dp)); Button(onClick = { showHelp = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Close") }
                            }
                        }
                    }
                }
                if (unlockedRocket != null) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { unlockedRocket = null }, contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(32.dp)) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("New Rocket Unlocked!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp)); Text(unlockedRocket!!.title, style = MaterialTheme.typography.headlineMedium, color = Color.Red, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(16.dp)); Text("Available in the Hangar.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(24.dp)); Button(onClick = { unlockedRocket = null }) { Text("Awesome") }
                            }
                        }
                    }
                }
                if (gameState == GameState.GAMEOVER) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GAME OVER", color = Color.White, style = MaterialTheme.typography.displayMedium)
                            Text("Score: $score", color = Color.White, style = MaterialTheme.typography.headlineSmall); Text("Best: $highScore", color = Color.Yellow, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(24.dp)); Button(onClick = { restartGame() }) { Text("RESTART") }
                            Spacer(Modifier.height(16.dp)); Button(onClick = { gameState = GameState.MAIN_MENU }) { Text("MAIN MENU") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodexCard(title: String, description: String, unlocked: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = if (unlocked) Color.DarkGray else Color.DarkGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (unlocked) Color.Yellow else Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.LightGray, textAlign = TextAlign.Start)
        }
    }
}
