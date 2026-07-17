import type { OutreachContact, OutreachStatus, CsvRow, DuplicateCheckResult } from "@/types/recruitmentContacts";
import { getFirestore } from "./config";

const COLLECTION = "recruitmentContacts";

export async function fetchAllContacts(): Promise<OutreachContact[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");

  const q = query(collection(firestore, COLLECTION), orderBy("importedAt", "desc"));
  const snapshot = await getDocs(q);

  return snapshot.docs.map((doc: any) => {
    const d = doc.data();
    return {
      email: doc.id,
      name: d.name ?? "",
      phone: d.phone ?? "",
      status: d.status ?? "pending",
      source: d.source ?? "manual",
      importedAt: d.importedAt ?? null,
      lastInviteAt: d.lastInviteAt ?? null,
      registeredAt: d.registeredAt ?? null,
      notes: d.notes ?? "",
      inviteCount: d.inviteCount ?? 0,
      nextEligibleAt: d.nextEligibleAt ?? null,
      campaignId: d.campaignId ?? "",
      emailStatus: d.emailStatus ?? "pending",
      stoppedReason: d.stoppedReason ?? "",
    } as OutreachContact;
  });
}

export async function importContacts(
  contacts: CsvRow[]
): Promise<number> {
  const firestore = await getFirestore();
  const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");

  let imported = 0;
  for (const c of contacts) {
    const ref = doc(firestore, COLLECTION, c.email.toLowerCase().trim());
    const snap = await getDoc(ref);
    if (snap.exists()) continue;

    await setDoc(ref, {
      name: c.name.trim() || "",
      email: c.email.toLowerCase().trim(),
      phone: (c.phone ?? "").trim(),
      status: "pending",
      source: "csv-import",
      importedAt: serverTimestamp(),
      lastInviteAt: null,
      registeredAt: null,
      notes: "",
      inviteCount: 0,
      nextEligibleAt: null,
      campaignId: "",
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
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");

  const promises = emails.map((email) =>
    updateDoc(doc(firestore, COLLECTION, email.toLowerCase().trim()), {
      status: "invited",
      lastInviteAt: serverTimestamp(),
    }).catch(() => {})
  );
  await Promise.allSettled(promises);
}

export async function addManualContact(
  name: string,
  email: string,
  phone?: string
): Promise<{ success: boolean; exists: boolean; duplicateType?: string }> {
  const key = email.toLowerCase().trim();

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
    importedAt: serverTimestamp(),
    lastInviteAt: null,
    registeredAt: null,
    notes: "",
    inviteCount: 0,
    nextEligibleAt: null,
    campaignId: "",
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

  const ref = doc(firestore, COLLECTION, email.toLowerCase().trim());
  const snap = await getDoc(ref);
  if (!snap.exists()) return;

  await updateDoc(ref, {
    status: "registered",
    registeredAt: serverTimestamp(),
  });
}

export async function checkDuplicate(
  email: string
): Promise<DuplicateCheckResult> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");
  const key = email.toLowerCase().trim();

  const contactRef = doc(firestore, COLLECTION, key);
  const contactSnap = await getDoc(contactRef);
  if (contactSnap.exists()) {
    return { status: "exists_in_outreach" };
  }

  const applicantRef = doc(firestore, "betaUsers", key);
  const applicantSnap = await getDoc(applicantRef);
  if (applicantSnap.exists()) {
    const status = applicantSnap.data()?.status || "";
    if (status === "active") return { status: "already_active" };
    if (status === "approved") return { status: "already_approved" };
    return { status: "already_applicant" };
  }

  return { status: "ok" };
}
