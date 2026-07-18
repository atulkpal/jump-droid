"use client";

import { useState, useEffect, useRef } from "react";
import ParticleCanvas from "@/app/transmission/ParticleCanvas";
import HeroSignal from "@/app/components/screens/HeroSignal";
import MysteryTransmission from "@/app/components/screens/MysteryTransmission";
import GameplayCards from "@/app/components/screens/GameplayCards";
import ScreenshotGallery from "@/app/components/screens/ScreenshotGallery";
import MissionLog from "@/app/components/screens/MissionLog";
import FooterSection from "@/app/components/screens/Footer";
import PlayStoreModal from "@/app/components/screens/PlayStoreModal";

export default function Home() {
  const [signalStrength, setSignalStrength] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [betaLanded, setBetaLanded] = useState(false);
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
        <HeroSignal onPlayStoreClick={() => setShowModal(true)} onBetaLanded={() => setBetaLanded(true)} onLandingReset={() => setBetaLanded(false)} />
        <MysteryTransmission />
        <GameplayCards />
        <ScreenshotGallery />
        <MissionLog onPlayStoreClick={() => setShowModal(true)} showBetaLanded={betaLanded} />
        <FooterSection />
      </main>

      {showModal && <PlayStoreModal onClose={() => setShowModal(false)} />}
    </>
  );
}
