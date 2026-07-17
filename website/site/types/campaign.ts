export interface CampaignConfig {
  delayDays: number;
  maxInvites: number;
  delayBetweenEmailsMs: number;
  batchSize: number;
  maxEmailsPerHour: number;
}

export interface CampaignContactFields {
  inviteCount: number;
  lastInviteAt: { seconds: number } | null;
  nextEligibleAt: { seconds: number } | null;
  campaignId: string;
  emailStatus: "pending" | "sent" | "failed";
  stoppedReason: string;
}

export interface EmailQueueItem {
  email: string;
  name: string;
  inviteNumber: number;
  campaignId: string;
}

export interface CampaignProcessResult {
  processed: number;
  sent: number;
  failed: number;
  converted: number;
  noResponse: number;
}
