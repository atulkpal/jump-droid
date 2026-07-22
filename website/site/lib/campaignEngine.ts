import type { Firestore } from "firebase-admin/firestore";
import { FieldValue } from "firebase-admin/firestore";
import type { CampaignConfig, CampaignProcessResult, CampaignContactData } from "@/types/campaign";
import type { OutreachContact } from "@/types/recruitmentContacts";
import type { SenderProfile } from "@/types/senderProfile";
import {
  getGlobalCampaignConfigAdmin,
  getRunningCampaignsAdmin,
  getScheduledCampaignsAdmin,
  getCampaignAdmin,
  mergeCampaignConfig,
  fetchEligibleContactsForCampaignAdmin,
  hardDeleteExpiredSoftDeletesAdmin,
  setCampaignStatusAdmin,
} from "@/lib/firebase/campaignService";
import { logEventAdmin } from "@/lib/firebase/activityService";
import { renderTemplate } from "@/lib/emailTemplates";
import {
  getAccessTokenAdmin,
  buildMimeMessage,
  logEmailAdmin,
  getSenderProfileAdmin,
  getTrackingUrl,
} from "@/lib/emailService";
import { detectBounces } from "@/lib/firebase/bounceDetectionAdmin";
import { detectReplies } from "@/lib/gmailReplyDetection";
import { createWriteBuffer, pushWrite, flushWrites, type PendingWrite } from "@/lib/campaignProcessor";

const DEFAULT_SENDER: SenderProfile = {
  name: "Ashwath AI",
  email: "ashwathai.dev@gmail.com",
};

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// ── Process all running campaigns (called by cron) ──

export async function processAllCampaigns(
  adminFirestore: Firestore
): Promise<CampaignProcessResult[]> {
  const buffer = createWriteBuffer();
  const globalConfig = await getGlobalCampaignConfigAdmin(adminFirestore);

  // Shared operations (once per cron run)
  const replyResult = await detectReplies(adminFirestore, buffer);
  if (replyResult.errors.length > 0) {
    const errRef = adminFirestore.collection("activityLog").doc();
    pushWrite(
      buffer,
      errRef,
      {
        applicantEmail: "system",
        eventType: "system_error",
        details: `Reply detection errors: ${replyResult.errors.join("; ")}`,
        createdAt: new Date(),
      },
      false
    );
  }

  await detectBounces(adminFirestore);
  await hardDeleteExpiredSoftDeletesAdmin(adminFirestore);

  // Start scheduled campaigns whose time has come
  await activateScheduledCampaigns(adminFirestore, buffer);

  // Get all running campaigns
  const runningCampaigns = await getRunningCampaignsAdmin(adminFirestore);
  if (runningCampaigns.length === 0) {
    await flushWrites(buffer, adminFirestore);
    return [];
  }

  const results: CampaignProcessResult[] = [];

  for (const campaign of runningCampaigns) {
    const config = await mergeCampaignConfig(globalConfig, campaign);
    const result = await processSingleCampaign(adminFirestore, campaign.id, config, buffer);
    results.push(result);
  }

  await flushWrites(buffer, adminFirestore);

  // Update lastDetectedAt for all running campaigns
  for (const campaign of runningCampaigns) {
    pushWrite(
      buffer,
      adminFirestore.collection("campaigns").doc(campaign.id),
      { lastDetectedAt: new Date() },
      true
    );
  }

  await flushWrites(buffer, adminFirestore);

  // Log email run completed for each campaign
  for (const r of results) {
    pushWrite(
      buffer,
      adminFirestore.collection("activityLog").doc(),
      {
        applicantEmail: `campaign:${r.campaignId}`,
        eventType: "email_run_completed",
        details: `Cron run: ${r.processed} processed, ${r.sent} sent, ${r.failed} failed, ${r.converted} converted, ${r.noResponse} no_response`,
        createdAt: new Date(),
        campaignId: r.campaignId,
      },
      false
    );
  }

  await flushWrites(buffer, adminFirestore);

  return results;
}

// ── Process one campaign ──

