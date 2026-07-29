# Implementation Plan — High-Efficiency Global Terminal & Intelligence Sync

The goal is to restore the Global Terminal functionality while aggressively minimizing Firestore reads and writes. We will transition from a "Fetch-on-Demand" model to a "Local-First with Cached Remote State" model.

## User Review Required

> [!IMPORTANT]
> I am replacing the expensive "Fetch All to find Rank" logic with a modern Firestore **Count Query**. This reduces thousands of potential reads to a single metadata operation.

> [!TIP]
> I will implement **Write-Squelching**. The app will remember your last successfully submitted score and will only talk to the server if you achieve a new personal best that exceeds that specific remote record.

## Proposed Changes

### 1. High-Efficiency `LeaderboardManager`
Optimize communication patterns to avoid redundant I/O.

#### [MODIFY] [LeaderboardManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/LeaderboardManager.kt)
- **Local Record Cache**: Store the `remoteBestScore` in `SharedPreferences` after a successful submission or fetch.
- **Write-Squelching**: In `submitScore()`, immediately return if `score <= localRemoteBest`. This eliminates the "Read-before-Write" check in 99% of sessions.
- **Optimized Ranking**: Replace the full collection fetch in `getPlayerRank()` with an aggregate `count()` query: `collection("leaderboard").whereGreaterThan("highScore", myScore).count()`.
- **Throttled Reads**: Add a `lastFetchTime` check. Don't fetch the top 50 list more than once every 5 minutes unless manually forced.

### 2. Intelligent Sync Triggering
Refine when the game attempts to communicate with Firebase.

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- Ensure score submission only happens if the *session score* is higher than the *all-time local high score*.

### 3. Robust Terminal UI (Low Data Mode)
Enhance the UI to handle intermittent connectivity and empty states gracefully.

#### [MODIFY] [LeaderboardScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/LeaderboardScreen.kt)
- **Local Fallback**: If offline or fetch fails, immediately show the player's local high score as a single list entry labeled "LOCAL TELEMETRY".
- **Visual Status**: Add a "LINK STATUS" indicator to the top of the terminal (e.g., `UPLINK: ACTIVE` vs `UPLINK: OFFLINE`).
- **Identity Link**: If `idToken` is missing (Blocker #1), show a clear instruction: "PILOT IDENTITY UNVERIFIED — RECONNECT IN SETTINGS".

## Verification Plan

### Automated Tests
- `gradle_build` to verify syntax.

### Manual Verification
1. **Minimal Reads**: Open the Terminal once. Close it. Re-open it. Verify it doesn't show a loading spinner (uses cached list).
2. **Minimal Writes**: Complete a run with a score *lower* than your record. Check logs to verify `submitScore` aborted early without calling Firestore.
3. **Rank Accuracy**: Achieve a high score and verify the "Pilot Position" (#X of Y) updates correctly using the new Count logic.
4. **Offline Handling**: Disable Wi-Fi and verify the Terminal still shows your own score instead of a total lockdown screen.
