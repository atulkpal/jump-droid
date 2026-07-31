# Changelog

All notable changes to this project are recorded as dated engineering events.

---

## 2026-07-31 (Dynamic Monetization & Notification Hardening)

**Version:** v2.2.4 — Elite Urgency & Global Delivery

**Status:** Implementation Complete — branch `development`

### Added — Native "Elite" Sales & Urgency
- **Dynamic Best-Offer Selection**: Updated `PurchaseManager.kt` to iterate through all available Google Play offers and automatically select the lowest price (e.g., ₹160 vs standard ₹200).
- **Subtle Urgency Mode**: Implemented a smart countdown timer that appears only when a sale is ending in less than 3 days. Displays contextual labels like "ENDS IN 2 DAYS" or "ENDING SOON" to drive conversion.
- **Native Discount Detection**: Integrated with `discountDisplayInfo` to extract official percentage labels (e.g., "20% OFF") directly from Play Store metadata, with a fallback calculation engine.

### Improved — Unified "Elite Upgrade" Flow
- **Universal Dialog**: Created a shared `EliteUpgradeDialog` and `EliteBenefitsList` in `EliteComponents.kt` to ensure 100% UI consistency across all purchase touchpoints.
- **Functional CTA Overhaul**: Wired the "GO PREMIUM" button in the System Protocol (About) screen and made the premium hints on the Game Over screen clickable.
- **Fixed Footer Layout**: Redesigned the Settings screen with a fixed bottom container for the Return button and Ad Banner, ensuring they are always accessible regardless of scroll position.

### Changed — Background Notification Recovery
- **Runtime Permissions**: Implemented a mandatory `POST_NOTIFICATIONS` request flow for Android 13+ devices to resolve the "missing notification" issue.
- **Global Topic Subscription**: Automatically subscribes all pilots to the `"all"` FCM topic on launch, enabling reliable broadcast updates from the Firebase Console.
- **Diagnostic Logging**: Added explicit FCM token and subscription status logging to Logcat for server-side verification.

### Fixed — Engine Safety & Rendering Stability
- **RadialGradient Crash Fix** (`IllegalArgumentException`): Implemented a three-tier safety system to prevent `NaN` values from reaching the native Skia shader engine.
    - **Engine Layer**: Guarded player steering physics against division by zero when `screenWidth` is 0.
    - **AI Layer**: Added non-finite value filters in `ThreatAIUpdater.kt` to halt `NaN` propagation.
    - **Renderer Layer**: Added defensive checks in `ScoutDroneRenderer.kt` before calling `Brush.radialGradient`.
- **Physics Stability**: Clamped `dt` to `(0..0.033f)` to ensure stable calculations even during frame spikes or cold starts.

---

## 2026-07-30 (Zen Mastery & Impossible Uplink)

**Version:** v2.2.3 — Zen Isolation & Elite Protocol

**Status:** Implementation Complete — branch `feature/zen-mp-overhaul`

### Added — Zen Mode "Peaceful Glide" Hardening
- **Absolute Suppression**: Implemented redundant guards in `EncounterDirector.kt` to ensure 100% suppression of Bosses, Mini-Bosses, and Reinforcements during Zen runs.
- **Silent Protocol**: Updated `GameEngine.kt` to skip all mission progress, achievement popups, and rank ceremonies in Zen mode. Discoveries are now archived silently.
- **One-Life Death Flow**: Overhauled `GameOverOverlay.kt` for Zen mode to remove all continue/revive/credit logic. Death is now final, correctly reflecting the "One Life, One Glide" philosophy.

### Improved — Zen Music Archives
- **Album Grouping**: Grouped 12 repetitively-named tracks into 6 thematic "Albums" (Planetary, Stratospheric, Industrial, Temporal, Ancient, Singularity) for a cleaner selection menu.
- **Motivational Footer**: Added a smart CTA in the music menu: "KEEP EXPLORING HIGHER TO UNLOCK MORE MUSIC!", appearing only when the collection is incomplete.
- **Enhanced Collection**: Added "FLEET COMMAND" (Menu Theme) and "CRITICAL THREAT" (Boss Theme) as permanent options in the Zen list.
- **Visual Feedback**: The active music selection is now highlighted in SciFiCyan for immediate recognition.

### Changed — Multiplayer "Uplink" Protocol
- **Elite Unlock Criteria**: Set a temporary "Impossible" threshold for Multiplayer access: **3 unique runs with a 50x Combo**. This acts as a skills-based blocker while VS systems are in development.
- **Stat Tracking**: Added `lifetimeCombosOver50` to `StatRecorder.kt` to track these elite skill runs.

### Fixed — Engine & UX
- **State Race Condition**: Refactored `GameEngine.restartGame()` to ensure a full state wipe (clearing platforms and stats) happens instantly, even before screen layout is valid, preventing "leaked" state from previous runs.
- **Pause/Help Restoration**: Fixed a bug where Pause and Help buttons were non-functional during Zen mode.
- **Universal Back-Pause**: Implemented a global `BackHandler` so the system back button now correctly triggers the pause menu during any active flight (Standard or Zen).


## 2026-07-30 (Intelligence Integrity & Terminal Mastery)

**Version:** v2.2.2 — Intelligence Integrity & Tactical Polish

**Status:** Implementation Complete — branch `bugfix/telemetry-integrity-overhaul`

### Added — Statistical Integrity & Migration
- **Self-Correction Engine**: Updated `StatRecorder.kt` with an intelligence layer that detects and fixes data inconsistencies (e.g., Total Distance < Best Ascent). Migrates legacy data to ensure cumulative stats reflect actual flight history.
- **Expedition Tracker**: Activated the `totalRuns` metric across the persistence and cloud-sync layers to track every unique pilot sortie.

### Improved — Fleet Terminal Overhaul
- **Score vs. Ascent Separation**: Overhauled the Telemetry Grid to explicitly distinguish between **BEST ASCENT** (physical height record) and **MAX SCORE** (pilot mastery record).
- **Metric Expansion**: Added **TOTAL DISTANCE**, **EXPEDITIONS**, **MAX COMBO**, **ARTIFACTS**, **CASH EARNED**, and **PERFECT LANDINGS** to the core dashboard.
- **Log Precision**: Updated the Historical Expedition Log to **PILOT MASTERY** and removed misleading meter units from skill-based scores.
- **Hierarchy Refinement**: Repositioned the **Boss Discovery Progress** bar above the Threat Log for a more logical top-down intelligence flow.
- **Vertical Balance**: Restored ergonomic component scaling (56dp cards) and re-enabled vertical scrolling to maintain a premium, balanced layout on all device sizes.

### Changed — Settings Optimization & Security
- **Clean Command**: Removed the defunct "Render Mode" toggle from system settings.
- **Debug Hardening**: Wrapped the "Trigger Test Notification" button in a `BuildConfig.DEBUG` guard to prevent production exposure while maintaining developer testing tools.

## 2026-07-30 (Polish & Analytics)

**Version:** v2.2.1 — Tactical Terminal & Boss Intelligence

**Status:** Implementation Complete — branch `bugfix/zen-terminal-polish`

### Added — High-Fidelity Pilot Command Center
- **2x4 Telemetry Grid**: Overhauled the Local Terminal into a dense tactical dashboard tracking 8 skill-based metrics (Altitude, Combo, Bosses, Escapes, Continues, Cash, Perfect Landings, Death-Defying).
- **Terminal Vitality**: Injected "life" into the UI with staggered card boot-up animations and a real-time vertical radar sweep effect.
- **Threat Neutralization Log**: Implemented a filtered intelligence table that exclusively lists confirmed boss encounters (Slayed/Escaped/Defeated By), hiding "CLASSIFIED" data.
- **Discovery Progress**: Added a glowing scan-particle animation to the threat database progress bar.

### Added — Vector-Scope CRT Console
- **Tactical Monitor UI**: Replaced the color-cycling Zen box with a monochrome CRT visualizer featuring scanlines, flickering, and a real-time vector waveform.
- **Stable State Protocol**: Assigned meaningful, stable colors to protocol stages: Green (Secure), Amber (Calibrating), Purple (Zen Authorized), Cyan (Uplink Protocol Established).

### Improved — Boss Interaction Logic
- **Tactical Defeat Attribution**: Implemented presence-based detection. If a player fails (off-screen or damage) while a Boss is active, the boss now correctly "claims" the defeat in the permanent pilot record.
- **Granular Escape Tracking**: Added individual ID-based escape counters to distinguish between bosses neutralised and those that successfully retreated.

### Changed — "Archive Intelligence" Reward Flow
- **The Droid Way**: Renamed the reward claim action to "ARCHIVE INTELLIGENCE" for thematic consistency.
- **Reward Badges**: Integrated high-visibility value badges (e.g., "+500 JC") directly onto reward cards to clarify exactly what data is being synced.
- **Buffer Flush Animation**: Polished the dissolution animation with a "SYNCING..." tactical overlay.

### Fixed — UX & Navigation Safety
- **Overlay State Lockdown**: Added `DisposableEffect` handlers to the `pause` and `help` dialog routes to ensure the game automatically resumes if the overlay is dismissed via system interaction (Back button/System gestures).
- **HUD Clutter Reduction**: Removed the redundant `?` button from the top-left HUD; information is now consolidated in the Pause menu.

---

## 2026-07-29 (Feature & Fix)

**Version:** v2.2.0 — Fleet Terminal & Hangar Ascension

**Status:** Implementation Complete — branch `feature/remote-announcements`

### Added — High-Efficiency Fleet Terminal
- **Firestore Aggregate Sync**: Replaced full-collection reads with `count()` aggregate queries for global ranking, reducing read costs by 99%.
- **Write-Squelching**: Implemented local best-score caching to prevent redundant Firestore writes unless a new global record is achieved.
- **Uplink HUD**: Added a live connection status panel to the terminal (ACTIVE / SEVERED) and an immersive CTA for unlinked pilots.

### Added — Hangar "Full Glory" Restoration
- **Pentagon Stat Chart**: Re-integrated the high-fidelity radar chart with dynamic 5-axis telemetry (Thrust, Fuel, Thermal, Hull, Steering).
- **Consolidated Console**: Integrated the Chassis Variant selector directly into the Performance Analytics panel for a unified Command Console experience.
- **Ergonomic Layout**: Removed vertical scrolling by optimizing panel density, fitting the full hangar interface onto a single screen.

### Improved — Zen Mode Immersion
- **Secure Terminal UI**: Overhauled the locked Zen state with immersive "Binary Rain" backgrounds, flickering decryption telemetry, and "Unauthorized Pilot" alerts.
- **Intelligence Network Fixes**: Fixed a critical bug where falling off the screen bypassed lifetime stat commitment. All game-over states now correctly contribute to Zen unlock progress.

### Fixed — Mission Reward Stability
- **Hardened Flick Gestures**: Replaced legacy pointer loop with `VelocityTracker` and `detectHorizontalDragGestures` in the Reward Card deck. Resolved "breaking" flick bug by implementing speed-sensitive dismissal logic and physics-based throw animations.
- **Card Flow Lockdown**: Fixed intermittent UI freezes on repeated runs by ensuring proper state reset between card transitions.

---

## 2026-07-29 (Feature)

**Version:** v2.1.3 — Remote Intelligence Protocol

**Status:** Implementation Complete — branch `feature/remote-announcements`

### Added — Remote Announcement System
- **Firestore Intelligence Feed**: Implemented a real-time listener for the `server_config/remote_config` document. Allows for dynamic, code-free news and event updates.
- **Announcement Persistence**: Integrated local state tracking to ensure "One-Time Read" behavior for remote messages, preventing HUD clutter.
- **Dynamic HUD Priority**: Announcements respect the `NotificationPriority` engine, allowing for `CRITICAL` (preemptive) or `TACTICAL` (queued) remote alerts.

### Changed — Background Connectivity
- **FCM Hardening**: Upgraded `JumpDroidFirebaseMessagingService` with `IMPORTANCE_HIGH` channels and cyan-glow visual alerts to ensure mission-critical push notifications reach pilots on all modern Android versions.

---

## 2026-07-29 (Hotfix)

**Version:** v2.1.2 — Startup Stability & Meta Polish

**Status:** Implementation Complete — branch `bugfix/workmanager-crash`

### Fixed — Critical Startup Crash
- **R8 Proguard Lockdown**: Fixed a fatal `RuntimeException` where WorkManager's Room database implementation was being stripped in release builds. Added explicit `-keep` rules for `androidx.room` and `androidx.work` to ensure production stability.

### Fixed — Zen Mode Persistence
- **Lifetime Stat Sync**: Fixed the `StatRecorder` failure to persist cumulative altitude and max combo across sessions. Zen Mode now correctly tracks lifetime progress toward the 10,000m and 50x combo goals.

### Added — Post-Expedition Summary
- **Session Summary Screen**: Replaced the empty state after reward collection with a comprehensive performance debrief.
- **Lore Sync Status**: Added a visual progress bar tracking total Codex/Archive completion percentage (0-100%).
- **Calibration HUD**: Added a Zen Mode calibration bar to the summary screen to show real-time progress toward the next unlock while Zen Mode remains locked.

---

## 2026-07-29

**Version:** v2.1.1 — Tactical Polish Update

**Status:** Implementation Complete — branch `master`

### Added — Zen Mode Protocol
- **Zen Command Console**: Replaced the sloppy button with a high-fidelity holographic terminal. Features detailed decryption progress bars for Altitude, Bosses, and Combo requirements.
- **Endless Atmosphere**: A pressure-free gameplay mode with no threats or bosses, focused on exploration and music.
- **Music Selector**: Floating UI in Zen Mode allowing players to toggle between all unlocked zone tracks and boss BGM.

