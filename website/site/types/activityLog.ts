export type ActivityEventType =
  | "registered"
  | "acknowledgement_sent"
  | "approved"
  | "welcome_email_sent"
  | "welcome_email_failed"
  | "activated"
  | "deleted"
  | "invitation_sent"
  | "invitation_failed"
  | "contact_converted"
  | "contact_no_response"
  | "outreach_duplicate";

export interface ActivityLogEntry {
  id: string;
  applicantEmail: string;
  eventType: ActivityEventType;
  details: string;
  createdAt: { seconds: number } | null;
}
