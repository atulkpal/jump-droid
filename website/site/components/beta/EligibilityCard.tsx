"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import type { EligibilityInfo } from "@/types/stats";
import { computeEligibility, computeDaysRemaining } from "@/lib/firebase/analytics";
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
  const daysRemaining = computeDaysRemaining(config.beta.endDate);

  const { requirementMode, requiredMinutes, requiredDays, requiredTotalHours } = config.beta;

  const ruleText = () => {
    if (requirementMode === "daily") {
      return `Play ${requiredMinutes} min each day for ${requiredDays} days.`;
    }
    if (requirementMode === "total") {
      return `Log ${requiredTotalHours} total hours by ${endDateDisplay}.`;
    }
    return `Play ${requiredMinutes} min each day for ${requiredDays} days AND log ${requiredTotalHours} total hours by ${endDateDisplay}.`;
  };

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
              {ruleText()}
            </p>
            <p className="font-mono text-[11px] text-cyan-400/60 mt-1">
              {daysRemaining > 0 ? `${daysRemaining} days remaining` : "Beta period has ended."}
            </p>
          </div>

          <div className="space-y-3">
            {(requirementMode === "daily" || requirementMode === "both") && (
              <>
                <div className="flex justify-between items-center">
                  <span className="font-mono text-[11px] text-slate-400">Eligible Days Completed</span>
                  <span className="font-mono text-sm text-cyan-100">{eligibility.eligibleDays}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-mono text-[11px] text-slate-400">Total Required Days</span>
                  <span className="font-mono text-sm text-cyan-100">{eligibility.totalRequiredDays}</span>
                </div>
              </>
            )}
            {(requirementMode === "total" || requirementMode === "both") && (
              <>
                <div className="flex justify-between items-center">
                  <span className="font-mono text-[11px] text-slate-400">Total Hours Logged</span>
                  <span className="font-mono text-sm text-cyan-100">{eligibility.totalHours}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-mono text-[11px] text-slate-400">Required Total Hours</span>
                  <span className="font-mono text-sm text-cyan-100">{eligibility.requiredTotalHours}</span>
                </div>
              </>
            )}
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
