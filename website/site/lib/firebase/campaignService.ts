import { getFirestore } from "./config";
import type { Firestore } from "firebase-admin/firestore";
import type {
  Campaign,
  CampaignConfig,
  CampaignContactData,
  CampaignStatus,
  PreflightCheck,
  PreflightResult,
} from "@/types/campaign";
import type { OutreachContact } from "@/types/recruitmentContacts";
import { logEventAdmin } from "./activityService";

const CONTACTS_COLLECTION = "recruitmentContacts";
const CAMPAIGNS_COLLECTION = "campaigns";
const CONFIG_DOC = "campaignConfig";

export const DEFAULT_CAMPAIGN_CONFIG: CampaignConfig = {
  delayDays: 4,
  maxInvites: 5,
  delayBetweenEmailsMs: 120000,
  batchSize: 5,
  maxEmailsPerHour: 100,
  senderAccountId: null,
  templateSequence: [],
};

function normalizeEmail(email: string): string {
  return email.toLowerCase().trim();
}

// ── Campaign ID generation: jd_camp_YYYYMMDD_NNNN ──

export async function generateCampaignId(adminFirestore: Firestore): Promise<string> {
  const today = new Date();
  const y = today.getFullYear();
  const m = String(today.getMonth() + 1).padStart(2, "0");
  const d = String(today.getDate()).padStart(2, "0");
  const prefix = `jd_camp_${y}${m}${d}`;

  const snap = await adminFirestore
    .collection(CAMPAIGNS_COLLECTION)
    .where("id", ">=", prefix)
    .where("id", "<", prefix + "_\uffff")
    .orderBy("id", "desc")
    .limit(1)
    .get();

  let seq = 1;
  if (!snap.empty) {
    const lastId = snap.docs[0].id;
    const lastSeq = parseInt(lastId.split("_").pop() || "0", 10);
    seq = (lastSeq % 9999) + 1;
  }

  return `${prefix}_${String(seq).padStart(4, "0")}`;
}

// ── Client-side methods ──

export async function listCampaigns(): Promise<Campaign[]> {
  const firestore = await getFirestore();
  const { collection, getDocs, query, orderBy } = await import("firebase/firestore");
  const q = query(collection(firestore, CAMPAIGNS_COLLECTION), orderBy("createdAt", "desc"));
  const snap = await getDocs(q);
  return snap.docs.map((d) => ({ id: d.id, ...d.data() } as Campaign));
}

export async function createCampaign(name: string, createdBy: string): Promise<Campaign> {
  const firestore = await getFirestore();
  const { collection, addDoc, doc, getDoc, serverTimestamp } = await import("firebase/firestore");

  const colRef = collection(firestore, CAMPAIGNS_COLLECTION);

  const tempRef = doc(colRef);
  const tempId = tempRef.id;

  const id = `${tempId}`;

  const campaignData = {
    id,
    name,
    status: "draft" as CampaignStatus,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    createdBy,
    scheduledStartAt: null,
    settings: { ...DEFAULT_CAMPAIGN_CONFIG },
    stats: { totalContacts: 0, emailsSent: 0, errors: 0, converted: 0, noResponse: 0 },
  };

  const { setDoc } = await import("firebase/firestore");
  await setDoc(doc(firestore, CAMPAIGNS_COLLECTION, id), campaignData);

  return { ...campaignData, id, createdAt: null, updatedAt: null, scheduledStartAt: null } as Campaign;
}

export async function getCampaign(id: string): Promise<Campaign | null> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");
  const snap = await getDoc(doc(firestore, CAMPAIGNS_COLLECTION, id));
  if (!snap.exists()) return null;
  return { id: snap.id, ...snap.data() } as Campaign;
}

export async function updateCampaign(id: string, data: Partial<Campaign>): Promise<void> {
  const firestore = await getFirestore();
  const { doc, updateDoc, serverTimestamp } = await import("firebase/firestore");
  await updateDoc(doc(firestore, CAMPAIGNS_COLLECTION, id), { ...data, updatedAt: serverTimestamp() });
}

