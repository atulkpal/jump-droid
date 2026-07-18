export type EmailAccountStatus = "connected" | "expired" | "error" | "deleted";

export type EmailProvider = "gmail";

export interface EmailAccount {
  email: string;
  displayName: string;
  refreshToken: string;
  accessToken: string;
  expiryDate: number;
  status: EmailAccountStatus;
  isDefault: boolean;
  createdBy: string;
  createdAt: { seconds: number } | null;
  lastUsedAt: { seconds: number } | null;
  errorMessage: string | null;
  provider: EmailProvider;
}
