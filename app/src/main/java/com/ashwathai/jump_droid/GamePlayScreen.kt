package com.ashwathai.jump_droid

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.Constants.ROCKET_HEIGHT
import com.ashwathai.jump_droid.ui.theme.*

@Composable
fun GamePlayScreen(engine: GameEngine, onMainMenu: () -> Unit) {
    val gameState = engine.gameState
    val altitudeManager = engine.altitudeManager
    val player = engine.player
    val comboManager = engine.comboManager
    
    val worldRenderer = remember { WorldRenderer() }
    val inputProcessor = remember { PlayerInputProcessor(engine.inputBufferManager) }

    val density = LocalDensity.current
    val context = LocalContext.current
    val activity = remember { context as? ComponentActivity }

    DisposableEffect(gameState) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val isActive = gameState == GameState.PLAYING || gameState == GameState.ASCENSION_PROTOCOL || gameState == GameState.PAUSED
        if (isActive) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL) {
                        engine.isThrusting = true
                        engine.soundManager.startThrust()
                        engine.thrustTarget = down.position
                        player.squashStretch = 1.2f
                        engine.spawnBurst(player.x, player.y + ROCKET_HEIGHT / 2, 10, SciFiWhite.copy(alpha = 0.5f), 50f)
                    }
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyDown = event.changes.any { it.pressed }
                        if (!anyDown) {
                            engine.isThrusting = false
                            engine.soundManager.stopThrust()
                            break
                        }
                        engine.thrustTarget = event.changes.firstOrNull { it.pressed }?.position ?: engine.thrustTarget
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        LaunchedEffect(maxWidth, maxHeight) {
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            engine.screenWidth = w
            engine.screenHeight = h
            engine.groundY = h - ROCKET_HEIGHT - 50f
            
            // Critical Recovery: Ensure game is initialized with correct dimensions
            if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL) {
                if (engine.platforms.isEmpty()) {
                    engine.restartGame()
                }
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos { currentTime ->
                    engine.runGameLoop(currentTime, engine.isThrusting, engine.thrustTarget, inputProcessor)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            worldRenderer.render(this, engine)
        }

        // --- HUD Layer ---
        HUDLayer(engine)

        // --- Overlays ---
        if (gameState == GameState.CONTINUE_READY) {
            ContinueReadyOverlay(
                onTap = { engine.gameState = GameState.PLAYING }
            )
        }

        if (gameState == GameState.PAUSED) {
            PauseOverlay(
                showDevMenu = engine.showDevMenu,
                infiniteFuel = engine.infiniteFuel,
                disableHeat = engine.disableHeat,
                infiniteShield = player.infiniteShield,
                invincibleHull = player.invincibleHull,
                cheatsEnabled = BuildConfig.DEBUG,
                onToggleDevMenu = { engine.showDevMenu = !engine.showDevMenu },
                onJumpToZone = { engine.jumpToZone(it) },
                onSpawnDevThreat = { engine.spawnDevThreat(it) },
                onSpawnDevPowerUp = { engine.spawnDevPowerUp(it) },
                onSpawnDevPlatform = { engine.spawnDevPlatform(it) },
                onToggleInfiniteFuel = { engine.infiniteFuel = !engine.infiniteFuel },
                onToggleDisableHeat = { engine.disableHeat = !engine.disableHeat },
                onToggleInfiniteShield = { player.infiniteShield = !player.infiniteShield },
                onToggleInvincibleHull = { player.invincibleHull = !player.invincibleHull },
                onUnlockAll = { engine.unlockAll() },
                onResume = { engine.gameState = engine.preOverlayState },
                onRestart = { engine.restartGame() },
                onMainMenu = onMainMenu,
                zone = altitudeManager.currentZone,
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager,
                sharedPrefs = engine.sharedPrefs
            )
        }

        if (gameState == GameState.GAMEOVER) {
            GameOverOverlay(
                score = engine.score,
                highScore = engine.progressionManager.highScore,
                progressionManager = engine.progressionManager,
                continuesUsed = engine.continuesUsed,
                isPremiumUser = engine.isPremiumUser,
                runBossesDefeated = engine.runBossesDefeated,
                bestComboThisRun = engine.comboManager.bestComboThisRun,
                onContinue = { engine.continueRun() },
                onRestart = { engine.restartGame() },
                onMainMenu = onMainMenu
            )
        }

        if (gameState == GameState.TUTORIAL && engine.activeDiscovery != null) {
            TutorialOverlay(
                activeDiscovery = engine.activeDiscovery!!,
                onAcknowledge = { engine.gameState = engine.preOverlayState; engine.activeDiscovery = null }
            )
        }

        if (gameState == GameState.HELP) {
            HelpOverlay(onDismiss = { engine.gameState = engine.preOverlayState })
        }

        if (engine.bossArrivalEvent != null && engine.bossArrivalTimer > 0) {
            BossArrivalOverlay(
                event = engine.bossArrivalEvent!!,
                timer = engine.bossArrivalTimer,
                gameTime = engine.gameTime
            )
        }

        if (gameState == GameState.UNLOCK && engine.currentUnlockEvent != null) {
            UnlockOverlay(
                unlockEvent = engine.currentUnlockEvent!!,
                onConfirm = {
                    val target = if (engine.preOverlayState == GameState.UNLOCK || engine.preOverlayState == GameState.GAMEOVER) GameState.PLAYING else engine.preOverlayState
                    engine.gameState = target
                    engine.currentUnlockEvent = null
                }
            )
        }

        if (engine.showAscensionCredits) {
            AscensionOverlay(onComplete = {
                engine.showAscensionCredits = false
                onMainMenu()
            })
        }
        
        // Zone Transition Overlay
        if (engine.zoneTransitionTimer > 0) {
            ZoneTransitionOverlay(
                zone = engine.zoneTransitionTo,
                timer = engine.zoneTransitionTimer,
                gameTime = engine.gameTime
            )
        }

        engine.signalDecodedMissionName?.let { name ->
            // Replaced by notification + burst in GameEngine init callback
        }
    }
}

