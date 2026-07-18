"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { signInWithGoogle, getCurrentUser } from "@/lib/firebase/authService";

export default function AdminLoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getCurrentUser().then((user) => {
      if (user) router.replace("/beta-dashboard");
      else setChecking(false);
    });
  }, [router]);

  const handleLogin = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      await signInWithGoogle();
      router.push("/beta-dashboard");
    } catch (e: any) {
      const msg =
        e?.code === "auth/popup-closed-by-user"
          ? "Sign-in cancelled."
          : e?.message || "Authentication failed. Please try again.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [router]);

  if (checking) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-white">
        <p className="font-mono text-xs text-slate-500 animate-pulse">Checking authentication...</p>
      </div>
    );
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-md w-full px-6">
        <div className="text-center mb-10">
          <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase mb-3">
            Admin Terminal
          </p>
          <h1 className="font-mono text-2xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
            Sign In
          </h1>
          <p className="font-mono text-xs text-slate-500 mt-3 max-w-sm mx-auto leading-relaxed">
            Sign in with your Google account to access the beta dashboard.
          </p>
        </div>

        <div className="rounded-xl border border-white/10 bg-white/[0.02] p-8">
          <button
            onClick={handleLogin}
            disabled={loading}
            className="w-full flex items-center justify-center gap-3 rounded-lg border border-white/20 bg-white/5 px-6 py-4 font-mono text-sm text-white transition-all hover:bg-white/10 hover:border-white/30 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <span className="animate-pulse">Signing in...</span>
            ) : (
              <>
                <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                <span>Sign in with Google</span>
              </>
            )}
          </button>

          {error && (
            <p className="font-mono text-xs text-red-400 mt-4 text-center">{error}</p>
          )}
        </div>

        <p className="font-mono text-[10px] text-slate-600 text-center mt-8">
          &copy; {new Date().getFullYear()} Ashwath AI &middot; Jump Droid
        </p>
      </main>
    </div>
  );
}
