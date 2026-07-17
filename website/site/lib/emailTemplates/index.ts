import type { EmailTemplate } from "@/types/emailLog";
import { renderAcknowledgement } from "./acknowledgement";
import { renderWelcome } from "./welcome";
import { renderInvitation1 } from "./invitation-1";
import { renderInvitation2 } from "./invitation-2";
import { renderInvitation3 } from "./invitation-3";
import { renderInvitation4 } from "./invitation-4";
import { renderInvitation5 } from "./invitation-5";

const INVITATION_MAP: Record<number, (name: string) => { html: string; subject: string }> = {
  1: renderInvitation1,
  2: renderInvitation2,
  3: renderInvitation3,
  4: renderInvitation4,
  5: renderInvitation5,
};

export function renderTemplate(
  template: EmailTemplate,
  name: string,
  inviteNumber?: number
): { html: string; subject: string; text: string } {
  let result: { html: string; subject: string };

  if (template === "acknowledgement") {
    result = renderAcknowledgement(name);
  } else if (template === "welcome") {
    result = renderWelcome(name);
  } else if (template === "invitation" || template.startsWith("invitation-")) {
    const num = inviteNumber || 1;
    const renderFn = INVITATION_MAP[num];
    if (!renderFn) {
      result = renderInvitation1(name);
    } else {
      result = renderFn(name);
    }
  } else {
    result = renderWelcome(name);
  }

  return {
    html: result.html,
    subject: result.subject,
    text: stripHtml(result.html),
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
