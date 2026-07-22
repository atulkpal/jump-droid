import type { OutreachContact, OutreachStatus, CsvRow, DuplicateCheckResult } from "@/types/recruitmentContacts";
import type { CampaignContactData } from "@/types/campaign";
import { getFirestore } from "./config";

const COLLECTION = "recruitmentContacts";

function normalizeEmail(email: string): string {
  return email.toLowerCase().trim();
}

function docToContact(id: string, d: any): OutreachContact {
  return {
    email: id,
    name: d.name ?? "",
    phone: d.phone ?? "",
    status: d.status ?? "pending",
    source: d.source ?? "manual",
    importedAt: d.importedAt ?? null,
    importedBy: d.importedBy ?? "",
    registeredAt: d.registeredAt ?? null,
    notes: d.notes ?? "",
    campaigns: d.campaigns ?? [],
    campaignData: d.campaignData ?? {},
    scheduledDeleteAt: d.scheduledDeleteAt ?? null,
  };
}

export async function fetchAllContacts(): Promise<OutreachContact[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");

  const q = query(collection(firestore, COLLECTION), orderBy("importedAt", "desc"));
  const snapshot = await getDocs(q);

  return snapshot.docs.map((doc: any) => docToContact(doc.id, doc.data()));
}

export async function fetchContactsByCampaign(campaignId: string): Promise<OutreachContact[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where } = await import("firebase/firestore");

  const q = query(
    collection(firestore, COLLECTION),
    where("campaigns", "array-contains", campaignId)
  );
  const snapshot = await getDocs(q);

  return snapshot.docs.map((doc: any) => docToContact(doc.id, doc.data()));
}

export async function importContacts(
  contacts: CsvRow[],
  importedBy?: string
): Promise<number> {
  const firestore = await getFirestore();
  const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");

  let imported = 0;
  for (const c of contacts) {
    const key = normalizeEmail(c.email);
    const ref = doc(firestore, COLLECTION, key);
    const snap = await getDoc(ref);
    if (snap.exists()) continue;

    await setDoc(ref, {
      name: c.name.trim() || "",
      email: key,
      phone: (c.phone ?? "").trim(),
      status: "pending",
      source: "csv-import",
      importedBy: importedBy || "",
      importedAt: serverTimestamp(),
      lastInviteAt: null,
      registeredAt: null,
      repliedAt: null,
      notes: "",
      inviteCount: 0,
      nextEligibleAt: null,
      campaignId: "",
      campaigns: [],
      campaignData: {},
      emailStatus: "pending",
      stoppedReason: "",
    });
    imported++;
  }
  return imported;
}

export async function batchUpdateInvited(
  emails: string[]
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp, increment } = await import("firebase/firestore");

  const promises = emails.map((email) =>
    updateDoc(doc(firestore, COLLECTION, normalizeEmail(email)), {
      lastInviteAt: serverTimestamp(),
      inviteCount: increment(1),
    }).catch(() => {})
  );
  await Promise.allSettled(promises);
}

export async function addManualContact(
  name: string,
  email: string,
  phone?: string,
  importedBy?: string
): Promise<{ success: boolean; exists: boolean; duplicateType?: string }> {
  const key = normalizeEmail(email);

  const duplicate = await checkDuplicate(key);
  if (duplicate.status !== "ok") {
    const msg = {
      exists_in_outreach: "Already in Outreach",
      already_applicant: "Already an Applicant",
      already_approved: "Already Approved",
      already_active: "Already an Active Member",
    }[duplicate.status] || "Already exists";

    return { success: false, exists: true, duplicateType: msg };
  }

  const firestore = await getFirestore();
  const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");

  await setDoc(doc(firestore, COLLECTION, key), {
    name: name.trim() || "",
    email: key,
    phone: (phone ?? "").trim(),
    status: "pending",
    source: "manual",
    importedBy: importedBy || "",
    importedAt: serverTimestamp(),
    lastInviteAt: null,
    registeredAt: null,
    notes: "",
    inviteCount: 0,
    nextEligibleAt: null,
    campaignId: "",
    campaigns: [],
    campaignData: {},
    emailStatus: "pending",
    stoppedReason: "",
  });

  return { success: true, exists: false };
}

