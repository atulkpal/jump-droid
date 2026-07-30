import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const db = getAdminFirestore();
    const testersSnap = await db.collection("testers").get();

    let totalMetersClimbed = 0;
    let totalBossesDefeated = 0;
    let totalPilots = testersSnap.size;
    let totalPlayTime = 0;

    if (testersSnap.empty) {
      // Return Legendary Mock Data if no testers exist
      return NextResponse.json({
        totalMetersClimbed: 4850200,
        totalBossesDefeated: 12402,
        totalPilots: 842,
        totalPlayTime: 4500000, // ~1,250h
        isMock: true
      });
    }

    testersSnap.forEach((doc) => {
      const data = doc.data();
      totalMetersClimbed += (data.highestScore || 0);
      totalBossesDefeated += data.bossesDefeated || 0;
      totalPlayTime += data.totalGameplayTime || 0;
    });

    return NextResponse.json({
      totalMetersClimbed,
      totalBossesDefeated,
      totalPilots,
      totalPlayTime,
      isMock: false
    });
  } catch (error) {
    console.error("Community stats error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
