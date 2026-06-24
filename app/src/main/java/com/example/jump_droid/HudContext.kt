package com.example.jump_droid

data class HudContext(
    val gameTime: Long,
    val interferenceTimer: Float = 0f,
    val zone: AltitudeZone = AltitudeZone.EARTH
)
