"use client";

import { useState, useMemo, useEffect } from "react";
import type { OutreachContact } from "@/types/recruitmentContacts";
import type { ActivityLogEntry } from "@/types/activityLog";
import { getFirestore } from "@/lib/firebase/config";
import { useRole } from "./AuthContext";
import SendEmailDialog from "./SendEmailDialog";
import CampaignInfoModal from "./CampaignInfoModal";

interface Props {
  contacts: OutreachContact[];
  selected: Set<string>;
  onSelectionChange: (selected: Set<string>) => void;
  onContactDeleted?: (email: string) => void;
  onRefreshContacts?: () => void;
  search: string;
  onSearchChange: (s: string) => void;
  campaignId?: string;
  onStatusClick?: (contact: OutreachContact) => void;
}

const STATUS_STYLES: Record<string, string> = {
  pending: "text-amber-400",
  invited: "text-blue-400",
  registered: "text-green-400",
  failed: "text-red-400",
  bounced: "text-red-500",
  converted: "text-purple-400",
  replied: "text-green-400",
  opened: "text-cyan-400",
  no_response: "text-slate-500",
  deleting: "text-red-400/50",
  unsubscribed: "text-red-400/60",
};

const STATUS_DOTS: Record<string, string> = {
  pending: "bg-amber-400",
  invited: "bg-blue-400",
  registered: "bg-green-400",
  failed: "bg-red-400",
  bounced: "bg-red-500",
  converted: "bg-purple-400",
  replied: "bg-green-400",
  opened: "bg-cyan-400",
  no_response: "bg-slate-500",
  deleting: "bg-red-400/50",
  unsubscribed: "bg-red-400",
};

const STATUS_LABELS: Record<string, string> = {
  pending: "Pending",
  invited: "Invited",
  registered: "Registered",
  failed: "Failed",
  bounced: "Bounced",
  converted: "Converted",
  replied: "Replied",
  opened: "Opened",
  no_response: "No Response",
  deleting: "Deleting",
  unsubscribed: "Unsubscribed",
};

const EVENT_LABELS: Record<string, string> = {
  registered: "Registered",
  acknowledgement_sent: "Acknowledged",
  approved: "Approved",
  welcome_email_sent: "Welcome Sent",
  welcome_email_failed: "Welcome Failed",
  activated: "Activated",
  deleted: "Deleted",
  invitation_sent: "Invitation Sent",
  invitation_failed: "Invitation Failed",
  contact_converted: "Converted",
  contact_no_response: "No Response",
  outreach_duplicate: "Duplicate",
  rejected: "Rejected",
  unsubscribed: "Unsubscribed",
  resubscribed: "Resubscribed",
  skipped_unsubscribed: "Skipped (Unsubscribed)",
  manual_send_triggered: "Manual Send",
  sender_fallback: "Sender Fallback",
  replied: "Replied",
  rate_limited: "Rate Limited",
  system_error: "System Error",
  email_opened: "Email Opened",
};

const EVENT_COLORS: Record<string, string> = {
  registered: "text-cyan-300",
  acknowledgement_sent: "text-blue-300",
  approved: "text-green-300",
  welcome_email_sent: "text-green-300",
  welcome_email_failed: "text-red-300",
  activated: "text-cyan-300",
  deleted: "text-red-300",
  invitation_sent: "text-blue-300",
  invitation_failed: "text-red-300",
  contact_converted: "text-purple-300",
  contact_no_response: "text-slate-400",
  outreach_duplicate: "text-amber-300",
  rejected: "text-red-300",
  unsubscribed: "text-red-400",
  resubscribed: "text-green-400",
  skipped_unsubscribed: "text-red-400/60",
  manual_send_triggered: "text-orange-400",
  sender_fallback: "text-amber-400",
  replied: "text-green-300",
  rate_limited: "text-amber-300",
  system_error: "text-red-300",
  email_opened: "text-cyan-300",
};

