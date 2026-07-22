"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useRole } from "@/components/beta/AuthContext";
import type { Campaign, CampaignStatus, CampaignConfig, PreflightCheck } from "@/types/campaign";
import type { TemplateWithSource } from "@/types/emailTemplates";

function safeDate(v: any): Date | null {
  if (!v) return null;
  if (typeof v === "number") return new Date(v);
  if (typeof v === "string") { const d = new Date(v); return isNaN(d.getTime()) ? null : d; }
  if (typeof v.seconds === "number") return new Date(v.seconds * 1000);
  if (v instanceof Date) return v;
  return null;
}

const STATUS_STYLES: Record<CampaignStatus, string> = {
  draft: "text-slate-400 border-slate-500/30 bg-slate-500/10",
  scheduled: "text-yellow-300 border-yellow-400/30 bg-yellow-400/10",
  running: "text-green-300 border-green-400/30 bg-green-400/10",
  paused: "text-orange-300 border-orange-400/30 bg-orange-400/10",
  completed: "text-blue-300 border-blue-400/30 bg-blue-400/10",
  failed: "text-red-300 border-red-400/30 bg-red-400/10",
  cancelled: "text-slate-500 border-slate-500/20 bg-slate-500/5",
};

const STATUS_ACTIONS: Record<CampaignStatus, { label: string; nextStatus: CampaignStatus; color: string }[]> = {
  draft: [
    { label: "Start", nextStatus: "running", color: "green" },
    { label: "Schedule", nextStatus: "scheduled", color: "yellow" },
  ],
  scheduled: [
    { label: "Start Now", nextStatus: "running", color: "green" },
  ],
  running: [
    { label: "Pause", nextStatus: "paused", color: "orange" },
    { label: "Cancel", nextStatus: "cancelled", color: "red" },
  ],
  paused: [
    { label: "Resume", nextStatus: "running", color: "green" },
    { label: "Cancel", nextStatus: "cancelled", color: "red" },
  ],
  completed: [],
  failed: [
    { label: "Restart", nextStatus: "running", color: "green" },
    { label: "Archive", nextStatus: "cancelled", color: "slate" },
  ],
  cancelled: [
    { label: "Reopen", nextStatus: "draft", color: "slate" },
  ],
};

const ACTION_COLORS: Record<string, string> = {
  green: "border-green-400/40 bg-green-400/10 text-green-300 hover:bg-green-400/20",
  orange: "border-orange-400/40 bg-orange-400/10 text-orange-300 hover:bg-orange-400/20",
  red: "border-red-400/40 bg-red-400/10 text-red-300 hover:bg-red-400/20",
  slate: "border-slate-500/40 bg-slate-500/10 text-slate-300 hover:bg-slate-500/20",
  yellow: "border-yellow-400/40 bg-yellow-400/10 text-yellow-300 hover:bg-yellow-400/20",
};

type Tab = "overview" | "audience" | "settings" | "activity" | "errors";

const TABS: { key: Tab; label: string }[] = [
  { key: "overview", label: "Overview" },
  { key: "audience", label: "Audience" },
  { key: "settings", label: "Settings" },
  { key: "activity", label: "Activity" },
  { key: "errors", label: "Errors" },
];

