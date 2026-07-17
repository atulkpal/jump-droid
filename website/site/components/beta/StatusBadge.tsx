import type { RecruitmentStatus } from "@/types/recruitment";

const STYLES: Record<string, string> = {
  pending: "border-amber-400/20 text-amber-400 bg-amber-400/10",
  approved: "border-green-400/20 text-green-400 bg-green-400/10",
  invited: "border-blue-400/20 text-blue-400 bg-blue-400/10",
  active: "border-cyan-400/20 text-cyan-400 bg-cyan-400/10",
  inactive: "border-slate-500/20 text-slate-400 bg-slate-500/10",
  completed: "border-purple-400/20 text-purple-400 bg-purple-400/10",
  rejected: "border-red-400/20 text-red-400 bg-red-400/10",
};

export default function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={`inline-block rounded-full border px-2 py-0.5 font-mono text-[10px] uppercase tracking-wider leading-none ${
        STYLES[status] ?? "border-white/10 text-slate-400 bg-white/5"
      }`}
    >
      {status}
    </span>
  );
}