### Added — High-Impact Reward Flow
- **Stacked Card Deck**: Replaced the static horizontal list with a Tinder-style stacked deck for rewards.
- **Swipe-to-Sync**: Implemented satisfying "flick" physics for claiming expedition data.
- **Currency HUD**: Added a top-bar HUD to the Game Over screen to provide a visual destination for flying reward icons.
- **Expedition Recovery Modal**: Separated rewards into a dedicated modal to eliminate scrolling and focus on narrative payoff.

### Fixed — Mission Architecture & Polish
- **Operation Debriefs**: Wrote 48 unique narrative debriefs that appear upon mission completion, tying actions into the core lore.
- **Insignia Watermarks**: Added high-opacity background insignias to mission cards for a premium "Certified" look.
- **Category Restoration**: Fixed the "Ghost Claimable" bug by exposing the missing `OVERHEAT` and `LANDINGS` categories in the Mission Log.

### Changed — Production Hardening
- **Feature Lockdown**: Sealed the unfinished Cosmetics and Leaderboard (TRM) sections behind high-fidelity maintenance protocols ("SYSTEM UPGRADE" / "TERMINAL OFFLINE").
- **Menu Vitality**: The ARC station icon now performs a tactical nudge wiggle when unread logs are detected. Badge counts are now perfectly centered in their pulse balls.
- **Gameplay Variance**: Randomized initial platform direction and introduced rare "Wrap-around" teleporting platforms.

---

## 2026-07-28

**Version:** v2.0.0 — Production Polish Finalized

**Status:** Implementation Complete — branch `master`

### Added — High-Fidelity UI Action
- **HUD "Thud" Fly-in**: Achievement and mission cards now slide in from the left with a physical **Overshoot** animation, making unlocks feel heavy and earned.
- **Category Visual Flourishes**: Added neon flourishes to HUD cards: Red "Heat" glow for Missions and Cyan "Digital Glitch" lines for Signals/Discoveries.
- **Main Menu Vitality**: Added expanding neon **Scan Rings** from the central rocket and 15 layers of drifting space debris/particles for a cinematic background.
- **Personal Best Glow**: Implemented a pulsating gold "New Record" glow on the Game Over screen when breaking high scores.

### Changed — Connectivity & Economy
- **Non-Intrusive GPG Login**: Restricted interactive Google Play Games sign-in popups to manual triggers only (via "CONNECT PILOT ID"). Silent sign-in remains active for connected players.
- **Play Billing Hardening**: Implemented connection retry logic and comprehensive `BillingResponseCode` logging to handle flaky network conditions during purchases.
- **Steeper Credit Economy**: Rebalanced Continue Credit costs to a tiered model (250, 500, 1000, 2000 JC) based on boss kills, ensuring credits remain a valuable mid-to-late game resource.
- **Shop Grid Refactor**: Cosmetic items (Trails/Paint) are now arranged in a responsive **2-column grid** using `FlowRow`, reducing vertical scrolling by 50%.

### Fixed — Layout & Branding
- **About Protocol Overlay**: Restructured the "Technical Data" section from a `Box` to a `Column`, resolving the overlapping text bug.
- **GameOver Button Clipping**: Added `verticalScroll` to the Game Over screen to ensure the "Return to Base" button is reachable on smaller devices.
- **Branding Reinforcement**: Strengthened "Powered by Ashwath AI" branding across Title, Menu, About, and Game Over footers.
- **Community Removal**: Removed the placeholder "Community" link from the About screen.

---

## 2026-07-27

**Version:** v2.0.0 — Production Ascension

**Status:** Implementation Complete — branch `master` (Production Release)

### Added — "Living World" Earth Overhaul
- **High-Fidelity Backdrop**: Re-engineered the Earth zone with a vibrant "Morning City" aesthetic.
- **Ice-Capped Mountains**: Distant peaks now feature procedural snow-caps with sunlight shading.
- **Dynamic Entities**: Added a slow-moving aircraft silhouette and pulsating aviation lights to the skyline.
- **Grounded Traffic**: Implemented a road layer with moving vehicles, headlight blooms, and lane markers.
- **Rural Detail**: Dotted the rolling hills with tiny homestead silhouettes for a "lived-in" feel.

### Added — HUD Immersion: The Flight Log
- **Non-Blocking Flow**: Achievements, missions, and module unlocks no longer pause gameplay. They now trigger tactical notifications and "settle" into a pulsating deck on the HUD.
- **Post-Run Summary**: Added a scrollable "Expedition Data" row to the Game Over screen for review.
- **Claim Flow**: Unified mission reward collection into a single "Claim All" action at the end of a run.
- **Tension Markers**: Progress bars now only appear at 95% completion to maintain high-stakes focus.

### Changed — Performance & Geometry Fixes
- **Global Parallax Fix**: Resolved project-wide bug where background elements (stars, clouds, dust) moved in reverse. All layers now correctly drift downward during ascent.
- **Path Caching**: Implemented geometric caching for mountain and hill silhouettes, eliminating per-frame vertex calculations.
- **Point Caching**: Star and particle positions are now pre-calculated to reduce Random churn.
- **Quality Guardrails**: Added `QUALITY_LOW_END` toggle to disable expensive window lights and blooms on struggling hardware.

### Added — Fleet Mastery & Rank Overhaul (Phase 10)
- **Visible Mastery Points (MP)**: Surfaced account-level progression points (MP) across all major screens. Formula: `MP = Discoveries + (Artifacts × 3) + (Zones × 5)`.
- **Procedural Rank Insignias**: Implemented 5 distinct, geometric rank medallions (Triangle to Stellar Nova) drawn via Compose Canvas with dynamic rotation and glow effects.
- **Mastery Readout**: Added a "Mastery Data" card to the Archive screen to explain the scoring system and next-rank thresholds.
- **Rank Recalibration**: Increased Rank V requirement to 500 MP to ensure long-term prestige.

### Added — Online Auth & Stability
- **Google Play Billing Upgrade**: Successfully migrated from PBL v7.1.0 to **v9.1.0**, implementing the latest `PendingPurchasesParams` API and enabling **Automatic Service Reconnection** for robust transaction handling.
- **Firebase Auth Integration**: Bridged Google Sign-In with Firebase Authentication to authorize Firestore writes, resolving the `PERMISSION_DENIED` leaderboard crash.
- **Global Terminal Reporting**: Successfully restored score reporting to the Global Terminal; scores now correctly appear for signed-in pilots.
- **Safe Activity Lookup**: Implemented a recursive `findActivity()` helper to resolve `ClassCastException` in ad flows on devices using wrapped contexts (e.g., Pixel 9 Pro).
- **Throttled Firestore Strategy**: Implemented "Minimum Writes" strategy. Data only syncs to cloud on `onPause` or Terminal engagement. Added hash-based write avoidance to `CloudSyncManager`.

### Added — Robust Reset Protocols
- **Reset Progress (Soft)**: Now correctly clears all gameplay data while **preserving** Premium/Ads-Removed status.
- **Factory Reset (Hard)**: Performs a total wipe of all 6 preference files, kills the login session, and returns the app to a "Day 1" state.
- **Persistence Fixes**: Resolved bug where Mastery Points would "stick" after a reset by explicitly clearing the `ArtifactManager` cache.

### Changed — Gameplay Pacing & Economy
- **Dynamic Conversion**: Tiered the Cash-to-Credits exchange price (100, 200, 400, 800 JC) based on lifetime bosses defeated.
- **Rocket Unlock Pacing**: Scaled unlock thresholds (Scout 5k, Tank 15k, Experimental 40k) to prevent premature fleet completion.
- **Mission Tuning**: Doubled early mission targets to prevent the "reward burst" at the start of new profiles.
- **Centered Reveals**: Centered and scaled the rocket preview (2.5x) in the `UnlockOverlay` for high-impact reveals.

### Fixed
- **Near-Miss Logic**: Fixed mission `id` check and ensured it only triggers during active gameplay at < 10% health.
- **Missing INF Button**: Restored the "About" button to the station tray which was accidentally removed in the previous layout pass.
- **Ground-Level Triggers**: Added a 10-meter altitude guard to all mission and achievement updates to prevent triggers while standing on the ground.

---

## 2026-07-26

**Version:** v1.5.3 — Dynamic Scoring & Cyber-Packet Visuals

**Status:** Implementation Complete — branch `feature/fleet-ascension-visual-overhaul`

### Added — Dynamic Scoring Engine
- **Decoupled Progression**: Separated physical altitude (`runAltitude` in meters) from performance score (`score` in points). Physical distance now correctly drives zone transitions and boss spawns, while score reflects skill mastery.
- **Boss Mastery Awards**: Implemented unique, data-driven score awards for all 12 bosses (250 pts for mini-bosses up to 5,000 pts for the Singularity).
- **Exploration Bonuses**: Granted +10 points for every unique platform landed on during a run.
- **Combo Score Injection**: Integrated combo streaks directly into the total score with per-step bonuses and major milestone awards (+100 pts every 5 steps).

### Added — Cyber-Packet Visual Identity
- **Flying Score Packets**: Scoring events now manifest as physical "Cyber-Packets" `[ +1000 ]` that fly from the event source to the HUD.
- **Neon Palette**: Implemented high-contrast neon colors for different score sources: Matrix Green (Platforms), Plasma Gold (Bosses), and Hyper Purple (Combos).
- **Rolling Counter**: HUD score display now rapidly "rolls" up as packets impact, accompanied by scale-pulse effects and localized particle blasts.
- **Visual Polish**: Added ghost trail frames and exponential motion curves to flying packets for a futuristic feel.

### Changed — Performance & Optimization
- **Staggered Explosions**: Distributed boss death particle bursts over 200ms to eliminate frame skips on emulator devices.
- **Overdraw Reduction**: Capped impact flash intensity to 0.7f to reduce GPU pressure.
- **Data Integrity**: Refactored `MissionManager` and `UnlockEngine` to use physical altitude (meters) for distance-based milestones, preventing "skill teleportation" through mission tracks.

### Fixed
- **Missing Boss Scores**: Resolved root cause where boss destruction never triggered the `onScoreUpdate` callback.
- **Persistence Ambiguity**: Added `highAltitude` (meters) to SharedPreferences to track lifetime best distance separately from `highScore` (points).

---

## 2026-07-28

**Version:** v2.0.0 — Production Polish Finalized

**Status:** Implementation Complete — branch `master`

### Added — High-Fidelity UI Action
- **HUD "Thud" Fly-in**: Achievement and mission cards now slide in from the left with a physical **Overshoot** animation, making unlocks feel heavy and earned.
- **Category Visual Flourishes**: Added neon flourishes to HUD cards: Red "Heat" glow for Missions and Cyan "Digital Glitch" lines for Signals/Discoveries.
- **Main Menu Vitality**: Added expanding neon **Scan Rings** from the central rocket and 15 layers of drifting space debris/particles for a cinematic background.
- **Personal Best Glow**: Implemented a pulsating gold "New Record" glow on the Game Over screen when breaking high scores.

### Changed — Connectivity & Economy
- **Non-Intrusive GPG Login**: Restricted interactive Google Play Games sign-in popups to manual triggers only (via "CONNECT PILOT ID"). Silent sign-in remains active for connected players.
- **Play Billing Hardening**: Implemented connection retry logic and comprehensive `BillingResponseCode` logging to handle flaky network conditions during purchases.
- **Steeper Credit Economy**: Rebalanced Continue Credit costs to a tiered model (250, 500, 1000, 2000 JC) based on boss kills, ensuring credits remain a valuable mid-to-late game resource.
- **Shop Grid Refactor**: Cosmetic items (Trails/Paint) are now arranged in a responsive **2-column grid** using `FlowRow`, reducing vertical scrolling by 50%.

### Fixed — Layout & Branding
- **About Protocol Overlay**: Restructured the "Technical Data" section from a `Box` to a `Column`, resolving the overlapping text bug.
- **GameOver Button Clipping**: Added `verticalScroll` to the Game Over screen to ensure the "Return to Base" button is reachable on smaller devices.
- **Branding Reinforcement**: Strengthened "Powered by Ashwath AI" branding across Title, Menu, About, and Game Over footers.
- **Community Removal**: Removed the placeholder "Community" link from the About screen.

---

## 2026-07-27

**Version:** v2.0.0 — Production Ascension

**Status:** Implementation Complete — branch `master` (Production Release)

### Added — "Living World" Earth Overhaul
- **High-Fidelity Backdrop**: Re-engineered the Earth zone with a vibrant "Morning City" aesthetic.
- **Ice-Capped Mountains**: Distant peaks now feature procedural snow-caps with sunlight shading.
- **Dynamic Entities**: Added a slow-moving aircraft silhouette and pulsating aviation lights to the skyline.
- **Grounded Traffic**: Implemented a road layer with moving vehicles, headlight blooms, and lane markers.
- **Rural Detail**: Dotted the rolling hills with tiny homestead silhouettes for a "lived-in" feel.

### Added — HUD Immersion: The Flight Log
- **Non-Blocking Flow**: Achievements, missions, and module unlocks no longer pause gameplay. They now trigger tactical notifications and "settle" into a pulsating deck on the HUD.
- **Post-Run Summary**: Added a scrollable "Expedition Data" row to the Game Over screen for review.
- **Claim Flow**: Unified mission reward collection into a single "Claim All" action at the end of a run.
- **Tension Markers**: Progress bars now only appear at 95% completion to maintain high-stakes focus.

### Changed — Performance & Geometry Fixes
- **Global Parallax Fix**: Resolved project-wide bug where background elements (stars, clouds, dust) moved in reverse. All layers now correctly drift downward during ascent.
- **Path Caching**: Implemented geometric caching for mountain and hill silhouettes, eliminating per-frame vertex calculations.
- **Point Caching**: Star and particle positions are now pre-calculated to reduce Random churn.
- **Quality Guardrails**: Added `QUALITY_LOW_END` toggle to disable expensive window lights and blooms on struggling hardware.

