import { NextResponse } from "next/server";
import { Timestamp } from "firebase-admin/firestore";
import { getAdminFirestore } from "@/lib/firebase/admin";
import {
  generateCampaignId,
  DEFAULT_CAMPAIGN_CONFIG,
} from "@/lib/firebase/campaignService";
import { logEventAdmin } from "@/lib/firebase/activityService";

export async function GET() {
  try {
    const adminFirestore = getAdminFirestore();
    const snap = await adminFirestore.collection("campaigns").orderBy("createdAt", "desc").get();
    const serializeTS = (v: any) =>
      v && typeof v.seconds === "number" ? new Date(v.seconds * 1000).toISOString() : v;
    const campaigns = snap.docs.map((d) => {
      const raw = d.data();
      return {
        id: d.id,
        ...raw,
        createdAt: serializeTS(raw.createdAt),
        updatedAt: serializeTS(raw.updatedAt),
        startedAt: serializeTS(raw.startedAt),
        scheduledStartAt: serializeTS(raw.scheduledStartAt),
      };
    });

    // Compute live stats + contactBreakdown for all campaigns from contact data
    try {
      const contactsSnap = await adminFirestore.collection("recruitmentContacts").get();
      const campaignStats: Record<string, {
        totalContacts: number; emailsSent: number; contacted: number; pending: number; totalSends: number;
        errors: number; converted: number; noResponse: number; bounced: number; contactBreakdown: Record<string, number>;
      }> = {};

      contactsSnap.docs.forEach((d) => {
        const data = d.data();
        const cids: string[] = data.campaigns || [];
        const campaignData = data.campaignData || {};

        for (const cid of cids) {
          if (!campaignStats[cid]) {
            campaignStats[cid] = {
              totalContacts: 0, emailsSent: 0, contacted: 0, pending: 0, totalSends: 0,
              errors: 0, converted: 0, noResponse: 0, bounced: 0, contactBreakdown: {},
            };
          }
          const cd = campaignData[cid] || {};
          const s = cd.status || data.status || "pending";
          campaignStats[cid].totalContacts++;
          campaignStats[cid].contactBreakdown[s] = (campaignStats[cid].contactBreakdown[s] || 0) + 1;
          if (cd.opened) {
            campaignStats[cid].contactBreakdown["opened"] = (campaignStats[cid].contactBreakdown["opened"] || 0) + 1;
          }
          const inviteCount = cd.inviteCount ?? 0;
          if (inviteCount > 0) {
            campaignStats[cid].contacted++;
            campaignStats[cid].totalSends += inviteCount;
            campaignStats[cid].emailsSent++;
          }
          if (cd.emailStatus === "failed" || cd.status === "failed") campaignStats[cid].errors++;
          if (cd.status === "bounced") campaignStats[cid].bounced++;
          if (cd.status === "converted" || data.status === "converted") campaignStats[cid].converted++;
          if (cd.status === "no_response") campaignStats[cid].noResponse++;
        }
      });

      for (const c of campaigns) {
        const id = (c as any).id;
        const s = campaignStats[id];
        if (s) {
          s.pending = s.totalContacts - s.contacted;
          (c as any).stats = s;
          (c as any).contactBreakdown = s.contactBreakdown;
        } else {
          (c as any).stats = { totalContacts: 0, emailsSent: 0, contacted: 0, pending: 0, totalSends: 0, errors: 0, converted: 0, noResponse: 0, bounced: 0 };
          (c as any).contactBreakdown = {};
        }
      }
    } catch {
      // fallback: stored stats remain as-is
    }

    return NextResponse.json(campaigns);
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to list campaigns" }, { status: 500 });
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { name, createdBy, scheduledStartAt } = body;

    if (!name || !name.trim()) {
      return NextResponse.json({ error: "Campaign name is required" }, { status: 400 });
    }
    if (!createdBy) {
      return NextResponse.json({ error: "createdBy is required" }, { status: 400 });
    }

    const adminFirestore = getAdminFirestore();
    const id = await generateCampaignId(adminFirestore);

    const campaignData = {
      id,
      name: name.trim(),
      status: scheduledStartAt ? "scheduled" : "draft",
      createdAt: Timestamp.now(),
      createdDate: new Date().toISOString(),
      updatedAt: Timestamp.now(),
      createdBy,
      scheduledStartAt: scheduledStartAt ? Timestamp.fromDate(new Date(scheduledStartAt)) : null,
      settings: { ...DEFAULT_CAMPAIGN_CONFIG },
      stats: { totalContacts: 0, emailsSent: 0, errors: 0, converted: 0, noResponse: 0 },
    };

    await adminFirestore.collection("campaigns").doc(id).set(campaignData);

    await logEventAdmin(
      adminFirestore,
      `campaign:${id}`,
      "campaign_created",
      `Campaign "${name.trim()}" created by ${createdBy}`,
      id
    );

    if (scheduledStartAt) {
      await logEventAdmin(
        adminFirestore,
        `campaign:${id}`,
        "campaign_scheduled",
        `Scheduled to start at ${new Date(scheduledStartAt).toISOString()}`,
        id
      );
    }

    return NextResponse.json(campaignData);
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to create campaign" }, { status: 500 });
  }
}
