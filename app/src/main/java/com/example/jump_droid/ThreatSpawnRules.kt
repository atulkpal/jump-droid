package com.example.jump_droid

/**
 * Defines the conditions under which a threat can be spawned.
 */
data class ThreatSpawnRules(
    val minAltitude: Int = 0,
    val maxAltitude: Int = Int.MAX_VALUE,
    val allowedZones: List<AltitudeZone> = emptyList(),
    val spawnChance: Float = 0.1f, // 0.0 to 1.0
    val requiresNewDiscovery: Boolean = false // If true, only spawns if not already discovered
)
