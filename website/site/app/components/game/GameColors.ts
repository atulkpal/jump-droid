// Game Color Palette — mapped from app/.../ui/theme/Color.kt
export const Colors = {
  // Core SciFi palette (matching game exactly)
  SciFiWhite: "#FFFFFF",
  SciFiCyan: "#00E5FF",      // Progression / Missions / Combo
  SciFiGold: "#FFD700",      // Rewards / Achievements
  SciFiRed: "#FF1744",       // Danger / Heat / Critical
  SciFiGreen: "#00E676",     // Recovery / Fuel
  SciFiPurple: "#D500F9",    // Discovery / Mystery / Artifacts
  SciFiBackground: "#0D001A",
  SciFiSurface: "#1A1A1A",
  SciFiBorder: "rgba(255,255,255,0.1)",

  // Rocket body colors (from RocketRenderer.kt)
  RocketBalanced: "#FFFFFF",
  RocketScout: "#FFD700",
  RocketTank: "#455A64",
  RocketExperimental: "#D500F9",

  // Zone background colors (from ZoneBackgroundRenderer.kt)
  ZoneEarthTop: "#1A237E",
  ZoneEarthMid: "#2196F3",
  ZoneEarthBot: "#66BB6A",
  ZoneCloudTop: "#00BCD4",
  ZoneCloudBot: "#311B92",
  ZoneAtmosTop: "#0D001A",
  ZoneAtmosBot: "#311B92",
  ZoneOrbitTop: "#000411",
  ZoneOrbitBot: "#1A0033",
  ZoneSpace: "#000000",
  ZoneVoid: "#000000",

  // Engine
  EngineNozzle: "#37474F",
  EngineNozzleDark: "#263238",
  FinColor: "#FF1744",
  CockpitGlow: "#00E5FF",
} as const;

// Altitude zone thresholds (from Constants.kt)
export const ZoneThresholds = [
  { zone: "Earth", threshold: 0, subtitle: "The Journey Begins" },
  { zone: "Cloud Layer", threshold: 500, subtitle: "Above The Clouds" },
  { zone: "Upper Atmosphere", threshold: 1500, subtitle: "Edge Of The Sky" },
  { zone: "Orbit", threshold: 4000, subtitle: "First Orbital Ascent" },
  { zone: "The Foundry", threshold: 5000, subtitle: "Automated Manufacturing Belt" },
  { zone: "Deep Space", threshold: 8000, subtitle: "Uncharted Territory" },
  { zone: "Chrono-Rift", threshold: 13000, subtitle: "Time-Fractured Anomaly" },
  { zone: "The Void", threshold: 15000, subtitle: "Unknown Region Detected" },
];

// Rocket configs (from Models.kt)
export const RocketConfigs = [
  {
    id: "BALANCED",
    name: "Explorer",
    color: Colors.RocketBalanced,
    thrust: 1.0,
    fuel: 1.0,
    heat: 1.0,
    unlockScore: 0,
    trait: "Sensor Array",
    traitDesc: "+20% discovery range",
  },
  {
    id: "SCOUT",
    name: "Striker",
    color: Colors.RocketScout,
    thrust: 1.25,
    fuel: 0.7,
    heat: 0.9,
    unlockScore: 2000,
    trait: "Target Lock",
    traitDesc: "Precision weak point strikes",
  },
  {
    id: "TANK",
    name: "Heavy",
    color: Colors.RocketTank,
    thrust: 0.85,
    fuel: 1.5,
    heat: 0.8,
    unlockScore: 5000,
    trait: "Kinetic Mass",
    traitDesc: "Shockwaves on weak point destruction",
  },
  {
    id: "EXPERIMENTAL",
    name: "Prototype",
    color: Colors.RocketExperimental,
    thrust: 1.5,
    fuel: 1.0,
    heat: 1.4,
    unlockScore: 10000,
    trait: "Overclocked Core",
    traitDesc: "Steer while overheated",
  },
  {
    id: "STEALTH",
    name: "Stealth",
    color: "#D500F9",
    thrust: 1.0,
    fuel: 0.8,
    heat: 1.0,
    unlockScore: 12000,
    trait: "Cloaking Field",
    traitDesc: "Evade patrols",
  },
  {
    id: "REFLECTOR",
    name: "Reflector",
    color: "#FF1744",
    thrust: 1.1,
    fuel: 1.0,
    heat: 1.2,
    unlockScore: 15000,
    trait: "Reactive Armor",
    traitDesc: "Physical knockback reflection",
  },
];

// Platform types (from Platform.kt + PlatformRenderer.kt)
export const PlatformTypes = [
  { type: "NORMAL", color: Colors.SciFiWhite, label: "", desc: "Safe landing zone" },
  { type: "MOVING", color: Colors.SciFiCyan, label: "", desc: "Oscillates horizontally" },
  { type: "BOOST", color: Colors.SciFiGold, label: "", desc: "Launches upward" },
  { type: "ICE", color: "#80DEEA", label: "", desc: "Very slippery" },
  { type: "BREAKABLE", color: Colors.SciFiRed, label: "", desc: "Collapses on landing" },
  { type: "PHASE", color: Colors.SciFiPurple, label: "", desc: "Appears and disappears" },
  { type: "FUEL", color: Colors.SciFiGreen, label: "FUEL", desc: "Restores fuel" },
  { type: "COOLING", color: Colors.SciFiCyan, label: "COOL", desc: "Reduces heat" },
  { type: "STABILITY", color: Colors.SciFiWhite, label: "STAB", desc: "Improves control" },
  { type: "MAGNETIC", color: Colors.SciFiPurple, label: "", desc: "Gravity field" },
  { type: "CONVEYOR", color: "#4b5563", label: "", desc: "Horizontal slide" },
  { type: "MIMIC", color: Colors.SciFiRed, label: "", desc: "Shatters and damages" },
];