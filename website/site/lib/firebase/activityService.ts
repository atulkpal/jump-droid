import type { ActivityEventType, ActivityLogEntry } from "@/types/activityLog";
import { getFirestore } from "./config";

export async function logEvent(
  applicantEmail: string,
  eventType: ActivityEventType,
  details?: string
): Promise<void> {
  const firestore = await getFirestore();
  const { collection, addDoc, serverTimestamp } = await import("firebase/firestore");

  await addDoc(collection(firestore, "activityLog"), {
    applicantEmail: applicantEmail.toLowerCase().trim(),
    eventType,
    details: details || "",
    createdAt: serverTimestamp(),
  });
}

export async function fetchTimeline(
  applicantEmail: string
): Promise<ActivityLogEntry[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where, orderBy } = await import("firebase/firestore");

  const q = query(
    collection(firestore, "activityLog"),
    where("applicantEmail", "==", applicantEmail.toLowerCase().trim()),
    orderBy("createdAt", "asc")
  );

  const snapshot = await getDocs(q);
  return snapshot.docs.map((doc: any) => {
    const d = doc.data();
    return {
      id: doc.id,
      applicantEmail: d.applicantEmail,
      eventType: d.eventType as ActivityEventType,
      details: d.details ?? "",
      createdAt: d.createdAt ?? null,
    } as ActivityLogEntry;
  });
}
