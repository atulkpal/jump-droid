package com.ashwathai.jump_droid

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Manages threat spawning rules, zone-specific weighting, and boss milestone triggers.
 * Fully data-driven — no hardcoded threat IDs or positions.
 */
class EncounterDirector {
    var threatSpawnTimer = 0f
    private var bossRecurrenceTimer = 0f
    private val bossCooldowns = mutableMapOf<String, Float>()

    private val zoneConfigs = mapOf(
        AltitudeZone.EARTH to ZoneConfig(
            zone = AltitudeZone.EARTH,
            spawnWeights = emptyMap(),
            intensity = 1.0f
        ),
        AltitudeZone.CLOUD_LAYER to ZoneConfig(
            zone = AltitudeZone.CLOUD_LAYER,
            spawnWeights = mapOf("HAZ_LIGHTNING" to 1.5f, "HAZ_TURBULENCE" to 1.5f, "ENT_CLOUD_SKIMMER" to 1.0f),
            intensity = 1.5f,
            bossMilestone = "MINI_BOSS_COMMANDER"
        ),
        AltitudeZone.UPPER_ATMOSPHERE to ZoneConfig(
            zone = AltitudeZone.UPPER_ATMOSPHERE,
            spawnWeights = mapOf("DEFAULT_HAZARD" to 1.4f),
            intensity = 2.0f,
            bossMilestone = "MINI_BOSS_THERMAL_HIVE"
        ),
        AltitudeZone.ORBIT to ZoneConfig(
            zone = AltitudeZone.ORBIT,
            spawnWeights = mapOf("HAZ_RADIATION" to 1.5f, "HAZ_SOLAR_FLARE" to 1.5f, "HAZ_LIGHTNING" to 0.3f, "ENT_ORBITAL_SENTRY" to 1.0f, "ENT_VOID_HARVESTER" to 1.0f),
            intensity = 3.0f,
            bossMilestone = "BOSS_GATEKEEPER"
        ),
        AltitudeZone.THE_FOUNDRY to ZoneConfig(
            zone = AltitudeZone.THE_FOUNDRY,
            spawnWeights = mapOf("HAZ_DEBRIS" to 1.8f, "HAZ_LIGHTNING" to 1.0f),
            intensity = 3.5f,
            bossMilestone = "MINI_BOSS_FORGER"
        ),
        AltitudeZone.DEEP_SPACE to ZoneConfig(
            zone = AltitudeZone.DEEP_SPACE,
            spawnWeights = mapOf("HAZ_RADIATION" to 1.5f, "HAZ_SOLAR_FLARE" to 1.5f, "HAZ_EMP" to 1.5f, "ENT_CORRUPTED_HULL" to 1.0f, "ENT_STALKER" to 1.0f, "ENT_VOID_WHALE" to 1.0f, "ENT_VOID_HARVESTER" to 1.0f),
            intensity = 4.0f,
            bossMilestone = "BOSS_LEVIATHAN"
        ),
        AltitudeZone.CHRONO_RIFT to ZoneConfig(
            zone = AltitudeZone.CHRONO_RIFT,
            spawnWeights = mapOf("HAZ_VOID_ANOMALY" to 1.5f, "DEFAULT_HAZARD" to 1.8f, "ENT_VOID_WHALE" to 1.0f),
            intensity = 4.8f
        ),
        AltitudeZone.VOID to ZoneConfig(
            zone = AltitudeZone.VOID,
            spawnWeights = mapOf("DEFAULT_HAZARD" to 2.0f, "ENT_VOID_WRAITH" to 1.0f, "ENT_VOID_WHALE" to 1.0f, "ENT_PHASE_WRAITH" to 1.0f),
            intensity = 5.0f,
            bossMilestone = "BOSS_VOID_ENGINE"
        ),
        AltitudeZone.THE_BEYOND to ZoneConfig(
            zone = AltitudeZone.THE_BEYOND,
            spawnWeights = mapOf("DEFAULT_HAZARD" to 2.2f, "ENT_VOID_WRAITH" to 1.2f, "ENT_VOID_HARVESTER" to 1.0f),
            intensity = 6.0f,
            bossMilestone = "BOSS_ARCHITECT"
        ),
        AltitudeZone.STELLAR_GATE to ZoneConfig(
            zone = AltitudeZone.STELLAR_GATE,
            spawnWeights = mapOf("HAZ_EMP" to 1.8f, "ENT_ORBITAL_SENTRY" to 1.5f, "ENT_PHASE_WRAITH" to 1.0f),
            intensity = 7.0f
        ),
        AltitudeZone.ANCIENT_CONSTRUCT to ZoneConfig(
            zone = AltitudeZone.ANCIENT_CONSTRUCT,
            spawnWeights = mapOf("HAZ_GRAVITY" to 2.0f, "ENT_STALKER" to 1.5f, "ENT_GRAVITY_RAM" to 1.0f),
            intensity = 8.5f,
            bossMilestone = "BOSS_ENTROPY_CORE"
        ),
        AltitudeZone.SINGULARITY to ZoneConfig(
            zone = AltitudeZone.SINGULARITY,
            spawnWeights = mapOf("DEFAULT_HAZARD" to 3.0f, "ENT_VOID_WRAITH" to 2.0f, "ENT_PHASE_WRAITH" to 1.0f, "ENT_GRAVITY_RAM" to 1.0f),
            intensity = 10.0f
        )
    )

