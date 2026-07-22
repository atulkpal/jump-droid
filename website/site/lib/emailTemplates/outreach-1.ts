import { renderLayout } from "./layout";

export function renderOutreach1(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Great to Connect</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">I came across your profile and thought you might be interested in the Jump Droid Beta Program. We're building a vertical exploration arcade game with a focus on precision propulsion and modular ship building.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">We're looking for passionate gamers to test early builds and help shape the final experience. As a beta tester, you'll get early access and be credited in the game.</p>
    <div style="text-align: center; margin: 24px 0;">
      <a href="{betaInfoUrl}" style="display: inline-block; padding: 12px 32px; font-size: 14px; font-weight: 600; color: #0a0a0f; background-color: #67e8f9; border-radius: 8px; text-decoration: none;">Learn More</a>
    </div>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Let me know if you have any questions!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Interested in Jump Droid?",
  };
}
