import type { OutreachContact } from "@/types/recruitmentContacts";

interface CardDef {
  key: string;
  label: string;
  color: string;
  bg: string;
  getCount: (c: OutreachContact[], campaignId?: string) => number;
  clickable: boolean;
}

function getCd(c: OutreachContact, campaignId?: string) {
  return campaignId ? c.campaignData?.[campaignId] : undefined;
}

function cntIn(contacts: OutreachContact[], campaignId: string | undefined, fn: (cd: ReturnType<typeof getCd>, c: OutreachContact) => boolean): number {
  return contacts.filter((c) => fn(getCd(c, campaignId), c)).length;
}

function totalIn(contacts: OutreachContact[], _cid?: string): number {
  return contacts.length;
}

function unsubIn(contacts: OutreachContact[], _cid?: string): number {
  return contacts.filter((c) => c.status === "unsubscribed").length;
}

const GLOBAL_CARDS: CardDef[] = [
  { key: "pending",  label: "Pending",     color: "text-amber-400",  bg: "bg-amber-400/8",  clickable: true,  getCount: (cs) => cntIn(cs, undefined, (_, c) => c.status === "pending" && Object.values(c.campaignData ?? {}).every((cd) => !cd.inviteCount)) },
  { key: "invited",  label: "Invited",     color: "text-blue-400",   bg: "bg-blue-400/8",   clickable: false, getCount: (cs) => cs.filter((c) => Object.values(c.campaignData ?? {}).some((cd) => cd.inviteCount > 0) && c.status !== "unsubscribed").length },
  { key: "registered", label: "Registered", color: "text-cyan-400",  bg: "bg-cyan-400/8",   clickable: true,  getCount: (cs) => cntIn(cs, undefined, (_, c) => c.status === "registered") },
  { key: "replied",  label: "Replied",     color: "text-green-400",  bg: "bg-green-400/8",  clickable: true,  getCount: (cs) => cs.filter((c) => Object.values(c.campaignData ?? {}).some((cd) => cd.status === "replied" || cd.replied === true)).length },
  { key: "converted", label: "Converted",  color: "text-purple-400", bg: "bg-purple-400/8", clickable: true,  getCount: (cs) => cntIn(cs, undefined, (_, c) => c.status === "converted") },
  { key: "noResponse", label: "No Response", color: "text-slate-500", bg: "bg-slate-500/8",  clickable: true,  getCount: (cs) => cntIn(cs, undefined, (_, c) => c.status === "no_response") },
  { key: "failed",   label: "Failed",      color: "text-red-400",    bg: "bg-red-400/8",    clickable: true,  getCount: (cs) => cs.filter((c) => Object.values(c.campaignData ?? {}).some((cd) => cd.status === "failed" || cd.emailStatus === "failed")).length },
  { key: "unsubscribed", label: "Unsubscribed", color: "text-red-400/60", bg: "bg-red-400/5", clickable: true, getCount: (cs) => cs.filter((c) => c.status === "unsubscribed").length },
];

const CAMPAIGN_CARDS: CardDef[] = [
  { key: "pending",   label: "Pending",     color: "text-amber-400",  bg: "bg-amber-400/8",  clickable: true,  getCount: (cs, cid) => cntIn(cs, cid, (cd) => !cd?.inviteCount && (!cd?.status || cd?.status === "pending")) },
  { key: "invited",   label: "Invited",     color: "text-blue-400",   bg: "bg-blue-400/8",   clickable: false, getCount: (cs, cid) => cntIn(cs, cid, (cd) => (cd?.inviteCount ?? 0) > 0) },
  { key: "replied",   label: "Engaged",     color: "text-green-400",  bg: "bg-green-400/8",  clickable: true,  getCount: (cs, cid) => cntIn(cs, cid, (cd) => cd?.status === "replied" || cd?.replied === true || cd?.opened === true) },
  { key: "converted", label: "Converted",   color: "text-purple-400", bg: "bg-purple-400/8", clickable: true,  getCount: (cs, cid) => cntIn(cs, cid, (cd, c) => cd?.status === "converted" || (!cd?.status && c.status === "converted")) },
  { key: "noResponse", label: "No Response", color: "text-slate-500", bg: "bg-slate-500/8",  clickable: true,  getCount: (cs, cid) => cntIn(cs, cid, (cd) => cd?.status === "no_response" || cd?.stoppedReason === "max_invites_reached") },
  { key: "failed",    label: "Failed",      color: "text-red-400",    bg: "bg-red-400/8",    clickable: true,  getCount: (cs, cid) => cntIn(cs, cid, (cd) => cd?.status === "failed" || cd?.emailStatus === "failed") },
  { key: "unsubscribed", label: "Unsubscribed", color: "text-red-400/60", bg: "bg-red-400/5", clickable: true, getCount: (cs) => cs.filter((c) => c.status === "unsubscribed").length },
];

