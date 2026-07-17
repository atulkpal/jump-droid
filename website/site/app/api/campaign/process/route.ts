import { NextResponse } from "next/server";
import { processCampaign } from "@/lib/campaignEngine";

export async function GET() {
  try {
    const result = await processCampaign();
    return NextResponse.json(result);
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message ?? "Campaign processing failed" },
      { status: 500 }
    );
  }
}
