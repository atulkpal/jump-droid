import { renderLayout } from "./layout";

export function renderInvitation2(name: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Reminder: Beta Access</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Just a friendly reminder that you're invited to join the Jump Droid Beta Program. Your early access is waiting!</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Beta testers get exclusive early access, the chance to influence development, and a permanent spot in the game credits.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="https://jump-droid.vercel.app/beta-info" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Apply Now</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">We hope to see you in the program!</p>
  `;

  return {
    html: renderLayout(body),
    subject: "Reminder: Join the Jump Droid Beta Program",
  };
}
