export interface DashboardStats {
  totalTesters: number;
  totalSessions: number;
  gamesPlayed: number;
  totalGameplayTime: number;
  bannerImpressions: number;
  rewardAds: number;
  estimatedRevenue: number;
}

export interface DailySummary {
  date: string;
  activeTesters: number;
  sessions: number;
  games: number;
  totalPlayTime: number;
  estimatedRevenue: number;
}

export interface EligibilityInfo {
  eligibleDays: number;
  totalRequiredDays: number;
  isEligible: boolean;
}

export const DAILY_GOAL_SECONDS = 1800;
export const BETA_END_DATE = "2026-08-05";
export const REVENUE_PER_REWARD_AD = 0.02;
export const REVENUE_PER_BANNER_IMPRESSION = 0.001;
