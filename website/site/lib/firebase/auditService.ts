import { getFirestore } from "./config";
import type { EmailAuditEventType } from "@/types/emailAudit";
import type { Firestore } from "firebase-admin/firestore";

export async function logEmailAudit(
  eventType: EmailAuditEventType,
  accountEmail: string,
  details: string,
  performedBy?: string
): Promise<void> {
  const firestore = await getFirestore();
  const { collection, addDoc, serverTimestamp } = await import("firebase/firestore");

  await addDoc(collection(firestore, "emailAuditLog"), {
    eventType,
    accountEmail: accountEmail.toLowerCase().trim(),
    details,
    performedBy: performedBy || "system",
    createdAt: serverTimestamp(),
  });
}

export async function fetchEmailAuditLog(
  limitCount: number = 100
): Promise<any[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy, limit } = await import("firebase/firestore");

  const q = query(
    collection(firestore, "emailAuditLog"),
    orderBy("createdAt", "desc"),
    limit(limitCount)
  );
  const snapshot = await getDocs(q);

  return snapshot.docs.map((d: any) => ({
    id: d.id,
    ...d.data(),
  }));
}

export async function logEmailAuditAdmin(
  adminFirestore: Firestore,
  eventType: EmailAuditEventType,
  accountEmail: string,
  details: string,
  performedBy?: string
): Promise<void> {
  try {
    await adminFirestore.collection("emailAuditLog").add({
      eventType,
      accountEmail: accountEmail.toLowerCase().trim(),
      details,
      performedBy: performedBy || "system",
      createdAt: new Date(),
    });
  } catch {
    // Non-critical logging failure
  }
}
