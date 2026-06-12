package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AltitudeManager {
    /**
     * The current altitude zone the player is in.
     * Observed by rendering, audio, and progression systems.
     */
    var currentZone by mutableStateOf(AltitudeZone.EARTH)
        private set

    /**
     * Callback for when the zone changes.
     * Useful for non-Compose systems (e.g. legacy audio, analytics).
     */
    var onZoneChanged: ((AltitudeZone) -> Unit)? = null

    /**
     * Updates the current zone based on altitude.
     * Detects transitions and triggers notifications.
     */
    fun updateAltitude(altitude: Int) {
        val newZone = AltitudeZone.fromAltitude(altitude)
        if (newZone != currentZone) {
            currentZone = newZone
            onZoneChanged?.invoke(newZone)
        }
    }

    /**
     * Helper to check if the player is in or above a specific zone.
     */
    fun isAtLeast(zone: AltitudeZone): Boolean {
        return currentZone.threshold >= zone.threshold
    }

    /**
     * Helper to check if the player is exactly in a specific zone.
     */
    fun isIn(zone: AltitudeZone): Boolean {
        return currentZone == zone
    }
}
