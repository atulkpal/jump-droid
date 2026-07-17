export interface Tester {
  docId: string;
  email: string;
  name: string;
  phone?: string;
  lastSeen?: TimestampData;
  versionName?: string;
  versionCode?: number;
  totalAppOpenTime?: number;
  todayAppOpenTime?: number;
  totalGameplayTime?: number;
  todayGameplayTime?: number;
  totalSessions?: number;
  highestScore?: number;
  rewardAdsWatched?: number;
  bannerImpressions?: number;
  continuesUsed?: number;
}

export interface TesterSession {
  id: string;
  testerEmail: string;
  testerName?: string;
  sessionStart?: TimestampData;
  sessionEnd?: TimestampData;
  appOpenTime?: number;
  gameplayTime?: number;
  bannerImpressions?: number;
  rewardAdsWatched?: number;
  continuesUsed?: number;
  highestScore?: number;
  gamesPlayed?: number;
  rocketTypes?: string[];
  zoneReached?: string;
  finalScore?: number;
  outcome?: string;
  versionName?: string;
  versionCode?: number;
  sessionStatus?: string;
}

export interface TimestampData {
  seconds: number;
  nanoseconds: number;
}
