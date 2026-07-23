import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { detectBounces } from "@/lib/firebase/bounceDetectionAdmin";
import { detectReplies } from "@/lib/gmailReplyDetection";
import { createWriteBuffer, flushWrites } from "@/lib/campaignProcessor";
import { pushWrite } from "@/lib/campaignProcessor";
import { logDebugAdmin } from "@/lib/debugLogger";

export async function POST(request: Request) {
  const secret = request.headers.get("x-cron-secret");
  if (secret !== process.env.CRON_SECRET) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const adminFirestore = getAdminFirestore();

  try {
    await logDebugAdmin(adminFirestore, "detect.start", "info", "Detection-only cron run started");

    const buffer = createWriteBuffer();

    // Reply detection
    const replyResult = await detectReplies(adminFirestore, buffer);
    if (replyResult.errors.length > 0) {
      pushWrite(
        buffer,
        adminFirestore.collection("activityLog").doc(),
        {
          applicantEmail: "system",
          eventType: "system_error",
          details: `Reply detection errors: ${replyResult.errors.join("; ")}`,
          createdAt: new Date(),
        },
        false
      );
    }

    await flushWrites(buffer, adminFirestore);

    // Bounce detection
    const bounceCount = await detectBounces(adminFirestore);

    // Log detection summary
    pushWrite(
      buffer,
      adminFirestore.collection("activityLog").doc(),
      {
        applicantEmail: "system",
        eventType: "detection_check",
        details: `Detection run: ${bounceCount} bounces, ${replyResult.totalReplied} replies found across ${replyResult.accountsPolled} accounts (${replyResult.totalChecked} checked)`,
        createdAt: new Date(),
      },
      false
    );

    await flushWrites(buffer, adminFirestore);

    await logDebugAdmin(adminFirestore, "detect.complete", "info",
      `Detection run completed: ${bounceCount} bounces, ${replyResult.totalReplied} replies`,
      { data: { bounced: bounceCount, replies: replyResult.totalReplied, checked: replyResult.totalChecked, errors: replyResult.errors } });

    return NextResponse.json({
      success: true,
      bounced: bounceCount,
      replies: replyResult.totalReplied,
      errors: replyResult.errors,
    });
  } catch (e: any) {
    await logDebugAdmin(adminFirestore, "detect.error", "error", `Detection cron failed: ${e?.message}`);
    return NextResponse.json(
      { error: e?.message ?? "Detection failed" },
      { status: 500 }
    );
  }
}
