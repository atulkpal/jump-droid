import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { processAllCampaigns } from "@/lib/campaignEngine";
import { logDebugAdmin } from "@/lib/debugLogger";

export async function POST(request: Request) {
  const secret = request.headers.get("x-cron-secret");
  if (secret !== process.env.CRON_SECRET) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const adminFirestore = getAdminFirestore();
  try {
    await logDebugAdmin(adminFirestore, "cron.start", "info", "Campaign send cron started");
    const results = await processAllCampaigns(adminFirestore);
    const aggregated = results.reduce(
      (acc, r) => ({
        processed: acc.processed + r.processed,
        sent: acc.sent + r.sent,
        failed: acc.failed + r.failed,
        converted: acc.converted + r.converted,
        noResponse: acc.noResponse + r.noResponse,
      }),
      { processed: 0, sent: 0, failed: 0, converted: 0, noResponse: 0 }
    );
    await logDebugAdmin(adminFirestore, "cron.complete", "info", "Campaign send cron completed", {
      data: aggregated,
    });
    return NextResponse.json({ ...aggregated, campaignCount: results.length });
  } catch (e: any) {
    await logDebugAdmin(adminFirestore, "cron.error", "error", `Campaign send cron failed: ${e?.message}`);
    return NextResponse.json(
      { error: e?.message ?? "Campaign processing failed" },
      { status: 500 }
    );
  }
}