### Added — Fleet Mastery & Rank Overhaul (Phase 10)
- **Visible Mastery Points (MP)**: Surfaced account-level progression points (MP) across all major screens. Formula: `MP = Discoveries + (Artifacts × 3) + (Zones × 5)`.
- **Procedural Rank Insignias**: Implemented 5 distinct, geometric rank medallions (Triangle to Stellar Nova) drawn via Compose Canvas with dynamic rotation and glow effects.
- **Mastery Readout**: Added a "Mastery Data" card to the Archive screen to explain the scoring system and next-rank thresholds.
- **Rank Recalibration**: Increased Rank V requirement to 500 MP to ensure long-term prestige.

### Added — Online Auth & Stability
- **Google Play Billing Upgrade**: Successfully migrated from PBL v7.1.0 to **v9.1.0**, implementing the latest `PendingPurchasesParams` API and enabling **Automatic Service Reconnection** for robust transaction handling.
- **Firebase Auth Integration**: Bridged Google Sign-In with Firebase Authentication to authorize Firestore writes, resolving the `PERMISSION_DENIED` leaderboard crash.
- **Global Terminal Reporting**: Successfully restored score reporting to the Global Terminal; scores now correctly appear for signed-in pilots.
- **Safe Activity Lookup**: Implemented a recursive `findActivity()` helper to resolve `ClassCastException` in ad flows on devices using wrapped contexts (e.g., Pixel 9 Pro).
- **Throttled Firestore Strategy**: Implemented "Minimum Writes" strategy. Data only syncs to cloud on `onPause` or Terminal engagement. Added hash-based write avoidance to `CloudSyncManager`.

### Added — Robust Reset Protocols
- **Reset Progress (Soft)**: Now correctly clears all gameplay data while **preserving** Premium/Ads-Removed status.
- **Factory Reset (Hard)**: Performs a total wipe of all 6 preference files, kills the login session, and returns the app to a "Day 1" state.
- **Persistence Fixes**: Resolved bug where Mastery Points would "stick" after a reset by explicitly clearing the `ArtifactManager` cache.

### Changed — Gameplay Pacing & Economy
- **Dynamic Conversion**: Tiered the Cash-to-Credits exchange price (100, 200, 400, 800 JC) based on lifetime bosses defeated.
- **Rocket Unlock Pacing**: Scaled unlock thresholds (Scout 5k, Tank 15k, Experimental 40k) to prevent premature fleet completion.
- **Mission Tuning**: Doubled early mission targets to prevent the "reward burst" at the start of new profiles.
- **Centered Reveals**: Centered and scaled the rocket preview (2.5x) in the `UnlockOverlay` for high-impact reveals.

### Fixed
- **Near-Miss Logic**: Fixed mission `id` check and ensured it only triggers during active gameplay at < 10% health.
- **Missing INF Button**: Restored the "About" button to the station tray which was accidentally removed in the previous layout pass.
- **Ground-Level Triggers**: Added a 10-meter altitude guard to all mission and achievement updates to prevent triggers while standing on the ground.

---

## 2026-07-26

**Version:** v1.6.0 (Planned) — Navigation Migration & Performance Finalization

**Status:** Implementation Complete — branch `feature/fleet-ascension-visual-overhaul`

### Added — Navigation-Based Overlay System
- **NavHost Overlays**: Successfully migrated 7 interactive overlays (`pause`, `game_over`, `tutorial`, `help`, `unlock`, `continue_ready`, `ascension_credits`) to the centralized `NavHost` as `dialog` routes.
- **Architectural Purity**: Removed all conditional visibility flags for interactive overlays in `GamePlayScreen.kt`.
- **Back-Stack Management**: Standardized back-navigation across all game states, ensuring a consistent user experience.

### Added — Cosmetic Economy (Phase 6)
- **Trail Requisition**: Activated 3 purchasable engine trails (Solar Flare, Void Glitch, Plasma Blue) in the Shop using Jump Credits (JC).
- **Paint Schemes**: Activated 3 purchasable hull coatings (Chrome, Stealth, Prototype-X) in the Shop.
- **Credit Banking**: Implemented JC-to-Credit exchange (100 JC = 1 Continue Credit).
- **Hangar Persistence**: Enhanced `ProgressionManager.kt` and `HangarScreen.kt` to track and display unlocked cosmetics with secure persistence and cloud-sync support.

### Changed — Performance & Rendering (Phase 8)
- **Zero-Allocation Noise**: Pre-calculated white-noise fragments for the `SINGULARITY` zone to eliminate per-frame `Random` and `Size` object churn.
- **Aurora Path Caching**: Implemented geometric caching for Aurora bands in the `UPPER_ATMOSPHERE`, reducing DrawScope overhead.
- **Visual Priority Queue**: Implemented a state-based priority system in `GameEngine.kt` to sequence major events (`Zone Transition` > `Boss Arrival` > `Artifact Lore`) and prevent UI overlap.

### Fixed
- **Firestore Stability**: Resolved a fatal `PERMISSION_DENIED` crash in `RemoteConfigManager` by removing illegal client-side writes to global server configuration.
- **Overlay Hierarchy**: Enhanced `GamePlayScreen` layering logic to automatically suppress background alerts (Boss Arrivals, Zone Transitions) when a blocking modal or dialog is active, resolving the "frozen/chaotic UI" state.
- **Warning Fade Bug**: Fixed root cause where `majorWarningTimer` was never decremented, causing tactical warnings to persist indefinitely.
- **Sign-In Silent Failure**: (Phase 7.1) Resolved `ActivityResult` parsing error and added mandatory `PlayGamesSdk` initialization.

---

## 2026-07-28

**Version:** v2.0.0 — Production Polish Finalized

**Status:** Implementation Complete — branch `master`

### Added — High-Fidelity UI Action
- **HUD "Thud" Fly-in**: Achievement and mission cards now slide in from the left with a physical **Overshoot** animation, making unlocks feel heavy and earned.
- **Category Visual Flourishes**: Added neon flourishes to HUD cards: Red "Heat" glow for Missions and Cyan "Digital Glitch" lines for Signals/Discoveries.
- **Main Menu Vitality**: Added expanding neon **Scan Rings** from the central rocket and 15 layers of drifting space debris/particles for a cinematic background.
- **Personal Best Glow**: Implemented a pulsating gold "New Record" glow on the Game Over screen when breaking high scores.

### Changed — Connectivity & Economy
- **Non-Intrusive GPG Login**: Restricted interactive Google Play Games sign-in popups to manual triggers only (via "CONNECT PILOT ID"). Silent sign-in remains active for connected players.
- **Play Billing Hardening**: Implemented connection retry logic and comprehensive `BillingResponseCode` logging to handle flaky network conditions during purchases.
- **Steeper Credit Economy**: Rebalanced Continue Credit costs to a tiered model (250, 500, 1000, 2000 JC) based on boss kills, ensuring credits remain a valuable mid-to-late game resource.
- **Shop Grid Refactor**: Cosmetic items (Trails/Paint) are now arranged in a responsive **2-column grid** using `FlowRow`, reducing vertical scrolling by 50%.

### Fixed — Layout & Branding
- **About Protocol Overlay**: Restructured the "Technical Data" section from a `Box` to a `Column`, resolving the overlapping text bug.
- **GameOver Button Clipping**: Added `verticalScroll` to the Game Over screen to ensure the "Return to Base" button is reachable on smaller devices.
- **Branding Reinforcement**: Strengthened "Powered by Ashwath AI" branding across Title, Menu, About, and Game Over footers.
- **Community Removal**: Removed the placeholder "Community" link from the About screen.

---

## 2026-07-27

**Version:** v2.0.0 — Production Ascension

**Status:** Implementation Complete — branch `master` (Production Release)

### Added — "Living World" Earth Overhaul
- **High-Fidelity Backdrop**: Re-engineered the Earth zone with a vibrant "Morning City" aesthetic.
- **Ice-Capped Mountains**: Distant peaks now feature procedural snow-caps with sunlight shading.
- **Dynamic Entities**: Added a slow-moving aircraft silhouette and pulsating aviation lights to the skyline.
- **Grounded Traffic**: Implemented a road layer with moving vehicles, headlight blooms, and lane markers.
- **Rural Detail**: Dotted the rolling hills with tiny homestead silhouettes for a "lived-in" feel.

### Added — HUD Immersion: The Flight Log
- **Non-Blocking Flow**: Achievements, missions, and module unlocks no longer pause gameplay. They now trigger tactical notifications and "settle" into a pulsating deck on the HUD.
- **Post-Run Summary**: Added a scrollable "Expedition Data" row to the Game Over screen for review.
- **Claim Flow**: Unified mission reward collection into a single "Claim All" action at the end of a run.
- **Tension Markers**: Progress bars now only appear at 95% completion to maintain high-stakes focus.

### Changed — Performance & Geometry Fixes
- **Global Parallax Fix**: Resolved project-wide bug where background elements (stars, clouds, dust) moved in reverse. All layers now correctly drift downward during ascent.
- **Path Caching**: Implemented geometric caching for mountain and hill silhouettes, eliminating per-frame vertex calculations.
- **Point Caching**: Star and particle positions are now pre-calculated to reduce Random churn.
- **Quality Guardrails**: Added `QUALITY_LOW_END` toggle to disable expensive window lights and blooms on struggling hardware.

### Added — Fleet Mastery & Rank Overhaul (Phase 10)
- **Visible Mastery Points (MP)**: Surfaced account-level progression points (MP) across all major screens. Formula: `MP = Discoveries + (Artifacts × 3) + (Zones × 5)`.
- **Procedural Rank Insignias**: Implemented 5 distinct, geometric rank medallions (Triangle to Stellar Nova) drawn via Compose Canvas with dynamic rotation and glow effects.
- **Mastery Readout**: Added a "Mastery Data" card to the Archive screen to explain the scoring system and next-rank thresholds.
- **Rank Recalibration**: Increased Rank V requirement to 500 MP to ensure long-term prestige.

### Added — Online Auth & Stability
- **Google Play Billing Upgrade**: Successfully migrated from PBL v7.1.0 to **v9.1.0**, implementing the latest `PendingPurchasesParams` API and enabling **Automatic Service Reconnection** for robust transaction handling.
- **Firebase Auth Integration**: Bridged Google Sign-In with Firebase Authentication to authorize Firestore writes, resolving the `PERMISSION_DENIED` leaderboard crash.
- **Global Terminal Reporting**: Successfully restored score reporting to the Global Terminal; scores now correctly appear for signed-in pilots.
- **Safe Activity Lookup**: Implemented a recursive `findActivity()` helper to resolve `ClassCastException` in ad flows on devices using wrapped contexts (e.g., Pixel 9 Pro).
- **Throttled Firestore Strategy**: Implemented "Minimum Writes" strategy. Data only syncs to cloud on `onPause` or Terminal engagement. Added hash-based write avoidance to `CloudSyncManager`.

### Added — Robust Reset Protocols
- **Reset Progress (Soft)**: Now correctly clears all gameplay data while **preserving** Premium/Ads-Removed status.
- **Factory Reset (Hard)**: Performs a total wipe of all 6 preference files, kills the login session, and returns the app to a "Day 1" state.
- **Persistence Fixes**: Resolved bug where Mastery Points would "stick" after a reset by explicitly clearing the `ArtifactManager` cache.

### Changed — Gameplay Pacing & Economy
- **Dynamic Conversion**: Tiered the Cash-to-Credits exchange price (100, 200, 400, 800 JC) based on lifetime bosses defeated.
- **Rocket Unlock Pacing**: Scaled unlock thresholds (Scout 5k, Tank 15k, Experimental 40k) to prevent premature fleet completion.
- **Mission Tuning**: Doubled early mission targets to prevent the "reward burst" at the start of new profiles.
- **Centered Reveals**: Centered and scaled the rocket preview (2.5x) in the `UnlockOverlay` for high-impact reveals.

### Fixed
- **Near-Miss Logic**: Fixed mission `id` check and ensured it only triggers during active gameplay at < 10% health.
- **Missing INF Button**: Restored the "About" button to the station tray which was accidentally removed in the previous layout pass.
- **Ground-Level Triggers**: Added a 10-meter altitude guard to all mission and achievement updates to prevent triggers while standing on the ground.

---

## 2026-07-26

**Version:** v1.5.3 — Dynamic Scoring & Cyber-Packet Visuals

**Status:** Implementation Complete — branch `feature/fleet-ascension-visual-overhaul`

### Added — Dynamic Scoring Engine
- **Decoupled Progression**: Separated physical altitude (`runAltitude` in meters) from performance score (`score` in points). Physical distance now correctly drives zone transitions and boss spawns, while score reflects skill mastery.
- **Boss Mastery Awards**: Implemented unique, data-driven score awards for all 12 bosses (250 pts for mini-bosses up to 5,000 pts for the Singularity).
- **Exploration Bonuses**: Granted +10 points for every unique platform landed on during a run.
- **Combo Score Injection**: Integrated combo streaks directly into the total score with per-step bonuses and major milestone awards (+100 pts every 5 steps).

### Added — Cyber-Packet Visual Identity
- **Flying Score Packets**: Scoring events now manifest as physical "Cyber-Packets" `[ +1000 ]` that fly from the event source to the HUD.
- **Neon Palette**: Implemented high-contrast neon colors for different score sources: Matrix Green (Platforms), Plasma Gold (Bosses), and Hyper Purple (Combos).
- **Rolling Counter**: HUD score display now rapidly "rolls" up as packets impact, accompanied by scale-pulse effects and localized particle blasts.
- **Visual Polish**: Added ghost trail frames and exponential motion curves to flying packets for a futuristic feel.

