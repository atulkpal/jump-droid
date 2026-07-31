# Multiplayer Development Handover

**Status:** Paused (Parked)
**Date:** 2026-07-30
**Base Version:** v2.2.0

## 1. Overview
Multiplayer in Jump Droid aims to provide real-time, seeded-random vertical races between two players. Development was paused during the connectivity phase (Realtime Database sync).

## 2. Technical Architecture
We use a **Hybrid Networking** approach to maximize performance while minimizing Firebase costs:

*   **Firestore (Lobby)**: Handles Room creation, Joining, and the starting "Handshake". Low-frequency updates.
*   **Firebase Realtime Database (Sync)**: Handles high-frequency rocket telemetry (position, thrust, tilt).
*   **Seeded Random (World)**: Both clients use the same `Int` seed to generate identical platforms and enemies locally. No world data is sent over the network.

## 3. Implemented Components

| File | Purpose |
|------|---------|
| `MultiplayerModels.kt` | Data classes for `MultiplayerRoom`, `PlayerMultiplayerState`, and `GlobalBroadcast`. |
| `MultiplayerManager.kt` | The engine for networking. Includes logic for room lifecycle and RTDB state listeners. |
| `MultiplayerScreen.kt` | The "Multiplayer Hub" UI. Features a Broadcast test and Room hosting/joining interface. |
| `GameState.MULTIPLAYER` | Added to `Models.kt` to handle the new navigation route. |

## 4. Current State & Known Issues
*   **Broadcast UI**: Functional. Pilots can type and send messages.
*   **Lobby System**: Partial implementation. Room codes can be generated.
*   **Issue: "DATABASE STATUS: CONNECTING..."**:
    *   **Description**: The connection listener for Firebase Realtime Database stays in the `connecting` state.
    *   **Suspected Cause**: Likely Firebase Security Rules are set to "Locked Mode" (rejecting all reads/writes) or the `databaseURL` in `google-services.json` does not match the active instance.
    *   **Resolution Path**: Verify Realtime Database rules in the Firebase Console and ensure they allow authenticated (or public for testing) access.

## 5. Next Steps (Phase 2 & 3)
1.  **Connectivity**: Fix the RTDB connection issue.
2.  **Seeded Generation**: Update `PlatformManager.generate()` to accept an external `Random` object.
3.  **Rocket Sync**: Wire `GameEngine` to call `multiplayerManager.syncLocalState()` in the game loop.
4.  **Interpolation**: Implement smoothing for the opponent's rocket to prevent jitter.
5.  **Rendering**: Update `WorldRenderer` to draw the `opponentState` as a holographic sprite.

---
*End of Handover*
