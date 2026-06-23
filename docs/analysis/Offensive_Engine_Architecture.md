# Jump Droid: Offensive Engine Architecture (Planning)

This report outlines the technical requirements for an **Offensive Engine** that enables enemies to perform telegraphed attacks, specifically addressing the "enemies don't attack" gap.

---

## 1. Projectile System
**Objective:** A global, optimized system for tracking and rendering ranged attacks.

### Architecture
- **Manager (`ProjectileManager.kt`):**
    - Stores a `SnapshotStateList<Projectile>`.
    - Updates projectile positions every frame.
    - Handles AABB collision against the `Player`.
- **Data Model (`Projectile.kt`):**
    - `type`: `BOLT` (linear), `MISSILE` (tracked), `WAVE` (area).
    - `damage`: Impact value.
    - `life`: Despawn timer.
- **Rendering:**
    - Integrated into the main `Canvas` loop in `GameScreen.kt` using a dedicated `drawProjectiles` call.

---

## 2. Telegraphed Attack Patterns
**Objective:** Standardize how threats signal an upcoming offensive action.

### The "Telegraph -> Trigger -> Cooldown" Cycle
Every aggressive enemy will follow a state machine:
1.  **TELEGRAPH:** Use `scanPulse` or color shifts (e.g., Scout Drone turns bright red).
2.  **TRIGGER:**
    - **Lunge:** Threat applies a massive velocity burst toward the player.
    - **Fire:** Threat calls `ProjectileManager.spawn()`.
    - **Burst:** Threat creates a temporary high-damage area.
3.  **COOLDOWN:** Threat returns to `IDLE` or `PATROL` for X seconds.

---

## 3. High-Priority Enemy Upgrades
Once the engine is in place, we can "activate" existing threats:

| Threat | New "Active" Attack | Gameplay Role |
| :--- | :--- | :--- |
| **Surveyor Probe** | 3-shot Burst (Telegraphed with red laser) | Sniper / Harasser |
| **Defense Node** | Pulse Shock (Large radial energy wave) | Area Denial |
| **Orbital Sentry** | Tracking Missile (Slow but follows player) | Hunter |
| **Command Cruiser**| Heavy Plasma Cannon (Massive beam) | Artillery |

---

## 4. Integration with Stealth & Combat
- **Stealth Interaction:** Enemies only enter the **Telegraph** state if the player is in the `HUNTING` (Detected) state.
- **Combat Interaction:** If the player performs a **Kinetic Slam** on an enemy while they are telegraphed, it deals double damage (Counter-Hit).
