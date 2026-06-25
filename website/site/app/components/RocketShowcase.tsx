"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import RocketSVG from "./game/RocketSVG";

const ROCKETS = [
  // Active Hulls
  {
    type: "BALANCED" as const,
    name: "Explorer",
    unlock: "Start Here",
    traitName: "Sensor Array",
    traitDesc: "Native +20% discovery range, revealing secrets earlier.",
    desc: "The standard model. Well-balanced fuel efficiency, thrust performance, and heat dissipation. Ideal for cataloging the lower atmospheres.",
    stats: {
      thrust: "1.0x",
      fuel: "1.0x",
      heat: "1.0x",
    },
    status: "active",
  },
  {
    type: "SCOUT" as const,
    name: "Striker",
    unlock: "2,000 meters",
    traitName: "Target Lock",
    traitDesc: "Enables precision target lock on enemy weak points.",
    desc: "A lightweight chassis built for rapid ascents. Sacrifices fuel capacity for high-thrust maneuverability and efficient engine cooling.",
    stats: {
      thrust: "1.25x",
      fuel: "0.7x",
      heat: "0.9x",
    },
    status: "active",
  },
  {
    type: "TANK" as const,
    name: "Heavy",
    unlock: "5,000 meters",
    traitName: "Kinetic Mass",
    traitDesc: "Triggers shockwaves on weak point destruction, clearing local entities.",
    desc: "Reinforced titanium mainframe housing massive fuel tanks. Heavy and slow, but boasts high stability, massive fuel reserves, and excellent heat containment.",
    stats: {
      thrust: "0.85x",
      fuel: "1.5x",
      heat: "0.8x",
    },
    status: "active",
  },
  {
    type: "EXPERIMENTAL" as const,
    name: "Prototype",
    unlock: "10,000 meters",
    traitName: "Overclocked Core",
    traitDesc: "Retain complete steering authority even while in overheated state.",
    desc: "A highly volatile test rig integrating experimental dark energy thrusters. High risk, high speed, and capable of extreme acceleration.",
    stats: {
      thrust: "1.5x",
      fuel: "1.0x",
      heat: "1.4x",
    },
    status: "active",
  },
  {
    type: "STEALTH" as const,
    name: "Stealth",
    unlock: "12,000 meters",
    traitName: "Cloaking Field",
    traitDesc: "Reduces enemy detection range by 40%, allowing you to climb past hostile entities undetected.",
    desc: "An angular, radar-absorbent fuselage constructed from experimental dark-matter composites. Designed specifically for evading dense patrol regions.",
    stats: {
      thrust: "1.0x",
      fuel: "0.8x",
      heat: "1.0x",
    },
    status: "active",
  },
  {
    type: "REFLECTOR" as const,
    name: "Reflector",
    unlock: "15,000 meters",
    traitName: "Reactive Armor",
    traitDesc: "Deals massive damage and high-velocity knockback to hostile entities upon physical collision.",
    desc: "A reinforced brawler hull layered with spiked impact-conversion plating. Converts landing failures or enemy strikes into kinetic shockwaves.",
    stats: {
      thrust: "1.1x",
      fuel: "1.0x",
      heat: "1.2x",
    },
    status: "active",
  },
];

