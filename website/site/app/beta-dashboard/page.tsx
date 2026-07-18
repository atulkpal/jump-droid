"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { fetchAllTesters } from "@/lib/firebase/testers";
import { fetchRecentSessions } from "@/lib/firebase/sessions";
import { fetchConfig, getDefaultConfig } from "@/lib/firebase/configService";
import { signOut as firebaseSignOut } from "@/lib/firebase/authService";
import OverviewCards from "@/components/beta/OverviewCards";
import TesterTable from "@/components/beta/TesterTable";
import RecentSessions from "@/components/beta/RecentSessions";
import DailySummaryTable from "@/components/beta/DailySummaryTable";
import FeedbackTable from "@/components/beta/FeedbackTable";

export default function BetaDashboardPage() {
  const router = useRouter();
  const [config, setConfig] = useState<DashboardConfig>(() => getDefaultConfig());
  const [testers, setTesters] = useState<Tester[]>([]);
  const [configLoading, setConfigLoading] = useState(true);
  const [sessions, setSessions] = useState<TesterSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [signingOut, setSigningOut] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [fetchedConfig, allTesters, recentSessions] = await Promise.all([
        fetchConfig(),
        fetchAllTesters(),
        fetchRecentSessions(500),
      ]);
      if (fetchedConfig) setConfig(fetchedConfig);
      setTesters(allTesters);
      setSessions(recentSessions);
    } catch (e: any) {
      setError(e?.message ?? "Failed to load dashboard data");
    } finally {
      setLoading(false);
      setConfigLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleSignOut = useCallback(async () => {
    setSigningOut(true);
    try {
      await firebaseSignOut();
      router.push("/");
    } catch {
      setSigningOut(false);
    }
  }, [router]);

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
          <div className="flex items-center gap-2">
            <Link
              href="/beta-dashboard/recruitment"
              className="rounded-lg border border-white/10 px-4 py-3 font-mono text-sm text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Recruitment
            </Link>
            <Link
              href="/beta-dashboard/email"
              className="rounded-lg border border-white/10 px-4 py-3 font-mono text-sm text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Email
            </Link>
            <Link
              href="/beta-dashboard/settings"
              className="rounded-lg border border-white/10 px-4 py-3 font-mono text-sm text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Settings
            </Link>
            <button
              onClick={handleSignOut}
              disabled={signingOut}
              className="rounded-lg border border-red-400/20 px-4 py-3 font-mono text-sm text-red-400/70 transition-colors hover:border-red-400/40 hover:text-red-400 disabled:opacity-50"
            >
              {signingOut ? "..." : "Sign Out"}
            </button>
          </div>
        </div>

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
