"use client";

import { useRef, useState, useMemo } from "react";
import RocketSVG from "./game/RocketSVG";
import PlatformSVG from "./game/PlatformSVG";
import ThreatSVG from "./game/ThreatSVG";
import { PLAY_STORE_URL } from "@/lib/constants";

const ZONES = [
  { name: "Earth", threshold: 0, bg: "#0a0a12", mid: "#1a1a2e" },
  { name: "Cloud Layer", threshold: 500, bg: "#0a0a18", mid: "#1a0a2e" },
  { name: "Upper Atmosphere", threshold: 1500, bg: "#080510", mid: "#1f0f3a" },
  { name: "Orbit", threshold: 4000, bg: "#050308", mid: "#150a20" },
  { name: "Deep Space", threshold: 8000, bg: "#020205", mid: "#0a0510" },
  { name: "The Void", threshold: 15000, bg: "#010103", mid: "#050308" },
];

const TOTAL_ALT = 16000;

export default function AscentJourney() {
  const containerRef = useRef<HTMLDivElement>(null);
  const [scrollTop, setScrollTop] = useState(0);

  const handleScroll = () => {
    if (containerRef.current) setScrollTop(containerRef.current.scrollTop);
  };

  const maxScroll = containerRef.current ? containerRef.current.scrollHeight - containerRef.current.clientHeight : 1;
  const progress = Math.min(scrollTop / Math.max(maxScroll, 1), 1);
  const altitude = Math.round(progress * TOTAL_ALT);

  let zone = ZONES[0];
  for (const z of ZONES) { if (altitude >= z.threshold) zone = z; }

  const bosses = useMemo(() => [
    { y: 0.20, name: "COMMAND CRUISER", desc: "Platform Jammer" },
    { y: 0.40, name: "THE GATEKEEPER", desc: "Orbital Sentinel" },
    { y: 0.55, name: "THE LEVIATHAN", desc: "Apex Predator" },
    { y: 0.70, name: "VOID ENGINE", desc: "Reality Warper" },
  ], []);

  const platforms = useMemo(() => [
    { y: 0.06, type: "NORMAL" as const }, { y: 0.13, type: "MOVING" as const },
    { y: 0.20, type: "BOOST" as const }, { y: 0.27, type: "ICE" as const },
    { y: 0.34, type: "BREAKABLE" as const }, { y: 0.41, type: "PHASE" as const },
    { y: 0.48, type: "FUEL" as const }, { y: 0.55, type: "COOLING" as const },
    { y: 0.62, type: "STABILITY" as const }, { y: 0.69, type: "MAGNETIC" as const },
  ], []);

  return (
    <div className="relative h-screen w-full overflow-hidden bg-black">
      {/* Scroll driver — thin wrapper, no visual content */}
      <div ref={containerRef} onScroll={handleScroll} className="absolute inset-0 overflow-y-auto overflow-x-hidden">
        <div className="h-[600vh]" />
      </div>

      {/* Background — parallax with scroll */}
      <div className="fixed inset-0 z-0" style={{
        background: `linear-gradient(180deg, ${zone.bg} 0%, ${zone.mid} 50%, ${zone.bg} 100%)`,
        transform: `translateY(${scrollTop * 0.3}px)`,
      }} />

      {/* Stars — different parallax speed */}
      <div className="fixed inset-0 z-0 opacity-50" style={{
        backgroundImage: `radial-gradient(1px 1px at 15% 25%, rgba(255,255,255,0.6), transparent),
          radial-gradient(1.5px 1.5px at 35% 65%, rgba(255,255,255,0.5), transparent),
          radial-gradient(1px 1px at 55% 15%, rgba(255,255,255,0.7), transparent),
          radial-gradient(1.5px 1.5px at 75% 85%, rgba(255,255,255,0.5), transparent),
          radial-gradient(1px 1px at 25% 75%, rgba(255,255,255,0.6), transparent),
          radial-gradient(1px 1px at 85% 35%, rgba(255,255,255,0.5), transparent)`,
        transform: `translateY(${scrollTop * 0.15}px)`,
      }} />

      {/* Vignette */}
      <div className="fixed inset-0 z-[1] pointer-events-none" style={{
        background: "radial-gradient(ellipse at center, transparent 30%, rgba(0,0,0,0.9) 100%)",
      }} />

      {/* Bosses — fly in from above */}
      {bosses.map((boss, i) => {
        const dist = Math.abs(progress - boss.y);
        const visible = dist < 0.03;
        return (
          <div key={i} className="fixed left-1/2 top-1/2 z-30 pointer-events-none" style={{
            transform: `translate(-50%, -50%) translateY(${visible ? 0 : -100}%) scale(${visible ? 1 : 0.8})`,
            opacity: visible ? 1 : 0,
            transition: "transform 0.5s cubic-bezier(0.175,0.885,0.32,1.275), opacity 0.2s",
          }}>
            <div className="px-10 py-5 rounded-2xl border-2 border-red-500/70 bg-black/90 shadow-[0_0_60px_rgba(255,23,68,0.4)]">
              <p className="text-xs uppercase tracking-[0.4em] text-red-400 font-bold">Threat Detected</p>
              <p className="mt-3 text-3xl font-black text-white tracking-wide">{boss.name}</p>
              <p className="text-sm text-red-300/90 mt-2">{boss.desc}</p>
            </div>
          </div>
        );
      })}

      {/* Platforms — fly past on alternating sides */}
      {platforms.map((p, i) => {
        const dist = Math.abs(progress - p.y);
        if (dist > 0.08) return null;
        const v = 1 - dist / 0.08;
        const offset = (progress - p.y) * 300;
        const side = i % 2 === 0 ? -1 : 1;
        return (
          <div key={i} className="fixed left-1/2 top-1/2 z-10 pointer-events-none" style={{
            transform: `translate(calc(-50% + ${side * 22}%), calc(-50% + ${offset}%)) scale(${0.7 + v * 0.3})`,
            opacity: v * 0.9,
            transition: "opacity 0.15s",
          }}>
            <PlatformSVG type={p.type} width={120} height={14} animated />
          </div>
        );
      })}

      {/* Rocket — center screen */}
      <div className="fixed left-1/2 top-1/2 z-20" style={{
        transform: `translate(-50%, -50%) rotate(${Math.sin(scrollTop / 180) * 2}deg)`,
        transition: "transform 0.1s ease-out",
      }}>
        <RocketSVG type="BALANCED" size={90} thrusting />
      </div>

      {/* HUD — large, readable */}
      <div className="fixed left-8 top-8 z-30 pointer-events-none">
        <p className="font-mono text-5xl font-black text-white tracking-tight">{altitude.toLocaleString()}m</p>
        <p className="text-lg uppercase tracking-[0.25em] text-cyan-300 font-bold mt-2">{zone.name}</p>
        <div className="mt-4 h-2 w-40 rounded-full bg-white/10">
          <div className="h-full rounded-full bg-cyan-400" style={{ width: `${progress * 100}%` }} />
        </div>
      </div>

      {/* Start CTA */}
      <div className="fixed bottom-10 left-1/2 z-30 -translate-x-1/2 text-center" style={{
        opacity: progress < 0.02 ? 1 : 0,
        transition: "opacity 0.5s",
        pointerEvents: progress < 0.02 ? "auto" : "none",
      }}>
        <p className="text-sm uppercase tracking-[0.4em] text-cyan-300 font-bold mb-3">Jump Droid</p>
        <p className="text-xl text-slate-200 mb-6 font-semibold">The Signal From the Void</p>
        <a href={PLAY_STORE_URL} target="_blank" rel="noopener noreferrer"
          className="inline-block rounded-full bg-cyan-400 px-10 py-4 text-lg font-black uppercase tracking-widest text-black hover:bg-cyan-300 shadow-[0_0_40px_rgba(0,229,255,0.5)]">
          Download
        </a>
      </div>

      {/* End CTA */}
      <div className="fixed bottom-10 left-1/2 z-30 -translate-x-1/2 text-center" style={{
        opacity: progress > 0.94 ? 1 : 0,
        transition: "opacity 0.5s",
        pointerEvents: progress > 0.94 ? "auto" : "none",
      }}>
        <p className="text-sm uppercase tracking-[0.4em] text-cyan-300 font-bold mb-3">Mission Complete</p>
        <p className="text-xl text-slate-200 mb-6 font-semibold">You reached The Signal.</p>
        <a href={PLAY_STORE_URL} target="_blank" rel="noopener noreferrer"
          className="inline-block rounded-full bg-cyan-400 px-10 py-4 text-lg font-black uppercase tracking-widest text-black hover:bg-cyan-300 shadow-[0_0_40px_rgba(0,229,255,0.5)]">
          Download
        </a>
      </div>
    </div>
  );
}
