package com.ashwathai.jump_droid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun GameScreen() {
    // This is now a legacy entry point. 
    // Navigation is handled in MainActivity via JumpDroidApp.
    val context = androidx.compose.ui.platform.LocalContext.current
    val engine = remember { GameEngine(context, FirebaseGameAnalytics(context)) }
    JumpDroidApp(engine = engine, onExit = {})
}
