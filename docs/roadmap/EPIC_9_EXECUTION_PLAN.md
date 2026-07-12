# EPIC 9 — Hidden Signals & Dynamic Unlocks: Execution Plan

**Author:** Architecture Agent
**Date:** 2026-06-24
**Status:** Ready to Execute
**Prerequisite:** EPIC 8.5 ✅ — `epic8.5-structured` (commit `eacdf3e`)
**Branch:** `epic9` (create from `development`)

---

## 1. Overview

EPIC 9 transforms the progression experience from a linear list into a discovery-driven journey. Three systems in parallel:

1. **Hidden Signals** — 7 secret missions with glitch UI, cryptic hints, and reveal ceremonies
2. **Dynamic Unlock Engine** — AND/OR requirement evaluator that gates 17 modules + 3 blueprints behind discoveries
3. **The Archive Expands** — Artifact set bonuses, 10 lore logs, lost technology blueprints

---

## 2. Sprint Breakdown

### Sprint 9.1: Hidden Signal Reveal Mechanics

**Goal:** Hidden missions show encrypted "SIGNAL LOST" state with glitch effect. On unlock, play reveal ceremony.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 1.1 | Create `GlitchText` composable | **New:** `GlitchText.kt` | `@Composable fun GlitchText(text: String, modifier: Modifier = Modifier)` — randomly flickers 2-3 characters per frame to `?`/`#`/`*`, overlays a subtle scanline gradient. Use `rememberInfiniteTransition` to drive the flicker at ~8Hz. `import` from `com.example.jump_droid` package. |
| 1.2 | Add `crypticHint` field to `Mission` | **Edit:** `Mission.kt` | Add `val crypticHint: String = ""` as constructor parameter at line ~57. Default empty string. |
| 1.3 | Populate hints for 7 hidden missions | **Edit:** `MissionRegistry.kt` lines 209-237 | Add hint text to each hidden mission constructor:<br>`"hidden_long_haul"` → `"A whisper from the deep... — Survive beyond 3,000m"`<br>`"hidden_heat_junkie"` → `"The core runs hot... — Push the limits below 1,500m"`<br>`"hidden_near_death"` → `"The edge of destruction... — Test fate below 1,000m"`<br>`"hidden_void_walker"` → `"The sky has a floor... — Find it."`<br>`"hidden_perfect_storm"` → `"Perfection is a pattern... — Complete flight_time_2 first"`<br>`"hidden_artifact_hunter"` → `"They left things behind... — Collect 3 artifacts to begin"`<br>`"hidden_momentum_legend"` → `"Speed is weight... — Master momentum_master_2 first"` |
| 1.4 | Update `HiddenSignalsCard` for encrypted state | **Edit:** `MissionScreen.kt` lines 295-331 | For each `mission` in the list: if `!mission.isUnlocked`, render title as `GlitchText("SIGNAL LOST")` in SciFiRed, show `mission.crypticHint` as subtitle in SciFiPurple italics, hide description/progress/claim. If unlocked, show normally. |
| 1.5 | Create `SignalDecodedOverlay` | **New:** `SignalDecodedOverlay.kt` | `@Composable fun SignalDecodedOverlay(missionName: String)` — full-screen Box with radial scan animation (drawArc sweep), semitransparent black background, "SIGNAL DECODED" in SciFiCyan with 2x letter spacing. Auto-dismiss after 2s. Return an `onDismiss: () -> Unit` callback. |
| 1.6 | Trigger reveal ceremony | **Edit:** `MissionManager.kt` `checkUnlocks()` at line 80 | When `isUnlocked` transitions from false to true for a hidden mission, emit a callback: `onHiddenSignalRevealed?.invoke(mission)`. Wire this in `GameScreen.kt` to show the overlay. |
| 1.7 | Persist unlock state | **Edit:** `ProgressionService.kt` + `MissionManager.kt` | Add `fun saveUnlockedMissionIds(ids: Set<String>)` and `fun getUnlockedMissionIds(): Set<String>` to `ProgressionService`. Call save in `checkUnlocks()` after evaluating. Restore in `init` block (around line 106). |

**Definition of Done:** All 7 hidden missions show glitch + hint when locked. Each plays reveal ceremony on unlock. State survives app restart.

---

### Sprint 9.2: Dynamic Unlock Engine

