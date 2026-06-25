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
    <nav className="fixed left-1/2 top-6 z-30 w-[min(90vw,960px)] -translate-x-1/2 rounded-full border border-white/10 bg-black/70 px-5 py-3 shadow-[0_0_60px_rgba(0,229,255,0.15)] backdrop-blur-xl flex flex-col md:flex-row justify-between items-center gap-3">
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
  );
}
