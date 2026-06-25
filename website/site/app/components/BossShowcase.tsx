"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import ThreatSVG from "./game/ThreatSVG";

const ALL_THREATS = [
  {
    type: "COMMAND_CRUISER" as const,
    name: "Command Cruiser",
    descriptor: "Platform Jammer",
    zone: "Orbit",
    ability: "Compromises landing zones by turning them red, rendering them toxic to touch.",
    tier: "Mini-Boss",
    category: "Mini-Bosses",
  },
  {
    type: "THE_GATEKEEPER" as const,
    name: "The Gatekeeper",
    descriptor: "Shield Sentinel",
    zone: "Upper Atmosphere",
    ability: "Ancient orbital defense grid deploying a rotating shield. Find the safe zones to bypass.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "STAR_EATER" as const,
    name: "Star-Eater",
    descriptor: "Light Devourer",
    zone: "Orbit",
    ability: "Cosmic construct that expands and contracts, absorbing all local energy and illumination.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "VOID_ENGINE" as const,
    name: "Void Engine",
    descriptor: "Reality Warper",
    zone: "The Void",
    ability: "Alters gravity. Distorts the coordinate system, resulting in directional control shifts.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "THE_LEVIATHAN" as const,
    name: "The Leviathan",
    descriptor: "Apex Hunter",
    zone: "Deep Space",
    ability: "Gigantic void predator that generates fast slipstreams, dragging the rocket off course.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "THE_SIGNAL" as const,
    name: "The Signal",
    descriptor: "Void Serpent",
    zone: "The Void",
    ability: "The final intelligence. Projects false telemetry readings and navigational decoy fields.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "THE_ARCHITECT" as const,
    name: "The Architect",
    descriptor: "Level Manipulator",
    zone: "The Foundry",
    ability: "Controls the automated assembly line, rhythmically adding and removing platform layouts.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "ENTROPY_CORE" as const,
    name: "Entropy Core",
    descriptor: "System drainer",
    zone: "Deep Space",
    ability: "Draws energy from surrounding fields. Spawns 4 pylons; drains fuel/shields until destroyed.",
    tier: "Boss",
    category: "Bosses",
  },
  {
    type: "THERMAL_HIVE" as const,
    name: "Thermal Hive",
    descriptor: "Heat Summoner",
    zone: "Upper Atmosphere",
    ability: "Unleashes swarms of bots, but triggers attacks only when the player's engine heat exceeds 70%.",
    tier: "Mini-Boss",
    category: "Mini-Bosses",
  },
  {
    type: "GRAVITY_ANCHOR" as const,
    name: "Gravity Anchor",
    descriptor: "Velocity Decelerator",
    zone: "Deep Space",
    ability: "Generates massive gravitational drag, increasing downward forces every 10 seconds.",
    tier: "Mini-Boss",
    category: "Mini-Bosses",
  },
  {
    type: "THE_FORGER" as const,
    name: "The Forger",
    descriptor: "Platform Converter",
    zone: "Orbit",
    ability: "Manipulates orbit structure, converting normal platforms into slippery ice or breakable fragments.",
    tier: "Mini-Boss",
    category: "Mini-Bosses",
  },
  {
    type: "HEAT_BAT" as const,
    name: "Heat Bat",
    descriptor: "Thermal Ambusher",
    zone: "Cloud Layer",
    ability: "A volatile flying organism that dives rapidly at the rocket when engine heat thresholds spike.",
    tier: "Enemy",
    category: "Enemies",
  },
  {
    type: "MIMIC_PLATFORM" as const,
    name: "Mimic Platform",
    descriptor: "Tactical Deceiver",
    zone: "Cloud Layer",
    ability: "Disguises itself as a normal landing zone, but shatters on impact, dealing hull integrity damage.",
    tier: "Enemy",
    category: "Enemies",
  },
  {
    type: "VOID_HARVESTER" as const,
    name: "Void Harvester",
    descriptor: "Resource Thief",
    zone: "Orbit",
    ability: "A mechanical scavenger that actively locks onto spawned power-ups, racing you to consume them.",
    tier: "Enemy",
    category: "Enemies",
  },
  {
    type: "PHASE_WRAITH" as const,
    name: "Phase Wraith",
    descriptor: "Overheat Spectre",
    zone: "The Void",
    ability: "Ghostly entity immune to all scanners except when the player rocket is in an overheated state.",
    tier: "Enemy",
    category: "Enemies",
  },
  {
    type: "GRAVITY_RAM" as const,
    name: "Gravity Ram",
    descriptor: "Aggressive Rammer",
    zone: "Deep Space",
    ability: "Telegraphs a vector path with a red indicator line, followed by a high-velocity dash strike.",
    tier: "Enemy",
    category: "Enemies",
  },
  {
    type: "CRYO_MIST" as const,
    name: "Cryo-Mist",
    descriptor: "Thermal Freezer",
    zone: "Cloud Layer",
    ability: "Freezes the engine thermal core. While inside, heat buildup locks in place—neither rising nor cooling.",
    tier: "Hazard",
    category: "Hazards",
  },
  {
    type: "MIRROR_SHARDS" as const,
    name: "Mirror Shards",
    descriptor: "Control Inverter",
    zone: "Deep Space",
    ability: "Reflective crystals that warp coordinate space. Inverts horizontal navigation inputs while inside.",
    tier: "Hazard",
    category: "Hazards",
  },
  {
    type: "GRAVITY_SHEAR" as const,
    name: "Gravity Shear",
    descriptor: "Shearing Currents",
    zone: "Deep Space",
    ability: "A dimensional border where forces split: top layers push upward, while bottom layers pull downward.",
    tier: "Hazard",
    category: "Hazards",
  },
];