export async function matchRegistration(
  email: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, getDoc, updateDoc, serverTimestamp } = await import("firebase/firestore");

  const ref = doc(firestore, COLLECTION, normalizeEmail(email));
  const snap = await getDoc(ref);
  if (!snap.exists()) return;

  await updateDoc(ref, {
    status: "registered",
    registeredAt: serverTimestamp(),
  });
}

export async function createContactFromApplicant(
  email: string,
  name: string
): Promise<boolean> {
  const key = normalizeEmail(email);
  const firestore = await getFirestore();
  const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");

  const ref = doc(firestore, COLLECTION, key);
  const snap = await getDoc(ref);
  if (snap.exists()) return false;

  await setDoc(ref, {
    name: name.trim() || "",
    email: key,
    phone: "",
    status: "pending",
    source: "applicant-conversion",
    importedBy: "Applicant",
    importedAt: serverTimestamp(),
    lastInviteAt: null,
    registeredAt: null,
    notes: "",
    inviteCount: 0,
    nextEligibleAt: null,
    campaignId: "",
    campaigns: [],
    campaignData: {},
    emailStatus: "pending",
    stoppedReason: "",
  });
  return true;
}

export async function checkDuplicate(
  email: string
): Promise<DuplicateCheckResult> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");
  const key = normalizeEmail(email);

  const contactRef = doc(firestore, COLLECTION, key);
  const contactSnap = await getDoc(contactRef);
  if (contactSnap.exists()) {
    return { status: "exists_in_outreach" };
  }

  const applicantRef = doc(firestore, "betaUsers", key);
  const applicantSnap = await getDoc(applicantRef);
  if (applicantSnap.exists()) {
    const s = applicantSnap.data()?.status || "";
    if (s === "active") return { status: "already_active" };
    if (s === "approved") return { status: "already_approved" };
    return { status: "already_applicant" };
  }

  return { status: "ok" };
}

export async function unsubscribeContact(email: string): Promise<void> {
  const key = normalizeEmail(email);
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  const ref = doc(firestore, COLLECTION, key);
  await updateDoc(ref, { status: "unsubscribed" });
  const { logEvent } = await import("@/lib/firebase/activityService");
  await logEvent(key, "unsubscribed", "Contact opted out of all emails").catch(() => {});
}

export async function resubscribeContact(email: string): Promise<void> {
  const key = normalizeEmail(email);
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  const ref = doc(firestore, COLLECTION, key);
  await updateDoc(ref, { status: "pending" });
  const { logEvent } = await import("@/lib/firebase/activityService");
  await logEvent(key, "resubscribed", "Contact was resubscribed by admin").catch(() => {});
}

export async function markReplied(
  email: string,
  campaignId?: string
): Promise<void> {
  const key = normalizeEmail(email);
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");
  const ref = doc(firestore, COLLECTION, key);

  const updates: Record<string, any> = {};

  if (campaignId) {
    updates[`campaignData.${campaignId}.status`] = "replied";
    updates[`campaignData.${campaignId}.replied`] = true;
    updates[`campaignData.${campaignId}.repliedAt`] = serverTimestamp();
  }

  await updateDoc(ref, updates);
  const { logEvent } = await import("@/lib/firebase/activityService");
  await logEvent(key, "replied", campaignId ? `Replied to campaign ${campaignId}` : "Replied to outreach email", campaignId).catch(() => {});
}

export async function getContactCampaigns(
  email: string
): Promise<string[]> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");
  const snap = await getDoc(doc(firestore, COLLECTION, normalizeEmail(email)));
  if (!snap.exists()) return [];
  return snap.data()?.campaigns ?? [];
}
