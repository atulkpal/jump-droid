# JUMP DROID WEBSITE V2 — PROGRESS TRACKER

**Last Updated:** 2026-06-24 22:14 IST  
**Current Phase:** Phase D — Encounter System Overhaul  
**Lead:** AI Engineer  
**Mode:** Act Mode Required

---

## HOW TO CONTINUE THIS WORK

**Current state:** All original phases A, B, C are complete and the build passes. However, the experience is **too static**. Entities are positioned by scroll progress rather than feeling like actual game encounters.

**Next work (Phase D):** Build a data-driven encounter system where enemies enter from above, interact with the rocket, and bosses interrupt the climb with full-screen takeover moments. The rocket should actually fly, not sit fixed in the center.

**Files to read first:**
- `website/DISCOVERY_REPORT.md` — Full game entity data (bosses, threats, platforms, zones)
- `website/site/app/page.tsx` — Current main page (uses window.scrollY)
- `website/site/app/components/` — All existing components

**Critical rule:** The game is the source of truth. All entity names, behaviors, and visuals must match the game codebase. Do NOT invent new artwork for entities that exist in the game — recreate them from the game's draw functions.

---

## HOW TO ACCESS

### V2 Website (in development)
```bash
cd website/site
npm run dev
# → http://localhost:3000
```

### Old Website (backup)
```bash
cd website/old-site
npm run dev -- -p 3001
# → http://localhost:3001/old
```

---

## CURRENT STATUS

```
🟢 Phase A: Foundation (Complete — A1 + A2 + A3)
🟢 Phase B1: Scroll Architecture (Complete)
🟢 Phase B2: Zone Backgrounds (Complete)
🟢 Phase B3: Interactive Elements (Complete)
🟢 Phase C1: Performance (Complete)
🟢 Phase C2: Mobile (Complete)
🟢 Phase C3: Surveyer Companion (Complete)
🟢 Phase D: Encounter System Overhaul (Complete)

✅ ALL PHASES COMPLETE
```

---

## ✅ COMPLETED PHASES (A through C)

### Phase A1: Content Accuracy Fixes ✅
- Corrected zone names (added Upper Atmosphere, removed The Signal as zone)
- Updated platform count from 5 to 10
- Added rocket traits and unlock milestones
- Fixed boss descriptions with actual mechanics
- Updated discovery categories to match real game data

### Phase A2: SVG Entity Library ✅
- Created `app/components/game/` directory
- `GameColors.ts` — Full SciFi color palette
- `RocketSVG.tsx` — Rocket with body, fins, cockpit, flame, shield arcs
- `PlatformSVG.tsx` — All 10 platform types
- `ThreatSVG.tsx` — All 8 enemy types
- CSS animation keyframes in globals.css

### Phase A3: Framer Motion Integration ✅
- Installed framer-motion
- Created `useScrollProgress`, `useInView`, `useParallax` hooks
- Motion wrappers on RocketSVG

### Phase B1: Scroll Architecture ✅
- `ScrollContainer.tsx` — throttled rAF scroll wrapper
- `AltitudeHUD.tsx` — altitude counter, zone name, progress bar
- `ZoneBackground.tsx` — framer-motion parallax backgrounds
- `RocketContainer.tsx` — motion-based rocket sway

### Phase B2: Zone Backgrounds ✅
- 6 zone background components (Earth through The Void)
- `ZoneBackgrounds.tsx` — orchestrator with lazy-loading via next/dynamic

### Phase B3: Interactive Elements ✅
- `PlatformShowcase.tsx` — 10 platforms with hover descriptions
- `BossEncounter.tsx` — boss fly-in with spring animation
- `SurveyerCompanion.tsx` — friendly drone + messages (TO BE REMOVED in Phase D)

### Phase C1: Performance ✅
- Throttled scroll handlers with rAF
- Lazy-loaded zone backgrounds
- Optimized starfield

### Phase C2: Mobile ✅
- Touch support (webkitOverflowScrolling)
- Reduced animation mode (prefers-reduced-motion)
- Responsive HUD sizing

### Phase C3: Surveyer Companion ✅
- `SurveyerSVG.tsx` — friendly drone design
- `SurveyerCompanion.tsx` — drone + message bubble
- **NOTE:** The friendly Surveyer companion does NOT exist in the game. It was a narrative device from the vision docs. The game only has the ENEMY "Surveyor Probe" (ENT_SCOUT_DRONE). **These components must be removed/replaced in Phase D.**

