"use client";

import { motion } from "framer-motion";
import PlatformSVG from "./game/PlatformSVG";
import ThreatSVG from "./game/ThreatSVG";

export interface Encounter {
  type: "platform" | "enemy" | "boss";
  progress: [number, number, number];
  entity: string;
  zone: string;
  summons?: string[];
  behavior?: string;
  message?: string;
  side?: "left" | "right";
}

export const ENCOUNTERS: Encounter[] = [
  // Earth (0m - 500m) -> progress range: [0, 0.0227]
  {
    type: "platform",
    progress: [0.002, 0.009, 0.016],
    entity: "NORMAL",
    zone: "Earth",
    side: "left",
  },
  {
    type: "enemy",
    progress: [0.005, 0.0136, 0.022],
    entity: "ENT_SCOUT_DRONE",
    zone: "Earth",
    behavior: "track-rocket",
    summons: ["ENT_SWARM_BOTS", "ENT_SWARM_BOTS"],
    message: "Contact detected. Drone swarm incoming.",
  },
  {
    type: "platform",
    progress: [0.01, 0.016, 0.022],
    entity: "MOVING",
    zone: "Earth",
    side: "right",
  },
  // Cloud Layer (500m - 1500m) -> progress range: [0.0227, 0.0682]
  {
    type: "enemy",
    progress: [0.025, 0.034, 0.043],
    entity: "ENT_CLOUD_SKIMMER",
    zone: "Cloud Layer",
    behavior: "drift-across",
  },
  {
    type: "platform",
    progress: [0.03, 0.039, 0.048],
    entity: "BOOST",
    zone: "Cloud Layer",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.045, 0.052, 0.059],
    entity: "ICE",
    zone: "Cloud Layer",
    side: "right",
  },
  {
    type: "enemy",
    progress: [0.05, 0.057, 0.064],
    entity: "ENT_SWARM_BOTS",
    zone: "Cloud Layer",
    behavior: "orbit",
  },
  {
    type: "platform",
    progress: [0.055, 0.062, 0.068],
    entity: "BREAKABLE",
    zone: "Cloud Layer",
    side: "right",
  },
  // Upper Atmosphere (1500m - 4000m) -> progress range: [0.0682, 0.1818]
  {
    type: "enemy",
    progress: [0.08, 0.105, 0.13],
    entity: "ENT_ORBITAL_SENTRY",
    zone: "Upper Atmosphere",
    behavior: "stationary",
  },
  {
    type: "platform",
    progress: [0.085, 0.115, 0.145],
    entity: "PHASE",
    zone: "Upper Atmosphere",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.125, 0.15, 0.175],
    entity: "FUEL",
    zone: "Upper Atmosphere",
    side: "right",
  },
  // Orbit (4000m - 5000m) -> progress range: [0.1818, 0.2273]
  {
    type: "enemy",
    progress: [0.185, 0.198, 0.21],
    entity: "ENT_CORRUPTED_HULL",
    zone: "Orbit",
    behavior: "fade-through",
  },
  {
    type: "platform",
    progress: [0.19, 0.202, 0.214],
    entity: "COOLING",
    zone: "Orbit",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.20, 0.212, 0.224],
    entity: "STABILITY",
    zone: "Orbit",
    side: "right",
  },
  // The Foundry (5000m - 8000m) -> progress range: [0.2273, 0.3636]
  {
    type: "platform",
    progress: [0.24, 0.275, 0.31],
    entity: "CONVEYOR",
    zone: "The Foundry",
    side: "left",
  },
  // Deep Space (8000m - 13000m) -> progress range: [0.3636, 0.5909]
  {
    type: "enemy",
    progress: [0.38, 0.415, 0.45],
    entity: "ENT_STALKER",
    zone: "Deep Space",
    behavior: "hunt",
  },
  {
    type: "platform",
    progress: [0.41, 0.435, 0.46],
    entity: "MAGNETIC",
    zone: "Deep Space",
    side: "left",
  },
  {
    type: "enemy",
    progress: [0.47, 0.51, 0.55],
    entity: "ENT_VOID_WHALE",
    zone: "Deep Space",
    behavior: "looming",
  },
  // Chrono-Rift (13000m - 15000m) -> progress range: [0.5909, 0.6818]
  {
    type: "platform",
    progress: [0.60, 0.635, 0.67],
    entity: "MIMIC",
    zone: "Chrono-Rift",
    side: "right",
  },
  // The Void (15000m - 22000m) -> progress range: [0.6818, 1.0]
  {
    type: "enemy",
    progress: [0.73, 0.77, 0.81],
    entity: "ENT_VOID_WRAITH",
    zone: "The Void",
    behavior: "fade-through",
  },
];

