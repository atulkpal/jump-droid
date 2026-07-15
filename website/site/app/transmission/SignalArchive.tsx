"use client";

import { motion } from "framer-motion";
import { PLAY_STORE_URL, BETA_TESTING_URL, SOCIAL_LINKS } from "@/lib/constants";

export default function SignalArchive({ progress }: { progress: number }) {
  const reveal = Math.max(0, Math.min(1, (progress - 0.1) / 0.9));

  const links = [
    {
      href: PLAY_STORE_URL,
      label: "Google Play",
      sub: "Primary receiver node",
    },
    {
      href: SOCIAL_LINKS.github,
      label: "GitHub",
      sub: "Signal architecture & logs",
    },
    {
      href: SOCIAL_LINKS.itchIo,
      label: "itch.io",
      sub: "Alternate distribution channel",
    },
    {
      href: BETA_TESTING_URL,
      label: "Beta Access",
      sub: "Early deployment channel",
    },
  ];

  return (
    <motion.section
      className="relative px-6 py-24 sm:py-32"
      initial={{ opacity: 0 }}
      animate={{ opacity: progress > 0 ? 1 : 0 }}
      transition={{ duration: 0.5 }}
    >
      <div
        className="mx-auto max-w-lg"
        style={{
          opacity: reveal,
          transform: `translateY(${(1 - reveal) * 24}px)`,
          transition: "opacity 0.6s ease-out, transform 0.6s ease-out",
        }}
      >
        <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase mb-4">
          Transmission Complete
        </p>
        <h2 className="font-mono text-base font-bold tracking-[0.1em] text-white uppercase mb-3">
          Signal Archive
        </h2>
        <p className="font-mono text-xs leading-relaxed text-slate-400 mb-8 max-w-md">
          The full transmission — source code, assets, and schematics — is available
          for download. Access the signal through any receiver node below.
        </p>

        <div className="flex flex-col gap-3">
          {links.map((link, i) => (
            <a
              key={link.label}
              href={link.href}
              target={link.href.startsWith("http") ? "_blank" : undefined}
              rel={link.href.startsWith("http") ? "noopener noreferrer" : undefined}
              className="group flex items-center justify-between rounded-lg border border-white/5 bg-white/[0.02] px-5 py-4 transition-all hover:border-cyan-400/20 hover:bg-cyan-400/5"
              style={{
                opacity: reveal,
                transform: `translateY(${(1 - reveal) * 16 * (i + 1)}px)`,
                transition: `opacity 0.5s ease-out ${i * 0.08}s, transform 0.5s ease-out ${i * 0.08}s, border-color 0.2s, background-color 0.2s`,
              }}
            >
              <div className="flex flex-col">
                <span className="font-mono text-sm font-semibold text-white group-hover:text-cyan-200 transition-colors">
                  {link.label}
                </span>
                <span className="font-mono text-[10px] text-slate-600 group-hover:text-slate-500 transition-colors">
                  {link.sub}
                </span>
              </div>
              <span className="font-mono text-xs text-slate-600 group-hover:text-cyan-400 transition-colors">
                →
              </span>
            </a>
          ))}
        </div>

        <p className="font-mono text-[10px] text-slate-700 mt-6 leading-relaxed">
          All transmissions are free. No account or registration required.
          Signal operates under the MIT open protocol.
        </p>
      </div>
    </motion.section>
  );
}
