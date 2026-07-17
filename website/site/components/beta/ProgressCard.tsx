"use client";

import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";

interface Props {
  tester: Tester;
  config: DashboardConfig;
}

export default function ProgressCard({ tester, config }: Props) {
  const dailyGoalSeconds = config.beta.requiredMinutes * 60;
  const todayPlay = tester.todayGameplayTime ?? 0;
  const remaining = Math.max(0, dailyGoalSeconds - todayPlay);
  const percent = Math.min(100, Math.round((todayPlay / dailyGoalSeconds) * 100));

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Progress
      </h2>
      <div className="space-y-4">
        <p className="font-mono text-xl text-white tracking-tight">
          {Math.floor(todayPlay / 60)} / {Math.floor(dailyGoalSeconds / 60)}
          <span className="text-sm text-slate-500 ml-1">min</span>
        </p>

        <div className="w-full h-3 rounded-full bg-white/5 overflow-hidden">
          <div
            className="h-full rounded-full bg-cyan-400 transition-all"
            style={{ width: `${percent}%` }}
          />
        </div>

        <div className="flex justify-between text-[11px] font-mono">
          <span className="text-slate-400">{percent}% complete</span>
          <span className={remaining === 0 ? "text-green-400" : "text-slate-300"}>
            {remaining === 0 ? "Goal reached" : `${Math.ceil(remaining / 60)} min remaining`}
          </span>
        </div>
      </div>
    </div>
  );
}
