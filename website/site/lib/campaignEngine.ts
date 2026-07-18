import type { CampaignConfig, CampaignProcessResult } from "@/types/campaign";
import type { OutreachContact } from "@/types/recruitmentContacts";
import type { SenderProfile } from "@/types/senderProfile";
import { getCampaignConfig, fetchEligibleContacts, markConverted, markNoResponse, markEmailFailed, incrementInviteCount } from "@/lib/firebase/campaignService";
import { logEvent } from "@/lib/firebase/activityService";
import { renderTemplate } from "@/lib/emailTemplates";
import { getAccessToken, buildMimeMessage, logEmail, getSenderProfile, detectBounces } from "@/lib/emailService";

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function processCampaign(): Promise<CampaignProcessResult> {
  const config = await getCampaignConfig();

  const senderAccountId = config.senderAccountId || undefined;
  const [sender] = await Promise.all([
    getSenderProfile(senderAccountId),
  ]);

  await detectBounces();

  const result: CampaignProcessResult = {
    processed: 0,
    sent: 0,
    failed: 0,
    converted: 0,
    noResponse: 0,
  };

  let eligibleContacts = await fetchEligibleContacts(config.maxInvites);

  if (eligibleContacts.length === 0) return result;

  let emailsSentThisHour = 0;

  for (const contact of eligibleContacts) {
    if (result.processed >= config.batchSize) break;
    if (emailsSentThisHour >= config.maxEmailsPerHour) break;

    result.processed++;

    const email = contact.email.toLowerCase().trim();
    const inviteNumber = (contact.inviteCount || 0) + 1;

    const applicantExists = await checkApplicant(email);
    if (applicantExists) {
      await markConverted(email);
      await logEvent(email, "contact_converted", "Registered as applicant during campaign");
      result.converted++;
      continue;
    }

    if (inviteNumber > config.maxInvites) {
      await markNoResponse(email);
      await logEvent(email, "contact_no_response", `Max invites (${config.maxInvites}) reached`);
      result.noResponse++;
      continue;
    }

    try {
      const { html, subject, text } = renderTemplate(
        "invitation",
        contact.name || contact.email,
        inviteNumber
      );

      const sentResult = await sendEmailViaGmail(email, contact.name || "", html, text, subject, "invitation", contact.campaignId || "default", sender, senderAccountId);

      if (sentResult.success) {
        await incrementInviteCount(email, config.delayDays);
        await logEvent(email, "invitation_sent", `Invitation #${inviteNumber} sent`);
        result.sent++;
      } else {
        await markEmailFailed(email, sentResult.error || "Send failed");
        await logEvent(email, "invitation_failed", `Invitation #${inviteNumber} failed: ${sentResult.error}`);
        result.failed++;
      }
    } catch (e: any) {
      await markEmailFailed(email, e?.message || "Unknown error");
      await logEvent(email, "invitation_failed", `Invitation #${inviteNumber} failed: ${e?.message}`);
      result.failed++;
    }

    emailsSentThisHour++;

    if (result.processed < eligibleContacts.length && emailsSentThisHour < config.maxEmailsPerHour) {
      await sleep(config.delayBetweenEmailsMs);
    }
  }

  return result;
}

async function checkApplicant(email: string): Promise<boolean> {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc } = await import("firebase/firestore");
    const snap = await getDoc(doc(firestore, "betaUsers", email));
    return snap.exists();
  } catch {
    return false;
  }
}

async function sendEmailViaGmail(
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
    const accessToken = await getAccessToken(senderAccountId);

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
      await logEmail(to, name, template, campaign, "campaign", "failed", "", "", body);
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

    await logEmail(to, name, template, campaign, "campaign", "sent", messageId, threadId, "");

    const { logEmailAudit } = await import("@/lib/firebase/auditService");
    await logEmailAudit("send_success", sender.email, `Campaign email sent to ${to}`, "system");

    return { success: true, gmailMessageId: messageId, gmailThreadId: threadId };
  } catch (e: any) {
    await logEmail(to, name, template, campaign, "campaign", "failed", "", "", e?.message || "");

    const { logEmailAudit } = await import("@/lib/firebase/auditService");
    await logEmailAudit("send_failed", sender.email, `Campaign email to ${to} failed: ${e?.message}`, "system");

    return { success: false, error: e?.message };
  }
}
