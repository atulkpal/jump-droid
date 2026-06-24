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
        "HAZ_VOID_ANOMALY" to VoidAnomalyRenderer()
    )

    fun forId(id: String): ThreatRenderer? = renderers[id]
}
