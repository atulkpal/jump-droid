import { NextRequest } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import {
  getGlobalCampaignConfigAdmin,
  getCampaignAdmin,
  mergeCampaignConfig,
  updateCampaignContactDataAdmin,
} from "@/lib/firebase/campaignService";
import {
  getAccessTokenAdmin,
  buildMimeMessage,
  logEmailAdmin,
  getSenderProfileAdmin,
  getTrackingUrl,
} from "@/lib/emailService";
import { logEventAdmin } from "@/lib/firebase/activityService";
import { renderTemplate } from "@/lib/emailTemplates";
import type { CampaignContactData } from "@/types/campaign";
import type { SenderProfile } from "@/types/senderProfile";

const DEFAULT_SENDER: SenderProfile = {
  name: "Ashwath AI",
  email: "ashwathai.dev@gmail.com",
};

const GMAIL_API_TIMEOUT = 15000;
const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

async function fetchGmail(url: string, options: RequestInit): Promise<Response> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), GMAIL_API_TIMEOUT);
  try {
    return await fetch(url, { ...options, signal: controller.signal });
  } finally {
    clearTimeout(timeoutId);
  }
}

export async function POST(req: NextRequest) {
  try {
    const { campaignId, emails } = await req.json();
    if (!campaignId || !emails || !Array.isArray(emails) || emails.length === 0) {
      return new Response(JSON.stringify({ error: "campaignId and emails array are required" }), {
        status: 400, headers: { "Content-Type": "application/json" },
      });
    }

    const adminFirestore = getAdminFirestore();
    const campaignDoc = await adminFirestore.collection("campaigns").doc(campaignId).get();
    if (!campaignDoc.exists) {
      return new Response(JSON.stringify({ error: "Campaign not found" }), {
        status: 404, headers: { "Content-Type": "application/json" },
      });
    }

    await adminFirestore.collection("campaigns").doc(campaignId).update({
      "manualSendOverride.active": true,
      "manualSendOverride.startedAt": { seconds: Math.floor(Date.now() / 1000) },
    });

    const globalConfig = await getGlobalCampaignConfigAdmin(adminFirestore);
    const campaign = await getCampaignAdmin(adminFirestore, campaignId);
    const config = await mergeCampaignConfig(globalConfig, campaign);
    config.delayBetweenEmailsMs = Math.max(1000, Math.floor(config.delayBetweenEmailsMs / 2));
    if (!config.templateSequence?.length) {
      config.templateSequence = ["outreach-1", "outreach-2", "outreach-3", "outreach-4", "outreach-5"];
    }

    const senderAccountId = config.senderAccountId || undefined;
    const sender = await getSenderProfileAdmin(adminFirestore, senderAccountId);

    const stream = new ReadableStream({
      async start(controller) {
        const encoder = new TextEncoder();
        const write = (data: any) => {
          try { controller.enqueue(encoder.encode(JSON.stringify(data) + "\n")); } catch {}
        };

        const results: { email: string; success: boolean; inviteNumber: number; error?: string }[] = [];
        let emailsSentThisHour = 0;
        const total = emails.length;

        for (let i = 0; i < total; i++) {
          try {
            const overrideCheck = await adminFirestore.collection("campaigns").doc(campaignId).get();
            if (!overrideCheck.data()?.manualSendOverride?.active) break;
          } catch { break; }

          if (emailsSentThisHour >= config.maxEmailsPerHour) break;

          const key = emails[i].toLowerCase().trim();
          let displayName = key;

          try {
            const contactSnap = await adminFirestore.collection("recruitmentContacts").doc(key).get();
            if (!contactSnap.exists) {
              results.push({ email: key, success: false, inviteNumber: 0, error: "Contact not found" });
              write({ type: "progress", email: key, displayName: key, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
              continue;
            }

            const contactData = contactSnap.data()!;
            displayName = contactData.name || key;

            if (contactData.status === "unsubscribed") {
              results.push({ email: key, success: false, inviteNumber: 0, error: "Unsubscribed" });
              write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
              continue;
            }
            if (contactData.campaignData?.[campaignId]?.replied) {
              results.push({ email: key, success: false, inviteNumber: 0, error: "Contact already replied" });
              write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
              continue;
            }

            const cd: CampaignContactData | undefined = contactData.campaignData?.[campaignId];
            const inviteNumber = (cd?.inviteCount ?? 0) + 1;

            if (inviteNumber > config.maxInvites) {
              results.push({ email: key, success: false, inviteNumber, error: "Max invites reached" });
              write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
              continue;
            }

            const applicantSnap = await adminFirestore.collection("betaUsers").doc(key).get();
            if (applicantSnap.exists) {
              const appStatus = applicantSnap.data()?.status;
              if (appStatus === "approved" || appStatus === "active") {
                const convertedFrom = applicantSnap.data()?.convertedFromCampaign || null;
                if (!convertedFrom || convertedFrom === campaignId) {
                  await adminFirestore.collection("recruitmentContacts").doc(key).update({ status: "converted", [`campaignData.${campaignId}.status`]: "converted" });
                }
                results.push({ email: key, success: false, inviteNumber, error: "Contact already approved/active" });
                write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
                continue;
              }
            }

            // Check template is configured for this step
            if (!config.templateSequence?.[inviteNumber - 1]) {
              results.push({ email: key, success: false, inviteNumber, error: "No template configured for this step" });
              write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
              continue;
            }

            try {
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
                await updateCampaignContactDataAdmin(adminFirestore, key, campaignId, {
                  assignedSender, currentStep: inviteNumber, startedAt: { seconds: Math.floor(Date.now() / 1000) },
                });
              }

              const templateName = config.templateSequence[inviteNumber - 1]!;
              const overrideDoc = templateName === "invitation" ? `invitation-${inviteNumber}` : templateName;
              const templateOverrideSnap = await adminFirestore.collection("emailTemplateOverrides").doc(overrideDoc).get();

              const unsubscribeUrl = `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/api/unsubscribe?email=${encodeURIComponent(key)}`;
              const trackingUrl = getTrackingUrl(key, campaignId);
              const trackingPixel = `<img src="${trackingUrl}" width="1" height="1" alt="" style="display:none" />`;
              const betaInfoUrl = campaignId
                ? `https://jump-droid.vercel.app/beta-info?c=${campaignId}`
                : "https://jump-droid.vercel.app/beta-info";

              let html: string, subject: string, text: string;
              if (templateOverrideSnap.exists) {
                const d = templateOverrideSnap.data()!;
                subject = d.subject || "";
                html = (d.htmlBody || "")
                  .replace(/\{\{name\}\}/g, contactData.name || key)
                  .replace(/\$\{name\}/g, contactData.name || key)
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
                const rendered = renderTemplate(templateName as any, contactData.name || key, inviteNumber, unsubscribeUrl, trackingPixel, campaignId);
                html = rendered.html;
                subject = rendered.subject;
                text = rendered.text;
              }

              let accessToken: string;
              try {
                accessToken = await getAccessTokenAdmin(adminFirestore, assignedSender || senderAccountId);
              } catch (e: any) {
                if ((assignedSender || senderAccountId) && (assignedSender || senderAccountId) !== DEFAULT_SENDER.email) {
                  accessToken = await getAccessTokenAdmin(adminFirestore, DEFAULT_SENDER.email);
                } else { throw e; }
              }

              const mime = buildMimeMessage(key, contactData.name || "", subject, html, text, currentSender);
              const sendRes = await fetchGmail("https://gmail.googleapis.com/gmail/v1/users/me/messages/send", {
                method: "POST",
                headers: { Authorization: `Bearer ${accessToken}`, "Content-Type": "application/json" },
                body: JSON.stringify({ raw: mime.raw }),
              });

              const responseBody = await sendRes.text();

              if (!sendRes.ok) {
                if ((assignedSender || senderAccountId) && (assignedSender || senderAccountId) !== DEFAULT_SENDER.email) {
                  try {
                    const fallbackToken = await getAccessTokenAdmin(adminFirestore, DEFAULT_SENDER.email);
                    const fallbackMime = buildMimeMessage(key, contactData.name || "", subject, html, text, DEFAULT_SENDER);
                    const fallbackRes = await fetchGmail("https://gmail.googleapis.com/gmail/v1/users/me/messages/send", {
                      method: "POST",
                      headers: { Authorization: `Bearer ${fallbackToken}`, "Content-Type": "application/json" },
                      body: JSON.stringify({ raw: fallbackMime.raw }),
                    });
                    const fallbackBody = await fallbackRes.text();
                    if (fallbackRes.ok) {
                      let fbId = "", fbThreadId = "";
                      try { const p = JSON.parse(fallbackBody); fbId = p.id || ""; fbThreadId = p.threadId || ""; } catch {}
                      await updateCampaignContactDataAdmin(adminFirestore, key, campaignId, {
                        inviteCount: inviteNumber, lastInviteAt: { seconds: Math.floor(Date.now() / 1000) },
                        nextEligibleAt: { seconds: Math.floor(Date.now() / 1000) + config.delayDays * 24 * 60 * 60 },
                        emailStatus: "sent", currentStep: inviteNumber, assignedSender: DEFAULT_SENDER.email, stoppedReason: "",
                        status: "invited",
                      });
                      await logEventAdmin(adminFirestore, key, "manual_send_triggered", `Manual send #${inviteNumber} sent (via fallback sender)`, campaignId);
                      await logEventAdmin(adminFirestore, key, "sender_fallback", `Fallback from ${assignedSender} to ${DEFAULT_SENDER.email}`, campaignId);
                      await logEmailAdmin(adminFirestore, key, contactData.name || "", "invitation", campaignId, "manual_send", "sent", fbId, fbThreadId, "");
                      results.push({ email: key, success: true, inviteNumber });
                      emailsSentThisHour++;
                      write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
                      continue;
                    }
                  } catch {}
                }
                throw new Error(responseBody);
              }

              let gmailMessageId = "", gmailThreadId = "";
              try { const p = JSON.parse(responseBody); gmailMessageId = p.id || ""; gmailThreadId = p.threadId || ""; } catch {}

              await updateCampaignContactDataAdmin(adminFirestore, key, campaignId, {
                inviteCount: inviteNumber, lastInviteAt: { seconds: Math.floor(Date.now() / 1000) },
                nextEligibleAt: { seconds: Math.floor(Date.now() / 1000) + config.delayDays * 24 * 60 * 60 },
                emailStatus: "sent", currentStep: inviteNumber, stoppedReason: "",
                status: "invited",
              });
              await logEventAdmin(adminFirestore, key, "manual_send_triggered", `Manual send #${inviteNumber} triggered`, campaignId);
              await logEmailAdmin(adminFirestore, key, contactData.name || "", "invitation", campaignId, "manual_send", "sent", gmailMessageId, gmailThreadId, "");
              results.push({ email: key, success: true, inviteNumber });
              emailsSentThisHour++;
              write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
            } catch (e: any) {
              await updateCampaignContactDataAdmin(adminFirestore, key, campaignId, {
                emailStatus: "failed", stoppedReason: e?.message || "Send failed",
                status: "failed",
              });
              await logEventAdmin(adminFirestore, key, "manual_send_triggered", `Manual send #${inviteNumber} failed: ${e?.message}`, campaignId);
              results.push({ email: key, success: false, inviteNumber, error: e?.message });
              write({ type: "progress", email: key, displayName, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
            }
          } catch (e: any) {
            results.push({ email: key, success: false, inviteNumber: 0, error: e?.message || "Processing error" });
            write({ type: "progress", email: key, displayName: key, sent: results.filter(r => r.success).length, failed: results.filter(r => !r.success).length, processed: results.length, total });
          }
        }

        try {
          await adminFirestore.collection("campaigns").doc(campaignId).update({ "manualSendOverride.active": false });
        } catch {}

        write({ type: "complete", results });
        try { controller.close(); } catch {}

        // Single cooldown delay after the entire batch (rate limiting protection)
        if (results.some((r) => r.success)) {
          await sleep(config.delayBetweenEmailsMs);
        }
      },
    });

    return new Response(stream, {
      headers: { "Content-Type": "application/x-ndjson" },
    });
  } catch (e: any) {
    return new Response(JSON.stringify({ error: e?.message ?? "Send-now failed" }), {
      status: 500, headers: { "Content-Type": "application/json" },
    });
  }
}
