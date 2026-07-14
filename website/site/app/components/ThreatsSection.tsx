"use client";

import { useState } from "react";
import ThreatSVG from "./game/ThreatSVG";

const ALL_THREATS = [
  { type: "COMMAND_CRUISER" as const, name: "Command Cruiser", descriptor: "Platform Jammer", zone: "Orbit", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Compromises landing zones by turning them red and toxic." },
  { type: "THE_GATEKEEPER" as const, name: "The Gatekeeper", descriptor: "Shield Sentinel", zone: "Upper Atmosphere", tier: "Boss", category: "Bosses", ability: "Deploys a rotating shield. Find the safe zones to bypass." },
  { type: "STAR_EATER" as const, name: "Star-Eater", descriptor: "Light Devourer", zone: "Orbit", tier: "Boss", category: "Bosses", ability: "Expands and contracts, absorbing all local energy." },
  { type: "VOID_ENGINE" as const, name: "Void Engine", descriptor: "Reality Warper", zone: "The Void", tier: "Boss", category: "Bosses", ability: "Alters gravity. Distorts directional control." },
  { type: "THE_LEVIATHAN" as const, name: "The Leviathan", descriptor: "Apex Hunter", zone: "Deep Space", tier: "Boss", category: "Bosses", ability: "Generates slipstreams, dragging the rocket off course." },
  { type: "THE_SIGNAL" as const, name: "The Signal", descriptor: "Void Serpent", zone: "The Void", tier: "Boss", category: "Bosses", ability: "Projects false telemetry and navigational decoys." },
  { type: "THE_ARCHITECT" as const, name: "The Architect", descriptor: "Level Manipulator", zone: "The Foundry", tier: "Boss", category: "Bosses", ability: "Rhythmically adds and removes platform layouts." },
  { type: "ENTROPY_CORE" as const, name: "Entropy Core", descriptor: "System Drainer", zone: "Deep Space", tier: "Boss", category: "Bosses", ability: "Spawns pylons that drain fuel and shields." },
  { type: "THERMAL_HIVE" as const, name: "Thermal Hive", descriptor: "Heat Summoner", zone: "Upper Atmosphere", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Unleashes swarms when engine heat exceeds 70%." },
  { type: "GRAVITY_ANCHOR" as const, name: "Gravity Anchor", descriptor: "Velocity Decelerator", zone: "Deep Space", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Increases downward forces every 10 seconds." },
  { type: "THE_FORGER" as const, name: "The Forger", descriptor: "Platform Converter", zone: "Orbit", tier: "Mini-Boss", category: "Mini-Bosses", ability: "Converts normal platforms into ice or breakable." },
  { type: "HEAT_BAT" as const, name: "Heat Bat", descriptor: "Thermal Ambusher", zone: "Cloud Layer", tier: "Enemy", category: "Enemies", ability: "Dives at the rocket when engine heat spikes." },
  { type: "MIMIC_PLATFORM" as const, name: "Mimic Platform", descriptor: "Tactical Deceiver", zone: "Cloud Layer", tier: "Enemy", category: "Enemies", ability: "Disguises as a landing zone, shatters on impact." },
  { type: "VOID_HARVESTER" as const, name: "Void Harvester", descriptor: "Resource Thief", zone: "Orbit", tier: "Enemy", category: "Enemies", ability: "Locks onto power-ups, racing you to consume them." },
  { type: "PHASE_WRAITH" as const, name: "Phase Wraith", descriptor: "Overheat Spectre", zone: "The Void", tier: "Enemy", category: "Enemies", ability: "Only visible when the rocket is overheated." },
  { type: "GRAVITY_RAM" as const, name: "Gravity Ram", descriptor: "Aggressive Rammer", zone: "Deep Space", tier: "Enemy", category: "Enemies", ability: "Telegraphs a path then dashes at high velocity." },
  { type: "CRYO_MIST" as const, name: "Cryo-Mist", descriptor: "Thermal Freezer", zone: "Cloud Layer", tier: "Hazard", category: "Hazards", ability: "Locks heat buildup in place while inside." },
  { type: "MIRROR_SHARDS" as const, name: "Mirror Shards", descriptor: "Control Inverter", zone: "Deep Space", tier: "Hazard", category: "Hazards", ability: "Inverts horizontal navigation inputs." },
  { type: "GRAVITY_SHEAR" as const, name: "Gravity Shear", descriptor: "Shearing Currents", zone: "Deep Space", tier: "Hazard", category: "Hazards", ability: "Top pushes upward, bottom pulls downward." },
];

const FILTERS = ["All", "Bosses", "Mini-Bosses", "Enemies", "Hazards"] as const;

export default function ThreatsSection() {
  const [filter, setFilter] = useState("All");

  const filtered = ALL_THREATS.filter((t) => filter === "All" || t.category === filter);

  return (
    <section id="threats" className="px-6 py-20">
      <div className="mx-auto max-w-6xl">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.35em] text-purple-400">
          Threats
        </p>
        <h2 className="mb-4 text-3xl font-black tracking-tight text-white md:text-4xl">
          Tactical Briefings
        </h2>

        <div className="flex flex-wrap gap-2 mb-6">
          {FILTERS.map((cat) => (
            <button
              key={cat}
              onClick={() => setFilter(cat)}
              className={`px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition ${
                filter === cat
                  ? "bg-purple-500/20 text-purple-300 border border-purple-500/30"
                  : "text-slate-500 border border-transparent hover:text-slate-300"
              }`}
            >
              {cat === "Mini-Bosses" ? "Mini" : cat}
            </button>
          ))}
        </div>

        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((t) => (
            <div
              key={t.type}
              className="rounded-xl border border-purple-400/15 bg-white/[0.03] p-4 flex flex-col gap-2"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-black uppercase tracking-wider text-purple-300/70">
                  {t.tier}
                </span>
                <span className="text-xs font-mono text-slate-600 border border-slate-800 bg-black/40 px-2 py-0.5 rounded">
                  {t.zone}
                </span>
              </div>
              <div className="flex items-center justify-center py-3">
                <ThreatSVG type={t.type} size={64} />
              </div>
              <p className="text-base font-bold text-white">{t.name}</p>
              <p className="text-xs text-slate-400 leading-relaxed">{t.ability}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
