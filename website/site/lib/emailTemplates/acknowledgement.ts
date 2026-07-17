import { renderLayout } from "./layout";

export function renderAcknowledgement(name: string): { html: string; subject: string } {
  const body = `
    <h1 style="font-size: 18px; color: #e2e8f0; margin: 0 0 16px;">Application Received</h1>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Hi ${name},</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">Thank you for applying to the Jump Droid Beta Program. We have received your application and will review it within the next 48 hours.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0 0 12px;">If your application is approved, you will receive a welcome email with instructions on how to get started.</p>
    <p style="font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 0;">In the meantime, feel free to explore more about Jump Droid at <a href="https://jump-droid.vercel.app">jump-droid.vercel.app</a>.</p>
  `;

  return {
    html: renderLayout(body),
    subject: "Application Received - Jump Droid Beta",
  };
}
