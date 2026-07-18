"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { fetchSessions } from "@/lib/firebase/sessions";

interface Props {
  tester: Tester;
  config: DashboardConfig;
}

export default function ProgressCard({ tester, config }: Props) {
  const [todayPlay, setTodayPlay] = useState(0);
  const [sessionsLoaded, setSessionsLoaded] = useState(false);

  useEffect(() => {
    const showDaily = config.beta.requirementMode === "daily" || config.beta.requirementMode === "both";
    if (!showDaily) {
      setSessionsLoaded(true);
      return;
    }
    fetchSessions(tester.docId).then((sessions) => {
      const todayStr = new Date().toISOString().split("T")[0];
      let total = 0;
      for (const s of sessions) {
        if (!s.sessionStart) continue;
        const date = new Date(s.sessionStart.seconds * 1000).toISOString().split("T")[0];
        if (date === todayStr) {
          total += s.gameplayTime ?? 0;
        }
      }
      setTodayPlay(total);
      setSessionsLoaded(true);
    }).catch(() => setSessionsLoaded(true));
  }, [tester.docId, config.beta.requirementMode]);

  const { requirementMode, requiredMinutes, requiredTotalHours } = config.beta;
  const dailyGoalSeconds = requiredMinutes * 60;
  const totalGoalSeconds = requiredTotalHours * 3600;

  const showDaily = requirementMode === "daily" || requirementMode === "both";
  const showTotal = requirementMode === "total" || requirementMode === "both";

  const dailyRemaining = Math.max(0, dailyGoalSeconds - todayPlay);
  const dailyPercent = Math.min(100, Math.round((todayPlay / dailyGoalSeconds) * 100));

  const totalPlay = tester.totalGameplayTime ?? 0;
  const totalRemaining = Math.max(0, totalGoalSeconds - totalPlay);
  const totalPercent = Math.min(100, Math.round((totalPlay / totalGoalSeconds) * 100));

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Progress
      </h2>
      {(!sessionsLoaded && showDaily) ? (
        <p className="font-mono text-xs text-slate-500">Loading...</p>
      ) : (
        <div className="space-y-6">
          {showDaily && (
            <div className="space-y-3">
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase">
                Today&apos;s Progress
              </p>
              <p className="font-mono text-xl text-white tracking-tight">
                {Math.floor(todayPlay / 60)} / {Math.floor(dailyGoalSeconds / 60)}
                <span className="text-sm text-slate-500 ml-1">min</span>
              </p>
              <div className="w-full h-3 rounded-full bg-white/5 overflow-hidden">
                <div
                  className="h-full rounded-full bg-cyan-400 transition-all"
                  style={{ width: `${dailyPercent}%` }}
                />
              </div>
              <div className="flex justify-between text-[11px] font-mono">
                <span className="text-slate-400">{dailyPercent}% complete</span>
                <span className={dailyRemaining === 0 ? "text-green-400" : "text-slate-300"}>
                  {dailyRemaining === 0 ? "Goal reached" : `${Math.ceil(dailyRemaining / 60)} min remaining`}
                </span>
              </div>
            </div>
          )}

          {showTotal && (
            <div className="space-y-3">
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase">
                Total Progress
              </p>
              <p className="font-mono text-xl text-white tracking-tight">
                {Math.floor(totalPlay / 3600)}.{Math.floor((totalPlay % 3600) / 60).toString().padStart(2, "0")} / {requiredTotalHours}
                <span className="text-sm text-slate-500 ml-1">hrs</span>
              </p>
              <div className="w-full h-3 rounded-full bg-white/5 overflow-hidden">
                <div
                  className="h-full rounded-full bg-cyan-400 transition-all"
                  style={{ width: `${totalPercent}%` }}
                />
              </div>
              <div className="flex justify-between text-[11px] font-mono">
                <span className="text-slate-400">{totalPercent}% complete</span>
                <span className={totalRemaining === 0 ? "text-green-400" : "text-slate-300"}>
                  {totalRemaining === 0 ? "Goal reached" : `${Math.ceil(totalRemaining / 3600 * 10) / 10} hrs remaining`}
                </span>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
