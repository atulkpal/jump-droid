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
  const cleanEmail = userData.email.toLowerCase().trim();

  // Write to Firestore directly from client (same connection beta page uses)
  const { getFirestore } = await import("@/lib/firebase/config");
  const firestore = await getFirestore();
  const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");

  const existingSnap = await getDoc(doc(firestore, "betaUsers", cleanEmail));
  if (existingSnap.exists()) {
    throw Object.assign(new Error("This email is already registered for the beta program."), { code: "already-exists" });
  }

  await setDoc(doc(firestore, "betaUsers", cleanEmail), {
    email: cleanEmail,
    name: userData.name || "",
    phone: userData.phone || "",
    codeJam: userData.codeJam || false,
    status: "pending",
    source,
    version: 1,
    notes: "",
    registeredFrom: "website",
    registeredAt: serverTimestamp(),
    acknowledgementSent: false,
  });

  // Non-critical outreach and logging
  try { await matchRegistration(cleanEmail); } catch {}
  try { await logEvent(cleanEmail, "registered"); } catch {}

  // Send acknowledgement email via server API
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

  if (res.ok) {
    const data = await res.json();
    if (data.acknowledgementSent) {
      try {
        await setDoc(doc(firestore, "betaUsers", cleanEmail), {
          acknowledgementSent: true,
        }, { merge: true });
      } catch {}
    }
  }
}
