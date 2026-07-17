"use client";

import type { RecruitmentSummary } from "@/types/recruitment";

interface Props {
  summary: RecruitmentSummary;
  activeFilter: string | null;
  onFilter: (status: string | null) => void;
}

const CARDS: {
  key: keyof RecruitmentSummary;
  label: string;
  color: string;
  borderColor: string;
}[] = [
  { key: "pending", label: "Pending", color: "text-amber-400", borderColor: "border-amber-400/20" },
  { key: "approved", label: "Approved", color: "text-green-400", borderColor: "border-green-400/20" },
  { key: "active", label: "Active", color: "text-cyan-400", borderColor: "border-cyan-400/20" },
  { key: "emailFailed", label: "Email Failed", color: "text-red-400", borderColor: "border-red-400/20" },
  { key: "rejected", label: "Rejected", color: "text-slate-400", borderColor: "border-slate-400/20" },
];

export default function RecruitmentSummaryCards({
  summary,
  activeFilter,
  onFilter,
}: Props) {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
      {CARDS.map(({ key, label, color, borderColor }) => {
        const isActive = activeFilter === key;
        return (
          <button
            key={key}
            type="button"
            onClick={() => onFilter(isActive ? null : key)}
            className={`rounded-lg border p-4 text-left transition-all cursor-pointer ${
              isActive
                ? `${borderColor} bg-white/[0.04]`
                : "border-white/5 bg-white/[0.02] hover:bg-white/[0.03]"
            }`}
          >
            <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-2">
              {label}
            </p>
            <p className={`font-mono text-lg ${color}`}>{summary[key]}</p>
          </button>
        );
      })}
    </div>
  );
}
