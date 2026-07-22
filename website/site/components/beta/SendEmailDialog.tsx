"use client";

import { useState, useEffect } from "react";
import { PERMANENT_TEMPLATES, getTemplateContent, loadAllTemplates } from "@/lib/firebase/templateService";
import type { TemplateWithSource } from "@/types/emailTemplates";
import { sendEmail } from "@/lib/emailService";
import { useAuth } from "./AuthContext";
import { getAuthUrl } from "@/lib/firebase/gmailService";
import { getAllowedAdminsConfig } from "@/lib/firebase/authService";

interface Props {
  recipientEmail: string;
  recipientName: string;
  onClose: () => void;
}

function isAccountVisible(
  accountEmail: string,
  role: string | undefined,
  currentUserEmail: string | undefined,
  ownerSet: Set<string>,
  adminSet: Set<string>,
  defaultSenderEmail?: string
): boolean {
  if (accountEmail === defaultSenderEmail) return true;
  if (role === "owner") return true;
  if (role === "user") {
    if (!currentUserEmail) return false;
    return accountEmail === currentUserEmail;
  }
  if (role === "admin") {
    if (ownerSet.has(accountEmail)) return false;
    if (adminSet.has(accountEmail) && accountEmail !== currentUserEmail) return false;
    return true;
  }
  return true;
}

export default function SendEmailDialog({ recipientEmail, recipientName, onClose }: Props) {
  const { user } = useAuth();
  const [subject, setSubject] = useState("");
  const [htmlBody, setHtmlBody] = useState("");
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [accounts, setAccounts] = useState<{ email: string; displayName?: string; isDefault?: boolean }[]>([]);
  const [selectedAccount, setSelectedAccount] = useState("");
  const [templates, setTemplates] = useState<TemplateWithSource[]>([]);
  const [ownerSet, setOwnerSet] = useState<Set<string>>(new Set());
  const [adminSet, setAdminSet] = useState<Set<string>>(new Set());

  useEffect(() => {
    loadAllTemplates().then(setTemplates);

    Promise.all([
      fetch("/api/email-accounts").then((res) => res.json()),
      getAllowedAdminsConfig().catch(() => null),
    ]).then(([accountsData, allowedConfig]) => {
      const list = (accountsData.accounts || []).filter((a: any) => a.status === "connected");
      setAccounts(list);
      const def = list.find((a: any) => a.isDefault);
      setSelectedAccount(def?.email || list[0]?.email || "");

      if (allowedConfig) {
        setOwnerSet(new Set(allowedConfig.owners));
        setAdminSet(new Set(allowedConfig.emails));
      }
    }).catch(() => {});
  }, []);

  const loadTemplate = async (key: string) => {
    if (!key) return;
    const info = templates.find((t) => t.templateKey === key);
    if (info?.htmlBody) {
      setSubject(info.subject);
      setHtmlBody(info.htmlBody);
    } else {
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

          <div>
            <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
              Send From
            </label>
            <select
              value={selectedAccount}
              onChange={(e) => {
                const val = e.target.value;
                if (val === "__connect_own__") {
                  const url = getAuthUrl(user?.email);
                  if (url) {
                    sessionStorage.setItem("pendingSend", JSON.stringify({
                      recipientEmail,
                      recipientName,
                      subject,
                      htmlBody,
                    }));
                    window.location.href = url;
                  }
                  return;
                }
                setSelectedAccount(val);
              }}
              className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
            >
              {(() => {
                const defaultSenderEmail = accounts.find((a) => a.isDefault)?.email;
                const visibleAccounts = accounts.filter((a) =>
                  isAccountVisible(a.email, user?.role, user?.email, ownerSet, adminSet, defaultSenderEmail)
                );
                const userHasConnected = user?.email && accounts.some((a) => a.email === user.email);
                if (visibleAccounts.length === 0 && user?.role !== "user") {
                  return <option value="">No connected accounts</option>;
                }
                return (
                  <>
                    {visibleAccounts.map((a) => (
                      <option key={a.email} value={a.email}>
                        {a.displayName || a.email}{a.isDefault ? " (Default)" : ""}
                      </option>
                    ))}
                    {user?.role === "user" && user.email && !userHasConnected && (
                      <option value="__connect_own__">
                        {"\u2192"} Send as {user.email} (Connect Gmail)
                      </option>
                    )}
                    {user?.role === "user" && user.email && userHasConnected && (
                      <option value={user.email}>
                        {user.displayName || user.email} (Me)
                      </option>
                    )}
                  </>
                );
              })()}
            </select>
          </div>

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
