"use client";

import { useState } from "react";
import { addManualContact } from "@/lib/firebase/outreachService";

interface Props {
  onAdded: () => void;
  onClose: () => void;
  importedBy?: string;
  campaignId?: string;
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function AddManualContact({ onAdded, onClose, importedBy, campaignId }: Props) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const trimmedEmail = email.trim();
    if (!EMAIL_RE.test(trimmedEmail)) {
      setError("Invalid email address");
      return;
    }
    if (!name.trim()) {
      setError("Name is required");
      return;
    }

    setSaving(true);
    try {
      const result = await addManualContact(name.trim(), trimmedEmail, phone.trim() || undefined, importedBy);
      if (result.exists) {
        setError(result.duplicateType || "Already in Outreach.");
        setSaving(false);
        return;
      }
      if (campaignId) {
        const { addContactToCampaign } = await import("@/lib/firebase/campaignService");
        await addContactToCampaign(trimmedEmail, campaignId, importedBy);
      }
      setDone(true);
      onAdded();
    } catch {
      setError("Failed to save contact.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center sm:items-center">
      <div className="fixed inset-0 bg-black/70" onClick={onClose} />
      <div className="relative mt-16 sm:mt-0 sm:w-[420px] w-full mx-4 rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Add Contact
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        {done ? (
          <div className="text-center space-y-4 py-6">
            <p className="font-mono text-sm text-green-400">
              Contact added successfully.
            </p>
            <button
              onClick={onClose}
              className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50"
            >
              Done
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-2">
                Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Jane Doe"
                className="w-full rounded-lg border border-white/10 bg-black/50 px-4 py-3 font-mono text-sm text-white placeholder-slate-600 outline-none transition-colors focus:border-cyan-400/40"
              />
            </div>
            <div>
              <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-2">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. jane@example.com"
                className="w-full rounded-lg border border-white/10 bg-black/50 px-4 py-3 font-mono text-sm text-white placeholder-slate-600 outline-none transition-colors focus:border-cyan-400/40"
              />
            </div>
            <div>
              <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-2">
                Phone <span className="text-slate-600 normal-case">(optional)</span>
              </label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="e.g. +1 555-0123"
                className="w-full rounded-lg border border-white/10 bg-black/50 px-4 py-3 font-mono text-sm text-white placeholder-slate-600 outline-none transition-colors focus:border-cyan-400/40"
              />
            </div>

            {error && (
              <p className="font-mono text-xs text-red-400">{error}</p>
            )}

            <div className="flex gap-3 pt-2">
              <button
                type="submit"
                disabled={saving}
                className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
              >
                {saving ? "Saving..." : "Add Contact"}
              </button>
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg border border-white/10 px-6 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white"
              >
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
