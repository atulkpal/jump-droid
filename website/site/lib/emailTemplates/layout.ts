export function renderLayout(bodyHtml: string): string {
  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { margin: 0; padding: 0; background-color: #0a0a0f; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
    .wrapper { max-width: 600px; margin: 0 auto; padding: 32px 16px; }
    .header { text-align: center; padding-bottom: 24px; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .logo { font-size: 20px; font-weight: 700; color: #67e8f9; letter-spacing: 0.1em; text-decoration: none; }
    .content { padding: 24px 0; }
    .footer { padding-top: 24px; border-top: 1px solid rgba(255,255,255,0.05); text-align: center; }
    .footer-text { font-size: 11px; color: #64748b; margin: 4px 0; }
    a { color: #67e8f9; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="header">
      <a href="https://jump-droid.vercel.app" class="logo">JUMP DROID</a>
    </div>
    <div class="content">${bodyHtml}</div>
    <div class="footer">
      <p class="footer-text">Ashwath AI &mdash; Jump Droid Beta Program</p>
      <p class="footer-text">This is an automated message. Please do not reply directly.</p>
    </div>
  </div>
</body>
</html>`;
}
