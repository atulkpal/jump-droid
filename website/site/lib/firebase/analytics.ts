import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardStats, DailySummary, EligibilityInfo } from "@/types/stats";
import type { DashboardConfig } from "@/types/config";
import { computeRevenue } from "./revenue";

export function computeDashboardStats(
  testers: Tester[],
  sessions: TesterSession[],
  config: DashboardConfig
): DashboardStats {
  const totalSessions = sessions.length;
  const gamesPlayed = sessions.reduce((sum, s) => sum + (s.gamesPlayed ?? 1), 0);
  const totalGameplayTime = sessions.reduce((sum, s) => sum + (s.gameplayTime ?? 0), 0);
  const bannerImpressions = sessions.reduce((sum, s) => sum + (s.bannerImpressions ?? 0), 0);
  const rewardAds = sessions.reduce((sum, s) => sum + (s.rewardAdsWatched ?? 0), 0);
  const revenue = computeRevenue(bannerImpressions, rewardAds, config.revenue);

  return {
    totalTesters: testers.length,
    totalSessions,
    gamesPlayed,
    totalGameplayTime,
    bannerImpressions,
    rewardAds,
    estimatedRevenue: revenue.totalRevenue,
  };
}

export function computeDailySummaries(
  sessions: TesterSession[],
  config: DashboardConfig
): DailySummary[] {
  const dayMap = new Map<
    string,
    DailySummary & { _testers: Set<string>; _bannerImpressions: number; _rewardAds: number }
  >();

  for (const s of sessions) {
    if (!s.sessionStart) continue;
    const date = new Date(s.sessionStart.seconds * 1000).toISOString().split("T")[0];

    if (!dayMap.has(date)) {
      dayMap.set(date, {
        date,
        activeTesters: 0,
        sessions: 0,
        games: 0,
        totalPlayTime: 0,
        estimatedRevenue: 0,
        _testers: new Set(),
        _bannerImpressions: 0,
        _rewardAds: 0,
      });
    }

    const entry = dayMap.get(date)!;
    entry.sessions += 1;
    entry.games += s.gamesPlayed ?? 1;
    entry.totalPlayTime += s.gameplayTime ?? 0;
    entry._bannerImpressions += s.bannerImpressions ?? 0;
    entry._rewardAds += s.rewardAdsWatched ?? 0;
    if (s.testerEmail) entry._testers.add(s.testerEmail);
  }

  return Array.from(dayMap.values())
    .map(({ _testers, _bannerImpressions, _rewardAds, ...rest }) => {
      const revenue = computeRevenue(_bannerImpressions, _rewardAds, config.revenue);
      return {
        ...rest,
        activeTesters: _testers.size,
        estimatedRevenue: revenue.totalRevenue,
      };
    })
    .sort((a, b) => b.date.localeCompare(a.date));
}

export function computeEligibility(
  sessions: TesterSession[],
  config: DashboardConfig
): EligibilityInfo {
  const goalSeconds = config.beta.requiredMinutes * 60;
  const start = new Date(config.beta.startDate);
  const end = new Date(config.beta.endDate);
  const today = new Date();

  const dayPlaytimeMap = new Map<string, number>();

  for (const s of sessions) {
    if (!s.sessionStart) continue;
    const date = new Date(s.sessionStart.seconds * 1000).toISOString().split("T")[0];
    const current = dayPlaytimeMap.get(date) ?? 0;
    dayPlaytimeMap.set(date, current + (s.gameplayTime ?? 0));
  }

  let eligibleDays = 0;
  const cursor = new Date(start);

  while (cursor <= today && cursor <= end) {
    const dateStr = cursor.toISOString().split("T")[0];
    const playtime = dayPlaytimeMap.get(dateStr) ?? 0;
    if (playtime >= goalSeconds) {
      eligibleDays += 1;
    }
    cursor.setDate(cursor.getDate() + 1);
  }

  return {
    eligibleDays,
    totalRequiredDays: config.beta.requiredDays,
    isEligible: eligibleDays >= config.beta.requiredDays || today > end,
  };
}

export function countTodayActiveTesters(sessions: TesterSession[]): number {
  const today = new Date().toISOString().split("T")[0];
  const unique = new Set<string>();
  for (const s of sessions) {
    if (!s.sessionStart) continue;
    const date = new Date(s.sessionStart.seconds * 1000).toISOString().split("T")[0];
    if (date === today && s.testerEmail) {
      unique.add(s.testerEmail);
    }
  }
  return unique.size;
}

export function formatDuration(seconds: number): string {
  const hrs = Math.floor(seconds / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  if (hrs > 0) return `${hrs}h ${mins}m`;
  return `${mins}m`;
}

export function computeDaysRemaining(endDate: string): number {
  const end = new Date(endDate);
  const today = new Date();
  const diff = end.getTime() - today.getTime();
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
}

export function computeCurrentDay(startDate: string): number {
  const start = new Date(startDate);
  const today = new Date();
  const diff = today.getTime() - start.getTime();
  return Math.max(1, Math.floor(diff / (1000 * 60 * 60 * 24)) + 1);
}
