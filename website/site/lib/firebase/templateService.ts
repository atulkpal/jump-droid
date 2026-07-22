import { getFirestore } from "./config";
import type { StoredTemplate, TemplateWithSource } from "@/types/emailTemplates";
import type { EmailTemplate } from "@/types/emailLog";

const OVERRIDES_COLLECTION = "emailTemplateOverrides";
const CUSTOM_COLLECTION = "emailTemplatesCustom";

export const PERMANENT_TEMPLATES: { key: EmailTemplate | string; label: string }[] = [
  { key: "acknowledgement", label: "Acknowledgement" },
  { key: "welcome", label: "Welcome" },
  { key: "outreach-1", label: "Outreach 1" },
  { key: "outreach-2", label: "Outreach 2" },
  { key: "outreach-3", label: "Outreach 3" },
  { key: "outreach-4", label: "Outreach 4" },
  { key: "outreach-5", label: "Outreach 5" },
  { key: "reject", label: "Rejection" },
];

export async function loadAllTemplates(): Promise<TemplateWithSource[]> {
  const firestore = await getFirestore();
  const { getDocs, collection } = await import("firebase/firestore");

  const result: TemplateWithSource[] = [];

  // Load overrides for permanent templates
  const overrideSnap = await getDocs(collection(firestore, OVERRIDES_COLLECTION));
  const overrideMap = new Map<string, StoredTemplate>();
  overrideSnap.docs.forEach((d) => {
    overrideMap.set(d.id, d.data() as StoredTemplate);
  });

  for (const pt of PERMANENT_TEMPLATES) {
    const override = overrideMap.get(pt.key);
    if (override) {
      result.push({ ...override, name: override.name || pt.label, templateKey: pt.key, isCustom: false });
    } else {
      result.push({
        name: pt.label,
        subject: "",
        htmlBody: "",
        templateKey: pt.key,
        isCustom: false,
      });
    }
  }

  // Load custom templates
  const customSnap = await getDocs(collection(firestore, CUSTOM_COLLECTION));
  customSnap.docs.forEach((d) => {
    const data = d.data() as StoredTemplate;
    result.push({ ...data, templateKey: d.id, isCustom: true });
  });

  return result;
}

export async function getTemplateContent(
  key: string
): Promise<{ subject: string; htmlBody: string } | null> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");

  const isPermanent = PERMANENT_TEMPLATES.some((t) => t.key === key);
  const collectionName = isPermanent ? OVERRIDES_COLLECTION : CUSTOM_COLLECTION;

  const snap = await getDoc(doc(firestore, collectionName, key));
  if (!snap.exists()) return null;
  const d = snap.data() as StoredTemplate;
  return { subject: d.subject, htmlBody: d.htmlBody };
}

export async function saveTemplateOverride(
  key: string,
  data: { name: string; subject: string; htmlBody: string }
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");

  await setDoc(
    doc(firestore, OVERRIDES_COLLECTION, key),
    {
      ...data,
      updatedAt: serverTimestamp(),
    },
    { merge: true }
  );
}

export async function deleteTemplateOverride(key: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, deleteDoc } = await import("firebase/firestore");
  await deleteDoc(doc(firestore, OVERRIDES_COLLECTION, key));
}

export async function createCustomTemplate(
  id: string,
  data: { name: string; subject: string; htmlBody: string }
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");

  await setDoc(doc(firestore, CUSTOM_COLLECTION, id), {
    ...data,
    isCustom: true,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });
}

export async function updateCustomTemplate(
  id: string,
  data: { name?: string; subject?: string; htmlBody?: string }
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");

  await updateDoc(doc(firestore, CUSTOM_COLLECTION, id), {
    ...data,
    updatedAt: serverTimestamp(),
  });
}

export async function deleteCustomTemplate(id: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, deleteDoc } = await import("firebase/firestore");
  await deleteDoc(doc(firestore, CUSTOM_COLLECTION, id));
}
