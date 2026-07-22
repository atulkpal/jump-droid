import { renderLayout } from "./layout";

export function renderOutreach4(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">One Last Note</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">I know I've reached out a few times, so I'll keep this brief. The Jump Droid Beta Program is still open and we'd be glad to have you.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Beta test slots are filling up, and I wanted to make sure you had every chance to get involved if you're interested. Your feedback could directly influence the final game.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="{betaInfoUrl}" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Apply Now</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Either way, thanks for your time!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "One Last Note About Jump Droid Beta",
  };
}
