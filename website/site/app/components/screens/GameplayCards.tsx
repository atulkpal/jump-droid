"use client";

import { useState, useEffect, useCallback } from "react";
import { GAME_CATEGORIES, ENTITY_DESCRIPTIONS } from "@/app/data/site-content";
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

function EntityIcon({ entity, large }: { entity: EntitySpec; large?: boolean }) {
  const Component = visualMap[entity.visual];
  if (entity.visual === "platform") {
    return (
      <div className={`flex items-center justify-center ${large ? "w-20 h-14 sm:w-28 sm:h-20" : "w-10 h-7 sm:w-12 sm:h-8"}`}>
        <Component type={entity.type} width={large ? 96 : 40} height={large ? 16 : 8} animated={false} />
      </div>
    );
  }
  if (entity.visual === "rocket") {
    return (
      <div className={`flex items-center justify-center ${large ? "w-16 h-20 sm:w-24 sm:h-28" : "w-8 h-10 sm:w-10 sm:h-12"}`}>
        <Component type={entity.type} size={large ? 80 : 36} thrusting={false} />
      </div>
    );
  }
  return (
    <div className={`flex items-center justify-center ${large ? "w-16 h-16 sm:w-24 sm:h-24" : "w-8 h-8 sm:w-10 sm:h-10"}`}>
      <Component type={entity.type} size={large ? 72 : 28} />
    </div>
  );
}

function formatType(type: string): string {
  return type.replace(/_/g, " ");
}

function CategoryTab({ cat, active, onClick }: { cat: GameCategory; active: boolean; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`rounded-xl border px-4 py-3 font-mono text-[11px] tracking-[0.1em] uppercase transition-all ${
        active
          ? "border-cyan-400/40 bg-cyan-400/10 text-cyan-300"
          : "border-white/5 bg-white/[0.02] text-slate-500 hover:border-white/10 hover:text-slate-300"
      }`}
    >
      {cat.title}
    </button>
  );
}

export default function GameplayCards() {
  const [selected, setSelected] = useState<string | null>(null);
  const [entityIdx, setEntityIdx] = useState(0);

  const category = selected ? GAME_CATEGORIES.find((c) => c.id === selected) : null;
  const entities = category?.entities ?? [];
  const currentEntity = entities[entityIdx] ?? null;

  const goNext = useCallback(() => {
    if (entities.length === 0) return;
    setEntityIdx((i) => (i + 1) % entities.length);
  }, [entities.length]);

  const goPrev = useCallback(() => {
    if (entities.length === 0) return;
    setEntityIdx((i) => (i - 1 + entities.length) % entities.length);
  }, [entities.length]);

  useEffect(() => {
    if (!category) return;
    const timer = setInterval(goNext, 4000);
    return () => clearInterval(timer);
  }, [category, goNext]);

  const handleSelect = (id: string) => {
    if (selected === id) {
      setSelected(null);
    } else {
      setSelected(id);
      setEntityIdx(0);
    }
  };

  return (
    <section className="flex w-full flex-col items-center px-6 py-16 sm:py-20">
      <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-6 text-center">
        Expedition Data
      </p>

      {/* Category tabs */}
      <div className="flex flex-wrap justify-center gap-2 mb-8 w-full max-w-2xl">
        {GAME_CATEGORIES.map((cat) => (
          <CategoryTab key={cat.id} cat={cat} active={selected === cat.id} onClick={() => handleSelect(cat.id)} />
        ))}
      </div>

      {selected && category && currentEntity ? (
        /* Expanded entity view */
        <div className="w-full max-w-lg mx-auto opacity-0 animate-fade-in-up" key={selected}>
          <div className="rounded-2xl border border-white/10 bg-white/[0.03] px-6 py-8 sm:px-10 sm:py-10">
            {/* Category header */}
            <div className="text-center mb-6">
              <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/40 uppercase mb-1">{category.subtitle}</p>
              <h3 className="font-mono text-lg sm:text-xl font-bold tracking-[0.15em] text-white uppercase">{category.title}</h3>
            </div>

            {/* Entity display */}
            <div className="flex items-center justify-center py-8 sm:py-10">
              <button
                onClick={goPrev}
                className="mr-4 sm:mr-6 rounded-full border border-white/10 bg-white/5 p-2 text-white/40 transition-all hover:border-white/20 hover:text-white/70"
                aria-label="Previous entity"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>

              <div className="flex flex-col items-center gap-3 sm:gap-4" key={entityIdx}>
                <div className="opacity-0 animate-fade-in-up" key={currentEntity.type}>
                  <EntityIcon entity={currentEntity} large />
                </div>
              </div>

              <button
                onClick={goNext}
                className="ml-4 sm:ml-6 rounded-full border border-white/10 bg-white/5 p-2 text-white/40 transition-all hover:border-white/20 hover:text-white/70"
                aria-label="Next entity"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>

            {/* Entity info */}
            <div className="text-center space-y-2" key={`info-${entityIdx}`}>
              <p className="font-mono text-xs tracking-[0.15em] text-slate-400 uppercase opacity-0 animate-fade-in-up">
                {formatType(currentEntity.type)}
              </p>
              <p className="font-mono text-sm leading-relaxed text-slate-500 max-w-sm mx-auto opacity-0 animate-fade-in-up" style={{ animationDelay: "0.15s", animationFillMode: "forwards" }}>
                {ENTITY_DESCRIPTIONS[currentEntity.type] ?? "Unknown entity."}
              </p>
              <p className="font-mono text-[10px] tracking-[0.1em] text-white/15 mt-3 opacity-0 animate-fade-in-up" style={{ animationDelay: "0.3s", animationFillMode: "forwards" }}>
                {entityIdx + 1} / {entities.length}
              </p>
            </div>
          </div>
        </div>
      ) : (
        /* Default: category preview grid */
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 w-full max-w-5xl">
          {GAME_CATEGORIES.map((cat) => (
            <button
              key={cat.id}
              onClick={() => handleSelect(cat.id)}
              className="group rounded-2xl border border-white/5 bg-white/[0.02] px-4 py-6 sm:px-5 sm:py-8 transition-all hover:border-white/10 hover:bg-white/[0.04] text-center"
            >
              <h3 className="font-mono text-xs sm:text-sm font-bold tracking-[0.15em] text-white uppercase mb-1 group-hover:text-cyan-200 transition-colors">
                {cat.title}
              </h3>
              <p className="font-mono text-[10px] sm:text-xs text-slate-500 mb-4 max-w-[160px] mx-auto">
                {cat.subtitle}
              </p>
              <div className="flex flex-wrap items-center justify-center gap-1">
                {cat.entities.slice(0, 4).map((e, i) => (
                  <EntityIcon key={i} entity={e} />
                ))}
                {cat.entities.length > 4 && (
                  <span className="font-mono text-[10px] text-white/20 ml-1">+{cat.entities.length - 4}</span>
                )}
              </div>
            </button>
          ))}
        </div>
      )}
    </section>
  );
}
