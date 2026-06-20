# Jump Droid: Ascension Sound Design (Planning)

This report defines the "Sonic Palette" and technical requirements for Jump Droid's first audio implementation.

---

## 1. The Sonic Palette: "Industrial Loneliness"
The soundscape should reflect the isolation of the ascent and the unnatural nature of the Great Signal.

- **The Rocket:** Deep, vibrating mechanical hums. Low-frequency thuds on landing.
- **The Hazards:** Static crackles (EMP), rushing wind (Turbulence), metallic clangs (Debris).
- **The Signal:** Glitchy, non-musical digital chirps and distorted radio "white noise."
- **The Ascension Team:** Low-fidelity radio static and "beep-code" confirmations during Satellite Strikes.

---

## 2. Core Audio Systems

### A. The Engine (`SoundManager.kt`)
- **SoundPool:** For short, latency-critical effects (Thrust, Land, Hit, Pulse).
- **MediaPlayer:** For long, looping atmospheric tracks (Zone Ambience).
- **Priority System:** Ensure critical alerts (Low Fuel, HUD Critical) cut through the mix.

### B. Dynamic Ambience
As altitude increases, the sound should shift:
- **Earth:** Birds, gentle wind, muffled distant machinery.
- **Orbit:** Absolute silence interrupted only by the rocket's vibration and suit breathing.
- **The Void:** Eerie, reversing echoes and heavy "reality tear" sounds.

---

## 3. The "Sound Roster" (Initial Implementation)

| Trigger | Sound Effect | Description |
| :--- | :--- | :--- |
| **Thrust Start** | Ignition Snap | Short metallic click + ignition roar. |
| **Landing** | Hydraulic Thud | Heavy pneumatic impact sound. |
| **Scout Detect** | Digital Ping | High-pitched "Alert" chirp. |
| **Satellite Strike** | Static Charge | Rising electrical whine -> Massive low-end boom. |
| **Weak Point Pop** | Glass Fracture | Sharp crystalline shatter. |
| **Low Fuel** | Geiger Click | Increasing frequency of warning clicks. |

---

## 4. Voice & Comms (The "Ghost in the Machine")
Instead of full voice acting, use **"Syllable-Synth"** (Animal Crossing style but darker/mechanical) for:
- **Ascension Team:** "TELEMETRY SYNCED... LOCKING TARGET..."
- **The Signal:** Unintelligible, reversing digital whispers.
