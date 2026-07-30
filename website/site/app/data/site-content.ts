export interface EntitySpec {
  visual: "rocket" | "platform" | "threat";
  type: string;
}

export interface GameCategory {
  id: string;
  title: string;
  subtitle: string;
  entities: EntitySpec[];
}

export interface HeroFeature {
  label: string;
  value: string;
}

export const HERO = {
  tagline: "ZEN MASTERY & IMPOSSIBLE UPLINK",
  title: "JUMP DROID",
  subtitle: "Pilot the ultimate droid explorer",
  description:
    "Jump Droid is a free, open-source Android arcade game. Touch to thrust. Manage fuel, heat, and shield as you climb through 12 atmospheric zones. Land on platforms, build combos, face bosses, unlock new rockets, and discover the truth hidden in the void.",
  cta: "Download on Play Store",
  ctaSecondary: "Download APK (GitHub)",
  ctaBeta: "⭐ Become a Beta Tester",
  modalTitle: "Jump Droid is currently in Beta",
  modalBody: [
    "🚀 Early Access",
    "🎁 Exclusive Beta Rewards",
    "🏆 Rewards for Outstanding Contributors",
    "💬 Help Shape Jump Droid",
  ],
  taglines: [
    "🚀 Early Access",
    "🎁 Exclusive Beta Rewards",
    "🏆 Rewards for Outstanding Testers",
    "💬 Help Shape Jump Droid",
    "⭐ Limited Beta Slots",
  ],
  features: [
    { label: "Zen Mode", value: "A peaceful, endless ascent focused on rhythm and atmosphere without boss interruptions." },
    { label: "Fleet Mastery", value: "Unlock and customize 12 unique rocket variants, each with modular progression paths." },
    { label: "Tactical Resource Loop", value: "Balance fuel consumption, thruster heat cooldown, shield recharge, and permanent hull integrity." },
  ] as HeroFeature[],
} as const;

export const ENTITY_DESCRIPTIONS: Record<string, string> = {
  NORMAL: "Solid ground. Reliable. The only platform that never betrays you.",
  MOVING: "Shifts left and right without warning. Patience is the only way across.",
  BOOST: "Touching it fires an upward thrust. Momentum is both a gift and a hazard.",
  ICE: "Zero friction. Once you land, there is no stopping — only timing.",
  BREAKABLE: "Cracks under pressure. One landing is all it gets. Keep moving.",
  PHASE: "Flickers in and out of existence. The pattern is predictable. You are not.",
  FUEL: "Contains emergency reserves. Every drop matters above 10,000m.",
  COOLING: "Instantly vents thruster heat. Essential for surviving the intense friction of the upper reaches.",
  STABILITY: "Provides a temporary gyroscopic lock, neutralizing gravity shear and turbulent winds.",
  MAGNETIC: "Pulls your hull. Landing is easy. Leaving is the real challenge.",
  CONVEYOR: "Moves you sideways whether you want it or not. Plan your exit.",
  MIMIC: "Perfectly imitates a solid platform until you land. Then it shatters.",
  SURVEYOR_PROBE: "Scans your approach. Fires a warning shot. Then the real assault begins.",
  DEFENSE_NODE: "Rotating shield array. Shoot the core between cycles — if you dare.",
  COMMAND_CRUISER: "Orbital battle platform. Turrets, drones, and a hull that regenerates.",
  THE_GATEKEEPER: "Guards the boundary between zones. No one has seen past it and returned unchanged.",
  VOID_ENGINE: "An ancient machine powered by dark matter. Its attacks follow no known pattern.",
  THE_LEVIATHAN: "Living fortress. Its weak points move. Its rage is infinite.",
  THE_ARCHITECT: "The master of The Foundry. Rhythmically manipulates the very platforms you stand on.",
  ENTROPY_CORE: "Global resource drain. You must destroy its pylons before your reserves hit zero.",
  STAR_EATER: "Colossal cosmic organism. Tracks your heat signature and accelerates as the hunt intensifies.",
  THE_SIGNAL: "Deceptive intelligence from The Void. Creates false navigation cues to lead you astray.",
  THERMAL_HIVE: "A biological swarm-nest that reacts to heat. Spawns reinforcements when your thrusters run hot.",
  GRAVITY_ANCHOR: "Static defense unit that increases gravitational pull. Outclimb its influence or be dragged down.",
  THE_FORGER: "Orbital drone that manipulates matter, converting solid platforms into ice or breakable debris.",
  THE_SINGULARITY: "The final encounter at Point Zero. Gravity flux, HUD distortion, and the truth of the climb.",
  BALANCED: "The starting point. Versatile, forgiving, and surprisingly capable in skilled hands.",
  SCOUT: "Light frame, blazing speed. Outrun everything — if you don't get hit.",
  TANK: "Heavy armor, slow climb. You can take punishment. Can you deal it?",
  EXPERIMENTAL: "Unstable prototype. Extreme power output. Extreme risk of catastrophic failure.",
  STEALTH: "Invisible to enemy sensors. Until you fire. Then you have three seconds to relocate.",
  REFLECTOR: "Its hull bends energy attacks back at the sender. You are your own worst enemy.",
  SKY_RAY: "Beams from above. No warning. No cover. Learn the gaps in the pattern.",
  AEROSOL_SWARM: "Thousands of micro-drones. Individually harmless. Collectively lethal.",
  DERELICT_ECHO: "The ghost of a fallen ship. It mimics your movements. It is learning.",
  VOID_TRACKER: "Locks onto your heat signature. The only way to lose it is to go cold.",
  VOID_WRAITH: "Phase-shifting assassin. It appears behind you. Always behind you.",
  VOID_HARVESTER: "Mechanical scavenger that hunts for power-ups. Race it to the resource or lose your edge.",
  PHASE_WRAITH: "Ethereal stalker from the Chrono-Rift. Only vulnerable when your hull is overheated.",
  COSMIC_LEVIATHAN: "A creature that swims through space itself. You are not its first prey.",
  HEAT_BAT: "Attracted to your thruster flame. The hotter you run, the more they come.",
  CRYO_MIST: "Freezing vapor. Slows your thrust response. One mistake is all it needs.",
  MIRROR_SHARDS: "Inverted gravity fields. Up becomes down. Down becomes certain death.",
  GRAVITY_SHEAR: "Sudden gravitational spikes. Your trajectory is never your own.",
  MIMIC_PLATFORM: "Looks like solid ground. It is not. Trust nothing.",
  GRAVITY_RAM: "Invisible force that slams you toward the nearest surface. Brace.",
  THE_FOUNDRY: "Ancient automated manufacturing belt in high orbit. Fast-moving platforms and industrial hazards demand precise timing.",
  CHRONO_RIFT: "A fractured region where time flows irregularly. Navigate through time-dilation bubbles and glitchy anomalies.",
  THE_BEYOND: "Where matter blurs into pure energy. Survive extreme temperature duality — from freezing mist to solar radiation.",
  STELLAR_GATE: "A massive artificial sky construct. Navigate through geometric mega-structures older than civilization.",
  ANCIENT_CONSTRUCT: "The heart of the signal. Organic-mechanical architecture pulsing with void-light at the edge of reality.",
  SINGULARITY: "The end of the climb. Non-Euclidean white-noise space where physics and thrust values fluctuate wildly.",
};

