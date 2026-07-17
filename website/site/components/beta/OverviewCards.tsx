"use client";

import { useState, useEffect } from "react";
import type { DashboardStats } from "@/types/stats";
import {
  computeDashboardStats,
  countTodayActiveTesters,
  formatDuration,
} from "@/lib/firebase/analytics";
import { fetchAllTesters } from "@/lib/firebase/testers";
import { fetchRecentSessions } from "@/lib/firebase/sessions";
export default function OverviewCards() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [todayActive, setTodayActive] = useState(0);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([fetchAllTesters(), fetchRecentSessions(500)])
      .then(([testers, sessions]) => {
        setStats(computeDashboardStats(testers, sessions));
        setTodayActive(countTodayActiveTesters(sessions));
        setConnected(true);
      })
      .catch((e) => setError(e?.message ?? "Failed to load overview"));
  }, []);

  const cards = [
    { label: "Total Testers", value: stats?.totalTesters.toString() ?? "—" },
    { label: "Today Active", value: todayActive.toString() || "0" },
    { label: "Total Sessions", value: stats?.totalSessions.toString() ?? "—" },
    { label: "Games Played", value: stats?.gamesPlayed.toString() ?? "—" },
    { label: "Total Gameplay Time", value: stats ? formatDuration(stats.totalGameplayTime) : "—" },
    { label: "Banner Impressions", value: stats?.bannerImpressions.toString() ?? "—" },
    { label: "Reward Ads", value: stats?.rewardAds.toString() ?? "—" },
    { label: "Estimated Revenue", value: "Configurable" },
  ];

  return (
    <>
      <div className="mb-6 flex items-center gap-2">
        <span
          className={`inline-block w-2 h-2 rounded-full ${
            connected ? "bg-green-400" : "bg-amber-400"
          }`}
        />
        <span className="font-mono text-[10px] tracking-[0.1em] text-slate-500 uppercase">
          {connected ? "Connected to Firestore" : "Firestore unavailable"}
        </span>
      </div>

      {error && (
        <p className="mb-4 font-mono text-xs text-red-400">{error}</p>
      )}

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {cards.map((card) => (
          <div
            key={card.label}
            className="rounded-lg border border-white/5 bg-white/[0.02] p-4"
          >
            <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-2">
              {card.label}
            </p>
            <p className="font-mono text-lg text-cyan-100">{card.value}</p>
          </div>
        ))}
      </div>
    </>
  );
}