export async function deleteCampaign(id: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, deleteDoc } = await import("firebase/firestore");
  await deleteDoc(doc(firestore, CAMPAIGNS_COLLECTION, id));
}

export async function duplicateCampaign(id: string, newName: string, createdBy: string): Promise<Campaign> {
  const source = await getCampaign(id);
  if (!source) throw new Error("Source campaign not found");

  const firestore = await getFirestore();
  const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");

  const colRef = (await import("firebase/firestore")).collection(firestore, CAMPAIGNS_COLLECTION);
  const tempRef = doc(colRef);

  const newId = tempRef.id;

  const campaignData = {
    id: newId,
    name: newName,
    status: "draft" as CampaignStatus,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    createdBy,
    scheduledStartAt: null,
    settings: { ...source.settings },
    stats: { totalContacts: 0, emailsSent: 0, errors: 0, converted: 0, noResponse: 0 },
  };

  await setDoc(doc(firestore, CAMPAIGNS_COLLECTION, newId), campaignData);

  return { ...campaignData, id: newId, createdAt: null, updatedAt: null, scheduledStartAt: null } as Campaign;
}

export async function getCampaignConfig(): Promise<CampaignConfig> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");

  const snap = await getDoc(doc(firestore, CONFIG_DOC, "default"));
  if (!snap.exists()) return { ...DEFAULT_CAMPAIGN_CONFIG };

  const d = snap.data();
  return {
    delayDays: d.delayDays ?? DEFAULT_CAMPAIGN_CONFIG.delayDays,
    maxInvites: d.maxInvites ?? DEFAULT_CAMPAIGN_CONFIG.maxInvites,
    delayBetweenEmailsMs: d.delayBetweenEmailsMs ?? DEFAULT_CAMPAIGN_CONFIG.delayBetweenEmailsMs,
    batchSize: d.batchSize ?? DEFAULT_CAMPAIGN_CONFIG.batchSize,
    maxEmailsPerHour: d.maxEmailsPerHour ?? DEFAULT_CAMPAIGN_CONFIG.maxEmailsPerHour,
    senderAccountId: d.senderAccountId ?? DEFAULT_CAMPAIGN_CONFIG.senderAccountId,
    templateSequence: d.templateSequence ?? DEFAULT_CAMPAIGN_CONFIG.templateSequence,
  };
}

export async function saveCampaignConfig(config: CampaignConfig): Promise<void> {
  const firestore = await getFirestore();
  const { doc, setDoc } = await import("firebase/firestore");
  await setDoc(doc(firestore, CONFIG_DOC, "default"), config);
}

// ── Admin SDK versions (for campaign engine running server-side) ──

export async function getGlobalCampaignConfigAdmin(adminFirestore: Firestore): Promise<CampaignConfig> {
  const snap = await adminFirestore.collection(CONFIG_DOC).doc("default").get();
  if (!snap.exists) return { ...DEFAULT_CAMPAIGN_CONFIG };

  const d = snap.data()!;
  return {
    delayDays: d.delayDays ?? DEFAULT_CAMPAIGN_CONFIG.delayDays,
    maxInvites: d.maxInvites ?? DEFAULT_CAMPAIGN_CONFIG.maxInvites,
    delayBetweenEmailsMs: d.delayBetweenEmailsMs ?? DEFAULT_CAMPAIGN_CONFIG.delayBetweenEmailsMs,
    batchSize: d.batchSize ?? DEFAULT_CAMPAIGN_CONFIG.batchSize,
    maxEmailsPerHour: d.maxEmailsPerHour ?? DEFAULT_CAMPAIGN_CONFIG.maxEmailsPerHour,
    senderAccountId: d.senderAccountId ?? DEFAULT_CAMPAIGN_CONFIG.senderAccountId,
    templateSequence: d.templateSequence ?? DEFAULT_CAMPAIGN_CONFIG.templateSequence,
  };
}

export async function getCampaignAdmin(adminFirestore: Firestore, campaignId: string): Promise<Campaign | null> {
  const snap = await adminFirestore.collection(CAMPAIGNS_COLLECTION).doc(campaignId).get();
  if (!snap.exists) return null;
  return { id: snap.id, ...snap.data() } as Campaign;
}

