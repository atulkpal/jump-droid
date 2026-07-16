# EPIC 8.5 — Architecture Decomposition Sprint

**Status:** Planned
**Branch:** `epic8-mission-migration` (new sub-branch TBD)
**Dependencies:** EPIC 8 complete, EPIC 9 deferred
**Audit Basis:** `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`

---

## 1. Why EPIC 8.5 Exists

The EPIC 8 Tech Debt Audit (2026-06-23) identified that the codebase has outgrown its rapid-prototyping architecture. Two files — `GameScreen.kt` (3,901 lines) and `ActiveThreat.kt` (1,224 lines) — are God Objects that will block all planned Phase 9 work (Hidden Signals, Dynamic Unlocks, more threats, more bosses, more progression).

Rather than deferring this debt into EPIC 9 (where it would compound with new feature work), **EPIC 8.5 dedicates a full sprint cycle to architectural decomposition before any new features are introduced.**

This is a **structural investment**: reducing file sizes, separating concerns, and introducing extensibility patterns so that Phase 9 work can proceed without exponential growth of the two God Objects.

---

## 2. Goals

1. **Reduce GameScreen.kt** from 3,901 lines to ~1,500 lines by extracting:
   - The game engine loop into a standalone `GameEngine` class
   - The threat rendering pipeline into per-type renderers
   - The navigation layer into a `NavHost` structure

2. **Reduce ActiveThreat.kt** from 1,224 lines to ~250 lines by:
   - Extracting per-threat AI behavior into strategy classes (`ThreatBehavior`)
   - Extracting collision/interaction logic into a `CollisionSystem`
   - Replacing the 17-callback `processInteraction` API with an event bus

3. **Decompose ProgressionManager.kt** (334 lines, 7+ responsibilities) into domain services:
   - `IntelligenceNetwork` (lifetime stats)
   - `UnlockService` (data-driven condition checking)
   - `MissionTracker` (mission progress, completions, claims)
   - `InventoryManager` (module ownership)

4. **Eliminate dead code and duplication**:
   - Delete `MissionRow.kt` (140 lines, zero imports)
   - Remove `isNew` zombie field from `Mission.kt`
   - Remove unused `CeremonyStage` values
   - Extract `StarfieldBackground` composable (eliminate 6x copy-paste)

5. **Improve testability**:
   - Extract inline filtering/sorting logic from `MissionScreen.kt` into `MissionManager`
   - Extract `GaugeBar` base composable from `HudWidgets.kt`
   - Make `Mission.checkCompletion()` a pure function

---

## 3. Scope

### In Scope
- Game engine extraction (physics loop, frame timing, update orchestration)
- Threat rendering extraction (per-type renderers)
- Threat AI strategy pattern (per-type behavior classes)
- Collision system extraction
- Event bus for threat-game interaction (replacing 17-callback API)
- ProgressionManager decomposition
- Navigation migration to `NavHost`
- HUD gauge abstraction
- Starfield background extraction
- Dead code removal
- Mission model cleanup
- `EncounterDirector` spawn table externalization
- `ComboManager` reward system unification

### Not in Scope
- No new gameplay features
- No new missions, threats, bosses, or progression systems
- No Hidden Signals implementation
- No Dynamic Unlocks implementation
- No balance changes
- No content additions
- No audio system
- No monetization system

### Success Criteria
1. Baseline tag `epic8.5-baseline` created with build verification and full metric capture
2. `GameScreen.kt` < 1,800 lines
3. `ActiveThreat.kt` < 350 lines
4. All existing managers retain identical public API (backward compatible)
5. `./gradlew assembleDebug` — `BUILD SUCCESSFUL`
6. All gameplay behavior unchanged (verified by smoke test)
7. Zero dead code files in production source tree
8. `ProgressionManager` split into ≤ 4 focused services

---

