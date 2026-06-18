package com.example.jump_droid

import kotlin.random.Random

/**
 * Manages threat spawning rules, zone-specific weighting, and boss milestone triggers.
 * Extracted from GameScreen.kt as part of Sprint T4.
 */
class EncounterDirector {
    var threatSpawnTimer = 0f

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
        onVisualFeedback: (shake: Float, flash: Float) -> Unit
    ) {
        // 1. Milestone Spawning (Boss Progression)
        val bossMilestones = listOf(
            "MINI_BOSS_COMMANDER" to 1500,
            "BOSS_GATEKEEPER" to 4000,
            "BOSS_LEVIATHAN" to 7000,
            "BOSS_STAR_EATER" to 10000,
            "BOSS_VOID_ENGINE" to 15000,
            "BOSS_SIGNAL" to 18000
        )

        bossMilestones.forEach { (id, threshold) ->
            if (score >= threshold && !bossesSpawned.contains(id)) {
                bossesSpawned.add(id)
                ThreatRegistry.getById(id)?.let { def ->
                    threatManager.spawnThreat(def, screenWidth / 2f, cameraY - 600f)
                    notificationManager.post("!!! ${def.name.uppercase()} ARRIVING !!!")
                    onVisualFeedback(50f, 1.0f)

                    val discovery = when(id) {
                        "MINI_BOSS_COMMANDER" -> DiscoveryType.THREAT_SENTINEL
                        "BOSS_GATEKEEPER" -> DiscoveryType.THREAT_GATEKEEPER
                        "BOSS_LEVIATHAN" -> DiscoveryType.THREAT_LEVIATHAN
                        "BOSS_STAR_EATER" -> DiscoveryType.THREAT_STAR_EATER
                        "BOSS_VOID_ENGINE" -> DiscoveryType.THREAT_VOID_ENGINE
                        "BOSS_SIGNAL" -> DiscoveryType.THREAT_SIGNAL
                        else -> null
                    }
                    discovery?.let { onDiscovery(it) }
                }
            }
        }

        // 2. Threat Spawning Logic
        threatSpawnTimer += dt
        if (threatSpawnTimer > 3f) { // Check every 3 seconds
            threatSpawnTimer = 0f
            val activeThreats = threatManager.activeThreats.toList()
            val eligible = ThreatRegistry.getEligibleThreats(score, currentZone)

            // Slow down spawning if boss is present
            val bossPresent = activeThreats.any { it.definition.type == ThreatType.BOSS || it.definition.type == ThreatType.MINI_BOSS }
            val spawnChanceMod = if (bossPresent) 0.3f else 1.0f

            // 2.1 Environmental Hazards
            if (activeThreats.none { it.definition.type == ThreatType.HAZARD }) {
                val hazards = eligible.filter { it.type == ThreatType.HAZARD }.shuffled()
                for (hazard in hazards) {
                    val weight = when (currentZone) {
                        AltitudeZone.CLOUD_LAYER -> when (hazard.id) {
                            "HAZ_LIGHTNING", "HAZ_TURBULENCE" -> 1.5f
                            else -> 1.0f
                        }
                        AltitudeZone.UPPER_ATMOSPHERE -> 1.4f
                        AltitudeZone.ORBIT -> when (hazard.id) {
                            "HAZ_RADIATION", "HAZ_SOLAR_FLARE" -> 1.5f
                            "HAZ_LIGHTNING" -> 0.3f
                            else -> 1.0f
                        }
                        AltitudeZone.DEEP_SPACE -> when (hazard.id) {
                            "HAZ_RADIATION", "HAZ_SOLAR_FLARE", "HAZ_EMP" -> 1.5f
                            else -> 1.0f
                        }
                        AltitudeZone.VOID -> 2.0f
                        else -> 1.0f
                    }

                    if (Random.nextFloat() < hazard.spawnRules.spawnChance * spawnChanceMod * weight) {
                        val spawnX = Random.nextFloat() * screenWidth
                        val spawnY = when (hazard.id) {
                            "HAZ_SOLAR_FLARE" -> cameraY - 400f
                            "HAZ_DEBRIS" -> cameraY - 200f
                            else -> cameraY + Random.nextFloat() * screenHeight
                        }
                        
                        val vx = if (hazard.id == "HAZ_DEBRIS") (Random.nextFloat() - 0.5f) * 100f else 0f
                        val vy = if (hazard.id == "HAZ_DEBRIS") 100f + Random.nextFloat() * 200f else 0f
                        
                        threatManager.spawnThreat(hazard, spawnX, spawnY, vx, vy)
                        notificationManager.post("${hazard.name.uppercase()} DETECTED")
                        break 
                    }
                }
            }

            // 2.2 Generic Enemies
            if (activeThreats.count { it.definition.id == "ENT_SCOUT_DRONE" } < 2) {
                eligible.find { it.id == "ENT_SCOUT_DRONE" }?.let { probeDef ->
                    if (Random.nextFloat() < probeDef.spawnRules.spawnChance * spawnChanceMod) {
                        val spawnX = if (Random.nextBoolean()) -50f else screenWidth + 50f
                        val vx = if (spawnX < 0) 150f else -150f
                        threatManager.spawnThreat(probeDef, spawnX, cameraY + Random.nextFloat() * (screenHeight * 0.5f), vx = vx)
                        notificationManager.post("SURVEYOR PROBE DETECTED")
                    }
                }
            }
            if (activeThreats.none { it.definition.id == "ENT_SWARM_BOTS" }) {
                eligible.find { it.id == "ENT_SWARM_BOTS" }?.let { swarmDef ->
                    if (Random.nextFloat() < swarmDef.spawnRules.spawnChance * spawnChanceMod) {
                        threatManager.spawnThreat(swarmDef, Random.nextFloat() * screenWidth, cameraY - 100f)
                        notificationManager.post("AEROSOL SWARM DETECTED")
                    }
                }
            }

            // 2.3 Zone-Specific Entities
            if (currentZone == AltitudeZone.CLOUD_LAYER && activeThreats.none { it.definition.id == "ENT_CLOUD_SKIMMER" }) {
                eligible.find { it.id == "ENT_CLOUD_SKIMMER" }?.let { rayDef ->
                    if (Random.nextFloat() < rayDef.spawnRules.spawnChance * spawnChanceMod) {
                        val dir = if (Random.nextBoolean()) 1f else -1f
                        val spawnX = if (dir > 0) -200f else screenWidth + 200f
                        threatManager.spawnThreat(rayDef, spawnX, cameraY + Random.nextFloat() * screenHeight, vx = dir * 50f)
                    }
                }
            }
            if (currentZone == AltitudeZone.ORBIT && activeThreats.none { it.definition.id == "ENT_ORBITAL_SENTRY" }) {
                eligible.find { it.id == "ENT_ORBITAL_SENTRY" }?.let { sentryDef ->
                    if (Random.nextFloat() < sentryDef.spawnRules.spawnChance * spawnChanceMod) {
                        threatManager.spawnThreat(sentryDef, Random.nextFloat() * screenWidth, cameraY + 200f)
                    }
                }
            }
            if (currentZone == AltitudeZone.DEEP_SPACE && activeThreats.none { it.definition.id == "ENT_CORRUPTED_HULL" }) {
                eligible.find { it.id == "ENT_CORRUPTED_HULL" }?.let { echoDef ->
                    if (Random.nextFloat() < echoDef.spawnRules.spawnChance * spawnChanceMod) {
                        threatManager.spawnThreat(echoDef, Random.nextFloat() * screenWidth, cameraY - 100f, vx = (Random.nextFloat() - 0.5f) * 30f, vy = 20f + Random.nextFloat() * 30f)
                    }
                }
            }

            // 2.4 Mini-Boss spawning (Fallback)
            if (activeThreats.none { it.definition.id == "MINI_BOSS_COMMANDER" }) {
                eligible.find { it.id == "MINI_BOSS_COMMANDER" }?.let { bossDef ->
                    if (Random.nextFloat() < bossDef.spawnRules.spawnChance) {
                        threatManager.spawnThreat(bossDef, screenWidth / 2f, cameraY - 600f)
                        notificationManager.post("COMMAND CRUISER INBOUND")
                        onVisualFeedback(20f, 0f)
                        onDiscovery(DiscoveryType.THREAT_SENTINEL)
                    }
                }
            }

            // 2.5 Boss Reinforcements & Hazards
            activeThreats.find { it.definition.id == "MINI_BOSS_COMMANDER" }?.let { boss ->
                if (boss.phase == 3 || boss.phase == 4) {
                    if (Random.nextFloat() < 0.08f) {
                        val def = ThreatRegistry.getById("ENT_SCOUT_DRONE")
                        def?.let {
                            val side = if (Random.nextBoolean()) 1f else -1f
                            val spawnX = if (side > 0) -100f else screenWidth + 100f
                            threatManager.spawnThreat(it, spawnX, boss.y + 100f, vx = side * 200f)
                            notificationManager.post("REINFORCEMENTS INBOUND")
                        }
                    }
                    if (Random.nextFloat() < 0.15f) {
                        val id = "HAZ_TURBULENCE"
                        ThreatRegistry.getById(id)?.let { threatManager.spawnThreat(it, boss.x, boss.y + 100f) }
                    }
                }
            }
        }
    }
}
