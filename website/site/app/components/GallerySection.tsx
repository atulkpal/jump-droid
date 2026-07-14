"use client";

import { useState, useRef, useEffect } from "react";
import { SCREENSHOTS } from "@/lib/constants";

export default function GallerySection() {
  const scrollRef = useRef<HTMLDivElement>(null);
  const [activeIndex, setActiveIndex] = useState(0);

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    const onScroll = () => {
      const idx = Math.round(el.scrollTop / el.clientHeight);
      setActiveIndex(Math.min(idx, SCREENSHOTS.length - 1));
    };
    el.addEventListener("scroll", onScroll, { passive: true });
    return () => el.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <section id="gallery" className="relative min-h-dvh overflow-hidden">
      <div
        ref={scrollRef}
        className="snap-y no-scrollbar h-dvh overflow-y-auto"
      >
        {SCREENSHOTS.map((shot, i) => (
          <div
            key={shot.src}
            className="snap-start h-dvh relative flex-shrink-0"
          >
            <img
              src={shot.src}
              alt={shot.alt}
              className="absolute inset-0 w-full h-full object-cover"
              loading={i === 0 ? "eager" : "lazy"}
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-8">
              <p className="text-lg font-bold text-white">{shot.caption}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="fixed right-4 top-1/2 -translate-y-1/2 flex flex-col gap-2 z-10">
        {SCREENSHOTS.map((_, i) => (
          <div
            key={i}
            className={`rounded-full transition-all duration-300 ${
              i === activeIndex
                ? "h-4 w-1 bg-cyan-400"
                : "h-1.5 w-1.5 bg-slate-500/60"
            }`}
          />
        ))}
      </div>
    </section>
  );
}
