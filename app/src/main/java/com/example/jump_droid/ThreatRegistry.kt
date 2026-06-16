package com.example.jump_droid

/**
 * Central registry for all threat definitions in the game.
 * Allows systems to query for valid threats based on altitude or zone.
 */
object ThreatRegistry {
    private val threats = mutableMapOf<String, ThreatDefinition>()

    init {
        populateCatalog()
    }

    /**
     * Registers a new threat definition.
     */
    fun register(definition: ThreatDefinition) {
        threats[definition.id] = definition
    }

    private fun populateCatalog() {
        // --- ENVIRONMENTAL HAZARDS ---

        register(ThreatDefinition(
            id = "HAZ_GUST",
            name = "Sudden Downdraft",
            description = "Unpredictable air currents that push the rocket downwards.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = emptyList(), spawnChance = 0.4f)
        ))

        register(ThreatDefinition(
            id = "HAZ_CROSSWIND",
            name = "Crosswind",
            description = "Strong lateral winds that drift the rocket sideways.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = emptyList(), spawnChance = 0.4f)
        ))

        register(ThreatDefinition(
            id = "HAZ_THERMAL",
            name = "Thermal Updraft",
            description = "Rising warm air that provides a helpful upward lift.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = emptyList(), spawnChance = 0.4f)
        ))

        register(ThreatDefinition(
            id = "HAZ_STORM",
            name = "Static Discharge",
            description = "Highly charged cloud pockets that interfere with engine electronics.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER), spawnChance = 0.2f)
        ))

        register(ThreatDefinition(
            id = "HAZ_ICE_STORM",
            name = "Flash Freeze",
            description = "Supercooled moisture that can rapidly encase the hull in ice.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE), spawnChance = 0.1f)
        ))

        register(ThreatDefinition(
            id = "HAZ_OXYGEN_DEPLETION",
            name = "Low Density Pocket",
            description = "Areas of extreme vacuum that reduce thruster efficiency.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.UPPER_ATMOSPHERE), spawnChance = 0.15f)
        ))

        register(ThreatDefinition(
            id = "HAZ_SOLAR_FLARE",
            name = "Radiation Wave",
            description = "Bursts of solar energy that cause internal heat build-up.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ORBIT), spawnChance = 0.05f)
        ))

        register(ThreatDefinition(
            id = "HAZ_METEOR",
            name = "Debris Field",
            description = "A cloud of high-velocity micro-meteorites and orbital trash.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE), spawnChance = 0.15f)
        ))

        register(ThreatDefinition(
            id = "HAZ_GRAVITY_WELL",
            name = "Event Horizon",
            description = "Localized gravity anomalies that warp the flight path.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.1f)
        ))

        register(ThreatDefinition(
            id = "HAZ_VOID_ANOMALY",
            name = "Void Anomaly",
            description = "Unnatural distortions in the fabric of space that pulse with an eerie light.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.08f)
        ))

        register(ThreatDefinition(
            id = "HAZ_VOID_TEAR",
            name = "Singularity Fragment",
            description = "Unstable rifts in space-time that threaten to consume the vessel.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_4,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.03f)
        ))

        // --- HOSTILE ENTITIES ---

        register(ThreatDefinition(
            id = "ENT_SCOUT_DRONE",
            name = "Surveyor Probe",
            description = "Autonomous drones guarding the lower altitudes.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.EARTH, AltitudeZone.CLOUD_LAYER), spawnChance = 0.3f)
        ))

        register(ThreatDefinition(
            id = "ENT_CLOUD_SKIMMER",
            name = "Sky Ray",
            description = "Biological organisms that have adapted to high-altitude flight.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER), spawnChance = 0.15f)
        ))

        register(ThreatDefinition(
            id = "ENT_SWARM_BOTS",
            name = "Aerosol Swarm",
            description = "Floating nano-colonies that drift through the clouds, shifting shape in the wind.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER), spawnChance = 0.25f)
        ))

        register(ThreatDefinition(
            id = "ENT_ORBITAL_SENTRY",
            name = "Defense Node",
            description = "Ancient defensive structures still active in high orbit.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ORBIT), spawnChance = 0.12f)
        ))

        register(ThreatDefinition(
            id = "ENT_CORRUPTED_HULL",
            name = "Derelict Echo",
            description = "Ghostly remains of previous failed ascents, now hostile.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE), spawnChance = 0.1f)
        ))

        register(ThreatDefinition(
            id = "ENT_STALKER",
            name = "Void Tracker",
            description = "Predatory machines that hunt by following thermal signatures.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE), spawnChance = 0.05f)
        ))

        register(ThreatDefinition(
            id = "ENT_VOID_WHALE",
            name = "Cosmic Leviathan",
            description = "Massive ethereal beings that drift through the outer reaches.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.VOID), spawnChance = 0.02f)
        ))

        register(ThreatDefinition(
            id = "ENT_VOID_WRAITH",
            name = "Shadow Entity",
            description = "Non-Euclidean horrors that exist only within the Void.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.07f)
        ))

        // --- BOSS ENCOUNTERS ---

        register(ThreatDefinition(
            id = "MINI_BOSS_COMMANDER",
            name = "Command Cruiser",
            description = "A massive tactical vessel overseeing regional surveillance and drone deployments.",
            type = ThreatType.MINI_BOSS,
            tier = ThreatTier.TIER_4,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ORBIT), spawnChance = 0.05f)
        ))

        register(ThreatDefinition(
            id = "BOSS_GATEKEEPER",
            name = "The Gatekeeper",
            description = "Ancient orbital defense platform that creates rotating safe zones.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ORBIT), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_STAR_EATER",
            name = "Star-Eater",
            description = "Massive cosmic organism that consumes light and energy.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_VOID_ENGINE",
            name = "The Void Engine",
            description = "Reality-warping machine that periodically alters gravity direction.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_LEVIATHAN",
            name = "The Leviathan",
            description = "Gigantic living creature that creates moving slipstreams.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_SIGNAL",
            name = "The Signal",
            description = "Unknown intelligence that creates false navigation cues.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.01f)
        ))
    }

    /**
     * Retrieves a threat definition by its unique ID.
     */
    fun getById(id: String): ThreatDefinition? = threats[id]

    /**
     * Returns all registered threats.
     */
    fun getAll(): List<ThreatDefinition> = threats.values.toList()

    /**
     * Returns a list of threats that are eligible to spawn at a specific altitude and zone.
     */
    fun getEligibleThreats(altitude: Int, zone: AltitudeZone): List<ThreatDefinition> {
        return threats.values.filter { threat ->
            val rules = threat.spawnRules
            val altitudeEligible = altitude in (rules.minAltitude..rules.maxAltitude)
            val zoneEligible = rules.allowedZones.isEmpty() || zone in rules.allowedZones
            altitudeEligible && zoneEligible
        }
    }

    /**
     * Clears all registered threats (useful for testing or reloading).
     */
    fun clear() {
        threats.clear()
    }
}
