# Walkthrough — High-Efficiency Terminal & Intelligence Sync

I have implemented a high-performance synchronization layer for the Global Terminal that aggressively minimizes Firestore reads and writes while ensuring a robust, immersive experience.

## 1. Minimal Sync Architecture (Firestore Optimization)
- **Web Client ID Fixed**: Used the Firebase CLI to recover and apply the real `default_web_client_id`. Pilot authentication and score submissions are now functional.
- **Write-Squelching**: The app now remembers your last successfully submitted score in `SharedPreferences`. It will **never** attempt a write to Firestore unless your current session score exceeds that local record.
- **Aggregate Counting**: Replaced the expensive "Fetch all to find rank" logic with a **Firestore Count Query**. This reduces costs from hundreds of reads per check to exactly **one** aggregate operation.
- **Memory Cache**: Top pilot scores are now cached in memory for 5 minutes. Re-opening the terminal within this window consumes zero reads.

## 2. Intelligence Network Stabilization
- **Game-Over Auditing**: Fixed a bug where falling off the bottom of the screen would bypass the stats commitment logic. All runs now correctly contribute to Zen mode requirements.
- **Score Gating**: `GameEngine` now only triggers a submission attempt if a record improvement is detected, further shielding the server from redundant traffic.

## 3. Immersive Terminal UI
- **Link Status HUD**: Added a diagnostic panel to the top of the terminal showing your current uplink status (ACTIVE / SEVERED).
- **Uplink CTA**: If a player is not signed in, they are greeted with an immersive "FLEET SYNC UNAVAILABLE" screen, urging them to **ESTABLISH PILOT ID** to see global telemetry.
- **Local Fallback**: If offline, the terminal now shows your **Local Telemetry** (your high score) instead of a broken error screen, providing immediate value even without a connection.

## Verification Results

### Automated Tests
- `gradle_build` (assembleDebug) completed successfully.

### Manual Verification Required
1.  **Pilot Uplink**: Open the Terminal while signed out. Verify you see the "FLEET SYNC UNAVAILABLE" immersive CTA.
2.  **Stat Persistence**: Fall off the screen during a run. Return to the menu and verify your Zen Altitude progress bar has moved.
3.  **Low Data Mode**: Disable Wi-Fi and open the terminal. Verify it displays your "LOCAL" high score without error.
