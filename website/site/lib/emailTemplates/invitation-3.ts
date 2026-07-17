import { renderLayout } from "./layout";

export function renderInvitation3(name: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Don't Miss Out</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">We've reached out a couple of times about the Jump Droid Beta Program, and we don't want you to miss out on this opportunity.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Jump Droid is a unique arcade experience with 8 zones, resource management, and a growing community of testers. Your feedback would be invaluable.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="https://jump-droid.vercel.app/beta-info" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Apply Now</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Apply today and help shape the future of Jump Droid!</p>
  `;

  return {
    html: renderLayout(body),
    subject: "Don't Miss Out on Jump Droid Beta",
  };
}
