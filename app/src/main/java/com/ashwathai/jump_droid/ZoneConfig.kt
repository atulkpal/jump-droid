package com.ashwathai.jump_droid

/**
 * Configuration for a specific altitude zone.
 * Defines threat spawn weighting, global intensity, and boss progression.
 */
data class ZoneConfig(
    val zone: AltitudeZone,
    val spawnWeights: Map<String, Float>,
    val intensity: Float,
    val bossMilestone: String? = null
)
