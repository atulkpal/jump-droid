"use client";

import { SCREENSHOTS } from "@/lib/constants";

export default function ScreenshotGallery() {
  return (
    <section className="flex min-h-dvh w-full flex-col items-center justify-center overflow-hidden px-0">
      <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-6 text-center px-6">
        Visual Log
      </p>

      <div
        className="flex w-full snap-x snap-mandatory gap-2 overflow-x-auto px-4 scrollbar-none"
        style={{ scrollSnapType: "x mandatory" }}
      >
        {SCREENSHOTS.map((s, i) => (
          <div
            key={i}
            className="flex w-[85vw] shrink-0 snap-center flex-col overflow-hidden rounded-xl sm:w-[65vw]"
          >
            <img
              src={s.src}
              alt={s.alt}
              className="w-full h-auto aspect-video object-cover rounded-xl"
              loading="lazy"
            />
            <p className="font-mono text-[11px] tracking-[0.15em] text-slate-500 mt-3 text-center">
              {String(i + 1).padStart(2, "0")} / {String(SCREENSHOTS.length).padStart(2, "0")} &middot; {s.caption}
            </p>
          </div>
        ))}
      </div>

      <p className="font-mono text-[10px] tracking-[0.15em] text-white/15 mt-4 animate-pulse">
        &larr; swipe &rarr;
      </p>
    </section>
  );
}
