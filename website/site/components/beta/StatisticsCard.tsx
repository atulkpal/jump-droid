"use client";

import type { Tester } from "@/types/tester";
import { formatDuration } from "@/lib/firebase/analytics";

interface Props {
  tester: Tester;
}

function formatLastSeen(seconds?: number): string {
  if (!seconds) return "—";
  const diff = Date.now() / 1000 - seconds;
  if (diff < 60) return "Just now";
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return new Date(seconds * 1000).toLocaleDateString();
}

export default function StatisticsCard({ tester }: Props) {
  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Statistics
      </h2>
      <div className="space-y-3">
        <div className="flex justify-between items-center">
          <span className="font-mono text-[11px] text-slate-400">Total Gameplay Time</span>
          <span className="font-mono text-sm text-cyan-100">
            {formatDuration(tester.totalGameplayTime ?? 0)}
          </span>
        </div>
        <div className="flex justify-between items-center">
          <span className="font-mono text-[11px] text-slate-400">Games Played</span>
          <span className="font-mono text-sm text-cyan-100">{tester.totalSessions ?? 0}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="font-mono text-[11px] text-slate-400">Highest Score</span>
          <span className="font-mono text-sm text-cyan-300">
            {tester.highestScore?.toLocaleString() ?? "—"}
          </span>
        </div>
        <div className="flex justify-between items-center">
          <span className="font-mono text-[11px] text-slate-400">Current Rank</span>
          <span className="font-mono text-sm text-slate-500">—</span>
        </div>
        <div className="flex justify-between items-center pt-2 border-t border-white/5">
          <span className="font-mono text-[11px] text-slate-400">Last Seen</span>
          <span className="font-mono text-sm text-slate-300">
            {formatLastSeen(tester.lastSeen?.seconds)}
          </span>
        </div>
      </div>
    </div>
  );
}
