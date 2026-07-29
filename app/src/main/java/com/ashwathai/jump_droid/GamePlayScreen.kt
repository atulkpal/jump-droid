package com.ashwathai.jump_droid

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Typeface
import android.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.navigation.NavController
import com.ashwathai.jump_droid.Constants.ROCKET_HEIGHT
import com.ashwathai.jump_droid.ui.theme.*

@Composable
fun GamePlayScreen(engine: GameEngine, onMainMenu: () -> Unit, navController: NavController) {
    val gameState = engine.gameState
    val altitudeManager = engine.altitudeManager
    val player = engine.player
    val comboManager = engine.comboManager
    
    val worldRenderer = remember { WorldRenderer() }
    val inputProcessor = remember { PlayerInputProcessor(engine.inputBufferManager) }

    val density = LocalDensity.current
    val context = LocalContext.current
    val activity = remember { context as? ComponentActivity }

    // Intercept back button during end-of-run states to prevent regressions
    if (gameState == GameState.EXPEDITION_REWARDS || gameState == GameState.GAMEOVER) {
        BackHandler {
            onMainMenu()
        }
    }

    LaunchedEffect(gameState) {
        when (gameState) {
            GameState.PAUSED -> navController.navigate("pause")
            GameState.GAMEOVER -> navController.navigate("game_over")
            GameState.EXPEDITION_REWARDS -> navController.navigate("expedition_rewards")
            GameState.TUTORIAL -> if (engine.activeDiscovery != null) navController.navigate("tutorial")
            GameState.HELP -> navController.navigate("help")
            GameState.UNLOCK -> if (engine.currentUnlockEvent != null) navController.navigate("unlock")
            GameState.CONTINUE_READY -> navController.navigate("continue_ready")
            else -> {}
        }
    }

    LaunchedEffect(engine.showAscensionCredits) {
        if (engine.showAscensionCredits) {
            navController.navigate("ascension_credits")
        }
    }

    DisposableEffect(gameState) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val isActive = gameState == GameState.PLAYING || gameState == GameState.ASCENSION_PROTOCOL || gameState == GameState.PAUSED || gameState == GameState.ZEN || gameState == GameState.EXPEDITION_REWARDS
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
                    if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL || engine.gameState == GameState.ZEN) {
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
            if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL || engine.gameState == GameState.ZEN) {
                if (engine.platforms.isEmpty()) {
                    engine.restartGame(engine.gameMode)
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
            worldRenderer.render(this, engine, context)
            
            // --- Flying Score Rendering (Cyber-Packet Style) ---
            engine.flyingScores.forEach { fs ->
                val targetX = size.width / 2f
                val targetY = 60f
                val currentX = fs.x + (targetX - fs.x) * fs.progress
                val currentY = fs.y + (targetY - fs.y) * fs.progress
                
                val alpha = if (fs.progress < 0.2f) fs.progress / 0.2f else if (fs.progress > 0.8f) (1f - fs.progress) / 0.2f else 1.0f
                
                // Motion blur effect (ghost frames)
                repeat(2) { i ->
                    val ghostProgress = (fs.progress - 0.05f * (i + 1)).coerceAtLeast(0f)
                    val gx = fs.x + (targetX - fs.x) * ghostProgress
                    val gy = fs.y + (targetY - fs.y) * ghostProgress
                    val gPaint = Paint().apply {
                        color = fs.color.toArgb()
                        textSize = (18f + (if (fs.value >= 100) 8f else 0f)) * (1f + ghostProgress * 0.3f)
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        this.alpha = (alpha * 60 / (i + 1)).toInt()
                        textAlign = Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText("[ +${fs.value} ]", gx, gy, gPaint)
                }

                // Main Packet Background
                val boxWidth = 80f + (if (fs.value >= 100) 40f else 0f)
                val boxHeight = 32f
                drawRect(
                    color = fs.color.copy(alpha = 0.15f * alpha),
                    topLeft = Offset(currentX - boxWidth/2, currentY - boxHeight/2),
                    size = Size(boxWidth, boxHeight)
                )
                
                // Main Futuristic Text
                val paint = Paint().apply {
                    color = fs.color.toArgb()
                    textSize = (18f + (if (fs.value >= 100) 8f else 0f)) * (1f + fs.progress * 0.3f)
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    setShadowLayer(15f, 0f, 0f, fs.color.toArgb())
                    this.alpha = (alpha * 255).toInt()
                    textAlign = Paint.Align.CENTER
                }
                
                drawContext.canvas.nativeCanvas.drawText(
                    "[ +${fs.value} ]",
                    currentX,
                    currentY + boxHeight/4, // Vertical centering adjustment
                    paint
                )
            }
        }

        // --- HUD Layer ---
        HUDLayer(engine, onNavigateArchive = { onMainMenu() })

        // --- Overlays (Transient/Visual Only - Mutually Exclusive) ---
        // Only render these background alerts if the game is actively running
        if (gameState == GameState.PLAYING || gameState == GameState.ASCENSION_PROTOCOL || gameState == GameState.ZEN) {
            when {
                engine.zoneTransitionTimer > 0 -> {
                    ZoneTransitionOverlay(
                        zone = engine.zoneTransitionTo,
                        timer = engine.zoneTransitionTimer,
                        gameTime = engine.gameTime
                    )
                }
                engine.bossArrivalEvent != null && engine.bossArrivalTimer > 0 -> {
                    BossArrivalOverlay(
                        event = engine.bossArrivalEvent!!,
                        timer = engine.bossArrivalTimer,
                        gameTime = engine.gameTime
                    )
                }
                engine.artifactLoreTimer > 0f && engine.artifactLoreType != null -> {
                    ArtifactLoreOverlay(
                        type = engine.artifactLoreType!!,
                        timer = engine.artifactLoreTimer,
                        totalDuration = 4f
                    )
                }
            }
        }

        engine.signalDecodedMissionName?.let { name ->
            // Replaced by notification + burst in GameEngine init callback
        }
    }
}