@Composable
fun HUDLayer(engine: GameEngine) {
    val player = engine.player
    val score = engine.score
    val altitudeManager = engine.altitudeManager
    val comboManager = engine.comboManager
    val notificationManager = engine.notificationManager
    
    Box(Modifier.fillMaxSize()) {
        AltitudeDisplay(
            modifier = Modifier.align(Alignment.TopCenter),
            score = score, highScore = engine.progressionManager.highScore,
            zone = altitudeManager.currentZone
        )

        TopRightUtilityButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding(),
            gameState = engine.gameState,
            onPause = { 
                if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL) {
                    engine.preOverlayState = engine.gameState
                    engine.gameState = GameState.PAUSED 
                }
            },
            onHelp = {
                if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL) {
                    engine.preOverlayState = engine.gameState
                    engine.gameState = GameState.HELP
                }
            }
        )

        val maxHudPull = engine.threatManager.activeThreats.maxOfOrNull { it.hudPullFactor } ?: 0f
        val hud = HudContext(gameTime = engine.gameTime, interferenceTimer = player.hudInterferenceTimer, zone = altitudeManager.currentZone, hudPullFactor = maxHudPull)

        LeftGauges(
            modifier = Modifier.align(Alignment.CenterStart),
            fuel = player.fuel, maxFuel = player.maxFuel,
            heat = player.heat, maxHeat = player.maxHeat,
            isOverheated = player.isOverheated,
            shield = player.shield, maxShield = player.maxShield,
            integrity = player.integrity, maxIntegrity = player.maxIntegrity,
            hud = hud
        )

        ComboDisplay(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding(),
            currentCombo = comboManager.currentCombo,
            comboTimeRemaining = comboManager.comboTimeRemaining,
            getWindowForCombo = { comboManager.getWindowForCombo(it) },
            zone = altitudeManager.currentZone
        )

        HeatEdgeGlow(heat = player.heat, maxHeat = player.maxHeat, isOverheated = player.isOverheated)

        NotificationLayer(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 180.dp),
            activeNotification = notificationManager.active,
            notificationAlpha = notificationManager.alpha,
            queue = notificationManager.queue,
            screenWidth = engine.screenWidth,
            zone = altitudeManager.currentZone
        )

        MissionProgressCard(
            activeMissions = engine.missionManager.activeMissions,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp)
        )

        if (engine.majorWarningText != null) {
            val warnAlpha = (engine.majorWarningTimer / 2f).coerceIn(0f, 1f)
            Text(
                text = engine.majorWarningText!!,
                modifier = Modifier.align(Alignment.Center).graphicsLayer(alpha = warnAlpha),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 4.sp),
                color = SciFiRed,
                textAlign = TextAlign.Center
            )
        }

        FloatingTextsLayer(texts = engine.floatingTextManager.texts, cameraY = engine.cameraY)

        val activeEvent = engine.discoveryManager.activeEvent
        if (activeEvent is DiscoveryEvent.Zone) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ZoneDiscoveryCard(activeEvent = activeEvent, score = engine.score)
            }
        }
    }
}

