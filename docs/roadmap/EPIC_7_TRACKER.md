# EPIC 7 — Rocket Evolution

## Status Legend
- [ ] Not Started
- [~] In Progress
- [x] Complete
- [!] Needs Rework
- [-] Deferred

## Purpose
Transform **Rocket = Vehicle** into **Rocket = Build**. The player should eventually have a preferred ship style.

---

## Progress Summary
- Sprint 7.1 — Module Framework ✅
- Sprint 7.2 — Core Modules ✅
- Sprint 7.3 — Mobility Builds ✅
- Sprint 7.4 — Utility Builds ✅
- Sprint 7.5 — Rocket Identity Audit 🔄
- Sprint 7.6 — Progression & Unlocks ⏳
- Sprint 7.7 — Balance & Validation ⏳

---

## Phase 1 — Framework (Sprint 7.1)
*Goal: Player can equip and save modules.*

### Ship Loadout UI
- [x] Loadout Screen layout
- [x] Module slots (Visual & Logic)
- [x] Equip / Unequip interactions
- [x] Save persistence (SharedPreferences/LoadoutManager)

### Module Framework
- [x] Module base class / interface
- [x] Equip system logic
- [x] Module activation hooks (onDamage, onThrust, etc.)
- [x] Stat modifier integration

---

## Phase 2 — Core Modules (Sprint 7.2)
*Goal: At least 6 modules functional.*

### Hull Modules
- [x] Reinforced Hull (+Max Integrity)
- [x] Impact Dampeners (-Damage taken)
- [x] Self Repair Matrix (Slow integrity regen)

### Shield Modules
- [x] Fast Recharge (-Regen delay)
- [x] Emergency Shield (Instant shield on hull hit, cooldown)
- [x] Reflective Shield (Damage enemies on shield hit)

---

## Phase 3 — Mobility Builds (Sprint 7.3)
*Goal: Player can build fast, efficient, or heat-focused ships.*

### Engine Modules
- [x] Burst Thrusters (+Initial thrust)
- [x] Long Burn Thrusters (+Thrust duration efficiency)
- [x] Vector Thrusters (+Horizontal steering)

### Heat Modules
- [x] Cooling Matrix (+Cooling rate)
- [x] Thermal Battery (+Max heat capacity)
- [x] Heat Sink (Instant heat vent on landing)

---

## Phase 4 — Utility Builds (Sprint 7.4)
*Goal: Exploration-focused builds become possible.*

### Exploration Modules
- [x] Survey Scanner (Reveal threats earlier)
- [x] Artifact Locator (Visual ping toward artifacts)
- [x] Threat Scanner (Display threat HP/Status)

### Support Modules
- [x] Auto Repair Drone (Fixes hull over time)
- [x] Emergency Beacon (Spawns rescue platform at critical fuel)

---

## Phase 5 — Rocket Classes (Sprint 7.5)
*Goal: Classes feel genuinely different.*

- [x] Scout Class bonuses & traits
- [x] Explorer Class bonuses & traits
- [x] Heavy Class bonuses & traits
- [x] Experimental Class bonuses & traits

---

## Phase 6 — Progression (Sprint 7.6)
*Goal: Modules earned through gameplay.*

- [x] Boss reward module drops
- [x] Discovery milestone rewards
- [x] Artifact collection rewards
- [x] Mission track rewards

---

## Phase 7 — Balance & Validation (Sprint 7.7)
*Goal: Valid builds and fair progression.*

- [x] Build Validation (Explorer, Striker, Heavy, Prototype)
- [x] Module stat balancing review
- [x] Class native trait balancing review
- [x] Progression curve tuning audit
- [x] Asset Readiness Audit

---

## Emergent Features

Purpose: Track systems that were added during implementation but were not part of the original EPIC plan.

### Sprint 7.1 Emergent Features
- [x] Module Registry
- [x] Module Hook Architecture
- [x] LoadoutManager
- [x] Loadout Persistence System
- [x] Modifier Layer Foundation
- [x] Unique Module Constraint Support

### Sprint 7.2 Emergent Features
- [x] Unlock Requirement Framework
- [x] Unlock Metadata Support
- [x] Module Cooldown State System
- [x] Visual Feedback Callback Framework
- [x] Burst Effect Callback Framework
- [x] Reactive Module Architecture

### Sprint 7.3 Emergent Features
- [x] Transformation Pipeline
- [x] Effective Stat Calculation System
- [x] Physics Loop Module Multipliers
- [x] Dynamic Flight Archetype Support
- [x] Runtime Stat Modification Pipeline

### Sprint 7.4 Emergent Features
- [x] World Marker System
- [x] Module Rendering Hook (onDraw)
- [x] Drone Entity Framework
- [x] Tactical Health Visualization
- [x] System Request Architecture
- [x] Platform Spawn Request System
- [x] Utility Visualization Framework

### Sprint 7.5 Emergent Features
- [x] Class Trait System
- [x] Native Multiplier Injection
- [x] Trait-Specific Visual FX
- [x] Rule-Breaking Physics Branching
- [x] Class Metadata Display

### Sprint 7.6 Emergent Features
- [x] Persistent Mission Counter
- [x] Module Ownership Set
- [x] Requirement Evaluation Engine
- [x] Ownership Guard System
- [x] Celebration Pipeline

---

## Technical Debt

Track items discovered during implementation that require future polish but do not block EPIC progress.

### Module Presentation
- [ ] Module Activation Effects
- [ ] Module Status Indicators
- [ ] Module Trigger Notifications
- [ ] Active Build Summary UI
- [ ] Loadout Stat Preview

### Utility Systems
- [ ] Marker Manager
- [ ] Scanner Performance Optimization
- [ ] Utility Module Preview UI

### Balance
- [ ] Reflective Shield Real Reflection Logic
- [ ] Steering Saturation Review
- [ ] Module Stacking Validation
- [ ] Heat Economy Balance Pass

---

## Deliverable Summary
By the end of EPIC 7, two players reaching the same altitude should be able to have completely different ships based on their chosen build paths.