package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class PlatformType {
    NORMAL,
    MOVING,
    BOOST,
    ICE,
    BREAKABLE
}

class Platform(
    initialX: Float,
    val y: Float,
    val width: Float,
    val type: PlatformType = PlatformType.NORMAL,
    val isMoving: Boolean = false,
    initialSpeed: Float = 0f
) {
    var x by mutableFloatStateOf(initialX)
    var speed by mutableFloatStateOf(initialSpeed)
    
    // For BREAKABLE platforms
    var isBreaking by mutableStateOf(false)
    var crackTime by mutableFloatStateOf(0f)
}
