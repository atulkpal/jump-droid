"use client";

import { PLAY_STORE_URL, BETA_TESTING_URL } from "@/lib/constants";

export default function PortalSection() {
  return (
    <section id="portal" className="relative min-h-dvh flex flex-col items-center justify-center px-6">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_30%,rgba(0,229,255,0.1),transparent_60%)]" />

      <div className="relative flex flex-col items-center gap-6 text-center">
        <p className="text-xs font-bold uppercase tracking-[0.35em] text-cyan-300">
          Jump Droid
        </p>

        <h1 className="max-w-3xl text-5xl font-black leading-[1.05] tracking-tight text-white md:text-7xl">
          Ascend Beyond the Atmosphere
        </h1>

        <p className="max-w-md text-base leading-relaxed text-slate-400">
          Touch to thrust. Manage fuel, heat, and shield. Face the void.
        </p>

        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="mt-4 inline-flex h-14 items-center justify-center rounded-full bg-cyan-400 px-10 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 hover:shadow-[0_0_40px_rgba(0,229,255,0.4)] md:h-16 md:px-12 md:text-base"
        >
          Download on Google Play
        </a>

        <a
          href={BETA_TESTING_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="text-sm font-semibold uppercase tracking-[0.15em] text-slate-500 transition hover:text-cyan-300"
        >
          Join the Beta &rarr;
        </a>
      </div>
    </section>
  );
}
