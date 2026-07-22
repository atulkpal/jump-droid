import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id: targetCampaignId } = await params;
    const { sourceCampaignId } = await request.json();

    if (!sourceCampaignId) {
      return NextResponse.json({ error: "sourceCampaignId is required" }, { status: 400 });
    }

    if (sourceCampaignId === targetCampaignId) {
      return NextResponse.json({ error: "Cannot import from the same campaign" }, { status: 400 });
    }

    const adminFirestore = getAdminFirestore();

    const contactsSnap = await adminFirestore
      .collection("recruitmentContacts")
      .where("campaigns", "array-contains", sourceCampaignId)
      .get();

    if (contactsSnap.empty) {
      return NextResponse.json({ imported: 0 });
    }

    const batch = adminFirestore.batch();
    let count = 0;

    contactsSnap.docs.forEach((docSnap) => {
      const data = docSnap.data();
      const campaigns: string[] = data.campaigns || [];
      if (campaigns.includes(targetCampaignId)) return;

      campaigns.push(targetCampaignId);
      const campaignData = { ...(data.campaignData || {}) };
      campaignData[targetCampaignId] = {
        inviteCount: 0,
        lastInviteAt: null,
        nextEligibleAt: null,
        emailStatus: "pending",
        stoppedReason: "",
        assignedSender: null,
        currentStep: 0,
        startedAt: null,
        replied: false,
        repliedAt: null,
        status: "pending",
      };

      batch.update(docSnap.ref, { campaigns, campaignData });
      count++;
    });

    if (count > 0) {
      await batch.commit();
    }

    return NextResponse.json({ imported: count });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to import contacts" }, { status: 500 });
  }
}
