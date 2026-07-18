import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  try {
    const { code } = await req.json();
    if (!code) {
      return NextResponse.json({ error: "Authorization code is required" }, { status: 400 });
    }

    const clientId = process.env.GOOGLE_CLIENT_ID || process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    const clientSecret = process.env.GOOGLE_CLIENT_SECRET;
    const redirectUri = `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/gmail/callback`;

    if (!clientId || !clientSecret) {
      return NextResponse.json({ error: "Google OAuth not configured" }, { status: 500 });
    }

    const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        code,
        client_id: clientId,
        client_secret: clientSecret,
        redirect_uri: redirectUri,
        grant_type: "authorization_code",
      }),
    });

    const tokenData = await tokenRes.json();
    if (!tokenRes.ok || !tokenData.refresh_token) {
      return NextResponse.json(
        { error: tokenData.error_description || tokenData.error || "Token exchange failed" },
        { status: 400 }
      );
    }

    const infoRes = await fetch(
      `https://www.googleapis.com/oauth2/v2/userinfo?access_token=${tokenData.access_token}`
    );
    const infoData = await infoRes.json();
    const email: string = infoData.email || "unknown";
    const displayName: string = infoData.name || email;

    return NextResponse.json({
      success: true,
      email: email.toLowerCase().trim(),
      displayName,
      refreshToken: tokenData.refresh_token,
      accessToken: tokenData.access_token,
      expiresIn: tokenData.expires_in || 3600,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Exchange failed" }, { status: 500 });
  }
}
