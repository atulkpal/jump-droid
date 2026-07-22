"use client";

import { useState, useMemo } from "react";
import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { formatDuration } from "@/lib/firebase/analytics";
import { computeRevenue, formatCurrency } from "@/lib/firebase/revenue";
import { useRole } from "./AuthContext";

interface Props {
  sessions: TesterSession[];
  testers: Tester[];
  config: DashboardConfig;
}

const PAGE_SIZES = [10, 20, 50, 100] as const;

export default function RecentSessions({ sessions, testers, config }: Props) {
  const { role } = useRole();
  const testerMap = new Map(testers.map((t) => [t.docId, t.name]));
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState<number>(10);

  const totalPages = Math.max(1, Math.ceil(sessions.length / pageSize));
  const safePage = Math.min(page, totalPages);

  const paginated = useMemo(() => {
    const start = (safePage - 1) * pageSize;
    return sessions.slice(start, start + pageSize);
  }, [sessions, safePage, pageSize]);

  const pageNumbers = useMemo(() => {
    const pages: (number | "...")[] = [];
    const maxVisible = 5;
    if (totalPages <= maxVisible + 2) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      pages.push(1);
      let start = Math.max(2, safePage - 1);
      let end = Math.min(totalPages - 1, safePage + 1);
      if (start > 2) pages.push("...");
      for (let i = start; i <= end; i++) pages.push(i);
      if (end < totalPages - 1) pages.push("...");
      pages.push(totalPages);
    }
    return pages;
  }, [totalPages, safePage]);

  return (
    <div>
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
              {role !== "user" && (
                <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                  Est. Revenue
                </th>
              )}
            </tr>
          </thead>
          <tbody>
            {paginated.length === 0 ? (
              <tr>
                <td colSpan={role !== "user" ? 7 : 6} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                  No recent sessions.
                </td>
              </tr>
            ) : (
              paginated.map((s) => {
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

      {sessions.length > 10 && (
        <div className="flex items-center justify-between mt-4">
          <div className="flex items-center gap-2">
            <span className="font-mono text-[10px] text-slate-500">Rows:</span>
            <select
              value={pageSize}
              onChange={(e) => {
                setPageSize(Number(e.target.value));
                setPage(1);
              }}
              className="rounded border border-white/10 bg-black px-2 py-1 font-mono text-[11px] text-white outline-none transition focus:border-cyan-400/40"
            >
              {PAGE_SIZES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-1">
            <button
              onClick={() => setPage(Math.max(1, safePage - 1))}
              disabled={safePage <= 1}
              className="rounded px-2 py-1 font-mono text-[11px] text-slate-500 hover:text-white transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
            >
              Prev
            </button>
            {pageNumbers.map((p, i) =>
              p === "..." ? (
                <span key={`e${i}`} className="px-1 font-mono text-[11px] text-slate-600">
                  ...
                </span>
              ) : (
                <button
                  key={p}
                  onClick={() => setPage(p as number)}
                  className={`rounded px-2 py-1 font-mono text-[11px] transition-colors ${
                    safePage === p
                      ? "bg-cyan-400/10 text-cyan-300"
                      : "text-slate-500 hover:text-white"
                  }`}
                >
                  {p}
                </button>
              )
            )}
            <button
              onClick={() => setPage(Math.min(totalPages, safePage + 1))}
              disabled={safePage >= totalPages}
              className="rounded px-2 py-1 font-mono text-[11px] text-slate-500 hover:text-white transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
