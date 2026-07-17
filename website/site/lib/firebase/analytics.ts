import type { Tester, TesterSession } from "@/types/tester";
import type { DashboardStats, DailySummary, EligibilityInfo } from "@/types/stats";
import { DAILY_GOAL_SECONDS, BETA_END_DATE } from "@/types/stats";

export function computeDashboardStats(
  testers: Tester[],
  sessions: TesterSession[]
): DashboardStats {
  const totalSessions = sessions.length;
  const gamesPlayed = sessions.reduce((sum, s) => sum + (s.gamesPlayed ?? 1), 0);
  const totalGameplayTime = sessions.reduce((sum, s) => sum + (s.gameplayTime ?? 0), 0);
  const bannerImpressions = sessions.reduce((sum, s) => sum + (s.bannerImpressions ?? 0), 0);
  const rewardAds = sessions.reduce((sum, s) => sum + (s.rewardAdsWatched ?? 0), 0);

  return {
    totalTesters: testers.length,
    totalSessions,
    gamesPlayed,
    totalGameplayTime,
    bannerImpressions,
    rewardAds,
    estimatedRevenue: 0,
  };
}

export function computeDailySummaries(sessions: TesterSession[]): DailySummary[] {
  const dayMap = new Map<string, DailySummary & { _testers: Set<string> }>();

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
      });
    }

    const entry = dayMap.get(date)!;
    entry.sessions += 1;
    entry.games += s.gamesPlayed ?? 1;
    entry.totalPlayTime += s.gameplayTime ?? 0;
    if (s.testerEmail) entry._testers.add(s.testerEmail);
  }

  return Array.from(dayMap.values()).map(({ _testers, ...rest }) => ({
    ...rest,
    activeTesters: _testers.size,
  })).sort((a, b) => b.date.localeCompare(a.date));
}

export function computeEligibility(sessions: TesterSession[]): EligibilityInfo {
  const betaEnd = new Date(BETA_END_DATE);
  const today = new Date();
  const eligibilityStart = new Date("2026-07-01");

  const dayPlaytimeMap = new Map<string, number>();

  for (const s of sessions) {
    if (!s.sessionStart) continue;
    const date = new Date(s.sessionStart.seconds * 1000).toISOString().split("T")[0];
    const current = dayPlaytimeMap.get(date) ?? 0;
    dayPlaytimeMap.set(date, current + (s.gameplayTime ?? 0));
  }

  let eligibleDays = 0;
  const cursor = new Date(eligibilityStart);

  while (cursor <= today && cursor <= betaEnd) {
    const dateStr = cursor.toISOString().split("T")[0];
    const playtime = dayPlaytimeMap.get(dateStr) ?? 0;
    if (playtime >= DAILY_GOAL_SECONDS) {
      eligibleDays += 1;
    }
    cursor.setDate(cursor.getDate() + 1);
  }

  const remainingMs = betaEnd.getTime() - today.getTime();
  const totalRequiredDays = Math.max(
    0,
    Math.ceil(remainingMs / (1000 * 60 * 60 * 24))
  );

  return {
    eligibleDays,
    totalRequiredDays,
    isEligible: eligibleDays >= totalRequiredDays || totalRequiredDays <= 0,
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
