"use client";

import { useState } from "react";

const CATEGORIES = [
  { category: "Platforms", count: "12 Types", description: "12 systems including conveyors and mimic traps." },
  { category: "Threats", count: "31 Targets", description: "31 cataloged threats across 8 operational zones." },
  { category: "Spacecraft", count: "6 Classes", description: "6 spacecraft configurations for deep ascent." },
  { category: "Powerups", count: "8 Modules", description: "Turbo Boosters, Heat Sinks, Hull Repairs." },
  { category: "Lore Logs", count: "6 Databases", description: "Files on the Ascension Program and the Signal." },
  { category: "Artifacts", count: "4 Items", description: "Rare relics hidden across altitudes." },
  { category: "Mechanics", count: "3 Systems", description: "Engine Heat, Overheat, and Combo dynamics." },
  { category: "Sectors", count: "8 Zones", description: "From Earth to the reality-warping Void." },
];

const LORE = [
  { title: "The Ascension Program", date: "EXP-01", zone: "Orbit", excerpt: "Objective: Probe coordinates 15,000m to isolate the electromagnetic beacon." },
  { title: "The First Signal", date: "SIG-01", zone: "The Void", excerpt: "Audio sequence near the Void boundary. Does not match standard communication." },
  { title: "The Lost Fleet", date: "FLT-04", zone: "Deep Space", excerpt: "Scattered hulls at 8,000m. Navigation systems were overridden." },
  { title: "The Ascension Logs", date: "LOG-99", zone: "Earth", excerpt: "Tectonic resonance harmonics match the Signal." },
  { title: "Project Origins", date: "EXP-05", zone: "Launchpad", excerpt: "Droid Explorers were originally deep mantle drilling chassis." },
  { title: "Ghost Transmissions", date: "EXP-06", zone: "The Void", excerpt: "TURN BACK. THE GATE IS NOT A DOOR. IT IS A SEAL." },
];

export default function ArchiveSection() {
  const [tab, setTab] = useState<"database" | "lore">("database");

  return (
    <section id="archive" className="px-6 py-20">
      <div className="mx-auto max-w-6xl">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.35em] text-cyan-300">
          Archive
        </p>
        <h2 className="mb-4 text-3xl font-black tracking-tight text-white md:text-4xl">
          Expedition Data Vault
        </h2>

        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setTab("database")}
            className={`px-5 py-2 rounded-full text-xs font-bold uppercase tracking-wider transition ${
              tab === "database"
                ? "bg-cyan-400 text-slate-950"
                : "text-slate-500 border border-white/10 hover:text-slate-300"
            }`}
          >
            Database
          </button>
          <button
            onClick={() => setTab("lore")}
            className={`px-5 py-2 rounded-full text-xs font-bold uppercase tracking-wider transition ${
              tab === "lore"
                ? "bg-purple-500 text-white"
                : "text-slate-500 border border-white/10 hover:text-slate-300"
            }`}
          >
            Lore
          </button>
        </div>

        {tab === "database" ? (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {CATEGORIES.map((c) => (
              <div key={c.category} className="rounded-xl border border-white/10 bg-white/[0.03] p-5 flex flex-col gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold uppercase tracking-wider text-cyan-300/85">{c.category}</span>
                </div>
                <p className="text-2xl font-black text-white">{c.count}</p>
                <p className="text-xs text-slate-400 leading-relaxed">{c.description}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {LORE.map((l) => (
              <div key={l.title} className="rounded-xl border border-purple-500/10 bg-white/[0.03] p-5 flex flex-col gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-mono uppercase text-purple-300">{l.date}</span>
                  <span className="text-xs font-mono text-slate-600 border border-slate-800 bg-black/40 px-2 py-0.5 rounded">{l.zone}</span>
                </div>
                <p className="text-base font-bold text-white">{l.title}</p>
                <p className="text-xs text-slate-400 leading-relaxed">{l.excerpt}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
