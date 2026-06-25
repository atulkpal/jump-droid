package com.example.jump_droid

import androidx.compose.ui.graphics.drawscope.DrawScope

interface ThreatRenderer {
    fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player
    )
}

object ThreatRendererRegistry {
    private val renderers: Map<String, ThreatRenderer> = mapOf(
        "HAZ_LIGHTNING" to LightningRenderer(),
        "HAZ_DEBRIS" to DebrisRenderer(),
        "HAZ_RADIATION" to RadiationRenderer(),
        "HAZ_SOLAR_FLARE" to SolarFlareRenderer(),
        "HAZ_TURBULENCE" to TurbulenceRenderer(),
        "HAZ_GRAVITY" to GravityRenderer(),
        "HAZ_EMP" to EmpRenderer(),
        "HAZ_GUST" to GustRenderer(),
        "HAZ_CROSSWIND" to CrosswindRenderer(),
        "HAZ_THERMAL" to ThermalRenderer(),
        "HAZ_STORM" to StormRenderer(),
        "HAZ_VOID_ANOMALY" to VoidAnomalyRenderer(),
        "ENT_SCOUT_DRONE" to ScoutDroneRenderer(),
        "ENT_SWARM_BOTS" to SwarmBotsRenderer(),
        "ENT_CLOUD_SKIMMER" to CloudSkimmerRenderer(),
        "ENT_ORBITAL_SENTRY" to OrbitalSentryRenderer(),
        "ENT_CORRUPTED_HULL" to CorruptedHullRenderer(),
        "ENT_STALKER" to StalkerRenderer(),
        "ENT_VOID_WHALE" to VoidWhaleRenderer(),
        "ENT_VOID_WRAITH" to VoidWraithRenderer(),
        "MINI_BOSS_COMMANDER" to CommanderRenderer(),
        "BOSS_GATEKEEPER" to GatekeeperRenderer(),
        "BOSS_STAR_EATER" to StarEaterRenderer(),
        "BOSS_VOID_ENGINE" to VoidEngineRenderer(),
        "BOSS_LEVIATHAN" to LeviathanRenderer(),
        "BOSS_SIGNAL" to SignalRenderer(),
        "HAZ_CRYO_MIST" to CryoMistRenderer(),
        "HAZ_MIRROR_SHARDS" to MirrorShardsRenderer(),
        "HAZ_GRAVITY_SHEAR" to GravityShearRenderer(),
        "ENT_HEAT_BAT" to HeatBatRenderer(),
        "ENT_VOID_HARVESTER" to VoidHarvesterRenderer(),
        "ENT_PHASE_WRAITH" to PhaseWraithRenderer(),
        "ENT_GRAVITY_RAM" to GravityRamRenderer(),
        "MINI_BOSS_THERMAL_HIVE" to ThermalHiveRenderer(),
        "MINI_BOSS_GRAVITY_ANCHOR" to GravityAnchorRenderer(),
        "MINI_BOSS_FORGER" to ForgerRenderer(),
        "BOSS_ARCHITECT" to ArchitectRenderer(),
        "BOSS_ENTROPY_CORE" to EntropyCoreRenderer()
    )

    fun forId(id: String): ThreatRenderer? = renderers[id]
}
