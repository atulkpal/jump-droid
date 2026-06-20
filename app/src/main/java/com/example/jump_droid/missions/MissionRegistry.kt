package com.example.jump_droid.missions

object MissionRegistry {
    private val allMissions: List<Mission> = listOf(
        // FLIGHT TIME (4 tiers)
        Mission("flight_time_1", MissionCategory.FLIGHT_TIME, MissionTier.TIER_1, "Sky Explorer", "Spend 5 minutes total in the air",
            MissionObjective(ObjectiveType.TOTAL_FLIGHT_TIME, 300f), Rewards(100, codexEntry = "codex_sky_explorer"),
            icon = "\uD83D\uDE80"),
        Mission("flight_time_2", MissionCategory.FLIGHT_TIME, MissionTier.TIER_2, "Sky Wanderer", "Spend 12 minutes total in the air",
            MissionObjective(ObjectiveType.TOTAL_FLIGHT_TIME, 720f), Rewards(250, powerup = "fuel_boost_1"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "flight_time_1"), icon = "\uD83D\uDE80"),
        Mission("flight_time_3", MissionCategory.FLIGHT_TIME, MissionTier.TIER_3, "Sky Champion", "Spend 30 minutes total in the air",
            MissionObjective(ObjectiveType.TOTAL_FLIGHT_TIME, 1800f), Rewards(500, artifact = "artifact_wind_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "flight_time_2"), icon = "\uD83D\uDE80"),
        Mission("flight_time_4", MissionCategory.FLIGHT_TIME, MissionTier.TIER_4, "Sky Legend", "Spend 60 minutes total in the air",
            MissionObjective(ObjectiveType.TOTAL_FLIGHT_TIME, 3600f), Rewards(1000, rocketSkin = "skin_sky_legend"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "flight_time_3"), icon = "\uD83D\uDE80"),

        // PLATFORM STAY (4 tiers)
        Mission("platform_stay_1", MissionCategory.PLATFORM_STAY, MissionTier.TIER_1, "Platform Dweller", "Spend 5 minutes on platforms",
            MissionObjective(ObjectiveType.TOTAL_PLATFORM_TIME, 300f), Rewards(80, codexEntry = "codex_platform_dweller"),
            icon = "\uD83C\uDFD7\uFE0F"),
        Mission("platform_stay_2", MissionCategory.PLATFORM_STAY, MissionTier.TIER_2, "Platform Guardian", "Spend 15 minutes on platforms",
            MissionObjective(ObjectiveType.TOTAL_PLATFORM_TIME, 900f), Rewards(200, powerup = "platform_boost"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "platform_stay_1"), icon = "\uD83C\uDFD7\uFE0F"),
        Mission("platform_stay_3", MissionCategory.PLATFORM_STAY, MissionTier.TIER_3, "Platform King", "Spend 30 minutes on platforms",
            MissionObjective(ObjectiveType.TOTAL_PLATFORM_TIME, 1800f), Rewards(400, artifact = "artifact_platform_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "platform_stay_2"), icon = "\uD83C\uDFD7\uFE0F"),
        Mission("platform_stay_4", MissionCategory.PLATFORM_STAY, MissionTier.TIER_4, "Platform Legend", "Spend 60 minutes on platforms",
            MissionObjective(ObjectiveType.TOTAL_PLATFORM_TIME, 3600f), Rewards(800, rocketSkin = "skin_platform_legend"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "platform_stay_3"), icon = "\uD83C\uDFD7\uFE0F"),

        // NO HEAT (3 tiers)
        Mission("no_heat_1", MissionCategory.NO_HEAT, MissionTier.TIER_1, "Cool Operator", "Fly 5 minutes without overheating",
            MissionObjective(ObjectiveType.ZERO_HEAT_TIME, 300f), Rewards(150, codexEntry = "codex_cool_operator"),
            icon = "\u2744\uFE0F"),
        Mission("no_heat_2", MissionCategory.NO_HEAT, MissionTier.TIER_2, "Ice Veins", "Fly 12 minutes without overheating",
            MissionObjective(ObjectiveType.ZERO_HEAT_TIME, 720f), Rewards(350, powerup = "heat_vent_plus"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "no_heat_1"), icon = "\u2744\uFE0F"),
        Mission("no_heat_3", MissionCategory.NO_HEAT, MissionTier.TIER_3, "Absolute Zero", "Fly 25 minutes without overheating",
            MissionObjective(ObjectiveType.ZERO_HEAT_TIME, 1500f), Rewards(600, artifact = "artifact_ice_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "no_heat_2"), icon = "\u2744\uFE0F"),

        // FUEL EFFICIENCY (3 tiers)
        Mission("fuel_efficiency_1", MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_1, "Fuel Saver", "Collect 10 fuel pickups",
            MissionObjective(ObjectiveType.FUEL_PICKUPS_COLLECTED, 10f), Rewards(80, codexEntry = "codex_fuel_saver"),
            icon = "\u26FD"),
        Mission("fuel_efficiency_2", MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_2, "Fuel Hoarder", "Collect 30 fuel pickups",
            MissionObjective(ObjectiveType.FUEL_PICKUPS_COLLECTED, 30f), Rewards(200, powerup = "fuel_capacity_plus"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "fuel_efficiency_1"), icon = "\u26FD"),
        Mission("fuel_efficiency_3", MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_3, "Fuel King", "Collect 75 fuel pickups",
            MissionObjective(ObjectiveType.FUEL_PICKUPS_COLLECTED, 75f), Rewards(450, artifact = "artifact_fuel_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "fuel_efficiency_2"), icon = "\u26FD"),

        // COMBO STREAK (3 tiers)
        Mission("combo_streak_1", MissionCategory.COMBO_STREAK, MissionTier.TIER_1, "Combo Starter", "Reach 20x combo",
            MissionObjective(ObjectiveType.MAX_COMBO, 20f), Rewards(100, codexEntry = "codex_combo_starter"),
            icon = "\uD83D\uDCA5"),
        Mission("combo_streak_2", MissionCategory.COMBO_STREAK, MissionTier.TIER_2, "Combo Specialist", "Reach 50x combo",
            MissionObjective(ObjectiveType.MAX_COMBO, 50f), Rewards(250, powerup = "combo_boost"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "combo_streak_1"), icon = "\uD83D\uDCA5"),
        Mission("combo_streak_3", MissionCategory.COMBO_STREAK, MissionTier.TIER_3, "Combo Master", "Reach 100x combo",
            MissionObjective(ObjectiveType.MAX_COMBO, 100f), Rewards(500, artifact = "artifact_combo_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "combo_streak_2"), icon = "\uD83D\uDCA5"),

        // BOSS SLAYER (3 tiers)
        Mission("boss_slayer_1", MissionCategory.BOSS_SLAYER, MissionTier.TIER_1, "Boss Buster", "Defeat 1 boss",
            MissionObjective(ObjectiveType.BOSSES_DEFEATED, 1f), Rewards(200, artifact = "artifact_boss_fragment"),
            icon = "\uD83D\uDC7E"),
        Mission("boss_slayer_2", MissionCategory.BOSS_SLAYER, MissionTier.TIER_2, "Boss Hunter", "Defeat 3 bosses",
            MissionObjective(ObjectiveType.BOSSES_DEFEATED, 3f), Rewards(400, rocketSkin = "skin_boss_hunter"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "boss_slayer_1"), icon = "\uD83D\uDC7E"),
        Mission("boss_slayer_3", MissionCategory.BOSS_SLAYER, MissionTier.TIER_3, "Boss Slayer", "Defeat 7 bosses",
            MissionObjective(ObjectiveType.BOSSES_DEFEATED, 7f), Rewards(700, artifact = "artifact_boss_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "boss_slayer_2"), icon = "\uD83D\uDC7E"),

        // DISCOVERY HUNTER (3 tiers)
        Mission("discovery_hunter_1", MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_1, "Discovery Novice", "Unlock 5 Codex entries",
            MissionObjective(ObjectiveType.CODEX_UNLOCKED, 5f), Rewards(120, codexEntry = "codex_discovery_novice"),
            icon = "\uD83D\uDD0D"),
        Mission("discovery_hunter_2", MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_2, "Discovery Seeker", "Unlock 15 Codex entries",
            MissionObjective(ObjectiveType.CODEX_UNLOCKED, 15f), Rewards(300, powerup = "discovery_boost"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "discovery_hunter_1"), icon = "\uD83D\uDD0D"),
        Mission("discovery_hunter_3", MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_3, "Discovery Master", "Unlock 30 Codex entries",
            MissionObjective(ObjectiveType.CODEX_UNLOCKED, 30f), Rewards(600, artifact = "artifact_discovery_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "discovery_hunter_2"), icon = "\uD83D\uDD0D"),

        // ALTITUDE CLIMBER (4 tiers)
        Mission("altitude_climber_1", MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_1, "Altitude Rookie", "Reach 500m altitude",
            MissionObjective(ObjectiveType.MAX_ALTITUDE, 500f), Rewards(100, codexEntry = "codex_altitude_rookie"),
            icon = "\u26F0\uFE0F"),
        Mission("altitude_climber_2", MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_2, "Altitude Challenger", "Reach 1500m altitude",
            MissionObjective(ObjectiveType.MAX_ALTITUDE, 1500f), Rewards(250, powerup = "altitude_boost"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "altitude_climber_1"), icon = "\u26F0\uFE0F"),
        Mission("altitude_climber_3", MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_3, "Altitude Champion", "Reach 4000m altitude",
            MissionObjective(ObjectiveType.MAX_ALTITUDE, 4000f), Rewards(500, artifact = "artifact_altitude_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "altitude_climber_2"), icon = "\u26F0\uFE0F"),
        Mission("altitude_climber_4", MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_4, "Altitude Legend", "Reach 10000m altitude",
            MissionObjective(ObjectiveType.MAX_ALTITUDE, 10000f), Rewards(1000, rocketSkin = "skin_altitude_legend"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "altitude_climber_3"), icon = "\u26F0\uFE0F"),

        // MOMENTUM MASTER (3 tiers)
        Mission("momentum_master_1", MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_1, "Momentum Builder", "Build 50 momentum",
            MissionObjective(ObjectiveType.MAX_MOMENTUM, 50f), Rewards(150, codexEntry = "codex_momentum_builder"),
            icon = "\uD83C\uDF0A"),
        Mission("momentum_master_2", MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_2, "Momentum Surfer", "Build 150 momentum",
            MissionObjective(ObjectiveType.MAX_MOMENTUM, 150f), Rewards(350, powerup = "momentum_boost"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "momentum_master_1"), icon = "\uD83C\uDF0A"),
        Mission("momentum_master_3", MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_3, "Momentum Champion", "Build 400 momentum",
            MissionObjective(ObjectiveType.MAX_MOMENTUM, 400f), Rewards(600, artifact = "artifact_momentum_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "momentum_master_2"), icon = "\uD83C\uDF0A"),

        // HAZARD SURVIVOR (3 tiers)
        Mission("hazard_survivor_1", MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_1, "Hazard Survivor", "Survive 10 hazard hits",
            MissionObjective(ObjectiveType.HAZARD_HITS_SURVIVED, 10f), Rewards(120, codexEntry = "codex_hazard_survivor"),
            icon = "\u26A1"),
        Mission("hazard_survivor_2", MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_2, "Hazard Veteran", "Survive 30 hazard hits",
            MissionObjective(ObjectiveType.HAZARD_HITS_SURVIVED, 30f), Rewards(300, powerup = "hazard_shield"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "hazard_survivor_1"), icon = "\u26A1"),
        Mission("hazard_survivor_3", MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_3, "Hazard Legend", "Survive 60 hazard hits",
            MissionObjective(ObjectiveType.HAZARD_HITS_SURVIVED, 60f), Rewards(600, artifact = "artifact_hazard_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "hazard_survivor_2"), icon = "\u26A1"),

        // PERFECT RUN (3 tiers)
        Mission("perfect_run_1", MissionCategory.PERFECT_RUN, MissionTier.TIER_1, "Perfect Run Novice", "No damage for 2 minutes",
            MissionObjective(ObjectiveType.PERFECT_RUN_TIME, 120f), Rewards(150, codexEntry = "codex_perfect_run_novice"),
            icon = "\uD83C\uDFC6"),
        Mission("perfect_run_2", MissionCategory.PERFECT_RUN, MissionTier.TIER_2, "Perfect Run Specialist", "No damage for 5 minutes",
            MissionObjective(ObjectiveType.PERFECT_RUN_TIME, 300f), Rewards(350, powerup = "perfect_shield"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "perfect_run_1"), icon = "\uD83C\uDFC6"),
        Mission("perfect_run_3", MissionCategory.PERFECT_RUN, MissionTier.TIER_3, "Perfect Run Master", "No damage for 10 minutes",
            MissionObjective(ObjectiveType.PERFECT_RUN_TIME, 600f), Rewards(700, artifact = "artifact_perfect_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "perfect_run_2"), icon = "\uD83C\uDFC6"),

        // COLLECTOR (3 tiers)
        Mission("collector_1", MissionCategory.COLLECTOR, MissionTier.TIER_1, "Collector Novice", "Collect 5 artifacts",
            MissionObjective(ObjectiveType.ARTIFACTS_COLLECTED, 5f), Rewards(130, codexEntry = "codex_collector_novice"),
            icon = "\uD83D\uDCE6"),
        Mission("collector_2", MissionCategory.COLLECTOR, MissionTier.TIER_2, "Collector Seeker", "Collect 15 artifacts",
            MissionObjective(ObjectiveType.ARTIFACTS_COLLECTED, 15f), Rewards(300, powerup = "collector_boost"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "collector_1"), icon = "\uD83D\uDCE6"),
        Mission("collector_3", MissionCategory.COLLECTOR, MissionTier.TIER_3, "Collector Master", "Collect 30 artifacts",
            MissionObjective(ObjectiveType.ARTIFACTS_COLLECTED, 30f), Rewards(600, artifact = "artifact_collector_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "collector_2"), icon = "\uD83D\uDCE6"),

        // BOOST CHAMPION (2 tiers)
        Mission("boost_champion_1", MissionCategory.BOOST_CHAMPION, MissionTier.TIER_1, "Dash Initiate", "Perform 10 dashes in one run",
            MissionObjective(ObjectiveType.DASHES_PER_RUN, 10f), Rewards(100, powerup = "dash_recharge"),
            icon = "\uD83D\uDCA8"),
        Mission("boost_champion_2", MissionCategory.BOOST_CHAMPION, MissionTier.TIER_2, "Dash Master", "Perform 30 dashes in one run",
            MissionObjective(ObjectiveType.DASHES_PER_RUN, 30f), Rewards(300, artifact = "artifact_dash_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "boost_champion_1"), icon = "\uD83D\uDCA8"),

        // COMBO PRO (2 tiers)
        Mission("combo_pro_1", MissionCategory.COMBO_PRO, MissionTier.TIER_1, "Combo Pro Starter", "Maintain 20x combo for 30s",
            MissionObjective(ObjectiveType.COMBO_MAINTAIN_TIME, 30f, comboThreshold = 20f), Rewards(120, codexEntry = "codex_combo_pro_starter"),
            icon = "\uD83D\uDD17"),
        Mission("combo_pro_2", MissionCategory.COMBO_PRO, MissionTier.TIER_2, "Combo Pro Specialist", "Maintain 50x combo for 60s",
            MissionObjective(ObjectiveType.COMBO_MAINTAIN_TIME, 60f, comboThreshold = 50f), Rewards(300, rocketSkin = "skin_combo_pro"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "combo_pro_1"), icon = "\uD83D\uDD17"),

        // HIDDEN MISSIONS
        Mission("hidden_long_haul", MissionCategory.FLIGHT_TIME, MissionTier.TIER_3, "The Long Haul", "Complete a single run lasting 10+ minutes",
            MissionObjective(ObjectiveType.TOTAL_FLIGHT_TIME, 600f), Rewards(500, artifact = "artifact_long_haul"),
            UnlockCondition(UnlockType.REACH_ALTITUDE, 3000f), icon = "\uD83D\uDE80", isHidden = true),
        Mission("hidden_heat_junkie", MissionCategory.NO_HEAT, MissionTier.TIER_2, "Heat Junkie", "Overheat 5 times total",
            MissionObjective(ObjectiveType.OVERHEAT_COUNT, 5f), Rewards(300, rocketSkin = "skin_heat_junkie"),
            UnlockCondition(UnlockType.REACH_ALTITUDE, 1500f), icon = "\u2744\uFE0F", isHidden = true),
        Mission("hidden_near_death", MissionCategory.PERFECT_RUN, MissionTier.TIER_2, "Near-Death Experience", "Complete a run below 10% health",
            MissionObjective(ObjectiveType.NEAR_DEATH_RUN, 1f), Rewards(400, codexEntry = "codex_near_death"),
            UnlockCondition(UnlockType.REACH_ALTITUDE, 1000f), icon = "\uD83C\uDFC6", isHidden = true),
        Mission("hidden_void_walker", MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_3, "Void Walker", "Reach the Void biome",
            MissionObjective(ObjectiveType.MAX_ALTITUDE, 5000f), Rewards(600, artifact = "artifact_void_core"),
            UnlockCondition(UnlockType.REACH_BIOME, 0f), icon = "\u26F0\uFE0F", isHidden = true),
        Mission("hidden_perfect_storm", MissionCategory.PERFECT_RUN, MissionTier.TIER_3, "Perfect Storm", "Complete 5 missions without dying",
            MissionObjective(ObjectiveType.CONSECUTIVE_WINS, 5f), Rewards(750, rocketSkin = "skin_perfect_storm"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "flight_time_2"), icon = "\uD83C\uDFC6", isHidden = true),
        Mission("hidden_artifact_hunter", MissionCategory.COLLECTOR, MissionTier.TIER_2, "Artifact Hunter", "Collect 5 different artifact types",
            MissionObjective(ObjectiveType.ARTIFACTS_COLLECTED, 5f), Rewards(700, artifact = "artifact_hunter_bonus"),
            UnlockCondition(UnlockType.COLLECT_ARTIFACT, 3f), icon = "\uD83D\uDCE6", isHidden = true),
        Mission("hidden_momentum_legend", MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_3, "Momentum Legend", "Build 400 momentum in one run",
            MissionObjective(ObjectiveType.MAX_MOMENTUM, 400f), Rewards(800, powerup = "momentum_legend_core"),
            UnlockCondition(UnlockType.COMPLETE_MISSION, 0f, "momentum_master_2"), icon = "\uD83C\uDF0A", isHidden = true)
    )

    fun getAllMissions(): List<Mission> = allMissions

    fun getMissionsByCategory(category: MissionCategory): List<Mission> =
        allMissions.filter { it.category == category }

    fun getMissionsByTier(tier: MissionTier): List<Mission> =
        allMissions.filter { it.tier == tier }

    fun getMissionById(id: String): Mission? = allMissions.find { it.id == id }
}
