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

export const HERO = {
  title: "THE SIGNAL",
  cta: "BEGIN MISSION",
} as const;

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
      { visual: "platform", type: "MAGNETIC" },
      { visual: "platform", type: "CONVEYOR" },
    ],
  },
  {
    id: "bosses",
    title: "11 Boss Encounters",
    subtitle: "Multi-phase predators. No patterns repeat.",
    entities: [
      { visual: "threat", type: "SURVEYOR_PROBE" },
      { visual: "threat", type: "DEFENSE_NODE" },
      { visual: "threat", type: "COMMAND_CRUISER" },
      { visual: "threat", type: "THE_GATEKEEPER" },
      { visual: "threat", type: "VOID_ENGINE" },
      { visual: "threat", type: "THE_LEVIATHAN" },
    ],
  },
  {
    id: "rockets",
    title: "6 Rocket Classes",
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
    title: "26+ Threat Variants",
    subtitle: "From swarms to cosmic leviathans",
    entities: [
      { visual: "threat", type: "SKY_RAY" },
      { visual: "threat", type: "AEROSOL_SWARM" },
      { visual: "threat", type: "DERELICT_ECHO" },
      { visual: "threat", type: "VOID_TRACKER" },
      { visual: "threat", type: "VOID_WRAITH" },
      { visual: "threat", type: "COSMIC_LEVIATHAN" },
    ],
  },
  {
    id: "hazards",
    title: "Environmental Hazards",
    subtitle: "Heat, cryo-mist, gravity shear, and more",
    entities: [
      { visual: "threat", type: "HEAT_BAT" },
      { visual: "threat", type: "CRYO_MIST" },
      { visual: "threat", type: "MIRROR_SHARDS" },
      { visual: "threat", type: "GRAVITY_SHEAR" },
      { visual: "threat", type: "MIMIC_PLATFORM" },
      { visual: "threat", type: "GRAVITY_RAM" },
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
  version: "v1.5.1",
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
