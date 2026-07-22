"use client";

import { useState, useEffect, useCallback } from "react";
import { getCurrentUser, fetchAdmin, getAllowedAdminsConfig } from "@/lib/firebase/authService";
import type { AllowedAdminsConfig } from "@/lib/firebase/authService";

const ROLE_STYLES: Record<string, string> = {
  owner: "border-cyan-400/30 bg-cyan-400/10 text-cyan-300",
  admin: "border-violet-400/30 bg-violet-400/10 text-violet-300",
  user: "border-slate-500/30 bg-slate-500/10 text-slate-300",
};

const ROLE_OPTIONS = ["owner", "admin", "user"] as const;

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
  const [newRole, setNewRole] = useState<string>("user");
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
  const isAdmin = myRole === "admin";

  const canWrite = isOwner || isAdmin;
  const availableRoles: readonly string[] = isOwner ? ROLE_OPTIONS : ["user"];

  const handleAddEmail = async () => {
    const email = newEmail.trim().toLowerCase();
    if (!email || !email.includes("@")) {
      setSaveError("Enter a valid email address.");
      return;
    }

    if (!allowedConfig) return;

    const inAdmins = allowedConfig.emails.includes(email);
    const inOwners = allowedConfig.owners.includes(email);
    const inUsers = allowedConfig.users?.includes(email) ?? false;

    if (inAdmins || inOwners || inUsers) {
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

      const updated: Record<string, string[]> = {
        emails: allowedConfig.emails,
        owners: allowedConfig.owners,
        users: allowedConfig.users ?? [],
      };

      if (newRole === "owner") {
        updated.owners = [...updated.owners, email];
      } else if (newRole === "admin") {
        updated.emails = [...updated.emails, email];
      } else {
        updated.users = [...updated.users, email];
      }

      await setDoc(doc(firestore, "appConfig", "allowedAdmins"), updated, { merge: true });

      setAllowedConfig({ ...allowedConfig, ...updated });
      setNewEmail("");
      setNewRole("user");
      setSaveSuccess(`${email} added as ${newRole}.`);
    } catch (e: any) {
      setSaveError(e?.message || "Failed to add email.");
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveEmail = async (email: string, roleCategory: string) => {
    if (!allowedConfig) return;

    if (roleCategory === "owner" && !isOwner) {
      setSaveError(`Only owners can remove owners.`);
      return;
    }
    if (roleCategory === "admin" && !isOwner) {
      setSaveError(`Only owners can remove admins.`);
      return;
    }

    setSaving(true);
    setSaveError(null);
    setSaveSuccess(null);

    try {
      const { doc, setDoc } = await import("firebase/firestore");
      const { getFirestore } = await import("@/lib/firebase/config");
      const firestore = await getFirestore();

      const updated: Record<string, string[]> = {
        emails: roleCategory === "admin" ? allowedConfig.emails.filter((e) => e !== email) : allowedConfig.emails,
        owners: roleCategory === "owner" ? allowedConfig.owners.filter((e) => e !== email) : allowedConfig.owners,
        users: roleCategory === "user" ? (allowedConfig.users ?? []).filter((e) => e !== email) : (allowedConfig.users ?? []),
      };

      await setDoc(doc(firestore, "appConfig", "allowedAdmins"), updated, { merge: true });

      setAllowedConfig({ ...allowedConfig, ...updated });
      setSaveSuccess(`${email} removed from allowlist.`);
      setAdmins(admins.filter((a) => a.email !== email));
    } catch (e: any) {
      setSaveError(e?.message || "Failed to remove email.");
    } finally {
      setSaving(false);
    }
  };

  const getAllowedList = () => {
    if (!allowedConfig) return [];
    const list: { email: string; role: string }[] = [];
    allowedConfig.owners.forEach((e) => list.push({ email: e, role: "owner" }));
    allowedConfig.emails.forEach((e) => {
      if (!list.some((l) => l.email === e)) list.push({ email: e, role: "admin" });
    });
    (allowedConfig.users ?? []).forEach((e) => {
      if (!list.some((l) => l.email === e)) list.push({ email: e, role: "user" });
    });
    return list;
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
              {allowedConfig && getAllowedList().length > 0 ? (
                <div className="space-y-2">
                  {getAllowedList().map(({ email, role }) => {
                    const canRemove =
                      (role === "user" && canWrite) ||
                      (role === "admin" && isOwner) ||
                      (role === "owner" && isOwner);
                    return (
                      <div
                        key={email}
                        className="flex items-center justify-between py-1"
                      >
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-sm text-white">{email}</span>
                          <span
                            className={`rounded-full border px-2.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.1em] ${
                              ROLE_STYLES[role] ?? "border-white/10 text-slate-400"
                            }`}
                          >
                            {role}
                          </span>
                        </div>
                        {canRemove && (
                          <button
                            onClick={() => handleRemoveEmail(email, role)}
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

              {canWrite && (
                <div className="mt-4 pt-4 border-t border-white/5 flex gap-2">
                  <input
                    type="email"
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                    placeholder="email@example.com"
                    className="flex-1 rounded-lg border border-white/10 bg-white/5 px-4 py-2 font-mono text-xs text-white placeholder:text-slate-600 outline-none focus:border-cyan-400/40 transition-colors"
                    onKeyDown={(e) => e.key === "Enter" && handleAddEmail()}
                  />
                  <select
                    value={newRole}
                    onChange={(e) => setNewRole(e.target.value)}
                    className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 font-mono text-xs text-white outline-none focus:border-cyan-400/40 transition-colors"
                  >
                    {availableRoles.map((r) => (
                      <option key={r} value={r}>
                        {r.charAt(0).toUpperCase() + r.slice(1)}
                      </option>
                    ))}
                  </select>
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
                        ROLE_STYLES[admin.role] ?? "border-white/10 text-slate-400"
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
          can sign in. Owners can add any role. Admins can only add the <strong className="text-slate-400">user</strong> role.
        </p>
        <p className="font-mono text-[10px] text-slate-600 leading-relaxed">
          When an allowed user signs in with Google, they are automatically added to the Registered Admins list.
        </p>
      </div>
    </div>
  );
}
