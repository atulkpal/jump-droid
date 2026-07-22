import type { CampaignContactData } from "./campaign";

export type OutreachStatus = "pending" | "invited" | "registered" | "converted" | "no_response" | "deleting" | "unsubscribed";

export interface OutreachContact {
  email: string;
  name: string;
  phone: string;
  status: OutreachStatus;
  source: string;
  importedAt: { seconds: number } | null;
  importedBy: string;
  registeredAt: { seconds: number } | null;
  notes: string;
  campaigns: string[];
  campaignData: Record<string, CampaignContactData>;
  scheduledDeleteAt?: { seconds: number } | null;
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
