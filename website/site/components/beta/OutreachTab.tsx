"use client";

import { useState, useEffect, useCallback, useMemo } from "react";
import type { OutreachContact } from "@/types/recruitmentContacts";
import { fetchAllContacts } from "@/lib/firebase/outreachService";
import { useAuth } from "./AuthContext";
import { getAuthUrl } from "@/lib/firebase/gmailService";
import { getAllowedAdminsConfig } from "@/lib/firebase/authService";
import OutreachDashboardCards from "./OutreachDashboardCards";
import OutreachContactsTable from "./OutreachContactsTable";
import StatusDetailModal from "./StatusDetailModal";
import OutreachImportCsv from "./OutreachImportCsv";
import AddManualContact from "./AddManualContact";
import ComposeEmailDialog from "./ComposeEmailDialog";
import SendNowConfirmDialog from "./SendNowConfirmDialog";

interface OutreachTabProps {
  onContactDeleted?: (email: string) => void;
  campaignId?: string;
}

function isAccountVisible(
  accountEmail: string,
  role: string | undefined,
  currentUserEmail: string | undefined,
  ownerSet: Set<string>,
  adminSet: Set<string>,
  defaultSenderEmail?: string
): boolean {
  if (accountEmail === defaultSenderEmail) return true;
  if (role === "owner") return true;
  if (role === "user") {
    if (!currentUserEmail) return false;
    return accountEmail === currentUserEmail;
  }
  if (role === "admin") {
    if (ownerSet.has(accountEmail)) return false;
    if (adminSet.has(accountEmail) && accountEmail !== currentUserEmail) return false;
    return true;
  }
  return true;
}

