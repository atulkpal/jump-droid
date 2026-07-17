"use client";

import { useState, useEffect } from "react";
import type { Tester } from "@/types/tester";
import { fetchAllTesters } from "@/lib/firebase/testers";

interface Props {
  onSelect: (tester: Tester) => void;
  selectedEmail: string | null;
}

export default function TesterSelector({ onSelect, selectedEmail }: Props) {
  const [testers, setTesters] = useState<Tester[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAllTesters()
      .then((data) => {
        setTesters(data);
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load testers");
        setLoading(false);
      });
  }, []);

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <label
        htmlFor="tester-select"
        className="font-mono text-[10px] tracking-[0.25em] text-cyan-400/60 uppercase mb-3 block"
      >
        Select Tester Profile
      </label>
      {error ? (
        <p className="font-mono text-xs text-red-400">{error}</p>
      ) : (
        <select
          id="tester-select"
          value={selectedEmail ?? ""}
          onChange={(e) => {
            const tester = testers.find((t) => t.email === e.target.value);
            if (tester) onSelect(tester);
          }}
          className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
        >
          <option value="" disabled>
            {loading ? "Loading testers..." : testers.length === 0 ? "No testers found" : "Choose your profile"}
          </option>
          {testers.map((t) => (
            <option key={t.email} value={t.email} className="bg-black text-white">
              {t.name} ({t.email})
            </option>
          ))}
        </select>
      )}
    </div>
  );
}
