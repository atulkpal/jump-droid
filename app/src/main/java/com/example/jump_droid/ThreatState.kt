package com.example.jump_droid

/**
 * Defines the lifecycle states of an active threat instance.
 */
enum class ThreatState {
    SPAWNING,  // Initializing, possibly performing an entry animation
    ACTIVE,    // Fully operational and interacting with the world
    DORMANT,   // Temporarily inactive (off-screen but tracked)
    DESTROYED  // Marked for cleanup and removal
}