const EVENT_DOTS: Record<string, string> = {
  registered: "bg-cyan-400",
  acknowledgement_sent: "bg-blue-400",
  approved: "bg-green-400",
  welcome_email_sent: "bg-green-400",
  welcome_email_failed: "bg-red-400",
  activated: "bg-cyan-400",
  deleted: "bg-red-400",
  invitation_sent: "bg-blue-400",
  invitation_failed: "bg-red-400",
  contact_converted: "bg-purple-400",
  contact_no_response: "bg-slate-500",
  outreach_duplicate: "bg-amber-400",
  rejected: "bg-red-400",
  unsubscribed: "bg-red-500",
  resubscribed: "bg-green-500",
  skipped_unsubscribed: "bg-red-500/50",
  manual_send_triggered: "bg-orange-500",
  sender_fallback: "bg-amber-500",
  replied: "bg-green-400",
  rate_limited: "bg-amber-400",
  system_error: "bg-red-400",
  email_opened: "bg-cyan-400",
};

function formatDate(ts: { seconds: number } | null): string {
  if (!ts?.seconds) return "—";
  return new Date(ts.seconds * 1000).toLocaleDateString();
}

function formatDateTime(ts: { seconds: number } | null): string {
  if (!ts?.seconds) return "—";
  return new Date(ts.seconds * 1000).toLocaleString();
}

function safeDateShort(ts: { seconds?: number } | null | undefined): string {
  if (!ts?.seconds) return "—";
  const d = new Date(ts.seconds * 1000);
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
}

async function softDeleteContact(email: string, previousStatus: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  const deleteAt = new Date(Date.now() + 24 * 60 * 60 * 1000);
  await updateDoc(doc(firestore, "recruitmentContacts", email.toLowerCase().trim()), {
    status: "deleting",
    previousStatus,
    scheduledDeleteAt: {
      seconds: Math.floor(deleteAt.getTime() / 1000),
    },
  });
}

async function restoreContact(email: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, getDoc, updateDoc } = await import("firebase/firestore");
  const ref = doc(firestore, "recruitmentContacts", email.toLowerCase().trim());
  const snap = await getDoc(ref);
  if (!snap.exists()) return;
  const prev = snap.data()?.previousStatus || "pending";
  await updateDoc(ref, {
    status: prev,
    scheduledDeleteAt: null,
    previousStatus: null,
  });
}

async function permanentDeleteContact(email: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, deleteDoc } = await import("firebase/firestore");
  await deleteDoc(doc(firestore, "recruitmentContacts", email.toLowerCase().trim()));
}

function effectiveStatus(c: OutreachContact, campaignId?: string): string {
  if (campaignId) {
    const cd = c.campaignData?.[campaignId];
    if (cd?.status === "replied" || cd?.replied) return "replied";
    if (cd?.opened) return "opened";
    return cd?.status || c.status || "unknown";
  }
  return c.status || "unknown";
}

