package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ProjectileType {
    BOLT, MISSILE, BEAM, WAVE
}

enum class ProjectileOwner {
    THREAT, PLAYER
}

class Projectile(
    initialX: Float,
    initialY: Float,
    val vx: Float,
    val vy: Float,
    val type: ProjectileType,
    val owner: ProjectileOwner,
    val damage: Float,
    val color: Color = Color.Yellow,
    val size: Float = 10f,
    var life: Float = 5.0f
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)

    fun update(dt: Float) {
        x += vx * dt
        y += vy * dt
        life -= dt
    }
}
