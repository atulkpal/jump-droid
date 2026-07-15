import { PLAY_STORE_URL, SOCIAL_LINKS, BETA_TESTING_URL } from "@/lib/constants";
import { MISSION_LOG, DOWNLOADS, BETA } from "@/app/data/site-content";
import { GooglePlayIcon, GitHubIcon, ItchIoIcon } from "@/app/components/PlatformIcons";

export default function MissionLog() {
  return (
    <section className="flex w-full items-center justify-center px-6 py-20 sm:py-28">
      <div className="mx-auto max-w-lg w-full">
        {/* Source code card */}
        <div className="rounded-xl border border-white/5 bg-white/[0.02] px-6 py-8 sm:px-8 mb-8">
          <div className="flex items-center justify-between mb-5">
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

          <p className="font-mono text-xs sm:text-sm leading-relaxed text-slate-500 mb-5">
            {MISSION_LOG.description}
          </p>

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

          <p className="text-center font-mono text-[10px] tracking-[0.1em] text-slate-600 mt-3">
            {MISSION_LOG.license} &middot; Free to use, modify, and distribute
          </p>
        </div>

        {/* Download buttons */}
        <h2 className="font-mono text-lg sm:text-xl font-bold tracking-[0.08em] text-white text-center uppercase mb-1">
          {DOWNLOADS.title}
        </h2>
        <p className="font-mono text-xs sm:text-sm text-slate-500 text-center mb-6">
          {DOWNLOADS.description}
        </p>

        <div className="space-y-3">
          <a
            href={PLAY_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="group flex w-full items-center justify-center gap-3 rounded-xl border px-5 py-4 transition-all"
            style={{
              borderColor: "#3DDC8466",
              backgroundColor: "#3DDC8418",
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = "#3DDC8430";
              e.currentTarget.style.borderColor = "#3DDC8499";
              e.currentTarget.style.boxShadow = "0 0 28px #3DDC8435";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = "#3DDC8418";
              e.currentTarget.style.borderColor = "#3DDC8466";
              e.currentTarget.style.boxShadow = "none";
            }}
          >
            <span className="w-7 h-7 flex-shrink-0"><GooglePlayIcon className="w-full h-full" /></span>
            <div className="text-left">
              <div className="font-mono text-sm sm:text-base font-bold tracking-[0.1em] uppercase" style={{ color: "#3DDC84" }}>
                Google Play
              </div>
              <div className="font-mono text-[10px] sm:text-[11px] text-slate-400 mt-0.5">
                Free &middot; 8 MB &middot; Android 8+
              </div>
            </div>
          </a>

          <div className="grid grid-cols-2 gap-3">
            <a
              href={SOCIAL_LINKS.github}
              target="_blank"
              rel="noopener noreferrer"
              className="group flex items-center justify-center gap-2.5 rounded-xl border px-4 py-4 transition-all"
              style={{
                borderColor: "#E0E0E066",
                backgroundColor: "#E0E0E018",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "#E0E0E030";
                e.currentTarget.style.borderColor = "#E0E0E099";
                e.currentTarget.style.boxShadow = "0 0 20px #E0E0E025";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "#E0E0E018";
                e.currentTarget.style.borderColor = "#E0E0E066";
                e.currentTarget.style.boxShadow = "none";
              }}
            >
              <span className="w-5 h-5 flex-shrink-0" style={{ color: "#E0E0E0" }}><GitHubIcon className="w-full h-full" /></span>
              <span className="font-mono text-xs sm:text-sm font-semibold tracking-[0.1em] uppercase" style={{ color: "#E0E0E0" }}>GitHub</span>
            </a>
            <a
              href={SOCIAL_LINKS.itchIo}
              target="_blank"
              rel="noopener noreferrer"
              className="group flex items-center justify-center gap-2.5 rounded-xl border px-4 py-4 transition-all"
              style={{
                borderColor: "#FA5C5C66",
                backgroundColor: "#FA5C5C18",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "#FA5C5C30";
                e.currentTarget.style.borderColor = "#FA5C5C99";
                e.currentTarget.style.boxShadow = "0 0 20px #FA5C5C25";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "#FA5C5C18";
                e.currentTarget.style.borderColor = "#FA5C5C66";
                e.currentTarget.style.boxShadow = "none";
              }}
            >
              <span className="w-5 h-5 flex-shrink-0"><ItchIoIcon className="w-full h-full" /></span>
              <span className="font-mono text-xs sm:text-sm font-semibold tracking-[0.1em] uppercase" style={{ color: "#FA5C5C" }}>itch.io</span>
            </a>
          </div>
        </div>

        {/* Beta CTA */}
        <div className="text-center mt-10 pt-8 border-t border-white/5">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-3">
            Join the Mission
          </p>
          <p className="font-mono text-xs sm:text-sm text-slate-500 mb-5 max-w-sm mx-auto">
            {BETA.description}
          </p>
          <a
            href={BETA_TESTING_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="group inline-flex items-center gap-2.5 rounded-full border border-amber-400/40 bg-amber-400/10 px-8 py-3.5 font-mono text-xs sm:text-sm tracking-[0.15em] text-amber-300/90 transition-all hover:bg-amber-400/20 hover:border-amber-400/60 hover:shadow-[0_0_24px_rgba(255,160,0,0.2)] uppercase"
          >
            <svg viewBox="0 0 24 24" className="w-4 h-4" fill="none" aria-hidden="true">
              <path d="M12 3v18M3 12h18" stroke="#FFA000" strokeWidth="2.5" strokeLinecap="round" />
              <circle cx="12" cy="12" r="9" stroke="#FFA000" strokeWidth="1.5" fill="none" />
            </svg>
            Join the Beta
          </a>
        </div>
      </div>
    </section>
  );
}