### Changed — Performance & Optimization
- **Staggered Explosions**: Distributed boss death particle bursts over 200ms to eliminate frame skips on emulator devices.
- **Overdraw Reduction**: Capped impact flash intensity to 0.7f to reduce GPU pressure.
- **Data Integrity**: Refactored `MissionManager` and `UnlockEngine` to use physical altitude (meters) for distance-based milestones, preventing "skill teleportation" through mission tracks.

### Fixed
- **Missing Boss Scores**: Resolved root cause where boss destruction never triggered the `onScoreUpdate` callback.
- **Persistence Ambiguity**: Added `highAltitude` (meters) to SharedPreferences to track lifetime best distance separately from `highScore` (points).

---

## 2026-07-28

**Version:** v2.0.0 — Production Polish Finalized

**Status:** Implementation Complete — branch `master`

### Added — High-Fidelity UI Action
- **HUD "Thud" Fly-in**: Achievement and mission cards now slide in from the left with a physical **Overshoot** animation, making unlocks feel heavy and earned.
- **Category Visual Flourishes**: Added neon flourishes to HUD cards: Red "Heat" glow for Missions and Cyan "Digital Glitch" lines for Signals/Discoveries.
- **Main Menu Vitality**: Added expanding neon **Scan Rings** from the central rocket and 15 layers of drifting space debris/particles for a cinematic background.
- **Personal Best Glow**: Implemented a pulsating gold "New Record" glow on the Game Over screen when breaking high scores.

### Changed — Connectivity & Economy
- **Non-Intrusive GPG Login**: Restricted interactive Google Play Games sign-in popups to manual triggers only (via "CONNECT PILOT ID"). Silent sign-in remains active for connected players.
- **Play Billing Hardening**: Implemented connection retry logic and comprehensive `BillingResponseCode` logging to handle flaky network conditions during purchases.
- **Steeper Credit Economy**: Rebalanced Continue Credit costs to a tiered model (250, 500, 1000, 2000 JC) based on boss kills, ensuring credits remain a valuable mid-to-late game resource.
- **Shop Grid Refactor**: Cosmetic items (Trails/Paint) are now arranged in a responsive **2-column grid** using `FlowRow`, reducing vertical scrolling by 50%.

### Fixed — Layout & Branding
- **About Protocol Overlay**: Restructured the "Technical Data" section from a `Box` to a `Column`, resolving the overlapping text bug.
- **GameOver Button Clipping**: Added `verticalScroll` to the Game Over screen to ensure the "Return to Base" button is reachable on smaller devices.
- **Branding Reinforcement**: Strengthened "Powered by Ashwath AI" branding across Title, Menu, About, and Game Over footers.
- **Community Removal**: Removed the placeholder "Community" link from the About screen.

---

## 2026-07-27

**Version:** v2.0.0 — Production Ascension

**Status:** Implementation Complete — branch `master` (Production Release)

### Added — "Living World" Earth Overhaul
- **High-Fidelity Backdrop**: Re-engineered the Earth zone with a vibrant "Morning City" aesthetic.
- **Ice-Capped Mountains**: Distant peaks now feature procedural snow-caps with sunlight shading.
- **Dynamic Entities**: Added a slow-moving aircraft silhouette and pulsating aviation lights to the skyline.
- **Grounded Traffic**: Implemented a road layer with moving vehicles, headlight blooms, and lane markers.
- **Rural Detail**: Dotted the rolling hills with tiny homestead silhouettes for a "lived-in" feel.

### Added — HUD Immersion: The Flight Log
- **Non-Blocking Flow**: Achievements, missions, and module unlocks no longer pause gameplay. They now trigger tactical notifications and "settle" into a pulsating deck on the HUD.
- **Post-Run Summary**: Added a scrollable "Expedition Data" row to the Game Over screen for review.
- **Claim Flow**: Unified mission reward collection into a single "Claim All" action at the end of a run.
- **Tension Markers**: Progress bars now only appear at 95% completion to maintain high-stakes focus.

### Changed — Performance & Geometry Fixes
- **Global Parallax Fix**: Resolved project-wide bug where background elements (stars, clouds, dust) moved in reverse. All layers now correctly drift downward during ascent.
- **Path Caching**: Implemented geometric caching for mountain and hill silhouettes, eliminating per-frame vertex calculations.
- **Point Caching**: Star and particle positions are now pre-calculated to reduce Random churn.
- **Quality Guardrails**: Added `QUALITY_LOW_END` toggle to disable expensive window lights and blooms on struggling hardware.

### Added — Fleet Mastery & Rank Overhaul (Phase 10)
- **Visible Mastery Points (MP)**: Surfaced account-level progression points (MP) across all major screens. Formula: `MP = Discoveries + (Artifacts × 3) + (Zones × 5)`.
- **Procedural Rank Insignias**: Implemented 5 distinct, geometric rank medallions (Triangle to Stellar Nova) drawn via Compose Canvas with dynamic rotation and glow effects.
- **Mastery Readout**: Added a "Mastery Data" card to the Archive screen to explain the scoring system and next-rank thresholds.
- **Rank Recalibration**: Increased Rank V requirement to 500 MP to ensure long-term prestige.

### Added — Online Auth & Stability
- **Google Play Billing Upgrade**: Successfully migrated from PBL v7.1.0 to **v9.1.0**, implementing the latest `PendingPurchasesParams` API and enabling **Automatic Service Reconnection** for robust transaction handling.
- **Firebase Auth Integration**: Bridged Google Sign-In with Firebase Authentication to authorize Firestore writes, resolving the `PERMISSION_DENIED` leaderboard crash.
- **Global Terminal Reporting**: Successfully restored score reporting to the Global Terminal; scores now correctly appear for signed-in pilots.
- **Safe Activity Lookup**: Implemented a recursive `findActivity()` helper to resolve `ClassCastException` in ad flows on devices using wrapped contexts (e.g., Pixel 9 Pro).
- **Throttled Firestore Strategy**: Implemented "Minimum Writes" strategy. Data only syncs to cloud on `onPause` or Terminal engagement. Added hash-based write avoidance to `CloudSyncManager`.

### Added — Robust Reset Protocols
- **Reset Progress (Soft)**: Now correctly clears all gameplay data while **preserving** Premium/Ads-Removed status.
- **Factory Reset (Hard)**: Performs a total wipe of all 6 preference files, kills the login session, and returns the app to a "Day 1" state.
- **Persistence Fixes**: Resolved bug where Mastery Points would "stick" after a reset by explicitly clearing the `ArtifactManager` cache.

### Changed — Gameplay Pacing & Economy
- **Dynamic Conversion**: Tiered the Cash-to-Credits exchange price (100, 200, 400, 800 JC) based on lifetime bosses defeated.
- **Rocket Unlock Pacing**: Scaled unlock thresholds (Scout 5k, Tank 15k, Experimental 40k) to prevent premature fleet completion.
- **Mission Tuning**: Doubled early mission targets to prevent the "reward burst" at the start of new profiles.
- **Centered Reveals**: Centered and scaled the rocket preview (2.5x) in the `UnlockOverlay` for high-impact reveals.

### Fixed
- **Near-Miss Logic**: Fixed mission `id` check and ensured it only triggers during active gameplay at < 10% health.
- **Missing INF Button**: Restored the "About" button to the station tray which was accidentally removed in the previous layout pass.
- **Ground-Level Triggers**: Added a 10-meter altitude guard to all mission and achievement updates to prevent triggers while standing on the ground.

---

## 2026-07-26

**Version:** v1.5.5 (Planned) — Colorful Vector Integration & HUD Refinement

**Status:** Implementation Complete — branch `feature/fleet-ascension-visual-overhaul`

### Added — Colorful Vector Suite (34 Assets)
- **Integration**: Integrated 34 high-fidelity multi-color vector icons across all UI surfaces (HUD, Menus, Hangar, Shop, Missions).
- **Format Migration**: Successfully migrated the entire UI asset ecosystem from raster (PNG) to **All-Vector (XML)** for infinite scalability and reduced build size.
- **Living HUD**: Implemented state-responsive animations for HUD indicators:
    - **Fuel**: Animated bounce and color-coded shadows.
    - **Heat**: Dynamic flicker and scale-based warning pulses.
    - **Shield**: Rotational sway and status-aware glow.
    - **Hull**: Heartbeat pulse tied to integrity percentage.

### Changed — UI Visual Polish
- **Classy Minimalist HUD**: Reduced HUD icon sizes (13-14dp) and implemented Monospaced Black typography for a technical "digital readout" aesthetic.
- **Station Tray Restoration**: Replaced legacy PNG station tray icons with vibrant, multi-color vectors.
- **Mission Tracks**: Every mission track now features a unique, colorful vector identifier.
- **Hangar Performance**: Replaced procedural performance icons with detailed vector assets.

### Fixed — Visual Regressions & UI Layering
- **Boss Arrival Intensity**: Reduced screen dimming from 60% to 40% and fixed the fade-out logic to reach 0% alpha reliably.
- **GameOver Layer Isolation**: Implemented a "Z-Order Lockdown" in `GamePlayScreen.kt` to suppress all background alerts (Boss Arrival, Zone Discovery, Lore) while the Game Over screen is active.
- **Notification De-duplication**: Scrapped the legacy "Boxed" zone discovery card in favor of the cleaner, minimal underline transition overlay.
- **Continuity Fixes**: Ensured `restartGame()` and `jumpToZone()` properly reset all boss arrival and zone transition states.

---

## 2026-07-28

**Version:** v2.0.0 — Production Polish Finalized

**Status:** Implementation Complete — branch `master`

### Added — High-Fidelity UI Action
- **HUD "Thud" Fly-in**: Achievement and mission cards now slide in from the left with a physical **Overshoot** animation, making unlocks feel heavy and earned.
- **Category Visual Flourishes**: Added neon flourishes to HUD cards: Red "Heat" glow for Missions and Cyan "Digital Glitch" lines for Signals/Discoveries.
- **Main Menu Vitality**: Added expanding neon **Scan Rings** from the central rocket and 15 layers of drifting space debris/particles for a cinematic background.
- **Personal Best Glow**: Implemented a pulsating gold "New Record" glow on the Game Over screen when breaking high scores.

### Changed — Connectivity & Economy
- **Non-Intrusive GPG Login**: Restricted interactive Google Play Games sign-in popups to manual triggers only (via "CONNECT PILOT ID"). Silent sign-in remains active for connected players.
- **Play Billing Hardening**: Implemented connection retry logic and comprehensive `BillingResponseCode` logging to handle flaky network conditions during purchases.
- **Steeper Credit Economy**: Rebalanced Continue Credit costs to a tiered model (250, 500, 1000, 2000 JC) based on boss kills, ensuring credits remain a valuable mid-to-late game resource.
- **Shop Grid Refactor**: Cosmetic items (Trails/Paint) are now arranged in a responsive **2-column grid** using `FlowRow`, reducing vertical scrolling by 50%.

### Fixed — Layout & Branding
- **About Protocol Overlay**: Restructured the "Technical Data" section from a `Box` to a `Column`, resolving the overlapping text bug.
- **GameOver Button Clipping**: Added `verticalScroll` to the Game Over screen to ensure the "Return to Base" button is reachable on smaller devices.
- **Branding Reinforcement**: Strengthened "Powered by Ashwath AI" branding across Title, Menu, About, and Game Over footers.
- **Community Removal**: Removed the placeholder "Community" link from the About screen.

---

## 2026-07-27

**Version:** v2.0.0 — Production Ascension

**Status:** Implementation Complete — branch `master` (Production Release)

### Added — "Living World" Earth Overhaul
- **High-Fidelity Backdrop**: Re-engineered the Earth zone with a vibrant "Morning City" aesthetic.
- **Ice-Capped Mountains**: Distant peaks now feature procedural snow-caps with sunlight shading.
- **Dynamic Entities**: Added a slow-moving aircraft silhouette and pulsating aviation lights to the skyline.
- **Grounded Traffic**: Implemented a road layer with moving vehicles, headlight blooms, and lane markers.
- **Rural Detail**: Dotted the rolling hills with tiny homestead silhouettes for a "lived-in" feel.

### Added — HUD Immersion: The Flight Log
- **Non-Blocking Flow**: Achievements, missions, and module unlocks no longer pause gameplay. They now trigger tactical notifications and "settle" into a pulsating deck on the HUD.
- **Post-Run Summary**: Added a scrollable "Expedition Data" row to the Game Over screen for review.
- **Claim Flow**: Unified mission reward collection into a single "Claim All" action at the end of a run.
- **Tension Markers**: Progress bars now only appear at 95% completion to maintain high-stakes focus.

### Changed — Performance & Geometry Fixes
- **Global Parallax Fix**: Resolved project-wide bug where background elements (stars, clouds, dust) moved in reverse. All layers now correctly drift downward during ascent.
- **Path Caching**: Implemented geometric caching for mountain and hill silhouettes, eliminating per-frame vertex calculations.
- **Point Caching**: Star and particle positions are now pre-calculated to reduce Random churn.
- **Quality Guardrails**: Added `QUALITY_LOW_END` toggle to disable expensive window lights and blooms on struggling hardware.

### Added — Fleet Mastery & Rank Overhaul (Phase 10)
- **Visible Mastery Points (MP)**: Surfaced account-level progression points (MP) across all major screens. Formula: `MP = Discoveries + (Artifacts × 3) + (Zones × 5)`.
- **Procedural Rank Insignias**: Implemented 5 distinct, geometric rank medallions (Triangle to Stellar Nova) drawn via Compose Canvas with dynamic rotation and glow effects.
- **Mastery Readout**: Added a "Mastery Data" card to the Archive screen to explain the scoring system and next-rank thresholds.
- **Rank Recalibration**: Increased Rank V requirement to 500 MP to ensure long-term prestige.

