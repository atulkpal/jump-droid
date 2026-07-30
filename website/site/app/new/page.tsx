"use client";

import { useState } from "react";
import ParticleCanvas from "@/app/transmission/ParticleCanvas";
import HeroSignal from "@/app/components/screens/HeroSignal";
import FooterSection from "@/app/components/screens/Footer";
import PlayStoreModal from "@/app/components/screens/PlayStoreModal";
import PilotFeed from "./components/PilotFeed";
import CommunityStats from "./components/CommunityStats";
import FleetHangar from "./components/FleetHangar";
import ZoneAscension from "./components/ZoneAscension";

export default function NewLanding() {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <ParticleCanvas strength={0.5} />

      <main className="relative z-10">
        <HeroSignal onPlayStoreClick={() => setShowModal(true)} />

        <PilotFeed />

        <CommunityStats />

        <FleetHangar />

        <div className="py-20 text-center bg-black">
          <p className="font-mono text-[10px] tracking-[0.5em] text-white/20 uppercase">
            Scroll to Ascend
          </p>
          <div className="mt-4 animate-bounce text-white/20">↓</div>
        </div>

        <ZoneAscension />

        <FooterSection />
      </main>

      {showModal && <PlayStoreModal onClose={() => setShowModal(false)} />}
    </>
  );
}
