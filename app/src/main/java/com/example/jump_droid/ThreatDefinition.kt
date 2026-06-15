package com.example.jump_droid

/**
 * Core definition of a threat entity.
 * This is a data template used to instantiate actual threats in the game world.
 */
data class ThreatDefinition(
    val id: String,
    val name: String,
    val description: String,
    val type: ThreatType,
    val tier: ThreatTier,
    val spawnRules: ThreatSpawnRules,
    
    // Future expansion points
    val discoveryType: DiscoveryType? = null,
    val baseHealth: Float = 100f,
    val damage: Float = 10f,
    val speed: Float = 100f,
    
    // Metadata for registry and lookup
    val tags: List<String> = emptyList()
)
