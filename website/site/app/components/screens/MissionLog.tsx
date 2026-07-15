import { MISSION_LOG } from "@/app/data/site-content";
import { SOCIAL_LINKS } from "@/lib/constants";

export default function MissionLog() {
  return (
    <section className="flex min-h-dvh w-full items-center justify-center px-6">
      <div className="mx-auto max-w-lg w-full">
        {/* Header */}
        <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-8 text-center">
          {MISSION_LOG.title}
        </p>

        {/* Status card */}
        <div className="rounded-xl border border-white/5 bg-white/[0.02] px-6 py-8 sm:px-8">
          <div className="flex items-center justify-between mb-6">
            <h2 className="font-mono text-sm sm:text-base font-bold tracking-[0.15em] text-white uppercase">
              {MISSION_LOG.heading}
            </h2>
            <span className="inline-flex items-center gap-1.5 rounded-full border border-green-500/30 bg-green-500/10 px-3 py-1">
              <span className="h-1.5 w-1.5 rounded-full bg-green-400" />
              <span className="font-mono text-[10px] tracking-[0.15em] text-green-400 font-semibold uppercase">
                {MISSION_LOG.status}
              </span>
            </span>
          </div>

          <p className="font-mono text-xs sm:text-sm leading-relaxed text-slate-500 mb-6">
            {MISSION_LOG.description}
          </p>

          <div className="space-y-2">
            <a
              href={SOCIAL_LINKS.github}
              target="_blank"
              rel="noopener noreferrer"
              className="group flex w-full items-center justify-center gap-2 rounded-lg border border-white/10 bg-white/5 px-5 py-3 font-mono text-xs sm:text-sm tracking-[0.15em] text-white/70 transition-all hover:border-white/20 hover:bg-white/10 hover:text-white uppercase"
            >
              <svg viewBox="0 0 24 24" className="w-4 h-4" fill="currentColor">
                <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.83.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.577.688.48C19.138 20.161 22 16.418 22 12c0-5.523-4.477-10-10-10z" />
              </svg>
              View on GitHub
            </a>

            <p className="text-center font-mono text-[10px] tracking-[0.1em] text-slate-600">
              {MISSION_LOG.license} &middot; Free to use, modify, and distribute
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
