"use client";

import type { TesterSession } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { computeDailySummaries, formatDuration } from "@/lib/firebase/analytics";
import { formatCurrency } from "@/lib/firebase/revenue";

interface Props {
  sessions: TesterSession[];
  config: DashboardConfig;
}

export default function DailySummaryTable({ sessions, config }: Props) {
  const summaries = computeDailySummaries(sessions, config);

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] overflow-x-auto">
      <table className="w-full text-left">
        <thead>
          <tr className="border-b border-white/5">
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Date
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Active Testers
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Sessions
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Games
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Total Play Time
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Est. Revenue
            </th>
          </tr>
        </thead>
        <tbody>
          {summaries.length === 0 ? (
            <tr>
              <td colSpan={6} className="px-4 py-8 text-center font-mono text-xs text-slate-600">
                No session data available.
              </td>
            </tr>
          ) : (
            summaries.map((row) => (
              <tr key={row.date} className="border-b border-white/5 hover:bg-white/[0.01]">
                <td className="px-4 py-3 font-mono text-xs text-white">{row.date}</td>
                <td className="px-4 py-3 font-mono text-xs text-slate-300">{row.activeTesters}</td>
                <td className="px-4 py-3 font-mono text-xs text-slate-300">{row.sessions}</td>
                <td className="px-4 py-3 font-mono text-xs text-slate-300">{row.games}</td>
                <td className="px-4 py-3 font-mono text-xs text-slate-300">
                  {formatDuration(row.totalPlayTime)}
                </td>
                <td className="px-4 py-3 font-mono text-xs text-cyan-100">
                  {formatCurrency(row.estimatedRevenue)}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
