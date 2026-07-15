"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

const links = [
  { href: "#hero", label: "Surface" },
  { href: "#features", label: "Features" },
  { href: "#screenshots", label: "Gallery" },
  { href: "#ascent", label: "Zones" },
  { href: "#hangar", label: "Rockets" },
  { href: "#simulation", label: "Launchpad" },
  { href: "#download", label: "Download" },
];

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

  const closeMenu = () => setMenuOpen(false);

  return (
    <nav
      className="fixed left-1/2 top-6 z-30 w-[min(90vw,960px)] -translate-x-1/2 rounded-full border border-white/10 bg-black/70 px-5 py-3 shadow-[0_0_60px_rgba(0,229,255,0.15)] backdrop-blur-xl"
      aria-label="Main navigation"
    >
      <div className="flex items-center justify-between gap-3">
        {/* Mobile hamburger */}
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="flex md:hidden rounded-full p-2 text-cyan-200 hover:bg-cyan-500/10 transition"
          aria-label={menuOpen ? "Close menu" : "Open menu"}
          aria-expanded={menuOpen}
        >
          <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            {menuOpen ? (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>

        {/* Desktop nav links */}
        <ul className="hidden md:flex flex-wrap items-center justify-center gap-1 sm:gap-2 text-[10px] sm:text-xs uppercase tracking-[0.25em] text-cyan-200/90">
          {links.map((link) => (
            <li key={link.href}>
              <a
                href={link.href}
                className="rounded-full px-2.5 py-1.5 transition hover:bg-cyan-500/10 hover:text-cyan-100"
              >
                {link.label}
              </a>
            </li>
          ))}
          <li>
            <Link
              href="/beta"
              className="rounded-full px-2.5 py-1.5 transition hover:bg-cyan-500/10 hover:text-cyan-100"
            >
              Beta
            </Link>
          </li>
        </ul>

        <button
          onClick={toggleCrt}
          className={`rounded-full px-3 py-1 text-[9px] font-bold uppercase tracking-widest transition cursor-pointer border ${
            crt
              ? "bg-cyan-400 border-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,229,255,0.3)]"
              : "border-cyan-300/30 text-cyan-300 hover:border-cyan-300/80"
          }`}
          aria-label={`CRT effect: ${crt ? "on" : "off"}`}
        >
          CRT: {crt ? "ON" : "OFF"}
        </button>
      </div>

      {/* Mobile dropdown */}
      {menuOpen && (
        <div className="mt-3 md:hidden rounded-2xl border border-white/10 bg-black/90 p-3">
          <ul className="flex flex-col gap-1 text-xs uppercase tracking-[0.25em] text-cyan-200/90">
            {links.map((link) => (
              <li key={link.href}>
                <a
                  href={link.href}
                  onClick={closeMenu}
                  className="block rounded-full px-4 py-2 transition hover:bg-cyan-500/10 hover:text-cyan-100"
                >
                  {link.label}
                </a>
              </li>
            ))}
            <li>
              <Link
                href="/beta"
                onClick={closeMenu}
                className="block rounded-full px-4 py-2 transition hover:bg-cyan-500/10 hover:text-cyan-100"
              >
                Beta
              </Link>
            </li>
          </ul>
        </div>
      )}
    </nav>
  );
}
