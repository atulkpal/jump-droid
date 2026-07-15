"use client";

import { motion } from "framer-motion";
import { PLAY_STORE_URL } from "@/lib/constants";

export default function HeroSection() {
  return (
    <section id="hero" className="relative overflow-hidden py-32 sm:py-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.16),_transparent_32%)]" />
      <div className="absolute left-0 top-0 h-full w-full bg-[linear-gradient(180deg,rgba(0,0,0,0.12),transparent_40%,rgba(0,0,0,0.85))]" />
      
      <div className="relative mx-auto flex max-w-6xl flex-col gap-16 px-6 text-center text-slate-100 sm:px-8 lg:px-12">
        <motion.div 
          className="space-y-6"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
        >
          <span className="inline-block text-xs uppercase tracking-[0.45em] text-cyan-300 font-extrabold bg-cyan-400/10 px-4 py-1.5 rounded-full border border-cyan-400/20">
            The Signal From the Void
          </span>
          <h1 className="mx-auto max-w-4xl text-5xl font-black leading-[1.05] tracking-tight text-white sm:text-6xl lg:text-7xl uppercase">
            Pilot the ultimate <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-cyan-300 to-purple-400">droid explorer</span>.
          </h1>
          <p className="mx-auto max-w-3xl text-base leading-8 text-slate-300 sm:text-lg">
            Jump Droid is a free, open-source Android arcade game. Touch to thrust. Manage fuel, heat, and shield as you climb through 8 atmospheric zones. Land on platforms, build combos, face bosses, unlock new rockets, and discover the truth hidden in the void.
          </p>
        </motion.div>

        <motion.div 
          className="mx-auto flex flex-col items-center justify-center gap-4 sm:flex-row"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.2, ease: "easeOut" }}
        >
          <a
            href={PLAY_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex min-w-[200px] items-center justify-center rounded-full bg-cyan-400 px-8 py-4 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 hover:shadow-[0_0_30px_rgba(0,229,255,0.4)]"
          >
            Download Now
          </a>
          <a
            href="#hangar"
            className="inline-flex min-w-[200px] items-center justify-center rounded-full border border-cyan-300/30 bg-white/5 px-8 py-4 text-sm font-black uppercase tracking-[0.2em] text-cyan-100 transition hover:bg-white/10"
          >
            Explore Hangar
          </a>
        </motion.div>

        <motion.div 
          className="grid gap-6 rounded-3xl border border-cyan-200/10 bg-slate-900/60 p-8 text-left backdrop-blur-md shadow-[0_0_80px_rgba(0,229,255,0.08)] sm:grid-cols-3"
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.4, ease: "easeOut" }}
        >
          {[
            { label: "Exploration First", value: "A vertical odyssey driven by altitude progression and discovery rather than simple scoring." },
            { label: "Tactical Resource Loop", value: "Balance fuel consumption, thruster heat cooldown, shield recharge, and permanent hull integrity." },
            { label: "Celestial Conflict", value: "Face phase-based boss encounters with destructible weak points guarding the atmospheric boundaries." },
          ].map((item) => (
            <div key={item.label} className="space-y-2">
              <p className="text-xs uppercase tracking-[0.25em] text-cyan-300 font-bold">{item.label}</p>
              <p className="text-sm leading-relaxed text-slate-300">{item.value}</p>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  );
}
