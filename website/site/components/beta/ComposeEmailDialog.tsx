"use client";

import { useState, useEffect, useCallback } from "react";
import type { TemplateWithSource } from "@/types/emailTemplates";

interface Props {
  recipients: { email: string; name: string }[];
  senderAccountId: string;
  onClose: () => void;
  onSent: (results: { email: string; success: boolean; error?: string }[]) => void;
}

const CUSTOM = "__custom__";

export default function ComposeEmailDialog({ recipients, senderAccountId, onClose, onSent }: Props) {
  const [allTemplates, setAllTemplates] = useState<TemplateWithSource[]>([]);
  const [template, setTemplate] = useState("");
  const [subject, setSubject] = useState("");
  const [htmlBody, setHtmlBody] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const { loadAllTemplates } = await import("@/lib/firebase/templateService");
        const loaded = await loadAllTemplates();
        setAllTemplates(loaded);
        if (loaded.length > 0) setTemplate(loaded[0].templateKey);
      } catch {}
    })();
  }, []);

  const loadFromHardcoded = useCallback(async (key: string) => {
    try {
      const { renderTemplate } = await import("@/lib/emailTemplates");
      const rendered = renderTemplate(key as any, recipients[0]?.name || "User");
      setSubject(rendered.subject);
      setHtmlBody(rendered.html);
    } catch {
      setSubject("");
      setHtmlBody("");
    }
  }, [recipients]);

  useEffect(() => {
    if (!template || template === CUSTOM) return;
    const tmpl = allTemplates.find((t) => t.templateKey === template);
    if (!tmpl) return;

    if (tmpl.isCustom) {
      setSubject(tmpl.subject);
      setHtmlBody(tmpl.htmlBody);
    } else if (tmpl.htmlBody) {
      setSubject(tmpl.subject);
      setHtmlBody(tmpl.htmlBody);
    } else {
      loadFromHardcoded(template);
    }
  }, [template, allTemplates, loadFromHardcoded]);

  const handleSend = async () => {
    if (template === CUSTOM) {
      if (!subject.trim() && !htmlBody.trim()) {
        setError("Subject and content cannot both be empty.");
        return;
      }
    }

    setSending(true);
    setError("");

    try {
      const res = await fetch("/api/gmail/send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          emails: recipients,
          template: "outreach-1",
          source: "outreach",
          senderAccountId: senderAccountId || undefined,
          subject: subject.trim() || undefined,
          htmlBody: htmlBody.trim() || undefined,
        }),
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({ error: `HTTP ${res.status}` }));
        setError(errData.error || `HTTP ${res.status}`);
        setSending(false);
        return;
      }

      const results = await res.json();
      onSent(Array.isArray(results) ? results : []);
      onClose();
    } catch (e: any) {
      setError(e?.message || "Network error");
    } finally {
      setSending(false);
    }
  };

  const isCustom = template === CUSTOM;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center" onClick={onClose}>
      <div className="fixed inset-0 bg-black/70" />
      <div
        className="relative w-full max-w-2xl mx-4 max-h-[85vh] overflow-y-auto rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Compose Email
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        <p className="font-mono text-[11px] text-slate-400 mb-5">
          Sending to {recipients.length} recipient{recipients.length !== 1 ? "s" : ""}
        </p>

        <div className="space-y-4">
          <div>
            <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1.5">
              Template
            </label>
            <select
              value={template}
              onChange={(e) => {
                setTemplate(e.target.value);
                setError("");
              }}
              className="w-full rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40"
            >
              {allTemplates.map((t) => (
                <option key={t.templateKey} value={t.templateKey}>{t.name}</option>
              ))}
              <option disabled className="text-slate-600">{'\u2500'.repeat(16)}</option>
              <option value={CUSTOM}>Custom Mail</option>
            </select>
          </div>

          {isCustom ? (
            <>
              <div>
                <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1.5">
                  Subject
                </label>
                <input
                  type="text"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  className="w-full rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40"
                />
              </div>
              <div>
                <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1.5">
                  HTML Body
                </label>
                <textarea
                  value={htmlBody}
                  onChange={(e) => setHtmlBody(e.target.value)}
                  rows={12}
                  className="w-full rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40 resize-y"
                />
              </div>
            </>
          ) : (
            <>
              <div>
                <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1.5">
                  Subject
                </label>
                <p className="w-full rounded-lg border border-white/5 bg-white/[0.02] px-3 py-2 font-mono text-xs text-slate-300">
                  {subject || '\u2014'}
                </p>
              </div>
              <div>
                <label className="block font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1.5">
                  Preview
                </label>
                <iframe
                  srcDoc={htmlBody}
                  sandbox=""
                  className="w-full h-72 rounded-lg border border-white/10 bg-white"
                  title="Email preview"
                />
              </div>
            </>
          )}
        </div>

        {error && (
          <p className="mt-3 font-mono text-xs text-red-400">{error}</p>
        )}

        <div className="mt-5 flex items-center justify-end gap-3">
          <button
            onClick={onClose}
            className="rounded-lg border border-white/10 px-4 py-2 font-mono text-xs text-slate-400 transition-colors hover:border-white/20 hover:text-white"
          >
            Cancel
          </button>
          <button
            onClick={handleSend}
            disabled={sending}
            className="rounded-lg border border-cyan-400/30 px-6 py-2 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30 disabled:cursor-not-allowed"
          >
            {sending ? "Sending..." : `Send to ${recipients.length}`}
          </button>
        </div>
      </div>
    </div>
  );
}
