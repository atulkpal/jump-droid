import type { EmailTemplate } from "@/types/emailLog";
import type { SenderProfile } from "@/types/senderProfile";

const DEFAULT_SENDER: SenderProfile = {
  name: "Ashwath AI",
  email: "ashwathai.dev@gmail.com",
};

export interface SendEmailResult {
  success: boolean;
  error?: string;
  gmailMessageId?: string;
  gmailThreadId?: string;
}

export async function getSenderProfile(): Promise<SenderProfile> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc } = await import("firebase/firestore");

    const snap = await getDoc(doc(firestore, "senderProfiles", "default"));
    if (snap.exists()) {
      const d = snap.data();
      return {
        name: d.name || DEFAULT_SENDER.name,
        email: d.email || DEFAULT_SENDER.email,
      };
    }
  } catch {
    // Fall through to default
  }
  return { ...DEFAULT_SENDER };
}

// ── Client-side helpers (call /api/gmail/send) ──

export async function sendEmail(
  to: string,
  name: string,
  template: EmailTemplate,
  campaign?: string,
  source?: string
): Promise<SendEmailResult> {
  try {
    const res = await fetch("/api/gmail/send", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        emails: [{ email: to, name }],
        template,
        campaign: campaign || "",
        source: source || "recruitment",
      }),
    });

    if (!res.ok) {
      const data = await res.json();
      return { success: false, error: data.error || `HTTP ${res.status}` };
    }

    const results = await res.json();
    const result = Array.isArray(results) ? results[0] : results;
    return {
      success: result.success === true,
      error: result.error,
      gmailMessageId: result.gmailMessageId,
      gmailThreadId: result.gmailThreadId,
    };
  } catch (e: any) {
    return { success: false, error: e?.message || "Network error" };
  }
}

export async function sendBulkEmails(
  recipients: { email: string; name: string }[],
  template: EmailTemplate,
  campaign?: string,
  source?: string
): Promise<SendEmailResult[]> {
  try {
    const res = await fetch("/api/gmail/send", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        emails: recipients,
        template,
        campaign: campaign || "",
        source: source || "recruitment",
      }),
    });

    if (!res.ok) {
      const data = await res.json();
      return [{ success: false, error: data.error || `HTTP ${res.status}` }];
    }

    return await res.json();
  } catch (e: any) {
    return [{ success: false, error: e?.message || "Network error" }];
  }
}

// ── Server-side functions (call Gmail API directly) ──

export async function getAccessToken(): Promise<string> {
  const { getFirestore } = await import("@/lib/firebase/config");
  const firestore = await getFirestore();
  const { doc, getDoc, updateDoc } = await import("firebase/firestore");

  const snap = await getDoc(doc(firestore, "gmailAuth", "tokens"));
  if (!snap.exists()) throw new Error("Not authenticated with Google");

  const data = snap.data();
  const clientId = process.env.GOOGLE_CLIENT_ID;
  const clientSecret = process.env.GOOGLE_CLIENT_SECRET;

  if (!clientId || !clientSecret) {
    throw new Error("Google OAuth not configured");
  }

  if (data.expiryDate > Date.now() + 60000) {
    return data.accessToken;
  }

  const refreshRes = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      refresh_token: data.refreshToken,
      client_id: clientId,
      client_secret: clientSecret,
      grant_type: "refresh_token",
    }),
  });

  const refreshed = await refreshRes.json();
  if (!refreshed.access_token) throw new Error("Token refresh failed");

  await updateDoc(doc(firestore, "gmailAuth", "tokens"), {
    accessToken: refreshed.access_token,
    expiryDate: Date.now() + (refreshed.expires_in || 3600) * 1000,
  });

  return refreshed.access_token;
}

export function buildMimeMessage(
  to: string,
  name: string,
  subject: string,
  html: string,
  text: string,
  sender?: SenderProfile
): { raw: string; subject: string } {
  const s = sender || DEFAULT_SENDER;
  const boundary = `boundary_${Date.now()}_${Math.random().toString(36).slice(2)}`;
  const subjectEncoded = Buffer.from(subject)
    .toString("base64")
    .replace(/=/g, "");

  const headers = [
    `From: ${s.name} <${s.email}>`,
    `To: "${name}" <${to}>`,
    `Subject: =?UTF-8?B?${subjectEncoded}?=`,
    "MIME-Version: 1.0",
    `Content-Type: multipart/alternative; boundary="${boundary}"`,
    "",
    `--${boundary}`,
    "Content-Type: text/plain; charset=UTF-8",
    "Content-Transfer-Encoding: 7bit",
    "",
    text,
    "",
    `--${boundary}`,
    "Content-Type: text/html; charset=UTF-8",
    "Content-Transfer-Encoding: 7bit",
    "",
    html,
    "",
    `--${boundary}--`,
  ].join("\r\n");

  return {
    raw: Buffer.from(headers).toString("base64url"),
    subject,
  };
}

export async function logEmail(
  recipient: string,
  recipientName: string,
  template: string,
  campaign: string,
  source: string,
  status: string,
  gmailMessageId: string,
  gmailThreadId: string,
  error: string
): Promise<void> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { collection, addDoc, serverTimestamp } = await import("firebase/firestore");

    await addDoc(collection(firestore, "emailLog"), {
      recipient,
      recipientName,
      template,
      campaign: campaign || "",
      source: source || "",
      status,
      queuedAt: null,
      sentAt: status === "sent" ? serverTimestamp() : null,
      failedAt: status === "failed" ? serverTimestamp() : null,
      retryCount: 0,
      providerMessageId: gmailMessageId || "",
      providerThreadId: gmailThreadId || "",
      failureReason: error ? error.slice(0, 500) : "",
      error: error || "",
    });
  } catch {
    // Logging failure should not break the send flow
  }
}

