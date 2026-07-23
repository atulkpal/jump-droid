import type { Firestore } from "firebase-admin/firestore";

export type DebugSeverity = "info" | "warn" | "error";

export interface DebugLogEntry {
  timestamp: Date;
  eventType: string;
  severity: DebugSeverity;
  campaignId: string | null;
  contactEmail: string | null;
  message: string;
  data: string | null;
}

function formatLog(entry: DebugLogEntry): string {
  const ts = entry.timestamp.toISOString();
  const sev = entry.severity.toUpperCase().padEnd(5);
  const ctx = [entry.campaignId, entry.contactEmail].filter(Boolean).join("|");
  return `[${ts}] [${sev}] [${entry.eventType}]${ctx ? ` [${ctx}]` : ""} ${entry.message}${entry.data ? ` | ${entry.data}` : ""}`;
}

export function logDebug(
  eventType: string,
  severity: DebugSeverity,
  message: string,
  options?: {
    campaignId?: string | null;
    contactEmail?: string | null;
    data?: Record<string, unknown> | null;
  }
): void {
  const entry: DebugLogEntry = {
    timestamp: new Date(),
    eventType,
    severity,
    campaignId: options?.campaignId ?? null,
    contactEmail: options?.contactEmail ?? null,
    message,
    data: options?.data ? JSON.stringify(options.data) : null,
  };

  console.log(formatLog(entry));
}

export async function logDebugAdmin(
  adminFirestore: Firestore,
  eventType: string,
  severity: DebugSeverity,
  message: string,
  options?: {
    campaignId?: string | null;
    contactEmail?: string | null;
    data?: Record<string, unknown> | null;
  }
): Promise<void> {
  const entry: DebugLogEntry = {
    timestamp: new Date(),
    eventType,
    severity,
    campaignId: options?.campaignId ?? null,
    contactEmail: options?.contactEmail ?? null,
    message,
    data: options?.data ? JSON.stringify(options.data) : null,
  };

  console.log(formatLog(entry));

  try {
    await adminFirestore.collection("systemLogs").add({
      ...entry,
      timestamp: entry.timestamp,
    });
  } catch {
    // Non-critical — debug logging must never throw
  }
}
