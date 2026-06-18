package com.example.jump_droid

import kotlin.random.Random

/**
 * Encapsulates platform generation logic and streak tracking.
 * Extracted from GameScreen.kt as part of Sprint T3.
 */
class PlatformManager {
    var breakableStreak = 0
    var phaseStreak = 0
    var magneticStreak = 0

    /**
     * Generates a new platform based on current score and screen dimensions.
     */
    fun generate(score: Int, screenWidth: Float, lastY: Float): Platform {
        val difficulty = (score / 2000f).coerceIn(0f, 1f)
        val pWidth = (250f - (difficulty * 100f)).coerceAtLeast(100f)
        val gapY = 250f + (difficulty * 150f) + Random.nextFloat() * 100f
        val nextY = lastY - gapY
        val nextX = Random.nextFloat() * (screenWidth - pWidth)

        val type = when {
            score < 500 -> if (Random.nextFloat() < 0.2f) PlatformType.MOVING else PlatformType.NORMAL
            else -> {
                val rand = Random.nextFloat()
                when {
                    rand < 0.10f -> PlatformType.MOVING
                    rand < 0.22f -> PlatformType.ICE
                    rand < 0.35f -> PlatformType.BOOST
                    rand < 0.48f -> PlatformType.BREAKABLE
                    rand < 0.60f && phaseStreak < 2 -> PlatformType.PHASE 
                    rand < 0.70f && magneticStreak < 2 -> PlatformType.MAGNETIC
                    rand < 0.78f -> PlatformType.STABILITY
                    rand < 0.82f -> PlatformType.FUEL
                    rand < 0.86f -> PlatformType.COOLING
                    else -> PlatformType.NORMAL
                }
            }
        }

        // Update streaks
        if (type == PlatformType.BREAKABLE) breakableStreak++ else breakableStreak = 0
        if (type == PlatformType.PHASE) phaseStreak++ else phaseStreak = 0
        if (type == PlatformType.MAGNETIC) magneticStreak++ else magneticStreak = 0

        val isMoving = type == PlatformType.MOVING
        val speed = if (isMoving) (100f + (difficulty * 200f)) * (if (Random.nextBoolean()) 1f else -1f) else 0f
        val totalBreakTime = if (type == PlatformType.BREAKABLE) 1.5f + Random.nextFloat() * 1.5f else 3f

        return Platform(nextX, nextY, pWidth, type, isMoving, speed, totalBreakTime)
    }

    /**
     * Resets streak counters.
     */
    fun reset() {
        breakableStreak = 0
        phaseStreak = 0
        magneticStreak = 0
    }
}
