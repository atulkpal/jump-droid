"use client";

import { useState, useEffect } from "react";
import { PLAY_STORE_URL } from "@/lib/constants";

export default function StickyNav() {
  const [crt, setCrt] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    const active = localStorage.getItem("crt-protocol") === "enabled";
    setCrt(active);
    document.documentElement.classList.toggle("crt-active", active);
  }, []);

  const toggleCrt = () => {
    const next = !crt;
    setCrt(next);
    document.documentElement.classList.toggle("crt-active", next);
    localStorage.setItem("crt-protocol", next ? "enabled" : "disabled");
  };

  return (
    <nav
      className="fixed left-1/2 top-4 z-50 w-[min(90vw,960px)] -translate-x-1/2 rounded-full border border-white/10 bg-black/70 px-4 py-2.5 shadow-lg backdrop-blur-xl md:top-6 md:px-5 md:py-3"
      aria-label="Main navigation"
    >
      <div className="flex items-center justify-between">
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="flex md:hidden rounded-full p-3 text-slate-400 hover:bg-white/5 transition min-h-[44px] min-w-[44px] items-center justify-center"
          aria-label={menuOpen ? "Close menu" : "Open menu"}
          aria-expanded={menuOpen}
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            {menuOpen ? (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>

        <p className="text-xs font-bold uppercase tracking-[0.25em] text-slate-400">
          Jump Droid
        </p>

        <div className="flex items-center gap-2">
          <a
            href={PLAY_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="hidden md:inline-flex h-9 items-center rounded-full bg-cyan-400 px-5 text-[10px] font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300"
          >
            Download
          </a>
          <button
            onClick={toggleCrt}
            className={`rounded-full px-3 py-1.5 text-[10px] font-bold uppercase tracking-widest transition cursor-pointer border min-h-[36px] ${
              crt
                ? "bg-cyan-400 border-cyan-400 text-slate-950"
                : "border-white/10 text-slate-500 hover:border-white/30"
            }`}
            aria-label={`CRT effect: ${crt ? "on" : "off"}`}
          >
            CRT
          </button>
        </div>
      </div>

      {menuOpen && (
        <div className="mt-3 md:hidden rounded-2xl border border-white/10 bg-black/90 p-3">
          <a
            href={PLAY_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="block w-full text-center rounded-full bg-cyan-400 py-3 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300"
          >
            Download on Google Play
          </a>
        </div>
      )}
    </nav>
  );
}
