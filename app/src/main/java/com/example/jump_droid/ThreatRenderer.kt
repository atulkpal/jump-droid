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
        "HAZ_LIGHTNING" to LightningRenderer()
    )

    fun forId(id: String): ThreatRenderer? = renderers[id]
}
