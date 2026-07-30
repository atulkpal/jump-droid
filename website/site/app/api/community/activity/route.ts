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

    if (activitySnap.empty) {
      // Return Simulation Activities if no log exists
      const mockActivities = [
        { id: "m1", eventType: "milestone", details: "Pilot ASH*** reached 100,000m (Ascension Protocol)", pilot: "ASH***", createdAt: new Date() },
        { id: "m2", eventType: "boss_kill", details: "Boss Star-Eater defeated by Pilot JON***", pilot: "JON***", createdAt: new Date() },
        { id: "m3", eventType: "combo", details: "New Fleet Record: 50x Combo by Pilot ZED***", pilot: "ZED***", createdAt: new Date() },
        { id: "m4", eventType: "discovery", details: "Deep Space Signal decoded in Zone 8", pilot: "ARC***", createdAt: new Date() },
        { id: "m5", eventType: "boss_kill", details: "Boss Void Engine neutralized by Pilot VAL***", pilot: "VAL***", createdAt: new Date() },
        { id: "m6", eventType: "milestone", details: "Fleet Total Distance exceeded 4,000,000m", pilot: "SYS***", createdAt: new Date() },
      ];
      return NextResponse.json(mockActivities);
    }

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
