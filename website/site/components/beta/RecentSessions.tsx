"use client";

import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { formatDuration } from "@/lib/firebase/analytics";
import { computeRevenue, formatCurrency } from "@/lib/firebase/revenue";

interface Props {
  sessions: TesterSession[];
  testers: Tester[];
  config: DashboardConfig;
}

export default function RecentSessions({ sessions, testers, config }: Props) {
  const testerMap = new Map(testers.map((t) => [t.docId, t.name]));

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] overflow-x-auto">
      <table className="w-full text-left">
        <thead>
          <tr className="border-b border-white/5">
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Tester
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Date
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Play Time
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Games
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Score
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Outcome
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Est. Revenue
            </th>
          </tr>
        </thead>
        <tbody>
          {sessions.length === 0 ? (
            <tr>
              <td colSpan={7} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                No recent sessions.
              </td>
            </tr>
          ) : (
            sessions.map((s) => {
              const name = s.testerName || testerMap.get(s.testerDocId ?? "") || "—";
              const revenue = computeRevenue(
                s.bannerImpressions ?? 0,
                s.rewardAdsWatched ?? 0,
                config.revenue
              );
              return (
                <tr key={s.id} className="border-b border-white/5 hover:bg-white/[0.01]">
                  <td className="px-4 py-3 font-mono text-xs text-white">{name}</td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {s.sessionStart
                      ? new Date(s.sessionStart.seconds * 1000).toLocaleDateString()
                      : "—"}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-300">
                    {formatDuration(s.gameplayTime ?? 0)}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-300">
                    {s.gamesPlayed ?? 1}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-cyan-300">
                    {s.finalScore?.toLocaleString() ?? "—"}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`font-mono text-[10px] tracking-[0.1em] uppercase ${
                        s.outcome === "COMPLETED" ? "text-green-400" : "text-red-400"
                      }`}
                    >
                      {s.outcome ?? "—"}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-cyan-100">
                    {formatCurrency(revenue.totalRevenue)}
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}
