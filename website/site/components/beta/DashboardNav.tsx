"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import { signOut as firebaseSignOut } from "@/lib/firebase/authService";
import { useAuth } from "./AuthContext";

const ROLE_STYLES: Record<string, string> = {
  owner: "border-cyan-400/30 bg-cyan-400/10 text-cyan-300",
  admin: "border-violet-400/30 bg-violet-400/10 text-violet-300",
  user: "border-slate-500/30 bg-slate-500/10 text-slate-300",
};

const NAV_ITEMS = [
  { href: "/beta-dashboard", label: "Dashboard", icon: "▦" },
  { href: "/beta-dashboard/recruitment", label: "Recruitment", icon: "⊞" },
  { href: "/beta-dashboard/campaigns", label: "Outreach", icon: "◎" },
  { href: "/beta-dashboard/email", label: "Email Templates", icon: "✉" },
  { href: "/beta-dashboard/settings", label: "Settings", icon: "⚙" },
];

export default function DashboardNav() {
  const pathname = usePathname();
  const { user } = useAuth();
  const [signingOut, setSigningOut] = useState(false);

  const handleSignOut = async () => {
    setSigningOut(true);
    try {
      await firebaseSignOut();
    } catch {
      setSigningOut(false);
    }
  };

  return (
    <nav className="sticky top-0 z-40 w-full border-b border-white/5 bg-black/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-3 sm:px-8">
        <div className="flex items-center gap-1 sm:gap-2">
          {NAV_ITEMS.map((item) => {
            const isActive =
              item.href === "/beta-dashboard"
                ? pathname === "/beta-dashboard"
                : pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-1.5 rounded-lg px-3 py-2 font-mono text-xs tracking-[0.1em] transition-colors ${
                  isActive
                    ? "bg-cyan-400/10 text-cyan-300"
                    : "text-slate-500 hover:text-white hover:bg-white/5"
                }`}
              >
                <span className="text-[11px]">{item.icon}</span>
                <span className="hidden sm:inline">{item.label}</span>
              </Link>
            );
          })}
        </div>
        <div className="flex items-center gap-3">
          {user && (
            <div className="hidden sm:flex items-center gap-2">
              <span className="font-mono text-[11px] text-slate-500">{user.email}</span>
              <span
                className={`rounded-full border px-2.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.1em] ${
                  ROLE_STYLES[user.role] ?? "border-white/10 text-slate-400"
                }`}
              >
                {user.role}
              </span>
            </div>
          )}
          <button
            onClick={handleSignOut}
            disabled={signingOut}
            className="rounded-lg px-3 py-2 font-mono text-xs tracking-[0.1em] text-slate-500 transition-colors hover:text-red-400 disabled:opacity-50"
          >
            {signingOut ? "..." : "Sign Out"}
          </button>
        </div>
      </div>
    </nav>
  );
}
