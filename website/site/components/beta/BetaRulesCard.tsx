export default function BetaRulesCard() {
  return (
    <>
      <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] p-6">
        <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
          Beta Rewards
        </h2>

        <div className="space-y-4">
          <div>
            <p className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-2">
              Every Eligible Tester
            </p>
            <ul className="space-y-2">
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>Name permanently listed in the Jump Droid Credits as a Closed Beta Tester.</span>
              </li>
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>A special &quot;Closed Beta Tester&quot; recognition badge in the Credits.</span>
              </li>
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>Early access to future Jump Droid updates.</span>
              </li>
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>Priority invitations to future Ashwath.AI beta programs.</span>
              </li>
            </ul>
          </div>

          <div className="border-t border-cyan-400/10 pt-4">
            <p className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-2">
              Top Players Also Receive
            </p>
            <ul className="space-y-2">
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>Special recognition as Top Beta Testers.</span>
              </li>
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>Final leaderboard position preserved in the Credits.</span>
              </li>
              <li className="font-mono text-[11px] text-slate-400 flex gap-3">
                <span className="text-cyan-400 shrink-0">•</span>
                <span>Additional surprise rewards (to be announced).</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
        <p className="font-mono text-xs text-slate-400 leading-relaxed italic">
          &ldquo;You&apos;re helping shape Jump Droid. Every minute played, bug reported and
          suggestion submitted directly improves the game.&rdquo;
        </p>
      </div>
    </>
  );
}
