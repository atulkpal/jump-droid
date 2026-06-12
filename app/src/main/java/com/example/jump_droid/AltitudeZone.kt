package com.example.jump_droid

import com.example.jump_droid.Constants.ZONE_THRESHOLD_CLOUD_LAYER
import com.example.jump_droid.Constants.ZONE_THRESHOLD_DEEP_SPACE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_EARTH
import com.example.jump_droid.Constants.ZONE_THRESHOLD_ORBIT
import com.example.jump_droid.Constants.ZONE_THRESHOLD_UPPER_ATMOSPHERE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_VOID

enum class AltitudeZone(
    val threshold: Int,
    val zoneName: String,
    val subtitle: String
) {
    EARTH(ZONE_THRESHOLD_EARTH, "Earth", "The Journey Begins"),
    CLOUD_LAYER(ZONE_THRESHOLD_CLOUD_LAYER, "Cloud Layer", "Above The Clouds"),
    UPPER_ATMOSPHERE(ZONE_THRESHOLD_UPPER_ATMOSPHERE, "Upper Atmosphere", "Edge Of The Sky"),
    ORBIT(ZONE_THRESHOLD_ORBIT, "Orbit", "First Orbital Ascent"),
    DEEP_SPACE(ZONE_THRESHOLD_DEEP_SPACE, "Deep Space", "Uncharted Territory"),
    VOID(ZONE_THRESHOLD_VOID, "The Void", "Unknown Region Detected");

    companion object {
        fun fromAltitude(altitude: Int): AltitudeZone {
            return entries.last { altitude >= it.threshold }
        }
    }
}
