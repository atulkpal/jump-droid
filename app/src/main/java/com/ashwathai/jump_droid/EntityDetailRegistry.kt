package com.ashwathai.jump_droid

/**
 * Data model for the Archive Detail Popups.
 * Aligns with Phase 6 requirements: STATUS / CLASSIFICATION / ARCHIVE RECORD / TACTICAL NOTE.
 */
data class EntityDetail(
    val id: String,
    val discoveryType: DiscoveryType,
    val threatId: String?,
    val platformType: PlatformType?,
    val powerUpType: PowerUpType?,
    val status: String,
    val classification: String,
    val archiveRecord: String,
    val tacticalNote: String
)

object EntityDetailRegistry {
    
    fun byDiscovery(type: DiscoveryType): EntityDetail {
        return data[type] ?: EntityDetail(
            id = type.name,
            discoveryType = type,
            threatId = threatMap[type],
            platformType = platformMap[type],
            powerUpType = powerUpMap[type],
            status = "UNKNOWN — SIGNAL LOST",
            classification = "UNIDENTIFIED",
            archiveRecord = "Record corrupted. Physical encounter required for data restoration.",
            tacticalNote = "Tactical data unavailable. Approach with extreme caution."
        )
    }

    private val data: Map<DiscoveryType, EntityDetail> = mapOf(
        // --- PLATFORMS ---
        DiscoveryType.NORMAL_PLATFORM to EntityDetail(
            id = "NORMAL_PLATFORM",
            discoveryType = DiscoveryType.NORMAL_PLATFORM,
            threatId = null,
            platformType = PlatformType.NORMAL,
            powerUpType = null,
            status = "STABLE // OPERATIONAL",
            classification = "STATIC HOLOGRAPHIC STRUCTURE",
            archiveRecord = "The backbone of the Ascension Program's infrastructure, designed for rapid deployment and droid recovery.",
            tacticalNote = "Safe landing zone. Use these to plan your next move and recover engine heat without resource drain."
        ),
        DiscoveryType.MOVING_PLATFORM to EntityDetail(
            id = "MOVING_PLATFORM",
            discoveryType = DiscoveryType.MOVING_PLATFORM,
            threatId = null,
            platformType = PlatformType.MOVING,
            powerUpType = null,
            status = "ACTIVE // OSCILLATING",
            classification = "MOBILE LOGISTICS NODE",
            archiveRecord = "Early prototypes for automated logistics, now oscillating perpetually between their last programmed waypoints.",
            tacticalNote = "Inherits momentum. Your ship will move with the platform. Use its velocity to launch yourself horizontally."
        ),
        DiscoveryType.ICE_PLATFORM to EntityDetail(
            id = "ICE_PLATFORM",
            discoveryType = DiscoveryType.ICE_PLATFORM,
            threatId = null,
            platformType = PlatformType.ICE,
            powerUpType = null,
            status = "CRYSTALLIZED // LOW FRICTION",
            classification = "SHIMMERING FROST BLOCK",
            archiveRecord = "High-altitude moisture that has crystallized on cold metallic surfaces, creating zero-friction zones.",
            tacticalNote = "Friction is reduced to 5%. Horizontal damping is significantly impaired. Precise steering is required to stay grounded."
        ),
        DiscoveryType.BOOST_PLATFORM to EntityDetail(
            id = "BOOST_PLATFORM",
            discoveryType = DiscoveryType.BOOST_PLATFORM,
            threatId = null,
            platformType = PlatformType.BOOST,
            powerUpType = null,
            status = "CHARGED // KINETIC READY",
            classification = "HIGH-IMPULSE LAUNCHER",
            archiveRecord = "Repurposed kinetic energy recovery systems that store and release massive vertical bursts on contact.",
            tacticalNote = "Vertical Impulse: 600 U/S. Launches you upward instantly. Excellent for bypassing clusters of hazards."
        ),
        DiscoveryType.BREAKABLE_PLATFORM to EntityDetail(
            id = "BREAKABLE_PLATFORM",
            discoveryType = DiscoveryType.BREAKABLE_PLATFORM,
            threatId = null,
            platformType = PlatformType.BREAKABLE,
            powerUpType = null,
            status = "DEGRADING // UNSTABLE",
            classification = "TEMPORARY REINFORCED ALLOY",
            archiveRecord = "Lightweight alloy meant for rapid deployment during the fleet's initial ascent. Structural integrity is poor.",
            tacticalNote = "Lifetime: 2.4s. Triggers collapse timer on landing. Use as a stepping stone, do not attempt to hover."
        ),
        DiscoveryType.PHASE_PLATFORM to EntityDetail(
            id = "PHASE_PLATFORM",
            discoveryType = DiscoveryType.PHASE_PLATFORM,
            threatId = null,
            platformType = PlatformType.PHASE,
            powerUpType = null,
            status = "FLUCTUATING // QUANTUM STATE",
            classification = "DIGITAL DISPLACEMENT BLOCK",
            archiveRecord = "Platforms that exist in a state of quantum flux, periodically phasing in and out of the material plane.",
            tacticalNote = "Cycle: 4.0s. Collision is only active when alpha > 0.9. Watch the glitch-displacement bands for timing cues."
        ),
        DiscoveryType.FUEL_PLATFORM to EntityDetail(
            id = "FUEL_PLATFORM",
            discoveryType = DiscoveryType.FUEL_PLATFORM,
            threatId = null,
            platformType = PlatformType.FUEL,
            powerUpType = null,
            status = "OPERATIONAL // REFUELING ACTIVE",
            classification = "AUTOMATED UTILITY NODE",
            archiveRecord = "Refueling stations once used to service the surveyor probe fleet during the mapping of the upper atmosphere.",
            tacticalNote = "Restores 50 units of fuel on landing. Essential for maintaining ascent in low-resource altitude zones."
        ),
        DiscoveryType.COOLING_PLATFORM to EntityDetail(
            id = "COOLING_PLATFORM",
            discoveryType = DiscoveryType.COOLING_PLATFORM,
            threatId = null,
            platformType = PlatformType.COOLING,
            powerUpType = null,
            status = "OPERATIONAL // CRYOGENIC FLOW",
            classification = "THERMAL MANAGEMENT STATION",
            archiveRecord = "Equipped with high-efficiency liquid nitrogen exchangers designed to prevent atmospheric friction meltdowns.",
            tacticalNote = "Instantly reduces engine heat by 30 units. Prioritize these when entering zones with Solar Flare hazards."
        ),
        DiscoveryType.STABILITY_PLATFORM to EntityDetail(
            id = "STABILITY_PLATFORM",
            discoveryType = DiscoveryType.STABILITY_PLATFORM,
            threatId = null,
            platformType = PlatformType.STABILITY,
            powerUpType = null,
            status = "OPERATIONAL // GYRO-DAMPENING",
            classification = "FLIGHT STABILIZATION ARRAY",
            archiveRecord = "Emits a localized dampening field that synchronizes with droid flight controllers to counteract turbulence.",
            tacticalNote = "Grants a 10-second stability buff. Drastically improves steering precision and resists horizontal drift."
        ),
        DiscoveryType.MAGNETIC_PLATFORM to EntityDetail(
            id = "MAGNETIC_PLATFORM",
            discoveryType = DiscoveryType.MAGNETIC_PLATFORM,
            threatId = null,
            platformType = PlatformType.MAGNETIC,
            powerUpType = null,
            status = "ACTIVE // HIGH MAGNETIC FIELD",
            classification = "SPATIAL ANCHOR PLATFORM",
            archiveRecord = "Ancient industrial magnets used to move heavy cargo between zones without physical contact.",
            tacticalNote = "Generates a gravity well. Its field will pull you in or push you away. Plan your trajectory early."
        ),
        DiscoveryType.FLUX_PLATFORM to EntityDetail(
            id = "FLUX_PLATFORM",
            discoveryType = DiscoveryType.FLUX_PLATFORM,
            threatId = null,
            platformType = PlatformType.FLUX,
            powerUpType = null,
            status = "READY // PHASE-SHIFT ACTIVE",
            classification = "INSTANTANEOUS TRAVERSAL BRIDGE",
            archiveRecord = "Experimental teleportation technology that bridges two points in space instantaneously using folded dimensions.",
            tacticalNote = "Teleports you to the linked platform on contact. Use it for rapid horizontal positioning across the screen."
        ),
        DiscoveryType.GRAVITON_PLATFORM to EntityDetail(
            id = "GRAVITON_PLATFORM",
            discoveryType = DiscoveryType.GRAVITON_PLATFORM,
            threatId = null,
            platformType = PlatformType.GRAVITON,
            powerUpType = null,
            status = "CRITICAL // GRAVITY WELL",
            classification = "SINGULARITY STABILIZATION UNIT",
            archiveRecord = "A localized singularity contained within a stabilization field, creating massive tidal forces.",
            tacticalNote = "Extreme gravity pull. Approaching too fast can trap your ship in a descent loop. Apply maximum thrust."
        ),
        DiscoveryType.CONVEYOR_PLATFORM to EntityDetail(
            id = "CONVEYOR_PLATFORM",
            discoveryType = DiscoveryType.CONVEYOR_PLATFORM,
            threatId = null,
            platformType = PlatformType.CONVEYOR,
            powerUpType = null,
            status = "ACTIVE // INDUSTRIAL CYCLE",
            classification = "AUTOMATED ASSEMBLY SURFACE",
            archiveRecord = "Relic of the orbital foundry's logistics system, still carrying non-existent cargo into the Void.",
            tacticalNote = "Applies continuous horizontal force while grounded. Walk against the roller direction to stay centered."
        ),
        DiscoveryType.MIMIC_PLATFORM to EntityDetail(
            id = "MIMIC_PLATFORM",
            discoveryType = DiscoveryType.MIMIC_PLATFORM,
            threatId = "ENT_MIMIC",
            platformType = PlatformType.MIMIC,
            powerUpType = null,
            status = "DANGER // STRUCTURAL FAILURE",
            classification = "DECEPTIVE UNSTABLE MATTER",
            archiveRecord = "Unstable matter that mimics standard structural patterns, waiting for physical contact to shatter.",
            tacticalNote = "Shatters instantly on landing and deals 15 integrity damage. Look for the reality-tear glitches to identify them."
        ),

        // --- HAZARDS ---
        DiscoveryType.HAZARD_LIGHTNING to EntityDetail(
            id = "HAZARD_LIGHTNING",
            discoveryType = DiscoveryType.HAZARD_LIGHTNING,
            threatId = "HAZ_LIGHTNING",
            platformType = null,
            powerUpType = null,
            status = "EXTREME // STATIC DISCHARGE",
            classification = "ELECTRICAL ATMOSPHERIC STORM",
            archiveRecord = "Friction between high-altitude clouds creates lethal arcs of energy that track conductive droid hulls.",
            tacticalNote = "Strike Damage: 25 Shield. Watch for preceding blue sparks. Move horizontally to dodge the discharge."
        ),
        DiscoveryType.HAZARD_DEBRIS to EntityDetail(
            id = "HAZARD_DEBRIS",
            discoveryType = DiscoveryType.HAZARD_DEBRIS,
            threatId = "HAZ_DEBRIS",
            platformType = null,
            powerUpType = null,
            status = "MODERATE // KINETIC DANGER",
            classification = "ORBITAL WRECKAGE BELT",
            archiveRecord = "Centuries of orbital junk and failed expedition remnants that form a hazardous belt around the planet.",
            tacticalNote = "Collision Damage: 25 Hull. Debris moves at constant velocity but can be difficult to track in dense fields."
        ),
        DiscoveryType.HAZARD_RADIATION to EntityDetail(
            id = "HAZARD_RADIATION",
            discoveryType = DiscoveryType.HAZARD_RADIATION,
            threatId = "HAZ_RADIATION",
            platformType = null,
            powerUpType = null,
            status = "HIGH // IONIZING ENERGY",
            classification = "COSMIC RADIATION ZONE",
            archiveRecord = "Weakened regions of the magnetic field where high-energy particles penetrate ship shielding.",
            tacticalNote = "Shield Drain: 15/sec. Do not linger in green radiation zones. Quick vertical traversal is required."
        ),
        DiscoveryType.HAZARD_SOLAR_FLARE to EntityDetail(
            id = "HAZARD_SOLAR_FLARE",
            discoveryType = DiscoveryType.HAZARD_SOLAR_FLARE,
            threatId = "HAZ_SOLAR_FLARE",
            platformType = null,
            powerUpType = null,
            status = "CRITICAL // PLASMA WAVE",
            classification = "CORONAL MASS EJECTION",
            archiveRecord = "Massive solar eruptions that wash the atmosphere in extreme thermal energy, overriding engine coolers.",
            tacticalNote = "Heat Gain: +120/sec. Rapidly leads to engine shutdown. Find cover or use a Heat Sink immediately."
        ),
        DiscoveryType.HAZARD_TURBULENCE to EntityDetail(
            id = "HAZARD_TURBULENCE",
            discoveryType = DiscoveryType.HAZARD_TURBULENCE,
            threatId = "HAZ_TURBULENCE",
            platformType = null,
            powerUpType = null,
            status = "MODERATE // UNSTABLE AIR",
            classification = "ATMOSPHERIC JET STREAM",
            archiveRecord = "Unstable air masses created by thermal gradients that challenge even the most advanced flight droids.",
            tacticalNote = "Drift Force: 1200 U/S. Disrupts steering and altitude. Stability platforms or Vector Thrusters are required."
        ),
        DiscoveryType.HAZARD_GRAVITY to EntityDetail(
            id = "HAZARD_GRAVITY",
            discoveryType = DiscoveryType.HAZARD_GRAVITY,
            threatId = "HAZ_GRAVITY",
            platformType = null,
            powerUpType = null,
            status = "SEVERE // SPATIAL WARPING",
            classification = "LOCALIZED GRAVITY DISTORTION",
            archiveRecord = "Anomalies created by collapsing gravity stabilizers in high orbit, pulling objects toward the planet.",
            tacticalNote = "Downward Pull: 1500 U/S. Drastically increases fuel consumption. Do not attempt to hover inside the distortion."
        ),
        DiscoveryType.HAZARD_EMP to EntityDetail(
            id = "HAZARD_EMP",
            discoveryType = DiscoveryType.HAZARD_EMP,
            threatId = "HAZ_EMP",
            platformType = null,
            powerUpType = null,
            status = "MODERATE // ELECTRONIC SHOCK",
            classification = "ELECTROMAGNETIC PULSE RING",
            archiveRecord = "Energy bursts from malfunctioning orbital sentries that scramble shield capacitors on contact.",
            tacticalNote = "Effect: Disables Shield Regen for 5s. Avoid the expanding energy rings to maintain defensive integrity."
        ),
        DiscoveryType.HAZARD_VOID_ANOMALY to EntityDetail(
            id = "HAZARD_VOID_ANOMALY",
            discoveryType = DiscoveryType.HAZARD_VOID_ANOMALY,
            threatId = "HAZ_VOID_ANOMALY",
            platformType = null,
            powerUpType = null,
            status = "CRITICAL // REALITY BREACH",
            classification = "NON-EUCLIDEAN SPATIAL RIFT",
            archiveRecord = "Fractures in the dimensional fabric where the Void bleeds into our reality, pulling matter into the breach.",
            tacticalNote = "Pull Force: 1200 U/S. Use maximum thrust to escape the event horizon. Trust physical position over HUD."
        ),
        DiscoveryType.HAZARD_CRYO_MIST to EntityDetail(
            id = "HAZARD_CRYO_MIST",
            discoveryType = DiscoveryType.HAZARD_CRYO_MIST,
            threatId = "HAZ_CRYO_MIST",
            platformType = null,
            powerUpType = null,
            status = "STABLE // ABSOLUTE ZERO",
            classification = "SUPER-COOLED VAPOR FIELD",
            archiveRecord = "Atmospheric moisture flash-frozen by anomalous spatial eddies, locking matter in its current thermal state.",
            tacticalNote = "Effect: Thermal Lock. While inside, engine heat cannot change. Use this to maintain thrust without overheating."
        ),
        DiscoveryType.HAZARD_MIRROR_SHARDS to EntityDetail(
            id = "HAZARD_MIRROR_SHARDS",
            discoveryType = DiscoveryType.HAZARD_MIRROR_SHARDS,
            threatId = "HAZ_MIRROR_SHARDS",
            platformType = null,
            powerUpType = null,
            status = "SEVERE // OPTICAL DISTORTION",
            classification = "FRAGMENTED REALITY REFRACTION",
            archiveRecord = "Rotating purple shards that refract visual and physical space, inverting the navigation logic of droids.",
            tacticalNote = "Effect: Axis Inversion. Left becomes Right, Right becomes Left. Requires immediate pilot adaptation."
        ),
        DiscoveryType.HAZARD_GRAVITY_SHEAR to EntityDetail(
            id = "HAZARD_GRAVITY_SHEAR",
            discoveryType = DiscoveryType.HAZARD_GRAVITY_SHEAR,
            threatId = "HAZ_GRAVITY_SHEAR",
            platformType = null,
            powerUpType = null,
            status = "SEVERE // TIDAL FORCES",
            classification = "OPPOSING GRAVITATIONAL VECTOR",
            archiveRecord = "Localized spatial warping where gravity waves collide, creating dangerous vertical tides of opposing force.",
            tacticalNote = "Effect: Split Force. Top half pushes UP, bottom half pulls DOWN. Navigate the center line with precision."
        ),

        // --- ENEMIES ---
        DiscoveryType.ENEMY_HEAT_BAT to EntityDetail(
            id = "ENEMY_HEAT_BAT",
            discoveryType = DiscoveryType.ENEMY_HEAT_BAT,
            threatId = "ENT_HEAT_BAT",
            platformType = null,
            powerUpType = null,
            status = "HUNTING // THERMAL DETECTION",
            classification = "ATMOSPHERIC THERMAL PREDATOR",
            archiveRecord = "Predatory shadows that strike when engine exhaust reaches critical temperatures. They are invisible to radar.",
            tacticalNote = "Trigger: Heat >= 70%. Keep engines cool to avoid detection. They will dive instantly once triggered."
        ),
        DiscoveryType.ENEMY_VOID_HARVESTER to EntityDetail(
            id = "ENEMY_VOID_HARVESTER",
            discoveryType = DiscoveryType.ENEMY_VOID_HARVESTER,
            threatId = "ENT_VOID_HARVESTER",
            platformType = null,
            powerUpType = null,
            status = "SCAVENGING // RESOURCE HUNTER",
            classification = "REPURPOSED SALVAGE UNIT",
            archiveRecord = "A mechanical squid that prioritizes power-ups and salvage over direct combat, hoarding them in its core.",
            tacticalNote = "Target: Power-Ups. Rushes to spawned items. Rewards fast racing for resources before they are consumed."
        ),
        DiscoveryType.ENEMY_PHASE_WRAITH to EntityDetail(
            id = "ENEMY_PHASE_WRAITH",
            discoveryType = DiscoveryType.ENEMY_PHASE_WRAITH,
            threatId = "ENT_PHASE_WRAITH",
            platformType = null,
            powerUpType = null,
            status = "SPECTRAL // OUT OF PHASE",
            classification = "ETHEREAL DIMENSIONAL GUARDIAN",
            archiveRecord = "Entities that exist partially out of phase, drawn to the erratic reactor signatures of overheated droids.",
            tacticalNote = "Vulnerability: Overheat Only. Can only be damaged when the player ship is in an Overheated state."
        ),
        DiscoveryType.ENEMY_GRAVITY_RAM to EntityDetail(
            id = "ENEMY_GRAVITY_RAM",
            discoveryType = DiscoveryType.ENEMY_GRAVITY_RAM,
            threatId = "ENT_GRAVITY_RAM",
            platformType = null,
            powerUpType = null,
            status = "DANGER // KINETIC STRIKE",
            classification = "GEOMETRIC KINETIC CONSTRUCT",
            archiveRecord = "Heavy kinetic strikers used for orbital clearance, now repurposed for automated aggression against droids.",
            tacticalNote = "Attack: Telegraphed Dash. Watch for the red line indicator and prepare for a massive knockback charge."
        ),
        DiscoveryType.ENEMY_SCOUT_DRONE to EntityDetail(
            id = "ENEMY_SCOUT_DRONE",
            discoveryType = DiscoveryType.ENEMY_SCOUT_DRONE,
            threatId = "ENT_SCOUT_DRONE",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // SURVEILLANCE",
            classification = "AUTONOMOUS SURVEYOR PROBE",
            archiveRecord = "The primary surveillance units of the Ascension Program, reporting unauthorized vertical movement.",
            tacticalNote = "Avoid the scanning beam. Once detected, the drone will alert reinforcements and initiate pursuit."
        ),
        DiscoveryType.ENEMY_CLOUD_SKIMMER to EntityDetail(
            id = "ENEMY_CLOUD_SKIMMER",
            discoveryType = DiscoveryType.ENEMY_CLOUD_SKIMMER,
            threatId = "ENT_CLOUD_SKIMMER",
            platformType = null,
            powerUpType = null,
            status = "STABLE // DRIFTING",
            classification = "HIGH-ALTITUDE ORGANISM",
            archiveRecord = "Biological entities that have adapted to ride jet streams, staying aloft through passive buoyancy.",
            tacticalNote = "Proximity Effect: Upward Lift. Their presence creates a localized updraft that can help conserve fuel."
        ),
        DiscoveryType.ENEMY_SWARM_BOTS to EntityDetail(
            id = "ENEMY_SWARM_BOTS",
            discoveryType = DiscoveryType.ENEMY_SWARM_BOTS,
            threatId = "ENT_SWARM_BOTS",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // FLOCKING",
            classification = "NANO-COLONY CLUSTER",
            archiveRecord = "Microscopic construction units that have grouped into hostile clusters, reacting as a single intelligence.",
            tacticalNote = "Individual units are weak, but the cluster can disrupt flight paths. Use high-speed thrust to break through."
        ),
        DiscoveryType.ENEMY_ORBITAL_SENTRY to EntityDetail(
            id = "ENEMY_ORBITAL_SENTRY",
            discoveryType = DiscoveryType.ENEMY_ORBITAL_SENTRY,
            threatId = "ENT_ORBITAL_SENTRY",
            platformType = null,
            powerUpType = null,
            status = "SENTINEL // PULSE READY",
            classification = "ANCIENT DEFENSE NODE",
            archiveRecord = "Ancient orbital structures still following corrupted defensive protocols, firing on all identified threats.",
            tacticalNote = "Target: Line-of-Sight. Their pulse bolts track positions accurately. Stay behind platforms to block fire."
        ),
        DiscoveryType.ENEMY_CORRUPTED_HULL to EntityDetail(
            id = "ENEMY_CORRUPTED_HULL",
            discoveryType = DiscoveryType.ENEMY_CORRUPTED_HULL,
            threatId = "ENT_CORRUPTED_HULL",
            platformType = null,
            powerUpType = null,
            status = "DORMANT // SALVAGE READY",
            classification = "GHOST EXPEDITION VESSEL",
            archiveRecord = "Wreckage of previous pilot hulls, now possessed by anomalous electromagnetic echoes from the Void.",
            tacticalNote = "Interact with these derelicts to recover valuable salvage, power-ups, and encrypted lore logs."
        ),
        DiscoveryType.ENEMY_STALKER to EntityDetail(
            id = "ENEMY_STALKER",
            discoveryType = DiscoveryType.ENEMY_STALKER,
            threatId = "ENT_STALKER",
            platformType = null,
            powerUpType = null,
            status = "HUNTING // THERMAL LOCK",
            classification = "DEEP-SPACE TRACKER UNIT",
            archiveRecord = "Predatory machines designed to track thermal signatures across the vacuum with lethal efficiency.",
            tacticalNote = "Aggression scales with Heat. Keep your temperature low to evade pursuit. Use Heat Sinks to drop tracking."
        ),
        DiscoveryType.ENEMY_VOID_WHALE to EntityDetail(
            id = "ENEMY_VOID_WHALE",
            discoveryType = DiscoveryType.ENEMY_VOID_WHALE,
            threatId = "ENT_VOID_WHALE",
            platformType = null,
            powerUpType = null,
            status = "STABLE // COLOSSAL",
            classification = "ETHEREAL COSMIC LEVIATHAN",
            archiveRecord = "Massive beings that drift through the outer reaches, disturbing space-time as they move through the vacuum.",
            tacticalNote = "Creates massive slipstreams that can push or pull your ship. Avoid the wake to maintain control."
        ),
        DiscoveryType.ENEMY_VOID_WRAITH to EntityDetail(
            id = "ENEMY_VOID_WRAITH",
            discoveryType = DiscoveryType.ENEMY_VOID_WRAITH,
            threatId = "ENT_VOID_WRAITH",
            platformType = null,
            powerUpType = null,
            status = "HUNTING // FUEL DRAIN",
            classification = "NON-EUCLIDEAN SHADOW ENTITY",
            archiveRecord = "Entities that exist only within the Void, phasing between dimensions to siphon energy from droids.",
            tacticalNote = "Vulnerability: Materialization. They are only damageable when they materialize to drain your fuel reserves."
        ),

        // --- BOSSES ---
        DiscoveryType.THREAT_SENTINEL to EntityDetail(
            id = "THREAT_SENTINEL",
            discoveryType = DiscoveryType.THREAT_SENTINEL,
            threatId = "MINI_BOSS_COMMANDER",
            platformType = null,
            powerUpType = null,
            status = "COMMANDER // ARMOR ACTIVE",
            classification = "AUTONOMOUS DEFENSE PLATFORM",
            archiveRecord = "A heavy tactical vessel overseeing surveillance and regional drone deployments in the upper atmosphere.",
            tacticalNote = "Destroy the 4 shield emitters to expose the reactor core. Watch for expanding gravity pulses."
        ),
        DiscoveryType.THREAT_VOID_ENGINE to EntityDetail(
            id = "THREAT_VOID_ENGINE",
            discoveryType = DiscoveryType.THREAT_VOID_ENGINE,
            threatId = "BOSS_VOID_ENGINE",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // SPATIAL WARPING",
            classification = "NON-MATERIAL ENGINE CORE",
            archiveRecord = "A massive structure drifting in the Void that seems to fold space, acting as a terminal gateway.",
            tacticalNote = "Periodically alters the direction of gravity. Time your jumps to match the shifting environmental tides."
        ),
        DiscoveryType.THREAT_GATEKEEPER to EntityDetail(
            id = "THREAT_GATEKEEPER",
            discoveryType = DiscoveryType.THREAT_GATEKEEPER,
            threatId = "BOSS_GATEKEEPER",
            platformType = null,
            powerUpType = null,
            status = "SENTINEL // PULSE ACTIVE",
            classification = "ANCIENT ORBITAL COMMANDER",
            archiveRecord = "The primary command unit for the orbital defense grid, programmed to prevent unauthorized departures.",
            tacticalNote = "Target rotating safe zones. Attack weak points when the shield gaps align. Beware the central beam."
        ),
        DiscoveryType.THREAT_STAR_EATER to EntityDetail(
            id = "THREAT_STAR_EATER",
            discoveryType = DiscoveryType.THREAT_STAR_EATER,
            threatId = "BOSS_STAR_EATER",
            platformType = null,
            powerUpType = null,
            status = "FEEDING // ENERGY DRAIN",
            classification = "COLOSSAL COSMIC ORGANISM",
            archiveRecord = "A creature that feeds on electromagnetic radiation, pulling light and energy toward its central maw.",
            tacticalNote = "Pulls all power-ups toward itself. Race to collect them before it consumes them to gain strength."
        ),
        DiscoveryType.THREAT_LEVIATHAN to EntityDetail(
            id = "THREAT_LEVIATHAN",
            discoveryType = DiscoveryType.THREAT_LEVIATHAN,
            threatId = "BOSS_LEVIATHAN",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // TIDE PULL",
            classification = "GIGANTIC VACUUM CREATURE",
            archiveRecord = "An ancient biological entity that moves through the vacuum as if it were water, guarding its territory.",
            tacticalNote = "Its segments are invulnerable except for the core near its maw. Stay clear of the massive wake."
        ),
        DiscoveryType.THREAT_SIGNAL to EntityDetail(
            id = "THREAT_SIGNAL",
            discoveryType = DiscoveryType.THREAT_SIGNAL,
            threatId = "BOSS_SIGNAL",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // HUD INTERFERENCE",
            classification = "UNKNOWN DATA INTELLIGENCE",
            archiveRecord = "The source of the transmissions that started the Program. It exists as data, not physical matter.",
            tacticalNote = "Creates deceptive navigation cues. Trust your physical position over the distorted HUD indicators."
        ),
        DiscoveryType.THREAT_THERMAL_HIVE to EntityDetail(
            id = "THREAT_THERMAL_HIVE",
            discoveryType = DiscoveryType.THREAT_THERMAL_HIVE,
            threatId = "MINI_BOSS_THERMAL_HIVE",
            platformType = null,
            powerUpType = null,
            status = "CHARGING // HEAT SENSITIVE",
            classification = "THERMAL SUMMONER COLLECTIVE",
            archiveRecord = "A biological mass that reacts violently to engine exhaust, spawning drones to neutralize thermal sources.",
            tacticalNote = "Only spawns Swarm Bots when player Heat is high. Manage your cooling to prevent being overwhelmed."
        ),
        DiscoveryType.THREAT_GRAVITY_ANCHOR to EntityDetail(
            id = "THREAT_GRAVITY_ANCHOR",
            discoveryType = DiscoveryType.THREAT_GRAVITY_ANCHOR,
            threatId = "MINI_BOSS_GRAVITY_ANCHOR",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // GRAVITY TETHER",
            classification = "SCALING SPATIAL STABILIZER",
            archiveRecord = "A massive gravitational stabilizer that has failed and now pulls all local matter into its singularity.",
            tacticalNote = "Downward pull increases every 10 seconds. You must ascend rapidly to escape the tidal pull."
        ),
        DiscoveryType.THREAT_FORGER to EntityDetail(
            id = "THREAT_FORGER",
            discoveryType = DiscoveryType.THREAT_FORGER,
            threatId = "MINI_BOSS_FORGER",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // CONVERSION CYCLE",
            classification = "MALFUNCTIONING FABRICATOR",
            archiveRecord = "An ancient industrial unit that reconfigures its surroundings, converting safe platforms into lethal traps.",
            tacticalNote = "Periodically turns Normal platforms into Ice or Breakable. Plan your landing path ahead of the pulse."
        ),
        DiscoveryType.THREAT_ARCHITECT to EntityDetail(
            id = "THREAT_ARCHITECT",
            discoveryType = DiscoveryType.THREAT_ARCHITECT,
            threatId = "BOSS_ARCHITECT",
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // LEVEL CONTROL",
            classification = "MASTER SYSTEM OVERSEER",
            archiveRecord = "The master intelligence behind the Sky structures. It controls the very environment you climb through.",
            tacticalNote = "Level is the Boss. Rhythmically adds and removes platforms. Speed and rhythm are your only defense."
        ),
        DiscoveryType.THREAT_ENTROPY_CORE to EntityDetail(
            id = "THREAT_ENTROPY_CORE",
            discoveryType = DiscoveryType.THREAT_ENTROPY_CORE,
            threatId = "BOSS_ENTROPY_CORE",
            platformType = null,
            powerUpType = null,
            status = "CRITICAL // SYSTEM DRAIN",
            classification = "ENTROPIC ENERGY RADIATOR",
            archiveRecord = "The primary radiator for the orbital foundry, now leaking anti-energy that drains all droid systems.",
            tacticalNote = "Multi-Vector Drain: Fuel/Shield/Heat pressure. Destroy 4 pylon clusters to destabilize the core."
        ),
        DiscoveryType.THREAT_SINGULARITY to EntityDetail(
            id = "THREAT_SINGULARITY",
            discoveryType = DiscoveryType.THREAT_SINGULARITY,
            threatId = "BOSS_SINGULARITY",
            platformType = null,
            powerUpType = null,
            status = "TERMINAL // REALITY COLLAPSE",
            classification = "THE ULTIMATE INTELLIGENCE",
            archiveRecord = "The terminal point of the signal. It doesn't just fight; it distorts your perception of reality.",
            tacticalNote = "HUD Distortion. Pulls all UI gauges into the screen center. Instinct is your only remaining guide."
        ),

        // --- POWER-UPS ---
        DiscoveryType.FUEL_TANK to EntityDetail(
            id = "FUEL_TANK",
            discoveryType = DiscoveryType.FUEL_TANK,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.FUEL_TANK,
            status = "STABLE // FUEL READY",
            classification = "LIQUID OXYGEN RESERVOIR",
            archiveRecord = "Standardized fuel tanks recovered from previous expeditions, essential for long-range vertical ascent.",
            tacticalNote = "Increases Max Fuel by 25 (up to 300). refills current reserves. Prioritize for endurance builds."
        ),
        DiscoveryType.TURBO_BOOSTER to EntityDetail(
            id = "TURBO_BOOSTER",
            discoveryType = DiscoveryType.TURBO_BOOSTER,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.TURBO_BOOSTER,
            status = "STABLE // THRUST BOOST",
            classification = "EXPERIMENTAL INJECTOR",
            archiveRecord = "An experimental fuel injector that temporarily overrides engine safety limits for massive thrust.",
            tacticalNote = "Thrust Multiplier: 1.2x. Duration: 8s. Use to escape gravity anomalies or speed through hazard zones."
        ),
        DiscoveryType.EFFICIENCY_MODULE to EntityDetail(
            id = "EFFICIENCY_MODULE",
            discoveryType = DiscoveryType.EFFICIENCY_MODULE,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.EFFICIENCY_MODULE,
            status = "STABLE // OPTIMIZER ACTIVE",
            classification = "COMBUSTION TUNING UNIT",
            archiveRecord = "Optimizes fuel-to-air ratios for thinner atmospheric conditions, reducing resource consumption.",
            tacticalNote = "Consumption: 0.8x. Duration: 8s. Combine with Turbo Booster for efficient long-distance traversal."
        ),
        DiscoveryType.HEAT_SINK to EntityDetail(
            id = "HEAT_SINK",
            discoveryType = DiscoveryType.HEAT_SINK,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.HEAT_SINK,
            status = "STABLE // THERMAL VENT",
            classification = "HIGH-VACUUM COOLING ARRAY",
            archiveRecord = "Advanced thermal paste and cooling fins designed for heat dissipation in low-density environments.",
            tacticalNote = "Vent: 50 Heat. Instantly clears Overheated state. Vital for high-thrust Experimental rocket builds."
        ),
        DiscoveryType.SHIELD_CAPSULE to EntityDetail(
            id = "SHIELD_CAPSULE",
            discoveryType = DiscoveryType.SHIELD_CAPSULE,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.SHIELD_CAPSULE,
            status = "STABLE // ENERGY READY",
            classification = "RECOVERY CAPACITOR ARRAY",
            archiveRecord = "Automated capacitor arrays recovered from drone wreckage, used to jumpstart shield regeneration.",
            tacticalNote = "Recharge: 25 Shield. Shields protect hull integrity from direct damage. Keep them active at all times."
        ),
        DiscoveryType.HULL_REPAIR to EntityDetail(
            id = "HULL_REPAIR",
            discoveryType = DiscoveryType.HULL_REPAIR,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.HULL_REPAIR,
            status = "STABLE // REPAIR NANO-FLUID",
            classification = "NANITE WELDING AGENT",
            archiveRecord = "Nanite-based agents designed for rapid field repairs of droid chassis during vertical expeditions.",
            tacticalNote = "Restore: 20 Hull. The only way to restore health during a run. Prioritize when integrity is critical."
        ),
        DiscoveryType.KINETIC_BATTERY to EntityDetail(
            id = "KINETIC_BATTERY",
            discoveryType = DiscoveryType.KINETIC_BATTERY,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.KINETIC_BATTERY,
            status = "ACTIVE // ENERGY CONVERSION",
            classification = "IMPACT ABSORPTION UNIT",
            archiveRecord = "Converts landing impact into a shield recharge or small thrust burst through kinetic recovery.",
            tacticalNote = "Effect: Impact to Energy. While active, successful landings restore small amounts of fuel and shield."
        ),
        DiscoveryType.MAGNETIC_SIPHON to EntityDetail(
            id = "MAGNETIC_SIPHON",
            discoveryType = DiscoveryType.MAGNETIC_SIPHON,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.MAGNETIC_SIPHON,
            status = "ACTIVE // ATTRACTION RADIUS",
            classification = "AUTOMATED RESOURCE COLLECTOR",
            archiveRecord = "Generates a localized magnetic field that pulls nearby salvage and power-ups toward the hull.",
            tacticalNote = "Radius: 400m. Automatically pulls nearby resources. High utility during intense combat encounters."
        ),
        DiscoveryType.OVERDRIVE_MODULE to EntityDetail(
            id = "OVERDRIVE_MODULE",
            discoveryType = DiscoveryType.OVERDRIVE_MODULE,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.OVERDRIVE_MODULE,
            status = "CRITICAL // SAFETY BYPASS",
            classification = "HIGH-RISK THRUST ENHANCER",
            archiveRecord = "A 'last resort' module that bypasses reactor dampeners for maximum speed at the cost of structural integrity.",
            tacticalNote = "Effect: Massive Thrust / Hull Decay. Use only when death is certain otherwise. Great for escape."
        ),
        DiscoveryType.ALTITUDE_BOOSTER to EntityDetail(
            id = "ALTITUDE_BOOSTER",
            discoveryType = DiscoveryType.ALTITUDE_BOOSTER,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.ALTITUDE_BOOSTER,
            status = "READY // ALTITUDE IMPULSE",
            classification = "CONCENTRATED FUEL SHOT",
            archiveRecord = "Provides a massive vertical thrust designed for rapid escape from dense atmospheric hazard zones.",
            tacticalNote = "Impulse: 2500 U/S. Instantly propels you through hundreds of meters. Watch for obstacles above!"
        ),
        DiscoveryType.POWERUP_ARTIFACT to EntityDetail(
            id = "POWERUP_ARTIFACT",
            discoveryType = DiscoveryType.POWERUP_ARTIFACT,
            threatId = null,
            platformType = null,
            powerUpType = PowerUpType.ARTIFACT,
            status = "STABLE // DATA CACHE",
            classification = "SEALED LORE CONTAINER",
            archiveRecord = "Sealed containers holding valuable data fragments from the old world and previous failed missions.",
            tacticalNote = "Unlocks new entries in the Data Archives and Artifact Sets. Collect to decipher the Great Signal."
        ),

        // --- ROCKETS ---
        DiscoveryType.ROCKET_BALANCED to EntityDetail(
            id = "ROCKET_BALANCED",
            discoveryType = DiscoveryType.ROCKET_BALANCED,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "OPERATIONAL // BALANCED",
            classification = "EXPLORER CLASS VESSEL",
            archiveRecord = "Reliable and stable, it has carried many pilots on their first ascent into the upper atmosphere.",
            tacticalNote = "Stats: 100% Thrust / 100% Fuel. Perfect for learning mechanics. No major weaknesses."
        ),
        DiscoveryType.ROCKET_SCOUT to EntityDetail(
            id = "ROCKET_SCOUT",
            discoveryType = DiscoveryType.ROCKET_SCOUT,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "OPERATIONAL // HIGH THRUST",
            classification = "STRIKER CLASS VESSEL",
            archiveRecord = "Designed for rapid surveying, it sacrifices fuel capacity and endurance for unmatched acceleration.",
            tacticalNote = "Stats: 125% Thrust / 70% Fuel. The 'Speeder.' Excellent for dodging and precision flight."
        ),
        DiscoveryType.ROCKET_TANK to EntityDetail(
            id = "ROCKET_TANK",
            discoveryType = DiscoveryType.ROCKET_TANK,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "OPERATIONAL // REINFORCED",
            classification = "HEAVY CLASS VESSEL",
            archiveRecord = "Massive fuel capacity and reinforced plating, built for long-range expeditions where resources are scarce.",
            tacticalNote = "Stats: 85% Thrust / 150% Fuel. The 'Endurance' hull. Survives long stretches without stations."
        ),
        DiscoveryType.ROCKET_EXPERIMENTAL to EntityDetail(
            id = "ROCKET_EXPERIMENTAL",
            discoveryType = DiscoveryType.ROCKET_EXPERIMENTAL,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // PROTOTYPE CORE",
            classification = "HIGH-RISK EXPERIMENTAL VESSEL",
            archiveRecord = "A dangerous prototype that pushes engine technology limits, prone to severe thermal instability.",
            tacticalNote = "Stats: 150% Thrust / 140% Heat. High speed potential. Requires expert thermal timing to manage."
        ),

        // --- AREAS ---
        DiscoveryType.AREA_EARTH to EntityDetail(
            id = "AREA_EARTH",
            discoveryType = DiscoveryType.AREA_EARTH,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "SAFE // ATMOSPHERE 100%",
            classification = "TERRESTRIAL ORIGIN",
            archiveRecord = "The familiar home world. Most droids never return to this altitude after their initial launch.",
            tacticalNote = "Altitude: 0-500m. Establish your flight rhythm here. Collect fuel before the air thins out."
        ),
        DiscoveryType.AREA_CLOUDS to EntityDetail(
            id = "AREA_CLOUDS",
            discoveryType = DiscoveryType.AREA_CLOUDS,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // STORM LAYER",
            classification = "HIGH-ALTITUDE WEATHER SYSTEM",
            archiveRecord = "A realm above the surface weather, dominated by high-speed jet streams and permanent lightning storms.",
            tacticalNote = "Altitude: 500-1500m. Introduction of environmental forces. Watch for horizontal wind drift."
        ),
        DiscoveryType.AREA_ATMOSPHERE to EntityDetail(
            id = "AREA_ATMOSPHERE",
            discoveryType = DiscoveryType.AREA_ATMOSPHERE,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "STABLE // THINNING AIR",
            classification = "UPPER STRATOSPHERE LAYER",
            archiveRecord = "The edge between sky and space. A place of silence and isolation where the sky turns bruised purple.",
            tacticalNote = "Altitude: 1500-4000m. Resource stations become sparse. Precision fuel management is critical."
        ),
        DiscoveryType.AREA_ORBIT to EntityDetail(
            id = "AREA_ORBIT",
            discoveryType = DiscoveryType.AREA_ORBIT,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // WRECKAGE BELT",
            classification = "LOW EARTH ORBIT ZONE",
            archiveRecord = "Where Earth becomes a distant world. A graveyard of forgotten satellites still active after eons.",
            tacticalNote = "Altitude: 4000-8000m. Introduction of solar hazards. Manage heat carefully to avoid detection."
        ),
        DiscoveryType.AREA_SPACE to EntityDetail(
            id = "AREA_SPACE",
            discoveryType = DiscoveryType.AREA_SPACE,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "STABLE // VACUUM READY",
            classification = "UNCHARTED DEEP SPACE",
            archiveRecord = "Beyond established routes. Signals become strange and maps become unreliable. You are truly alone.",
            tacticalNote = "Altitude: 8000-15000m. Encounter massive gravity-shifting entities and resource competition."
        ),
        DiscoveryType.AREA_VOID to EntityDetail(
            id = "AREA_VOID",
            discoveryType = DiscoveryType.AREA_VOID,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "CRITICAL // ANOMALY ZONE",
            classification = "DIMENSIONAL RIFT REGION",
            archiveRecord = "Sensors report impossible readings. The stars themselves seem distorted in this region that should not exist.",
            tacticalNote = "Altitude: 15000m+. Survival at the limits of physics. Trust your eyes over HUD instruments."
        ),
        DiscoveryType.AREA_FOUNDRY to EntityDetail(
            id = "AREA_FOUNDRY",
            discoveryType = DiscoveryType.AREA_FOUNDRY,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // INDUSTRIAL CYCLE",
            classification = "ANCIENT MANUFACTURING BELT",
            archiveRecord = "A vast automated complex suspended in high orbit, still running industrial protocols after eons.",
            tacticalNote = "Region: Industrial. Fast moving platforms and laser grids. Reaching the top is the primary goal."
        ),
        DiscoveryType.AREA_CHRONO_RIFT to EntityDetail(
            id = "AREA_CHRONO_RIFT",
            discoveryType = DiscoveryType.AREA_CHRONO_RIFT,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "UNSTABLE // TEMPORAL SHIFT",
            classification = "FRACTURED TIME STREAM",
            archiveRecord = "Time flows irregularly here. Past and future bleed together in a chaotic temporal region.",
            tacticalNote = "Region: Temporal. Time-dilation bubbles can slow or speed up your ship. Use them to dodge threats."
        ),
        DiscoveryType.AREA_BEYOND to EntityDetail(
            id = "AREA_BEYOND",
            discoveryType = DiscoveryType.AREA_BEYOND,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "UNSTABLE // MATTER BLUR",
            classification = "NON-EUCLIDEAN REGION",
            archiveRecord = "Where matter and energy blur. A place where the laws of physics are merely suggestions.",
            tacticalNote = "Region: Exotic. Standard weapons are less effective. Focus on speed and precise evasion."
        ),
        DiscoveryType.AREA_GATE to EntityDetail(
            id = "AREA_GATE",
            discoveryType = DiscoveryType.AREA_GATE,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "SENTINEL // ARTIFICIAL SKY",
            classification = "MONOLITHIC STELLAR STRUCTURE",
            archiveRecord = "A massive structure spanning the entire horizon, built by a civilization that preceded the Program.",
            tacticalNote = "Region: Ancient. Heavily guarded by orbital defense systems. High combat intensity zone."
        ),
        DiscoveryType.AREA_CONSTRUCT to EntityDetail(
            id = "AREA_CONSTRUCT",
            discoveryType = DiscoveryType.AREA_CONSTRUCT,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ACTIVE // MONOLITHIC",
            classification = "ANCIENT FORTRESS STRUCTURE",
            archiveRecord = "The scale of this structure is beyond human comprehension. It is the heart of the Great Signal.",
            tacticalNote = "Region: Monolithic. Navigate the massive internal corridors. Watch for the 'Heart' of the static."
        ),
        DiscoveryType.AREA_SINGULARITY to EntityDetail(
            id = "AREA_SINGULARITY",
            discoveryType = DiscoveryType.AREA_SINGULARITY,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "TERMINAL // REALITY END",
            classification = "BEYOND REALITY THRESHOLD",
            archiveRecord = "The final threshold. There is no turning back from the point where reality breaks down.",
            tacticalNote = "Region: Terminal. The final test of adaptation. Origin will reset to 0 upon completion."
        ),

        // --- LORE ---
        DiscoveryType.LORE_ASCENSION to EntityDetail(
            id = "LORE_ASCENSION",
            discoveryType = DiscoveryType.LORE_ASCENSION,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "RECORD // DECRYPTED",
            classification = "PROGRAM HISTORY FRAGMENT",
            archiveRecord = "A multinational effort to reach the source of the 'Great Signal' after its discovery decades ago.",
            tacticalNote = "Check archives frequently for new narrative updates synchronized with your highest altitude."
        ),
        DiscoveryType.LORE_SIGNAL to EntityDetail(
            id = "LORE_SIGNAL",
            discoveryType = DiscoveryType.LORE_SIGNAL,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "RECORD // PARTIAL DATA",
            classification = "GREAT SIGNAL ANALYSIS",
            archiveRecord = "It originated from the Void, carrying a repeating sequence that matched no known language or logic.",
            tacticalNote = "Artifact collection increases signal decoding progress. Complete sets reveal true transmissions."
        ),
        DiscoveryType.LORE_LOST_FLEET to EntityDetail(
            id = "LORE_LOST_FLEET",
            discoveryType = DiscoveryType.LORE_LOST_FLEET,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "RECORD // CLASSIFIED",
            classification = "EXPEDITION CASUALTY LOG",
            archiveRecord = "Records of multiple vessels that vanished during the initial push into the Void, their signals lost forever.",
            tacticalNote = "Derelict hulls in Deep Space contain encrypted logs from these lost missions. Salvage them."
        ),
        DiscoveryType.LORE_LOGS to EntityDetail(
            id = "LORE_LOGS",
            discoveryType = DiscoveryType.LORE_LOGS,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "RECORD // FRAGMENT",
            classification = "SYSTEM PROTOCOL LOG",
            archiveRecord = "Encrypted data logs suggesting the signal source might be internal to the planet, reflected off the Void.",
            tacticalNote = "Achievement-based lore unlocks. Complete specific objectives to decrypt these fragments."
        ),
        DiscoveryType.DISCOVERY_THE_END to EntityDetail(
            id = "DISCOVERY_THE_END",
            discoveryType = DiscoveryType.DISCOVERY_THE_END,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "RECORD // TERMINAL",
            classification = "THE FINAL TRUTH",
            archiveRecord = "The Ascension Program is complete. You have crossed the Singularity and seen what lies beyond the climb.",
            tacticalNote = "Unlocks Ascension Prestige. Restart the journey with increased difficulty and legendary modules."
        ),

        DiscoveryType.LOG_GENERIC to EntityDetail(
            id = "LOG_GENERIC",
            discoveryType = DiscoveryType.LOG_GENERIC,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ARCHIVED // DECRYPTED",
            classification = "HISTORICAL DATA LOG",
            archiveRecord = "A fragment of personal or official communication recovered from a previous mission. These logs provide a glimpse into the lives of those who climbed before us.",
            tacticalNote = "Logs provide narrative context and lore. They do not directly impact flight physics but are essential for understanding the Great Signal."
        ),

        DiscoveryType.ACHIEVEMENT_GENERIC to EntityDetail(
            id = "ACHIEVEMENT_GENERIC",
            discoveryType = DiscoveryType.ACHIEVEMENT_GENERIC,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "CERTIFIED // PILOT RECORD",
            classification = "OPERATIONAL EXCELLENCE AWARD",
            archiveRecord = "A formal recognition of exceptional performance, technical skill, or resilience displayed during the Ascension Program.",
            tacticalNote = "Achievements represent mastery of game systems. Most unlock specialized ship skins or engine trail effects."
        ),

        // --- ARTIFACTS ---
        DiscoveryType.ART_RECORDER to EntityDetail(
            id = "ART_RECORDER",
            discoveryType = DiscoveryType.ART_RECORDER,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // DAMAGED",
            classification = "FLIGHT RECORDER UNIT",
            archiveRecord = "A damaged recording unit from a previous mission, containing garbled audio of 'structures in the clouds'.",
            tacticalNote = "Part of the 'Lost Expedition' set. Collect the full set to unlock the audio transcript."
        ),
        DiscoveryType.ART_ALLOY to EntityDetail(
            id = "ART_ALLOY",
            discoveryType = DiscoveryType.ART_ALLOY,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // EXOTIC",
            classification = "UNKNOWN CRYSTALLINE ALLOY",
            archiveRecord = "A fragment of material with a molecular structure suggesting it was grown, not forged or machined.",
            tacticalNote = "Part of the 'Exotic Matter' set. Increases damage resistance bonus when set is complete."
        ),
        DiscoveryType.ART_BEACON to EntityDetail(
            id = "ART_BEACON",
            discoveryType = DiscoveryType.ART_BEACON,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // BROADCASTING",
            classification = "ENCRYPTED SIGNAL BEACON",
            archiveRecord = "Found drifting in orbit, continuously broadcasting a single prime number that changes every eon.",
            tacticalNote = "Part of the 'Signal Echo' set. Increases discovery range when set is complete."
        ),
        DiscoveryType.ART_DRONE to EntityDetail(
            id = "ART_DRONE",
            discoveryType = DiscoveryType.ART_DRONE,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // LOOPING",
            classification = "SURVEY DRONE CORE",
            archiveRecord = "The AI within this core is stuck in a loop, repeating 'The Void calls' across all binary frequencies.",
            tacticalNote = "Part of the 'Automated Legacy' set. Grants small fuel efficiency bonus when set is complete."
        ),
        DiscoveryType.ART_PRE_SIGNAL_MAP to EntityDetail(
            id = "ART_PRE_SIGNAL_MAP",
            discoveryType = DiscoveryType.ART_PRE_SIGNAL_MAP,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // HISTORICAL",
            classification = "PRE-SIGNAL STAR CHART",
            archiveRecord = "A primitive star chart dating before the Ascension Program, showing a different Void path.",
            tacticalNote = "Part of the 'Old World' set. Unlocks Lore View in the Archives when complete."
        ),
        DiscoveryType.ART_BIOMECH_SHARD to EntityDetail(
            id = "ART_BIOMECH_SHARD",
            discoveryType = DiscoveryType.ART_BIOMECH_SHARD,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // PULSING",
            classification = "BIOMECHANICAL HULL SHARD",
            archiveRecord = "A hull segment that pulses with a strange heartbeat, proving some threats are more than machine.",
            tacticalNote = "Part of the 'Construct' set. Increases permanent integrity bonus when set is complete."
        ),
        DiscoveryType.ART_ARCHITECT_SIGNATURE to EntityDetail(
            id = "ART_ARCHITECT_SIGNATURE",
            discoveryType = DiscoveryType.ART_ARCHITECT_SIGNATURE,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "ITEM // ULTIMATE",
            classification = "THE ARCHITECT'S KEY",
            archiveRecord = "A cryptographic key found at the Singularity. It carries the mark of the one who built the sky.",
            tacticalNote = "The ultimate artifact. Required for performing 'Prestige' resets at Point Zero."
        ),

        // --- MECHANICS ---
        DiscoveryType.HEAT_SYSTEM to EntityDetail(
            id = "HEAT_SYSTEM",
            discoveryType = DiscoveryType.HEAT_SYSTEM,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "SYSTEM // THERMAL",
            classification = "ENGINE HEAT MANAGEMENT",
            archiveRecord = "Atmospheric friction and engine stress create thermal energy that must be managed by the droid.",
            tacticalNote = "Thrusting generates heat. Stop thrusting to cool. High heat triggers predatory Heat Bats."
        ),
        DiscoveryType.OVERHEAT_SYSTEM to EntityDetail(
            id = "OVERHEAT_SYSTEM",
            discoveryType = DiscoveryType.OVERHEAT_SYSTEM,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "STATUS // CRITICAL",
            classification = "THERMAL LOCKOUT PROTOCOL",
            archiveRecord = "Automated safety protocols that lock the engine to prevent catastrophic meltdown when heat is maxed.",
            tacticalNote = "Locked for 2s. You cannot thrust but retain steering. Use Heat Sinks to instantly clear this state."
        ),
        DiscoveryType.EFFICIENCY_SURVIVAL to EntityDetail(
            id = "EFFICIENCY_SURVIVAL",
            discoveryType = DiscoveryType.EFFICIENCY_SURVIVAL,
            threatId = null,
            platformType = null,
            powerUpType = null,
            status = "SYSTEM // COMBO",
            classification = "FLIGHT EFFICIENCY REWARDS",
            archiveRecord = "Maintaining peak flight rhythm triggers emergency supply drops from supporting orbital droids.",
            tacticalNote = "Land on consecutive platforms to build a combo. High combos spawn fuel and repair rewards."
        )
    )

