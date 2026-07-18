"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter, usePathname } from "next/navigation";
import { getFirebaseAuth, onAuthChange, fetchAdmin } from "@/lib/firebase/authService";

export default function BetaDashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [status, setStatus] = useState<"checking" | "authorized" | "denied">("checking");
  const [error, setError] = useState<string | null>(null);
  const adminVerifiedRef = useRef(false);

  useEffect(() => {
    const check = async () => {
      const auth = await getFirebaseAuth();
      const { onAuthStateChanged } = await import("firebase/auth");
      const fbUser = await new Promise<any>((resolve) => {
        const unsub = onAuthStateChanged(auth, (u) => { unsub(); resolve(u); });
      });
      if (!fbUser) {
        if (pathname !== "/beta-dashboard/login") {
          router.replace("/beta-dashboard/login");
        } else {
          setStatus("authorized");
        }
        return;
      }

      const user = { uid: fbUser.uid, email: fbUser.email || "", displayName: fbUser.displayName || "" };

      if (pathname === "/beta-dashboard/login") {
        router.replace("/beta-dashboard");
        return;
      }

      if (adminVerifiedRef.current) {
        setStatus("authorized");
        return;
      }

      try {
        const admin = await fetchAdmin(user.uid);
        if (!admin) {
          setError("You do not have admin access. Contact the project owner.");
          setStatus("denied");
          return;
        }
        adminVerifiedRef.current = true;
        setStatus("authorized");
      } catch (e: any) {
        setError(e?.message || "Failed to verify admin access.");
        setStatus("denied");
      }
    };

    check();

    const unsub = onAuthChange((user) => {
      if (!user && pathname !== "/beta-dashboard/login") {
        router.replace("/beta-dashboard/login");
      }
    });

    return unsub;
  }, [router, pathname]);

  if (status === "checking") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-white">
        <p className="font-mono text-xs text-slate-500 animate-pulse">Verifying access...</p>
      </div>
    );
  }

  if (status === "denied") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
        <div className="fixed inset-0 z-0 bg-glow-top-cyan" />
        <main className="relative z-10 mx-auto max-w-md w-full px-6 text-center">
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-red-400 uppercase mb-4">Access Denied</h1>
          <p className="font-mono text-xs text-slate-400 leading-relaxed mb-6">{error || "You do not have permission to access this page."}</p>
          <a
            href="/"
            className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-5 py-3 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
          >
            &larr; Back to Home
          </a>
        </main>
      </div>
    );
  }

  return <>{children}</>;
}
