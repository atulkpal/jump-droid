import { NextResponse } from "next/server";
import { Timestamp } from "firebase-admin/firestore";
import { getAdminFirestore } from "@/lib/firebase/admin";
import {
  getCampaignAdmin,
  generateCampaignId,
} from "@/lib/firebase/campaignService";
import { logEventAdmin } from "@/lib/firebase/activityService";

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const body = await request.json();
    const { newName, createdBy } = body;

    if (!newName || !newName.trim()) {
      return NextResponse.json({ error: "New campaign name is required" }, { status: 400 });
    }
    if (!createdBy) {
      return NextResponse.json({ error: "createdBy is required" }, { status: 400 });
    }

    const adminFirestore = getAdminFirestore();

    const source = await getCampaignAdmin(adminFirestore, id);
    if (!source) {
      return NextResponse.json({ error: "Source campaign not found" }, { status: 404 });
    }

    const newId = await generateCampaignId(adminFirestore);

    const campaignData = {
      id: newId,
      name: newName.trim(),
      status: "draft",
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
      createdBy,
      scheduledStartAt: null,
      settings: { ...source.settings },
      stats: { totalContacts: 0, emailsSent: 0, errors: 0, converted: 0, noResponse: 0 },
    };

    await adminFirestore.collection("campaigns").doc(newId).set(campaignData);

    await logEventAdmin(
      adminFirestore,
      `campaign:${newId}`,
      "campaign_duplicated",
      `Campaign "${newName.trim()}" duplicated from "${source.name}" by ${createdBy}`,
      newId
    );

    return NextResponse.json(campaignData);
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to duplicate campaign" }, { status: 500 });
  }
}