export default function RocketShowcase() {
  const [selectedType, setSelectedType] = useState<"BALANCED" | "SCOUT" | "TANK" | "EXPERIMENTAL" | "STEALTH" | "REFLECTOR">("BALANCED");
  const [thrusting, setThrusting] = useState(true);
  const [shield, setShield] = useState(50);
  const [heat, setHeat] = useState(20);

  const currentSelectedType = selectedType;
  const current = ROCKETS.find((r) => r.type === currentSelectedType)!;

  return (
    <section id="hangar" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom,_rgba(0,229,255,0.14),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-4 sm:px-8 lg:px-12">
        
        <div className="mb-8 flex flex-col md:flex-row md:items-end md:justify-between gap-6">
          <div className="space-y-4">
            <p className="text-sm uppercase tracking-[0.35em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 inline-block">
              The Hangar
            </p>
            <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
              Spaceship Evolution
            </h2>
            <p className="max-w-xl text-slate-300 text-sm leading-relaxed">
              Expedition spacecraft designed for the vertical climb. Choose a vessel type to preview configuration and telemetry.
            </p>
          </div>
        </div>

        <div className="grid gap-8 lg:grid-cols-12">
          {/* Class Selectors (Left Columns) */}
          <div className="lg:col-span-5 w-full">
            <div className="flex flex-row lg:flex-col gap-4 overflow-x-auto lg:overflow-x-visible pb-4 lg:pb-0 [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] snap-x snap-mandatory w-full">
              {ROCKETS.map((rocket) => {
                const isSelected = currentSelectedType === rocket.type;
                const isAdvanced = rocket.type === "STEALTH" || rocket.type === "REFLECTOR";
                return (
                  <button
                    key={rocket.type}
                    onClick={() => setSelectedType(rocket.type)}
                    className={`text-left rounded-3xl border p-5 transition-all backdrop-blur-sm shrink-0 snap-start w-[265px] sm:w-[320px] lg:w-full ${
                      isSelected
                        ? isAdvanced
                          ? "border-purple-500 bg-purple-500/10 shadow-[0_0_20px_rgba(213,0,249,0.15)]"
                          : "border-cyan-400 bg-cyan-400/10 shadow-[0_0_20px_rgba(0,229,255,0.15)]"
                        : "border-cyan-300/15 bg-slate-950/60 hover:border-cyan-400/30 hover:bg-slate-900/60"
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <div>
                        <span className={`text-[10px] font-black uppercase tracking-wider ${
                          isAdvanced ? "text-purple-400" : "text-cyan-300/80"
                        }`}>
                          {rocket.unlock}
                        </span>
                        <h3 className="text-xl font-bold text-white tracking-wide mt-1">{rocket.name}</h3>
                      </div>
                      {isSelected && (
                        <span className={`text-[10px] uppercase tracking-widest font-black px-2 py-0.5 rounded ${
                          isAdvanced ? "bg-purple-500 text-white" : "bg-cyan-400 text-slate-950"
                        }`}>
                          Selected
                        </span>
                      )}
                    </div>
                    <p className="mt-3 text-xs text-slate-300 leading-relaxed truncate">{rocket.desc}</p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Interactive Hangar Simulator (Right Columns) */}
          <div className="lg:col-span-7 rounded-3xl border border-cyan-300/15 bg-slate-950/40 p-8 backdrop-blur-md flex flex-col md:flex-row gap-8 items-center md:items-stretch">
            
            {/* Live Model View */}
            <div className="flex-1 flex flex-col items-center justify-center bg-black/40 rounded-2xl border border-white/5 p-6 relative w-full min-h-[280px]">
              <div className="absolute top-4 left-4 text-[10px] font-mono uppercase tracking-widest text-slate-500">
                Live Preview
              </div>
              <div className="relative">
                <RocketSVG
                  type={currentSelectedType}
                  size={160}
                  thrusting={thrusting}
                  shield={shield}
                  heat={heat}
                  showFlame={thrusting}
                />
              </div>
            </div>

            {/* Simulated Specs & Sliders */}
            <div className="flex-1 flex flex-col justify-between w-full space-y-6">
              <div>
                <span className="text-xs uppercase tracking-widest text-cyan-300 font-bold">
                  Class Statistics
                </span>
                <div className="grid grid-cols-3 gap-3 mt-3">
                  {[
                    { label: "Thrust", val: current.stats.thrust },
                    { label: "Fuel Capacity", val: current.stats.fuel },
                    { label: "Heat Limit", val: current.stats.heat },
                  ].map((stat) => (
                    <div key={stat.label} className="bg-black/60 rounded-2xl p-3 border border-white/5 text-center">
                      <p className="text-[9px] uppercase tracking-wider text-slate-400 font-medium">{stat.label}</p>
                      <p className="text-sm font-black text-white mt-1">{stat.val}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* Hangar Control Telemetry */}
              <div className="space-y-4">
                <span className="text-xs uppercase tracking-widest text-cyan-300 font-bold block">
                  Simulated Telemetry
                </span>

                {/* Flame / Thrust Toggle */}
                <div className="flex items-center justify-between">
                  <label className="text-xs text-slate-300 font-bold">Thrusters Active</label>
                  <button
                    onClick={() => setThrusting(!thrusting)}
                    className={`px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider transition ${
                      thrusting
                        ? "bg-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,229,255,0.3)]"
                        : "bg-slate-800 text-slate-400 hover:bg-slate-700"
                    }`}
                  >
                    {thrusting ? "On" : "Off"}
                  </button>
                </div>

                {/* Heat Slider */}
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-bold">
                    <span className="text-slate-300">Engine Heat ({heat}%)</span>
                    <span className={heat >= 100 ? "text-red-400" : "text-slate-400"}>
                      {heat >= 100 ? "OVERHEAT WARNING" : "Normal"}
                    </span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={heat}
                    onChange={(e) => setHeat(parseInt(e.target.value))}
                    className="w-full h-1 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-cyan-400"
                  />
                </div>

                {/* Shield Slider */}
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-bold">
                    <span className="text-slate-300">Shield Charge ({shield}%)</span>
                    <span className="text-cyan-400">{shield > 0 ? "Active" : "Depleted"}</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={shield}
                    onChange={(e) => setShield(parseInt(e.target.value))}
                    className="w-full h-1 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-cyan-400"
                  />
                </div>
              </div>

              {/* Passive Ability description */}
              <div className={`border rounded-2xl p-4 ${
                (current.type === "STEALTH" || current.type === "REFLECTOR") ? "bg-purple-500/5 border-purple-500/10" : "bg-cyan-400/5 border-cyan-400/10"
              }`}>
                <p className={`text-[10px] font-black uppercase tracking-widest ${
                  (current.type === "STEALTH" || current.type === "REFLECTOR") ? "text-purple-400" : "text-cyan-300"
                }`}>
                  Passive Ability: {current.traitName}
                </p>
                <p className="text-xs text-slate-300 mt-1 leading-relaxed">{current.traitDesc}</p>
              </div>

            </div>

          </div>
        </div>

      </div>
    </section>
  );
}
