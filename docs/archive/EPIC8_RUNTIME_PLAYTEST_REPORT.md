# EPIC 8 RUNTIME PLAYTEST & VISION VALIDATION REPORT

**Project:** Jump Droid  
**Role:** Player QA Tester / Vision Validator  
**Status:** Playtest Complete  
**Final Verdict:** **REJECTED** (Release Blockers Identified)

---

## BUILD STATUS
**Result:** SUCCESS  
The project builds successfully and reaches the Title Screen. All core navigation routes and gameplay systems are accessible at runtime.

---

## SESSIONS SUMMARY

### SESSION 1: Navigation & Onboarding
*   **Action:** First-time user flow from Title to Launch.
*   **Result:** Navigation is responsive, but the "Terminal" button incorrectly routes to a Developer Debug menu. This exposes cheats (Infinite Fuel, Shield, etc.) to players and breaks narrative immersion.

### SESSION 2: Mission System Audit
*   **Action:** Verified the 12-track mission architecture and claim flow.
*   **Result:** Evolving mission cards work at runtime (e.g., Rookie -> Experienced). Claiming rewards correctly updates completion percentages.

### SESSION 3: High-Intensity Stress Test
*   **Action:** Triggered simultaneous Discovery, Mission Completion, and Game Over in the Void zone.
*   **Result:** Significant visual overlap issues. HUD elements, discovery cards, and Game Over text render on top of each other, creating unreadable visual "soup."

### SESSION 4: Persistence Verification
*   **Action:** Accumulated mission progress and signals, then performed a cold restart of the app.
*   **Result:** **CRITICAL FAILURE.** While mission completion states persist, the "Signals Recovered" counter and partial progress on active missions reset to zero.

### SESSION 5: Entity & System Validation
*   **Action:** Triggered Boss encounters (SIGNAL, GATEKEEPER) and Hazard escalations.
*   **Result:** Entity logic is sound. Scaling feels fair. However, the Combo Ring position contradicts the documented vision (Mandated: Top-Left; Observed: Top-Right/On-Rocket).

---

## PLAYABILITY REPORT
Jump Droid is physically satisfying. The weight of the rocket and the propulsion mechanics feel professional and "Physics-First." The transition between atmospheric layers provides a clear sense of vertical scale and danger.

---

## VISUAL CLUTTER REPORT
*   **Overlapping UI:** The "MISSION COMPLETE" banner renders directly over the player and center-screen Discovery cards.
*   **Notification Spam:** "SURVEYOR PROBE" notifications trigger frequently and stay in the tactical field too long.
*   **Visual Noise:** During high combos, particle effects from thrust + combo ring + floating text for rewards overlap, making it difficult to see oncoming platforms.

---

## HUD & FEEDBACK REPORT
*   **Good:** Vertical gauges (Fuel/Heat vs Shield/Hull) provide excellent high-speed readability. Impact flashes are visceral.
*   **Bad:** The Combo Ring is frequently obscured by the "Pause" and "Help" buttons in the top right.
*   **Excessive:** The "MISSION COMPLETE" bottom banner is oversized and dominates the HUD for 3+ seconds.
*   **Missing:** A clear numerical multiplier (e.g., "x20") is missing from the persistent HUD; it is only visible in the Combo Ring arc.

---

## MISSION EXPERIENCE REPORT
The "Evolving Card" mechanic is a standout success; it keeps the Mission Log clean. However, the lack of persistence for "Hidden Signals" makes the secret mission track feel broken to the player.

---

## ENTITY VALIDATION REPORT
*   **Platforms:** Moving and Boost platforms are reliable across all sub-steps.
*   **Bosses:** The "SIGNAL" boss mechanics (Distortion/HUD Interference) are functional and challenging.
*   **Powerups:** Magnetism on mission rewards is a great UX touch, though it sometimes pulls items off-screen before they can be seen.

---

## DIFFICULTY & BALANCE REPORT
*   **Pacing:** The learning curve is excellent. Earth and Cloud layers provide ample time to learn controls before the aggressive tracking of the Orbital Sentry begins.
*   **Fairness:** High. The droid always feels like it has a path to recovery via combo rewards.

---

## PLANNED VS IMPLEMENTED REPORT

| System | Planned | Observed | Status |
| :--- | :--- | :--- | :--- |
| **Combo Ring** | Top-Left Corner | Top-Right / Overlap | **DIFFERENT** |
| **Terminal** | Lore/Narrative System | Developer Menu | **INCONSISTENT** |
| **Persistence** | Permanent Stats | Claims Only | **PARTIAL** |
| **12-Track UI** | 12 Unified Vertical Tracks| 11 Tracks + Hidden Ops | **MATCHES** |

---

## CRITICAL ISSUES
**1. Persistence Data Loss (Hidden Missions)**
*   **Observed:** "Signals Recovered" count resets to 0/7 on every app restart.
*   **Expected:** Signal count should be stored in SharedPreferences.
*   **Impact:** Release Blocker. Players lose hours of "Endgame" progress.

---

## MAJOR ISSUES
**1. Immersion-Breaking Navigation (Terminal)**
*   **Observed:** "TERMINAL" button leads to the Developer/Cheat menu.
*   **Expected:** "TERMINAL" should be locked or lead to About/Lore content.
*   **Impact:** UX Failure. Exposes debug tools to standard users.

**2. Celebration Overlap**
*   **Observed:** Game Over screen renders over active mission celebrations.
*   **Expected:** The failure screen should wait for active HUD animations to clear.

---

## MINOR ISSUES
**1. Dev Menu UI**
*   **Observed:** Toggles use hard-to-read vertical letter stacks.

---

## POLISH FINDINGS
*   **Feedback:** Claiming a mission needs a "Success" sound or haptic. Currently, the button just disappears.
*   **Hierarchy:** The Mission Log summary (Claimable/Total) uses very small text (7.sp) that is unreadable on some device ratios.

---

## VISION GAPS
*   **Combo Position:** `AGENTS.md` explicitly mandates the Top-Left to avoid clutter. Moving it to the Top-Right/Bottom has caused the overlap issues the manual was written to prevent.

---

## OPPORTUNITIES
*   **Color Themes:** Category-specific colors in the Mission Log (e.g. Red for Elite Combat) would improve scanning speed.

---

## FINAL RELEASE CANDIDATE SCORE
### **3 / 10**

**QA SIGN-OFF:** **REJECTED**  
*The game is mechanically excellent, but the persistence failure and the "Terminal" routing oversight prevent it from being a release candidate.*
