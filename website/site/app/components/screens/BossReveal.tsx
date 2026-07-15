"use client";

import { useState, useEffect } from "react";
import ThreatSVG from "@/app/components/game/ThreatSVG";

const BOSSES = [
  { type: "COMMAND_CRUISER" as const, size: 50 },
  { type: "THE_GATEKEEPER" as const, size: 80 },
  { type: "VOID_ENGINE" as const, size: 55 },
  { type: "THE_LEVIATHAN" as const, size: 50 },
  { type: "THE_SIGNAL" as const, size: 45 },
];

export default function BossReveal() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.2 }
    );

    const el = document.getElementById("boss-reveal");
    if (el) observer.observe(el);

    return () => observer.disconnect();
  }, []);

  return (
    <section
      id="boss-reveal"
      className="relative flex min-h-dvh w-full flex-col items-center justify-center overflow-hidden px-6"
    >
      {/* Purple glow */}
      <div
        className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(213,0,249,0.08),transparent_60%)] pointer-events-none transition-opacity duration-1000"
        style={{ opacity: visible ? 1 : 0 }}
      />

      <div className="relative z-10 text-center mb-8">
        <p
          className="font-mono text-[10px] tracking-[0.25em] text-red-400/60 uppercase mb-3 transition-all duration-1000"
          style={{ opacity: visible ? 1 : 0, transitionDelay: "0.2s" }}
        >
          Encounter at 12,000m
        </p>
        <h2
          className="font-mono text-xl sm:text-2xl font-bold tracking-[0.1em] text-white uppercase mb-2 transition-all duration-1000"
          style={{ opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(12px)", transitionDelay: "0.4s" }}
        >
          Boss Encounters
        </h2>
        <p
          className="font-mono text-sm sm:text-base leading-relaxed text-slate-500 max-w-sm mx-auto transition-all duration-1000"
          style={{ opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(12px)", transitionDelay: "0.6s" }}
        >
          11 unique threats. Multi-phase attack patterns. No two encounters play the same.
        </p>
      </div>

      {/* Boss roster */}
      <div className="relative flex items-center justify-center gap-3 sm:gap-5">
        {BOSSES.map((boss, i) => (
          <div
            key={i}
            className="transition-all duration-1000"
            style={{
              opacity: visible ? 0.35 : 0,
              transform: visible ? "scale(1) translateY(0)" : "scale(0.7) translateY(20px)",
              transitionDelay: `${0.8 + i * 0.15}s`,
            }}
          >
            <div
              className="flex items-center justify-center"
              style={{
                width: `${boss.size}px`,
                height: `${boss.size}px`,
              }}
            >
              <ThreatSVG type={boss.type} size={boss.size} />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
