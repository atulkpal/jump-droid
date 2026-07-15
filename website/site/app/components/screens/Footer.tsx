import { PLAY_STORE_URL, SOCIAL_LINKS } from "@/lib/constants";
import { FOOTER } from "@/app/data/site-content";
import { GitHubIcon, ItchIoIcon } from "@/app/components/PlatformIcons";

export default function FooterSection() {
  return (
    <footer className="w-full border-t border-white/5 px-6 py-12 sm:py-16">
      <div className="mx-auto max-w-lg text-center">
        <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-4">
          {FOOTER.tagline}
        </p>
        <p className="font-mono text-xs sm:text-sm text-slate-500 leading-relaxed mb-8 max-w-sm mx-auto">
          {FOOTER.description}
        </p>

        <div className="flex flex-wrap items-center justify-center gap-3 mb-8">
          <a
            href={PLAY_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 rounded-lg border border-white/5 bg-white/[0.02] px-4 py-2 font-mono text-[11px] tracking-[0.1em] text-slate-500 transition-all hover:border-white/10 hover:text-slate-300 uppercase"
          >
            <svg viewBox="0 0 24 24" className="w-3.5 h-3.5 flex-shrink-0" fill="none" aria-hidden="true">
              <rect x="0.5" y="0.5" width="23" height="23" rx="4.5" fill="#3DDC84" />
              <path d="M6.5 5v14l10.5-7z" fill="#fff" />
            </svg>
            Google Play
          </a>
          <a
            href={SOCIAL_LINKS.github}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 rounded-lg border border-white/5 bg-white/[0.02] px-4 py-2 font-mono text-[11px] tracking-[0.1em] text-slate-500 transition-all hover:border-white/10 hover:text-slate-300 uppercase"
          >
            <span className="w-3.5 h-3.5 flex-shrink-0"><GitHubIcon className="w-full h-full" /></span>
            GitHub
          </a>
          <a
            href={SOCIAL_LINKS.itchIo}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 rounded-lg border border-white/5 bg-white/[0.02] px-4 py-2 font-mono text-[11px] tracking-[0.1em] text-slate-500 transition-all hover:border-white/10 hover:text-slate-300 uppercase"
          >
            <span className="w-3.5 h-3.5 flex-shrink-0"><ItchIoIcon className="w-full h-full" /></span>
            itch.io
          </a>
          <a
            href={SOCIAL_LINKS.privacy}
            className="inline-flex items-center gap-1.5 rounded-lg border border-white/5 bg-white/[0.02] px-4 py-2 font-mono text-[11px] tracking-[0.1em] text-slate-500 transition-all hover:border-white/10 hover:text-slate-300 uppercase"
          >
            Privacy
          </a>
        </div>

        <p className="font-mono text-[10px] tracking-[0.1em] text-slate-700">
          &copy; {new Date().getFullYear()} Ashwath AI &middot; Jump Droid &middot; Open Source (MIT)
        </p>
      </div>
    </footer>
  );
}
