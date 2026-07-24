package com.ashwathai.jump_droid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.core.content.edit
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

val LocalAnalytics = staticCompositionLocalOf<GameAnalytics> { error("No Analytics provided") }

class MainActivity : ComponentActivity() {
    private var gameEngine: GameEngine? = null
    private lateinit var analytics: GameAnalytics
    private lateinit var playerAnalytics: PlayerAnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val firebaseAnalytics = FirebaseGameAnalytics(this)
        playerAnalytics = PlayerAnalyticsManager(this, firebaseAnalytics)
        analytics = playerAnalytics
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        setContent {
            val engine = remember { GameEngine(this, analytics) }
            gameEngine = engine
            var showRegistration by remember { mutableStateOf(!playerAnalytics.isConsented) }
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
                JumpDroidApp(engine, onExit = { finish() })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        playerAnalytics.onAppBackground()
        gameEngine?.soundManager?.pauseAll()
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
fun JumpDroidApp(engine: GameEngine, onExit: () -> Unit) {
    val navController = rememberNavController()
    val analytics = LocalAnalytics.current

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
                soundManager = engine.soundManager
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
                    engine.restartGame()
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
                hasNewEntries = engine.codexNotification != null || engine.discoveryManager.getUnreadCount() > 0,
                progressionManager = engine.progressionManager
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
                onMainMenu = { navController.navigate("main_menu") }
            )
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
                soundManager = engine.soundManager
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
                    engine.restartGame()
                    navController.navigate("title") 
                },
                onFactoryReset = {
                    engine.sharedPrefs.edit { clear() }
                    engine.restartGame()
                    navController.navigate("title")
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
            LeaderboardScreen(onDismiss = { navController.popBackStack() })
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
    }
}
