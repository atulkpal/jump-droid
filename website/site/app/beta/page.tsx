import type { Metadata } from "next";
import Link from "next/link";
import { BETA_TESTING_URL } from "@/lib/constants";

export const metadata: Metadata = {
  title: "Beta Testing",
  description:
    "Join the Jump Droid beta testing program on Google Play. Get early access to new features, test upcoming builds, and help shape the game.",
  openGraph: {
    title: "Beta Testing — Jump Droid",
    description:
      "Join the Jump Droid beta testing program on Google Play. Get early access to new features and help shape the game.",
    type: "website",
    url: "https://jump-droid.vercel.app/beta",
    siteName: "Jump Droid",
  },
  twitter: {
    card: "summary",
    title: "Beta Testing — Jump Droid",
    description:
      "Join the Jump Droid beta testing program on Google Play.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function BetaPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.08),transparent_32%)]" />

      <main className="relative z-10 mx-auto max-w-4xl px-6 py-24 sm:px-8 sm:py-32 lg:px-12">
        {/* Header */}
        <div className="mb-16 space-y-4">
          <p className="inline-block rounded-full border border-cyan-400/20 bg-cyan-400/10 px-3 py-1 text-sm font-extrabold uppercase tracking-[0.35em] text-cyan-300">
            Beta
          </p>
          <h1 className="text-4xl font-black leading-[1.05] tracking-tight text-white sm:text-5xl lg:text-6xl uppercase">
            Beta <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-cyan-300 to-purple-400">Testing</span>
          </h1>
          <p className="max-w-2xl text-base leading-8 text-slate-300">
            Get early access to upcoming features, test new builds before public release, and help shape the future of Jump Droid.
          </p>
        </div>

        <div className="space-y-8">
          {/* What is Beta */}
          <section className="scroll-mt-24 rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-8 backdrop-blur-md transition hover:border-cyan-400/20">
            <h2 className="mb-4 text-lg font-bold tracking-wide text-white uppercase">
              About the Program
            </h2>
            <div className="space-y-3 text-sm leading-relaxed text-slate-300">
              <p>
                The Jump Droid Beta Testing Program gives you early access to experimental builds before they reach the public release channel.
                As a beta tester, you will:
              </p>
              <ul className="list-disc pl-5 space-y-1">
                <li>Try new features, rocket classes, and zones before anyone else</li>
                <li>Report bugs and provide feedback that directly influences development</li>
                <li>Help us stress-test balance changes and new mechanics</li>
                <li>Receive special recognition in the Codex as an early supporter</li>
              </ul>
            </div>
          </section>

          {/* How to Join */}
          <section className="scroll-mt-24 rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-8 backdrop-blur-md transition hover:border-cyan-400/20">
            <h2 className="mb-4 text-lg font-bold tracking-wide text-white uppercase">
              How to Join
            </h2>
            <div className="space-y-4 text-sm leading-relaxed text-slate-300">
              <ol className="list-decimal pl-5 space-y-2">
                <li>Tap the button below to open the Google Play Beta page</li>
                <li>Click &quot;Become a Tester&quot; and confirm your participation</li>
                <li>Wait a few minutes for the beta opt-in to propagate</li>
                <li>Open Google Play, search for Jump Droid, and update to the latest beta build</li>
              </ol>
              <p className="text-xs text-slate-400">
                Beta builds are updated frequently. You can leave the program at any time from the same Google Play page.
              </p>
            </div>
          </section>

          {/* CTA */}
          <div className="flex flex-col items-center gap-6 rounded-3xl border border-cyan-400/20 bg-cyan-400/5 p-10 text-center">
            <p className="text-xl font-bold text-white tracking-wide uppercase">
              Ready to Test the Void?
            </p>
            <p className="max-w-md text-sm text-slate-300">
              Join the beta program and help us refine the ascent. Your feedback shapes every update.
            </p>
            <a
              href={BETA_TESTING_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-full bg-cyan-400 px-8 py-4 text-sm font-black uppercase tracking-[0.2em] text-slate-950 transition hover:bg-cyan-300 hover:shadow-[0_0_30px_rgba(0,229,255,0.4)]"
              aria-label="Become a Jump Droid beta tester on Google Play"
            >
              Become a Beta Tester
            </a>
          </div>

          {/* Back link */}
          <div className="text-center">
            <Link
              href="/"
              className="text-sm text-cyan-300 underline underline-offset-2 hover:text-cyan-100 transition"
            >
              &larr; Back to home
            </Link>
          </div>
        </div>

        <p className="mt-16 text-center text-xs text-slate-500">
          Jump Droid — The Signal From the Void.
        </p>
      </main>
    </div>
  );
}
