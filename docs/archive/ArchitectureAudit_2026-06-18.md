# Jump Droid — Architecture Audit

**Date:** 2026-06-18
**Mode:** READ-ONLY Audit
**Status:** No code modified, no patches generated

---

## SECTION 1 — CODEBASE OVERVIEW

### Project Structure

```
Jump_droid/
├── app/
│   ├── build.gradle.kts             (module config)
│   └── src/main/java/com/example/jump_droid/
│       ├── GameScreen.kt            ★ 4,360 lines (55%)
│       ├── ActiveThreat.kt          ★ 562 lines
│       ├── ZoneBackgroundRenderer.kt   341 lines
│       ├── RocketRenderer.kt           341 lines
│       ├── PlatformRenderer.kt         307 lines
│       ├── ThreatRegistry.kt           243 lines
│       ├── AmbientSystem.kt            199 lines
│       ├── Models.kt                   182 lines
│       ├── ProgressionManager.kt       122 lines
│       ├── ParallaxSystem.kt           117 lines
│       ├── ComboManager.kt             115 lines
│       ├── MissionManager.kt           109 lines
│       ├── DiscoveryManager.kt          68 lines
│       ├── MissionRegistry.kt           50 lines
│       ├── Mission.kt                   50 lines
│       ├── ThreatManager.kt             46 lines
│       ├── AltitudeManager.kt           40 lines
│       ├── Platform.kt                  38 lines
│       ├── Constants.kt                 35 lines
│       ├── Boss.kt                      30 lines (legacy)
│       ├── AltitudeZone.kt              24 lines
│       ├── ThreatDefinition.kt          22 lines
│       ├── ThreatSpawnRules.kt          11 lines
│       ├── ThreatTier.kt                11 lines
│       ├── ThreatType.kt                10 lines
│       ├── ThreatState.kt               10 lines
│       ├── MissionReward.kt             14 lines
│       ├── MissionType.kt               12 lines
│       ├── MainActivity.kt              14 lines
│       ├── DevConfig.kt                  4 lines
│       └── ui/theme/                    4 files, ~232 lines total
├── build.gradle.kts                  (root config)
└── ARCHITECTURE.md, README.md, VISION.md
```

**Total: 36 Kotlin source files, ~7,437 lines**

### Major Packages

There are **no sub-packages**. All game code resides in `com.example.jump_droid`. The `ui/theme/` sub-package is the only organizational boundary.

### Major Systems (by file)

| System | File(s) | Lines | Role |
|--------|---------|-------|------|
| **Engine** | `GameScreen.kt` | 4,360 | Game loop, physics, collision, input, all state, HUD, menus, dispatch |
| **Threat AI** | `ActiveThreat.kt` | 562 | Per-threat AI behaviors: hazards, enemies, 6 bosses |
| **Background** | `ZoneBackgroundRenderer.kt` | 341 | Per-zone parallax gradient backgrounds |
| **Rocket Render** | `RocketRenderer.kt` | 341 | Body, thruster, shield, destruction, battle scars |
| **Platform Render** | `PlatformRenderer.kt` | 307 | Canvas drawing for 10 platform types |
| **Threat Defs** | `ThreatRegistry.kt` | 243 | 31 threat template definitions |
| **Ambient** | `AmbientSystem.kt` | 199 | Birds, clouds, satellites, anomalies |
| **Data Models** | `Models.kt` | 182 | Player, PowerUp, Particle, enums, discovery types |
| **Progression** | `ProgressionManager.kt` | 122 | Artifact records, ascension rank, perm stats |
| **Parallax** | `ParallaxSystem.kt` | 117 | Generic parallax layer framework |
| **Combo** | `ComboManager.kt` | 115 | 5-tier combo, rewards, survival drops |
| **Missions** | `MissionManager.kt` | 109 | Runtime mission tracking, 3-track cycling |
| **Discovery** | `DiscoveryManager.kt` | 68 | Codex tracking + SharedPreferences |

### Risk Level per Major File

