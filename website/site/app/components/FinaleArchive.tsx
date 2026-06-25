"use client";

import { motion } from "framer-motion";
import ThreatSVG from "./game/ThreatSVG";
import PlatformSVG from "./game/PlatformSVG";
import RocketSVG from "./game/RocketSVG";

const HAZARDS = [
  { name: "Lightning Storm", zone: "Cloud→Void", tier: 2 },
  { name: "Turbulence Front", zone: "Cloud→Void", tier: 1 },
  { name: "Debris Field", zone: "Upper Atmos→Void", tier: 2 },
  { name: "Radiation Zone", zone: "Orbit→Void", tier: 3 },
  { name: "Solar Flare", zone: "Orbit→Void", tier: 3 },
  { name: "EMP Pulse", zone: "Orbit→Void", tier: 3 },
  { name: "Gravity Distortion", zone: "Deep Space→Void", tier: 4 },
  { name: "Void Anomaly", zone: "Deep Space→Void", tier: 4 },
];

const ENEMIES = [
  { name: "Surveyor Probe", zone: "Earth→Orbit", tier: 1 },
  { name: "Sky Ray", zone: "Cloud Layer", tier: 1 },
  { name: "Aerosol Swarm", zone: "Cloud→Upper Atmos", tier: 2 },
  { name: "Defense Node", zone: "Orbit", tier: 2 },
  { name: "Derelict Echo", zone: "Deep Space", tier: 2 },
  { name: "Void Tracker", zone: "Deep Space", tier: 3 },
  { name: "Cosmic Leviathan", zone: "Deep Space→Void", tier: 3 },
  { name: "Shadow Entity", zone: "Void", tier: 3 },
];

const BOSSES = [
  { type: "COMMAND_CRUISER" as const, name: "Command Cruiser", zone: "Orbit", behavior: "PLATFORM CONSUMER" },
  { type: "THE_GATEKEEPER" as const, name: "The Gatekeeper", zone: "Upper Atmosphere", behavior: "ROTATING SHIELD" },
  { type: "STAR_EATER" as const, name: "Star-Eater", zone: "Orbit", behavior: "LIGHT DEVOURER" },
  { type: "VOID_ENGINE" as const, name: "Void Engine", zone: "The Void", behavior: "REALITY WARP" },
  { type: "THE_LEVIATHAN" as const, name: "The Leviathan", zone: "Deep Space", behavior: "SLIPSTREAM HUNTER" },
  { type: "THE_SIGNAL" as const, name: "The Signal", zone: "The Void", behavior: "HUD DECOY" },
  { type: "THE_ARCHITECT" as const, name: "The Architect", zone: "The Foundry", behavior: "LEVEL MANIPULATOR" },
  { type: "ENTROPY_CORE" as const, name: "Entropy Core", zone: "Deep Space", behavior: "SYSTEM DRAIN" },
];

const ROCKETS = [
  { name: "Explorer", trait: "Sensor Array", unlock: 0 },
  { name: "Striker", trait: "Target Lock", unlock: 2000 },
  { name: "Heavy", trait: "Kinetic Mass", unlock: 5000 },
  { name: "Prototype", trait: "Overclocked Core", unlock: 10000 },
  { name: "Stealth", trait: "Cloaking Field", unlock: 12000 },
  { name: "Reflector", trait: "Reactive Armor", unlock: 15000 },
];

