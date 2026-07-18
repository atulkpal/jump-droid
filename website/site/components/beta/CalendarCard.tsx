"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { computeDayActivity, computeDaysRemaining } from "@/lib/firebase/analytics";
import { fetchSessions } from "@/lib/firebase/sessions";

interface Props {
  tester: Tester;
  config: DashboardConfig;
}

const MONTH_NAMES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

export default function CalendarCard({ tester, config }: Props) {
  const [dayPlaytime, setDayPlaytime] = useState<Map<string, number>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchSessions(tester.docId)
      .then((sessions) => {
        setDayPlaytime(computeDayActivity(sessions));
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load session data");
        setLoading(false);
      });
  }, [tester.docId]);

  const goalSeconds = config.beta.requiredMinutes * 60;
  const start = new Date(config.beta.startDate + "T00:00:00Z");
  const end = new Date(config.beta.endDate + "T23:59:59Z");
  const today = new Date();
  const lastVisible = today < end ? today : end;
  const daysRemaining = computeDaysRemaining(config.beta.endDate);

  // Build days list from start to lastVisible
  const days: { date: string; playtime: number }[] = [];
  const cursor = new Date(start);
  while (cursor <= lastVisible) {
    const dateStr = cursor.toISOString().split("T")[0];
    days.push({ date: dateStr, playtime: dayPlaytime.get(dateStr) ?? 0 });
    cursor.setUTCDate(cursor.getUTCDate() + 1);
  }

  // Chunk into weeks of 7
  const weeks: { date: string; playtime: number }[][] = [];
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(days.slice(i, i + 7));
  }

  // Track which week column starts a new month
  let lastMonth = -1;
  const monthAtWeek = weeks.map((week) => {
    if (week.length === 0) return "";
    const m = new Date(week[0].date).getMonth();
    if (m !== lastMonth) {
      lastMonth = m;
      return MONTH_NAMES[m];
    }
    return "";
  });

  const colorClass = (playtime: number) => {
    if (playtime === 0) return "bg-white/5";
    const ratio = playtime / goalSeconds;
    if (ratio >= 1) return "bg-green-400";
    if (ratio >= 0.5) return "bg-green-500/70";
    return "bg-green-500/30";
  };

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-3">
        Activity Calendar
      </h2>
      <p className="font-mono text-[10px] text-slate-500 mb-4">
        {config.beta.startDate} &ndash; {config.beta.endDate}
        {daysRemaining > 0 && ` (${daysRemaining} days remaining)`}
      </p>
      {loading ? (
        <p className="font-mono text-xs text-slate-500">Loading...</p>
      ) : error ? (
        <p className="font-mono text-xs text-red-400">{error}</p>
      ) : days.length === 0 ? (
        <p className="font-mono text-xs text-slate-500">No data yet.</p>
      ) : (
        <div className="overflow-x-auto pb-2">
          <div className="flex gap-[3px]">
            {weeks.map((week, wi) => (
              <div key={wi} className="flex flex-col gap-[3px] items-center">
                <span className="font-mono text-[10px] text-slate-500 h-3 leading-none">
                  {monthAtWeek[wi]}
                </span>
                {week.map((d) => (
                  <div
                    key={d.date}
                    className={`w-3 h-3 rounded-sm ${colorClass(d.playtime)}`}
                    title={`${d.date}: ${Math.floor(d.playtime / 60)} min`}
                  />
                ))}
                {week.length < 7 && Array.from({ length: 7 - week.length }).map((_, i) => (
                  <div key={`empty-${i}`} className="w-3 h-3" />
                ))}
              </div>
            ))}
          </div>
          <div className="flex gap-2 items-center mt-4">
            <span className="font-mono text-[10px] text-slate-500">Less</span>
            <div className="w-3 h-3 rounded-sm bg-white/5" />
            <div className="w-3 h-3 rounded-sm bg-green-500/30" />
            <div className="w-3 h-3 rounded-sm bg-green-500/70" />
            <div className="w-3 h-3 rounded-sm bg-green-400" />
            <span className="font-mono text-[10px] text-slate-500">More</span>
          </div>
        </div>
      )}
    </div>
  );
}
