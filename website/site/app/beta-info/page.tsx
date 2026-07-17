import type { Metadata } from "next";
import Link from "next/link";
import BetaRegistrationForm from "@/components/beta/BetaRegistrationForm";
import BetaAccordion from "@/components/beta/BetaAccordion";

export const metadata: Metadata = {
  title: "Jump Droid Beta Program — Early Access",
  description:
    "Join the Jump Droid beta program. Help shape the game before public release. Sign up for early access and influence development.",
  openGraph: {
    title: "Jump Droid Beta Program — Early Access",
    description:
      "Join the Jump Droid beta program. Sign up for early access and help shape the game before public release.",
    type: "website",
    url: "https://jump-droid.vercel.app/beta-info",
    siteName: "Jump Droid",
  },
  twitter: {
    card: "summary",
    title: "Jump Droid Beta Program — Early Access",
    description: "Join the Jump Droid beta program and help shape the game.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function BetaInfoPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-2xl px-6 py-16 sm:px-8 sm:py-20">
        <div className="mb-6 space-y-4">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
            Beta Program
          </p>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Jump Droid
            <br />
            <span className="text-cyan-300">Beta Program</span>
          </h1>
          <p className="font-mono text-xs leading-relaxed text-slate-400 max-w-lg">
            Help shape Jump Droid before everyone else gets to play it.
          </p>
        </div>

        <div className="mb-6 rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] p-4">
          <div className="flex items-center gap-2 mb-3">
            <span className="text-cyan-400 text-sm">&#9889;</span>
            <span className="font-mono text-[10px] tracking-[0.15em] text-cyan-200 uppercase font-bold">
              Beta Challenge
            </span>
          </div>
          <ul className="space-y-1">
            <li className="font-mono text-[11px] text-slate-400 before:content-['>_'] before:text-cyan-400/60 before:font-mono">
              Play 30 min/day &mdash; Earn rewards
            </li>
            <li className="font-mono text-[11px] text-slate-400 before:content-['>_'] before:text-cyan-400/60 before:font-mono">
              Get your name in the game credits
            </li>
            <li className="font-mono text-[11px] text-slate-400 before:content-['>_'] before:text-cyan-400/60 before:font-mono">
              Join the Code Jam &mdash; contribute code (optional)
            </li>
          </ul>
        </div>

        <section className="mb-6 rounded-lg border border-white/5 bg-white/[0.02] p-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
            Registration
          </h2>
          <BetaRegistrationForm />
        </section>

        <BetaAccordion />

        <div className="mt-8 text-center">
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