**Goal:** A formal requirement evaluator that gates modules/blueprints behind conditions with AND/OR logic.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 2.1 | Enhance `UnlockRequirement` with AND/OR | **Edit:** `Module.kt` lines 16-24 | Add `enum class LogicOp { AND, OR }`. Change `UnlockRequirement` to: `data class UnlockRequirement(val type: UnlockType, val target: String = "", val value: Float = 1f, val operator: LogicOp = LogicOp.AND)`. Keep existing 5 `UnlockType` values. Add 2 new: `UnlockType.ARTIFACT_SET` and `UnlockType.MISSION`. Total 7 unlock types. |
| 2.2 | Create `UnlockEngine` evaluator | **New:** `UnlockEngine.kt` | `object UnlockEngine { fun evaluate(req: UnlockRequirement, progression: ProgressionManager, missionManager: MissionManager): Boolean }`. Implement each `UnlockType`: `SCORE` → highScore >= value; `ALTITUDE` → highScore >= value; `DISCOVERY` → isDiscovered(targetDiscoveryTypeName); `MISSION` → mission with target ID is completed; `ARTIFACT` → lifetime artifacts >= value; `ARTIFACT_SET` → set fully discovered; `MISSION` → mission completed. For `AND` operator, check each sub-requirement. For `OR`, check any. For single requirements, `AND` is default. |
| 2.3 | Update 17 module requirements | **Edit:** `ModuleRegistry.kt` | Replace each `override val unlockRequirement` with real values. Reference existing `UnlockType` enum values. Use `DiscoveryType` names as target for DISCOVERY type. Set thresholds based on module power level (COMMON=unlock early, LEGENDARY=unlock late). Example:<br>`TurboBooster` → `ALTITUDE, 2000`<br>`ShieldCap` → `DISCOVERY, "discovery_shield", 1`<br>`EfficiencyModule` → `ARTIFACT, "", 10` |
| 2.4 | Gate module equipping in LoadoutScreen | **Edit:** `LoadoutScreen.kt` + `LoadoutManager.kt` | In `LoadoutManager`, add `fun isModuleUnlocked(module: Module): Boolean` that calls `UnlockEngine.evaluate`. In `LoadoutScreen.kt`, grey out locked modules in the list. Add a small "🔒" overlay icon to the module card. Show requirement text below module name (e.g. "Requires: 2,000m altitude"). Prevent tap-to-equip on locked modules. |
| 2.5 | Add telemetry hook after mission unlock | **Edit:** `MissionManager.kt` `checkUnlocks()` | After the existing unlock evaluation loop, call `ProgressionManager.checkModuleUnlocks()`. Add `fun checkModuleUnlocks()` to `ProgressionManager` that iterates all 17 modules and evaluates each. If newly unlocked, call `onModuleUnlocked?.invoke(module)`. |
| 2.6 | Module unlock notification | **Edit:** `GameScreen.kt` | Wire `onModuleUnlocked` callback to show: `floatingTextManager.add(FloatingText("MODULE UNLOCKED: [name]", ...))` in SciFiGold + `screenShake = 15f`. Ensure notification is queued or delayed if another ceremony is active to prevent overlap. |

**Definition of Done:** All 17 modules gated by real requirements. Locked modules show requirement text. Unlock triggers notification. AND/OR logic works.

---

### Sprint 9.3: Artifact Set Bonuses

**Goal:** Group artifacts into thematic sets. Full collection grants passive stat bonuses.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 3.1 | Create `ArtifactSet` data model | **New:** `ArtifactSet.kt` | `data class ArtifactSet(val id: String, val name: String, val discoveries: List<DiscoveryType>, val bonus: ArtifactBonus)` with `sealed class ArtifactBonus { data class FuelRegen(val multiplier: Float) ... }`. Define 3-5 bonuses: `FUEL_REGEN(+10%)`, `SHIELD_REGEN(+15%)`, `HEAT_COOLDOWN(-20%)`, `HULL_BOOST(+25)`, `THRUST_BOOST(+5%)`. |
| 3.2 | Define the 3-5 artifact sets | **Edit:** `ArtifactSet.kt` | Add a `val ALL_SETS: List<ArtifactSet>` companion list. Group `DiscoveryType` entries logically: "The Great Signal" (LORE_SIGNAL, FREQUENCY_DATA, etc.), "Ancient Survey" (AREA_EARTH, AREA_CLOUDS, AREA_ATMOSPHERE), "Void Relics" (AREA_SPACE, AREA_VOID, LORE_LOGS), "Efficiency Corps" (EFFICIENCY_FUEL, EFFICIENCY_SURVIVAL, BOOST_PLATFORM), "Core Systems" (SHIELD_CAPSULE, OVERHEAT_SYSTEM, COOLANT). Each set gets 3 discoveries. |
| 3.3 | Evaluate and apply set bonuses | **Edit:** `ProgressionManager.kt` | Add `var activeSetBonuses: List<ArtifactBonus> by mutableStateOf(emptyList())`. Add `fun reevaluateSetBonuses()` that checks each set's discoveries against `sharedPrefs`. For fully discovered sets, add bonus to active list. Call from `checkModuleUnlocks()`. |
| 3.4 | Apply bonuses to player stats | **Edit:** `Player` class in `Models.kt` or runtime in `GameEngine.kt` | When building `Player` state each frame, check `progressionManager.activeSetBonuses`. Apply multipliers to fuel regen, shield regen, heat cooldown, max hull, thrust force. Keep as multiplicative stack. |
| 3.5 | Set progress UI in Archive | **Edit:** `ArchiveScreen.kt` | Add section "Artifact Collections" below the codex list. For each `ArtifactSet`, show name + progress bar (3/3) + bonus description. When complete, show green "SET ACTIVE" badge with bonus text. |

