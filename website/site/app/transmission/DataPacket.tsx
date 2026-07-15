"use client";

import { motion } from "framer-motion";
import type { PacketData } from "@/app/data/transmission-packets";
import { SCREENSHOTS } from "@/lib/constants";
import RocketSVG from "@/app/components/game/RocketSVG";
import PlatformSVG from "@/app/components/game/PlatformSVG";
import ThreatSVG from "@/app/components/game/ThreatSVG";
import type { FC } from "react";

function useTypedLines(lines: string[], progress: number): string[] {
  const fullText = lines.join("\n");
  const totalChars = fullText.length;
  const visibleChars = Math.max(0, Math.min(totalChars, Math.floor(totalChars * Math.min(progress, 1))));
  return fullText.slice(0, visibleChars).split("\n");
}

function formatBodyLine(line: string): string {
  if (/^[A-Z][A-Z\s]{2,}[:]/.test(line)) {
    return line;
  }
  return line;
}

const visualMap: Record<string, FC> = {
  rocket: RocketSVG as FC,
  platform: PlatformSVG as FC,
  threat: ThreatSVG as FC,
};

function PacketVisual({ type, progress }: { type: "rocket" | "platform" | "threat"; progress: number }) {
  const Component = visualMap[type];
  return (
    <div
      className="flex justify-center py-4"
      style={{
        opacity: Math.min(1, progress * 2),
        transform: `translateY(${(1 - Math.min(1, progress * 2)) * 16}px)`,
        transition: "opacity 0.4s ease-out, transform 0.4s ease-out",
      }}
    >
      <div className="scale-[0.6] origin-center">
        <Component />
      </div>
    </div>
  );
}

function ScreenshotSlot({ index, progress }: { index: number; progress: number }) {
  const screenshot = SCREENSHOTS[index];
  if (!screenshot) return null;

  const revealProgress = Math.max(0, Math.min(1, (progress - 0.6) / 0.4));

  return (
    <motion.div
      className="relative mt-6 overflow-hidden rounded-lg border border-white/5 bg-black/40"
      initial={{ opacity: 0, filter: "blur(8px)" }}
      animate={{
        opacity: revealProgress,
        filter: `blur(${(1 - revealProgress) * 8}px)`,
      }}
      transition={{ duration: 0.6, ease: "easeOut" }}
    >
      <img
        src={screenshot.src}
        alt={screenshot.alt}
        className="w-full h-auto"
        loading="lazy"
      />
      <div className="absolute inset-0 bg-[repeating-linear-gradient(0deg,transparent,transparent_2px,rgba(0,0,0,0.08)_2px,rgba(0,0,0,0.08)_4px)] pointer-events-none" />
      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent px-3 py-2">
        <p className="font-mono text-[10px] tracking-[0.2em] text-cyan-400/60 uppercase">
          Visual Data Packet [{(index + 1).toString().padStart(2, "0")}/07]
        </p>
      </div>
    </motion.div>
  );
}

export default function DataPacket({
  packet,
  progress,
}: {
  packet: PacketData;
  progress: number;
}) {
  const typedLines = useTypedLines(packet.bodyLines, progress);
  const isActive = progress > 0 && progress < 1;
  const headerReveal = Math.min(1, progress * 3);

  const accentColor =
    packet.accent === "gold"
      ? "text-amber-300 border-amber-400/20"
      : packet.accent === "red"
        ? "text-red-300 border-red-400/20"
        : "text-cyan-300 border-cyan-400/20";

  const headerBorder =
    packet.accent === "gold"
      ? "border-amber-400/10"
      : packet.accent === "red"
        ? "border-red-400/10"
        : "border-cyan-400/10";

  return (
    <motion.section
      className="relative px-6 py-16 sm:py-20"
      initial={{ opacity: 0 }}
      animate={{ opacity: isActive ? 1 : 0.3 }}
      transition={{ duration: 0.5 }}
    >
      <div className={`mx-auto max-w-lg lg:max-w-2xl border-t ${headerBorder} pt-6`}>
        {/* Packet header */}
        <div
          className="flex items-center gap-3 mb-6"
          style={{
            opacity: headerReveal,
            transform: `translateY(${(1 - headerReveal) * 12}px)`,
            transition: "opacity 0.4s ease-out, transform 0.4s ease-out",
          }}
        >
          <span className={`font-mono text-[10px] tracking-[0.25em] ${accentColor} uppercase`}>
            {packet.tag}
          </span>
          <span className="h-px flex-1 bg-white/5" />
        </div>

        <h2
          className={`font-mono text-sm sm:text-base font-bold tracking-[0.15em] uppercase mb-4 sm:mb-5 ${
            packet.accent === "gold"
              ? "text-amber-200"
              : packet.accent === "red"
                ? "text-red-200"
                : "text-cyan-200"
          }`}
          style={{
            opacity: headerReveal,
            transform: `translateY(${(1 - headerReveal) * 12}px)`,
            transition: "opacity 0.4s ease-out, transform 0.4s ease-out 0.05s",
          }}
        >
          {packet.label}
        </h2>

        {/* Visual element */}
        {packet.visualType && <PacketVisual type={packet.visualType} progress={progress} />}

        {/* Body text (typing effect) */}
        <div className="font-mono text-xs sm:text-sm leading-relaxed sm:leading-relaxed text-slate-300 space-y-3 sm:space-y-4">
          {typedLines.map((line, i) => (
            <p key={i}>{formatBodyLine(line)}</p>
          ))}
          {progress < 1 && typedLines.length > 0 && (
            <span className="inline-block w-[2px] h-[1.1em] bg-cyan-400/70 ml-[1px] align-text-bottom animate-cursor-blink" />
          )}
        </div>

        {/* Screenshot */}
        {packet.screenshotIndex !== undefined && (
          <ScreenshotSlot index={packet.screenshotIndex} progress={progress} />
        )}
      </div>
    </motion.section>
  );
}
