"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import ParticleCanvas from "@/app/transmission/ParticleCanvas";
import SignalStrength from "@/app/transmission/SignalStrength";
import SignalPulse from "@/app/transmission/SignalPulse";
import DataPacket from "@/app/transmission/DataPacket";
import SignalArchive from "@/app/transmission/SignalArchive";
import SignalSource from "@/app/transmission/SignalSource";
import SignalTerminated from "@/app/transmission/SignalTerminated";
import { PACKETS, COORDINATE_LINES } from "@/app/data/transmission-packets";

function computeSectionProgress(el: HTMLElement, windowHeight: number): number {
  const rect = el.getBoundingClientRect();
  const total = rect.height + windowHeight;
  const scrolled = windowHeight - rect.top;
  return Math.max(0, Math.min(1, scrolled / total));
}

export default function Home() {
  const [signalStrength, setSignalStrength] = useState(0);
  const [progs, setProgs] = useState<number[]>([]);
  const refs = useRef<(HTMLDivElement | null)[]>([]);

  const setRef = useCallback((i: number) => (el: HTMLDivElement | null) => {
    refs.current[i] = el;
  }, []);

  useEffect(() => {
    let rafId: number;

    const handleScroll = () => {
      if (rafId) cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        const sh = document.documentElement.scrollHeight - window.innerHeight;
        const global = sh > 0 ? window.scrollY / sh : 0;
        setSignalStrength(global);

        const wh = window.innerHeight;
        const updated = refs.current.map((el) =>
          el ? computeSectionProgress(el, wh) : 0
        );
        setProgs(updated);
      });
    };

    handleScroll();
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => {
      window.removeEventListener("scroll", handleScroll);
      if (rafId) cancelAnimationFrame(rafId);
    };
  }, []);

  const p = (i: number) => progs[i] ?? 0;

  return (
    <>
      <ParticleCanvas strength={signalStrength} />
      <SignalStrength percent={signalStrength * 100} />

      <main className="relative z-20" id="main-content">
        {/* 0: Signal Pulse */}
        <div ref={setRef(0)} className="min-h-screen flex items-center">
          <SignalPulse progress={p(0)} />
        </div>

        {/* 1: Coordinates Decoded */}
        <div ref={setRef(1)} className="flex items-center px-6 py-20 sm:py-24">
          <div className="mx-auto w-full max-w-lg lg:max-w-2xl">
            {COORDINATE_LINES.map((line, i) => {
              const lineProgress = Math.max(0, Math.min(1, (p(1) - i * 0.12) / 0.3));
              return (
                <p
                  key={i}
                  className="font-mono text-xs sm:text-sm tracking-[0.15em] text-slate-400 leading-8 sm:leading-10"
                  style={{
                    opacity: lineProgress,
                    transform: `translateY(${(1 - lineProgress) * 12}px)`,
                    transition: "opacity 0.4s ease-out, transform 0.4s ease-out",
                  }}
                >
                  {">"} {line}
                </p>
              );
            })}
          </div>
        </div>

        {/* 2-7: Data Packets */}
        {PACKETS.map((packet, i) => (
          <div key={packet.id} ref={setRef(i + 2)}>
            <DataPacket packet={packet} progress={p(i + 2)} />
          </div>
        ))}

        {/* 8: Signal Archive */}
        <div ref={setRef(8)}>
          <SignalArchive progress={p(8)} />
        </div>

        {/* 9: Signal Source */}
        <div ref={setRef(9)}>
          <SignalSource progress={p(9)} />
        </div>

        {/* 10: Signal Terminated */}
        <SignalTerminated />
      </main>
    </>
  );
}
