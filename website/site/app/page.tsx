"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import ParticleCanvas from "@/app/transmission/ParticleCanvas";
import HeroSignal from "@/app/components/screens/HeroSignal";
import MysteryTransmission from "@/app/components/screens/MysteryTransmission";
import GameplayCards from "@/app/components/screens/GameplayCards";
import ScreenshotGallery from "@/app/components/screens/ScreenshotGallery";
import BossReveal from "@/app/components/screens/BossReveal";
import MissionLog from "@/app/components/screens/MissionLog";
import Downloads from "@/app/components/screens/Downloads";
import BetaCta from "@/app/components/screens/BetaCta";
import FooterSection from "@/app/components/screens/Footer";

export default function Home() {
  const [signalStrength, setSignalStrength] = useState(0);
  const missionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let rafId: number;

    const handleScroll = () => {
      if (rafId) cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        const sh = document.documentElement.scrollHeight - window.innerHeight;
        setSignalStrength(sh > 0 ? window.scrollY / sh : 0);
      });
    };

    handleScroll();
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => {
      window.removeEventListener("scroll", handleScroll);
      if (rafId) cancelAnimationFrame(rafId);
    };
  }, []);

  const handleBegin = useCallback(() => {
    missionRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  return (
    <>
      <ParticleCanvas strength={signalStrength} />

      <main className="relative z-10" id="main-content">
        <HeroSignal onBegin={handleBegin} />

        <div ref={missionRef}>
          <MysteryTransmission />
        </div>

        <GameplayCards />

        <ScreenshotGallery />

        <BossReveal />

        <MissionLog />

        <Downloads />

        <BetaCta />

        <FooterSection />
      </main>
    </>
  );
}
