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

export async function getSenderProfile(accountEmail?: string): Promise<SenderProfile> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc, collection, getDocs, query, where } = await import("firebase/firestore");

    if (accountEmail) {
      const snap = await getDoc(doc(firestore, "emailAccounts", accountEmail.toLowerCase().trim()));
      if (snap.exists()) {
        const d = snap.data();
        if (d.status === "connected" || d.status === "expired") {
          return { name: d.displayName || d.email, email: d.email };
        }
      }
    } else {
      const q = query(
        collection(firestore, "emailAccounts"),
        where("isDefault", "==", true),
        where("status", "==", "connected")
      );
      const snap = await getDocs(q);
      if (!snap.empty) {
        const d = snap.docs[0].data();
        return { name: d.displayName || d.email, email: d.email };
      }

      const fallback = query(
        collection(firestore, "emailAccounts"),
        where("status", "==", "connected")
      );
      const fallbackSnap = await getDocs(fallback);
      if (!fallbackSnap.empty) {
        const d = fallbackSnap.docs[0].data();
        return { name: d.displayName || d.email, email: d.email };
      }
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
  source?: string,
  senderAccountId?: string,
  customHtml?: string,
  customSubject?: string
): Promise<SendEmailResult> {
  try {
    const body: any = {
      emails: [{ email: to, name }],
      template,
      campaign: campaign || "",
      source: source || "recruitment",
    };
    if (senderAccountId) body.senderAccountId = senderAccountId;
    if (customHtml) body.htmlBody = customHtml;
    if (customSubject) body.subject = customSubject;

    const res = await fetch("/api/gmail/send", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
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
  source?: string,
  senderAccountId?: string
): Promise<SendEmailResult[]> {
  try {
    const body: any = {
      emails: recipients,
      template,
      campaign: campaign || "",
      source: source || "recruitment",
    };
    if (senderAccountId) body.senderAccountId = senderAccountId;

    const res = await fetch("/api/gmail/send", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
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

// ── Server-side functions ──

export async function getAccessToken(accountEmail?: string): Promise<string> {
  const { getFirestore } = await import("@/lib/firebase/config");
  const firestore = await getFirestore();
  const { doc, getDoc, updateDoc, collection, getDocs, query, where } = await import("firebase/firestore");

  let docSnap: any;

  if (accountEmail) {
    docSnap = await getDoc(doc(firestore, "emailAccounts", accountEmail.toLowerCase().trim()));
    if (!docSnap.exists()) throw new Error("Email account not found");
  } else {
    const q = query(
      collection(firestore, "emailAccounts"),
      where("isDefault", "==", true),
      where("status", "in", ["connected", "expired"])
    );
    const results = await getDocs(q);
    if (!results.empty) {
      docSnap = results.docs[0];
    } else {
      const fallback = query(collection(firestore, "emailAccounts"), where("status", "in", ["connected", "expired"]));
      const fallbackResults = await getDocs(fallback);
      if (fallbackResults.empty) throw new Error("Not authenticated with Google");
      docSnap = fallbackResults.docs[0];
    }
  }

  const data = docSnap.data();
  if (!data) throw new Error("Email account data not found");
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
  if (!refreshed.access_token) {
    const errMsg = refreshed.error_description || refreshed.error || "Token refresh failed";
    const email = data.email || "unknown";

    if (refreshRes.status === 400 || refreshRes.status === 401) {
      await updateDoc(doc(firestore, "emailAccounts", email.toLowerCase().trim()), {
        status: "expired",
        errorMessage: errMsg.slice(0, 500),
      });

      const { logEmailAudit } = await import("@/lib/firebase/auditService");
      await logEmailAudit("oauth_error", email, `Token refresh failed: ${errMsg}`);
    }

    throw new Error(errMsg);
  }

  const email = data.email || "unknown";
  await updateDoc(doc(firestore, "emailAccounts", email.toLowerCase().trim()), {
    accessToken: refreshed.access_token,
    expiryDate: Date.now() + (refreshed.expires_in || 3600) * 1000,
    status: "connected",
    errorMessage: null,
    lastUsedAt: (await import("firebase/firestore")).serverTimestamp(),
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

// ── Server-side (Admin SDK) versions ──

import type { Firestore, DocumentSnapshot } from "firebase-admin/firestore";

export async function getSenderProfileAdmin(
  adminFirestore: Firestore,
  accountEmail?: string
): Promise<SenderProfile> {
  try {
    if (accountEmail) {
      const snap = await adminFirestore.collection("emailAccounts").doc(accountEmail.toLowerCase().trim()).get();
      if (snap.exists) {
        const d = snap.data()!;
        if (d.status === "connected" || d.status === "expired") {
          return { name: d.displayName || d.email, email: d.email };
        }
      }
    } else {
      const q = adminFirestore
        .collection("emailAccounts")
        .where("isDefault", "==", true)
        .where("status", "==", "connected")
        .limit(1);
      const snap = await q.get();
      if (!snap.empty) {
        const d = snap.docs[0].data();
        return { name: d.displayName || d.email, email: d.email };
      }

      const fallback = adminFirestore
        .collection("emailAccounts")
        .where("status", "==", "connected")
        .limit(1);
      const fallbackSnap = await fallback.get();
      if (!fallbackSnap.empty) {
        const d = fallbackSnap.docs[0].data();
        return { name: d.displayName || d.email, email: d.email };
      }
    }
  } catch {
    // Fall through to default
  }
  return { ...DEFAULT_SENDER };
}

export async function getAccessTokenAdmin(
  adminFirestore: Firestore,
  accountEmail?: string
): Promise<string> {
  let docSnap: DocumentSnapshot | null = null;

  if (accountEmail) {
    docSnap = await adminFirestore.collection("emailAccounts").doc(accountEmail.toLowerCase().trim()).get();
    if (!docSnap.exists) throw new Error("Email account not found");
  } else {
    const q = adminFirestore
      .collection("emailAccounts")
      .where("isDefault", "==", true)
      .where("status", "in", ["connected", "expired"])
      .limit(1);
    const results = await q.get();
    if (!results.empty) {
      docSnap = results.docs[0];
    } else {
      const fallback = adminFirestore
        .collection("emailAccounts")
        .where("status", "in", ["connected", "expired"])
        .limit(1);
      const fallbackResults = await fallback.get();
      if (fallbackResults.empty) throw new Error("Not authenticated with Google");
      docSnap = fallbackResults.docs[0];
    }
  }

  if (!docSnap) throw new Error("Email account not found");
  const data = docSnap.data();
  if (!data) throw new Error("Email account data not found");
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
  if (!refreshed.access_token) {
    const errMsg = refreshed.error_description || refreshed.error || "Token refresh failed";
    const email = data.email || "unknown";

    if (refreshRes.status === 400 || refreshRes.status === 401) {
      await adminFirestore.collection("emailAccounts").doc(email.toLowerCase().trim()).update({
        status: "expired",
        errorMessage: errMsg.slice(0, 500),
      });

      const { logEmailAuditAdmin } = await import("@/lib/firebase/auditService");
      await logEmailAuditAdmin(adminFirestore, "oauth_error", email, `Token refresh failed: ${errMsg}`);
    }

    throw new Error(errMsg);
  }

  const email = data.email || "unknown";
  await adminFirestore.collection("emailAccounts").doc(email.toLowerCase().trim()).update({
    accessToken: refreshed.access_token,
    expiryDate: Date.now() + (refreshed.expires_in || 3600) * 1000,
    status: "connected",
    errorMessage: null,
  });

  return refreshed.access_token;
}

export async function logEmailAdmin(
  adminFirestore: Firestore,
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
    await adminFirestore.collection("emailLog").add({
      recipient,
      recipientName,
      template,
      campaign: campaign || "",
      source: source || "",
      status,
      queuedAt: null,
      sentAt: status === "sent" ? new Date() : null,
      failedAt: status === "failed" ? new Date() : null,
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
