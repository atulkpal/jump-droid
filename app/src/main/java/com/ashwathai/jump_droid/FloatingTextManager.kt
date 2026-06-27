package com.ashwathai.jump_droid

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
            if (ft.sourceThreat != null) {
                if (ft.sourceThreat!!.state != ThreatState.DESTROYED && ft.sourceThreat!!.isTracking) {
                    ft.x = ft.sourceThreat!!.x
                    ft.y = ft.sourceThreat!!.y + ft.anchorOffsetY
                } else {
                    iterator.remove()
                    continue
                }
            } else {
                ft.y -= 50f * dt
            }
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
