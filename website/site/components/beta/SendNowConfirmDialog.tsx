"use client";

import { useState, useRef } from "react";

interface ContactResult {
  email: string;
  success: boolean;
  inviteNumber: number;
  error?: string;
}

interface Props {
  campaignId: string;
  campaignName: string;
  contacts: { email: string; name: string }[];
  onClose: () => void;
  onComplete?: (results: ContactResult[]) => void;
}

export default function SendNowConfirmDialog({ campaignId, campaignName, contacts, onClose, onComplete }: Props) {
  const [step, setStep] = useState<"confirm" | "sending" | "done" | "error">("confirm");
  const [results, setResults] = useState<ContactResult[]>([]);
  const [errorMsg, setErrorMsg] = useState("");
  const [aborted, setAborted] = useState(false);
  const [currentName, setCurrentName] = useState("");
  const [processed, setProcessed] = useState(0);
  const [total, setTotal] = useState(contacts.length);
  const [sentCount, setSentCount] = useState(0);
  const [failedCount, setFailedCount] = useState(0);
  const [etaSeconds, setEtaSeconds] = useState(0);
  const startTimeRef = useRef(0);

  const handleSend = async () => {
    setStep("sending");
    setCurrentName("Starting...");
    setProcessed(0);
    setTotal(contacts.length);
    setSentCount(0);
    setFailedCount(0);
    setEtaSeconds(0);

    const startTime = Date.now();
    startTimeRef.current = startTime;

    try {
      const res = await fetch("/api/campaign/send-now", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          campaignId,
          emails: contacts.map((c) => c.email),
        }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.error || `Server error ${res.status}`);
      }

      const reader = res.body?.getReader();
      if (!reader) throw new Error("Response body not available");

      const decoder = new TextDecoder();
      let buffer = "";
      const resultList: ContactResult[] = [];

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          if (!line.trim()) continue;
          try {
            const data = JSON.parse(line);
            if (data.type === "progress") {
              setCurrentName(data.displayName || data.email);
              setProcessed(data.processed);
              setTotal(data.total);
              setSentCount(data.sent);
              setFailedCount(data.failed);
              const elapsed = Date.now() - startTimeRef.current;
              const perContact = data.processed > 0 ? elapsed / data.processed : 0;
              const remaining = (data.total - data.processed) * perContact;
              setEtaSeconds(Math.round(remaining / 1000));
            } else if (data.type === "complete") {
              resultList.push(...(data.results || []));
              setResults(resultList);
              setCurrentName("");
              onComplete?.(resultList);
              setStep("done");
              return;
            }
          } catch {}
        }
      }

      // Stream ended without "complete" event
      setErrorMsg("Connection closed before completion. Some emails may have been sent.");
      setStep("error");
    } catch (e: any) {
      setErrorMsg(e?.message || "Send failed");
      setStep("error");
    }
  };

  const handleStop = async () => {
    setAborted(true);
    try {
      const { getFirestore } = await import("@/lib/firebase/config");
      const { doc, updateDoc } = await import("firebase/firestore");
      const firestore = await getFirestore();
      await updateDoc(doc(firestore, "campaigns", campaignId), {
        "manualSendOverride.active": false,
      });
    } catch {}
    onClose();
  };

  const sent = results.filter((r) => r.success).length;
  const failed = results.filter((r) => !r.success).length;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center" onClick={() => { if (step !== "sending") onClose(); }}>
      <div className="fixed inset-0 bg-black/70" />
      <div
        className="relative w-full max-w-lg mx-4 rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {step === "confirm" && (
          <>
            <div className="flex items-center gap-3 mb-4">
              <span className="text-2xl text-red-400 font-bold">{'\u26A0'}</span>
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-red-300 uppercase">
                Force Send Next Email
              </h2>
            </div>

            <div className="space-y-3 mb-6">
              <p className="font-mono text-xs text-slate-300">
                This will immediately send the next email in the campaign sequence to{" "}
                <span className="text-white font-bold">{total} contact{total !== 1 ? "s" : ""}</span>{" "}
                in &ldquo;<span className="text-cyan-300">{campaignName}</span>&rdquo;.
              </p>

              <div className="rounded-lg border border-red-400/20 bg-red-400/5 p-3 space-y-2">
                <p className="font-mono text-[11px] text-red-300 font-semibold">Risks</p>
                <ul className="list-disc list-inside font-mono text-[11px] text-red-200/80 space-y-1">
                  <li>Each contact&apos;s campaign stage advances (invite count, step)</li>
                  <li>Overrides the normal schedule timing for these contacts</li>
                  <li>Batch size and delay rules from campaign config will apply</li>
                  <li>This action cannot be undone</li>
                </ul>
              </div>

              <p className="font-mono text-[10px] text-slate-500">
                A STOP button will appear during processing.
              </p>
            </div>

            <div className="flex items-center justify-end gap-3">
              <button
                onClick={onClose}
                className="rounded-lg border border-white/10 px-4 py-2 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={handleSend}
                className="rounded-lg border border-red-400/30 px-4 py-2 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10"
              >
                I Understand, Send Now
              </button>
            </div>
          </>
        )}

        {step === "sending" && (
          <>
            <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4 text-center">
              Sending...
            </h2>

            <div className="space-y-4 mb-6">
              <div className="w-full h-2 rounded-full bg-white/5 overflow-hidden">
                <div
                  className="h-full rounded-full bg-cyan-400 transition-all duration-300"
                  style={{ width: `${total > 0 ? Math.round((processed / total) * 100) : 0}%` }}
                />
              </div>

              <p className="font-mono text-xs text-slate-300 text-center">
                {processed} / {total}
                {currentName && <span className="text-slate-500"> &middot; Now: {currentName}</span>}
              </p>

              <div className="flex items-center justify-center gap-6">
                <div className="text-center">
                  <p className="font-mono text-sm text-green-400">{sentCount}</p>
                  <p className="font-mono text-[10px] text-slate-500 uppercase tracking-[0.05em]">Sent</p>
                </div>
                <div className="text-center">
                  <p className="font-mono text-sm text-red-400">{failedCount}</p>
                  <p className="font-mono text-[10px] text-slate-500 uppercase tracking-[0.05em]">Failed</p>
                </div>
                <div className="text-center">
                  <p className="font-mono text-sm text-white">{total - processed}</p>
                  <p className="font-mono text-[10px] text-slate-500 uppercase tracking-[0.05em]">Remaining</p>
                </div>
              </div>

              {processed > 0 && etaSeconds > 0 && (
                <p className="font-mono text-xs text-slate-500 text-center animate-pulse">
                  ETA: ~{etaSeconds >= 60 ? `${Math.floor(etaSeconds / 60)}m ${etaSeconds % 60}s` : `${etaSeconds}s`}
                </p>
              )}
            </div>

            <div className="flex items-center justify-center gap-3">
              <button
                onClick={handleStop}
                disabled={aborted}
                className="rounded-lg border border-red-400/30 px-4 py-2 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10 disabled:opacity-30 disabled:cursor-not-allowed"
              >
                {aborted ? "Stopping..." : "STOP"}
              </button>
            </div>
          </>
        )}

        {step === "done" && (
          <>
            <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
              Complete
            </h2>

            <div className="space-y-3 mb-6">
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs text-slate-400">Sent</span>
                <span className="font-mono text-sm text-green-400">{sent}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs text-slate-400">Failed</span>
                <span className="font-mono text-sm text-red-400">{failed}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs text-slate-400">Total</span>
                <span className="font-mono text-sm text-white">{results.length}</span>
              </div>
            </div>

            {failed > 0 && (
              <div className="mb-4 max-h-32 overflow-y-auto space-y-1 border-t border-red-400/10 pt-3">
                <p className="font-mono text-[10px] text-red-400/60 uppercase mb-2">Failures</p>
                {results.filter((r) => !r.success).map((r, i) => (
                  <p key={i} className="font-mono text-[11px] text-red-400/80">
                    <span className="text-red-300">{r.email}</span>: {r.error}
                  </p>
                ))}
              </div>
            )}

            <div className="flex items-center justify-end gap-3">
              <button
                onClick={onClose}
                className="rounded-lg border border-cyan-400/30 px-6 py-2 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10"
              >
                Done
              </button>
            </div>
          </>
        )}

        {step === "error" && (
          <>
            <div className="flex items-center gap-3 mb-4">
              <span className="text-2xl text-red-400 font-bold">{'\u26A0'}</span>
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-red-300 uppercase">
                Send Failed
              </h2>
            </div>

            <div className="rounded-lg border border-red-400/20 bg-red-400/5 p-3 mb-6">
              <p className="font-mono text-[11px] text-red-300">{errorMsg}</p>
            </div>

            <div className="flex items-center justify-end gap-3">
              <button
                onClick={onClose}
                className="rounded-lg border border-white/10 px-4 py-2 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
              >
                Close
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