## 4. Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Baseline screenshots become outdated if decomposition starts before capture | Low | Medium | Capture baseline as first action in Sprint 8.5.0; tag immediately |
| Game engine extraction breaks physics timing | Low | High | Extract in stages with `git cherry-pick` between each; verify gameplay feels identical |
| NavHost migration breaks screen state | Medium | High | Preserve `GameState` enum as navigation target during transition; validate all 14 screens render correctly |
| Event bus introduces subtle timing bugs | Medium | Medium | Use synchronous event dispatch (same-thread); no async; validate with existing test patterns |
| `ProgressionManager` decomposition breaks mission persistence | Low | High | Keep `recordMissionCompletion` and `saveMissionProgress` as facade until all call sites migrated; test save/load cycle |
| Sprint scope creep | High | Medium | Strict scope enforcement: each sprint has a hard exit criterion. If a sprint exceeds estimate, cut unstarted items. |

---

## 5. Dependencies

| Dependency | Type | Notes |
|-----------|------|-------|
| EPIC 8 validation signoff | Precondition | Must confirm no blocking bugs remain |
| `epic8-mission-migration` branch | Base | All decomposition work based on current HEAD |
| Baseline tag `epic8.5-baseline` | Precondition for 8.5.1+ | Tagged HEAD with full metric capture before any code change |
| No concurrent feature branches | Process | EPIC 8.5 requires exclusive branch access to avoid merge conflicts |
| Audit document (EPIC8_TECH_DEBT_AUDIT.md) | Reference | Prioritization and scope defined by audit findings |

---

## 6. Sprint Breakdown

### Sprint 8.5.0 — Baseline Capture
**Target:** Establish known-good EPIC 8 baseline before any decomposition work begins
**Effort:** 1 session
**Risk:** None

| Item | Action | Deliverable |
|------|--------|-------------|
| Baseline tag strategy | Create `epic8.5-baseline` tag at current HEAD after build verification | Git tag |
| Build verification | `./gradlew assembleDebug` — confirm `BUILD SUCCESSFUL` | Build log entry |
| File size inventory | Record line counts for all key files: GameScreen.kt, ActiveThreat.kt, ProgressionManager.kt, HudWidgets.kt, MissionManager.kt, EncounterDirector.kt, ComboManager.kt, NavigationManager.kt | Section in Baseline Report |
| Key architecture metrics | Count imports per file, function count, callback parameters, StateList usages | Baseline Report |
| Screenshot baseline | Capture screenshots of Title, Hangar, Command, Mission Dashboard, Gameplay, Settings, About, Archive screens | `docs/screenshots/EPIC8_BASELINE/` |
| Baseline report | Write `docs/analysis/EPIC8_5_BASELINE_REPORT.md` with all captured data | Report file |

**Exit criterion:** All baseline data captured, tagged, and committed. Zero code modifications to production files.

---

### Sprint 8.5.1 — Low-Risk Cleanup
**Target:** Dead code removal, zombie fields, model cleanup
**Effort:** 1 session
**Risk:** None

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Delete MissionRow.kt | FINDING-14 | Remove file, verify zero imports | `MissionRow.kt` |
| Remove `isNew` from Mission.kt | FINDING-07 | Remove field and all references | `Mission.kt` |
| Remove unused CeremonyStage values | FINDING-06 | Collapse to `NONE`, `GLOW`, `REPLACING` | `Mission.kt`, `GameScreen.kt` |
| Make `checkCompletion()` pure | FINDING-06 | Remove ceremonyStage side effect, move to caller | `Mission.kt` |
| Remove redundant ProgressionManager param | FINDING-11 | Remove method-level `progressionManager` from `claimMissionRewards` | `MissionManager.kt` |
| Remove `MissionType.COMBO` | FINDING-07 audit | Remove dead enum value, inline icon mapping | `MissionType.kt`, `GameScreen.kt` |

### Sprint 8.5.2 — HUD Decomposition
**Target:** Reduce gauge duplication, extract starfield
**Effort:** 1 session
**Risk:** Low

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Extract `GaugeBar` base composable | FINDING-17 | Create shared gauge bar with configurable color/icon | `HudWidgets.kt` |
| Hoist cross-cutting HUD state | FINDING-17 | Create `HudState` data class for `gameTime`, `interferenceTimer`, `zone` | `HudWidgets.kt`, `GameScreen.kt` |
| Extract `StarfieldBackground` composable | FINDING-12 | Single reusable starfield; replace in 6 screens | New file + 6 screens |
| Extract MissionScreen track selection | FINDING-16 | Move inline filter/sort to `MissionManager` | `MissionScreen.kt`, `MissionManager.kt` |

