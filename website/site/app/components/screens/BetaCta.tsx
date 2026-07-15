import { BETA_TESTING_URL } from "@/lib/constants";
import { BETA } from "@/app/data/site-content";

export default function BetaCta() {
  return (
    <section className="flex min-h-dvh w-full items-center justify-center px-6">
      <div className="mx-auto max-w-lg w-full text-center">
        <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-4">
          Join the Mission
        </p>
        <h2 className="font-mono text-lg sm:text-xl font-bold tracking-[0.1em] text-white uppercase mb-3">
          {BETA.title}
        </h2>
        <p className="font-mono text-xs sm:text-sm text-slate-500 mb-8 max-w-sm mx-auto">
          {BETA.description}
        </p>
        <a
          href={BETA_TESTING_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="group inline-flex items-center gap-2.5 rounded-full border border-amber-400/40 bg-amber-400/10 px-8 py-4 font-mono text-xs sm:text-sm tracking-[0.15em] text-amber-300/90 transition-all hover:bg-amber-400/20 hover:border-amber-400/60 hover:shadow-[0_0_24px_rgba(255,160,0,0.2)] uppercase"
        >
          <svg viewBox="0 0 24 24" className="w-4 h-4" fill="none" aria-hidden="true">
            <path d="M12 3v18M3 12h18" stroke="#FFA000" strokeWidth="2.5" strokeLinecap="round" />
            <circle cx="12" cy="12" r="9" stroke="#FFA000" strokeWidth="1.5" fill="none" />
          </svg>
          Join the Beta
        </a>
      </div>
    </section>
  );
}