| File | Risk Level | Reason |
|------|-----------|--------|
| `GameScreen.kt` | **Critical** | 4,360 lines, single composable, mixed responsibilities, no tests |
| `ActiveThreat.kt` | **Medium** | 562 lines, moderate complexity but self-contained |
| `RocketRenderer.kt` | **Low** | 341 lines, pure rendering, no state mutation |
| `PlatformRenderer.kt` | **Low** | 307 lines, pure rendering, well-encapsulated |
| `ZoneBackgroundRenderer.kt` | **Low** | 341 lines, pure rendering |
| `AmbientSystem.kt` | **Low** | 199 lines, well-encapsulated |
| `ComboManager.kt` | **Low** | 115 lines, self-contained logic |
| `MissionManager.kt` | **Low** | 109 lines, self-contained |
| `ThreatRegistry.kt` | **Low** | 243 lines, data-only |

---

## SECTION 2 — GAMESCREEN RESPONSIBILITY ANALYSIS

Estimated percentage ownership of responsibilities within `GameScreen.kt` (4,360 lines):

| Responsibility | Est. Lines | % | Notes |
|---------------|-----------|---|-------|
| **Physics & Collision** | ~900 | 20.6% | Gravity, thrust, AABB collision, substep loop, friction, damping |
| **Threat Interaction Logic** | ~550 | 12.6% | Per-threat proximity effects, damage, boss weak points, boss mechanics |
| **HUD Rendering** | ~500 | 11.5% | Altitude display, fuel/heat/shield/integrity gauges, combo bar, notification layer |
| **Screen State Machine** | ~480 | 11.0% | TITLE, MAIN_MENU, HANGAR, ARCHIVE, SETTINGS, ABOUT, LEADERBOARD, PAUSED, TUTORIAL, UNLOCK, GAMEOVER |
| **Game Loop & Timers** | ~400 | 9.2% | withFrameNanos, dt calculation, frame state reset, timer tracking |
| **Combo & Reward Handling** | ~300 | 6.9% | Flying rewards, reward collection, burst effects, combo celebration |
| **Threat Spawning Logic** | ~250 | 5.7% | Spawn timers, hazard/enemy/boss eligibility, zone weighting, boss milestones |
| **Platform Management** | ~200 | 4.6% | Generation, recycling, cleanup, moving platform logic |
| **Mission System Integration** | ~200 | 4.6% | Progress updates, ceremony state machine, particle effects, synergy bonuses |
| **PowerUp Management** | ~180 | 4.1% | Spawn timer, magnetic pull, collection effects, reward dispatch |
| **Input Handling** | ~50 | 1.1% | pointerInput, gesture detection, thrust control |
| **Dev Menu** | ~80 | 1.8% | Zone jump, threat spawn, infinite fuel, no heat |
| **Achievement Checks** | ~50 | 1.1% | Score milestones, unlock conditions, SharedPreferences write |
| **Notification System** | ~70 | 1.6% | Queue management, alpha fade, threat names, warnings |
| **State Variables** | ~150 | 3.4% | All `remember` declarations (~60+ vars) |

### Key Finding

GameScreen has **at least 15 distinct responsibilities**. The `when(gameState)` block alone handles **10 unique screen modes**. The physics/collision and threat interaction logic together account for ~33% of the file, but even isolated physics extraction is difficult because it is interleaved with threat effects, powerup management, and boss mechanics within the same substep loop.

---

## SECTION 3 — RESPONSIBILITY MAP

