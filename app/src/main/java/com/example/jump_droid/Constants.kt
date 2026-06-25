package com.example.jump_droid

object Constants {
    const val BASE_GRAVITY = 1800f
    const val BASE_THRUST_POWER = 4000f
    const val BASE_FUEL_CONSUMPTION = 27f // Sprint E Balance: 3x more efficient (was 80f)
    const val FUEL_RECHARGE_RATE = 40f
    const val LANDING_BOUNCE_VELOCITY = -150f
    const val HORIZONTAL_DAMPING = 0.85f
    const val AIR_FRICTION = 0.98f
    const val ROCKET_WIDTH = 40f
    const val ROCKET_HEIGHT = 70f
    const val PLATFORM_HEIGHT = 20f
    const val SCREEN_PADDING = 20f
    const val MOVING_PLATFORM_CHANCE = 0.2f
    const val BASE_FUEL_CAPACITY = 100f
    const val MAX_FUEL_CAPACITY_LIMIT = 300f // Sprint E: Capacity ceiling
    const val MAX_HEAT = 100f
    const val MAX_HEAT_CAPACITY_LIMIT = 300f // Sprint E: Future-proofing heat upgrades
    const val HEAT_GENERATION_RATE = 13f // Sprint E Balance: 3x slower build-up (was 40f)
    const val COOLING_RATE = 20f
    const val OVERHEAT_COOLDOWN_TIME = 2.0f

    // EPIC 5: Survival System Constants
    const val BASE_INTEGRITY = 100f
    const val BASE_SHIELD = 50f
    const val SHIELD_REGEN_RATE = 1.0f // Reduced further to 1.0 (was 1.6)
    const val SHIELD_REGEN_DELAY = 4f // Seconds to wait after damage
    const val SURVIVAL_CRITICAL_THRESHOLD = 0.25f

    // --- Altitude Zones ---
    const val ZONE_THRESHOLD_EARTH = 0
    const val ZONE_THRESHOLD_CLOUD_LAYER = 500
    const val ZONE_THRESHOLD_UPPER_ATMOSPHERE = 1500
    const val ZONE_THRESHOLD_ORBIT = 4000
    const val ZONE_THRESHOLD_FOUNDRY = 6000
    const val ZONE_THRESHOLD_DEEP_SPACE = 8000
    const val ZONE_THRESHOLD_CHRONO_RIFT = 13000
    const val ZONE_THRESHOLD_VOID = 15000
    const val ZONE_THRESHOLD_BEYOND = 25000
    const val ZONE_THRESHOLD_GATE = 45000
    const val ZONE_THRESHOLD_CONSTRUCT = 70000
    const val ZONE_THRESHOLD_SINGULARITY = 100000
}
