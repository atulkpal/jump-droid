import { renderLayout } from "./layout";

export function renderOutreach3(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Still Interested?</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">I've reached out a couple of times about the Jump Droid Beta Program and wanted to give it one more shot. We truly value diverse perspectives in our testing group.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Jump Droid features 8 atmospheric zones, a modular rocket build system, and various hostile entities to navigate. It's a challenging but rewarding experience.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="{betaInfoUrl}" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">See What's New</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Would love to have you on board!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Jump Droid Beta — Still Interested?",
  };
}