```
GameScreen (@Composable | 4,360 lines)
│
├── State Variables (~60+ remember declarations)
│   ├── Player state (mutable state)
│   ├── Game state (GameState enum)
│   ├── Collections (platforms, powerUps, particles, etc.)
│   ├── Timers & counters (~20 float/long states)
│   ├── Visual state (screenShake, impactFlash, glitch)
│   └── Manager instances
│
├── Input Handling (pointerInput -> isThrusting, thrustTarget)
│
├── Game Loop (LaunchedEffect + withFrameNanos)
│   ├── Manager Updates (discovery, threat, ambient, combo)
│   ├── Combo System Integration
│   │   ├── Flying reward animation
│   │   ├── Survival reward milestones
│   │   └── New combo high celebration
│   │
│   ├── Timer Updates (screenShake, impactFlash, invulnerability, etc.)
│   │
│   ├── Sub-Stepped Physics Loop (4 substeps)
│   │   ├── Platform Updates (moving, breaking, jamming)
│   │   ├── Threat Interaction Logic
│   │   │   ├── Per-hazard collision (lightning, debris, radiation, etc.)
│   │   │   ├── Per-enemy collision (drone, swarm, sentry, etc.)
│   │   │   ├── Per-boss collision (6 bosses x weak points x mechanics)
│   │   │   └── Boss encounter rewards
│   │   │
│   │   ├── Magnetic Platform Gravity
│   │   ├── Player Physics (thrust, gravity, steering, fuel, heat)
│   │   ├── AABB Collision Resolution
│   │   │   ├── Platform landing (10 types x handlers)
│   │   │   └── Ground collision
│   │   └── Bounds Enforcement
│   │
│   ├── Post-Physics Updates
│   │   ├── Air friction
│   │   ├── Screen bounds
│   │   ├── Death trigger (fall off screen)
│   │   ├── Broken platform cleanup + bursts
│   │   ├── PowerUp management (spawn, magnetic, collection, effects)
│   │   ├── Platform generation + recycling
│   │   ├── Mission System Updates
│   │   │   ├── Exploration, platforming, survival, discovery progress
│   │   │   ├── Ceremony state machine (4 stages x particles x stage transitions)
│   │   │   └── Mission synergy bonuses
│   │   ├── Survival System Updates (shield regen, hull failure, destruction)
│   │   ├── Emergency Warnings (shield/hull critical)
│   │   ├── Fuel recharge, score calculation, zone update
│   │   └── Achievement checks + rocket unlocks
│   │
│   └── Notification Queue Processing
│
├── Canvas Rendering
│   ├── Screen Shake Translation
│   ├── Zone Background + Ambience
│   │   ├── Reality distortion overlay
│   │   └── Speed lines
│   ├── Threat Rendering (12+ threat types x complex visuals)
│   ├── Platform Rendering (delegated to PlatformRenderer)
│   ├── PowerUp Rendering
│   ├── Flying Reward Rendering
│   ├── Rocket Rendering (delegated to RocketRenderer)
│   ├── Particle Rendering (with star special case)
│   ├── Landing Effect Rendering
│   └── Impact Flash
│
├── HUD Overlays (Compose)
│   ├── Top: Altitude display
│   ├── Left: Fuel gauge + Heat gauge
│   ├── Right: Shield gauge + Integrity gauge
│   ├── Top-Center: Mission cards (3 active)
│   ├── Combo HUD bar
│   ├── Notification layer
│   ├── Floating texts
│   └── Zone discovery card
│
├── Screen State Machine (~10 screens)
│   ├── TITLE screen
│   ├── MAIN_MENU (6 buttons)
│   ├── HANGAR (rocket selection + progression summary)
│   ├── ARCHIVE (8 category codex browser)
│   ├── SETTINGS
│   ├── ABOUT
│   ├── LEADERBOARD
│   ├── PAUSED (resume, restart, main menu, dev menu)
│   ├── TUTORIAL (discovery card overlay)
│   ├── HELP (legend screen)
│   ├── UNLOCK (new rocket announcement)
│   └── GAMEOVER (score, continue, new expedition)
│
├── Helper Functions
│   ├── spawnBurst (particle burst)
│   ├── checkDiscovery (codex + mission hooks)
│   ├── handleRewardCollection (combo reward dispatch)
│   ├── handleLanding (10 platform types + combo logic)
│   ├── checkUnlock (rocket + achievement + lore discovery)
│   ├── saveHighScore
│   ├── continueRun (respawn logic)
│   ├── generatePlatform (difficulty-scaled generation)
│   ├── jumpToZone (dev cheat)
│   ├── spawnDevThreat (dev cheat)
│   ├── applyDamage (shield -> integrity -> destruction)
│   ├── unlockAll (dev cheat)
│   └── restartGame (full state reset)
│
└── Top-Level Composables (file-level)
    ├── AchievementsList (list of 6 achievements)
    ├── MissionType.toIcon()
    ├── PowerupBadge (@Composable)
    └── CodexCard (@Composable)
```

---

## SECTION 4 — SAFE EXTRACTION CANDIDATES

