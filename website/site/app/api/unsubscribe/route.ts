import { NextResponse } from "next/server";

export async function POST(req: Request) {
  try {
    const { email } = await req.json();
    if (!email) {
      return NextResponse.json({ error: "Email is required" }, { status: 400 });
    }

    const cleanEmail = email.toLowerCase().trim();
    const { getAdminFirestore } = await import("@/lib/firebase/admin");
    const adminFirestore = getAdminFirestore();

    const ref = adminFirestore.collection("recruitmentContacts").doc(cleanEmail);
    const snap = await ref.get();
    if (!snap.exists) {
      return NextResponse.json({ error: "Contact not found" }, { status: 404 });
    }

    const data = snap.data() || {};
    const campaigns: string[] = data.campaigns || [];
    const updates: Record<string, any> = { status: "unsubscribed" };
    for (const cid of campaigns) {
      updates[`campaignData.${cid}.status`] = "unsubscribed";
    }
    await ref.update(updates);

    const { logEventAdmin } = await import("@/lib/firebase/activityService");
    await logEventAdmin(adminFirestore, cleanEmail, "unsubscribed", "Unsubscribed via self-service link");

    return NextResponse.json({ success: true });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message ?? "Unsubscribe failed" },
      { status: 500 }
    );
  }
}

export async function GET(req: Request) {
  const { searchParams } = new URL(req.url);
  const email = searchParams.get("email");
  if (!email) {
    return NextResponse.json({ error: "Email is required" }, { status: 400 });
  }

  try {
    const cleanEmail = email.toLowerCase().trim();
    const { getAdminFirestore } = await import("@/lib/firebase/admin");
    const adminFirestore = getAdminFirestore();

    const ref = adminFirestore.collection("recruitmentContacts").doc(cleanEmail);
    const snap = await ref.get();
    if (!snap.exists) {
      return new Response(
        `<html><body style="background:#0a0a0f;color:#fff;font-family:monospace;padding:40px;text-align:center"><h1>Contact not found</h1></body></html>`,
        { status: 404, headers: { "Content-Type": "text/html" } }
      );
    }

    const data = snap.data() || {};
    const campaigns: string[] = data.campaigns || [];
    const updates: Record<string, any> = { status: "unsubscribed" };
    for (const cid of campaigns) {
      updates[`campaignData.${cid}.status`] = "unsubscribed";
    }
    await ref.update(updates);

    const { logEventAdmin } = await import("@/lib/firebase/activityService");
    await logEventAdmin(adminFirestore, cleanEmail, "unsubscribed", "Unsubscribed via one-click List-Unsubscribe");

    return new Response(
      `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Unsubscribed</title></head><body style="background:#0a0a0f;color:#fff;font-family:monospace;padding:60px 20px;text-align:center"><h1 style="color:#67e8f9;font-size:24px;letter-spacing:0.1em">Unsubscribed</h1><p style="color:#94a3b8;font-size:14px;margin-top:12px">You have been unsubscribed from all emails.</p></body></html>`,
      { status: 200, headers: { "Content-Type": "text/html" } }
    );
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message ?? "Unsubscribe failed" },
      { status: 500 }
    );
  }
}