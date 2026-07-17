import type { Tester } from "@/types/tester";
import { getFirestore } from "./config";

export async function fetchAllTesters(): Promise<Tester[]> {
  const firestore = await getFirestore();

  const { collection, getDocs } = await import("firebase/firestore");
  const snapshot = await getDocs(collection(firestore, "testers"));
  return snapshot.docs.map((doc: any) => {
    const data = doc.data();
    return { ...data, docId: doc.id, email: data.email } as Tester;
  });
}

export async function fetchTester(docId: string): Promise<Tester | null> {
  const firestore = await getFirestore();

  const { doc, getDoc } = await import("firebase/firestore");
  const snap = await getDoc(doc(firestore, "testers", docId));
  if (!snap.exists()) return null;
  const data = snap.data();
  return { ...data, docId: snap.id, email: data.email } as Tester;
}
