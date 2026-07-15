"use client";

import { useState, useEffect, useRef, useMemo } from "react";
import { INTRO_LINES } from "@/app/data/transmission-packets";
import RocketSVG from "@/app/components/game/RocketSVG";
import PlatformSVG from "@/app/components/game/PlatformSVG";

export default function SignalPulse({ progress }: { progress: number }) {
  const [typedProgress, setTypedProgress] = useState(0);
  const isActive = progress > 0.05;
  const wasActiveRef = useRef(false);

  useEffect(() => {
    if (!isActive) {
      wasActiveRef.current = false;
      return;
    }
    if (wasActiveRef.current) return;
    wasActiveRef.current = true;

    const chars = INTRO_LINES[0].length + INTRO_LINES[1].length;
    const duration = Math.max(3000, chars * 28);
    const start = Date.now();

    const onFrame = () => {
      const elapsed = Date.now() - start;
      const t = Math.min(elapsed / duration, 1);
      setTypedProgress(t);
      if (t >= 1) return;
      raf = requestAnimationFrame(onFrame);
    };
    let raf = requestAnimationFrame(onFrame);

    return () => cancelAnimationFrame(raf);
  }, [isActive]);

  const fullText = INTRO_LINES[0] + INTRO_LINES[1];
  const totalChars = fullText.length;
  const visibleChars = Math.floor(typedProgress * totalChars);
  const visiblePart = fullText.slice(0, visibleChars);
  const typed1 = visiblePart.slice(0, INTRO_LINES[0].length);
  const typed2 = visiblePart.slice(INTRO_LINES[0].length);

  const pulseScale = 0.8 + progress * 0.4;
  const pulseOpacity = 0.3 + progress * 0.7;
  const teaserOpacity = 0.06 + progress * 0.08;

  const dots = useMemo(() =>
    Array.from({ length: 60 }, (_, i) => ({
      angle: (i / 60) * Math.PI * 2,
      radius: 24 + ((i * 7 + 13) % 100) / 100 * 12,
      delay: ((i * 17 + 11) % 100) / 100 * 2,
    })), []);

  return (
    <section className="relative flex w-full flex-col items-center justify-center px-6">

      {/* Teaser rocket silhouette — left */}
      <div
        className="absolute left-4 sm:left-8 bottom-20 sm:bottom-28 pointer-events-none"
        style={{
          opacity: teaserOpacity,
          animation: "entity-float 10s ease-in-out infinite",
          width: "120px",
          height: "auto",
        }}
      >
        <RocketSVG />
      </div>

      {/* Teaser platform silhouette — right */}
      <div
        className="absolute right-4 sm:right-8 bottom-24 sm:bottom-32 pointer-events-none"
        style={{
          opacity: teaserOpacity * 0.8,
          animation: "entity-float 13s ease-in-out infinite",
          animationDelay: "2s",
          width: "140px",
          height: "auto",
        }}
      >
        <PlatformSVG type="NORMAL" width={140} height={24} />
      </div>

      <div className="flex flex-col items-center gap-8">
        {/* Orbiting dots */}
        <div
          className="relative flex items-center justify-center"
          style={{
            transform: `scale(${pulseScale})`,
            opacity: pulseOpacity,
            transition: "transform 0.5s ease-out, opacity 0.5s ease-out",
          }}
        >
          {dots.map((d, i) => (
            <div
              key={i}
              className="absolute rounded-full bg-cyan-400"
              style={{
                width: "1.5px",
                height: "1.5px",
                transform: `rotate(${d.angle}rad) translateX(${d.radius}px)`,
                animation: `orbit-pulse 3s ease-in-out ${d.delay}s infinite`,
                opacity: 0.2 + progress * 0.8,
              }}
            />
          ))}
          <div className="h-8 w-8 rounded-full border border-cyan-400/40 flex items-center justify-center">
            <div className="h-2 w-2 rounded-full bg-cyan-300 shadow-[0_0_8px_rgba(0,229,255,0.6)]" />
          </div>
        </div>

        {/* Typed text */}
        <div className="flex flex-col items-center gap-1 font-mono text-sm tracking-[0.2em] text-cyan-300/80">
          <p>
            {typed1}
            {typed1.length < INTRO_LINES[0].length && (
              <span className="inline-block w-[2px] h-[1em] bg-cyan-300 ml-[2px] animate-cursor-blink" />
            )}
          </p>
          <p>
            {typed2}
            {typed2.length < INTRO_LINES[1].length && typed1.length >= INTRO_LINES[0].length && (
              <span className="inline-block w-[2px] h-[1em] bg-cyan-300 ml-[2px] animate-cursor-blink" />
            )}
          </p>
        </div>
      </div>
    </section>
  );
}
