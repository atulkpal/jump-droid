import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Account Deletion Protocol — Jump Droid",
  description:
    "Jump Droid account deletion protocol. Learn how to request deletion of your connected data.",
  openGraph: {
    title: "Account Deletion Protocol — Jump Droid",
    description:
      "Jump Droid account deletion protocol. Learn how to request deletion of your connected data.",
    type: "website",
    url: "https://jump-droid.vercel.app/account-deletion",
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
    title: "Account Deletion Protocol — Jump Droid",
    description:
      "Jump Droid account deletion protocol. Learn how to request deletion of your connected data.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

const sections = [
  {
    id: "overview",
    title: "Overview",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          Jump Droid can be played completely <strong className="text-slate-200">without</strong> creating an account.
          Connecting a Google account is entirely optional and is only used for online features such as cloud saves.
        </p>
        <p>
          If you have chosen to connect an account, you have the right to request its deletion and the removal of all
          associated data from our systems.
        </p>
      </div>
    ),
  },
  {
    id: "data-deleted",
    title: "Data to be Deleted",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>When an account deletion request is processed, the following information will be permanently removed:</p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40">Connected Google account identifier</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Cloud save data and progress snapshots</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Player profile and online statistics</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Community profile information (if applicable)</li>
        </ul>
      </div>
    ),
  },
  {
    id: "data-retained",
    title: "Data Retained",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>
          We may retain limited, non-identifiable information for the minimum period required for:
        </p>
        <ul className="space-y-1 pl-4">
          <li className="before:content-['>_'] before:text-cyan-400/40">Legal obligations and regulatory compliance</li>
          <li className="before:content-['>_'] before:text-cyan-400/40">Fraud prevention and security monitoring</li>
        </ul>
      </div>
    ),
  },
  {
    id: "request-process",
    title: "How to Request Deletion",
    content: (
      <div className="font-mono text-xs leading-relaxed text-slate-400 space-y-3">
        <p>To initiate a deletion request, please contact our support transmitter:</p>
        <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] px-5 py-3">
          <p className="text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-1">Email</p>
          <a
            href="mailto:hi.jumpdroid@gmail.com?subject=Account Deletion Request"
            className="text-slate-200 hover:text-cyan-300 transition-colors underline underline-offset-2"
          >
            hi.jumpdroid@gmail.com
          </a>
          <p className="text-[9px] text-slate-500 mt-2">
            Subject: <span className="text-slate-400 italic">Account Deletion Request</span>
          </p>
        </div>
        <p className="text-[11px]">
          Requests are normally verified and processed within <strong className="text-slate-200">30 days</strong>.
        </p>
      </div>
    ),
  },
  {
    id: "guest-play",
    title: "Continue Playing",
    content: (
      <p className="font-mono text-xs leading-relaxed text-slate-400">
        Deleting your account does <strong className="text-slate-200">not</strong> prevent you from continuing to play Jump Droid.
        You can always return as a guest; however, online features and cross-device synchronization will be disabled.
      </p>
    ),
  },
];

export default function AccountDeletionPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-2xl px-6 py-24 sm:px-8 sm:py-32">
        <div className="mb-16 space-y-4">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
            Data Purge Protocol
          </p>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Account
            <br />
            <span className="text-cyan-300">Deletion</span>
          </h1>
          <p className="font-mono text-xs leading-relaxed text-slate-400 max-w-lg">
            This protocol details the removal of your connected identity and associated telemetry.
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
                href="/privacy"
                className="font-mono text-[10px] tracking-widest text-slate-600 hover:text-cyan-400 transition-colors uppercase"
            >
                Data Handling Protocol
            </Link>
        </div>
      </main>
    </div>
  );
}
