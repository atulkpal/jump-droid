"use client";

import { useState, useEffect, useMemo } from "react";
import ZoneBackgrounds from "./components/zone-backgrounds/ZoneBackgrounds";
import FlyingRocket from "./components/FlyingRocket";
import AltitudeHUD from "./components/AltitudeHUD";
import EncounterSystem, { ENCOUNTERS, BOSS_ENCOUNTERS } from "./components/EncounterSystem";
import BossEncounter from "./components/BossEncounter";
import FinaleArchive from "./components/FinaleArchive";

const ZONES = [
  { name: "Earth", threshold: 0 },
  { name: "Cloud Layer", threshold: 500 },
  { name: "Upper Atmosphere", threshold: 1500 },
  { name: "Orbit", threshold: 4000 },
  { name: "Deep Space", threshold: 8000 },
  { name: "The Void", threshold: 15000 },
];

const TOTAL_ALT = 16000;
const SCROLL_HEIGHT = 7000; // vh-based scroll distance

type BossPhase = "ENTER" | "FIGHT" | "EXIT";

function getCurrentEncounter(progress: number) {
  const all = [...ENCOUNTERS, ...BOSS_ENCOUNTERS];
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
      setScrollHeight(document.body.scrollHeight);
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
    <>
      {/* Scroll spacer */}
      <div style={{ height: `${SCROLL_HEIGHT}vh`, pointerEvents: "none" }} />

      {/* Zone backgrounds (lazy-loaded) */}
      <ZoneBackgrounds altitude={altitude} />

      {/* Encounters (enter/peak/exit) */}
      <EncounterSystem progress={progress} rocketX={rocketX} viewWidth={1200} />

      {/* Boss encounter (takes over screen during FIGHT phase) */}
      {isBossActive && (
        <BossEncounter
          entity={activeEncounter.entity as any}
          phase={bossState.phase}
          progress={bossState.phaseProgress}
          message={activeEncounter.message}
        />
      )}

      {/* Rocket (flies through world) */}
      <FlyingRocket progress={progress} rocketX={rocketX} />

      {/* HUD */}
      <AltitudeHUD altitude={altitude} zoneName={zone.name} progress={progress} />

      {/* Finale archive */}
      {progress > 0.92 && <FinaleArchive progress={progress} />}

      {/* Start CTA */}
      <div
        className="fixed bottom-10 left-1/2 z-30 -translate-x-1/2 text-center"
        style={{ opacity: progress < 0.02 ? 1 : 0, transition: "opacity 0.5s" }}
      >
        <p className="text-sm uppercase tracking-[0.4em] text-cyan-300 font-bold mb-3">Jump Droid</p>
        <p className="text-xl text-slate-200 mb-6 font-semibold">The Signal From the Void</p>
        <a
          href="https://play.google.com"
          target="_blank"
          rel="noopener noreferrer"
          className="pointer-events-auto inline-block rounded-full bg-cyan-400 px-10 py-4 text-lg font-black uppercase tracking-widest text-black hover:bg-cyan-300 shadow-[0_0_40px_rgba(0,229,255,0.5)]"
        >
          Download
        </a>
      </div>

      {/* End CTA (hidden when finale archive is active) */}
      <div
        className="fixed bottom-10 left-1/2 z-30 -translate-x-1/2 text-center"
        style={{ opacity: progress > 0.94 && progress <= 0.92 ? 1 : 0, transition: "opacity 0.5s", display: progress > 0.92 ? "none" : "block" }}
      >
        <p className="text-sm uppercase tracking-[0.4em] text-cyan-300 font-bold mb-3">Mission Complete</p>
        <p className="text-xl text-slate-200 mb-6 font-semibold">You reached The Signal.</p>
        <a
          href="https://play.google.com"
          target="_blank"
          rel="noopener noreferrer"
          className="pointer-events-auto inline-block rounded-full bg-cyan-400 px-10 py-4 text-lg font-black uppercase tracking-widest text-black hover:bg-cyan-300 shadow-[0_0_40px_rgba(0,229,255,0.5)]"
        >
          Download
        </a>
      </div>
    </>
  );
}