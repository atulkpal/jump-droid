"use client";

import { useState, useEffect } from "react";
import { PLAY_STORE_URL, BETA_TESTING_URL, SOCIAL_LINKS } from "@/lib/constants";
import { GooglePlayIcon, GitHubIcon, ItchIoIcon } from "@/app/components/PlatformIcons";

function PlatformBtn({
  href,
  icon,
  label,
  color,
}: {
  href: string;
  icon: React.ReactNode;
  label: string;
  color: string;
}) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="group inline-flex items-center gap-1.5 rounded-md border px-2.5 py-2 sm:px-3.5 sm:py-2 transition-all"
      style={{
        borderColor: color,
        backgroundColor: `${color}22`,
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.backgroundColor = `${color}38`;
        e.currentTarget.style.boxShadow = `0 0 16px ${color}50`;
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.backgroundColor = `${color}22`;
        e.currentTarget.style.boxShadow = "none";
      }}
    >
      <span className="w-4 h-4 sm:w-4 sm:h-4 flex-shrink-0" style={{ color }}>{icon}</span>
      <span
        className="hidden sm:inline font-mono text-[11px] font-semibold tracking-[0.08em] uppercase whitespace-nowrap"
        style={{ color }}
      >
        {label}
      </span>
    </a>
  );
}

export default function StickyDownloadBar() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      const sh = document.documentElement.scrollHeight - window.innerHeight;
      const progress = sh > 0 ? window.scrollY / sh : 0;
      setVisible(progress > 0.35 && progress < 0.82);
    };
    handleScroll();
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  if (!visible) return null;

  return (
    <div className="fixed bottom-0 left-0 right-0 z-40 border-t border-white/5 bg-black/85 backdrop-blur-md transition-opacity duration-500">
      <div className="mx-auto flex items-center justify-center gap-2 sm:gap-3 px-3 py-2.5 sm:px-6">
        <PlatformBtn
          href={PLAY_STORE_URL}
          icon={<GooglePlayIcon className="w-full h-full" />}
          label="Play Store"
          color="#3DDC84"
        />
        <PlatformBtn
          href={SOCIAL_LINKS.itchIo}
          icon={<ItchIoIcon className="w-full h-full" />}
          label="itch.io"
          color="#FA5C5C"
        />
        <PlatformBtn
          href={SOCIAL_LINKS.github}
          icon={<GitHubIcon className="w-full h-full" />}
          label="GitHub"
          color="#E0E0E0"
        />
        <PlatformBtn
          href={BETA_TESTING_URL}
          icon={
            <svg viewBox="0 0 24 24" className="w-full h-full" fill="none" aria-hidden="true">
              <path d="M12 3v18M3 12h18" stroke="#FFA000" strokeWidth="2.5" strokeLinecap="round" />
              <circle cx="12" cy="12" r="9" stroke="#FFA000" strokeWidth="1.5" fill="none" />
            </svg>
          }
          label="Beta"
          color="#FFA000"
        />
      </div>
    </div>
  );
}
