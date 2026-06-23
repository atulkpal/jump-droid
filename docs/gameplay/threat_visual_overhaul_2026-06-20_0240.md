# Threat Visual Overhaul — 2026-06-20

## Summary
Complete visual rework of all 24 threat renderings to better communicate their abilities, mechanics, and danger levels at a glance.

## Changed Files
- `GameScreen.kt` — Canvas rendering `when(threat.definition.id)` block
- `THREATS.md` — Updated Visual Appearance columns
- `BOSS_DESIGN_BIBLE.md` — Updated Visual Appearance sections

---

## Batch 1: Stub Hazards (Previously No-Op)

### HAZ_GUST
- **Before:** No rendering (empty compatibility case)
- **After:** Large directional wind arrow (fades over 0.5s), leaf/particle trail swept along path, screen ripple line

### HAZ_CROSSWIND
- **Before:** No rendering (empty compatibility case)
- **After:** 3-5 sine-wave horizontal bands at fixed altitudes, direction arrows on each band, thickness = wind strength

### HAZ_THERMAL
- **Before:** No rendering (empty compatibility case)
- **After:** Vertical heat column with orange shimmer gradient, upward particle stream, mirage distortion offset, warm glow

---

## Batch 2: Hazard Visual Communication Fixes

### HAZ_LIGHTNING
- **Before:** 3 stacked circles as clouds, simple zigzag bolt, yellow telegraph ring
- **After:** Cumulonimbus anvil cloud shape (Path), cloud swells and brightens during telegraph (yellow→white), dashed strike-zone indicator on ground, branched fork lightning (3 jagged lines), electric arc particles

### HAZ_DEBRIS
- **Before:** 3 random shapes (solar panel, hull, truss), no danger indication
- **After:** Unified sharp-jagged debris shape, orange glow on leading edge, spark particles from rotation, speed blur ghost copies, red danger pulse when on collision course

### HAZ_RADIATION
- **Before:** Purple aura, boundary ring, green particles — no shield-drain indication
- **After:** Energy-siphon tendrils reaching toward player inside zone, Geiger-counter random burst rects, screen static overlay, shield-drain preview indicator

### HAZ_SOLAR_FLARE
- **Before:** Flat gradient rect with arc overlays, no wave feel
- **After:** Wavy flame-front top edge (animated sine Path), 5-7 flame tongue tendrils, heat-distortion shimmer, 20 ember particles, white screen flash on passage

### HAZ_TURBULENCE
- **Before:** 25 wind streaks, 3 pressure rings — no direction or strength cues
- **After:** Arrow-tipped streak clusters (direction shown), vortex swirl at center, strength-graded streak count (10-40), large wind-direction arrow

### HAZ_GRAVITY
- **Before:** 4 lensing rings, black core, cyan ring — no pull direction
- **After:** 8 downward-pointing gravity arrows from core, background star stretch (tidal distortion), sucked particles, player overlay force lines, fuel-penalty symbol

### HAZ_EMP
- **Before:** Expanding cyan ring + echo + glitch rects — no shield-regen indication
- **After:** Electrical arcs on ring, shield-broken X icon, HUD static lines, shrinking arc segments as timer indicator, spark trail

### HAZ_VOID_ANOMALY
- **Before:** Pink glow + 3 expanding rings
- **After:** Inward-spiraling vortex particles, jagged space-tear rim, reality-warp offset copy, tidal pull lines, deeper magenta palette

---

## Batch 3: Enemy Visual Overhauls

### ENT_SCOUT_DRONE
- **Before:** Gray/red rectangle body, eye, antenna, transmission rings
- **After:** 4-state color palette (blue patrol, yellow detect, red tracking, pink transmit, orange flee), antenna extends during transmission, detailed probe shape, sine-wave signal bars

### ENT_CLOUD_SKIMMER
- **Before:** Cyan manta ray path, slipstream lines, trailing dots — looked hostile
- **After:** Teal/green friendly palette, upward arrow wing markings, glow trail showing slipstream, contact sparkle particles, exaggerated playful wing-flap

### ENT_SWARM_BOTS
- **Before:** 12 orbiting white dots, rare cyan spark
- **After:** 35 particles, 3 "queen" bots with red core, ghost trail copies, swarm boundary ring, collision flash sparks, 15% cyan spark rate

### ENT_ORBITAL_SENTRY
- **Before:** Rotating gray square, cyan core, scan ring — lock-on invisible
- **After:** Laser sight line to player, 4-bracket lock-on reticle, fuel-drain orange stream, combo-freeze icon, octagonal turret chassis

### ENT_CORRUPTED_HULL
- **Before:** Dark gray rects, green pulse — looked like trash
- **After:** Bronze/gold metallic hull, "SALVAGE" text glow, open crate lid with power-up peek, friendly green/yellow beacon, gold sparkle particles, attract flow lines

### ENT_STALKER
- **Before:** Triangle body, heat particles, scan rings (initial implementation)
- **After:** Added thermal shimmer lines, body segmentation glow, alert-level indicator bar

### ENT_VOID_WHALE
- **Before:** Curved body, star dots, trailing particles (initial implementation)
- **After:** Full whale silhouette with tail + fins, nebula star-field body, slipstream direction arrows, void-wake lingering dots, scaled up (120→160)

### ENT_VOID_WRAITH
- **Before:** Shadow humanoid, crackle, glitch (initial implementation)
- **After:** Phase-transition white flash, purple/gray state-indicator glow, wireframe outline when phased, pupil tracking on eyes

---

## Batch 4: Mini-Boss Polish

### MINI_BOSS_COMMANDER
- **Before:** Dark cruiser, bridge, antennas, radar, engines, scanning beams, weak points, gravity pulse
- **After:** Phase-color shift (blue→red→orange hull), gravity-pulse debris particles, jam-wave ring, rotating beacon on weak points, shield-bubble visual

---

## Batch 5: Boss Visual Overhauls

### BOSS_GATEKEEPER
- **Before:** Orbital ring, 4 barrier arcs, central eye, weak point nodes
- **After:** Green safe-gap / red danger arc coloring, solid energy barrier walls, push-back force lines, rotating shield on weak points, iris-tracking central eye, afterimage ghost rings

### BOSS_STAR_EATER
- **Before:** Black/purple aura, spiral particles, dark core, magenta eye, 12 tendrils
- **After:** Power-up suction stream trails, player suction debris orbit speed indicator, hunger-meter core pulse rate, dentition teeth ring, after-image ghost copy

### BOSS_LEVIATHAN
- **Before:** 6 blue circles, sine offset, slipstream lines, weak points
- **After:** Organic ellipse + armor plate segments, directional slipstream arrows per segment, glowing head with mouth, bioluminescent vein patterns, wall-pressure red edge glow, tail whip telegraph

### BOSS_VOID_ENGINE
- **Before:** 3 rotating pink bars, warp aura, energy arcs, shift arrows
- **After:** Screen-wide gravity-shift arrows, control-inversion buildup (pink tint + warning), reality-tear rim, arm afterimage ghosts, core instability arc bursts

### BOSS_SIGNAL
- **Before:** Glitch rectangles, weak point circle, faint pulse ring
- **After:** Screen-tear lines, ghost platform preview flicker, HUD-glitch wrong values, decoy Signal copies, static-noise TV ring, binary-rain particles, frame-skip flicker

---

## Documentation Updates

- **THREATS.md:** All 5 tables updated with new visual descriptions
- **BOSS_DESIGN_BIBLE.md:** All visual appearance fields updated