**Definition of Done:** 3-5 artifact sets defined. Full discovery of all pieces grants visible gameplay bonus. UI shows collection progress.

---

### Sprint 9.4: Rare Lore Logs

**Goal:** 10 altitude-triggered lore fragments with dedicated viewer.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 4.1 | Create `LoreLog` data | **New:** `LoreLog.kt` | `data class LoreLog(val id: String, val title: String, val text: String, val unlockAltitude: Int, val category: LoreCategory)` with `enum class LoreCategory { SIGNAL, SURVIVOR, ANCIENT, VOID, EPILOGUE }`. Define 10 logs at altitudes 500, 1000, 2000, 3000, 4000, 5000, 6000, 7500, 9000, 10000. Each with 1-2 paragraph "voice log" style text. |
| 4.2 | Persist discovered logs | **Edit:** `ProgressionService.kt` + `ProgressionManager.kt` | `fun saveDiscoveredLog(logId: String)` / `fun getDiscoveredLogs(): Set<String>`. Store as SharedPreferences `discovered_log_$id` boolean. |
| 4.3 | Trigger lore on altitude threshold | **Edit:** `ProgressionManager.kt` `checkUnlocks()` | Add call to `evaluateLoreLogs(score)` in the existing hook. Function checks if any `LoreLog` has `unlockAltitude <= score` and not yet discovered. If so, save and emit `onLoreDiscovered?.invoke(log)`. |
| 4.4 | Lore log viewer in Archive | **Edit:** `ArchiveScreen.kt` | Add "LORE LOGS" tab. List discovered logs by category. Tap to view full text in modal: white-on-black, serif-style font, slow scroll. Undiscovered logs show as "SIGNAL LOST" with GlitchText. |
| 4.5 | In-game lore popup | **Edit:** `GameScreen.kt` | Wire `onLoreDiscovered` callback: show `FloatingText(log.title, ...)` in SciFiWhite with 3s duration + screenShake 8f. Add a brief `NotificationManager` post: "LORE LOG RECOVERED — [category]". |

**Definition of Done:** 10 lore logs trigger at altitude milestones. Archive viewer shows all discovered logs with category grouping. Each log has unique text.

---

### Sprint 9.5: Blueprints & Stabilization

