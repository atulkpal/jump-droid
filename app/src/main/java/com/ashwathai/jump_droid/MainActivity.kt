package com.ashwathai.jump_droid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    private var gameEngine: GameEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        setContent {
            val engine = remember { GameEngine(this) }
            gameEngine = engine
            JumpDroidApp(engine, onExit = { finish() })
        }
    }

    override fun onPause() {
        super.onPause()
        gameEngine?.soundManager?.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        gameEngine?.soundManager?.resumeAll()
    }
}

@Composable
fun JumpDroidApp(engine: GameEngine, onExit: () -> Unit) {
    val navController = rememberNavController()

    // --- Audio: Menu Music Management ---
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
            if (route == "game") {
                engine.soundManager.stopMusic()
            } else {
                engine.soundManager.playMenuMusic()
            }
        }
    }

    NavHost(navController = navController, startDestination = "title") {
        composable("title") {
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
        composable("main_menu") {
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
                hasNewEntries = engine.codexNotification != null
            )
        }
        composable("game") {
            GamePlayScreen(
                engine = engine, 
                onMainMenu = { navController.navigate("main_menu") }
            )
        }
        composable("hangar") {
            HangarScreen(
                player = engine.player,
                highScore = engine.progressionManager.highScore,
                progressionManager = engine.progressionManager,
                sharedPrefs = engine.sharedPrefs,
                onNavigate = { state ->
                    when (state) {
                        GameState.PLAYING -> { 
                            engine.gameState = GameState.PLAYING
                            engine.restartGame()
                            navController.navigate("game") 
                        }
                        GameState.MAIN_MENU -> navController.navigate("main_menu")
                        GameState.LOADOUT -> navController.navigate("loadout")
                        else -> navController.navigate("main_menu")
                    }
                },
                soundManager = engine.soundManager
            )
        }
        composable("loadout") {
            LoadoutScreen(
                loadoutManager = engine.loadoutManager,
                progressionManager = engine.progressionManager,
                missionManager = engine.missionManager,
                onNavigate = { navController.popBackStack() }
            )
        }
        composable("archive") {
            ArchiveScreen(
                sharedPrefs = engine.sharedPrefs,
                discoveryManager = engine.discoveryManager,
                progressionManager = engine.progressionManager,
                onNavigate = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                sharedPrefs = engine.sharedPrefs,
                soundManager = engine.soundManager,
                hapticManager = engine.hapticManager,
                purchaseManager = engine.purchaseManager,
                onWipeData = { 
                    engine.restartGame()
                    navController.navigate("title") 
                },
                onReturn = { navController.popBackStack() }
            )
        }
        composable("about") {
            AboutScreen(onDismiss = { navController.popBackStack() })
        }
        composable("missions") {
            MissionScreen(
                missionManager = engine.missionManager,
                player = engine.player,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable("leaderboard") {
            LeaderboardScreen(onDismiss = { navController.popBackStack() })
        }
    }
}
