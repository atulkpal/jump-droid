"use client";

import { useState } from "react";
import Carousel from "./Carousel";
import ThreatSVG from "./game/ThreatSVG";

const ALL_THREATS = [
  { type: "COMMAND_CRUISER" as const, name: "Command Cruiser", descriptor: "Platform Jammer", zone: "Orbit", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Compromises landing zones by turning them red, rendering them toxic to touch." },
  { type: "THE_GATEKEEPER" as const, name: "The Gatekeeper", descriptor: "Shield Sentinel", zone: "Upper Atmosphere", tier: "Boss", category: "Bosses", ability: "Ancient orbital defense grid deploying a rotating shield. Find the safe zones to bypass." },
  { type: "STAR_EATER" as const, name: "Star-Eater", descriptor: "Light Devourer", zone: "Orbit", tier: "Boss", category: "Bosses", ability: "Cosmic construct that expands and contracts, absorbing all local energy and illumination." },
  { type: "VOID_ENGINE" as const, name: "Void Engine", descriptor: "Reality Warper", zone: "The Void", tier: "Boss", category: "Bosses", ability: "Alters gravity. Distorts the coordinate system, resulting in directional control shifts." },
  { type: "THE_LEVIATHAN" as const, name: "The Leviathan", descriptor: "Apex Hunter", zone: "Deep Space", tier: "Boss", category: "Bosses", ability: "Gigantic void predator that generates fast slipstreams, dragging the rocket off course." },
  { type: "THE_SIGNAL" as const, name: "The Signal", descriptor: "Void Serpent", zone: "The Void", tier: "Boss", category: "Bosses", ability: "The final intelligence. Projects false telemetry readings and navigational decoy fields." },
  { type: "THE_ARCHITECT" as const, name: "The Architect", descriptor: "Level Manipulator", zone: "The Foundry", tier: "Boss", category: "Bosses", ability: "Controls the automated assembly line, rhythmically adding and removing platform layouts." },
  { type: "ENTROPY_CORE" as const, name: "Entropy Core", descriptor: "System drainer", zone: "Deep Space", tier: "Boss", category: "Bosses", ability: "Draws energy from surrounding fields. Spawns 4 pylons; drains fuel/shields until destroyed." },
  { type: "THERMAL_HIVE" as const, name: "Thermal Hive", descriptor: "Heat Summoner", zone: "Upper Atmosphere", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Unleashes swarms of bots, but triggers attacks only when engine heat exceeds 70%." },
  { type: "GRAVITY_ANCHOR" as const, name: "Gravity Anchor", descriptor: "Velocity Decelerator", zone: "Deep Space", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Generates massive gravitational drag, increasing downward forces every 10 seconds." },
  { type: "THE_FORGER" as const, name: "The Forger", descriptor: "Platform Converter", zone: "Orbit", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Converts normal platforms into slippery ice or breakable fragments." },
  { type: "HEAT_BAT" as const, name: "Heat Bat", descriptor: "Thermal Ambusher", zone: "Cloud Layer", tier: "Enemy", category: "Enemies", ability: "A volatile flying organism that dives rapidly at the rocket when engine heat thresholds spike." },
  { type: "MIMIC_PLATFORM" as const, name: "Mimic Platform", descriptor: "Tactical Deceiver", zone: "Cloud Layer", tier: "Enemy", category: "Enemies", ability: "Disguises itself as a normal landing zone, but shatters on impact, dealing hull damage." },
  { type: "VOID_HARVESTER" as const, name: "Void Harvester", descriptor: "Resource Thief", zone: "Orbit", tier: "Enemy", category: "Enemies", ability: "Locks onto spawned power-ups, racing you to consume them." },
  { type: "PHASE_WRAITH" as const, name: "Phase Wraith", descriptor: "Overheat Spectre", zone: "The Void", tier: "Enemy", category: "Enemies", ability: "Ghostly entity immune to scanners except when the rocket is overheated." },
  { type: "GRAVITY_RAM" as const, name: "Gravity Ram", descriptor: "Aggressive Rammer", zone: "Deep Space", tier: "Enemy", category: "Enemies", ability: "Telegraphs a vector path with a red indicator line, followed by a high-velocity dash strike." },
  { type: "CRYO_MIST" as const, name: "Cryo-Mist", descriptor: "Thermal Freezer", zone: "Cloud Layer", tier: "Hazard", category: "Hazards", ability: "Freezes the engine thermal core. While inside, heat buildup locks in place." },
  { type: "MIRROR_SHARDS" as const, name: "Mirror Shards", descriptor: "Control Inverter", zone: "Deep Space", tier: "Hazard", category: "Hazards", ability: "Reflective crystals that warp coordinate space. Inverts horizontal navigation inputs." },
  { type: "GRAVITY_SHEAR" as const, name: "Gravity Shear", descriptor: "Shearing Currents", zone: "Deep Space", tier: "Hazard", category: "Hazards", ability: "A dimensional border where forces split: top pushes upward, bottom pulls downward." },
];

const FILTERS = ["All", "Bosses", "Mini-Bosses", "Enemies", "Hazards"] as const;

export default function ThreatsSection() {
  const [filter, setFilter] = useState<string>("All");

  const filtered = ALL_THREATS.filter(
    (t) => filter === "All" || t.category === filter
  );

  return (
    <section id="threats" className="relative min-h-dvh flex flex-col justify-center overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(213,0,249,0.06),transparent_60%)]" />

      <div className="flex flex-col h-full">
        <div className="px-6 pt-6 flex items-center justify-between gap-4">
          <p className="text-sm font-bold uppercase tracking-[0.35em] text-purple-300">
            Threats
          </p>
          <div className="flex gap-1.5 bg-slate-950/60 p-1 rounded-xl border border-purple-400/10">
            {FILTERS.map((cat) => (
              <button
                key={cat}
                onClick={() => setFilter(cat)}
                className={`px-3 py-1.5 rounded-lg text-[11px] font-bold uppercase tracking-wider transition min-h-[36px] ${
                  filter === cat
                    ? "bg-purple-500/20 text-purple-300 border border-purple-500/30"
                    : "text-slate-500 hover:text-slate-300 border border-transparent"
                }`}
              >
                {cat === "Mini-Bosses" ? "Mini" : cat}
              </button>
            ))}
          </div>
        </div>

        <Carousel
          items={filtered.map((t) => (
            <div
              key={t.type}
              className="rounded-2xl border border-purple-400/15 bg-white/[0.03] p-6 flex flex-col items-center gap-3 h-full md:p-8"
            >
              <span className="text-[11px] font-black uppercase tracking-wider text-purple-300/70">
                {t.tier}
              </span>
              <div className="flex items-center justify-center w-full py-4">
                <ThreatSVG type={t.type} size={80} />
              </div>
              <p className="text-lg font-bold text-white text-center">{t.name}</p>
              <p className="text-sm text-slate-400 text-center leading-relaxed">{t.ability}</p>
            </div>
          ))}
        />
      </div>
    </section>
  );
}