// Boss encounter windows (separate, longer, non-overlapping)
export const BOSS_ENCOUNTERS: Encounter[] = [
  {
    type: "boss",
    progress: [0.0682, 0.091, 0.1136], // 1500m - 2500m
    entity: "COMMAND_CRUISER",
    zone: "Upper Atmosphere",
    message: "CRUISER DETECTED. Platform landing systems jammed.",
  },
  {
    type: "boss",
    progress: [0.1818, 0.2045, 0.2273], // 4000m - 5000m
    entity: "THE_GATEKEEPER",
    zone: "Orbit",
    message: "GATEKEEPER SHIELD ONLINE. Kinetic shields detected.",
  },
  {
    type: "boss",
    progress: [0.2273, 0.2636, 0.2955], // 5000m - 6500m
    entity: "THE_ARCHITECT",
    zone: "The Foundry",
    message: "ARCHITECT OVERRIDE. Automated assembly lines active.",
  },
  {
    type: "boss",
    progress: [0.3182, 0.3409, 0.3636], // 7000m - 8000m
    entity: "THE_LEVIATHAN",
    zone: "The Foundry",
    message: "LEVIATHAN DETECTED. Ethereal wake slipstream warning.",
  },
  {
    type: "boss",
    progress: [0.4545, 0.4886, 0.5227], // 10000m - 11500m
    entity: "STAR_EATER",
    zone: "Deep Space",
    message: "STAR-EATER ACTIVE. Thermal energy absorption spikes.",
  },
  {
    type: "boss",
    progress: [0.5455, 0.5682, 0.5909], // 12000m - 13000m
    entity: "ENTROPY_CORE",
    zone: "Deep Space",
    message: "ENTROPY DRAINER ONLINE. Active ship systems failing.",
  },
  {
    type: "boss",
    progress: [0.6818, 0.7273, 0.7727], // 15000m - 17000m
    entity: "VOID_ENGINE",
    zone: "The Void",
    message: "VOID ENGINE IGNITING. Gravitational distortion active.",
  },
  {
    type: "boss",
    progress: [0.8182, 0.8636, 0.9091], // 18000m - 20000m
    entity: "THE_SIGNAL",
    zone: "The Void",
    message: "THE SIGNAL ACQUIRED. Reality coordinates corrupting.",
  },
];

function getLocalProgress(globalProgress: number, encounterProgress: [number, number, number]) {
  const [enter, peak, exit] = encounterProgress;
  if (globalProgress < enter) return -1; // not started
  if (globalProgress > exit) return 2; // finished
  if (globalProgress < peak) {
    // ENTER phase
    return (globalProgress - enter) / (peak - enter);
  }
  // PEAK -> EXIT phase
  return 1 + (globalProgress - peak) / (exit - peak);
}

function getXPosition(
  behavior: string | undefined,
  localProgress: number,
  rocketX: number,
  side: "left" | "right" | undefined,
  viewW: number
) {
  const cx = viewW / 2;
  const drift = (viewW * 0.25) * (side === "left" ? -1 : 1);

  switch (behavior) {
    case "track-rocket":
      return { x: cx + (rocketX - cx) * 0.6, y: 0 };
    case "drift-across":
      const startX = -120;
      const endX = viewW + 120;
      return { x: startX + (endX - startX) * Math.min(localProgress, 1), y: 0 };
    case "orbit":
    case "summon":
      return { x: cx + drift, y: 0 };
    case "stationary":
      return { x: cx + drift, y: 0 };
    case "fade-through":
      return { x: cx, y: 0 };
    case "hunt":
      return { x: cx + (rocketX - cx) * 0.3, y: 0 };
    case "looming":
      return { x: cx, y: 0 };
    default:
      return { x: cx + drift, y: 0 };
  }
}

