import { getFirestore } from "./config";
import type { CampaignConfig, CampaignProcessResult } from "@/types/campaign";
import type { OutreachContact } from "@/types/recruitmentContacts";

const CONTACTS_COLLECTION = "recruitmentContacts";
const CONFIG_DOC = "campaignConfig";

export async function getCampaignConfig(): Promise<CampaignConfig> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");

  const snap = await getDoc(doc(firestore, CONFIG_DOC, "default"));
  if (!snap.exists()) {
    return {
      delayDays: 4,
      maxInvites: 5,
      delayBetweenEmailsMs: 5000,
      batchSize: 10,
      maxEmailsPerHour: 100,
      senderAccountId: null,
    };
  }

  const d = snap.data();
  return {
    delayDays: d.delayDays ?? 4,
    maxInvites: d.maxInvites ?? 5,
    delayBetweenEmailsMs: d.delayBetweenEmailsMs ?? 5000,
    batchSize: d.batchSize ?? 10,
    maxEmailsPerHour: d.maxEmailsPerHour ?? 100,
    senderAccountId: d.senderAccountId ?? null,
  };
}

export async function saveCampaignConfig(config: CampaignConfig): Promise<void> {
  const firestore = await getFirestore();
  const { doc, setDoc } = await import("firebase/firestore");
  await setDoc(doc(firestore, CONFIG_DOC, "default"), config);
}

export async function fetchEligibleContacts(
  maxInvites: number
): Promise<OutreachContact[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, where, orderBy } = await import("firebase/firestore");

  const now = Date.now() / 1000;

  const q = query(
    collection(firestore, CONTACTS_COLLECTION),
    where("inviteCount", "<", maxInvites),
    orderBy("inviteCount", "asc"),
    orderBy("nextEligibleAt", "asc")
  );

  const snapshot = await getDocs(q);
  const results: OutreachContact[] = [];

  for (const docSnap of snapshot.docs) {
    const d = docSnap.data();
    const nextEligible = d.nextEligibleAt?.seconds ?? 0;
    const stopped = d.stoppedReason || "";

    if (stopped) continue;

    const isEligible =
      nextEligible === 0 || nextEligible <= now;

    if (!isEligible) continue;

    const status = d.status || "pending";
    if (status === "converted" || status === "no_response") continue;

    results.push({
      email: docSnap.id,
      name: d.name ?? "",
      phone: d.phone ?? "",
      status,
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
    } as OutreachContact);
  }

  return results;
}

export async function markConverted(email: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");

  await updateDoc(doc(firestore, CONTACTS_COLLECTION, email.toLowerCase().trim()), {
    status: "converted",
    stoppedReason: "registered",
  });
}

export async function markNoResponse(email: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");

  await updateDoc(doc(firestore, CONTACTS_COLLECTION, email.toLowerCase().trim()), {
    status: "no_response",
    stoppedReason: "max_invites_reached",
  });
}

export async function markEmailFailed(email: string, reason: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc } = await import("firebase/firestore");

  await updateDoc(doc(firestore, CONTACTS_COLLECTION, email.toLowerCase().trim()), {
    emailStatus: "failed",
    stoppedReason: reason,
  });
}

export async function incrementInviteCount(
  email: string,
  delayDays: number
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp, Timestamp } = await import("firebase/firestore");

  const nextEligible = Timestamp.fromDate(
    new Date(Date.now() + delayDays * 24 * 60 * 60 * 1000)
  );

  await updateDoc(doc(firestore, CONTACTS_COLLECTION, email.toLowerCase().trim()), {
    inviteCount: (await import("firebase/firestore")).increment(1),
    lastInviteAt: serverTimestamp(),
    nextEligibleAt: nextEligible,
    emailStatus: "sent",
  });
}
