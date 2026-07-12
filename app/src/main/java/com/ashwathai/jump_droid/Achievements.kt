package com.ashwathai.jump_droid

val AchievementsList = listOf(
    Achievement("first_launch", "First Launch", "Reach 100 score.") { it.maxAltitude >= 100 },
    Achievement("sky_breaker", "Sky Breaker", "Reach Cloud Layer.") { it.maxAltitude >= 500 },
    Achievement("orbital_pilot", "Orbital Pilot", "Reach Orbit.") { it.maxAltitude >= 4000 },
    Achievement("deep_space", "Deep Space Explorer", "Reach Deep Space.") { it.maxAltitude >= 8000 },
    Achievement("combo_master", "Combo Master", "Achieve Combo x10.") { it.maxCombo >= 10 },
    Achievement("thermal_survivor", "Thermal Survivor", "Recover from overheating 25 times.") { it.overheatCount >= 25 },
    Achievement("depth_walker", "Depth Walker", "Reach 25,000m without using a Fuel refill.") { it.maxAltitude >= 25000 && it.fuelPickupsCollected == 0 },
    Achievement("resourceful", "Resourceful", "Collect 5 Power-Ups in a single run.") { it.powerUpsCollected >= 5 },
    Achievement("untouchable", "Untouchable", "Defeat a major boss without taking hull damage.") { it.bossesDefeated >= 1 && it.hazardHitsSurvived == 0 },
    Achievement("infinite_ascent", "Infinite Ascent", "Reach the Singularity (100,000m).") { it.maxAltitude >= 100000 },
)