### Added — Online Auth & Stability
- **Google Play Billing Upgrade**: Successfully migrated from PBL v7.1.0 to **v9.1.0**, implementing the latest `PendingPurchasesParams` API and enabling **Automatic Service Reconnection** for robust transaction handling.
- **Firebase Auth Integration**: Bridged Google Sign-In with Firebase Authentication to authorize Firestore writes, resolving the `PERMISSION_DENIED` leaderboard crash.
- **Global Terminal Reporting**: Successfully restored score reporting to the Global Terminal; scores now correctly appear for signed-in pilots.
- **Safe Activity Lookup**: Implemented a recursive `findActivity()` helper to resolve `ClassCastException` in ad flows on devices using wrapped contexts (e.g., Pixel 9 Pro).
- **Throttled Firestore Strategy**: Implemented "Minimum Writes" strategy. Data only syncs to cloud on `onPause` or Terminal engagement. Added hash-based write avoidance to `CloudSyncManager`.

### Added — Robust Reset Protocols
- **Reset Progress (Soft)**: Now correctly clears all gameplay data while **preserving** Premium/Ads-Removed status.
- **Factory Reset (Hard)**: Performs a total wipe of all 6 preference files, kills the login session, and returns the app to a "Day 1" state.
- **Persistence Fixes**: Resolved bug where Mastery Points would "stick" after a reset by explicitly clearing the `ArtifactManager` cache.

### Changed — Gameplay Pacing & Economy
- **Dynamic Conversion**: Tiered the Cash-to-Credits exchange price (100, 200, 400, 800 JC) based on lifetime bosses defeated.
- **Rocket Unlock Pacing**: Scaled unlock thresholds (Scout 5k, Tank 15k, Experimental 40k) to prevent premature fleet completion.
- **Mission Tuning**: Doubled early mission targets to prevent the "reward burst" at the start of new profiles.
- **Centered Reveals**: Centered and scaled the rocket preview (2.5x) in the `UnlockOverlay` for high-impact reveals.

### Fixed
- **Near-Miss Logic**: Fixed mission `id` check and ensured it only triggers during active gameplay at < 10% health.
- **Missing INF Button**: Restored the "About" button to the station tray which was accidentally removed in the previous layout pass.
- **Ground-Level Triggers**: Added a 10-meter altitude guard to all mission and achievement updates to prevent triggers while standing on the ground.

---

## 2026-07-26

**Version:** v1.5.3 — Dynamic Scoring & Cyber-Packet Visuals

**Status:** Implementation Complete — branch `feature/fleet-ascension-visual-overhaul`

### Added — Dynamic Scoring Engine
- **Decoupled Progression**: Separated physical altitude (`runAltitude` in meters) from performance score (`score` in points). Physical distance now correctly drives zone transitions and boss spawns, while score reflects skill mastery.
- **Boss Mastery Awards**: Implemented unique, data-driven score awards for all 12 bosses (250 pts for mini-bosses up to 5,000 pts for the Singularity).
- **Exploration Bonuses**: Granted +10 points for every unique platform landed on during a run.
- **Combo Score Injection**: Integrated combo streaks directly into the total score with per-step bonuses and major milestone awards (+100 pts every 5 steps).

### Added — Cyber-Packet Visual Identity
- **Flying Score Packets**: Scoring events now manifest as physical "Cyber-Packets" `[ +1000 ]` that fly from the event source to the HUD.
- **Neon Palette**: Implemented high-contrast neon colors for different score sources: Matrix Green (Platforms), Plasma Gold (Bosses), and Hyper Purple (Combos).
- **Rolling Counter**: HUD score display now rapidly "rolls" up as packets impact, accompanied by scale-pulse effects and localized particle blasts.
- **Visual Polish**: Added ghost trail frames and exponential motion curves to flying packets for a futuristic feel.

### Changed — Performance & Optimization
- **Staggered Explosions**: Distributed boss death particle bursts over 200ms to eliminate frame skips on emulator devices.
- **Overdraw Reduction**: Capped impact flash intensity to 0.7f to reduce GPU pressure.
- **Data Integrity**: Refactored `MissionManager` and `UnlockEngine` to use physical altitude (meters) for distance-based milestones, preventing "skill teleportation" through mission tracks.

### Fixed
- **Missing Boss Scores**: Resolved root cause where boss destruction never triggered the `onScoreUpdate` callback.
- **Persistence Ambiguity**: Added `highAltitude` (meters) to SharedPreferences to track lifetime best distance separately from `highScore` (points).

---

## 2026-07-28

**Version:** v2.0.0 — Production Polish Finalized

**Status:** Implementation Complete — branch `master`

### Added — High-Fidelity UI Action
- **HUD "Thud" Fly-in**: Achievement and mission cards now slide in from the left with a physical **Overshoot** animation, making unlocks feel heavy and earned.
- **Category Visual Flourishes**: Added neon flourishes to HUD cards: Red "Heat" glow for Missions and Cyan "Digital Glitch" lines for Signals/Discoveries.
- **Main Menu Vitality**: Added expanding neon **Scan Rings** from the central rocket and 15 layers of drifting space debris/particles for a cinematic background.
- **Personal Best Glow**: Implemented a pulsating gold "New Record" glow on the Game Over screen when breaking high scores.

### Changed — Connectivity & Economy
- **Non-Intrusive GPG Login**: Restricted interactive Google Play Games sign-in popups to manual triggers only (via "CONNECT PILOT ID"). Silent sign-in remains active for connected players.
- **Play Billing Hardening**: Implemented connection retry logic and comprehensive `BillingResponseCode` logging to handle flaky network conditions during purchases.
- **Steeper Credit Economy**: Rebalanced Continue Credit costs to a tiered model (250, 500, 1000, 2000 JC) based on boss kills, ensuring credits remain a valuable mid-to-late game resource.
- **Shop Grid Refactor**: Cosmetic items (Trails/Paint) are now arranged in a responsive **2-column grid** using `FlowRow`, reducing vertical scrolling by 50%.

### Fixed — Layout & Branding
- **About Protocol Overlay**: Restructured the "Technical Data" section from a `Box` to a `Column`, resolving the overlapping text bug.
- **GameOver Button Clipping**: Added `verticalScroll` to the Game Over screen to ensure the "Return to Base" button is reachable on smaller devices.
- **Branding Reinforcement**: Strengthened "Powered by Ashwath AI" branding across Title, Menu, About, and Game Over footers.
- **Community Removal**: Removed the placeholder "Community" link from the About screen.

---

## 2026-07-27

**Version:** v2.0.0 — Production Ascension

**Status:** Implementation Complete — branch `master` (Production Release)

### Added — "Living World" Earth Overhaul
- **High-Fidelity Backdrop**: Re-engineered the Earth zone with a vibrant "Morning City" aesthetic.
- **Ice-Capped Mountains**: Distant peaks now feature procedural snow-caps with sunlight shading.
- **Dynamic Entities**: Added a slow-moving aircraft silhouette and pulsating aviation lights to the skyline.
- **Grounded Traffic**: Implemented a road layer with moving vehicles, headlight blooms, and lane markers.
- **Rural Detail**: Dotted the rolling hills with tiny homestead silhouettes for a "lived-in" feel.

### Added — HUD Immersion: The Flight Log
- **Non-Blocking Flow**: Achievements, missions, and module unlocks no longer pause gameplay. They now trigger tactical notifications and "settle" into a pulsating deck on the HUD.
- **Post-Run Summary**: Added a scrollable "Expedition Data" row to the Game Over screen for review.
- **Claim Flow**: Unified mission reward collection into a single "Claim All" action at the end of a run.
- **Tension Markers**: Progress bars now only appear at 95% completion to maintain high-stakes focus.

### Changed — Performance & Geometry Fixes
- **Global Parallax Fix**: Resolved project-wide bug where background elements (stars, clouds, dust) moved in reverse. All layers now correctly drift downward during ascent.
- **Path Caching**: Implemented geometric caching for mountain and hill silhouettes, eliminating per-frame vertex calculations.
- **Point Caching**: Star and particle positions are now pre-calculated to reduce Random churn.
- **Quality Guardrails**: Added `QUALITY_LOW_END` toggle to disable expensive window lights and blooms on struggling hardware.

### Added — Fleet Mastery & Rank Overhaul (Phase 10)
- **Visible Mastery Points (MP)**: Surfaced account-level progression points (MP) across all major screens. Formula: `MP = Discoveries + (Artifacts × 3) + (Zones × 5)`.
- **Procedural Rank Insignias**: Implemented 5 distinct, geometric rank medallions (Triangle to Stellar Nova) drawn via Compose Canvas with dynamic rotation and glow effects.
- **Mastery Readout**: Added a "Mastery Data" card to the Archive screen to explain the scoring system and next-rank thresholds.
- **Rank Recalibration**: Increased Rank V requirement to 500 MP to ensure long-term prestige.

### Added — Online Auth & Stability
- **Google Play Billing Upgrade**: Successfully migrated from PBL v7.1.0 to **v9.1.0**, implementing the latest `PendingPurchasesParams` API and enabling **Automatic Service Reconnection** for robust transaction handling.
- **Firebase Auth Integration**: Bridged Google Sign-In with Firebase Authentication to authorize Firestore writes, resolving the `PERMISSION_DENIED` leaderboard crash.
- **Global Terminal Reporting**: Successfully restored score reporting to the Global Terminal; scores now correctly appear for signed-in pilots.
- **Safe Activity Lookup**: Implemented a recursive `findActivity()` helper to resolve `ClassCastException` in ad flows on devices using wrapped contexts (e.g., Pixel 9 Pro).
- **Throttled Firestore Strategy**: Implemented "Minimum Writes" strategy. Data only syncs to cloud on `onPause` or Terminal engagement. Added hash-based write avoidance to `CloudSyncManager`.

### Added — Robust Reset Protocols
- **Reset Progress (Soft)**: Now correctly clears all gameplay data while **preserving** Premium/Ads-Removed status.
- **Factory Reset (Hard)**: Performs a total wipe of all 6 preference files, kills the login session, and returns the app to a "Day 1" state.
- **Persistence Fixes**: Resolved bug where Mastery Points would "stick" after a reset by explicitly clearing the `ArtifactManager` cache.

### Changed — Gameplay Pacing & Economy
- **Dynamic Conversion**: Tiered the Cash-to-Credits exchange price (100, 200, 400, 800 JC) based on lifetime bosses defeated.
- **Rocket Unlock Pacing**: Scaled unlock thresholds (Scout 5k, Tank 15k, Experimental 40k) to prevent premature fleet completion.
- **Mission Tuning**: Doubled early mission targets to prevent the "reward burst" at the start of new profiles.
- **Centered Reveals**: Centered and scaled the rocket preview (2.5x) in the `UnlockOverlay` for high-impact reveals.

### Fixed
- **Near-Miss Logic**: Fixed mission `id` check and ensured it only triggers during active gameplay at < 10% health.
- **Missing INF Button**: Restored the "About" button to the station tray which was accidentally removed in the previous layout pass.
- **Ground-Level Triggers**: Added a 10-meter altitude guard to all mission and achievement updates to prevent triggers while standing on the ground.

---

## 2026-07-26

**Version:** v1.5.4 (Planned) — Total Canvas Visual Overhaul

**Status:** Implementation Complete — branch `feature/fleet-ascension-visual-overhaul`

### Fixed — Google Sign-In & Play Games Identity
- **Silent Failure Resolution**: Fixed root cause where sign-in result was never captured. Now uses `GoogleSignIn.getSignedInAccountFromIntent(data)` for interactive picker results.
- **SDK Initialization**: Added mandatory `PlayGamesSdk.initialize(this)` and `com.google.android.gms.games.APP_ID` manifest entry.
- **Identity Prioritization**: Enhanced `LoginManager.kt` to explicitly fetch and prioritize Play Games **GamerTag (Alias)** over Google Account names.
- **Configuration Sync**: Generated and synced production SHA-1 fingerprints (Debug & Release) with Firebase and Play Console via CLI.
- **Sign-Out UX**: Added confirmation dialogs and clear "SIGN OUT ([Name])" labels to prevent accidental disconnects.

### Added — Total Boss Visual Pass (12 Bosses)
- **Commander**: Long flickering exhaust trails, rotating turret pods, extending sensor arrays, and hexagonal jam pulses.
- **Star-Eater**: Dense purple accretion disk with orbiting light motes and central "gravity lens" radial gradient. Liquid-like hunger tendrils.
- **Thermal Hive**: Screen-wide thermal haze overlay and increased orbiting swarm density. Red vascular internal vein pulsing.
- **Forger**: Holographic "blueprint" grid projection during fabrication. Multi-jointed assembly arm animations. High-intensity cyan strobes.
- **Architect**: "Fractal Unfolding" core with 4 counter-rotating geometric shells. Energy conduit lightning links between sub-structures.
- **Entropy Core**: Plasma siphon beams pulling from screen edges. Periodic cooling vent steam particles.
- **Singularity**: Reality fractures (color-inverting tears) and massive Phase 4 "Event Horizon" dark halo.
- **Legacy Pass**: Enhanced Gatekeeper afterimages, Leviathan segment smoothness, and Signal glitch density.
- **Death Sequences**: Scaled cataclysmic explosion intensity across all bosses in `ThreatInteractionProcessor.kt`.

