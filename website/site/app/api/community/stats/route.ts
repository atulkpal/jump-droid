import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const db = getAdminFirestore();
    const testersSnap = await db.collection("testers").get();

    let totalAltitude = 0;
    let totalBossesDefeated = 0;
    let totalPilots = testersSnap.size;
    let totalPlayTime = 0;

    testersSnap.forEach((doc) => {
      const data = doc.data();
      totalAltitude = Math.max(totalAltitude, data.highestScore || 0); // Actually using highestScore as altitude for simplicity in stats
      totalBossesDefeated += data.bossesDefeated || 0; // Assuming this field exists or we add it
      totalPlayTime += data.totalGameplayTime || 0;
    });

    // In a real scenario, we might want the SUM of all altitudes if we mean "total meters climbed"
    // Let's assume totalAltitude is sum of all pilots' high scores for a bigger "community" number
    let totalMetersClimbed = 0;
    testersSnap.forEach((doc) => {
        totalMetersClimbed += (doc.data().highestScore || 0);
    });

    return NextResponse.json({
      totalMetersClimbed,
      totalBossesDefeated,
      totalPilots,
      totalPlayTime,
    });
  } catch (error) {
    console.error("Community stats error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
