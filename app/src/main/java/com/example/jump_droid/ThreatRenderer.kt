package com.example.jump_droid

import androidx.compose.ui.graphics.drawscope.DrawScope

interface ThreatRenderer {
    fun DrawScope.render(threat: ActiveThreat, cameraY: Float, alpha: Float, player: Player)
}
