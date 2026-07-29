# Tasks — High-Efficiency Terminal & Minimal Firestore Sync

- [x] **Leaderboard Intelligence (LeaderboardManager.kt)**
    - [x] Implement `localRemoteBest` cache in SharedPreferences
    - [x] Add **Write-Squelching** to `submitScore`
    - [x] Replace full-fetch rank logic with **Firestore Count Query**
    - [x] Implement 5-minute memory cache for the pilot list
- [x] **Intelligent Sync Triggers (GameEngine.kt)**
    - [x] Refine `submitScore` calls to only trigger on high-score events
- [x] **Robust Terminal UI (LeaderboardScreen.kt)**
    - [x] Add **"UPLINK STATUS"** diagnostic panel at the top
    - [x] Implement **"LOCAL TELEMETRY"** fallback for offline/empty states
    - [x] Add **"PILOT UPLINK REQUIRED"** CTA for signed-out pilots:
        - *Lingo: "FLEET SYNC UNAVAILABLE // ESTABLISH PILOT ID TO VIEW GLOBAL TELEMETRY"*
    - [x] Implement direct "SYNC IDENTITY" button within the terminal screen
- [x] **Verification**
    - [x] `gradle_build`
    - [x] Verify Firestore usage in logs (minimal calls)
