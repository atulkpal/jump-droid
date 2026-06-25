"use client";

import { useState, useEffect, useMemo } from "react";
import ZoneBackgrounds from "./components/zone-backgrounds/ZoneBackgrounds";
import FlyingRocket from "./components/FlyingRocket";
import AltitudeHUD from "./components/AltitudeHUD";
import EncounterSystem, { ENCOUNTERS, BOSS_ENCOUNTERS } from "./components/EncounterSystem";
import BossEncounter from "./components/BossEncounter";
import StickyNav from "./components/StickyNav";
import HeroSection from "./components/HeroSection";
import GameplayExplained from "./components/GameplayExplained";
import PlatformShowcase from "./components/PlatformShowcase";
import BossShowcase from "./components/BossShowcase";
import RocketShowcase from "./components/RocketShowcase";
import DiscoveryArchive from "./components/DiscoveryArchive";
import ProgressionSystems from "./components/ProgressionSystems";
import MissionControl from "./components/MissionControl";
import GameSimulator from "./components/GameSimulator";

const ZONES = [
  { name: "Earth", threshold: 0 },
  { name: "Cloud Layer", threshold: 500 },
  { name: "Upper Atmosphere", threshold: 1500 },
  { name: "Orbit", threshold: 4000 },
  { name: "The Foundry", threshold: 5000 },
  { name: "Deep Space", threshold: 8000 },
  { name: "Chrono-Rift", threshold: 13000 },
  { name: "The Void", threshold: 15000 },
];

const TOTAL_ALT = 22000;

type BossPhase = "ENTER" | "FIGHT" | "EXIT";

function getCurrentEncounter(progress: number) {
  const all = [...BOSS_ENCOUNTERS, ...ENCOUNTERS];
  const active = all.find((e) => {
    const [enter, , exit] = e.progress;
    return progress >= enter && progress <= exit;
  });
  return active || null;
}

function getBossPhase(progress: number, encounter: any | null): { phase: BossPhase; phaseProgress: number } | null {
  if (!encounter || encounter.type !== "boss") return null;
  const [enter, peak, exit] = encounter.progress;
  if (progress < enter) return { phase: "ENTER", phaseProgress: 0 };
  if (progress < peak) {
    return { phase: "ENTER", phaseProgress: (progress - enter) / (peak - enter) };
  }
  if (progress < exit) {
    return { phase: "FIGHT", phaseProgress: (progress - peak) / (exit - peak) };
  }
  return { phase: "EXIT", phaseProgress: 1 };
}

function calculateRocketX(progress: number): number {
  // Base flight path: left 30% -> center 50% with sine sway
  const baseX = 30 + progress * 20;
  const sway = Math.sin(progress * 18) * 3 + Math.sin(progress * 7) * 1.5;
  return baseX + sway;
}

export default function Home() {
  const [scrollTop, setScrollTop] = useState(0);
  const [scrollHeight, setScrollHeight] = useState(0);
  const [clientHeight, setClientHeight] = useState(0);

  useEffect(() => {
    const handleScroll = () => {
      setScrollTop(window.scrollY);
      setScrollHeight(document.documentElement.scrollHeight || document.body.scrollHeight);
      setClientHeight(window.innerHeight);
    };

    handleScroll();
    window.addEventListener("scroll", handleScroll, { passive: true });
    window.addEventListener("resize", handleScroll);
    return () => {
      window.removeEventListener("scroll", handleScroll);
      window.removeEventListener("resize", handleScroll);
    };
  }, []);

  const maxScroll = Math.max(scrollHeight - clientHeight, 1);
  const progress = Math.min(scrollTop / maxScroll, 1);
  const altitude = Math.round(progress * TOTAL_ALT);

  let zone = ZONES[0];
  for (const z of ZONES) { if (altitude >= z.threshold) zone = z; }

  const activeEncounter = useMemo(() => getCurrentEncounter(progress), [progress]);
  const bossState = useMemo(() => getBossPhase(progress, activeEncounter), [progress, activeEncounter]);
  const rocketX = calculateRocketX(progress);

  const isBossActive = activeEncounter?.type === "boss" && bossState?.phase === "FIGHT";

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      {/* Sticky Navigation */}
      <StickyNav />

      {/* FIXED BACKGROUND LAYERS (z-0 to z-15) */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        <ZoneBackgrounds altitude={altitude} />
      </div>

      <div className="fixed inset-0 z-[5] pointer-events-none">
        <EncounterSystem progress={progress} rocketX={rocketX} viewWidth={1200} />
      </div>

      <div className="fixed inset-0 z-[25] pointer-events-none">
        <FlyingRocket progress={progress} rocketX={rocketX} />
      </div>

      {/* Boss warning/takeover overlay effect (z-30) */}
      {isBossActive && (
        <BossEncounter
          entity={activeEncounter.entity as any}
          phase={bossState.phase}
          progress={bossState.phaseProgress}
          message={activeEncounter.message}
        />
      )}

      {/* HUD (z-30) */}
      <AltitudeHUD altitude={altitude} zoneName={zone.name} progress={progress} />

      {/* SCROLLABLE FOREGROUND CONTENT (z-20) */}
      <main className="relative z-20 pointer-events-auto">
        <HeroSection />
        
        <GameplayExplained />
        
        {/* Platform Showcase Section */}
        <section id="ascent" className="relative overflow-hidden py-24 sm:py-32">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.08),transparent_20%)]" />
          <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
            <div className="mb-12 max-w-2xl space-y-4">
              <p className="text-sm uppercase tracking-[0.35em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 inline-block">
                Atmospheres & Platforms
              </p>
              <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
                Twelve platform types. Adapt or fall.
              </h2>
              <p className="max-w-2xl text-slate-300 text-sm leading-relaxed">
                Platforms behave differently in each zone. From simple solid ground to shifting, icy, breaking, or magnetic fields—timing your thrusters is key.
              </p>
            </div>
            <PlatformShowcase />
          </div>
        </section>

        <BossShowcase />
        
        <RocketShowcase />
        
        <GameSimulator />
        
        <DiscoveryArchive />
        
        <ProgressionSystems />
        
        <MissionControl />

        {/* Footer */}
        <footer className="border-t border-white/10 bg-black/90 py-12 text-slate-400 relative z-20">
          <div className="mx-auto flex flex-col gap-6 px-6 sm:px-8 lg:px-12 lg:flex-row lg:items-center lg:justify-between">
            <p className="text-sm">Jump Droid — The Signal From the Void.</p>
            <div className="flex flex-wrap items-center gap-6 text-sm">
              <a href="#hero" className="transition hover:text-cyan-200">Back to top</a>
              <a href="#mission-control" className="transition hover:text-cyan-200">Crew Briefing</a>
              <a href="#" className="transition hover:text-cyan-200">Privacy Protocol</a>
            </div>
          </div>
        </footer>
      </main>
    </div>
  );
}