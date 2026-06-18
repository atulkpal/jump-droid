package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Encapsulates the notification queue and lifecycle management.
 * Extracted from GameScreen.kt as part of Sprint T3.
 */
class NotificationManager {
    // State variables
    val queue = mutableStateListOf<String>()
    var active by mutableStateOf<String?>(null)
    var alpha by mutableFloatStateOf(0f)
    var timer by mutableFloatStateOf(0f)

    /**
     * Adds a new message to the display queue.
     */
    fun post(message: String) {
        queue.add(message)
    }

    /**
     * Updates timers and processes the queue.
     * Should be called once per frame from the game loop.
     */
    fun update(dt: Float) {
        if (active != null) {
            timer -= dt
            if (timer <= 0f) {
                alpha -= dt * 2f
                if (alpha <= 0f) {
                    active = null
                }
            }
        } else if (queue.isNotEmpty()) {
            active = queue.removeAt(0)
            alpha = 1f
            timer = 2.0f // Display duration
        }
    }

    /**
     * Immediately displays a notification, bypassing the queue.
     */
    fun showImmediately(message: String, initialAlpha: Float = 1.0f) {
        active = message
        alpha = initialAlpha
        timer = 2.0f
    }

    /**
     * Resets the manager state.
     */
    fun clear() {
        queue.clear()
        active = null
        alpha = 0f
        timer = 0f
    }
}
