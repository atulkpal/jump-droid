"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useRole } from "@/components/beta/AuthContext";
import type { Campaign, CampaignStatus } from "@/types/campaign";

const STATUS_STYLES: Record<CampaignStatus, string> = {
  draft: "text-slate-400 border-slate-500/30 bg-slate-500/10",
  scheduled: "text-yellow-300 border-yellow-400/30 bg-yellow-400/10",
  running: "text-green-300 border-green-400/30 bg-green-400/10",
  paused: "text-orange-300 border-orange-400/30 bg-orange-400/10",
  completed: "text-blue-300 border-blue-400/30 bg-blue-400/10",
  failed: "text-red-300 border-red-400/30 bg-red-400/10",
  cancelled: "text-slate-500 border-slate-500/20 bg-slate-500/5",
};

const RANDOM_NAMES = [
  "Operation Fluffy", "Beta Boogaloo", "Campaign McCFace", "Rocket Sneakers",
  "The Void", "Project Unicorn", "Cosmic Oops", "Graviton Surprise",
  "Droid Dreams", "Pixel Push", "Zero Gravity", "Launch Party",
  "Signal Lost", "Alien Ant Farm", "Space Pancakes", "Moon Cheese",
  "Orbit Unknown", "Thruster Test", "Warp Speed Nap", "Singularity Snack",
];

function randomCampaignName(): string {
  return RANDOM_NAMES[Math.floor(Math.random() * RANDOM_NAMES.length)];
}

