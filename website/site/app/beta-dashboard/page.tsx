"use client";

import OverviewCards from "@/components/beta/OverviewCards";
import TesterTable from "@/components/beta/TesterTable";
import RecentSessions from "@/components/beta/RecentSessions";
import DailySummaryTable from "@/components/beta/DailySummaryTable";
import FeedbackTable from "@/components/beta/FeedbackTable";

export default function BetaDashboardPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.06),transparent_32%)]" />

      <main className="relative z-10 mx-auto max-w-7xl px-6 py-24 sm:px-8 sm:py-32">
        <div className="mb-12 space-y-4">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
            Admin Terminal
          </p>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Beta
            <br />
            <span className="text-cyan-300">Dashboard</span>
          </h1>
        </div>

        <section className="mb-16">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Overview
          </h2>
          <OverviewCards />
        </section>

        <section className="mb-16">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Testers
          </h2>
          <TesterTable />
        </section>

        <section className="mb-16">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Recent Sessions
          </h2>
          <RecentSessions />
        </section>

        <section className="mb-16">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Daily Summary
          </h2>
          <DailySummaryTable />
        </section>

        <section className="mb-16">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Feedback
          </h2>
          <FeedbackTable />
        </section>
      </main>
    </div>
  );
}
