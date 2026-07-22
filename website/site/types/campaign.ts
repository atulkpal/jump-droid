export type CampaignStatus =
  | "draft"
  | "scheduled"
  | "running"
  | "paused"
  | "completed"
  | "failed"
  | "cancelled";

export interface CampaignConfig {
  delayDays: number;
  maxInvites: number;
  delayBetweenEmailsMs: number;
  batchSize: number;
  maxEmailsPerHour: number;
  senderAccountId: string | null;
  templateSequence?: string[];
}

export interface CampaignContactData {
  inviteCount: number;
  lastInviteAt: { seconds: number } | null;
  nextEligibleAt: { seconds: number } | null;
  emailStatus: "pending" | "sent" | "failed";
  stoppedReason: string;
  assignedSender: string | null;
  currentStep: number;
  startedAt: { seconds: number } | null;
  replied: boolean;
  repliedAt: { seconds: number } | null;
  replySnippet?: string;
  opened?: boolean;
  openedAt?: { seconds: number } | null;
  status?: "pending" | "invited" | "replied" | "converted" | "no_response" | "failed" | "bounced" | "unsubscribed";
}

export interface Campaign {
  id: string;
  name: string;
  status: CampaignStatus;
  createdAt: { seconds: number } | null;
  createdDate?: string;
  updatedAt: { seconds: number } | null;
  startedAt?: { seconds: number } | null;
  createdBy: string;
  scheduledStartAt: { seconds: number } | null;
  settings: CampaignConfig;
  stats: {
    totalContacts: number;
    emailsSent: number;
    contacted: number;
    pending: number;
    totalSends: number;
    errors: number;
    converted: number;
    noResponse: number;
    bounced: number;
  };
  contactBreakdown?: Record<string, number>;
  manualSendOverride?: {
    active: boolean;
    startedAt?: { seconds: number };
  };
  lastDetectedAt?: { seconds: number } | null;
}

export interface PreflightCheck {
  label: string;
  status: "ok" | "warn" | "error";
  message: string;
}

export interface PreflightResult {
  passed: boolean;
  checks: PreflightCheck[];
}

export interface CampaignProcessResult {
  campaignId: string;
  processed: number;
  sent: number;
  failed: number;
  converted: number;
  noResponse: number;
}
