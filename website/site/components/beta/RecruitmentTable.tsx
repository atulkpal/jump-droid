"use client";

import { useState, useMemo } from "react";
import type { RecruitmentApplicant } from "@/types/recruitment";
import StatusBadge from "./StatusBadge";

interface Props {
  applicants: RecruitmentApplicant[];
  onSelect: (applicant: RecruitmentApplicant) => void;
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

export default function RecruitmentTable({ applicants, onSelect }: Props) {
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    if (!search.trim()) return applicants;
    const q = search.toLowerCase().trim();
    return applicants.filter(
      (a) =>
        a.email.toLowerCase().includes(q) ||
        a.name.toLowerCase().includes(q) ||
        a.phone.includes(q)
    );
  }, [applicants, search]);

  return (
    <div>
      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search by name, email, or phone..."
        className="mb-4 w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
      />

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
                Code Jam
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Source
              </th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td
                  colSpan={7}
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
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {a.codeJam ? "Yes" : "No"}
                  </td>
                  <td className="px-4 py-3">
                    <SourceBadge source={a.source} />
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
