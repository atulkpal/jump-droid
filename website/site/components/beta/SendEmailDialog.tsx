"use client";

import { useState, useEffect } from "react";
import { PERMANENT_TEMPLATES, getTemplateContent, loadAllTemplates } from "@/lib/firebase/templateService";
import type { TemplateWithSource } from "@/types/emailTemplates";
import { sendEmail } from "@/lib/emailService";

interface Props {
  recipientEmail: string;
  recipientName: string;
  onClose: () => void;
}

export default function SendEmailDialog({ recipientEmail, recipientName, onClose }: Props) {
  const [subject, setSubject] = useState("");
  const [htmlBody, setHtmlBody] = useState("");
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [accounts, setAccounts] = useState<{ email: string; displayName?: string }[]>([]);
  const [selectedAccount, setSelectedAccount] = useState("");
  const [templates, setTemplates] = useState<TemplateWithSource[]>([]);

  useEffect(() => {
    loadAllTemplates().then(setTemplates);

    fetch("/api/email-accounts")
      .then((res) => res.json())
      .then((data) => {
        const list = (data.accounts || []).filter((a: any) => a.status === "connected");
        setAccounts(list);
        const def = list.find((a: any) => a.isDefault);
        setSelectedAccount(def?.email || list[0]?.email || "");
      })
      .catch(() => {});
  }, []);

  const loadTemplate = async (key: string) => {
    if (!key) return;
    const info = templates.find((t) => t.templateKey === key);
    if (info?.htmlBody) {
      setSubject(info.subject);
      setHtmlBody(info.htmlBody);
    } else {
      // Load from hardcoded template
      const { renderTemplate } = await import("@/lib/emailTemplates");
      try {
        const rendered = renderTemplate(key as any, recipientName);
        setSubject(rendered.subject);
        setHtmlBody(rendered.html);
      } catch {
        setSubject("");
        setHtmlBody("<p>Hi " + recipientName + ",</p><p></p><p>— Ashwath AI</p>");
      }
    }
  };

  const handleSend = async () => {
    if (!subject.trim()) return;
    setSending(true);
    setResult(null);
    try {
      const res = await sendEmail(
        recipientEmail,
        recipientName,
        "welcome",
        "",
        "manual",
        selectedAccount || undefined,
        htmlBody || undefined,
        subject
      );
      setResult(res.success ? "Sent" : `Failed: ${res.error}`);
    } catch (e: any) {
      setResult(`Error: ${e.message}`);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center sm:items-center" onClick={onClose}>
      <div className="fixed inset-0 bg-black/70" />
      <div
        className="relative mt-16 sm:mt-0 sm:w-[580px] w-full mx-4 rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Send Email
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        <div className="space-y-4">
          <div>
            <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
              To
            </label>
            <p className="font-mono text-sm text-white">{recipientName} &lt;{recipientEmail}&gt;</p>
          </div>

          <div>
            <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
              Template
            </label>
            <select
              onChange={(e) => loadTemplate(e.target.value)}
              className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
            >
              <option value="">— Free text —</option>
              {templates.map((t) => (
                <option key={t.templateKey} value={t.templateKey}>
                  {t.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
              Subject
            </label>
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              placeholder="Email subject"
              className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
            />
          </div>

          <div>
            <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
              Content (HTML)
            </label>
            <textarea
              value={htmlBody}
              onChange={(e) => setHtmlBody(e.target.value)}
              rows={10}
              className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40 resize-y"
            />
          </div>

          {accounts.length > 0 && (
            <div>
              <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                Send From
              </label>
              <select
                value={selectedAccount}
                onChange={(e) => setSelectedAccount(e.target.value)}
                className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
              >
                {accounts.map((a) => (
                  <option key={a.email} value={a.email}>
                    {a.displayName || a.email}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="flex items-center gap-3 pt-2">
            <button
              onClick={handleSend}
              disabled={sending || !subject.trim()}
              className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30"
            >
              {sending ? "Sending..." : "Send Email"}
            </button>
            <button
              onClick={onClose}
              className="rounded-lg border border-white/10 px-6 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white"
            >
              Cancel
            </button>
            {result && (
              <span
                className={`font-mono text-[11px] ${
                  result === "Sent" ? "text-green-400" : "text-red-400"
                }`}
              >
                {result}
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
