"use client";

import { useMemo } from "react";
import { INTRO_LINES } from "@/app/data/transmission-packets";
import RocketSVG from "@/app/components/game/RocketSVG";

function useTypedText(text: string, progress: number): string {
  const totalChars = text.length;
  const visibleChars = Math.max(0, Math.min(totalChars, Math.floor(totalChars * Math.min(progress, 1))));
  return text.slice(0, visibleChars);
}

export default function SignalPulse({ progress }: { progress: number }) {
  const typed1 = useTypedText(INTRO_LINES[0], Math.max(0, (progress - 0) / 0.5));
  const typed2 = useTypedText(INTRO_LINES[1], Math.max(0, (progress - 0.5) / 0.5));

  const pulseScale = 0.8 + progress * 0.4;
  const pulseOpacity = 0.3 + progress * 0.7;
  const teaserOpacity = 0.03 + progress * 0.04;

  const dots = useMemo(() =>
    Array.from({ length: 60 }, (_, i) => ({
      angle: (i / 60) * Math.PI * 2,
      radius: 24 + ((i * 7 + 13) % 100) / 100 * 12,
      delay: ((i * 17 + 11) % 100) / 100 * 2,
    })), []);

  return (
    <section className="relative flex min-h-screen flex-col items-center justify-center px-6">

      {/* Teaser rocket silhouette */}
      <div
        className="absolute left-4 sm:left-8 bottom-20 sm:bottom-28 pointer-events-none animate-float"
        style={{
          opacity: teaserOpacity,
          animationDuration: "10s",
          width: "120px",
          height: "auto",
        }}
      >
        <RocketSVG />
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
            {typed2.length < INTRO_LINES[1].length && progress > 0.4 && (
              <span className="inline-block w-[2px] h-[1em] bg-cyan-300 ml-[2px] animate-cursor-blink" />
            )}
          </p>
        </div>
      </div>
    </section>
  );
}
