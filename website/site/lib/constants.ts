export const PLAY_STORE_URL =
  "https://play.google.com/store/apps/details?id=com.ashwathai.jump_droid";

export const BETA_TESTING_URL =
  "https://play.google.com/apps/testing/com.ashwathai.jump_droid";

export const SOCIAL_LINKS = {
  github: "https://github.com/atulkpal/jump-droid",
  itchIo: "https://ashwathai.itch.io/jump-droid",
  email: "mailto:ashwathai.dev@gmail.com",
  privacy: "/privacy",
} as const;

export const SCREENSHOTS = [
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/01-begin-the-ascension.jpg",
    alt: "Begin the Ascension — title screen with scanning drone",
    caption: "Begin the Ascension",
  },
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/02-master-precision-flight.jpg",
    alt: "Master Precision Flight — gameplay in Earth zone",
    caption: "Master Precision Flight",
  },
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/03-face-colossal-bosses.jpg",
    alt: "Face Colossal Bosses — Cloud Commander encounter",
    caption: "Face Colossal Bosses",
  },
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/04-build-your-perfect-fleet.jpg",
    alt: "Build Your Perfect Fleet — hangar customization",
    caption: "Build Your Perfect Fleet",
  },
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/05-discover-a-lost-civilization.jpg",
    alt: "Discover a Lost Civilization — lore and discovery",
    caption: "Discover a Lost Civilization",
  },
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/06-adapt-or-be-destroyed.jpg",
    alt: "Adapt or Be Destroyed — HUD with all gauges",
    caption: "Adapt or Be Destroyed",
  },
  {
    src: "https://raw.githubusercontent.com/atulkpal/jump-droid/master/media/screenshots/07-every-expedition-makes-you-stronger.jpg",
    alt: "Every Expedition Makes You Stronger — endgame Ascension",
    caption: "Every Expedition Makes You Stronger",
  },
] as const;

export const FEATURES = [
  {
    icon: "🚀",
    title: "Precision Flight",
    description:
      "Touch to thrust, steer by tilting. Master fuel, heat, and shield management through 8 atmospheric zones.",
  },
  {
    icon: "⚔️",
    title: "Boss Encounters",
    description:
      "Face 11 unique bosses with multi-phase attack patterns, weak point targeting, and distinct AI.",
  },
  {
    icon: "🏗️",
    title: "Build Your Fleet",
    description:
      "Choose from 6 rocket classes and customize with unlockable modules. Each build changes how you play.",
  },
  {
    icon: "🌍",
    title: "Discover Lore",
    description:
      "Hidden signals, secret missions, and Codex entries reward thorough exploration of every zone.",
  },
  {
    icon: "🛡️",
    title: "Adapt or Fall",
    description:
      "14 platform types, 26+ threats, and environmental hazards keep every ascent unpredictable.",
  },
  {
    icon: "♾️",
    title: "Endless Ascent",
    description:
      "Prestige system, Eternal Mode, and infinite scaling beyond 100,000m for endless replayability.",
  },
] as const;
