"use client";

import { useState, useEffect, useCallback } from "react";
import { getCurrentUser, fetchAdmin, getAllowedAdminsConfig } from "@/lib/firebase/authService";
import type { AllowedAdminsConfig } from "@/lib/firebase/authService";

interface Admin {
  uid: string;
  email: string;
  displayName: string;
  role: string;
}

export default function AdminsPage() {
  const [admins, setAdmins] = useState<Admin[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [allowedConfig, setAllowedConfig] = useState<AllowedAdminsConfig | null>(null);
  const [myRole, setMyRole] = useState<string | null>(null);
  const [newEmail, setNewEmail] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const { collection, getDocs, query, orderBy } = await import("firebase/firestore");
      const { getFirestore } = await import("@/lib/firebase/config");
      const firestore = await getFirestore();

      const q = query(collection(firestore, "admins"), orderBy("createdAt", "asc"));
      const snap = await getDocs(q);
      const list: Admin[] = snap.docs.map((d: any) => ({
        uid: d.id,
        email: d.data().email || d.id,
        displayName: d.data().displayName || "",
        role: d.data().role || "admin",
      }));
      setAdmins(list);

      const user = await getCurrentUser();
      if (user) {
        const admin = await fetchAdmin(user.uid);
        setMyRole(admin?.role || null);
      }

      const config = await getAllowedAdminsConfig();
      setAllowedConfig(config);
    } catch (e: any) {
      setError(e?.message || "Failed to load");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const isOwner = myRole === "owner";

  const handleAddEmail = async () => {
    const email = newEmail.trim().toLowerCase();
    if (!email || !email.includes("@")) {
      setSaveError("Enter a valid email address.");
      return;
    }

    if (!allowedConfig) return;
    if (allowedConfig.emails.includes(email)) {
      setSaveError(`${email} is already in the allowlist.`);
      return;
    }

    setSaving(true);
    setSaveError(null);
    setSaveSuccess(null);

    try {
      const { doc, setDoc } = await import("firebase/firestore");
      const { getFirestore } = await import("@/lib/firebase/config");
      const firestore = await getFirestore();

      await setDoc(doc(firestore, "appConfig", "allowedAdmins"), {
        emails: [...allowedConfig.emails, email],
        owners: allowedConfig.owners,
      }, { merge: true });

      setAllowedConfig({ ...allowedConfig, emails: [...allowedConfig.emails, email] });
      setNewEmail("");
      setSaveSuccess(`${email} added to allowlist.`);
    } catch (e: any) {
      setSaveError(e?.message || "Failed to add email.");
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveEmail = async (email: string) => {
    if (!allowedConfig) return;
    if (allowedConfig.owners.includes(email)) {
      setSaveError(`Cannot remove ${email} — they are an owner.`);
      return;
    }

    setSaving(true);
    setSaveError(null);
    setSaveSuccess(null);

    try {
      const { doc, setDoc } = await import("firebase/firestore");
      const { getFirestore } = await import("@/lib/firebase/config");
      const firestore = await getFirestore();

      await setDoc(doc(firestore, "appConfig", "allowedAdmins"), {
        emails: allowedConfig.emails.filter((e) => e !== email),
        owners: allowedConfig.owners,
      }, { merge: true });

      setAllowedConfig({
        ...allowedConfig,
        emails: allowedConfig.emails.filter((e) => e !== email),
      });
      setSaveSuccess(`${email} removed from allowlist.`);
      setAdmins(admins.filter((a) => a.email !== email));
    } catch (e: any) {
      setSaveError(e?.message || "Failed to remove email.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-6">
        Admin Access
      </h2>

      {error && (
        <div className="mb-6 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}

      {saveError && (
        <div className="mb-6 rounded-lg border border-amber-400/20 bg-amber-400/10 px-4 py-3">
          <p className="font-mono text-xs text-amber-400">{saveError}</p>
        </div>
      )}

      {saveSuccess && (
        <div className="mb-6 rounded-lg border border-green-400/20 bg-green-400/10 px-4 py-3">
          <p className="font-mono text-xs text-green-400">{saveSuccess}</p>
        </div>
      )}

      {loading ? (
        <p className="font-mono text-xs text-slate-500 animate-pulse">Loading admins...</p>
      ) : (
        <>
          {/* Allowed Emails (Allowlist) */}
          <section className="mb-8">
            <h3 className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
              Allowed Emails
            </h3>
            <div className="rounded-xl border border-white/10 bg-white/[0.02] px-5 py-4">
              {allowedConfig && allowedConfig.emails.length > 0 ? (
                <div className="space-y-2">
                  {allowedConfig.emails.map((email) => {
                    const isOwnerEmail = allowedConfig.owners.includes(email);
                    return (
                      <div
                        key={email}
                        className="flex items-center justify-between py-1"
                      >
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-sm text-white">{email}</span>
                          {isOwnerEmail && (
                            <span className="rounded-full border border-cyan-400/20 bg-cyan-400/5 px-2 py-0.5 font-mono text-[10px] text-cyan-300 uppercase tracking-[0.1em]">
                              Owner
                            </span>
                          )}
                        </div>
                        {isOwner && !isOwnerEmail && (
                          <button
                            onClick={() => handleRemoveEmail(email)}
                            disabled={saving}
                            className="font-mono text-[11px] text-red-400/60 hover:text-red-400 transition-colors disabled:opacity-30"
                          >
                            Remove
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <p className="font-mono text-xs text-slate-500">No emails in allowlist.</p>
              )}

              {isOwner && (
                <div className="mt-4 pt-4 border-t border-white/5 flex gap-2">
                  <input
                    type="email"
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                    placeholder="email@example.com"
                    className="flex-1 rounded-lg border border-white/10 bg-white/5 px-4 py-2 font-mono text-xs text-white placeholder:text-slate-600 outline-none focus:border-cyan-400/40 transition-colors"
                    onKeyDown={(e) => e.key === "Enter" && handleAddEmail()}
                  />
                  <button
                    onClick={handleAddEmail}
                    disabled={saving || !newEmail.trim()}
                    className="rounded-lg border border-cyan-400/30 px-4 py-2 font-mono text-xs text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    {saving ? "Adding..." : "Add Email"}
                  </button>
                </div>
              )}
            </div>
          </section>

          {/* Current Admins */}
          <section>
            <h3 className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
              Registered Admins
            </h3>
            {admins.length === 0 ? (
              <div className="rounded-xl border border-dashed border-white/10 p-12 text-center">
                <p className="font-mono text-xs text-slate-500">No admins have signed in yet.</p>
              </div>
            ) : (
              <div className="space-y-2">
                {admins.map((admin) => (
                  <div
                    key={admin.uid}
                    className="rounded-xl border border-white/10 bg-white/[0.02] px-5 py-4 flex items-center justify-between"
                  >
                    <div>
                      <p className="font-mono text-sm text-white">{admin.displayName || admin.email}</p>
                      <p className="font-mono text-xs text-slate-500 mt-0.5">{admin.email}</p>
                    </div>
                    <span
                      className={`rounded-full border px-3 py-1 font-mono text-[10px] uppercase tracking-[0.1em] ${
                        admin.role === "owner"
                          ? "border-cyan-400/30 bg-cyan-400/10 text-cyan-300"
                          : "border-white/10 text-slate-400"
                      }`}
                    >
                      {admin.role}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      )}

      <div className="mt-10 rounded-xl border border-white/5 bg-white/[0.01] px-5 py-4 space-y-2">
        <p className="font-mono text-[10px] text-slate-600 leading-relaxed">
          Only users whose email is in the <strong className="text-slate-400">Allowed Emails</strong> list
          can sign in. Owners (you and atulkpal@gmail.com) can add or remove emails.
        </p>
        <p className="font-mono text-[10px] text-slate-600 leading-relaxed">
          When an allowed user signs in with Google, they are automatically added to the Registered Admins list.
          New users added via the allowlist get the <strong className="text-slate-400">admin</strong> role.
        </p>
      </div>
    </div>
  );
}
