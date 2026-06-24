"use client";

import { motion } from "framer-motion";
import ThreatSVG from "./game/ThreatSVG";

const BOSS_DATA: Record<string, { name: string; behavior: string }> = {
  COMMAND_CRUISER: { name: "COMMAND CRUISER", behavior: "PLATFORM CONSUMER" },
  THE_GATEKEEPER: { name: "THE GATEKEEPER", behavior: "ORBITAL SENTINEL" },
  VOID_ENGINE: { name: "VOID ENGINE", behavior: "REALITY WARP" },
  THE_LEVIATHAN: { name: "THE LEVIATHAN", behavior: "APEX PREDATOR" },
  THE_SIGNAL: { name: "THE SIGNAL", behavior: "VOID SERPENT" },
  STAR_EATER: { name: "STAR-EATER", behavior: "COSMIC ORGANISM" },
};

interface BossEncounterProps {
  entity: "COMMAND_CRUISER" | "THE_GATEKEEPER" | "VOID_ENGINE" | "THE_LEVIATHAN" | "THE_SIGNAL" | "STAR_EATER";
  phase: "ENTER" | "FIGHT" | "EXIT";
  progress: number;
  message?: string;
}

export default function BossEncounter({ entity, phase, progress, message }: BossEncounterProps) {
  const boss = BOSS_DATA[entity] || { name: entity, behavior: "UNKNOWN" };

  const isEnter = phase === "ENTER";
  const isFight = phase === "FIGHT";
  const isExit = phase === "EXIT";

  // Phase-specific visual state
  const bossScale = isEnter ? 0.6 + progress * 0.8 : isFight ? 1 : 1 - progress * 0.3;
  const glows = isFight ? "shadow-[0_0_80px_rgba(255,23,68,0.6)]" : "shadow-[0_0_40px_rgba(255,23,68,0.3)]";

  return (
    <motion.div
      className="fixed inset-0 z-30 flex items-center justify-center pointer-events-none"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      {/* Boss warning overlay */}
      {isFight && (
        <motion.div
          className="absolute inset-0 bg-red-900/10"
          animate={{ opacity: [0.2, 0.4, 0.2] }}
          transition={{ duration: 0.8, repeat: Infinity }}
        />
      )}

      {/* Boss entity */}
      <motion.div
        className={`relative ${glows}`}
        animate={{
          scale: bossScale,
          y: isEnter ? [80, 0] : isExit ? [0, -60] : 0,
        }}
        transition={{
          type: "spring",
          stiffness: 140,
          damping: 16,
        }}
      >
        {/* Label */}
        <div className="absolute -top-16 left-1/2 -translate-x-1/2 text-center whitespace-nowrap">
          <motion.p
            className="text-xs uppercase tracking-[0.4em] text-red-400 font-bold bg-black/80 px-4 py-1.5 rounded"
            animate={{ opacity: [0.7, 1, 0.7] }}
            transition={{ duration: autoRepeat(), repeat: Infinity }}
          >
            THREAT DETECTED
          </motion.p>
          <p className="mt-2 text-2xl font-black text-white tracking-wider">{boss.name}</p>
          <p className="text-xs text-red-300/80 tracking-widest uppercase">{boss.behavior}</p>
        </div>

        {/* Boss visual */}
        <ThreatSVG type={entity} size={isFight ? 160 : 140} />

        {/* Phase indicator */}
        {isFight && (
          <div className="absolute -bottom-12 left-1/2 -translate-x-1/2">
            <div className="flex gap-2">
              <span className="text-[10px] tracking-widest text-red-400/60">ENGAGED</span>
            </div>
          </div>
        )}
      </motion.div>
    </motion.div>
  );
}

function autoRepeat() {
  return 0.6 + Math.random() * 0.4;
}