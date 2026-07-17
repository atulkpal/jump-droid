"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function GmailCallbackPageWrapper() {
  return (
    <Suspense fallback={<div className="flex min-h-screen items-center justify-center bg-black text-white"><p className="font-mono text-xs text-slate-500">Loading...</p></div>}>
      <GmailCallbackPage />
    </Suspense>
  );
}

function GmailCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [status, setStatus] = useState("Exchanging authorization code...");

  useEffect(() => {
    const code = searchParams.get("code");
    const error = searchParams.get("error");

    if (error) {
      setStatus(`Authorization failed: ${error}`);
      return;
    }

    if (!code) {
      setStatus("No authorization code received.");
      return;
    }

    fetch("/api/gmail/exchange", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.success) {
          setStatus("Authenticated successfully. Redirecting...");
          setTimeout(() => router.push("/beta-dashboard/recruitment?tab=outreach"), 1500);
        } else {
          setStatus(data.error || "Authentication failed.");
        }
      })
      .catch((e) => {
        setStatus(`Authentication failed: ${e.message}`);
      });
  }, [searchParams, router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-black text-white">
      <p className="font-mono text-xs text-slate-400">{status}</p>
    </div>
  );
}
