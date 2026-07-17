"use client";

import Link from "next/link";
import { PLAY_STORE_URL } from "@/lib/constants";
import { HERO } from "@/app/data/site-content";

export default function PlayStoreModal({ onClose }: { onClose: () => void }) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div className="fixed inset-0 bg-black/70" />
      <div
        className="relative w-full max-w-md rounded-2xl border border-white/10 bg-[#0a0a0f] p-8 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="font-mono text-sm font-bold tracking-[0.1em] text-white uppercase mb-6">
          {HERO.modalTitle}
        </h2>

        <p className="font-mono text-xs text-slate-400 mb-4">
          Become a Beta Tester and enjoy:
        </p>

        <ul className="space-y-3 mb-8">
          {HERO.modalBody.map((item, i) => (
            <li key={i} className="font-mono text-xs text-slate-300 leading-relaxed">
              {item}
            </li>
          ))}
        </ul>

        <div className="flex flex-col gap-3">
          <Link
            href="/beta-info"
            onClick={onClose}
            className="rounded-full border border-cyan-400/40 bg-cyan-400/10 px-6 py-3 font-mono text-xs tracking-[0.2em] text-cyan-300 uppercase text-center transition-all hover:bg-cyan-400/20 hover:border-cyan-400/60 hover:shadow-[0_0_28px_rgba(0,229,255,0.2)]"
          >
            {HERO.ctaBeta}
          </Link>
          <button
            onClick={onClose}
            className="rounded-full border border-white/10 px-6 py-3 font-mono text-xs tracking-[0.2em] text-slate-400 uppercase transition-colors hover:border-white/20 hover:text-white"
          >
            Maybe Later
          </button>
        </div>

        <div className="mt-6 text-center">
          <a
            href={PLAY_STORE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="font-mono text-[10px] tracking-[0.15em] text-slate-600 underline underline-offset-2 hover:text-slate-400 transition-colors"
          >
            Continue to Google Play &rarr;
          </a>
        </div>
      </div>
    </div>
  );
}
