"use client";

export default function SignalStrength({ percent }: { percent: number }) {
  const clamped = Math.max(0, Math.min(100, Math.round(percent)));

  return (
    <div className="fixed top-4 right-4 z-30 flex items-start gap-2 pointer-events-none select-none">
      <div className="flex flex-col items-end gap-1">
        <span className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/70 uppercase">
          Signal
        </span>
        <span className="font-mono text-xs font-bold text-cyan-300 tabular-nums">
          {clamped}%
        </span>
      </div>
      <div className="relative h-16 w-[3px] rounded-full bg-white/5 overflow-hidden">
        <div
          className="absolute bottom-0 w-full rounded-full bg-gradient-to-t from-cyan-500 to-cyan-300 transition-all duration-150 ease-out"
          style={{ height: `${clamped}%` }}
        />
      </div>
    </div>
  );
}
