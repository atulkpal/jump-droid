import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { getCampaignAdmin } from "@/lib/firebase/campaignService";
import { detectBounces } from "@/lib/firebase/bounceDetectionAdmin";
import { detectReplies } from "@/lib/gmailReplyDetection";
import { createWriteBuffer, flushWrites } from "@/lib/campaignProcessor";
import { logEventAdmin } from "@/lib/firebase/activityService";

export async function POST(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const diagnostics: Record<string, any> = {};

  try {
    const { id } = await params;
    const adminFirestore = getAdminFirestore();

    const campaign = await getCampaignAdmin(adminFirestore, id);
    if (!campaign) {
      return NextResponse.json({ error: "Campaign not found" }, { status: 404 });
    }

    // Step 1: Bounce detection
    let bounced = 0;
    try {
      bounced = await detectBounces(adminFirestore);
      diagnostics.bounces = { ok: true, count: bounced };
    } catch (e: any) {
      diagnostics.bounces = { ok: false, error: e?.message };
    }

    // Step 2: Reply detection
    let replyResult = { totalReplied: 0, totalChecked: 0, errors: [] as string[] };
    try {
      const buffer = createWriteBuffer();
      replyResult = await detectReplies(adminFirestore, buffer);
      await flushWrites(buffer, adminFirestore);
      diagnostics.replies = { ok: true, ...replyResult };
    } catch (e: any) {
      diagnostics.replies = { ok: false, error: e?.message };
    }

    // Step 3: Always save detection timestamp
    try {
      await adminFirestore.collection("campaigns").doc(id).update({
        lastDetectedAt: new Date(),
        updatedAt: new Date(),
      });
      diagnostics.saved = true;
    } catch (e: any) {
      diagnostics.saved = false;
      diagnostics.saveError = e?.message;
    }

    // Step 4: Log event (non-critical)
    try {
      await logEventAdmin(
        adminFirestore,
        `campaign:${id}`,
        "campaign_updated",
        `Manual detect: ${bounced} bounces, ${replyResult.totalReplied} replies found`,
        id
      );
    } catch {}

    return NextResponse.json({
      success: true,
      bounced,
      replies: replyResult.totalReplied,
      replyErrors: replyResult.errors,
      diagnostics,
    });
  } catch (e: any) {
    return NextResponse.json({
      error: e?.message ?? "Detection failed",
      diagnostics,
    }, { status: 500 });
  }
}