export async function getRunningCampaignsAdmin(adminFirestore: Firestore): Promise<Campaign[]> {
  const snap = await adminFirestore
    .collection(CAMPAIGNS_COLLECTION)
    .where("status", "==", "running")
    .get();

  return snap.docs.map((d) => ({ id: d.id, ...d.data() } as Campaign));
}

export async function getScheduledCampaignsAdmin(adminFirestore: Firestore): Promise<Campaign[]> {
  const now = new Date();
  const snap = await adminFirestore
    .collection(CAMPAIGNS_COLLECTION)
    .where("status", "==", "scheduled")
    .get();

  return snap.docs.map((d) => ({ id: d.id, ...d.data() } as Campaign));
}

export async function mergeCampaignConfig(
  global: CampaignConfig,
  campaign: Campaign | null
): Promise<CampaignConfig> {
  if (!campaign) return { ...global };
  return {
    delayDays: campaign.settings?.delayDays ?? global.delayDays,
    maxInvites: campaign.settings?.maxInvites ?? global.maxInvites,
    delayBetweenEmailsMs: campaign.settings?.delayBetweenEmailsMs ?? global.delayBetweenEmailsMs,
    batchSize: campaign.settings?.batchSize ?? global.batchSize,
    maxEmailsPerHour: campaign.settings?.maxEmailsPerHour ?? global.maxEmailsPerHour,
    senderAccountId: campaign.settings?.senderAccountId ?? global.senderAccountId,
    templateSequence: campaign.settings?.templateSequence ?? global.templateSequence,
  };
}

export async function fetchEligibleContactsForCampaignAdmin(
  adminFirestore: Firestore,
  campaignId: string,
  maxInvites: number
): Promise<OutreachContact[]> {
  const now = Date.now() / 1000;

  const snapshot = await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .where("campaigns", "array-contains", campaignId)
    .get();

  const results: OutreachContact[] = [];

  for (const docSnap of snapshot.docs) {
    const d = docSnap.data();
    const cd: CampaignContactData | undefined = d.campaignData?.[campaignId];

    const inviteCount = cd?.inviteCount ?? 0;
    if (inviteCount >= maxInvites) continue;

    const nextEligible = cd?.nextEligibleAt?.seconds ?? 0;
    if (nextEligible !== 0 && nextEligible > now) continue;

    const stopped = cd?.stoppedReason || d.stoppedReason || "";
    if (stopped) continue;

    const status = cd?.status || d.status || "pending";
    if (status === "converted" || status === "no_response" || status === "deleting" || status === "unsubscribed") continue;

    results.push({
      email: docSnap.id,
      name: d.name ?? "",
      phone: d.phone ?? "",
      status,
      source: d.source ?? "manual",
      importedAt: d.importedAt ?? null,
      registeredAt: d.registeredAt ?? null,
      notes: d.notes ?? "",
      campaigns: d.campaigns ?? [],
      campaignData: d.campaignData ?? {},
    } as OutreachContact);
  }

  return results;
}

export async function fetchAllEligibleContactsAdmin(
  adminFirestore: Firestore,
  maxInvites: number
): Promise<OutreachContact[]> {
  const now = Date.now() / 1000;

  const snapshot = await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .get();

  const results: OutreachContact[] = [];

  for (const docSnap of snapshot.docs) {
    const d = docSnap.data();
    if (!d.campaigns || !Array.isArray(d.campaigns) || d.campaigns.length === 0) continue;

    const stopped = d.stoppedReason || "";
    if (stopped) continue;

    const status = d.status || "pending";
    if (status === "converted" || status === "no_response" || status === "deleting") continue;

    for (const cid of d.campaigns) {
      const cd: CampaignContactData | undefined = d.campaignData?.[cid];
      const inviteCount = cd?.inviteCount ?? 0;
      if (inviteCount >= maxInvites) continue;

      const nextEligible = cd?.nextEligibleAt?.seconds ?? 0;
      if (nextEligible !== 0 && nextEligible > now) continue;

      const campaignStatus = cd?.status || "pending";
      if (campaignStatus === "converted" || campaignStatus === "no_response" || campaignStatus === "replied") continue;

      results.push({
        email: docSnap.id,
        name: d.name ?? "",
        phone: d.phone ?? "",
        status,
        source: d.source ?? "manual",
        importedAt: d.importedAt ?? null,
        registeredAt: d.registeredAt ?? null,
        notes: d.notes ?? "",
        campaigns: d.campaigns ?? [],
        campaignData: d.campaignData ?? {},
      } as OutreachContact);
    }
  }

  return results;
}