    private val threatMap: Map<DiscoveryType, String> = mapOf(
        DiscoveryType.HAZARD_LIGHTNING to "HAZ_LIGHTNING",
        DiscoveryType.HAZARD_DEBRIS to "HAZ_DEBRIS",
        DiscoveryType.HAZARD_RADIATION to "HAZ_RADIATION",
        DiscoveryType.HAZARD_SOLAR_FLARE to "HAZ_SOLAR_FLARE",
        DiscoveryType.HAZARD_TURBULENCE to "HAZ_TURBULENCE",
        DiscoveryType.HAZARD_GRAVITY to "HAZ_GRAVITY",
        DiscoveryType.HAZARD_EMP to "HAZ_EMP",
        DiscoveryType.HAZARD_VOID_ANOMALY to "HAZ_VOID_ANOMALY",
        DiscoveryType.HAZARD_CRYO_MIST to "HAZ_CRYO_MIST",
        DiscoveryType.HAZARD_MIRROR_SHARDS to "HAZ_MIRROR_SHARDS",
        DiscoveryType.HAZARD_GRAVITY_SHEAR to "HAZ_GRAVITY_SHEAR",
        DiscoveryType.THREAT_SENTINEL to "MINI_BOSS_COMMANDER",
        DiscoveryType.THREAT_VOID_ENGINE to "BOSS_VOID_ENGINE",
        DiscoveryType.THREAT_GATEKEEPER to "BOSS_GATEKEEPER",
        DiscoveryType.THREAT_STAR_EATER to "BOSS_STAR_EATER",
        DiscoveryType.THREAT_LEVIATHAN to "BOSS_LEVIATHAN",
        DiscoveryType.THREAT_SIGNAL to "BOSS_SIGNAL",
        DiscoveryType.THREAT_THERMAL_HIVE to "MINI_BOSS_THERMAL_HIVE",
        DiscoveryType.THREAT_GRAVITY_ANCHOR to "MINI_BOSS_GRAVITY_ANCHOR",
        DiscoveryType.THREAT_FORGER to "MINI_BOSS_FORGER",
        DiscoveryType.THREAT_ARCHITECT to "BOSS_ARCHITECT",
        DiscoveryType.THREAT_ENTROPY_CORE to "BOSS_ENTROPY_CORE",
        DiscoveryType.THREAT_SINGULARITY to "BOSS_SINGULARITY",
        DiscoveryType.ENEMY_HEAT_BAT to "ENT_HEAT_BAT",
        DiscoveryType.ENEMY_VOID_HARVESTER to "ENT_VOID_HARVESTER",
        DiscoveryType.ENEMY_PHASE_WRAITH to "ENT_PHASE_WRAITH",
        DiscoveryType.ENEMY_GRAVITY_RAM to "ENT_GRAVITY_RAM",
        DiscoveryType.ENEMY_SCOUT_DRONE to "ENT_SCOUT_DRONE",
        DiscoveryType.ENEMY_CLOUD_SKIMMER to "ENT_CLOUD_SKIMMER",
        DiscoveryType.ENEMY_SWARM_BOTS to "ENT_SWARM_BOTS",
        DiscoveryType.ENEMY_ORBITAL_SENTRY to "ENT_ORBITAL_SENTRY",
        DiscoveryType.ENEMY_CORRUPTED_HULL to "ENT_CORRUPTED_HULL",
        DiscoveryType.ENEMY_STALKER to "ENT_STALKER",
        DiscoveryType.ENEMY_VOID_WHALE to "ENT_VOID_WHALE",
        DiscoveryType.ENEMY_VOID_WRAITH to "ENT_VOID_WRAITH",
        DiscoveryType.MIMIC_PLATFORM to "ENT_MIMIC"
    )

