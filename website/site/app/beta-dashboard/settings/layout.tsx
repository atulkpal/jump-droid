"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const SETTINGS_TABS = [
  { label: "Email Accounts", href: "/beta-dashboard/settings/email-accounts" },
  { label: "Config", href: "/beta-dashboard/settings/config" },
  { label: "Admins", href: "/beta-dashboard/settings/admins" },
  { label: "Campaign", href: "/beta-dashboard/settings/campaign" },
  { label: "System", href: "/beta-dashboard/settings/system" },
];

export default function SettingsLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-5xl px-6 py-24 sm:px-8 sm:py-32">
        <div className="mb-10">
          <Link
            href="/beta-dashboard"
            className="font-mono text-xs text-slate-500 hover:text-white transition-colors mb-6 inline-block"
          >
            &larr; Back to Dashboard
          </Link>
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase mt-4">
            Settings
          </h1>
        </div>

        <div className="flex gap-1 border-b border-white/10 mb-8">
          {SETTINGS_TABS.map((tab) => {
            const isActive = pathname === tab.href;
            return (
              <Link
                key={tab.href}
                href={tab.href}
                className={`px-5 py-3 font-mono text-xs tracking-[0.15em] uppercase transition-colors border-b-2 ${
                  isActive
                    ? "text-cyan-300 border-cyan-400"
                    : "text-slate-500 border-transparent hover:text-slate-300"
                }`}
              >
                {tab.label}
              </Link>
            );
          })}
        </div>

        {children}
      </main>
    </div>
  );
}
