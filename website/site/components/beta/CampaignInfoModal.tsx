"use client";

import type { OutreachContact } from "@/types/recruitmentContacts";

interface Props {
  contact: OutreachContact;
  campaignNames: Record<string, string>;
  onClose: () => void;
}

export default function CampaignInfoModal({ contact, campaignNames, onClose }: Props) {
  const isUnsubscribed = contact.status === "unsubscribed";
  const campaigns = contact.campaigns ?? [];

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      onClick={onClose}
    >
      <div className="fixed inset-0 bg-black/70" />
      <div
        className="relative w-full max-w-md mx-4 rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Campaigns
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        <p className="font-mono text-[11px] text-slate-400 mb-4 break-all">
          {contact.name || contact.email}
        </p>

        {isUnsubscribed && (
          <div className="mb-4 rounded-md border border-red-400/30 bg-red-400/10 px-4 py-3">
            <div className="flex items-center gap-2">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-red-400/20 border border-red-400/40">
                <span className="text-[9px] font-bold text-red-400">U</span>
              </span>
              <span className="font-mono text-[11px] text-red-400">
                This contact is globally unsubscribed &mdash; no emails will be sent.
              </span>
            </div>
          </div>
        )}

        {campaigns.length === 0 ? (
          <p className="font-mono text-xs text-slate-500">
            Not enrolled in any campaigns.
          </p>
        ) : (
          <div className="space-y-2">
            {campaigns.map((cid) => {
              const cd = contact.campaignData?.[cid];
              return (
                <div
                  key={cid}
                  className="flex items-start gap-2 rounded-lg border border-white/5 bg-white/[0.02] p-3"
                >
                  <span
                    className={`mt-1 h-2 w-2 shrink-0 rounded-full ${
                      isUnsubscribed ? "bg-red-500" : "bg-cyan-500"
                    }`}
                  />
                  <div className="min-w-0 flex-1">
                    <p className="font-mono text-xs text-white leading-tight">
                      {campaignNames[cid] || cid}
                    </p>
                    {cd && (
                      <div className="mt-1 space-y-0.5">
                        <p className="font-mono text-[10px] text-slate-500">
                          Status: <span className="text-cyan-300">{cd.emailStatus}</span>
                          {cd.currentStep != null && (
                            <span> &middot; Step {cd.currentStep}/5</span>
                          )}
                        </p>
                        {cd.inviteCount != null && cd.inviteCount > 0 && (
                          <p className="font-mono text-[10px] text-slate-500">
                            Invites sent: {cd.inviteCount}
                          </p>
                        )}
                        {cd.lastInviteAt?.seconds && (
                          <p className="font-mono text-[10px] text-slate-500">
                            Last sent: {new Date(cd.lastInviteAt.seconds * 1000).toLocaleDateString()}
                          </p>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        <button
          onClick={onClose}
          className="mt-5 w-full rounded-lg border border-white/10 px-4 py-2.5 font-mono text-xs tracking-[0.1em] text-slate-400 transition-colors hover:border-white/20 hover:text-white"
        >
          Close
        </button>
      </div>
    </div>
  );
}