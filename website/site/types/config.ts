export interface DashboardConfig {
  beta: {
    startDate: string;
    endDate: string;
    requiredDays: number;
    requiredMinutes: number;
    requirementMode: "daily" | "total" | "both";
    requiredTotalHours: number;
  };
  revenue: {
    bannerEcpmUsd: number;
    rewardedEcpmUsd: number;
    usdToInr: number;
    exchangeRateUpdatedAt: TimestampData | null;
  };
  updatedAt?: TimestampData;
}

export interface TimestampData {
  seconds: number;
  nanoseconds: number;
}
