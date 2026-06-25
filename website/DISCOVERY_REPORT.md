# JUMP DROID WEBSITE V2 — COMPREHENSIVE DISCOVERY REPORT

**Date:** 2026-06-24  
**Author:** AI Engineer  
**Scope:** Full codebase audit of both website (`/website/site`) and game (`/app/src/main/java/com/example/jump_droid/`)

---

## TABLE OF CONTENTS

1. [Website Analysis](#website-analysis)
2. [Game Analysis](#game-analysis)
3. [Content Accuracy Audit](#content-accuracy-audit)
4. [Asset Extraction Opportunities](#asset-extraction-opportunities)
5. [Redesign Proposal](#redesign-proposal)
6. [Technical Feasibility Assessment](#technical-feasibility-assessment)
7. [Recommended Implementation Plan](#recommended-implementation-plan)

---

## PHASE 1: WEBSITE ANALYSIS

### Current Architecture

- **Framework:** Next.js 14+ with App Router
- **Styling:** Tailwind CSS with custom CSS variables
- **Fonts:** Inter (body), JetBrains Mono (monospace)
- **Components:** 9 total components
- **Layout:** Traditional linear scroll page
- **Rendering:** All static HTML/CSS — no canvas, SVG, or animation libraries
- **State:** No client-side state management
- **Dependencies:** Minimal (Next.js, React, Tailwind)

### Key Components & Assessment

| Component | Current State | Issue | Action |
|---|---|---|---|
| **StickyNav** | Fixed nav with anchor links | Functional | Keep as-is |
| **AltitudeSidebar** | Zone labels in sidebar | Should be interactive scroll indicator | Enhance |
| **HeroSection** | Static text + CTA buttons | No rocket, no animation, no launch pad feel | Replace |
| **GameplayExplained** | 4 static cards | Accurate but no interactive demo | Keep/Enhance |
| **BossShowcase** | 6 text-only cards | No visual representation | Replace |
| **RocketShowcase** | 4 cards with text stats | No visual rocket rendering | Replace |
| **DiscoveryArchive** | 6 generic category cards | Placeholder text, wrong counts | Replace |
| **ProgressionSystems** | 3-column text layout | No visual progression elements | Replace |
| **MissionControl** | Newsletter signup form | Functional | Keep as-is |

### Reusable Elements

- Tailwind theming system (colors, spacing, typography)
- Card pattern (rounded-2xl border bg-white/5)
- Gradient overlay patterns
- Grid layout patterns
- Zone color scheme (cyan for primary, purple for mystery, gold for rewards)

### Technical Constraints

- No Framer Motion in dependencies
- No Canvas/SVG components
- No scroll-driven animation infrastructure
- Mobile-responsive via Tailwind breakpoints
- No backend/API integration

---

## PHASE 2: GAME ANALYSIS

### 🚀 Rocket Classes (from `Models.kt`)

Source: `app/src/main/java/com/example/jump_droid/Models.kt` (lines 85-99)

```kotlin
enum class RocketType(
    val title: String,
    val traitName: String,
    val traitDescription: String,
    val thrustMult: Float,
    val fuelMult: Float,
    val heatMult: Float,
    val unlockScore: Int,
    val discovery: DiscoveryType
) {
    BALANCED("Explorer", "Sensor Array", "Native +20% discovery range.", 1.0f, 1.0f, 1.0f, 0, ...),
    SCOUT("Striker", "Target Lock", "Precision strikes on weak points.", 1.25f, 0.7f, 0.9f, 2000, ...),
    TANK("Heavy", "Kinetic Mass", "Impact shockwaves on weak point destruction.", 0.85f, 1.5f, 0.8f, 5000, ...),
    EXPERIMENTAL("Prototype", "Overclocked Core", "Retain steering authority while overheated.", 1.5f, 1.0f, 1.4f, 10000, ...)
}
```

| Class | In-Game Name | Unlock Score | Thrust | Fuel | Heat | Unique Trait |
|---|---|---|---|---|---|---|
| BALANCED | Explorer | 0 (start) | 1.0x | 1.0x | 1.0x | Sensor Array (+20% discovery range) |
| SCOUT | Striker | 2,000 | 1.25x | 0.7x | 0.9x | Target Lock (precision weak points) |
| TANK | Heavy | 5,000 | 0.85x | 1.5x | 0.8x | Kinetic Mass (impact shockwaves) |
| EXPERIMENTAL | Prototype | 10,000 | 1.5x | 1.0x | 1.4x | Overclocked Core (steer while overheated) |

**Rocket Colors:** BALANCED=SciFiWhite, SCOUT=SciFiGold, TANK=Color(0xFF455A64), EXPERIMENTAL=SciFiPurple

**Rocket Dimensions:** 40w × 70h pixels

### Rockets Canvas Visual (from `RocketRenderer.kt`)

The rocket is rendered with:
- **Main Fuselage:** Rectangle with panel lines (horizontal lines at 25%, 50%, 75%)
- **Nose Cone:** Triangle at top (dark gray)
- **Engine Nozzle:** Small rectangle at bottom (dark gray with inner detail)
- **Fins:** Two triangles on sides (SciFiRed)
- **Cockpit:** Circle with glow (SciFiCyan)
- **Edge highlights:** White lines on right side and nose cone
- **Thruster Flame:** Outer flame (SciFiGold to transparent) + inner flame (white to cyan) with flicker animation
- **Side Thrusters:** Small cyan flames when maneuvering
- **Shield Effect:** Rotating arc plates around rocket (SciFiCyan, density varies by shield %)
- **Damage Effects:** Black circles on hull + spark effects when damaged
- **Destruction Sequence:** Flash → break into debris → tumble as dark gray
- **Squash/Stretch:** Scale animation on landing

### 🛸 Surveyer Drone

**CRITICAL FINDING:** The friendly "Surveyer" companion drone referenced in the V2 vision documents does **NOT exist** as a gameplay entity in the current codebase. 

What does exist:
- `ThreatType.ENEMY` entry in `ThreatRegistry.kt`: "Surveyor Probe" — an autonomous enemy drone guarding lower altitudes
- No companion/friendly Surveyer class, renderer, or behavior exists

**Implication:** The Surveyer companion for V2 will require original design and creation.

### 🏗️ Platform Systems (from `Platform.kt` + `PlatformRenderer.kt`)

**10 Platform Types CONFIRMED in game code:**

| # | Type | Color | Behavior | Visual Features |
|---|---|---|---|---|
| 1 | NORMAL | SciFiWhite | Safe landing zone | Base with edge highlights, border |
| 2 | MOVING | SciFiCyan | Oscillates horizontally | Arrow indicators showing direction |
| 3 | BOOST | SciFiGold | Launches upward | Flame gradient above, arrow symbol |
| 4 | ICE | SciFiCyan (transparent) | Slippery surface | Shimmer gradient, crystal lines, sparkles |
| 5 | BREAKABLE | SciFiRed | Collapses (2.4s) | Hazard stripes, growing cracks |
| 6 | PHASE | SciFiPurple | Appears/disappears (4s cycle) | Scan line, opacity cycling |
| 7 | FUEL | SciFiGreen | Restores fuel | "FUEL" label, corner decorations |
| 8 | COOLING | SciFiCyan | Reduces heat | "COOL" label, corner decorations |
| 9 | STABILITY | SciFiWhite | Improves control | "STAB" label, corner decorations |
| 10 | MAGNETIC | SciFiPurple | Gravity influence field | Expanding ring pulses |

**Platform Properties:**
- Height: 20px (constant `PLATFORM_HEIGHT`)
- Width: Variable per platform
- Visual border: `SciFiBorder` (white at 0.1 alpha)
- Zone-adaptive: Platforms gain emissive glow in higher zones (Upper Atmosphere+)
- Jammed state: Red flash overlay when boss-interrupted

### 👾 Boss Systems (from `Boss.kt` + `ThreatRegistry.kt`)

**6 Boss Encounters CONFIRMED:**

| Boss | Type | Behavior | Description |
|---|---|---|---|
| Command Cruiser | MINI_BOSS | PLATFORM_CONSUMER | A tactical vessel that jams landing platforms |
| The Gatekeeper | BOSS | PROJECTILE_SHOOTER | Ancient orbital defense with rotating safe zones |
| Star-Eater | BOSS | ICE_CONVERTER | Massive cosmic organism consuming light/energy |
| The Void Engine | BOSS | WIND_MAKER | Reality-warping machine altering gravity direction |
| The Leviathan | BOSS | ITEM_STEALER | Gigantic creature creating moving slipstreams |
| The Signal | BOSS | VOID_SERPENT | Unknown intelligence creating false navigation cues |

**Boss Properties:**
- Each has: `health`, `timer` (countdown), `x`, `y`, `isActive` flag
- Associated DiscoveryType for codex entry
- Tier system (MINI_BOSS=Tier 4, BOSS=Tier 5)
- Zone-specific spawn rules

### ⚡ Powerups (from `PowerUpType` enum)

Source: `Models.kt` lines 10-13

```kotlin
enum class PowerUpType {
    FUEL_TANK, TURBO_BOOSTER, EFFICIENCY_MODULE, HEAT_SINK, 
    ARTIFACT, ALTITUDE_BOOSTER, SHIELD_CAPSULE, HULL_REPAIR
}
```

**8 Powerup Types:**
1. FUEL_TANK — Increases max fuel capacity
2. TURBO_BOOSTER — Increases thrust power
3. EFFICIENCY_MODULE — Reduces fuel consumption
4. HEAT_SINK — Instantly removes heat
5. ARTIFACT — Special discovery item
6. ALTITUDE_BOOSTER — Score/altitude bonus
7. SHIELD_CAPSULE — Shield restore
8. HULL_REPAIR — Integrity restore

### ☢️ Hazards / Threats (from `ThreatRegistry.kt` + `ThreatDefinition.kt`)

**31 Total Threats across 4 tiers:**

**8 Environmental Hazards:**
| ID | Name | Zones | Tier |
|---|---|---|---|
| HAZ_LIGHTNING | Lightning Storm | Cloud→Void | 2 |
| HAZ_TURBULENCE | Turbulence Front | Cloud→Void | 1 |
| HAZ_DEBRIS | Debris Field | Upper Atmos→Void | 2 |
| HAZ_RADIATION | Radiation Zone | Orbit→Void | 3 |
| HAZ_SOLAR_FLARE | Solar Flare | Orbit→Void | 3 |
| HAZ_EMP | EMP Pulse | Orbit→Void | 3 |
| HAZ_GRAVITY | Gravity Distortion | Deep Space→Void | 4 |
| HAZ_VOID_ANOMALY | Void Anomaly | Deep Space→Void | 4 |

**8 Enemy Entities:**
| ID | Name | Zones | Tier |
|---|---|---|---|
| ENT_SCOUT_DRONE | Surveyor Probe | Earth→Orbit | 1 |
| ENT_CLOUD_SKIMMER | Sky Ray | Cloud Layer | 1 |
| ENT_SWARM_BOTS | Aerosol Swarm | Cloud→Upper Atmos | 2 |
| ENT_ORBITAL_SENTRY | Defense Node | Orbit | 2 |
| ENT_CORRUPTED_HULL | Derelict Echo | Deep Space | 2 |
| ENT_STALKER | Void Tracker | Deep Space | 3 |
| ENT_VOID_WHALE | Cosmic Leviathan | Deep Space→Void | 3 |
| ENT_VOID_WRAITH | Shadow Entity | Void | 3 |

**1 Mini-Boss + 6 Bosses** (listed above)

### 🗺️ Zone Progression (from `AltitudeZone.kt` + `Constants.kt`)

Source: `AltitudeZone.kt` (lines 10-27), `Constants.kt` (lines 32-37)

```kotlin
enum class AltitudeZone(val threshold: Int, val zoneName: String, val subtitle: String) {
    EARTH(0, "Earth", "The Journey Begins"),
    CLOUD_LAYER(500, "Cloud Layer", "Above The Clouds"),
    UPPER_ATMOSPHERE(1500, "Upper Atmosphere", "Edge Of The Sky"),
    ORBIT(4000, "Orbit", "First Orbital Ascent"),
    DEEP_SPACE(8000, "Deep Space", "Uncharted Territory"),
    VOID(15000, "The Void", "Unknown Region Detected")
}
```

| Zone | Threshold | Subtitle | Background | Special Effects |
|---|---|---|---|---|
| Earth | 0m | The Journey Begins | Blue sky → green ground | Sun, mountain silhouettes |
| Cloud Layer | 500m | Above The Clouds | Cyan → purple gradient | Cloud layers, lightning flashes |
| Upper Atmosphere | 1,500m | Edge Of The Sky | Dark purple gradient | Aurora borealis, atmospheric dust |
| Orbit | 4,000m | First Orbital Ascent | Near-black with stars | Stars with flares, distant planet, golden debris |
| Deep Space | 8,000m | Uncharted Territory | Black with nebulae | Nebulae, galaxy swirl, derelict structures |
| The Void | 15,000m | Unknown Region Detected | Pure black → red pulse | Distortion ripples, red radial pulse |

### 🎯 Mission Systems (from `MissionManager.kt` + `MissionType.kt`)

**3 Core Mission Tracks:**
1. **EXPLORATION** — Tracked by altitude progression, zone discovery
2. **PLATFORMING** — Tracked by combo count, consecutive landings
3. **SURVIVAL** — Tracked by damage avoidance, shield management

**15 Mission Templates** total. Active 3 at a time (one per track). Rotating selection prevents repetition.

### 📚 Codex/Discovery System (from `DiscoveryType` enum in `Models.kt`)

**43 Total Discoveries across 8 Categories:**

| Category | Count | Entries |
|---|---|---|
| PLATFORMS | 5 | Normal, Moving, Ice, Boost, Breakable, Phase, Fuel, Cooling, Stability, Magnetic |
| POWERUPS | 4 | Fuel Tank, Turbo Booster, Efficiency Module, Heat Sink |
| MECHANICS | 3 | Engine Heat, Overheated, Combat Efficiency |
| AREAS | 6 | Earth, Cloud Layer, Upper Atmosphere, Orbit, Deep Space, The Void |
| ROCKETS | 4 | Explorer, Striker, Heavy, Prototype |
| LORE | 4 | The Ascension Program, The First Signal, The Lost Fleet, The Ascension Logs |
| ARTIFACTS | 4 | Flight Recorder, Unknown Alloy, Encrypted Beacon, Drone Core |
| THREATS | 13 | Lightning Storm, Debris Field, Radiation Zone, Solar Flare, Turbulence, Gravity, EMP + The Sentinel, Void Engine, Gatekeeper, Star-Eater, Leviathan, The Signal |
| **Total** | **43** | |

### 🏅 Player Resource System (from `Player` class in `Models.kt`)

- **Fuel:** 100 base, max 300, consumed by thrusting, recharges on platforms
- **Heat:** 100 base, max 300, generated by thrusting, 100 = overheat (2s cooldown)
- **Shield:** 50 base, regenerates after 4s delay, damaged by impacts
- **Integrity:** 100 base, permanent hull damage, no regeneration
- **Combo:** Consecutive platform lands, 5+ = shield restore
- **Powerup Timers:** turbo, efficiency, stability, control inversion, HUD interference
- **Special States:** invulnerability (post-damage), infiniteShield, invincibleHull

### 🎨 Zone Background Rendering (from `ZoneBackgroundRenderer.kt`)

Each zone has a full parallax rendering system:

**Earth (615 lines):**
- Gradient: Blue → Green (top to bottom)
- Parallax layers: Mountain silhouettes (2 layers), cloud circles, sun
- Color transition: `0xFF1A237E → 0xFF2196F3 → 0xFF66BB6A`

**Cloud Layer:**
- Gradient: Cyan → Purple (evolving)
- 4 parallax cloud layers with drift animation
- Lightning flash effects (every 8s)
- Color transition: `0xFF00BCD4 → 0xFF4DD0E1 → 0xFFE0F7FA`

**Upper Atmosphere:**
- Gradient: Dark Purple
- Aurora borealis (5 sinusoidal bands, green → purple)
- Atmospheric dust particles
- Color transition: `0xFF0D001A → 0xFF1A0033 → 0xFF311B92`

**Orbit:**
- Gradient: Near-black
- 80 stars with twinkle + flare effects
- Distant planet, golden debris glow
- Orbit curve (golden arc)
- Color transition: `0xFF000411 → 0xFF0D001A → 0xFF1A0033`

**Deep Space:**
- Gradient: Pure black
- 100 stars (some blue-tinted)
- 2 nebulae (purple/magenta)
- 1 blue nebula
- Derelict structures (rectangular shapes)
- Galaxy swirl
- Color: Pure black

**The Void:**
- Gradient: Pure black with red pulses
- 30 red-tinted stars
- 3 distortion ripples (expanding red circles)
- Red radial pulse overlay
- Color: Pure black → `0xFFFF1744` pulse

### Game Color Palette (from `ui/theme/Color.kt`)

```kotlin
val SciFiWhite = Color(0xFFFFFFFF)
val SciFiCyan = Color(0xFF00E5FF)   // Progression / Missions / Combo
val SciFiGold = Color(0xFFFFD700)   // Rewards / Achievements
val SciFiRed = Color(0xFFFF1744)    // Danger / Heat / Critical
val SciFiGreen = Color(0xFF00E676)  // Recovery / Fuel
val SciFiPurple = Color(0xFFD500F9) // Discovery / Mystery
val SciFiBackground = Color(0xFF0D001A)
val SciFiSurface = Color(0xFF1A1A1A).copy(alpha = 0.6f)
val SciFiBorder = Color(0xFFFFFFFF).copy(alpha = 0.1f)
```

---

## PHASE 3: CONTENT ACCURACY AUDIT

### Critical Inaccuracies

| Claim | Website Says | Game Reality | Severity |
|---|---|---|---|
| **Zone Names** | Earth, Cloud Layer, Orbit, Deep Space, The Void, The Signal | Earth, Cloud Layer, **Upper Atmosphere**, Orbit, Deep Space, The Void | 🔴 MISSING ZONE |
| **The Signal as Zone** | Listed as a zone | The Signal is a **boss**, not a zone | 🔴 INCORRECT |
| **Zone Count** | "Six atmospheres" | 6 zones but names are wrong | 🔴 INACCURATE |
| **Rocket Traits** | Not mentioned | Each rocket has unique passive trait | 🔴 MISSING |
| **Boss Behaviors** | Generic quotes only | Each has specific mechanics | 🟠 INACCURATE |
| **Platform Count** | "5 types" | 10 types (5 missing) | 🔴 WRONG |
| **Artifact Count** | "5 items" | 5 artifacts + 4 powerups = 9 | 🟠 INACCURATE |
| **Threat Count** | "8 enemies" | 8 enemies + 8 hazards + 1 mini-boss + 6 bosses = 23 visible | 🟠 UNDERSOLD |
| **Combo System** | Not mentioned | Core gameplay loop (5 lands = shield restore) | 🔴 MISSING |
| **Mission System** | Not mentioned | 15 templates across 3 tracks | 🟠 MISSING |
| **Ascension Ranks** | Not mentioned | 5 tiers (Novice → Void Explorer) | 🟠 MISSING |
| **Lore Count** | "12 entries" | 4 lore entries currently | 🟠 OVERSTATED |
| **Discovery Count** | "43 total" | Shows 6 of 43 | 🟠 UNDERREPRESENTED |
| **Upper Atmosphere Zone** | Not listed | Exists at 1,500m threshold | 🔴 MISSING |

### What to Keep

- "The Signal From the Void" tagline
- Existing visual atmosphere (cyan/purple/gold color scheme)
- Sci-fi technical tone
- Existing deployment configuration
- Existing technology stack (Next.js, Tailwind)

---

## PHASE 4: ASSET EXTRACTION OPPORTUNITIES

### Entities Recreatable as SVG

| Game Entity | Game Rendering | SVG Feasibility | Notes |
|---|---|---|---|
| **Rocket Body** | Rect + Triangle (nose) + Triangle (fins) + Circle (cockpit) | ✅ TRIVIAL | Simple geometric shapes |
| **Thruster Flame** | Curved paths + gradients + flicker | ✅ EASY | SVG path + CSS animation |
| **Shield Plates** | Rotating arc segments | ✅ EASY | CSS transforms on arcs |
| **Normal Platform** | Rectangle with highlights | ✅ TRIVIAL | SVG rect |
| **Moving Platform** | Rectangle + arrow indicators | ✅ TRIVIAL | SVG rect + path arrows |
| **Boost Platform** | Rectangle + gradient flame + arrow | ✅ EASY | SVG + CSS gradient |
| **Ice Platform** | Rectangle + shimmer + crystals | ✅ MEDIUM | SVG + CSS shimmer animation |
| **Breakable Platform** | Rectangle + stripe pattern + cracks | ✅ MEDIUM | SVG clip paths |
| **Phase Platform** | Rectangle + opacity cycle + scan line | ✅ EASY | CSS opacity animation |
| **Fuel/Cooling/Stability** | Rectangle + label + corner dots | ✅ TRIVIAL | SVG rect + text |
| **Magnetic Platform** | Rectangle + expanding ring pulses | ✅ EASY | CSS radial animation |
| **Stars** | Circles with twinkle | ⚠️ MEDIUM | Canvas better for performance |
| **Clouds** | Overlapping ovals with drift | ✅ EASY | CSS animated clouds |
| **Aurora** | Sinusoidal bands | ⚠️ MEDIUM | SVG path animation |

### Entities Requiring Canvas

| Element | Reason |
|---|---|
| Star field (100+ stars) | Performance at scale |
| Particle effects (debris, dust) | Per-frame animation |
| Nebula gradients | Radial complexity |

### Entities NOT Present in Game (Require Original Creation)

- Friendly Surveyer companion drone
- Mission Control UI elements (HUD overlay, telemetry readouts)
- Boss encounter transition effects

---

## PHASE 5: REDESIGN PROPOSAL

### Core Concept: The Ascent

The homepage transforms from a traditional landing page into a vertical ascent experience. Scrolling represents upward progress through Jump Droid's world.

### New Homepage Structure

```
┌─────────────────────────────────────────────────┐
│ LAUNCH PAD                                       │
│ Rocket on launch platform, atmospheric effects   │
│ Primary CTA: "Download on Google Play"           │
│ Altitude: 0m                                     │
├─────────────────────────────────────────────────┤
│ INITIAL ASCENT                                   │
│ Rocket hovers, platforms scroll past             │
│ Mission Control: "Thrusters engaged..."          │
├─────────────────────────────────────────────────┤
│ PLATFORM ZONE                                    │
│ 10 platform types scroll past with labels        │
│ Interactive: hover to see platform behavior      │
├─────────────────────────────────────────────────┤
│ SURVEYER CONTACT                                 │
│ Mission Control message overlay                  │
│ "Surveyer online. Scanning for threats..."       │
├─────────────────────────────────────────────────┤
│ ATMOSPHERIC TRANSITION                           │
│ Background shifts Earth → Cloud → Space         │
│ Parallax layers animate                          │
├─────────────────────────────────────────────────┤
│ BOSS ENCOUNTER                                   │
│ Boss enters from above                           │
│ Rocket reacts, warning systems activate          │
│ Detailed boss information section                │
├─────────────────────────────────────────────────┤
│ ROCKET HANGAR                                    │
│ 4 rocket classes with SVG renders                │
│ Unlock milestones + traits displayed             │
├─────────────────────────────────────────────────┤
│ DEEP SPACE                                       │
│ Starfield, nebulae, environmental elements       │
│ Discovery/Codex showcase                         │
├─────────────────────────────────────────────────┤
│ VOID ZONE                                        │
│ Minimal design, high contrast                    │
│ Final progression themes                         │
├─────────────────────────────────────────────────┤
│ FINAL TRANSMISSION                               │
│ CTA, Play Store links                           │
│ Community links, mission complete                │
└─────────────────────────────────────────────────┘
```

### Scroll Architecture

- **Scroll-driven progression**: Scrolling = climbing altitude
- **Rocket**: Persistent element, follows scroll with delay/offset
- **Zone backgrounds**: Transition gradients based on scroll position
- **Platforms**: Appear as UI elements scrolling past
- **Surveyer**: Appears at trigger points with messages
- **Boss encounters**: Interrupt scroll with full-viewport takeover

### Mobile Experience

- Touch-compatible interactions
- Reduced parallax intensity
- Rocket simplified for smaller screens
- Platform grid collapses to single column
- Performance-optimized animations

---

## PHASE 6: TECHNICAL FEASIBILITY ASSESSMENT

### Technology Choices

| Need | Recommended | Rationale |
|---|---|---|
| Entity rendering | SVG | Game entities are geometrically simple. SVG provides crisp rendering at any scale |
| Animation | Framer Motion | Best-in-class React animation library, scroll-driven animations |
| Parallax backgrounds | CSS + Canvas | CSS for gradients, lightweight Canvas for stars/particles |
| Scroll detection | Framer Motion `useScroll` + `useTransform` | Native scroll-linked animation support |
| Color system | CSS custom properties | Maps directly to game's SciFi color palette |
| Performance | CSS `will-change`, `transform` only | GPU-accelerated animations, mobile-friendly |

### Why SVG Over Canvas

1. Game entities are geometrically simple (rectangles, triangles, circles, paths)
2. SVG scales infinitely (important for responsive design)
3. SVG integrates natively with React (components, props, state)
4. SVG + CSS animations = GPU accelerated
5. No canvas context management needed
6. Accessibility support (aria labels, semantic elements)

### Where Canvas Is Still Needed

- Star field rendering (100+ particles per frame)
- Nebula gradient effects
- Aurora band animations (optional)

### Dependency Additions

```json
{
  "framer-motion": "^11.0.0"
}
```

### Mobile Constraints

- Reduce particle count in starfield on mobile
- Simplify shield effect (fewer arc segments)
- Throttle scroll event handlers
- Use `transform: translateZ(0)` for GPU acceleration
- Test on mid-range Android devices

---

## PHASE 7: RECOMMENDED IMPLEMENTATION PLAN

### Phase A: Foundation (Current Sprint)

**Step A1: Content Accuracy Fixes**
- Correct zone names (add Upper Atmosphere, remove The Signal as zone)
- Update platform count from 5 to 10
- Add rocket traits and unlock milestones
- Fix boss descriptions with actual mechanics
- Update discovery categories to match real game data
- Add missing systems (combo, mission, ascension ranks)

**Step A2: SVG Entity Library**
- Create `/app/components/game/` directory
- `RocketSVG.tsx` — Full rocket render with body, fins, cockpit, nose cone
- `PlatformSVG.tsx` — All 10 platform type renders
- `ThrusterFlame.tsx` — Animated flame component
- `ShieldEffect.tsx` — Rotating shield arc component

**Step A3: Framer Motion Integration**
- Install framer-motion
- Convert page to scroll-tracked sections
- Add scroll-triggered rocket hover animation
- Add section entrance animations

### Phase B: Ascent Experience (Next Sprint)

**Step B1: Scroll Architecture**
- Scroll-linked altitude counter
- Zone background transitions based on scroll
- Rocket follows scroll with parallax offset
- Platform scrolling elements

**Step B2: Zone Backgrounds**
- Convert ZoneBackgroundRenderer.kt logic to CSS gradients
- Create lightweight Canvas star field
- Implement zone transition interpolation

**Step B3: Interactive Elements**
- Platform hover interaction demo
- Boss encounter scroll interruption
- Surveyer message system

### Phase C: Polish (Final Sprint)

**Step C1: Performance Optimization**
- Throttle scroll handlers
- Lazy-load section components
- Optimize Canvas starfield

**Step C2: Mobile Refinement**
- Touch interaction testing
- Reduced animation mode
- Mobile-specific layout adjustments

**Step C3: Surveyer Companion**
- Design original Surveyer drone
- Create mission control message system
- Integrate throughout scroll journey

---

## APPENDIX: FILE REFERENCE

### Website Files
- `/website/site/app/page.tsx` — Main page (94 lines)
- `/website/site/app/layout.tsx` — Root layout (35 lines)
- `/website/site/app/globals.css` — Global styles
- `/website/site/app/components/StickyNav.tsx` — Navigation (28 lines)
- `/website/site/app/components/AltitudeSidebar.tsx` — Zone indicator (20 lines)
- `/website/site/app/components/HeroSection.tsx` — Hero (45 lines)
- `/website/site/app/components/GameplayExplained.tsx` — Gameplay steps (56 lines)
- `/website/site/app/components/BossShowcase.tsx` — Boss cards (67 lines)
- `/website/site/app/components/RocketShowcase.tsx` — Rocket cards (85 lines)
- `/website/site/app/components/DiscoveryArchive.tsx` — Codex archive (41 lines)
- `/website/site/app/components/ProgressionSystems.tsx` — Progression (65 lines)
- `/website/site/app/components/MissionControl.tsx` — Newsletter (54 lines)

### Game Source Files (Kotlin)
- `Models.kt` — Core enums, Player class, PowerUpType, DiscoveryType, RocketType (200 lines)
- `RocketRenderer.kt` — Canvas rocket rendering (417 lines)
- `Platform.kt` — Platform class (46 lines)
- `PlatformRenderer.kt` — All 10 platform visual renders (376 lines)
- `Boss.kt` — Boss class (33 lines)
- `ZoneBackgroundRenderer.kt` — Zone backgrounds + parallax (615 lines)
- `AltitudeZone.kt` — Zone enum with thresholds (27 lines)
- `Constants.kt` — Game constants (38 lines)
- `ThreatRegistry.kt` — All 31 threat definitions (286 lines)
- `ThreatDefinition.kt` — Threat data class (23 lines)
- `ThreatType.kt` — Threat categories (11 lines)
- `MissionManager.kt` — Mission system (123 lines)
- `DiscoveryManager.kt` — Discovery persistence (78 lines)
- `ui/theme/Color.kt` — SciFi color palette (22 lines)

### Design Documents
- `website/docs/STRATEGIC_WEBSITE_AUDIT.md` — Comprehensive audit (671 lines)
- `website/IMPLEMENTATION_PLAN.md` — v1 implementation plan (174 lines)
- `website/IMPLEMENTATION_SUMMARY.md` — Implementation summary
- `website/GAME_SYSTEMS_COMPREHENSIVE_AUDIT.md` — Game systems audit