export async function updateCampaignContactDataAdmin(
  adminFirestore: Firestore,
  email: string,
  campaignId: string,
  data: Partial<CampaignContactData>
): Promise<void> {
  const key = normalizeEmail(email);
  const updatePath: Record<string, any> = {};
  for (const [k, v] of Object.entries(data)) {
    updatePath[`campaignData.${campaignId}.${k}`] = v;
  }

  await adminFirestore.collection(CONTACTS_COLLECTION).doc(key).update(updatePath).catch(() => {});
}

export async function addContactToCampaignAdmin(
  adminFirestore: Firestore,
  email: string,
  campaignId: string,
  importedBy?: string
): Promise<void> {
  const key = normalizeEmail(email);
  const ref = adminFirestore.collection(CONTACTS_COLLECTION).doc(key);

  const snap = await ref.get();

  if (snap.exists && snap.data()!.status === "unsubscribed") {
    return;
  }

  if (!snap.exists) {
    await ref.set({
      name: key,
      email: key,
      phone: "",
      status: "pending",
      source: "campaign",
      importedBy: importedBy || "System",
      importedAt: new Date(),
      lastInviteAt: null,
      registeredAt: null,
      notes: "",
      inviteCount: 0,
      nextEligibleAt: null,
      campaignId: "",
      campaigns: [campaignId],
      campaignData: {
        [campaignId]: {
          inviteCount: 0,
          lastInviteAt: null,
          nextEligibleAt: null,
          emailStatus: "pending",
          stoppedReason: "",
          assignedSender: null,
          currentStep: 0,
          startedAt: null,
          status: "pending",
        },
      },
      emailStatus: "pending",
      stoppedReason: "",
    });
    logEventAdmin(adminFirestore, key, "contact_added_to_campaign", `Added to campaign ${campaignId}`, campaignId).catch(() => {});
    return;
  }

  const d = snap.data()!;
  const campaigns: string[] = d.campaigns || [];
  if (campaigns.includes(campaignId)) {
    logEventAdmin(adminFirestore, key, "outreach_duplicate", `Already in campaign ${campaignId}`, campaignId).catch(() => {});
    return;
  }

  campaigns.push(campaignId);

  const cd = d.campaignData || {};
  cd[campaignId] = {
    inviteCount: 0,
    lastInviteAt: null,
    nextEligibleAt: null,
    emailStatus: "pending",
    stoppedReason: "",
    assignedSender: null,
    currentStep: 0,
    startedAt: null,
    status: "pending",
  };

  await ref.update({
    campaigns,
    campaignData: cd,
  });
  logEventAdmin(adminFirestore, key, "contact_added_to_campaign", `Added to campaign ${campaignId}`, campaignId).catch(() => {});
}

