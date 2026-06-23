# Boss Implementation Gap Analysis (June 19, 2026)

This report compares the **BOSS_DESIGN_BIBLE.md** (Intent) against the actual implementation found in **THREATS.md** and the codebase (Reality).

---

## 1. Boss Implementation Status

| Entity | ID | Status | Findings |
| :--- | :--- | :--- | :--- |
| **Command Cruiser** | `MINI_BOSS_COMMANDER` | **Implemented** | The only fully functional mini-boss. Uses "Platform Jamming" (not in design bible). |
| **The Gatekeeper** | `BOSS_GATEKEEPER` | **Partial** | Uses rotating gaps/barriers. Design "Projectiles" are not yet in code. |
| **Star Eater** | `BOSS_STAR_EATER` | **Partial** | Uses "Suction Pull" (not in design). Design "Flaming Birds" phase is missing. |
| **The Leviathan** | `BOSS_LEVIATHAN` | **Partial** | Uses "Segmented Body" logic. Design "Playable Area Shrinking" is missing. |
| **Void Engine** | `BOSS_VOID_ENGINE` | **Prototype** | Uses lateral "Gravity Shifts". Design "Control Inversion" is not yet implemented. |
| **The Signal** | `BOSS_SIGNAL` | **Prototype** | Uses "Ghost Platforms" (not in design). Design "HUD Jamming" is missing. |
| **Chrono Warden** | `BOSS_CHRONO_WARDEN` | **Planned** | No mention in registry or AI logic. |
| **Magma-Core Titan** | `BOSS_MAGMA_TITAN` | **Planned** | No mention in registry or AI logic. |
| **Frost Wyrmling** | `MINI_BOSS_WYRM` | **Planned** | No mention in registry or AI logic. |
| **Crystal Guardian** | `MINI_BOSS_GUARDIAN` | **Planned** | No mention in registry or AI logic. |
| **Scrap Berserker** | `MINI_BOSS_BERSERKER` | **Planned** | No mention in registry or AI logic. |
| **Void Tracker** | `ENT_STALKER` | **Registry Only** | Entry exists in `ThreatRegistry.kt` but has zero AI/Behavior code. |
| **Cosmic Leviathan** | `ENT_VOID_WHALE` | **Registry Only** | Entry exists in registry but has zero AI/Behavior code. |
| **Shadow Entity** | `ENT_VOID_WRAITH` | **Registry Only** | Entry exists in registry but has zero AI/Behavior code. |

---

## 2. Key Suggestions for Realignment

### A. Sync Design with Reality (The "Pivot")
The implemented mechanics for **Star Eater** (Suction) and **The Signal** (Ghost Platforms) are unique to Jump Droid's physics.
- **Recommendation:** Update the `BOSS_DESIGN_BIBLE.md` to reflect these "Happy Accidents" as the official intended design.

### B. Consolidate "Leviathan" Confusion
There are currently two "Leviathans" in the files:
- `BOSS_LEVIATHAN` (Deep Space Boss - Partial)
- `ENT_VOID_WHALE` (Cosmic Leviathan - Registry Only)
- **Recommendation:** Remove the "Registry Only" whale and merge any unique design ideas into the actual segmented Boss.

### C. Implement "Control Inversion" for Void Engine
The Design Bible suggests "Control Inversion" for the Void Engine's 3rd phase.
- **Recommendation:** Adding control inversion would elevate this from **Prototype** to **Partial/Implemented** and provide a true "Reality Warping" feel.

### D. Cleanup "Registry Only" Deadwood
The registry contains several entries (`STALKER`, `VOID_WRAITH`) that likely contribute to spawn-rate calculations but do nothing when they appear. 
- **Recommendation:** Either implement a basic "Drift and Damage" AI for these in Sprint C or remove them from the registry to improve the spawn frequency of functional threats.

### E. Projectile System Requirement
Both documents refer to projectiles (`PROJECTILE_SHOOTER` behavior). However, the engine currently has **no projectile/bullet system**. 
- **Recommendation:** A `ProjectileManager` and collision layer must be built from scratch before attempting to implement Phase 2 of *The Gatekeeper* or other ranged threats.
