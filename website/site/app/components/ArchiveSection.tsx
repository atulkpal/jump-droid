"use client";

import { useState } from "react";
import Carousel from "./Carousel";

const CATEGORIES = [
  { category: "Platforms", count: "12 Types", description: "12 distinct systems including horizontal conveyors and disguised mimic traps." },
  { category: "Threats", count: "31 Targets", description: "31 cataloged threats across all 8 operational zones." },
  { category: "Spacecraft", count: "6 Classes", description: "6 fully certified spacecraft configurations optimized for deep ascent." },
  { category: "Powerups", count: "8 Modules", description: "Deployable kits including Turbo Boosters, Heat Sinks, and Hull Repairs." },
  { category: "Lore logs", count: "6 Databases", description: "Decrypted files detailing the Ascension Program and sub-surface core signals." },
  { category: "Artifacts", count: "4 Items", description: "Rare expedition relics hidden across altitudes." },
  { category: "Mechanics", count: "3 Systems", description: "Telemetry tracking of Engine Heat, Overheat cycles, and Combo dynamics." },
  { category: "Sectors", count: "8 Zones", description: "8 active operational zones ranging from Earth to the Void." },
];

const LORE_LOGS = [
  { id: "ascension", title: "The Ascension Program", date: "EXP-01", zone: "Orbit", excerpt: "Joint orbital initiative authorized. Objective: Probe coordinates 15,000m to isolate the source of the electromagnetic beacon." },
  { id: "signal", title: "The First Signal", date: "SIG-01", zone: "The Void", excerpt: "Audio signal sequence localized near the Void boundary. Does not match standard communication models." },
  { id: "lost-fleet", title: "The Lost Fleet", date: "FLT-04", zone: "Deep Space", excerpt: "Scattered hulls identified at 8,000m. Signal patterns suggest automated navigational systems were overridden." },
  { id: "ascension-logs", title: "The Ascension Logs", date: "LOG-99", zone: "Earth", excerpt: "Deep-core scans reveal tectonic resonance harmonics matching the Signal." },
  { id: "origins", title: "Project Origins", date: "EXP-05", zone: "Launchpad", excerpt: "Droid Explorers were originally heavy sub-surface excavation chassis designed for deep mantle drilling." },
  { id: "ghost-trans", title: "Ghost Transmissions", date: "EXP-06", zone: "The Void", excerpt: "TURN BACK. THE GATE IS NOT A DOOR. IT IS A SEAL." },
];

export default function ArchiveSection() {
  const [tab, setTab] = useState<"database" | "lore">("database");

  return (
    <section id="archive" className="relative min-h-dvh flex flex-col justify-center overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(0,229,255,0.04),transparent_60%)]" />

      <div className="flex flex-col h-full">
        <div className="px-6 pt-6 flex items-center justify-between gap-4">
          <p className="text-sm font-bold uppercase tracking-[0.35em] text-cyan-300">
            Archive
          </p>
          <div className="flex bg-slate-950/80 p-0.5 rounded-full border border-cyan-300/15">
            <button
              onClick={() => setTab("database")}
              className={`px-5 py-2 rounded-full text-[11px] font-bold uppercase tracking-wider transition ${
                tab === "database"
                  ? "bg-cyan-400 text-slate-950"
                  : "text-slate-400 hover:text-slate-200"
              }`}
            >
              Database
            </button>
            <button
              onClick={() => setTab("lore")}
              className={`px-5 py-2 rounded-full text-[11px] font-bold uppercase tracking-wider transition ${
                tab === "lore"
                  ? "bg-purple-500 text-white"
                  : "text-slate-400 hover:text-slate-200"
              }`}
            >
              Lore
            </button>
          </div>
        </div>

        <Carousel
          items={
            tab === "database"
              ? CATEGORIES.map((c) => (
                  <div
                    key={c.category}
                    className="rounded-2xl border border-cyan-300/10 bg-white/[0.03] p-6 flex flex-col justify-center gap-3 h-full md:p-8"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold uppercase tracking-widest text-cyan-300/85">
                        {c.category}
                      </span>
                      <span className="text-[11px] font-mono text-slate-500 border border-slate-800 bg-black/40 px-2 py-0.5 rounded">
                        Data
                      </span>
                    </div>
                    <p className="text-3xl font-black text-white">{c.count}</p>
                    <p className="text-sm text-slate-400 leading-relaxed">{c.description}</p>
                  </div>
                ))
              : LORE_LOGS.map((l) => (
                  <div
                    key={l.id}
                    className="rounded-2xl border border-purple-500/10 bg-white/[0.03] p-6 flex flex-col justify-center gap-3 h-full md:p-8"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-mono uppercase tracking-wider text-purple-300">
                        {l.date}
                      </span>
                      <span className="text-[11px] font-mono text-slate-500 border border-slate-800 bg-black/40 px-2 py-0.5 rounded">
                        {l.zone}
                      </span>
                    </div>
                    <p className="text-lg font-bold text-white">{l.title}</p>
                    <p className="text-sm text-slate-400 leading-relaxed">{l.excerpt}</p>
                  </div>
                ))
          }
        />
      </div>
    </section>
  );
}