export default function BossShowcase() {
  const [filter, setFilter] = useState<"All" | "Bosses" | "Mini-Bosses" | "Enemies" | "Hazards">("All");

  const filteredThreats = ALL_THREATS.filter(
    (t) => filter === "All" || t.category === filter
  );

  return (
    <section id="bosses" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(213,0,249,0.12),transparent_30%)]" />
      <div className="relative mx-auto max-w-6xl px-4 sm:px-8 lg:px-12">
        
        <div className="mb-8 flex flex-col md:flex-row md:items-end md:justify-between gap-6">
          <div className="space-y-4">
            <p className="text-sm uppercase tracking-[0.35em] text-purple-400 font-extrabold bg-purple-400/10 px-3 py-1 rounded-full border border-purple-400/20 inline-block">
              Threat Intelligence
            </p>
            <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
              Tactical Briefings
            </h2>
            <p className="max-w-xl text-slate-300 text-sm leading-relaxed">
              Analyze defensive parameters and mechanical profiles of high-altitude entities across all zones.
            </p>
          </div>
        </div>

        {/* Category Filters */}
        <div className="flex flex-wrap gap-2 mb-8 bg-slate-950/40 p-2.5 rounded-2xl border border-purple-400/10 max-w-xl">
          {(["All", "Bosses", "Mini-Bosses", "Enemies", "Hazards"] as const).map((cat) => (
            <button
              key={cat}
              onClick={() => setFilter(cat)}
              className={`px-4 py-1.5 rounded-xl text-xs font-bold uppercase tracking-wider transition ${
                filter === cat
                  ? "bg-purple-500/20 text-purple-300 border border-purple-500/40"
                  : "text-slate-400 border border-transparent hover:text-slate-200"
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Threats Grid */}
        <AnimatePresence mode="wait">
          <motion.div
            key={filter}
            className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.3 }}
          >
            {filteredThreats.map((boss, i) => (
              <motion.article
                key={boss.name}
                className="group rounded-3xl border bg-slate-950/60 p-6 backdrop-blur-md transition flex flex-col justify-between border-purple-400/15 hover:border-purple-400/40 hover:bg-slate-900/60"
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: i * 0.03 }}
              >
                <div className="space-y-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <span className="text-[9px] font-black uppercase tracking-wider text-purple-300/80">
                        {boss.tier}
                      </span>
                      <h3 className="text-lg font-bold text-white tracking-wide mt-0.5">{boss.name}</h3>
                    </div>
                    <span className="text-[9px] font-mono text-slate-400 border border-slate-800 bg-black/40 px-2 py-0.5 rounded">
                      {boss.zone}
                    </span>
                  </div>
                  
                  <div className="h-28 flex items-center justify-center bg-black/40 rounded-2xl border border-white/5 group-hover:border-purple-400/10 transition-all">
                    <ThreatSVG type={boss.type} size={70} />
                  </div>

                  <div className="space-y-1">
                    <p className="text-[10px] uppercase tracking-widest font-bold text-purple-300">
                      {boss.descriptor}
                    </p>
                    <p className="text-xs text-slate-300 leading-relaxed">
                      {boss.ability}
                    </p>
                  </div>
                </div>
              </motion.article>
            ))}
          </motion.div>
        </AnimatePresence>
      </div>
    </section>
  );
}
