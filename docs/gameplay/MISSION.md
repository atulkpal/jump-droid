# Jump Droid - Mission System Documentation

## Overview

The Mission System provides players with long-term goals, rewards exploration, and drives engagement through tiered challenges. Missions are progression-driven, with higher tiers unlocking as players complete earlier objectives. Some missions remain hidden until specific conditions are met, creating discovery moments.

---

## Core Principles

- **Progression-driven** – Missions unlock based on player achievements, not arbitrary gates.
- **Tiered** – Each mission category has multiple tiers (e.g., Flight Time → 5min, 12min, 30min).
- **Rewarding** – Completing missions gives in-game cash, unlockables, and Codex entries.
- **Hidden** – Locked missions are invisible until their prerequisites are met (creates "discovery" moments).
- **Persistent** – Progress is saved even if the player quits mid-mission.

---

## Mission Categories & Tiers

| Category | Tier 1 (Unlock) | Tier 2 | Tier 3 | Tier 4 (Elite) |
| :--- | :--- | :--- | :--- | :--- |
| **Flight Time** | 5 min total | 12 min | 30 min | 60 min |
| **Platform Stay** | 5 min total on platforms | 15 min | 30 min | 60 min |
| **No Heat** | 5 min flight with 0 heat | 12 min | 25 min | 45 min |
| **Fuel Efficiency** | Collect 10 fuel pickups | 30 fuel | 75 fuel | 150 fuel |
| **Combo Streak** | 20x combo | 50x combo | 100x combo | 250x combo |
| **Boss Slayer** | Defeat 1 boss | 3 bosses | 7 bosses | 12 bosses |
| **Discovery Hunter** | Unlock 5 Codex entries | 15 entries | 30 entries | 50 entries |
| **Altitude Climber** | Reach 500m altitude | 1500m | 4000m | 10000m |
| **Momentum Master** | Build 50 momentum | 150 momentum | 400 momentum | 800 momentum |
| **Hazard Survivor** | Survive 10 hazard hits | 30 hazards | 60 hazards | 100 hazards |
| **Perfect Run** | No damage taken for 2 min | 5 min | 10 min | 20 min |
| **Collector** | Collect 5 artifacts | 15 artifacts | 30 artifacts | 50 artifacts |

---

## Hidden/Locked Missions (Discovery-Based)

These missions are **invisible** until the player meets a specific trigger:

| Mission Name | Unlock Condition | Objective | Reward |
| :--- | :--- | :--- | :--- |
| **The Long Haul** | Reach 3000m altitude | Complete a single run lasting 10+ min | Rare artifact + 500 cash |
| **Heat Junkie** | Overheat 3 times in one run | Reach 100% heat 5 times total | Special rocket skin + 300 cash |
| **Near-Death Experience** | Survive with <5% health | Complete a run with <10% health remaining | Codex entry + 400 cash |
| **Void Walker** | Reach the Void biome | Stay in Void biome for 2 continuous min | Void artifact + 600 cash |
| **Perfect Storm** | Complete 3 missions | Complete 5 missions without dying | Legendary rocket skin |
| **Artifact Hunter** | Collect 3 artifacts | Collect 5 different artifact types | Artifact bonus + 700 cash |
| **Momentum Legend** | Build 200 momentum in one run | Build 400 momentum in one run | Legendary powerup + 800 cash |

---

## Reward System

### Reward Types

| Reward Type | Example | How It's Used |
| :--- | :--- | :--- |
| **Cash** | 100–1000 | In-game currency for unlocks |
| **Rocket Skins** | "Scout Mk2" | Visual customisation |
| **Codex Entries** | "Lore of the Leviathan" | Expands story/universe |
| **Powerups** | "Fuel Boost" | Permanent upgrade |
| **Unlockables** | "New Biome" | Unlocks new content |

### Reward Tables per Tier

| Tier | Cash Reward | Bonus Reward |
| :--- | :--- | :--- |
| **Tier 1 (Easy)** | 100 cash | 1 Codex entry |
| **Tier 2 (Medium)** | 250 cash | 1 random powerup |
| **Tier 3 (Hard)** | 500 cash | 1 rare artifact |
| **Tier 4 (Elite)** | 1000 cash | 1 rocket skin or special unlockable |

---

## Data Model (Kotlin Implementation)

### Mission.kt

```kotlin
// Mission.kt
enum class MissionCategory {
    FLIGHT_TIME,
    PLATFORM_STAY,
    NO_HEAT,
    FUEL_EFFICIENCY,
    COMBO_STREAK,
    BOSS_SLAYER,
    DISCOVERY_HUNTER,
    ALTITUDE_CLIMBER,
    MOMENTUM_MASTER,
    HAZARD_SURVIVOR,
    PERFECT_RUN,
    COLLECTOR,
    BOOST_CHAMPION,
    COMBO_PRO
}

enum class MissionTier {
    TIER_1, TIER_2, TIER_3, TIER_4
}

enum class MissionState {
    LOCKED,      // Not yet visible
    AVAILABLE,   // Visible but not started
    IN_PROGRESS, // Started but not complete
    COMPLETED,   // Fully done
    CLAIMED      // Reward collected
}

data class Mission(
    val id: String,
    val category: MissionCategory,
    val tier: MissionTier,
    val name: String,
    val description: String,
    val objective: MissionObjective,
    val rewards: Rewards,
    val unlockCondition: UnlockCondition? = null  // null = always visible
)

data class MissionObjective(
    val type: ObjectiveType,
    val targetValue: Float,
    val currentValue: Float = 0f,
    val unit: String = "",
    val comboThreshold: Float? = null  // Only used for COMBO_MAINTAIN_TIME
)

enum class ObjectiveType {
    TOTAL_FLIGHT_TIME,     // seconds
    TOTAL_PLATFORM_TIME,   // seconds
    ZERO_HEAT_TIME,        // seconds
    PICKUPS_COLLECTED,     // count
    MAX_COMBO,             // count
    BOSSES_DEFEATED,       // count
    CODEX_UNLOCKED,        // count
    MAX_ALTITUDE,          // meters
    MAX_MOMENTUM,          // momentum points
    HAZARD_HITS_SURVIVED,  // count
    PERFECT_RUN_TIME,      // seconds
    ARTIFACTS_COLLECTED,   // count
    DASHES_PER_RUN,        // count
    COMBO_MAINTAIN_TIME,   // seconds (with comboThreshold)
    FUEL_PICKUPS_COLLECTED // count
}

data class Rewards(
    val cash: Int,
    val codexEntry: String? = null,
    val powerup: String? = null,
    val artifact: String? = null,
    val rocketSkin: String? = null,
    val unlockable: String? = null
)

data class UnlockCondition(
    val type: UnlockType,
    val value: Float,
    val missionId: String? = null  // for mission-chain unlocks
)

enum class UnlockType {
    REACH_ALTITUDE,
    DEFEAT_BOSS,
    COMPLETE_MISSION,
    UNLOCK_CODEX_ENTRY,
    REACH_BIOME,
    COLLECT_ARTIFACT
}