package com.ashwathai.jump_droid

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.games.PlayGamesSdk
import com.google.firebase.FirebaseApp
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

val LocalAnalytics = staticCompositionLocalOf<GameAnalytics> { error("No Analytics provided") }

class MainActivity : ComponentActivity() {
    private var gameEngine: GameEngine? = null
    private lateinit var analytics: GameAnalytics
    private lateinit var playerAnalytics: PlayerAnalyticsManager
    private val lifecycleScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        PlayGamesSdk.initialize(this)
        val firebaseAnalytics = FirebaseGameAnalytics(this)
        playerAnalytics = PlayerAnalyticsManager(this, firebaseAnalytics)
        analytics = playerAnalytics
        MobileAds.initialize(this) {}
        
        // Experimental: Asset Rendering Init
        val sharedPrefs = getSharedPreferences("jump_droid_prefs", MODE_PRIVATE)
        DevConfig.RENDER_MODE_ASSETS = sharedPrefs.getBoolean("render_mode_assets", false)

        enableEdgeToEdge()
        setContent {
            val engine = remember { GameEngine(this, analytics) }
            gameEngine = engine
            var showRegistration by remember { mutableStateOf(!playerAnalytics.isConsented) }
            LaunchedEffect(Unit) { engine.loginManager.restoreSession() }
            if (showRegistration) {
                BetaRegistrationDialog(
                    onRegister = { email, name, phone ->
                        playerAnalytics.registerTester(email, name, phone)
                        showRegistration = false
                    },
                    onSkip = {
                        playerAnalytics.skipRegistration()
                        showRegistration = false
                    }
                )
            }
            CompositionLocalProvider(LocalAnalytics provides analytics) {
                JumpDroidApp(
                    engine = engine,
                    onWipeData = {
                        engine.progressionManager.wipeData(isFactoryReset = false)
                        engine.restartGame()
                    },
                    onFactoryReset = {
                        // 1. Sign out
                        engine.loginManager.signOut()
                        
                        // 2. Wipe main progression
                        engine.progressionManager.wipeData(isFactoryReset = true)
                        
                        // 3. Clear all secondary pref files
                        listOf(
                            "LoginPrefs",
                            "jump_droid_prefs",
                            "PlayerAnalyticsPrefs",
                            "RemoteConfigPrefs",
                            "fcm_prefs"
                        ).forEach { prefName ->
                            getSharedPreferences(prefName, MODE_PRIVATE).edit().clear().apply()
                        }
                        
                        // 4. Reset engine and go home
                        engine.restartGame()
                    },
                    onExit = { finish() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        playerAnalytics.onAppBackground()
        gameEngine?.let { engine ->
            engine.soundManager.pauseAll()
            lifecycleScope.launch {
                engine.cloudSyncManager.saveToCloud()
                engine.leaderboardManager.submitScore(engine.progressionManager.highScore, engine.progressionManager.highScore)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        playerAnalytics.onAppForeground()
        gameEngine?.soundManager?.resumeAll()
    }
}

@Composable
fun BetaRegistrationDialog(
    onRegister: (email: String, name: String, phone: String) -> Unit,
    onSkip: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SciFiWhite,
        unfocusedTextColor = SciFiWhite,
        focusedLabelColor = SciFiCyan,
        unfocusedLabelColor = SciFiWhite.copy(alpha = 0.7f),
        cursorColor = SciFiCyan,
        focusedBorderColor = SciFiCyan,
        unfocusedBorderColor = SciFiWhite.copy(alpha = 0.3f),
        focusedContainerColor = Color(0xFF2A2A2A),
        unfocusedContainerColor = Color(0xFF2A2A2A)
    )

    Dialog(onDismissRequest = { /* block back press */ }) {
        Surface(
            color = SciFiSurface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BETA TESTER REGISTRATION",
                    color = SciFiCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Welcome to the Jump Droid Closed Beta. Register once to help us improve the game.",
                    color = SciFiWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = false },
                    label = { Text("Email *") },
                    singleLine = true,
                    isError = emailError,
                    supportingText = if (emailError) {{ Text("Invalid email") }} else null,
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Used only for closed beta testing.",
                    color = SciFiWhite.copy(alpha = 0.35f),
                    fontSize = 10.sp
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            emailError = true
                        } else {
                            onRegister(email.trim(), name.trim(), phone.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("START TESTING", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onSkip) {
                    Text("Skip", color = SciFiWhite.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun JumpDroidApp(
    engine: GameEngine,
    onWipeData: () -> Unit,
    onFactoryReset: () -> Unit,
    onExit: () -> Unit
) {
    val navController = rememberNavController()
    val analytics = LocalAnalytics.current

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            engine.loginManager.onSignInResult(result.data)
        }
    }

    // --- Audio: Menu Music Management ---
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route ?: "unknown"
            analytics.logScreenView(route, route.replaceFirstChar { it.uppercase() })
            if (route == "game") {
                engine.soundManager.stopMusic()
            } else {
                engine.soundManager.killAllMusic()
                engine.soundManager.playMenuMusic()
            }
        }
    }

    NavHost(navController = navController, startDestination = "title") {
        composable(
            route = "title",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            TitleScreen(
                onNavigate = { state ->
                    when (state) {
                        GameState.PLAYING -> {
                            engine.gameState = GameState.PLAYING
                            engine.restartGame()
                            navController.navigate("game")
                        }
                        GameState.MAIN_MENU -> navController.navigate("main_menu")
                        GameState.HANGAR -> navController.navigate("hangar")
                        GameState.ARCHIVE -> navController.navigate("archive")
                        GameState.SETTINGS -> navController.navigate("settings")
                        GameState.ABOUT -> navController.navigate("about")
                        else -> navController.navigate("main_menu")
                    }
                },
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager
            )
        }
        composable(
            route = "main_menu",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            MainMenuScreen(
                onLaunch = { 
                    engine.gameState = GameState.PLAYING
                    engine.restartGame(GameMode.STANDARD)
                    navController.navigate("game") 
                },
                onLaunchZen = {
                    engine.gameState = GameState.ZEN
                    engine.restartGame(GameMode.ZEN)
                    navController.navigate("game")
                },
                onNavigate = { state ->
                    when (state) {
                        GameState.HANGAR -> navController.navigate("hangar")
                        GameState.ARCHIVE -> { engine.codexNotification = null; navController.navigate("archive") }
                        GameState.SETTINGS -> navController.navigate("settings")
                        GameState.ABOUT -> navController.navigate("about")
                        GameState.MISSIONS -> navController.navigate("missions")
                        GameState.LEADERBOARD -> navController.navigate("leaderboard")
                        GameState.SHOP -> navController.navigate("shop")
                        GameState.MULTIPLAYER -> navController.navigate("multiplayer")
                        else -> {}
                    }
                },
                onExit = onExit,
                highScore = engine.progressionManager.highScore,
                onPrestige = { 
                    engine.restartGame()
                    navController.navigate("title") 
                },
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager,
                archiveUnreadCount = engine.discoveryManager.getUnreadCount(),
                hasNewEntries = engine.codexNotification != null || engine.discoveryManager.getUnreadCount() > 0,
                progressionManager = engine.progressionManager,
                loginManager = engine.loginManager,
                onSignIn = { signInLauncher.launch(engine.loginManager.getSignInIntent()) }
            )
        }
        composable(
            route = "game",
            enterTransition = { fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300)) }
        ) {
            GamePlayScreen(
                engine = engine, 
                onMainMenu = { 
                    navController.navigate("main_menu") {
                        popUpTo("title") { inclusive = false }
                    }
                },
                navController = navController
            )
        }
        dialog(
            route = "pause",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PauseOverlay(
                showDevMenu = engine.showDevMenu,
                infiniteFuel = engine.infiniteFuel,
                disableHeat = engine.disableHeat,
                infiniteShield = engine.player.infiniteShield,
                invincibleHull = engine.player.invincibleHull,
                cheatsEnabled = BuildConfig.DEBUG,
                onToggleDevMenu = { engine.showDevMenu = !engine.showDevMenu },
                onJumpToZone = { engine.jumpToZone(it); navController.popBackStack() },
                onSpawnDevThreat = { engine.spawnDevThreat(it); navController.popBackStack() },
                onSpawnDevPowerUp = { engine.spawnDevPowerUp(it); navController.popBackStack() },
                onSpawnDevPlatform = { engine.spawnDevPlatform(it); navController.popBackStack() },
                onToggleInfiniteFuel = { engine.infiniteFuel = !engine.infiniteFuel },
                onToggleDisableHeat = { engine.disableHeat = !engine.disableHeat },
                onToggleInfiniteShield = { engine.player.infiniteShield = !engine.player.infiniteShield },
                onToggleInvincibleHull = { engine.player.invincibleHull = !engine.player.invincibleHull },
                onUnlockAll = { engine.unlockAll(); navController.popBackStack() },
                onResume = { 
                    engine.gameState = engine.preOverlayState
                    navController.popBackStack() 
                },
                onRestart = { 
                    engine.restartGame()
                    navController.popBackStack() 
                },
                onMainMenu = {
                    navController.navigate("main_menu") {
                        popUpTo("title") { inclusive = false }
                    }
                },
                zone = engine.altitudeManager.currentZone,
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager,
                sharedPrefs = engine.sharedPrefs
            )
        }
        dialog(
            route = "game_over",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            GameOverOverlay(
                score = engine.score,
                highScore = engine.progressionManager.highScore,
                altitude = engine.runAltitude,
                altitudePoints = engine.altitudePoints,
                platformPoints = engine.platformPoints,
                bossPoints = engine.bossPoints,
                comboPoints = engine.comboPoints,
                progressionManager = engine.progressionManager,
                continuesUsed = engine.continuesUsed,
                isPremiumUser = engine.isPremiumUser,
                runBossesDefeated = engine.runBossesDefeated,
                bestComboThisRun = engine.comboManager.bestComboThisRun,
                onContinue = { 
                    engine.continueRun()
                    navController.popBackStack()
                },
                onRestart = { 
                    engine.restartGame()
                    navController.popBackStack()
                },
                onMainMenu = {
                    navController.navigate("main_menu") {
                        popUpTo("title") { inclusive = false }
                    }
                }
            )
        }
        dialog(
            route = "expedition_rewards",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ExpeditionRewardsOverlay(
                pendingUnlocks = engine.pendingUnlocks,
                progressionManager = engine.progressionManager,
                sessionStats = engine.getGameStats(),
                onClaimReward = { event ->
                    if (event is UnlockEvent.Mission) {
                        engine.missionManager.claimMissionRewards(event.mission.id, engine.player)
                    }
                    engine.soundManager.playSfx("sfx_collect_item")
                },
                onAllClaimed = {
                    engine.pendingUnlocks.clear()
                    engine.gameState = GameState.GAMEOVER
                    navController.popBackStack()
                }
            )
        }
        dialog(
            route = "tutorial",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            engine.activeDiscovery?.let { discovery ->
                TutorialOverlay(
                    activeDiscovery = discovery,
                    onAcknowledge = { 
                        engine.gameState = engine.preOverlayState
                        engine.activeDiscovery = null
                        navController.popBackStack()
                    }
                )
            }
        }
        dialog(
            route = "help",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            HelpOverlay(onDismiss = { 
                engine.gameState = engine.preOverlayState
                navController.popBackStack() 
            })
        }
        dialog(
            route = "unlock",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            engine.currentUnlockEvent?.let { event ->
                UnlockOverlay(
                    unlockEvent = event,
                    onConfirm = {
                        val target = if (engine.preOverlayState == GameState.UNLOCK || engine.preOverlayState == GameState.GAMEOVER) GameState.PLAYING else engine.preOverlayState
                        engine.gameState = target
                        engine.currentUnlockEvent = null
                        navController.popBackStack()
                    }
                )
            }
        }
        dialog(
            route = "continue_ready",
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ContinueReadyOverlay(
                onTap = { 
                    engine.gameState = GameState.PLAYING
                    navController.popBackStack() 
                }
            )
        }
        dialog("ascension_credits") {
            AscensionOverlay(onComplete = {
                engine.showAscensionCredits = false
                navController.navigate("main_menu") {
                    popUpTo("title") { inclusive = false }
                }
            })
        }
        composable(
            route = "hangar",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            HangarScreen(
                player = engine.player,
                highScore = engine.progressionManager.highScore,
                progressionManager = engine.progressionManager,
                loadoutManager = engine.loadoutManager,
                missionManager = engine.missionManager,
                sharedPrefs = engine.sharedPrefs,
                onNavigate = { state ->
                    when (state) {
                        GameState.PLAYING -> { 
                            engine.gameState = GameState.PLAYING
                            engine.restartGame()
                            navController.navigate("game") 
                        }
                        GameState.MAIN_MENU -> navController.navigate("main_menu")
                        else -> navController.navigate("main_menu")
                    }
                },
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager
            )
        }
        composable(
            route = "archive",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            ArchiveScreen(
                sharedPrefs = engine.sharedPrefs,
                discoveryManager = engine.discoveryManager,
                progressionManager = engine.progressionManager,
                onNavigate = { navController.popBackStack() }
            )
        }
        composable(
            route = "settings",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            SettingsScreen(
                sharedPrefs = engine.sharedPrefs,
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager,
                purchaseManager = engine.purchaseManager,
                onWipeData = {
                    onWipeData()
                    navController.navigate("title") {
                        popUpTo("title") { inclusive = true }
                    }
                },
                onFactoryReset = {
                    onFactoryReset()
                    navController.navigate("title") {
                        popUpTo("title") { inclusive = true }
                    }
                },
                onReturn = { navController.popBackStack() }
            )
        }
        composable(
            route = "about",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            AboutScreen(onDismiss = { navController.popBackStack() })
        }
        composable(
            route = "missions",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            MissionScreen(
                missionManager = engine.missionManager,
                player = engine.player,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(
            route = "leaderboard",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            LeaderboardScreen(
                leaderboardManager = engine.leaderboardManager,
                progressionManager = engine.progressionManager,
                cloudSyncManager = engine.cloudSyncManager,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(
            route = "shop",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            ShopScreen(
                progressionManager = engine.progressionManager,
                purchaseManager = engine.purchaseManager,
                soundManager = engine.soundManager,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(
            route = "multiplayer",
            enterTransition = { slideInHorizontally { it } + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)) }
        ) {
            MultiplayerScreen(
                engine = engine,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
