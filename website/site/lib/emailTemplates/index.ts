import type { EmailTemplate } from "@/types/emailLog";
import { renderAcknowledgement } from "./acknowledgement";
import { renderWelcome } from "./welcome";
import { renderReject } from "./reject";
import { renderInvitation1 } from "./invitation-1";
import { renderInvitation2 } from "./invitation-2";
import { renderInvitation3 } from "./invitation-3";
import { renderInvitation4 } from "./invitation-4";
import { renderInvitation5 } from "./invitation-5";
import { renderOutreach1 } from "./outreach-1";
import { renderOutreach2 } from "./outreach-2";
import { renderOutreach3 } from "./outreach-3";
import { renderOutreach4 } from "./outreach-4";
import { renderOutreach5 } from "./outreach-5";

const INVITATION_MAP: Record<number, (name: string, unsubscribeUrl?: string) => { html: string; subject: string }> = {
  1: renderInvitation1,
  2: renderInvitation2,
  3: renderInvitation3,
  4: renderInvitation4,
  5: renderInvitation5,
};

const OUTREACH_MAP: Record<number, (name: string, unsubscribeUrl?: string) => { html: string; subject: string }> = {
  1: renderOutreach1,
  2: renderOutreach2,
  3: renderOutreach3,
  4: renderOutreach4,
  5: renderOutreach5,
};

export function renderTemplate(
  template: EmailTemplate,
  name: string,
  inviteNumber?: number,
  unsubscribeUrl?: string,
  trackingPixel?: string,
  campaignId?: string
): { html: string; subject: string; text: string } {
  let result: { html: string; subject: string };

  const renderWithUrl = (fn: (name: string, unsubscribeUrl?: string) => { html: string; subject: string }) =>
    fn(name, unsubscribeUrl);

  if (template === "acknowledgement") {
    result = renderWithUrl(renderAcknowledgement);
  } else if (template === "welcome") {
    result = renderWithUrl(renderWelcome);
  } else if (template === "reject") {
    result = renderWithUrl(renderReject);
  } else if (template === "invitation" || template.startsWith("invitation-") || template.startsWith("outreach-")) {
    if (template.startsWith("outreach-")) {
      const parsed = parseInt(template.split("-")[1], 10);
      const renderFn = OUTREACH_MAP[parsed];
      if (renderFn) {
        result = renderFn(name, unsubscribeUrl);
      } else {
        result = renderOutreach1(name, unsubscribeUrl);
      }
    } else {
      const num = inviteNumber || 1;
      const renderFn = INVITATION_MAP[num];
      if (renderFn) {
        result = renderFn(name, unsubscribeUrl);
      } else {
        result = renderInvitation1(name, unsubscribeUrl);
      }
    }
  } else {
    result = renderWithUrl(renderWelcome);
  }

  const betaInfoUrl = campaignId
    ? `https://jump-droid.vercel.app/beta-info?c=${campaignId}`
    : "https://jump-droid.vercel.app/beta-info";

  let html = result.html.replace("{trackingPixel}", trackingPixel || "").replace(/\{betaInfoUrl\}/g, betaInfoUrl);

  if (campaignId) {
    html = html.replace(
      /(<a\s+[^>]*?href\s*=\s*")([^"]+)(")/gi,
      (match, before, url, after) => {
        if (/^(mailto:|tel:|#|javascript:)/i.test(url)) return match;
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
  }

  return {
    html,
    subject: result.subject,
    text: stripHtml(html),
  };
}

function stripHtml(html: string): string {
  return html
    .replace(/<[^>]*>/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

export function getSubjectForInvite(inviteNumber: number): string {
  const subjects: Record<number, string> = {
    1: "You're Invited to Join the Jump Droid Beta Program!",
    2: "Reminder: Join the Jump Droid Beta Program",
    3: "Don't Miss Out on Jump Droid Beta",
    4: "Last Few Spots - Jump Droid Beta",
    5: "Final Invitation - Jump Droid Beta",
  };
  return subjects[inviteNumber] || subjects[1];
}