export default function CampaignsPage() {
  const { role } = useRole();
  const router = useRouter();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState("");
  const [scheduledStart, setScheduledStart] = useState(new Date().toISOString().slice(0, 16));
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canManage = role === "owner" || role === "admin";

  const loadCampaigns = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/campaigns");
      if (!res.ok) throw new Error("Failed to load");
      const data = await res.json();
      setCampaigns(data);
    } catch (e: any) {
      setError(e?.message || "Failed to load campaigns");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCampaigns();
  }, [loadCampaigns]);

  const handleCreate = async () => {
    if (!newName.trim()) return;
    setCreating(true);
    setError(null);
    try {
      const res = await fetch("/api/campaigns", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: newName.trim(),
          createdBy: "admin",
          scheduledStartAt: scheduledStart || undefined,
        }),
      });
      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Failed to create");
      }
      const campaign = await res.json();
      setShowCreate(false);
      setNewName("");
      setScheduledStart("");
      router.push(`/beta-dashboard/campaigns/${campaign.id}?tab=settings`);
    } catch (e: any) {
      setError(e?.message || "Failed to create campaign");
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="mx-auto max-w-6xl">
      <div className="flex items-center justify-between mb-8">
        <h1 className="font-mono text-sm font-bold tracking-[0.15em] text-cyan-200 uppercase">
          Campaigns
        </h1>
        {canManage && (
          <button
            onClick={() => { setNewName(randomCampaignName()); setScheduledStart(new Date().toISOString().slice(0, 16)); setShowCreate(true); }}
            className="flex items-center gap-2 rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-5 py-2.5 font-mono text-xs tracking-[0.1em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20"
          >
            <span className="text-base leading-none">+</span>
            New Campaign
          </button>
        )}
      </div>

      {error && (
        <div className="mb-6 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}

      {loading ? (
        <p className="font-mono text-xs text-slate-500 animate-pulse">Loading campaigns...</p>
      ) : campaigns.length === 0 ? (
        <div className="rounded-lg border border-white/5 p-12 text-center">
          <p className="font-mono text-sm text-slate-500 mb-2">No campaigns yet</p>
          <p className="font-mono text-xs text-slate-600">
            {canManage ? 'Click "+ New Campaign" to create your first campaign.' : "No campaigns are available."}
          </p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {campaigns.map((c) => {
            const bd = (c as any).contactBreakdown ?? {};
            const total = c.stats?.totalContacts ?? 0;
            const totalSends = c.stats?.totalSends ?? 0;
            const plannedSends = total * (c.settings?.maxInvites ?? 5);
            const sentPct = plannedSends > 0 ? Math.min(100, Math.round((totalSends / plannedSends) * 100)) : 0;

            const toDate = (v: any): Date | null => {
              if (!v) return null;
              if (typeof v.seconds === "number") return new Date(v.seconds * 1000);
              const d = new Date(v);
              return isNaN(d.getTime()) ? null : d;
            };
            const startAt = toDate(c.scheduledStartAt) ?? toDate(c.createdAt);
            const totalDurationDays = (c.settings?.maxInvites ?? 5) * (c.settings?.delayDays ?? 4);
            const estEnd = startAt
              ? new Date(startAt.getTime() + totalDurationDays * 24 * 60 * 60 * 1000)
              : null;

            const startedAtDate = toDate(c.startedAt) ?? toDate(c.scheduledStartAt) ?? toDate(c.createdAt) ?? (c.createdDate ? new Date(c.createdDate) : null);
            const createdDate = c.createdDate ? new Date(c.createdDate) : toDate(c.createdAt);
            const hasStarted = c.status !== "draft" && c.status !== "scheduled";

            return (
              <Link
                key={c.id}
                href={`/beta-dashboard/campaigns/${c.id}`}
                className="group rounded-xl border border-white/5 bg-white/[0.02] p-5 transition-all hover:border-white/10 hover:bg-white/[0.04]"
              >
                <div className="flex items-start justify-between mb-3">
                  <h3 className="font-mono text-sm font-bold text-white group-hover:text-cyan-200 transition-colors truncate pr-2">
                    {c.name}
                  </h3>
                  <span
                    className={`shrink-0 rounded-full border px-2.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.1em] ${
                      STATUS_STYLES[c.status] ?? "text-slate-400"
                    }`}
                  >
                    {c.status}
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-x-4 mb-3">
                  <div className="space-y-1">
                    <p className="font-mono text-[9px] text-slate-600 uppercase tracking-[0.1em] mb-1.5">Pipeline</p>
                    {[
                      { key: "pending", label: "Pending", color: "bg-amber-400", val: bd["pending"] ?? 0 },
                      { key: "invited", label: "Invited", color: "bg-blue-400", val: (bd["invited"] ?? 0) + (bd["replied"] ?? 0) },
                      { key: "converted", label: "Converted", color: "bg-purple-400", val: bd["converted"] ?? 0 },
                      { key: "failed", label: "Failed", color: "bg-red-400", val: bd["failed"] ?? 0 },
                    ].map((r) => (
                      <div key={r.key} className="flex items-center justify-between">
                        <span className="flex items-center gap-1.5">
                          <span className={`h-1.5 w-1.5 shrink-0 rounded-full ${r.color}`} />
                          <span className="font-mono text-[10px] text-slate-400">{r.label}</span>
                        </span>
                        <span className="font-mono text-xs text-white">{r.val}</span>
                      </div>
                    ))}
                  </div>
                  <div className="space-y-1">
                    <p className="font-mono text-[9px] text-slate-600 uppercase tracking-[0.1em] mb-1.5">Engagement</p>
                    {[
                      { key: "opened", label: "Opened", color: "bg-cyan-400", val: bd["opened"] ?? 0 },
                      { key: "replied", label: "Replied", color: "bg-green-400", val: bd["replied"] ?? 0 },
                      { key: "bounced", label: "Bounced", color: "bg-red-500", val: bd["bounced"] ?? 0 },
                      { key: "no_response", label: "No Response", color: "bg-slate-500", val: bd["no_response"] ?? 0 },
                    ].map((r) => (
                      <div key={r.key} className="flex items-center justify-between">
                        <span className="flex items-center gap-1.5">
                          <span className={`h-1.5 w-1.5 shrink-0 rounded-full ${r.color}`} />
                          <span className="font-mono text-[10px] text-slate-400">{r.label}</span>
                        </span>
                        <span className="font-mono text-xs text-white">{r.val}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="mb-3">
                  <p className="font-mono text-[10px] text-slate-400">
                    {totalSends} sent / {plannedSends} planned
                  </p>
                  <div className="h-1 rounded-full bg-white/5 overflow-hidden mt-1">
                    <div
                      className="h-full rounded-full bg-slate-500 transition-all"
                      style={{ width: `${sentPct}%` }}
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <p className="font-mono text-[9px] text-slate-600">
                    {hasStarted
                      ? startedAtDate
                        ? `Started ${startedAtDate.toLocaleDateString()}`
                        : "—"
                      : createdDate
                        ? `Created ${createdDate.toLocaleDateString()}`
                        : "—"}
                  </p>
                  <p className="font-mono text-[9px] text-slate-600">
                    {hasStarted && estEnd ? `Est End ${estEnd.toLocaleDateString()}` : "—"}
                  </p>
                </div>
              </Link>
            );
          })}
        </div>
      )}

      {showCreate && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
          onClick={() => setShowCreate(false)}
        >
          <div
            className="w-full max-w-md rounded-xl border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="font-mono text-sm font-bold tracking-[0.15em] text-cyan-200 uppercase mb-6">
              New Campaign
            </h2>

            <div className="space-y-4">
              <div>
                <label className="font-mono text-xs text-slate-400 mb-1.5 block">Campaign Name *</label>
                <input
                  type="text"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g. Summer 2026 Launch"
                  className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
                  autoFocus
                />
              </div>
              <div>
                <label className="font-mono text-xs text-slate-400 mb-1.5 block">Schedule Start (optional)</label>
                <input
                  type="datetime-local"
                  value={scheduledStart}
                  onChange={(e) => setScheduledStart(e.target.value)}
                  className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 [color-scheme:dark]"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 mt-8">
              <button
                onClick={() => setShowCreate(false)}
                className="rounded-lg px-5 py-2.5 font-mono text-xs text-slate-400 transition-colors hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={handleCreate}
                disabled={creating || !newName.trim()}
                className="rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-6 py-2.5 font-mono text-xs tracking-[0.1em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {creating ? "Creating..." : "Create"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
