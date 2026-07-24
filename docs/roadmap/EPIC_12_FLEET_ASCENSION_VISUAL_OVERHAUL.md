# EPIC 12 — Fleet Ascension: Visual Overhaul & Fleet Expansion

**Author:** Architecture Agent
**Date:** 2026-07-24
**Status:** ACTIVE
**Branch:** `feature/fleet-ascension-visual-overhaul`
**Prerequisites:** EPIC 11 ✅ — `epic11-complete` tag

---

## 0. Design Library Governance

All content derives from pre-existing `docs/design/` library entries and `docs/roadmap/MONETIZATION_VISION.md`.

| Content | Library Trace |
|---------|---------------|
| Chassis variants per class | `ROCKET_LIBRARY.md` (§B Future Rockets — Stealth Hull APPROVED) |
| Engine trail customization | `MONETIZATION_VISION.md` (§2 — Particle Customization Phase 3) |
| Rocket skins | `MONETIZATION_VISION.md` (§2 — Rocket Skins Phase 2) |
| UI themes | `MONETIZATION_VISION.md` (§2 — UI Themes) |
| Premium cosmetic variants | `MONETIZATION_VISION.md` (§2 — Cosmetic Economy) |
| Fleet Packs | `MONETIZATION_VISION.md` (§2 — Fleet Packs, long-term) |

---

## 1. Overview

EPIC 12 is a **presentation and discoverability** overhaul, not a content expansion. The game already has all core systems (12 zones, 11 bosses, 26+ threats, 14 platforms, 4 rocket classes, 17 modules, 48 missions, 43 codex entries, prestige, eternal mode). The problem is that these systems are **invisible, poorly communicated, or unrewarding** to the player.

The plan merges the original EPIC 12 — Fleet Expansion (chassis variants, engine trails, paint schemes, premium cosmetics) with a comprehensive visual overhaul spanning Main Menu, Hangar, Missions, HUD, navigation, feedback systems, and monetization surface.

**Core question the plan answers:** *"How do we make every system in Jump Droid feel discoverable, rewarding, and connected?"*

### Guiding Principles

1. **Canvas identity stays.** All rendering remains procedural `DrawScope`. No static assets.
2. **No pay-to-win.** All gameplay-affecting content remains earnable through play per MONETIZATION_VISION.md.
3. **Anonymous play always supported.** Login is optional.
4. **Build on existing docs.** All content traces to approved Design Library entries.
5. **No new game systems.** Everything proposed is presentation, navigation, feedback, and surfacing of existing content.

---

## 2. The Problem Map

| Problem | Evidence | Affects |
|---------|----------|---------|
| Main Menu is crowded | 9+ buttons, no hierarchy, no attention cues | All players |
| Equipment has no visual feedback | Equipping hull/shield/engine modules shows zero change on rocket | Mid-game players |
| Hangar overwhelms | 4 classes + 17 modules shown simultaneously, no stat comparison | New/intermediate players |
| Missions feel unimportant | Flat progress bar list, no storytelling, no sense of journey | All players |
| Unlocks are silent | Notifications flash briefly, no "here's what this means" moment | All players |
| HUD is information-dense | 4 separate gauges, text notifications, no hierarchy | All players |
| Continue experience feels punitive | 3-attempt retry system | Casual players |
| Codex/Archive is forgotten | No in-game prompt to check it after initial discovery | Mid-game players |
| Lore is invisible | 43 entries exist but never surface during gameplay | Engaged players |
| Boss encounters lack cinematic flair | No arrival buildup, minimal telegraph | All players |
| Fleet Expansion content doesn't exist | 12 chassis, engine trails, paint schemes are planned but unimplemented | Progression-focused players |

---

## 3. Phase Breakdown

### Phase 1 — Foundation: Visual Identity & Navigation

