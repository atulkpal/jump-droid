"use client";

import { useState, useEffect, useCallback } from "react";
import type { OutreachContact } from "@/types/recruitmentContacts";
import { fetchAllContacts, batchUpdateInvited } from "@/lib/firebase/outreachService";
import { getAuthUrl, checkAuthStatus, disconnectGmail } from "@/lib/firebase/gmailService";
import OutreachDashboardCards from "./OutreachDashboardCards";
import OutreachContactsTable from "./OutreachContactsTable";
import OutreachImportCsv from "./OutreachImportCsv";
import AddManualContact from "./AddManualContact";

export default function OutreachTab() {
  const [contacts, setContacts] = useState<OutreachContact[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [importOpen, setImportOpen] = useState(false);
  const [manualOpen, setManualOpen] = useState(false);
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState<{
    selected: number;
    sent: number;
    failed: number;
    errors: { email: string; error: string }[];
  } | null>(null);
  const [authStatus, setAuthStatus] = useState<boolean | null>(null);

  const loadContacts = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchAllContacts();
      setContacts(data);
    } catch {
      // handled silently
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadContacts();
    checkAuthStatus().then(setAuthStatus);
  }, [loadContacts]);

  const handleImported = () => {
    setImportOpen(false);
    loadContacts();
  };

  const handleDisconnect = async () => {
    try {
      await disconnectGmail();
      setAuthStatus(false);
      setResult(null);
    } catch {
      // handled silently
    }
  };

  const handleManualAdded = () => {
    setManualOpen(false);
    loadContacts();
  };

  const handleSend = async () => {
    const selectedContacts = contacts.filter((c) => selected.has(c.email));
    if (selectedContacts.length === 0) return;

    const authed = await checkAuthStatus();
    if (!authed) {
      const url = getAuthUrl();
      if (url) {
        window.location.href = url;
      } else {
        alert(
          "Google OAuth is not configured. Set NEXT_PUBLIC_GOOGLE_CLIENT_ID in .env.local"
        );
      }
      return;
    }

    setSending(true);
    setResult(null);

    try {
      const res = await fetch("/api/gmail/send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          emails: selectedContacts.map((c) => ({
            email: c.email,
            name: c.name || c.email,
          })),
        }),
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({ error: `HTTP ${res.status}` }));
        setResult({
          selected: selectedContacts.length,
          sent: 0,
          failed: selectedContacts.length,
          errors: [{ email: "", error: errData.error || `HTTP ${res.status}` }],
        });
        return;
      }

      const results: {
        email: string;
        success: boolean;
        error?: string;
      }[] = await res.json();

      if (!Array.isArray(results)) {
        setResult({
          selected: selectedContacts.length,
          sent: 0,
          failed: selectedContacts.length,
          errors: [{ email: "", error: "Unexpected response format" }],
        });
        return;
      }

      const succeeded = results.filter((r) => r.success);
      const failed = results.filter((r) => !r.success);

      setResult({
        selected: selectedContacts.length,
        sent: succeeded.length,
        failed: failed.length,
        errors: failed.map((r) => ({ email: r.email, error: r.error || "Unknown error" })),
      });

      if (succeeded.length > 0) {
        await batchUpdateInvited(
          succeeded.map((r) => r.email)
        );
        await loadContacts();
        setSelected(new Set());
      }
    } catch {
      setResult({
        selected: selectedContacts.length,
        sent: 0,
        failed: selectedContacts.length,
        errors: [],
      });
    } finally {
      setSending(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
          Outreach Dashboard
        </h2>
        <div className="flex gap-3">
          <button
            onClick={() => setManualOpen(true)}
            className="rounded-lg border border-white/10 px-4 py-2 font-mono text-xs tracking-[0.15em] text-slate-300 transition-colors hover:border-white/20 hover:text-white"
          >
            + Manual Entry
          </button>
          <button
            onClick={() => setImportOpen(true)}
            className="rounded-lg border border-cyan-400/30 px-4 py-2 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50"
          >
            Import CSV
          </button>
        </div>
      </div>

      {loading ? (
        <p className="font-mono text-xs text-slate-500">Loading contacts...</p>
      ) : (
        <>
          <section className="mb-6">
            <OutreachDashboardCards contacts={contacts} />
          </section>

          {authStatus === true && (
            <section className="mb-6">
              <div className="flex items-center justify-between rounded-lg border border-white/5 bg-white/[0.02] px-4 py-2">
                <span className="font-mono text-[11px] text-green-400/60">
                  Gmail connected
                </span>
                <button
                  onClick={handleDisconnect}
                  className="font-mono text-[11px] text-slate-500 hover:text-red-400 transition-colors"
                >
                  Switch Account
                </button>
              </div>
            </section>
          )}

          <section className="mb-6">
            <OutreachContactsTable
              contacts={contacts}
              selected={selected}
              onSelectionChange={setSelected}
            />
          </section>

          {selected.size > 0 && (
            <section className="mb-6">
              <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] p-4">
                <div className="flex items-center justify-between">
                  <p className="font-mono text-xs text-slate-400">
                    {selected.size} contact{selected.size !== 1 ? "s" : ""} selected
                  </p>
                  <button
                    onClick={handleSend}
                    disabled={sending}
                    className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    {sending
                      ? "Sending..."
                      : authStatus === false
                      ? "Authenticate & Send"
                      : "Send Invitations"}
                  </button>
                </div>
              </div>
            </section>
          )}

          {result && (
            <section className="mb-6">
              <div className="rounded-lg border border-white/5 bg-white/[0.02] p-4">
                <h3 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-3">
                  Results
                </h3>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <p className="font-mono text-[10px] text-slate-500 uppercase">Selected</p>
                    <p className="font-mono text-sm text-white">{result.selected}</p>
                  </div>
                  <div>
                    <p className="font-mono text-[10px] text-slate-500 uppercase">Sent</p>
                    <p className="font-mono text-sm text-green-400">{result.sent}</p>
                  </div>
                  <div>
                    <p className="font-mono text-[10px] text-slate-500 uppercase">Failed</p>
                    <p className="font-mono text-sm text-red-400">{result.failed}</p>
                  </div>
                </div>
                {result.sent + result.failed > 0 && (
                  <p className="font-mono text-[11px] text-slate-400 mt-2">
                    Success rate:{" "}
                    {((result.sent / (result.sent + result.failed)) * 100).toFixed(0)}%
                  </p>
                )}
                {result.errors.length > 0 && (
                  <div className="mt-3 border-t border-red-400/10 pt-3">
                    <p className="font-mono text-[10px] text-red-400/60 uppercase mb-2">
                      Failure Details
                    </p>
                    <div className="max-h-32 overflow-y-auto space-y-1">
                      {result.errors.map((e, i) => (
                        <p key={i} className="font-mono text-[11px] text-red-400/80">
                          {e.email && (
                            <span className="text-red-300">{e.email}</span>
                          )}
                          {e.email && e.error && <span>: </span>}
                          {e.error && <span>{e.error}</span>}
                        </p>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </section>
          )}
        </>
      )}

      {manualOpen && (
        <AddManualContact onAdded={handleManualAdded} onClose={() => setManualOpen(false)} />
      )}
      {importOpen && (
        <OutreachImportCsv onImported={handleImported} onClose={() => setImportOpen(false)} />
      )}
    </div>
  );
}