| Candidate | Difficulty | Risk | Benefit | Recommended Order |
|-----------|-----------|------|---------|-------------------|
| **Screen State Machine** | Low | Low | High | 1 |
| **HUD Composable widgets** | Low | Low | High | 2 |
| **Notification System** | Low | Low | Medium | 3 |
| **Platform Generation** | Low | Low | Medium | 4 |
| **Achievement System** | Low | Low | Low | 5 |
| **Timers + Visual State** | Medium | Low | Medium | 6 |
| **PowerUp Management** | Medium | Medium | Medium | 7 |
| **Threat Collision Logic** | High | High | Very High | 8 |
| **Input Handling** | Medium | Medium | Medium | 9 |
| **Rendering (Canvas block)** | Very High | High | Very High | 10 |

### Detailed Assessment

**1. Screen State Machine** (easiest win)
- All 10 `GameState` screens are independent `when` branches
- Each screen already reads/writes `gameState` only
- Extract to `@Composable fun GameTitle()`, `GameMainMenu()`, `HangarScreen()`, etc.
- No shared mutable state concerns beyond `gameState` itself

**2. HUD Composable widgets**
- Fuel gauge, heat gauge, shield gauge, integrity gauge are pure visual
- They read `player.*` values but don't mutate them
- Mission cards, combo HUD, notification layer similarly read-only
- Can extract to stateless composables that take values as params

**3. Notification System**
- Self-contained queue (`notificationQueue`, `notificationTimer`, `activeNotification`)
- Only writes to UI state, no gameplay impact
- Straightforward class extraction

**4. Platform Generation**
- `generatePlatform()` function is pure calculation (score -> difficulty -> type)
- No side effects, no state mutation
- Easy to extract to a utility class

**5. Achievement System**
- `AchievementsList` + `checkUnlock` achievement loop
- Pure logic with SharedPreferences writes
- Can be extracted to a manager class

---

## SECTION 5 — HIGH-RISK AREAS (DO NOT REFACTOR YET)

### 1. Sub-Stepped Physics Loop (GameScreen.kt ~lines 1053-1614)

**Risk: Critical.** This is the heart of the game. It interleaves:
- Platform state updates (moving, breaking, jamming)
- Per-threat proximity collisions (12+ threat types with unique logic)
- Magnetic platform gravity
- Player physics (thrust, gravity, steering, fuel, heat)
- AABB collision resolution (10 platform types)
- Ground collision

The 4 substep loop is performance-sensitive and **tightly coupled to frame timing**. Any extraction here risks regression of collision reliability, platform landing detection, and threat interaction timing.

### 2. Threat Collision Logic (GameScreen.kt ~lines 1082-1447)

**Risk: High.** Inline within the physics substep loop. Each threat type has:
- Unique proximity detection (distance squared checks)
- Unique damage formula (shield vs integrity)
- Unique status effects (fuel drain, heat, combo freeze, tracking)
- Unique visual feedback (spawnBurst, screenShake, notifications)
- Boss weak point system (6 bosses with different geometry)
- Boss phase-specific mechanics (gravity pulse, jam cooldown, reinforcement spawn)

This code reads **and writes** `player.*` fields, `platforms.*`, `powerUps.*`, and `threatManager.*` simultaneously. Attempting to extract this would require disentangling a complex read-write dependency graph.

### 3. Boss Encounter State (GameScreen.kt ~lines 839-870, ~1420-1444)

The boss spawn milestone checks, encounter lifecycle (arrival -> phase tracking -> retreat -> reward), and defeat rewards are scattered across the game loop. Boss state (`bossesSpawned`, `phase`, `activeWeakPoints`) is mutated from both the game loop and `ActiveThreat.update()` via `threatManager`. This dual-source mutation pattern makes safe extraction very difficult.

### 4. Canvas Rendering Block (GameScreen.kt ~lines 2040-3022)

While individual renderers have been extracted (RocketRenderer, PlatformRenderer), the Canvas block still contains:
- Orchestration logic (what to render in which state)
- Threat rendering (12+ types with ~800 lines of inline DrawScope)
- Particle, powerup, flying reward, landing effect rendering
- Screen shake translation
- Speed lines

The threat rendering alone is ~800 lines of spaghetti `when` blocks with game-state-dependent animations. Extracting this would require defining a clear rendering pipeline interface.

---

## SECTION 6 — TECHNICAL DEBT ANALYSIS