interface Props {
  contacts: OutreachContact[];
  campaigns?: { id: string; status: string }[];
  campaignId?: string;
  activeFilter?: string | null;
  onFilter?: (key: string | null) => void;
  headerRight?: React.ReactNode;
  search?: string;
  onSearchChange?: (s: string) => void;
}

export default function OutreachDashboardCards({ contacts, campaigns, campaignId, activeFilter, onFilter, headerRight, search, onSearchChange }: Props) {
  const isCampaign = !!campaignId;
  const cards = isCampaign ? CAMPAIGN_CARDS : GLOBAL_CARDS;
  const total = contacts.length;
  const campCounts = !isCampaign && campaigns
    ? {
        total: campaigns.length,
        active: campaigns.filter((c) => c.status === "running").length,
        paused: campaigns.filter((c) => c.status === "paused").length,
        upcoming: campaigns.filter((c) => c.status === "scheduled").length,
        ended: campaigns.filter((c) => ["completed", "cancelled", "failed"].includes(c.status)).length,
      }
    : null;

  const handleClick = (key: string, clickable: boolean) => {
    if (!clickable || !onFilter) return;
    onFilter(activeFilter === key ? null : key);
  };

  const renderCard = ({ key, label, color, bg, getCount, clickable }: CardDef) => {
    const count = getCount(contacts, campaignId);
    const isActive = activeFilter === key;

    let subLabels: { label: string; count: number; color: string }[] | null = null;
    if (key === "replied" && isCampaign) {
      const replied = cntIn(contacts, campaignId, (cd) => cd?.status === "replied" || cd?.replied === true);
      const opened = cntIn(contacts, campaignId, (cd) => cd?.opened === true);
      subLabels = [
        { label: "Replied", count: replied, color: "text-green-400" },
        { label: "Opened", count: opened, color: "text-cyan-400" },
      ];
    }

    return (
      <button
        key={key}
        type="button"
        disabled={!clickable}
        onClick={() => handleClick(key, clickable)}
        className={`rounded-lg border text-left p-2 transition-all ${
          isActive
            ? "border-cyan-400/40 bg-cyan-400/5"
            : clickable
              ? "border-white/10 bg-white/[0.02] hover:border-white/20 cursor-pointer"
              : "border-white/5 bg-white/[0.02] cursor-default"
        }`}
      >
        <p className={`font-mono text-[10px] tracking-[0.15em] uppercase mb-1.5 ${color}`}>
          {label}
        </p>
        <p className={`font-mono text-lg ${color}`}>{count}</p>
        {subLabels && (
          <div className="flex gap-2 mt-1">
            {subLabels.map((sl) => (
              <span key={sl.label} className={`font-mono text-[9px] ${sl.color}`}>
                {sl.label} {sl.count}
              </span>
            ))}
          </div>
        )}
      </button>
    );
  };

  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="rounded-lg border border-white/5 bg-white/[0.02] p-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mr-1">
              {isCampaign ? "Enrolled" : "Contacts"}
            </span>
            <span className="font-mono text-lg text-cyan-400">{total}</span>
          </div>
          {headerRight && (
            <div className="flex items-center gap-3">
              {headerRight}
            </div>
          )}
        </div>
      </div>

      {isCampaign ? (
        <div className="space-y-2">
          <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2">
            {cards.map(renderCard)}
          </div>
          <input
            type="text"
            value={search ?? ""}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search contacts..."
            className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
          />
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-[3fr_2fr] gap-4">
          <div className="grid grid-rows-3 gap-2">
            <div className="grid grid-cols-4 gap-2">
              {cards.slice(0, 4).map(renderCard)}
            </div>
            <div className="grid grid-cols-4 gap-2">
              {cards.slice(4).map(renderCard)}
            </div>
            <div>
              <input
                type="text"
                value={search ?? ""}
                onChange={(e) => onSearchChange?.(e.target.value)}
                placeholder="Search contacts..."
                className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
              />
            </div>
          </div>

          {campCounts && (
            <div className="rounded-lg border border-white/5 bg-white/[0.02] p-3 row-span-3 self-start w-full">
              <h4 className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
                Campaigns
              </h4>
              <div className="space-y-1.5">
                {[
                  { key: "active", label: "Active", color: "text-green-400" },
                  { key: "paused", label: "Paused", color: "text-amber-400" },
                  { key: "upcoming", label: "Upcoming", color: "text-blue-400" },
                  { key: "ended", label: "Ended", color: "text-slate-500" },
                ].map(({ key, label, color }) => (
                  <div key={key} className="flex items-center justify-between">
                    <span className="font-mono text-[11px] text-slate-400">{label}</span>
                    <span className={`font-mono text-sm ${color}`}>{campCounts[key as keyof typeof campCounts]}</span>
                  </div>
                ))}
              </div>
              <hr className="border-white/10 my-2" />
              <div className="flex items-center justify-between">
                <span className="font-mono text-[11px] font-bold text-white">Total Campaigns</span>
                <span className="font-mono text-sm font-bold text-white">{campCounts.total}</span>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
