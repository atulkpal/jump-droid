import { renderLayout } from "./layout";

export function renderInvitation4(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Last Few Spots</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">This is your fourth invitation to join the Jump Droid Beta Program. Beta slots are filling up and we want to give you every chance to be part of it.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">As a tester, you'll experience the game before anyone else and help us polish the final release.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="{betaInfoUrl}" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Apply Now</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">This may be your last chance &mdash; don't wait!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Last Few Spots - Jump Droid Beta",
  };
}
