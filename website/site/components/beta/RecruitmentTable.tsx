"use client";

import { useState, useMemo, useEffect } from "react";
import type { RecruitmentApplicant } from "@/types/recruitment";
import StatusBadge from "./StatusBadge";
import SendEmailDialog from "./SendEmailDialog";

interface Props {
  applicants: RecruitmentApplicant[];
  onSelect: (applicant: RecruitmentApplicant) => void;
  campaignId?: string;
}

function formatDate(ts: { seconds: number } | null): string {
  if (!ts?.seconds) return "—";
  return new Date(ts.seconds * 1000).toLocaleDateString();
}

function formatPhone(v: string): string {
  return v || "—";
}

function SourceBadge({ source }: { source: string }) {
  const s = source || "website";
  return (
    <span className="inline-block rounded-full border border-white/10 px-2 py-0.5 font-mono text-[10px] text-slate-400 leading-none capitalize">
      {s}
    </span>
  );
}

export default function RecruitmentTable({ applicants, onSelect, campaignId }: Props) {
  const [search, setSearch] = useState("");
  const [emailDialog, setEmailDialog] = useState<{ email: string; name: string } | null>(null);
  const [campaignPopup, setCampaignPopup] = useState<string | null>(null);
  const [campaignNames, setCampaignNames] = useState<Record<string, string>>({});
  const [selectedCampaignFilter, setSelectedCampaignFilter] = useState<string>("__all__");
  const [campaigns, setCampaigns] = useState<{ id: string; name: string }[]>([]);

  useEffect(() => {
    fetch("/api/campaigns")
      .then((r) => r.json())
      .then((list: any[]) => {
        const map: Record<string, string> = {};
        list.forEach((c) => { map[c.id] = c.name; });
        setCampaignNames(map);
        setCampaigns(list);
      })
      .catch(() => {});
  }, []);

  const filtered = useMemo(() => {
    let result = applicants;
    if (!campaignId && selectedCampaignFilter !== "__all__") {
      result = result.filter((a) => a.campaigns?.includes(selectedCampaignFilter));
    }
    if (!search.trim()) return result;
    const q = search.toLowerCase().trim();
    return result.filter(
      (a) =>
        a.email.toLowerCase().includes(q) ||
        a.name.toLowerCase().includes(q) ||
        a.phone.includes(q)
    );
  }, [applicants, search, campaignId, selectedCampaignFilter]);

  return (
    <div>
      <div className="flex items-center gap-3 mb-4">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, or phone..."
          className="flex-1 rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
        />
        {!campaignId && campaigns.length > 0 && (
          <select
            value={selectedCampaignFilter}
            onChange={(e) => setSelectedCampaignFilter(e.target.value)}
            className="rounded-lg border border-white/10 bg-black px-3 py-3 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40"
          >
            <option value="__all__">All Campaigns</option>
            {campaigns.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        )}
      </div>

      <div className="rounded-lg border border-white/5 bg-white/[0.02] overflow-x-auto">
        <table className="w-full text-left">
          <thead>
            <tr className="border-b border-white/5">
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Name
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Email
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Phone
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Registered
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Status
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Campaigns
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Code Jam
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Source
              </th>
              <th className="px-4 py-3 w-10"></th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td
                  colSpan={9}
                  className="px-4 py-8 text-center font-mono text-xs text-slate-500"
                >
                  {search.trim()
                    ? "No applicants match your search."
                    : "No applicants found."}
                </td>
              </tr>
            ) : (
              filtered.map((a) => (
                <tr
                  key={a.docId}
                  className="border-b border-white/5 hover:bg-white/[0.01] cursor-pointer"
                  onClick={() => onSelect(a)}
                >
                  <td className="px-4 py-3">
                    <span className="font-mono text-xs text-white">
                      {a.name || "—"}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {a.email}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {formatPhone(a.phone)}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {formatDate(a.registeredAt)}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <StatusBadge status={a.status} />
                      {a.status === "approved" && a.emailStatus === "failed" && (
                        <span className="font-mono text-[10px] text-red-400">
                          &bull; Email Failed
                        </span>
                      )}
                      {a.status === "approved" && a.emailStatus === "pending" && (
                        <span className="font-mono text-[10px] text-amber-400">
                          &bull; Email Pending
                        </span>
                      )}
                    </div>
                  </td>
                  <td
                    className="px-4 py-3 relative font-mono text-xs text-slate-400 hover:text-cyan-300 transition-colors cursor-pointer"
                    onClick={(e) => {
                      e.stopPropagation();
                      const next = campaignPopup === a.email ? null : a.email;
                      setCampaignPopup(next);
                    }}
                  >
                    {(a.campaigns?.length ?? 0) > 0
                      ? `${(a.campaigns ?? []).length} Campaign${(a.campaigns ?? []).length > 1 ? "s" : ""}`
                      : "\u2014"}
                    {campaignPopup === a.email && (a.campaigns?.length ?? 0) > 0 && (
                      <div className="absolute top-full left-0 mt-1 z-50 w-56 rounded-lg border border-white/10 bg-[#0a0a0f] p-3 shadow-2xl">
                        <p className="font-mono text-[10px] text-slate-500 uppercase tracking-[0.1em] mb-2">Campaigns</p>
                        <div className="space-y-2">
                          {a.campaigns!.map((cid) => {
                            const cd = a.campaignData?.[cid];
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
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {a.codeJam ? "Yes" : "No"}
                  </td>
                  <td className="px-4 py-3">
                    <SourceBadge source={a.source} />
                  </td>
                  <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => setEmailDialog({ email: a.email, name: a.name || a.email })}
                      className="text-slate-500 hover:text-cyan-400 transition-colors"
                      title="Send email"
                    >
                      &#x2709;
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {emailDialog && (
        <SendEmailDialog
          recipientEmail={emailDialog.email}
          recipientName={emailDialog.name}
          onClose={() => setEmailDialog(null)}
        />
      )}
    </div>
  );
}
