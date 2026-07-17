import { NextResponse } from "next/server";

function getCallbackUrl(): string {
  const appUrl = process.env.NEXT_PUBLIC_APP_URL;
  if (appUrl) return `${appUrl}/gmail/callback`;
  if (process.env.NODE_ENV === "development") {
    return "http://localhost:3000/gmail/callback";
  }
  throw new Error(
    "NEXT_PUBLIC_APP_URL is required in production. " +
    "Set it to your deployment URL (e.g. https://jump-droid.vercel.app)."
  );
}

export async function POST(req: Request) {
  try {
    const { code } = await req.json();
    if (!code) {
      return NextResponse.json({ error: "Missing authorization code" }, { status: 400 });
    }

    const callbackUrl = getCallbackUrl();

    const clientId = process.env.GOOGLE_CLIENT_ID;
    const clientSecret = process.env.GOOGLE_CLIENT_SECRET;

    if (!clientId || !clientSecret) {
      return NextResponse.json(
        { error: "Google OAuth not configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET." },
        { status: 500 }
      );
    }

    const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        code,
        client_id: clientId,
        client_secret: clientSecret,
        redirect_uri: callbackUrl,
        grant_type: "authorization_code",
      }),
    });

    const tokens = await tokenRes.json();
    if (!tokens.refresh_token) {
      return NextResponse.json({ error: "No refresh_token in response" }, { status: 400 });
    }

    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");

    await setDoc(doc(firestore, "gmailAuth", "tokens"), {
      refreshToken: tokens.refresh_token,
      accessToken: tokens.access_token,
      expiryDate: Date.now() + (tokens.expires_in || 3600) * 1000,
      updatedAt: serverTimestamp(),
    });

    return NextResponse.json({ success: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Exchange failed" }, { status: 500 });
  }
}
