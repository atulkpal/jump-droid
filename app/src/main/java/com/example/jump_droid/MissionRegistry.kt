package com.example.jump_droid

/**
 * Migrated Mission Registry.
 * Contains 48 missions from feature/mission-system, aligned with EPIC 7 architecture.
 */
object MissionRegistry {
    private val missionTemplates = mutableMapOf<String, Mission>()

    init {
        // --- FLIGHT TIME (SURVIVAL) ---
        register(Mission("flight_time_1", "Sky Explorer", "Spend 5 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_1, 300,
            listOf(MissionReward.Cash(100), MissionReward.Artifact(DiscoveryType.LORE_ASCENSION)), icon = "\uD83D\uDE80"))
        register(Mission("flight_time_2", "Sky Wanderer", "Spend 12 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_2, 720,
            listOf(MissionReward.Cash(250), MissionReward.PowerUp(PowerUpType.FUEL_TANK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_1"), icon = "\uD83D\uDE80"))
        register(Mission("flight_time_3", "Sky Champion", "Spend 30 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_3, 1800,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_2"), icon = "\uD83D\uDE80"))
        register(Mission("flight_time_4", "Sky Legend", "Spend 60 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_4, 3600,
            listOf(MissionReward.Cash(1000), MissionReward.Unlock(RocketType.SCOUT)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_3"), icon = "\uD83D\uDE80"))

        // --- PLATFORM STAY (PLATFORMING) ---
        register(Mission("platform_stay_1", "Platform Dweller", "Spend 5 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_1, 300,
            listOf(MissionReward.Cash(80), MissionReward.Artifact(DiscoveryType.NORMAL_PLATFORM)), icon = "\uD83C\uDFD7\uFE0F"))
        register(Mission("platform_stay_2", "Platform Guardian", "Spend 15 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_2, 900,
            listOf(MissionReward.Cash(200), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "platform_stay_1"), icon = "\uD83C\uDFD7\uFE0F"))
        register(Mission("platform_stay_3", "Platform King", "Spend 30 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_3, 1800,
            listOf(MissionReward.Cash(400), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "platform_stay_2"), icon = "\uD83C\uDFD7\uFE0F"))
        register(Mission("platform_stay_4", "Platform Legend", "Spend 60 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_4, 3600,
            listOf(MissionReward.Cash(800), MissionReward.Unlock(RocketType.TANK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "platform_stay_3"), icon = "\uD83C\uDFD7\uFE0F"))

        // --- PLATFORM LANDINGS (PLATFORMING) ---
        register(Mission("plat_land_1", "Touchdown", "Land on 50 platforms total",
            MissionType.PLATFORMING, MissionCategory.LANDINGS, MissionTier.TIER_1, 50,
            listOf(MissionReward.Cash(100), MissionReward.Artifact(DiscoveryType.NORMAL_PLATFORM)), icon = "\uD83D\uDEEB"))
        register(Mission("plat_land_2", "Precision Pilot", "Land on 200 platforms total",
            MissionType.PLATFORMING, MissionCategory.LANDINGS, MissionTier.TIER_2, 200,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "plat_land_1"), icon = "\uD83D\uDEEB"))
        register(Mission("plat_land_3", "Surface Master", "Land on 1000 platforms total",
            MissionType.PLATFORMING, MissionCategory.LANDINGS, MissionTier.TIER_3, 1000,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "plat_land_2"), icon = "\uD83D\uDEEB"))

        // --- NO HEAT (SURVIVAL) ---
        register(Mission("no_heat_1", "Cool Operator", "Fly 5 minutes without overheating",
            MissionType.SURVIVAL, MissionCategory.NO_HEAT, MissionTier.TIER_1, 300,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.HEAT_SYSTEM)), icon = "\u2744\uFE0F"))
        register(Mission("no_heat_2", "Ice Veins", "Fly 12 minutes without overheating",
            MissionType.SURVIVAL, MissionCategory.NO_HEAT, MissionTier.TIER_2, 720,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.HEAT_SINK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "no_heat_1"), icon = "\u2744\uFE0F"))
        register(Mission("no_heat_3", "Absolute Zero", "Fly 25 minutes without overheating",
            MissionType.SURVIVAL, MissionCategory.NO_HEAT, MissionTier.TIER_3, 1500,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_BEACON)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "no_heat_2"), icon = "\u2744\uFE0F"))

        // --- FUEL EFFICIENCY (SURVIVAL) ---
        register(Mission("fuel_efficiency_1", "Fuel Saver", "Collect 10 fuel pickups",
            MissionType.SURVIVAL, MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_1, 10,
            listOf(MissionReward.Cash(80), MissionReward.Artifact(DiscoveryType.FUEL_TANK)), icon = "\u26FD"))
        register(Mission("fuel_efficiency_2", "Fuel Hoarder", "Collect 30 fuel pickups",
            MissionType.SURVIVAL, MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_2, 30,
            listOf(MissionReward.Cash(200), MissionReward.PowerUp(PowerUpType.FUEL_TANK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "fuel_efficiency_1"), icon = "\u26FD"))
        register(Mission("fuel_efficiency_3", "Fuel King", "Collect 75 fuel pickups",
            MissionType.SURVIVAL, MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_3, 75,
            listOf(MissionReward.Cash(450), MissionReward.Artifact(DiscoveryType.ART_DRONE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "fuel_efficiency_2"), icon = "\u26FD"))

        // --- COMBO STREAK (PLATFORMING) ---
        register(Mission("combo_streak_1", "Combo Starter", "Reach 20x combo",
            MissionType.PLATFORMING, MissionCategory.COMBO_STREAK, MissionTier.TIER_1, 20,
            listOf(MissionReward.Cash(100), MissionReward.Artifact(DiscoveryType.EFFICIENCY_SURVIVAL)), icon = "\uD83D\uDCA5"))
        register(Mission("combo_streak_2", "Combo Specialist", "Reach 50x combo",
            MissionType.PLATFORMING, MissionCategory.COMBO_STREAK, MissionTier.TIER_2, 50,
            listOf(MissionReward.Cash(250), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "combo_streak_1"), icon = "\uD83D\uDCA5"))
        register(Mission("combo_streak_3", "Combo Master", "Reach 100x combo",
            MissionType.PLATFORMING, MissionCategory.COMBO_STREAK, MissionTier.TIER_3, 100,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "combo_streak_2"), icon = "\uD83D\uDCA5"))

        // --- BOSS SLAYER (BOSS) ---
        register(Mission("boss_slayer_1", "Boss Buster", "Defeat 1 boss",
            MissionType.BOSS, MissionCategory.BOSS_SLAYER, MissionTier.TIER_1, 1,
            listOf(MissionReward.Cash(200), MissionReward.Artifact(DiscoveryType.THREAT_SENTINEL)), icon = "\uD83D\uDC7E"))
        register(Mission("boss_slayer_2", "Boss Hunter", "Defeat 3 bosses",
            MissionType.BOSS, MissionCategory.BOSS_SLAYER, MissionTier.TIER_2, 3,
            listOf(MissionReward.Cash(400), MissionReward.Unlock(RocketType.EXPERIMENTAL)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "boss_slayer_1"), icon = "\uD83D\uDC7E"))
        register(Mission("boss_slayer_3", "Boss Slayer", "Defeat 7 bosses",
            MissionType.BOSS, MissionCategory.BOSS_SLAYER, MissionTier.TIER_3, 7,
            listOf(MissionReward.Cash(700), MissionReward.Artifact(DiscoveryType.THREAT_VOID_ENGINE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "boss_slayer_2"), icon = "\uD83D\uDC7E"))

        // --- DISCOVERY HUNTER (DISCOVERY) ---
        register(Mission("discovery_hunter_1", "Discovery Novice", "Unlock 5 Codex entries",
            MissionType.DISCOVERY, MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_1, 5,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.LORE_ASCENSION)), icon = "\uD83D\uDD0D"))
        register(Mission("discovery_hunter_2", "Discovery Seeker", "Unlock 15 Codex entries",
            MissionType.DISCOVERY, MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_2, 15,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "discovery_hunter_1"), icon = "\uD83D\uDD0D"))
        register(Mission("discovery_hunter_3", "Discovery Master", "Unlock 30 Codex entries",
            MissionType.DISCOVERY, MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_3, 30,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_BEACON)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "discovery_hunter_2"), icon = "\uD83D\uDD0D"))

        // --- ALTITUDE CLIMBER (EXPLORATION) ---
        register(Mission("altitude_climber_1", "Altitude Rookie", "Reach 500m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_1, 500,
            listOf(MissionReward.Cash(100), MissionReward.Artifact(DiscoveryType.AREA_CLOUDS)), icon = "\u26F0\uFE0F"))
        register(Mission("altitude_climber_2", "Altitude Challenger", "Reach 1500m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_2, 1500,
            listOf(MissionReward.Cash(250), MissionReward.PowerUp(PowerUpType.ALTITUDE_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "altitude_climber_1"), icon = "\u26F0\uFE0F"))
        register(Mission("altitude_climber_3", "Altitude Champion", "Reach 4000m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_3, 4000,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.AREA_ORBIT)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "altitude_climber_2"), icon = "\u26F0\uFE0F"))
        register(Mission("altitude_climber_4", "Altitude Legend", "Reach 10000m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_4, 10000,
            listOf(MissionReward.Cash(1000), MissionReward.Artifact(DiscoveryType.AREA_VOID)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "altitude_climber_3"), icon = "\u26F0\uFE0F"))

        // --- MOMENTUM MASTER (EXPLORATION) ---
        register(Mission("momentum_master_1", "Momentum Builder", "Build 50 momentum",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_1, 50,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.LORE_LOST_FLEET)), icon = "\uD83C\uDF0A"))
        register(Mission("momentum_master_2", "Momentum Surfer", "Build 150 momentum",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_2, 150,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "momentum_master_1"), icon = "\uD83C\uDF0A"))
        register(Mission("momentum_master_3", "Momentum Champion", "Build 400 momentum",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_3, 400,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "momentum_master_2"), icon = "\uD83C\uDF0A"))

        // --- HAZARD SURVIVOR (SURVIVAL) ---
        register(Mission("hazard_survivor_1", "Hazard Survivor", "Survive 10 hazard hits",
            MissionType.SURVIVAL, MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_1, 10,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.HAZARD_LIGHTNING)), icon = "\u26A1"))
        register(Mission("hazard_survivor_2", "Hazard Veteran", "Survive 30 hazard hits",
            MissionType.SURVIVAL, MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_2, 30,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.SHIELD_CAPSULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "hazard_survivor_1"), icon = "\u26A1"))
        register(Mission("hazard_survivor_3", "Hazard Legend", "Survive 60 hazard hits",
            MissionType.SURVIVAL, MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_3, 60,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.HAZARD_RADIATION)), icon = "\u26A1"))

        // --- PERFECT RUN (SURVIVAL) ---
        register(Mission("perfect_run_1", "Perfect Run Novice", "No damage for 2 minutes",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_1, 120,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.LORE_LOGS)), icon = "\uD83C\uDFC6"))
        register(Mission("perfect_run_2", "Perfect Run Specialist", "No damage for 5 minutes",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_2, 300,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.SHIELD_CAPSULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "perfect_run_1"), icon = "\uD83C\uDFC6"))
        register(Mission("perfect_run_3", "Perfect Run Master", "No damage for 10 minutes",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_3, 600,
            listOf(MissionReward.Cash(700), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "perfect_run_2"), icon = "\uD83C\uDFC6"))

        // --- COLLECTOR (DISCOVERY) ---
        register(Mission("collector_1", "Collector Novice", "Collect 5 artifacts",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_1, 5,
            listOf(MissionReward.Cash(130), MissionReward.Artifact(DiscoveryType.LORE_SIGNAL)), icon = "\uD83D\uDCE6"))
        register(Mission("collector_2", "Collector Seeker", "Collect 15 artifacts",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_2, 15,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "collector_1"), icon = "\uD83D\uDCE6"))
        register(Mission("collector_3", "Collector Master", "Collect 30 artifacts",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_3, 30,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_DRONE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "collector_2"), icon = "\uD83D\uDCE6"))

        // --- BOOST CHAMPION (PLATFORMING) ---
        register(Mission("boost_champion_1", "Dash Initiate", "Perform 10 dashes in one run",
            MissionType.PLATFORMING, MissionCategory.BOOST_CHAMPION, MissionTier.TIER_1, 10,
            listOf(MissionReward.Cash(100), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)), icon = "\uD83D\uDCA8"))
        register(Mission("boost_champion_2", "Dash Master", "Perform 30 dashes in one run",
            MissionType.PLATFORMING, MissionCategory.BOOST_CHAMPION, MissionTier.TIER_2, 30,
            listOf(MissionReward.Cash(300), MissionReward.Artifact(DiscoveryType.BOOST_PLATFORM)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "boost_champion_1"), icon = "\uD83D\uDCA8"))

        // --- COMBO PRO (PLATFORMING) ---
        register(Mission("combo_pro_1", "Combo Pro Starter", "Maintain 20x combo for 30s",
            MissionType.PLATFORMING, MissionCategory.COMBO_PRO, MissionTier.TIER_1, 30,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.EFFICIENCY_SURVIVAL)), icon = "\uD83D\uDD17"))
        register(Mission("combo_pro_2", "Combo Pro Specialist", "Maintain 50x combo for 60s",
            MissionType.PLATFORMING, MissionCategory.COMBO_PRO, MissionTier.TIER_2, 60,
            listOf(MissionReward.Cash(300), MissionReward.Artifact(DiscoveryType.LORE_SIGNAL)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "combo_pro_1"), icon = "\uD83D\uDD17"))

        // --- HIDDEN MISSIONS ---
        register(Mission("hidden_long_haul", "The Long Haul", "Complete a single run lasting 10+ minutes",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_3, 600,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.REACH_ALTITUDE, 3000f), icon = "\uD83D\uDE80", isHidden = true,
            crypticHint = "A whisper from the deep... — Survive beyond 3,000m"))
        register(Mission("hidden_heat_junkie", "Heat Junkie", "Overheat 5 times total",
            MissionType.SURVIVAL, MissionCategory.OVERHEAT, MissionTier.TIER_2, 5,
            listOf(MissionReward.Cash(300), MissionReward.Artifact(DiscoveryType.OVERHEAT_SYSTEM)),
            MissionUnlockCondition(MissionUnlockType.REACH_ALTITUDE, 1500f), icon = "\u2744\uFE0F", isHidden = true,
            crypticHint = "The core runs hot... — Push the limits below 1,500m"))
        register(Mission("hidden_near_death", "Near-Death Experience", "Complete a run below 10% health",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_2, 1,
            listOf(MissionReward.Cash(400), MissionReward.Artifact(DiscoveryType.LORE_LOGS)),
            MissionUnlockCondition(MissionUnlockType.REACH_ALTITUDE, 1000f), icon = "\uD83C\uDFC6", isHidden = true,
            crypticHint = "The edge of destruction... — Test fate below 1,000m"))
        register(Mission("hidden_void_walker", "Void Walker", "Reach the Void biome",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_3, 5000,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.AREA_VOID)),
            MissionUnlockCondition(MissionUnlockType.REACH_BIOME, 0f), icon = "\u26F0\uFE0F", isHidden = true,
            crypticHint = "The sky has a floor... — Find it."))
        register(Mission("hidden_perfect_storm", "Perfect Storm", "Complete 5 missions without dying",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_3, 5,
            listOf(MissionReward.Cash(750), MissionReward.Artifact(DiscoveryType.HAZARD_LIGHTNING)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_2"), icon = "\uD83C\uDFC6", isHidden = true,
            crypticHint = "Perfection is a pattern... — Complete flight_time_2 first"))
        register(Mission("hidden_artifact_hunter", "Artifact Hunter", "Collect 5 different artifact types",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_2, 5,
            listOf(MissionReward.Cash(700), MissionReward.Artifact(DiscoveryType.ART_DRONE)),
            MissionUnlockCondition(MissionUnlockType.COLLECT_ARTIFACT, 3f), icon = "\uD83D\uDCE6", isHidden = true,
            crypticHint = "They left things behind... — Collect 3 artifacts to begin"))
        register(Mission("hidden_momentum_legend", "Momentum Legend", "Build 400 momentum in one run",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_3, 400,
            listOf(MissionReward.Cash(800), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "momentum_master_2"), icon = "\uD83C\uDF0A", isHidden = true,
            crypticHint = "Speed is weight... — Master momentum_master_2 first"))
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
