"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { HERO } from "@/app/data/site-content";
import { SOCIAL_LINKS } from "@/lib/constants";
import MoonGlow from "@/app/components/MoonGlow";
import BetaTagline from "./BetaTagline";

interface Props {
  onPlayStoreClick: () => void;
  onBetaLanded?: () => void;
  onLandingReset?: () => void;
}

export default function HeroSignal({ onPlayStoreClick, onBetaLanded, onLandingReset }: Props) {
  const [sticky, setSticky] = useState(false);
  const [landing, setLanding] = useState(false);
  const [landed, setLanded] = useState(false);
  const ctaRef = useRef<HTMLDivElement>(null);
  const stickyElRef = useRef<HTMLDivElement>(null);
  const landingOffset = useRef(0);
  const callbacksRef = useRef({ onBetaLanded, onLandingReset });
  callbacksRef.current = { onBetaLanded, onLandingReset };

  useEffect(() => {
    const handleScroll = () => {
      const cta = ctaRef.current;
      if (!cta) return;

      const ctaRect = cta.getBoundingClientRect();
      const heroPast = ctaRect.top < 80 && ctaRect.bottom < 80;

      const landingEl = document.getElementById("beta-landing");
      const landingTop = landingEl ? landingEl.getBoundingClientRect().top : Infinity;

      if (landed) {
        if (!heroPast) {
          setLanded(false);
          setSticky(false);
          callbacksRef.current.onLandingReset?.();
        }
        return;
      }

      if (sticky && landingTop < window.innerHeight * 0.75 && !landing) {
        landingOffset.current = landingTop - 50;
        setLanding(true);
        return;
      }

      if (sticky && landingTop > window.innerHeight && landing) {
        setLanding(false);
        return;
      }

      if (landing && landingTop < 100) {
        setLanded(true);
        setLanding(false);
        return;
      }

      if (!landing) {
        setSticky(heroPast);
      }
    };

    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, [sticky, landing, landed]);

  useEffect(() => {
    if (!landed) return;

    const topEl = document.getElementById("download-section-end");
    const bottomEl = document.getElementById("made-with");
    const landingEl = document.getElementById("beta-landing");
    if (!landingEl) return;

    if (topEl && bottomEl) {
      const top = topEl.getBoundingClientRect().bottom + window.scrollY;
      const bottom = bottomEl.getBoundingClientRect().top + window.scrollY;
      const mid = (top + bottom) / 2;

      const betaBtn = landingEl.querySelector("a");
      const btnCenter = betaBtn
        ? betaBtn.getBoundingClientRect().top + betaBtn.offsetHeight / 2 + window.scrollY
        : landingEl.offsetTop + landingEl.offsetHeight / 2;

      window.scrollTo({ top: window.scrollY + btnCenter - mid, behavior: "auto" });
    } else {
      landingEl.scrollIntoView({ block: "center" });
    }

    callbacksRef.current.onBetaLanded?.();
  }, [landed]);

  const playStoreIcon = (
    <svg viewBox="0 0 24 24" className="w-4 h-4 flex-shrink-0" fill="none" aria-hidden="true">
      <rect x="0.5" y="0.5" width="23" height="23" rx="4.5" fill="#3DDC84" />
      <path d="M6.5 5v14l10.5-7z" fill="#fff" />
    </svg>
  );

  const githubIcon = (
    <svg viewBox="0 0 24 24" className="w-4 h-4" fill="currentColor">
      <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.83.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.577.688.48C19.138 20.161 22 16.418 22 12c0-5.523-4.477-10-10-10z" />
    </svg>
  );

  const desktopButtons = (inHero: boolean) => {
    const sideBtn = "inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-5 py-2.5 font-mono text-xs tracking-[0.2em] text-white/60 uppercase transition-all hover:border-white/20 hover:bg-white/10 hover:text-white/80";
    const betaBtn = "inline-flex items-center gap-2 rounded-full border border-cyan-400/40 bg-cyan-400/10 px-10 py-4 font-mono text-sm tracking-[0.2em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 hover:shadow-[0_0_28px_rgba(0,229,255,0.2)]";

    return (
      <>
        <button onClick={onPlayStoreClick} className={sideBtn}>
          {playStoreIcon}
          {HERO.cta}
        </button>
        <Link href="/beta-info" className={betaBtn}>
          {HERO.ctaBeta}
        </Link>
        <a href={`${SOCIAL_LINKS.github}/releases`} target="_blank" rel="noopener noreferrer" className={sideBtn}>
          {githubIcon}
          {HERO.ctaSecondary}
        </a>
      </>
    );
  };

  const mobileButtons = (inLanding = false) => {
    const iconBtn = "flex items-center justify-center w-12 h-12 rounded-full border border-white/10 bg-white/5 text-white/60 hover:border-white/20 hover:text-white/80 transition-all flex-shrink-0";
    const alphaBtn = "inline-flex items-center gap-2 rounded-full border border-cyan-400/40 bg-cyan-400/10 px-7 py-3.5 font-mono text-xs tracking-[0.2em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 hover:shadow-[0_0_28px_rgba(0,229,255,0.2)] flex-shrink-0";

    return (
      <>
        <button onClick={onPlayStoreClick} className={iconBtn} aria-label="Download on Play Store">
          {playStoreIcon}
        </button>
        <Link href="/beta-info" className={alphaBtn}>
          ⭐ Join Beta
        </Link>
        <a href={`${SOCIAL_LINKS.github}/releases`} target="_blank" rel="noopener noreferrer" className={iconBtn} aria-label="Download APK from GitHub">
          {githubIcon}
        </a>
      </>
    );
  };

  return (
    <>
      {/* Sticky CTA bar */}
      <div
        ref={stickyElRef}
        className={`fixed left-0 right-0 z-30 border-b border-white/5 bg-black/80 backdrop-blur-xl px-4 py-3 shadow-lg transition-all duration-700 ease-out ${
          landing
            ? "opacity-100"
            : sticky && !landed
            ? "opacity-100 translate-y-0"
            : "opacity-0 -translate-y-full pointer-events-none"
        }`}
        style={{
          top: landing ? `${landingOffset.current}px` : "50px",
        }}
      >
        <div className="mx-auto flex max-w-4xl items-center justify-center gap-4">
          <div className="hidden sm:flex items-center justify-center gap-4">
            <div
              className={`transition-all duration-500 ${
                landing || landed ? "opacity-0 scale-95 pointer-events-none w-0 overflow-hidden" : "opacity-100"
              }`}
            >
              <button onClick={onPlayStoreClick} className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-5 py-2.5 font-mono text-xs tracking-[0.2em] text-white/60 uppercase whitespace-nowrap transition-all hover:border-white/20 hover:bg-white/10 hover:text-white/80">
                {playStoreIcon}
                {HERO.cta}
              </button>
            </div>

            <Link
              href="/beta-info"
              className={`inline-flex items-center gap-2 rounded-full border border-cyan-400/40 bg-cyan-400/10 font-mono tracking-[0.2em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 hover:shadow-[0_0_28px_rgba(0,229,255,0.2)] ${
                landing || landed ? "px-10 py-4 text-sm" : "px-8 py-3 text-sm"
              }`}
            >
              {HERO.ctaBeta}
            </Link>

            <div
              className={`transition-all duration-500 ${
                landing || landed ? "opacity-0 scale-95 pointer-events-none w-0 overflow-hidden" : "opacity-100"
              }`}
            >
              <a href={`${SOCIAL_LINKS.github}/releases`} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-5 py-2.5 font-mono text-xs tracking-[0.2em] text-white/60 uppercase whitespace-nowrap transition-all hover:border-white/20 hover:bg-white/10 hover:text-white/80">
                {githubIcon}
                {HERO.ctaSecondary}
              </a>
            </div>
          </div>

          <div className="flex sm:hidden items-center justify-center gap-4">
            <div className={`transition-all duration-500 ${landing || landed ? "opacity-0 scale-95 w-0 overflow-hidden" : "opacity-100"}`}>
              <button onClick={onPlayStoreClick} className="flex items-center justify-center w-11 h-11 rounded-full border border-white/10 bg-white/5 text-white/60 hover:border-white/20 hover:text-white/80 transition-all flex-shrink-0" aria-label="Download on Play Store">
                {playStoreIcon}
              </button>
            </div>
            <Link href="/beta-info" className="inline-flex items-center gap-2 rounded-full border border-cyan-400/40 bg-cyan-400/10 px-7 py-3.5 font-mono text-xs tracking-[0.2em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 hover:shadow-[0_0_28px_rgba(0,229,255,0.2)] flex-shrink-0">
              ⭐ Join Beta
            </Link>
            <div className={`transition-all duration-500 ${landing || landed ? "opacity-0 scale-95 w-0 overflow-hidden" : "opacity-100"}`}>
              <a href={`${SOCIAL_LINKS.github}/releases`} target="_blank" rel="noopener noreferrer" className="flex items-center justify-center w-11 h-11 rounded-full border border-white/10 bg-white/5 text-white/60 hover:border-white/20 hover:text-white/80 transition-all flex-shrink-0" aria-label="Download APK from GitHub">
                {githubIcon}
              </a>
            </div>
          </div>
        </div>
      </div>

      <section className="relative flex min-h-screen w-full flex-col items-center justify-center overflow-hidden px-6 pt-20 pb-16 sm:pt-28 sm:pb-20">
        <MoonGlow />

        <p
          className="font-mono text-[10px] sm:text-xs tracking-[0.35em] text-cyan-400/50 uppercase mb-5 opacity-0 animate-fade-in-up"
          style={{ animationDelay: "0.3s", animationFillMode: "forwards" }}
        >
          {HERO.tagline}
        </p>

        <h1
          className="font-mono text-5xl sm:text-7xl md:text-8xl lg:text-9xl font-bold tracking-[-0.02em] text-white uppercase text-center leading-none mb-3 sm:mb-4 opacity-0 animate-fade-in-up"
          style={{ animationDelay: "0.6s", animationFillMode: "forwards" }}
        >
          {HERO.title}
        </h1>

        <p
          className="font-mono text-sm sm:text-base tracking-[0.1em] text-slate-400 text-center max-w-xl mb-8 opacity-0 animate-fade-in-up"
          style={{ animationDelay: "1s", animationFillMode: "forwards" }}
        >
          {HERO.subtitle}
        </p>

        {/* CTA sentinel wrapper — always visible on all sizes */}
        <div ref={ctaRef}>
          {/* Desktop CTA row (in hero) */}
          <div
            className={`hidden sm:flex flex-col items-center justify-center gap-3 opacity-0 animate-fade-in-up mb-10 ${
              sticky ? "opacity-0 pointer-events-none" : ""
            }`}
            style={{
              animationDelay: "1.4s",
              animationFillMode: "forwards",
            }}
          >
            <div className="flex items-center justify-center gap-4">
              {desktopButtons(true)}
            </div>
            <div className="flex flex-col items-center">
              <div className="w-1/2 border-t border-cyan-400/15 opacity-60" />
              <div className="py-1.5">
                <BetaTagline />
              </div>
              <div className="w-1/2 border-t border-cyan-400/15 opacity-60" />
            </div>
          </div>

          {/* Mobile CTA row (in hero) */}
          <div
            className={`flex sm:hidden flex-col items-center justify-center gap-3 opacity-0 animate-fade-in-up mb-10 ${
              sticky ? "opacity-0 pointer-events-none" : ""
            }`}
            style={{
              animationDelay: "1.4s",
              animationFillMode: "forwards",
            }}
          >
          <div className="flex items-center justify-center gap-4">
            {mobileButtons()}
          </div>
          <BetaTagline />
        </div>
      </div>

      {/* Description + features */}
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
    </>
  );
}
