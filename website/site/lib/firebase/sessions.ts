import type { TesterSession } from "@/types/tester";
import { getFirestore } from "./config";

export async function fetchSessions(docId: string): Promise<TesterSession[]> {
  const firestore = await getFirestore();

  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");
  const q = query(
    collection(firestore, "testers", docId, "sessions"),
    orderBy("sessionStart", "desc")
  );
  const snapshot = await getDocs(q);
  return snapshot.docs.map(
    (doc: any) => ({ id: doc.id, ...doc.data(), testerEmail: docId }) as TesterSession
  );
}

export async function fetchRecentSessions(limitCount = 50): Promise<TesterSession[]> {
  const firestore = await getFirestore();

  const { collectionGroup, getDocs, query, orderBy, limit } = await import("firebase/firestore");
  const q = query(
    collectionGroup(firestore, "sessions"),
    orderBy("sessionStart", "desc"),
    limit(limitCount)
  );
  const snapshot = await getDocs(q);
  return snapshot.docs.map((doc: any) => ({ id: doc.id, ...doc.data() }) as TesterSession);
}
