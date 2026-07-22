"use client";

import type { OutreachContact } from "@/types/recruitmentContacts";

interface Props {
  contact: OutreachContact;
  campaignId?: string;
  onClose: () => void;
}

function safeDate(ts: { seconds?: number } | null | undefined): string {
  if (!ts?.seconds) return "—";
  return new Date(ts.seconds * 1000).toLocaleString();
}

export default function StatusDetailModal({ contact, campaignId, onClose }: Props) {
  const c = contact;
  const d = campaignId ? c.campaignData?.[campaignId] : undefined;
  const status = d?.status || c.status || "unknown";
  const name = c.name || c.email;

  let details: { label: string; value: string }[] = [];

  if (status === "replied") {
    details = [
      { label: "Step", value: `${d?.currentStep ?? "—"}` },
      { label: "Reply Date", value: safeDate(d?.repliedAt) },
      { label: "Snippet", value: "" },
    ];
  } else if (status === "bounced") {
    details = [
      { label: "Reason", value: d?.stoppedReason || "permanent_bounce" },
      { label: "Date", value: "—" },
    ];
  } else if (status === "failed") {
    details = [
      { label: "Error", value: d?.stoppedReason || "Unknown" },
      { label: "Last Attempt", value: safeDate(d?.lastInviteAt) },
    ];
  } else if (status === "no_response") {
    details = [
      { label: "Total Invites", value: `${d?.inviteCount ?? 0}` },
      { label: "Last Invited", value: safeDate(d?.lastInviteAt) },
      { label: "Reason", value: d?.stoppedReason || "max_invites_reached" },
    ];
  } else if (status === "invited") {
    details = [
      { label: "Last Invited", value: safeDate(d?.lastInviteAt) },
      { label: "Next Eligible", value: safeDate(d?.nextEligibleAt) },
      { label: "Current Step", value: `${d?.currentStep ?? "—"}` },
    ];
  } else if (status === "converted") {
    details = [
      { label: "Converted", value: "Yes" },
    ];
  } else if (status === "unsubscribed") {
    details = [
      { label: "Unsubscribed", value: "Opted out" },
    ];
  } else {
    details = [
      { label: "Status", value: status },
    ];
  }

  const snippet = d?.replySnippet || "";
  const words = snippet.split(/\s+/).slice(0, 300).join(" ");

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-[10vh]" onClick={onClose}>
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" />
      <div
        className="relative w-full max-w-lg mx-4 rounded-xl border border-white/10 bg-[#0a0a0f] shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-white/5">
          <div>
            <h2 className="font-mono text-sm font-bold text-white">{name}</h2>
            <p className="font-mono text-[10px] text-slate-500 mt-0.5">{status}</p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg border border-white/10 px-3 py-1.5 font-mono text-[11px] text-slate-400 hover:text-white hover:border-white/20 transition-colors"
          >
            Close
          </button>
        </div>
        <div className="px-5 py-4 space-y-3">
          <div className="grid grid-cols-2 gap-3">
            {details.map((det) =>
              det.label !== "Snippet" ? (
                <div key={det.label}>
                  <p className="font-mono text-[9px] text-slate-600 uppercase tracking-[0.15em]">{det.label}</p>
                  <p className="font-mono text-xs text-white mt-0.5">{det.value || "—"}</p>
                </div>
              ) : null
            )}
          </div>
          {status === "replied" && words && (
            <div className="border-t border-white/5 pt-3">
              <p className="font-mono text-[9px] text-slate-600 uppercase tracking-[0.15em] mb-1.5">Reply Text</p>
              <div className="max-h-40 overflow-y-auto font-mono text-[10px] text-slate-400 leading-relaxed whitespace-pre-wrap bg-white/[0.02] rounded-lg p-3 border border-white/5">
                {words || "—"}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
