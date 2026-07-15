export interface PacketData {
  id: string;
  tag: string;
  label: string;
  bodyLines: string[];
  visualType?: "rocket" | "platform" | "threat";
  screenshotIndex?: number;
  accent?: "cyan" | "gold" | "red";
}

export const INTRO_LINES = [
  "INCOMING TRANSMISSION...",
  "LOCKING SIGNAL...",
];

export const COORDINATE_LINES = [
  "SOURCE:                UNKNOWN",
  "TARGET:                UPPER ATMOSPHERE",
  "MISSION:               CLASSIFIED",
  "BANDWIDTH:             NOMINAL",
  "RECEIVING:             6 PACKETS",
];

export const PACKETS: PacketData[] = [
  {
    id: "ascent",
    tag: "DATA PACKET 1/6",
    label: "ASCENT PROTOCOL",
    bodyLines: [
      "The expedition begins with a single ascent. Our logs show the droid clears the launch zone, but thermal buildup is immediate.",
      "Propulsion reserves deplete faster than projected. The first 2,000 meters separate those who manage their resources from those who burn out.",
      "HEAT: RISING. FUEL: DRAINING. ASCENT: CONTINUING.",
    ],
    visualType: "rocket",
    screenshotIndex: 0,
  },
  {
    id: "landing",
    tag: "DATA PACKET 2/6",
    label: "LANDING SITES",
    bodyLines: [
      "At 4,000 meters the atmosphere changes. Structures appear — ancient platforms suspended in the current.",
      "Not natural. Not random. Each one demands precise timing. Miss the approach and you fall. Land clean and the ascent continues.",
      "14 platform types logged. Surface composition varies. Approach protocols required for each.",
    ],
    visualType: "platform",
    screenshotIndex: 1,
  },
  {
    id: "entities",
    tag: "DATA PACKET 3/6",
    label: "ANOMALOUS ENTITIES",
    bodyLines: [
      "At 8,000 meters we encountered the first entity. Classification: Cloud Commander.",
      "Multi-phase attack pattern detected. Weak points cycle. Evasion alone is not sufficient — return fire is mandatory.",
      "Only 1 in 3 expeditions survive the first encounter. Subsequent entities escalate in complexity.",
    ],
    visualType: "threat",
    screenshotIndex: 2,
    accent: "red",
  },
  {
    id: "craft",
    tag: "DATA PACKET 4/6",
    label: "UNKNOWN CRAFT",
    bodyLines: [
      "Fleet database contains 6 unregistered vehicle designs. Each one changes the ascent profile entirely.",
      "Light scouts consume less fuel. Heavy armor absorbs impact but slows climb. Some are optimized for combat, others for evasion.",
      "Choose your craft. Adapt your strategy. The expedition evolves with every choice.",
    ],
    visualType: "rocket",
    screenshotIndex: 3,
  },
  {
    id: "protocol",
    tag: "DATA PACKET 5/6",
    label: "OPEN PROTOCOL",
    bodyLines: [
      "Signal architecture uses open standards. The full transmission — source code, assets, schematics — is publicly available.",
      "Licensed under MIT. Replication and modification are not only permitted but encouraged.",
      "Signal logs maintained at: github.com/atulkpal/jump-droid",
      "Early deployment channel open at play.google.com/apps/testing/com.ashwathai.jump_droid",
    ],
    accent: "gold",
  },
  {
    id: "final",
    tag: "DATA PACKET 6/6",
    label: "FINAL STAGE",
    bodyLines: [
      "At 20,000 meters the signal changes. Something is transmitting back from beyond the known zones.",
      "Origin: UNKNOWN. Contents: CLASSIFIED. The logs suggest the ascent was never the objective.",
      "It was never about reaching the top. It was about what we would find there.",
    ],
    visualType: "threat",
    screenshotIndex: 6,
    accent: "red",
  },
];