@Composable
private fun ZoneTransitionOverlay(
    zone: AltitudeZone,
    timer: Float,
    gameTime: Long
) {
    val totalDuration = 3f
    val progress = ((totalDuration - timer) / totalDuration).coerceIn(0f, 1f)
    val fadeIn = (progress / 0.2f).coerceIn(0f, 1f)
    val holdStart = 0.2f
    val holdEnd = 0.8f
    val fadeOut = ((progress - holdEnd) / 0.2f).coerceIn(0f, 1f)
    val alpha = when {
        progress < holdStart -> fadeIn
        progress > holdEnd -> 1f - fadeOut
        else -> 1f
    }.coerceIn(0f, 1f)

    val zoneAccent = when (zone) {
        AltitudeZone.EARTH, AltitudeZone.CLOUD_LAYER -> SciFiCyan
        AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT -> Color(0xFF00BFFF)
        AltitudeZone.THE_FOUNDRY -> SciFiOrange
        AltitudeZone.DEEP_SPACE, AltitudeZone.CHRONO_RIFT -> SciFiPurple
        AltitudeZone.VOID, AltitudeZone.THE_BEYOND -> Color(0xFFE91E63)
        AltitudeZone.STELLAR_GATE, AltitudeZone.ANCIENT_CONSTRUCT -> SciFiGold
        AltitudeZone.SINGULARITY -> SciFiRed
    }
    val textPulse = (kotlin.math.sin(gameTime / 200f) * 0.1f + 0.9f)

    Box(Modifier.fillMaxSize().graphicsLayer(alpha = alpha).background(Color.Black.copy(alpha = 0.5f * alpha)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                zone.zoneName.uppercase(),
                color = zoneAccent.copy(alpha = textPulse),
                fontSize = 24.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                zone.subtitle,
                color = SciFiWhite.copy(alpha = 0.6f * textPulse),
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.width(120.dp).height(2.dp).background(zoneAccent.copy(alpha = 0.4f * textPulse))
            )
        }
    }
}

@Composable
private fun HeatEdgeGlow(heat: Float, maxHeat: Float, isOverheated: Boolean) {
    val heatRatio = (heat / maxHeat).coerceIn(0f, 1f)
    if (heatRatio < 0.7f && !isOverheated) return

    val pulse = rememberInfiniteTransition(label = "HeatGlowPulse").animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "HeatGlowPulseVal")
    val glowAlpha = if (isOverheated) 0.5f else (heatRatio - 0.7f) / 0.3f * pulse.value
    val glowColor = if (isOverheated || heatRatio > 0.9f) SciFiRed else SciFiRed.copy(green = 0.3f)

    Box(Modifier.fillMaxSize().pointerInput(Unit) {}) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val edgeSize = w * 0.15f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(glowColor.copy(alpha = glowAlpha * 0.4f), Color.Transparent),
                    startY = 0f, endY = edgeSize
                ),
                size = Size(w, edgeSize)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, glowColor.copy(alpha = glowAlpha * 0.4f)),
                    startY = h - edgeSize, endY = h
                ),
                topLeft = Offset(0f, h - edgeSize),
                size = Size(w, edgeSize)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                    startX = 0f, endX = edgeSize
                ),
                size = Size(edgeSize, h)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, glowColor.copy(alpha = glowAlpha)),
                    startX = w - edgeSize, endX = w
                ),
                topLeft = Offset(w - edgeSize, 0f),
                size = Size(edgeSize, h)
            )
        }
    }
}
