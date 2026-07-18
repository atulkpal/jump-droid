import { getFirestore } from "./config";
import type { EmailAccount, EmailAccountStatus } from "@/types/emailAccount";
import type { Firestore } from "firebase-admin/firestore";

const COLLECTION = "emailAccounts";

export async function getEmailAccount(email: string): Promise<EmailAccount | null> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");

  const snap = await getDoc(doc(firestore, COLLECTION, email.toLowerCase().trim()));
  if (!snap.exists()) return null;
  return snap.data() as EmailAccount;
}

export async function listEmailAccounts(): Promise<EmailAccount[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");

  const q = query(collection(firestore, COLLECTION), orderBy("createdAt", "desc"));
  const snapshot = await getDocs(q);

  return snapshot.docs
    .map((d: any) => d.data() as EmailAccount)
    .filter((a) => a.status !== "deleted");
}

export async function createEmailAccount(
  email: string,
  data: Omit<EmailAccount, "email" | "createdAt">
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");

  const ref = doc(firestore, COLLECTION, email.toLowerCase().trim());
  const { getDoc } = await import("firebase/firestore");
  const existing = await getDoc(ref);
  if (existing.exists()) return;

  await setDoc(ref, {
    ...data,
    email: email.toLowerCase().trim(),
    createdAt: serverTimestamp(),
  });
}

export async function updateAccountStatus(
  email: string,
  status: EmailAccountStatus,
  errorMessage?: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");

  const update: any = { status };
  if (errorMessage !== undefined) update.errorMessage = errorMessage;
  await updateDoc(doc(firestore, COLLECTION, email.toLowerCase().trim()), update);
}

export async function updateAccountTokens(
  email: string,
  accessToken: string,
  refreshToken: string,
  expiryDate: number
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");

  await updateDoc(doc(firestore, COLLECTION, email.toLowerCase().trim()), {
    accessToken,
    refreshToken,
    expiryDate,
    status: "connected",
    errorMessage: null,
    lastUsedAt: serverTimestamp(),
  });
}

export async function setDefaultAccount(email: string): Promise<void> {
  const firestore = await getFirestore();
  const { collection, getDocs, doc, writeBatch } = await import("firebase/firestore");

  const snapshot = await getDocs(collection(firestore, COLLECTION));
  const batch = writeBatch(firestore);

  snapshot.docs.forEach((d: any) => {
    const ref = doc(firestore, COLLECTION, d.id);
    batch.update(ref, { isDefault: d.id === email.toLowerCase().trim() });
  });

  await batch.commit();
}

export async function softDeleteAccount(email: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");

  await updateDoc(doc(firestore, COLLECTION, email.toLowerCase().trim()), {
    status: "deleted",
  });
}

export async function getDefaultSenderAccount(): Promise<EmailAccount | null> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where } = await import("firebase/firestore");

  const q = query(
    collection(firestore, COLLECTION),
    where("isDefault", "==", true),
    where("status", "==", "connected")
  );
  const snapshot = await getDocs(q);

  if (snapshot.empty) {
    const fallback = query(
      collection(firestore, COLLECTION),
      where("status", "==", "connected")
    );
    const fallbackSnap = await getDocs(fallback);
    if (fallbackSnap.empty) return null;
    return fallbackSnap.docs[0].data() as EmailAccount;
  }

  return snapshot.docs[0].data() as EmailAccount;
}

export async function listEmailAccountsAdmin(adminFirestore: Firestore): Promise<EmailAccount[]> {
  const snapshot = await adminFirestore
    .collection(COLLECTION)
    .orderBy("createdAt", "desc")
    .get();
  return snapshot.docs
    .map((d) => d.data() as EmailAccount)
    .filter((a) => a.status !== "deleted");
}

export async function getDefaultSenderAccountAdmin(adminFirestore: Firestore): Promise<EmailAccount | null> {
  const q = adminFirestore
    .collection(COLLECTION)
    .where("isDefault", "==", true)
    .where("status", "==", "connected")
    .limit(1);
  const snapshot = await q.get();
  if (!snapshot.empty) return snapshot.docs[0].data() as EmailAccount;

  const fallback = adminFirestore
    .collection(COLLECTION)
    .where("status", "==", "connected")
    .limit(1);
  const fallbackSnap = await fallback.get();
  if (fallbackSnap.empty) return null;
  return fallbackSnap.docs[0].data() as EmailAccount;
}