export default function OutreachTab({ onContactDeleted, campaignId }: OutreachTabProps) {
  const { user } = useAuth();
  const [contacts, setContacts] = useState<OutreachContact[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [importOpen, setImportOpen] = useState(false);
  const [manualOpen, setManualOpen] = useState(false);
  const [selectedCampaign, setSelectedCampaign] = useState<string>(campaignId || "__all_contacts__");
  const [selectedSendCampaign, setSelectedSendCampaign] = useState<string>("");
  const [campaigns, setCampaigns] = useState<{ id: string; name: string; status: string }[]>([]);
  const [sending, setSending] = useState(false);
  const [composeOpen, setComposeOpen] = useState(false);
  const [sendNowOpen, setSendNowOpen] = useState(false);
  const [result, setResult] = useState<{
    selected: number;
    sent: number;
    failed: number;
    errors: { email: string; error: string }[];
  } | null>(null);
  const [accounts, setAccounts] = useState<{ email: string; displayName?: string; status: string; isDefault?: boolean }[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<string>("");
  const [accountsLoading, setAccountsLoading] = useState(true);
  const [ownerSet, setOwnerSet] = useState<Set<string>>(new Set());
  const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const [adminSet, setAdminSet] = useState<Set<string>>(new Set());
  const [search, setSearch] = useState("");
  const [statusModalContact, setStatusModalContact] = useState<OutreachContact | null>(null);

  const loadContacts = useCallback(async () => {
    setLoading(true);
    try {
      let data: OutreachContact[];
      if (campaignId) {
        const { fetchContactsByCampaign } = await import("@/lib/firebase/outreachService");
        data = await fetchContactsByCampaign(campaignId);
      } else {
        data = await fetchAllContacts();
      }
      setContacts(data);
    } catch {
      // handled silently
    } finally {
      setLoading(false);
    }
  }, [campaignId]);

  useEffect(() => {
    loadContacts();
  }, [loadContacts]);

  useEffect(() => {
    fetch("/api/campaigns")
      .then((r) => r.json())
      .then((list) => setCampaigns(list))
      .catch(() => {});
  }, []);

  useEffect(() => {
    Promise.all([
      fetch("/api/email-accounts").then((res) => res.json()),
      getAllowedAdminsConfig().catch(() => null),
    ]).then(([data, allowedConfig]) => {
      const list: { email: string; displayName?: string; status: string; isDefault?: boolean }[] = data.accounts || [];
      setAccounts(list);
      const connected = list.filter((a: any) => a.status === "connected");
      const defaultConn = connected.find((a: any) => a.isDefault);
      setSelectedAccount(defaultConn?.email || connected[0]?.email || "");
      setAccountsLoading(false);

      if (allowedConfig) {
        setOwnerSet(new Set(allowedConfig.owners));
        setAdminSet(new Set(allowedConfig.emails));
      }
    })    .catch(() => setAccountsLoading(false));
  }, []);

  const handleImported = () => {
    setImportOpen(false);
    loadContacts();
  };

  const handleManualAdded = () => {
    setManualOpen(false);
    loadContacts();
  };

  const handleComposeSend = async (results: { email: string; success: boolean; error?: string }[]) => {
    const succeeded = results.filter((r) => r.success);
    const failed = results.filter((r) => !r.success);
    const selectedContacts = filteredContacts.filter((c) => selected.has(c.email));

    setResult({
      selected: selectedContacts.length,
      sent: succeeded.length,
      failed: failed.length,
      errors: failed.map((r) => ({ email: r.email, error: r.error || "Unknown error" })),
    });

    if (succeeded.length > 0) {
      await loadContacts();
      setSelected(new Set());
    }
  };

  const handleSend = async () => {
    const selectedContacts = filteredContacts.filter((c) => selected.has(c.email));
    if (selectedContacts.length === 0) return;

    const unsubscribedContacts = selectedContacts.filter((c) => c.status === "unsubscribed");
    const eligibleContacts = selectedContacts.filter((c) => c.status !== "unsubscribed");

    if (!selectedSendCampaign) {
      setComposeOpen(true);
      return;
    }

    setSending(true);
    setResult(null);

    try {
      const results: { email: string; success: boolean; error?: string }[] = [];
      for (const c of eligibleContacts) {
        try {
          const { addContactToCampaign } = await import("@/lib/firebase/campaignService");
          await addContactToCampaign(c.email, selectedSendCampaign, user?.email || "");
          results.push({ email: c.email, success: true });
        } catch (e: any) {
          results.push({ email: c.email, success: false, error: e?.message || "Enrollment failed" });
        }
      }
      for (const c of unsubscribedContacts) {
        results.push({ email: c.email, success: false, error: "Unsubscribed — skipped" });
      }

      const succeeded = results.filter((r) => r.success);
      const failed = results.filter((r) => !r.success);

      setResult({
        selected: selectedContacts.length,
        sent: succeeded.length,
        failed: failed.length,
        errors: failed.map((r) => ({ email: r.email, error: r.error || "Unknown error" })),
      });

      if (succeeded.length > 0) {
        await loadContacts();
        setSelected(new Set());
      }
    } catch {
      setResult({
        selected: selectedContacts.length,
        sent: 0,
        failed: selectedContacts.length,
        errors: [],
      });
    } finally {
      setSending(false);
    }
  };

  const renderSenderOptions = () => {
    const connected = accounts.filter((a) => a.status === "connected");
    const defaultSenderEmail = connected.find((a) => a.isDefault)?.email;
    const visibleAccounts = connected.filter((a) =>
      isAccountVisible(a.email, user?.role, user?.email, ownerSet, adminSet, defaultSenderEmail)
    );
    const userHasConnected = user?.email && connected.some((a) => a.email === user.email);
    if (visibleAccounts.length === 0 && user?.role !== "user") {
      return <option value="">No accounts</option>;
    }
    return (
      <>
        {visibleAccounts.map((a) => (
          <option key={a.email} value={a.email}>
            {a.displayName || a.email}{a.isDefault ? " (Default)" : ""}
          </option>
        ))}
        {user?.role === "user" && user.email && !userHasConnected && (
          <option value="__connect_own__">
            {"\u2192"} Send as {user.email} (Connect)
          </option>
        )}
        {user?.role === "user" && user.email && userHasConnected && (
          <option value={user.email}>
            {user.displayName || user.email} (Me)
          </option>
        )}
      </>
    );
  };

  const buildSender = (val: string) => {
    if (val === "__connect_own__") {
      const url = getAuthUrl(user?.email);
      if (url) {
        sessionStorage.setItem("pendingSend", JSON.stringify({}));
        window.location.href = url;
      }
      return;
    }
    setSelectedAccount(val);
  };

  const filteredContacts = useMemo(() => {
    let filtered = contacts;

    // Step 1: Apply status filter
    if (statusFilter) {
      if (!campaignId) {
        if (statusFilter === "pending") {
          filtered = filtered.filter((c) => c.status === "pending" && Object.values(c.campaignData ?? {}).every((cd) => !cd.inviteCount));
        } else if (statusFilter === "noResponse") {
          filtered = filtered.filter((c) => c.status === "no_response");
        } else if (statusFilter === "unsubscribed") {
          filtered = filtered.filter((c) => c.status === "unsubscribed");
        } else if (statusFilter === "replied") {
          filtered = filtered.filter((c) => Object.values(c.campaignData ?? {}).some((cd) => cd.status === "replied" || cd.replied === true));
        } else if (statusFilter === "failed") {
          filtered = filtered.filter((c) => Object.values(c.campaignData ?? {}).some((cd) => cd.status === "failed" || cd.emailStatus === "failed"));
        } else {
          filtered = filtered.filter((c) => c.status === statusFilter);
        }
      } else {
        const cdStatus = (c: typeof filtered[0]) => c.campaignData?.[campaignId]?.status;
        if (statusFilter === "pending") {
          filtered = filtered.filter((c) => !c.campaignData?.[campaignId]?.inviteCount && (!cdStatus(c) || cdStatus(c) === "pending"));
        } else if (statusFilter === "replied") {
          filtered = filtered.filter((c) => cdStatus(c) === "replied" || c.campaignData?.[campaignId]?.replied === true || c.campaignData?.[campaignId]?.opened === true);
        } else if (statusFilter === "converted") {
          filtered = filtered.filter((c) => cdStatus(c) === "converted" || (!cdStatus(c) && c.status === "converted"));
        } else if (statusFilter === "noResponse") {
          filtered = filtered.filter((c) => cdStatus(c) === "no_response" || c.campaignData?.[campaignId]?.stoppedReason === "max_invites_reached");
        } else if (statusFilter === "failed") {
          filtered = filtered.filter((c) => cdStatus(c) === "failed" || c.campaignData?.[campaignId]?.emailStatus === "failed");
        } else if (statusFilter === "unsubscribed") {
          filtered = filtered.filter((c) => c.status === "unsubscribed");
        }
      }
    }

    // Step 2: Apply campaign selector filter (global view only)
    if (!campaignId) {
      if (selectedCampaign === "__all__") {
        return filtered.filter((c) => c.campaigns?.length > 0);
      }
      if (selectedCampaign === "__all_contacts__") {
        return filtered;
      }
      if (selectedCampaign === "__unassigned__") {
        return filtered.filter((c) => !c.campaigns?.length && c.status !== "unsubscribed");
      }
      if (selectedCampaign === "__unsubscribed__") {
        return filtered.filter((c) => c.status === "unsubscribed");
      }
      return filtered.filter((c) => c.campaigns?.includes(selectedCampaign));
    }

    return filtered;
  }, [contacts, campaignId, selectedCampaign, statusFilter]);

  const handleRemoveFromCampaign = async () => {
    if (!campaignId) return;
    const selectedContacts = filteredContacts.filter((c) => selected.has(c.email));

    for (const c of selectedContacts) {
      try {
        const { removeContactFromCampaign } = await import("@/lib/firebase/campaignService");
        await removeContactFromCampaign(c.email, campaignId);
      } catch {
        // silently fail per-contact
      }
    }

    await loadContacts();
    setSelected(new Set());
  };

  const handleBulkUnsubscribe = async () => {
    const selectedEmails = filteredContacts.filter((c) => selected.has(c.email) && c.status !== "unsubscribed").map((c) => c.email);
    if (selectedEmails.length === 0) return;

    const { unsubscribeContact } = await import("@/lib/firebase/outreachService");
    for (const email of selectedEmails) {
      try {
        await unsubscribeContact(email);
      } catch {
        // silently fail per-contact
      }
    }

    await loadContacts();
    setSelected(new Set());
  };

  return (
    <div>
      {loading ? (
        <p className="font-mono text-xs text-slate-500">Loading contacts...</p>
      ) : (
        <>
          <section>
            <OutreachDashboardCards contacts={filteredContacts} campaigns={!campaignId ? campaigns : undefined} campaignId={campaignId} activeFilter={statusFilter} onFilter={setStatusFilter} search={search} onSearchChange={setSearch} headerRight={
              <div className="inline-flex items-center gap-3">
                {!campaignId && campaigns.length > 0 && (
                  <select
                    value={selectedCampaign}
                    onChange={(e) => { setSelectedCampaign(e.target.value); setStatusFilter(null); }}
                    className="rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40"
                  >
                    <option value="__all_contacts__">All Contacts</option>
                    <option value="__all__">All Campaigns</option>
                    <option value="__unassigned__">Unassigned</option>
                    <option disabled className="text-slate-600">{'\u2500'.repeat(16)}</option>
                    {campaigns.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                    <option disabled className="text-slate-600">{'\u2500'.repeat(16)}</option>
                    <option value="__unsubscribed__" className="text-red-400">Unsubscribed</option>
                  </select>
                )}
                <button
                  onClick={() => setManualOpen(true)}
                  className="rounded-lg border border-white/10 px-4 py-2 font-mono text-xs tracking-[0.15em] text-slate-300 transition-colors hover:border-white/20 hover:text-white"
                >
                  + Manual Entry
                </button>
                <button
                  onClick={() => setImportOpen(true)}
                  className="rounded-lg border border-cyan-400/30 px-4 py-2 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50"
                >
                  Import CSV
                </button>
              </div>
            } />
          </section>

          <section className="mb-3">
            <OutreachContactsTable
              contacts={filteredContacts}
              selected={selected}
              onSelectionChange={setSelected}
              onContactDeleted={onContactDeleted}
              onRefreshContacts={loadContacts}
              search={search}
              onSearchChange={setSearch}
              campaignId={campaignId}
              onStatusClick={setStatusModalContact}
            />
          </section>

          {selected.size > 0 && (
            campaignId ? (
              <section className="mb-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <p className="font-mono text-xs text-slate-400 shrink-0 whitespace-nowrap">
                      {selected.size} contact{selected.size !== 1 ? "s" : ""} selected
                    </p>
                    <button
                      onClick={handleRemoveFromCampaign}
                      className="rounded-lg border border-red-400/30 px-3 py-1.5 font-mono text-[11px] text-red-300 transition-colors hover:bg-red-400/10 whitespace-nowrap"
                    >
                      Remove from Campaign
                    </button>
                  </div>
                  <div className="flex items-center gap-2">
                    <select
                      value={selectedSendCampaign}
                      onChange={(e) => setSelectedSendCampaign(e.target.value)}
                      className="rounded-lg border border-white/10 bg-black px-2 py-1.5 font-mono text-[11px] text-white outline-none transition focus:border-cyan-400/40"
                    >
                      <option value="">Add to Campaign...</option>
                      {campaigns.map((c) =>
                        c.id === campaignId ? (
                          <option key={c.id} value={c.id} disabled className="text-slate-500">
                            {c.name} (current)
                          </option>
                        ) : (
                          <option key={c.id} value={c.id}>{c.name}</option>
                        )
                      )}
                    </select>
                    <button
                      onClick={handleSend}
                      disabled={!selectedSendCampaign || sending}
                      className="rounded-lg border border-cyan-400/30 px-3 py-1.5 font-mono text-[11px] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30 disabled:cursor-not-allowed whitespace-nowrap"
                    >
                      {sending ? "..." : "Add to Campaign"}
                    </button>
                    <button
                      onClick={() => setSendNowOpen(true)}
                      className="rounded-lg border border-red-400/30 px-3 py-1.5 font-mono text-[11px] text-red-300 transition-colors hover:bg-red-400/10 whitespace-nowrap"
                    >
                      Send Next Mail Now
                    </button>
                  </div>
                </div>
              </section>
            ) : (
              <section className="mb-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <p className="font-mono text-xs text-slate-400 shrink-0 whitespace-nowrap">
                      {selected.size} contact{selected.size !== 1 ? "s" : ""} selected
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <select
                      value={selectedSendCampaign}
                      onChange={(e) => setSelectedSendCampaign(e.target.value)}
                      className="rounded-lg border border-white/10 bg-black px-2 py-1.5 font-mono text-[11px] text-white outline-none transition focus:border-cyan-400/40"
                    >
                      <option value="">Campaign</option>
                      {campaigns.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                    <button
                      onClick={handleBulkUnsubscribe}
                      className="rounded-lg border border-red-400/30 px-3 py-1.5 font-mono text-[11px] text-red-300 transition-colors hover:bg-red-400/10 whitespace-nowrap"
                    >
                      Unsubscribe
                    </button>
                    <select
                      value={selectedAccount}
                      onChange={(e) => buildSender(e.target.value)}
                      className="rounded-lg border border-white/10 bg-black px-2 py-1.5 font-mono text-[11px] text-white outline-none transition focus:border-cyan-400/40 max-w-[160px]"
                    >
                      {renderSenderOptions()}
                    </select>
                    <button
                      onClick={handleSend}
                      disabled={sending}
                      className="rounded-lg border border-cyan-400/30 px-4 py-1.5 font-mono text-xs tracking-[0.1em] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30 disabled:cursor-not-allowed whitespace-nowrap w-[180px] flex-none shrink-0 text-center"
                    >
                      {sending ? "..." : selectedSendCampaign ? "Add to Campaign" : "Send Email"}
                    </button>
                    <a
                      href="/beta-dashboard/settings/email-accounts"
                      className="font-mono text-[10px] text-slate-500 hover:text-white transition-colors whitespace-nowrap"
                    >
                      Manage
                    </a>
                  </div>
                </div>
              </section>
            )
          )}

          {result && (
            <section className="mb-3">
              <div className="rounded-lg border border-white/5 bg-white/[0.02] p-4">
                <h3 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-3">
                  Results
                </h3>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <p className="font-mono text-[10px] text-slate-500 uppercase">Selected</p>
                    <p className="font-mono text-sm text-white">{result.selected}</p>
                  </div>
                  <div>
                    <p className="font-mono text-[10px] text-slate-500 uppercase">Sent</p>
                    <p className="font-mono text-sm text-green-400">{result.sent}</p>
                  </div>
                  <div>
                    <p className="font-mono text-[10px] text-slate-500 uppercase">Failed</p>
                    <p className="font-mono text-sm text-red-400">{result.failed}</p>
                  </div>
                </div>
                {result.sent + result.failed > 0 && (
                  <p className="font-mono text-[11px] text-slate-400 mt-2">
                    Success rate:{" "}
                    {((result.sent / (result.sent + result.failed)) * 100).toFixed(0)}%
                  </p>
                )}
                {result.errors.length > 0 && (
                  <div className="mt-3 border-t border-red-400/10 pt-3">
                    <p className="font-mono text-[10px] text-red-400/60 uppercase mb-2">
                      Failure Details
                    </p>
                    <div className="max-h-32 overflow-y-auto space-y-1">
                      {result.errors.map((e, i) => (
                        <p key={i} className="font-mono text-[11px] text-red-400/80">
                          {e.email && (
                            <span className="text-red-300">{e.email}</span>
                          )}
                          {e.email && e.error && <span>: </span>}
                          {e.error && <span>{e.error}</span>}
                        </p>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </section>
          )}
        </>
      )}

      {manualOpen && (
        <AddManualContact onAdded={handleManualAdded} onClose={() => setManualOpen(false)} importedBy={user?.email || ""} campaignId={campaignId} />
      )}
      {importOpen && (
        <OutreachImportCsv onImported={handleImported} onClose={() => setImportOpen(false)} importedBy={user?.email || ""} campaignId={campaignId} />
      )}

      {composeOpen && (
        <ComposeEmailDialog
          recipients={filteredContacts.filter((c) => selected.has(c.email) && c.status !== "unsubscribed").map((c) => ({ email: c.email, name: c.name || c.email }))}
          senderAccountId={selectedAccount}
          onClose={() => setComposeOpen(false)}
          onSent={handleComposeSend}
        />
      )}

      {sendNowOpen && campaignId && (
        <SendNowConfirmDialog
          campaignId={campaignId}
          campaignName={campaigns.find((c) => c.id === campaignId)?.name || "Unknown"}
          contacts={filteredContacts.filter((c) => selected.has(c.email) && c.status !== "unsubscribed").map((c) => ({ email: c.email, name: c.name || c.email }))}
          onClose={() => setSendNowOpen(false)}
          onComplete={() => { loadContacts(); setSelected(new Set()); }}
        />
      )}

      {statusModalContact && (
        <StatusDetailModal
          contact={statusModalContact}
          campaignId={campaignId}
          onClose={() => setStatusModalContact(null)}
        />
      )}
    </div>
  );
}
