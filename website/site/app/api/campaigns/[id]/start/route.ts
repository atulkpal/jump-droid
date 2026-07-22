import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";
import {
  getCampaignAdmin,
  runPreflightAdmin,
} from "@/lib/firebase/campaignService";
import { logEventAdmin } from "@/lib/firebase/activityService";

export async function POST(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const adminFirestore = getAdminFirestore();

    const campaign = await getCampaignAdmin(adminFirestore, id);
    if (!campaign) {
      return NextResponse.json({ error: "Campaign not found" }, { status: 404 });
    }

    if (campaign.status === "running") {
      return NextResponse.json({ error: "Campaign is already running" }, { status: 400 });
    }

    if (campaign.status === "completed" || campaign.status === "cancelled") {
      return NextResponse.json({ error: "Cannot start a completed or cancelled campaign" }, { status: 400 });
    }

    // Run pre-flight check
    const preflight = await runPreflightAdmin(adminFirestore, id);

    if (!preflight.passed) {
      await logEventAdmin(
        adminFirestore,
        `campaign:${id}`,
        "preflight_failed",
        `Pre-flight failed: ${preflight.checks.filter((c) => c.status === "error").map((c) => c.label).join(", ")}`,
        id
      );
      return NextResponse.json({ error: "Pre-flight validation failed", preflight }, { status: 400 });
    }

    await logEventAdmin(
      adminFirestore,
      `campaign:${id}`,
      "preflight_passed",
      `Pre-flight validation passed (${preflight.checks.length} checks)`,
      id
    );

    await adminFirestore.collection("campaigns").doc(id).update({
      status: "running",
      startedAt: new Date(),
      updatedAt: new Date(),
    });

    await logEventAdmin(
      adminFirestore,
      `campaign:${id}`,
      "campaign_started",
      `Campaign "${campaign.name}" started`,
      id
    );

    return NextResponse.json({ success: true, preflight });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Failed to start campaign" }, { status: 500 });
  }
}

// Also expose GET for pre-flight check without starting
export async function GET(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const adminFirestore = getAdminFirestore();

    const campaign = await getCampaignAdmin(adminFirestore, id);
    if (!campaign) {
      return NextResponse.json({ error: "Campaign not found" }, { status: 404 });
    }

    const preflight = await runPreflightAdmin(adminFirestore, id);
    return NextResponse.json(preflight);
  } catch (e: any) {
    return NextResponse.json({ error: e?.message ?? "Pre-flight check failed" }, { status: 500 });
  }
}
