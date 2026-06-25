"use client";

interface AltitudeHUDProps {
  altitude: number;
  zoneName: string;
  progress: number;
}

export default function AltitudeHUD({ altitude, zoneName, progress }: AltitudeHUDProps) {
  return (
    <div className="fixed left-4 top-4 z-30 pointer-events-none sm:left-8 sm:top-8">
      <p className="font-mono text-3xl font-black text-white tracking-tight sm:text-5xl">
        {altitude.toLocaleString()}m
      </p>
      <p className="text-sm uppercase tracking-[0.2em] text-cyan-300 font-bold mt-1 sm:text-lg sm:mt-2">
        {zoneName}
      </p>
      <div className="mt-2 h-1.5 w-24 rounded-full bg-white/10 sm:mt-4 sm:h-2 sm:w-40">
        <div
          className="h-full rounded-full bg-cyan-400 transition-all duration-200"
          style={{ width: `${progress * 100}%` }}
        />
      </div>
    </div>
  );
}