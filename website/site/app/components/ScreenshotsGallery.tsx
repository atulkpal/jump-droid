"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { SCREENSHOTS } from "@/lib/constants";
import SectionWrapper from "./SectionWrapper";
import SectionHeader from "./SectionHeader";

export default function ScreenshotsGallery() {
  const [activeIndex, setActiveIndex] = useState(0);

  const prev = () => setActiveIndex((i) => (i === 0 ? SCREENSHOTS.length - 1 : i - 1));
  const next = () => setActiveIndex((i) => (i === SCREENSHOTS.length - 1 ? 0 : i + 1));

  return (
    <SectionWrapper id="screenshots">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(0,229,255,0.06),transparent_32%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <SectionHeader
          pill="Gallery"
          title="Expedition Visuals"
          description="Captured during active ascent telemetry. Each image represents a critical phase of the vertical expedition."
        />

        <div className="grid gap-8 lg:grid-cols-12 items-center">
          {/* Thumbnails */}
          <div className="hidden lg:flex lg:col-span-3 flex-col gap-3" role="tablist" aria-label="Screenshot selector">
            {SCREENSHOTS.map((shot, i) => (
              <button
                key={i}
                role="tab"
                aria-selected={i === activeIndex}
                aria-controls={`screenshot-panel-${i}`}
                onClick={() => setActiveIndex(i)}
                className={`text-left rounded-2xl border p-3 transition-all ${
                  i === activeIndex
                    ? "border-cyan-400 bg-cyan-400/10"
                    : "border-cyan-300/10 bg-slate-950/60 hover:border-cyan-400/30"
                }`}
              >
                <p className="text-xs font-bold tracking-wide text-white truncate">{shot.caption}</p>
              </button>
            ))}
          </div>

          {/* Main display */}
          <div className="lg:col-span-9">
            <div
              className="relative aspect-video rounded-3xl border border-cyan-300/15 bg-slate-950/60 overflow-hidden"
              role="tabpanel"
              id={`screenshot-panel-${activeIndex}`}
              aria-label={SCREENSHOTS[activeIndex].caption}
            >
              <AnimatePresence mode="wait">
                <motion.img
                  key={activeIndex}
                  src={SCREENSHOTS[activeIndex].src}
                  alt={SCREENSHOTS[activeIndex].alt}
                  className="h-full w-full object-cover"
                  initial={{ opacity: 0, scale: 1.05 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.95 }}
                  transition={{ duration: 0.3 }}
                />
              </AnimatePresence>

              {/* Caption overlay */}
              <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-6">
                <p className="text-lg font-bold text-white tracking-wide">
                  {SCREENSHOTS[activeIndex].caption}
                </p>
              </div>

              {/* Prev / Next buttons */}
              <button
                onClick={prev}
                className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-black/60 p-3 text-white transition hover:bg-black/80"
                aria-label="Previous screenshot"
              >
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <button
                onClick={next}
                className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-black/60 p-3 text-white transition hover:bg-black/80"
                aria-label="Next screenshot"
              >
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                  className={`h-2 w-2 rounded-full transition-all ${
                    i === activeIndex ? "w-6 bg-cyan-400" : "bg-slate-600"
                  }`}
                />
              ))}
            </div>
          </div>
        </div>
      </div>
    </SectionWrapper>
  );
}
