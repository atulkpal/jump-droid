import type { Firestore } from "firebase-admin/firestore";
import {
  getSenderProfileAdmin,
  getAccessTokenAdmin,
} from "@/lib/emailService";
import { logDebug } from "@/lib/debugLogger";

function decodeBounceBody(payload: any): string {
  const parts: string[] = [];
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

async function getLastBounceCheck(adminFirestore: Firestore): Promise<number | null> {
  try {
    const snap = await adminFirestore.collection("appConfig").doc("bounceDetection").get();
    return snap.exists ? (snap.data()!.lastCheckAt?.seconds ?? null) : null;
  } catch {
    return null;
  }
}

async function setLastBounceCheck(adminFirestore: Firestore): Promise<void> {
  try {
    await adminFirestore.collection("appConfig").doc("bounceDetection").set(
      { lastCheckAt: new Date() },
      { merge: true }
    );
  } catch {
    // Non-critical
  }
}

async function isBounceProcessed(adminFirestore: Firestore, messageId: string): Promise<boolean> {
  try {
    const snap = await adminFirestore.collection("appConfig").doc("bounceDetection").get();
    if (!snap.exists) return false;
    const processed: string[] = snap.data()!.processedBounceIds || [];
    return processed.includes(messageId);
  } catch {
    return false;
  }
}

async function markBounceProcessed(adminFirestore: Firestore, messageId: string): Promise<void> {
  try {
    const { FieldValue } = await import("firebase-admin/firestore");
    await adminFirestore.collection("appConfig").doc("bounceDetection").set(
      { processedBounceIds: FieldValue.arrayUnion(messageId) },
      { merge: true }
    );
  } catch {
    // Non-critical
  }
}

async function markBouncedInEmailLog(adminFirestore: Firestore, recipient: string): Promise<void> {
  try {
    const snap = await adminFirestore
      .collection("emailLog")
      .where("recipient", "==", recipient.toLowerCase().trim())
      .get();
    if (snap.empty) return;
    const docs = snap.docs;
    docs.sort((a, b) => ((b.data().sentAt?.seconds ?? 0) - (a.data().sentAt?.seconds ?? 0)));
    await docs[0].ref.update({
      status: "failed",
      failedAt: new Date(),
      failureReason: "permanent_bounce",
      error: "Bounced: recipient address rejected or does not exist",
    });
  } catch {
    // Non-critical
  }
}

async function markBouncedInOutreach(adminFirestore: Firestore, recipient: string, sentCampaigns?: Set<string>): Promise<void> {
  try {
    const ref = adminFirestore.collection("recruitmentContacts").doc(recipient.toLowerCase().trim());
    const snap = await ref.get();
    if (!snap.exists) return;
    const data = snap.data() || {};
    const campaigns: string[] = data.campaigns || [];

    // Only mark campaigns that actually sent to this contact (or all if no filter provided)
    const targetCampaigns = sentCampaigns && sentCampaigns.size > 0
      ? campaigns.filter(c => sentCampaigns.has(c))
      : campaigns;

    if (targetCampaigns.length === 0) {
      logDebug("bounce_detection.no_campaign_match", "warn",
        `Bounce for ${recipient} but no matching campaign with sent record — skipping outreach update`,
        { contactEmail: recipient });
      return;
    }

    const updates: Record<string, any> = {
      status: "bounced",
      stoppedReason: "permanent_bounce",
      emailStatus: "failed",
    };
    for (const cid of targetCampaigns) {
      updates[`campaignData.${cid}.status`] = "bounced";
      updates[`campaignData.${cid}.stoppedReason`] = "permanent_bounce";
      updates[`campaignData.${cid}.emailStatus`] = "failed";
    }
    await ref.update(updates);
  } catch {
    // Non-critical
  }
}

async function markBouncedInApplicants(adminFirestore: Firestore, recipient: string): Promise<void> {
  try {
    const ref = adminFirestore.collection("betaUsers").doc(recipient.toLowerCase().trim());
    const snap = await ref.get();
    if (!snap.exists) return;
    await ref.update({
      emailStatus: "failed",
    });
  } catch {
    // Non-critical
  }
}

export async function detectBounces(adminFirestore: Firestore): Promise<number> {
  let bounced = 0;
  try {
    const sender = await getSenderProfileAdmin(adminFirestore);
    const lastCheck = await getLastBounceCheck(adminFirestore);

    // Load all connected email accounts
    const accountsSnap = await adminFirestore
      .collection("emailAccounts")
      .where("status", "==", "connected")
      .get();
    const accountEmails: string[] = [];
    accountsSnap.forEach((doc) => {
      const d = doc.data();
      if (d.email) accountEmails.push(d.email);
    });
    // Also try the sender profile's email as fallback
    if (sender.email && !accountEmails.includes(sender.email)) {
      accountEmails.push(sender.email);
    }

    let q = "from:mailer-daemon@googlemail.com";
    if (lastCheck) {
      q += ` after:${Math.floor(lastCheck)}`;
    }

    for (const accountEmail of accountEmails) {
      let accessToken: string;
      try {
        accessToken = await getAccessTokenAdmin(adminFirestore, accountEmail);
      } catch {
        continue;
      }

      const listRes = await fetch(
        `https://gmail.googleapis.com/gmail/v1/users/me/messages?q=${encodeURIComponent(q)}`,
        { headers: { Authorization: `Bearer ${accessToken}` } }
      );
      if (!listRes.ok) continue;

      const listData = await listRes.json();
      const messages: { id: string }[] = listData.messages || [];
      if (messages.length === 0) continue;

      for (const msg of messages) {
        try {
          if (await isBounceProcessed(adminFirestore, msg.id)) continue;

          const getRes = await fetch(
            `https://gmail.googleapis.com/gmail/v1/users/me/messages/${msg.id}?format=full`,
            { headers: { Authorization: `Bearer ${accessToken}` } }
          );
          if (!getRes.ok) continue;

          const msgData = await getRes.json();
          const bodyText = decodeBounceBody(msgData.payload);
          const bouncedEmails = extractBouncedEmails(bodyText, sender.email);

          for (const be of bouncedEmails) {
            // Verify email was actually sent by us before marking bounced
            const sentSnap = await adminFirestore
              .collection("emailLog")
              .where("recipient", "==", be.toLowerCase().trim())
              .where("status", "==", "sent")
              .limit(1)
              .get();

            if (sentSnap.empty) {
              logDebug("bounce_detection.not_sent", "warn",
                `Bounce found for ${be} but no sent emailLog record — skipping (possibly stale or unrelated bounce)`,
                { contactEmail: be });
              continue;
            }

            // Extract which campaigns sent to this contact
            const sentCampaigns = new Set<string>();
            sentSnap.docs.forEach(d => {
              const cid = d.data().campaign;
              if (cid) sentCampaigns.add(cid);
            });

            await Promise.all([
              markBouncedInEmailLog(adminFirestore, be),
              markBouncedInOutreach(adminFirestore, be, sentCampaigns),
              markBouncedInApplicants(adminFirestore, be),
            ]);

            const { logEventAdmin } = await import("@/lib/firebase/activityService");
            await logEventAdmin(adminFirestore, be, "invitation_failed", "Permanent bounce detected via Gmail DSN");

            bounced++;
          }

          await markBounceProcessed(adminFirestore, msg.id);
        } catch {
          // Per-message failure should not break the batch
        }
      }
    }

    await setLastBounceCheck(adminFirestore);
  } catch {
    // Bounce detection failure should not crash the caller
  }
  return bounced;
}
