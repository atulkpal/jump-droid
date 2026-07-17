import { NextResponse } from "next/server";
import type { SenderProfile } from "@/types/senderProfile";
import { getAccessToken, buildMimeMessage, logEmail, getSenderProfile } from "@/lib/emailService";

function buildPlainInvitation(to: string, name: string): { body: string; subject: string } {
  const body = [
    `Hi ${name || "there"},`,
    "",
    "You're invited to join the Jump Droid Beta Program!",
    "",
    "Jump Droid is a free, open-source Android arcade game. Touch to thrust. Manage fuel, heat, and shield as you climb through 8 atmospheric zones.",
    "",
    "As a beta tester, you'll get:",
    "- Early access to Jump Droid before public release",
    "- The opportunity to help shape development with your feedback",
    "- Your name permanently listed in the game credits",
    "- Optional rewards (30 minutes of gameplay per day qualifies)",
    "- Optional Code Jam participation",
    "",
    "Sign up here: https://jump-droid.vercel.app/beta-info",
    "",
    "Thank you for helping make Jump Droid better!",
    "",
    "\u2014 Ashwath AI",
  ].join("\r\n");
  return { body, subject: "You're Invited to Join the Jump Droid Beta Program!" };
}

function buildPlainWelcome(to: string, name: string): { body: string; subject: string } {
  const body = [
    `Hi ${name || "there"},`,
    "",
    "Welcome to the Jump Droid Beta Program!",
    "",
    "You've been approved as a beta tester. Here's what to do next:",
    "",
    "1. Opt-in to the beta on Google Play:",
    "   https://play.google.com/apps/testing/com.ashwathai.jump_droid",
    "",
    "2. Download and install Jump Droid from the Play Store:",
    "   https://play.google.com/store/apps/details?id=com.ashwathai.jump_droid",
    "",
    "3. Play at least 30 minutes per day to qualify for rewards.",
    "4. Share your feedback via email or Discord.",
    "",
    "As a beta tester, your name will be listed in the game credits.",
    "",
    "If you signed up for the Code Jam, we'll reach out with details soon.",
    "",
    "Thank you for helping make Jump Droid better!",
    "",
    "\u2014 Ashwath AI",
  ].join("\r\n");
  return { body, subject: "Welcome to the Jump Droid Beta Program!" };
}

function buildPlainAcknowledgment(to: string, name: string): { body: string; subject: string } {
  const body = [
    `Hi ${name || "there"},`,
    "",
    "Thank you for applying to the Jump Droid Beta Program.",
    "",
    "We have received your application and will review it within the next 48 hours.",
    "",
    "If your application is approved, you will receive a welcome email with instructions on how to get started.",
    "",
    "In the meantime, feel free to explore more about Jump Droid at https://jump-droid.vercel.app",
    "",
    "\u2014 Ashwath AI",
  ].join("\r\n");
  return { body, subject: "Application Received - Jump Droid Beta" };
}

function getPlainTextTemplate(to: string, name: string, template: string): { body: string; subject: string } {
  switch (template) {
    case "welcome":
      return buildPlainWelcome(to, name);
    case "acknowledgement":
      return buildPlainAcknowledgment(to, name);
    case "invitation":
    default:
      return buildPlainInvitation(to, name);
  }
}

export async function POST(req: Request) {
  try {
    const { emails, template, campaign, source, htmlBody, sender: reqSender } = await req.json();
    if (!emails || !Array.isArray(emails) || emails.length === 0) {
      return NextResponse.json({ error: "No recipients provided" }, { status: 400 });
    }

    const emailTemplate = template || "invitation";
    const sender: SenderProfile = reqSender || await getSenderProfile();
    const accessToken = await getAccessToken();
    const results: {
      email: string;
      success: boolean;
      error?: string;
      gmailMessageId?: string;
      gmailThreadId?: string;
    }[] = [];

    for (const item of emails) {
      try {
        const plainText = getPlainTextTemplate(item.email, item.name || "", emailTemplate);

        let raw: string;
        if (htmlBody) {
          const mime = buildMimeMessage(
            item.email,
            item.name || "",
            plainText.subject,
            htmlBody,
            plainText.body,
            sender
          );
          raw = mime.raw;
        } else {
          const subjectEncoded = Buffer.from(plainText.subject)
            .toString("base64")
            .replace(/=/g, "");
          const headers = [
            `From: ${sender.name} <${sender.email}>`,
            `To: ${item.email}`,
            `Subject: =?UTF-8?B?${subjectEncoded}?=`,
            "MIME-Version: 1.0",
            "Content-Type: text/plain; charset=UTF-8",
            "Content-Transfer-Encoding: 7bit",
            "",
            plainText.body,
          ].join("\r\n");
          raw = Buffer.from(headers).toString("base64url");
        }

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
          results.push({ email: item.email, success: false, error: body });
          await logEmail(
            item.email,
            item.name || "",
            emailTemplate,
            campaign || "",
            source || "",
            "failed",
            "",
            "",
            body
          );
        } else {
          let messageId = "";
          let threadId = "";
          try {
            const parsed = JSON.parse(body);
            messageId = parsed.id || "";
            threadId = parsed.threadId || "";
          } catch {
            // ignore parse errors
          }
          results.push({
            email: item.email,
            success: true,
            gmailMessageId: messageId,
            gmailThreadId: threadId,
          });
          await logEmail(
            item.email,
            item.name || "",
            emailTemplate,
            campaign || "",
            source || "",
            "sent",
            messageId,
            threadId,
            ""
          );
        }
      } catch (e: any) {
        results.push({ email: item.email, success: false, error: e?.message });
        await logEmail(
          item.email,
          item.name || "",
          emailTemplate,
          campaign || "",
          source || "",
          "failed",
          "",
          "",
          e?.message || ""
        );
      }
    }

    return NextResponse.json(results);
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Send failed" }, { status: 500 });
  }
}
