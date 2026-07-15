"use client";

import { motion } from "framer-motion";
import { PLAY_STORE_URL, BETA_TESTING_URL, SOCIAL_LINKS } from "@/lib/constants";

export default function SignalArchive({ progress }: { progress: number }) {
  const reveal = Math.max(0, Math.min(1, (progress - 0.1) / 0.9));

  return (
    <motion.section
      className="relative w-full px-6 py-8 sm:py-12"
      initial={{ opacity: 0 }}
      animate={{ opacity: progress > 0 ? 1 : 0 }}
      transition={{ duration: 0.5 }}
    >
      <div
        className="mx-auto max-w-lg lg:max-w-2xl"
        style={{
          opacity: reveal,
          transform: `translateY(${(1 - reveal) * 24}px)`,
          transition: "opacity 0.6s ease-out, transform 0.6s ease-out",
        }}
      >
        <p className="font-mono text-[11px] tracking-[0.25em] text-cyan-400/60 uppercase mb-3">
          Transmission Complete
        </p>
        <h2 className="font-mono text-lg sm:text-xl font-bold tracking-[0.05em] text-white uppercase mb-3 leading-snug">
          The Expedition Awaits
        </h2>
        <p className="font-mono text-sm lg:text-base leading-relaxed text-slate-400 mb-8 max-w-lg">
          The signal fragments tell only part of the story. Download the full
          transmission and experience the complete ascent. Free. Open source.
          No account. No excuses.
        </p>

        {/* Primary CTA — Google Play */}
        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="group block w-full rounded-lg border border-cyan-400/30 bg-cyan-400/10 px-6 py-5 text-center transition-all hover:bg-cyan-400/20 hover:border-cyan-400/50 hover:shadow-[0_0_32px_rgba(0,229,255,0.2)]"
          style={{
            opacity: reveal,
            transition: `opacity 0.5s ease-out 0.1s, background-color 0.2s, border-color 0.2s, box-shadow 0.2s`,
          }}
        >
          <span className="font-mono text-base sm:text-lg font-bold tracking-[0.15em] text-white group-hover:text-cyan-100 transition-colors uppercase">
            Install on Google Play
          </span>
          <span className="block font-mono text-[11px] text-cyan-400/50 group-hover:text-cyan-400/70 mt-1 transition-colors">
            Free &middot; 8 MB &middot; Android 8+
          </span>
        </a>

        {/* Secondary row — Beta + links */}
        <div
          className="mt-4 grid grid-cols-2 gap-2"
          style={{
            opacity: reveal,
            transition: `opacity 0.5s ease-out 0.2s`,
          }}
        >
          <a
            href={BETA_TESTING_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="group block rounded-lg border border-amber-400/15 bg-amber-400/[0.04] px-4 py-4 text-center transition-all hover:bg-amber-400/10 hover:border-amber-400/30 hover:shadow-[0_0_20px_rgba(255,192,0,0.1)]"
          >
            <span className="font-mono text-sm font-bold tracking-[0.1em] text-amber-200/80 group-hover:text-amber-100 transition-colors uppercase">
              Join the Beta
            </span>
            <span className="block font-mono text-[10px] text-amber-400/40 mt-0.5 transition-colors">
              Early access deployment
            </span>
          </a>
          <a
            href={SOCIAL_LINKS.github}
            target="_blank"
            rel="noopener noreferrer"
            className="group block rounded-lg border border-white/5 bg-white/[0.015] px-4 py-4 text-center transition-all hover:border-white/10"
          >
            <span className="font-mono text-sm font-semibold text-white/60 group-hover:text-white/80 transition-colors">
              GitHub
            </span>
            <span className="block font-mono text-[10px] text-slate-700 mt-0.5">
              Source code
            </span>
          </a>
        </div>

        <div
          className="flex flex-wrap justify-center gap-4 mt-5"
          style={{
            opacity: reveal,
            transition: `opacity 0.5s ease-out 0.3s`,
          }}
        >
          <a
            href={SOCIAL_LINKS.itchIo}
            target="_blank"
            rel="noopener noreferrer"
            className="font-mono text-[11px] text-slate-600 hover:text-slate-400 transition-colors underline underline-offset-2"
          >
            itch.io
          </a>
          <a
            href={SOCIAL_LINKS.privacy}
            className="font-mono text-[11px] text-slate-600 hover:text-slate-400 transition-colors underline underline-offset-2"
          >
            Privacy
          </a>
        </div>
      </div>
    </motion.section>
  );
}
