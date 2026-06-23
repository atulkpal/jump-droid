package com.example.jump_droid

/**
 * Central source of truth for all mission definitions in Jump Droid.
 * Stores and provides lookup for mission templates.
 */
object MissionRegistry {
    private val missionTemplates = mutableMapOf<String, Mission>()

    init {
        // --- EXPLORATION ---
        register(Mission("exp_clouds", "Reach Cloud Layer", "Ascend to 500m.", MissionType.EXPLORATION, 500, MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)))
        register(Mission("exp_atmo", "Upper Atmosphere", "Reach 1500m.", MissionType.EXPLORATION, 1500, MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)))
        register(Mission("exp_orbit", "Reach Orbit", "Reach 4000m.", MissionType.EXPLORATION, 4000, MissionReward.Artifact(DiscoveryType.ART_ALLOY)))
        register(Mission("exp_space", "Deep Space", "Reach 8000m.", MissionType.EXPLORATION, 8000, MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)))
        register(Mission("exp_void", "The Void", "Reach 15000m.", MissionType.EXPLORATION, 15000, MissionReward.Artifact(DiscoveryType.ART_BEACON)))

        // --- PLATFORMING ---
        register(Mission("plat_land_5", "Basic Landing", "Land on 5 platforms.", MissionType.PLATFORMING, 5, MissionReward.PowerUp(PowerUpType.FUEL_TANK)))
        register(Mission("plat_moving", "Acrobat", "Land on 3 moving platforms.", MissionType.PLATFORMING, 3, MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)))
        register(Mission("plat_land_20", "Platform Veteran", "Land on 20 platforms.", MissionType.PLATFORMING, 20, MissionReward.PowerUp(PowerUpType.HEAT_SINK)))
        register(Mission("plat_boost", "Springboard", "Use 3 boost platforms.", MissionType.PLATFORMING, 3, MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)))
        register(Mission("plat_land_50", "Sky Dweller", "Land on 50 platforms.", MissionType.PLATFORMING, 50, MissionReward.PowerUp(PowerUpType.FUEL_TANK, 2)))

        // --- SURVIVAL ---
        register(Mission("surv_air_30", "Flight Time", "Stay airborne for 30s.", MissionType.SURVIVAL, 30, MissionReward.PowerUp(PowerUpType.FUEL_TANK)))
        register(Mission("surv_cool", "Cool Engine", "Avoid overheat for 60s.", MissionType.SURVIVAL, 60, MissionReward.PowerUp(PowerUpType.HEAT_SINK)))
        register(Mission("surv_air_60", "Sky Master", "Stay airborne for 60s.", MissionType.SURVIVAL, 60, MissionReward.PowerUp(PowerUpType.HEAT_SINK)))
        register(Mission("surv_cool_2", "Thermal Ace", "Avoid overheat for 120s.", MissionType.SURVIVAL, 120, MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)))

        // --- DISCOVERY & BOSS ---
        register(Mission("disc_ray", "Sky Biologist", "Discover a Sky Ray.", MissionType.DISCOVERY, 1, MissionReward.Artifact(DiscoveryType.ART_DRONE)))
        register(Mission("boss_cruiser", "Cruiser Survivor", "Survive the Command Cruiser.", MissionType.BOSS, 1, MissionReward.Artifact(DiscoveryType.ART_BEACON)))
    }

    /**
     * Registers a new mission template.
     */
    fun register(mission: Mission) {
        missionTemplates[mission.id] = mission
    }

    /**
     * Retrieves a mission template by ID.
     */
    fun getById(id: String): Mission? = missionTemplates[id]

    /**
     * Returns all available mission templates.
     */
    fun getAllTemplates(): List<Mission> = missionTemplates.values.toList()

    /**
     * Clears all templates (useful for testing).
     */
    fun clear() {
        missionTemplates.clear()
    }
}