---

## ✅ PHASE D: ENCOUNTER SYSTEM OVERHAUL (COMPLETE)

### Problem Statement

The current site is **too static and section-oriented**:
- Rocket sits fixed in center — it should fly
- Entities are positioned by scroll progress — they should enter from above
- Bosses are static overlays — they should interrupt with phases
- No interaction between rocket and entities
- Everything feels like a website, not a game encounter

### Solution: Data-Driven Encounter System

Build a single data array that drives all encounters. Each encounter has enter/peak/exit phases. The rocket flies through the world. Entities enter from above and interact with the rocket.

---

### Phase D1: Remove Friendly Surveyer, Fix Enemy Model ✅
- [x] Deleted `SurveyerMessage.tsx`, `SurveyerCompanion.tsx`, `game/SurveyerSVG.tsx`
- [x] Updated `page.tsx` to remove friendly Surveyer
- [x] Confirmed: Surveyor Probe is an ENEMY (tracks + summons), not a companion

---

### Phase D2: Data-Driven Encounter System ✅
- [x] Created `EncounterSystem.tsx` with `ENCOUNTERS` + `BOSS_ENCOUNTERS` arrays
- [x] Encounter data: Surveyor Probe, Sky Ray, Swarm, Defense Node, Derelict, Void Tracker, Leviathan
- [x] Boss data: Command Cruiser, Gatekeeper, Void Engine
- [x] `getLocalProgress`, `getXPosition`, `getScale` logic

This is the **core new component**. It renders all encounters based on scroll progress.

```typescript
// Data structure for encounters
interface Encounter {
  type: 'platform' | 'enemy' | 'boss' | 'powerup' | 'warning';
  progress: [number, number, number]; // [enter, peak, exit] — 0.0 to 1.0
  entity: string; // entity type identifier
  zone: string;
  summons?: string[]; // enemies spawned during encounter
  behavior?: string; // 'track-rocket' | 'drift-across' | 'orbit' | 'summon'
  message?: string; // brief zone warning text
  phases?: BossPhase[]; // for bosses only
}
```

**Encounter array (full data):**
```typescript
const ENCOUNTERS = [
  // Surveyor Probe — tracks rocket, summons swarm
  {
    type: 'enemy',
    progress: [0.05, 0.12, 0.18],
    entity: 'SURVEYOR_PROBE',
    zone: 'Earth',
    behavior: 'track-rocket',
    summons: ['AEROSOL_SWARM', 'AEROSOL_SWARM'],
    message: "Contact detected. Drone swarm incoming."
  },
  // Sky Ray — drifts across screen
  {
    type: 'enemy',
    progress: [0.22, 0.28, 0.35],
    entity: 'SKY_RAY',
    zone: 'Cloud Layer',
    behavior: 'drift-across'
  },
  // ... more enemies
];
```

**Platform encounters (continuous throughout):**
```typescript
// Platforms that drift past as rocket flies up
{
  type: 'platform',
  progress: [0.08, 0.14, 0.20], // staggered windows
  entity: 'NORMAL',
  zone: 'Earth'
},
// ... 12+ platform encounters
```

**Boss encounters:**
```typescript
{
  type: 'boss',
  progress: [0.30, 0.50, 0.65], // longer encounter window
  entity: 'COMMAND_CRUISER',
  zone: 'Orbit',
  phases: [
    { name: 'ENTER', duration: 0.05 }, // boss flies in
    { name: 'FIGHT', duration: 0.10 }, // boss active, platforms jammed
    { name: 'EXIT', duration: 0.05 }, // boss flees
  ]
}
```

---

### Phase D3: EncounterEntity ✅ (merged into EncounterSystem)
- [x] Encounter rendering logic built into `EncounterSystem.tsx`
- [x] Supports platforms, enemies, bosses with enter/peak/exit

Reusable component for all encounter entities. Handles enter/peak/exit phases automatically.

```typescript
interface EncounterEntityProps {
  encounter: Encounter;
  progress: number; // 0-1 overall scroll progress
  rocketX: number; // rocket screen position for tracking behavior
  onEncounterEnd?: (entity: string) => void;
}
```