**Goal:** Make the game look and feel premium. Modernize Main Menu. Establish visual heirarchy.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 1.1 | **Main Menu Redesign** | `MainMenuScreen.kt` | Hero canvas with animated rocket silhouette + atmospheric particles. Single "ASCEND" primary button. Secondary actions in bottom sheet/drawer (Hangar, Missions, Archive, Shop, Settings). Attention badges for missions, unlocks, shop. |
| 1.2 | **Navigation Architecture Pass** | `MainActivity.kt`, all Screen files | Standardize screen transitions (fade+scale for gameplay, slide for menus). Consistent back navigation. Ensure NavHost covers all routes. |
| 1.3 | **Color & Typography Audit** | All Screen files, `Theme.kt` | Audit all screens for brand guide palette consistency (SciFiBackground, SciFiSurface, SciFiAccent, etc.). Minimum contrast ratios for accessibility. |
| 1.4 | **Equipment Visual Feedback (Canvas)** | `RocketRenderer.kt`, `Player.kt` | Hull upgrades → visible armor plate overlay. Shield upgrades → energy aura intensity scaling. Engine upgrades → trail color/particle density changes. Module equipping → visual indicator nodes on rocket body. |

### Phase 2 — Player Experience: Hangar, Loadout & Missions

**Goal:** Transform the Hangar from a data dump into a meaningful customization hub. Make missions feel like a journey.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 2.1 | **Hangar Tabbed Redesign** | `HangarScreen.kt` | Tabs: OVERVIEW → ROCKETS → MODULES → COSMETICS. Overview shows current build card with visual rocket preview. Rockets tab: card-per-class with stat comparison (radar/bar chart). Modules tab: categorized by slot, tooltip preview. Cosmetics tab: skins, trails, paints. |
| 2.2 | **Stat Comparison System** | New: `StatCompare.kt`, `HangarScreen.kt` | Bar/radar chart comparing thrust, fuel cap, heat gen, maneuverability across rocket classes. Show delta when selecting different chassis. |
| 2.3 | **Missions Timeline UI** | `MissionScreen.kt` | Present missions as timeline/journey per track. Each track has visual theme (color + icon). Show next mission reward prominently. Track progression visualized as ascending path. |
| 2.4 | **In-Run Mission Progress** | `HudWidgets.kt`, `GameEngine.kt` | Compact mission progress card on HUD that appears when active mission is ≥75% complete. Shows mission name + reward. |
| 2.5 | **Loadout → Hangar Merge** | `LoadoutScreen.kt`, `HangarScreen.kt` | Remove standalone LoadoutScreen. Add pre-flight "quick equip" panel on the zone-select or pre-run screen showing current build + START. |
| 2.6 | **Chassis Variants per Class (Fleet Framework)** | `RocketRenderer.kt`, `Player.kt`, `HangarScreen.kt` | 3 chassis per class (Explorer: Pathfinder/Nomad/Surveyor, Striker: Interceptor/Raptor/Phantom, Heavy: Atlas/Bulwark/Leviathan, Prototype: X-01/X-07/Singularity). Each chassis is a visual variant with its own Canvas-rendered geometry. Stat profiles per chassis. |
| 2.7 | **Fleet Collection UI** | `ArchiveScreen.kt` or new `FleetScreen.kt` | Collection grid showing all 12 chassis. Locked/unlocked states. Completion progress for fleet mastery rewards. |

### Phase 3 — Gameplay UX: HUD, Feedback & Continue

