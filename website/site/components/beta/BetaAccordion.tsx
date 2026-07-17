"use client";

import { useState } from "react";

interface Section {
  id: string;
  title: string;
  content: React.ReactNode;
}

const SECTIONS: Section[] = [
  {
    id: "why-join",
    title: "Why Join?",
    content: (
      <ul className="space-y-2">
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Your name <strong className="text-slate-300">permanently listed</strong> in the Jump Droid game credits as a Beta Tester.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Get early access to Jump Droid before the public release.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Influence development with your feedback.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Direct line of communication with the developer.</span>
        </li>
      </ul>
    ),
  },
  {
    id: "rewards",
    title: "Rewards",
    content: (
      <ul className="space-y-2">
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>30 minutes of gameplay per day qualifies you for participation rewards.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Rewards may include cash, Amazon/Flipkart vouchers (India only), or equivalent.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Top 3 contributors at the end of the beta receive additional rewards.</span>
        </li>
      </ul>
    ),
  },
  {
    id: "expectations",
    title: "What We Expect",
    content: (
      <ul className="space-y-2">
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Play when you can.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Report bugs.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Suggest improvements.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Tell us what you enjoyed.</span>
        </li>
      </ul>
    ),
  },
  {
    id: "code-jam",
    title: "Code Jam",
    content: (
      <ul className="space-y-2">
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Review code and fix bugs in the Jump Droid codebase.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Learn Android and game development hands-on.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Build an open-source portfolio.</span>
        </li>
        <li className="font-mono text-[11px] text-slate-400 flex gap-3">
          <span className="text-cyan-400 shrink-0">•</span>
          <span>Work directly with the developer on the game.</span>
        </li>
      </ul>
    ),
  },
];

export default function BetaAccordion() {
  const [open, setOpen] = useState<Set<string>>(new Set());

  const toggle = (id: string) => {
    setOpen((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  return (
    <div className="mb-6 space-y-3">
      {SECTIONS.map((s) => {
        const isOpen = open.has(s.id);
        return (
          <div
            key={s.id}
            className="rounded-lg border border-white/5 bg-white/[0.02] overflow-hidden"
          >
            <button
              type="button"
              onClick={() => toggle(s.id)}
              className="w-full flex items-center justify-between p-5 text-left transition-colors hover:bg-white/[0.01]"
            >
              <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
                {s.title}
              </h2>
              <span
                className={`font-mono text-sm text-cyan-400/60 transition-transform ${
                  isOpen ? "rotate-45" : ""
                }`}
              >
                +
              </span>
            </button>
            {isOpen && (
              <div className="px-5 pb-5 border-t border-white/5 pt-4">
                {s.content}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
