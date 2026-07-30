"use client";

import { useState, useEffect } from "react";

interface Activity {
  id: string;
  eventType: string;
  details: string;
  createdAt: string;
  pilot: string;
}

export default function PilotFeed() {
  const [activities, setActivities] = useState<Activity[]>([]);

  useEffect(() => {
    async function fetchActivity() {
      try {
        const res = await fetch("/api/community/activity");
        const data = await res.json();
        if (Array.isArray(data)) {
          setActivities(data);
        }
      } catch (e) {
        console.error("Failed to fetch activity", e);
      }
    }
    fetchActivity();
    const timer = setInterval(fetchActivity, 30000); // Update every 30s
    return () => clearInterval(timer);
  }, []);

  if (activities.length === 0) return null;

  return (
    <div className="w-full overflow-hidden bg-black/40 border-y border-white/5 py-2 relative">
      <div className="absolute left-4 top-1/2 -translate-y-1/2 z-20 flex items-center gap-2 bg-black/80 px-3 py-1 rounded-full border border-cyan-400/20 backdrop-blur-md">
        <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse" />
        <span className="font-mono text-[9px] tracking-[0.2em] text-cyan-400 uppercase">Live Transmission</span>
      </div>

      <div className="flex animate-marquee whitespace-nowrap gap-12 pl-40">
        {activities.map((a) => (
          <div key={a.id} className="flex items-center gap-3 font-mono text-[10px] sm:text-xs tracking-wider">
            <span className="text-cyan-400">[{a.pilot}]</span>
            <span className="text-slate-400">{a.details}</span>
            <span className="text-white/10">•</span>
          </div>
        ))}
        {/* Duplicate for seamless loop */}
        {activities.map((a) => (
          <div key={`${a.id}-dup`} className="flex items-center gap-3 font-mono text-[10px] sm:text-xs tracking-wider">
            <span className="text-cyan-400">[{a.pilot}]</span>
            <span className="text-slate-400">{a.details}</span>
            <span className="text-white/10">•</span>
          </div>
        ))}
      </div>

      <style jsx>{`
        @keyframes marquee {
          0% { transform: translateX(0); }
          100% { transform: translateX(-50%); }
        }
        .animate-marquee {
          animation: marquee 40s linear infinite;
        }
      `}</style>
    </div>
  );
}
