"use client";

import { motion } from "framer-motion";

const SYSTEMS = [
  {
    title: "Ascension Ranks",
    subtitle: "5 Discovery Tiers",
    desc: "Your rank scales automatically based on your total Codex discovery count. Progress from a Novice surveyor to an elite Void Explorer as you scan new platforms, enemies, and anomalies.",
    cta: "Rank up to unlock telemetry databases.",
  },
  {
    title: "Mission Manager",
    subtitle: "15 Templates, 3 Tracks",
    desc: "Take on rotating operations across three separate tracks: Exploration (altitude goals), Platforming (consecutive landing skills), and Survival (hazard mitigation). Keep runs fresh with unique active objectives.",
    cta: "3 active missions rotating per run.",
  },
  {
    title: "Combo Economy",
    subtitle: "Active Skill Recovery",
    desc: "Gravity pulls hard, but skill rewards you. Land consecutive jumps on platforms to build your combo multiplier. Reaching a 5x landing combo instantly triggers stabilizer systems to restore your shields.",
    cta: "Land 5 platforms to restore shield.",
  },
];

export default function ProgressionSystems() {
  return (
    <section className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(255,215,0,0.06),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 inline-block">
            The Systems
          </p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
            Designed for replayability
          </h2>
          <p className="max-w-2xl text-slate-300 text-sm leading-relaxed">
            Every expedition is unique. Push your limits with overlapping systems that reward precision, survival, and exploration.
          </p>
        </div>
        <div className="grid gap-6 lg:grid-cols-3">
          {SYSTEMS.map((system, i) => (
            <motion.article
              key={system.title}
              className="rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-8 backdrop-blur-md transition hover:border-cyan-400/40 hover:bg-slate-900/60 flex flex-col justify-between"
              initial={{ opacity: 0, y: 25 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-50px" }}
              transition={{ duration: 0.5, delay: i * 0.1 }}
            >
              <div className="space-y-4">
                <div>
                  <span className="text-[10px] font-black uppercase tracking-wider text-cyan-300">
                    {system.subtitle}
                  </span>
                  <h3 className="text-xl font-bold text-white tracking-wide mt-1">{system.title}</h3>
                </div>
                <p className="text-sm text-slate-300 leading-relaxed">
                  {system.desc}
                </p>
              </div>
              <div className="mt-6 border-t border-white/5 pt-4 text-xs font-semibold italic text-cyan-300/80">
                {system.cta}
              </div>
            </motion.article>
          ))}
        </div>
      </div>
    </section>
  );
}
