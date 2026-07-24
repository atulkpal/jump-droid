import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { processAllCampaigns } from "@/lib/campaignEngine";

export async function POST() {
  try {
    const adminFirestore = getAdminFirestore();
    const results = await processAllCampaigns(adminFirestore);
    return NextResponse.json({ success: true, campaignCount: results.length });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Trigger failed" }, { status: 500 });
  }
}
