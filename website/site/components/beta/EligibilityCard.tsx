"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import type { EligibilityInfo } from "@/types/stats";
import { computeEligibility } from "@/lib/firebase/analytics";
import { fetchSessions } from "@/lib/firebase/sessions";

interface Props {
  tester: Tester;
  config: DashboardConfig;
}

export default function EligibilityCard({ tester, config }: Props) {
  const [eligibility, setEligibility] = useState<EligibilityInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchSessions(tester.docId)
      .then((sessions) => {
        setEligibility(computeEligibility(sessions, config));
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load session data");
        setLoading(false);
      });
  }, [tester.docId, config]);

  const endDateDisplay = new Date(config.beta.endDate).toLocaleDateString("en-IN", {
    day: "numeric",
    month: "long",
  });

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Eligibility
      </h2>
      {loading ? (
        <p className="font-mono text-xs text-slate-500">Calculating...</p>
      ) : error ? (
        <p className="font-mono text-xs text-red-400">{error}</p>
      ) : !eligibility ? (
        <p className="font-mono text-xs text-slate-500">No session data available.</p>
      ) : (
        <div className="space-y-4">
          <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.03] px-4 py-3">
            <p className="font-mono text-xs text-cyan-200 leading-relaxed">
              {eligibility.totalRequiredDays > 0
                ? `${eligibility.totalRequiredDays} days required to qualify.`
                : "Qualification period has ended."}
            </p>
            <p className="font-mono text-[11px] text-cyan-400/60 mt-1">
              Keep playing every day until {endDateDisplay}.
            </p>
          </div>

          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="font-mono text-[11px] text-slate-400">Eligible Days Completed</span>
              <span className="font-mono text-sm text-cyan-100">{eligibility.eligibleDays}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="font-mono text-[11px] text-slate-400">Total Required Days</span>
              <span className="font-mono text-sm text-cyan-100">{eligibility.totalRequiredDays}</span>
            </div>
            <div className="flex justify-between items-center pt-2 border-t border-white/5">
              <span className="font-mono text-[11px] text-slate-400">Status</span>
              <span
                className={`font-mono text-sm font-bold ${
                  eligibility.isEligible ? "text-green-400" : "text-red-400"
                }`}
              >
                {eligibility.isEligible ? "Eligible" : "Ineligible"}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
