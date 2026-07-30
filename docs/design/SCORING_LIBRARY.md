# Jump Droid: Scoring Library (The Mastery Bible)

This document is the single source of truth for the Dynamic Scoring System. It defines how performance is quantified, visualized, and persisted.

---

## 1. Core Logic

Jump Droid utilizes a **Decoupled Scoring Engine**. Physical progression is driven by altitude, while competitive standing is driven by skill-based points.

### The Master Formula
`Total Score = AltitudePoints + BossPoints + PlatformPoints + ComboPoints`

| Source | Calculation | Frequency | Visual Style |
| :--- | :--- | :--- | :--- |
| **Altitude** | `runAltitude / 10` | Continuous | Silent Rolling |
| **Bosses** | `definition.scoreAward` | Once per defeat | Large [Cyber-Packet] |
| **Platforms** | `+10` per unique landing | Once per platform | Small [Cyber-Packet] |
| **Combos** | `+5` per step + `+100` per milestone (5x) | Per landing | Purple [Cyber-Packet] |

---

## 2. Point Registries

### 2.1 Boss Mastery Awards
Boss rewards are data-driven and proportional to the zone intensity and mechanical complexity.

| Boss ID | Name | Tier | Score Award |
| :--- | :--- | :--- | :--- |
| `MINI_BOSS_COMMANDER` | Command Cruiser | 4 | 250 |
| `MINI_BOSS_THERMAL_HIVE` | Thermal Hive | 4 | 300 |
| `MINI_BOSS_FORGER` | The Forger | 4 | 350 |
| `MINI_BOSS_GRAVITY_ANCHOR` | Gravity Anchor | 4 | 400 |
| `BOSS_GATEKEEPER` | The Gatekeeper | 5 | 1,000 |
| `BOSS_LEVIATHAN` | The Leviathan | 5 | 1,200 |
| `BOSS_STAR_EATER` | Star-Eater | 5 | 1,500 |
| `BOSS_VOID_ENGINE` | The Void Engine | 5 | 2,000 |
| `BOSS_SIGNAL` | The Signal | 5 | 2,500 |
| `BOSS_ARCHITECT` | The Architect | 5 | 3,000 |
| `BOSS_ENTROPY_CORE` | Entropy Core | 5 | 4,000 |
| `BOSS_SINGULARITY` | The Singularity | 5 | 5,000 |

---

## 3. Fleet Mastery Points (MP)

Mastery Points represent account-level progression and determine the player's **Ascension Rank**. Unlike Score, MP never resets.

### 3.1 The Mastery Formula
`MP = UniqueDiscoveries + (UniqueArtifacts × 3) + (UniqueZones × 5)`

*   **Unique Discovery (+1)**: Any new codex entry (Threat, PowerUp, Platform, Lore).
*   **Unique Artifact (+3 bonus)**: Rare data caches recovered during runs.
*   **Unique Zone (+5 bonus)**: Reaching a new biome for the first time.

### 3.2 Rank Thresholds (The Prestigious Path)

| Rank | Tier | MP Required | Insignia |
| :--- | :--- | :--- | :--- |
| **I** | Explorer I | 0 | Bronze Triangle |
| **II** | Explorer II | 50 | Silver Square |
| **III** | Explorer III | 150 | Gold Pentagon |
| **IV** | Explorer IV | 300 | Cyan Hexagon |
| **V** | Explorer V | 500 | Purple Stellar Nova |

---

## 4. Credit Economy (The Recovery Path)

Continue Credits allow players to salvage a failed run. They can be earned via rewarded ads or purchased using Jump Credits (JC).

### Tiered Exchange Rates
The cost to purchase a credit increases as the player defeats more bosses, ensuring long-term challenge.

| Bosses Defeated | Exchange Rate (JC → 1 Credit) |
| :--- | :--- |
| **0 - 4** | 250 JC |
| **5 - 9** | 500 JC |
| **10 - 14** | 1,000 JC |
| **15+** | 2,000 JC |

---

## 5. Visual Identity (Cyber-Packet)

Scoring events are communicated through high-fidelity "Cyber-Packets" that fly from the world to the HUD.

### Aesthetic Specifications
*   **Design**: Digital brackets `[ +VALUE ]` with a semi-transparent neon background.
*   **Typography**: Monospace Bold, sized between 16sp (Platforms) and 24sp (Bosses).
*   **Neon Palette**:
    *   **Platforms**: `0xFF00FF41` (Matrix Green)
    *   **Bosses**: `0xFFFFD700` (Plasma Gold)
    *   **Combos**: `0xFFBC13FE` (Hyper Purple)
*   **Motion**: Exponential acceleration towards the Top-Center HUD.
*   **Feedback**: 
    *   **Tail**: 2 ghost trail frames.
    *   **Impact**: Particle blast at HUD coordinates + Score counter scale pulse.

---

## 4. Persistence & Governance

### Dual Metric Tracking
To prevent skill points from breaking distance-based missions, and to provide comprehensive pilot telemetry, multiple metrics are saved:
1.  **`highScore`**: Total cumulative points (Mastery / Leaderboards).
2.  **`highAltitude`**: Physical meters reached (Progress / Missions / Unlocks).
3.  **`lifetimeAltitude`**: Total cumulative distance flown across all expeditions.
4.  **`totalRuns`**: Total number of unique sorties initiated.

### Statistical Self-Correction
The `StatRecorder` includes a migration layer to maintain data integrity across updates:
*   If `lifetimeAltitude` (Cumulative) is less than `highAltitude` (Record), it is automatically corrected to the sum of top runs.
*   `totalRuns` is initialized from the non-zero entry count in the top-run history if it appears uninitialized.

### Agent Rules
*   Never use `score` for zone gating or boss spawns. Use `runAltitude`.
*   Always display the unit **"m"** for Altitude/Ascent metrics and **raw numbers** for Score/Mastery metrics in the UI.
*   Any new boss added to `ThreatRegistry.kt` **must** include a `scoreAward` entry.
*   Platform score is strictly limited to the **first** landing on a specific platform instance.