export async function addContactToCampaign(
  email: string,
  campaignId: string,
  importedBy?: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, getDoc, updateDoc, setDoc, serverTimestamp } = await import("firebase/firestore");
  const key = normalizeEmail(email);
  const ref = doc(firestore, CONTACTS_COLLECTION, key);

  const snap = await getDoc(ref);

  if (snap.data()?.status === "unsubscribed") return;

  if (!snap.exists) {
    await setDoc(ref, {
      name: key,
      email: key,
      phone: "",
      status: "pending",
      source: "campaign",
      importedBy: importedBy || "System",
      importedAt: serverTimestamp(),
      lastInviteAt: null,
      registeredAt: null,
      notes: "",
      inviteCount: 0,
      nextEligibleAt: null,
      campaignId: "",
      campaigns: [campaignId],
      campaignData: {
        [campaignId]: {
          inviteCount: 0,
          lastInviteAt: null,
          nextEligibleAt: null,
          emailStatus: "pending",
          stoppedReason: "",
          assignedSender: null,
          currentStep: 0,
          startedAt: null,
          status: "pending",
        },
      },
      emailStatus: "pending",
      stoppedReason: "",
    });
    const { logEvent } = await import("./activityService");
    logEvent(key, "contact_added_to_campaign", `Added to campaign ${campaignId}`, campaignId).catch(() => {});
    return;
  }

  const d = snap.data()!;
  const campaigns: string[] = d.campaigns || [];
  if (campaigns.includes(campaignId)) {
    const { logEvent } = await import("./activityService");
    logEvent(key, "outreach_duplicate", `Already in campaign ${campaignId}`, campaignId).catch(() => {});
    return;
  }

  campaigns.push(campaignId);

  const cd = d.campaignData || {};
  cd[campaignId] = {
    inviteCount: 0,
    lastInviteAt: null,
    nextEligibleAt: null,
    emailStatus: "pending",
    stoppedReason: "",
    assignedSender: null,
    currentStep: 0,
    startedAt: null,
    status: "pending",
  };

  await updateDoc(ref, { campaigns, campaignData: cd });
  const { logEvent } = await import("./activityService");
  logEvent(key, "contact_added_to_campaign", `Added to campaign ${campaignId}`, campaignId).catch(() => {});
}

export async function removeContactFromCampaign(
  email: string,
  campaignId: string
): Promise<void> {
  const firestore = await getFirestore();
  const { doc, getDoc, updateDoc } = await import("firebase/firestore");
  const key = normalizeEmail(email);
  const ref = doc(firestore, CONTACTS_COLLECTION, key);

  const snap = await getDoc(ref);
  if (!snap.exists) return;

  const d = snap.data()!;
  const campaigns: string[] = (d.campaigns || []).filter((c: string) => c !== campaignId);
  const cd = { ...(d.campaignData || {}) };
  delete cd[campaignId];

  await updateDoc(ref, { campaigns, campaignData: cd });
  const { logEvent } = await import("./activityService");
  logEvent(key, "contact_removed_from_campaign", `Removed from campaign ${campaignId}`, campaignId).catch(() => {});
}

export async function setCampaignStatusAdmin(
  adminFirestore: Firestore,
  campaignId: string,
  status: CampaignStatus
): Promise<void> {
  await adminFirestore
    .collection(CAMPAIGNS_COLLECTION)
    .doc(campaignId)
    .update({ status, updatedAt: new Date() });
}

// ── Pre-flight validation ──

