import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Privacy Policy — Jump Droid",
  description:
    "Jump Droid privacy policy. Learn how we collect, use, and protect your data when you play our mobile game.",
  openGraph: {
    title: "Privacy Policy — Jump Droid",
    description:
      "Jump Droid privacy policy. Learn how we collect, use, and protect your data when you play our mobile game.",
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
    title: "Privacy Policy — Jump Droid",
    description:
      "Jump Droid privacy policy. Learn how we collect, use, and protect your data when you play our mobile game.",
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
      <p className="text-slate-300 text-sm leading-relaxed">
        This Privacy Policy is effective as of <strong className="text-white">July 14, 2026</strong>.
        We may update this policy from time to time. Changes will be posted on this page with an updated effective date.
      </p>
    ),
  },
  {
    id: "information-collected",
    title: "Information We Collect",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>
          Jump Droid collects minimal data necessary to improve the game experience, fix crashes,
          and serve advertisements. We do <strong className="text-white">not</strong> collect your name, email address,
          or any personally identifiable information unless you voluntarily contact us.
        </p>
        <p>The following types of data may be collected automatically:</p>
      </div>
    ),
  },
  {
    id: "firebase-analytics",
    title: "Firebase Analytics",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>
          Jump Droid uses <strong className="text-white">Firebase Analytics</strong>, a service provided by Google LLC.
          Firebase Analytics collects anonymised usage data such as:
        </p>
        <ul className="list-disc pl-5 space-y-1">
          <li>Session duration and engagement metrics</li>
          <li>Screen views and feature interactions</li>
          <li>In-app events (e.g., level reached, boss defeated, module unlocked)</li>
          <li>Device language and region settings</li>
        </ul>
        <p>
          This data is aggregated and used solely to understand how players interact with the game
          so we can prioritise improvements. Firebase Analytics does <strong className="text-white">not</strong> transmit
          your identity to us.
        </p>
      </div>
    ),
  },
  {
    id: "firebase-crashlytics",
    title: "Firebase Crashlytics",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>
          Jump Droid uses <strong className="text-white">Firebase Crashlytics</strong> to monitor and diagnose crashes
          and performance issues. When the app crashes, Crashlytics collects:
        </p>
        <ul className="list-disc pl-5 space-y-1">
          <li>Crash stack traces and error logs</li>
          <li>Device model, OS version, and available memory</li>
          <li>Timestamp of the crash event</li>
          <li>App version and build number</li>
        </ul>
        <p>
          This data helps us identify and fix stability issues quickly. It is <strong className="text-white">not</strong> linked
          to any personal identifier and is retained only as long as necessary for debugging.
        </p>
      </div>
    ),
  },
  {
    id: "google-admob",
    title: "Google AdMob",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>
          Jump Droid displays advertisements via <strong className="text-white">Google AdMob</strong>. AdMob may
          collect and process data to serve relevant ads. This includes:
        </p>
        <ul className="list-disc pl-5 space-y-1">
          <li>Device advertising ID (resettable at any time in your device settings)</li>
          <li>IP address (used for coarse location targeting)</li>
          <li>App installs and interaction with ads</li>
          <li>Device type and connectivity status</li>
        </ul>
        <p>
          AdMob operates under Google&apos;s Privacy Policy. You can learn more about how Google uses data
          at{" "}
          <a
            href="https://policies.google.com/privacy"
            target="_blank"
            rel="noopener noreferrer"
            className="text-cyan-300 underline underline-offset-2 hover:text-cyan-100 transition"
          >
            policies.google.com/privacy
          </a>
          .
        </p>
      </div>
    ),
  },
  {
    id: "device-information",
    title: "Device Information",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>The following device-level information may be collected by our third-party services:</p>
        <ul className="list-disc pl-5 space-y-1">
          <li>Operating system version (e.g., Android 14, 15)</li>
          <li>Device make and model</li>
          <li>Screen resolution and density</li>
          <li>Available storage and memory (aggregated, anonymised)</li>
          <li>Network type (Wi-Fi, cellular)</li>
        </ul>
        <p>
          This information is used for crash analysis, performance optimisation, and ad targeting
          as described above. We do <strong className="text-white">not</strong> combine this data with any personal identifier.
        </p>
      </div>
    ),
  },
  {
    id: "no-account",
    title: "No Account Creation",
    content: (
      <p className="text-slate-300 text-sm leading-relaxed">
        Jump Droid does <strong className="text-white">not</strong> require you to create an account, register an email address,
        or provide any personal information to play the game. All game progress is stored locally on your device.
        There is no login system, no profile system, and no multiplayer or social features that would require account creation.
      </p>
    ),
  },
  {
    id: "childrens-privacy",
    title: "Children&apos;s Privacy",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>
          Jump Droid is not directed at children under the age of 13. We do <strong className="text-white">not</strong> knowingly
          collect personal information from children. If you are a parent or guardian and believe your child
          has provided us with personal data, please contact us so we can delete it.
        </p>
        <p>
          In compliance with the Children&apos;s Online Privacy Protection Act (COPPA), we do not serve
          interest-based advertising to users identified as children. AdMob may serve contextual
          (non-personalised) ads in such cases.
        </p>
      </div>
    ),
  },
  {
    id: "third-party",
    title: "Third-Party Services",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>Jump Droid integrates the following third-party services, each governed by its own privacy policy:</p>
        <ul className="list-disc pl-5 space-y-2">
          <li>
            <strong className="text-white">Google Firebase</strong> (Analytics & Crashlytics) —{" "}
            <a
              href="https://firebase.google.com/support/privacy"
              target="_blank"
              rel="noopener noreferrer"
              className="text-cyan-300 underline underline-offset-2 hover:text-cyan-100 transition"
            >
              firebase.google.com/support/privacy
            </a>
          </li>
          <li>
            <strong className="text-white">Google AdMob</strong> —{" "}
            <a
              href="https://support.google.com/admob/answer/6128543"
              target="_blank"
              rel="noopener noreferrer"
              className="text-cyan-300 underline underline-offset-2 hover:text-cyan-100 transition"
            >
              support.google.com/admob/answer/6128543
            </a>
          </li>
          <li>
            <strong className="text-white">Google Play Services</strong> —{" "}
            <a
              href="https://policies.google.com/privacy"
              target="_blank"
              rel="noopener noreferrer"
              className="text-cyan-300 underline underline-offset-2 hover:text-cyan-100 transition"
            >
              policies.google.com/privacy
            </a>
          </li>
        </ul>
        <p>
          We encourage you to review the privacy policies of these third-party providers for detailed
          information about their data handling practices.
        </p>
      </div>
    ),
  },
  {
    id: "data-security",
    title: "Data Security",
    content: (
      <p className="text-slate-300 text-sm leading-relaxed">
        We implement industry-standard security measures to protect your data. All data transmitted
        to Firebase and AdMob is encrypted using TLS/SSL protocols. Data stored on our servers
        (limited to aggregated analytics and crash reports) is access-controlled and retained only
        as long as necessary for the purposes described in this policy. Despite these measures,
        no method of electronic storage or transmission is 100% secure.
      </p>
    ),
  },
  {
    id: "user-rights",
    title: "Your Rights",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>Depending on your jurisdiction, you may have the following rights regarding your data:</p>
        <ul className="list-disc pl-5 space-y-1">
          <li><strong className="text-white">Access</strong> — Request a copy of data associated with your device</li>
          <li><strong className="text-white">Deletion</strong> — Request deletion of analytics and crash data linked to your device</li>
          <li><strong className="text-white">Opt-out of Ads Personalisation</strong> — Disable advertising ID in your device settings (Android: Settings → Privacy → Ads)</li>
          <li><strong className="text-white">Withdraw Consent</strong> — Uninstall the app to stop all data collection</li>
        </ul>
        <p>
          To exercise these rights, contact us at{" "}
          <strong className="text-white">ashwathai.dev@gmail.com</strong>. We will respond within 30 days.
        </p>
      </div>
    ),
  },
  {
    id: "policy-updates",
    title: "Policy Updates",
    content: (
      <p className="text-slate-300 text-sm leading-relaxed">
        We may update this Privacy Policy from time to time. Changes will be posted on this page with an
        updated effective date. We encourage you to review this policy periodically. Continued use of
        Jump Droid after changes constitutes acceptance of the updated policy. Material changes will be
        communicated via an in-app notice or a prominent note on our website.
      </p>
    ),
  },
  {
    id: "contact",
    title: "Contact Us",
    content: (
      <div className="space-y-3 text-slate-300 text-sm leading-relaxed">
        <p>
          If you have questions, concerns, or requests regarding this Privacy Policy or how we handle your data,
          please contact us:
        </p>
        <div className="rounded-2xl border border-cyan-400/20 bg-cyan-400/5 px-6 py-4">
          <p className="text-sm font-bold text-cyan-200 uppercase tracking-wider">Email</p>
          <a
            href="mailto:ashwathai.dev@gmail.com"
            className="text-base text-white hover:text-cyan-300 transition underline underline-offset-2"
          >
            ashwathai.dev@gmail.com
          </a>
        </div>
      </div>
    ),
  },
];

