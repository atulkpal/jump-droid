package com.example.jump_droid

/**
 * Defines high-level categories of threats encountered during ascent.
 */
enum class ThreatType {
    HAZARD,     // Environmental stationary or rhythmic dangers (e.g. electrical clouds)
    ENEMY,      // Active entities that track or move towards the player
    MINI_BOSS,  // Elite encounters with higher durability and unique patterns
    BOSS        // Major zone-climax encounters
}