### 1. Duplicate HUD Declaration (GameScreen.kt ~lines 3452-3506)

The altitude display block is **declared twice** in succession with identical code. This appears to be an accidental duplicate from a refactoring attempt.

### 2. Overloaded File

`GameScreen.kt` at 4,360 lines:
- No single function is "too long" in isolation (`restartGame` ~60 lines)
- But the file as a whole violates single-responsibility by containing:
  - A game engine
  - A rendering pipeline
  - A UI framework for 10 screens
  - A physics engine
  - A particle system
  - An input system
  - Achievement logic
  - Persistence logic
  - A notification system

### 3. Architectural Bottlenecks

- **`Player` is a God object** (Models.kt:141-182): Contains physics state, survival state, visual state, combo state, powerup state, rocket state. It is read/written from almost every system.
- **`gameState` single choke point**: Every screen transition flows through one variable. Any bug in state management causes the entire UI to flip to the wrong screen.
- **No separation of concerns between game loop phases**: Update logic, collision detection, and rendering triggers are interleaved in one coroutine scope.

### 4. Future Maintenance Risks

- Adding a 7th boss requires changes in **5 places**: ThreatRegistry, ActiveThreat (AI + rendering), GameScreen (collision + milestone spawning)
- Adding a new HUD element requires reading `Player` from within the composable tree, which is already accessed by 20+ places
- No testing infrastructure for game logic -- all mission, combo, and threat behavior is untested
- `SharedPreferences` keys are strings scattered across files (no constants, no repository pattern)
- Emoji usage in HUD may not render consistently across all devices

### 5. Inconsistencies

- `Boss.kt` (30 lines) is marked as legacy but still exists
- Some rendering uses `android.graphics.*` (native canvas) while most uses `DrawScope` -- mixed approach in `PlatformRenderer.kt:226-237`
- `codexNotification` and `activeDiscovery` serve similar purposes but are separate variables
- `player.maxHeat` is declared (Models.kt:153) but `MAX_HEAT` constant is used throughout instead

---

## SECTION 7 -- EPIC 6 READINESS

### Assessment: Proceed with caution.

**What EPIC 6 likely needs:**
- Bosses: Already have 6 bosses (+1 legacy mini-boss). Framework exists in `ActiveThreat`.
- Projectiles: **Not implemented anywhere.** Would require a new projectile system (spawn, lifecycle, collision, rendering).
- Enemy AI: Works well for existing 6 enemy types. Adding new types is data-driven via `ThreatRegistry`.
- Additional hazards: Data-driven. Register in `ThreatRegistry`, add behavior in `ActiveThreat`, add rendering in GameScreen's Canvas block.
- More progression systems: `ProgressionManager` is small (122 lines). Adding significant new progression would require more abstraction.

### Likely Pressure Points

| Pressure Point | Why |
|---------------|-----|
| **Projectile system** | Zero existing infrastructure. Would need entity management, collision, rendering, lifetime -- all currently in GameScreen |
| **Threat rendering** | Already ~800 lines inside Canvas `when` block. Adding more threats makes this worse |
| **GameScreen line count** | 4,360 lines now. Adding EPIC 6 features will push past 5,000 |
| **Boss collision logic** | Currently 300+ lines inline for 6 bosses. New bosses = more inline code |
| **Player object** | Already holds 20+ fields. Adding more survival/progression stats compounds the god object problem |

### Recommendation for EPIC 6

- **Do NOT block EPIC 6 on refactoring** -- the risk of regressions is higher than the benefit
- **Budget time to extract threat rendering** before adding new threat types (reduces friction)
- **Build a projectile system** as a separate file from day one -- don't put it in GameScreen
- **Add a `GameState.PLAYING` sub-state machine** before adding more gameplay modes (avoid extending the when block further)

---

## SECTION 8 -- REFACTOR STRATEGY (If Refactoring Becomes Necessary)

### Recommended Order (incremental, no rewrites)

