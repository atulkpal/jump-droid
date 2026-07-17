import type { Feedback } from "@/types/feedback";
import { getFirestore } from "./config";

const FEEDBACK_COLLECTION = "testerFeedback";

export async function submitFeedback(
  docId: string,
  testerName: string | undefined,
  data: Omit<Feedback, "id" | "testerEmail" | "testerName" | "createdAt">
): Promise<void> {
  const firestore = await getFirestore();

  const { collection, addDoc, Timestamp } = await import("firebase/firestore");
  await addDoc(collection(firestore, "testers", docId, FEEDBACK_COLLECTION), {
    rating: data.rating,
    category: data.category,
    comment: data.comment,
    createdAt: Timestamp.now(),
  });
}

export async function fetchFeedbackByTester(docId: string): Promise<Feedback[]> {
  const firestore = await getFirestore();

  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");
  const q = query(
    collection(firestore, "testers", docId, FEEDBACK_COLLECTION),
    orderBy("createdAt", "desc")
  );
  const snapshot = await getDocs(q);
  return snapshot.docs.map((doc: any) => ({ id: doc.id, ...doc.data() }) as Feedback);
}

export async function fetchAllFeedback(): Promise<Feedback[]> {
  const firestore = await getFirestore();

  const { collectionGroup, getDocs, query, orderBy } = await import("firebase/firestore");
  const q = query(
    collectionGroup(firestore, FEEDBACK_COLLECTION),
    orderBy("createdAt", "desc")
  );
  const snapshot = await getDocs(q);
  return snapshot.docs.map((doc: any) => ({ id: doc.id, ...doc.data() }) as Feedback);
}
