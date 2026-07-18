import { NextResponse } from "next/server";

export async function POST() {
  const missing: string[] = [];

  const clientId = process.env.GOOGLE_CLIENT_ID || process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
  const clientSecret = process.env.GOOGLE_CLIENT_SECRET;
  const appUrl = process.env.NEXT_PUBLIC_APP_URL;

  if (!clientId) missing.push("GOOGLE_CLIENT_ID");
  if (!clientSecret) missing.push("GOOGLE_CLIENT_SECRET");
  if (!appUrl) missing.push("NEXT_PUBLIC_APP_URL");

  if (missing.length > 0) {
    return NextResponse.json({
      valid: false,
      missing,
      error: `Missing OAuth configuration: ${missing.join(", ")}`,
    });
  }

  return NextResponse.json({ valid: true });
}
