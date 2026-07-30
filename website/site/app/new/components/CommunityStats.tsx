"use client";

import { useState, useEffect } from "react";

interface Stats {
  totalMetersClimbed: number;
  totalBossesDefeated: number;
  totalPilots: number;
  totalPlayTime: number;
}

export default function CommunityStats() {
  const [stats, setStats] = useState<Stats | null>(null);

  useEffect(() => {
    async function fetchStats() {
      try {
        const res = await fetch("/api/community/stats");
        const data = await res.json();
        setStats(data);
      } catch (e) {
        console.error("Failed to fetch community stats", e);
      }
    }
    fetchStats();
  }, []);

  const StatItem = ({ label, value, suffix = "" }: { label: string; value: number; suffix?: string }) => (
    <div className="flex flex-col items-center">
      <p className="font-mono text-[10px] tracking-[0.3em] text-cyan-400/40 uppercase mb-2">{label}</p>
      <p className="font-mono text-3xl sm:text-4xl font-bold text-white tabular-nums">
        {value.toLocaleString()}{suffix}
      </p>
    </div>
  );

  if (!stats) return null;

  return (
    <section className="py-20 px-6 w-full max-w-5xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-8 sm:gap-12">
      <StatItem label="Total Distance" value={stats.totalMetersClimbed} suffix="m" />
      <StatItem label="Bosses Defeated" value={stats.totalBossesDefeated} />
      <StatItem label="Active Pilots" value={stats.totalPilots} />
      <StatItem label="Mission Time" value={Math.floor(stats.totalPlayTime / 3600)} suffix="h" />
    </section>
  );
}
