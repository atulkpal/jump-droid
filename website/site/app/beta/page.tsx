"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import Link from "next/link";
import type { Tester } from "@/types/tester";
import type { DashboardConfig } from "@/types/config";
import { fetchConfig, getDefaultConfig } from "@/lib/firebase/configService";
import CalendarCard from "@/components/beta/CalendarCard";
import ProgressCard from "@/components/beta/ProgressCard";
import StartPlayingCard from "@/components/beta/StartPlayingCard";
import EligibilityCard from "@/components/beta/EligibilityCard";
import StatisticsCard from "@/components/beta/StatisticsCard";
import Leaderboard from "@/components/beta/Leaderboard";
import CommunityProgress from "@/components/beta/CommunityProgress";
import FeedbackSection from "@/components/beta/FeedbackSection";
import BetaRulesCard from "@/components/beta/BetaRulesCard";

type PageState =
  | { phase: "input" }
  | { phase: "loading" }
  | { phase: "pending"; email: string }
  | { phase: "not_registered" }
  | { phase: "approved"; tester: Tester; config: DashboardConfig }
  | { phase: "error"; message: string };

const LS_KEY = "beta-email";

export default function BetaPortalPage() {
  const [email, setEmail] = useState("");
  const [state, setState] = useState<PageState>({ phase: "input" });
  const autoChecked = useRef(false);

  const checkStatus = useCallback(async (cleanEmail: string) => {
    setState({ phase: "loading" });
    try {
      const { getFirestore } = await import("@/lib/firebase/config");
      const firestore = await getFirestore();
      const { doc, getDocFromServer, collection, getDocs, query, where, limit } = await import("firebase/firestore");

      // 1. Check testers collection first (existing approved testers)
      const tq = query(collection(firestore, "testers"), where("email", "==", cleanEmail), limit(1));
      const testerSnap = await getDocs(tq);
      const inTesters = !testerSnap.empty;

      // 2. Check betaUsers collection (new registration flow)
      const betaSnap = await getDocFromServer(doc(firestore, "betaUsers", cleanEmail));
      const inBetaUsers = betaSnap.exists();
      const betaStatus: string | null = inBetaUsers ? (betaSnap.data()?.status || "pending") : null;

      // 3. Approved testers
      if (inTesters || (betaStatus && ["approved", "active", "member"].includes(betaStatus))) {
        let config: DashboardConfig;
        try { const c = await fetchConfig(); config = c ?? getDefaultConfig(); }
        catch { config = getDefaultConfig(); }

        if (inTesters) {
          const d = testerSnap.docs[0];
          const data = d.data();
          const tester: Tester = { ...data, docId: d.id, email: data.email } as Tester;
          localStorage.setItem(LS_KEY, cleanEmail);
          setState({ phase: "approved", tester, config });
        } else {
          const betaData = betaSnap.data()!;
          const tester: Tester = { email: cleanEmail, name: betaData.name || cleanEmail, docId: cleanEmail } as Tester;
          localStorage.setItem(LS_KEY, cleanEmail);
          setState({ phase: "approved", tester, config });
        }
        return;
      }

      // 4. Pending applicant
      if (inBetaUsers && betaStatus === "pending") {
        setState({ phase: "pending", email: cleanEmail });
        return;
      }

      // 5. Not found in either
      setState({ phase: "not_registered" });
    } catch (e: any) {
      setState({ phase: "error", message: e?.message || "Something went wrong" });
    }
  }, []);

  useEffect(() => {
    if (autoChecked.current) return;
    const saved = localStorage.getItem(LS_KEY);
    if (saved) {
      autoChecked.current = true;
      setEmail(saved);
      checkStatus(saved);
    }
  }, [checkStatus]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const clean = email.trim().toLowerCase();
    if (!clean) return;
    checkStatus(clean);
  };

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

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

        {state.phase === "input" && (
          <div className="rounded-xl border border-white/10 bg-white/[0.02] p-8">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label htmlFor="beta-email" className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Email Address
                </label>
                <input
                  id="beta-email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white placeholder:text-slate-600 outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
                  required
                />
              </div>
              <button
                type="submit"
                disabled={!email.trim()}
                className="w-full rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
              >
                Check Status
              </button>
            </form>
          </div>
        )}

        {state.phase === "loading" && (
          <div className="text-center py-16">
            <p className="font-mono text-xs text-slate-500 animate-pulse">Checking your status...</p>
          </div>
        )}

        {state.phase === "pending" && (
          <div className="rounded-xl border border-amber-400/20 bg-amber-400/[0.03] p-8 text-center">
            <p className="font-mono text-sm text-amber-300 mb-4">{"\u2709\uFE0F"} Application Received</p>
            <p className="font-mono text-xs text-slate-400 leading-relaxed max-w-md mx-auto">
              We&apos;ve received your interest to join the Jump Droid beta program.
              You will receive an email within 24–48 hours once your application is reviewed.
            </p>
            <div className="mt-6 flex items-center justify-center gap-3">
              <button
                onClick={() => checkStatus(state.email)}
                className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
              >
                Check Again
              </button>
              <button
                onClick={() => { localStorage.removeItem(LS_KEY); setState({ phase: "input" }); }}
                className="font-mono text-[11px] text-slate-500 underline underline-offset-2 hover:text-slate-300 transition-colors"
              >
                Change email
              </button>
            </div>
          </div>
        )}

        {state.phase === "not_registered" && (
          <div className="rounded-xl border border-amber-400/20 bg-amber-400/[0.03] p-8 text-center">
            <p className="font-mono text-sm text-amber-300 mb-4">{"\u26A0\uFE0F"} Not Registered</p>
            <p className="font-mono text-xs text-slate-400 leading-relaxed max-w-md mx-auto">
              You are not registered. Please{" "}
              <Link href="/beta-info" className="text-cyan-400 underline underline-offset-2 hover:text-cyan-300">
                register here
              </Link>{" "}
              to join the Jump Droid beta program.
            </p>
            <button
              onClick={() => { localStorage.removeItem(LS_KEY); setState({ phase: "input" }); }}
              className="mt-6 font-mono text-[11px] text-slate-500 underline underline-offset-2 hover:text-slate-300 transition-colors"
            >
              Change email
            </button>
          </div>
        )}

        {state.phase === "approved" && (
          <div className="space-y-8">
            <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.03] p-5">
              <p className="font-mono text-sm text-cyan-200">
                {"\uD83D\uDE80"} Welcome, Beta Tester!
              </p>
              <p className="font-mono text-xs text-slate-400 mt-2 leading-relaxed">
                Signed in as <span className="text-slate-300">{state.tester.name} ({state.tester.email})</span>
              </p>
              <p className="font-mono text-xs text-slate-400 mt-1 leading-relaxed">
                Thank you for helping build Jump Droid. Your feedback and gameplay
                are shaping the future of the game.
              </p>
              <button
                onClick={() => { localStorage.removeItem(LS_KEY); setState({ phase: "input" }); }}
                className="mt-3 font-mono text-[11px] text-slate-500 underline underline-offset-2 hover:text-slate-300 transition-colors"
              >
                Not you? Change email
              </button>
            </div>

            <CalendarCard tester={state.tester} config={state.config} />
            <ProgressCard tester={state.tester} config={state.config} />
            <StartPlayingCard totalGameplayTime={state.tester.totalGameplayTime} />
            <EligibilityCard tester={state.tester} config={state.config} />
            <StatisticsCard tester={state.tester} />
            <Leaderboard />
            <CommunityProgress />
            <FeedbackSection tester={state.tester} />
            <BetaRulesCard />
          </div>
        )}

        {state.phase === "error" && (
          <div className="rounded-xl border border-red-400/20 bg-red-400/[0.03] p-8 text-center">
            <p className="font-mono text-xs text-red-400 mb-4">{state.message}</p>
            <div className="flex items-center justify-center gap-3">
              <button
                onClick={() => setState({ phase: "input" })}
                className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
              >
                Try Again
              </button>
              <button
                onClick={() => { localStorage.removeItem(LS_KEY); setState({ phase: "input" }); }}
                className="font-mono text-[11px] text-slate-500 underline underline-offset-2 hover:text-slate-300 transition-colors"
              >
                Change email
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
