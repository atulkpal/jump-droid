import { getFirestore } from "./config";
import type { AdminDoc, AdminRole } from "@/types/admin";

const COLLECTION = "admins";

export async function fetchAllAdmins(): Promise<AdminDoc[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");
  const q = query(collection(firestore, COLLECTION), orderBy("createdAt", "asc"));
  const snap = await getDocs(q);
  return snap.docs.map((d: any) => {
    const data = d.data();
    return {
      uid: d.id,
      email: data.email || d.id,
      displayName: data.displayName || "",
      role: data.role || "admin",
      createdAt: data.createdAt || null,
    } as AdminDoc;
  });
}

export async function updateAdminRole(
  uid: string,
  role: AdminRole
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");
  await updateDoc(doc(firestore, COLLECTION, uid), { role });
}

export async function deleteAdmin(uid: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, deleteDoc } = await import("firebase/firestore");
  await deleteDoc(doc(firestore, COLLECTION, uid));
}

export async function fetchAdminByEmail(email: string): Promise<AdminDoc | null> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where } = await import("firebase/firestore");
  const q = query(collection(firestore, COLLECTION), where("email", "==", email.toLowerCase().trim()));
  const snap = await getDocs(q);
  if (snap.empty) return null;
  const d = snap.docs[0];
  return { uid: d.id, ...d.data() } as AdminDoc;
}
