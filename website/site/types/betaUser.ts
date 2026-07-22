export interface BetaUserRegistration {
  email: string;
  name?: string;
  phone?: string;
  codeJam?: boolean;
  convertedFrom?: string;
}

export interface BetaUserDoc extends BetaUserRegistration {
  status: "pending";
  source: string;
  version: number;
  notes: string;
  registeredFrom: string;
  registeredAt: unknown;
  acknowledgementSent: boolean;
}