### Added — Rocket Visual Upgrade
- **Thruster Flame**: 2-layer outer/inner flicker with smooth Blue→Gold→Red heat-based color transitions. Mach diamonds and core heat shimmer.
- **Body Details**: Pulsing energy glow highlights on panel lines, rivet details, and enhanced armor ridges for Chassis 2.
- **Damage States**: Multi-layered scorch marks (>40%), electrical sparking (>70%), and internal "Integrity Breach" glow (>85%).
- **Shielding**: Persistent energy sphere pulse on hit and hexagonal lattice background on active shield plates.

### Added — Particle System Pass
- **Landing Effects**: Zone-tinted, multi-layered expanding rings.
- **Combo Rewards**: Persistent flight trails for rewards flying toward the player.
- **Thrust Trail**: Enhanced shimmer and background glow for engine particles.

### Technical Foundation (Phase 8)
- **ProgressionManager Decomposition**: Split into 5 domain services (ArtifactManager, ModuleInventory, MissionTracker, UnlockService, StatRecorder).
- **StarfieldBackground Extraction**: Consistently unified 6 duplicate implementations into a single shared composable.
- **Navigation Research**: Identified 9 overlay candidates for `NavHost` migration.
- **Performance Profiling**: Identified bottlenecks in `ZoneBackgroundRenderer.kt` (Aurora paths and starfield loops).

---

## 2026-07-24

**Version:** v1.5.3 — Scout Drone Gradient Crash Fix (Hotfix)

**Status:** Hotfix — branch `hotfix/v1.5.3-scout-drone-gradient-crash`

### Fixed
- **RadialGradient crash in ScoutDroneRenderer** (`IllegalArgumentException` from `nativeCreate`): Guarded the `Brush.radialGradient()` radius with `.coerceAtLeast(0.01f)` to prevent native Skia shader creation failure on certain devices. Radius value is now clamped to a minimum of 0.01f to ensure the native `RadialGradient` layer never receives an invalid radius.

---

## 2026-07-18

**Version:** v1.5.2 — Website Community Platform & Hero CTA Redesign

**Status:** Deployed to Vercel ✅

### Added — Community & Growth Platform (`website/site/`)

**4 new routes:**
- `/beta` — Tester Portal: profile selector, progress card, eligibility, statistics, leaderboard, community progress, feedback section
- `/beta-info` — Public beta info with registration form + FAQ accordion
- `/beta-dashboard` — Admin dashboard: 8 KPI overview cards, tester table, recent sessions, daily summary, feedback table, config modal
- `/beta-dashboard/recruitment` — Recruitment dashboard with Applicants tab (summary, filters, table, side panel, activity timeline) + Outreach tab (contacts table, CSV import, manual add, Gmail auth, send invites)

**27 beta components** in `components/beta/`:
- TesterSelector, ProgressCard, EligibilityCard, StatisticsCard, Leaderboard, CommunityProgress, FeedbackSection, BetaRulesCard
- OverviewCards, TesterTable, RecentSessions, DailySummaryTable, FeedbackTable, ConfigurationCard
- RecruitmentSummaryCards, RecruitmentTable, RecruitmentSidePanel, ActivityTimeline, StatusBadge
- OutreachDashboardCards, OutreachContactsTable, OutreachImportCsv, AddManualContact, OutreachTab
- BetaRegistrationForm, BetaAccordion

**Campaign automation engine:**
- `lib/campaignEngine.ts` — full pipeline: bounce detection → eligibility fetch → 5-call invite sequence → tracking
- `lib/firebase/campaignService.ts` — config CRUD, eligible contact queries, status updates
- Vercel cron every 8h → `GET /api/campaign/process`
- Configurable: delayDays=4, maxInvites=5, batchSize=10, maxEmailsPerHour=100
- Respects timing delay between emails (5s), per-hour cap (100/hr)

**Gmail API integration:**
- OAuth flow: `getAuthUrl()` → consent → `/gmail/callback` → `POST /api/gmail/exchange` → tokens stored in Firestore `gmailAuth/tokens`
- `POST /api/gmail/send` — multipart/alternative HTML emails via Gmail API
- `GET /api/gmail/status` — check auth status
- `POST /api/gmail/disconnect` — "Switch Account" clears stored tokens
- Auto token refresh (60s expiry buffer)
- **Bounce detection**: polls Gmail DSN inbox from `mailer-daemon@googlemail.com`, idempotent via `processedBounceIds` array, updates emailLog + recruitmentContacts + betaUsers

**Email template system:**
- `lib/emailTemplates/layout.ts` — shared dark-theme HTML shell with inline CSS
- 7 templates: acknowledgement, welcome, invitation-1 through invitation-5
- `index.ts` — registry with `renderTemplate(template, name, inviteNumber?)`
- All sent as multipart/alternative (HTML + plaintext fallback)

**Firestore data model:**
- 10+ collections: testers, sessions, testerFeedback, betaUsers, recruitmentContacts, activityLog, emailLog, dashboardConfig, campaignConfig, gmailAuth, senderProfiles, appConfig
- 3 composite indexes deployed via `firestore.indexes.json`
- Default configs for campaign (delayDays, maxInvites, etc.), dashboard (beta dates, revenue eCPM), sender profile

**6 API routes:**
- `POST /api/recruitment/register` — server-side registration + acknowledgement email + matchRegistration + logEvent
- `GET /api/campaign/process` — cron trigger
- `POST /api/gmail/send` — send email(s)
- `GET /api/gmail/status` — auth status
- `POST /api/gmail/exchange` — OAuth code exchange
- `POST /api/gmail/disconnect` — delete tokens

**12 TypeScript type files** in `types/`: betaUser, tester, config, recruitment, recruitmentContacts, campaign, feedback, stats, activityLog, emailLog, senderProfile

**Documentation:**
- `docs/COMMUNITY_PLATFORM.md` — comprehensive reference with architecture, routing, component map, Firestore data model, Gmail integration, campaign automation, reuse checklist, setup guide

### Added — Website Hero CTA Redesign

- **Premium Beta CTA** with 3-state lifecycle (`sticky` → `landing` → `landed`) via scroll handler
- **Single CTA sentinel wrapper** (`ctaRef` on shared parent div) — eliminates mobile duplicate CTA during transition
- **Sticky bar** at `top: 72px` with 700ms fade-in/fade-out; desktop side buttons fade on landing, mobile buttons hidden instantly
- **BetaTagline** (`BetaTagline.tsx`) — rotating rewards/taglines inside the Beta button
- **Play Store modal** (`PlayStoreModal.tsx`) — replaces direct Play Store link; opens overlay when Play Store icon is clicked
- **Precise midpoint scroll calculation** — replaces `scrollIntoView({ block: "center" })` with `getBoundingClientRect` midpoint between `#download-section-end` and `#made-with`, using `behavior: "auto"` for consistent positioning across viewport heights

### Changed

- **Footer**: "Built with ❤️" → "Made with ❤️ by Ashwath AI" in `screens/Footer.tsx`
- **Landing zone spacing**: `mt-10 pt-8 min-h-[80px]` → `mt-8 pt-4 pb-8 min-h-[60px]` for vertical centering
- **Section bottom padding**: `py-20 sm:py-28` → `py-20 sm:py-32` for breathing room
- **Root AGENTS.md**: Added "Website & Community Platform" to Master Documentation Index
- **docs/INVENTORY.md**: Updated with all new website files

### Build

- `npm run build` — BUILD SUCCESSFUL (zero errors, 19 routes)

---

## 2026-07-16

**Version:** v1.5.2 — Beta Analytics V0 / Stabilization

**Status:** Release-ready ✅

### Added
- **PlayerAnalyticsManager**: Firestore-backed tester analytics using decorator pattern on `GameAnalytics`. Tracks 15 fields per tester in `testers/{email}` collection plus per-session history in `testers/{email}/sessions/{id}` subcollection.
- **BetaRegistrationDialog**: First-launch dialog asking for email (required), name (optional), phone (optional). Validates email via `Patterns.EMAIL_ADDRESS`. Never shows again after submission or skip.
- **Firestore integration**: Added `firebase-firestore` dependency. Internet permission already present.

### Changed
- **Documentation restructure**: Archived ~59 historical backlog files to `docs/archive/`. Removed empty `architecture/`, `REPORTS/`, `releases/` directories.
- **New docs**: `docs/ANALYTICS.md` — dedicated analytics & ads reference with event catalog, screen tracking, AdMob config, governance, Beta V0 docs, Migration Guide, Production Cleanup section. `docs/RELEASES.md` — single-file release history replacing per-version publication reports.
- **Updates**: `INVENTORY.md` fixed analytics/ad file paths. `ARCHITECTURE.md` gained Analytics & Ads section. `AGENTS.md` updated Master Index.
- **Version code**: 5 → 6 (versionName remains 1.5.2).

### Stabilization
- **Removed 5 temporary debug Log.d lines** (verbose lifecycle traces: onAppForeground, onAppBackground, bg timeout, crash recovery, session resume).
- **Wrapped 10 remaining Log calls in `if (BuildConfig.DEBUG)`** — silent in release builds; debug builds retain full `adb logcat -s BetaAnalytics` tracing.
- **Documented session lifecycle**: One completed game = one analytics session. Added `docs/ANALYTICS.md` "Session Lifecycle" and "Production Cleanup Opportunity" sections.
- **Updated KDoc** in `PlayerAnalyticsManager.kt` to reflect Beta V0 session model.

### Cleanup Opportunity (~190 lines)
Documented in `docs/ANALYTICS.md` — app-lifecycle infrastructure (`onAppForeground`, `onAppBackground`, crash resilience, timers, incremental tracking) can be removed in a future production pass with zero impact on one-game=one-session metrics.

---

## 2026-07-15

**Version:** v1.5.2 — Closed Beta

**Status:** Closed Beta ✅

### Added
- **ContinueReadyOverlay**: New dark overlay with "Tap anywhere to resume" after continue. Game state `CONTINUE_READY` replaces direct `PLAYING` transition — player must tap to resume.
- **Share Jump Droid**: Share button in Game Over overlay (top arrow icon) and Main Menu footer. Shares altitude score or generic invite text via Android share intent.
- **Main Menu responsiveness**: Replaced hardcoded `width(280.dp)` with `fillMaxWidth(0.85f)` for proper display across screen sizes.
- **Heat Bat 4-state AI**: `ENT_HEAT_BAT` now has SEARCHING (patrol), ATTACKING (dive to heat), ORBITING (circle near heat), FLEEING (retreat at high heat) behavioral states via `ThreatAIUpdater.kt`.
- **Boss cooldown map**: `EncounterDirector.bossCooldowns` prevents re-spawning a boss within 60s of defeat. Cooldown ticks down with dt.
- **Archive NEW system**: `DiscoveryManager.markViewed()`/`isViewed()`/`getUnreadCount()` tracks unread discoveries. ArchiveScreen shows purple dot in header and "NEW" badge on unread entries. CodexCard supports optional `isNew` badge.
- **ADR documentation**: `docs/ADR_DISTRIBUTION_STRATEGY.md` — distribution strategy for closed beta.
- **Production checklist**: `docs/PRODUCTION_CHECKLIST.md` — pre/post release tasks.

### Changed
- **Version bump**: `versionCode 2 → 5`, `versionName "1.5.1" → "1.5.2"`.
- **Website version**: Updated to v1.5.2.
- **GameOver continue UX**: Ad failure now shows progressive messages ("LINK WEAK" → "ONE ATTEMPT REMAINING" → "FORCED RELINK"). 3rd failed attempt grants free continue.

### Build
- `./gradlew assembleRelease` — BUILD SUCCESSFUL
- `./gradlew assembleDebug` — BUILD SUCCESSFUL

---

---

## 2026-07-13

**Version:** v1.5.1 — Release Polish Update

**Status:** Published ✅

### Added
- **Portrait lock**: App-wide `screenOrientation="portrait"` prevents landscape rotation.
- **Keep screen on**: `FLAG_KEEP_SCREEN_ON` active during PLAYING, ASCENSION_PROTOCOL, and PAUSED states; clears on game over, navigation away, or app backgrounding.

### Changed
- **Version bump**: `versionCode 1 → 2`, `versionName "1.5.0" → "1.5.1"`.

### Build
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- `./gradlew assembleRelease` — BUILD SUCCESSFUL
- `./gradlew bundleRelease` — BUILD SUCCESSFUL
- All artifacts signed and verified.

---

## 2026-06-25 (Final)

**Sprint / Phase:** EPIC 11 — Ascension (The End) — MERGED TO `development`

**Branch:** `refactor/cleanup` → merged to `development`

**Status:** Complete ✅

### Added
- **SoundManager.kt**: Audio engine with SoundPool + programmatic PCM generation. Handles thrust, landing, collect, damage, death SFX. Muted by default. Volume controls in Settings.
- **GameEngine refactoring**: State container extracted from GameScreen.kt (~30 state vars + 12 managers moved to dedicated class).

### Fixed
- **App icon**: Removed adaptive icon XMLs causing circular crop. User's custom PNG placed in all mipmap density directories.
- **Dev menu spawn velocity**: Was spawning threats with vx=0 — patrol-based enemies (Scout Drone, Sky Ray, Void Whale, etc.) would freeze in place. Now passes `def.spawnVx`/`spawnVy` to `spawnThreat`.

### Build
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (only pre-existing `LinearProgressIndicator` deprecation)

---

## 2026-06-24

**Sprint / Phase:** EPIC 8.5 Execution — Architecture Decomposition Complete

**Branch:** `epic8.5-rebased`

**Status:** Complete ✅

