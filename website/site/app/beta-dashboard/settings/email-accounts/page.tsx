"use client";

import { useState, useEffect, useCallback } from "react";
import { getAuthUrl } from "@/lib/firebase/gmailService";
import { getFirestore } from "@/lib/firebase/config";
import { useRole } from "@/components/beta/AuthContext";

interface Account {
  email: string;
  displayName: string;
  status: string;
  isDefault: boolean;
  errorMessage: string | null;
  provider: string;
}

export default function EmailAccountsPage() {
  const { role } = useRole();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [defaultEmail, setDefaultEmail] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [configValid, setConfigValid] = useState<boolean | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const loadAccounts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const firestore = await getFirestore();
      const { collection, getDocs, query, orderBy } = await import("firebase/firestore");
      const q = query(collection(firestore, "emailAccounts"), orderBy("createdAt", "desc"));
      const snap = await getDocs(q);
      const list: Account[] = snap.docs
        .map((d: any) => {
          const data = d.data();
          return {
            email: data.email || d.id,
            displayName: data.displayName || "",
            status: data.status || "connected",
            isDefault: data.isDefault || false,
            errorMessage: data.errorMessage || null,
            provider: data.provider || "gmail",
          };
        })
        .filter((a) => a.status !== "deleted" && a.email && a.email !== "unknown");
      setAccounts(list);
      const defaultAcc = list.find((a) => a.isDefault);
      setDefaultEmail(defaultAcc?.email || null);
    } catch (e: any) {
      setError(e?.message || "Failed to load accounts");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAccounts();
  }, [loadAccounts]);

  const handleAddAccount = useCallback(async () => {
    setError(null);
    try {
      const validRes = await fetch("/api/gmail/validate-config", { method: "POST" });
      const validData = await validRes.json();
      if (!validData.valid) {
        setConfigValid(false);
        setError(validData.error || "OAuth configuration is incomplete.");
        return;
      }
      setConfigValid(true);
      const url = getAuthUrl();
      if (!url) {
        setError("Failed to generate authentication URL.");
        return;
      }
      window.location.href = url;
    } catch (e: any) {
      setError(e?.message || "Failed to validate OAuth configuration");
    }
  }, []);

  const handleSetDefault = useCallback(async (email: string) => {
    setActionLoading(email);
    setError(null);
    try {
      const firestore = await getFirestore();
      const { collection, getDocs, writeBatch } = await import("firebase/firestore");
      const batch = writeBatch(firestore);
      const allSnap = await getDocs(collection(firestore, "emailAccounts"));
      allSnap.docs.forEach((d: any) => {
        batch.update(d.ref, { isDefault: d.id === email });
      });
      await batch.commit();
      await loadAccounts();
    } catch (e: any) {
      setError(e?.message || "Failed to set default sender");
    } finally {
      setActionLoading(null);
    }
  }, [loadAccounts]);

  const handleReconnect = useCallback((email: string) => {
    const url = getAuthUrl(email);
    if (url) window.location.href = url;
  }, []);

  const handleRemove = useCallback(async (email: string) => {
    if (!confirm(`Remove ${email}? This account will be disconnected but can be re-added later.`)) return;
    setActionLoading(email);
    setError(null);
    try {
      const firestore = await getFirestore();
      const { doc, updateDoc } = await import("firebase/firestore");
      await updateDoc(doc(firestore, "emailAccounts", email), { status: "deleted" });
      await loadAccounts();
    } catch (e: any) {
      setError(e?.message || "Failed to remove account");
    } finally {
      setActionLoading(null);
    }
  }, [loadAccounts]);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
          Connected Accounts
        </h2>
        <button
          onClick={handleAddAccount}
          className="rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-5 py-2.5 font-mono text-xs tracking-[0.15em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60"
        >
          + Add Email Account
        </button>
      </div>

      {error && (
        <div className="mb-6 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}

      {configValid === false && (
        <div className="mb-6 rounded-lg border border-amber-400/20 bg-amber-400/10 px-4 py-3">
          <p className="font-mono text-xs text-amber-400">
            OAuth configuration is invalid. Check that GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET are set in your environment variables.
          </p>
        </div>
      )}

      {loading ? (
        <p className="font-mono text-xs text-slate-500 animate-pulse">Loading accounts...</p>
      ) : accounts.length === 0 ? (
        <div className="rounded-xl border border-dashed border-white/10 p-12 text-center">
          <p className="font-mono text-xs text-slate-500 mb-3">No email accounts connected.</p>
          <p className="font-mono text-[10px] text-slate-600">
            Click "Add Email Account" to connect a Gmail account for sending invitations and campaign emails.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {accounts.map((account) => (
            <div
              key={account.email}
              className="rounded-xl border border-white/10 bg-white/[0.02] px-5 py-4 flex items-center justify-between"
            >
              <div className="flex items-center gap-4">
                <span className="text-lg">
                  {account.status === "connected" ? "\u{1F7E2}" : account.status === "expired" ? "\u{1F7E1}" : "\u{1F534}"}
                </span>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-sm text-white">
                      {account.displayName || account.email}
                    </span>
                    {account.isDefault && (
                      <span className="rounded-full border border-cyan-400/30 bg-cyan-400/10 px-2.5 py-0.5 font-mono text-[10px] text-cyan-300 uppercase tracking-[0.1em]">
                        Default
                      </span>
                    )}
                  </div>
                  <p className="font-mono text-xs text-slate-500 mt-0.5">{account.email}</p>
                  {account.status === "expired" && account.errorMessage && (
                    <p className="font-mono text-[10px] text-amber-400/60 mt-1">{account.errorMessage}</p>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-2">
                {account.status === "expired" && (
                  <button
                    onClick={() => handleReconnect(account.email)}
                    disabled={actionLoading === account.email}
                    className="rounded-lg border border-amber-400/30 px-3 py-2 font-mono text-[10px] text-amber-300 uppercase tracking-[0.1em] transition-colors hover:border-amber-400/60 hover:bg-amber-400/10 disabled:opacity-50"
                  >
                    Reconnect
                  </button>
                )}
                {account.status === "connected" && !account.isDefault && role !== "user" && (
                  <button
                    onClick={() => handleSetDefault(account.email)}
                    disabled={actionLoading === account.email}
                    className="rounded-lg border border-white/10 px-3 py-2 font-mono text-[10px] text-slate-400 uppercase tracking-[0.1em] transition-colors hover:border-white/20 hover:text-white disabled:opacity-50"
                  >
                    Set as Default
                  </button>
                )}
                {role !== "user" && (
                  <button
                    onClick={() => handleRemove(account.email)}
                    disabled={actionLoading === account.email}
                    className="rounded-lg border border-red-400/20 px-3 py-2 font-mono text-[10px] text-red-400/60 uppercase tracking-[0.1em] transition-colors hover:border-red-400/40 hover:text-red-400 disabled:opacity-50"
                  >
                    {actionLoading === account.email ? "..." : "Remove"}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-10 rounded-xl border border-white/5 bg-white/[0.01] px-5 py-4">
        <p className="font-mono text-[10px] text-slate-600 leading-relaxed">
          Adding an email account will never overwrite existing accounts. 
          Each account is stored separately and can be set as the default sender for campaigns.
          Expired accounts can be reconnected without losing their configuration.
        </p>
      </div>
    </div>
  );
}
