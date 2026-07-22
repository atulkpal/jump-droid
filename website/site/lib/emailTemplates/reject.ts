import { renderLayout } from "./layout";

export function renderReject(name: string, reason?: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const reasonHtml = reason
    ? `<p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Reason: ${reason}</p>`
    : "";

  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Application Update</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Thank you for your interest in the Jump Droid Beta Program. After reviewing your application, we regret to inform you that we are unable to offer you a spot at this time.</p>
    ${reasonHtml}
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">We appreciate your understanding and encourage you to stay tuned for future opportunities.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">— The Jump Droid Team</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Application Status - Jump Droid Beta",
  };
}
