"use client";

import { useState, useMemo } from "react";
import type { OutreachContact } from "@/types/recruitmentContacts";

interface Props {
  contacts: OutreachContact[];
  selected: Set<string>;
  onSelectionChange: (selected: Set<string>) => void;
}

const STATUS_STYLES: Record<string, string> = {
  pending: "text-amber-400",
  invited: "text-blue-400",
  registered: "text-green-400",
  failed: "text-red-400",
  converted: "text-purple-400",
  no_response: "text-slate-500",
};

function formatDate(ts: { seconds: number } | null): string {
  if (!ts?.seconds) return "—";
  return new Date(ts.seconds * 1000).toLocaleDateString();
}

export default function OutreachContactsTable({
  contacts,
  selected,
  onSelectionChange,
}: Props) {
  const [search, setSearch] = useState("");

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

  return (
    <div>
      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search contacts..."
        className="mb-4 w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
      />

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
                Phone
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Status
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Invites
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Next
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Campaign
              </th>
              <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
                Imported
              </th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={9} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                  {search.trim() ? "No contacts match your search." : "No contacts yet. Import a CSV to get started."}
                </td>
              </tr>
            ) : (
              filtered.map((c) => (
                <tr
                  key={c.email}
                  className="border-b border-white/5 hover:bg-white/[0.01] cursor-pointer"
                  onClick={() => toggleOne(c.email)}
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
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">
                    {c.phone || "\u2014"}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`font-mono text-xs ${
                        STATUS_STYLES[c.status] ?? "text-slate-400"
                      }`}
                    >
                      {c.status}
                    </span>
                  </td>
        <td className="px-4 py-3 font-mono text-xs text-slate-400">
                        {c.inviteCount || 0}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-slate-400">
                        {formatDate(c.nextEligibleAt)}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-slate-400">
                        {c.campaignId || "\u2014"}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-slate-400">
                        {formatDate(c.importedAt)}
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
