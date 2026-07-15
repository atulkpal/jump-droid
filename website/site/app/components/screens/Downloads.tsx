import { PLAY_STORE_URL, SOCIAL_LINKS } from "@/lib/constants";
import { DOWNLOADS } from "@/app/data/site-content";
import { GooglePlayIcon, GitHubIcon, ItchIoIcon } from "@/app/components/PlatformIcons";

export default function Downloads() {
  return (
    <section className="flex w-full items-center justify-center px-6 py-20 sm:py-28">
      <div className="mx-auto max-w-lg w-full">
        <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-2 text-center">
          Final Transmission
        </p>
        <h2 className="font-mono text-lg sm:text-xl font-bold tracking-[0.1em] text-white text-center uppercase mb-2">
          {DOWNLOADS.title}
        </h2>
        <p className="font-mono text-xs sm:text-sm text-slate-500 text-center mb-8">
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
                Free &middot; Android 8+
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

        <p className="text-center font-mono text-[10px] tracking-[0.1em] text-slate-600 mt-6">
          {DOWNLOADS.version} &middot; Open Source (MIT)
        </p>
      </div>
    </section>
  );
}
