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
    data class Generic(val type: DiscoveryType) : DiscoveryEvent()
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
     * Discovers a zone and its corresponding DiscoveryType.
     */
    fun discoverZone(zone: AltitudeZone) {
        val discoveryType = when(zone) {
            AltitudeZone.EARTH -> DiscoveryType.AREA_EARTH
            AltitudeZone.CLOUD_LAYER -> DiscoveryType.AREA_CLOUDS
            AltitudeZone.UPPER_ATMOSPHERE -> DiscoveryType.AREA_ATMOSPHERE
            AltitudeZone.ORBIT -> DiscoveryType.AREA_ORBIT
            AltitudeZone.DEEP_SPACE -> DiscoveryType.AREA_SPACE
            AltitudeZone.VOID -> DiscoveryType.AREA_VOID
        }
        
        if (discover(discoveryType)) {
            triggerEvent(DiscoveryEvent.Zone(zone))
        }
    }

    /**
     * General discovery for any DiscoveryType.
     * Returns true if it's a new discovery.
     */
    fun discover(type: DiscoveryType): Boolean {
        val key = "discovery_$type"
        if (!sharedPrefs.getBoolean(key, false)) {
            sharedPrefs.edit { putBoolean(key, true) }
            return true
        }
        return false
    }

    private fun triggerEvent(event: DiscoveryEvent) {
        activeEvent = event
        eventTimer = 4.0f // Display for 4 seconds
    }

    fun isDiscovered(type: DiscoveryType): Boolean {
        return sharedPrefs.getBoolean("discovery_$type", false)
    }
}
