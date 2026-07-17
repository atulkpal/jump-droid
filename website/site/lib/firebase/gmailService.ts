const SCOPES = [
  "https://www.googleapis.com/auth/gmail.send",
  "https://www.googleapis.com/auth/gmail.readonly",
].join(" ");

function getClientId(): string | null {
  return process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ?? null;
}

export function getAuthUrl(): string | null {
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
  return `https://accounts.google.com/o/oauth2/v2/auth?${params}`;
}

export async function checkAuthStatus(): Promise<boolean> {
  try {
    const res = await fetch("/api/gmail/status");
    const data = await res.json();
    return data.authenticated === true;
  } catch {
    return false;
  }
}

export async function disconnectGmail(): Promise<void> {
  const res = await fetch("/api/gmail/disconnect", { method: "POST" });
  if (!res.ok) {
    throw new Error("Failed to disconnect Gmail");
  }
}

export async function sendInvitations(
  emails: { email: string; name: string }[]
): Promise<{ email: string; success: boolean; error?: string }[]> {
  const res = await fetch("/api/gmail/send", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ emails }),
  });
  if (!res.ok) throw new Error("Failed to send invitations");
  return res.json();
}
