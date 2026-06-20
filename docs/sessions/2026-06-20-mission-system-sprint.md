# Session Resume — Mission System Sprint (2026-06-20)

## Branch Map

| Branch | Commit | State | Contents |
|--------|--------|-------|----------|
| `development` | `0837b76` | ✅ stable, playable | Baseline before mission work. Old `Mission*.kt` files in root package. |
| `feature/mission-system` | `8a562fc` | 🔧 STILL BROKEN | All mission system changes. Builds clean, claim flow still broken after attempted fixes. Multiple root causes suspected but unconfirmed. |

To resume:
```bash
git checkout feature/mission-system
```

## What Was Built

### New Package: `com.example.jump_droid.missions/`

```
missions/
  Mission.kt              — enums + data classes (MissionCategory, MissionTier, MissionState, ObjectiveType, UnlockType, GameStats)
  MissionRegistry.kt      — 47 missions, 14 categories, 4 tiers each + 7 hidden
  MissionManager.kt       — coroutine runtime with mutableStateMapOf, pendingStartRun, claimRewards safety net
  MissionRepository.kt    — DataStore persistence (save/load mission states + progress)
  ui/
    MissionScreen.kt      — 2-column glassmorphism grid with CLAIM pills, tier-colored cards, SIGNAL LOST overlay
```

### Key Changes Outside Package
- `GameScreen.kt` — mission integration calls, GameStats construction, overheatCount + consecutiveWins tracking
- `HudWidgets.kt` — ComboCircleTimer (32dp pie circle), zoneGaugeAccents made public
- `MainMenuScreen.kt` — MISSIONS button replaces MISSION DATA, badge indicator (⚡/🔔)
- `Models.kt` — added `GameState.MISSIONS`
- `ProgressionManager.kt` — added `getTotalDiscoveries()`
- `ActiveThreat.kt` — `onBossDefeated` replaces `onMissionProgress`
- `build.gradle.kts` + `gradle/libs.versions.toml` — added DataStore 1.1.4

### Critical Fixes Applied
1. **_progress.clear() removed from startRun()** — was wiping all progress including COMPLETED missions
2. **endRun() persists COMPLETED** — was silently dropping COMPLETED states (only saved IN_PROGRESS→AVAILABLE)
3. **claimRewards() safety net** — also accepts IN_PROGRESS if raw progress >= targetValue
4. **startRun() skips reset** — if saved progress already meets target, jumps directly to COMPLETED (avoids AVAILABLE→IN_PROGRESS→0 reset)
5. **getVisibleMissions() includes hidden LOCKED** — was filtering them out entirely ("hidden cards lost" bug)
6. **_missionStates/_progress → mutableStateMapOf** — was plain mutableMapOf, Compose never detected state changes

## Fix Attempt: Claim Flow (Session 2026-06-20, Evening)

**Bug:** Mission reaches 100% progress (bar full / CLAIM pill visible), but clicking CLAIM doesn't transition to CLAIMED.

**3 fixes applied (none sufficient — bug persists):**

1. **`endRun()` async state mutation** — `_missionStates` mutations were inside `scope.launch {}`, so `startRun()` (called synchronously after) saw stale IN_PROGRESS states. Fix: snapshot-based save + sync state mutation outside the launch block.

2. **`startRun()` only handled `AVAILABLE`** — when race caused stale IN_PROGRESS, `startRun()` skipped those missions. Fix: accept `AVAILABLE || IN_PROGRESS`.

3. **`isClaimable` didn't exclude CLAIMED** — `rawProgress >= targetValue` kept CLAIM pill visible after claim. Fix: `state != MissionState.CLAIMED && (...)`.

**Remaining suspects (unconfirmed — next session should investigate first):**

1. **`update()` early return at line 76** — `return mission` exits the function immediately after the first mission completes. Other missions at 100% in the same frame are skipped. If the run ends that frame (e.g., death), they never transition to COMPLETED. Fix: iterate all missions, collect first as `completionEvent`, don't early-return.

2. **Threshold mismatch** — `isClaimable` shows CLAIM at `progress > 0.99f` (99%) but `claimRewards` requires `pct >= 1f` (100%). For near-miss values like 59.7s/60s = 99.5%, CLAIM pill shows but `claimRewards` silently rejects. Fix: relax `claimRewards` to `pct > 0.99f` to match.

3. **No safety net in MissionScreen** — needs a `LaunchedEffect` that force-transitions `claimRewards()` on composition when `rawProgress >= targetValue` and state is stale `IN_PROGRESS`.

