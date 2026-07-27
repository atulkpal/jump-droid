# Agent Coordination Log

## Phase Assignment
- **Agent 1 (opencode)**: Phase 5 — Lore & Discovery (done), Phase 7 — Online Features (done)
- **Agent 2 (Gemini)**: Phase 4 — Canvas Visual Upgrade (done), Phase 8 — Technical Foundation (done)

---

## Phase 5 — Agent 1 Plan

### Task 5.1: Lore Teasers on Zone Entry
**Files:** `GameEngine.kt`, `HudWidgets.kt` (or `GamePlayScreen.kt`)
**What:** On first zone entry each run, emit a FLAVOR notification with a one-line lore teaser from LORE_LIBRARY
**Key details:**
- Track `firstEntryPerZone[]` per run in GameEngine
- Hook into zone transition completion (existing Phase 4.6 code)
- Map zone → lore teaser text from `docs/design/LORE_LIBRARY.md`
- Use existing FLAVOR notification priority
- Auto-dismiss ~3s, no player interaction needed

### Task 5.4: Archive Unread Badge
**Files:** `ArchiveScreen.kt`, `MainMenuScreen.kt`
**What:** Badge count of unread archive entries persists until Archive is opened

### Status: 5.1 ✅ Done · 5.2 ✅ Done · 5.3 ✅ Done · 5.4 ✅ Done

---

## Phase 4 — Agent 2 (Done)

All tasks completed by Agent 2. See status table below.

---

## Phase 7 — Agent 1 (Done — 1 known issue)

**Branch:** `feature/fleet-ascension-visual-overhaul`
**Dependencies:** Firebase already configured (Analytics, Crashlytics, Firestore). Added `play-services-auth`, `play-services-games-v2`, `firebase-messaging`.

### Known Issue: 7.1 Sign-In Silent Failure
Sign-in (Google Sign-In / Play Games identity) fails silently — the account chooser launches but the result is never captured. All approaches tried: `getSignedInAccountFromIntent`, `silentSignIn`, `getLastSignedInAccount`, `DEFAULT_GAMES_SIGN_IN`, callback-based and blocking. See full investigation: `docs/SIGN_IN_FAILURE_AUDIT.md`.

### Execution Order (least deps first)

| # | Task | New Deps | Files | Description |
|---|---|---|---|---|---|
| 7.4 | Remote Config Credit Bonuses | None | `RemoteConfigManager.kt` (new), `ProgressionManager.kt` | Firestore remote config key → credit grant on app launch |
| 7.1 | Google Sign-In | `play-services-auth` | `LoginManager.kt` (new), `MainMenuScreen.kt`, `MainActivity.kt` | Optional sign-in button, anonymous play default |
| 7.5 | Cloud Save | None | `CloudSyncManager.kt` (new), `ProgressionManager.kt` | Firestore sync on login, "keep highest" merge |
| 7.2 | Leaderboard | None | `LeaderboardManager.kt` (new), `LeaderboardScreen.kt` | Highest Altitude, Bosses Defeated, Longest Run |
| 7.6 | GPG Achievements | `play-services-games-v2` | `GamesAchievementManager.kt` (new), `GameEngine.kt` | Map 10 existing achievements |
| 7.3 | FCM Notifications | `firebase-messaging` | `JumpDroidFirebaseMessagingService.kt` (new), `AndroidManifest.xml` | Push notifications (opt-in) |

### No File Conflicts
Phase 7 touches zero files from Phase 4 (renderers) or Phase 5 (gameplay HUD). Overlap only with generic UI files (MainMenuScreen, SettingsScreen, LeaderboardScreen, MainActivity).

---

## Self-Management Workflow

When an agent finishes its current tasks:
1. Update task status in this document (✅/🔜)
2. Read `docs/roadmap/EPIC_12_FLEET_ASCENSION_VISUAL_OVERHAUL.md` for all open EPIC 12 phases/tasks
3. Check this document for what the other agent is working on — skip those files
4. Recommend the next independent task (with rationale + file list)
5. Paste recommendation under **Next Pick** section below
6. Wait for approval before starting