async function processSingleCampaign(
  adminFirestore: Firestore,
  campaignId: string,
  config: CampaignConfig,
  buffer: PendingWrite[]
): Promise<CampaignProcessResult> {
  const senderAccountId = config.senderAccountId || undefined;
  let sender = await getSenderProfileAdmin(adminFirestore, senderAccountId);

  const result: CampaignProcessResult = {
    campaignId,
    processed: 0,
    sent: 0,
    failed: 0,
    converted: 0,
    noResponse: 0,
  };

  let eligibleContacts = await fetchEligibleContactsForCampaignAdmin(
    adminFirestore,
    campaignId,
    config.maxInvites
  );

  if (eligibleContacts.length === 0) return result;

  let emailsSentThisHour = 0;

  for (const contact of eligibleContacts) {
    if (result.processed >= config.batchSize) break;
    if (emailsSentThisHour >= config.maxEmailsPerHour) break;

    result.processed++;

    const email = contact.email.toLowerCase().trim();
    const inviteNumber = (contact.campaignData?.[campaignId]?.inviteCount ?? 0) + 1;

    // Check if contact is unsubscribed (direct read — cached per-call, unavoidable)
    const contactDoc = await adminFirestore.collection("recruitmentContacts").doc(email).get();
    if (contactDoc.exists && contactDoc.data()!.status === "unsubscribed") {
      pushWrite(
        buffer,
        adminFirestore.collection("activityLog").doc(),
        { applicantEmail: email, eventType: "skipped_unsubscribed", details: "Contact unsubscribed — skipped", createdAt: new Date(), campaignId },
        false
      );
      continue;
    }

    // Check if contact replied or converted (skip further invites)
    const cdStatus = contactDoc.data()?.campaignData?.[campaignId]?.status;
    if (contactDoc.exists && (cdStatus === "replied" || cdStatus === "converted" || contactDoc.data()!.campaignData?.[campaignId]?.replied)) {
      continue;
    }

    // Check if contact became an applicant (with attribution)
    const applicantSource = await checkApplicant(adminFirestore, email);
    if (applicantSource !== null) {
      const isAttributedToMe = !applicantSource || applicantSource === campaignId;
      if (isAttributedToMe) {
        pushWrite(
          buffer,
          adminFirestore.collection("recruitmentContacts").doc(email),
          { status: "converted", stoppedReason: "registered", [`campaignData.${campaignId}.status`]: "converted", [`campaignData.${campaignId}.stoppedReason`]: "registered" },
          true
        );
        pushWrite(
          buffer,
          adminFirestore.collection("activityLog").doc(),
          { applicantEmail: email, eventType: "contact_converted", details: "Registered as applicant during campaign", createdAt: new Date(), campaignId },
          false
        );
        result.converted++;
        continue;
      }
    }

    if (inviteNumber > config.maxInvites) {
      pushWrite(
        buffer,
        adminFirestore.collection("recruitmentContacts").doc(email),
        { status: "no_response", stoppedReason: "max_invites_reached" },
        true
      );
      pushWrite(
        buffer,
        adminFirestore.collection("recruitmentContacts").doc(email),
        { [`campaignData.${campaignId}.status`]: "no_response", [`campaignData.${campaignId}.stoppedReason`]: "max_invites_reached", [`campaignData.${campaignId}.emailStatus`]: "failed" },
        true
      );
      pushWrite(
        buffer,
        adminFirestore.collection("activityLog").doc(),
        { applicantEmail: email, eventType: "contact_no_response", details: `Max invites (${config.maxInvites}) reached`, createdAt: new Date(), campaignId },
        false
      );
      result.noResponse++;
      continue;
    }

    try {
      // Determine assigned sender (sender consistency)
      const cdSnap = await adminFirestore.collection("recruitmentContacts").doc(email).get();
      const cd: CampaignContactData | undefined = cdSnap.data()?.campaignData?.[campaignId];
      let assignedSender = cd?.assignedSender ?? null;
      let currentSender: SenderProfile = sender;

      if (assignedSender) {
        try {
          currentSender = await getSenderProfileAdmin(adminFirestore, assignedSender);
        } catch {
          assignedSender = null;
          currentSender = sender;
        }
      }

      if (!assignedSender) {
        currentSender = sender;
        assignedSender = currentSender.email;
        pushWrite(
          buffer,
          adminFirestore.collection("recruitmentContacts").doc(email),
          {
            [`campaignData.${campaignId}.assignedSender`]: assignedSender,
            [`campaignData.${campaignId}.currentStep`]: inviteNumber,
            [`campaignData.${campaignId}.startedAt`]: { seconds: Math.floor(Date.now() / 1000) },
          },
          true
        );
      }

      // Build tracking pixel
      const trackingUrl = getTrackingUrl(email, campaignId);
      const trackingPixel = `<img src="${trackingUrl}" width="1" height="1" alt="" style="display:none" />`;

      // Render template
      const templateName = config.templateSequence?.[inviteNumber - 1];
      if (!templateName) continue;
      const overrideDoc = templateName === "invitation" ? `invitation-${inviteNumber}` : templateName;
      const overrideSnap = await adminFirestore
        .collection("emailTemplateOverrides")
        .doc(overrideDoc)
        .get();
      let html: string;
      let subject: string;
      let text: string;

      const unsubscribeUrl = `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/api/unsubscribe?email=${encodeURIComponent(email)}`;
      const betaInfoUrl = campaignId
        ? `https://jump-droid.vercel.app/beta-info?c=${campaignId}`
        : "https://jump-droid.vercel.app/beta-info";

      if (overrideSnap.exists) {
        const data = overrideSnap.data()!;
        subject = data.subject || "";
        html = (data.htmlBody || "")
          .replace(/\{\{name\}\}/g, contact.name || contact.email)
          .replace(/\$\{name\}/g, contact.name || contact.email)
          .replace(/\{betaInfoUrl\}/g, betaInfoUrl);
        html = html.replace(
          /(<a\s+[^>]*?href\s*=\s*")([^"]*beta-info[^"]*)(")/gi,
          (match, before, url, after) => {
            if (/[?&]c=/.test(url)) return match;
            const sep = url.includes("?") ? "&" : "?";
            const hashIx = url.indexOf("#");
            if (hashIx >= 0) {
              url = url.slice(0, hashIx) + sep + "c=" + campaignId + url.slice(hashIx);
            } else {
              url += sep + "c=" + campaignId;
            }
            return before + url + after;
          }
        );
        html += trackingPixel;
        html += `<br><p style="font-size:12px;color:#888;text-align:center"><a href="${unsubscribeUrl}" style="color:#888">Unsubscribe</a></p>`;
        text = html.replace(/<[^>]*>/g, "").replace(/\s+/g, " ").trim();
      } else {
        const rendered = renderTemplate(
          templateName as any,
          contact.name || contact.email,
          inviteNumber,
          unsubscribeUrl,
          trackingPixel,
          campaignId
        );
        html = rendered.html;
        subject = rendered.subject;
        text = rendered.text;
      }

      const sentResult = await sendEmailViaGmail(
        adminFirestore,
        email,
        contact.name || "",
        html,
        text,
        subject,
        "invitation",
        campaignId,
        currentSender,
        assignedSender || senderAccountId
      );

      if (sentResult.success) {
        const now = Math.floor(Date.now() / 1000);
        pushWrite(
          buffer,
          adminFirestore.collection("recruitmentContacts").doc(email),
          {
            [`campaignData.${campaignId}.inviteCount`]: inviteNumber,
            [`campaignData.${campaignId}.lastInviteAt`]: { seconds: now },
            [`campaignData.${campaignId}.nextEligibleAt`]: { seconds: now + config.delayDays * 24 * 60 * 60 },
            [`campaignData.${campaignId}.emailStatus`]: "sent",
            [`campaignData.${campaignId}.currentStep`]: inviteNumber,
            [`campaignData.${campaignId}.stoppedReason`]: "",
            [`campaignData.${campaignId}.status`]: "invited",
          },
          true
        );
        pushWrite(
          buffer,
          adminFirestore.collection("activityLog").doc(),
          { applicantEmail: email, eventType: "invitation_sent", details: `Invitation #${inviteNumber} sent`, createdAt: new Date(), campaignId },
          false
        );
        // Top-level tracking
        pushWrite(
          buffer,
          adminFirestore.collection("recruitmentContacts").doc(email),
          { inviteCount: FieldValue.increment(1), lastInviteAt: { seconds: now } },
          true
        );
        result.sent++;
      } else {
        // If assigned sender failed, try fallback to default sender
        if (assignedSender && assignedSender !== DEFAULT_SENDER.email && senderAccountId !== DEFAULT_SENDER.email) {
          try {
            const fallbackResult = await sendEmailViaGmail(
              adminFirestore,
              email,
              contact.name || "",
              html,
              text,
              subject,
              "invitation",
              campaignId,
              DEFAULT_SENDER,
              DEFAULT_SENDER.email
            );

            if (fallbackResult.success) {
              const now = Math.floor(Date.now() / 1000);
              pushWrite(
                buffer,
                adminFirestore.collection("recruitmentContacts").doc(email),
                {
                  [`campaignData.${campaignId}.inviteCount`]: inviteNumber,
                  [`campaignData.${campaignId}.lastInviteAt`]: { seconds: now },
                  [`campaignData.${campaignId}.nextEligibleAt`]: { seconds: now + config.delayDays * 24 * 60 * 60 },
                  [`campaignData.${campaignId}.emailStatus`]: "sent",
                  [`campaignData.${campaignId}.currentStep`]: inviteNumber,
                  [`campaignData.${campaignId}.assignedSender`]: DEFAULT_SENDER.email,
                  [`campaignData.${campaignId}.stoppedReason`]: "",
                  [`campaignData.${campaignId}.status`]: "invited",
                },
                true
              );
              pushWrite(
                buffer,
                adminFirestore.collection("activityLog").doc(),
                { applicantEmail: email, eventType: "invitation_sent", details: `Invitation #${inviteNumber} sent (via fallback sender)`, createdAt: new Date(), campaignId },
                false
              );
              pushWrite(
                buffer,
                adminFirestore.collection("activityLog").doc(),
                { applicantEmail: email, eventType: "sender_fallback", details: `Fallback from ${assignedSender} to ${DEFAULT_SENDER.email}`, createdAt: new Date(), campaignId },
                false
              );
              result.sent++;
              continue;
            }
          } catch {
            // Fallback also failed, mark as failed
          }
        }

        pushWrite(
          buffer,
          adminFirestore.collection("recruitmentContacts").doc(email),
          {
            [`campaignData.${campaignId}.emailStatus`]: "failed",
            [`campaignData.${campaignId}.stoppedReason`]: sentResult.error || "Send failed",
            [`campaignData.${campaignId}.status`]: "failed",
          },
          true
        );
        pushWrite(
          buffer,
          adminFirestore.collection("activityLog").doc(),
          { applicantEmail: email, eventType: "invitation_failed", details: `Invitation #${inviteNumber} failed: ${sentResult.error}`, createdAt: new Date(), campaignId },
          false
        );
        result.failed++;
      }
    } catch (e: any) {
      pushWrite(
        buffer,
        adminFirestore.collection("recruitmentContacts").doc(email),
        {
          [`campaignData.${campaignId}.emailStatus`]: "failed",
          [`campaignData.${campaignId}.stoppedReason`]: e?.message || "Unknown error",
          [`campaignData.${campaignId}.status`]: "failed",
        },
        true
      );
      pushWrite(
        buffer,
        adminFirestore.collection("activityLog").doc(),
        { applicantEmail: email, eventType: "invitation_failed", details: `Invitation #${inviteNumber} failed: ${e?.message}`, createdAt: new Date(), campaignId },
        false
      );
      result.failed++;
    }

    emailsSentThisHour++;

    if (result.processed < eligibleContacts.length && emailsSentThisHour < config.maxEmailsPerHour) {
      await sleep(config.delayBetweenEmailsMs);
    }
  }

  // Update campaign stats (push to buffer)
  const campaignRef = adminFirestore.collection("campaigns").doc(campaignId);
  pushWrite(
    buffer,
    campaignRef,
    {
      "stats.emailsSent": FieldValue.increment(result.sent),
      "stats.errors": FieldValue.increment(result.failed),
      "stats.converted": FieldValue.increment(result.converted),
      "stats.noResponse": FieldValue.increment(result.noResponse),
      updatedAt: new Date(),
    },
    true
  );

  return result;
}

