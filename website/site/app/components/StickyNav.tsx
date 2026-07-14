"use client";

import { useState, useEffect } from "react";

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
      className="fixed left-1/2 top-4 z-50 w-[min(90vw,960px)] -translate-x-1/2 rounded-full border border-white/10 bg-black/70 px-4 py-2.5 shadow-[0_0_60px_rgba(0,229,255,0.1)] backdrop-blur-xl md:top-6 md:px-5 md:py-3"
      aria-label="Main navigation"
    >
      <div className="flex items-center justify-between">
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="flex md:hidden rounded-full p-3 text-cyan-200 hover:bg-cyan-500/10 transition min-h-[44px] min-w-[44px] items-center justify-center"
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

        <p className="hidden md:block text-xs font-bold uppercase tracking-[0.25em] text-cyan-300">
          Jump Droid
        </p>

        <div className="hidden md:flex items-center gap-1 text-[11px] uppercase tracking-[0.25em] text-cyan-200/90">
          {["Platforms", "Threats", "Fleet", "Archive"].map((label) => (
            <a
              key={label}
              href={`#${label.toLowerCase() === "fleet" ? "rockets" : label.toLowerCase()}`}
              className="rounded-full px-3 py-1.5 transition hover:bg-cyan-500/10 hover:text-cyan-100"
            >
              {label}
            </a>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={toggleCrt}
            className={`rounded-full px-4 py-2 text-xs font-bold uppercase tracking-widest transition cursor-pointer border min-h-[44px] ${
              crt
                ? "bg-cyan-400 border-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,229,255,0.3)]"
                : "border-cyan-300/30 text-cyan-300 hover:border-cyan-300/80"
            }`}
            aria-label={`CRT effect: ${crt ? "on" : "off"}`}
          >
            CRT: {crt ? "ON" : "OFF"}
          </button>
        </div>
      </div>

      {menuOpen && (
        <div className="mt-3 md:hidden rounded-2xl border border-white/10 bg-black/90 p-3">
          <ul className="flex flex-col gap-1 text-sm uppercase tracking-[0.25em] text-cyan-200/90">
            {["Platforms", "Threats", "Fleet", "Archive"].map((label) => (
              <li key={label}>
                <a
                  href={`#${label.toLowerCase() === "fleet" ? "rockets" : label.toLowerCase()}`}
                  onClick={() => setMenuOpen(false)}
                  className="block rounded-full px-4 py-3 transition hover:bg-cyan-500/10 hover:text-cyan-100"
                >
                  {label}
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </nav>
  );
}
