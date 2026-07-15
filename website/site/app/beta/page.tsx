import type { Metadata } from "next";
import Link from "next/link";
import { BETA_TESTING_URL } from "@/lib/constants";

export const metadata: Metadata = {
  title: "Classified Signal Log — Jump Droid",
  description:
    "Early access deployment channel for Jump Droid. Request access to pre-release builds, test unreleased features, and help decode the final transmission.",
  openGraph: {
    title: "Classified Signal Log — Jump Droid",
    description:
      "Early access deployment channel for Jump Droid. Request access to pre-release builds and help decode the final transmission.",
    type: "website",
    url: "https://jump-droid.vercel.app/beta",
    siteName: "Jump Droid",
  },
  twitter: {
    card: "summary",
    title: "Classified Signal Log — Jump Droid",
    description: "Early access deployment channel for Jump Droid Beta.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function BetaPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.06),transparent_32%)]" />

      <main className="relative z-10 mx-auto max-w-2xl px-6 py-24 sm:px-8 sm:py-32">
        {/* Header */}
        <div className="mb-16 space-y-4">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
            Classified Signal Log
          </p>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Early Access
            <br />
            <span className="text-cyan-300">Deployment</span>
          </h1>
          <p className="font-mono text-xs leading-relaxed text-slate-400 max-w-lg">
            This channel provides access to pre-release builds of the transmission.
            Test unreleased features before public deployment and help decode the
            final signal.
          </p>
        </div>

        {/* About */}
        <section className="mb-8 rounded-lg border border-white/5 bg-white/[0.02] p-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Program Overview
          </h2>
          <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
            <p>
              Beta testers receive early access to experimental builds before they
              reach the public release channel. Responsibilities include:
            </p>
            <ul className="space-y-2 pl-4">
              <li className="before:content-['>_'] before:text-cyan-400/60">
                Testing new features, rocket classes, and zones before public release
              </li>
              <li className="before:content-['>_'] before:text-cyan-400/60">
                Reporting anomalies and providing feedback that shapes development
              </li>
              <li className="before:content-['>_'] before:text-cyan-400/60">
                Stress-testing balance changes and new mechanics
              </li>
              <li className="before:content-['>_'] before:text-cyan-400/60">
                Recognition in the Codex as an early supporter
              </li>
            </ul>
          </div>
        </section>

        {/* How to Join */}
        <section className="mb-8 rounded-lg border border-white/5 bg-white/[0.02] p-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Access Protocol
          </h2>
          <ol className="font-mono text-xs leading-relaxed text-slate-400 space-y-2 pl-4 list-decimal">
            <li>Open the Google Play Beta page via the link below</li>
            <li>Select &quot;Become a Tester&quot; and confirm participation</li>
            <li>Wait a few minutes for propagation</li>
            <li>Update Jump Droid to the latest beta build from Google Play</li>
          </ol>
          <p className="font-mono text-[10px] text-slate-600 mt-3">
            Beta builds are updated frequently. You may leave the program at any time.
          </p>
        </section>

        {/* CTA */}
        <div className="flex flex-col items-center gap-4 rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] p-8 text-center">
          <p className="font-mono text-xs font-bold tracking-[0.1em] text-white uppercase">
            Request Access Token
          </p>
          <a
            href={BETA_TESTING_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 rounded border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50"
            aria-label="Become a Jump Droid beta tester on Google Play"
          >
            Access Deployment Channel →
          </a>
        </div>

        {/* Back link */}
        <div className="mt-12 text-center">
          <Link
            href="/"
            className="font-mono text-xs text-cyan-400/60 hover:text-cyan-300 transition-colors underline underline-offset-2"
          >
            &larr; Return to transmission
          </Link>
        </div>
      </main>
    </div>
  );
}
