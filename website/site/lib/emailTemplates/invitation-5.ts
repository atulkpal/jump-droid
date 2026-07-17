import { renderLayout } from "./layout";

export function renderInvitation5(name: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Final Invitation</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">This is the last time we'll reach out about the Jump Droid Beta Program. We've sent several invitations and wanted to make sure you had every opportunity to join.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">If you're still interested in early access and helping shape Jump Droid, here's your final chance.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="https://jump-droid.vercel.app/beta-info" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Apply Now</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Thank you for your time and consideration.</p>
  `;

  return {
    html: renderLayout(body),
    subject: "Final Invitation - Jump Droid Beta",
  };
}
