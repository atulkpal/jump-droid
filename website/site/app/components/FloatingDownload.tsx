"use client";

import { useState, useEffect } from "react";
import { PLAY_STORE_URL } from "@/lib/constants";

export default function FloatingDownload() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const onScroll = () => {
      setVisible(window.scrollY > window.innerHeight * 0.5);
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <div
      className={`fixed bottom-6 left-1/2 -translate-x-1/2 z-40 transition-all duration-500 ${
        visible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-4 pointer-events-none"
      }`}
    >
      <a
        href={PLAY_STORE_URL}
        target="_blank"
        rel="noopener noreferrer"
        className="inline-flex h-12 items-center justify-center rounded-full bg-cyan-400 px-6 text-xs font-black uppercase tracking-[0.2em] text-slate-950 shadow-[0_0_30px_rgba(0,229,255,0.3)] transition hover:bg-cyan-300 md:h-14 md:px-8 md:text-sm"
      >
        Download on Google Play
      </a>
    </div>
  );
}