// ── Activate scheduled campaigns whose time has come ──

async function activateScheduledCampaigns(
  adminFirestore: Firestore,
  buffer: PendingWrite[]
): Promise<void> {
  const now = Date.now() / 1000;
  const scheduled = await getScheduledCampaignsAdmin(adminFirestore);

  for (const campaign of scheduled) {
    if (!campaign.scheduledStartAt) continue;
    const scheduledSeconds = campaign.scheduledStartAt.seconds ?? 0;
    if (scheduledSeconds <= now) {
      pushWrite(
        buffer,
        adminFirestore.collection("campaigns").doc(campaign.id),
        { status: "running", startedAt: new Date(), updatedAt: new Date() },
        true
      );
      pushWrite(
        buffer,
        adminFirestore.collection("activityLog").doc(),
        {
          applicantEmail: `campaign:${campaign.id}`,
          eventType: "campaign_started",
          details: `Campaign "${campaign.name}" started automatically`,
          createdAt: new Date(),
          campaignId: campaign.id,
        },
        false
      );
    }
  }
}

// ── Legacy single-campaign export (backward compat) ──

export async function processCampaign(
  adminFirestore: Firestore
): Promise<CampaignProcessResult> {
  const results = await processAllCampaigns(adminFirestore);
  if (results.length === 0) {
    return { campaignId: "", processed: 0, sent: 0, failed: 0, converted: 0, noResponse: 0 };
  }
  // Aggregate all results
  return results.reduce(
    (acc, r) => ({
      campaignId: "all",
      processed: acc.processed + r.processed,
      sent: acc.sent + r.sent,
      failed: acc.failed + r.failed,
      converted: acc.converted + r.converted,
      noResponse: acc.noResponse + r.noResponse,
    }),
    { campaignId: "all", processed: 0, sent: 0, failed: 0, converted: 0, noResponse: 0 }
  );
}

