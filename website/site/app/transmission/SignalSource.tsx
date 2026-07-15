"use client";

import { motion } from "framer-motion";
import { SOCIAL_LINKS } from "@/lib/constants";

export default function SignalSource({ progress }: { progress: number }) {
  const reveal = Math.max(0, Math.min(1, (progress - 0.1) / 0.9));

  return (
    <motion.section
      className="relative px-6 py-24 sm:py-32"
      initial={{ opacity: 0 }}
      animate={{ opacity: progress > 0 ? 1 : 0 }}
      transition={{ duration: 0.5 }}
    >
      <div
        className="mx-auto max-w-lg border-t border-white/5 pt-8"
        style={{
          opacity: reveal,
          transform: `translateY(${(1 - reveal) * 20}px)`,
          transition: "opacity 0.6s ease-out, transform 0.6s ease-out",
        }}
      >
        <h2 className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase mb-5">
          Transmitter Identified
        </h2>

        <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
          <p>
            Signal origin traced to <span className="text-slate-200">Ashwath AI</span>.
            Independent development studio. Operating system: Free and Open.
          </p>
          <p>
            Mission: build free, open-source software, AI, and games for everyone.
            No investors. No data harvesting. Just craft.
          </p>
        </div>

        <div className="mt-8 flex flex-wrap gap-4">
          <a
            href={SOCIAL_LINKS.email}
            className="font-mono text-xs text-cyan-400/70 hover:text-cyan-300 transition-colors underline underline-offset-2"
          >
            Contact transmitter
          </a>
          <a
            href={SOCIAL_LINKS.privacy}
            className="font-mono text-xs text-cyan-400/70 hover:text-cyan-300 transition-colors underline underline-offset-2"
          >
            Data handling protocol
          </a>
        </div>
      </div>
    </motion.section>
  );
}