export async function runPreflightAdmin(
  adminFirestore: Firestore,
  campaignId: string
): Promise<PreflightResult> {
  const campaign = await getCampaignAdmin(adminFirestore, campaignId);
  const checks: PreflightCheck[] = [];

  if (!campaign) {
    return { passed: false, checks: [{ label: "Campaign", status: "error", message: "Campaign not found" }] };
  }

  // Sender check
  const senderId = campaign.settings.senderAccountId;
  if (senderId) {
    const senderSnap = await adminFirestore.collection("emailAccounts").doc(senderId).get();
    if (!senderSnap.exists) {
      checks.push({ label: "Sender Account", status: "error", message: `Sender "${senderId}" not found` });
    } else if (senderSnap.data()?.status !== "connected") {
      checks.push({ label: "Sender Account", status: "error", message: `Sender "${senderId}" is not connected (status: ${senderSnap.data()?.status})` });
    } else {
      checks.push({ label: "Sender Account", status: "ok", message: `Sender "${senderId}" is connected` });
    }
  } else {
    const defaultSnap = await adminFirestore
      .collection("emailAccounts")
      .where("isDefault", "==", true)
      .where("status", "==", "connected")
      .limit(1)
      .get();
    if (defaultSnap.empty) {
      checks.push({ label: "Default Sender", status: "error", message: "No default sender configured" });
    } else {
      checks.push({ label: "Default Sender", status: "ok", message: "Default sender is connected" });
    }
  }

  // Templates check (check campaign subcollection + global overrides + code defaults always available)
  const templateSnap = await adminFirestore.collection("campaigns").doc(campaignId).collection("templates").get();
  const campaignTemplateCount = templateSnap.docs.length;
  if (campaignTemplateCount === 0) {
    checks.push({ label: "Templates", status: "ok", message: "Using code defaults (no campaign-specific overrides)" });
  } else {
    checks.push({ label: "Templates", status: "ok", message: `${campaignTemplateCount} campaign-specific template override(s) found` });
  }

  // Scheduling check
  if (campaign.scheduledStartAt) {
    const scheduledSeconds = campaign.scheduledStartAt.seconds ?? 0;
    if (scheduledSeconds <= Date.now() / 1000) {
      checks.push({ label: "Schedule", status: "warn", message: "Scheduled start time is in the past" });
    } else {
      checks.push({ label: "Schedule", status: "ok", message: `Scheduled to start ${new Date(scheduledSeconds * 1000).toISOString()}` });
    }
  } else {
    checks.push({ label: "Schedule", status: "ok", message: "No scheduled start (starts immediately)" });
  }

  // Contacts check
  const contactsSnap = await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .where("campaigns", "array-contains", campaignId)
    .limit(1)
    .get();
  if (contactsSnap.empty) {
    checks.push({ label: "Contacts", status: "error", message: "No contacts enrolled in this campaign" });
  } else {
    checks.push({ label: "Contacts", status: "ok", message: "Contacts found" });
  }

  // Limits check
  const cfg = campaign.settings;
  if (cfg.batchSize < 1) checks.push({ label: "Batch Size", status: "error", message: "Batch size must be at least 1" });
  else checks.push({ label: "Batch Size", status: "ok", message: `${cfg.batchSize}` });

  if (cfg.maxInvites < 1) checks.push({ label: "Max Invites", status: "error", message: "Max invites must be at least 1" });
  else checks.push({ label: "Max Invites", status: "ok", message: `${cfg.maxInvites}` });

  const passed = !checks.some((c) => c.status === "error");

  return { passed, checks };
}

// ── Legacy single-campaign helpers (backward compat) ──

export async function getCampaignConfigAdmin(adminFirestore: Firestore): Promise<CampaignConfig> {
  return getGlobalCampaignConfigAdmin(adminFirestore);
}

export async function markConvertedAdmin(
  adminFirestore: Firestore,
  email: string
): Promise<void> {
  await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .doc(normalizeEmail(email))
    .update({
      status: "converted",
      stoppedReason: "registered",
    });
}

export async function markNoResponseAdmin(
  adminFirestore: Firestore,
  email: string
): Promise<void> {
  await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .doc(normalizeEmail(email))
    .update({
      status: "no_response",
      stoppedReason: "max_invites_reached",
    });
}

export async function markEmailFailedAdmin(
  adminFirestore: Firestore,
  email: string,
  reason: string
): Promise<void> {
  await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .doc(normalizeEmail(email))
    .update({
      emailStatus: "failed",
      stoppedReason: reason,
    });
}

export async function hardDeleteExpiredSoftDeletesAdmin(
  adminFirestore: Firestore
): Promise<number> {
  const now = Date.now() / 1000;
  const snapshot = await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .where("status", "==", "deleting")
    .get();

  let deleted = 0;
  for (const docSnap of snapshot.docs) {
    const d = docSnap.data();
    const scheduledDelete = d.scheduledDeleteAt?.seconds ?? Infinity;
    if (scheduledDelete <= now) {
      await docSnap.ref.delete();
      deleted++;
    }
  }
  return deleted;
}

export async function incrementInviteCountAdmin(
  adminFirestore: Firestore,
  email: string,
  delayDays: number
): Promise<void> {
  const nextEligible = new Date(Date.now() + delayDays * 24 * 60 * 60 * 1000);
  const snap = await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .doc(normalizeEmail(email))
    .get();
  const current = snap.data()?.inviteCount ?? 0;

  await adminFirestore
    .collection(CONTACTS_COLLECTION)
    .doc(normalizeEmail(email))
    .update({
      inviteCount: current + 1,
      lastInviteAt: new Date(),
      nextEligibleAt: nextEligible,
      emailStatus: "sent",
    });
}
