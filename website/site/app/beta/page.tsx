"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { fetchConfig, getDefaultConfig } from "@/lib/firebase/configService";
import TesterSelector from "@/components/beta/TesterSelector";
import ProgressCard from "@/components/beta/ProgressCard";
import EligibilityCard from "@/components/beta/EligibilityCard";
import StatisticsCard from "@/components/beta/StatisticsCard";
import Leaderboard from "@/components/beta/Leaderboard";
import CommunityProgress from "@/components/beta/CommunityProgress";
import FeedbackSection from "@/components/beta/FeedbackSection";
import BetaRulesCard from "@/components/beta/BetaRulesCard";

export default function BetaPortalPage() {
  const [selectedTester, setSelectedTester] = useState<Tester | null>(null);
  const [config, setConfig] = useState<DashboardConfig | null>(null);
  const [configLoading, setConfigLoading] = useState(true);

  useEffect(() => {
    fetchConfig()
      .then((c) => setConfig(c ?? getDefaultConfig()))
      .catch(() => setConfig(getDefaultConfig()))
      .finally(() => setConfigLoading(false));
  }, []);

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.06),transparent_32%)]" />

      <main className="relative z-10 mx-auto max-w-2xl px-6 py-24 sm:px-8 sm:py-32">
        <div className="mb-12 space-y-4">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
            Tester Terminal
          </p>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Beta
            <br />
            <span className="text-cyan-300">Portal</span>
          </h1>
          <p className="font-mono text-xs leading-relaxed text-slate-400 max-w-lg">
            Track your progress, check eligibility, and submit feedback for the Jump Droid closed beta.
          </p>
        </div>

        <div className="space-y-8">
          <TesterSelector
            onSelect={setSelectedTester}
            selectedEmail={selectedTester?.email ?? null}
          />

          {selectedTester && config && !configLoading && (
            <>
              <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.03] p-5">
                <p className="font-mono text-sm text-cyan-200">
                  &#x1F680; Welcome, Beta Tester!
                </p>
                <p className="font-mono text-xs text-slate-400 mt-1 leading-relaxed">
                  Thank you for helping build Jump Droid. Your feedback and gameplay
                  are shaping the future of the game.
                </p>
              </div>

              <ProgressCard tester={selectedTester} config={config} />
              <EligibilityCard tester={selectedTester} config={config} />
              <StatisticsCard tester={selectedTester} />
              <Leaderboard />
              <CommunityProgress />
              <FeedbackSection tester={selectedTester} />
              <BetaRulesCard />
            </>
          )}

          {selectedTester && !config && !configLoading && (
            <p className="text-center font-mono text-xs text-slate-500 py-12">
              Using default configuration.
            </p>
          )}

          {!selectedTester && (
            <p className="text-center font-mono text-xs text-slate-600 py-12">
              Select a tester profile above to view your beta dashboard.
            </p>
          )}
        </div>
      </main>
    </div>
  );
}