### Sprint 8.5.3 — Notification & Celebration Extraction
**Target:** Clean ceremony lifecycle, unify reward systems
**Effort:** 1 session
**Risk:** Low-Medium

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Extract ceremony lifecycle from GameScreen | FINDING-06 | Move timer-based ceremony to `MissionManager` | `GameScreen.kt`, `MissionManager.kt` |
| Merge ComboManager reward systems | FINDING-09 | Unify tier rewards + survival rewards into one table | `ComboManager.kt` |
| Externalize combo reward table | FINDING-09 | Move to data config | `ComboManager.kt` |
| Reduce MissionManager coupling | FINDING-11 | Define `ProgressionService` interface | `MissionManager.kt` |

### Sprint 8.5.4 — Threat Rendering Extraction
**Target:** Move 1,947 lines of inline Canvas rendering to per-type renderers
**Effort:** 2-3 sessions
**Risk:** Medium

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Define `ThreatRenderer` interface | FINDING-05 | Interface with `DrawScope.renderThreat(threat)` | New file |
| Extract hazard renderers | FINDING-05 | 9 hazard types → 9 renderers | 9 new files |
| Extract enemy renderers | FINDING-05 | 5 enemy types → 5 renderers | 5 new files |
| Extract boss renderers | FINDING-05 | 6 bosses → 6 renderers | 6 new files |
| Replace inline Canvas block | FINDING-05 | Single dispatch call in GameScreen | `GameScreen.kt` |
| Eliminate `toIcon()` extension | FINDING-05 cleanup | Move icon mapping into renderer or config | `GameScreen.kt` |

### Sprint 8.5.5 — Game Engine Boundary Creation
**Target:** Extract physics loop, frame timing, update orchestration
**Effort:** 2-3 sessions
**Risk:** Medium-High

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Define `GameEngine` class | FINDING-01 | Encapsulate `LaunchedEffect` + `withFrameNanos` loop | New file |
| Extract physics sub-steps | FINDING-01 | 4-step physics update into engine method | New + `GameScreen.kt` |
| Extract entity update orchestration | FINDING-01 | Threat, projectile, power-up, mission update dispatch | New + `GameScreen.kt` |
| GameScreen retains Composable only | FINDING-01 | Rendering + UI orchestration only | `GameScreen.kt` |

### Sprint 8.5.6 — ActiveThreat Strategy Architecture
**Target:** Extract AI strategies, collision system, event bus
**Effort:** 2-3 sessions
**Risk:** Medium

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Define `ThreatBehavior` interface | FINDING-02 | Interface with `update()`, `onCollision()`, `onSpawn()` | New file |
| Extract hazard AI strategies | FINDING-02 | 9 hazard types → 9 strategies | 9 new files |
| Extract enemy AI strategies | FINDING-02 | 5 enemy types → 5 strategies | 5 new files |
| Extract boss AI strategies | FINDING-02 | 6 bosses → 6 strategies | 6 new files |
| Extract `CollisionSystem` | FINDING-02 | Separates collision from ActiveThreat | New file |
| Replace 17-callback API with event bus | FINDING-02 | Synchronous event dispatch | New file + `GameScreen.kt` |

### Sprint 8.5.7 — Progression Service Decomposition
**Target:** Split ProgressionManager into domain services
**Effort:** 1-2 sessions
**Risk:** Medium

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Extract `IntelligenceNetwork` | FINDING-03 | Lifetime stats tracking | New file |
| Extract `UnlockService` | FINDING-03 | Data-driven unlock evaluation | New file |
| Extract `MissionTracker` | FINDING-03 | Mission progress persistence | New file |
| Extract `InventoryManager` | FINDING-03 | Module ownership (may already be adequately separated) | New file |
| Externalize EncounterDirector tables | FINDING-08 | JSON or data class for spawn rules | `EncounterDirector.kt` |

