# Walkthrough - Visual Polish, Stability & Firestore Optimization

I have implemented several UI refinements, fixed critical crashes, and optimized Firestore usage to adhere to the **"Minimum Writes"** strategy.

## Key Changes

### 1. Stability Fixes
- **Firestore Permission Fix**: Bridged Google Sign-In with Firebase Authentication in `LoginManager`. This ensures the app is authorized to write to Firestore, resolving the `PERMISSION_DENIED` crash.
- **Activity Cast Fix**: Replaced unsafe context casts in `GameOverOverlay.kt` with a safe `findActivity()` helper to prevent `ClassCastException` on devices with wrapped contexts (common in Compose Dialogs).

### 2. UI & Reveal Polish
- **Centered Rocket Reveal**: Centered and scaled (2.5x) the rocket preview in the `UnlockOverlay`. This makes new ship unlocks feel more impactful and visually rewarding.
- **Archive Button Pulse**: The "ARC" (Archive) button's red badge now pulses with a scale animation when there are unread entries.

### 3. Firestore "Minimum Writes" Strategy
- **Throttled Sync**: Removed automatic cloud sync on every game over. Data is now synced only during `onPause` (leaving the app) or when entering the Terminal.
- **Hash-Based Write Avoidance**: Added a data-hash check in `CloudSyncManager` to prevent uploading data if it hasn't changed since the last write.
- **Leaderboard Guard**: `LeaderboardManager` now checks the local high score before attempting a remote write.
- **Auth Guard**: All Firestore operations now check `FirebaseAuth.getInstance().currentUser != null` to prevent unauthorized write attempts.

### 4. Economy & Pacing
- **Dynamic Conversion**: The cost to convert Cash to Credits (Tokens) now scales based on bosses defeated (100, 200, 400, 800 JC).
- **Mission Tuning**: Increased thresholds for early missions to ensure a more satisfying progression curve.

### 5. Bug Fixes
- **Mission Unlock Sync**: Added a call to `checkUnlocks()` when entering the Mission screen to ensure newly satisfied conditions are updated immediately.
- **Near-Death Logic**: Fixed the mission logic to correctly track the 10% health threshold and added gameplay guards.

## Technical Details
- **Procedural Rendering**: Used `scale` and `pivot` in `UnlockOverlay.kt` for high-fidelity ship reveals.
- **Lifecycle Management**: Utilized `onPause` in `MainActivity` for reliable, low-frequency data persistence.
