package com.example.jump_droid

object Constants {
    const val BASE_GRAVITY = 1800f
    const val BASE_THRUST_POWER = 4000f
    const val BASE_FUEL_CONSUMPTION = 80f
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
    const val MAX_HEAT = 100f
    const val HEAT_GENERATION_RATE = 40f
    const val COOLING_RATE = 20f
    const val OVERHEAT_COOLDOWN_TIME = 2.0f

    // --- Altitude Zones ---
    const val ZONE_THRESHOLD_EARTH = 0
    const val ZONE_THRESHOLD_CLOUD_LAYER = 500
    const val ZONE_THRESHOLD_UPPER_ATMOSPHERE = 1500
    const val ZONE_THRESHOLD_ORBIT = 4000
    const val ZONE_THRESHOLD_DEEP_SPACE = 8000
    const val ZONE_THRESHOLD_VOID = 15000
}