**Goal:** Reduce HUD clutter. Make every unlock a celebration. Fix the continue flow.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 3.1 | **HUD Gauge Consolidation** | `HudWidgets.kt` | Combine shield + integrity into a single health panel (shield bar top, hull bar below). Keep fuel/heat as separate gauges. Add zone progress bar (thin vertical indicator). Standardize iconography across all screens. |
| 3.2 | **HUD Heat Warning Enhancement** | `HudWidgets.kt` | Visual danger zone markers on heat gauge. More prominent warning at 70%+ (pulsing border, screen-edge glow tinting orange→red). |
| 3.3 | **Combo Ring Explanation** | `HudWidgets.kt` | Brief rotating text hint near combo ring: "LAND ON DIFFERENT PLATFORMS TO BUILD COMBO" (shown once at first combo level 3). |
| 3.4 | **Unlock Celebration System** | `GameEngine.kt`, `ProgressionManager.kt`, `CanvasEffects.kt` | Every unlock (mission, module, rocket, archive entry, achievement) triggers: full-screen brief glow animation → "UNLOCKED: [Name]" with entity preview → "WHAT THIS DOES" summary → "VIEW IN [HANGAR/ARCHIVE/MISSIONS]" action button. Wire into existing `checkUnlock()`, `checkDiscovery()`, `spawnBurst()`. |
| 3.5 | **Continue Credit System** | `AdComponents.kt`, `GameOverOverlay.kt`, `ProgressionManager.kt`, `ShopScreen.kt` | Players watch ads proactively to bank continue credits (1 ad = 1 credit, max 10 banked). "WATCH AD → +1 CREDIT" button on Main Menu + pre-flight screen. At game over: if credits > 0, "CONTINUE (1 CREDIT)" button; if 0, fall back to rewarded ad flow. Credit counter badge on Main Menu and Game Over overlay. Credits persist in SharedPreferences. Also allow cash purchase: 100 cash = 1 credit. Server-pushable bonus credits via Firestore remote config. |
| 3.6 | **Boss Arrival Cinematic** | `ThreatInteractionProcessor.kt`, `CanvasEffects.kt`, `HudWidgets.kt` | Boss arrival: screen dim → "WARNING: [BOSS NAME]" text with boss silhouette → boss enters from top/side with zone-tinted particle burst. Keep existing defeat sequence (Phase 1 item 4) with boss-specific explosion colors + reward burst. |
| 3.7 | **Notification Priority Tuning** | `NotificationManager.kt` | Audit all notification call sites. Ensure CRITICAL/TACTICAL/FLAVOR priority assignments are correct post all Phase 3 changes. Remove remaining weak notifications. |

### Phase 4 — Canvas Visual Upgrade

**Goal:** Make the procedurally-drawn game look stunning. Deliver on EPIC 12 cosmetic features.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 4.1 | **Rocket Visual Upgrade** | `RocketRenderer.kt` | Animated thruster flame (2-layer outer/inner with flicker — already partially exists). Body panel lines + subtle glow highlights. Damage states: scorch marks at <50% hull, sparking at <25%. Shield bubble: visible energy sphere pulse on hit (0.3s). Heat state: engine glow shifts blue→orange→red with heat level. |
| 4.2 | **Engine Trail Customization** | `RocketRenderer.kt`, `CanvasEffects.kt`, `Player.kt` | Trail color and particle pattern configurable per equipped trail. Base: cyan plasma. Purchasable: Solar Flare (orange/gold), Void Glitch (purple/black), Plasma Blue (bright cyan). Follows MONETIZATION_VISION.md Phase 3 specification. |
| 4.3 | **Paint Scheme System** | `RocketRenderer.kt`, `Player.kt`, `HangarScreen.kt` | Base hull color replaceable via paint schemes. Applied as color transform on existing Canvas rocket geometry. 3-5 base paints unlockable through gameplay, premium paints purchasable. |
| 4.4 | **Boss Visual Upgrade Pass** | All `*Renderer.kt` files | Gatekeeper: afterimage ring trails. Leviathan: bioluminescent body pulse. Void Engine: enhanced reality-tear rim animation. Signal: increased glitch rect count + screen-tear density. All bosses: zone-tinted death explosion colors (already partially done). |
| 4.5 | **Particle System Pass** | `CanvasEffects.kt` | Thrust trail: heat shimmer particles during thrust. Landing effects: zone-tinted expanding rings (already exists). Combo reward flight path: animate from combo ring to player with trail. Power-up collection: brief colored burst on pickup. Hazard proximity: subtle screen-edge glow tint. |
| 4.6 | **Zone Transition Animation** | `GameEngine.kt`, `ZoneBackgroundRenderer.kt` | Zone transition: current zone fades out → zone name card displays with one-line lore teaser → next zone fades in. Ensure smooth parallax crossfade between zones. |

