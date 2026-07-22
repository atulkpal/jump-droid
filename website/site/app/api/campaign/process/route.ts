import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { processCampaign } from "@/lib/campaignEngine";

export async function POST(request: Request) {
  const secret = request.headers.get("x-cron-secret");
  if (secret !== process.env.CRON_SECRET) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  try {
    const adminFirestore = getAdminFirestore();
    const result = await processCampaign(adminFirestore);
    return NextResponse.json(result);
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message ?? "Campaign processing failed" },
      { status: 500 }
    );
  }
}
