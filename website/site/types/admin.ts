export type AdminRole = "owner" | "admin" | "user";

export interface AdminDoc {
  uid: string;
  email: string;
  displayName: string;
  role: AdminRole;
  createdAt?: { seconds: number };
}
