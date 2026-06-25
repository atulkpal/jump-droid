package com.example.jump_droid

/**
 * Controls where a threat is positioned when spawned.
 */
enum class SpawnPosition {
    ABOVE_CAMERA,    // Center of screen, above camera (default for bosses)
    BELOW_RANDOM_X,  // Random X along a Y below the screen (default for hazards)
    SIDE_ENTRY,       // Enter from left/right, random Y in view
    ABOVE_SCREEN,     // Just above the screen top, random X
    RANDOM_SCREEN     // Random position anywhere on screen
}

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
    
    // Spawn behavior — data-driven positioning for EncounterDirector
    val spawnPosition: SpawnPosition = SpawnPosition.ABOVE_CAMERA,
    val spawnVx: Float = 0f,
    val spawnVy: Float = 0f,
    
    // Metadata for registry and lookup
    val tags: List<String> = emptyList()
)