export default function OutreachContactsTable({
  contacts,
  selected,
  onSelectionChange,
  onContactDeleted,
  onRefreshContacts,
  search,
  onSearchChange,
  campaignId,
  onStatusClick,
}: Props) {
  const { role } = useRole();
  const [emailDialog, setEmailDialog] = useState<{ email: string; name: string } | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [inviteTimelineEmail, setInviteTimelineEmail] = useState<string | null>(null);
  const [inviteTimelineEvents, setInviteTimelineEvents] = useState<ActivityLogEntry[]>([]);
  const [inviteTimelineLoading, setInviteTimelineLoading] = useState(false);
  const [campaignPopupEmail, setCampaignPopupEmail] = useState<string | null>(null);
  const [campaignModalContact, setCampaignModalContact] = useState<OutreachContact | null>(null);
  const [campaignNames, setCampaignNames] = useState<Record<string, string>>({});

  useEffect(() => {
    fetch("/api/campaigns")
      .then((r) => r.json())
      .then((list: any[]) => {
        const map: Record<string, string> = {};
        list.forEach((c) => { map[c.id] = c.name; });
        setCampaignNames(map);
      })
      .catch(() => {});
  }, []);

  const fetchTimelineEvents = async (
    email: string | null,
    setEvents: (e: ActivityLogEntry[]) => void,
    setLoading: (l: boolean) => void
  ) => {
    if (!email) {
      setEvents([]);
      return;
    }
    setLoading(true);
    try {
      const { fetchTimeline } = await import("@/lib/firebase/activityService");
      const events = await fetchTimeline(email);
      setEvents(events);
    } catch {
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    if (!search.trim()) return contacts;
    const q = search.toLowerCase().trim();
    return contacts.filter(
      (c) =>
        c.email.toLowerCase().includes(q) ||
        c.name.toLowerCase().includes(q)
    );
  }, [contacts, search]);

  const allSelected =
    filtered.length > 0 && filtered.every((c) => selected.has(c.email));
  const someSelected =
    filtered.some((c) => selected.has(c.email)) && !allSelected;

  const toggleAll = () => {
    const next = new Set(selected);
    if (allSelected) {
      filtered.forEach((c) => next.delete(c.email));
    } else {
      filtered.forEach((c) => next.add(c.email));
    }
    onSelectionChange(next);
  };

  const toggleOne = (email: string) => {
    const next = new Set(selected);
    if (next.has(email)) next.delete(email);
    else next.add(email);
    onSelectionChange(next);
  };

  const handleDelete = async (email: string) => {
    setDeleting(true);
    try {
      const c = contacts.find((x) => x.email === email);
      await softDeleteContact(email, c?.status || "pending");
      onContactDeleted?.(email);
      onRefreshContacts?.();
      setConfirmDelete(null);
    } catch {
      // handled silently
    } finally {
      setDeleting(false);
    }
  };

  const handleRestore = async (email: string) => {
    try {
      await restoreContact(email);
      onContactDeleted?.(email);
      onRefreshContacts?.();
    } catch {
      // handled silently
    }
  };

  const handlePermanentDelete = async (email: string) => {
    try {
      await permanentDeleteContact(email);
      onContactDeleted?.(email);
      onRefreshContacts?.();
    } catch {
      // handled silently
    }
  };

  function latestInviteAt(c: OutreachContact): { seconds: number } | null {
    let latest: { seconds: number } | null = null;
    for (const cd of Object.values(c.campaignData ?? {})) {
      if (cd.lastInviteAt && (!latest || cd.lastInviteAt.seconds > latest.seconds)) latest = cd.lastInviteAt;
    }
    return latest;
  }

  function earliestNextEligible(c: OutreachContact): { seconds: number } | null {
    let earliest: { seconds: number } | null = null;
    for (const cd of Object.values(c.campaignData ?? {})) {
      if (cd.nextEligibleAt && (!earliest || cd.nextEligibleAt.seconds < earliest.seconds)) earliest = cd.nextEligibleAt;
    }
    return earliest;
  }

  function totalInvites(c: OutreachContact): number {
    return Object.values(c.campaignData ?? {}).reduce((sum, cd) => sum + (cd.inviteCount ?? 0), 0);
  }

  function statusTooltip(c: OutreachContact): string {
    const s = effectiveStatus(c, campaignId);
    const d = campaignId ? c.campaignData?.[campaignId] : undefined;
    if (s === "replied") return `Replied ${safeDateShort(d?.repliedAt)} — Step ${d?.currentStep || "—"}`;
    if (s === "opened") return `Opened ${safeDateShort(d?.openedAt)}`;
    if (s === "bounced") return `Bounced: ${d?.stoppedReason || "permanent_bounce"}`;
    if (s === "failed") return `Failed: ${d?.stoppedReason || "Unknown"}`;
    if (s === "no_response") return `No response after ${d?.inviteCount ?? 0} invites`;
    if (s === "invited") return `Invited ${safeDateShort(d?.lastInviteAt)} — Step ${d?.currentStep || "—"}`;
    if (s === "converted") return "Converted to active member";
    if (s === "unsubscribed") return "Opted out";
    if (s === "pending") return "Not yet emailed";
    return s;
  }

  return (
    <div>

      <div className="rounded-lg border border-white/5 bg-white/[0.02] overflow-x-auto">
        <table className="w-full text-left">
          <thead>
            <tr className="border-b border-white/5">
              <th className="px-4 py-3 w-10">
                <input
                  type="checkbox"
                  checked={allSelected}
                  ref={(el) => {
                    if (el) el.indeterminate = someSelected;
                  }}
                  onChange={toggleAll}
                  className="h-4 w-4 rounded border-white/10 bg-black text-cyan-400 focus:ring-cyan-400/20 focus:ring-1"
                />
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Name
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Email
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Status
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Last Sent
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Next Planned
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Step
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Campaigns
              </th>
              <th className="px-4 py-3 w-10"></th>
              <th className="px-4 py-3 w-10"></th>
              {role !== "user" && <th className="px-4 py-3 w-10"></th>}
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={role !== "user" ? 11 : 10} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                  {search.trim() ? "No contacts match your search." : "No contacts yet. Import a CSV to get started."}
                </td>
              </tr>
            ) : (
              filtered.map((c) => (
                <tr
                  key={c.email}
                  className="border-b border-white/5 hover:bg-white/[0.01] cursor-pointer"
                  onClick={() => {
                    fetchTimelineEvents(c.email, setInviteTimelineEvents, setInviteTimelineLoading);
                    setInviteTimelineEmail(c.email);
                  }}
                >
                  <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={selected.has(c.email)}
                      onChange={() => toggleOne(c.email)}
                      className="h-4 w-4 rounded border-white/10 bg-black text-cyan-400 focus:ring-cyan-400/20 focus:ring-1"
                    />
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-white">
                    {c.name || "—"}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {c.email}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className="group/status relative inline-flex items-center gap-1.5 cursor-pointer"
                      onClick={(e) => { e.stopPropagation(); onStatusClick?.(c); }}
                    >
                      <div className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2 mb-1.5 hidden group-hover/status:block z-10">
                        <div className="bg-slate-800 text-slate-200 font-mono text-[9px] px-2 py-1 rounded whitespace-nowrap shadow-lg border border-white/10">
                          {statusTooltip(c)}
                        </div>
                      </div>
                      {c.status === "unsubscribed" ? (
                        <>
                          <span className="h-1.5 w-1.5 rounded-full bg-red-400" />
                          <span className="text-red-400/60 font-mono text-xs">Unsubscribed</span>
                        </>
                      ) : (
                        <>
                          <span className={`h-1.5 w-1.5 shrink-0 rounded-full ${STATUS_DOTS[effectiveStatus(c, campaignId)] ?? "bg-slate-500"}`} />
                          <span className={`font-mono text-xs ${STATUS_STYLES[effectiveStatus(c, campaignId)] ?? "text-slate-400"}`}>
                            {STATUS_LABELS[effectiveStatus(c, campaignId)] ?? effectiveStatus(c, campaignId)}
                          </span>
                        </>
                      )}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {formatDate(campaignId ? c.campaignData?.[campaignId]?.lastInviteAt : latestInviteAt(c))}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {formatDate(campaignId ? c.campaignData?.[campaignId]?.nextEligibleAt : earliestNextEligible(c))}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {campaignId
                      ? (c.campaignData?.[campaignId]?.currentStep ?? 0) > 0
                        ? `${c.campaignData?.[campaignId]?.currentStep}`
                        : "\u2014"
                      : totalInvites(c) > 0
                        ? `${totalInvites(c)}`
                        : "\u2014"}
                  </td>
                  <td
                    className="px-4 py-3 relative font-mono text-xs transition-colors"
                    onMouseEnter={() => setCampaignPopupEmail(c.email)}
                    onMouseLeave={() => setCampaignPopupEmail(null)}
                    onClick={(e) => {
                      e.stopPropagation();
                      setCampaignModalContact(c);
                    }}
                  >
                    {c.status === "unsubscribed" ? (
                      <span className="text-red-400 cursor-pointer hover:text-red-300">
                        Unsubscribed
                      </span>
                    ) : (c.campaigns?.length ?? 0) > 0 ? (
                      <span className="text-slate-400 cursor-pointer hover:text-cyan-300">
                        {c.campaigns.length} Campaign{c.campaigns.length > 1 ? "s" : ""}
                      </span>
                    ) : (
                      <span className="text-slate-500">{'\u2014'}</span>
                    )}
                    {campaignPopupEmail === c.email && c.campaigns?.length > 0 && (
                      <div className="absolute top-full left-0 mt-1 z-50 w-56 rounded-lg border border-white/10 bg-[#0a0a0f] p-3 shadow-2xl">
                        <p className="font-mono text-[10px] text-slate-500 uppercase tracking-[0.1em] mb-2">Campaigns</p>
                        <div className="space-y-2">
                          {c.campaigns.map((cid) => {
                            const cd = c.campaignData?.[cid];
                            return (
                              <div key={cid} className="flex items-start gap-2">
                                <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-cyan-500" />
                                <div className="min-w-0">
                                  <p className="font-mono text-[11px] text-white leading-tight">
                                    {campaignNames[cid] || cid}
                                  </p>
                                  {cd && (
                                    <p className="font-mono text-[9px] text-slate-500">
                                      Status: {cd.emailStatus} &middot; Step {cd.currentStep}
                                    </p>
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => setEmailDialog({ email: c.email, name: c.name || c.email })}
                      className="text-slate-500 hover:text-cyan-400 transition-colors"
                      title="Send email"
                    >
                      &#x2709;
                    </button>
                  </td>
                  <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                    {c.status === "unsubscribed" ? (
                      role === "owner" || role === "admin" ? (
                        <button
                          onClick={async () => {
                            const { resubscribeContact } = await import("@/lib/firebase/outreachService");
                            try {
                              await resubscribeContact(c.email);
                              onContactDeleted?.(c.email);
                              onRefreshContacts?.();
                            } catch {}
                          }}
                          className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-red-400/20 border border-red-400/40 hover:bg-red-400/30 transition-colors"
                          title="Resubscribe"
                        >
                          <span className="text-[9px] font-bold text-red-400">U</span>
                        </button>
                      ) : (
                        <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-red-400/20 border border-red-400/40" title="Unsubscribed">
                          <span className="text-[9px] font-bold text-red-400">U</span>
                        </span>
                      )
                    ) : (
                      <button
                        onClick={async () => {
                          const { unsubscribeContact } = await import("@/lib/firebase/outreachService");
                          try {
                            await unsubscribeContact(c.email);
                            onContactDeleted?.(c.email);
                            onRefreshContacts?.();
                          } catch {}
                        }}
                        className="inline-flex h-5 w-5 items-center justify-center rounded-full border border-red-400/40 text-red-400 hover:bg-red-400/10 transition-colors"
                        title="Unsubscribe"
                      >
                        <span className="text-xs leading-none">&minus;</span>
                      </button>
                    )}
                  </td>
                  {role !== "user" && (
                    <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                      {c.status === "deleting" ? (
                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => handleRestore(c.email)}
                            className="text-[10px] text-green-400 hover:text-green-300 transition-colors"
                            title="Cancel delete and restore"
                          >
                            Restore
                          </button>
                          <button
                            onClick={() => handlePermanentDelete(c.email)}
                            className="text-[10px] text-red-400 hover:text-red-300 transition-colors"
                            title="Delete permanently now"
                          >
                            Delete Now
                          </button>
                        </div>
                      ) : confirmDelete === c.email ? (
                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => handleDelete(c.email)}
                            disabled={deleting}
                            className="text-[10px] text-red-400 hover:text-red-300 transition-colors"
                          >
                            {deleting ? "..." : "Confirm"}
                          </button>
                          <button
                            onClick={() => setConfirmDelete(null)}
                            className="text-[10px] text-slate-500 hover:text-white transition-colors"
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => setConfirmDelete(c.email)}
                          className="text-slate-500 hover:text-red-400 transition-colors"
                          title="Delete contact"
                        >
                          &#x2715;
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {inviteTimelineEmail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" onClick={() => { setInviteTimelineEmail(null); setInviteTimelineEvents([]); }}>
          <div className="fixed inset-0 bg-black/70" />
          <div
            className="relative max-h-[85vh] w-full max-w-lg mx-4 overflow-y-auto rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-6">
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
                Contact Details
              </h2>
              <button
                onClick={() => { setInviteTimelineEmail(null); setInviteTimelineEvents([]); }}
                className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
              >
                &#x2715;
              </button>
            </div>

            <div className="space-y-6">
              {(() => {
                const c = contacts.find((x) => x.email === inviteTimelineEmail);
                if (!c) return <p className="font-mono text-xs text-slate-500">Contact not found.</p>;
                return (
                  <>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">Name</p>
                        <p className="font-mono text-sm text-white">{c.name || "\u2014"}</p>
                      </div>
                      <div>
                        <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">Email</p>
                        <p className="font-mono text-sm text-cyan-100 break-all">{c.email}</p>
                      </div>
                      <div>
                        <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">Phone</p>
                        <p className="font-mono text-sm text-white">{c.phone || "\u2014"}</p>
                      </div>
                      <div>
                        <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">Status</p>
                        <span className={`font-mono text-sm ${STATUS_STYLES[effectiveStatus(c, campaignId)] ?? "text-slate-400"}`}>
                          {effectiveStatus(c, campaignId)}
                        </span>
                      </div>
                    </div>

                    <div className="border-t border-white/5 pt-4">
                      <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">Campaign Info</p>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <p className="font-mono text-[10px] text-slate-500">Invites Sent</p>
                          <p className="font-mono text-sm text-white">{totalInvites(c)}</p>
                        </div>
                        <div>
                          <p className="font-mono text-[10px] text-slate-500">Campaigns</p>
                          <div className="space-y-1 mt-1">
                            {(c.campaigns ?? []).length > 0
                              ? c.campaigns.map((cid) => (
                                  <p key={cid} className="font-mono text-xs text-cyan-100">
                                    {campaignNames[cid] || cid}
                                  </p>
                                ))
                              : <p className="font-mono text-sm text-white">{'\u2014'}</p>}
                          </div>
                        </div>
                        <div>
                          <p className="font-mono text-[10px] text-slate-500">Last Invited</p>
                          <p className="font-mono text-sm text-white">{formatDateTime(latestInviteAt(c))}</p>
                        </div>
                        <div>
                          <p className="font-mono text-[10px] text-slate-500">Next Eligible</p>
                          <p className="font-mono text-sm text-white">{formatDate(earliestNextEligible(c))}</p>
                        </div>
                        <div>
                          <p className="font-mono text-[10px] text-slate-500">Imported</p>
                          <p className="font-mono text-sm text-white">{formatDate(c.importedAt)}</p>
                        </div>
                        <div>
                          <p className="font-mono text-[10px] text-slate-500">Email Status</p>
                          <p className="font-mono text-sm text-white">{(c as any).emailStatus || "\u2014"}</p>
                        </div>
                      </div>
                    </div>

                    <div className="border-t border-white/5 pt-4">
                      <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">Activity Timeline</p>
                      {inviteTimelineLoading ? (
                        <p className="font-mono text-xs text-slate-500">Loading...</p>
                      ) : inviteTimelineEvents.length === 0 ? (
                        <p className="font-mono text-xs text-slate-500">No activity recorded.</p>
                      ) : (
                        <div className="space-y-1 max-h-48 overflow-y-auto">
                          {inviteTimelineEvents.map((event) => (
                            <div key={event.id} className="flex items-start gap-2 py-1">
                              <span
                                className={`mt-1 h-2 w-2 shrink-0 rounded-full ${
                                  EVENT_DOTS[event.eventType] ?? "bg-slate-500"
                                }`}
                              />
                              <div className="min-w-0">
                                <p
                                  className={`font-mono text-[11px] leading-tight ${
                                    EVENT_COLORS[event.eventType] ?? "text-slate-300"
                                  }`}
                                >
                                  {EVENT_LABELS[event.eventType] ?? event.eventType}
                                </p>
                                {event.details && (
                                  <p className="font-mono text-[10px] text-slate-500 truncate">{event.details}</p>
                                )}
                                <p className="font-mono text-[9px] text-slate-600">
                                  {formatDateTime(event.createdAt)}
                                </p>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </>
                );
              })()}
            </div>
          </div>
        </div>
      )}

      {emailDialog && (
        <SendEmailDialog
          recipientEmail={emailDialog.email}
          recipientName={emailDialog.name}
          onClose={() => setEmailDialog(null)}
        />
      )}

      {campaignModalContact && (
        <CampaignInfoModal
          contact={campaignModalContact}
          campaignNames={campaignNames}
          onClose={() => setCampaignModalContact(null)}
        />
      )}
    </div>
  );
}
