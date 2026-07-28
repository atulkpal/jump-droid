package com.ashwathai.jump_droid

import androidx.compose.ui.graphics.Color
import com.ashwathai.jump_droid.ui.theme.*

data class MissionTrack(
    val name: String,
    val iconRes: Int,
    val color: Color,
    val categories: List<MissionCategory>
)

/**
 * Migrated Mission Registry.
 * Contains 48 missions from feature/mission-system, aligned with EPIC 7 architecture.
 */
object MissionRegistry {
    val MISSION_TRACKS = listOf(
        MissionTrack("Aeronautics", R.drawable.ic_track_aero, SciFiCyan, listOf(MissionCategory.FLIGHT_TIME, MissionCategory.NO_HEAT, MissionCategory.OVERHEAT)),
        MissionTrack("Ground Support", R.drawable.ic_track_ground, SciFiWhite, listOf(MissionCategory.PLATFORM_STAY, MissionCategory.LANDINGS)),
        MissionTrack("Resource Mgmt", R.drawable.ic_track_resource, SciFiGreen, listOf(MissionCategory.FUEL_EFFICIENCY)),
        MissionTrack("Combo Mastery", R.drawable.ic_track_combo, SciFiGold, listOf(MissionCategory.COMBO_STREAK, MissionCategory.COMBO_PRO)),
        MissionTrack("Elite Combat", R.drawable.ic_track_combat, SciFiRed, listOf(MissionCategory.BOSS_SLAYER)),
        MissionTrack("Surveying", R.drawable.ic_track_survey, SciFiPurple, listOf(MissionCategory.DISCOVERY_HUNTER)),
        MissionTrack("Ascension Path", R.drawable.ic_track_climb, SciFiCyan, listOf(MissionCategory.ALTITUDE_CLIMBER)),
        MissionTrack("Kinetic Control", R.drawable.ic_track_kinetic, SciFiOrange, listOf(MissionCategory.MOMENTUM_MASTER, MissionCategory.BOOST_CHAMPION)),
        MissionTrack("Reinforcement", R.drawable.ic_track_defense, SciFiGreen, listOf(MissionCategory.HAZARD_SURVIVOR)),
        MissionTrack("Precision Flight", R.drawable.ic_track_precision, SciFiGold, listOf(MissionCategory.PERFECT_RUN)),
        MissionTrack("Archeology", R.drawable.ic_track_archeo, SciFiPurple, listOf(MissionCategory.COLLECTOR))
    )

    fun getTrackForCategory(category: MissionCategory): MissionTrack? {
        return MISSION_TRACKS.find { category in it.categories }
    }

    private val missionTemplates = mutableMapOf<String, Mission>()

