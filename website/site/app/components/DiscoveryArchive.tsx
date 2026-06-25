"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

const CATEGORIES = [
  { category: "Platforms", count: "12 Types", description: "12 distinct systems including horizontal conveyors and disguised mimic traps." },
  { category: "Threats", count: "31 Targets", description: "31 cataloged threats across all 8 operational zones." },
  { category: "Spacecraft", count: "6 Classes", description: "6 fully certified spacecraft configurations optimized for deep ascent." },
  { category: "Powerups", count: "8 Modules", description: "Deployable kits including Turbo Boosters, Heat Sinks, and Hull Repairs." },
  { category: "Lore logs", count: "6 Databases", description: "Decrypted files detailing the Ascension Program and sub-surface core signals." },
  { category: "Artifacts", count: "4 Items", description: "Rare expedition relics (Flight Recorder, Unknown Alloy) hidden across altitudes." },
  { category: "Mechanics", count: "3 Systems", description: "Telemetry tracking of Engine Heat, Overheat cycles, and Combo dynamics." },
  { category: "Sectors", count: "8 Zones", description: "8 active operational zones ranging from Earth to the reality-warping Void." },
];

const LORE_LOGS = [
  {
    id: "ascension",
    title: "The Ascension Program",
    date: "EXP-01 telemetry",
    zone: "Orbit",
    content: "Data-Stream Decryption: Joint orbital initiative authorized. Objective: Probe coordinates 15,000m to isolate the source of the electromagnetic beacon. Multiple automated crew fleets deployed. Ground command reports zero returns from initial orbital boundary crossings.",
    status: "active",
  },
  {
    id: "signal",
    title: "The First Signal",
    date: "SIG-01 telemetry",
    zone: "The Void",
    content: "Frequency Analysis: Audio signal sequence localized near the Void boundary. Does not match standard communication models. Decompressed packets resemble high-frequency biometric rhythm. The Signal emits from coordinate center 15,800m.",
    status: "active",
  },
  {
    id: "lost-fleet",
    title: "The Lost Fleet",
    date: "FLT-04 telemetry",
    zone: "Deep Space",
    content: "Recovery Protocol: Scattered hulls identified at 8,000m. Signal patterns suggest automated navigational systems were overridden. Blackbox data corrupt, referencing a sentinel 'Gatekeeper' blocking propulsion telemetry.",
    status: "active",
  },
  {
    id: "ascension-logs",
    title: "The Ascension Logs",
    date: "LOG-99 telemetry",
    zone: "Earth",
    content: "Sensory Telemetry: Deep-core scans reveal tectonic resonance harmonics matching the Signal. Recommendation: Analyze if the transmitter is situated beneath the Earth's crust rather than deep space. Signals may be echoes.",
    status: "active",
  },
  {
    id: "origins",
    title: "Project Origins",
    date: "EXP-05 telemetry",
    zone: "Earth Launchpad",
    content: "Classified Archives: Discovery files confirm Droid Explorers were originally heavy sub-surface excavation chassis designed for deep mantle drilling. Propulsion systems were retrofitted to escape gravity, explaining high thermal capacity and heavy structural mass.",
    status: "active",
  },
  {
    id: "ghost-trans",
    title: "Ghost Transmissions",
    date: "EXP-06 telemetry",
    zone: "The Void",
    content: "Telemetry Override: Minor HUD static localized during deep ascent translates to text: 'TURN BACK. THE GATE IS NOT A DOOR. IT IS A SEAL.' Telemetry trace shows source is inside The Void. Anomaly engines are acting as locks.",
    status: "active",
  },
];