    /**
     * Computes spawn position and velocity from a threat definition's data-driven config.
     */
    private fun computeSpawnPosition(
        def: ThreatDefinition,
        screenWidth: Float,
        screenHeight: Float,
        cameraY: Float,
        score: Int = 0
    ): Triple<Float, Float, Float> {
        val (sx, sy) = when (def.spawnPosition) {
            SpawnPosition.ABOVE_CAMERA -> Pair(screenWidth / 2f, cameraY - 600f)
            SpawnPosition.BELOW_RANDOM_X -> Pair(
                Random.nextFloat() * screenWidth,
                cameraY + Random.nextFloat() * screenHeight
            )
            SpawnPosition.SIDE_ENTRY -> {
                val side = if (Random.nextBoolean()) 1f else -1f
                Pair(
                    if (side > 0) -100f else screenWidth + 100f,
                    cameraY + Random.nextFloat() * (screenHeight * 0.5f)
                )
            }
            SpawnPosition.ABOVE_SCREEN -> Pair(
                Random.nextFloat() * screenWidth,
                cameraY - 100f
            )
            SpawnPosition.RANDOM_SCREEN -> Pair(
                Random.nextFloat() * screenWidth,
                cameraY + Random.nextFloat() * screenHeight
            )
        }

        // Velocity: use definition as base, add random variance for dynamism
        val vxSign = if (def.spawnPosition == SpawnPosition.SIDE_ENTRY) {
            if (sx < 0) 1f else -1f
        } else 1f
        
        // EPIC 11: Speed Scaling for Eternal Mode
        val eternalFactor = if (score > 100000) (score - 100000) / 20000f else 0f
        val cappedFactor = min(eternalFactor, 3f) // Cap speed multiplier at 4x
        val speedMult = 1.0f + cappedFactor
        
        val finalVx = (def.spawnVx * speedMult) * vxSign + (Random.nextFloat() - 0.5f) * 20f
        val finalVy = (def.spawnVy * speedMult) + (Random.nextFloat() - 0.5f) * 10f

        return Triple(sx, sy, if (def.spawnPosition == SpawnPosition.SIDE_ENTRY) 0f else finalVy)
    }

    /**
     * Spawns a single threat at a data-driven position.
     */
    private fun spawnAtConfigPosition(
        def: ThreatDefinition,
        screenWidth: Float,
        screenHeight: Float,
        cameraY: Float,
        threatManager: ThreatManager,
        notificationManager: NotificationManager,
        score: Int = 0,
        difficultyMultiplier: Float = 1f,
        message: String? = null
    ) {
        val (sx, sy, svy) = computeSpawnPosition(def, screenWidth, screenHeight, cameraY, score)
        val vxSign = if (def.spawnPosition == SpawnPosition.SIDE_ENTRY) {
            if (sx < 0) 1f else -1f
        } else 1f
        val eternalFactor = if (score > 100000) (score - 100000) / 20000f else 0f
        val cappedFactor = min(eternalFactor, 3f)
        val speedMult = 1.0f + cappedFactor
        val finalVx = (def.spawnVx * speedMult) * vxSign + (Random.nextFloat() - 0.5f) * 20f
        threatManager.spawnThreat(def, sx, sy, vx = finalVx, vy = svy, difficultyMultiplier = difficultyMultiplier)
        message?.let { notificationManager.post(it, NotificationPriority.TACTICAL) }
    }

