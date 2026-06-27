package com.ashwathai.jump_droid

import androidx.compose.runtime.Composable

@Composable
fun GameScreen() {
    // This is now a legacy entry point. 
    // Navigation is handled in MainActivity via JumpDroidApp.
    JumpDroidApp(onExit = {})
}
