import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Data Handling Protocol — Jump Droid",
  description:
    "Jump Droid data handling protocol. Learn how we collect, use, and protect your data in this free, open-source Android arcade game.",
  openGraph: {
    title: "Data Handling Protocol — Jump Droid",
    description:
      "Jump Droid data handling protocol. Learn how we collect, use, and protect your data in this free, open-source Android arcade game.",
    type: "website",
    url: "https://jump-droid.vercel.app/privacy",
    siteName: "Jump Droid",
    images: [
      {
        url: "/icon.png",
        width: 256,
        height: 256,
      },
    ],
  },
  twitter: {
    card: "summary",
    title: "Data Handling Protocol — Jump Droid",
    description:
      "Jump Droid data handling protocol. Learn how we collect, use, and protect your data in this free, open-source Android arcade game.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

const sections = [
  {
    id: "effective-date",
    title: "Effective Date",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          This protocol is effective as of <strong className="text-slate-200">July 29, 2026</strong>.
          Updates will be posted with a revised effective date.
        </p>
      </div>
    ),
  },
  {
    id: "information-collected",
    title: "Information Collected",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Jump Droid collects minimal data necessary to improve the experience, fix anomalies,
          and serve advertisements. The game is fully playable without an account.
        </p>
        <p>The following data may be collected automatically or voluntarily:</p>
      </div>
    ),
  },
  {
    id: "google-sign-in",
    title: "Optional Google Sign-In",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Connecting a Google account is <strong className="text-slate-200">optional</strong>. If you choose to connect,
          we may collect:
        </p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40">Google account identifier (unique ID)</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Cloud save data (game progress, unlocks)</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Achievements and leaderboard entries</li>
        </ul>
        <p>
            This data is used solely to enable online features. You can request deletion of this data at any time via the{" "}
            <Link href="/account-deletion" className="text-cyan-400/70 hover:text-cyan-300 underline underline-offset-2 transition-colors">
                Account Deletion page
            </Link>.
        </p>
      </div>
    ),
  },
  {
    id: "firebase-analytics",
    title: "Firebase Analytics",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Jump Droid uses <strong className="text-slate-200">Firebase Analytics</strong> to collect anonymised usage data such as:
        </p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40">Session duration and engagement metrics</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Screen views and feature interactions</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">In-app events (level reached, boss defeated)</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Device language and region settings</li>
        </ul>
        <p>
          This data is aggregated and used solely to understand how players interact with the game.
          It does <strong className="text-slate-200">not</strong> transmit your identity.
        </p>
      </div>
    ),
  },
  {
    id: "firebase-crashlytics",
    title: "Firebase Crashlytics",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Jump Droid uses <strong className="text-slate-200">Firebase Crashlytics</strong> to diagnose performance issues.
          When an anomaly occurs, it collects:
        </p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40">Crash stack traces and error logs</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Device model and OS version</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">App version and build number</li>
        </ul>
      </div>
    ),
  },
  {
    id: "google-admob",
    title: "Google AdMob",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Jump Droid displays ads via <strong className="text-slate-200">Google AdMob</strong>. AdMob may
          collect:
        </p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40">Device advertising ID</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">IP address (coarse location)</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Ad interactions</li>
        </ul>
        <p>
          Learn more at{" "}
          <a
            href="https://policies.google.com/privacy"
            target="_blank"
            rel="noopener noreferrer"
            className="text-cyan-400/70 hover:text-cyan-300 underline underline-offset-2 transition-colors"
          >
            policies.google.com/privacy
          </a>.
        </p>
      </div>
    ),
  },
  {
    id: "childrens-privacy",
    title: "Children&apos;s Privacy",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Jump Droid is not directed at children under 13. We do <strong className="text-slate-200">not</strong> knowingly
          collect personal information from children.
        </p>
      </div>
    ),
  },
  {
    id: "third-party",
    title: "Third-Party Services",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>Jump Droid integrates the following third-party services:</p>
        <ul className="space-y-2 pl-4">
          <li>
            <strong className="text-slate-200">Google Firebase</strong> —{" "}
            <a href="https://firebase.google.com/support/privacy" target="_blank" rel="noopener noreferrer"
              className="text-cyan-400/70 hover:text-cyan-300 underline underline-offset-2 transition-colors">
              firebase.google.com/support/privacy
            </a>
          </li>
          <li>
            <strong className="text-slate-200">Google AdMob</strong> —{" "}
            <a href="https://support.google.com/admob/answer/6128543" target="_blank" rel="noopener noreferrer"
              className="text-cyan-400/70 hover:text-cyan-300 underline underline-offset-2 transition-colors">
              support.google.com/admob/answer/6128543
            </a>
          </li>
          <li>
            <strong className="text-slate-200">Google Play Services</strong> —{" "}
            <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer"
              className="text-cyan-400/70 hover:text-cyan-300 underline underline-offset-2 transition-colors">
              policies.google.com/privacy
            </a>
          </li>
        </ul>
      </div>
    ),
  },
  {
    id: "data-security",
    title: "Data Security",
    content: (
      <p className="font-mono text-xs leading-relaxed text-slate-400">
        Industry-standard security measures are implemented. All data transmitted is encrypted using TLS/SSL.
        Aggregated data is access-controlled and retained only as long as necessary.
      </p>
    ),
  },
  {
    id: "user-rights",
    title: "Your Rights",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>Depending on your jurisdiction, you may have the following rights:</p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40"><strong className="text-slate-200">Access</strong> — Request a copy of associated data</li>
          <li className="before:content-['>_'] before:text-cyan-400/40"><strong className="text-slate-200">Deletion</strong> — Request deletion via the <Link href="/account-deletion" className="text-cyan-300/80 hover:text-cyan-300 underline">Account Deletion page</Link></li>
          <li className="before:content-['>_'] before:text-cyan-400/40"><strong className="text-slate-200">Opt-out</strong> — Disable advertising ID in device settings</li>
        </ul>
        <p>
          To exercise these rights: <strong className="text-slate-200">hi.jumpdroid@gmail.com</strong>.
        </p>
      </div>
    ),
  },
  {
    id: "contact",
    title: "Contact Transmitter",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>For questions regarding this protocol:</p>
        <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] px-5 py-3">
          <p className="text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-1">Email</p>
          <a
            href="mailto:hi.jumpdroid@gmail.com"
            className="text-slate-200 hover:text-cyan-300 transition-colors underline underline-offset-2"
          >
            hi.jumpdroid@gmail.com
          </a>
        </div>
      </div>
    ),
  },
];

