"use client";

import { useState } from "react";
import { CHASSIS_DATA } from "@/app/data/site-content";
import RocketSVG from "@/app/components/game/RocketSVG";

function RadarChart({ stats }: { stats: typeof CHASSIS_DATA[string] }) {
  const size = 200;
  const center = size / 2;
  const radius = 80;

  const points = [
    { label: "THR", value: stats.thrust },
    { label: "SHD", value: stats.shield },
    { label: "HEAT", value: stats.heat },
    { label: "HULL", value: stats.hull },
    { label: "SPD", value: stats.speed },
  ];

  const getPointCoords = (index: number, value: number) => {
    const angle = (Math.PI * 2 * index) / points.length - Math.PI / 2;
    const r = (radius * value) / 100;
    return {
      x: center + r * Math.cos(angle),
      y: center + r * Math.sin(angle),
    };
  };

  const pathData = points.map((p, i) => {
    const coords = getPointCoords(i, p.value);
    return `${i === 0 ? "M" : "L"} ${coords.x} ${coords.y}`;
  }).join(" ") + " Z";

  return (
    <div className="relative w-[200px] h-[200px]">
      <svg viewBox={`0 0 ${size} ${size}`} className="w-full h-full">
        {/* Background grids */}
        {[0.2, 0.4, 0.6, 0.8, 1].map((r) => (
          <path
            key={r}
            d={points.map((_, i) => {
              const coords = getPointCoords(i, 100 * r);
              return `${i === 0 ? "M" : "L"} ${coords.x} ${coords.y}`;
            }).join(" ") + " Z"}
            fill="none"
            stroke="rgba(255,255,255,0.05)"
            strokeWidth="1"
          />
        ))}
        {/* Axes */}
        {points.map((_, i) => {
          const coords = getPointCoords(i, 100);
          return (
            <line
              key={i}
              x1={center}
              y1={center}
              x2={coords.x}
              y2={coords.y}
              stroke="rgba(255,255,255,0.05)"
              strokeWidth="1"
            />
          );
        })}
        {/* Data polygon */}
        <path
          d={pathData}
          fill="rgba(0, 229, 255, 0.2)"
          stroke="#00E5FF"
          strokeWidth="2"
          className="transition-all duration-500"
        />
        {/* Labels */}
        {points.map((p, i) => {
          const coords = getPointCoords(i, 120);
          return (
            <text
              key={p.label}
              x={coords.x}
              y={coords.y}
              fill="rgba(255,255,255,0.3)"
              fontSize="10"
              textAnchor="middle"
              className="font-mono"
            >
              {p.label}
            </text>
          );
        })}
      </svg>
    </div>
  );
}

export default function FleetHangar() {
  const [selected, setSelected] = useState("BALANCED");
  const stats = CHASSIS_DATA[selected];

  return (
    <section className="flex flex-col items-center py-20 px-6 bg-white/[0.02] border-y border-white/5">
      <div className="text-center mb-12">
        <p className="font-mono text-[10px] tracking-[0.3em] text-cyan-400/40 uppercase mb-2">Hangar Protocol</p>
        <h2 className="font-mono text-2xl sm:text-3xl font-bold tracking-wider text-white uppercase">Fleet Analysis</h2>
      </div>

      <div className="grid md:grid-cols-2 gap-12 max-w-4xl w-full items-center">
        {/* Selector + Stats */}
        <div className="flex flex-col gap-8 order-2 md:order-1">
          <div className="flex flex-wrap gap-2">
            {Object.keys(CHASSIS_DATA).map((type) => (
              <button
                key={type}
                onClick={() => setSelected(type)}
                className={`px-4 py-2 rounded-lg font-mono text-[10px] tracking-widest uppercase transition-all ${
                  selected === type
                    ? "bg-cyan-400/10 border border-cyan-400/40 text-cyan-300"
                    : "bg-white/5 border border-white/5 text-slate-500 hover:bg-white/10"
                }`}
              >
                {type}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-8 bg-black/40 p-8 rounded-2xl border border-white/5">
            <RadarChart stats={stats} />
            <div className="space-y-4 flex-1">
              {Object.entries(stats).map(([key, val]) => (
                <div key={key}>
                  <div className="flex justify-between font-mono text-[9px] uppercase tracking-tighter text-slate-500 mb-1">
                    <span>{key}</span>
                    <span>{val}%</span>
                  </div>
                  <div className="w-full h-1 bg-white/5 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-cyan-400/40 transition-all duration-700"
                      style={{ width: `${val}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Visual Preview */}
        <div className="flex flex-col items-center justify-center order-1 md:order-2">
          <div className="relative group">
            <div className="absolute inset-0 bg-cyan-400/20 blur-3xl rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-1000" />
            <div className="relative transform scale-150 py-12">
               <RocketSVG type={selected as any} size={120} thrusting />
            </div>
          </div>
          <p className="mt-8 font-mono text-sm tracking-widest text-white/20 uppercase italic">
            Visual classification: {selected}
          </p>
        </div>
      </div>
    </section>
  );
}
