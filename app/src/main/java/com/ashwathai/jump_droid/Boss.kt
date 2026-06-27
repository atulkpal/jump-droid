package com.ashwathai.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class BossBehavior {
    PLATFORM_CONSUMER,
    PROJECTILE_SHOOTER,
    ICE_CONVERTER,
    WIND_MAKER,
    ITEM_STEALER,
    VOID_SERPENT
}

class Boss(
    val name: String,
    val description: String,
    val threatRating: Int,
    val spawnWeight: Float,
    val behavior: BossBehavior,
    val discoveryType: DiscoveryType,
    initialHealth: Float,
    val countdownTime: Float,
    val rewardTier: Int
) {
    var health by mutableFloatStateOf(initialHealth)
    var timer by mutableFloatStateOf(countdownTime)
    var x by mutableFloatStateOf(0f)
    var y by mutableFloatStateOf(0f)
    var isActive by mutableStateOf(false)
}
