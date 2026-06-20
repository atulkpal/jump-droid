# Jump Droid — Mission System

## Overview

The Mission System provides players with long-term goals, rewards exploration, and drives engagement through tiered challenges. Missions are progression-driven, with higher tiers unlocking as players complete earlier objectives. Some missions remain hidden until specific conditions are met, creating discovery moments.

**Status:** Active development on `feature/mission-system`. Not yet merged to `development`.

---

## Core Principles

- **Progression-driven** — Missions unlock based on player achievements, not arbitrary gates.
- **Tiered** — Each mission category has multiple tiers (e.g., Flight Time → 5min, 12min, 30min).
- **Rewarding** — Completing missions gives in-game cash, unlockables, and Codex entries.
- **Hidden** — Locked hidden missions show a SIGNAL LOST overlay until their prerequisites are met.
- **Persistent** — Progress and state are saved via DataStore across sessions.

---

## Branch Status

| Branch | Purpose | State |
|--------|---------|-------|
| `development` | Stable playable baseline (no mission system) | ✅ Build clean |
| `feature/mission-system` | Active mission system WIP | 🔧 Build clean, claim flow not yet stable |

---

## Mission Categories & Tiers

47 missions across 14 categories with 4 tiers each, plus 7 hidden missions.

| Category | Tier 1 (ROOKIE) | Tier 2 (EXPERIENCED) | Tier 3 (MASTER) | Tier 4 (GOD) |
| :--- | :--- | :--- | :--- | :--- |
| **Flight Time** | 5 min | 12 min | 30 min | 60 min |
| **Platform Stay** | 5 min on platforms | 15 min | 30 min | 60 min |
| **No Heat** | 5 min at 0 heat | 12 min | 25 min | 45 min |
| **Fuel Efficiency** | 10 fuel pickups | 30 | 75 | 150 |
| **Combo Streak** | 20x combo | 50x | 100x | 250x |
| **Boss Slayer** | 1 boss | 3 | 7 | 12 |
| **Discovery Hunter** | 5 codex entries | 15 | 30 | 50 |
| **Altitude Climber** | 500m | 1500m | 4000m | 10000m |
| **Momentum Master** | 50 momentum | 150 | 400 | 800 |
| **Hazard Survivor** | 10 hazard hits | 30 | 60 | 100 |
| **Perfect Run** | 2 min no damage | 5 min | 10 min | 20 min |
| **Collector** | 5 artifacts | 15 | 30 | 50 |
| **Boost Champion** | 10 dashes/run | 30 dashes/run | — | — |
| **Combo Pro** | 20x combo 30s maintain | 50x combo 30s maintain | — | — |

Hidden missions (7): heat_junkie, near_death, perfect_storm, void_walker, long_haul, artifact_hunter, momentum_legend

---

## Architecture

### Package Structure (new in `feature/mission-system`)

```
com.example.jump_droid.missions/
  Mission.kt              — Enums + data classes
  MissionRegistry.kt      — 47 mission definitions
  MissionManager.kt       — Coroutine runtime, state management
  MissionRepository.kt    — DataStore persistence
  ui/
    MissionScreen.kt      — 2-column glassmorphism card grid
```

### Data Flow

1. `MissionRepository` reads/writes DataStore (suspend functions)
2. `MissionManager` wraps repository with `mutableStateMapOf` for Compose reactivity
3. `GameScreen.kt` calls `missionManager.update(gameStats)` each frame during PLAYING
4. `MissionManager.update()` iterates all missions, checks objectives, transitions COMPLETED
5. `MissionScreen` reads state via `getMissionState()` + `getProgressPercentage()` directly from snapshot-state maps

### State Machine

```
LOCKED → AVAILABLE → IN_PROGRESS → COMPLETED → CLAIMED
  ↑          ↑                          ↑
  unlock     startRun()                 update()
  condition                             (progress >= target)
```

---

## UI: MissionScreen

- **2-column grid** with glassmorphism cards (`background: rgba(26,26,46,80%)`)
- Each card shows: emoji icon, mission name (14sp), tier pill badge, 4dp progress bar, percentage + state label
- **COMPLETED**: tier-colored border glow + full-width CLAIM pill button
- **CLAIMED**: dimmed tier tint + checkmark
- **IN_PROGRESS**: faint tint + active dot
- **LOCKED (hidden)**: red-tinted SIGNAL LOST overlay
- **LOCKED (regular)**: dimmed, lock icon, gray LOCKED pill

---

## Known Issues

1. **Claim flow not stable** — 100% progress sometimes doesn't transition to COMPLETED state. Multiple fixes applied (`mutableStateMapOf`, fallback raw-progress check in `claimRewards()`, `startRun()` skips progress reset for already-met targets), but issue persists.
2. **Hidden mission unlock feedback** — Newly unlocked hidden missions should show "SIGNAL RECEIVED" animation but currently just appear as normal cards.
3. **Cash wallet** — Cash rewards are displayed but not persisted. No wallet exists yet.
4. **MainMenuScreen badge** — MISSIONS button has badge (⏡/🔔) but it's computed from in-memory state only, not reactive.

---

## Next Steps

1. Debug and stabilize the COMPLETED transition — ensure `update()` reliably fires when `progress >= targetValue`
2. Add SIGNAL RECEIVED animation for newly unlocked hidden missions
3. Implement cash wallet persistence
4. Merge `feature/mission-system` → `development`
5. Explore full glassmorphism design system from `docs/gameplay/stitch_jump_droid_mission_screen.zip`