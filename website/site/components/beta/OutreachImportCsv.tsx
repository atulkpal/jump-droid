"use client";

import { useState, useRef } from "react";
import type { CsvRow } from "@/types/recruitmentContacts";
import { importContacts } from "@/lib/firebase/outreachService";

interface Props {
  onImported: () => void;
  onClose: () => void;
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function parseCsv(text: string): CsvRow[] {
  const lines = text.split("\n").map((l) => l.trim()).filter(Boolean);
  const rows: CsvRow[] = [];

  for (const line of lines) {
    const parts = line.split(",").map((p) => p.trim().replace(/^"|"$/g, ""));
    if (parts.length < 1) continue;
    const email = (parts[parts.length - 1] || "").toLowerCase().trim();
    if (!EMAIL_RE.test(email)) continue;
    let name = "";
    let phone = "";
    if (parts.length === 2) {
      name = parts[0];
    } else if (parts.length >= 3) {
      phone = parts[parts.length - 2];
      name = parts.slice(0, -2).join(" ");
    }
    rows.push({ name, email, phone });
  }
  return rows;
}

export default function OutreachImportCsv({ onImported, onClose }: Props) {
  const [preview, setPreview] = useState<CsvRow[] | null>(null);
  const [importing, setImporting] = useState(false);
  const [done, setDone] = useState(false);
  const [count, setCount] = useState(0);
  const fileRef = useRef<HTMLInputElement>(null);

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      const text = ev.target?.result as string;
      const rows = parseCsv(text);
      setPreview(rows);
    };
    reader.readAsText(file);
  };

  const handleImport = async () => {
    if (!preview || preview.length === 0) return;
    setImporting(true);
    try {
      const imported = await importContacts(preview);
      setCount(imported);
      setDone(true);
      onImported();
    } catch {
      // handled silently
    } finally {
      setImporting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center sm:items-center">
      <div className="fixed inset-0 bg-black/70" onClick={onClose} />
      <div className="relative mt-16 sm:mt-0 sm:w-[500px] w-full mx-4 rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Import CSV
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        {done ? (
          <div className="text-center space-y-4 py-6">
            <p className="font-mono text-sm text-green-400">
              {count} contact{count !== 1 ? "s" : ""} imported successfully.
            </p>
            <p className="font-mono text-xs text-slate-400">
              Duplicate emails were skipped.
            </p>
            <button
              onClick={onClose}
              className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50"
            >
              Done
            </button>
          </div>
        ) : !preview ? (
          <div className="space-y-4">
            <p className="font-mono text-xs text-slate-400">
               Upload a CSV file with columns: <code className="text-cyan-300">Name,Email</code> or <code className="text-cyan-300">Name,Phone,Email</code>
            </p>
            <label className="block cursor-pointer rounded-lg border border-dashed border-white/10 p-8 text-center hover:border-white/20 transition-colors">
              <input
                ref={fileRef}
                type="file"
                accept=".csv"
                onChange={handleFile}
                className="hidden"
              />
              <p className="font-mono text-xs text-slate-500">
                Click to select a CSV file
              </p>
            </label>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="font-mono text-xs text-slate-400">
              {preview.length} valid row{preview.length !== 1 ? "s" : ""} found.
            </p>
            <div className="max-h-48 overflow-y-auto rounded-lg border border-white/5 bg-black/50">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-white/5">
                    <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-3 py-2">Name</th>
                    <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-3 py-2">Phone</th>
                    <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-3 py-2">Email</th>
                  </tr>
                </thead>
                <tbody>
                  {preview.slice(0, 50).map((row, i) => (
                    <tr key={i} className="border-b border-white/5 last:border-0">
                      <td className="px-3 py-2 font-mono text-xs text-slate-300">{row.name || "—"}</td>
                      <td className="px-3 py-2 font-mono text-xs text-slate-400">{row.phone || "—"}</td>
                      <td className="px-3 py-2 font-mono text-xs text-slate-400">{row.email}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {preview.length > 50 && (
                <p className="p-3 font-mono text-[10px] text-slate-600 text-center">
                  +{preview.length - 50} more rows
                </p>
              )}
            </div>
            <div className="flex gap-3">
              <button
                onClick={handleImport}
                disabled={importing}
                className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
              >
                {importing ? "Importing..." : `Import ${preview.length} Contact${preview.length !== 1 ? "s" : ""}`}
              </button>
              <button
                onClick={() => setPreview(null)}
                className="rounded-lg border border-white/10 px-6 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white"
              >
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
