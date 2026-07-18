"use client";

import { useState, useEffect } from "react";

interface HealthEntry {
  label: string;
  status: "ok" | "warning" | "error" | "info";
  value: string;
}

export default function SystemHealthPage() {
  const [entries, setEntries] = useState<HealthEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const check = async () => {
      try {
        const results: HealthEntry[] = [];

        // Firebase
        try {
          const { getFirestore } = await import("@/lib/firebase/config");
          const firestore = await getFirestore();
          const { doc, getDoc } = await import("firebase/firestore");
          await getDoc(doc(firestore, "appConfig", "bounceDetection"));
          results.push({ label: "Firebase / Firestore", status: "ok", value: "Connected" });
        } catch {
          results.push({ label: "Firebase / Firestore", status: "error", value: "Not reachable" });
        }

        // OAuth config
        try {
          const validRes = await fetch("/api/gmail/validate-config", { method: "POST" });
          const validData = await validRes.json();
          results.push({
            label: "OAuth Configuration",
            status: validData.valid ? "ok" : "error",
            value: validData.valid ? "Configured" : `Missing: ${(validData.missing || []).join(", ")}`,
          });
        } catch {
          results.push({ label: "OAuth Configuration", status: "error", value: "Check failed" });
        }

        // Connected accounts
        try {
          const accountsRes = await fetch("/api/email-accounts");
          const accountsData = await accountsRes.json();
          const count = (accountsData.accounts || []).length;
          const defaultEmail = accountsData.defaultSenderEmail || "None";
          results.push({
            label: "Email Accounts",
            status: count > 0 ? "ok" : "warning",
            value: `${count} connected (Default: ${defaultEmail})`,
          });
        } catch {
          results.push({ label: "Email Accounts", status: "error", value: "Failed to load" });
        }

        // Last campaign run
        try {
          const { collection, getDocs, query, orderBy, limit } = await import("firebase/firestore");
          const { getFirestore } = await import("@/lib/firebase/config");
          const firestore = await getFirestore();

          const q = query(collection(firestore, "emailLog"), orderBy("sentAt", "desc"), limit(1));
          const snap = await getDocs(q);
          if (!snap.empty) {
            const d = snap.docs[0].data();
            const sentAt = d.sentAt?.toDate?.()?.toISOString?.() || d.sentAt?.seconds
              ? new Date(d.sentAt.seconds * 1000).toISOString()
              : "Unknown";
            results.push({ label: "Last Email Sent", status: "info", value: sentAt });
          } else {
            results.push({ label: "Last Email Sent", status: "info", value: "No emails sent yet" });
          }
        } catch {
          results.push({ label: "Last Email Sent", status: "warning", value: "Unable to determine" });
        }

        // Audit log health
        try {
          const { fetchEmailAuditLog } = await import("@/lib/firebase/auditService");
          const auditEntries = await fetchEmailAuditLog(1);
          results.push({
            label: "Email Audit Log",
            status: "ok",
            value: `${auditEntries.length} entries`,
          });
        } catch {
          results.push({ label: "Email Audit Log", status: "warning", value: "Unable to read" });
        }

        // App version
        results.push({
          label: "Application Version",
          status: "info",
          value: "v1.5.2",
        });

        // Scheduler
        results.push({
          label: "Campaign Scheduler",
          status: "info",
          value: "Every 8h (Vercel Cron)",
        });

        setEntries(results);
      } catch (e: any) {
        setError(e?.message || "Health check failed");
      } finally {
        setLoading(false);
      }
    };

    check();
  }, []);

  const statusColor = (status: string) => {
    switch (status) {
      case "ok": return "text-green-400";
      case "warning": return "text-amber-400";
      case "error": return "text-red-400";
      default: return "text-slate-400";
    }
  };

  const statusDot = (status: string) => {
    switch (status) {
      case "ok": return "\u{1F7E2}";
      case "warning": return "\u{1F7E1}";
      case "error": return "\u{1F534}";
      default: return "\u{26AA}";
    }
  };

  return (
    <div>
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-6">
        System Health
      </h2>

      {error && (
        <div className="mb-6 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}

      {loading ? (
        <p className="font-mono text-xs text-slate-500 animate-pulse">Running diagnostics...</p>
      ) : (
        <div className="space-y-2">
          {entries.map((entry, i) => (
            <div
              key={i}
              className="rounded-xl border border-white/10 bg-white/[0.02] px-5 py-4 flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <span>{statusDot(entry.status)}</span>
                <span className="font-mono text-sm text-white">{entry.label}</span>
              </div>
              <span className={`font-mono text-xs ${statusColor(entry.status)}`}>{entry.value}</span>
            </div>
          ))}
        </div>
      )}

      <div className="mt-10 rounded-xl border border-white/5 bg-white/[0.01] px-5 py-4">
        <p className="font-mono text-[10px] text-slate-600 leading-relaxed">
          System health checks run on page load. If any services show errors, check your environment variables
          and Firestore configuration.
        </p>
      </div>
    </div>
  );
}
