package com.ashwathai.jump_droid

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.*
import kotlin.random.Random
import androidx.core.content.edit
import com.ashwathai.jump_droid.Constants.AIR_FRICTION
import com.ashwathai.jump_droid.Constants.BASE_FUEL_CONSUMPTION
import com.ashwathai.jump_droid.Constants.BASE_GRAVITY
import com.ashwathai.jump_droid.Constants.BASE_THRUST_POWER
import com.ashwathai.jump_droid.Constants.COOLING_RATE
import com.ashwathai.jump_droid.Constants.HEAT_GENERATION_RATE
import com.ashwathai.jump_droid.Constants.MAX_HEAT
import com.ashwathai.jump_droid.Constants.PLATFORM_HEIGHT
import com.ashwathai.jump_droid.Constants.ROCKET_HEIGHT
import com.ashwathai.jump_droid.Constants.ROCKET_WIDTH
import com.ashwathai.jump_droid.Constants.SCREEN_PADDING
import com.ashwathai.jump_droid.Constants.ZONE_THRESHOLD_SINGULARITY

class GameEngine(
    context: android.content.Context
) {
    val sharedPrefs = context.getSharedPreferences("JumpDroidPrefs", android.content.Context.MODE_PRIVATE)
    val soundManager = SoundManager(context)
    val hapticManager = HapticManager(context)
    val purchaseManager = PurchaseManager(context).also { it.initialize() }

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
    val loadoutManager = LoadoutManager(sharedPrefs)

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
    var preOverlayState by mutableStateOf(GameState.PLAYING)
    var activeDiscovery by mutableStateOf<DiscoveryType?>(null)
    var unlockedRocket by mutableStateOf<RocketType?>(null)
    var codexNotification by mutableStateOf<DiscoveryType?>(null)
    val isPremiumUser: Boolean get() = purchaseManager.isPremiumUser
    var signalDecodedMissionName by mutableStateOf<String?>(null)
    var showAscensionCredits by mutableStateOf(false)
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

    var lastFrameTime = 0L

    init {
        altitudeManager.onZoneChanged = { zone ->
            soundManager.handleZoneChange(zone)
        }
        threatManager.onThreatDestroyed = { def ->
            if (def.type == ThreatType.BOSS || def.type == ThreatType.MINI_BOSS) {
                totalBossesDefeated++
            }
        }
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
            soundManager.playSfx("sfx_fanfare_unlock")
            hapticManager.vibrate(HapticManager.HapticType.SUCCESS)
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
            soundManager.playSfx("sfx_data_scan")
            notificationManager.post("SIGNAL ARCHIVED: ${log.category.name}", NotificationPriority.FLAVOR)
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
            soundManager.playSfx("sfx_fanfare_unlock")
            hapticManager.vibrate(HapticManager.HapticType.SUCCESS)
            impactFlashAlpha = 0.4f
        }
    }

    fun getGameStats() = GameStats(
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

    fun checkDiscovery(type: DiscoveryType): Boolean {
        val isNew = discoveryManager.discover(type)
        if (type.category == "ARTIFACTS") {
            progressionManager.recordArtifactDiscovery(type, score, altitudeManager.currentZone)
            totalArtifactsCollected++
        }
        if (isNew) {
            progressionManager.updateRank()
            codexNotification = type
            activeDiscovery = type
            notificationManager.post("New Archive Entry — ${type.title}", NotificationPriority.FLAVOR)
            if (type.category == "ARTIFACTS") {
                missionManager.updateProgress(MissionType.DISCOVERY) { it.id.contains("art_find") }
                spawnBurst(player.x, player.y, 30, SciFiPurple, 300f)
            }
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
        return isNew
    }

    fun handleRewardCollection(reward: ComboReward) {
        val rewardColor = when (reward) {
            is ComboReward.Fuel -> SciFiGreen
            is ComboReward.PowerUp -> if (reward.type == PowerUpType.HULL_REPAIR) SciFiGreen else SciFiCyan
            is ComboReward.AltitudeBoost -> SciFiWhite
            is ComboReward.Artifact -> SciFiPurple
        }
        val rewardName = when (reward) {
            is ComboReward.Fuel -> "FUEL RECOVERED"
            is ComboReward.PowerUp -> reward.type.name.replace("_", " ")
            is ComboReward.AltitudeBoost -> "ALTITUDE BOOST"
            is ComboReward.Artifact -> "ARTIFACT DISCOVERED"
        }
        floatingTextManager.add(FloatingText(rewardName, player.x, player.y - 100f, color = rewardColor, isCritical = reward is ComboReward.Artifact))
        when (reward) {
            is ComboReward.Artifact -> notificationManager.post(rewardName, NotificationPriority.FLAVOR)
            is ComboReward.AltitudeBoost -> notificationManager.post(rewardName, NotificationPriority.TACTICAL)
            else -> {}
        }
        when (reward) {
            is ComboReward.Fuel -> {
                player.fuel = min(player.maxFuel, player.fuel + reward.amount)
                spawnBurst(player.x, player.y, 20, SciFiGreen, 200f)
            }
            is ComboReward.PowerUp -> powerUpManager.add(player.x, player.y - 100f, reward.type, isMissionReward = true)
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
        progressionManager.checkUnlocks(
            stats = getGameStats(),
            player = player,
            onRocketUnlock = { type -> 
                unlockedRocket = type
                soundManager.playSfx("sfx_fanfare_unlock")
                preOverlayState = gameState
                gameState = GameState.UNLOCK 
            },
            onAchievementUnlock = { achievement -> 
                floatingTextManager.add(FloatingText("ACHIEVEMENT: ${achievement.title}", player.x, player.y - 200f, color = SciFiGold, isCritical = true))
                soundManager.playSfx("sfx_fanfare_unlock")
                spawnBurst(player.x, player.y, 30, SciFiGold, 300f)
            },
            onLoreDiscovery = { type -> checkDiscovery(type) }
        )
    }

    fun saveHighScore(newScore: Int) {
        progressionManager.saveHighScore(newScore)
    }

    fun applyDamage(amount: Float) {
        if (amount > 0) { totalHazardHits++; hasTakenDamageThisRun = true }
        survivalManager.applyDamage(
            amount = amount,
            player = player,
            isGameOver = gameState != GameState.PLAYING && gameState != GameState.ASCENSION_PROTOCOL,
            onGameOver = {
                soundManager.playSfx("sfx_gameover")
                soundManager.stopThrust()
                spawnBurst(player.x, player.y, 50, SciFiRed, 500f)
                screenShake = 25f
                gameState = GameState.GAMEOVER
                saveHighScore(score)
                progressionManager.commitSessionStats(getGameStats())
            },
            onVisualFeedback = { shake, flash -> screenShake = shake; impactFlashAlpha = flash },
            onBurst = { x, y, count, color, speed -> spawnBurst(x, y, count, color, speed) },
            onPlaySfx = { soundManager.playSfx(it) },
            onVibrate = { hapticManager.vibrate(it) }
        )
    }

    fun handleLanding(platform: Platform?, yTop: Float) {
        player.velocityY = 0f
        val alreadyLanded = platform?.hasBeenLandedOn ?: false
        if (!alreadyLanded) {
            player.squashStretch = 0.8f
            
            val landSfx = when (platform?.type) {
                PlatformType.ICE -> "sfx_land_ice"
                PlatformType.BOOST, PlatformType.PHASE, PlatformType.FLUX -> "sfx_land_energy"
                PlatformType.MAGNETIC, PlatformType.GRAVITON -> "sfx_land_gravity"
                PlatformType.FUEL, PlatformType.COOLING, PlatformType.STABILITY -> "sfx_land_utility"
                PlatformType.BREAKABLE, PlatformType.MIMIC -> "sfx_land_fragile"
                else -> "sfx_land_metal"
            }
            soundManager.playSfx(landSfx)

            hapticManager.vibrate(HapticManager.HapticType.IMPACT_LIGHT)
            landingEffects.add(LandingEffect(player.x, yTop))
            spawnBurst(player.x, yTop, 15, SciFiBorder, 120f)
            platform?.hasBeenLandedOn = true
            if (player.kineticBatteryTimer > 0f) {
                player.fuel = min(player.maxFuel, player.fuel + 5f)
                player.shield = min(player.maxShield, player.shield + 2f)
                floatingTextManager.add(FloatingText("ENERGY RECOVERED", player.x, yTop - 60f, color = Color.White))
                spawnBurst(player.x, yTop, 20, Color.White, 200f)
            }
        }
        if (platform != null) {
            player.activeModules.forEach { it.onLanding(player, platform) }
            if (!alreadyLanded) {
                missionManager.updateProgress(MissionType.PLATFORMING) { it.id.startsWith("plat_land_") }
                totalPlatformLandings++
            }
            // Combo logic and lastPlatform tracking (restored from original)
            if (platform != player.lastPlatform) {
                if (player.comboFreezeTimer <= 0f) {
                    if (!alreadyLanded) {
                        comboManager.onLanding()
                    }
                }
                player.lastLandingTime = gameTime
            }
            player.lastPlatform = platform
            when (platform.type) {
                PlatformType.BOOST -> { 
                    player.velocityY = -600f
                    if (checkDiscovery(DiscoveryType.BOOST_PLATFORM)) { floatingTextManager.add(FloatingText("BOOST — Quick thrust upward!", player.x, player.y - 100f, color = SciFiGold)) }
                    if (!alreadyLanded) { spawnBurst(player.x, yTop, 25, SciFiGold, 400f); screenShake = 10f; missionManager.updateProgress(MissionType.PLATFORMING) { it.id == "plat_boost" } }
                }
                PlatformType.FUEL -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.fuel = min(player.maxFuel, player.fuel + 50f)
                    spawnBurst(player.x, yTop, 20, SciFiGreen, 200f)
                    floatingTextManager.add(FloatingText("FUEL RECHARGE", player.x, player.y - 100f, color = SciFiGreen))
                    if (checkDiscovery(DiscoveryType.FUEL_PLATFORM)) { floatingTextManager.add(FloatingText("FUEL — Restores fuel reserves!", player.x, player.y - 140f, color = SciFiGreen)) }
                }
                PlatformType.COOLING -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.heat = max(0f, player.heat - 30f)
                    spawnBurst(player.x, yTop, 20, SciFiCyan, 200f)
                    soundManager.playSfx("sfx_cooling_vent")
                    floatingTextManager.add(FloatingText("ENGINES COOLED", player.x, player.y - 100f, color = SciFiCyan))
                    if (checkDiscovery(DiscoveryType.COOLING_PLATFORM)) { floatingTextManager.add(FloatingText("COOLING — Reduces engine heat!", player.x, player.y - 140f, color = SciFiCyan)) }
                }
                PlatformType.STABILITY -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.stabilityTimer = 10f
                    spawnBurst(player.x, yTop, 20, SciFiWhite, 200f)
                    floatingTextManager.add(FloatingText("FLIGHT STABILIZED", player.x, player.y - 100f, color = SciFiWhite))
                    if (checkDiscovery(DiscoveryType.STABILITY_PLATFORM)) { floatingTextManager.add(FloatingText("STABILITY — Improves flight control!", player.x, player.y - 140f, color = SciFiWhite)) }
                }
                PlatformType.MIMIC -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    platform.isBreaking = true
                    platform.crackTime = platform.totalBreakTime
                    applyDamage(15f)
                    spawnBurst(player.x, yTop, 30, Color.Red, 400f)
                    floatingTextManager.add(FloatingText("MIMIC TRAP!", player.x, player.y - 100f, color = Color.Red))
                    if (checkDiscovery(DiscoveryType.MIMIC_PLATFORM)) { floatingTextManager.add(FloatingText("MIMIC — Deceptive appearance!", player.x, player.y - 140f, color = Color.Red)) }
                }
                PlatformType.ICE -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.velocityX *= 0.98f
                    if (checkDiscovery(DiscoveryType.ICE_PLATFORM)) { floatingTextManager.add(FloatingText("ICE — Reduced traction!", player.x, player.y - 100f, color = SciFiWhite)) }
                }
                PlatformType.MOVING -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.velocityX = platform.speed
                    if (checkDiscovery(DiscoveryType.MOVING_PLATFORM)) { floatingTextManager.add(FloatingText("MOVING — Ride the drift!", player.x, player.y - 100f, color = SciFiCyan)) }
                    if (!alreadyLanded) {
                        missionManager.updateProgress(MissionType.PLATFORMING) { it.id == "plat_moving" }
                    }
                }
                PlatformType.FLUX -> {
                    if (checkDiscovery(DiscoveryType.FLUX_PLATFORM)) { floatingTextManager.add(FloatingText("FLUX — Teleports horizontally!", player.x, player.y - 140f, color = SciFiPurple)) }
                    if (player.fluxCooldown <= 0f) {
                        val oldX = player.x
                        player.x = screenWidth - player.x
                        player.fluxCooldown = 0.2f
                        spawnBurst(oldX, yTop, 20, SciFiPurple, 200f)
                        spawnBurst(player.x, yTop, 30, SciFiPurple, 300f)
                        floatingTextManager.add(FloatingText("FLUX TELEPORT", player.x, player.y - 100f, color = SciFiPurple))
                    }
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                }
                PlatformType.GRAVITON -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    floatingTextManager.add(FloatingText("GRAVITY WELL", player.x, player.y - 100f, color = SciFiRed))
                    if (checkDiscovery(DiscoveryType.GRAVITON_PLATFORM)) { floatingTextManager.add(FloatingText("GRAVITON — Alters local gravity!", player.x, player.y - 140f, color = SciFiRed)) }
                }
                PlatformType.MAGNETIC -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    if (checkDiscovery(DiscoveryType.MAGNETIC_PLATFORM)) { floatingTextManager.add(FloatingText("MAGNETIC — Pulls your trajectory!", player.x, player.y - 100f, color = SciFiPurple)) }
                }
                PlatformType.PHASE -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.velocityX *= Constants.HORIZONTAL_DAMPING
                    if (checkDiscovery(DiscoveryType.PHASE_PLATFORM)) { floatingTextManager.add(FloatingText("PHASE — Pass through solids!", player.x, player.y - 100f, color = SciFiPurple)) }
                }
                PlatformType.BREAKABLE -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.velocityX *= Constants.HORIZONTAL_DAMPING
                    platform.isBreaking = true
                    if (checkDiscovery(DiscoveryType.BREAKABLE_PLATFORM)) { floatingTextManager.add(FloatingText("BREAKABLE — Don't linger!", player.x, player.y - 100f, color = Color.Red)) }
                }
                PlatformType.CONVEYOR -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    if (checkDiscovery(DiscoveryType.CONVEYOR_PLATFORM)) { floatingTextManager.add(FloatingText("CONVEYOR — Hold your line!", player.x, player.y - 100f, color = SciFiCyan)) }
                }
                else -> {
                    player.velocityY = com.ashwathai.jump_droid.Constants.LANDING_BOUNCE_VELOCITY
                    player.velocityX *= Constants.HORIZONTAL_DAMPING
                    if (platform.isTrapPlatform) platform.isBreaking = true
                    if (checkDiscovery(DiscoveryType.NORMAL_PLATFORM)) { floatingTextManager.add(FloatingText("STANDARD — Basic landing surface", player.x, player.y - 100f, color = SciFiBorder)) }
                }
            }
        }
    }

    fun handlePowerUp(pu: PowerUp) {
        when (pu.type) {
            PowerUpType.FUEL_TANK -> {
                if (player.maxFuel < Constants.MAX_FUEL_CAPACITY_LIMIT) {
                    player.maxFuel = min(Constants.MAX_FUEL_CAPACITY_LIMIT, player.maxFuel + 25f)
                    player.fuel = player.maxFuel
                } else player.fuel = player.maxFuel
                totalFuelPickups++
                spawnBurst(pu.x, pu.y, 30, SciFiGold, 300f)
                checkDiscovery(DiscoveryType.FUEL_TANK)
            }
            PowerUpType.TURBO_BOOSTER -> { player.turboTimer = 8f; spawnBurst(pu.x, pu.y, 30, SciFiCyan, 300f); checkDiscovery(DiscoveryType.TURBO_BOOSTER) }
            PowerUpType.SHIELD_CAPSULE -> { player.shield = min(player.maxShield, player.shield + 25f); spawnBurst(pu.x, pu.y, 30, SciFiCyan, 400f); checkDiscovery(DiscoveryType.SHIELD_CAPSULE) }
            PowerUpType.HULL_REPAIR -> { player.integrity = min(player.maxIntegrity, player.integrity + 20f); spawnBurst(pu.x, pu.y, 30, SciFiGreen, 400f); checkDiscovery(DiscoveryType.HULL_REPAIR) }
            PowerUpType.EFFICIENCY_MODULE -> { checkDiscovery(DiscoveryType.EFFICIENCY_MODULE) }
            PowerUpType.HEAT_SINK -> { checkDiscovery(DiscoveryType.HEAT_SINK) }
            PowerUpType.KINETIC_BATTERY -> { checkDiscovery(DiscoveryType.KINETIC_BATTERY) }
            PowerUpType.MAGNETIC_SIPHON -> { checkDiscovery(DiscoveryType.MAGNETIC_SIPHON) }
            PowerUpType.OVERDRIVE_MODULE -> { checkDiscovery(DiscoveryType.OVERDRIVE_MODULE) }
            PowerUpType.ALTITUDE_BOOSTER -> { checkDiscovery(DiscoveryType.ALTITUDE_BOOSTER) }
            PowerUpType.ARTIFACT -> { checkDiscovery(DiscoveryType.POWERUP_ARTIFACT) }
            else -> {}
        }
    }

    fun handlePlayerPhysics(dt: Float, physicsFlux: Float) {
        val canThrust = effectiveThrust && (player.fuel > 0f || infiniteFuel) && !player.isOverheated
        if (canThrust) {
            var thrustMult = 1.0f; var fuelMult = 1.0f; var steerMult = 1.0f; var heatGenMult = 1.0f
            player.activeModules.forEach {
                thrustMult *= it.onThrust(player, dt)
                fuelMult *= it.onFuelConsume(player, dt)
                steerMult *= it.onSteer(player, dt)
            }
            val currentThrust = BASE_THRUST_POWER * player.rocketType.thrustMult * thrustMult * physicsFlux
            player.velocityY -= currentThrust * dt
            if (!infiniteFuel) {
                val currentConsumption = BASE_FUEL_CONSUMPTION * player.rocketType.fuelMult * (if (player.efficiencyTimer > 0) 0.8f else 1.0f) * fuelMult
                player.fuel = max(0f, player.fuel - currentConsumption * dt)
            }
            if (!disableHeat) {
                player.activeModules.forEach { heatGenMult *= it.onHeatChange(player, player.heat) }
                player.heat = min(MAX_HEAT, player.heat + HEAT_GENERATION_RATE * player.rocketType.heatMult * heatGenMult * dt)
            }

            val dx = effectiveTarget.x - player.x
            player.velocityX += (dx / (screenWidth / 3f)).coerceIn(-1f, 1f) * currentThrust * 0.7f * steerMult * dt
        } else {
            var coolMult = 1.0f
            player.activeModules.forEach { coolMult *= it.onCooling(player, dt) }
            player.heat = max(0f, player.heat - COOLING_RATE * coolMult * dt)
        }
        player.velocityY += BASE_GRAVITY * physicsFlux * dt
        player.x += player.velocityX * dt
        player.y += player.velocityY * dt
        
        // --- Screen Boundary Logic (Bounce) ---
        val rHalfW = ROCKET_WIDTH / 2
        if (player.x < SCREEN_PADDING) {
            player.x = SCREEN_PADDING
            player.velocityX = -player.velocityX * 0.5f
        } else if (player.x > screenWidth - SCREEN_PADDING && screenWidth > 0f) {
            player.x = screenWidth - SCREEN_PADDING
            player.velocityX = -player.velocityX * 0.5f
        }
    }

    fun resolveCollisions(dt: Float) {
        val rHalfW = ROCKET_WIDTH / 2; val rHalfH = ROCKET_HEIGHT / 2
        val oldX = player.x - player.velocityX * dt
        val oldY = player.y - player.velocityY * dt
        val oldTop = oldY - rHalfH; val oldBottom = oldY + rHalfH
        val oldLeft = oldX - rHalfW; val oldRight = oldX + rHalfW

        platforms.forEach { platform ->
            if (platform.isBreaking && platform.crackTime > platform.totalBreakTime) return@forEach
            if (platform.type == PlatformType.PHASE) {
                val cycle = 4000L
                val progress = (gameTime % cycle) / cycle.toFloat()
                if (progress >= 0.4f) return@forEach
            }
            val pLeft = platform.x; val pRight = platform.x + platform.width; val pTop = platform.y; val pBottom = platform.y + PLATFORM_HEIGHT

            // Standard AABB overlap
            val rLeft = player.x - rHalfW; val rRight = player.x + rHalfW
            val rTop = player.y - rHalfH; val rBottom = player.y + rHalfH

            if (rRight > pLeft && rLeft < pRight && rBottom >= pTop && rTop < pBottom) {
                // Determine direction from previous-frame position
                val wasAbove = oldBottom <= pTop + 5f
                val wasBelow = oldTop >= pBottom - 5f
                val wasLeft = oldRight <= pLeft + 5f
                val wasRight = oldLeft >= pRight - 5f

                when {
                    wasAbove && player.velocityY >= 0 -> {
                        // Top landing
                        player.isOnPlatform = true
                        if (platform != player.lastPlatform) {
                            handleLanding(platform, pTop)
                        } else {
                            player.y = pTop - rHalfH
                            player.velocityY = 0f
                        }
                        // Moving platform carry-over
                        if (platform.isMoving) player.x += platform.speed * dt
                        if (platform.type == PlatformType.CONVEYOR) player.x += 150f * dt
                    }
                    wasBelow && player.velocityY < 0 -> {
                        // Underside hit
                        player.y = pBottom + rHalfH
                        player.velocityY = -player.velocityY * 0.5f
                    }
                    wasLeft && player.velocityX > 0 -> {
                        // Left edge
                        player.x = pLeft - rHalfW
                        player.velocityX = -player.velocityX * 0.5f
                    }
                    wasRight && player.velocityX < 0 -> {
                        // Right edge
                        player.x = pRight + rHalfW
                        player.velocityX = -player.velocityX * 0.5f
                    }
                    else -> {
                        // Fallback: resolve via shallowest penetration
                        val overlapTop = rBottom - pTop
                        val overlapBottom = pBottom - rTop
                        val overlapLeft = rRight - pLeft
                        val overlapRight = pRight - rLeft
                        val minOverlap = minOf(overlapTop, overlapBottom, overlapLeft, overlapRight)
                        when (minOverlap) {
                            overlapTop -> if (player.velocityY >= 0) {
                                player.isOnPlatform = true
                                if (platform != player.lastPlatform) handleLanding(platform, pTop)
                                else { player.y = pTop - rHalfH; player.velocityY = 0f }
                                if (platform.isMoving) player.x += platform.speed * dt
                                if (platform.type == PlatformType.CONVEYOR) player.x += 150f * dt
                            }
                            overlapBottom -> if (player.velocityY < 0) {
                                player.y = pBottom + rHalfH; player.velocityY = -player.velocityY * 0.5f
                            }
                            overlapLeft -> { player.x = pLeft - rHalfW; player.velocityX = -player.velocityX * 0.5f }
                            overlapRight -> { player.x = pRight + rHalfW; player.velocityX = -player.velocityX * 0.5f }
                        }
                    }
                }
            }
        }
        if (player.y > groundY && groundY > 0f) {
            player.isOnPlatform = true
            player.y = groundY
            if (player.lastPlatform != null) handleLanding(null, groundY)
            else { if (player.velocityY > 0) player.velocityY = -player.velocityY * 0.2f; if (abs(player.velocityY) < 50f) player.velocityY = 0f }
        }
    }

    fun update(dt: Float) {
        if (gameState != GameState.PLAYING && gameState != GameState.ASCENSION_PROTOCOL) return
        if (screenWidth <= 0f) return

        // EPIC 7: Module Update Hook
        player.activeModules.forEach { module ->
            module.onUpdate(player, dt, onSpawnPlatform = { x, y, type ->
                if (platforms.size < 50) {
                    platforms.add(Platform(x, y, 150f, type))
                }
            })
        }

        player.updateTimers(dt)
        discoveryManager.update(dt)
        threatManager.update(dt, cameraY, screenHeight, screenWidth, player, powerUpManager.powerUps)
        
        // EPIC 11 Meta-boss logic
        if (gameState == GameState.ASCENSION_PROTOCOL && bossesSpawned.contains("BOSS_SINGULARITY")) {
            if (threatManager.activeThreats.none { it.definition.id == "BOSS_SINGULARITY" }) {
                if (!showAscensionCredits) {
                    checkDiscovery(DiscoveryType.DISCOVERY_THE_END)
                    showAscensionCredits = true
                }
            }
        }

        ambientManager.update(dt, cameraY, screenWidth, screenHeight, altitudeManager.currentZone)
        
        // Audio: Boss Music Check
        val hasBoss = threatManager.activeThreats.any { it.definition.type == ThreatType.BOSS }
        soundManager.setBossActive(hasBoss)

        comboManager.update(dt)
        val comboRingX = 60f
        val comboRingY = 60f

        if (comboManager.immediateSurvivalRewards.isNotEmpty()) {
            comboManager.immediateSurvivalRewards.forEach { reward ->
                flyingRewards.add(FlyingReward(type = reward, x = comboRingX, y = comboRingY, targetX = player.x, targetY = player.y - cameraY, scale = 4.0f))
            }
            comboManager.immediateSurvivalRewards.clear()
        }

        if (comboManager.pendingReward != null) {
            val rewardName = when (comboManager.pendingReward) {
                is ComboReward.Fuel -> "COMBO REWARD"
                is ComboReward.PowerUp -> "COMBO REWARD"
                is ComboReward.AltitudeBoost -> "COMBO REWARD"
                is ComboReward.Artifact -> "ARTIFACT DISCOVERED"
                null -> "COMBO REWARD"
            }
            floatingTextManager.add(FloatingText(rewardName, comboRingX + 60f, comboRingY + 10f, color = SciFiGold, isCritical = true))
            flyingRewards.add(FlyingReward(type = comboManager.pendingReward!!, x = comboRingX, y = comboRingY, targetX = player.x, targetY = player.y - cameraY, scale = 3.5f))
            comboManager.pendingReward = null
        }

        val rewardIter = flyingRewards.iterator()
        while (rewardIter.hasNext()) {
            val fr = rewardIter.next()
            fr.progress += dt
            if (fr.progress >= 1.0f) {
                handleRewardCollection(fr.type)
                rewardIter.remove()
            } else {
                fr.targetX = player.x
                fr.targetY = player.y - cameraY
            }
        }

        notificationManager.update(dt)
        floatingTextManager.update(dt)
        
        // Missions
        val stats = getGameStats()
        missionManager.updateProgressAll(stats)
        missionManager.checkUnlocks()
        missionManager.updateProgress(MissionType.EXPLORATION, absoluteValue = score)
        
        missionManager.activeMissions.forEach { mission ->
            if (mission.isCompleted && !missionManager.isInCeremony(mission.id)) {
                missionManager.startCeremony(mission.id)
                notificationManager.post("MISSION COMPLETE: ${mission.name.uppercase()}", NotificationPriority.TACTICAL)
                soundManager.playSfx("sfx_fanfare_mission")
                hapticManager.vibrate(HapticManager.HapticType.SUCCESS)
                val celebrationX = screenWidth / 2f
                val celebrationY = cameraY + 80f
                spawnBurst(celebrationX, celebrationY, 40, SciFiGold, 350f)
                spawnBurst(celebrationX - 50f, celebrationY + 20f, 20, SciFiCyan, 250f)
                spawnBurst(celebrationX + 50f, celebrationY + 20f, 20, SciFiCyan, 250f)
                floatingTextManager.add(FloatingText("MISSION COMPLETE", celebrationX, celebrationY - 40f, color = SciFiGold, isCritical = true, life = 2.0f))
                impactFlashAlpha = max(impactFlashAlpha, 0.6f)
                screenShake = max(screenShake, 10f)
            }
        }

        missionManager.updateCeremonies(dt).forEach { mid ->
            missionManager.activeMissions.find { it.id == mid }?.ceremonyStage = CeremonyStage.REPLACING
        }
        missionManager.selectNextMission()

        // Survival
        survivalManager.update(dt, player, gameTime, notificationManager, 
            onGameOver = { gameState = GameState.GAMEOVER; saveHighScore(score) }, 
            onShake = { screenShake = max(screenShake, it) }, 
            shieldRegenMultiplier = progressionManager.getShieldRegenMultiplier(),
            onPlaySfx = { soundManager.playSfx(it) },
            onVibrate = { hapticManager.vibrate(it) }
        )

        // Visuals
        screenShake = max(0f, screenShake - dt * 30f)
        impactFlashAlpha = max(0f, impactFlashAlpha - dt * 4f)
        player.squashStretch += (1.0f - player.squashStretch) * 10f * dt

        if (player.isOverheated) {
            player.overheatTimer -= dt
            if (player.overheatTimer <= 0) { player.isOverheated = false; player.heat = 0f; player.totalOverheats++ }
        } else if (player.heat >= Constants.MAX_HEAT && !player.isOverheated) {
            player.isOverheated = true
            player.overheatTimer = Constants.OVERHEAT_COOLDOWN_TIME
            soundManager.playSfx("sfx_overheat_alarm")
        }

        // Particles & Effects
        landingEffects.removeAll { it.life <= 0 }; landingEffects.forEach { it.life -= dt }
        particles.iterator().let { ite ->
            while (ite.hasNext()) {
                val p = ite.next()
                p.life -= dt
                if (p.life <= 0) ite.remove() else { p.x += p.vx * dt; p.y += p.vy * dt; p.vy += 500f * dt }
            }
        }

        // Physics + Projectiles (Integrated sub-steps)
        val subSteps = 4
        val sdt = dt / subSteps
        
        // EPIC 11: Singularity zone physics flux
        val physicsFlux = if (altitudeManager.currentZone == AltitudeZone.SINGULARITY) {
            1.0f + sin(score / 500f) * 0.2f
        } else 1.0f

        repeat(subSteps) {
            player.isOnPlatform = false
            if (player.heat == 0f) noOverheatTimer += sdt
            if (!hasTakenDamageThisRun) perfectRunTimer += sdt

            player.activeTether?.applyPhysics(player, sdt, cameraY)

            platforms.forEach { p ->
                if (p.isMoving) {
                    p.x += p.speed * sdt
                    if (p.x < 0 || p.x + p.width > screenWidth) p.speed *= -1
                }
                if (p.isBreaking) p.crackTime += sdt
            }

            // Magnetic & Graviton field proximity effects
            platforms.forEach { platform ->
                if (platform.type == PlatformType.MAGNETIC || platform.type == PlatformType.GRAVITON) {
                    val dx = player.x - (platform.x + platform.width / 2f)
                    val dy = player.y - (platform.y + PLATFORM_HEIGHT / 2f)
                    val distSq = dx * dx + dy * dy
                    val isGraviton = platform.type == PlatformType.GRAVITON
                    val radius = if (isGraviton) 180f else 250f
                    if (distSq < radius * radius) {
                        val dist = sqrt(distSq)
                        val force = (1f - dist / radius) * (if (isGraviton) 3000f else 1200f)
                        player.velocityX -= (dx / dist) * force * sdt
                        player.velocityY -= (dy / dist) * force * sdt
                        val dampBase = if (isGraviton) 0.25f else 0.15f
                        val damp = 1f - dampBase * (1f - dist / radius)
                        player.velocityX *= damp
                        player.velocityY *= damp
                    }
                }
            }

            // Projectile Sub-step Update for higher accuracy
            projectileManager.update(sdt)
            projectileManager.processPlayerCollision(player) { applyDamage(it.damage) }
            
            // Audio: Projectile platform impacts
            val projIter = projectileManager.projectiles.iterator()
            while (projIter.hasNext()) {
                val p = projIter.next()
                platforms.forEach { platform ->
                    val pLeft = platform.x; val pRight = platform.x + platform.width
                    val pTop = platform.y; val pBottom = platform.y + PLATFORM_HEIGHT
                    if (p.x in pLeft..pRight && p.y in pTop..pBottom) {
                        soundManager.playSfx("sfx_impact_small")
                        spawnBurst(p.x, p.y, 5, p.color, 100f)
                        projIter.remove()
                        return@forEach
                    }
                }
            }

            threatManager.activeThreats.toList().forEach { threat ->
                threat.processInteraction(player, sdt, effectiveThrust, platforms, powerUpManager.powerUps, gameTime, screenWidth, screenHeight, cameraY, 
                    onVisualFeedback = { s, f -> screenShake = max(screenShake, s); impactFlashAlpha = max(impactFlashAlpha, f) },
                    onNotification = { m, a -> notificationManager.showImmediately(m, priority = NotificationPriority.CRITICAL, duration = a ?: 2.0f) },
                    onMajorWarning = { m, d -> majorWarningText = m; majorWarningTimer = d },
                    onFloatingText = { m, x, y, c, cr, l -> floatingTextManager.add(FloatingText(m, x, y, life = l, color = c, isCritical = cr)) },
                    onDiscovery = { checkDiscovery(it) },
                    onBurst = { x, y, c, cl, s -> spawnBurst(x, y, c, cl, s) },
                    onScoreUpdate = { score += it },
                    onMissionProgress = { missionManager.updateProgress(it) },
                    onSpawnGhostPlatform = { x, y -> platforms.add(Platform(x, y, 150f, PlatformType.NORMAL).apply { isTrapPlatform = true; totalBreakTime = 0.3f }) },
                    onSpawnReinforcements = { spawnEscalationThreat("HAZ_EMP", player.x, cameraY) },
                    onAnchoredText = { floatingTextManager.add(it) },
                    onEscalationEvent = { tx, ty, src -> spawnEscalationThreat(src.definition.id, tx, ty) },
                    activeThreats = threatManager.activeThreats,
                    onSpawnProjectile = { x, y, vx, vy, t, o, d, c, sz, l -> projectileManager.spawn(x, y, vx, vy, t, o, d, c, sz, l) },
                    onSpawnThreat = { id, x, y, vx, vy -> ThreatRegistry.getById(id)?.let { threatManager.spawnThreat(it, x, y, vx, vy) } },
                    onDamage = { amount -> applyDamage(amount) },
                    onPlaySfx = { sfx -> soundManager.playSfx(sfx) },
                    onVibrate = { hapticType -> hapticManager.vibrate(hapticType) }
                )
            }

            handlePlayerPhysics(sdt, physicsFlux)
            resolveCollisions(sdt)
        }

        // Cleanup broken platforms
        val platformIter = platforms.iterator()
        while (platformIter.hasNext()) {
            val p = platformIter.next()
            if (p.isBreaking && p.crackTime > p.totalBreakTime) {
                spawnBurst(p.x + p.width / 2, p.y, 25, Color(0xFFFF9800), 200f)
                platformIter.remove()
            }
        }

        // Air friction: raw constant, once per frame (matches original behavior)
        player.velocityX *= AIR_FRICTION
        player.velocityY *= AIR_FRICTION

        // Power-ups
        powerUpManager.updateAutoSpawn(dt, screenWidth, cameraY, screenHeight, threatManager.activeThreats, platforms)
        powerUpManager.updateMovement(dt, gameTime, player, platforms, cameraY, screenHeight)
        for (pu in powerUpManager.checkCollection(player)) {
            totalPowerUps++
            soundManager.playSfx("collect")
            handlePowerUp(pu)
        }

        // World Generation
        if (platforms.isNotEmpty()) {
            val highestPlatformY = platforms.minOf { it.y }
            if (highestPlatformY > cameraY - screenHeight) {
                var lastY = highestPlatformY
                repeat(5) { generatePlatform(lastY).let { platforms.add(it); lastY = it.y } }
            }
        }
        platforms.removeAll { it.y > cameraY + screenHeight + 600f }

        runDurationTimer += dt
        if (!player.isOnPlatform) airborneTimer += dt

        if (!effectiveThrust) player.fuel = min(player.maxFuel, player.fuel + Constants.FUEL_RECHARGE_RATE * dt)

        // Thrust trail particles (orange bubbles)
        if (effectiveThrust) {
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

        // Director
        encounterDirector.update(dt, score, altitudeManager.currentZone, screenWidth, screenHeight, cameraY, player.x, player.y, bossesSpawned, threatManager, notificationManager, onDiscovery = { checkDiscovery(it) }, onVisualFeedback = { s, f -> screenShake = max(screenShake, s); impactFlashAlpha = max(impactFlashAlpha, f) })

        // Camera
        if (player.y < cameraY + screenHeight * 0.4f) cameraY = player.y - screenHeight * 0.4f
        
        // Score
        val absoluteY = player.y - baseAltitude
        if (absoluteY < highestYReached) {
            highestYReached = absoluteY
            val newScore = ((groundY - highestYReached) / 10f).toInt()
            if (newScore > score) {
                score = newScore
                altitudeManager.updateAltitude(score)
                checkUnlock(score)
                
                if (score >= ZONE_THRESHOLD_SINGULARITY && gameState == GameState.PLAYING) {
                    gameState = GameState.ASCENSION_PROTOCOL
                    triggerAscensionOriginReset()
                    ThreatRegistry.getById("BOSS_SINGULARITY")?.let { threatManager.spawnThreat(it, screenWidth / 2f, -400f) }
                }
            }
        }

        if (player.y - (ROCKET_HEIGHT / 2) > cameraY + screenHeight && screenHeight > 0f) {
            spawnBurst(player.x, player.y, 30, SciFiRed, 400f)
            screenShake = 20f
            gameState = GameState.GAMEOVER
            saveHighScore(score)
        }
    }

    fun generatePlatform(lastY: Float): Platform = platformManager.generate(score, screenWidth, lastY)

    fun restartGame() {
        if (screenWidth <= 0f) return
        player.x = screenWidth / 2f
        player.y = groundY
        player.velocityX = 0f; player.velocityY = 0f
        
        // RE-INITIALIZE ROCKET STATS (Fix for restart regression)
        player.maxFuel = com.ashwathai.jump_droid.Constants.BASE_FUEL_CAPACITY * player.rocketType.fuelMult
        player.fuel = player.maxFuel
        player.maxIntegrity = progressionManager.permanentMaxIntegrity + progressionManager.getHullBonusAmount()
        player.integrity = player.maxIntegrity
        player.maxShield = progressionManager.permanentMaxShield
        player.shield = player.maxShield
        player.maxHeat = com.ashwathai.jump_droid.Constants.MAX_HEAT * player.rocketType.heatMult

        player.isOverheated = false; player.lastPlatform = null; player.combo = 0
        score = 0; cameraY = 0f; baseAltitude = 0f; continuesUsed = 0; airborneTimer = 0f; noOverheatTimer = 0f
        highestYReached = player.y
        lastFrameTime = 0L
        platforms.clear(); flyingRewards.clear(); particles.clear(); landingEffects.clear()
        powerUpManager.powerUps.clear(); threatManager.clear(); projectileManager.clear(); bossesSpawned.clear()
        
        var currentY = groundY - 250f
        repeat(15) { generatePlatform(currentY).let { platforms.add(it); currentY = it.y } }
        
        altitudeManager.updateAltitude(0)
        soundManager.playZoneMusic(AltitudeZone.EARTH)
        gameState = GameState.PLAYING
    }

    // --- Developer Cheats ---
    fun jumpToZone(zone: AltitudeZone) {
        score = zone.threshold
        highestYReached = groundY - score * 10f
        player.y = highestYReached
        cameraY = player.y - screenHeight * 0.5f
        player.velocityX = 0f; player.velocityY = 0f
        platforms.clear()
        var currentY = player.y - 250f
        repeat(15) { generatePlatform(currentY).let { platforms.add(it); currentY = it.y } }
        altitudeManager.updateAltitude(score)
        gameState = GameState.PLAYING
    }

    fun spawnDevThreat(id: String) {
        ThreatRegistry.getById(id)?.let { threatManager.spawnThreat(it, player.x, cameraY - 300f) }
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
        RocketType.entries.forEach { rt -> sharedPrefs.edit { putBoolean("unlock_${rt.name}", true) } }
        DiscoveryType.entries.forEach { dt -> sharedPrefs.edit { putBoolean("discovery_$dt", true) } }
        gameState = GameState.PLAYING
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

    fun triggerAscensionOriginReset() {
        baseAltitude += abs(cameraY)
        val shiftAmount = cameraY
        cameraY = 0f
        player.y -= shiftAmount
        
        // Maintain relative score
        highestYReached -= shiftAmount

        platforms.clear()
        particles.clear()
        landingEffects.clear()
        flyingRewards.clear()
        projectileManager.clear()
        powerUpManager.powerUps.clear()
        threatManager.clear()
        groundY -= shiftAmount
    }

    fun continueRun() {
        if (continuesUsed >= 1) return

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

        player.velocityX = 0f; player.velocityY = 0f
        player.fuel = player.maxFuel * 0.5f
        player.maxIntegrity = progressionManager.permanentMaxIntegrity + progressionManager.getHullBonusAmount()
        player.integrity = player.maxIntegrity * 0.5f
        player.shield = player.maxShield * 0.5f
        player.destructionTimer = 0f
        player.heat = 0f
        player.isOverheated = false
        player.lastPlatform = spawnPlatform
        player.invulnerabilityTimer = 3.0f

        spawnBurst(player.x, player.y - 100f, 40, SciFiWhite, 300f)
        screenShake = 15f
        impactFlashAlpha = 1.0f
        floatingTextManager.add(FloatingText("SYSTEM REBOOTED", player.x, player.y - 150f, color = SciFiCyan))

        continuesUsed++
        gameState = GameState.PLAYING
    }

    fun runGameLoop(currentTime: Long, isThrusting: Boolean, thrustTarget: Offset, inputProcessor: PlayerInputProcessor) {
        if (lastFrameTime == 0L) {
            lastFrameTime = currentTime
            return
        }
        val dt = min(0.033f, (currentTime - lastFrameTime) / 1_000_000_000f)
        lastFrameTime = currentTime

        if (gameState == GameState.PLAYING || gameState == GameState.ASCENSION_PROTOCOL) {
            val singularity = threatManager.activeThreats.find { it.definition.id == "BOSS_SINGULARITY" }
            inputProcessor.glitchFactor = singularity?.hudPullFactor ?: 0f

            val (effThrust, effTarget) = inputProcessor.processInput(isThrusting, thrustTarget, gameTime, player, dt)
            effectiveThrust = effThrust
            effectiveTarget = effTarget
            
            update(dt)
            gameTime += (dt * 1000).toLong()
        }
    }
}
