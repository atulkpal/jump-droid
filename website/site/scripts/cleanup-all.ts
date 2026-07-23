import { initializeApp, cert, getApps } from "firebase-admin/app";
import { getFirestore, type Firestore } from "firebase-admin/firestore";

const KEEP_COLLECTIONS = new Set([
  "emailTemplateOverrides",
  "emailTemplatesCustom",
  "gmailAuth",
  "senderProfiles",
]);

const DELETE_COLLECTIONS = [
  "campaigns",
  "recruitmentContacts",
  "betaUsers",
  "emailLog",
  "activityLog",
  "campaignConfig",
  "appConfig",
];

async function deleteAllDocs(db: Firestore, collectionId: string) {
  let total = 0;
  const batchSize = 20;

  while (true) {
    const snapshot = await db.collection(collectionId).limit(batchSize).get();
    if (snapshot.empty) break;

    for (const docSnap of snapshot.docs) {
      const subCols = await docSnap.ref.listCollections();
      for (const subCol of subCols) {
        const subSnap = await subCol.get();
        for (const subDoc of subSnap.docs) {
          await subDoc.ref.delete();
        }
      }
      await docSnap.ref.delete();
      total++;
    }
    console.log(`  ${collectionId}: deleted ${total} docs so far`);
  }

  return total;
}

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
  let total = 0;

  for (const col of DELETE_COLLECTIONS) {
    console.log(`Deleting ${col}...`);
    const count = await deleteAllDocs(db, col);
    total += count;
    console.log(`  Done: ${count} deleted`);
  }

  console.log(`\n---`);
  console.log(`Total docs deleted across all collections: ${total}`);
  console.log(`Preserved collections: ${[...KEEP_COLLECTIONS].join(", ")}`);
}

cleanup().catch((e) => {
  console.error("Cleanup failed:", e);
  process.exit(1);
});
