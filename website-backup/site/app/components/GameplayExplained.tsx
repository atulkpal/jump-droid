"use client";

import { motion } from "framer-motion";

export default function GameplayExplained() {
  const steps = [
    {
      step: "01",
      title: "Touch to Thrust",
      description: "Tap or hold your screen to activate the primary boosters. Keep a close eye on your fuel reserves and combustion heat.",
    },
    {
      step: "02",
      title: "Land & Combo",
      description: "Touch down on platforms to stabilize. Landing consecutive jumps builds a combo multiplier which restores your shields.",
    },
    {
      step: "03",
      title: "Manage Resources",
      description: "Juggle three systems: Fuel capacity (thrust duration), Engine Heat (cooldown limits), and Shields (impact defense).",
    },
    {
      step: "04",
      title: "Survive Encounters",
      description: "Climb through hazards and face unique bosses every 1,500m of altitude. Find their weak points and shut them down.",
    },
  ];

  return (
    <section id="gameplay" className="relative overflow-hidden py-24 sm:py-32">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(0,229,255,0.08),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="space-y-12">
          <div className="max-w-2xl space-y-4">
            <p className="text-sm uppercase tracking-[0.35em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 inline-block">
              Core Loop
            </p>
            <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
              The mechanics of ascent
            </h2>
            <p className="text-slate-300 text-sm leading-relaxed">
              Every vertical meter is earned. Learn the rules of gravity and mechanical stability before breaking orbit.
            </p>
          </div>
          <div className="grid gap-6 sm:grid-cols-2">
            {steps.map((item, i) => (
              <motion.article
                key={item.step}
                className="rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-8 backdrop-blur-md transition hover:border-cyan-400/40 hover:bg-slate-900/60 flex gap-6"
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: "-100px" }}
                transition={{ duration: 0.5, delay: i * 0.1 }}
              >
                <div className="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-b from-cyan-400 to-cyan-700 select-none">
                  {item.step}
                </div>
                <div className="space-y-2">
                  <h3 className="text-lg font-bold text-white tracking-wide">{item.title}</h3>
                  <p className="text-sm leading-relaxed text-slate-300">{item.description}</p>
                </div>
              </motion.article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
