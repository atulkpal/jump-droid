"use client";

import { useState, useEffect, useCallback } from "react";
import type { TemplateWithSource } from "@/types/emailTemplates";
import {
  loadAllTemplates,
  PERMANENT_TEMPLATES,
  saveTemplateOverride,
  deleteTemplateOverride,
  createCustomTemplate,
  updateCustomTemplate,
  deleteCustomTemplate,
} from "@/lib/firebase/templateService";
import { renderTemplate } from "@/lib/emailTemplates";
import { sendEmail } from "@/lib/emailService";
import { useRole } from "@/components/beta/AuthContext";

export default function EmailPage() {
  const { role } = useRole();
  const [templates, setTemplates] = useState<TemplateWithSource[]>([]);
  const [selectedKey, setSelectedKey] = useState<string>("");
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");
  const [htmlBody, setHtmlBody] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [sending, setSending] = useState(false);
  const [sendTo, setSendTo] = useState("");
  const [sendResult, setSendResult] = useState<string | null>(null);
  const [accounts, setAccounts] = useState<{ email: string; displayName?: string }[]>([]);
  const [selectedAccount, setSelectedAccount] = useState("");
  const [editingNew, setEditingNew] = useState(false);

  useEffect(() => {
    loadAllTemplates().then((list) => {
      setTemplates(list);
      setLoading(false);
      if (list.length > 0) setSelectedKey(list[0].templateKey);
    }).catch(() => setLoading(false));
  }, []);

  useEffect(() => {
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

  const selected = templates.find((t) => t.templateKey === selectedKey);
  const isPermanent = PERMANENT_TEMPLATES.some((t) => t.key === selectedKey);
  const isCustom = selected?.isCustom;

  useEffect(() => {
    if (!selected) return;
    if (selected.isCustom || selected.htmlBody) {
      setName(selected.name);
      setSubject(selected.subject);
      setHtmlBody(selected.htmlBody);
    } else {
      // Load from hardcoded template for preview
      try {
        const rendered = renderTemplate(selectedKey as any, "{{name}}");
        setName(selected.name);
        setSubject(rendered.subject);
        setHtmlBody(rendered.html);
      } catch {
        setName(selected.name);
        setSubject("");
        setHtmlBody("");
      }
    }
    setSendResult(null);
    setEditingNew(false);
  }, [selectedKey, selected]);

  const handleSave = useCallback(async () => {
    if (!selectedKey || !subject.trim()) return;
    setSaving(true);
    try {
      if (editingNew) {
        const id = name.toLowerCase().replace(/[^a-z0-9]+/g, "-");
        await createCustomTemplate(id, { name, subject, htmlBody });
        setEditingNew(false);
        const list = await loadAllTemplates();
        setTemplates(list);
        setSelectedKey(id);
      } else if (isCustom) {
        await updateCustomTemplate(selectedKey, { name, subject, htmlBody });
      } else {
        await saveTemplateOverride(selectedKey, { name, subject, htmlBody });
        const list = await loadAllTemplates();
        setTemplates(list);
      }
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } catch (e: any) {
      console.error("Save failed", e);
    } finally {
      setSaving(false);
    }
  }, [selectedKey, name, subject, htmlBody, isCustom, editingNew]);

  const handleReset = useCallback(async () => {
    if (!selectedKey || isCustom) return;
    setSaving(true);
    try {
      await deleteTemplateOverride(selectedKey);
      const rendered = renderTemplate(selectedKey as any, "{{name}}");
      const templateInfo = PERMANENT_TEMPLATES.find((t) => t.key === selectedKey);
      setName(templateInfo?.label || selectedKey);
      setSubject(rendered.subject);
      setHtmlBody(rendered.html);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } catch (e: any) {
      console.error("Reset failed", e);
    } finally {
      setSaving(false);
    }
  }, [selectedKey, isCustom]);

  const handleDelete = useCallback(async () => {
    if (!selectedKey || !isCustom) return;
    setSaving(true);
    try {
      await deleteCustomTemplate(selectedKey);
      const list = await loadAllTemplates();
      setTemplates(list);
      if (list.length > 0) setSelectedKey(list[0].templateKey);
    } catch (e: any) {
      console.error("Delete failed", e);
    } finally {
      setSaving(false);
    }
  }, [selectedKey, isCustom]);

  const handleAddNew = () => {
    setEditingNew(true);
    setName("");
    setSubject("");
    setHtmlBody("<p>Hi {{name}},</p><p></p><p>— Ashwath AI</p>");
    setSelectedKey("__new__");
  };

  const handleSendTest = useCallback(async () => {
    if (!sendTo.trim() || !subject.trim()) return;
    setSending(true);
    setSendResult(null);
    try {
      const result = await sendEmail(
        sendTo.trim(),
        sendTo.trim(),
        selectedKey as any || "welcome",
        "test",
        "email-page",
        selectedAccount || undefined,
        htmlBody || undefined,
        subject
      );
      setSendResult(result.success ? "Sent successfully" : `Failed: ${result.error}`);
    } catch (e: any) {
      setSendResult(`Error: ${e.message}`);
    } finally {
      setSending(false);
    }
  }, [sendTo, subject, htmlBody, selectedAccount, selectedKey]);

  const previewUrl = URL.createObjectURL(new Blob([htmlBody || ""], { type: "text/html" }));

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-black text-white selection:bg-cyan-500/30">
      <div className="fixed inset-0 z-0 bg-glow-top-cyan" />

      <main className="relative z-10 mx-auto max-w-7xl px-6 py-16 sm:px-8 sm:py-20">
        <div className="mb-10">
          <h1 className="font-mono text-xl font-bold tracking-[0.1em] text-white uppercase">
            Email
            <span className="text-cyan-300 ml-2">Templates</span>
          </h1>
        </div>

        {loading ? (
          <p className="font-mono text-xs text-slate-500">Loading templates...</p>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
            {/* Left sidebar — template list */}
            <div className="lg:col-span-1">
              <div className="rounded-lg border border-white/5 bg-white/[0.02] p-4">
                <div className="flex items-center justify-between mb-3">
                  <h2 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase">
                    Templates
                  </h2>
                  <button
                    onClick={handleAddNew}
                    className="font-mono text-[10px] text-cyan-400 hover:text-cyan-300 transition-colors"
                  >
                    + New
                  </button>
                </div>
                <div className="space-y-1">
                  {templates.map((t) => (
                    <button
                      key={t.templateKey}
                      onClick={() => setSelectedKey(t.templateKey)}
                      className={`w-full text-left px-3 py-2 rounded-lg font-mono text-xs transition-colors ${
                        selectedKey === t.templateKey
                          ? "bg-cyan-400/10 text-cyan-300 border border-cyan-400/20"
                          : "text-slate-400 hover:text-white hover:bg-white/5"
                      }`}
                    >
                      <span>{t.name}</span>
                      {!t.isCustom && (
                        <span className="text-[10px] text-slate-500 ml-1">&#x1f512;</span>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Editor */}
            <div className="lg:col-span-2">
              <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6 space-y-4">
                <h2 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-4">
                  {editingNew ? "New Template" : "Editor"}
                </h2>

                <div>
                  <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                    Name
                  </label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Template name"
                    disabled={!isCustom && !editingNew}
                    className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 disabled:opacity-50"
                  />
                </div>

                <div>
                  <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                    Subject
                  </label>
                  <input
                    type="text"
                    value={subject}
                    onChange={(e) => setSubject(e.target.value)}
                    placeholder="Email subject line"
                    className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
                  />
                </div>

                <div>
                  <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                    HTML Body
                  </label>
                  <textarea
                    value={htmlBody}
                    onChange={(e) => setHtmlBody(e.target.value)}
                    rows={16}
                    className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40 resize-y font-mono"
                  />
                </div>

                <div className="flex items-center gap-3 pt-2">
                  {(!isPermanent || role !== "user") && (
                    <button
                      onClick={handleSave}
                      disabled={saving || !subject.trim()}
                      className="rounded-lg border border-cyan-400/30 px-5 py-2.5 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30"
                    >
                      {saving ? "Saving..." : "Save"}
                    </button>
                  )}
                  {isPermanent && !editingNew && role !== "user" && (
                    <button
                      onClick={handleReset}
                      disabled={saving}
                      className="rounded-lg border border-white/10 px-5 py-2.5 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white"
                    >
                      Reset to Default
                    </button>
                  )}
                  {isCustom && !editingNew && (
                    <button
                      onClick={handleDelete}
                      disabled={saving}
                      className="rounded-lg border border-red-400/20 px-5 py-2.5 font-mono text-xs tracking-[0.15em] text-red-400/70 transition-colors hover:border-red-400/40 hover:text-red-400"
                    >
                      Delete
                    </button>
                  )}
                  {saved && (
                    <span className="font-mono text-[11px] text-green-400">Saved</span>
                  )}
                </div>
              </div>
            </div>

            {/* Preview + Test send */}
            <div className="lg:col-span-2 space-y-6">
              <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
                <h2 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-4">
                  Preview
                </h2>
                {htmlBody ? (
                  <iframe
                    src={previewUrl}
                    className="w-full h-96 rounded-lg border border-white/5 bg-white"
                    title="Email preview"
                  />
                ) : (
                  <p className="font-mono text-xs text-slate-500">Select a template to preview</p>
                )}
              </div>

              <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
                <h2 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-4">
                  Send Test Email
                </h2>
                <div className="flex items-center gap-3">
                  <input
                    type="email"
                    value={sendTo}
                    onChange={(e) => setSendTo(e.target.value)}
                    placeholder="you@example.com"
                    className="flex-1 rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
                  />
                  {accounts.length > 0 && (
                    <select
                      value={selectedAccount}
                      onChange={(e) => setSelectedAccount(e.target.value)}
                      className="rounded-lg border border-white/10 bg-black px-3 py-3 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40"
                    >
                      {accounts.map((a) => (
                        <option key={a.email} value={a.email}>
                          {a.displayName || a.email}
                        </option>
                      ))}
                    </select>
                  )}
                  <button
                    onClick={handleSendTest}
                    disabled={sending || !sendTo.trim()}
                    className="rounded-lg border border-cyan-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30 shrink-0"
                  >
                    {sending ? "Sending..." : "Send"}
                  </button>
                </div>
                {sendResult && (
                  <p
                    className={`font-mono text-[11px] mt-2 ${
                      sendResult.startsWith("Sent") ? "text-green-400" : "text-red-400"
                    }`}
                  >
                    {sendResult}
                  </p>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
