package com.example.jump_droid

import androidx.compose.runtime.mutableStateListOf

/**
 * Manages the lifecycle of floating text popups.
 * Extracted from GameScreen.kt as part of Sprint T3.
 */
class FloatingTextManager {
    val texts = mutableStateListOf<FloatingText>()

    /**
     * Adds a new floating text instance.
     */
    fun add(text: FloatingText) {
        texts.add(text)
    }

    /**
     * Updates the life and position of all active floating texts.
     * Should be called once per frame from the game loop.
     */
    fun update(dt: Float) {
        val iterator = texts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            ft.life -= dt
            ft.y -= 50f * dt // Upward drift
            if (ft.life <= 0) {
                iterator.remove()
            }
        }
    }

    /**
     * Resets the manager state.
     */
    fun clear() {
        texts.clear()
    }
}