export default function FinaleArchive({ progress }: { progress: number }) {
  const visible = progress > 0.92;
  const opacity = progress > 0.95 ? 1 : (progress - 0.92) / 0.03;

  return (
    <motion.div
      className="fixed inset-0 z-40 bg-black/95 backdrop-blur-md overflow-y-auto"
      initial={{ opacity: 0 }}
      animate={{ opacity }}
      transition={{ duration: 0.5 }}
      style={{ pointerEvents: visible ? "auto" : "none" }}
    >
      <div className="max-w-6xl mx-auto px-6 py-16">
        {/* Header */}
        <div className="text-center mb-16">
          <p className="text-xs uppercase tracking-[0.4em] text-cyan-400 font-bold mb-4">
            Mission Archive
          </p>
          <h2 className="text-4xl font-black text-white tracking-wider mb-4">
            COMPLETE ECOSYSTEM
          </h2>
          <p className="text-sm text-slate-400 max-w-2xl mx-auto">
            All discovered entities, threats, and platforms encountered during ascent.
            Jump Droid contains multiple tiers of challenges across 8 zones.
          </p>
        </div>

        {/* Rocket classes */}
        <section className="mb-16">
          <h3 className="text-lg font-bold text-cyan-300 tracking-widest uppercase mb-6 border-b border-white/10 pb-2">
            Rocket Classes
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-6 gap-6">
            {ROCKETS.map((r) => (
              <div key={r.name} className="flex flex-col items-center p-4 rounded-xl border border-white/5 bg-white/5">
                <RocketSVG type={r.name.toUpperCase() as any} size={60} />
                <p className="mt-3 text-sm font-bold text-white">{r.name}</p>
                <p className="text-[10px] text-cyan-400/80 uppercase tracking-wider mt-1">{r.trait}</p>
                <p className="text-[10px] text-slate-500 mt-1">Unlock: {r.unlock.toLocaleString()}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Platforms */}
        <section className="mb-16">
          <h3 className="text-lg font-bold text-cyan-300 tracking-widest uppercase mb-6 border-b border-white/10 pb-2">
            Platform Types
          </h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4">
            {["NORMAL", "MOVING", "BOOST", "ICE", "BREAKABLE", "PHASE", "FUEL", "COOLING", "STABILITY", "MAGNETIC", "CONVEYOR", "MIMIC"].map((p) => (
              <div key={p} className="flex flex-col items-center p-3 rounded-lg border border-white/5 bg-white/5">
                <PlatformSVG type={p as any} width={100} height={16} />
                <p className="mt-2 text-[10px] font-bold uppercase tracking-widest text-slate-300">{p}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Environmental Hazards */}
        <section className="mb-16">
          <h3 className="text-lg font-bold text-red-400 tracking-widest uppercase mb-6 border-b border-white/10 pb-2">
            Environmental Hazards
          </h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
            {HAZARDS.map((h) => (
              <div key={h.name} className="p-3 rounded-lg border border-red-500/10 bg-red-500/5">
                <p className="text-sm font-bold text-red-300">{h.name}</p>
                <p className="text-[10px] text-red-400/60 mt-1">Zone: {h.zone}</p>
                <p className="text-[10px] text-red-400/60">Tier: {h.tier}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Enemies */}
        <section className="mb-16">
          <h3 className="text-lg font-bold text-yellow-400 tracking-widest uppercase mb-6 border-b border-white/10 pb-2">
            Enemy Entities
          </h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
            {ENEMIES.map((e) => (
              <div key={e.name} className="flex flex-col items-center p-4 rounded-xl border border-yellow-500/10 bg-yellow-500/5">
                <ThreatSVG type={e.name.replace(/\s+/g, "_").toUpperCase() as any} size={50} />
                <p className="mt-2 text-sm font-bold text-yellow-200">{e.name}</p>
                <p className="text-[10px] text-yellow-400/60 mt-1">Zone: {e.zone}</p>
                <p className="text-[10px] text-yellow-400/60">Tier: {e.tier}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Bosses */}
        <section className="mb-16">
          <h3 className="text-lg font-bold text-red-500 tracking-widest uppercase mb-6 border-b border-white/10 pb-2">
            Boss Encounters
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {BOSSES.map((b) => (
              <div key={b.name} className="flex flex-col items-center p-6 rounded-xl border border-red-500/20 bg-red-500/5">
                <ThreatSVG type={b.type} size={100} />
                <p className="mt-4 text-lg font-black text-red-300 tracking-wider">{b.name}</p>
                <p className="text-xs text-red-400/80 tracking-widest uppercase mt-1">{b.behavior}</p>
                <p className="text-[10px] text-red-400/60 mt-2">Zone: {b.zone}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Footer */}
        <div className="text-center mt-16 pt-8 border-t border-white/10">
          <p className="text-xs text-slate-500 tracking-widest uppercase">
            Jump Droid — The Signal From the Void
          </p>
        </div>
      </div>
    </motion.div>
  );
}