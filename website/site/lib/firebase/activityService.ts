import type { ActivityEventType, ActivityLogEntry } from "@/types/activityLog";
import { getFirestore } from "./config";
import type { Firestore } from "firebase-admin/firestore";

export async function logEventAdmin(
  adminFirestore: Firestore,
  applicantEmail: string,
  eventType: ActivityEventType,
  details?: string,
  campaignId?: string
): Promise<void> {
  try {
    const docData: Record<string, any> = {
      applicantEmail: applicantEmail.toLowerCase().trim(),
      eventType,
      details: details || "",
      createdAt: new Date(),
    };
    if (campaignId) docData.campaignId = campaignId;

    await adminFirestore.collection("activityLog").add(docData);
  } catch {
    // Non-critical logging failure
  }
}

export async function logEvent(
  applicantEmail: string,
  eventType: ActivityEventType,
  details?: string,
  campaignId?: string
): Promise<void> {
  const firestore = await getFirestore();
  const { collection, addDoc, serverTimestamp } = await import("firebase/firestore");

  const docData: Record<string, any> = {
    applicantEmail: applicantEmail.toLowerCase().trim(),
    eventType,
    details: details || "",
    createdAt: serverTimestamp(),
  };
  if (campaignId) docData.campaignId = campaignId;

  await addDoc(collection(firestore, "activityLog"), docData);
}

const TS_ASC = (a: any, b: any) => ((a.createdAt?.seconds ?? 0) - (b.createdAt?.seconds ?? 0));
const TS_DESC = (a: any, b: any) => ((b.createdAt?.seconds ?? 0) - (a.createdAt?.seconds ?? 0));

export async function fetchTimeline(
  applicantEmail: string
): Promise<ActivityLogEntry[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where } = await import("firebase/firestore");

  const q = query(
    collection(firestore, "activityLog"),
    where("applicantEmail", "==", applicantEmail.toLowerCase().trim())
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
      campaignId: d.campaignId ?? undefined,
    } as ActivityLogEntry;
  }).sort(TS_ASC);
}

export async function fetchCampaignTimeline(
  campaignId: string
): Promise<ActivityLogEntry[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where } = await import("firebase/firestore");

  const q = query(
    collection(firestore, "activityLog"),
    where("campaignId", "==", campaignId)
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
      campaignId: d.campaignId ?? undefined,
    } as ActivityLogEntry;
  }).sort(TS_ASC);
}

const ERROR_EVENT_TYPES = new Set<ActivityEventType>([
  "invitation_failed",
  "welcome_email_failed",
  "campaign_failed",
  "system_error",
  "rate_limited",
]);

export async function fetchCampaignErrors(
  campaignId: string
): Promise<any[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where, limit } = await import("firebase/firestore");

  const [emailLogSnap, activitySnap] = await Promise.all([
    getDocs(query(
      collection(firestore, "emailLog"),
      where("campaign", "==", campaignId),
      where("status", "==", "failed"),
      limit(200)
    )),
    getDocs(query(
      collection(firestore, "activityLog"),
      where("campaignId", "==", campaignId),
      limit(200)
    )),
  ]);

  const emailErrors = emailLogSnap.docs.map((doc: any) => {
    const d = doc.data();
    return {
      id: doc.id,
      _source: "emailLog" as const,
      recipient: d.recipient,
      error: d.failureReason || d.error || "Unknown error",
      template: d.template,
      timestamp: d.failedAt ?? null,
    };
  });

  const activityErrors = activitySnap.docs
    .filter((doc: any) => ERROR_EVENT_TYPES.has(doc.data().eventType))
    .map((doc: any) => {
      const d = doc.data();
      return {
        id: doc.id,
        _source: "activityLog" as const,
        recipient: d.applicantEmail,
        error: d.details || d.eventType,
        template: null,
        timestamp: d.createdAt ?? null,
      };
    });

  const merged = [...emailErrors, ...activityErrors].sort((a, b) => {
    const ta = a.timestamp?.seconds ?? 0;
    const tb = b.timestamp?.seconds ?? 0;
    return tb - ta;
  });

  return merged;
}
