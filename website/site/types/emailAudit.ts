export type EmailAuditEventType =
  | "oauth_connected"
  | "oauth_reconnected"
  | "oauth_disconnected"
  | "oauth_error"
  | "default_sender_changed"
  | "account_removed"
  | "send_success"
  | "send_failed";

export interface EmailAuditEntry {
  id: string;
  eventType: EmailAuditEventType;
  accountEmail: string;
  details: string;
  performedBy: string;
  createdAt: { seconds: number } | null;
}