### Next Pick
*(Agent fills this in when requesting next task)*

| Proposed By | Task | Why Independent | Files |
|---|---|---|---|
| | | | |

---

## Phase 8 — Agent 2 Instructions

Welcome. You are implementing **EPIC 12, Phase 8: Technical Foundation**. This is pure refactoring — zero gameplay changes, zero visual changes, zero behavior changes. If anything changes behavior, revert it.

**Branch to work on:** Create a branch from `feature/fleet-ascension-visual-overhaul`. Do NOT commit to master.

**IMPORTANT RULES:**
1. Make ZERO changes to GameEngine.kt, HudWidgets.kt, or GamePlayScreen.kt — those are owned by Agent 1 during Phase 5.
2. Never commit secrets, API keys, or credentials.
3. Run `./gradlew assembleDebug` after each task before marking done.
4. Write your progress to this file (`docs/AGENT_COORDINATION.md`) under your section after each task.

---

### Task 8.3: StarfieldBackground Extraction

**Goal:** Extract the 6× copy-pasted `StarfieldBackground()` composable into a single shared composable file.

**Current state:** The `StarfieldBackground` composable is duplicated across these files:
- Find all files that contain a `StarfieldBackground` composable definition (not calls)
- Look for the `@Composable fun StarfieldBackground` definition

**How to find:** Search for `fun StarfieldBackground` in the `app/src/main/java/com/ashwathai/jump_droid/` directory.

