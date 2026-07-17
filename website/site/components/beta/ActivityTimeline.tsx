"use client";

import { useState, useEffect } from "react";
import type { ActivityLogEntry, ActivityEventType } from "@/types/activityLog";
import { fetchTimeline } from "@/lib/firebase/activityService";

interface Props {
  applicantEmail: string;
}

const EVENT_CONFIG: Record<
  ActivityEventType,
  { label: string; color: string; dot: string }
> = {
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
