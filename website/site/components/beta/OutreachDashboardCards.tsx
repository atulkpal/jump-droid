import type { OutreachContact } from "@/types/recruitmentContacts";

interface Props {
  contacts: OutreachContact[];
}

const CARDS: { key: string; label: string; color: string }[] = [
  { key: "total", label: "Contacts", color: "text-white" },
  { key: "pending", label: "Pending", color: "text-amber-400" },
  { key: "invited", label: "Invited", color: "text-blue-400" },
  { key: "registered", label: "Registered", color: "text-green-400" },
  { key: "converted", label: "Converted", color: "text-purple-400" },
  { key: "noResponse", label: "No Response", color: "text-slate-500" },
];

export default function OutreachDashboardCards({ contacts }: Props) {
  const counts = {
    total: contacts.length,
    pending: contacts.filter((c) => c.status === "pending").length,
    invited: contacts.filter((c) => c.status === "invited").length,
    registered: contacts.filter((c) => c.status === "registered").length,
    converted: contacts.filter((c) => c.status === "converted").length,
    noResponse: contacts.filter((c) => c.status === "no_response").length,
  };

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
      {CARDS.map(({ key, label, color }) => (
        <div
          key={key}
          className="rounded-lg border border-white/5 bg-white/[0.02] p-4"
        >
          <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-2">
            {label}
          </p>
          <p className={`font-mono text-lg ${color}`}>{counts[key as keyof typeof counts]}</p>
        </div>
      ))}
    </div>
  );
}
