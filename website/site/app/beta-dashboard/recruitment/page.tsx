"use client";

import { Suspense, useState, useEffect, useCallback, useMemo } from "react";
import { useSearchParams } from "next/navigation";
import type { RecruitmentApplicant, RecruitmentStatus } from "@/types/recruitment";
import { fetchAllApplicants } from "@/lib/firebase/recruitmentService";
import RecruitmentSummaryCards from "@/components/beta/RecruitmentSummaryCards";
import RecruitmentTable from "@/components/beta/RecruitmentTable";
import RecruitmentSidePanel from "@/components/beta/RecruitmentSidePanel";
import OutreachTab from "@/components/beta/OutreachTab";

function pct(part: number, total: number): string {
  if (!total) return "\u2014";
  return `${((part / total) * 100).toFixed(1)}%`;
}

const TABS = [
  { id: "applicants", label: "Applicants" },
  { id: "outreach", label: "Contacts" },
];

export default function RecruitmentPageWrapper() {
  return (
    <Suspense fallback={<p className="font-mono text-xs text-slate-500">Loading...</p>}>
      <RecruitmentPage />
    </Suspense>
  );
}

function RecruitmentPage() {
  const searchParams = useSearchParams();
  const [tab, setTab] = useState<string>("applicants");

  useEffect(() => {
    const t = searchParams.get("tab");
    if (t === "outreach" || t === "applicants") setTab(t);
  }, [searchParams]);

  const [applicants, setApplicants] = useState<RecruitmentApplicant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<RecruitmentApplicant | null>(null);
  const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const [sourceFilter, setSourceFilter] = useState<string | null>(null);
  const [emailStatusFilter, setEmailStatusFilter] = useState<string | null>(null);

  const loadApplicants = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAllApplicants();
      setApplicants(data);
    } catch (e: any) {
      setError(e?.message ?? "Failed to load applicants");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadApplicants();
  }, [loadApplicants]);

  const handleStatusChanged = useCallback(
    (email: string, status: RecruitmentStatus) => {
      setApplicants((prev) =>
        prev.map((a) => (a.email === email ? { ...a, status } : a))
      );
      setSelected((prev) =>
        prev?.email === email ? { ...prev, status } : prev
      );
    },
    []
  );

  const handleDeleted = useCallback(
    (email: string) => {
      setApplicants((prev) => prev.filter((a) => a.email !== email));
      setSelected((prev) => (prev?.email === email ? null : prev));
    },
    []
  );

  const handleNotesSaved = useCallback(
    (email: string, notes: string) => {
      setApplicants((prev) =>
        prev.map((a) => (a.email === email ? { ...a, notes } : a))
      );
      setSelected((prev) =>
        prev?.email === email ? { ...prev, notes } : prev
      );
    },
    []
  );

  const filtered = useMemo(() => {
    return applicants.filter((a) => {
      if (statusFilter === "emailFailed") {
        if (a.emailStatus !== "failed") return false;
      } else if (statusFilter) {
        if (a.status !== statusFilter) return false;
      }
      if (sourceFilter && a.source !== sourceFilter) return false;
      if (emailStatusFilter && a.emailStatus !== emailStatusFilter) return false;
      return true;
    });
  }, [applicants, statusFilter, sourceFilter, emailStatusFilter]);

  const total = applicants.length;
  const approved = applicants.filter((a) => a.status === "approved").length;
  const active = applicants.filter((a) => a.status === "active").length;

  const summary = {
    pending: applicants.filter((a) => a.status === "pending").length,
    approved,
    invited: applicants.filter((a) => a.status === "invited").length,
    active,
    rejected: applicants.filter((a) => a.status === "rejected").length,
    emailFailed: applicants.filter((a) => a.emailStatus === "failed").length,
  };

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-7xl px-6 py-16 sm:px-8 sm:py-20">
        <div className="mb-12">
          <div className="space-y-4">
            <p className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase">
              Admin Terminal
            </p>
            <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase leading-snug">
              Recruitment
              <br />
              <span className="text-cyan-300">Dashboard</span>
            </h1>
          </div>
        </div>

        <div className="mb-8 flex gap-6 border-b border-white/5">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              onClick={() => setTab(t.id)}
              className={`pb-3 font-mono text-xs tracking-[0.15em] uppercase transition-colors border-b-2 -mb-px ${
                tab === t.id
                  ? "text-cyan-300 border-cyan-400/40"
                  : "text-slate-500 border-transparent hover:text-white"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {tab === "applicants" ? (
          <>
            {loading ? (
              <p className="font-mono text-xs text-slate-500">Loading applicants...</p>
            ) : error ? (
              <p className="font-mono text-xs text-red-400">{error}</p>
            ) : (
              <>
                <section className="mb-8">
                  <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                    Summary
                  </h2>
                  <div className="grid gap-4 sm:grid-cols-[1fr_auto]">
                    <RecruitmentSummaryCards
                      summary={summary}
                      activeFilter={statusFilter}
                      onFilter={setStatusFilter}
                    />
                    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-4 sm:w-56">
                      <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
                        Conversion
                      </p>
                      <div className="space-y-2">
                        <div className="flex justify-between items-center">
                          <span className="font-mono text-[11px] text-slate-400">
                            Registered
                          </span>
                          <span className="font-mono text-xs text-white">{total}</span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="font-mono text-[11px] text-slate-400">
                            &rarr; Approved
                          </span>
                          <span className="font-mono text-xs text-green-400">
                            {approved} ({pct(approved, total)})
                          </span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="font-mono text-[11px] text-slate-400">
                            &rarr; Active
                          </span>
                          <span className="font-mono text-xs text-cyan-400">
                            {active} ({pct(active, total)})
                          </span>
                        </div>
                        <div className="border-t border-white/5 pt-2 mt-2">
                          <div className="flex justify-between items-center">
                            <span className="font-mono text-[11px] text-slate-400">
                              Approval &rarr; Active
                            </span>
                            <span className="font-mono text-xs text-cyan-100">
                              {pct(active, approved)}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </section>

                <section className="mb-8">
                  <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
                    Applicants
                  </h2>

                  <div className="mb-4 flex flex-wrap items-center gap-3">
                    <select
                      value={sourceFilter ?? ""}
                      onChange={(e) => setSourceFilter(e.target.value || null)}
                      className="rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-slate-300 outline-none transition focus:border-cyan-400/40"
                    >
                      <option value="">All Sources</option>
                      {[
                        ...new Set(applicants.map((a) => a.source).filter(Boolean)),
                      ].sort().map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>

                    <select
                      value={emailStatusFilter ?? ""}
                      onChange={(e) => setEmailStatusFilter(e.target.value || null)}
                      className="rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-slate-300 outline-none transition focus:border-cyan-400/40"
                    >
                      <option value="">All Email Status</option>
                      <option value="pending">Pending</option>
                      <option value="sent">Sent</option>
                      <option value="failed">Failed</option>
                    </select>

                    {(statusFilter || sourceFilter || emailStatusFilter) && (
                      <button
                        type="button"
                        onClick={() => {
                          setStatusFilter(null);
                          setSourceFilter(null);
                          setEmailStatusFilter(null);
                        }}
                        className="rounded-lg border border-white/10 px-3 py-2 font-mono text-[10px] tracking-[0.1em] text-slate-400 transition-colors hover:border-white/20 hover:text-white uppercase"
                      >
                        Clear filters &#x2715;
                      </button>
                    )}
                  </div>

                  <RecruitmentTable
                    applicants={filtered}
                    onSelect={setSelected}
                  />
                </section>
              </>
            )}

            {selected && (
              <RecruitmentSidePanel
                applicant={selected}
                onClose={() => setSelected(null)}
                onStatusChanged={handleStatusChanged}
                onNotesSaved={handleNotesSaved}
                onDeleted={handleDeleted}
              />
            )}
          </>
        ) : (
          <OutreachTab />
        )}
      </main>
    </div>
  );
}
