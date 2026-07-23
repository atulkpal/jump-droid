import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { processCampaign } from "@/lib/campaignEngine";
import { logDebugAdmin } from "@/lib/debugLogger";

export async function POST(request: Request) {
  const secret = request.headers.get("x-cron-secret");
  if (secret !== process.env.CRON_SECRET) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const adminFirestore = getAdminFirestore();
  try {
    await logDebugAdmin(adminFirestore, "cron.start", "info", "Campaign cron job started");
    const result = await processCampaign(adminFirestore);
    await logDebugAdmin(adminFirestore, "cron.complete", "info", "Campaign cron job completed", {
      data: {
        processed: result.processed,
        sent: result.sent,
        failed: result.failed,
        converted: result.converted,
        noResponse: result.noResponse,
      },
    });
    return NextResponse.json(result);
  } catch (e: any) {
    await logDebugAdmin(adminFirestore, "cron.error", "error", `Campaign cron job failed: ${e?.message}`);
    return NextResponse.json(
      { error: e?.message ?? "Campaign processing failed" },
      { status: 500 }
    );
  }
}
