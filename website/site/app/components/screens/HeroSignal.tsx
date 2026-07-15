"use client";

import { HERO } from "@/app/data/site-content";
import { PLAY_STORE_URL, SOCIAL_LINKS } from "@/lib/constants";
import MoonGlow from "@/app/components/MoonGlow";

export default function HeroSignal() {
  return (
    <section className="relative flex min-h-screen w-full flex-col items-center justify-center overflow-hidden px-6 pt-20 pb-16 sm:pt-28 sm:pb-20">
      <MoonGlow />

      {/* Tagline */}
      <p
        className="font-mono text-[10px] sm:text-xs tracking-[0.35em] text-cyan-400/50 uppercase mb-5 opacity-0 animate-fade-in-up"
        style={{ animationDelay: "0.3s", animationFillMode: "forwards" }}
      >
        {HERO.tagline}
      </p>

      {/* Main title — no typewriter, clean fade */}
      <h1
        className="font-mono text-5xl sm:text-7xl md:text-8xl lg:text-9xl font-bold tracking-[-0.02em] text-white uppercase text-center leading-none mb-3 sm:mb-4 opacity-0 animate-fade-in-up"
        style={{ animationDelay: "0.6s", animationFillMode: "forwards" }}
      >
        {HERO.title}
      </h1>

      {/* Subtitle */}
      <p
        className="font-mono text-sm sm:text-base tracking-[0.1em] text-slate-400 text-center max-w-xl mb-5 opacity-0 animate-fade-in-up"
        style={{ animationDelay: "1s", animationFillMode: "forwards" }}
      >
        {HERO.subtitle}
      </p>

      {/* CTAs */}
      <div
        className="flex flex-col sm:flex-row items-center justify-center gap-3 mb-12 opacity-0 animate-fade-in-up"
        style={{ animationDelay: "1.4s", animationFillMode: "forwards" }}
      >
        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="group inline-flex items-center gap-2 rounded-full border border-cyan-400/40 bg-cyan-400/10 px-7 py-3 font-mono text-xs sm:text-sm tracking-[0.2em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 hover:shadow-[0_0_28px_rgba(0,229,255,0.2)]"
        >
          <svg viewBox="0 0 24 24" className="w-4 h-4 flex-shrink-0" fill="none" aria-hidden="true">
            <rect x="0.5" y="0.5" width="23" height="23" rx="4.5" fill="#3DDC84" />
            <path d="M6.5 5v14l10.5-7z" fill="#fff" />
          </svg>
          {HERO.cta}
        </a>
        <a
          href="https://github.com/atulkpal/jump-droid/releases"
          target="_blank"
          rel="noopener noreferrer"
          className="group inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-7 py-3 font-mono text-xs sm:text-sm tracking-[0.2em] text-white/60 uppercase transition-all hover:border-white/20 hover:bg-white/10 hover:text-white/80"
        >
          <svg viewBox="0 0 24 24" className="w-4 h-4" fill="currentColor">
            <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.83.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.577.688.48C19.138 20.161 22 16.418 22 12c0-5.523-4.477-10-10-10z" />
          </svg>
          {HERO.ctaSecondary}
        </a>
      </div>

      {/* Description + features — side by side on desktop */}
      <div
        className="w-full max-w-3xl mx-auto opacity-0 animate-fade-in-up"
        style={{ animationDelay: "1.8s", animationFillMode: "forwards" }}
      >
        <p className="font-mono text-xs sm:text-sm leading-relaxed text-slate-500 text-center max-w-xl mx-auto mb-8">
          {HERO.description}
        </p>

        <div className="grid sm:grid-cols-3 gap-3">
          {HERO.features.map((f) => (
            <div
              key={f.label}
              className="rounded-xl border border-white/5 bg-white/[0.02] px-4 py-3 sm:px-5 sm:py-4"
            >
              <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase font-bold mb-1">
                {f.label}
              </p>
              <p className="font-mono text-[11px] leading-relaxed text-slate-500">
                {f.value}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