    private val platformMap: Map<DiscoveryType, PlatformType> = mapOf(
        DiscoveryType.NORMAL_PLATFORM to PlatformType.NORMAL,
        DiscoveryType.MOVING_PLATFORM to PlatformType.MOVING,
        DiscoveryType.BOOST_PLATFORM to PlatformType.BOOST,
        DiscoveryType.ICE_PLATFORM to PlatformType.ICE,
        DiscoveryType.BREAKABLE_PLATFORM to PlatformType.BREAKABLE,
        DiscoveryType.PHASE_PLATFORM to PlatformType.PHASE,
        DiscoveryType.FUEL_PLATFORM to PlatformType.FUEL,
        DiscoveryType.COOLING_PLATFORM to PlatformType.COOLING,
        DiscoveryType.STABILITY_PLATFORM to PlatformType.STABILITY,
        DiscoveryType.MAGNETIC_PLATFORM to PlatformType.MAGNETIC,
        DiscoveryType.FLUX_PLATFORM to PlatformType.FLUX,
        DiscoveryType.GRAVITON_PLATFORM to PlatformType.GRAVITON,
        DiscoveryType.CONVEYOR_PLATFORM to PlatformType.CONVEYOR,
        DiscoveryType.MIMIC_PLATFORM to PlatformType.MIMIC
    )

    private val powerUpMap: Map<DiscoveryType, PowerUpType> = mapOf(
        DiscoveryType.FUEL_TANK to PowerUpType.FUEL_TANK,
        DiscoveryType.TURBO_BOOSTER to PowerUpType.TURBO_BOOSTER,
        DiscoveryType.EFFICIENCY_MODULE to PowerUpType.EFFICIENCY_MODULE,
        DiscoveryType.HEAT_SINK to PowerUpType.HEAT_SINK,
        DiscoveryType.SHIELD_CAPSULE to PowerUpType.SHIELD_CAPSULE,
        DiscoveryType.HULL_REPAIR to PowerUpType.HULL_REPAIR,
        DiscoveryType.KINETIC_BATTERY to PowerUpType.KINETIC_BATTERY,
        DiscoveryType.MAGNETIC_SIPHON to PowerUpType.MAGNETIC_SIPHON,
        DiscoveryType.OVERDRIVE_MODULE to PowerUpType.OVERDRIVE_MODULE,
        DiscoveryType.ALTITUDE_BOOSTER to PowerUpType.ALTITUDE_BOOSTER,
        DiscoveryType.POWERUP_ARTIFACT to PowerUpType.ARTIFACT
    )
}
