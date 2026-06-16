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
    BREAKABLE,
    PHASE,     // Task 1: Timing challenge
    FUEL,      // Task 2: Energy variants
    COOLING,   // Task 2
    STABILITY, // Task 2
    MAGNETIC   // Task 3: Gravity influence
}

class Platform(
    initialX: Float,
    val y: Float,
    val width: Float,
    val type: PlatformType = PlatformType.NORMAL,
    val isMoving: Boolean = false,
    initialSpeed: Float = 0f,
    val totalBreakTime: Float = 3f // Randomized between 2-4 in generator
) {
    var x by mutableFloatStateOf(initialX)
    var speed by mutableFloatStateOf(initialSpeed)
    
    // For BREAKABLE platforms
    var isBreaking by mutableStateOf(false)
    var crackTime by mutableFloatStateOf(0f)

    // For Boss interactions
    var isJammed by mutableStateOf(false)
    var jamTimer by mutableFloatStateOf(0f)

    // For mission/combo reliability
    var hasBeenLandedOn by mutableStateOf(false)
}
