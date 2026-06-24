# EPIC 10 — The Outer Reaches (Planning)

**Goal:** Expand the vertical journey beyond the Void into uncharted space, introducing new mechanics, environmental storytelling, and visual spectacles.

---

## 1. New Altitude Zones

| Zone | Threshold | Visual Theme | Narrative Hook |
| :--- | :--- | :--- | :--- |
| **The Beyond** | 25,000m | Ethereal, shimmering nebula. | "Where light and matter begin to blur." |
| **Stellar Gate** | 45,000m | High-tech mega-structures, artificial sky. | "A doorway built by those who came before." |
| **Ancient Construct** | 70,000m | Monolithic, dark geometric shapes. | "The source of the heartbeat in the static." |
| **Singular Point** | 100,000m | Reality-warping, monochromatic. | "The end of the ascent. The beginning of Ascension." |

---

## 2. New Gameplay Mechanics

### Ancient Sky Structures
- **Flux Platforms**: Teleport the player horizontally to a linked exit point on touch.
- **Graviton Platforms**: Create a localized gravity well that rotates the player's acceleration vector.

### Environmental World Events
Timed occurrences managed by a new `EventDirector`:
- **Meteor Shower**: High-speed projectile storm from above.
- **Nebula Fog**: Triggers HUD interference; navigation relies on proximity pings.
- **Solar Storm**: Extreme heat generation + temporary shield suppression.

### Elite Entities
- **Void Phantom**: High-damage enemy that only moves/attacks while the player is NOT thrusting.
- **Stellar Harvester**: Large neutral-hostile entity that attracts and "consumes" nearby power-ups.

---

## 3. Sprint Roadmap

### Sprint 10.1: Expansion of the Sky
- [ ] Add thresholds to `Constants.kt`.
- [ ] Add entries to `AltitudeZone.kt`.
- [ ] Expand `DiscoveryType` and `LoreLog` registries.
- [ ] Implement background gradients and parallax layers for "The Beyond" and "Stellar Gate".

### Sprint 10.2: Mega-Structures & Flux
- [ ] Implement `FluxPlatform` and `GravitonPlatform` logic.
- [ ] Add geometric "Mega-structure" parallax layers.
- [ ] Update `PlatformManager` algorithms for high-altitude variance.

### Sprint 10.3: The Event Director
- [ ] Create `EventDirector` to handle scheduled environmental events.
- [ ] Implement "Meteor Shower" (global projectile spawn).
- [ ] Implement "Nebula Fog" (stat/HUD modifier).

### Sprint 10.4: Apex Threats
- [ ] Implement `VoidPhantom` AI and renderer.
- [ ] Implement `StellarHarvester` AI (item attraction logic).
- [ ] Balance boss scaling for 25k+ altitudes.

### Sprint 10.5: Final Ascension Lore
- [ ] Populate 10 Lore Logs for the new zones.
- [ ] Define "Endless Void" set bonuses.
- [ ] Performance optimization pass for high-density rendering.

---

## 4. Technical Prerequisite: Data-Driven Spawning
The current `EncounterDirector.kt` is heavily hardcoded.
**Task 10.0 (Pre-sprint)**: Move zone spawn weights and boss milestones into a `ZoneConfig` data structure to avoid growing the director's code size.
