"use client";

import { useState, useEffect, useRef } from "react";
import { ZONE_THEMES } from "@/app/data/site-content";
import ThreatSVG from "@/app/components/game/ThreatSVG";

export default function ZoneAscension() {
  const [activeZone, setActiveZone] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleScroll = () => {
      if (!containerRef.current) return;
      const rect = containerRef.current.getBoundingClientRect();
      const sectionHeight = rect.height / ZONE_THEMES.length;
      const scrollPos = Math.max(0, -rect.top);
      const index = Math.min(
        ZONE_THEMES.length - 1,
        Math.floor(scrollPos / sectionHeight)
      );
      setActiveZone(index);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const currentTheme = ZONE_THEMES[activeZone];

  return (
    <section ref={containerRef} className="relative w-full" style={{ height: `${ZONE_THEMES.length * 100}vh` }}>
      {/* Background Color Transition */}
      <div
        className="fixed inset-0 z-0 transition-colors duration-1000 ease-in-out"
        style={{
          backgroundColor: "#000000",
          backgroundImage: `radial-gradient(circle at center, ${currentTheme.glow}, transparent 70%)`
        }}
      />

      {/* Floating Entities */}
      <div className="fixed inset-0 z-10 pointer-events-none">
        {currentTheme.entities.map((e, i) => (
          <div
            key={`${activeZone}-${i}`}
            className="absolute animate-float opacity-40 transition-all duration-1000"
            style={{
              top: `${20 + i * 30}%`,
              left: `${15 + (i % 2) * 60}%`,
              transform: `scale(${0.8 + Math.random() * 0.5})`
            }}
          >
            <ThreatSVG type={e.type as any} size={80} />
          </div>
        ))}
      </div>

      {/* Content Overlay */}
      {ZONE_THEMES.map((zone, i) => (
        <div key={zone.id} className="relative h-screen flex flex-col items-center justify-center z-20 px-6">
          <div className="text-center">
            <p className="font-mono text-[10px] tracking-[0.4em] text-cyan-400/50 uppercase mb-4">
              Altitude: {zone.altitude}
            </p>
            <h2 className="font-mono text-4xl sm:text-6xl font-bold tracking-tight text-white uppercase mb-6">
              {zone.name}
            </h2>
            <div className="w-12 h-0.5 bg-white/20 mx-auto mb-8" />
            <p className="font-mono text-sm text-slate-500 max-w-sm mx-auto">
              {/* We could add zone-specific flavor text here from AREA_LIBRARY if we had it in site-content */}
              Atmospheric conditions stabilizing at {zone.altitude}.
            </p>
          </div>
        </div>
      ))}

      <style jsx>{`
        @keyframes float {
          0%, 100% { transform: translateY(0) rotate(0deg); }
          50% { transform: translateY(-20px) rotate(5deg); }
        }
        .animate-float {
          animation: float 8s ease-in-out infinite;
        }
      `}</style>
    </section>
  );
}
