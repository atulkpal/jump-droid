export type RecruitmentStatus =
  | "pending"
  | "approved"
  | "invited"
  | "active"
  | "inactive"
  | "completed"
  | "rejected";

export type EmailStatus = "pending" | "sent" | "failed";

export interface RecruitmentApplicant {
  docId: string;
  email: string;
  name: string;
  phone: string;
  codeJam: boolean;
  status: RecruitmentStatus;
  source: string;
  version: number;
  notes: string;
  registeredFrom: string;
  registeredAt: { seconds: number } | null;
  approvedAt: { seconds: number } | null;
  invitedAt: { seconds: number } | null;
  emailStatus: EmailStatus;
  acknowledgementSent?: boolean;
  acknowledgementError?: string;
  campaigns?: string[];
  campaignData?: Record<string, { inviteCount: number; emailStatus: string; currentStep: number }>;
}

export type VisibleStatus = Extract<
  RecruitmentStatus,
  "pending" | "approved" | "invited" | "active" | "rejected"
>;

export interface RecruitmentSummary {
  pending: number;
  approved: number;
  invited: number;
  active: number;
  rejected: number;
  emailFailed: number;
}
