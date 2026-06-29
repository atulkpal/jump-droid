package com.ashwathai.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
        if (gameState == GameState.PAUSED) {
            PauseOverlay(
                showDevMenu = engine.showDevMenu,
                infiniteFuel = engine.infiniteFuel,
                disableHeat = engine.disableHeat,
                infiniteShield = player.infiniteShield,
                invincibleHull = player.invincibleHull,
                cheatsEnabled = true,
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

        if (gameState == GameState.UNLOCK && engine.unlockedRocket != null) {
            UnlockOverlay(
                unlockedRocket = engine.unlockedRocket!!,
                onConfirm = { engine.gameState = engine.preOverlayState; engine.unlockedRocket = null }
            )
        }

        if (engine.showAscensionCredits) {
            AscensionOverlay(onComplete = {
                engine.showAscensionCredits = false
                onMainMenu()
            })
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
            heat = player.heat, maxHeat = player.maxHeat, isOverheated = player.isOverheated,
            hud = hud
        )

        ComboDisplay(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding(),
            currentCombo = comboManager.currentCombo,
            comboTimeRemaining = comboManager.comboTimeRemaining,
            getWindowForCombo = { comboManager.getWindowForCombo(it) },
            zone = altitudeManager.currentZone
        )

        RightGauges(
            modifier = Modifier.align(Alignment.CenterEnd),
            shield = player.shield, maxShield = player.maxShield,
            integrity = player.integrity, maxIntegrity = player.maxIntegrity,
            hud = hud
        )

        NotificationLayer(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 180.dp),
            activeNotification = notificationManager.active,
            notificationAlpha = notificationManager.alpha,
            queue = notificationManager.queue,
            screenWidth = engine.screenWidth,
            zone = altitudeManager.currentZone
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
    }
}
