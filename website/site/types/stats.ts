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


