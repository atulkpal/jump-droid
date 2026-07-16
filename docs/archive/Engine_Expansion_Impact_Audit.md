# Engine Expansion: Existing Systems Impact Audit

This report analyzes how the proposed engine expansions will affect the current Jump Droid codebase and identifies the specific touchpoints where existing logic will be modified.

---

## 1. Executive Summary
The proposed implementation is designed to be **additive** rather than **destructive**. Most systems (Rendering, Physics, Threat AI) will remain intact, with the new functionality plugged into existing "hooks." The most significant changes are found in the **Input Pipeline** and **Threat Interaction Signatures**.

---

## 2. System-by-System Impact Audit

### A. Physics Loop (`GameScreen.kt`)
*   **Change:** Addition of two new sub-routines within the physics substep loop: `ProjectileManager.processCollisions()` and `TetherManager.applyForces()`.
*   **Effect:** Existing AABB collision (Player ↔ Platform) remains unchanged. The new systems simply add additional force vectors or damage events to the player object.
*   **System Impact:** **Low.** 

### B. Input Pipeline (`GameScreen.kt` & `pointerInput`)
*   **Change:** Currently, `pointerInput` updates a `remember { mutableStateOf(false) }` boolean called `isThrusting`. This will be replaced by a call to `InputBufferManager.recordInput()`. The physics loop will then query `InputBufferManager.getEffectiveThrust()` instead of reading the boolean directly.
*   **Effect:** Every system that relies on the `isThrusting` flag (Thrust physics, Heat generation, certain Threat triggers) will now read a "delayed" or "buffered" value. This allows for effects like *Chrono-Lag* to work globally.
*   **System Impact:** **High.** Requires refactoring all `isThrusting` references in `GameScreen.kt`.

### C. Threat AI & Interaction (`ActiveThreat.kt`)
*   **Change:** The signature of `processInteraction()` and `update()` will be expanded. 
    - **Current:** `processInteraction(player, platforms, powerUps, ...)`
    - **New:** `processInteraction(player, platforms, powerUps, activeThreats, ...)`
*   **Effect:** This allows one threat to "see" other threats (Entity-to-Entity interaction). For example, a `Void Harvester` can now scan the `powerUps` list to move toward them.
*   **System Impact:** **Medium.** Existing AI logic for current threats will not break, but the boilerplate method signatures across the project must be updated.

### D. Rendering Pipeline (`Canvas` in `GameScreen.kt`)
*   **Change:** Addition of a `drawVisualObstruction()` call within the `Canvas` block.
*   **Effect:** This layer will likely be inserted **after** the threat/platform rendering but **before** the HUD rendering. This ensures that fog or smoke obscures the game world while keeping the critical HUD gauges visible.
*   **System Impact:** **Low.** No existing rendering logic is modified.

### E. Player State (`Models.kt`)
*   **Change:** Addition of `tetherAnchor` and `inputLatency` fields to the `Player` class.
*   **Effect:** Minimal. These are simple state holders that the new Managers will use to apply their effects.
*   **System Impact:** **Negligible.**

---

## 3. Risk Assessment

| Risk | Description | Mitigation Strategy |
| :--- | :--- | :--- |
| **Performance Degradation** | E2E interaction (threats scanning for power-ups) adds $O(N^2)$ complexity. | Limit scanning frequency to every 5-10 frames and use distance-squared checks. |
| **Control "Feel" Issues** | Input buffering might introduce accidental "input lag" if the default buffer is not 0. | Ensure the default latency is exactly 0.0s and only modified by specific zone effects. |
| **Rendering Overlap** | Visual obstructions might hide things the player *needs* to see (like the rocket itself). | Use `clipPath` to create a "clarity radius" around the player's rocket. |

---

## 4. Conclusion
The implementation **will not break existing systems** but will require a systematic update of the **Input Manager** and **Threat Interaction** signatures. Once these structural adjustments are made, the new features (Projectiles, Fog, E2E) can be added as modular components without further affecting the baseline game loop.
