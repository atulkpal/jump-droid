"use client";

import { useState, useEffect } from "react";
import type { Feedback } from "@/types/feedback";
import { fetchAllFeedback } from "@/lib/firebase/feedback";

export default function FeedbackTable() {
  const [feedback, setFeedback] = useState<Feedback[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAllFeedback()
      .then((data) => {
        setFeedback(data);
        setLoading(false);
      })
      .catch((e) => {
        setError(e?.message ?? "Failed to load feedback");
        setLoading(false);
      });
  }, []);

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] overflow-x-auto">
      <table className="w-full text-left">
        <thead>
          <tr className="border-b border-white/5">
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Tester
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Rating
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Category
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Comment
            </th>
            <th className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase px-4 py-3">
              Date
            </th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={5} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                Loading feedback...
              </td>
            </tr>
          ) : error ? (
            <tr>
              <td colSpan={5} className="px-4 py-8 text-center font-mono text-xs text-red-400">
                {error}
              </td>
            </tr>
          ) : feedback.length === 0 ? (
            <tr>
              <td colSpan={5} className="px-4 py-8 text-center font-mono text-xs text-slate-500">
                No feedback submitted yet.
              </td>
            </tr>
          ) : (
            feedback.map((fb) => (
              <tr key={fb.id} className="border-b border-white/5 hover:bg-white/[0.01]">
                <td className="px-4 py-3">
                  <p className="font-mono text-xs text-white">{fb.testerName ?? fb.testerEmail}</p>
                </td>
                <td className="px-4 py-3 font-mono text-xs text-cyan-300">{fb.rating}/5</td>
                <td className="px-4 py-3 font-mono text-[10px] tracking-[0.1em] text-slate-400 uppercase">
                  {fb.category}
                </td>
                <td className="px-4 py-3 font-mono text-xs text-slate-300 max-w-xs truncate">
                  {fb.comment}
                </td>
                <td className="px-4 py-3 font-mono text-xs text-slate-500">
                  {fb.createdAt
                    ? new Date(fb.createdAt.seconds * 1000).toLocaleDateString()
                    : "—"}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
