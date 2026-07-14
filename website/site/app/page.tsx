"use client";

import { useState, useEffect, useMemo } from "react";
import dynamic from "next/dynamic";
import ZoneBackgrounds from "./components/zone-backgrounds/ZoneBackgrounds";
import FlyingRocket from "./components/FlyingRocket";
import AltitudeHUD from "./components/AltitudeHUD";
import EncounterSystem, { ENCOUNTERS, BOSS_ENCOUNTERS } from "./components/EncounterSystem";
import BossEncounter from "./components/BossEncounter";
import StickyNav from "./components/StickyNav";
import PortalSection from "./components/PortalSection";
import PlatformsSection from "./components/PlatformsSection";
import RocketsSection from "./components/RocketsSection";
import ArchiveSection from "./components/ArchiveSection";
import GallerySection from "./components/GallerySection";
import LaunchSection from "./components/LaunchSection";
import FloatingDownload from "./components/FloatingDownload";

const ThreatsSection = dynamic(() => import("./components/ThreatsSection"), {
  loading: () => <div className="min-h-dvh" />,
});

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
      <StickyNav />

      <div className="fixed inset-0 z-0 pointer-events-none">
        <ZoneBackgrounds altitude={altitude} />
      </div>

      <div className="fixed inset-0 z-[5] pointer-events-none">
        <EncounterSystem progress={progress} rocketX={rocketX} viewWidth={1200} />
      </div>

      <div className="fixed inset-0 z-[25] pointer-events-none">
        <FlyingRocket progress={progress} rocketX={rocketX} />
      </div>

      {isBossActive && (
        <BossEncounter
          entity={activeEncounter.entity as any}
          phase={bossState.phase}
          progress={bossState.phaseProgress}
          message={activeEncounter.message}
        />
      )}

      <AltitudeHUD altitude={altitude} zoneName={zone.name} progress={progress} />

      <FloatingDownload />

      <main id="main-content" className="relative z-20 pointer-events-auto">
        <PortalSection />
        <PlatformsSection />
        <ThreatsSection />
        <RocketsSection />
        <ArchiveSection />
        <GallerySection />
        <LaunchSection />
      </main>
    </div>
  );
}