export default function DiscoveryArchive() {
  const [activeTab, setActiveTab] = useState<"database" | "lore">("database");
  const [selectedLore, setSelectedLore] = useState<string>("ascension");

  const currentLore = LORE_LOGS.find((l) => l.id === selectedLore)!;

  return (
    <section id="archive" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(0,229,255,0.06),transparent_32%)]" />
      <div className="relative mx-auto max-w-6xl px-4 sm:px-8 lg:px-12">
        
        <div className="mb-8 flex flex-col md:flex-row md:items-end md:justify-between gap-6">
          <div className="space-y-4">
            <p className="text-sm uppercase tracking-[0.35em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 inline-block">
              The Codex
            </p>
            <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
              Expedition Data Vault
            </h2>
            <p className="max-w-xl text-slate-300 text-sm leading-relaxed">
              Permanent registry logs compiled from vertical flights. Explore cataloged categories or decrypt logs detailing the mystery of the Signal.
            </p>
          </div>

          {/* Database vs Lore Tab */}
          <div className="flex bg-slate-950/80 p-1.5 rounded-full border border-cyan-300/15 max-w-xs self-start md:self-end">
            <button
              onClick={() => setActiveTab("database")}
              className={`px-6 py-2 rounded-full text-xs font-bold uppercase tracking-wider transition ${
                activeTab === "database"
                  ? "bg-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,229,255,0.3)]"
                  : "text-slate-400 hover:text-slate-200"
              }`}
            >
              Mainframe
            </button>
            <button
              onClick={() => setActiveTab("lore")}
              className={`px-6 py-2 rounded-full text-xs font-bold uppercase tracking-wider transition ${
                activeTab === "lore"
                  ? "bg-purple-500 text-white shadow-[0_0_15px_rgba(213,0,249,0.3)]"
                  : "text-slate-400 hover:text-slate-200"
              }`}
            >
              Lore Logs
            </button>
          </div>
        </div>

        <AnimatePresence mode="wait">
          {activeTab === "database" ? (
            <motion.div
              key="database-grid"
              className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4"
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -15 }}
              transition={{ duration: 0.3 }}
            >
              {CATEGORIES.map((discovery, i) => (
                <motion.article
                  key={discovery.category}
                  className="group rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-6 backdrop-blur-md transition hover:border-cyan-400/40 hover:bg-slate-900/60 flex flex-col justify-between"
                  initial={{ opacity: 0, y: 15 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.4, delay: i * 0.03 }}
                >
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-[10px] font-bold uppercase tracking-widest text-cyan-300/85">
                        {discovery.category}
                      </span>
                      <span className="text-[9px] font-mono text-slate-500 border border-slate-800 bg-black/40 px-2 py-0.5 rounded">
                        Telemetry
                      </span>
                    </div>
                    <h3 className="text-2xl font-black text-white tracking-wide">{discovery.count}</h3>
                    <p className="text-xs leading-relaxed text-slate-300">
                      {discovery.description}
                    </p>
                  </div>
                </motion.article>
              ))}
            </motion.div>
          ) : (
            <motion.div
              key="lore-logs-terminal"
              className="grid gap-8 lg:grid-cols-12 rounded-3xl border border-purple-500/10 bg-slate-950/40 p-8 backdrop-blur-md items-stretch"
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -15 }}
              transition={{ duration: 0.3 }}
            >
              {/* Left Selector (5 Columns) */}
              <div className="lg:col-span-5 flex flex-row lg:flex-col gap-3 overflow-x-auto lg:overflow-x-visible pb-4 lg:pb-0 [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] snap-x snap-mandatory w-full">
                {LORE_LOGS.map((log) => {
                  const isSelected = selectedLore === log.id;
                  const isAdvanced = log.id === "origins" || log.id === "ghost-trans";
                  return (
                    <button
                      key={log.id}
                      onClick={() => setSelectedLore(log.id)}
                      className={`text-left rounded-2xl border p-4 transition-all shrink-0 snap-start w-[240px] sm:w-[285px] lg:w-full ${
                        isSelected
                          ? isAdvanced
                            ? "border-purple-500 bg-purple-500/10 shadow-[0_0_15px_rgba(213,0,249,0.15)]"
                            : "border-cyan-400 bg-cyan-400/10 shadow-[0_0_15px_rgba(0,229,255,0.15)]"
                          : "border-purple-400/5 bg-black/40 hover:border-purple-400/20 hover:bg-slate-900/60"
                      }`}
                    >
                      <div className="flex justify-between items-center">
                        <h4 className="text-sm font-bold text-white tracking-wide">{log.title}</h4>
                        <span className={`text-[8px] font-mono uppercase tracking-wider px-1.5 py-0.5 rounded ${
                          isAdvanced ? "bg-purple-500/20 text-purple-300" : "bg-cyan-500/10 text-cyan-200"
                        }`}>
                          {isAdvanced ? "Decrypted" : "Active"}
                        </span>
                      </div>
                      <p className="text-[10px] text-slate-400 font-mono mt-1">{log.date}</p>
                    </button>
                  );
                })}
              </div>

              {/* Right Decryption Screen (7 Columns) */}
              <div className="lg:col-span-7 flex flex-col justify-between bg-black/60 rounded-2xl border border-white/5 p-8 relative min-h-[300px]">
                <div className="absolute top-4 left-4 text-[9px] font-mono uppercase tracking-widest text-slate-500">
                  Mainframe Decryption protocol
                </div>
                <div className="absolute top-4 right-4 text-[9px] font-mono text-purple-400">
                  {currentLore.zone}
                </div>

                <div className="mt-8 space-y-6">
                  <div>
                    <h3 className="text-xl font-bold text-white tracking-wide">{currentLore.title}</h3>
                    <p className="text-[10px] text-purple-300 font-mono mt-1">{currentLore.date} // Sector: {currentLore.zone}</p>
                  </div>

                  <div className="border-t border-purple-500/15 pt-4">
                    <p className="text-sm text-slate-300 leading-relaxed font-mono whitespace-pre-wrap select-all">
                      {currentLore.content}
                    </p>
                  </div>
                </div>

                <div className="mt-8 flex justify-between items-center text-[10px] font-mono text-slate-500">
                  <span>LOG ID: {currentLore.id.toUpperCase()}</span>
                  <span className="animate-pulse">DECRYPTION SUCCESSFUL_</span>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

      </div>
    </section>
  );
}
