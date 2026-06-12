package com.example.jump_droid

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

/**
 * Represents a discovery event in the game.
 */
sealed class DiscoveryEvent {
    data class Zone(val zone: AltitudeZone) : DiscoveryEvent()
    data class Artifact(val type: DiscoveryType) : DiscoveryEvent()
    data class Rocket(val type: RocketType) : DiscoveryEvent()
}

/**
 * Manages the discovery of zones, artifacts, and other secrets.
 * Handles persistence and event triggering.
 */
class DiscoveryManager(private val sharedPrefs: SharedPreferences) {

    var activeEvent by mutableStateOf<DiscoveryEvent?>(null)
        private set

    private var eventTimer = 0f

    fun update(dt: Float) {
        if (activeEvent != null) {
            eventTimer -= dt
            if (eventTimer <= 0) {
                activeEvent = null
            }
        }
    }

    /**
     * Checks if a zone has been discovered. If not, triggers the discovery event.
     */
    fun discoverZone(zone: AltitudeZone) {
        val key = "discovery_zone_${zone.name}"
        if (!sharedPrefs.getBoolean(key, false)) {
            sharedPrefs.edit { putBoolean(key, true) }
            triggerEvent(DiscoveryEvent.Zone(zone))
        }
    }

    /**
     * Generic discovery for future expansion (Artifacts, etc.)
     */
    fun discoverGeneric(type: DiscoveryType) {
        val key = "discovery_generic_${type.name}"
        if (!sharedPrefs.getBoolean(key, false)) {
            sharedPrefs.edit { putBoolean(key, true) }
            triggerEvent(DiscoveryEvent.Artifact(type))
        }
    }

    private fun triggerEvent(event: DiscoveryEvent) {
        activeEvent = event
        eventTimer = 4.0f // Display for 4 seconds
    }

    fun isDiscovered(zone: AltitudeZone): Boolean {
        return sharedPrefs.getBoolean("discovery_zone_${zone.name}", false)
    }
}
