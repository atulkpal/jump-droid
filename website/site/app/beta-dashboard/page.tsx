"use client";

import { useState, useEffect, useCallback } from "react";
import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { fetchAllTesters } from "@/lib/firebase/testers";
import { fetchRecentSessions } from "@/lib/firebase/sessions";
import { fetchConfig, getDefaultConfig } from "@/lib/firebase/configService";
import { fetchUsdToInr } from "@/lib/firebase/exchangeRate";
import ConfigurationCard from "@/components/beta/ConfigurationCard";
import OverviewCards from "@/components/beta/OverviewCards";
import TesterTable from "@/components/beta/TesterTable";
import RecentSessions from "@/components/beta/RecentSessions";
import DailySummaryTable from "@/components/beta/DailySummaryTable";
import FeedbackTable from "@/components/beta/FeedbackTable";

export default function BetaDashboardPage() {
  const [config, setConfig] = useState<DashboardConfig>(() => getDefaultConfig());
  const [testers, setTesters] = useState<Tester[]>([]);
  const [sessions, setSessions] = useState<TesterSession[]>([]);
  const [exchangeRateStatus, setExchangeRateStatus] = useState("");
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [fetchedConfig, allTesters, recentSessions] = await Promise.all([
        fetchConfig(),
        fetchAllTesters(),
        fetchRecentSessions(500),
      ]);

      const baseConfig = fetchedConfig ?? getDefaultConfig();

      const liveRate = await fetchUsdToInr();
      if (liveRate !== null) {
        baseConfig.revenue.usdToInr = liveRate;
        setExchangeRateStatus("Live rate applied");
      } else {
        setExchangeRateStatus(fetchedConfig ? "Using stored exchange rate" : "");
      }

      setConfig(baseConfig);
      setTesters(allTesters);
      setSessions(recentSessions);
    } catch (e: any) {
      setError(e?.message ?? "Failed to load dashboard data");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleConfigSaved = useCallback((saved: DashboardConfig) => {
    setConfig(saved);
    setExchangeRateStatus("");
  }, []);

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-7xl px-6 py-24 sm:px-8 sm:py-32">
        <div className="mb-12 flex items-start justify-between">
          <div className="space-y-4">
            <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
              Admin Terminal
            </p>
            <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
              Beta
              <br />
              <span className="text-cyan-300">Dashboard</span>
            </h1>
          </div>
          <div className="flex items-center gap-3">
            <a
              href="/beta-dashboard/recruitment"
              className="rounded-lg border border-white/10 px-4 py-3 font-mono text-sm text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Recruitment
            </a>
            <button
              onClick={() => setSettingsOpen(true)}
              className="rounded-lg border border-white/10 px-4 py-3 font-mono text-sm text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              &#x2699; Settings
            </button>
          </div>
        </div>

        {settingsOpen && (
          <ConfigurationCard
            config={config}
            exchangeRateStatus={exchangeRateStatus}
            onConfigSaved={handleConfigSaved}
            onClose={() => setSettingsOpen(false)}
          />
        )}

        {loading ? (
          <p className="font-mono text-xs text-slate-500">Loading dashboard...</p>
        ) : error ? (
          <p className="font-mono text-xs text-red-400">{error}</p>
        ) : (
          <>
            <section className="mb-16">
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                Overview
              </h2>
              <OverviewCards testers={testers} sessions={sessions} config={config} />
            </section>

            <section className="mb-16">
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                Testers
              </h2>
              <TesterTable testers={testers} config={config} />
            </section>

            <section className="mb-16">
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                Recent Sessions
              </h2>
              <RecentSessions sessions={sessions} testers={testers} config={config} />
            </section>

            <section className="mb-16">
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                Daily Summary
              </h2>
              <DailySummaryTable sessions={sessions} config={config} />
            </section>

            <section className="mb-16">
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                Feedback
              </h2>
              <FeedbackTable />
            </section>
          </>
        )}
      </main>
    </div>
  );
}