function decodeBounceBody(payload: any): string {
  const parts: any[] = [];
  function walk(node: any) {
    if (node.body?.data) {
      parts.push(Buffer.from(node.body.data, "base64").toString("utf-8"));
    }
    if (node.parts) {
      node.parts.forEach(walk);
    }
  }
  walk(payload);
  return parts.join("\n");
}

function extractBouncedEmails(bodyText: string, senderEmail: string): string[] {
  const emailRegex = /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
  const found = bodyText.match(emailRegex) || [];
  return [...new Set(found)].filter((e) => e.toLowerCase() !== senderEmail.toLowerCase());
}

async function getLastBounceCheck(): Promise<number | null> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc } = await import("firebase/firestore");
    const snap = await getDoc(doc(firestore, "appConfig", "bounceDetection"));
    return snap.exists() ? (snap.data().lastCheckAt?.seconds ?? null) : null;
  } catch {
    return null;
  }
}

async function setLastBounceCheck(): Promise<void> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");
    await setDoc(doc(firestore, "appConfig", "bounceDetection"), { lastCheckAt: serverTimestamp() }, { merge: true });
  } catch {
    // Non-critical
  }
}

async function isBounceProcessed(messageId: string): Promise<boolean> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc } = await import("firebase/firestore");
    const snap = await getDoc(doc(firestore, "appConfig", "bounceDetection"));
    if (!snap.exists()) return false;
    const processed: string[] = snap.data().processedBounceIds || [];
    return processed.includes(messageId);
  } catch {
    return false;
  }
}

async function markBounceProcessed(messageId: string): Promise<void> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, setDoc, arrayUnion } = await import("firebase/firestore");
    await setDoc(doc(firestore, "appConfig", "bounceDetection"), { processedBounceIds: arrayUnion(messageId) }, { merge: true });
  } catch {
    // Non-critical
  }
}

async function markBouncedInEmailLog(recipient: string): Promise<void> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { collection, getDocs, query, where, orderBy, doc, updateDoc, serverTimestamp } = await import("firebase/firestore");

    const q = query(
      collection(firestore, "emailLog"),
      where("recipient", "==", recipient.toLowerCase().trim()),
      orderBy("sentAt", "desc")
    );
    const snap = await getDocs(q);
    if (snap.empty) return;

    const latest = snap.docs[0];
    await updateDoc(doc(firestore, "emailLog", latest.id), {
      status: "failed",
      failedAt: serverTimestamp(),
      failureReason: "permanent_bounce",
      error: "Bounced: recipient address rejected or does not exist",
    });
  } catch {
    // Non-critical
  }
}

async function markBouncedInOutreach(recipient: string): Promise<void> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc, updateDoc } = await import("firebase/firestore");

    const ref = doc(firestore, "recruitmentContacts", recipient.toLowerCase().trim());
    const snap = await getDoc(ref);
    if (!snap.exists()) return;

    await updateDoc(ref, {
      status: "failed",
      stoppedReason: "permanent_bounce",
      emailStatus: "failed",
    });
  } catch {
    // Non-critical
  }
}

async function markBouncedInApplicants(recipient: string): Promise<void> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc, updateDoc } = await import("firebase/firestore");

    const ref = doc(firestore, "betaUsers", recipient.toLowerCase().trim());
    const snap = await getDoc(ref);
    if (!snap.exists()) return;

    await updateDoc(ref, {
      emailStatus: "failed",
    });
  } catch {
    // Non-critical
  }
}

export async function detectBounces(): Promise<number> {
  let bounced = 0;
  try {
    const sender = await getSenderProfile();
    const lastCheck = await getLastBounceCheck();
    const accessToken = await getAccessToken();

    let q = "from:mailer-daemon@googlemail.com";
    if (lastCheck) {
      q += ` after:${Math.floor(lastCheck)}`;
    }

    const listRes = await fetch(
      `https://gmail.googleapis.com/gmail/v1/users/me/messages?q=${encodeURIComponent(q)}`,
      { headers: { Authorization: `Bearer ${accessToken}` } }
    );

    if (!listRes.ok) return 0;

    const listData = await listRes.json();
    const messages: { id: string }[] = listData.messages || [];
    if (messages.length === 0) {
      await setLastBounceCheck();
      return 0;
    }

    const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();

    for (const msg of messages) {
      try {
        if (await isBounceProcessed(msg.id)) continue;

        const getRes = await fetch(
          `https://gmail.googleapis.com/gmail/v1/users/me/messages/${msg.id}?format=full`,
          { headers: { Authorization: `Bearer ${accessToken}` } }
        );
        if (!getRes.ok) continue;

        const msgData = await getRes.json();
        const bodyText = decodeBounceBody(msgData.payload);
        const bouncedEmails = extractBouncedEmails(bodyText, sender.email);

        for (const be of bouncedEmails) {
          await Promise.all([
            markBouncedInEmailLog(be),
            markBouncedInOutreach(be),
            markBouncedInApplicants(be),
          ]);

          const { logEvent } = await import("@/lib/firebase/activityService");
          await logEvent(be, "invitation_failed", "Permanent bounce detected via Gmail DSN");

          bounced++;
        }

        await markBounceProcessed(msg.id);
      } catch {
        // Per-message failure should not break the batch
      }
    }

    await setLastBounceCheck();
  } catch {
    // Bounce detection failure should not crash the caller
  }
  return bounced;
}