export default function PrivacyPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-2xl px-6 py-24 sm:px-8 sm:py-32">
        <div className="mb-16 space-y-4">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
            Receiver Protocol
          </p>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Data Handling
            <br />
            <span className="text-cyan-300">Protocol</span>
          </h1>
          <p className="font-mono text-xs leading-relaxed text-slate-400 max-w-lg">
            Last updated: July 29, 2026. This document explains what data Jump Droid
            collects, why, and how you stay in control.
          </p>
        </div>

        <div className="space-y-4">
          {sections.map((s) => (
            <section
              key={s.id}
              id={s.id}
              className="scroll-mt-24 rounded-lg border border-white/5 bg-white/[0.02] p-6 transition hover:border-cyan-400/10"
            >
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                {s.title}
              </h2>
              {s.content}
            </section>
          ))}
        </div>

        <div className="mt-12 flex flex-wrap justify-center gap-6">
            <Link
                href="/"
                className="font-mono text-[10px] tracking-widest text-slate-600 hover:text-cyan-400 transition-colors uppercase"
            >
                Surface
            </Link>
            <Link
                href="/account-deletion"
                className="font-mono text-[10px] tracking-widest text-slate-600 hover:text-cyan-400 transition-colors uppercase"
            >
                Account Deletion
            </Link>
        </div>
      </main>
    </div>
  );
}
