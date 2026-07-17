"use client";

import { useState, useEffect, useCallback } from "react";
import type { DashboardConfig } from "@/types/config";
import { getDefaultConfig, updateConfig } from "@/lib/firebase/configService";
import { computeDaysRemaining, computeCurrentDay } from "@/lib/firebase/analytics";
import { formatCurrency } from "@/lib/firebase/revenue";

interface Props {
  config: DashboardConfig;
  exchangeRateStatus: string;
  onConfigSaved: (config: DashboardConfig) => void;
  onClose: () => void;
}

function formatTimestamp(ts: { seconds: number } | undefined): string {
  if (!ts?.seconds) return "—";
  return new Date(ts.seconds * 1000).toLocaleString();
}

export default function ConfigurationCard({ config, exchangeRateStatus, onConfigSaved, onClose }: Props) {
  const [draft, setDraft] = useState<DashboardConfig>(() => getDefaultConfig());
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setDraft(JSON.parse(JSON.stringify(config)));
  }, [config]);

  useEffect(() => {
    if (saved) {
      const t = setTimeout(() => setSaved(false), 3000);
      return () => clearTimeout(t);
    }
  }, [saved]);

  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    if (e.key === "Escape") onClose();
  }, [onClose]);

  useEffect(() => {
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [handleKeyDown]);

  const setField = useCallback((section: "beta" | "revenue", field: string, value: string | number) => {
    setDraft((prev) => ({
      ...prev,
      [section]: { ...prev[section], [field]: value },
    }));
  }, []);

  const validate = useCallback((): boolean => {
    const errors: Record<string, string> = {};
    const b = draft.beta;
    const r = draft.revenue;
    if (b.endDate <= b.startDate) errors.endDate = "End date must be after start date";
    if (b.requiredDays < 1) errors.requiredDays = "Must be at least 1";
    if (b.requiredMinutes < 1) errors.requiredMinutes = "Must be at least 1";
    if (r.bannerEcpmUsd < 0) errors.bannerEcpmUsd = "Cannot be negative";
    if (r.rewardedEcpmUsd < 0) errors.rewardedEcpmUsd = "Cannot be negative";
    if (r.usdToInr <= 0) errors.usdToInr = "Must be greater than 0";
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  }, [draft]);

  const hasChanges = JSON.stringify(draft) !== JSON.stringify(config);

  const handleSave = useCallback(async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      const toSave = { ...draft };
      toSave.revenue = { ...draft.revenue };
      await updateConfig(toSave);
      setSaved(true);
      onConfigSaved(toSave);
      setTimeout(() => onClose(), 500);
    } catch (e) {
      console.error("Save failed", e);
    } finally {
      setSaving(false);
    }
  }, [draft, validate, onConfigSaved, onClose]);

  const handleReset = useCallback(() => {
    const defaults = getDefaultConfig();
    setDraft(defaults);
    onConfigSaved(defaults);
  }, [onConfigSaved]);

  const bannerEcpmInr = draft.revenue.bannerEcpmUsd * draft.revenue.usdToInr;
  const rewardedEcpmInr = draft.revenue.rewardedEcpmUsd * draft.revenue.usdToInr;
  const daysRemaining = computeDaysRemaining(draft.beta.endDate);
  const currentDay = computeCurrentDay(draft.beta.startDate);

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center sm:items-center">
      <div className="fixed inset-0 bg-black/70" onClick={onClose} />
      <div className="relative mt-16 sm:mt-0 sm:max-h-[85vh] sm:w-[600px] w-full mx-4 overflow-y-auto rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Configuration
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        <div className="space-y-6">
          <div>
            <h3 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-4">
              Beta
            </h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Start Date
                </label>
                <input
                  type="date"
                  value={draft.beta.startDate}
                  onChange={(e) => setField("beta", "startDate", e.target.value)}
                  className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
                />
              </div>
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  End Date
                </label>
                <input
                  type="date"
                  value={draft.beta.endDate}
                  onChange={(e) => setField("beta", "endDate", e.target.value)}
                  className={`w-full rounded-lg border bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20 ${
                    validationErrors.endDate ? "border-red-400" : "border-white/10"
                  }`}
                />
                {validationErrors.endDate && (
                  <p className="mt-1 font-mono text-[10px] text-red-400">{validationErrors.endDate}</p>
                )}
              </div>
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Required Days
                </label>
                <input
                  type="number"
                  min={1}
                  value={draft.beta.requiredDays}
                  onChange={(e) => setField("beta", "requiredDays", parseInt(e.target.value) || 0)}
                  className={`w-full rounded-lg border bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20 ${
                    validationErrors.requiredDays ? "border-red-400" : "border-white/10"
                  }`}
                />
                {validationErrors.requiredDays && (
                  <p className="mt-1 font-mono text-[10px] text-red-400">{validationErrors.requiredDays}</p>
                )}
              </div>
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Required Minutes
                </label>
                <input
                  type="number"
                  min={1}
                  value={draft.beta.requiredMinutes}
                  onChange={(e) => setField("beta", "requiredMinutes", parseInt(e.target.value) || 0)}
                  className={`w-full rounded-lg border bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20 ${
                    validationErrors.requiredMinutes ? "border-red-400" : "border-white/10"
                  }`}
                />
                {validationErrors.requiredMinutes && (
                  <p className="mt-1 font-mono text-[10px] text-red-400">{validationErrors.requiredMinutes}</p>
                )}
              </div>
            </div>
          </div>

          <div className="border-t border-white/5 pt-6">
            <h3 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-4">
              Revenue
            </h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  USD &rarr; INR Rate
                </label>
                <input
                  type="number"
                  step="any"
                  min={0.01}
                  value={draft.revenue.usdToInr}
                  onChange={(e) => setField("revenue", "usdToInr", parseFloat(e.target.value) || 0)}
                  className={`w-full rounded-lg border bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20 ${
                    validationErrors.usdToInr ? "border-red-400" : "border-white/10"
                  }`}
                />
                {validationErrors.usdToInr && (
                  <p className="mt-1 font-mono text-[10px] text-red-400">{validationErrors.usdToInr}</p>
                )}
                {exchangeRateStatus && (
                  <p className="mt-1 font-mono text-[10px] text-slate-500">{exchangeRateStatus}</p>
                )}
              </div>
              <div></div>
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Banner eCPM (USD)
                </label>
                <input
                  type="number"
                  step="any"
                  min={0}
                  value={draft.revenue.bannerEcpmUsd}
                  onChange={(e) => setField("revenue", "bannerEcpmUsd", parseFloat(e.target.value) || 0)}
                  className={`w-full rounded-lg border bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20 ${
                    validationErrors.bannerEcpmUsd ? "border-red-400" : "border-white/10"
                  }`}
                />
                {validationErrors.bannerEcpmUsd && (
                  <p className="mt-1 font-mono text-[10px] text-red-400">{validationErrors.bannerEcpmUsd}</p>
                )}
              </div>
              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Rewarded eCPM (USD)
                </label>
                <input
                  type="number"
                  step="any"
                  min={0}
                  value={draft.revenue.rewardedEcpmUsd}
                  onChange={(e) => setField("revenue", "rewardedEcpmUsd", parseFloat(e.target.value) || 0)}
                  className={`w-full rounded-lg border bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20 ${
                    validationErrors.rewardedEcpmUsd ? "border-red-400" : "border-white/10"
                  }`}
                />
                {validationErrors.rewardedEcpmUsd && (
                  <p className="mt-1 font-mono text-[10px] text-red-400">{validationErrors.rewardedEcpmUsd}</p>
                )}
              </div>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-4 rounded-lg border border-white/5 bg-black/50 p-4">
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                  Banner eCPM (&₹;)
                </p>
                <p className="font-mono text-sm text-cyan-100">{formatCurrency(bannerEcpmInr)}</p>
              </div>
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                  Rewarded eCPM (&₹;)
                </p>
                <p className="font-mono text-sm text-cyan-100">{formatCurrency(rewardedEcpmInr)}</p>
              </div>
            </div>
          </div>

          <div className="border-t border-white/5 pt-6">
            <h3 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-4">
              Beta Status
            </h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                  Current Beta
                </p>
                <p className="font-mono text-sm text-white">
                  {draft.beta.startDate} &rarr; {draft.beta.endDate}
                </p>
              </div>
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                  Days Remaining
                </p>
                <p className="font-mono text-sm text-cyan-100">{daysRemaining}</p>
              </div>
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                  Current Day of Beta
                </p>
                <p className="font-mono text-sm text-cyan-100">{currentDay}</p>
              </div>
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                  Last Updated
                </p>
                <p className="font-mono text-sm text-slate-300">{formatTimestamp(config.updatedAt)}</p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-4 pt-2">
            <button
              onClick={handleSave}
              disabled={!hasChanges || saving || Object.keys(validationErrors).length > 0}
              className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
            >
              {saving ? "Saving..." : "Save Changes"}
            </button>
            <button
              onClick={handleReset}
              className="rounded-lg border border-white/10 px-6 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Reset Defaults
            </button>
            {saved && (
              <p className="font-mono text-[11px] text-green-400">Configuration Saved</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
