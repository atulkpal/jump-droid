import { renderLayout } from "./layout";

export function renderInvitation1(name: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">You're Invited</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">We'd like to invite you to join the Jump Droid Beta Program. Jump Droid is a free, open-source Android arcade game where you manage fuel, heat, and shield as you climb through 8 atmospheric zones.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">As a beta tester, you'll get early access, help shape development, and be listed in the game credits.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="https://jump-droid.vercel.app/beta-info" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Apply Now</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Thank you for your interest!</p>
  `;

  return {
    html: renderLayout(body),
    subject: "You're Invited to Join the Jump Droid Beta Program!",
  };
}
