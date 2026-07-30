# Multiplayer Implementation Task List

## Phase 0: Setup & Broadcast Test
- [x] Add Firebase Realtime Database dependencies
- [ ] Create `MultiplayerModels.kt` (Lobby, PlayerState)
- [ ] Implement `MultiplayerManager.kt` with basic Broadcast (Global Ping)
- [ ] Add "MULTIPLAYER" button to Main Menu
- [ ] Create `MultiplayerScreen.kt` with "Broadcast Test" button
- [ ] Verify Broadcast works across two devices

## Phase 1: Matchmaking & Lobby
- [ ] Implement `createRoom()` (Firestore code generation)
- [ ] Implement `joinRoom()` (Firestore lookup)
- [ ] Implement `readyCheck` logic

## Phase 2: World Determinism (Seed Sync)
- [ ] Update `PlatformManager` to support external `Random` seed
- [ ] Update `EncounterDirector` to support external `Random` seed
- [ ] Sync seed during `Room` initialization

## Phase 3: Real-time Rocket Sync
- [ ] Implement high-frequency RTDB sync for local player
- [ ] Implement interpolation for opponent rocket
- [ ] Update `WorldRenderer` to draw opponent holographic rocket
- [ ] Add visual feedback for opponent actions (thrust, damage)

## Phase 4: Competitive Features
- [ ] Altitude leaderboard (side-by-side)
- [ ] "Eliminated" state sync
- [ ] Rematch system