| Step | Extraction | Effort | Risk | Cumulative Reduction |
|------|-----------|--------|------|---------------------|
| **Step 1** | Screen Composables (Title -> GameOver) | 1-2 days | Low | ~450 lines |
| **Step 2** | HUD Meter Composables (fuel, heat, shield, integrity) | 0.5 days | Low | ~150 lines |
| **Step 3** | Notification System -> class | 0.5 days | Low | ~70 lines |
| **Step 4** | Platform Generator -> utility class | 0.5 days | Low | ~40 lines |
| **Step 5** | Achievement System -> manager class | 0.5 days | Low | ~60 lines |
| **Step 6** | PowerUp Manager -> class | 1 day | Medium | ~180 lines |
| **Step 7** | Timers + Visual State (screenShake, impactFlash, etc.) -> state holder class | 1 day | Low | ~50 lines |
| **Step 8** | Threat Collision -> separate file/functions (but keep in substep loop) | 2-3 days | High | ~500 lines |
| **Step 9** | Physics -> simulation class (sub-step loop, gravity, AABB) | 3-5 days | Very High | ~800 lines |
| **Step 10** | Canvas Rendering Pipeline | 3-5 days | Very High | ~1000 lines |

### Key Principles

- **Never refactor what EPIC 6 is actively touching** -- refactor before or after, never during
- **Each step must leave the game compilable and playable** -- no multi-step breakages
- **Prefer extraction over abstraction** -- move code to new files before trying to redesign it
- **Add parameter objects early** -- when extracting, create data classes for grouped parameters to avoid 8-parameter functions
- **Write characterization tests** before refactoring the physics loop -- capture current behavior by recording frame-by-frame state

---

## SECTION 9 -- EXECUTIVE SUMMARY

### 1. Is GameScreen actually a problem today?

**Yes, but not an emergency.** At 4,360 lines it is the single-file bottleneck for the project. It works because Jetpack Compose's `@Composable` scoping allows local state to be shared across all systems without formal interfaces. However, the cost is:
- Long compile times for any change
- High cognitive load for new developers
- Regression risk when modifying any system
- Poor testability (no way to unit test physics, collision, or threats)

### 2. Is refactoring urgent?

**No.** The architecture is functional and has survived 5 EPICs of features. The extraction of `RocketRenderer`, `PlatformRenderer`, `ZoneBackgroundRenderer`, `ThreatManager`, `ActiveThreat`, `ComboManager`, `MissionManager`, and `AmbientSystem` shows that the team *already* knows how to extract. The remaining GameScreen code is the hardest part: the physics engine, the rendering orchestrator, and the collision system.

### 3. What is the biggest risk in the current architecture?

**The sub-stepped physics loop + inline threat collision logic** (lines 1053-1447). This is a write-heavy section where 15 systems read and mutate shared state within a performance-critical loop. One bad refactor breaks collision detection, platform landing, or damage application -- all of which are game-breaking bugs that are hard to detect without automated tests.

### 4. What should be left alone?

- **The sub-stepped physics loop** (4-substep AABB collision resolution)
- **The `when(gameState)` state machine** (risk of introducing dead states)
- **Boss phase logic in `ActiveThreat`** (already well-encapsulated)
- **The rendering pipeline order** (changing draw order could cause visual regressions)

### 5. What should be cleaned first?

**The screen state machine (Step 1).** This is low-risk, high-reward, and immediately reduces GameScreen by ~400 lines. It requires no architectural changes -- just move each `GameState` branch to its own `@Composable` file:
- `TitleScreen.kt`
- `MainMenuScreen.kt`
- `HangarScreen.kt`
- `ArchiveScreen.kt`
- `SettingsScreen.kt`
- `AboutScreen.kt`
- `LeaderboardScreen.kt`
- `PauseScreen.kt`
- `TutorialScreen.kt`
- `GameOverScreen.kt`
- `UnlockScreen.kt`

### 6. Can EPIC 6 safely proceed without refactoring?

**Yes.** The threat system is already data-driven, meaning new bosses and hazards can be added without touching GameScreen's structure. The pressure points (projectile system, threat rendering density) can be managed by:
- Building the projectile system as a **new standalone file** from the start (do not add it to GameScreen)
- Extracting **threat rendering** into `ThreatRenderer.kt` before adding 3+ new threats
- Limiting new boss count to 2-3 (the current 6-boss framework already demonstrates diminishing returns per slot)

The real danger is not in adding features -- it is in trying to refactor the physics loop while adding features simultaneously. As long as EPIC 6 avoids touching the substep loop internals, it can proceed safely.

---

*End of Report*
