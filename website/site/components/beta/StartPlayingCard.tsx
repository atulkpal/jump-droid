"use client";

import { PLAY_STORE_URL } from "@/lib/constants";

interface Props {
  totalGameplayTime?: number;
}

export default function StartPlayingCard({ totalGameplayTime }: Props) {
  if (totalGameplayTime && totalGameplayTime > 0) return null;

  return (
    <div className="rounded-lg border border-white/5 bg-gradient-to-br from-white/[0.02] to-transparent p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-3">
        Start Playing to Win!
      </h2>
      <p className="font-mono text-sm text-slate-300 mb-4 leading-relaxed">
        Download Jump Droid on the Google Play Store and start your ascent. Your game time counts toward the beta leaderboard and eligibility rewards.
      </p>
      <a
        href={PLAY_STORE_URL}
        target="_blank"
        rel="noopener noreferrer"
        className="inline-block rounded-lg bg-green-500/20 border border-green-400/30 px-5 py-2.5 font-mono text-xs font-bold text-green-300 tracking-wider hover:bg-green-500/30 transition-colors"
      >
        Download on Google Play →
      </a>
    </div>
  );
}
