"use client";

import { useState, useEffect } from "react";
import type { DailySummary } from "@/types/stats";
import { computeDailySummaries, formatDuration } from "@/lib/firebase/analytics";
import { fetchRecentSessions } from "@/lib/firebase/sessions";

export default function DailySummaryTable() {
  const [summaries, setSummaries] = useState<DailySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchRecentSessions(500)
      .then((sessions) => {
        setSummaries(computeDailySummaries(sessions));
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load daily summary");
        setLoading(false);
      });
  }, []);

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
          {loading ? (
            <tr>
              <td colSpan={6} className="px-4 py-8 text-center font-mono text-xs text-slate-600">
                Loading daily summary...
              </td>
            </tr>
          ) : error ? (
            <tr>
              <td colSpan={6} className="px-4 py-8 text-center font-mono text-xs text-red-400">
                {error}
              </td>
            </tr>
          ) : summaries.length === 0 ? (
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
                <td className="px-4 py-3 font-mono text-xs text-slate-500">Configurable</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
