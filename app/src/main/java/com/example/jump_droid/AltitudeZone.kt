package com.example.jump_droid

import com.example.jump_droid.Constants.ZONE_THRESHOLD_CLOUD_LAYER
import com.example.jump_droid.Constants.ZONE_THRESHOLD_DEEP_SPACE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_EARTH
import com.example.jump_droid.Constants.ZONE_THRESHOLD_FOUNDRY
import com.example.jump_droid.Constants.ZONE_THRESHOLD_ORBIT
import com.example.jump_droid.Constants.ZONE_THRESHOLD_CHRONO_RIFT
import com.example.jump_droid.Constants.ZONE_THRESHOLD_UPPER_ATMOSPHERE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_VOID

import com.example.jump_droid.Constants.ZONE_THRESHOLD_BEYOND
import com.example.jump_droid.Constants.ZONE_THRESHOLD_CONSTRUCT
import com.example.jump_droid.Constants.ZONE_THRESHOLD_GATE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_SINGULARITY

enum class AltitudeZone(
    val threshold: Int,
    val zoneName: String,
    val subtitle: String
) {
    EARTH(ZONE_THRESHOLD_EARTH, "Earth", "The Journey Begins"),
    CLOUD_LAYER(ZONE_THRESHOLD_CLOUD_LAYER, "Cloud Layer", "Above The Clouds"),
    UPPER_ATMOSPHERE(ZONE_THRESHOLD_UPPER_ATMOSPHERE, "Upper Atmosphere", "Edge Of The Sky"),
    ORBIT(ZONE_THRESHOLD_ORBIT, "Orbit", "First Orbital Ascent"),
    THE_FOUNDRY(ZONE_THRESHOLD_FOUNDRY, "The Foundry", "Automated Manufacturing Belt"),
    DEEP_SPACE(ZONE_THRESHOLD_DEEP_SPACE, "Deep Space", "Uncharted Territory"),
    CHRONO_RIFT(ZONE_THRESHOLD_CHRONO_RIFT, "Chrono-Rift", "The Fractured Passage"),
    VOID(ZONE_THRESHOLD_VOID, "The Void", "Unknown Region Detected"),
    THE_BEYOND(ZONE_THRESHOLD_BEYOND, "The Beyond", "Where Matter Blurs"),
    STELLAR_GATE(ZONE_THRESHOLD_GATE, "Stellar Gate", "The Artificial Sky"),
    ANCIENT_CONSTRUCT(ZONE_THRESHOLD_CONSTRUCT, "Ancient Construct", "The Heart In The Static"),
    SINGULARITY(ZONE_THRESHOLD_SINGULARITY, "Singularity", "Beyond Reality");

    companion object {
        fun fromAltitude(altitude: Int): AltitudeZone {
            return entries.last { altitude >= it.threshold }
        }
    }
}
