import type { BetaUserRegistration } from "@/types/betaUser";
import { matchRegistration } from "./outreachService";
import { logEvent } from "./activityService";

function detectSource(): string {
  if (typeof document === "undefined") return "website";

  const params = new URLSearchParams(window.location.search);
  const sourceParam = params.get("source") || params.get("ref") || "";
  if (sourceParam) return sourceParam.trim().toLowerCase();

  const ref = document.referrer?.toLowerCase() || "";
  if (!ref) return "direct";

  const known: [string, string][] = [
    ["github.com", "github"],
    ["reddit.com", "reddit"],
    ["itch.io", "itch.io"],
    ["youtube.com", "youtube"],
    ["linkedin.com", "linkedin"],
    ["apkpure.com", "apkpure"],
    ["discord.com", "referral"],
    ["discord.gg", "referral"],
  ];
  for (const [match, label] of known) {
    if (ref.includes(match)) return label;
  }
  return "referral";
}

export async function registerBetaUser(userData: BetaUserRegistration): Promise<void> {
  const source = detectSource();

  const res = await fetch("/api/recruitment/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      email: userData.email,
      name: userData.name || "",
      phone: userData.phone || "",
      codeJam: userData.codeJam || false,
      source,
    }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.error || `Registration failed (HTTP ${res.status})`);
  }

  const data = await res.json();
  if (!data.acknowledgementSent) {

  }
}
