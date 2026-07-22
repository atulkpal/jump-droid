import { renderLayout } from "./layout";

export function renderWelcome(name: string, unsubscribeUrl?: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Welcome to the Beta Program</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">You've been approved as a Jump Droid beta tester. Here's what to do next:</p>
    <ol style="font-size: 14px; color: #94a3b8; line-height: 1.8; margin: 0 0 16px; padding-left: 20px;">
      <li>Opt-in to the beta on Google Play: <a href="https://play.google.com/apps/testing/com.ashwathai.jump_droid">Join the Beta</a></li>
      <li>Download Jump Droid from the Play Store: <a href="https://play.google.com/store/apps/details?id=com.ashwathai.jump_droid">Get the App</a></li>
      <li>Play at least 30 minutes per day to qualify for rewards</li>
      <li>Share your feedback via email or Discord</li>
    </ol>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">As a beta tester, your name will be listed in the game credits. If you signed up for the Code Jam, we'll reach out with details soon.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">Thank you for helping make Jump Droid better!</p>
  `;

  return {
    html: renderLayout(body, unsubscribeUrl),
    subject: "Welcome to the Jump Droid Beta Program!",
  };
}
