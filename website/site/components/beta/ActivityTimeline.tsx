"use client";

import { useState, useEffect } from "react";
import type { ActivityLogEntry, ActivityEventType } from "@/types/activityLog";
import { fetchTimeline } from "@/lib/firebase/activityService";

interface Props {
  applicantEmail: string;
}

const EVENT_CONFIG: Partial<Record<
  ActivityEventType,
  { label: string; color: string; dot: string }
>> = {
  registered: {
    label: "Registered",
    color: "text-slate-400",
    dot: "bg-slate-500",
  },
  approved: {
    label: "Approved",
    color: "text-green-400",
    dot: "bg-green-400",
  },
  welcome_email_sent: {
    label: "Welcome Email Sent",
    color: "text-cyan-400",
    dot: "bg-cyan-400",
  },
  welcome_email_failed: {
    label: "Welcome Email Failed",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  activated: {
    label: "Activated",
    color: "text-cyan-300",
    dot: "bg-cyan-300",
  },
  deleted: {
    label: "Deleted",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  acknowledgement_sent: {
    label: "Acknowledgement Sent",
    color: "text-cyan-400",
    dot: "bg-cyan-400",
  },
  invitation_sent: {
    label: "Invitation Sent",
    color: "text-blue-400",
    dot: "bg-blue-400",
  },
  invitation_failed: {
    label: "Invitation Failed",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  contact_converted: {
    label: "Converted",
    color: "text-purple-400",
    dot: "bg-purple-400",
  },
  contact_no_response: {
    label: "No Response",
    color: "text-slate-500",
    dot: "bg-slate-500",
  },
  outreach_duplicate: {
    label: "Duplicate Outreach",
    color: "text-amber-400",
    dot: "bg-amber-400",
  },
  rejected: {
    label: "Rejected",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  campaign_created: {
    label: "Campaign Created",
    color: "text-cyan-400",
    dot: "bg-cyan-400",
  },
  campaign_scheduled: {
    label: "Campaign Scheduled",
    color: "text-yellow-400",
    dot: "bg-yellow-400",
  },
  campaign_started: {
    label: "Campaign Started",
    color: "text-green-400",
    dot: "bg-green-400",
  },
  campaign_paused: {
    label: "Campaign Paused",
    color: "text-orange-400",
    dot: "bg-orange-400",
  },
  campaign_resumed: {
    label: "Campaign Resumed",
    color: "text-green-400",
    dot: "bg-green-400",
  },
  campaign_completed: {
    label: "Campaign Completed",
    color: "text-blue-400",
    dot: "bg-blue-400",
  },
  campaign_failed: {
    label: "Campaign Failed",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  campaign_cancelled: {
    label: "Campaign Cancelled",
    color: "text-slate-500",
    dot: "bg-slate-500",
  },
  preflight_passed: {
    label: "Pre-flight Passed",
    color: "text-green-400",
    dot: "bg-green-400",
  },
  preflight_failed: {
    label: "Pre-flight Failed",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  sender_fallback: {
    label: "Sender Fallback",
    color: "text-amber-400",
    dot: "bg-amber-400",
  },
  contact_added_to_campaign: {
    label: "Added to Campaign",
    color: "text-cyan-300",
    dot: "bg-cyan-300",
  },
  email_run_completed: {
    label: "Email Run Completed",
    color: "text-blue-300",
    dot: "bg-blue-300",
  },
  unsubscribed: {
    label: "Unsubscribed",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  resubscribed: {
    label: "Resubscribed",
    color: "text-green-400",
    dot: "bg-green-400",
  },
  skipped_unsubscribed: {
    label: "Skipped (Unsubscribed)",
    color: "text-red-400/60",
    dot: "bg-red-500/50",
  },
  manual_send_triggered: {
    label: "Manual Send",
    color: "text-orange-400",
    dot: "bg-orange-400",
  },
  replied: {
    label: "Replied",
    color: "text-green-400",
    dot: "bg-green-400",
  },
  rate_limited: {
    label: "Rate Limited",
    color: "text-amber-400",
    dot: "bg-amber-400",
  },
  system_error: {
    label: "System Error",
    color: "text-red-400",
    dot: "bg-red-400",
  },
  email_opened: {
    label: "Email Opened",
    color: "text-cyan-400",
    dot: "bg-cyan-400",
  },
};

function formatTimestamp(ts: { seconds: number } | null): string {
  if (!ts?.seconds) return "";
  return new Date(ts.seconds * 1000).toLocaleString();
}

export default function ActivityTimeline({ applicantEmail }: Props) {
  const [events, setEvents] = useState<ActivityLogEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchTimeline(applicantEmail).then((data) => {
      if (!cancelled) {
        setEvents(data);
        setLoading(false);
      }
    });
    return () => {
      cancelled = true;
    };
  }, [applicantEmail]);

  if (loading) {
    return (
      <p className="font-mono text-[11px] text-slate-500">
        Loading timeline...
      </p>
    );
  }

  if (events.length === 0) {
    return (
      <p className="font-mono text-[11px] text-slate-500">
        No activity recorded yet.
      </p>
    );
  }

  return (
    <div className="relative space-y-0">
      {events.map((event, i) => {
        const cfg = EVENT_CONFIG[event.eventType] ?? {
          label: event.eventType,
          color: "text-slate-400",
          dot: "bg-slate-500",
        };
        const isLast = i === events.length - 1;
        return (
          <div key={event.id} className="relative flex gap-4 pb-4">
            {!isLast && (
              <div className="absolute left-[7px] top-3 bottom-0 w-px bg-white/5" />
            )}
            <div className={`mt-1.5 h-3.5 w-3.5 shrink-0 rounded-full ${cfg.dot}`} />
            <div className="min-w-0 flex-1">
              <p className={`font-mono text-xs ${cfg.color}`}>{cfg.label}</p>
              {event.details && (
                <p className="font-mono text-[10px] text-slate-500 mt-0.5">
                  {event.details}
                </p>
              )}
              <p className="font-mono text-[10px] text-slate-600 mt-0.5">
                {formatTimestamp(event.createdAt)}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
