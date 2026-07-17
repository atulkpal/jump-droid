"use client";

import { useState, useEffect } from "react";
import type { TesterSession } from "@/types/tester";
import { fetchRecentSessions } from "@/lib/firebase/sessions";
import { formatDuration } from "@/lib/firebase/analytics";

export default function RecentSessions() {
  const [sessions, setSessions] = useState<TesterSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchRecentSessions(50)
      .then((data) => {
        setSessions(data);
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load recent sessions");
        setLoading(false);
      });
  }, []);

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
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={6} className="px-4 py-8 text-center font-mono text-xs text-slate-600">
                Loading recent sessions...
              </td>
            </tr>
          ) : error ? (
            <tr>
              <td colSpan={6} className="px-4 py-8 text-center font-mono text-xs text-red-400">
                {error}
              </td>
            </tr>
          ) : sessions.length === 0 ? (
            <tr>
              <td colSpan={6} className="px-4 py-8 text-center font-mono text-xs text-slate-600">
                No recent sessions.
              </td>
            </tr>
          ) : (
            sessions.map((s) => (
              <tr key={s.id} className="border-b border-white/5 hover:bg-white/[0.01]">
                <td className="px-4 py-3 font-mono text-xs text-white">
                  {s.testerName ?? s.testerEmail ?? "—"}
                </td>
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
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
