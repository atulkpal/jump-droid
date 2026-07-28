import { NextResponse } from "next/server";
import { readCampaignLogs, clearCampaignLogs } from "@/lib/campaignLogger";

export async function GET() {
  const content = readCampaignLogs();
  return new NextResponse(content, {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}

export async function DELETE() {
  clearCampaignLogs();
  return NextResponse.json({ success: true });
}
