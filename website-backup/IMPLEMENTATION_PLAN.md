# Website Implementation Plan
**Date:** 2026-06-23  
**Scope:** `/website/site` only — content improvements, no visual redesign  
**Objective:** Clarify gameplay, showcase progression, strengthen boss presence  

---

## Changes Summary

### 1. Hero Section (Minor Update)
**Current issue:** "Tactical vertical expedition" is vague  
**Change:** Add 1-2 clarifying sentences explaining gameplay loop  
**Impact:** Answers "What do I do?" faster  
**Effort:** Text update only (no component change)

```
FROM: "...physics-driven thrust, phase-based bosses, and a hidden Codex..."
TO: "...physics-driven thrust. Touch to climb against gravity. 
Manage fuel, heat, and shield while landing on platforms and facing bosses..."
```

---

### 2. NEW Section: "THE GAMEPLAY" (After Hero)
**Purpose:** Answer "What do I actually do?" in 10 seconds  
**Content:**
- "Touch to thrust. Hold to burn fuel."
- "Land on platforms. Build combos. Restore shield."
- "Three resources: Fuel (thrust), Heat (cooldown), Shield (damage)."
- Small visual: Simple icon trio or one screenshot

**Component:** New `GameplayExplained.tsx`  
**Effort:** 30 min (component + styling)

---

### 3. Ascent Section (Minor Expansion)
**Current issue:** Only 3 zones shown (Cloud, Orbit, Void). Game has 6.  
**Change:** List all 6 zones by name  
**Impact:** Shows full scope of journey  
**Effort:** Text update (add Earth, Deep Space, and clarify zone 6 name)

```
Current: Cloud Layer | Orbit | Void
New: Earth → Cloud Layer → Orbit → Deep Space → The Void → ???
```

---

### 4. NEW Section: "BOSSES" (Between Ascent and Hangar)
**Purpose:** Showcase boss variety and spectacle  
**Content:** 6 boss cards showing:
- Boss name (Command Cruiser, Gatekeeper, Leviathan, Star-Eater, Void Engine, The Signal)
- 1-2 word descriptor (e.g., "Platform Jamming", "Shield Sentinel")
- Optional: Simple icon/silhouette

**Component:** New `BossShowcase.tsx`  
**Layout:** Grid (2-3 per row, responsive)  
**Effort:** 45 min (component + styling)

---

### 5. Hangar Section (Minor Enhancement)
**Current issue:** 
- Rockets look like cosmetics (no progression signal)
- Module customization invisible

**Changes:**
- Add unlock milestone badges (e.g., "Unlocked at 2K score")
- Change messaging from "Choose your build" to "Climb to unlock. Customize to dominate."
- Add 1-line note about module customization (e.g., "Customize with 17 module types")

**Effort:** 20 min (text updates + minor styling)

---

### 6. Archive → "Codex" Section (Redesign)
**Current issue:** Generic fragments feel like mystery, not progression  
**Change:** 
- Rename "Archive" to "Codex"
- Replace generic "Fragment 01: The First Signal" with real categories
- Show 6 example discovery types: Platforms, Enemies, Rockets, Lore, Artifacts, Mechanics
- Replace placeholder "Encrypted details" text with brief discovery purpose

**Effort:** 20 min (text + messaging changes)

---

### 7. NEW Section: "PROGRESSION" (After Codex)
**Purpose:** Communicate long-term goals and replayability  
**Content:** Brief explanation of 3 systems:
- **Ascension Ranks**: 5 tiers based on discovery count (Novice → Void Explorer)
- **Mission System**: 3 types (Exploration / Platforming / Survival) keep runs fresh
- **Combo Economy**: Skill directly rewards survival (land platforms → restore shield)

**Component:** New `ProgressionSystems.tsx`  
**Layout:** 3 columns or vertical cards  
**Effort:** 40 min (component + styling)

---

### 8. Mission Control (No Change)
Already communicates community well. Keep as-is.

---

## Section Order (New → Old → New)
```
Hero (clarified)
  ↓
THE GAMEPLAY (NEW)
  ↓
Ascent (expanded to 6 zones)
  ↓
BOSSES (NEW)
  ↓
Hangar (enhanced with unlock milestones)
  ↓
Codex (renamed + recontextualized)
  ↓
PROGRESSION (NEW)
  ↓
Mission Control
  ↓
Footer
```

---

## Visual Impact
- **New sections:** 2 components (Gameplay, Bosses)
- **Enhanced sections:** 1 component (Progression)
- **Refined sections:** 3 (Hero, Ascent, Codex, Hangar)
- **Visual design:** NO CHANGES — preserve colors, typography, spacing
- **Page length:** +30–40% (adds 3–4 new content blocks)

---

## Estimated Effort
- New components: 2 hours
- Text updates: 30 min
- Responsive refinements: 30 min
- **Total: ~3 hours**

---

## Files to Modify/Create
**Create:**
- `app/components/GameplayExplained.tsx` (new)
- `app/components/BossShowcase.tsx` (new)
- `app/components/ProgressionSystems.tsx` (new)

**Update:**
- `app/page.tsx` (import new components, reorganize sections)
- `app/components/HeroSection.tsx` (clarify copy)
- `app/components/AltitudeSidebar.tsx` (no change, keep)
- `app/components/RocketShowcase.tsx` (add unlock badges, update messaging)
- `app/components/DiscoveryArchive.tsx` (rename to Codex, update text)
- `app/components/MissionControl.tsx` (no change, keep)

---

## Success Metrics (Post-Implementation)
- Can a new visitor answer "What do I do?" in 10 seconds? (Goal: YES)
- Are rocket unlock milestones visible? (Goal: YES)
- Are bosses given presence? (Goal: YES)
- Does the page explain progression? (Goal: YES)
- Is the visual design preserved? (Goal: YES)

---

## Approval Gate
Before implementing: Confirm this plan aligns with objectives.
After approval: Execute all changes in `/website/site` directly.