**Goal:** Cosmetic unlockables + polish pass.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 5.1 | Add `BlueprintType` enum | **Edit:** `Models.kt` or **New:** `Blueprint.kt` | `enum class BlueprintType { ENGINE_TRAIL_CYAN, HUD_THEME_AMBER, ROCKET_SKIN_OBSIDIAN }`. Each has `displayName: String` and `icon: String`. |
| 5.2 | Define blueprint unlock requirements | **Edit:** same file | Companion map: each `BlueprintType` → `UnlockRequirement`. Example: ENGINE_TRAIL_CYAN → `UnlockRequirement(UnlockType.ARTIFACT, "", 15)`, HUD_THEME_AMBER → `UnlockRequirement(UnlockType.SCORE, "", 8000)`, ROCKET_SKIN_OBSIDIAN → `UnlockRequirement(UnlockType.MISSION, "hidden_void_walker", 1)`. |
| 5.3 | Persist blueprints | **Edit:** `ProgressionService.kt` | `fun saveUnlockedBlueprint(id: String)` / `fun getUnlockedBlueprints(): Set<String>`. |
| 5.4 | Evaluate blueprint unlocks | **Edit:** `ProgressionManager.kt` `checkModuleUnlocks()` | In the same evaluation loop, check each `BlueprintType` requirement. If met and not yet saved, save and invoke callback. |
| 5.5 | Blueprint notification | **Edit:** `GameScreen.kt` | Wire callback: `floatingTextManager.add(FloatingText("BLUEPRINT ACQUIRED: [name]", ...))` in SciFiGold + `impactFlashAlpha = 0.4f`. |
| 5.6 | Blueprint preview in Hangar/Loadout | **Edit:** `HangarScreen.kt` or `LoadoutScreen.kt` | Add small section at bottom: "BLUEPRINTS" showing acquired items. For now, just icon + name (visual rendering of skins/trails deferred to EPIC 12). |
| 5.7 | Visual regression pass | **Edit:** `GameScreen.kt`, `MissionScreen.kt` | Playtest and verify no HUD clutter when multiple hidden signals active. Ensure `GlobalShowObjective` rotation doesn't conflict. |
| 5.8 | Balance pass | **Edit:** `MissionRegistry.kt`, `ModuleRegistry.kt` | Tune reveal thresholds. Verify first hidden mission unlocks around 1,000-1,500m for playability. Ensure 17 module unlock requirements feel achievable. |

**Definition of Done:** 3 blueprints unlockable. Blueprint section visible in Hangar. Visual regression pass clean. Build green.

---

## 3. File Inventory

### New files to create (8)

| File | Purpose |
|------|---------|
| `GlitchText.kt` | Glitch effect composable |
| `SignalDecodedOverlay.kt` | Reveal ceremony overlay |
| `UnlockEngine.kt` | Requirement evaluator |
| `ArtifactSet.kt` | Artifact set data + bonus models |
| `LoreLog.kt` | Lore log data + 10 entries |
| `Blueprint.kt` | Blueprint type definitions |

### Existing files to modify (14)

| File | What changes |
|------|-------------|
| `Mission.kt` | Add `crypticHint` field |
| `MissionRegistry.kt` | Populate hints for 7 hidden missions |
| `MissionScreen.kt` | Encrypted state in HiddenSignalsCard, glitch rendering |
| `MissionManager.kt` | Reveal ceremony callback, unlock persistence |
| `Module.kt` | Enhance UnlockRequirement with AND/OR |
| `ModuleRegistry.kt` | Update 17 module requirements |
| `ProgressionService.kt` | Add save/get for unlocks, artifacts, lore |
| `ProgressionManager.kt` | Module unlock evaluation, artifact set evaluation, lore triggers |
| `LoadoutScreen.kt` | Gate module equipping, show requirement text |
| `GameScreen.kt` | Wire unlock/lore/blueprint callbacks, ceremony overlay trigger |
| `Models.kt` | BlueprintType enum |
| `HangarScreen.kt` | Blueprint section |
| `ArchiveScreen.kt` | Lore log viewer, artifact set progress |
| `Player` (in `Models.kt` or engine) | Apply set bonus multipliers |

---

## 4. Dependencies & Order

```
Sprint 9.1 ───────────────────┐
                              ├──► All independent of each other
Sprint 9.2 ───────────────────┤
                              │    Can run in parallel
Sprint 9.3 ───────────────────┤
                              │
Sprint 9.4 ───────────────────┤
                              │
Sprint 9.5 (polish) ◄─────────┘  Depends on all previous
```

**Within each sprint, tasks are sequential** (each builds on the previous).

**Parallel work is possible** across sprints if multiple engineers available: 9.1 (UI), 9.2 (engine), 9.3 (data), and 9.4 (content) are independent.

---

## 5. Rollback & Verification

- **Rollback:** `git tag epic9-sprint1` before each sprint starts
- **Build:** `./gradlew assembleDebug` at end of each task, each sprint
- **Playtest:** After each sprint, verify via manual play that:
  - No visual regressions in HUD
  - Previously working missions still track
  - No build warnings
- **GameScreen.kt line budget:** Must stay under **2,200 lines** (currently 1,998)

---

## 6. Acceptance Criteria

- [ ] 7 hidden missions with unique glitch + hint + reveal ceremony
- [ ] 17 modules gated by real requirements (AND/OR)
- [ ] 3-5 artifact sets with passive stat bonuses
- [ ] 10 lore logs with archive viewer
- [ ] 3 blueprints unlockable (visual render deferred)
- [ ] All prior EPIC 8 content works without regression
- [ ] Build green, playtest passes
