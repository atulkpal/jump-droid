import type { DashboardConfig } from "@/types/config";
import { getFirestore } from "./config";

const COLLECTION = "dashboardConfig";
const DOC = "settings";

export const DEFAULT_CONFIG: DashboardConfig = {
  beta: {
    startDate: "2026-07-16",
    endDate: "2026-08-05",
    requiredDays: 14,
    requiredMinutes: 30,
  },
  revenue: {
    bannerEcpmUsd: 0.23,
    rewardedEcpmUsd: 1.80,
    usdToInr: 87.25,
    exchangeRateUpdatedAt: null,
  },
};

export function getDefaultConfig(): DashboardConfig {
  return JSON.parse(JSON.stringify(DEFAULT_CONFIG));
}

export async function fetchConfig(): Promise<DashboardConfig | null> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");
  const snap = await getDoc(doc(firestore, COLLECTION, DOC));
  if (!snap.exists()) return null;
  const data = snap.data();
  return {
    beta: {
      startDate: data.beta?.startDate ?? DEFAULT_CONFIG.beta.startDate,
      endDate: data.beta?.endDate ?? DEFAULT_CONFIG.beta.endDate,
      requiredDays: data.beta?.requiredDays ?? DEFAULT_CONFIG.beta.requiredDays,
      requiredMinutes: data.beta?.requiredMinutes ?? DEFAULT_CONFIG.beta.requiredMinutes,
    },
    revenue: {
      bannerEcpmUsd: data.revenue?.bannerEcpmUsd ?? DEFAULT_CONFIG.revenue.bannerEcpmUsd,
      rewardedEcpmUsd: data.revenue?.rewardedEcpmUsd ?? DEFAULT_CONFIG.revenue.rewardedEcpmUsd,
      usdToInr: data.revenue?.usdToInr ?? DEFAULT_CONFIG.revenue.usdToInr,
      exchangeRateUpdatedAt: data.revenue?.exchangeRateUpdatedAt ?? null,
    },
    updatedAt: data.updatedAt,
  };
}

export async function updateConfig(data: DashboardConfig): Promise<void> {
  const firestore = await getFirestore();
  const { doc, setDoc, serverTimestamp } = await import("firebase/firestore");
  await setDoc(
    doc(firestore, COLLECTION, DOC),
    {
      beta: data.beta,
      revenue: {
        ...data.revenue,
      },
      updatedAt: serverTimestamp(),
    },
    { merge: true }
  );
}
