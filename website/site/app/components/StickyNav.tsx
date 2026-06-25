"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

const links = [
  { href: "#hero", label: "Surface" },
  { href: "#ascent", label: "Zones" },
  { href: "#hangar", label: "Rockets" },
  { href: "#simulation", label: "Launchpad" },
  { href: "#archive", label: "Codex" },
  { href: "#mission-control", label: "Mission" },
];

export default function StickyNav() {
  const [crt, setCrt] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    const active = localStorage.getItem("crt-protocol") === "enabled";
    setCrt(active);
    if (active) {
      document.documentElement.classList.add("crt-active");
    } else {
      document.documentElement.classList.remove("crt-active");
    }
  }, []);

  const toggleCrt = () => {
    const next = !crt;
    setCrt(next);
    if (next) {
      document.documentElement.classList.add("crt-active");
      localStorage.setItem("crt-protocol", "enabled");
    } else {
      document.documentElement.classList.remove("crt-active");
      localStorage.setItem("crt-protocol", "disabled");
    }
  };

  return (
    <>
      {/* Mobile Menu Trigger Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed right-4 top-4 z-40 flex h-10 w-10 items-center justify-center rounded-full border border-cyan-400/20 bg-black/80 text-cyan-400 shadow-[0_0_20px_rgba(0,229,255,0.15)] backdrop-blur-md md:hidden cursor-pointer hover:border-cyan-400/50 transition-all duration-300"
        aria-label="Toggle navigation menu"
      >
        {isOpen ? (
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        ) : (
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        )}
      </button>

      {/* Mobile Fullscreen Menu Overlay */}
      {isOpen && (
        <div className="fixed inset-0 z-30 flex flex-col items-center justify-center bg-black/95 backdrop-blur-lg md:hidden p-6 gap-8 animate-in fade-in zoom-in-95 duration-200">
          <div className="text-center space-y-1">
            <span className="text-[10px] uppercase tracking-[0.45em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3.5 py-1 rounded-full border border-cyan-400/20">
              Navigation Protocol
            </span>
            <h2 className="text-xl font-black text-white tracking-widest uppercase mt-4">Jump Droid</h2>
          </div>

          <ul className="flex flex-col items-center gap-6 text-sm uppercase tracking-[0.25em] text-cyan-200/90 font-bold">
            {links.map((link) => (
              <li key={link.href}>
                <Link
                  href={link.href}
                  onClick={() => setIsOpen(false)}
                  className="rounded-full px-6 py-2 transition hover:bg-cyan-500/10 hover:text-cyan-100 block text-center"
                >
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>

          <div className="border-t border-white/10 w-24 my-2" />

          <button
            onClick={toggleCrt}
            className={`rounded-full px-5 py-2 text-[10px] font-bold uppercase tracking-widest transition cursor-pointer border ${
              crt 
                ? "bg-cyan-400 border-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,229,255,0.3)]" 
                : "border-cyan-300/30 text-cyan-300 hover:border-cyan-300/80"
            }`}
          >
            CRT: {crt ? "ON" : "OFF"}
          </button>
        </div>
      )}

      {/* Desktop Pill Navigation Bar */}
      <nav className="fixed left-1/2 top-6 z-30 hidden md:flex w-[min(90vw,960px)] -translate-x-1/2 rounded-full border border-white/10 bg-black/70 px-5 py-3 shadow-[0_0_60px_rgba(0,229,255,0.15)] backdrop-blur-xl justify-between items-center gap-3">
        <ul className="flex flex-wrap items-center justify-center gap-1 sm:gap-2 text-[10px] sm:text-xs uppercase tracking-[0.25em] text-cyan-200/90">
          {links.map((link) => (
            <li key={link.href}>
              <Link
                href={link.href}
                className="rounded-full px-2.5 py-1.5 transition hover:bg-cyan-500/10 hover:text-cyan-100"
              >
                {link.label}
              </Link>
            </li>
          ))}
        </ul>
        <button
          onClick={toggleCrt}
          className={`rounded-full px-3 py-1 text-[9px] font-bold uppercase tracking-widest transition cursor-pointer border ${
            crt 
              ? "bg-cyan-400 border-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,229,255,0.3)]" 
              : "border-cyan-300/30 text-cyan-300 hover:border-cyan-300/80"
          }`}
        >
          CRT: {crt ? "ON" : "OFF"}
        </button>
      </nav>
    </>
  );
}