### Phase 5 — Systems Integration: Discovery, Lore & Narrative

**Goal:** Surface the 43 codex entries and lore story naturally during gameplay.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 5.1 | **Lore Teasers on Zone Entry** | `GameEngine.kt`, `HudWidgets.kt` | On first zone entry each run: show one-line lore teaser as FLAVOR notification ("This is where The Lost Fleet vanished..."). Teaser text maps to LORE_LIBRARY entries. |
| 5.2 | **Artifact Discovery Overlay** | `DiscoveryManager.kt`, `CanvasEffects.kt` | On artifact pickup: brief lore snippet overlay ("Flight Recorder: '...structures in the clouds...'") before dismissal. Full text in Archive. |
| 5.3 | **Codex HUD Quick-Access** | `HudWidgets.kt`, `GameEngine.kt` | Small collapsible "CODEX" button on HUD (bottom-right, 32dp). Opens mini-panel showing discoverable entries nearby (type, name, distance). Connected to DiscoveryManager. |
| 5.4 | **New Archive Badge Retention** | `ArchiveScreen.kt`, `MainMenuScreen.kt` | Archive button shows badge count of unread entries. Badge persists until Archive is opened. Already partially implemented (Phase 3) — ensure it catches ALL new entries. |

### Phase 6 — Monetization Surface

**Goal:** Make the Shop feel premium. Activate the existing cash system into a meaningful earnable currency. Deliver first cosmetic purchasables per MONETIZATION_VISION.md.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 6.1 | **Cash System Activation** | `ShopScreen.kt`, `ProgressionManager.kt`, `MissionRegistry.kt` | Activate the existing `totalCash` system as the earnable free currency. Cash earned via Missions, combo rewards, achievements. Spendable on: cosmetic skins (500-1000 cash), engine trails (300-800 cash), paint schemes (200-500 cash), continue credits (100 cash = 1 credit). Cash balance displayed on Shop, Main Menu, and Game Over. Ensure `grantReward(Cash)` is wired through all reward sources. |
| 6.2 | **Shop Redesign** | `ShopScreen.kt` | Remove V2 placeholder cards. Organize into tabs: COSMETICS (cash purchases), PREMIUM (real money), CREDITS (cash ↔ credits exchange). Premium card: "REMOVE ADS ($2.99)" with "ADS REMOVED ✓" state. Cash balance prominent at top. |
| 6.3 | **First Cosmetic Skins & Trails** | `RocketRenderer.kt`, `Player.kt`, `ShopScreen.kt` | 3 cash-purchasable rocket skins: Chrome (500 cash), Stealth (750 cash), Prototype-X (1000 cash). 3 engine trails: Solar Flare (300 cash), Void Glitch (500 cash), Plasma Blue (400 cash). Skin = color transform + minor geometry changes on existing Canvas rocket. Trail = particle color + pattern config. Optionally premium-exclusive variants for $0.99 each. |
| 6.4 | **Continue Credit Purchase** | `ShopScreen.kt`, `AdComponents.kt` | Credit exchange card: "BUY CREDITS — 100 cash = 1 credit" + "WATCH AD → +1 CREDIT" button. Shows current credit bank (X/10). |
| 6.5 | **Premium Value Display** | `SettingsScreen.kt` | "Go Premium" button shows feature comparison panel: ads removed, early access, profile badge, supporter recognition. |
| 6.6 | **Rewarded Ad In-Game Option** | `AdComponents.kt`, `GameEngine.kt` | Add "WATCH AD: REFUEL 50%" button that appears when fuel < 20%. One offer per run. Follows existing rewarded continue architecture. |

### Phase 7 — Online Features