function SectionCard({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="scroll-mt-24 rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-8 backdrop-blur-md transition hover:border-cyan-400/20">
      <h2 className="mb-4 text-lg font-bold tracking-wide text-white uppercase">
        {title}
      </h2>
      {children}
    </section>
  );
}

export default function PrivacyPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      {/* Background gradient */}
      <div className="fixed inset-0 z-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.08),transparent_32%)]" />

      {/* Content */}
      <main className="relative z-10 mx-auto max-w-4xl px-6 py-24 sm:px-8 sm:py-32 lg:px-12">
        {/* Header */}
        <div className="mb-16 space-y-4">
          <p className="inline-block rounded-full border border-cyan-400/20 bg-cyan-400/10 px-3 py-1 text-sm font-extrabold uppercase tracking-[0.35em] text-cyan-300">
            Legal
          </p>
          <h1 className="text-4xl font-black leading-[1.05] tracking-tight text-white sm:text-5xl lg:text-6xl uppercase">
            Privacy <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-cyan-300 to-purple-400">Policy</span>
          </h1>
          <p className="max-w-2xl text-base leading-8 text-slate-300">
            Last updated: July 14, 2026. Your privacy matters. This document explains what data Jump Droid
            collects, why, and how you stay in control.
          </p>
        </div>

        {/* Policy sections */}
        <div className="space-y-6">
          {sections.map((s) => (
            <SectionCard key={s.id} id={s.id} title={s.title}>
              {s.content}
            </SectionCard>
          ))}
        </div>

        {/* Footer note */}
        <p className="mt-16 text-center text-xs text-slate-500">
          Jump Droid — The Signal From the Void.
        </p>
      </main>
    </div>
  );
}
