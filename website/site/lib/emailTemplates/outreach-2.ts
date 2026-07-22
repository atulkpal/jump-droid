import { renderLayout } from "./layout";

export function renderOutreach2(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Following Up</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">I wanted to follow up on my earlier message about the Jump Droid Beta Program. Just in case it got buried!</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">We're building something unique — a vertical exploration game where you manage fuel, heat, and shield while climbing through atmospheric zones. Your perspective as a gamer would be really valuable.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="{betaInfoUrl}" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Check It Out</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Happy to answer any questions!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Quick Follow-Up: Jump Droid Beta",
  };
}
