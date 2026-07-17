"use client";

import { useState, useEffect, useRef } from "react";
import { motion } from "framer-motion";
import type { PacketData } from "@/app/data/transmission-packets";
import { SCREENSHOTS } from "@/lib/constants";
import RocketSVG from "@/app/components/game/RocketSVG";
import PlatformSVG from "@/app/components/game/PlatformSVG";
import ThreatSVG from "@/app/components/game/ThreatSVG";
import type { FC } from "react";

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

function GhostEntity({ type, visible }: { type: "rocket" | "platform" | "threat"; visible: boolean }) {
  const Component = visualMap[type];
  return (
    <div
      className="absolute inset-0 flex items-center justify-center pointer-events-none select-none overflow-hidden"
      style={{
        opacity: visible ? 0.12 : 0,
        transition: "opacity 1.5s ease-out",
      }}
    >
      <div
        className="scale-[1.8] sm:scale-[2.5] origin-center"
        style={{
          animation: "entity-float 10s ease-in-out infinite",
        }}
      >
        <Component />
      </div>
      <div className="absolute inset-0 bg-glow-ellipse-cyan" />
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
  const [revealTyping, setRevealTyping] = useState(0);
  const doneRef = useRef(false);

  const isActive = progress > 0.1 && progress < 0.9;
  const isVisible = progress > 0 && progress < 1;

  useEffect(() => {
    if (!isActive) {
      doneRef.current = false;
      return;
    }
    if (doneRef.current) return;

    const chars = packet.bodyLines.join("\n").length;
    const duration = Math.max(3000, chars * 22);
    const start = Date.now();

    const onFrame = () => {
      const elapsed = Date.now() - start;
      const t = Math.min(elapsed / duration, 1);
      setRevealTyping(t);
      if (t >= 1) {
        doneRef.current = true;
        return;
      }
      raf = requestAnimationFrame(onFrame);
    };
    let raf = requestAnimationFrame(onFrame);

    return () => cancelAnimationFrame(raf);
  }, [isActive, packet.bodyLines]);

  const fullText = packet.bodyLines.join("\n");
  const totalChars = fullText.length;
  const visibleChars = Math.max(0, Math.min(totalChars, Math.floor(totalChars * Math.min(revealTyping, 1))));
  const typedText = fullText.slice(0, visibleChars);
  const typedLines = packet.bodyLines.map(() => "");

  if (typedText.length > 0) {
    const lines = typedText.split("\n");
    for (let i = 0; i < typedLines.length; i++) {
      typedLines[i] = lines[i] ?? "";
    }
  }

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
      className="relative min-h-screen w-full flex items-center justify-center overflow-hidden"
      initial={{ opacity: 0 }}
      animate={{ opacity: isVisible ? 1 : 0.3 }}
      transition={{ duration: 0.5 }}
    >
      {/* Ghost entity — full-size background */}
      {packet.visualType && <GhostEntity type={packet.visualType} visible={isActive} />}

      {/* Text content */}
      <div className="relative z-10 mx-auto w-full max-w-lg lg:max-w-2xl px-6 py-8 sm:py-12">
        <div className={`border-t ${headerBorder} pt-6`}>
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

          {/* Body text (typing effect) */}
          <div className="font-mono text-sm lg:text-base leading-relaxed text-slate-300 space-y-3 sm:space-y-4">
            {typedLines.map((line, i) => (
              <p key={i}>{formatBodyLine(line)}</p>
            ))}
            {revealTyping < 1 && revealTyping > 0 && (
              <span className="inline-block w-[2px] h-[1.1em] bg-cyan-400/70 ml-[1px] align-text-bottom animate-cursor-blink" />
            )}
          </div>

          {/* Screenshot */}
          {packet.screenshotIndex !== undefined && (
            <ScreenshotSlot index={packet.screenshotIndex} progress={progress} />
          )}
        </div>
      </div>
    </motion.section>
  );
}