    /**
     * Updates the AI Director, rolling for spawns and checking milestones.
     */
    fun update(
        dt: Float,
        score: Int,
        currentZone: AltitudeZone,
        screenWidth: Float,
        screenHeight: Float,
        cameraY: Float,
        playerX: Float,
        playerY: Float,
        bossesSpawned: MutableSet<String>,
        threatManager: ThreatManager,
        notificationManager: NotificationManager,
        onDiscovery: (DiscoveryType) -> Unit,
        onVisualFeedback: (shake: Float, flash: Float) -> Unit,
        onBossSpawned: (ThreatDefinition) -> Unit = {}
    ) {
        // Tick boss cooldowns
        val iter = bossCooldowns.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            entry.setValue(entry.value - dt)
            if (entry.value <= 0f) iter.remove()
        }

        val config = zoneConfigs[currentZone] ?: return
        val intensityFactor = config.intensity
        val zoneMultiplier = 0.8f + intensityFactor * 0.4f

        // 1. Milestone Spawning (Boss Progression)
        val bossMilestones = listOf(
            "MINI_BOSS_COMMANDER" to 1500,
            "MINI_BOSS_THERMAL_HIVE" to 3000,
            "BOSS_GATEKEEPER" to 4500,
            "MINI_BOSS_FORGER" to 6500,
            "BOSS_LEVIATHAN" to 8500,
            "BOSS_STAR_EATER" to 11000,
            "MINI_BOSS_GRAVITY_ANCHOR" to 14000,
            "BOSS_VOID_ENGINE" to 17000,
            "BOSS_SIGNAL" to 21000,
            "BOSS_ARCHITECT" to 30000,
            "BOSS_ENTROPY_CORE" to 50000,
            "BOSS_SINGULARITY" to 100000
        )

        // Skip milestone spawning if a boss is already alive — one boss at a time
        if (threatManager.activeThreats.any { it.definition.type == ThreatType.BOSS || it.definition.type == ThreatType.MINI_BOSS }) {
            // Boss alive — skip milestone spawns
        } else {
            var spawnedThisFrame = false
            bossMilestones.forEach { (id, threshold) ->
                if (!spawnedThisFrame && score >= threshold && !bossesSpawned.contains(id) && !bossCooldowns.containsKey(id)) {
                    ThreatRegistry.getById(id)?.let { def ->
                        if (currentZone in def.spawnRules.allowedZones || def.spawnRules.allowedZones.isEmpty()) {
                            spawnedThisFrame = true
                            bossesSpawned.add(id)
                            bossCooldowns[id] = 60f
                            spawnAtConfigPosition(
                                def, screenWidth, screenHeight, cameraY,
                                threatManager, notificationManager, score,
                                difficultyMultiplier = zoneMultiplier,
                                message = "!!! ${def.name.uppercase()} ARRIVING !!!"
                            )
                            onBossSpawned(def)
                            onVisualFeedback(50f, 1.0f)

                            val discovery = when(id) {
                                "MINI_BOSS_COMMANDER" -> DiscoveryType.THREAT_SENTINEL
                                "MINI_BOSS_THERMAL_HIVE" -> DiscoveryType.THREAT_THERMAL_HIVE
                                "BOSS_GATEKEEPER" -> DiscoveryType.THREAT_GATEKEEPER
                                "MINI_BOSS_FORGER" -> DiscoveryType.THREAT_FORGER
                                "BOSS_LEVIATHAN" -> DiscoveryType.THREAT_LEVIATHAN
                                "MINI_BOSS_GRAVITY_ANCHOR" -> DiscoveryType.THREAT_GRAVITY_ANCHOR
                                "BOSS_STAR_EATER" -> DiscoveryType.THREAT_STAR_EATER
                                "BOSS_VOID_ENGINE" -> DiscoveryType.THREAT_VOID_ENGINE
                                "BOSS_SIGNAL" -> DiscoveryType.THREAT_SIGNAL
                                "BOSS_ARCHITECT" -> DiscoveryType.THREAT_ARCHITECT
                                "BOSS_ENTROPY_CORE" -> DiscoveryType.THREAT_ENTROPY_CORE
                                "BOSS_SINGULARITY" -> DiscoveryType.THREAT_SINGULARITY
                                else -> null
                            }
                            discovery?.let { onDiscovery(it) }
                        }
                    }
                }
            }
        }

