"use client";

import { useState, useRef, useEffect, ReactNode } from "react";

interface CarouselProps {
  items: ReactNode[];
  label?: string;
}

export default function Carousel({ items, label }: CarouselProps) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const [activeIndex, setActiveIndex] = useState(0);

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    const onScroll = () => {
      const ratio = el.scrollLeft / (el.scrollWidth - el.clientWidth);
      const idx = Math.round(ratio * (items.length - 1));
      setActiveIndex(isNaN(idx) ? 0 : idx);
    };
    el.addEventListener("scroll", onScroll, { passive: true });
    return () => el.removeEventListener("scroll", onScroll);
  }, [items.length]);

  return (
    <div className="flex flex-col h-full">
      {label && (
        <p className="px-6 pt-6 pb-3 text-sm font-bold uppercase tracking-[0.35em] text-cyan-300">
          {label}
        </p>
      )}

      <div
        ref={scrollRef}
        className="snap-x no-scrollbar flex gap-4 overflow-x-auto px-6 pb-3 flex-1 items-start md:grid md:grid-cols-3 md:gap-6 md:overflow-visible md:px-12"
      >
        {items.map((item, i) => (
          <div
            key={i}
            className="snap-start flex-shrink-0 w-[80vw] max-w-sm md:w-auto md:max-w-none"
          >
            {item}
          </div>
        ))}
      </div>

      <div className="flex justify-center gap-2 py-4 md:hidden">
        {items.map((_, i) => (
          <div
            key={i}
            className={`h-1.5 rounded-full transition-all duration-300 ${
              i === activeIndex ? "w-6 bg-cyan-400" : "w-1.5 bg-slate-600"
            }`}
          />
        ))}
      </div>
    </div>
  );
}
