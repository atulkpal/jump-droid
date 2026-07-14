import { PLAY_STORE_URL } from "@/lib/constants";

export default function PortalSection() {
  return (
    <section id="portal" className="min-h-dvh flex flex-col items-center justify-center px-6">
      <div className="flex flex-col items-center gap-6 text-center">
        <p className="text-xs font-bold uppercase tracking-[0.35em] text-slate-500">
          Jump Droid
        </p>

        <h1 className="max-w-3xl text-5xl font-black leading-[1.05] tracking-tight text-white md:text-7xl">
          Ascend Beyond the Atmosphere
        </h1>

        <p className="text-base text-slate-500">
          Touch to thrust. Manage fuel, heat, and shield. Face the void.
        </p>

        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="mt-2 inline-flex h-14 items-center justify-center rounded-full bg-cyan-400 px-10 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 md:h-16 md:px-12 md:text-base"
        >
          Download on Google Play
        </a>
      </div>
    </section>
  );
}
