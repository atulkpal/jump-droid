"use client";

import { useState } from "react";
import { SCREENSHOTS } from "@/lib/constants";

export default function ScreenshotGallery() {
  const [activeIndex, setActiveIndex] = useState(0);

  const prev = () => setActiveIndex((i) => (i === 0 ? SCREENSHOTS.length - 1 : i - 1));
  const next = () => setActiveIndex((i) => (i === SCREENSHOTS.length - 1 ? 0 : i + 1));

  return (
    <section className="flex w-full flex-col items-center px-6 py-20 sm:py-28">
      <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-8 text-center">
        Visual Log
      </p>

      <div className="mx-auto w-full max-w-6xl">
        <div className="grid gap-6 lg:grid-cols-12 items-start">
          {/* Desktop thumbnail tabs */}
          <div className="hidden lg:flex lg:col-span-3 flex-col gap-2.5" role="tablist" aria-label="Screenshot selector">
            {SCREENSHOTS.map((shot, i) => (
              <button
                key={i}
                role="tab"
                aria-selected={i === activeIndex}
                onClick={() => setActiveIndex(i)}
                className={`text-left rounded-xl border px-4 py-3 font-mono text-[11px] tracking-[0.1em] uppercase transition-all ${
                  i === activeIndex
                    ? "border-cyan-400/40 bg-cyan-400/10 text-cyan-300"
                    : "border-white/5 bg-white/[0.02] text-slate-500 hover:border-white/10 hover:text-slate-300"
                }`}
              >
                {shot.caption}
              </button>
            ))}
          </div>

          {/* Main image */}
          <div className="lg:col-span-9">
            <div className="relative aspect-video rounded-xl border border-white/5 bg-black/60 overflow-hidden">
              <img
                key={activeIndex}
                src={SCREENSHOTS[activeIndex].src}
                alt={SCREENSHOTS[activeIndex].alt}
                className="h-full w-full object-contain bg-black transition-opacity duration-300"
              />

              {/* Caption overlay */}
              <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-5">
                <p className="font-mono text-sm font-bold text-white tracking-wide">
                  {SCREENSHOTS[activeIndex].caption}
                </p>
              </div>

              {/* Prev / Next buttons */}
              <button
                onClick={prev}
                className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-black/60 p-2.5 text-white/70 transition hover:bg-black/80 hover:text-white"
                aria-label="Previous screenshot"
              >
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <button
                onClick={next}
                className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-black/60 p-2.5 text-white/70 transition hover:bg-black/80 hover:text-white"
                aria-label="Next screenshot"
              >
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>

            {/* Mobile dot indicators */}
            <div className="mt-4 flex justify-center gap-2 lg:hidden" role="tablist" aria-label="Screenshot selector">
              {SCREENSHOTS.map((_, i) => (
                <button
                  key={i}
                  role="tab"
                  aria-selected={i === activeIndex}
                  aria-label={`Screenshot ${i + 1}`}
                  onClick={() => setActiveIndex(i)}
                  className={`h-2 rounded-full transition-all ${
                    i === activeIndex ? "w-6 bg-cyan-400" : "w-2 bg-slate-600"
                  }`}
                />
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
