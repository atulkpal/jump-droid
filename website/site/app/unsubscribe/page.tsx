"use client";

import { useState } from "react";

export default function UnsubscribePage() {
  const [email, setEmail] = useState("");
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      const res = await fetch("/api/unsubscribe", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });
      const data = await res.json();
      if (res.ok && data.success) {
        setDone(true);
      } else {
        setError(data.error || "Unsubscribe failed");
      }
    } catch {
      setError("Network error. Please try again.");
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[#0a0a0f] p-6">
      <div className="w-full max-w-md rounded-lg border border-white/5 bg-white/[0.02] p-8">
        <h1 className="font-mono text-center text-lg tracking-[0.15em] text-cyan-300 uppercase mb-2">
          Unsubscribe
        </h1>

        {done ? (
          <div className="text-center">
            <p className="font-mono text-sm text-green-400 mb-1">
              You have been unsubscribed.
            </p>
            <p className="font-mono text-xs text-slate-500">
              You will no longer receive emails from us.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block font-mono text-[11px] text-slate-500 mb-1.5">
                Enter your email address
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
              />
            </div>
            <button
              type="submit"
              className="w-full rounded-lg border border-red-400/30 px-4 py-3 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10"
            >
              Unsubscribe
            </button>
            {error && (
              <p className="font-mono text-xs text-red-400 text-center">{error}</p>
            )}
          </form>
        )}
      </div>
    </main>
  );
}