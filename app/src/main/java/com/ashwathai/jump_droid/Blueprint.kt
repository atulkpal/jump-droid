package com.ashwathai.jump_droid

enum class BlueprintType(val displayName: String, val icon: String) {
    ENGINE_TRAIL_CYAN("Cyan Engine Trail", "💠"),
    HUD_THEME_AMBER("Amber HUD Theme", "📟"),
    ROCKET_SKIN_OBSIDIAN("Obsidian Rocket Skin", "🌑")
}

object BlueprintRegistry {
    val ALL_BLUEPRINTS = mapOf(
        BlueprintType.ENGINE_TRAIL_CYAN to UnlockRequirement(UnlockType.ARTIFACT, value = 15f),
        BlueprintType.HUD_THEME_AMBER to UnlockRequirement(UnlockType.SCORE, value = 8000f),
        BlueprintType.ROCKET_SKIN_OBSIDIAN to UnlockRequirement(UnlockType.MISSION, target = "hidden_void_walker")
    )
}
