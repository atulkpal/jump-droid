import { NextResponse } from "next/server";
import type { SenderProfile } from "@/types/senderProfile";
import { buildMimeMessage } from "@/lib/emailService";
import { getAccessTokenAdmin, getSenderProfileAdmin, logEmailAdmin } from "@/lib/emailService";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { renderTemplate } from "@/lib/emailTemplates";
import type { EmailTemplate } from "@/types/emailLog";

export async function POST(req: Request) {
  try {
    const { emails, template, campaign, source, senderAccountId, subject: customSubject, htmlBody: customHtml } = await req.json();
    if (!emails || !Array.isArray(emails) || emails.length === 0) {
      return NextResponse.json({ error: "No recipients provided" }, { status: 400 });
    }
    if (!template || !template.trim()) {
      return NextResponse.json({ error: "No template specified" }, { status: 400 });
    }

    const adminFirestore = getAdminFirestore();
    const emailTemplate: EmailTemplate = template;
    const sender: SenderProfile = await getSenderProfileAdmin(adminFirestore, senderAccountId);
    const accessToken = await getAccessTokenAdmin(adminFirestore, senderAccountId);
    const results: {
      email: string;
      success: boolean;
      error?: string;
      gmailMessageId?: string;
      gmailThreadId?: string;
    }[] = [];

    // Check for template overrides
    let overrideSubject: string | null = null;
    let overrideHtml: string | null = null;
    try {
      const overrideSnap = await adminFirestore.collection("emailTemplateOverrides").doc(emailTemplate).get();
      if (overrideSnap.exists) {
        const d = overrideSnap.data()!;
        if (d.subject) overrideSubject = d.subject;
        if (d.htmlBody) overrideHtml = d.htmlBody;
      }
    } catch {
      // ignore override lookup failures
    }

    for (const item of emails) {
      try {
        let subject: string;
        let html: string;
        let text: string;

        const recipientName = item.name || "";
        if (customHtml) {
          subject = customSubject?.replace(/\{\{name\}\}/gi, recipientName).replace(/\$\{name\}/gi, recipientName) || "";
          html = customHtml.replace(/\{\{name\}\}/gi, recipientName).replace(/\$\{name\}/gi, recipientName);
          text = html.replace(/<[^>]*>/g, "").replace(/\s+/g, " ").trim();
        } else if (overrideHtml) {
          subject = overrideSubject?.replace(/\{\{name\}\}/gi, recipientName).replace(/\$\{name\}/gi, recipientName) || "";
          html = overrideHtml.replace(/\{\{name\}\}/gi, recipientName).replace(/\$\{name\}/gi, recipientName);
          text = html.replace(/<[^>]*>/g, "").replace(/\s+/g, " ").trim();
        } else {
          const unsubscribeUrl = `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/api/unsubscribe?email=${encodeURIComponent(item.email)}`;
          const rendered = renderTemplate(emailTemplate, item.name || "", undefined, unsubscribeUrl);
          subject = rendered.subject;
          html = rendered.html;
          text = rendered.text;
        }

        const mime = buildMimeMessage(
          item.email,
          item.name || "",
          subject,
          html,
          text,
          sender
        );

        const sendRes = await fetch(
          "https://gmail.googleapis.com/gmail/v1/users/me/messages/send",
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${accessToken}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ raw: mime.raw }),
          }
        );

        const responseBody = await sendRes.text();

        if (!sendRes.ok) {
          results.push({ email: item.email, success: false, error: responseBody });
          await logEmailAdmin(
            adminFirestore,
            item.email,
            item.name || "",
            emailTemplate,
            campaign || "",
            source || "",
            "failed",
            "",
            "",
            responseBody
          );
        } else {
          let messageId = "";
          let threadId = "";
          try {
            const parsed = JSON.parse(responseBody);
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
          await logEmailAdmin(
            adminFirestore,
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
        await logEmailAdmin(
          adminFirestore,
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