**Behavior:**
- Calculate `localProgress` from `encounter.progress` vs overall `progress`
- **ENTER phase** (0.0 → 0.3): fly in from above, scale up
- **PEAK phase** (0.3 → 0.7): hold position, execute behavior
  - For `track-rocket`: move toward rocketX
  - For `drift-across`: move horizontally
  - For `summon`: spawn minions around entity
- **EXIT phase** (0.7 → 1.0): drift away, scale down

Rendering:
- Platforms: use `PlatformSVG` positioned off to the side
- Enemies: use `ThreatSVG` with motion animations
- Bosses: use `BossEncounter` with phase-specific behavior
- Warnings: text overlay that fades in/out

---

### Phase D4: FlyingRocket ✅
- [x] Created `FlyingRocket.tsx` with flight path (bottom 12%→54%, left sway)
- [x] Spring-based motion, continuous sine rotation + scale

Rocket that actually flies, not fixed center.

```typescript
interface FlyingRocketProps {
  progress: number;
  encounterProgress: number; // are we in an encounter?
  bossBehavior?: string; // boss-specific rocket behavior
}
```

**Flight path:**
- **Start** (progress 0): `bottom: 10%, left: 30%` — launch position
- **Climb** (progress 0 → 0.8): `bottom: 55%, left: 50%` — flies upward and centers
- **End** (progress 0.8 → 1.0): `bottom: 45%, left: 50%` — stabilizes

**Sway:**
- `left` offset: `Math.sin(progress * 20) * 5%` — continuous gentle sway
- `rotation`: `Math.sin(progress * 15) * 3deg` — tilts with sway

**Encounter reactions:**
- Enemy tracks rocket: rocket dodges (small rapid movement)
- Boss fight: rocket shrinks to 70%, shifts to `left: 40%` (avoiding center)
- Platform pass: rocket pulses (brief size increase)

**Rendering:**
- Uses `RocketSVG` internally
- Wrapped in `motion.div` for smooth position/rotation
- `transition={{ type: "spring", stiffness: 100, damping: 15 }}` for organic feel

---

### Phase D5: BossEncounter v2 ✅
- [x] Rewrote `BossEncounter.tsx` with ENTER/FIGHT/EXIT phases
- [x] Added boss-specific visuals: red glow, warning overlay, phase indicator
- [x] Extended `ThreatSVG.tsx` with all 6 boss types

New version with proper phases.

```typescript
interface BossEncounterProps {
  boss: {
    name: string;
    behavior: string;
    phases: BossPhase[];
    message: string;
  };
  activePhase: number; // 0=enter, 1=fight, 2=exit
  phaseProgress: number; // 0-1 within current phase
  onComplete: () => void;
}
```

**Boss-specific behaviors:**

| Boss | Enter | Fight Behavior | Exit |
|---|---|---|---|
| Command Cruiser | Descends from top, red glow | Platforms turn red/jammed, flashes | Ascends, leaves red trail |
| The Gatekeeper | Rotating shield appears | Safe zone rotates (CSS animation on shield segments) | Shield retracts |
| Star-Eater | Grows from top | Expands/contracts (absorbing effect) | Shrinks rapidly |
| Void Engine | Reality distortion (screen tilt) | Everything sways, rocket struggles | Distortion snaps back |

**Visual elements:**
- Boss name + "THREAT DETECTED" overlay
- Phase indicator (ENTER → FIGHT → EXIT)
- Boss-specific visual effects (red flash for jammed platforms, rotating shield for Gatekeeper)
- Warning message from zone system

---

### Phase D6: FinaleArchive ✅
- [x] Created `FinaleArchive.tsx` (appears at progress > 0.92)
- [x] Sections: Rocket Classes, Platforms, Hazards, Enemies, Bosses
- [x] Uses actual game entity data from DISCOVERY_REPORT.md

Appears at `progress > 0.92`. Shows complete ecosystem.

```typescript
interface FinaleArchiveProps {
  progress: number;
}
```

**Layout:**
- Full-screen overlay with dark background
- Sections: "THREATS" | "PLATFORMS" | "ROCKETS" | "BOSSES"
- Grid of entity cards
- Each card: SVG preview + name + zone + behavior
- Rocket flies in front of archive (small, centered)

**Data sources:**
- All 31 threats (organized by type: hazards, enemies, bosses)
- All 10 platforms
- All 4 rocket classes
- All 6 bosses with their mechanics

---

### Phase D7: page.tsx Rewrite ✅
- [x] Replaced static layout with encounter-driven architecture
- [x] Wires EncounterSystem + FlyingRocket + BossEncounter + FinaleArchive
- [x] Data-driven: `getCurrentEncounter`, `getBossPhase`, `calculateRocketX`

