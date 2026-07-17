export type OutreachStatus = "pending" | "invited" | "registered" | "failed" | "converted" | "no_response";

export interface OutreachContact {
  email: string;
  name: string;
  phone: string;
  status: OutreachStatus;
  source: string;
  importedAt: { seconds: number } | null;
  lastInviteAt: { seconds: number } | null;
  registeredAt: { seconds: number } | null;
  notes: string;
  inviteCount: number;
  nextEligibleAt: { seconds: number } | null;
  campaignId: string;
  emailStatus: "pending" | "sent" | "failed";
  stoppedReason: string;
}

export interface CsvRow {
  name: string;
  email: string;
  phone: string;
}

export type DuplicateCheckResult =
  | { status: "ok" }
  | { status: "exists_in_outreach" }
  | { status: "already_applicant" }
  | { status: "already_approved" }
  | { status: "already_active" };
