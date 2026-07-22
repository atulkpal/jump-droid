"use client";

import { useState, useEffect, useCallback } from "react";
import { useRole } from "@/components/beta/AuthContext";

interface Account {
  email: string;
  displayName: string;
}

interface CampaignConfigForm {
  delayDays: number;
  maxInvites: number;
  delayBetweenEmailsMs: number;
  batchSize: number;
  maxEmailsPerHour: number;
  senderAccountId: string | null;
  templateSequence?: string[];
}

export default function CampaignSettingsPage() {
  const { role } = useRole();
  const [config, setConfig] = useState<CampaignConfigForm | null>(null);
  const [templateSequence, setTemplateSequence] = useState<string[]>(["outreach-1", "outreach-2", "outreach-3", "outreach-4", "outreach-5"]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [senderOption, setSenderOption] = useState<"default" | "specific">("default");
  const [specificSender, setSpecificSender] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const [configRes, accountsRes] = await Promise.all([
          import("@/lib/firebase/campaignService").then((m) => m.getCampaignConfig()),
          fetch("/api/email-accounts").then((r) => r.json()),
        ]);

        setConfig(configRes);
        if (configRes.templateSequence) setTemplateSequence(configRes.templateSequence);
        const emailAccounts: Account[] = accountsRes.accounts || [];
        setAccounts(emailAccounts);

        if (configRes.senderAccountId) {
          setSenderOption("specific");
          setSpecificSender(configRes.senderAccountId);
        } else {
          setSenderOption("default");
          setSpecificSender("");
        }
      } catch (e: any) {
        setError(e?.message || "Failed to load settings");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleSave = useCallback(async () => {
    if (!config) return;
    setSaving(true);
    setError(null);
    setSaved(false);

    try {
      const updatedConfig = {
        ...config,
        senderAccountId: senderOption === "specific" ? specificSender : null,
        templateSequence,
      };

      const { saveCampaignConfig } = await import("@/lib/firebase/campaignService");
      await saveCampaignConfig(updatedConfig);

      if (senderOption === "specific" && specificSender) {
        await fetch("/api/email-accounts/set-default", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: specificSender }),
        });
      }

      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (e: any) {
      setError(e?.message || "Failed to save settings");
    } finally {
      setSaving(false);
    }
  }, [config, senderOption, specificSender]);

  if (loading) {
    return <p className="font-mono text-xs text-slate-500 animate-pulse">Loading campaign settings...</p>;
  }

  if (!config) {
    return <p className="font-mono text-xs text-red-400">Failed to load campaign settings.</p>;
  }

  return (
    <div className="max-w-lg">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-6">
        Global Campaign Settings
      </h2>
      <div className="mb-6 rounded-lg border border-cyan-400/10 bg-cyan-400/[0.03] px-4 py-3">
        <p className="font-mono text-[11px] text-cyan-300">
          These defaults apply to all new campaigns. Per-campaign overrides are available in each{" "}
          <a href="/beta-dashboard/campaigns" className="underline hover:text-white">Campaign Workspace</a>.
        </p>
      </div>

      {error && (
        <div className="mb-6 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}

      {saved && (
        <div className="mb-6 rounded-lg border border-green-400/20 bg-green-400/10 px-4 py-3">
          <p className="font-mono text-xs text-green-400">Settings saved successfully.</p>
        </div>
      )}

      <div className="space-y-5">
        <div>
          <label className="font-mono text-xs text-slate-400 mb-1.5 block">Delay Between Emails (days)</label>
          <input
            type="number"
            value={config.delayDays}
            onChange={(e) => setConfig({ ...config, delayDays: parseInt(e.target.value) || 4 })}
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
            min={1}
            max={30}
            disabled={role === "user"}
          />
          <p className="font-mono text-[10px] text-slate-600 mt-1">Number of days between each invitation attempt.</p>
        </div>

        <div>
          <label className="font-mono text-xs text-slate-400 mb-1.5 block">Max Invites Per Contact</label>
          <input
            type="number"
            value={config.maxInvites}
            onChange={(e) => {
              const n = parseInt(e.target.value) || 5;
              setConfig({ ...config, maxInvites: n });
              setTemplateSequence((prev) => {
                const seq = [...prev];
                while (seq.length < n) {
                  const nextPos = seq.length + 1;
                  seq.push(nextPos <= 5 ? `outreach-${nextPos}` : "");
                }
                while (seq.length > n) seq.pop();
                return seq;
              });
            }}
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
            min={1}
            max={20}
            disabled={role === "user"}
          />
        </div>

        <div>
          <label className="font-mono text-xs text-slate-400 mb-3 block">Mail Sequence</label>
          {Array.from({ length: config.maxInvites }, (_, i) => (
            <div key={i} className="flex items-center gap-3 mb-2">
              <span className="font-mono text-[11px] text-slate-500 w-16 shrink-0">Mail {i + 1}</span>
              <select
                value={templateSequence[i] ?? ""}
                onChange={(e) => {
                  const seq = [...templateSequence];
                  seq[i] = e.target.value;
                  setTemplateSequence(seq);
                }}
                className="flex-1 rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
                disabled={role === "user"}
              >
                <option value="">— Select template —</option>
                <option value="outreach-1">outreach-1</option>
                <option value="outreach-2">outreach-2</option>
                <option value="outreach-3">outreach-3</option>
                <option value="outreach-4">outreach-4</option>
                <option value="outreach-5">outreach-5</option>
                <option value="invitation">invitation (auto)</option>
                <option value="acknowledgement">acknowledgement</option>
                <option value="welcome">welcome</option>
                <option value="reject">reject</option>
              </select>
            </div>
          ))}
        </div>

        <div>
          <label className="font-mono text-xs text-slate-400 mb-1.5 block">Batch Size (per campaign run)</label>
          <input
            type="number"
            value={config.batchSize}
            onChange={(e) => setConfig({ ...config, batchSize: parseInt(e.target.value) || 10 })}
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
            min={1}
            max={100}
            disabled={role === "user"}
          />
        </div>

        <div>
          <label className="font-mono text-xs text-slate-400 mb-1.5 block">Max Emails Per Hour</label>
          <input
            type="number"
            value={config.maxEmailsPerHour}
            onChange={(e) => setConfig({ ...config, maxEmailsPerHour: parseInt(e.target.value) || 100 })}
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
            min={1}
            max={500}
            disabled={role === "user"}
          />
        </div>

        <div>
          <label className="font-mono text-xs text-slate-400 mb-1.5 block">Delay Between Individual Emails (seconds)</label>
          <input
            type="number"
            value={Math.round(config.delayBetweenEmailsMs / 1000)}
            onChange={(e) => setConfig({ ...config, delayBetweenEmailsMs: (parseInt(e.target.value) || 120) * 1000 })}
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
            min={1}
            max={300}
            step={1}
            disabled={role === "user"}
          />
        </div>

        <div className="border-t border-white/10 pt-5">
          <label className="font-mono text-xs text-slate-400 mb-3 block">Campaign Sender</label>

          <div className="space-y-3">
            <label className="flex items-center gap-3">
              <input
                type="radio"
                name="senderOption"
                checked={senderOption === "default"}
                onChange={() => setSenderOption("default")}
                className="accent-cyan-400"
                disabled={role === "user"}
              />
              <span className="font-mono text-sm text-white">Use Default Sender</span>
            </label>

            <label className="flex items-center gap-3">
              <input
                type="radio"
                name="senderOption"
                checked={senderOption === "specific"}
                onChange={() => setSenderOption("specific")}
                className="accent-cyan-400"
                disabled={role === "user"}
              />
              <span className="font-mono text-sm text-white">Select Specific Sender</span>
            </label>

            {senderOption === "specific" && (
              <select
                value={specificSender}
                onChange={(e) => setSpecificSender(e.target.value)}
                className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 mt-2 disabled:opacity-30"
                disabled={role === "user"}
              >
                <option value="">Select an account...</option>
                {accounts.map((a) => (
                  <option key={a.email} value={a.email}>
                    {a.displayName || a.email}
                  </option>
                ))}
              </select>
            )}
          </div>

          <p className="font-mono text-[10px] text-slate-600 mt-2">
            If no specific sender is selected, campaigns will use the default email account.
          </p>
        </div>
      </div>

      {role !== "user" && (
        <button
          onClick={handleSave}
          disabled={saving || (senderOption === "specific" && !specificSender)}
          className="mt-8 rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-8 py-3.5 font-mono text-xs tracking-[0.15em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {saving ? "Saving..." : "Save Settings"}
        </button>
      )}
    </div>
  );
}
