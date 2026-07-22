import type { Firestore, DocumentReference } from "firebase-admin/firestore";

export interface PendingWrite {
  ref: DocumentReference;
  data: Record<string, any>;
  isUpdate: boolean;
}

export function createWriteBuffer(): PendingWrite[] {
  return [];
}

export function pushWrite(
  buffer: PendingWrite[],
  ref: DocumentReference,
  data: Record<string, any>,
  isUpdate: boolean
): void {
  buffer.push({ ref, data, isUpdate });
}

export async function flushWrites(
  buffer: PendingWrite[],
  adminFirestore: Firestore
): Promise<void> {
  if (buffer.length === 0) return;

  for (let i = 0; i < buffer.length; i += 400) {
    const batch = adminFirestore.batch();
    const chunk = buffer.slice(i, i + 400);

    for (const { ref, data, isUpdate } of chunk) {
      if (isUpdate) {
        batch.update(ref, data);
      } else {
        batch.set(ref, data, { merge: true });
      }
    }

    await batch.commit();
  }
}
