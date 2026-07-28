# Jump Droid: Missions vs. Achievements Architecture

This document formalizes the distinction between the Mission system and the Achievement system to ensure clear progression and player motivation.

---

## 1. Missions (The "Operations")

Missions represent specific, tiered tactical objectives that the player performs during their ascent. They are the primary driver of the game's economy and world-building.

*   **Structure**: Grouped into **11 Tracks** (Aeronautics, Ground Support, etc.).
*   **Progression**: Each track has 4 Tiers (**Rookie -> Experienced -> Master -> God**). Completing one tier unlocks the next.
*   **Payoff**:
    *   **Currency**: Credits (JC) and Cash.
    *   **Lore**: Each mission completion grants a **Mission Debrief**—a short narrative report on the outcome of the operation.
    *   **Unlocks**: High-tier missions can unlock new Rockets and Modules.
*   **Player Feeling**: "I am completing an operation for the Ascension Program. My actions have meaning in the world."

---

## 2. Achievements (The "Milestones")

Achievements are global, lifetime milestones that track a player's absolute mastery and history with the game. They are "medals of honor" rather than tactical objectives.

*   **Structure**: A flat list of unique, non-repeatable challenges (First Launch, Sky Breaker, Untouchable).
*   **Progression**: Non-linear. Any achievement can be unlocked at any time once requirements are met.
*   **Payoff**:
    *   **Mastery Points (MP)**: Driving the player's overall Rank (e.g., Lead Pilot, Flight Commander).
    *   **Prestige**: Visual badges in the Data Archive.
    *   **Google Play Integration**: Syncing with global player profiles.
*   **Player Feeling**: "I have achieved a legendary feat. I am one of the top pilots in the program."

---

## 3. Complementary Design

| Feature | Missions | Achievements |
| :--- | :--- | :--- |
| **Repeatability** | Multi-tier (evolving cards) | One-time unlock |
| **Narrative** | Tactical Debriefs (Dynamic) | Lore Entries (Static) |
| **Primary Reward** | Currency & Modules | Mastery Points & Rank |
| **UI Location** | Mission Log (Dashboard) | Data Archive (Hall of Fame) |
| **Difficulty** | Scaling (Easy to Hard) | High Threshold (Skill/Time based) |

---

## 4. Key Takeaways for Polish

1.  **Missions** should feel like **work for a cause**. The "Debrief" is critical to make it feel like an operation rather than a checkbox.
2.  **Achievements** should feel like **personal legacy**. They should be celebrated with larger notifications and permanent archive entries.
3.  Avoid overlapping objectives. If an objective is in a Mission Track (e.g., Altitude), the corresponding Achievement should be significantly harder or focused on a unique twist (e.g., "Reach Orbit without using Fuel").
