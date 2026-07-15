"use client";

import { useEffect, useState, useRef } from "react";
import { HERO } from "@/app/data/site-content";

export default function HeroSignal({ onBegin }: { onBegin: () => void }) {
  const [showCta, setShowCta] = useState(false);
  const autoRef = useRef(false);

  useEffect(() => {
    if (autoRef.current) return;
    autoRef.current = true;
    const timer = setTimeout(() => setShowCta(true), 2800);
    return () => clearTimeout(timer);
  }, []);

  return (
    <section className="relative z-10 flex h-dvh w-full flex-col items-center justify-center px-6">
      {/* Altitude indicator */}
      <div className="absolute top-8 left-6 right-6 flex justify-between font-mono text-[10px] tracking-[0.2em] text-white/20 uppercase">
        <span>ALT: 0m</span>
        <span className="inline-flex items-center gap-1.5">
          <span className="h-1.5 w-1.5 rounded-full bg-green-400 animate-pulse" />
          SIGNAL ACQUIRED
        </span>
      </div>

      {/* Title */}
      <div className="flex flex-col items-center gap-4">
        <h1 className="font-mono text-4xl sm:text-5xl md:text-6xl font-bold tracking-[0.15em] text-white uppercase">
          <span
            className="inline-block overflow-hidden whitespace-nowrap border-r-2 border-cyan-400/70 animate-typewriter"
            style={{
              animation: "typewriter 1.8s steps(11) forwards, blink-caret 0.8s step-end 4",
              maxWidth: "fit-content",
            }}
          >
            {HERO.title}
          </span>
        </h1>

        {/* Subtitle line */}
        <p
          className="font-mono text-xs sm:text-sm tracking-[0.25em] text-cyan-400/50 opacity-0 animate-fade-in-up"
          style={{ animationDelay: "2s", animationFillMode: "forwards" }}
        >
          A FREE OPEN-SOURCE ASCENT
        </p>
      </div>

      {/* CTA */}
      <div
        className={`absolute bottom-16 sm:bottom-20 transition-all duration-1000 ${
          showCta ? "opacity-100 translate-y-0" : "opacity-0 translate-y-4"
        }`}
      >
        <button
          onClick={onBegin}
          className="group relative inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-8 py-3 font-mono text-xs sm:text-sm tracking-[0.2em] text-white/70 transition-all hover:border-cyan-400/30 hover:bg-cyan-400/5 hover:text-cyan-300 uppercase"
        >
          {HERO.cta}
          <svg
            className="w-3.5 h-3.5 transition-transform group-hover:translate-y-1"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M12 5v14M5 12l7 7 7-7" />
          </svg>
        </button>
      </div>
    </section>
  );
}
