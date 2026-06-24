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
  // Surveyor Probe — tracks rocket, summons swarm
  {
    type: "enemy",
    progress: [0.04, 0.10, 0.16],
    entity: "ENT_SCOUT_DRONE",
    zone: "Earth",
    behavior: "track-rocket",
    summons: ["AEROSOL_SWARM", "AEROSOL_SWARM"],
    message: "Contact detected. Drone swarm incoming.",
  },
  {
    type: "platform",
    progress: [0.06, 0.09, 0.14],
    entity: "NORMAL",
    zone: "Earth",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.09, 0.13, 0.18],
    entity: "MOVING",
    zone: "Earth",
    side: "right",
  },
  // Sky Ray drifts across
  {
    type: "enemy",
    progress: [0.20, 0.26, 0.32],
    entity: "ENT_CLOUD_SKIMMER",
    zone: "Cloud Layer",
    behavior: "drift-across",
  },
  {
    type: "platform",
    progress: [0.22, 0.26, 0.30],
    entity: "BOOST",
    zone: "Cloud Layer",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.26, 0.30, 0.35],
    entity: "ICE",
    zone: "Cloud Layer",
    side: "right",
  },
  // Swarm bots
  {
    type: "enemy",
    progress: [0.36, 0.42, 0.48],
    entity: "ENT_SWARM_BOTS",
    zone: "Cloud Layer",
    behavior: "orbit",
  },
  {
    type: "platform",
    progress: [0.38, 0.42, 0.47],
    entity: "BREAKABLE",
    zone: "Cloud Layer",
    side: "right",
  },
  // Defense Node in Upper Atmosphere
  {
    type: "enemy",
    progress: [0.52, 0.58, 0.64],
    entity: "ENT_ORBITAL_SENTRY",
    zone: "Upper Atmosphere",
    behavior: "stationary",
  },
  {
    type: "platform",
    progress: [0.54, 0.58, 0.63],
    entity: "PHASE",
    zone: "Upper Atmosphere",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.58, 0.62, 0.67],
    entity: "FUEL",
    zone: "Upper Atmosphere",
    side: "right",
  },
  // Derelict Echo
  {
    type: "enemy",
    progress: [0.70, 0.76, 0.82],
    entity: "ENT_CORRUPTED_HULL",
    zone: "Orbit",
    behavior: "fade-through",
  },
  {
    type: "platform",
    progress: [0.72, 0.76, 0.80],
    entity: "COOLING",
    zone: "Orbit",
    side: "left",
  },
  {
    type: "platform",
    progress: [0.76, 0.80, 0.84],
    entity: "STABILITY",
    zone: "Orbit",
    side: "right",
  },
  // Void Tracker in Deep Space
  {
    type: "enemy",
    progress: [0.84, 0.90, 0.95],
    entity: "ENT_STALKER",
    zone: "Deep Space",
    behavior: "hunt",
  },
  // COSMIC LEVIATHAN as mini-boss before Void Engine
  {
    type: "enemy",
    progress: [0.88, 0.94, 0.99],
    entity: "ENT_VOID_WHALE",
    zone: "Deep Space",
    behavior: "looming",
  },
];

// Boss encounter windows (separate, longer)
export const BOSS_ENCOUNTERS: Encounter[] = [
  {
    type: "boss",
    progress: [0.20, 0.35, 0.50],
    entity: "COMMAND_CRUISER",
    zone: "Orbit",
    message: "PLATFORM CONSUMER DETECTED. All landing zones compromised.",
  },
  {
    type: "boss",
    progress: [0.45, 0.62, 0.78],
    entity: "THE_GATEKEEPER",
    zone: "Deep Space",
    message: "ORBITAL SENTINEL ACTIVE. Rotating safe zones detected.",
  },
  {
    type: "boss",
    progress: [0.75, 0.88, 0.98],
    entity: "VOID_ENGINE",
    zone: "The Void",
    message: "REALITY WARP SIGNATURE. Gravity systems failing.",
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
              className="absolute"
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