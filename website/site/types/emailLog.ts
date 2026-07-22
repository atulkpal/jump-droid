export type EmailTemplate = "acknowledgement" | "invitation" | "invitation-1" | "invitation-2" | "invitation-3" | "invitation-4" | "invitation-5" | "outreach-1" | "outreach-2" | "outreach-3" | "outreach-4" | "outreach-5" | "welcome" | "reject";

export type EmailStatus = "queued" | "sent" | "failed";

export interface EmailLogEntry {
  id: string;
  recipient: string;
  recipientName: string;
  template: EmailTemplate;
  campaign: string;
  source: string;
  status: EmailStatus;
  queuedAt: { seconds: number } | null;
  sentAt: { seconds: number } | null;
  failedAt: { seconds: number } | null;
  retryCount: number;
  providerMessageId: string;
  providerThreadId: string;
  failureReason: string;
  error: string;
}
