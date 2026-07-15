"use client";

import { useState, useEffect, useRef } from "react";
import ParticleCanvas from "@/app/transmission/ParticleCanvas";
import HeroSignal from "@/app/components/screens/HeroSignal";
import MysteryTransmission from "@/app/components/screens/MysteryTransmission";
import GameplayCards from "@/app/components/screens/GameplayCards";
import ScreenshotGallery from "@/app/components/screens/ScreenshotGallery";
import MissionLog from "@/app/components/screens/MissionLog";
import FooterSection from "@/app/components/screens/Footer";

export default function Home() {
  const [signalStrength, setSignalStrength] = useState(0);
  const rafRef = useRef<number>(0);

  useEffect(() => {
    const handleScroll = () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
      rafRef.current = requestAnimationFrame(() => {
        const sh = document.documentElement.scrollHeight - window.innerHeight;
        setSignalStrength(sh > 0 ? window.scrollY / sh : 0);
      });
    };

    handleScroll();
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => {
      window.removeEventListener("scroll", handleScroll);
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, []);

  return (
    <>
      <ParticleCanvas strength={signalStrength} />

      <main className="relative z-10" id="main-content">
        <HeroSignal />
        <MysteryTransmission />
        <GameplayCards />
        <ScreenshotGallery />
        <MissionLog />
        <FooterSection />
      </main>
    </>
  );
}