export const TRANSMISSION_LINES = [
  "A transmission was detected",
  "at the edge of known space.",
  "Origin: Unknown.",
  "Contents: Classified.",
  "What lies beyond the signal?",
] as const;

export const GAME_CATEGORIES: GameCategory[] = [
  {
    id: "platforms",
    title: "14 Platform Types",
    subtitle: "Each demands a different approach",
    entities: [
      { visual: "platform", type: "NORMAL" },
      { visual: "platform", type: "MOVING" },
      { visual: "platform", type: "BOOST" },
      { visual: "platform", type: "ICE" },
      { visual: "platform", type: "BREAKABLE" },
      { visual: "platform", type: "PHASE" },
      { visual: "platform", type: "FUEL" },
      { visual: "platform", type: "COOLING" },
      { visual: "platform", type: "STABILITY" },
      { visual: "platform", type: "MAGNETIC" },
      { visual: "platform", type: "CONVEYOR" },
      { visual: "platform", type: "MIMIC" },
    ],
  },
  {
    id: "bosses",
    title: "12 Boss Encounters",
    subtitle: "Multi-phase predators. No patterns repeat.",
    entities: [
      { visual: "threat", type: "SURVEYOR_PROBE" },
      { visual: "threat", type: "COMMAND_CRUISER" },
      { visual: "threat", type: "THE_GATEKEEPER" },
      { visual: "threat", type: "VOID_ENGINE" },
      { visual: "threat", type: "THE_LEVIATHAN" },
      { visual: "threat", type: "THE_ARCHITECT" },
      { visual: "threat", type: "ENTROPY_CORE" },
      { visual: "threat", type: "STAR_EATER" },
      { visual: "threat", type: "THE_SIGNAL" },
      { visual: "threat", type: "THERMAL_HIVE" },
      { visual: "threat", type: "GRAVITY_ANCHOR" },
      { visual: "threat", type: "THE_FORGER" },
    ],
  },
  {
    id: "rockets",
    title: "12 Rocket Variants",
    subtitle: "Each changes how you fly, fight, and fall",
    entities: [
      { visual: "rocket", type: "BALANCED" },
      { visual: "rocket", type: "SCOUT" },
      { visual: "rocket", type: "TANK" },
      { visual: "rocket", type: "EXPERIMENTAL" },
      { visual: "rocket", type: "STEALTH" },
      { visual: "rocket", type: "REFLECTOR" },
    ],
  },
  {
    id: "threats",
    title: "32+ Threat Variants",
    subtitle: "From swarms to cosmic leviathans",
    entities: [
      { visual: "threat", type: "SKY_RAY" },
      { visual: "threat", type: "VOID_TRACKER" },
      { visual: "threat", type: "COSMIC_LEVIATHAN" },
      { visual: "threat", type: "VOID_HARVESTER" },
      { visual: "threat", type: "PHASE_WRAITH" },
      { visual: "threat", type: "GRAVITY_RAM" },
      { visual: "threat", type: "HEAT_BAT" },
    ],
  },
  {
    id: "hazards",
    title: "Environmental Hazards",
    subtitle: "Heat, cryo-mist, gravity shear, and more",
    entities: [
      { visual: "threat", type: "CRYO_MIST" },
      { visual: "threat", type: "MIRROR_SHARDS" },
      { visual: "threat", type: "GRAVITY_SHEAR" },
      { visual: "threat", type: "MIMIC_PLATFORM" },
    ],
  },
];

export const MISSION_LOG = {
  title: "MISSION LOG",
  heading: "SOURCE CODE",
  status: "PUBLIC" as const,
  description: "The full transmission — source code, assets, and schematics — is publicly available.",
  license: "MIT License",
} as const;

export const DOWNLOADS = {
  title: "INSTALL JUMP DROID",
  version: "v2.2.3",
  description: "Free. Open source. Available on all major platforms.",
} as const;

export const BETA = {
  title: "BECOME AN EARLY PILOT",
  description: "Join the beta channel and fly before the public release.",
} as const;

export const FOOTER = {
  tagline: "Built by Ashwath AI",
  description: "Building free, open-source software, AI, and games for everyone.",
} as const;