@Composable
fun HUDLayer(engine: GameEngine, onNavigateArchive: () -> Unit) {
    val player = engine.player
    val score = engine.visualScore
    val altitudeManager = engine.altitudeManager
    val comboManager = engine.comboManager
    val notificationManager = engine.notificationManager
    
    Box(Modifier.fillMaxSize()) {
        AltitudeDisplay(
            modifier = Modifier.align(Alignment.TopCenter),
            score = score, 
            altitude = engine.runAltitude,
            highScore = engine.progressionManager.highScore,
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
            hud = hud
        )
        RightGauges(
            modifier = Modifier.align(Alignment.CenterEnd),
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

        if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL || engine.gameState == GameState.ZEN) {
            MissionProgressCard(
                activeMissions = engine.missionManager.activeMissions,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp)
            )

            AchievementDeck(
                pendingUnlocks = engine.pendingUnlocks,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 80.dp)
            )

            CodexQuickAccess(
                discoveryManager = engine.discoveryManager,
                onNavigateArchive = onNavigateArchive,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp)
            )
        }

        if (engine.gameState == GameState.ZEN) {
            ZenMusicSelector(
                unlockedTracks = engine.progressionManager.unlockedMusicTracks,
                onTrackSelected = { engine.soundManager.playSpecificTrackByName(it) },
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 150.dp)
            )
        }

        if (engine.gameState != GameState.GAMEOVER && engine.majorWarningText != null) {
            val warnAlpha = (engine.majorWarningTimer / 2f).coerceIn(0f, 1f)
            Text(
                text = engine.majorWarningText!!,
                modifier = Modifier.align(Alignment.Center).graphicsLayer(alpha = warnAlpha),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 4.sp),
                color = SciFiRed,
                textAlign = TextAlign.Center
            )
        }

        if (engine.gameState != GameState.GAMEOVER) {
            FloatingTextsLayer(texts = engine.floatingTextManager.texts, cameraY = engine.cameraY)
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
fun ZenMusicSelector(
    unlockedTracks: Set<String>,
    onTrackSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Button(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.buttonColors(containerColor = SciFiPurple.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("MUSIC \u25B2", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
        }

        AnimatedVisibility(visible = expanded) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SciFiPurple.copy(alpha = 0.4f)),
                modifier = Modifier.padding(top = 4.dp).width(160.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    val tracks = listOf("DYNAMIC") + unlockedTracks.toList().sorted()
                    tracks.forEach { track ->
                        Text(
                            text = track.replace("bgm_", "").uppercase(),
                            color = SciFiWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackSelected(track); expanded = false }
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }
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