### Added
- **26 Threat Renderers**: One file per threat (11 hazards, 8 enemies, 6 bosses + 3 shared). `ThreatRenderer` interface + `ThreatRendererRegistry` with eager map dispatch.
- **ThreatAIUpdater.kt**: Extracted AI behavior `when`-block from `ActiveThreat.update()` (566 lines).
- **ThreatInteractionProcessor.kt**: Extracted collision/damage `when`-block from `ActiveThreat.processInteraction()` (551 lines).
- **StarfieldBackground.kt**: Shared animated starfield composable (replaced 6 copy-paste implementations).
- **GaugeBar.kt**: Base gauge composable with shared clipPath + seg ticks + interference lines (4 gauges refactored).
- **HudContext.kt**: Bundles `gameTime` + `interferenceTimer` + `zone` — eliminates 12 parameter passthroughs.
- **GameEngine.kt**: State container with all managers + 40+ observable game state vars.
- **ProgressionService.kt**: Interface decoupling MissionManager from ProgressionManager.
- **GameStats.kt**: Data class for session stat commit.
- **MissionScreen.kt**: Created from earlier extraction.
- **PowerUpManager.kt**: PowerUp lifecycle extracted from GameScreen.

### Changed
- **ActiveThreat.kt**: 1,224 → 123 lines (90% reduction). AI and interaction logic delegated to extension functions.
- **GameScreen.kt**: 3,901 → 2,011 lines (48% reduction). Threat rendering extracted to 26 files. Ceremony lifecycle extracted. PowerUpManager extracted.
- **Mission.kt**: `checkCompletion()` made pure (side-effect moved to callers).
- **MissionManager.kt**: Added `getBestMissionForTrack()`. Decoupled from ProgressionManager via interface. Ceremony lifecycle extracted from GameScreen.
- **ComboManager.kt**: Rewards merged (unified tier + survival drop table). Tier table externalized to `TIERS` companion.
- **HudWidgets.kt**: 4 gauges refactored to use shared `GaugeBar` composable. `HudContext` hoisted.
- **MainMenuScreen.kt, AboutScreen.kt, SettingsScreen.kt, PauseOverlay.kt, GameOverOverlay.kt, HangarScreen.kt**: Starfield replaced with shared `StarfieldBackground` composable.
- **AGENTS.md**: Updated for EPIC 8.5 completion. Points to `epic8.5-structured` tag.

### Removed
- **MissionRow.kt**: Deleted (ceremony lifecycle moved elsewhere).
- **`isNew` property**: Removed from `Mission.kt`.
- **`MissionType.COMBO`**: Removed.
- **`CeremonyStage`**: Simplified to NONE/GLOW/REPLACING.
- **Duplicate ComboDisplay**: Removed from center HUD.

### Tag
- **`epic8.5-structured`**: Rollback point at commit `9363434`.

---

## 2026-06-23

**Sprint / Phase:** EPIC 8.5 Planning — Architecture Decomposition Roadmap

**Branch:** `epic8-mission-migration`

**Status:** Planned

### Added
- **EPIC 8.5 Decomposition Plan**: 9-sprint roadmap (8.5.0–8.5.8) to decompose `GameScreen.kt` (3,901 lines), `ActiveThreat.kt` (1,224 lines), `ProgressionManager.kt`, and navigation into manageable components before Phase 9 feature work begins.
- **EPIC 8 Tech Debt Audit**: 17 findings classified across 5 severity levels. 2 CRITICAL (GameScreen God Object, ActiveThreat God Object), 3 MAJOR, 8 MODERATE, 4 MINOR. Report: `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`
- **EPIC 8 Validation Report**: Final validation of all EPIC 8 systems — stabilization, polish, and mission event audit. No blockers found. Report: `docs/REPORTS/EPIC8_VALIDATION_REPORT.md`
- **EPIC 8.5 Planning Report**: Executive summary of decomposition sprint planning. Report: `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md`

### Changed
- **EPIC 9**: Officially deferred until EPIC 8.5 Architecture Decomposition completes.
- **AGENTS.md**: Updated for EPIC 8.5 transition. Known issues now include GameScreen.kt (3,901 lines) and ActiveThreat.kt (1,224 lines) as EPIC 8.5 targets.
- **EPIC_8_TRACKER.md**: Status updated to "Functionally Complete — Awaiting Runtime Signoff". Post-8 content moved under deferred column.
- **JumpDroid_EPIC_Tracker.md**: EPIC 8.5 section added with 9-sprint structure (8.5.0–8.5.8). EPIC 9 marked DEFERRED.

### Documentation
- `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md`: Full decomposition roadmap with sprint breakdown, risks, dependencies, and success criteria.
- `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md`: Executive summary for cross-team communication.
- `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`: Comprehensive 17-finding codebase audit.

### Revision: Sprint 8.5.0 Baseline Added
- **Request**: User approved the decomposition plan but requested Sprint 8.5.0 (Baseline Capture) be inserted before Sprint 8.5.1.
- **Action**: Added Sprint 8.5.0 to all documents. Existing sprints 8.5.1–8.5.8 unchanged.
- **8.5.0 scope**: Baseline tag `epic8.5-baseline`, build verification, file size inventory, architecture metrics, screenshot baseline, baseline report.
- **Impact**: 9 sprints total (was 8). Estimated effort: 13-18 sessions (was 12-17). All sprint numbers 8.5.1–8.5.8 preserved. Sprint 8.5.0 is precondition for all subsequent sprints.
- **Documents updated**: EPIC_8_5_DECOMPOSITION_PLAN.md, EPIC8_5_PLANNING_REPORT.md, JumpDroid_EPIC_Tracker.md, CHANGELOG.md

---

## 2026-06-22

**Sprint / Phase:** EPIC 8 — Missions & Progression (Phase 1-3)

**Branch:** `epic8-mission-migration`

**Status:** In Progress

### Added
- **Intelligence Network**: Real-time gameplay stat tracking across physics and combat loops.
- **GameStats Model**: Unified 19-point metric schema for run evaluation.
- **Mission Reward Sealed Classes**: Added `Cash` and `ModuleUnlock` support to the reward pipeline.
- **Persistent Lifetime Stats**: Added cumulative flight time and encounter tracking to `ProgressionManager`.
- **EPIC 8 Tracker**: Detailed tracking for mission migration.

### Changed
- **Mission Model Refactor**: Aligned legacy mission data with EPIC 7 sealed reward classes.
- **Mission Registry Port**: Migrated 48 missions from prototype branch to production architecture.
- **Manager Integration**: Unified `MissionManager` with `ProgressionManager` for central reward handling.

---

## 2026-06-22

**Sprint / Phase:** Sprint C Completion — Structural Fix, Boss Projectiles, Enemy Projectiles, Zone Redistribution

**Branch:** `development` (merged from `sprintc-fixes`)

**Commit:** `a1accfb`

**Status:** Completed

### Fixed
- **Critical structural bug**: Boss `update()` code was inside `ThreatType.ENEMY ->` `when` branch. Commander (MINI_BOSS) and Gatekeeper (BOSS) never executed their AI logic. Moved entity-specific handlers outside the `when` block.
- **BOSS_VOID_ENGINE**: `onVisualFeedback(10f, 0.4f)` now fires max once per 0.5s instead of every sub-step (240/sec). `gravityPulseTimer` dead code removed.
- **BOSS_LEVIATHAN**: Weak point oscillation corrected from `sin(life×1000f)` to `sin(life×1.5f)`. Wall pressure increased 50× to 100000.
- **BOSS_SIGNAL**: Phase 3 now checks `activeWeakPoints <= 0` for exit. Weak point position uses per-frame Random instead of seeded.
- **ENT_VOID_WHALE**: Slipstream force multiplied 16× (2000 → 32000), vacuum 16× (3000 → 48000) for noticeable gameplay impact.
- **ENT_VOID_WRAITH**: Visual distinction between materialized (purple aura, full body, crackling energy) and phased (gray wireframe, 8% alpha, glitch rects).
- **ENT_STALKER**: Base speed increased, alertLevel ramp rate increased.
- **HAZ_VOID_ANOMALY**: Zone-specific duration override for longer-lasting Void anomalies.
- **BOLT rendering**: Reverted to original size (5-trail, no 2.5× scale, no pulsing glow).
- **Floating text**: Critical text font size reduced from `headlineSmall` to `titleSmall` (~42% smaller).

### Added
- **Boss projectile systems**: All 6 bosses fire phase-specific projectiles. Commander (P3+ 3-way BOLT). Gatekeeper (P2 BOLT / P3 BEAM). Star-Eater (P2 WAVE / P3 MISSILE). Leviathan (P2 BOLT / P3 BEAM). Void Engine (P2 WAVE / P3 3-way BOLT). Signal (P2 BOLT / P3 BEAM).
- **Enemy projectiles**: Defense Node fires orange BOLT (8dmg, 2s cooldown) when locked-on. Void Tracker fires red BOLT (8dmg, 1.5s cooldown) at alertLevel > 0.5.
- **Zone redistribution**: Earth is boss-free. Commander spans Cloud Layer–Void. Gatekeeper: Orbit+. Star-Eater/Leviathan: Deep Space+. Void Engine/Signal: Void. All bosses expanded to native zone + all zones above.
- **Difficulty scaling**: Zone-based HP multiplier (×1.0–×3.0) for bosses at spawn via `difficultyMultiplier`.
- **Threat density scaling**: `intensityFactor` applied to EncounterDirector spawn intervals and caps.
- **EncounterDirector zone gating**: Milestone boss spawns check `currentZone in def.spawnRules.allowedZones`.
- **Cloud Commander fallback**: Spawn chance reduced 60% (×0.4 multiplier).
- **`onSpawnThreat` callback**: Wired in GameScreen.kt for boss minion spawning.

### Documentation
- **THREAT_MASTER_TABLE.md**: Created with Status, AI Behavior, and Projectiles columns for all 22 entities.
- **THREATS.md**: Updated all boss/projectile entries. Checklist finalized. Changelog entries added.
- **BOSS_DESIGN_BIBLE.md**: Updated boss entries, zone expansions, enemy projectiles, structural fix note, v1.3 changelog.
- **SPRINT_C_AUDIT.md**: Updated with current session fixes, bug resolutions, Appendix A projectile data.
- **SPRINT_C_COMPLIANCE_GAP.md**: Updated gap closure status.

---

## 2026-06-20

**Sprint / Phase:** Visual Overhaul Sprint 2-3 — Premium Screen Polish

**Branch:** `development`

**Commits:** `35e03ce`, `47ee28e`, `afbecdc`, `47d4191`

**Status:** Completed

### Added
- **TitleScreen scanning drone**: Cinematic patrol with Z-axis depth scaling, rotating radar sweep beam with layered glow, light column, rocket detection glow
- **Ambient scan rings**: Pulsing concentric circles emanating from rocket on title screen
- **Floating data particles**: 6 orbiting colored data dots around the rocket
- **Animated starfields**: Drifting starfield with sine-wave twinkle on HangarScreen, SettingsScreen, AboutScreen, GameOverOverlay, PauseOverlay
- **Status footers**: "HANGAR // ROCKET SELECT // {rank}", "SYSTEM PREFERENCES // AUDIO // DATA", "POWERED BY ASHWATH.AI // V1.2.0", "MISSION PAUSED // {ZONE} SECTOR"
- **Gauge value labels**: Raw integer value beside emoji icons, percentage label below bar on all 4 gauges

### Changed
- **HudWidgets**: Widened gauge bars from 6dp → 10dp, replaced solid fills with vertical gradient fills, added 10-segment tick marks, shield shimmer effect, consistent RoundedCornerShape(4.dp)
- **MainMenuScreen**: "COMMAND CENTER" text sizing from `headlineLarge` → `headlineMedium` with tighter `letterSpacing(2.sp)` to prevent wrapping
- **Track backgrounds**: Reverted from zone-accented tint to neutral `SciFiSurface.copy(alpha = 0.4f)`
- **Gauge number visibility**: Raw value 10sp full color, percentage 8sp full color
- **Card borders**: ComboHudBar and ZoneDiscoveryCard borders thinned to 0.5dp with `alpha = 0.4f` for premium look
- **Text shadows**: Added pulsing glow shadows to titles across all screens

### Fixed
- **TitleScreen drone**: Replaced triangle cone with thin radar sweep line, removed "⚠ HUMAN DETECTED" overlay text
- **Text wrapping**: All screen titles now use `headlineMedium` with 2.sp letter spacing to prevent 2-line wrapping on narrow screens

### Removed
- Triangle scan cone from TitleScreen drone (replaced with sweep line)
- Zone-tinted gauge track backgrounds (reverted to neutral dark)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL across all 4 commits
- All composable signatures unchanged — zero breakage risk for HUD revamp

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T4 Recovery

**Branch:** `refactor/t4-recovery`

**Status:** COMPLETE AND STABILIZED

### Added
- `SurvivalManager.kt` — Extracted damage and destruction lifecycle logic.
- `EncounterDirector.kt` — Extracted spawn rules and boss milestones.

### Changed
- `GameScreen.kt`: Delegated 400+ lines of threat interaction to `ActiveThreat.kt`.
- `GameScreen.kt`: Integrated `SurvivalManager` and `EncounterDirector`.
- `ActiveThreat.kt`: Added `processInteraction` for delegated proximity logic.

### Fixed
- **Mission Lifecycle**: Restored `missionManager.selectNextMission()` to fix ceremony loops and combo HUD instability.
- **Spawn Density**: Expanded enemy routing for `UPPER_ATMOSPHERE` and tuned probabilities to development baseline.
- **Thread Safety**: Applied `.toList()` snapshots to all critical StateList iterations to resolve `ConcurrentModificationException`.
- **Continue Recovery**: Fixed `continueRun()` state reset to prevent immediate death-loop on hull destruction.

### Notes
- T4 architecture is now fully validated and production-ready.
- Cumulative game loop reduction: 4,344 → 2,538 lines (−41.6%).

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T3 — Logic & Manager Extraction

**Branch:** `refactor/logic-extraction`

**Commit:** `e79068d`