**Architectural concern:** All `GameStats` fields are per-run (reset on `restartGame()`). Missions with lifetime targets (e.g. 3600s flight time) are impossible to complete in a single run. This is by design for per-run achievements, but the `update()` function has no accumulation logic — it reads `calculateProgress()` fresh each frame. Missions that need cumulative progress (like FLIGHT_TIME TIER_4) must be completed within a single run.

## UI Design System (From Reference)

Source: `docs/gameplay/stitch_jump_droid_mission_screen.zip`

### Colors
- Surface: `#1A1A2E` at 80% opacity (glass)
- Background: `#0A0A1A` (void)
- Primary: `#2196F3` (blue)
- Tier 1: Green, Tier 2: Cyan, Tier 3: Purple, Tier 4: Gold
- Text: White `#FFFFFF`, secondary: `#9E9E9E`

### Typography
- Sora — display/headlines (bold, wide)
- Hanken Grotesk — body text
- Space Grotesk — labels, data, stats (slightly monospaced)

### Spacing
- Unit: 4px, standard margin: 20px, grid gap: 12px, card padding: 16px

### Glassmorphism
- `background: rgba(26, 26, 46, 0.8)` with `backdrop-filter: blur(16px)`
- 1px inner border with white-to-transparent gradient at 15% opacity
- Pill-shaped badges (ROOKIE, EXPERIENCED, MASTER, GOD)

## Files to Be Aware Of

### Modified on feature/mission-system
- `app/src/main/java/com/example/jump_droid/GameScreen.kt`
- `app/src/main/java/com/example/jump_droid/HudWidgets.kt`
- `app/src/main/java/com/example/jump_droid/MainMenuScreen.kt`
- `app/src/main/java/com/example/jump_droid/Models.kt`
- `app/src/main/java/com/example/jump_droid/ProgressionManager.kt`
- `app/src/main/java/com/example/jump_droid/ActiveThreat.kt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

### Deleted from root (replaced by missions/ package)
- `app/src/main/java/com/example/jump_droid/Mission.kt`
- `app/src/main/java/com/example/jump_droid/MissionManager.kt`
- `app/src/main/java/com/example/jump_droid/MissionRegistry.kt`
- `app/src/main/java/com/example/jump_droid/MissionReward.kt`
- `app/src/main/java/com/example/jump_droid/MissionRow.kt`
- `app/src/main/java/com/example/jump_droid/MissionType.kt`

### New (missions/ package)
- `app/src/main/java/com/example/jump_droid/missions/Mission.kt`
- `app/src/main/java/com/example/jump_droid/missions/MissionManager.kt`
- `app/src/main/java/com/example/jump_droid/missions/MissionRegistry.kt`
- `app/src/main/java/com/example/jump_droid/missions/MissionRepository.kt`
- `app/src/main/java/com/example/jump_droid/missions/ui/MissionScreen.kt`

## Known Issues (by Priority)

1. **[CRITICAL] Claim flow still broken** — 3 fixes applied (endRun sync, startRun accepts IN_PROGRESS, isClaimable excludes CLAIMED) but CLAIM pill still doesn't transition reliably. Suspect: update() early return skips missions, threshold mismatch (99% vs 100%), and no safety net in MissionScreen.
2. **[MEDIUM] Hidden mission unlock feedback missing** — newly unlocked hidden missions lack "SIGNAL RECEIVED" animation
3. **[LOW] No cash wallet** — cash rewards displayed but not persisted
4. **[LOW] MainMenu badge not reactive** — badge state computed from in-memory map, not Compose snapshot
5. **[LOW] Deprecated LinearProgressIndicator** — used in MissionScreen

## Next Steps (Ranked)

1. **[CRITICAL] Debug claim flow further** — (a) Fix `update()` early return: iterate all missions instead of `return mission`. (b) Relax `claimRewards` threshold from `>= 1f` to `> 0.99f`. (c) Add `LaunchedEffect` safety net in MissionScreen to force `claimRewards()` when `rawProgress >= targetValue` and state is `IN_PROGRESS`.
2. **Add SIGNAL RECEIVED overlay** — brief animation for hidden missions that just unlocked
3. **Merge feature/mission-system → development** when claim flow is stable
4. **Implement cash wallet** persistence
5. **Expand glassmorphism** to other screens (Hangar, MainMenu, etc.)

## Build & Test Commands

```bash
git checkout feature/mission-system
./gradlew assembleDebug
git diff development..feature/mission-system --stat   # see full file delta
git log --oneline development..feature/mission-system  # see commit history on branch
```
