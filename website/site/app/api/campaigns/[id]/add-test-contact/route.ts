import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { addContactToCampaignAdmin } from "@/lib/firebase/campaignService";

export async function POST(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id: campaignId } = await params;
    const adminFirestore = getAdminFirestore();
    const testEmail = "test@jumpdroid.com";

    await addContactToCampaignAdmin(adminFirestore, testEmail, campaignId, "System");

    return NextResponse.json({ success: true, email: testEmail });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to add test contact" }, { status: 500 });
  }
}