**Goal:** Add optional cloud sync and leaderboards using existing Firestore infrastructure.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 7.1 | **Optional Google Sign-In** | `MainActivity.kt`, new `LoginManager.kt` | "Sign in with Google" button on Main Menu. Anonymous play remains default and fully supported. Login unlocks cloud features. |
| 7.2 | **Leaderboard** | `LeaderboardScreen.kt`, new `LeaderboardManager.kt` | Categories: Highest Altitude, Most Bosses Defeated, Longest Run Time. Firestore-backed. Friend tracking if signed in. |
| 7.3 | **Device Notifications (FCM)** | New: `NotificationService.kt`, `SettingsScreen.kt` | Integrate Firebase Cloud Messaging for push notifications. Opt-in via Settings toggle (default off). Use cases: daily bonus reminder, credit bonus grants, new content announcements, re-engagement. Topics-based targeting (all players, beta testers). Standard notification permission flow (API 33+). Tap → opens Main Menu. |
| 7.4 | **Server-Pushable Credit Bonuses** | New: `RemoteConfigManager.kt`, `ProgressionManager.kt` | Read remote config values from Firestore on app launch. Config key: `credit_bonus_grant` (int, default 0). If > 0, grant that many credits on next sync, then reset to 0. Enables server-side bonus pushes without app update. |
| 7.5 | **Cloud Save** | `ProgressionManager.kt`, new `CloudSyncManager.kt` | Sync player stats, progression, unlocks, settings to Firestore. Sync on login. Merge conflicts with "keep highest" strategy. |
| 7.6 | **Google Play Games Achievements** | `GameAnalytics.kt`, new `AchievementSyncManager.kt` | Map existing 10 achievements (ACHIEVEMENT_LIBRARY.md) to Google Play Games. No new achievements needed. |

### Phase 8 — Technical Foundation

**Goal:** Clean up deferred tech debt. Improve code health for sustained development.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 8.1 | **GameScreen Continued Decomposition** | `GameScreen.kt`, `GameEngine.kt` | Continue EPIC 8.5 extraction. Move remaining HUD rendering concerns to HudWidgets. Move game loop logic fully to GameEngine. Target: GameScreen < 500 lines. |
| 8.2 | **ProgressionManager Decomposition** | `ProgressionManager.kt` | Split into domain services: `ArtifactManager`, `ModuleInventory`, `MissionTracker`, `UnlockService`, `StatRecorder`. Wrap as `ProgressionService` interface. |
| 8.3 | **StarfieldBackground Extraction** | 6 Screen files → `StarfieldBackground.kt` | Extract 6× copy-pasted star animation into single shared composable. |
| 8.4 | **Navigation Migration** | `MainActivity.kt`, all Screen files | Ensure all overlay composables are NavHost routes. Add deep linking support. |
| 8.5 | **Performance Profiling** | Various | Profile frame drops in upper zones (Foundry, Chrono-Rift, Void) and dense threat fields. Optimize Canvas redraw regions. |

---

## 4. Dependency Graph

```
Phase 1: Visual Identity + Navigation
  │
  ├──► Phase 2: Hangar, Missions, Fleet (depends on Phase 1 navigation)
  │     │
  │     └──► Phase 5: Lore/Codex (depends on Phase 2 + Phase 3 HUD layout)
  │
  ├──► Phase 3: Gameplay UX (largely independent of Phase 1-2)
  │     │
  │     ├──► Phase 4: Canvas Visuals (depends on Phase 3 HUD coordinate space)
  │     │
  │     └──► Phase 6: Monetization (depends on Phase 3-4 visual quality)
  │
  ├──► Phase 7: Online Features (depends on Phase 1-3 for stable UX)
  │
  └──► Phase 8: Tech Debt (runs in parallel, prioritizes files being modified)
```

**Recommended execution order:** Phase 1 → Phase 3 → Phase 4 → Phase 2 → Phase 5 → Phase 6 → Phase 7, with Phase 8 as parallel background work when touching files.

