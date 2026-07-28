"use client";

import { useState, useEffect, useRef, useCallback } from "react";

export default function CampaignLogsPage() {
  const [logs, setLogs] = useState<string>("");
  const [running, setRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const scrollRef = useRef<HTMLPreElement>(null);
  const lastLengthRef = useRef(0);

  const fetchLogs = useCallback(async () => {
    try {
      const res = await fetch("/api/campaign/logs");
      if (!res.ok) return;
      const text = await res.text();
      setLogs(text);
    } catch {
      // poll will retry
    }
  }, []);

  // Auto-poll while running
  useEffect(() => {
    if (running) {
      pollRef.current = setInterval(fetchLogs, 1500);
      return () => {
        if (pollRef.current) clearInterval(pollRef.current);
        pollRef.current = null;
      };
    }
  }, [running, fetchLogs]);

  // Initial fetch
  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  // Auto-scroll when new content arrives
  useEffect(() => {
    if (scrollRef.current && logs.length > lastLengthRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
    lastLengthRef.current = logs.length;
  }, [logs]);

  const handleRun = async () => {
    setRunning(true);
    setError(null);
    try {
      await fetch("/api/campaign/trigger", { method: "POST" });
      // Fetch logs one final time after completion
      await fetchLogs();
    } catch (e: any) {
      setError(e?.message || "Run failed");
    } finally {
      setRunning(false);
    }
  };

  const handleClear = async () => {
    try {
      await fetch("/api/campaign/logs", { method: "DELETE" });
      setLogs("");
    } catch {
      // non-critical
    }
  };

  const lines = logs
    .split("\n")
    .filter((l) => l.length > 0);

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-4" style={{ height: "calc(100vh - 120px)" }}>
      <div className="flex items-center justify-between shrink-0">
        <h1 className="font-mono text-sm font-bold tracking-[0.15em] text-cyan-200 uppercase">
          Campaign Logs
        </h1>
        <div className="flex items-center gap-3">
          {error && (
            <p className="font-mono text-xs text-red-400">{error}</p>
          )}
          <button
            onClick={handleClear}
            className="rounded-lg border border-white/10 px-4 py-2 font-mono text-xs text-slate-400 transition-all hover:border-white/20 hover:text-white"
          >
            Clear
          </button>
          <button
            onClick={handleRun}
            disabled={running}
            className="flex items-center gap-2 rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-5 py-2.5 font-mono text-xs tracking-[0.1em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {running ? (
              <>
                <span className="h-2 w-2 animate-pulse rounded-full bg-cyan-400" />
                Running...
              </>
            ) : (
              <>
                <span className="text-base leading-none">&#9654;</span>
                Run Campaign Processor
              </>
            )}
          </button>
        </div>
      </div>

      <pre
        ref={scrollRef}
        className="flex-1 overflow-auto rounded-xl border border-white/5 bg-black/60 p-5 font-mono text-xs leading-relaxed text-slate-300"
        style={{ scrollBehavior: "smooth" }}
      >
        {lines.length === 0 ? (
          <span className="text-slate-600">No logs yet. Click "Run Campaign Processor" to start.</span>
        ) : (
          lines.map((line, i) => {
            let color = "text-slate-300";
            if (line.includes("[ERROR]")) color = "text-red-300";
            else if (line.includes("[WARN]")) color = "text-yellow-300";
            else if (line.includes("✓")) color = "text-green-300";
            else if (line.includes("✗")) color = "text-red-300";
            else if (line.includes("=====")) color = "text-cyan-300 font-bold";
            else if (line.includes("finished") || line.includes("auto-completed")) color = "text-blue-300";
            return (
              <div key={i} className={color}>
                {line}
              </div>
            );
          })
        )}
      </pre>
    </div>
  );
}