function getScale(localProgress: number): number {
  if (localProgress < 0) return 0;
  if (localProgress < 0.3) return 0.5 + localProgress * 1.5; // grow in
  if (localProgress < 0.7) return 1; // hold
  if (localProgress > 2) return 0;
  const exitProgress = (localProgress - 0.7) / 1.3;
  return Math.max(0, 1 - exitProgress);
}

export default function EncounterSystem({
  progress,
  rocketX,
  viewWidth = 1200,
}: {
  progress: number;
  rocketX: number;
  viewWidth?: number;
}) {
  const visibleEncounters: (Encounter & { localProgress: number; x: number; scale: number })[] = [];

  for (const enc of [...ENCOUNTERS, ...BOSS_ENCOUNTERS]) {
    const lp = getLocalProgress(progress, enc.progress);
    if (lp < 0 || lp > 2) continue;

    const pos = getXPosition(enc.behavior, lp, rocketX, enc.side, viewWidth);
    const scale = getScale(lp);

    visibleEncounters.push({ ...enc, localProgress: lp, x: pos.x, scale });
  }

  return (
    <div className="fixed inset-0 z-10 pointer-events-none">
      {visibleEncounters.map((enc, i) => {
        const isBoss = enc.type === "boss";
        const zIndex = isBoss ? 25 : 15;

        if (enc.type === "platform") {
          return (
            <motion.div
              key={`enc-${i}`}
              className="absolute bottom-1/3"
              animate={{
                x: enc.x,
                scale: enc.scale,
                opacity: enc.scale > 0 ? 1 : 0,
              }}
              transition={{
                type: "spring",
                stiffness: 120,
                damping: 18,
              }}
              style={{ zIndex }}
            >
              <PlatformSVG type={enc.entity as any} width={140} height={20} animated />
            </motion.div>
          );
        }

        if (enc.type === "enemy" || enc.type === "boss") {
          const summonsToRender =
            enc.localProgress >= 0.3 && enc.localProgress <= 0.7 && enc.summons
              ? enc.summons.map((s, si) => {
                  const angle = (si / enc.summons!.length) * Math.PI * 2;
                  const orbitR = 70;
                  return {
                    summon: s,
                    x: Math.cos(angle) * orbitR,
                    y: Math.sin(angle) * orbitR,
                  };
                })
              : [];
          return (
            <motion.div
              key={`enc-${i}`}
              className="absolute top-1/3"
              animate={{
                x: enc.x,
                scale: enc.scale,
                opacity: enc.scale > 0 ? 1 : 0,
              }}
              transition={{
                type: "spring",
                stiffness: 100,
                damping: 16,
              }}
              style={{ zIndex }}
            >
              {/* Warning message */}
              {enc.message && enc.localProgress >= 0.2 && enc.localProgress <= 0.8 && (
                <motion.div
                  className="absolute left-1/2 -translate-x-1/2 -top-12 whitespace-nowrap"
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                >
                  <span className="text-xs uppercase tracking-[0.3em] text-red-400 font-bold bg-black/80 px-3 py-1 rounded">
                    {enc.message}
                  </span>
                </motion.div>
              )}

              {/* Main entity */}
              <ThreatSVG type={enc.entity as any} size={isBoss ? 120 : 70} />

              {/* Summons */}
              {summonsToRender.map((s, si) => (
                <motion.div
                  key={si}
                  className="absolute"
                  animate={{
                    x: enc.x + s.x,
                    y: s.y,
                    scale: 0.7,
                  }}
                  transition={{
                    type: "spring",
                    stiffness: 80,
                    damping: 14,
                  }}
                >
                  <ThreatSVG type={s.summon as any} size={35} />
                </motion.div>
              ))}
            </motion.div>
          );
        }

        return null;
      })}
    </div>
  );
}