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
        // --- ENVIRONMENTAL HAZARDS (SPRINT B) ---

        register(ThreatDefinition(
            id = "HAZ_LIGHTNING",
            name = "Lightning Storm",
            description = "Electrical buildup and strikes that damage shields.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_2,
            discoveryType = DiscoveryType.HAZARD_LIGHTNING,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT, AltitudeZone.VOID),
                spawnChance = 0.45f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_TURBULENCE",
            name = "Turbulence Front",
            description = "Violent atmospheric currents that disrupt flight control.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_1,
            discoveryType = DiscoveryType.HAZARD_TURBULENCE,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT, AltitudeZone.VOID),
                spawnChance = 0.45f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_DEBRIS",
            name = "Debris Field",
            description = "Floating wreckage that causes collision damage.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_2,
            discoveryType = DiscoveryType.HAZARD_DEBRIS,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID),
                spawnChance = 0.4f
            ),
            spawnPosition = SpawnPosition.BELOW_RANDOM_X,
            spawnVy = 200f
        ))

        register(ThreatDefinition(
            id = "HAZ_RADIATION",
            name = "Radiation Zone",
            description = "Intense cosmic energy that drains energy shields.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            discoveryType = DiscoveryType.HAZARD_RADIATION,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID),
                spawnChance = 0.3f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_SOLAR_FLARE",
            name = "Solar Flare",
            description = "Massive plasma wave that rapidly increases heat.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            discoveryType = DiscoveryType.HAZARD_SOLAR_FLARE,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID),
                spawnChance = 0.3f
            ),
            spawnPosition = SpawnPosition.ABOVE_SCREEN
        ))

        register(ThreatDefinition(
            id = "HAZ_EMP",
            name = "EMP Pulse",
            description = "Energy ring that disables shield regeneration.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            discoveryType = DiscoveryType.HAZARD_EMP,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID),
                spawnChance = 0.2f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_GRAVITY",
            name = "Gravity Distortion",
            description = "Spatial warping that increases gravity and fuel usage.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.HAZARD_GRAVITY,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.VOID),
                spawnChance = 0.2f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_VOID_ANOMALY",
            name = "Void Anomaly",
            description = "A rift in reality that distorts space and pulls everything toward it.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.HAZARD_VOID_ANOMALY,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.VOID),
                spawnChance = 0.15f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_CRYO_MIST",
            name = "Cryo-Mist",
            description = "Super-cooled vapor that locks engine thermal state.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_2,
            discoveryType = DiscoveryType.HAZARD_CRYO_MIST,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.THE_FOUNDRY),
                spawnChance = 0.3f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_MIRROR_SHARDS",
            name = "Mirror Shards",
            description = "Fragmented reality that inverts horizontal navigation.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.HAZARD_MIRROR_SHARDS,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.CHRONO_RIFT, AltitudeZone.THE_BEYOND),
                spawnChance = 0.25f
            )
        ))

        register(ThreatDefinition(
            id = "HAZ_GRAVITY_SHEAR",
            name = "Gravity Shear",
            description = "Opposing gravitational forces that split vertical velocity.",
            type = ThreatType.HAZARD,
            tier = ThreatTier.TIER_3,
            discoveryType = DiscoveryType.HAZARD_GRAVITY_SHEAR,
            spawnRules = ThreatSpawnRules(
                allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.THE_BEYOND, AltitudeZone.ANCIENT_CONSTRUCT),
                spawnChance = 0.3f
            )
        ))

        // --- HOSTILE ENTITIES ---

        register(ThreatDefinition(
            id = "ENT_SCOUT_DRONE",
            name = "Surveyor Probe",
            description = "Autonomous drones guarding the lower altitudes.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.EARTH, AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT), spawnChance = 0.4f),
            spawnPosition = SpawnPosition.SIDE_ENTRY,
            spawnVx = 150f
        ))

        register(ThreatDefinition(
            id = "ENT_CLOUD_SKIMMER",
            name = "Sky Ray",
            description = "Biological organisms that have adapted to high-altitude flight.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_1,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER), spawnChance = 0.15f),
            spawnPosition = SpawnPosition.SIDE_ENTRY,
            spawnVx = 50f
        ))

        register(ThreatDefinition(
            id = "ENT_SWARM_BOTS",
            name = "Aerosol Swarm",
            description = "Floating nano-colonies that drift through the clouds, shifting shape in the wind.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_2,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE), spawnChance = 0.35f),
            spawnPosition = SpawnPosition.ABOVE_SCREEN
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
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE), spawnChance = 0.1f),
            spawnPosition = SpawnPosition.ABOVE_CAMERA,
            spawnVy = 30f
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
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.VOID), spawnChance = 0.02f),
            spawnPosition = SpawnPosition.SIDE_ENTRY,
            spawnVx = 30f
        ))

        register(ThreatDefinition(
            id = "ENT_VOID_WRAITH",
            name = "Shadow Entity",
            description = "Non-Euclidean horrors that exist only within the Void.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_3,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.07f),
            spawnPosition = SpawnPosition.RANDOM_SCREEN
        ))

        register(ThreatDefinition(
            id = "ENT_HEAT_BAT",
            name = "Heat Bat",
            description = "Predatory shadows that strike when your engines run hot.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_2,
            discoveryType = DiscoveryType.ENEMY_HEAT_BAT,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.THE_FOUNDRY), spawnChance = 0.2f)
        ))

        register(ThreatDefinition(
            id = "ENT_VOID_HARVESTER",
            name = "Void Harvester",
            description = "Scavenging units that prioritize and consume power-ups.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_3,
            discoveryType = DiscoveryType.ENEMY_VOID_HARVESTER,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.THE_BEYOND), spawnChance = 0.15f)
        ))

        register(ThreatDefinition(
            id = "ENT_PHASE_WRAITH",
            name = "Phase Wraith",
            description = "Ethereal guardians vulnerable only when your systems are critical.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.ENEMY_PHASE_WRAITH,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID, AltitudeZone.STELLAR_GATE, AltitudeZone.SINGULARITY), spawnChance = 0.1f)
        ))

        register(ThreatDefinition(
            id = "ENT_GRAVITY_RAM",
            name = "Gravity Ram",
            description = "Heavy geometric constructs that execute telegraphed kinetic strikes.",
            type = ThreatType.ENEMY,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.ENEMY_GRAVITY_RAM,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY), spawnChance = 0.12f)
        ))

        // --- BOSS ENCOUNTERS ---

        register(ThreatDefinition(
            id = "MINI_BOSS_COMMANDER",
            name = "Command Cruiser",
            description = "A massive tactical vessel overseeing regional surveillance and drone deployments.",
            type = ThreatType.MINI_BOSS,
            tier = ThreatTier.TIER_4,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.CLOUD_LAYER, AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID), spawnChance = 0.05f)
        ))

        register(ThreatDefinition(
            id = "BOSS_GATEKEEPER",
            name = "The Gatekeeper",
            description = "Ancient orbital defense platform that creates rotating safe zones.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ORBIT, AltitudeZone.DEEP_SPACE, AltitudeZone.VOID), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_STAR_EATER",
            name = "Star-Eater",
            description = "Massive cosmic organism that consumes light and energy.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.VOID), spawnChance = 0.01f)
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
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.VOID), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_SIGNAL",
            name = "The Signal",
            description = "Unknown intelligence that creates false navigation cues.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.VOID), spawnChance = 0.01f)
        ))

        // --- SPRINT 10.6 LIBRARY BOSSES ---

        register(ThreatDefinition(
            id = "MINI_BOSS_THERMAL_HIVE",
            name = "Thermal Hive",
            description = "Heat-sensitive collective that spawns swarms when engines run hot.",
            type = ThreatType.MINI_BOSS,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.THREAT_THERMAL_HIVE,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.UPPER_ATMOSPHERE, AltitudeZone.CLOUD_LAYER), spawnChance = 0.04f)
        ))

        register(ThreatDefinition(
            id = "MINI_BOSS_GRAVITY_ANCHOR",
            name = "Gravity Anchor",
            description = "Static spatial anchor that intensifies downward pull over time.",
            type = ThreatType.MINI_BOSS,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.THREAT_GRAVITY_ANCHOR,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.DEEP_SPACE, AltitudeZone.THE_BEYOND), spawnChance = 0.03f)
        ))

        register(ThreatDefinition(
            id = "MINI_BOSS_FORGER",
            name = "The Forger",
            description = "Industrial fabricator that converts safe surfaces into hazardous ones.",
            type = ThreatType.MINI_BOSS,
            tier = ThreatTier.TIER_4,
            discoveryType = DiscoveryType.THREAT_FORGER,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.THE_FOUNDRY, AltitudeZone.ORBIT), spawnChance = 0.04f)
        ))

        register(ThreatDefinition(
            id = "BOSS_ARCHITECT",
            name = "The Architect",
            description = "Automated level controller that deconstructs your path to the stars.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            discoveryType = DiscoveryType.THREAT_ARCHITECT,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.THE_BEYOND, AltitudeZone.STELLAR_GATE), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_ENTROPY_CORE",
            name = "Entropy Core",
            description = "A massive radiator that siphons energy and integrity across multiple vectors.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            discoveryType = DiscoveryType.THREAT_ENTROPY_CORE,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.ANCIENT_CONSTRUCT, AltitudeZone.SINGULARITY), spawnChance = 0.01f)
        ))

        register(ThreatDefinition(
            id = "BOSS_SINGULARITY",
            name = "The Singularity",
            description = "The ultimate intelligence. It doesn't just fight you; it distorts your very perception of reality.",
            type = ThreatType.BOSS,
            tier = ThreatTier.TIER_5,
            spawnRules = ThreatSpawnRules(allowedZones = listOf(AltitudeZone.SINGULARITY), spawnChance = 0.01f)
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