    init {
        // --- FLIGHT TIME (SURVIVAL) ---
        register(Mission("flight_time_1", "Sky Explorer", "Spend 10 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_1, 600,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.LORE_ASCENSION)), icon = "\uD83D\uDE80",
            debrief = "Initial flight data synced. Basic surveying requirements met. Sensors are beginning to map the lower cloud layers."))
        register(Mission("flight_time_2", "Sky Wanderer", "Spend 25 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_2, 1500,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.FUEL_TANK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_1"), icon = "\uD83D\uDE80",
            debrief = "Extended atmospheric presence confirmed. Telemetry logs show your droid is adapting well to high-altitude turbulence."))
        register(Mission("flight_time_3", "Sky Champion", "Spend 30 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_3, 1800,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_2"), icon = "\uD83D\uDE80",
            debrief = "Endurance records shattered. The core database is now rich with atmospheric pressure data required for deep orbital insertion."))
        register(Mission("flight_time_4", "Sky Legend", "Spend 60 minutes total in the air",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_4, 3600,
            listOf(MissionReward.Cash(1000), MissionReward.Unlock(RocketType.SCOUT)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_3"), icon = "\uD83D\uDE80",
            debrief = "Legendary flight duration achieved. You have spent more time in the sky than any scout unit in program history. The Void is within reach."))

        // --- PLATFORM STAY (PLATFORMING) ---
        register(Mission("platform_stay_1", "Platform Dweller", "Spend 10 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_1, 600,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.NORMAL_PLATFORM)), icon = "\uD83C\uDFD7\uFE0F",
            debrief = "Static stability test complete. The structural integrity of the lower deployment zones has been verified for heavy droid traffic."))
        register(Mission("platform_stay_2", "Platform Guardian", "Spend 15 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_2, 900,
            listOf(MissionReward.Cash(200), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "platform_stay_1"), icon = "\uD83C\uDFD7\uFE0F",
            debrief = "Strategic occupation protocols successful. By maintaining presence on these structures, you've allowed for precise sensor calibration across the zone."))
        register(Mission("platform_stay_3", "Platform King", "Spend 30 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_3, 1800,
            listOf(MissionReward.Cash(400), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "platform_stay_2"), icon = "\uD83C\uDFD7\uFE0F",
            debrief = "Mastery of static maneuvers logged. The Ascension Program has identified these platforms as key logistical hubs thanks to your survey."))
        register(Mission("platform_stay_4", "Platform Legend", "Spend 60 minutes on platforms",
            MissionType.PLATFORMING, MissionCategory.PLATFORM_STAY, MissionTier.TIER_4, 3600,
            listOf(MissionReward.Cash(800), MissionReward.Unlock(RocketType.TANK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "platform_stay_3"), icon = "\uD83C\uDFD7\uFE0F",
            debrief = "Total dominance of the sky-infrastructure. You have turned hostile floating debris into a network of reliable outposts. Extraordinary work."))

        // --- PLATFORM LANDINGS (PLATFORMING) ---
        register(Mission("plat_land_1", "Touchdown", "Land on 150 platforms total",
            MissionType.PLATFORMING, MissionCategory.LANDINGS, MissionTier.TIER_1, 150,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.NORMAL_PLATFORM)), icon = "\uD83D\uDEEB",
            debrief = "Landing protocols verified. Kinetic energy recovery systems are functioning within expected parameters for standard deployment."))
        register(Mission("plat_land_2", "Precision Pilot", "Land on 200 platforms total",
            MissionType.PLATFORMING, MissionCategory.LANDINGS, MissionTier.TIER_2, 200,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "plat_land_1"), icon = "\uD83D\uDEEB",
            debrief = "Rapid sequence landings achieved. Your pilot algorithms are demonstrating superior predictive logic in high-speed maneuvers."))
        register(Mission("plat_land_3", "Surface Master", "Land on 1000 platforms total",
            MissionType.PLATFORMING, MissionCategory.LANDINGS, MissionTier.TIER_3, 1000,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "plat_land_2"), icon = "\uD83D\uDEEB",
            debrief = "Legendary contact frequency. The program has optimized landing gear durability based on your extensive field data. A true master of the ascent."))

        // --- NO HEAT (SURVIVAL) ---
        register(Mission("no_heat_1", "Cool Operator", "Fly 5 minutes without overheating",
            MissionType.SURVIVAL, MissionCategory.NO_HEAT, MissionTier.TIER_1, 300,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.HEAT_SYSTEM)), icon = "\u2744\uFE0F",
            debrief = "Thermal efficiency check passed. Your thruster management is preserving the core's coolant reserves for the higher, thinner atmosphere."))
        register(Mission("no_heat_2", "Ice Veins", "Fly 12 minutes without overheating",
            MissionType.SURVIVAL, MissionCategory.NO_HEAT, MissionTier.TIER_2, 720,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.HEAT_SINK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "no_heat_1"), icon = "\u2744\uFE0F",
            debrief = "Exceptional thermal discipline. By avoiding core stress, you've extended the operational lifespan of your droid's critical engine components."))
        register(Mission("no_heat_3", "Absolute Zero", "Fly 25 minutes without overheating",
            MissionType.SURVIVAL, MissionCategory.NO_HEAT, MissionTier.TIER_3, 1500,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_BEACON)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "no_heat_2"), icon = "\u2744\uFE0F",
            debrief = "Total thermal equilibrium achieved. You have mastered the delicate balance of thrust and cooling, a feat few droids ever replicate."))

        // --- FUEL EFFICIENCY (SURVIVAL) ---
        register(Mission("fuel_efficiency_1", "Fuel Saver", "Collect 10 fuel pickups",
            MissionType.SURVIVAL, MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_1, 10,
            listOf(MissionReward.Cash(80), MissionReward.Artifact(DiscoveryType.FUEL_TANK)), icon = "\u26FD",
            debrief = "Supply chain established. Your recovery of these fuel cells has secured the energy reserves for the next wave of surveyor droids."))
        register(Mission("fuel_efficiency_2", "Fuel Hoarder", "Collect 30 fuel pickups",
            MissionType.SURVIVAL, MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_2, 30,
            listOf(MissionReward.Cash(200), MissionReward.PowerUp(PowerUpType.FUEL_TANK)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "fuel_efficiency_1"), icon = "\u26FD",
            debrief = "Mass salvage complete. You've recovered enough liquid oxygen to power a scout ship's entire ascent through the cloud layer."))
        register(Mission("fuel_efficiency_3", "Fuel King", "Collect 75 fuel pickups",
            MissionType.SURVIVAL, MissionCategory.FUEL_EFFICIENCY, MissionTier.TIER_3, 75,
            listOf(MissionReward.Cash(450), MissionReward.Artifact(DiscoveryType.ART_DRONE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "fuel_efficiency_2"), icon = "\u26FD",
            debrief = "Logistical supremacy achieved. The Ascension Program's fuel reserves are now at peak capacity thanks to your relentless collection efforts."))

        // --- COMBO STREAK (PLATFORMING) ---
        register(Mission("combo_streak_1", "Combo Starter", "Reach 35x combo",
            MissionType.PLATFORMING, MissionCategory.COMBO_STREAK, MissionTier.TIER_1, 35,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.EFFICIENCY_SURVIVAL)), icon = "\uD83D\uDCA5",
            debrief = "Momentum protocol initiated. Your rhythmic landings are beginning to generate the kinetic data needed for high-speed ascent maneuvers."))
        register(Mission("combo_streak_2", "Combo Specialist", "Reach 50x combo",
            MissionType.PLATFORMING, MissionCategory.COMBO_STREAK, MissionTier.TIER_2, 50,
            listOf(MissionReward.Cash(250), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "combo_streak_1"), icon = "\uD83D\uDCA5",
            debrief = "Advanced flow-state confirmed. Your droid is maintaining peak efficiency, turning every landing into a building block for orbital velocity."))
        register(Mission("combo_streak_3", "Combo Master", "Reach 100x combo",
            MissionType.PLATFORMING, MissionCategory.COMBO_STREAK, MissionTier.TIER_3, 100,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "combo_streak_2"), icon = "\uD83D\uDCA5",
            debrief = "Kinetic mastery achieved. You have demonstrated absolute precision in a chaotic environment. The data from this streak is a masterclass in platforming."))

        // --- BOSS SLAYER (BOSS) ---
        register(Mission("boss_slayer_1", "Boss Buster", "Defeat 1 boss",
            MissionType.BOSS, MissionCategory.BOSS_SLAYER, MissionTier.TIER_1, 1,
            listOf(MissionReward.Cash(200), MissionReward.Artifact(DiscoveryType.THREAT_SENTINEL)), icon = "\uD83D\uDC7E",
            debrief = "Hostile entity neutralized. The program has identified this guardian as a remnant of an ancient orbital defense system. One less threat in our path."))
        register(Mission("boss_slayer_2", "Boss Hunter", "Defeat 3 bosses",
            MissionType.BOSS, MissionCategory.BOSS_SLAYER, MissionTier.TIER_2, 3,
            listOf(MissionReward.Cash(400), MissionReward.Unlock(RocketType.EXPERIMENTAL)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "boss_slayer_1"), icon = "\uD83D\uDC7E",
            debrief = "Sector cleared. By eliminating these major threats, you've opened a secure corridor for the follow-up research vessels. The skies are slightly safer."))
        register(Mission("boss_slayer_3", "Boss Slayer", "Defeat 7 bosses",
            MissionType.BOSS, MissionCategory.BOSS_SLAYER, MissionTier.TIER_3, 7,
            listOf(MissionReward.Cash(700), MissionReward.Artifact(DiscoveryType.THREAT_VOID_ENGINE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "boss_slayer_2"), icon = "\uD83D\uDC7E",
            debrief = "Legendary combat logs synced. You have systematically dismantled the Void's primary defenders. Your combat algorithms are now the program's standard."))

        // --- DISCOVERY HUNTER (DISCOVERY) ---
        register(Mission("discovery_hunter_1", "Discovery Novice", "Unlock 5 Codex entries",
            MissionType.DISCOVERY, MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_1, 5,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.LORE_ASCENSION)), icon = "\uD83D\uDD0D",
            debrief = "New data packets decrypted. Your reconnaissance of the lower atmospheres is revealing a complex history of forgotten infrastructure."))
        register(Mission("discovery_hunter_2", "Discovery Seeker", "Unlock 15 Codex entries",
            MissionType.DISCOVERY, MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_2, 15,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "discovery_hunter_1"), icon = "\uD83D\uDD0D",
            debrief = "Archive expansion complete. You've identified multiple anomalies that suggest the Signal's origin is tied to these very structures."))
        register(Mission("discovery_hunter_3", "Discovery Master", "Unlock 30 Codex entries",
            MissionType.DISCOVERY, MissionCategory.DISCOVERY_HUNTER, MissionTier.TIER_3, 30,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_BEACON)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "discovery_hunter_2"), icon = "\uD83D\uDD0D",
            debrief = "Library of the Stars. Your extensive survey has mapped the vast majority of the program's interest points. We are no longer flying blind."))

        // --- ALTITUDE CLIMBER (EXPLORATION) ---
        register(Mission("altitude_climber_1", "Altitude Rookie", "Reach 1200m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_1, 1200,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.AREA_CLOUDS)), icon = "\u26F0\uFE0F",
            debrief = "Cloud layer breached. The first milestone of the Ascension Program is behind you. The air is thinner, but the path is open."))
        register(Mission("altitude_climber_2", "Altitude Challenger", "Reach 1500m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_2, 1500,
            listOf(MissionReward.Cash(250), MissionReward.PowerUp(PowerUpType.ALTITUDE_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "altitude_climber_1"), icon = "\u26F0\uFE0F",
            debrief = "Atmospheric exit sequence initiated. You are now operating at altitudes where most standard droids experience structural failure. Maintain trajectory."))
        register(Mission("altitude_climber_3", "Altitude Champion", "Reach 4000m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_3, 4000,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.AREA_ORBIT)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "altitude_climber_2"), icon = "\u26F0\uFE0F",
            debrief = "Orbit attained. Earth is now a distant memory. You've reached the threshold of the deep reaches. The program's true goal is finally in sight."))
        register(Mission("altitude_climber_4", "Altitude Legend", "Reach 10000m altitude",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_4, 10000,
            listOf(MissionReward.Cash(1000), MissionReward.Artifact(DiscoveryType.AREA_VOID)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "altitude_climber_3"), icon = "\u26F0\uFE0F",
            debrief = "Void transition complete. You have ascended further than any recorded unit. You are now writing the maps for the future of the Program."))

        // --- MOMENTUM MASTER (EXPLORATION) ---
        register(Mission("momentum_master_1", "Momentum Builder", "Build 50 momentum",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_1, 50,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.LORE_LOST_FLEET)), icon = "\uD83C\uDF0A",
            debrief = "Kinetic build-up confirmed. Your droid is effectively harvesting energy from its own movement. A vital skill for the higher zones."))
        register(Mission("momentum_master_2", "Momentum Surfer", "Build 150 momentum",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_2, 150,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "momentum_master_1"), icon = "\uD83C\uDF0A",
            debrief = "High-speed surveying logged. By maintaining extreme momentum, you've allowed the program to cover vast distances in record time."))
        register(Mission("momentum_master_3", "Momentum Champion", "Build 400 momentum",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_3, 400,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "momentum_master_2"), icon = "\uD83C\uDF0A",
            debrief = "Ultimate kinetic mastery. You have become a blur of motion in the sky, proving that speed is the droid's greatest survival asset."))

        // --- HAZARD SURVIVOR (SURVIVAL) ---
        register(Mission("hazard_survivor_1", "Hazard Survivor", "Survive 10 hazard hits",
            MissionType.SURVIVAL, MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_1, 10,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.HAZARD_LIGHTNING)), icon = "\u26A1",
            debrief = "Damage mitigation tested. Your droid's hull can withstand more than we anticipated. The data from these impacts will improve our shielding."))
        register(Mission("hazard_survivor_2", "Hazard Veteran", "Survive 30 hazard hits",
            MissionType.SURVIVAL, MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_2, 30,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.SHIELD_CAPSULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "hazard_survivor_1"), icon = "\u26A1",
            debrief = "Hull integrity masterclass. You've survived enough environmental stress to power an entire department's research for a month. A true survivor."))
        register(Mission("hazard_survivor_3", "Hazard Legend", "Survive 60 hazard hits",
            MissionType.SURVIVAL, MissionCategory.HAZARD_SURVIVOR, MissionTier.TIER_3, 60,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.HAZARD_RADIATION)), icon = "\u26A1",
            debrief = "Unbreakable spirit. Your droid has become a testament to the Program's engineering. To take so many hits and keep ascending... it's inspiring."))

        // --- PERFECT RUN (SURVIVAL) ---
        register(Mission("perfect_run_1", "Perfect Run Novice", "No damage for 2 minutes",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_1, 120,
            listOf(MissionReward.Cash(150), MissionReward.Artifact(DiscoveryType.LORE_LOGS)), icon = "\uD83C\uDFC6",
            debrief = "Evasion protocols confirmed. By avoiding all damage, you've preserved critical droid systems for the higher-intensity zones."))
        register(Mission("perfect_run_2", "Perfect Run Specialist", "No damage for 5 minutes",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_2, 300,
            listOf(MissionReward.Cash(350), MissionReward.PowerUp(PowerUpType.SHIELD_CAPSULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "perfect_run_1"), icon = "\uD83C\uDFC6",
            debrief = "Flawless surveying logged. Your ability to navigate the atmosphere without a single hull scratch is exactly what the Program needs for long-range missions."))
        register(Mission("perfect_run_3", "Perfect Run Master", "No damage for 10 minutes",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_3, 600,
            listOf(MissionReward.Cash(700), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "perfect_run_2"), icon = "\uD83C\uDFC6",
            debrief = "Untouchable performance. You have navigated the most hostile regions of the sky with absolute perfection. You are the standard all future droids will strive for."))

        // --- COLLECTOR (DISCOVERY) ---
        register(Mission("collector_1", "Collector Novice", "Collect 5 artifacts",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_1, 5,
            listOf(MissionReward.Cash(130), MissionReward.Artifact(DiscoveryType.LORE_SIGNAL)), icon = "\uD83D\uDCE6",
            debrief = "Archaeological recovery successful. These fragments are already yielding insights into the Program's origins. Every piece matters."))
        register(Mission("collector_2", "Collector Seeker", "Collect 15 artifacts",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_2, 15,
            listOf(MissionReward.Cash(300), MissionReward.PowerUp(PowerUpType.EFFICIENCY_MODULE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "collector_1"), icon = "\uD83D\uDCE6",
            debrief = "Curator of the clouds. You've recovered enough historical data to reconstruct an entire era of the early Program. Excellent work."))
        register(Mission("collector_3", "Collector Master", "Collect 30 artifacts",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_3, 30,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.ART_DRONE)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "collector_2"), icon = "\uD83D\uDCE6",
            debrief = "Master Archivist. Your collection is the most complete record of the Void's history in existence. You've preserved our past while securing our future."))

        // --- BOOST CHAMPION (PLATFORMING) ---
        register(Mission("boost_champion_1", "Dash Initiate", "Perform 10 dashes in one run",
            MissionType.PLATFORMING, MissionCategory.BOOST_CHAMPION, MissionTier.TIER_1, 10,
            listOf(MissionReward.Cash(100), MissionReward.PowerUp(PowerUpType.TURBO_BOOSTER)), icon = "\uD83D\uDCA8",
            debrief = "High-thrust maneuvers verified. Your droid's injectors are handling the stress of rapid acceleration well."))
        register(Mission("boost_champion_2", "Dash Master", "Perform 30 dashes in one run",
            MissionType.PLATFORMING, MissionCategory.BOOST_CHAMPION, MissionTier.TIER_2, 30,
            listOf(MissionReward.Cash(300), MissionReward.Artifact(DiscoveryType.BOOST_PLATFORM)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "boost_champion_1"), icon = "\uD83D\uDCA8",
            debrief = "Thrust supremacy achieved. You've mastered the art of the dash, turning your droid into a lightning bolt in the sky. Unparalleled speed."))

        // --- COMBO PRO (PLATFORMING) ---
        register(Mission("combo_pro_1", "Combo Pro Starter", "Maintain 20x combo for 30s",
            MissionType.PLATFORMING, MissionCategory.COMBO_PRO, MissionTier.TIER_1, 30,
            listOf(MissionReward.Cash(120), MissionReward.Artifact(DiscoveryType.EFFICIENCY_SURVIVAL)), icon = "\uD83D\uDD17",
            debrief = "Sustained efficiency logged. Your ability to maintain momentum over time is providing critical data for long-distance orbital traversal."))
        register(Mission("combo_pro_2", "Combo Pro Specialist", "Maintain 50x combo for 60s",
            MissionType.PLATFORMING, MissionCategory.COMBO_PRO, MissionTier.TIER_2, 60,
            listOf(MissionReward.Cash(300), MissionReward.Artifact(DiscoveryType.LORE_SIGNAL)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "combo_pro_1"), icon = "\uD83D\uDD17",
            debrief = "Flow-state master. You've maintained peak operational efficiency longer than any unit in the Program. You are a machine of pure rhythm."))

        // --- HIDDEN MISSIONS ---
        register(Mission("hidden_long_haul", "The Long Haul", "Complete a single run lasting 10+ minutes",
            MissionType.SURVIVAL, MissionCategory.FLIGHT_TIME, MissionTier.TIER_3, 600,
            listOf(MissionReward.Cash(500), MissionReward.Artifact(DiscoveryType.ART_RECORDER)),
            MissionUnlockCondition(MissionUnlockType.REACH_ALTITUDE, 3000f), icon = "\uD83D\uDE80", isHidden = true,
            crypticHint = "A whisper from the deep... — Survive beyond 3,000m",
            debrief = "Operation Long Haul successful. You've proven that droids can maintain deep-recon missions far beyond Earth's protective shell. Incredible endurance logged."))
        register(Mission("hidden_heat_junkie", "Heat Junkie", "Overheat 5 times total",
            MissionType.SURVIVAL, MissionCategory.OVERHEAT, MissionTier.TIER_2, 5,
            listOf(MissionReward.Cash(300), MissionReward.Artifact(DiscoveryType.OVERHEAT_SYSTEM)),
            MissionUnlockCondition(MissionUnlockType.REACH_ALTITUDE, 1500f), icon = "\u2744\uFE0F", isHidden = true,
            crypticHint = "The core runs hot... — Push the limits below 1,500m",
            debrief = "Stress-testing complete. By repeatedly pushing your core to its limits, you've provided invaluable data on structural failure points. The Program thanks you for your sacrifice."))
        register(Mission("hidden_near_death", "Near-Death Experience", "Complete a run below 10% health",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_2, 1,
            listOf(MissionReward.Cash(400), MissionReward.Artifact(DiscoveryType.LORE_LOGS)),
            MissionUnlockCondition(MissionUnlockType.REACH_ALTITUDE, 2500f), icon = "\uD83C\uDFC6", isHidden = true,
            crypticHint = "The edge of destruction... — Test fate below 2,500m",
            debrief = "Crisis management protocol logged. To perform at such a high level while on the verge of destruction is a feat of pure machine will. You have truly tested the edge."))
        register(Mission("hidden_void_walker", "Void Walker", "Reach the Void biome",
            MissionType.EXPLORATION, MissionCategory.ALTITUDE_CLIMBER, MissionTier.TIER_3, 5000,
            listOf(MissionReward.Cash(600), MissionReward.Artifact(DiscoveryType.AREA_VOID)),
            MissionUnlockCondition(MissionUnlockType.REACH_BIOME, 0f), icon = "\u26F0\uFE0F", isHidden = true,
            crypticHint = "The sky has a floor... — Find it.",
            debrief = "The Void attained. You have reached a region that should not exist. The sensor readings are impossible, yet here you are. The Program's true mission begins now."))
        register(Mission("hidden_perfect_storm", "Perfect Storm", "Complete 5 missions without dying",
            MissionType.SURVIVAL, MissionCategory.PERFECT_RUN, MissionTier.TIER_3, 5,
            listOf(MissionReward.Cash(750), MissionReward.Artifact(DiscoveryType.HAZARD_LIGHTNING)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "flight_time_2"), icon = "\uD83C\uDFC6", isHidden = true,
            crypticHint = "Perfection is a pattern... — Complete flight_time_2 first",
            debrief = "Total operational supremacy. To execute multiple complex operations without a single failure is the pinnacle of droid efficiency. You are the Program's perfect storm."))
        register(Mission("hidden_artifact_hunter", "Artifact Hunter", "Collect 5 different artifact types",
            MissionType.DISCOVERY, MissionCategory.COLLECTOR, MissionTier.TIER_2, 5,
            listOf(MissionReward.Cash(700), MissionReward.Artifact(DiscoveryType.ART_DRONE)),
            MissionUnlockCondition(MissionUnlockType.COLLECT_ARTIFACT, 3f), icon = "\uD83D\uDCE6", isHidden = true,
            crypticHint = "They left things behind... — Collect 3 artifacts to begin",
            debrief = "Diverse collection secured. Your ability to identify and recover varying archaeological types is helping us piece together the puzzle of the Signal. The full picture is emerging."))
        register(Mission("hidden_momentum_legend", "Momentum Legend", "Build 400 momentum in one run",
            MissionType.EXPLORATION, MissionCategory.MOMENTUM_MASTER, MissionTier.TIER_3, 400,
            listOf(MissionReward.Cash(800), MissionReward.Artifact(DiscoveryType.ART_ALLOY)),
            MissionUnlockCondition(MissionUnlockType.COMPLETE_MISSION, 0f, "momentum_master_2"), icon = "\uD83C\uDF0A", isHidden = true,
            crypticHint = "Speed is weight... — Master momentum_master_2 first",
            debrief = "Kinetic legend attained. You have transformed velocity into a tangible force of exploration. No atmospheric barrier can hold you back now."))
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
