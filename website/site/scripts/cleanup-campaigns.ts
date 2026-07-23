import { initializeApp, cert, getApps } from "firebase-admin/app";
import { getFirestore, type Firestore } from "firebase-admin/firestore";

async function deleteSubcollections(db: Firestore, docRef: FirebaseFirestore.DocumentReference) {
  const collections = await docRef.listCollections();
  for (const subCol of collections) {
    const subSnap = await subCol.get();
    for (const subDoc of subSnap.docs) {
      await deleteSubcollections(db, subDoc.ref);
      await subDoc.ref.delete();
    }
  }
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
  const batchSize = 20;

  let total = 0;

  while (true) {
    const snapshot = await db.collection("campaigns").limit(batchSize).get();
    if (snapshot.empty) break;

    for (const docSnap of snapshot.docs) {
      await deleteSubcollections(db, docSnap.ref);
      await docSnap.ref.delete();
      total++;
      console.log(`Deleted campaign: ${docSnap.id} (${total})`);
    }
  }

  console.log(`\nCleanup complete. Deleted ${total} campaigns total.`);
}

cleanup().catch((e) => {
  console.error("Cleanup failed:", e);
  process.exit(1);
});
