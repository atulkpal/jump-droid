"use client";

import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { formatDuration } from "@/lib/firebase/analytics";
import { computeRevenue, formatCurrency } from "@/lib/firebase/revenue";
import { useRole } from "./AuthContext";

interface Props {
  testers: Tester[];
  config: DashboardConfig;
}

export default function TesterTable({ testers, config }: Props) {
  const { role } = useRole();
  const sorted = [...testers].sort((a, b) => {
    const at = a.lastSeen?.seconds ?? 0;
    const bt = b.lastSeen?.seconds ?? 0;
    return bt - at;
  });

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] overflow-x-auto">
      <table className="w-full text-left">
        <thead>
          <tr className="border-b border-white/5">
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Tester
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Last Seen
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Today
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Total
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Games
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Best Score
            </th>
            {role !== "user" && (
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Est. Revenue
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {sorted.length === 0 ? (
            <tr>
              <td colSpan={role !== "user" ? 7 : 6} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                No testers found.
              </td>
            </tr>
          ) : (
            sorted.map((t) => {
              const revenue = computeRevenue(
                t.bannerImpressions ?? 0,
                t.rewardAdsWatched ?? 0,
                config.revenue
              );
              return (
                <tr key={t.email} className="border-b border-white/5 hover:bg-white/[0.01]">
                  <td className="px-4 py-3">
                    <button
                      className="font-mono text-xs text-white text-left cursor-pointer transition hover:text-cyan-300"
                      onClick={() => {}}
                    >
                      {t.name}
                    </button>
                    <p className="font-mono text-[10px] text-slate-500">{t.email}</p>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {t.lastSeen
                      ? new Date(t.lastSeen.seconds * 1000).toLocaleDateString()
                      : "—"}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-300">
                    {formatDuration(t.todayGameplayTime ?? 0)}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-300">
                    {formatDuration(t.totalGameplayTime ?? 0)}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-300">
                    {t.totalSessions ?? 0}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-cyan-300">
                    {t.highestScore?.toLocaleString() ?? "—"}
                  </td>
                  {role !== "user" && (
                    <td className="px-4 py-3 font-mono text-xs text-cyan-100">
                      {formatCurrency(revenue.totalRevenue)}
                    </td>
                  )}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}