---

## 5. Effort Estimate

| Phase | Description | Est. Days | Dependencies |
|-------|-------------|-----------|--------------|
| 1 | Visual Identity & Navigation | 5-7 | None |
| 2 | Hangar, Missions, Fleet | 8-12 | Phase 1 |
| 3 | Gameplay UX (HUD, Unlocks, Continue, Credits) | 7-12 | None (parallel with Phase 1/2) |
| 4 | Canvas Visual Upgrade | 8-14 | Phase 3 (coordinate space) |
| 5 | Discovery & Lore Surface | 3-5 | Phase 2, 3 |
| 6 | Monetization Surface + Cash System | 6-10 | Phase 3, 4 |
| 7 | Online Features (FCM, Remote Config, Login) | 7-12 | Phase 1-3 (stable UX) |
| 8 | Technical Foundation | 4-8 | Runs parallel |

**Total:** ~48-80 days development

---

## 6. Verification

### Build Gate
- `./gradlew assembleDebug` must succeed after every task

### Runtime Verification
- Main Menu renders with new hierarchy, all navigation routes work
- Hangar tabs switch correctly, stat comparison renders, chassis variants visible
- Mission timeline renders, progress tracks correctly
- HUD shows consolidated gauges, zone progress bar, codex button
- Unlock celebrations trigger for all unlock types
- Continue flow: loading modal → ad attempt → grace continue
- Boss arrival animation plays per boss type
- Rocket visual upgrades render per equipment state
- Skins/trails/paints apply and render on rocket
- Shop shows correct items, purchase flow works (test ads)
- Continue credits: watch ad → credit banked (max 10), spend at game over, persist across sessions
- Cash system: earned via missions/combo, displayed on Shop/Menu, spendable on skins/trails/credits
- Cash ↔ credit exchange works (100 cash → 1 credit)
- Server-pushable credit bonus via remote config
- FCM notification received on device, tap opens Main Menu
- Leaderboard reads/writes to Firestore
- Cloud save syncs correctly

### Visual Regression
- All 42 existing renderers must render identically (except intentional upgrades)
- Screenshots saved to `docs/screenshots/` for BEFORE/AFTER comparison

---

## 7. Design Library Traceability Matrix

| Feature | Library Source | Status |
|---------|---------------|--------|
| Chassis variants | `ROCKET_LIBRARY.md` §B Future Rockets | APPROVED (Stealth Hull) |
| Engine trails | `MONETIZATION_VISION.md` §2 Phase 3 | PLANNED |
| Rocket skins | `MONETIZATION_VISION.md` §2 Phase 2 | PLANNED |
| UI themes | `MONETIZATION_VISION.md` §2 | PLANNED |
| Fleet Packs | `MONETIZATION_VISION.md` §2 (long-term) | PLANNED |
| Premium cosmetics | `MONETIZATION_VISION.md` §2 Cosmetic Economy | PLANNED |
| Continue UX | `RELEASE_POLISH_PLAN.md` Phase 1 Item 16 | IMPLEMENTED (base) |
| Unlock celebration | `RELEASE_POLISH_PLAN.md` Phase 3 Item 13 | IMPLEMENTED (base) |
| Notification system | `RELEASE_POLISH_PLAN.md` Phase 2 | IMPLEMENTED |
| Boss death sequence | `RELEASE_POLISH_PLAN.md` Phase 1 Item 4 | IMPLEMENTED (base) |
| Archive badge | `RELEASE_POLISH_PLAN.md` Phase 3 | IMPLEMENTED (base) |
| Archive redesign | `RELEASE_POLISH_PLAN.md` Phase 4 Item 6 | IMPLEMENTED |
| GlobalAdBanner | `RELEASE_POLISH_PLAN.md` Phase 2 Item 17 | IMPLEMENTED |
| Premium purchase | `RELEASE_POLISH_PLAN.md` Phase 4 Item 18 | IMPLEMENTED (base) |
