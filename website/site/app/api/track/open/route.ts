import { NextResponse } from "next/server";
import crypto from "crypto";
import { getAdminFirestore } from "@/lib/firebase/admin";

const TRANSPARENT_GIF = Buffer.from(
  "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7",
  "base64"
);

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const token = searchParams.get("t");
  const email = searchParams.get("e");
  const campaignId = searchParams.get("c");

  const returnPixel = () =>
    new Response(TRANSPARENT_GIF, {
      headers: {
        "Content-Type": "image/gif",
        "Cache-Control": "no-cache, no-store, must-revalidate",
        Pragma: "no-cache",
        Expires: "0",
      },
    });

  if (!token || !email || !campaignId) {
    return returnPixel();
  }

  const expected = crypto
    .createHmac("sha256", process.env.TRACKING_SECRET || "fallback-secret")
    .update(`${email}|${campaignId}`)
    .digest("hex")
    .slice(0, 12);

  if (token !== expected) {
    return returnPixel();
  }

  try {
    const adminFirestore = getAdminFirestore();
    const cleanEmail = email.toLowerCase().trim();
    await adminFirestore.collection("activityLog").add({
      applicantEmail: cleanEmail,
      eventType: "email_opened",
      details: "",
      createdAt: new Date(),
      campaignId,
    });
    await adminFirestore.collection("recruitmentContacts").doc(cleanEmail).update({
      [`campaignData.${campaignId}.opened`]: true,
      [`campaignData.${campaignId}.openedAt`]: new Date(),
    }).catch(() => {});
  } catch {
    // Non-critical — pixel should always return
  }

  return returnPixel();
}
