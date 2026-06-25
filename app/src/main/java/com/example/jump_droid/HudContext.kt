package com.example.jump_droid

data class HudContext(
    val gameTime: Long,
    val interferenceTimer: Float = 0f,
    val zone: AltitudeZone = AltitudeZone.EARTH,
    val hudPullFactor: Float = 0f // EPIC 11: Singularity mechanic
)