**Status:** Completed

### Added
- `NotificationManager.kt` — Encapsulates notification queue and timer logic
- `FloatingTextManager.kt` — Manages lifecycle of floating status text popups
- `PlatformManager.kt` — Encapsulates platform generation and streak tracking

### Changed
- `GameScreen.kt`: 20+ call sites updated to use `NotificationManager`
- `GameScreen.kt`: 15+ call sites updated to use `FloatingTextManager`
- `ProgressionManager.kt`: Added `saveHighScore`, `checkUnlocks`, and `wipeData` methods
- `GameScreen.kt`: Achievement and high score logic delegated to `ProgressionManager`
- `GameScreen.kt`: Platform generation delegated to `PlatformManager`
- Net reduction: 76 lines (3,109 → 3,033)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified on emulator

### Notes
- This sprint shifted the focus from UI extraction to logic modularization.
- Core game loop remains in `GameScreen.kt` but is now significantly leaner.

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T1 — Phase 1

**Branch:** `refactor/ui-extraction`

**Commit:** `fb93e9d`

**Status:** Completed

### Added
- 15 standalone UI composable files extracted from `GameScreen.kt`:
  - Screens: `TitleScreen.kt`, `MainMenuScreen.kt`, `HangarScreen.kt`, `ArchiveScreen.kt`, `SettingsScreen.kt`, `AboutScreen.kt`, `LeaderboardScreen.kt`
  - Overlays: `PauseOverlay.kt`, `TutorialOverlay.kt`, `HelpOverlay.kt`, `UnlockOverlay.kt`, `GameOverOverlay.kt`
  - HUD widgets: `HudWidgets.kt` (8 functions: `AltitudeDisplay`, `FuelGauge`, `HeatGauge`, `ShieldGauge`, `IntegrityGauge`, `ComboHudBar`, `NotificationLayer`, `ZoneDiscoveryCard`)
  - Shared components: `CodexCard.kt`, `Achievements.kt`
- Architecture report: `docs/architecture/Refactor_T1_Phase1.md`
- Phase 2 implementation plan: `docs/architecture/Refactor_T1_Phase2_Plan.md`

### Changed
- `GameScreen.kt`: removed inline `AchievementsList` val (moved to `Achievements.kt`)
- `GameScreen.kt`: removed inline `CodexCard` composable (moved to `CodexCard.kt`)
- Net reduction: 52 lines (4,344 → 4,292)

### Fixed
- Import errors in all 15 extracted files:
  - `graphicsLayer` wrong package path (`foundation.layout` → `ui.graphics`)
  - Missing imports: `border`, `shadow`, `CircleShape`, `statusBarsPadding`, `fillMaxHeight`, `offset`, `safeDrawingPadding`, `sp`
  - Invalid `roundToPx` import removed (function resolves via `Dp` member without explicit import)
  - Duplicate `dp` import in `TitleScreen.kt` removed

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (zero errors, 2 pre-existing deprecation warnings)
- All 15 extracted files compile alongside `GameScreen.kt`
- Dex artifact produced without issues

### Notes
- GameScreen.kt still contains inline implementations for all screens, overlays, and HUD widgets. The extracted files are defined but not yet called. Phase 2 will perform the replacement.

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T1 — Phase 2

**Branch:** `refactor/ui-extraction`

**Commit:** `686bfd0`

**Tags:** `refactor-t1-phase1`, `refactor-t1-phase2`

**Status:** Completed

### Added
- Phase 2 completion report: `docs/architecture/Refactor_T1_Phase2_Report.md`
- `refactor-t1-phase1` and `refactor-t1-phase2` git tags

### Changed
- `GameScreen.kt`: replaced all 7 screen branches with extracted composable calls (TITLE, MAIN_MENU, HANGAR, ARCHIVE, SETTINGS, ABOUT, LEADERBOARD)
- `GameScreen.kt`: replaced 6 HUD widgets with extracted calls (AltitudeDisplay, FuelGauge, HeatGauge, ShieldGauge, IntegrityGauge, ComboHudBar, NotificationLayer, ZoneDiscoveryCard)
- `GameScreen.kt`: deleted duplicate altitude display block
- `GameScreen.kt`: replaced 5 overlays with extracted calls (PauseOverlay, TutorialOverlay, HelpOverlay, UnlockOverlay, GameOverOverlay)
- Net reduction: 966 lines (4,292 → 3,326)

### Fixed
- `ComboHudBar` parameter type mismatch: `getWindowForCombo` required lambda wrapper `{ comboManager.getWindowForCombo(it) }`

### Removed
- `PowerupBadge` composable — unused (zero call sites, 23 lines)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (zero errors)
- Brace balance: 681 `{` = 681 `}` ✅
- APK installed and launched on emulator (`Medium_Phone API 35`)
- `adb shell monkey` — `Events injected: 1`

### Notes
- Mission row cards (~124 lines) and floating combo texts (~17 lines) remain inline — deferred past Phase 2
- `MissionType.toIcon()` extension kept — still called by inline mission cards
- Refactor Sprint T1 is fully complete. Branch ready to merge to `main`.

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Phase A (Low Risk)

**Branch:** `refactor/ui-extraction`

**Base Commit:** `686bfd0` (T1 Phase 2)

**Status:** Completed

### Added
- `TopRightUtilityButtons.kt` — Help (`?`) + Pause (`||`) buttons (23 lines)
- `MissionRow.kt` — Mission card row with ceremony stages, progress bars, AnimatedContent (108 lines)
- `FloatingTextsLayer.kt` — Animated floating text overlay (16 lines)
- `LeftGauges` / `RightGauges` appended to `HudWidgets.kt` — Column wrappers for fuel/heat/shield/integrity gauges (22 lines)
- T2 plan: `docs/architecture/Refactor_T2_Plan.md`

### Changed
- `GameScreen.kt`: 5 inline blocks replaced with extracted composable calls
- Net reduction: 147 lines (3,326 → 3,179)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (11s, zero errors)
- ADB install — Success
- Emulator launch — Events injected: 1

### Notes
- All 4 extractions use explicit parameter patterns; `BoxScope`-dependent composables accept `Modifier` parameter
- T2B (Canvas effects, ~104 lines) is planned but not started
- Threat entity rendering (~826 lines) remains inline indefinitely

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Phase B (Medium Risk)

**Branch:** `refactor/ui-extraction`

**Base Commit:** `2fe24f1` (T2A)

**Status:** Completed

### Added
- `CanvasEffects.kt` — 8 `DrawScope` extension functions for canvas rendering (168 lines)

### Changed
- `GameScreen.kt`: 8 inline Canvas blocks replaced with extracted function calls
- Net reduction: 70 lines (3,179 → 3,109)

### Extractions
- `drawRealityDistortion` — magenta overlay near Void Anomaly
- `drawSpeedLines` — vertical lines during fast descent
- `drawGround` — brown ground rectangle
- `drawParticles` — sparkle/circle particle rendering
- `drawLandingEffects` — expanding cyan ring circles
- `drawPowerUps` — colored shapes per powerup type
- `drawFlyingRewards` — animated reward items
- `drawImpactFlash` — white stroked border screen flash

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (5s, zero errors)
- ADB install — Success
- Emulator launch — Success (PID running)

### Notes
- Refactor Sprint T2 is fully complete. T1 + T2 cumulative reduction: 4,344 → 3,109 lines (−28.4%)
- Threat entity rendering (~826 lines) remains inline indefinitely

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Positioning Fixes + Merge

**Branch:** `refactor/ui-extraction`

**Base Commit:** `24a3eb9` (T2B)

**Status:** Completed

### Fixed
- `AltitudeDisplay` — added `modifier` parameter; caller passes `Modifier.align(Alignment.TopCenter)` for correct positioning
- `NotificationLayer` — added `modifier` parameter; positioned at `TopCenter.padding(top = 240.dp)`, text shrunk from `headlineSmall` → `bodyLarge`, `letterSpacing` reduced from `4.sp` → `2.sp`

### Changed
- `OPENCODE.md` — updated branch strategy, completed phases, and next tasks
- `docs/CHANGELOG.md` — added this entry

### Merged
- `refactor/ui-extraction` merged into `development` (`af3d0ae`)
- Git tags `refactor-t1-phase1`, `refactor-t1-phase2`, `refactor-t2` pushed to remote

---

## 2026-06-25

**Sprint / Phase:** EPIC 11 — Ascension (The End) — COMPLETE

**Branch:** `epic11-ascension`

**Status:** Complete ✅

### Added
- **PlayerInputProcessor.kt**: Extracted input logic from GameScreen.kt with `glitchFactor` hook for Singularity boss fight.
- **SingularityRenderer.kt**: Final boss renderer with white-noise core, geometric fragments, and reality rift lines.
- **AscensionOverlay.kt**: Full-screen ceremonial credits with Architect's Log and Hall of Pioneers.
- **Omega Modules**: `MOD_VOID_ENGINE` (infinite fuel) and `MOD_SINGULARITY_CORE` (perfect stability) — Legendary tier.
- **Origin Reset**: Coordinate normalization at 100,000m to prevent floating-point jitter.
- **Eternal Mode**: Capped infinite scaling beyond 100,000m (0.25s min interval, 4x max speed, overflow-safe at ~550,000m).
- **Prestige System**: Permanent +10% hull/shield multiplier per reset, unlocked at 100km.
- **Design Libraries**: Updated ROCKET_LIBRARY.md, AREA_LIBRARY.md, THREAT_LIBRARY.md, ARTIFACT_LIBRARY.md with EPIC 11 content.

### Changed
- **GameScreen.kt**: ASCENSION_PROTOCOL game state handling, origin reset logic, prestige wiring, ascension credits overlay.
- **HudWidgets.kt**: HUD pull animation via `graphicsLayer.translationX` using `hudPullFactor` from Singularity.
- **ThreatAIUpdater.kt**: Singularity AI with HUD Pull cycle (8s), slow tracking, instant DESTROYED on weak point kill.
- **ThreatRegistry.kt**: Registered BOSS_SINGULARITY with SINGULARITY zone.
- **ThreatRenderer.kt**: Registered SingularityRenderer in the registry.
- **ActiveThreat.kt**: Added `hudPullFactor` field for Singularity mechanic.

### Fixed
- **Boss death sequences**: Escape phase corrected per boss (Commander→5, others→4). Added `activeWeakPoints <= 0` guard in 7 boss AIs to prevent timer-based phase override from blocking escape.
- **Star Eater**: Suction reduced 3000→1500. Weak point moved from center to orbiting position (100px radius). Hit detection radius increased 80→120.
- **Boss pursuit speeds**: Increased tracking factors for Void Engine (0.2→0.4), Signal (0.05→0.15, 0.2→0.25), Architect (0.2→0.3), Entropy Core (0.1→0.2).
- **Fuel/Heat gauge alignment**: Numeric values padded to 3 characters to prevent layout shift at 100.
- **HUD zone-adaptive colors**: Removed from FuelGauge — now uses static SciFiGreen (→SciFiRed when low/critical), matching other gauges.
- **Platform colors**: Earth platforms now use warm brown (#795548), Cloud platforms use light cyan (#80DEEA) instead of plain white.
- **Hidden signal `isUnlocked` persistence**: `syncState()` now restores `isUnlocked` from SharedPrefs, fixing unlocked signals showing glitch effect after restart.
- **Claimable mission count**: Dashboard now shows "(N HIDDEN)" hint when claimable count includes hidden signals.

### Gameplay Balance
- **Eternal Mode**: Safe capped scaling prevents overflow at extreme altitudes.
- **Prestige System**: Reset grants +10% hull/shield per level, gated behind 100km completion.

### Technical Debt
- `GameScreen.kt` line count: ~1,943 lines (budget: 2,200) — headroom for future work.
- `PlayerInputProcessor.kt` extracted, reducing GameScreen complexity.

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## Historical Milestones

The following milestones summarize completed work prior to the changelog's creation. These entries are reconstructed from commit history and were not recorded as changelog events at the time of completion.

### EPIC 4: The Ascension Program

| Sprint | Commit | Summary |
|--------|--------|---------|
| EPIC 4 Start | `e744ae0` | Missions, achievements, and progression system initiated. |
| Sprint A | `6235350` | Mission reliability and early-game feel improvements. |
| Sprint B | `1404aaa` | Combo Renaissance — combo system overhaul and scoring refinements. |
| Sprint C | `86ce562` | Mission UX and communication — mission cards, notifications, player feedback. |
| Sprint D | `17956c0` | Platform evolution — dynamic platform generation and difficulty scaling. |
| Sprint E | `a00c623` | Pre-release candidate stabilization before Ascension Program launch. |
| Sprint F | `fa3101b` | Ascension Program complete — full mission tree, achievement tracking, progression gates, and Codex system operational. |

**Scope:** The Ascension Program introduced the mission system (6 mission types with difficulty tiers), achievement tracking (6 achievements with unlock conditions), player progression gates (rocket unlocks at score thresholds), the Codex (discovery categories with lore entries and per-category completion tracking), and the Zone system (altitude-based zones with distinct visual themes).

### EPIC 5: Survival Protocol

| Sprint | Commit | Summary |
|--------|--------|---------|
| Sprint B | `aee2c37` | Threats, survival economy, and destruction system stable. |

**Scope (in progress):** Survival Protocol introduces the threat system (hostile entities with spawn rules and tier progression), survival economy (shield regeneration delay, hull integrity critical thresholds, overheat management), destruction sequence (3-phase visual breakup with debris and fire effects), and visual redesigns (shield energy armor plates, catastrophic breakup particles, loss-of-control tumble).

*Note: EPIC 5 Sprint A is not reflected in the commit history as a discrete milestone and was subsumed into the Sprint B delivery.*
