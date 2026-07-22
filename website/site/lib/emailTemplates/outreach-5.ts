import { renderLayout } from "./layout";

export function renderOutreach5(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Final Check-In</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">This will be my last message about the Jump Droid Beta Program. We've sent a few notes and wanted to make sure you had every opportunity to join if it interests you.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">The project is open-source and community-driven. If you ever change your mind, you can always sign up at your convenience.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="{betaInfoUrl}" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Learn More</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Wishing you all the best!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Closing the Loop — Jump Droid Beta",
  };
}
