package com.example.jump_droid

import com.example.jump_droid.Constants.ZONE_THRESHOLD_CLOUD_LAYER
import com.example.jump_droid.Constants.ZONE_THRESHOLD_DEEP_SPACE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_EARTH
import com.example.jump_droid.Constants.ZONE_THRESHOLD_ORBIT
import com.example.jump_droid.Constants.ZONE_THRESHOLD_UPPER_ATMOSPHERE
import com.example.jump_droid.Constants.ZONE_THRESHOLD_VOID

enum class AltitudeZone(
    val threshold: Int,
    // Future Expansion Placeholders
    val zoneName: String,
    val zoneDescription: String = "",
    // val zoneColorPalette: ColorPalette? = null,
    // val zoneMusic: SoundAsset? = null,
    // val zoneBossPool: List<BossType> = emptyList(),
    // val zoneArtifactPool: List<ArtifactType> = emptyList(),
    // val zonePowerupPool: List<PowerUpType> = emptyList(),
    // val zoneBackgroundTheme: Theme? = null
) {
    EARTH(ZONE_THRESHOLD_EARTH, "Earth"),
    CLOUD_LAYER(ZONE_THRESHOLD_CLOUD_LAYER, "Cloud Layer"),
    UPPER_ATMOSPHERE(ZONE_THRESHOLD_UPPER_ATMOSPHERE, "Upper Atmosphere"),
    ORBIT(ZONE_THRESHOLD_ORBIT, "Orbit"),
    DEEP_SPACE(ZONE_THRESHOLD_DEEP_SPACE, "Deep Space"),
    VOID(ZONE_THRESHOLD_VOID, "The Void");

    companion object {
        fun fromAltitude(altitude: Int): AltitudeZone {
            return entries.last { altitude >= it.threshold }
        }
    }
}
