const SCOPES = [
  "https://www.googleapis.com/auth/gmail.send",
  "https://www.googleapis.com/auth/gmail.readonly",
  "https://www.googleapis.com/auth/userinfo.email",
  "https://www.googleapis.com/auth/userinfo.profile",
].join(" ");

function getClientId(): string | null {
  return process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ?? null;
}

export function getAuthUrl(adminEmail?: string): string | null {
  const clientId = getClientId();
  if (!clientId) return null;

  const redirectUri = `${window.location.origin}/gmail/callback`;
  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: redirectUri,
    response_type: "code",
    scope: SCOPES,
    access_type: "offline",
    prompt: "consent",
  });
  if (adminEmail) params.set("state", adminEmail);
  return `https://accounts.google.com/o/oauth2/v2/auth?${params}`;
}

export async function checkAuthStatus(accountEmail?: string): Promise<boolean> {
  try {
    const url = accountEmail
      ? `/api/gmail/status?email=${encodeURIComponent(accountEmail)}`
      : "/api/gmail/status";
    const res = await fetch(url);
    const data = await res.json();
    return data.authenticated === true;
  } catch {
    return false;
  }
}

export async function disconnectGmail(accountEmail?: string): Promise<void> {
  const res = await fetch("/api/gmail/disconnect", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: accountEmail }),
  });
  if (!res.ok) throw new Error("Failed to disconnect Gmail");
}

export async function sendInvitations(
  emails: { email: string; name: string }[],
  senderAccountId?: string
): Promise<{ email: string; success: boolean; error?: string }[]> {
  const res = await fetch("/api/gmail/send", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ emails, senderAccountId }),
  });
  if (!res.ok) throw new Error("Failed to send invitations");
  return res.json();
}
