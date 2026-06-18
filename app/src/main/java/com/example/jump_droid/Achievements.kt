package com.example.jump_droid

val AchievementsList = listOf(
    Achievement("first_launch", "First Launch", "Reach 100 score.") { s, _, _ -> s >= 100 },
    Achievement("sky_breaker", "Sky Breaker", "Reach Cloud Layer.") { s, _, _ -> s >= 500 },
    Achievement("orbital_pilot", "Orbital Pilot", "Reach Orbit.") { s, _, _ -> s >= 4000 },
    Achievement("deep_space", "Deep Space Explorer", "Reach Deep Space.") { s, _, _ -> s >= 8000 },
    Achievement("combo_master", "Combo Master", "Achieve Combo x10.") { _, c, _ -> c >= 10 },
    Achievement("thermal_survivor", "Thermal Survivor", "Recover from overheating 25 times.") { _, _, o -> o >= 25 },
)
