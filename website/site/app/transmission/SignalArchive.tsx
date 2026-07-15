"use client";

import { motion } from "framer-motion";
import { PLAY_STORE_URL, BETA_TESTING_URL, SOCIAL_LINKS } from "@/lib/constants";
import { GooglePlayIcon, GitHubIcon, ItchIoIcon } from "@/app/components/PlatformIcons";

function CtaButton({
  href,
  icon,
  label,
  sublabel,
  color,
  large,
}: {
  href: string;
  icon: React.ReactNode;
  label: string;
  sublabel?: string;
  color: string;
  large?: boolean;
}) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="group block w-full rounded-lg border px-4 py-4 text-center transition-all"
      style={{
        borderColor: `${color}66`,
        backgroundColor: `${color}18`,
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.backgroundColor = `${color}30`;
        e.currentTarget.style.borderColor = `${color}99`;
        e.currentTarget.style.boxShadow = `0 0 28px ${color}35`;
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.backgroundColor = `${color}18`;
        e.currentTarget.style.borderColor = `${color}66`;
        e.currentTarget.style.boxShadow = "none";
      }}
    >
      <div className="flex items-center justify-center gap-2.5">
        <span className={`flex-shrink-0 ${large ? "w-7 h-7" : "w-5 h-5"}`} style={{ color }}>{icon}</span>
        <div className="text-left">
          <div
            className={`font-mono font-bold tracking-[0.1em] uppercase transition-colors ${
              large ? "text-base sm:text-lg" : "text-sm"
            }`}
            style={{ color }}
          >
            {label}
          </div>
          {sublabel && (
            <div className="font-mono text-[10px] sm:text-[11px] text-slate-400 mt-0.5 transition-colors">
              {sublabel}
            </div>
          )}
        </div>
      </div>
    </a>
  );
}

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

        {/* Google Play — primary */}
        <div style={{ opacity: reveal, transition: "opacity 0.5s ease-out 0.1s" }}>
          <CtaButton
            href={PLAY_STORE_URL}
            icon={<GooglePlayIcon className="w-full h-full" />}
            label="Install from Google Play"
            sublabel="Free &middot; Android 8+"
            color="#3DDC84"
            large
          />
        </div>

        {/* Secondary row: GitHub | itch.io | Beta */}
        <div
          className="mt-3 grid grid-cols-3 gap-2"
          style={{ opacity: reveal, transition: "opacity 0.5s ease-out 0.2s" }}
        >
          <CtaButton
            href={SOCIAL_LINKS.github}
            icon={<GitHubIcon className="w-full h-full" />}
            label="GitHub"
            sublabel="View source"
            color="#E0E0E0"
          />
          <CtaButton
            href={SOCIAL_LINKS.itchIo}
            icon={<ItchIoIcon className="w-full h-full" />}
            label="itch.io"
            sublabel="Download"
            color="#FA5C5C"
          />
          <CtaButton
            href={BETA_TESTING_URL}
            icon={
              <svg viewBox="0 0 24 24" className="w-full h-full" fill="none" aria-hidden="true">
                <path d="M12 3v18M3 12h18" stroke="#FFA000" strokeWidth="2.5" strokeLinecap="round" />
                <circle cx="12" cy="12" r="9" stroke="#FFA000" strokeWidth="1.5" fill="none" />
              </svg>
            }
            label="Beta"
            sublabel="Early access"
            color="#FFA000"
          />
        </div>

        {/* Privacy */}
        <div
          className="flex justify-center mt-5"
          style={{ opacity: reveal, transition: "opacity 0.5s ease-out 0.3s" }}
        >
          <a
            href={SOCIAL_LINKS.privacy}
            className="font-mono text-[11px] text-slate-500 hover:text-slate-300 transition-colors underline underline-offset-2"
          >
            Privacy
          </a>
        </div>
      </div>
    </motion.section>
  );
}
