"use client";

import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import {
  computeDashboardStats,
  countTodayActiveTesters,
  formatDuration,
} from "@/lib/firebase/analytics";
import { formatCurrency } from "@/lib/firebase/revenue";
import { useRole } from "./AuthContext";

interface Props {
  testers: Tester[];
  sessions: TesterSession[];
  config: DashboardConfig;
}

export default function OverviewCards({ testers, sessions, config }: Props) {
  const { role } = useRole();
  const stats = computeDashboardStats(testers, sessions, config);
  const todayActive = countTodayActiveTesters(sessions);

  const cards = [
    { label: "Total Testers", value: stats.totalTesters.toString() },
    { label: "Today Active", value: todayActive.toString() },
    { label: "Total Sessions", value: stats.totalSessions.toString() },
    { label: "Games Played", value: stats.gamesPlayed.toString() },
    { label: "Total Gameplay Time", value: formatDuration(stats.totalGameplayTime) },
    { label: "Banner Impressions", value: stats.bannerImpressions.toString() },
    { label: "Reward Ads", value: stats.rewardAds.toString() },
    ...(role !== "user"
      ? [{ label: "Estimated Revenue", value: formatCurrency(stats.estimatedRevenue) }]
      : []),
  ];

  return (
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
  );
}
