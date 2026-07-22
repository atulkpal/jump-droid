"use client";

import { useState } from "react";
import { registerBetaUser } from "@/lib/firebase/recruitment";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^\+?[\d\s\-().]{7,20}$/;

function validateEmail(v: string): string | null {
  if (!v.trim()) return "Email address is required.";
  if (!EMAIL_RE.test(v.trim())) return "Enter a valid email address.";
  return null;
}

function validatePhone(v: string): string | null {
  if (!v.trim()) return null;
  if (!PHONE_RE.test(v.trim())) return "Enter a valid phone number (e.g. +1 555 000 0000).";
  return null;
}

export default function BetaRegistrationForm({ convertedFrom }: { convertedFrom?: string }) {
  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [agreed, setAgreed] = useState(false);
  const [codeJam, setCodeJam] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [phoneError, setPhoneError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const eErr = validateEmail(email);
    const pErr = validatePhone(phone);
    setEmailError(eErr);
    setPhoneError(pErr);

    if (eErr || pErr || !agreed || submitting) return;

    setSubmitting(true);
    setError(null);

    try {
      await registerBetaUser({
        email: email.trim(),
        name: name.trim() || undefined,
        phone: phone.trim() || undefined,
        codeJam,
        convertedFrom,
      });
      setSuccess(true);
    } catch (e: any) {
      if (e?.code === "already-exists") {
        setError("This email is already registered for the beta program.");
      } else {
        setError(e?.message ?? "Something went wrong. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (success) {
    return (
      <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] p-8 text-center space-y-3">
        <span className="text-2xl">🚀</span>
        <p className="font-mono text-sm font-bold tracking-[0.1em] text-white uppercase">
          You&apos;re on the list!
        </p>
        <p className="font-mono text-xs text-slate-400">Thanks for signing up.</p>
        <p className="font-mono text-xs text-slate-400">
          Your registration has been received.
        </p>
        <p className="font-mono text-xs text-slate-400 leading-relaxed max-w-sm mx-auto">
          We&apos;ll review your request and email you as soon as beta access is
          ready.
        </p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label
          htmlFor="beta-email"
          className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2"
        >
          Email address <span className="text-cyan-400">*</span>
        </label>
        <input
          id="beta-email"
          type="email"
          value={email}
          onChange={(e) => { setEmail(e.target.value); setEmailError(null); }}
          placeholder="you@example.com"
          required
          className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
        />
        {emailError && (
          <p className="font-mono text-[11px] text-red-400 mt-1">{emailError}</p>
        )}
      </div>

      <div>
        <label
          htmlFor="beta-name"
          className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2"
        >
          Name <span className="text-slate-500">(optional)</span>
        </label>
        <input
          id="beta-name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="How should we know you?"
          className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
        />
        <p className="font-mono text-[10px] text-slate-500 mt-1">
          Used for recognition in the Codex if provided.
        </p>
      </div>

      <div>
        <label
          htmlFor="beta-phone"
          className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2"
        >
          Phone <span className="text-slate-500">(optional)</span>
        </label>
        <input
          id="beta-phone"
          type="tel"
          value={phone}
          onChange={(e) => { setPhone(e.target.value); setPhoneError(null); }}
          placeholder="+1 (555) 000-0000"
          className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
        />
        {phoneError && (
          <p className="font-mono text-[11px] text-red-400 mt-1">{phoneError}</p>
        )}
        <p className="font-mono text-[10px] text-slate-500 mt-1">
          We&apos;ll only use this if email bounces.
        </p>
      </div>

      <label className="flex items-start gap-3 cursor-pointer">
        <input
          type="checkbox"
          checked={agreed}
          onChange={(e) => setAgreed(e.target.checked)}
          className="mt-0.5 h-4 w-4 rounded border-white/10 bg-black text-cyan-400 focus:ring-cyan-400/20 focus:ring-1"
        />
        <span className="font-mono text-[11px] text-slate-400 leading-relaxed select-none">
          I understand this is a beta version and may contain bugs.
        </span>
      </label>

      <label className="flex items-start gap-3 cursor-pointer">
        <input
          type="checkbox"
          checked={codeJam}
          onChange={(e) => setCodeJam(e.target.checked)}
          className="mt-0.5 h-4 w-4 rounded border-white/10 bg-black text-cyan-400 focus:ring-cyan-400/20 focus:ring-1"
        />
        <span className="font-mono text-[11px] text-slate-400 leading-relaxed select-none">
          I&apos;m interested in joining the Code Jam
        </span>
      </label>

      <button
        type="submit"
        disabled={!email.trim() || !agreed || submitting}
        className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
      >
        {submitting ? "Submitting..." : "Submit Registration"}
      </button>

      {error && (
        <p className="font-mono text-[11px] text-red-400">{error}</p>
      )}

      <p className="font-mono text-[10px] text-slate-500 leading-relaxed">
        We only use your information for the Jump Droid beta program. We never
        sell or share your personal information.
      </p>
    </form>
  );
}
