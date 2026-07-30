import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const db = getAdminFirestore();
    const activitySnap = await db.collection("activityLog")
      .orderBy("createdAt", "desc")
      .limit(10)
      .get();

    const activities = activitySnap.docs.map(doc => {
      const data = doc.data();
      return {
        id: doc.id,
        eventType: data.eventType,
        details: data.details,
        createdAt: data.createdAt?.toDate?.() || new Date(),
        // Masking email for privacy
        pilot: data.applicantEmail ? data.applicantEmail.split("@")[0].substring(0, 3) + "***" : "Unknown Pilot"
      };
    });

    return NextResponse.json(activities);
  } catch (error) {
    console.error("Community activity error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
