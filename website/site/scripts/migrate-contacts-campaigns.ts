/**
 * One-time migration script to add `campaigns` array and `campaignData` map
 * to existing recruitmentContacts that have `campaignId` set.
 *
 * Run: npx ts-node --skip-project scripts/migrate-contacts-campaigns.ts
 * Or deploy as a serverless function / Firebase function.
 */

import { initializeApp, cert, getApps } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

async function migrate() {
  const key = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;
  if (!key) {
    console.error("FIREBASE_SERVICE_ACCOUNT_KEY not set");
    process.exit(1);
  }

  const parsed = JSON.parse(key.replace(/\n/g, "").replace(/\r/g, ""));
  if (getApps().length === 0) {
    initializeApp({ credential: cert(parsed) });
  }

  const db = getFirestore();

  const snapshot = await db.collection("recruitmentContacts").get();
  let updated = 0;
  let skipped = 0;

  for (const docSnap of snapshot.docs) {
    const d = docSnap.data();
    const campaigns: string[] = d.campaigns || [];
    const campaignData: Record<string, any> = d.campaignData || {};

    // If campaigns is already populated, skip
    if (campaigns.length > 0) {
      skipped++;
      continue;
    }

    // If no campaignId set, skip
    const legacyCampaignId = d.campaignId || "";
    if (!legacyCampaignId) {
      skipped++;
      continue;
    }

    // Add the legacy campaignId to campaigns array
    campaigns.push(legacyCampaignId);

    // Create campaignData entry from existing flat fields
    campaignData[legacyCampaignId] = {
      inviteCount: d.inviteCount ?? 0,
      lastInviteAt: d.lastInviteAt ?? null,
      nextEligibleAt: d.nextEligibleAt ?? null,
      emailStatus: d.emailStatus ?? "pending",
      stoppedReason: d.stoppedReason ?? "",
      assignedSender: null,
      currentStep: d.inviteCount ?? 0,
      startedAt: d.lastInviteAt ?? null,
    };

    await docSnap.ref.update({
      campaigns,
      campaignData,
    });

    updated++;
    console.log(`Migrated ${docSnap.id}: campaignId=${legacyCampaignId}, inviteCount=${d.inviteCount ?? 0}`);
  }

  console.log(`\nMigration complete. Updated: ${updated}, Skipped: ${skipped}`);
}

migrate().catch((e) => {
  console.error("Migration failed:", e);
  process.exit(1);
});