### Sprint 8.5.8 — Navigation Migration
**Target:** Replace `when(gameState)` with `NavHost`
**Effort:** 2-3 sessions
**Risk:** Medium

| Item | Finding Ref | Action | Files |
|------|-------------|--------|-------|
| Add NavHost dependency | FINDING-04 | Update `build.gradle.kts` | `build.gradle.kts` |
| Define NavGraph | FINDING-04 | Create navigation graph for all 14 screens | New file (or `MainActivity.kt`) |
| Migrate screen composables | FINDING-04 | Wrap each screen in NavHost route | `GameScreen.kt` + all screen files |
| Remove `gameState` enum from UI | FINDING-04 | Replace with NavController navigation | `GameScreen.kt` |

---

## 7. Estimated Order of Execution

```
Sprint 8.5.0  ─── Baseline Capture           ─── 1 session    ─── FOUNDATIONAL, ZERO risk
       │
       ▼
Sprint 8.5.1  ─── Low-Risk Cleanup          ─── 1 session    ─── HIGH value, ZERO risk
       │
       ▼
Sprint 8.5.2  ─── HUD Decomposition          ─── 1 session    ─── HIGH value, LOW risk
       │
       ▼
Sprint 8.5.3  ─── Notification & Celebration ─── 1 session    ─── MEDIUM value, LOW risk
       │
       ▼
Sprint 8.5.4  ─── Threat Rendering Extraction ─── 2-3 sessions ─── HIGH value, MEDIUM risk
       │                                          (parallel with 8.5.5)
       ▼
Sprint 8.5.5  ─── Game Engine Boundary        ─── 2-3 sessions ─── HIGHEST value, MEDIUM-HIGH risk
       │                                          (parallel with 8.5.4)
       ▼
Sprint 8.5.6  ─── ActiveThreat Strategies     ─── 2-3 sessions ─── HIGH value, MEDIUM risk
       │
       ▼
Sprint 8.5.7  ─── Progression Decomposition   ─── 1-2 sessions ─── MEDIUM value, MEDIUM risk
       │
       ▼
Sprint 8.5.8  ─── Navigation Migration        ─── 2-3 sessions ─── MEDIUM value, MEDIUM risk
```

**Total estimated effort:** 13-18 sessions
**Dependencies within 8.5:** 8.5.0 → 8.5.1 → 8.5.2 → 8.5.3 → [8.5.4 parallel 8.5.5] → 8.5.6 → 8.5.7 → 8.5.8

---

## 8. Post-EPIC 8.5 State

After EPIC 8.5 completes:

| File | Before | After (Target) | Reduction |
|------|--------|---------------|-----------|
| `GameScreen.kt` | 3,901 lines | ~1,500 lines | 62% |
| `ActiveThreat.kt` | 1,224 lines | ~250 lines | 80% |
| `ProgressionManager.kt` | 334 lines | ~80 lines (facade) | 76% |
| `HudWidgets.kt` | 654 lines | ~400 lines | 39% |
| `MissionRow.kt` | 140 lines | 0 (deleted) | 100% |

New files created: ~25 (renderers, strategies, services, composables)
Total codebase change: ~14,800 → ~14,500 lines (reduction despite more files, due to eliminated duplication)

---

## 9. Integration with EPIC 9

EPIC 9 (Hidden Signals & Dynamic Unlocks) must NOT begin until EPIC 8.5 completes.

**Reasoning:**
- Adding Hidden Signals screens to the current state-based `when` block (FINDING-04) continues the pattern of growing GameScreen.kt
- Adding Dynamic Unlocks to the current ProgressionManager (FINDING-03) worsens the SRP violation
- Adding more threats/bosses to current ActiveThreat (FINDING-02) grows a 1,224-line file further
- Adding content to the current inline threat renderer (FINDING-05) adds to the 1,947-line Canvas block

After EPIC 8.5, EPIC 9 work will add files within the new architectural boundaries rather than growing the God Objects.
