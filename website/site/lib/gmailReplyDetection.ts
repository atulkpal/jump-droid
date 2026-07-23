import type { Firestore } from "firebase-admin/firestore";
import { FieldValue } from "firebase-admin/firestore";
import { getAccessTokenAdmin } from "@/lib/emailService";
import type { PendingWrite } from "@/lib/campaignProcessor";
import { pushWrite } from "@/lib/campaignProcessor";
import { logDebug, logDebugAdmin } from "@/lib/debugLogger";

const REPLY_CONFIG_DOC = "appConfig/replyDetection";
const DEFAULT_SENDER_EMAIL = "ashwathai.dev@gmail.com";

export interface ReplyDetectionResult {
  accountsPolled: number;
  totalChecked: number;
  totalReplied: number;
  errors: string[];
}

function extractPlainText(payload: any): string {
  if (payload.mimeType === "text/plain" && payload.body?.data) {
    return Buffer.from(payload.body.data, "base64").toString("utf-8");
  }
  if (payload.parts) {
    for (const part of payload.parts) {
      const result = extractPlainText(part);
      if (result) return result;
    }
  }
  return "";
}

async function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function detectReplies(
  adminFirestore: Firestore,
  buffer: PendingWrite[]
): Promise<ReplyDetectionResult> {
  const errors: string[] = [];

  // 1. Read config
  const configSnap = await adminFirestore.doc(REPLY_CONFIG_DOC).get();
  const lastCheckAt: number = configSnap.exists
    ? (configSnap.data()?.lastCheckAt ?? Math.floor(Date.now() / 1000) - 7 * 24 * 3600)
    : Math.floor(Date.now() / 1000) - 7 * 24 * 3600;
  const processedIds: string[] = configSnap.exists
    ? (configSnap.data()?.processedReplyIds ?? [])
    : [];
  const processedSet = new Set(processedIds);

  // 2. Load all contact emails into memory
  const contactsSnap = await adminFirestore.collection("recruitmentContacts").get();
  const contactEmails = new Set<string>();
  contactsSnap.forEach((doc) => contactEmails.add(doc.id));

  // 3. Load recent emailLog into memory (threadId → campaign info)
  const emailLogSnap = await adminFirestore
    .collection("emailLog")
    .orderBy("sentAt", "desc")
    .limit(1000)
    .get();
  const threadMap = new Map<string, { campaignId: string; recipient: string }>();
  emailLogSnap.forEach((doc) => {
    const d = doc.data();
    const threadId: string = d.providerThreadId || "";
    if (d.status === "sent" && threadId) {
      threadMap.set(threadId, {
        campaignId: d.campaign || "",
        recipient: (d.recipient || "").toLowerCase().trim(),
      });
    }
  });

  logDebug("reply_detection.thread_map", "info", `Reply detection: ${threadMap.size} threads in emailLog (${emailLogSnap.docs.length} log entries)`);

  if (threadMap.size === 0) {
    logDebug("reply_detection.no_threads", "info", "No sent email threads found — skipping reply detection");
    return { accountsPolled: 0, totalChecked: 0, totalReplied: 0, errors: [] };
  }

  // 4. Load sender accounts
  const accountsSnap = await adminFirestore
    .collection("emailAccounts")
    .where("status", "==", "connected")
    .get();
  const senderAccounts: string[] = [];
  accountsSnap.forEach((doc) => {
    const d = doc.data();
    if (d.email) senderAccounts.push(d.email);
  });
  if (!senderAccounts.includes(DEFAULT_SENDER_EMAIL)) {
    senderAccounts.push(DEFAULT_SENDER_EMAIL);
  }

  let totalChecked = 0;
  const replyUpdates: Array<{
    email: string;
    campaignId: string;
    gmailMsgId: string;
    senderAccount: string;
    snippet: string;
  }> = [];

  // 5. Poll each sender inbox
  for (const senderEmail of senderAccounts) {
    let accessToken: string;
    try {
      accessToken = await getAccessTokenAdmin(adminFirestore, senderEmail);
    } catch (e: any) {
      errors.push(`Auth failed for ${senderEmail}: ${e?.message}`);
      continue;
    }

    let pageToken: string | undefined;
    let retries = 0;

    do {
      try {
        const listUrl = `https://gmail.googleapis.com/gmail/v1/users/me/messages?q=in:inbox after:${lastCheckAt}&maxResults=50${
          pageToken ? `&pageToken=${pageToken}` : ""
        }`;
        const listRes = await fetch(listUrl, {
          headers: { Authorization: `Bearer ${accessToken}` },
        });

        if (listRes.status === 429) {
          retries++;
          if (retries > 3) {
            errors.push(`Rate limited on ${senderEmail} after 3 retries`);
            break;
          }
          console.warn(`[ReplyDetection] Rate limited on ${senderEmail}, retry ${retries}`);
          const errRef = adminFirestore.collection("activityLog").doc();
          pushWrite(
            buffer,
            errRef,
            {
              applicantEmail: "system",
              eventType: "rate_limited",
              details: `Gmail API rate limited on ${senderEmail} (retry ${retries}/3)`,
              createdAt: new Date(),
            },
            false
          );
          await sleep(retries * 1000);
          continue;
        }

        if (!listRes.ok) {
          errors.push(`List failed for ${senderEmail}: ${listRes.status}`);
          break;
        }

        retries = 0;
        const listData = await listRes.json();
        const messages: Array<{ id: string; threadId: string }> =
          listData.messages || [];
        pageToken = listData.nextPageToken;

        for (const msg of messages) {
          if (processedSet.has(msg.id)) continue;
          totalChecked++;

          // Check if this message's threadId matches a known sent thread
          const match = threadMap.get(msg.threadId);
          if (!match) continue;

          // Fetch full message to verify sender and extract reply body
          try {
            const fullRes = await fetch(
              `https://gmail.googleapis.com/gmail/v1/users/me/messages/${msg.id}?format=full`,
              { headers: { Authorization: `Bearer ${accessToken}` } }
            );

            if (!fullRes.ok) continue;
            const full = await fullRes.json();
            const headers: Array<{ name: string; value: string }> =
              full.payload?.headers || [];
            const fromHeader =
              headers.find((h) => h.name === "From")?.value || "";

            // Extract email from From header
            const fromMatch =
              fromHeader.match(/<([^>]+)>/) ||
              fromHeader.match(/([^\s]+@[^\s]+)/);
            const fromEmail = fromMatch
              ? fromMatch[1].toLowerCase().trim()
              : "";

            // Verify the sender is our contact
            if (fromEmail && fromEmail === match.recipient) {
              const snippet = extractPlainText(full.payload).slice(0, 2000);
              replyUpdates.push({
                email: fromEmail,
                campaignId: match.campaignId,
                gmailMsgId: msg.id,
                senderAccount: senderEmail,
                snippet,
              });
              processedSet.add(msg.id);
              logDebug("reply_detection.match_found", "info",
                `Reply from ${fromEmail} matched thread ${msg.threadId} → campaign ${match.campaignId}`,
                { campaignId: match.campaignId, contactEmail: fromEmail });
            } else {
              logDebug("reply_detection.sender_mismatch", "info",
                `Thread ${msg.threadId} matched but sender ${fromEmail} !== recipient ${match.recipient}`,
                { campaignId: match.campaignId });
            }
          } catch {
            // Skip messages that fail metadata fetch
          }
        }
      } catch (e: any) {
        errors.push(`Error polling ${senderEmail}: ${e?.message}`);
        break;
      }
    } while (pageToken);
  }

  // 6. Write replies to buffer
  const newIds = Array.from(processedSet).filter(
    (id) => !processedIds.includes(id)
  );

  for (const reply of replyUpdates) {
    const contactRef = adminFirestore
      .collection("recruitmentContacts")
      .doc(reply.email);
    pushWrite(
      buffer,
      contactRef,
      {
        [`campaignData.${reply.campaignId}.status`]: "replied",
        [`campaignData.${reply.campaignId}.replied`]: true,
        [`campaignData.${reply.campaignId}.repliedAt`]: new Date(),
        [`campaignData.${reply.campaignId}.openedAt`]: new Date(),
        [`campaignData.${reply.campaignId}.replySnippet`]: reply.snippet,
      },
      true
    );

    const activityRef = adminFirestore.collection("activityLog").doc();
    pushWrite(
      buffer,
      activityRef,
      {
        applicantEmail: reply.email,
        eventType: "replied",
        details: `Reply detected in ${reply.senderAccount} inbox`,
        createdAt: new Date(),
        campaignId: reply.campaignId,
      },
      false
    );
  }

  // Update config
  if (newIds.length > 0) {
    const configRef = adminFirestore.doc(REPLY_CONFIG_DOC);
      pushWrite(
        buffer,
        configRef,
        {
          lastCheckAt: Math.floor(Date.now() / 1000),
          processedReplyIds: FieldValue.arrayUnion(...newIds),
        },
        false
      );
  }

  return {
    accountsPolled: senderAccounts.length,
    totalChecked,
    totalReplied: replyUpdates.length,
    errors,
  };
}
