"use client";

import { useRef } from "react";
import { GAME_CATEGORIES } from "@/app/data/site-content";
import type { EntitySpec, GameCategory } from "@/app/data/site-content";
import RocketSVG from "@/app/components/game/RocketSVG";
import PlatformSVG from "@/app/components/game/PlatformSVG";
import ThreatSVG from "@/app/components/game/ThreatSVG";
import type { FC } from "react";

const visualMap: Record<string, FC<Record<string, unknown>>> = {
  rocket: RocketSVG as unknown as FC<Record<string, unknown>>,
  platform: PlatformSVG as unknown as FC<Record<string, unknown>>,
  threat: ThreatSVG as unknown as FC<Record<string, unknown>>,
};

function EntityThumb({ entity }: { entity: EntitySpec }) {
  const Component = visualMap[entity.visual];

  if (entity.visual === "platform") {
    return (
      <div className="flex items-center justify-center w-12 h-8 sm:w-14 sm:h-10 opacity-30 hover:opacity-60 transition-opacity">
        <Component type={entity.type} width={48} height={10} animated={false} />
      </div>
    );
  }
  if (entity.visual === "rocket") {
    return (
      <div className="flex items-center justify-center w-10 h-12 sm:w-12 sm:h-14 opacity-30 hover:opacity-60 transition-opacity">
        <Component type={entity.type} size={48} thrusting={false} />
      </div>
    );
  }
  return (
    <div className="flex items-center justify-center w-10 h-10 sm:w-12 sm:h-12 opacity-30 hover:opacity-60 transition-opacity">
      <Component type={entity.type} size={34} />
    </div>
  );
}

function CategoryCard({ category }: { category: GameCategory }) {
  return (
    <div className="flex w-[80vw] shrink-0 snap-center flex-col items-center justify-center rounded-2xl border border-white/5 bg-white/[0.02] px-6 py-10 sm:w-[55vw] sm:px-10">
      <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-1">
        Mission Data
      </p>
      <h3 className="font-mono text-base sm:text-lg font-bold tracking-[0.15em] text-white text-center uppercase mb-1">
        {category.title}
      </h3>
      <p className="font-mono text-xs sm:text-sm text-slate-500 text-center mb-8 max-w-[220px]">
        {category.subtitle}
      </p>
      <div className="flex flex-wrap items-center justify-center gap-1.5 sm:gap-2 max-w-[280px]">
        {category.entities.map((e, i) => (
          <EntityThumb key={i} entity={e} />
        ))}
      </div>
    </div>
  );
}

export default function GameplayCards() {
  const ref = useRef<HTMLDivElement>(null);

  return (
    <section className="flex min-h-dvh w-full flex-col items-center justify-center overflow-hidden px-0">
      <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-6 text-center px-6">
        Expedition Data
      </p>

      <div
        ref={ref}
        className="flex w-full snap-x snap-mandatory gap-4 overflow-x-auto px-6 pb-4 scrollbar-none"
        style={{ scrollSnapType: "x mandatory" }}
      >
        {GAME_CATEGORIES.map((cat) => (
          <CategoryCard key={cat.id} category={cat} />
        ))}
      </div>

      <p className="font-mono text-[10px] tracking-[0.15em] text-white/15 mt-4 animate-pulse">
        &larr; swipe &rarr;
      </p>
    </section>
  );
}