**Structure:**
```tsx
export default function Home() {
  // Scroll state
  const [scrollTop, setScrollTop] = useState(0);
  const [scrollHeight, setScrollHeight] = useState(0);
  const [clientHeight, setClientHeight] = useState(0);

  // Derived
  const maxScroll = Math.max(scrollHeight - clientHeight, 1);
  const progress = Math.min(scrollTop / maxScroll, 1);
  const altitude = Math.round(progress * TOTAL_ALT);

  // Current zone
  let zone = ZONES[0];
  for (const z of ZONES) { if (altitude >= z.threshold) zone = z; }

  // Current active encounter
  const activeEncounter = ENCOUNTERS.find(e => {
    const [enter, peak, exit] = e.progress;
    return progress >= enter && progress <= exit;
  });

  // Current boss phase
  const bossPhase = getCurrentBossPhase(activeEncounter, progress);

  // Rocket flight position
  const rocketFlight = calculateRocketFlight(progress, activeEncounter);

  return (
    <>
      {/* Scroll spacer */}
      <div style={{ height: '600vh', pointerEvents: 'none' }} />

      {/* Zone backgrounds (lazy-loaded) */}
      <ZoneBackgrounds altitude={altitude} />

      {/* Platforms (continuous drift) */}
      <PlatformDrift progress={progress} />

      {/* Encounters (enter/peak/exit) */}
      <EncounterSystem
        encounters={ENCOUNTERS}
        progress={progress}
        rocketX={rocketFlight.x}
      />

      {/* Rocket (flies through world) */}
      <FlyingRocket
        progress={progress}
        encounter={activeEncounter}
        position={rocketFlight}
      />

      {/* Boss encounter (takes over screen) */}
      {activeEncounter?.type === 'boss' && (
        <BossEncounter
          boss={activeEncounter}
          phase={bossPhase}
          phaseProgress={bossPhaseProgress}
        />
      )}

      {/* HUD */}
      <AltitudeHUD altitude={altitude} zoneName={zone.name} progress={progress} />

      {/* Zone warning overlay */}
      {activeEncounter?.message && (
        <ZoneWarning message={activeEncounter.message} />
      )}

      {/* Finale archive */}
      {progress > 0.92 && <FinaleArchive progress={progress} />}

      {/* CTAs */}
      <StartCTA progress={progress} />
      <EndCTA progress={progress} />
    </>
  );
}
```

---

### KEY TECHNICAL DECISIONS

| Decision | Implementation |
|---|---|
| Scroll drives everything | `window.scrollY` → progress → all positions |
| Encounter windows | Each encounter has [enter, peak, exit] in progress space |
| Rocket flight path | `bottom: 10% → 55%`, `left: 30% → 50%` with sine sway |
| Enemy tracking | Enemy X position interpolates toward rocketX during peak phase |
| Boss phases | State machine: ENTER → FIGHT → EXIT with timers |
| Platform drift | Platforms float from right to left, staggered by progress |
| Finale trigger | `progress > 0.92` shows archive overlay |

---

### GAME ENTITY DATA (from DISCOVERY_REPORT.md)

**Threats to include:**
- Surveyor Probe (tracks + summons)
- Sky Ray (drifts across)
- Aerosol Swarm (orbiting minions)
- Defense Node (stationary turret)
- Derelict Echo (fades through)
- Void Tracker (hunts player)
- Cosmic Leviathan (looming, slow)
- Shadow Entity (stalking, unpredictable)

**Bosses with behaviors:**
- Command Cruiser (PLATFORM_CONSUMER — jams platforms)
- The Gatekeeper (PROJECTILE_SHOOTER — rotating safe zones)
- Star-Eater (ICE_CONVERTER — expands/contracts)
- Void Engine (WIND_MAKER — gravity distortion)
- The Leviathan (ITEM_STEALER — moving slipstreams)
- The Signal (VOID_SERPENT — false navigation)

**Platforms to drift past:**
All 10 types: Normal, Moving, Boost, Ice, Breakable, Phase, Fuel, Cooling, Stability, Magnetic

---

### BUILD STATE BEFORE STARTING

```
✓ Compiled successfully
✓ TypeScript passed
✓ All pages generated (static)
```

No build errors. Ready to proceed.