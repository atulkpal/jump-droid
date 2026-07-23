import { initializeApp, cert, getApps } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

async function cleanup() {
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
  const batchSize = 20;

  let total = 0;

  while (true) {
    const snapshot = await db.collection("recruitmentContacts").limit(batchSize).get();
    if (snapshot.empty) break;

    const batch = db.batch();
    let count = 0;
    snapshot.forEach((docSnap) => {
      const d = docSnap.data();
      if (d.campaignData || d.campaigns) {
        batch.update(docSnap.ref, {
          campaignData: FieldValue.delete(),
          campaigns: FieldValue.delete(),
        });
        count++;
      }
    });
    if (count > 0) {
      await batch.commit();
      total += count;
      console.log(`Cleaned ${count} contacts (total: ${total})`);
    }
  }

  console.log(`\nCleanup complete. Cleaned campaign data from ${total} contacts.`);
}

cleanup().catch((e) => {
  console.error("Cleanup failed:", e);
  process.exit(1);
});
