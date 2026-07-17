import type { RecruitmentApplicant, RecruitmentStatus } from "@/types/recruitment";
import { getFirestore } from "./config";

export async function fetchAllApplicants(): Promise<RecruitmentApplicant[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");

  const q = query(collection(firestore, "betaUsers"), orderBy("registeredAt", "desc"));
  const snapshot = await getDocs(q);

  return snapshot.docs.map((doc: any) => {
    const data = doc.data();
    return {
      docId: doc.id,
      email: data.email ?? doc.id,
      name: data.name ?? "",
      phone: data.phone ?? "",
      codeJam: data.codeJam ?? false,
      status: data.status ?? "pending",
      source: data.source ?? "website",
      version: data.version ?? 1,
      notes: data.notes ?? "",
      registeredFrom: data.registeredFrom ?? "",
      registeredAt: data.registeredAt ?? null,
      approvedAt: data.approvedAt ?? null,
      invitedAt: data.invitedAt ?? null,
      emailStatus: data.emailStatus ?? "",
    } as RecruitmentApplicant;
  });
}

export async function updateApplicantStatus(
  email: string,
  status: RecruitmentStatus
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  await updateDoc(doc(firestore, "betaUsers", email), { status });
}

export async function approveApplicant(
  email: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");
  await updateDoc(doc(firestore, "betaUsers", email), {
    status: "approved",
    emailStatus: "pending",
    approvedAt: serverTimestamp(),
  });
}

export async function activateApplicant(
  email: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");
  await updateDoc(doc(firestore, "betaUsers", email), {
    status: "active",
    emailStatus: "sent",
    invitedAt: serverTimestamp(),
  });
}

export async function failEmailStatus(
  email: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  await updateDoc(doc(firestore, "betaUsers", email), {
    emailStatus: "failed",
  });
}

export async function deleteApplicant(
  email: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, deleteDoc } = await import("firebase/firestore");

  await deleteDoc(doc(firestore, "betaUsers", email)).catch(() => {});

  const contactRef = doc(firestore, "recruitmentContacts", email.toLowerCase().trim());
  await deleteDoc(contactRef).catch(() => {});
}

export async function updateApplicantNotes(
  email: string,
  notes: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  await updateDoc(doc(firestore, "betaUsers", email), { notes });
}
