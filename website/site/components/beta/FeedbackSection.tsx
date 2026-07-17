"use client";

import { useState, useEffect } from "react";
import type { Feedback, FeedbackCategory } from "@/types/feedback";
import { submitFeedback, fetchFeedbackByTester } from "@/lib/firebase/feedback";
import type { Tester } from "@/types/tester";

const CATEGORIES: FeedbackCategory[] = [
  "Bug",
  "Gameplay",
  "Performance",
  "Ads",
  "Suggestion",
  "Other",
];

interface Props {
  tester: Tester;
}

export default function FeedbackSection({ tester }: Props) {
  const [feedbackList, setFeedbackList] = useState<Feedback[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [rating, setRating] = useState<number>(5);
  const [category, setCategory] = useState<FeedbackCategory>("Gameplay");
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const loadFeedback = () => {
    setLoading(true);
    setError(null);
    fetchFeedbackByTester(tester.docId)
      .then(setFeedbackList)
      .catch((e) => setError(e?.message ?? "Failed to load feedback"))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadFeedback();
  }, [tester.docId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!comment.trim() || submitting) return;
    setSubmitting(true);
    setSubmitError(null);
    try {
      await submitFeedback(tester.docId, tester.name, { rating, category, comment: comment.trim() });
      setSubmitted(true);
      setComment("");
      await loadFeedback();
      setTimeout(() => setSubmitted(false), 3000);
    } catch (e: any) {
      setSubmitError(e?.message ?? "Failed to submit feedback");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="rounded-lg border border-white/5 bg-white/[0.02] p-6">
      <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase mb-4">
        Feedback
      </h2>

      <form onSubmit={handleSubmit} className="space-y-4 mb-8">
        <div>
          <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
            Rating
          </label>
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map((n) => (
              <button
                key={n}
                type="button"
                onClick={() => setRating(n)}
                className={`w-9 h-9 rounded-lg border text-sm font-mono transition ${
                  rating === n
                    ? "border-cyan-400/40 bg-cyan-400/10 text-cyan-300"
                    : "border-white/10 text-slate-500 hover:border-white/20"
                }`}
              >
                {n}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label
            htmlFor="feedback-category"
            className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2"
          >
            Category
          </label>
          <select
            id="feedback-category"
            value={category}
            onChange={(e) => setCategory(e.target.value as FeedbackCategory)}
            className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
          >
            {CATEGORIES.map((c) => (
              <option key={c} value={c} className="bg-black text-white">
                {c}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label
            htmlFor="feedback-comment"
            className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2"
          >
            Comment
          </label>
          <textarea
            id="feedback-comment"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none resize-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
            placeholder="Describe your experience..."
          />
        </div>

        <button
          type="submit"
          disabled={!comment.trim() || submitting}
          className="rounded-lg border border-cyan-400/30 px-6 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          {submitting ? "Submitting..." : "Submit Feedback"}
        </button>

        {submitted && (
          <p className="font-mono text-[11px] text-green-400">Feedback submitted.</p>
        )}
        {submitError && (
          <p className="font-mono text-[11px] text-red-400">{submitError}</p>
        )}
      </form>

      <div className="border-t border-white/5 pt-6">
        <h3 className="font-mono text-[10px] tracking-[0.15em] text-cyan-400/60 uppercase mb-3">
          My Feedback
        </h3>
        {loading ? (
          <p className="font-mono text-xs text-slate-500">Loading feedback...</p>
        ) : error ? (
          <p className="font-mono text-xs text-red-400">{error}</p>
        ) : feedbackList.length === 0 ? (
          <p className="font-mono text-xs text-slate-500">No feedback submitted yet.</p>
        ) : (
          <div className="space-y-3">
            {feedbackList.map((fb) => (
              <div
                key={fb.id}
                className="rounded-lg border border-white/5 bg-black/50 p-4"
              >
                <div className="flex items-center gap-3 mb-2">
                  <span className="font-mono text-[11px] text-cyan-300">
                    {fb.rating}/5
                  </span>
                  <span className="font-mono text-[10px] tracking-[0.1em] text-slate-500 uppercase">
                    {fb.category}
                  </span>
                  {fb.createdAt && (
                    <span className="font-mono text-[10px] text-slate-500 ml-auto">
                      {new Date(fb.createdAt.seconds * 1000).toLocaleDateString()}
                    </span>
                  )}
                </div>
                <p className="font-mono text-xs text-slate-300">{fb.comment}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