export default function CampaignWorkspacePage() {
  const { id } = useParams<{ id: string }>();
  const { role } = useRole();
  const router = useRouter();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const searchParams = useSearchParams();
  const initialTab = (searchParams.get("tab") as Tab) || "overview";
  const [activeTab, setActiveTab] = useState<Tab>(initialTab);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [scheduleDate, setScheduleDate] = useState("");
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [detectLoading, setDetectLoading] = useState(false);
  const settingsValidateRef = useRef<() => boolean>(() => true);

  // "Add Contacts" modal state (lives in parent so it survives SettingsTab unmount during refresh)
  const [showAddContactsModal, setShowAddContactsModal] = useState(false);
  const [modalImportCampaigns, setModalImportCampaigns] = useState<any[]>([]);
  const [modalSelectedImportCampaign, setModalSelectedImportCampaign] = useState("");
  const [modalImporting, setModalImporting] = useState(false);
  const [modalAddingTestContact, setModalAddingTestContact] = useState(false);
  const pendingAddContactsRef = useRef(false);
  const settingsSavedRef = useRef(false);

  // After campaign reloads, show modal if settings were just saved and campaign has no contacts
  useEffect(() => {
    if (pendingAddContactsRef.current && campaign && !campaign.stats?.totalContacts) {
      pendingAddContactsRef.current = false;
      fetch("/api/campaigns")
        .then((r) => r.json())
        .then((data) => setModalImportCampaigns(data.filter((c: any) => c.id !== campaign.id && (c.stats?.totalContacts ?? 0) > 0)))
        .catch(() => {});
      setShowAddContactsModal(true);
    }
  }, [campaign]);

  const handleModalImportContacts = async () => {
    if (!modalSelectedImportCampaign) return;
    setModalImporting(true);
    try {
      const res = await fetch(`/api/campaigns/${id}/import-contacts`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sourceCampaignId: modalSelectedImportCampaign }),
      });
      if (!res.ok) throw new Error("Failed to import");

      const startRes = await fetch(`/api/campaigns/${id}/start`, { method: "POST" });
      if (!startRes.ok) {
        const data = await startRes.json();
        throw new Error(data.error || "Pre-flight check failed");
      }

      setShowAddContactsModal(false);
      await loadCampaign();
    } catch (e: any) {
      setError(e?.message || "Import & start failed");
    } finally {
      setModalImporting(false);
    }
  };

  const handleModalAddTestContact = async () => {
    setModalAddingTestContact(true);
    try {
      const res = await fetch(`/api/campaigns/${id}/add-test-contact`, { method: "POST" });
      if (!res.ok) throw new Error("Failed to add test contact");

      const startRes = await fetch(`/api/campaigns/${id}/start`, { method: "POST" });
      if (!startRes.ok) {
        const data = await startRes.json();
        throw new Error(data.error || "Pre-flight check failed");
      }

      setShowAddContactsModal(false);
      await loadCampaign();
    } catch (e: any) {
      setError(e?.message || "Failed to start campaign");
    } finally {
      setModalAddingTestContact(false);
    }
  };

  const handleSettingsSaved = useCallback(() => {
    settingsSavedRef.current = true;
    pendingAddContactsRef.current = true;
  }, []);

  const loadCampaign = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const res = await fetch(`/api/campaigns/${id}`);
      if (!res.ok) {
        if (res.status === 404) throw new Error("Campaign not found");
        throw new Error("Failed to load");
      }
      const data = await res.json();
      setCampaign(data);
    } catch (e: any) {
      setError(e?.message || "Failed to load campaign");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadCampaign();
  }, [loadCampaign]);

  const handleStatusAction = async (nextStatus: CampaignStatus) => {
    if (!campaign) return;

    // Show schedule modal if scheduling without a start date
    if (nextStatus === "scheduled" && !campaign.scheduledStartAt) {
      setScheduleDate(new Date().toISOString().slice(0, 16));
      setShowScheduleModal(true);
      return;
    }

    setActionLoading(nextStatus);
    setError(null);

    try {
      if (nextStatus === "running" && campaign.status === "draft") {
        const res = await fetch(`/api/campaigns/${id}/start`, { method: "POST" });
        if (!res.ok) {
          const data = await res.json();
          throw new Error(data.error || "Pre-flight check failed");
        }
      } else {
        const res = await fetch(`/api/campaigns/${id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ status: nextStatus }),
        });
        if (!res.ok) throw new Error("Failed to update status");
      }

      await loadCampaign();
    } catch (e: any) {
      setError(e?.message || "Action failed");
    } finally {
      setActionLoading(null);
    }
  };

  const handleScheduleConfirm = async () => {
    if (!campaign || !scheduleDate) return;
    setScheduleLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/campaigns/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ scheduledStartAt: scheduleDate, status: "scheduled" }),
      });
      if (!res.ok) throw new Error("Failed to schedule campaign");
      setShowScheduleModal(false);
      await loadCampaign();
    } catch (e: any) {
      setError(e?.message || "Schedule failed");
    } finally {
      setScheduleLoading(false);
    }
  };

  const handleDetect = async () => {
    if (!campaign) return;
    setDetectLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/campaigns/${id}/detect`, { method: "POST" });
      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Detection failed");
      }
      await loadCampaign();
    } catch (e: any) {
      setError(e?.message || "Detection failed");
    } finally {
      setDetectLoading(false);
    }
  };

  if (loading) {
    return <p className="font-mono text-xs text-slate-500 animate-pulse">Loading campaign...</p>;
  }

  if (error && !campaign) {
    return (
      <div className="rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
        <p className="font-mono text-xs text-red-400">{error}</p>
      </div>
    );
  }

  if (!campaign) return null;

  const canManage = role === "owner" || role === "admin";

  return (
    <div className="mx-auto max-w-6xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <button
            onClick={() => router.push("/beta-dashboard/campaigns")}
            className="font-mono text-[10px] text-slate-500 hover:text-white transition-colors mb-2 block"
          >
            &larr; Back to Campaigns
          </button>
          <div className="flex items-center gap-3">
            <h1 className="font-mono text-sm font-bold tracking-[0.15em] text-cyan-200 uppercase">
              {campaign.name}
            </h1>
            <span
              className={`rounded-full border px-2.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.1em] ${
                STATUS_STYLES[campaign.status] ?? "text-slate-400"
              }`}
            >
              {campaign.status}
            </span>
          </div>
          <p className="font-mono text-[10px] text-slate-600 mt-1">{campaign.id}</p>
        </div>

        <div className="flex items-center gap-2">
          {canManage &&
            STATUS_ACTIONS[campaign.status]?.map((action) => (
              <button
                key={action.nextStatus}
                onClick={() => handleStatusAction(action.nextStatus)}
                disabled={actionLoading === action.nextStatus}
                className={`rounded-lg border px-4 py-2 font-mono text-[11px] tracking-[0.1em] uppercase transition-all disabled:opacity-50 disabled:cursor-not-allowed ${
                  ACTION_COLORS[action.color]
                }`}
              >
                {actionLoading === action.nextStatus ? "..." : action.label}
              </button>
            ))}
          {(campaign.status === "running" || campaign.status === "paused") && (
            <button
              onClick={handleDetect}
              disabled={detectLoading}
              className="rounded-lg border border-cyan-500/30 px-4 py-2 font-mono text-[11px] tracking-[0.1em] uppercase text-cyan-300 transition-all hover:bg-cyan-500/10 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {detectLoading ? "..." : "Detect"}
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="mb-6 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}

      {showScheduleModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
          onClick={() => setShowScheduleModal(false)}
        >
          <div
            className="w-full max-w-md rounded-xl border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="font-mono text-sm font-bold tracking-[0.15em] text-yellow-300 uppercase mb-6">
              Schedule Campaign
            </h2>

            <div className="space-y-4">
              <div>
                <label className="font-mono text-xs text-slate-400 mb-1.5 block">Start Date &amp; Time</label>
                <input
                  type="datetime-local"
                  value={scheduleDate}
                  onChange={(e) => setScheduleDate(e.target.value)}
                  className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-yellow-400/40 [color-scheme:dark]"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 mt-8">
              <button
                onClick={() => setShowScheduleModal(false)}
                className="rounded-lg px-5 py-2.5 font-mono text-xs text-slate-400 transition-colors hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={handleScheduleConfirm}
                disabled={scheduleLoading || !scheduleDate}
                className="rounded-lg border border-yellow-400/40 bg-yellow-400/10 px-6 py-2.5 font-mono text-xs tracking-[0.1em] text-yellow-300 uppercase transition-all hover:bg-yellow-400/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {scheduleLoading ? "Scheduling..." : "Schedule"}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="flex gap-1 mb-8 border-b border-white/5 pb-0 overflow-x-auto">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => {
              if (activeTab === "settings" && tab.key !== "settings") {
                if (!settingsValidateRef.current()) return;
              }
              setActiveTab(tab.key);
              if (tab.key === "overview") loadCampaign();
            }}
            className={`shrink-0 px-4 py-3 font-mono text-xs tracking-[0.1em] transition-colors border-b-2 ${
              activeTab === tab.key
                ? "border-cyan-400 text-cyan-300"
                : "border-transparent text-slate-500 hover:text-white"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === "overview" && (
        <OverviewTab campaign={campaign} canManage={canManage} onRefresh={loadCampaign} role={role} />
      )}
      {activeTab === "audience" && (
        <AudienceTab campaignId={id} />
      )}
      {activeTab === "settings" && (
        <SettingsTab campaign={campaign} canManage={canManage} onRefresh={loadCampaign} onRegisterSettingsValidate={(fn) => { settingsValidateRef.current = fn; }} onSaved={handleSettingsSaved} />
      )}
      {activeTab === "activity" && (
        <ActivityTab campaignId={id} />
      )}
      {activeTab === "errors" && (
        <ErrorsTab campaignId={id} campaignName={campaign.name} />
      )}

      {showAddContactsModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
          onClick={() => setShowAddContactsModal(false)}
        >
          <div
            className="w-full max-w-md rounded-xl border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="font-mono text-sm font-bold tracking-[0.15em] text-cyan-200 uppercase mb-2">
              Add Contacts
            </h2>
            <p className="font-mono text-xs text-slate-500 mb-6 leading-relaxed">
              This campaign has no contacts. Add some to begin sending.
            </p>

            <div className="space-y-3">
              <div className="rounded-lg border border-white/10 bg-white/[0.02] p-3">
                <p className="font-mono text-xs text-white mb-2 font-bold">Import from existing campaign</p>
                {modalImportCampaigns.length > 0 ? (
                  <>
                    <select
                      value={modalSelectedImportCampaign}
                      onChange={(e) => setModalSelectedImportCampaign(e.target.value)}
                      className="w-full rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none focus:border-cyan-400/40 mb-2"
                    >
                      <option value="">Select campaign...</option>
                      {modalImportCampaigns.map((ic) => (
                        <option key={ic.id} value={ic.id}>{ic.name} ({ic.stats?.totalContacts ?? 0} contacts)</option>
                      ))}
                    </select>
                    <button
                      onClick={handleModalImportContacts}
                      disabled={modalImporting || !modalSelectedImportCampaign}
                      className="w-full rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-4 py-2 font-mono text-xs tracking-[0.1em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {modalImporting ? "Importing & Starting..." : "Import & Start"}
                    </button>
                  </>
                ) : (
                  <p className="font-mono text-[11px] text-slate-500">No other campaigns with contacts found.</p>
                )}
              </div>

              <button
                onClick={handleModalAddTestContact}
                disabled={modalAddingTestContact}
                className="w-full rounded-lg border border-green-400/40 bg-green-400/10 px-4 py-3 font-mono text-xs tracking-[0.1em] text-green-300 uppercase transition-all hover:bg-green-400/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {modalAddingTestContact ? "Starting..." : "Add Test Contact & Start"}
              </button>
            </div>

            <div className="flex items-center justify-end gap-3 mt-6">
              <button
                onClick={() => setShowAddContactsModal(false)}
                className="rounded-lg px-4 py-2 font-mono text-xs text-slate-400 transition-colors hover:text-white"
              >
                Add Later
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Overview Tab ──

function OverviewTab({
  campaign,
  canManage,
  onRefresh,
  role,
}: {
  campaign: Campaign;
  canManage: boolean;
  onRefresh: () => void;
  role?: string | null;
}) {
  const [preflight, setPreflight] = useState<PreflightCheck[] | null>(null);
  const [preflightLoading, setPreflightLoading] = useState(false);
  const [showDeletePopup, setShowDeletePopup] = useState(false);
  const [deleteNumA, setDeleteNumA] = useState(0);
  const [deleteNumB, setDeleteNumB] = useState(0);
  const [deleteAnswer, setDeleteAnswer] = useState("");
  const [deleteCorrect, setDeleteCorrect] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const IMPORTANT_EVENTS = new Set([
    "contact_converted", "contact_added_to_campaign", "contact_removed_from_campaign",
    "replied", "email_opened",
    "invitation_failed", "welcome_email_failed",
    "rate_limited", "system_error", "unsubscribed",
  ]);

  const [overviewData, setOverviewData] = useState<{
    replied: number;
    opened: number;
    lastRun: string | null;
    recentActivity: any[];
  }>({ replied: 0, opened: 0, lastRun: null, recentActivity: [] });

  useEffect(() => {
    if (!campaign?.id) return;
    const load = async () => {
      try {
        const { fetchCampaignTimeline } = await import("@/lib/firebase/activityService");
        const events = await fetchCampaignTimeline(campaign.id);
        const repliedCnt = events.filter((e: any) => e.eventType === "replied").length;
        const openedCnt = events.filter((e: any) => e.eventType === "email_opened").length;
        const runEvents = events.filter((e: any) => e.eventType === "email_run_completed");
        const lastRunEvent = runEvents.length > 0 ? runEvents[runEvents.length - 1] : null;
        const lastRunVal = safeDate(lastRunEvent?.createdAt)?.toLocaleString() ?? null;
        const important = events.filter((e: any) => IMPORTANT_EVENTS.has(e.eventType));
        setOverviewData({
          replied: repliedCnt,
          opened: openedCnt,
          lastRun: lastRunVal,
          recentActivity: important.slice(-5).reverse(),
        });
      } catch {
        // non-critical
      }
    };
    load();
  }, [campaign]);

  const openDeletePopup = () => {
    const a = Math.floor(Math.random() * 9) + 1;
    const b = Math.floor(Math.random() * 9) + 1;
    setDeleteNumA(a);
    setDeleteNumB(b);
    setDeleteAnswer("");
    setDeleteCorrect(false);
    setDeleteError(null);
    setShowDeletePopup(true);
  };

  const handleDeleteConfirm = async () => {
    setDeleting(true);
    setDeleteError(null);
    try {
      const res = await fetch(`/api/campaigns/${campaign.id}`, { method: "DELETE" });
      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Failed to delete");
      }
      const { useRouter } = await import("next/navigation");
      // Use window.location as a simple redirect
      window.location.href = "/beta-dashboard/campaigns";
    } catch (e: any) {
      setDeleteError(e?.message || "Delete failed");
      setDeleting(false);
    }
  };

  const runPreflight = async () => {
    setPreflightLoading(true);
    try {
      const res = await fetch(`/api/campaigns/${campaign.id}/start`);
      const data = await res.json();
      setPreflight(data.checks || []);
    } catch {
      setPreflight([]);
    } finally {
      setPreflightLoading(false);
    }
  };

  const isStarted = campaign.status !== "draft" && campaign.status !== "scheduled";
  const startDate = safeDate(campaign.startedAt) ?? safeDate(campaign.scheduledStartAt) ?? safeDate(campaign.createdAt);
  const totalDurationDays = (campaign.settings?.maxInvites ?? 5) * (campaign.settings?.delayDays ?? 4);
  const estEnd = startDate
    ? new Date(startDate.getTime() + totalDurationDays * 24 * 60 * 60 * 1000)
    : null;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_280px] gap-4 items-start">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
          <StatCardCompact
            label="Total Contacts"
            value={campaign.stats?.totalContacts ?? 0}
            subtitle={`${campaign.stats?.pending ?? 0} haven't been emailed`}
          />
          <StatCardCompact
            label="Contacted"
            value={campaign.stats?.contacted ?? 0}
            color="green"
            tooltip={`Total sends: ${campaign.stats?.totalSends ?? 0}`}
          />
          <StatCardCompact label="Replied" value={overviewData.replied} color="green" />
          <StatCardCompact label="Opened" value={overviewData.opened} color="cyan" />
          <StatCardCompact label="Converted" value={campaign.stats?.converted ?? 0} color="purple" />
          <StatCardCompact label="Bounced" value={campaign.stats?.bounced ?? 0} color="red" />
          <StatCardCompact label="No Response" value={campaign.stats?.noResponse ?? 0} color="yellow" />
          <StatCardCompact label="Errors" value={campaign.stats?.errors ?? 0} color="red" />
          <StatCardCompact label="Last Run" value={overviewData.lastRun ?? "—"} />
        </div>
        <StatusDonut breakdown={(campaign as any).contactBreakdown ?? {}} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_1.5fr] gap-4">
        <div className="rounded-xl border border-white/5 bg-white/[0.02] p-5">
          <h3 className="font-mono text-xs font-bold tracking-[0.1em] text-slate-300 uppercase mb-2">Campaign Info</h3>
          <div className="space-y-2 text-sm">
            <InfoRow label="ID" value={campaign.id} />
            <InfoRow label="Status" value={campaign.status} />
            <InfoRow label="Created By" value={campaign.createdBy} />
            <InfoRow
              label="Created"
              value={campaign.createdDate ? new Date(campaign.createdDate).toLocaleString() : safeDate(campaign.createdAt)?.toLocaleString() ?? "—"}
            />
            <InfoRow
              label={isStarted ? "Started" : "Scheduled Start"}
              value={startDate?.toLocaleString() ?? "—"}
            />
            {isStarted && (
              <InfoRow
                label="Est End"
                value={campaign.status === "paused" ? "—" : estEnd?.toLocaleString() ?? "—"}
              />
            )}
            <InfoRow
              label="Delay Between Emails"
              value={`${campaign.settings.delayDays} day(s)`}
            />
            <InfoRow
              label="Max Invites"
              value={`${campaign.settings.maxInvites}`}
            />
            <InfoRow
              label="Batch Size"
              value={`${campaign.settings.batchSize}`}
            />
            <InfoRow
              label="Sender"
              value={campaign.settings.senderAccountId || "Default"}
            />
            <InfoRow
              label="Last Detected"
              value={safeDate(campaign.lastDetectedAt)?.toLocaleString() ?? "—"}
            />
          </div>
        </div>
        <div className="rounded-xl border border-white/5 bg-white/[0.02] p-5">
          <h3 className="font-mono text-xs font-bold tracking-[0.1em] text-slate-300 uppercase mb-3">
            Recent Activity
          </h3>
          {overviewData.recentActivity.length > 0 ? (
            <div className="space-y-2">
              {overviewData.recentActivity.map((event: any) => (
                <div key={event.id} className="flex items-start gap-2">
                  <span className="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-cyan-500" />
                  <div className="min-w-0">
                    <p className="font-mono text-[11px] text-white truncate">{event.details || event.eventType}</p>
                    <p className="font-mono text-[9px] text-slate-500">
                      {safeDate(event.createdAt)?.toLocaleString() ?? "Just now"}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="font-mono text-xs text-slate-500">No recent activity.</p>
          )}
        </div>
      </div>

      {canManage && (
        <div className="rounded-xl border border-white/5 bg-white/[0.02] p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-mono text-xs font-bold tracking-[0.1em] text-slate-300 uppercase">
              Pre-flight Check
            </h3>
            <button
              onClick={runPreflight}
              disabled={preflightLoading}
              className="rounded-lg border border-white/10 px-3 py-1.5 font-mono text-[10px] text-slate-400 hover:text-white transition-colors disabled:opacity-50"
            >
              {preflightLoading ? "Running..." : "Run Check"}
            </button>
          </div>

          {preflight && preflight.length > 0 && (
            <div className="space-y-2">
              {preflight.map((check, i) => (
                <div key={i} className="flex items-center gap-3">
                  <span
                    className={`h-2 w-2 shrink-0 rounded-full ${
                      check.status === "ok" ? "bg-green-400" : check.status === "warn" ? "bg-yellow-400" : "bg-red-400"
                    }`}
                  />
                  <div>
                    <p className="font-mono text-xs text-white">{check.label}</p>
                    <p className="font-mono text-[10px] text-slate-500">{check.message}</p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {preflight && preflight.length === 0 && (
            <p className="font-mono text-xs text-slate-500">Run a pre-flight check to validate campaign readiness.</p>
          )}
        </div>
      )}

      {role === "owner" && (
        <div className="rounded-xl border border-red-400/20 bg-red-400/[0.02] p-5">
          <h3 className="font-mono text-xs font-bold tracking-[0.1em] text-red-400 uppercase mb-2">
            Danger Zone
          </h3>
          <p className="font-mono text-[11px] text-red-300/60 mb-4 leading-relaxed">
            Deleting this campaign will permanently remove it and all associated data. This action cannot be undone.
          </p>
          <button
            onClick={openDeletePopup}
            className="rounded-lg border border-red-400/40 bg-red-400/10 px-5 py-2.5 font-mono text-xs tracking-[0.1em] text-red-300 uppercase transition-all hover:bg-red-400/20"
          >
            Delete Campaign
          </button>
        </div>
      )}

      {showDeletePopup && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
          onClick={() => setShowDeletePopup(false)}
        >
          <div
            className="w-full max-w-sm rounded-xl border border-red-400/20 bg-[#0a0a0f] p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="font-mono text-xs font-bold tracking-[0.15em] text-red-400 uppercase mb-4">
              Delete Campaign
            </h3>
            <p className="font-mono text-xs text-slate-400 mb-4 leading-relaxed">
              This will permanently delete <span className="text-cyan-100">{campaign.name}</span>. To confirm, solve:
            </p>
            <p className="font-mono text-lg font-bold text-white mb-3 text-center">
              What is {deleteNumA} + {deleteNumB}?
            </p>
            <input
              type="number"
              value={deleteAnswer}
              onChange={(e) => {
                setDeleteAnswer(e.target.value);
                setDeleteCorrect(parseInt(e.target.value) === deleteNumA + deleteNumB);
              }}
              placeholder="Answer"
              className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 mb-4"
              autoFocus
            />
            {deleteError && (
              <p className="font-mono text-xs text-red-400 mb-4">{deleteError}</p>
            )}
            <div className="flex items-center justify-end gap-3">
              <button
                onClick={() => setShowDeletePopup(false)}
                disabled={deleting}
                className="rounded-lg px-4 py-2 font-mono text-xs text-slate-400 transition-colors hover:text-white disabled:opacity-50"
              >
                Cancel
              </button>
              {deleteCorrect && (
                <button
                  onClick={handleDeleteConfirm}
                  disabled={deleting}
                  className="rounded-lg border border-red-400/40 bg-red-400/10 px-5 py-2.5 font-mono text-xs tracking-[0.1em] text-red-300 uppercase transition-all hover:bg-red-400/20 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {deleting ? "Deleting..." : "\u2620 Delete Campaign"}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center gap-3 py-1">
      <span className="font-mono text-[11px] text-slate-500 w-36 shrink-0">{label}</span>
      <span className="font-mono text-xs text-white">{value}</span>
    </div>
  );
}

function StatCardCompact({
  label,
  value,
  color,
  subtitle,
  tooltip,
}: {
  label: string;
  value: number | string;
  color?: "green" | "red" | "blue" | "cyan" | "purple" | "yellow";
  subtitle?: string;
  tooltip?: string;
}) {
  const colorClass = color === "green" ? "text-green-400" : color === "red" ? "text-red-400" : color === "blue" ? "text-blue-400" : color === "cyan" ? "text-cyan-400" : color === "purple" ? "text-purple-400" : color === "yellow" ? "text-yellow-400" : "text-white";
  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-2.5 group/card relative">
      {tooltip && (
        <div className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2 mb-1.5 hidden group-hover/card:block z-10">
          <div className="bg-slate-800 text-slate-200 font-mono text-[9px] px-2 py-1 rounded whitespace-nowrap shadow-lg border border-white/10">
            {tooltip}
          </div>
        </div>
      )}
      <p className={`font-mono text-lg font-bold ${colorClass}`}>{value}</p>
      <p className="font-mono text-[9px] text-slate-500 uppercase tracking-[0.1em] mt-0.5">{label}</p>
      {subtitle && (
        <p className="font-mono text-[8px] text-slate-600 mt-0.5">{subtitle}</p>
      )}
    </div>
  );
}

const DONUT_COLORS: Record<string, string> = {
  pending: "#475569",
  invited: "#60a5fa",
  replied: "#34d399",
  registered: "#fbbf24",
  converted: "#a78bfa",
  no_response: "#f87171",
  failed: "#ef4444",
  bounced: "#dc2626",
  unsubscribed: "#6b7280",
  deleting: "#9ca3af",
};

const DONUT_LABELS: Record<string, string> = {
  pending: "Pending",
  invited: "Invited",
  replied: "Replied",
  registered: "Registered",
  converted: "Converted",
  no_response: "No Response",
  failed: "Failed",
  bounced: "Bounced",
  unsubscribed: "Unsubscribed",
  deleting: "Deleting",
};

function StatusDonut({ breakdown }: { breakdown: Record<string, number> }) {
  const entries = Object.entries(breakdown).filter(([, v]) => v > 0);
  const total = entries.reduce((sum, [, v]) => sum + v, 0);
  if (total === 0 || entries.length === 0) {
    return (
      <div className="rounded-lg border border-white/5 bg-white/[0.02] p-3 flex items-center justify-center h-full min-h-[180px]">
        <p className="font-mono text-[10px] text-slate-500">No data</p>
      </div>
    );
  }
  const radius = 42;
  const circumference = 2 * Math.PI * radius;
  let offset = 0;
  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-3">
      <p className="font-mono text-[9px] text-slate-500 uppercase tracking-[0.1em] mb-2 text-center">Status</p>
      <svg viewBox="0 0 120 120" className="w-full max-w-[120px] mx-auto">
        {entries.map(([key, val]) => {
          const length = (val / total) * circumference;
          const dash = `${length} ${circumference - length}`;
          const s = offset;
          offset += length;
          return (
            <circle
              key={key}
              cx="60"
              cy="60"
              r={radius}
              fill="none"
              stroke={DONUT_COLORS[key] || "#475569"}
              strokeWidth="14"
              strokeDasharray={dash}
              strokeDashoffset={-s}
              transform="rotate(-90 60 60)"
            />
          );
        })}
        <text x="60" y="56" textAnchor="middle" className="fill-white font-mono text-lg font-bold">
          {total}
        </text>
        <text x="60" y="70" textAnchor="middle" className="fill-slate-500 font-mono text-[8px]">
          contacts
        </text>
      </svg>
      <div className="space-y-1 mt-2">
        {entries.map(([key, val]) => (
          <div key={key} className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-1.5 min-w-0">
              <span className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: DONUT_COLORS[key] || "#475569" }} />
              <span className="font-mono text-[9px] text-slate-400 truncate">{DONUT_LABELS[key] || key}</span>
            </div>
            <span className="font-mono text-[9px] text-white shrink-0">{val}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Audience Tab ──

function AudienceTab({ campaignId }: { campaignId: string }) {
  const OutreachTab = require("@/components/beta/OutreachTab").default;

  return <OutreachTab campaignId={campaignId} />;
}

// ── Templates Tab ──

// ── Settings Tab ──

function SettingsTab({
  campaign,
  canManage,
  onRefresh,
  onRegisterSettingsValidate,
  onSaved,
}: {
  campaign: Campaign;
  canManage: boolean;
  onRefresh: () => void;
  onRegisterSettingsValidate?: (fn: () => boolean) => void;
  onSaved?: () => void;
}) {
  const [settings, setSettings] = useState<CampaignConfig>({ ...campaign.settings });
  const [accounts, setAccounts] = useState<{ email: string; displayName: string }[]>([]);
  const [senderOption, setSenderOption] = useState<"default" | "specific">(
    campaign.settings.senderAccountId ? "specific" : "default"
  );
  const [templateSequence, setTemplateSequence] = useState<string[]>(
    (campaign.settings.templateSequence?.length ?? 0) > 0
      ? [...campaign.settings.templateSequence!]
      : Array(campaign.settings.maxInvites || 5).fill("")
  );
  const [shakePositions, setShakePositions] = useState<Set<number>>(new Set());
  const [availableTemplates, setAvailableTemplates] = useState<TemplateWithSource[]>([]);
  const [templateSubjects, setTemplateSubjects] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const settingsLocked = !canManage || (campaign.status !== "draft" && campaign.status !== "scheduled" && campaign.status !== "paused");
  const templatesEditable = canManage && (campaign.status === "draft" || campaign.status === "scheduled" || campaign.status === "paused");

  useEffect(() => {
    fetch("/api/email-accounts")
      .then((r) => r.json())
      .then((d) => setAccounts(d.accounts || []))
      .catch(() => {});
  }, []);

  useEffect(() => {
    import("@/lib/firebase/templateService").then(({ loadAllTemplates }) =>
      loadAllTemplates().then(setAvailableTemplates).catch(() => {})
    ).catch(() => {});
  }, []);

  useEffect(() => {
    if (availableTemplates.length === 0) return;
    const loadSubjects = async () => {
      const subjects: Record<string, string> = {};
      for (const t of availableTemplates) {
        if (t.subject) {
          subjects[t.templateKey] = t.subject;
        } else {
          try {
            const { renderTemplate } = await import("@/lib/emailTemplates");
            const rendered = renderTemplate(t.templateKey as any, "User");
            subjects[t.templateKey] = rendered.subject;
          } catch {
            subjects[t.templateKey] = "";
          }
        }
      }
      setTemplateSubjects(subjects);
    };
    loadSubjects();
  }, [availableTemplates]);

  useEffect(() => {
    if (onRegisterSettingsValidate) {
      onRegisterSettingsValidate(() => {
        const emptyPositions = new Set<number>();
        templateSequence.forEach((t, i) => {
          if (!t || t.trim() === "") emptyPositions.add(i);
        });
        setShakePositions(emptyPositions);
        if (emptyPositions.size > 0) {
          setTimeout(() => setShakePositions(new Set()), 1000);
          return false;
        }
        return true;
      });
    }
  }, [templateSequence, onRegisterSettingsValidate]);

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      const updatedSettings = {
        ...settings,
        senderAccountId: senderOption === "specific" ? settings.senderAccountId : null,
        templateSequence,
      };

      const res = await fetch(`/api/campaigns/${campaign.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ settings: updatedSettings }),
      });

      if (!res.ok) throw new Error("Failed to save");

      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
      onSaved?.();
      onRefresh();
    } catch (e: any) {
      setError(e?.message || "Failed to save settings");
    } finally {
      setSaving(false);
    }
  };

  const campaignStartedAt = safeDate(campaign.scheduledStartAt) ?? safeDate(campaign.createdAt);

  return (
    <div className="max-w-lg space-y-5">
      {settingsLocked && (
        <div className="rounded-lg border border-amber-400/20 bg-amber-400/10 px-4 py-3">
          <p className="font-mono text-xs text-amber-400">
            Campaign settings are locked while the campaign is {campaign.status}.
          </p>
        </div>
      )}

      {error && (
        <div className="rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3">
          <p className="font-mono text-xs text-red-400">{error}</p>
        </div>
      )}
      {saved && (
        <div className="rounded-lg border border-green-400/20 bg-green-400/10 px-4 py-3">
          <p className="font-mono text-xs text-green-400">Settings saved.</p>
        </div>
      )}

      <Field label="Delay Between Emails (days)">
        <input type="number" value={settings.delayDays} onChange={(e) => setSettings({ ...settings, delayDays: parseInt(e.target.value) || 4 })}
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
          min={1} max={30} disabled={settingsLocked} />
      </Field>

      <Field label="Max Invites Per Contact">
        <input type="number" value={settings.maxInvites} onChange={(e) => {
          const n = parseInt(e.target.value) || 5;
          setSettings({ ...settings, maxInvites: n });
          setTemplateSequence((prev) => {
            const seq = [...prev];
            while (seq.length < n) {
              seq.push("");
            }
            while (seq.length > n) seq.pop();
            return seq;
          });
          setShakePositions(new Set());
        }}
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
          min={1} max={20} disabled={settingsLocked} />
      </Field>

      <Field label="Mail Sequence">
        {Array.from({ length: settings.maxInvites }, (_, i) => {
          const scheduledDate = campaignStartedAt
            ? new Date(campaignStartedAt.getTime() + i * settings.delayDays * 24 * 60 * 60 * 1000)
            : null;
          return (
            <div key={i} className="flex items-center gap-3 mb-2">
              <span className="font-mono text-[11px] text-slate-500 w-16 shrink-0">Mail {i + 1}</span>
              <div className={`flex-1 ${shakePositions.has(i) ? "animate-shake" : ""}`}>
                <select
                  value={templateSequence[i] ?? ""}
                  onChange={(e) => {
                    const seq = [...templateSequence];
                    seq[i] = e.target.value;
                    setTemplateSequence(seq);
                    setShakePositions((prev) => { const n = new Set(prev); n.delete(i); return n; });
                  }}
                  className={`w-full rounded-lg border bg-black px-3 py-2 font-mono text-xs text-white outline-none transition disabled:opacity-30 ${
                    shakePositions.has(i) ? "border-red-400/60" : "border-white/10 focus:border-cyan-400/40"
                  }`}
                  disabled={!templatesEditable}
                >
                  <option value="">— Select template —</option>
                  {availableTemplates.map((t) => (
                    <option key={t.templateKey} value={t.templateKey}>{t.name}</option>
                  ))}
                  <option disabled className="text-slate-600">{'\u2500'.repeat(16)}</option>
                  <option value="invitation">invitation (auto)</option>
                </select>
                {templateSequence[i] && templateSubjects[templateSequence[i]] && (
                  <p className="font-mono text-[9px] text-slate-500 mt-1 truncate">
                    {templateSubjects[templateSequence[i]]}
                  </p>
                )}
              </div>
              {scheduledDate && (
                <span className="font-mono text-[9px] text-slate-600 w-28 shrink-0 text-right">
                  {scheduledDate.toLocaleDateString()}
                </span>
              )}
            </div>
          );
        })}
      </Field>

      <Field label="Batch Size">
        <input type="number" value={settings.batchSize} onChange={(e) => setSettings({ ...settings, batchSize: parseInt(e.target.value) || 5 })}
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
          min={1} max={100} disabled={settingsLocked} />
      </Field>

      <Field label="Max Emails Per Hour">
        <input type="number" value={settings.maxEmailsPerHour} onChange={(e) => setSettings({ ...settings, maxEmailsPerHour: parseInt(e.target.value) || 100 })}
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
          min={1} max={500} disabled={settingsLocked} />
      </Field>

      <Field label="Delay Between Emails (seconds)">
        <input type="number" value={Math.round(settings.delayBetweenEmailsMs / 1000)} onChange={(e) => setSettings({ ...settings, delayBetweenEmailsMs: (parseInt(e.target.value) || 120) * 1000 })}
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 disabled:opacity-30"
          min={1} max={300} step={1} disabled={settingsLocked} />
      </Field>

      <div className="border-t border-white/10 pt-5">
        <label className="font-mono text-xs text-slate-400 mb-3 block">Campaign Sender</label>
        <div className="space-y-3">
          <label className="flex items-center gap-3">
            <input type="radio" name="senderOption" checked={senderOption === "default"}
              onChange={() => setSenderOption("default")} className="accent-cyan-400" disabled={settingsLocked} />
            <span className="font-mono text-sm text-white">Use Default Sender</span>
          </label>
          <label className="flex items-center gap-3">
            <input type="radio" name="senderOption" checked={senderOption === "specific"}
              onChange={() => setSenderOption("specific")} className="accent-cyan-400" disabled={settingsLocked} />
            <span className="font-mono text-sm text-white">Select Specific Sender</span>
          </label>
          {senderOption === "specific" && (
            <select
              value={settings.senderAccountId || ""}
              onChange={(e) => setSettings({ ...settings, senderAccountId: e.target.value || null })}
              className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 font-mono text-sm text-white outline-none focus:border-cyan-400/40 mt-2 disabled:opacity-30"
              disabled={settingsLocked}
            >
              <option value="">Select an account...</option>
              {accounts.map((a) => (
                <option key={a.email} value={a.email}>{a.displayName || a.email}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {canManage && (
        <button onClick={handleSave} disabled={saving || settingsLocked}
          className="mt-4 rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-8 py-3.5 font-mono text-xs tracking-[0.15em] text-cyan-300 uppercase transition-all hover:bg-cyan-400/20 disabled:opacity-50">
          {saving ? "Saving..." : "Save Settings"}
        </button>
      )}
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="font-mono text-xs text-slate-400 mb-1.5 block">{label}</label>
      {children}
    </div>
  );
}

// ── Activity Tab ──

const ACTIVITY_PAGE_SIZE = 20;

function ActivityTab({ campaignId }: { campaignId: string }) {
  const [allEvents, setAllEvents] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [visibleCount, setVisibleCount] = useState(ACTIVITY_PAGE_SIZE);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const { fetchCampaignTimeline } = await import("@/lib/firebase/activityService");
        const data = await fetchCampaignTimeline(campaignId);
        setAllEvents(data.reverse());
      } catch {
        setAllEvents([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [campaignId]);

  if (loading) return <p className="font-mono text-xs text-slate-500 animate-pulse">Loading activity...</p>;

  if (allEvents.length === 0) {
    return <p className="font-mono text-xs text-slate-500">No activity recorded for this campaign.</p>;
  }

  const visible = allEvents.slice(0, visibleCount);
  const hasMore = visibleCount < allEvents.length;

  return (
    <div className="space-y-1 max-w-2xl">
      {visible.map((event) => (
        <div key={event.id} className="flex items-start gap-3 py-2 border-b border-white/5">
          <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-cyan-500" />
          <div className="min-w-0">
            <p className="font-mono text-xs text-white">{event.details || event.eventType}</p>
            <p className="font-mono text-[10px] text-slate-500">
              {event.eventType} &middot;{" "}
              {safeDate(event.createdAt)?.toLocaleString() ?? "Just now"}
            </p>
          </div>
        </div>
      ))}
      {hasMore && (
        <button
          onClick={() => setVisibleCount((prev) => prev + ACTIVITY_PAGE_SIZE)}
          className="w-full py-3 font-mono text-[11px] text-slate-500 hover:text-white transition-colors"
        >
          Load More ({Math.min(ACTIVITY_PAGE_SIZE, allEvents.length - visibleCount)})
        </button>
      )}
    </div>
  );
}

// ── Errors Tab ──

const ERRORS_PAGE_SIZE = 10;

function ErrorsTab({ campaignId, campaignName }: { campaignId: string; campaignName: string }) {
  const [allErrors, setAllErrors] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [visibleCount, setVisibleCount] = useState(ERRORS_PAGE_SIZE);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const { fetchCampaignErrors } = await import("@/lib/firebase/activityService");
        const merged = await fetchCampaignErrors(campaignId);
        setAllErrors(merged);
      } catch {
        setAllErrors([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [campaignId]);

  if (loading) return <p className="font-mono text-xs text-slate-500 animate-pulse">Loading errors...</p>;

  if (allErrors.length === 0) {
    return <p className="font-mono text-xs text-slate-500">No errors recorded for this campaign.</p>;
  }

  const visible = allErrors.slice(0, visibleCount);
  const hasMore = visibleCount < allErrors.length;

  return (
    <div className="space-y-2 max-w-3xl">
      {visible.map((err) => (
        <div key={`${err._source}-${err.id}`} className={`rounded-lg border p-4 ${
          err._source === "emailLog"
            ? "border-red-400/10 bg-red-400/5"
            : "border-amber-400/10 bg-amber-400/5"
        }`}>
          <div className="flex items-center gap-2 mb-1">
            <span className={`h-2 w-2 shrink-0 rounded-full ${
              err._source === "emailLog" ? "bg-red-400" : "bg-amber-400"
            }`} />
            <p className="font-mono text-xs text-white">{err.recipient}</p>
            <p className="font-mono text-[10px] text-slate-500 ml-auto">
              {safeDate(err.timestamp)?.toLocaleString() ?? "—"}
            </p>
          </div>
          <p className={`font-mono text-[11px] ${
            err._source === "emailLog" ? "text-red-300" : "text-amber-300"
          }`}>{err.error}</p>
          {err.template && (
            <p className="font-mono text-[10px] text-slate-500 mt-1">Template: {err.template}</p>
          )}
          {err._source === "activityLog" && (
            <p className="font-mono text-[9px] text-slate-600 mt-1">Source: activity log</p>
          )}
        </div>
      ))}
      {hasMore && (
        <button
          onClick={() => setVisibleCount((prev) => prev + ERRORS_PAGE_SIZE)}
          className="w-full py-3 font-mono text-[11px] text-slate-500 hover:text-white transition-colors"
        >
          Load More ({Math.min(ERRORS_PAGE_SIZE, allErrors.length - visibleCount)})
        </button>
      )}
    </div>
  );
}
