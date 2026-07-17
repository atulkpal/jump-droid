"use client";

import { useState, useEffect } from "react";
import type { TesterSession } from "@/types/tester";
import { fetchAllTesters } from "@/lib/firebase/testers";
import { fetchRecentSessions } from "@/lib/firebase/sessions";
import { formatDuration } from "@/lib/firebase/analytics";

export default function CommunityProgress() {
  const [testerCount, setTesterCount] = useState(0);
  const [totalGames, setTotalGames] = useState(0);
  const [totalPlayTime, setTotalPlayTime] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([fetchAllTesters(), fetchRecentSessions(500)])
      .then(([testers, sessions]) => {
        setTesterCount(testers.length);
        setTotalGames(sessions.reduce((sum, s) => sum + (s.gamesPlayed ?? 1), 0));
        setTotalPlayTime(sessions.reduce((sum, s) => sum + (s.gameplayTime ?? 0), 0));
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load community data");
        setLoading(false);
      });
  }, []);

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Community Progress
      </h2>
      {loading ? (
        <p className="font-mono text-xs text-slate-500">Loading...</p>
      ) : error ? (
        <p className="font-mono text-xs text-red-400">{error}</p>
      ) : (
        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <span className="font-mono text-[11px] text-slate-400">Beta Testers</span>
            <span className="font-mono text-sm text-cyan-100">{testerCount}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="font-mono text-[11px] text-slate-400">Total Community Play Time</span>
            <span className="font-mono text-sm text-cyan-100">
              {formatDuration(totalPlayTime)}
            </span>
          </div>
          <div className="flex justify-between items-center">
            <span className="font-mono text-[11px] text-slate-400">Total Games Played</span>
            <span className="font-mono text-sm text-cyan-100">{totalGames.toLocaleString()}</span>
          </div>
        </div>
      )}
    </div>
  );
}
