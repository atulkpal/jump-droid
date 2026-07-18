"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import { fetchAllTesters } from "@/lib/firebase/testers";

const RANK_COLORS = ["text-yellow-300", "text-slate-300", "text-amber-600"];

export default function Leaderboard() {
  const [top, setTop] = useState<Tester[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAllTesters()
      .then((testers) => {
        const sorted = [...testers]
          .filter((t) => (t.highestScore ?? 0) > 0)
          .sort((a, b) => (b.highestScore ?? 0) - (a.highestScore ?? 0))
          .slice(0, 10);
        setTop(sorted);
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load leaderboard");
        setLoading(false);
      });
  }, []);

  if (loading) return null;
  if (error) return null;
  if (top.length === 0) return null;

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Leaderboard
      </h2>
      <div className="space-y-2">
        {top.map((tester, i) => (
          <div
            key={tester.email}
            className="flex items-center gap-3 rounded-lg border border-white/5 px-4 py-3"
          >
            <span
              className={`font-mono text-sm font-bold w-6 ${
                i < 3 ? RANK_COLORS[i] : "text-slate-500"
              }`}
            >
              #{i + 1}
            </span>
            <div className="flex-1 min-w-0">
              <p className="font-mono text-xs text-white truncate">{tester.name}</p>
            </div>
            <span className="font-mono text-sm text-cyan-300 shrink-0">
              {tester.highestScore?.toLocaleString() ?? "—"}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
