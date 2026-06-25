package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import kotlin.math.sin

/**
 * Handles processing of raw player input, including latency buffering,
 * control inversion, and future glitch factors for EPIC 11.
 */
class PlayerInputProcessor(
    private val inputBufferManager: InputBufferManager
) {
    // EPIC 11: Glitch factor state
    var glitchFactor = 0f // 0 to 1
    private var driftTimer = 0f

    fun processInput(
        rawThrust: Boolean,
        rawTarget: Offset,
        gameTime: Long,
        player: Player,
        dt: Float
    ): Pair<Boolean, Offset> {
        // Record raw input into the buffer
        inputBufferManager.recordInput(rawThrust, rawTarget, gameTime)

        // Resolve latency (Sprint 8.5 logic)
        val (effThrust, effTarget) = inputBufferManager.getEffectiveThrust(gameTime, player.inputLatency)

        // 1. Glitch Delay (Task 1.3: intermittent 50ms drops)
        val isGlitched = glitchFactor > 0f && (gameTime / 100) % 20L == 0L
        val finalThrust = if (isGlitched) false else effThrust

        // 2. Control Inversion (Task 1.3/EPIC 11 extraction)
        var targetX = effTarget.x
        if (player.controlInversionTimer > 0f) {
            // Flip the target relative to the player to invert steering direction
            val dx = targetX - player.x
            targetX = player.x - dx
        }

        // 3. Horizontal Drift (Task 1.3: Singularity glitch)
        var finalTarget = Offset(targetX, effTarget.y)
        if (glitchFactor > 0f) {
            driftTimer += dt
            val drift = sin(driftTimer * 3.5f) * 250f * glitchFactor
            finalTarget = finalTarget.copy(x = finalTarget.x + drift)
        }

        return finalThrust to finalTarget
    }

    fun reset() {
        driftTimer = 0f
        glitchFactor = 0f
    }
}