async function checkApplicant(
  adminFirestore: Firestore,
  email: string
): Promise<string | null> {
  try {
    const snap = await adminFirestore.collection("betaUsers").doc(email).get();
    if (!snap.exists) return null;
    return snap.data()?.convertedFromCampaign || null;
  } catch {
    return null;
  }
}

async function sendEmailViaGmail(
  adminFirestore: Firestore,
  to: string,
  name: string,
  html: string,
  text: string,
  subject: string,
  template: string,
  campaign: string,
  sender: SenderProfile,
  senderAccountId?: string
): Promise<{ success: boolean; error?: string; gmailMessageId?: string; gmailThreadId?: string }> {
  try {
    let accessToken: string;
    try {
      accessToken = await getAccessTokenAdmin(adminFirestore, senderAccountId);
    } catch (e: any) {
      // If sender account fails, try default
      if (senderAccountId && senderAccountId !== DEFAULT_SENDER.email) {
        accessToken = await getAccessTokenAdmin(adminFirestore, DEFAULT_SENDER.email);
      } else {
        throw e;
      }
    }

    const { raw } = buildMimeMessage(to, name, subject, html, text, sender);

    const sendRes = await fetch(
      "https://gmail.googleapis.com/gmail/v1/users/me/messages/send",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ raw }),
      }
    );

    const body = await sendRes.text();

    if (!sendRes.ok) {
      await logEmailAdmin(adminFirestore, to, name, template, campaign, "campaign", "failed", "", "", body);
      return { success: false, error: body };
    }

    let messageId = "";
    let threadId = "";
    try {
      const parsed = JSON.parse(body);
      messageId = parsed.id || "";
      threadId = parsed.threadId || "";
    } catch {
      // Non-critical parse failure
    }

    await logEmailAdmin(adminFirestore, to, name, template, campaign, "campaign", "sent", messageId, threadId, "");

    const { logEmailAuditAdmin } = await import("@/lib/firebase/auditService");
    await logEmailAuditAdmin(adminFirestore, "send_success", sender.email, `Campaign email sent to ${to}`, "system");

    return { success: true, gmailMessageId: messageId, gmailThreadId: threadId };
  } catch (e: any) {
    await logEmailAdmin(adminFirestore, to, name, template, campaign, "campaign", "failed", "", "", e?.message || "");

    const { logEmailAuditAdmin } = await import("@/lib/firebase/auditService");
    await logEmailAuditAdmin(adminFirestore, "send_failed", sender.email, `Campaign email to ${to} failed: ${e?.message}`, "system");

    return { success: false, error: e?.message };
  }
}
