"use client";

import { Suspense, useEffect, useState, useCallback } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function GmailCallbackPageWrapper() {
  return (
    <Suspense fallback={
      <div className="flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
        <p className="font-mono text-xs text-slate-500 animate-pulse">Loading...</p>
      </div>
    }>
      <GmailCallbackPage />
    </Suspense>
  );
}

type CallbackState =
  | { phase: "exchanging" }
  | { phase: "saving" }
  | { phase: "error"; title: string; message: string; detail: string }
  | { phase: "success" }
  | { phase: "config_error"; message: string };

const ERROR_MAP: Record<string, { title: string; message: string }> = {
  "invalid_client": {
    title: "OAuth Configuration Error",
    message: "The Google OAuth client ID or secret is not configured correctly. Contact the project owner to verify the Google Cloud Console credentials.",
  },
  "access_denied": {
    title: "Access Denied",
    message: "You declined the authorization request. No changes were made to your accounts.",
  },
  "redirect_uri_mismatch": {
    title: "Redirect URI Mismatch",
    message: "The redirect URI does not match the allowed URIs in the Google Cloud Console. The OAuth callback URL must be registered exactly as it appears in the browser address bar.",
  },
};

function GmailCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [state, setState] = useState<CallbackState>({ phase: "exchanging" });
  const [countdown, setCountdown] = useState(5);

  const exchange = useCallback(async (code: string) => {
    setState({ phase: "exchanging" });
    try {
      const res = await fetch("/api/gmail/exchange", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code }),
      });

      const data = await res.json();

      if (!data.success) {
        const known = data.error?.includes("invalid_client")
          ? ERROR_MAP["invalid_client"]
          : null;
        setState({
          phase: "error",
          title: known?.title || "Authentication Failed",
          message: known?.message || (data.error || "Unable to connect Gmail. Please try again."),
          detail: known ? "" : (data.error || ""),
        });
        return;
      }

      setState({ phase: "saving" });

      const { getFirebaseAuth } = await import("@/lib/firebase/authService");
      const auth = await getFirebaseAuth();
      const { onAuthStateChanged } = await import("firebase/auth");
      await new Promise<void>((resolve) => {
        const unsub = onAuthStateChanged(auth, () => { unsub(); resolve(); });
      });
      if (!auth.currentUser) {
        setState({
          phase: "error",
          title: "Not Signed In",
          message: "Your session expired during the OAuth flow. Please sign in again and retry.",
          detail: "",
        });
        return;
      }

      const { getFirestore } = await import("@/lib/firebase/config");
      const firestore = await getFirestore();
      const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");

      const accountRef = doc(firestore, "emailAccounts", data.email);
      const existing = await getDoc(accountRef);
      if (existing.exists()) {
        setState({
          phase: "error",
          title: "Account Already Exists",
          message: "This email account is already connected. Use Reconnect if the token has expired.",
          detail: "",
        });
        return;
      }

      const { collection, getDocs } = await import("firebase/firestore");
      const allAccounts = await getDocs(collection(firestore, "emailAccounts"));
      const isDefault = allAccounts.empty;

      await setDoc(accountRef, {
        email: data.email,
        displayName: data.displayName,
        refreshToken: data.refreshToken,
        accessToken: data.accessToken,
        expiryDate: Date.now() + (data.expiresIn || 3600) * 1000,
        status: "connected",
        isDefault,
        createdBy: "owner",
        createdAt: serverTimestamp(),
        lastUsedAt: serverTimestamp(),
        errorMessage: null,
        provider: "gmail",
      });

      const { logEmailAudit } = await import("@/lib/firebase/auditService");
      await logEmailAudit("oauth_connected", data.email, "Gmail account connected via OAuth");

      setState({ phase: "success" });
      let c = 5;
      setCountdown(c);
      const interval = setInterval(() => {
        c--;
        setCountdown(c);
        if (c <= 0) {
          clearInterval(interval);
          router.push("/beta-dashboard/settings/email-accounts");
        }
      }, 1000);
    } catch (e: any) {
      const msg = e?.message || "";
      if (msg.includes("permission") || msg.includes("denied") || msg.includes("Missing")) {
        setState({
          phase: "error",
          title: "Permission Denied",
          message: "Could not save the email account. Make sure you are signed in as an admin and try again.",
          detail: msg,
        });
      } else {
        setState({
          phase: "error",
          title: "Connection Error",
          message: "Could not reach the authentication server. Check your internet connection and try again.",
          detail: msg,
        });
      }
    }
  }, [router]);

  useEffect(() => {
    const code = searchParams.get("code");
    const errorParam = searchParams.get("error");

    if (errorParam) {
      const known = ERROR_MAP[errorParam];
      if (known) {
        setState({ phase: "error", title: known.title, message: known.message, detail: "" });
      } else {
        setState({
          phase: "error",
          title: "Authorization Failed",
          message: "Google returned an error during authorization.",
          detail: errorParam,
        });
      }
      return;
    }

    if (!code) {
      setState({
        phase: "error",
        title: "Missing Authorization Code",
        message: "No authorization code was received from Google. Please try adding your account again.",
        detail: "",
      });
      return;
    }

    exchange(code);
  }, [searchParams, exchange]);

  if (state.phase === "exchanging" || state.phase === "saving") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
        <div className="fixed inset-0 z-0 bg-glow-top-cyan" />
        <main className="relative z-10 text-center">
          <p className="font-mono text-xs text-slate-400 animate-pulse">
            {state.phase === "exchanging" ? "Connecting Gmail account..." : "Saving account..."}
          </p>
        </main>
      </div>
    );
  }

  if (state.phase === "success") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
        <div className="fixed inset-0 z-0 bg-glow-top-cyan" />
        <main className="relative z-10 text-center">
          <p className="font-mono text-sm text-green-400 mb-3">{"\u2705"} Gmail Connected</p>
          <p className="font-mono text-xs text-slate-500">
            Redirecting to Email Accounts in {countdown}s...
          </p>
        </main>
      </div>
    );
  }

  if (state.phase === "config_error") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
        <div className="fixed inset-0 z-0 bg-glow-top-cyan" />
        <main className="relative z-10 mx-auto max-w-md w-full px-6 text-center">
          <div className="rounded-xl border border-amber-400/20 bg-amber-400/10 p-8">
            <p className="font-mono text-sm text-amber-400 mb-4">{"\u26A0\uFE0F"} OAuth Configuration Error</p>
            <p className="font-mono text-xs text-slate-400 leading-relaxed mb-6">{state.message}</p>
            <a
              href="/beta-dashboard/settings/email-accounts"
              className="inline-flex rounded-lg border border-white/10 px-5 py-3 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Back to Email Accounts
            </a>
          </div>
        </main>
      </div>
    );
  }

  // Error state
  return (
    <div className="flex min-h-screen items-center justify-center bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />
      <main className="relative z-10 mx-auto max-w-md w-full px-6 text-center">
        <div className={`rounded-xl border p-8 ${state.phase === "error" ? "border-red-400/20 bg-red-400/5" : "border-white/10 bg-white/[0.02]"}`}>
          <p className={`font-mono text-sm mb-4 ${state.phase === "error" ? "text-red-400" : "text-slate-300"}`}>
            {"\u274C"} {state.title}
          </p>

          <p className="font-mono text-xs text-slate-400 leading-relaxed mb-6">
            {state.phase === "error" ? state.message : ""}
          </p>

          {state.phase === "error" && state.detail && (
            <details className="mb-6 text-left">
              <summary className="font-mono text-[10px] text-slate-600 cursor-pointer hover:text-slate-400">
                Technical Details
              </summary>
              <p className="font-mono text-[10px] text-slate-600 mt-2 break-all bg-white/[0.02] rounded p-2">
                {state.detail}
              </p>
            </details>
          )}

          <div className="flex items-center justify-center gap-3">
            <a
              href="/beta-dashboard/settings/email-accounts"
              className="inline-flex rounded-lg border border-white/10 px-5 py-3 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Back to Email Accounts
            </a>
            <a
              href="/beta-dashboard/settings/email-accounts"
              onClick={(e) => {
                e.preventDefault();
                const url = new URL(window.location.origin + "/gmail/callback");
                // Re-trigger OAuth by going back to add page
                window.location.href = "/beta-dashboard/settings/email-accounts";
              }}
              className="inline-flex rounded-lg border border-cyan-400/30 px-5 py-3 font-mono text-xs text-cyan-300 transition-colors hover:bg-cyan-400/10"
            >
              Retry
            </a>
          </div>
        </div>
      </main>
    </div>
  );
}