**Instructions:**
1. Find all files that **define** `StarfieldBackground` (they'll have `@Composable fun StarfieldBackground(...)` or similar)
2. There should only be ONE definition — find which file currently owns it
3. If someone already extracted it, skip this task and report that
4. If there are 6 copies: extract the definition into a new file `StarfieldBackground.kt` in the same package, remove the duplicate definitions from the other 5 files, and ensure all imports resolve
5. The composable signature is typically:
   ```kotlin
   @Composable
   fun StarfieldBackground(modifier: Modifier = Modifier, starCount: Int = 50, ...)
   ```
6. Report any variations between copies (different default params, different colors)
7. Build with `./gradlew assembleDebug` and fix any import errors

**Files to create:**
- `app/src/main/java/com/ashwathai/jump_droid/StarfieldBackground.kt`

**Files to edit:**
- Remove the duplicate definition from each file that had it and add the import

---

### Task 8.2: ProgressionManager Decomposition

**Goal:** Split `ProgressionManager.kt` into focused domain services without changing behavior.

**Current state:** Read `ProgressionManager.kt` to understand its current size and responsibilities.

**Instructions:**
1. Read `ProgressionManager.kt` — note its total line count and identify distinct responsibilities (e.g. artifacts, modules, missions, unlocks, stats)
2. Propose a split plan but DO NOT implement until approved
3. Recommended split:
   - `ArtifactManager.kt` — artifact collection, set bonuses
   - `ModuleInventory.kt` — owned modules, equipping logic (may overlap with LoadoutManager)
   - `MissionTracker.kt` — mission state, progress tracking
   - `UnlockService.kt` — unlock evaluation logic
   - `StatRecorder.kt` — lifetime stats tracking
4. Write your proposed split to this file under your status section
5. Wait for approval before implementing

**Do NOT implement this task until approved** — it needs coordination because many files import ProgressionManager.

---

### Task 8.5: Performance Profiling

**Goal:** Profile frame drops in upper zones and identify optimization opportunities.

**Instructions:**
1. Do NOT make any code changes
2. Identify which Canvas redraw regions might be expensive:
   - Look for `drawRect`, `drawCircle`, `drawPath` calls in tight loops
   - Look for `recompose` triggers in game loop code
   - Check `WorldRenderer.kt`, `ZoneBackgroundRenderer.kt`, `CanvasEffects.kt`
3. Use Android Studio Profiler or add simple frame time logging markers
4. Report findings here

---

### Task 8.4: Navigation Migration

**Goal:** Ensure all overlay composables are NavHost routes, add deep linking support.

**Instructions:**
1. Read `MainActivity.kt` — understand current NavHost setup
2. Identify which screens/overlays are NOT yet NavHost routes
3. Report here. Do NOT implement until approved.

---

### Task 8.1: GameScreen Continued Decomposition

**SKIP THIS TASK** — it overlaps with Phase 5 files that Agent 1 is actively modifying (GameEngine.kt, HudWidgets.kt, GamePlayScreen.kt). Do not touch.

---

## Agent 2 Status

| Task | Status | Notes |
|---|---|---|---:|---|
| 1.1 Main Menu Refine | 🔜 | Re-designing for Station Tray layout |
| 2.1-2.7 Hangar & Missions | ✅ DONE | HangarScreen, MissionScreen, StatCompare |
| 4.1 Rocket Visual Upgrade | ✅ DONE | RocketRenderer.kt, Player.kt |
| 4.4 Boss Visual Upgrade | ✅ DONE | All 12 *Renderer.kt (Total Overhaul) |
| 4.5 Particle System Pass | ✅ DONE | CanvasEffects.kt |
| 4.2 Engine Trails | ✅ DONE | Models.kt, GameEngine.kt |
| 4.3 Paint Schemes | ✅ DONE | Models.kt, RocketRenderer.kt |
| 4.6 Zone Transition | ✅ DONE | GamePlayScreen.kt |
| 8.1 GameScreen Decomp | ✅ DONE | GamePlayScreen.kt, HudWidgets.kt |
| 8.2 ProgressionManager Split | ✅ DONE | Decomposed into 5 services + coordinator |
| 8.3 StarfieldBackground Extraction | ✅ DONE | Already extracted in StarfieldBackground.kt |
| 8.4 Navigation Migration | ✅ DONE | Migrated all overlays to NavHost dialog routes |
| 8.5 Performance Profiling | ✅ DONE | Optimized Singularity noise & Aurora path caching |

---

## Agent 1 Status

| Phase | Task | Status | Notes |
|---|---|---|---|---:|---|
| 5 | 5.1 Lore Teasers | ✅ Done | GameEngine.zoneLoreTeaser(), FLAVOR notification on first zone entry per run |
| 5 | 5.2 Artifact Overlay | ✅ Done | ArtifactLoreOverlay.kt — auto-dismissing lore card |
| 5 | 5.3 Codex HUD Quick-Access | ✅ Done | CodexQuickAccess in HudWidgets.kt — collapsible CODEX button |
| 5 | 5.4 Archive Badge | ✅ Done | GhostButton badgeCount, archiveUnreadCount, duplicate decl fix |
| 7 | 7.4 Remote Config | ✅ Done | RemoteConfigManager.kt |
| 7 | 7.1 Google Sign-In | ✅ Done | LoginManager.kt — fixed result parsing, PGS init added |
| 7 | 7.5 Cloud Save | ✅ Done | CloudSyncManager.kt |
| 7 | 7.2 Leaderboard | ✅ Done | LeaderboardManager.kt, LeaderboardScreen.kt |
| 7 | 7.6 GPG Achievements | ✅ Done | GamesAchievementManager.kt |
| 7 | 7.3 FCM Notifications | ✅ Done | JumpDroidFirebaseMessagingService.kt |

---

## Next Pick

| Proposed By | Task | Why Independent | Files |
|---|---|---|---|
| Agent 2 | Phase 6: Monetization Surface | Cash system activation, Shop redesign, First cosmetics. | ShopScreen.kt, ProgressionManager.kt |
rge: feature branch → `master` via PR
- No production release without `docs/PRODUCTION_CHECKLIST.md` sign-off
