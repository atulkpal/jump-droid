import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import { logEventAdmin } from "@/lib/firebase/activityService";

export async function GET(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const adminFirestore = getAdminFirestore();
    const snap = await adminFirestore.collection("campaigns").doc(id).get();
    if (!snap.exists) {
      return NextResponse.json({ error: "Campaign not found" }, { status: 404 });
    }

    let breakdown: Record<string, number> = {};
    let liveContacted = 0;
    let liveTotalSends = 0;
    let liveErrors = 0;
    let liveBounced = 0;
    try {
      const contactSnap = await adminFirestore
        .collection("recruitmentContacts")
        .where("campaigns", "array-contains", id)
        .get();
      contactSnap.docs.forEach((d) => {
        const data = d.data();
        const cd = data.campaignData?.[id] || {};
        const s = cd.status || data.status || "unknown";
        breakdown[s] = (breakdown[s] || 0) + 1;
        const inviteCount = cd.inviteCount ?? 0;
        if (inviteCount > 0) {
          liveContacted++;
          liveTotalSends += inviteCount;
        }
        if (cd.emailStatus === "failed" || cd.status === "failed") liveErrors++;
        if (cd.status === "bounced") liveBounced++;
      });
    } catch { /* non-critical */ }

    const totalContacts = Object.values(breakdown).reduce((s, v) => s + v, 0);

    const raw = snap.data() || {};
    const serializeTS = (v: any) =>
      v && typeof v.seconds === "number" ? new Date(v.seconds * 1000).toISOString() : v;

    return NextResponse.json({
      id: snap.id,
      ...raw,
      createdAt: serializeTS(raw.createdAt),
      updatedAt: serializeTS(raw.updatedAt),
      startedAt: serializeTS(raw.startedAt),
      scheduledStartAt: serializeTS(raw.scheduledStartAt),
      lastDetectedAt: serializeTS(raw.lastDetectedAt),
      contactBreakdown: breakdown,
      stats: {
        totalContacts,
        emailsSent: liveContacted,
        contacted: liveContacted,
        pending: totalContacts - liveContacted,
        totalSends: liveTotalSends,
        errors: liveErrors,
        converted: breakdown["converted"] ?? 0,
        noResponse: breakdown["no_response"] ?? 0,
        bounced: liveBounced,
      },
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to get campaign" }, { status: 500 });
  }
}

export async function PUT(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const body = await request.json();
    const adminFirestore = getAdminFirestore();

    const snap = await adminFirestore.collection("campaigns").doc(id).get();
    if (!snap.exists) {
      return NextResponse.json({ error: "Campaign not found" }, { status: 404 });
    }

    const before = snap.data();

    // Only allow updating certain fields
    const allowedFields = ["name", "settings", "scheduledStartAt", "status"];
    const updateData: Record<string, any> = { updatedAt: new Date() };

    for (const field of allowedFields) {
      if (field in body) {
        updateData[field] = field === "scheduledStartAt" && body[field]
          ? new Date(body[field])
          : body[field];
      }
    }

    // Set startedAt when campaign starts for the first time (draft/scheduled → running)
    if (
      updateData.status === "running" &&
      before?.status &&
      (before.status === "draft" || before.status === "scheduled")
    ) {
      updateData.startedAt = new Date();
    }

    await adminFirestore.collection("campaigns").doc(id).update(updateData);

    // Log what changed
    const changed = Object.keys(updateData).filter((k) => k !== "updatedAt");
    if (changed.length > 0) {
      logEventAdmin(
        adminFirestore,
        `campaign:${id}`,
        "campaign_updated",
        `Campaign updated: ${changed.join(", ")}`,
        id
      ).catch(() => {});
    }

    // If status changed, log specific lifecycle event
    if (updateData.status && before?.status !== updateData.status) {
      const statusEventMap: Record<string, string> = {
        paused: "campaign_paused",
        running: "campaign_resumed",
        completed: "campaign_completed",
        failed: "campaign_failed",
        cancelled: "campaign_cancelled",
      };
      const lifecycleEvent = statusEventMap[updateData.status as string];
      if (lifecycleEvent) {
        logEventAdmin(
          adminFirestore,
          `campaign:${id}`,
          lifecycleEvent as any,
          `Campaign status changed from "${before?.status}" to "${updateData.status}"`,
          id
        ).catch(() => {});
      }
    }

    const updated = await adminFirestore.collection("campaigns").doc(id).get();
    return NextResponse.json({ id: updated.id, ...updated.data() });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to update campaign" }, { status: 500 });
  }
}

export async function DELETE(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const adminFirestore = getAdminFirestore();

    const snap = await adminFirestore.collection("campaigns").doc(id).get();
    if (!snap.exists) {
      return NextResponse.json({ error: "Campaign not found" }, { status: 404 });
    }

    const campaignName = snap.data()?.name || id;

    // Log before deletion
    await logEventAdmin(
      adminFirestore,
      `campaign:${id}`,
      "campaign_archived",
      `Campaign "${campaignName}" deleted`,
      id
    );

    await adminFirestore.collection("campaigns").doc(id).delete();

    // Cleanup orphaned campaign refs from contacts
    try {
      const contactsSnap = await adminFirestore
        .collection("recruitmentContacts")
        .where("campaigns", "array-contains", id)
        .get();
      const batch = adminFirestore.batch();
      contactsSnap.docs.forEach((docSnap) => {
        const data = docSnap.data();
        const campaigns = (data.campaigns || []).filter((c: string) => c !== id);
        const campaignData = { ...(data.campaignData || {}) };
        delete campaignData[id];
        batch.update(docSnap.ref, { campaigns, campaignData });
      });
      await batch.commit();
    } catch { /* non-critical cleanup */ }

    return NextResponse.json({ success: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to delete campaign" }, { status: 500 });
  }
}