        // 2. Threat Spawning Logic
        threatSpawnTimer += dt
        bossRecurrenceTimer += dt
        
        // EPIC 11: Eternal Mode Scaling (Capped)
        val eternalFactor = if (score > 100000) (score - 100000) / 15000f else 0f
        val cappedEternalFactor = min(eternalFactor, 30f) // Cap at ~550,000m
        val effectiveIntensity = intensityFactor + cappedEternalFactor
        
        val spawnInterval = max(0.25f, 3f / effectiveIntensity)
        if (threatSpawnTimer > spawnInterval) {
            threatSpawnTimer = 0f
            val activeThreats = threatManager.activeThreats.toList()
            val eligible = ThreatRegistry.getEligibleThreats(score, currentZone)

            // Slow down spawning if boss is present; scale up with zone intensity
            val bossPresent = activeThreats.any { it.definition.type == ThreatType.BOSS || it.definition.type == ThreatType.MINI_BOSS }
            val zoneSpawnMod = 0.5f + intensityFactor * 0.5f
            val spawnChanceMod = zoneSpawnMod * if (bossPresent) 0.1f else 1.0f

            // 2.1 Environmental Hazards — data-driven from config weights + ThreatDefinition spawn config
            val maxHazards = max(1, (intensityFactor * 0.5f).toInt())
            if (activeThreats.count { it.definition.type == ThreatType.HAZARD } < maxHazards) {
                val hazards = eligible.filter { it.type == ThreatType.HAZARD }
                    .filter { !(bossPresent && it.id == "HAZ_SOLAR_FLARE") }
                    .shuffled()
                for (hazard in hazards) {
                    val weight = config.spawnWeights[hazard.id] ?: config.spawnWeights["DEFAULT_HAZARD"] ?: 1.0f
                    if (Random.nextFloat() < hazard.spawnRules.spawnChance * spawnChanceMod * weight) {
                        spawnAtConfigPosition(
                            hazard, screenWidth, screenHeight, cameraY,
                            threatManager, notificationManager, score,
                            message = "${hazard.name.uppercase()} DETECTED"
                        )
                        break
                    }
                }
            }

            // 2.2 Generic Enemies — data-driven from Tier 1/2 eligibles + zone intensity caps
            val maxScoutDrones = (2f * intensityFactor).toInt().coerceAtLeast(1)
            val maxSwarmBots = max(1, intensityFactor.toInt())
            val lowerTierEnemies = eligible.filter { it.type == ThreatType.ENEMY && it.tier <= ThreatTier.TIER_2 }
            for (enemy in lowerTierEnemies) {
                val currentCount = activeThreats.count { it.definition.id == enemy.id }
                val maxCount = when {
                    enemy.tier == ThreatTier.TIER_1 -> maxScoutDrones
                    else -> maxSwarmBots
                }
                if (currentCount < maxCount && Random.nextFloat() < enemy.spawnRules.spawnChance * spawnChanceMod) {
                    val messages = mapOf(
                        "ENT_SCOUT_DRONE" to "SURVEYOR PROBE DETECTED",
                        "ENT_SWARM_BOTS" to "AEROSOL SWARM DETECTED",
                        "ENT_CLOUD_SKIMMER" to "SKY RAY SIGHTED",
                        "ENT_ORBITAL_SENTRY" to "DEFENSE NODE ACTIVE",
                        "ENT_CORRUPTED_HULL" to "DERELICT ECHO DETECTED",
                        "ENT_HEAT_BAT" to "THERMAL PREDATOR APPROACHING"
                    )
                    spawnAtConfigPosition(
                        enemy, screenWidth, screenHeight, cameraY,
                        threatManager, notificationManager, score,
                        message = messages[enemy.id]
                    )
                }
            }

            // 2.3 Zone-Specific Entities — data-driven from config spawnWeights
            val maxZoneNormals = max(1, (intensityFactor * 0.5f).toInt())
            val zoneEntities = eligible.filter { it.type == ThreatType.ENEMY && it.tier > ThreatTier.TIER_2 }
            for (entity in zoneEntities) {
                if (config.spawnWeights.containsKey(entity.id)) {
                    val currentCount = activeThreats.count { it.definition.id == entity.id }
                    if (currentCount < maxZoneNormals) {
                        if (Random.nextFloat() < entity.spawnRules.spawnChance * spawnChanceMod * (config.spawnWeights[entity.id] ?: 1.0f)) {
                            spawnAtConfigPosition(
                                entity, screenWidth, screenHeight, cameraY,
                                threatManager, notificationManager, score
                            )
                        }
                    }
                }
            }

            // 2.4 Mini-Boss spawning (Fallback) — data-driven from ThreatRegistry
            val miniBossFallbacks = eligible
                .filter { it.type == ThreatType.MINI_BOSS && it.spawnRules.spawnChance > 0f }
                .shuffled()
            val activeMiniBossIds = activeThreats.map { it.definition.id }
            if (activeThreats.none { it.definition.type == ThreatType.MINI_BOSS }) {
                for (bossDef in miniBossFallbacks) {
                    if (bossDef.id !in activeMiniBossIds && bossesSpawned.none { it.startsWith("MINI_BOSS") }) {
                        val zoneMod = if (currentZone in bossDef.spawnRules.allowedZones) 0.6f else 0.1f
                        if (Random.nextFloat() < bossDef.spawnRules.spawnChance * zoneMod) {
                            spawnAtConfigPosition(
                                bossDef, screenWidth, screenHeight, cameraY,
                                threatManager, notificationManager, score,
                                difficultyMultiplier = zoneMultiplier
                            )
                            onBossSpawned(bossDef)
                            onVisualFeedback(20f, 0f)
                            bossDef.discoveryType?.let { onDiscovery(it) }
                            break
                        }
                    }
                }
            }

            // 2.5 Boss Reinforcements — data-driven: any active boss with tier >= TIER_4 may spawn escorts
            val activeBoss = activeThreats.find { it.definition.type == ThreatType.MINI_BOSS || it.definition.type == ThreatType.BOSS }
            if (activeBoss != null && activeBoss.definition.tier >= ThreatTier.TIER_4 && activeBoss.phase >= 3) {
                if (Random.nextFloat() < 0.08f) {
                    val escort = eligible.filter { it.type == ThreatType.ENEMY && it.tier <= ThreatTier.TIER_2 }.randomOrNull()
                    escort?.let { def ->
                        val side = if (Random.nextBoolean()) 1f else -1f
                        val spawnX = if (side > 0) -100f else screenWidth + 100f
                        threatManager.spawnThreat(def, spawnX, activeBoss.y + 100f, vx = side * 200f)
                        // Removed: clutter — player is already engaged in combat
                    }
                }
                if (Random.nextFloat() < 0.15f) {
                    val escortHazards = eligible.filter { it.type == ThreatType.HAZARD && it.id != activeBoss.definition.id }
                    escortHazards.randomOrNull()?.let { def ->
                        threatManager.spawnThreat(def, activeBoss.x, activeBoss.y + 100f)
                    }
                }
            }

            // 2.6 Boss Recurrence — previously-defeated bosses reappear during dry spells
            if (bossRecurrenceTimer > 3f
                && activeThreats.none { it.definition.type == ThreatType.BOSS || it.definition.type == ThreatType.MINI_BOSS }
            ) {
                // Eligible: previously-defeated bosses that can spawn here + any mini-boss allowed in this zone
                val defeatedBosses = bossesSpawned.filter { id ->
                    val def = ThreatRegistry.getById(id)
                    def != null && currentZone in def.spawnRules.allowedZones
                }
                val zoneMiniBosses = ThreatRegistry.getEntries()
                    .filter { it.type == ThreatType.MINI_BOSS && currentZone in it.spawnRules.allowedZones }
                    .map { it.id }
                val eligible = (defeatedBosses + zoneMiniBosses).distinct()
                if (eligible.isNotEmpty()) {
                    val recChance = (0.05f * intensityFactor).coerceAtMost(0.25f)
                    if (Random.nextFloat() < recChance) {
                        val bossId = eligible.random()
                        ThreatRegistry.getById(bossId)?.let { def ->
                            spawnAtConfigPosition(
                                def, screenWidth, screenHeight, cameraY,
                                threatManager, notificationManager, score,
                                difficultyMultiplier = zoneMultiplier * 1.3f,
                                message = "RECURRENCE: ${def.name.uppercase()}"
                            )
                            onBossSpawned(def)
                            onVisualFeedback(30f, 0.5f)
                            def.discoveryType?.let { onDiscovery(it) }
                        }
                        bossRecurrenceTimer = 0f
                    }
                }
            }
        }
    }
}
