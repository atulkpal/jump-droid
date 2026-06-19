# Jump Droid: Threat Library (Design Vault)

This document is the permanent repository for all proposed, evaluated, and classified threat-related content. It serves as a source for future development cycles.

---

## 1. Hazards

| Name | Classification | Role | Zone | Difficulty | Logic & Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Cryo-Mist** | **APPROVED** | Heat Controller | Atmosphere | ⭐⭐ | **Freezes Heat bar.** While inside, heat cannot change. Low technical risk; unique tactical choice. |
| **Mirror Shards** | **APPROVED** | Deceiver | Deep Space | ⭐⭐⭐⭐ | **Inverts Horizontal Axis.** High gameplay impact with minimal code overhead. |
| **Gravity Shear** | **APPROVED** | Controller | Deep Space | ⭐⭐⭐ | **Split Force.** Top half pushes UP, bottom half pulls DOWN. Leverages existing physics. |
| **Fuel Leak** | **BACKLOG** | Resource Predator| Deep Space | ⭐⭐⭐ | **3.0x Fuel Consumption.** Good concept, but potentially redundant with fuel enemies. |
| **Magnetic Storm**| **BACKLOG** | Controller | Orbit | ⭐⭐⭐ | **Steering Drift.** Left/Right inputs skewed toward center. Requires careful balancing. |
| **Hull Virus** | **BACKLOG** | Timer | Void | ⭐⭐⭐⭐ | **Integrity Decay.** 2% decay/sec for 10s. Best for endgame difficulty spikes. |
| **Static Tether** | **BACKLOG** | Controller | Orbit | ⭐⭐⭐ | **Stops Vertical Progress.** Requires "Burst Thrust" to break. Technically complex physics. |
| **Chrono-Lag** | **BACKLOG** | Controller | Void | ⭐⭐⭐⭐⭐ | **Input Delay.** 0.2s - 0.5s response lag. Extremely high difficulty and technical cost. |
| **Oxygen Pocket** | **REJECTED** | Support | Atmosphere | ⭐ | **+100% Thrust / 3x Heat.** Redundant with Power-Up logic; risks player confusion. |
| **Solar Wind** | **REJECTED** | Area Denial | Orbit | ⭐⭐ | **Permanent Global Force.** Constant horizontal push. Can feel like a control bug. |

---

## 2. Hostile Entities (Enemies)

| Name | Classification | Role | Visual | Difficulty | Behavior & Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Heat Bat** | **APPROVED** | Ambusher | Dark Silhouette | ⭐⭐⭐ | **Heat Trigger.** Dives when player Heat >= 70%. Encourages thermal management. |
| **Mimic Platform**| **APPROVED** | Deceiver | Platform Clone | ⭐⭐⭐⭐ | **Shatters on Landing.** Deals 15 Integrity damage. High interaction value. |
| **Void Harvester**| **APPROVED** | Resource Predator| Mechanical Squid | ⭐⭐⭐ | **Eats Power-Ups.** Rushes to spawned items. Rewards fast racing for resources. |
| **Phase Wraith** | **APPROVED** | Stalker | Blue Humanoid | ⭐⭐⭐⭐ | **Overheat Only.** Damagable only when player is Overheated. Rewards high-risk play. |
| **Gravity Ram** | **APPROVED** | Ambusher | Triangular Ship | ⭐⭐⭐ | **Telegraphed Dash.** Red line indicator followed by massive knockback charge. |
| **Drift Mine** | **BACKLOG** | Area Denial | Spiky Orb | ⭐⭐⭐ | **Explodes into Debris.** Prox trigger + object spawning. Similar to Debris fields. |
| **Orbital Tug** | **BACKLOG** | Controller | Industrial Ship| ⭐⭐⭐ | **Tractor Beam.** Pulls player toward Hazards. Force-based AI is difficult to tune. |
| **Sentinel Block** | **BACKLOG** | Juggernaut | Rune Stone | ⭐⭐ | **Slow Patrol.** Predictable horizontal movement. Good for early zones only. |
| **Fuel Parasite** | **REJECTED** | Hunter | Tiny Drone | ⭐⭐⭐ | **Attaches & Drains.** Requires "Shaking" to remove. Technically invasive logic. |
| **Shield Remora** | **REJECTED** | Resource Predator| Circular Drone | ⭐⭐ | **Siphons Shields.** Follows at fixed distance. Overlaps too heavily with Void Harvester. |

---

## 3. Mini-Bosses

| Name | Classification | Role | Zone | Difficulty | Behavior & Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Thermal Hive** | **APPROVED** | Summoner | Atmosphere | ⭐⭐⭐ | **Heat-Based Spawning.** Only spawns Swarm Bots when player Heat is high. |
| **Gravity Anchor** | **APPROVED** | Controller | Deep Space | ⭐⭐⭐⭐ | **Climb or Die.** Downward pull increases every 10s. Static boss scaling race. |
| **The Forger** | **APPROVED** | Platform Control| Orbit | ⭐⭐⭐ | **Platform Conversion.** Periodically turns Normal platforms into Ice or Breakable. |
| **Scrap King** | **BACKLOG** | Juggernaut | Orbit | ⭐⭐⭐ | **Debris Shield.** Pulls debris to create armor. Blocked by lack of projectile system. |
| **Nebula Serpent**| **BACKLOG** | Visibility | Atmosphere | ⭐⭐⭐ | **Smoke Trail.** Leaves smoke that hides platforms. Requires "Fog of War" renderer. |

---

## 4. Major Bosses

| Name | Classification | Role | Zone | Difficulty | Behavior & Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **The Architect** | **APPROVED** | Controller | The Foundry | ⭐⭐⭐⭐ | **Level is the Boss.** Rhythmically adds/removes platforms. Reaching the top is the win. |
| **Entropy Core** | **APPROVED** | Resource Predator| Deep Space | ⭐⭐⭐⭐⭐ | **Global Drain.** Simultaneous Fuel/Shield/Heat pressure. Destroy 4 pylons to win. |
| **Chrono Warden** | **BACKLOG** | Time-Warp | Chrono-Rift | ⭐⭐⭐⭐⭐ | **State Rewind.** Periodically resets player pos/stats to 3s prior. High bug risk. |
| **The Singularity**| **BACKLOG** | Meta-Deceiver | Event Horizon | ⭐⭐⭐⭐⭐ | **HUD Distortion.** Pulls UI gauges into center.Reserved for true final expansion. |
| **Solar Monarch** | **REJECTED** | Juggernaut | Solar Core | ⭐⭐⭐⭐ | **Flare Dance.** Redundant with Star Eater and existing Solar Flare mechanics. |

---

## 5. Future Zones

| Name | Classification | Altitude | Atmosphere | Gimmick | Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **The Foundry** | **BACKLOG** | 5000m - 7000m | Mechanical | Fast moving platforms; rare powerful items. | Industrial theme; needs many assets. |
| **Chrono-Rift** | **BACKLOG** | 13000m - 15000m| Glitchy | Time-dilation bubbles (50% speed slow). | Visual hook; needs complex shaders. |
| **Event Horizon** | **BACKLOG** | 20000m+ | White Noise | Gravity/Thrust values change every 1000m. | True final zone for post-Void. |
| **Subterranean** | **REJECTED** | -1000m - 0m | Magma | Narrow bounds; falling rocks; rising heat. | Descent conflicts with Ascent model. |
| **Solar Core** | **REJECTED** | 9000m - 12000m | Blinding | 1% Heat/sec; requires Heat Sinks. | Redundant with existing solar themes. |
