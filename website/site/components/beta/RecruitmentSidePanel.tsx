"use client";

import { useState, useEffect, useCallback } from "react";
import type { RecruitmentApplicant, RecruitmentStatus } from "@/types/recruitment";
import {
  updateApplicantStatus,
  updateApplicantNotes,
  approveApplicant,
  activateApplicant,
  failEmailStatus,
  deleteApplicant,
} from "@/lib/firebase/recruitmentService";
import { logEvent } from "@/lib/firebase/activityService";
import { sendEmail } from "@/lib/emailService";
import StatusBadge from "./StatusBadge";
import ActivityTimeline from "./ActivityTimeline";

interface Props {
  applicant: RecruitmentApplicant;
  onClose: () => void;
  onStatusChanged: (email: string, status: RecruitmentStatus) => void;
  onNotesSaved: (email: string, notes: string) => void;
  onDeleted: (email: string) => void;
}

function formatDate(ts: { seconds: number } | null): string {
  if (!ts?.seconds) return "\u2014";
  return new Date(ts.seconds * 1000).toLocaleString();
}

const CAN_APPROVE: RecruitmentStatus[] = ["pending", "rejected"];

export default function RecruitmentSidePanel({
  applicant,
  onClose,
  onStatusChanged,
  onNotesSaved,
  onDeleted,
}: Props) {
  const [notes, setNotes] = useState(applicant.notes);
  const [savingNotes, setSavingNotes] = useState(false);
  const [notesSaved, setNotesSaved] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [approveError, setApproveError] = useState<string | null>(null);
  const [sendingWelcome, setSendingWelcome] = useState(false);
  const [rejecting, setRejecting] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleteConfirmed, setDeleteConfirmed] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  useEffect(() => {
    setNotes(applicant.notes);
    setNotesSaved(false);
    setConfirming(false);
    setApproveError(null);
    setShowDeleteConfirm(false);
    setDeleteConfirmed(false);
    setDeleteError(null);
  }, [applicant.notes, applicant.email, applicant.status, applicant.emailStatus]);

  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    },
    [onClose]
  );

  useEffect(() => {
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [handleKeyDown]);

  const handleApproveClick = () => {
    setConfirming(true);
    setApproveError(null);
  };

  const handleCancelConfirm = () => {
    setConfirming(false);
  };

  const handleConfirmAddToGroup = async () => {
    setProcessing(true);
    setApproveError(null);
    try {
      await approveApplicant(applicant.email);
      await logEvent(applicant.email, "approved");
      onStatusChanged(applicant.email, "approved");

      setSendingWelcome(true);
      const result = await sendEmail(
        applicant.email,
        applicant.name || "Tester",
        "welcome",
        "beta-onboarding",
        "recruitment"
      );
      if (result.success) {
        await activateApplicant(applicant.email);
        await logEvent(applicant.email, "welcome_email_sent");
        await logEvent(applicant.email, "activated");
        onStatusChanged(applicant.email, "active");
      } else {
        await failEmailStatus(applicant.email);
        await logEvent(applicant.email, "welcome_email_failed", result.error);
        onStatusChanged(applicant.email, "approved");
      }
    } catch (e: any) {
      await failEmailStatus(applicant.email);
      await logEvent(applicant.email, "welcome_email_failed", e?.message);
      onStatusChanged(applicant.email, "approved");
      setApproveError(e?.message || "Failed to approve applicant.");
    } finally {
      setProcessing(false);
      setSendingWelcome(false);
      setConfirming(false);
    }
  };

  const handleResendWelcome = async () => {
    setSendingWelcome(true);
    try {
      const result = await sendEmail(
        applicant.email,
        applicant.name || "Tester",
        "welcome",
        "beta-onboarding",
        "recruitment"
      );
      if (result.success) {
        await activateApplicant(applicant.email);
        await logEvent(applicant.email, "welcome_email_sent");
        await logEvent(applicant.email, "activated");
        onStatusChanged(applicant.email, "active");
      } else {
        await failEmailStatus(applicant.email);
        await logEvent(applicant.email, "welcome_email_failed", result.error);
        onStatusChanged(applicant.email, "approved");
      }
    } catch (e: any) {
      await failEmailStatus(applicant.email);
      await logEvent(applicant.email, "welcome_email_failed", e?.message);
      onStatusChanged(applicant.email, "approved");
    } finally {
      setSendingWelcome(false);
    }
  };

  const handleReject = async () => {
    setRejecting(true);
    setApproveError(null);
    try {
      await updateApplicantStatus(applicant.email, "rejected");
      onStatusChanged(applicant.email, "rejected");
    } catch {
      // handled silently
    } finally {
      setRejecting(false);
    }
  };

  const handleDeleteClick = () => {
    setShowDeleteConfirm(true);
    setDeleteConfirmed(false);
    setDeleteError(null);
  };

  const handleDeleteCancel = () => {
    setShowDeleteConfirm(false);
    setDeleteConfirmed(false);
  };

  const handleDeleteConfirm = async () => {
    if (
      (applicant.status === "approved" || applicant.status === "active") &&
      !deleteConfirmed
    ) {
      setDeleteConfirmed(true);
      return;
    }

    setDeleting(true);
    setDeleteError(null);
    try {
      await logEvent(applicant.email, "deleted");
      await deleteApplicant(applicant.email);
      onDeleted(applicant.email);
      onClose();
    } catch (e: any) {
      setDeleteError(e?.message || "Failed to delete applicant.");
    } finally {
      setDeleting(false);
    }
  };

  const handleSaveNotes = async () => {
    setSavingNotes(true);
    try {
      await updateApplicantNotes(applicant.email, notes);
      onNotesSaved(applicant.email, notes);
      setNotesSaved(true);
      setTimeout(() => setNotesSaved(false), 2500);
    } catch {
      // handled silently
    } finally {
      setSavingNotes(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center sm:items-center">
      <div className="fixed inset-0 bg-black/70" onClick={onClose} />
      <div className="relative mt-16 sm:mt-0 sm:max-h-[85vh] sm:w-[600px] w-full mx-4 overflow-y-auto rounded-lg border border-white/10 bg-[#0a0a0f] p-6 shadow-2xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-mono text-xs font-bold tracking-[0.15em] text-cyan-200 uppercase">
            Applicant Details
          </h2>
          <button
            onClick={onClose}
            className="font-mono text-sm text-slate-500 hover:text-white transition-colors px-2 py-1"
          >
            &#x2715;
          </button>
        </div>

        <div className="space-y-6">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Name
              </p>
              <p className="font-mono text-sm text-white">
                {applicant.name || "\u2014"}
              </p>
            </div>
            <div>
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Email
              </p>
              <p className="font-mono text-sm text-cyan-100 break-all">
                {applicant.email}
              </p>
            </div>
            <div>
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Phone
              </p>
              <p className="font-mono text-sm text-white">
                {applicant.phone || "\u2014"}
              </p>
            </div>
            <div>
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Registration Date
              </p>
              <p className="font-mono text-sm text-white">
                {formatDate(applicant.registeredAt)}
              </p>
            </div>
            <div>
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Status
              </p>
              <StatusBadge status={applicant.status} />
            </div>
            <div>
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Code Jam Interest
              </p>
              <p className="font-mono text-sm text-white">
                {applicant.codeJam ? "Yes" : "No"}
              </p>
            </div>
          </div>

          {showDeleteConfirm ? (
            <div className="rounded-lg border border-red-400/10 bg-red-400/[0.02] p-4 space-y-4">
              <p className="font-mono text-xs font-bold text-red-200">
                Delete Applicant
              </p>
              {(applicant.status === "approved" || applicant.status === "active") &&
                !deleteConfirmed && (
                  <p className="font-mono text-xs text-slate-400 leading-relaxed">
                    Have you already removed{" "}
                    <span className="text-cyan-100">{applicant.email}</span> from the
                    community (Google Group)?
                  </p>
                )}
              {deleteConfirmed && (
                <p className="font-mono text-xs text-red-300 leading-relaxed">
                  This will permanently delete this applicant and all associated
                  records. This action cannot be undone.
                </p>
              )}
              <div className="flex gap-3">
                <button
                  onClick={handleDeleteConfirm}
                  disabled={deleting}
                  className="rounded-lg border border-red-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10 hover:border-red-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  {deleting
                    ? "Deleting..."
                    : deleteConfirmed
                    ? "Yes, delete permanently"
                    : "Yes, I\u2019ve removed them"}
                </button>
                <button
                  onClick={handleDeleteCancel}
                  disabled={deleting}
                  className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  Cancel
                </button>
              </div>
              {deleteError && (
                <p className="font-mono text-xs text-red-400">{deleteError}</p>
              )}
            </div>
          ) : confirming ? (
            <div className="rounded-lg border border-cyan-400/10 bg-cyan-400/[0.02] p-4 space-y-4">
              <div>
                <p className="font-mono text-xs font-bold text-cyan-200 mb-3">
                  Step 1: Add to Google Group
                </p>
                <p className="font-mono text-xs text-slate-400 leading-relaxed">
                  Manually add{" "}
                  <span className="text-cyan-100">{applicant.email}</span> to your
                  beta testers Google Group in the Play Console.
                </p>
              </div>
              <div className="rounded-lg border border-white/5 bg-black/30 p-3">
                <p className="font-mono text-[11px] text-slate-500 leading-relaxed">
                  After you confirm, a Beta Welcome Email will be sent automatically
                  to the applicant.
                </p>
              </div>
              <div className="flex gap-3">
                <button
                  onClick={handleConfirmAddToGroup}
                  disabled={processing}
                  className="rounded-lg border border-cyan-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  {processing
                    ? "Processing..."
                    : "\u2713 I\u2019ve added them to the group"}
                </button>
                <button
                  onClick={handleCancelConfirm}
                  disabled={processing}
                  className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  Cancel
                </button>
              </div>
              {approveError && (
                <p className="font-mono text-xs text-red-400">{approveError}</p>
              )}
            </div>
          ) : (
            <div className="border-t border-white/5 pt-6 space-y-4">
              <div>
                <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
                  Actions
                </p>
                <div className="flex gap-3 flex-wrap">
                  {CAN_APPROVE.includes(applicant.status) && (
                    <button
                      onClick={handleApproveClick}
                      className="rounded-lg border border-cyan-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50"
                    >
                      Approve
                    </button>
                  )}
                  {applicant.status !== "rejected" &&
                    applicant.status !== "approved" &&
                    applicant.status !== "active" && (
                      <button
                        onClick={handleReject}
                        disabled={rejecting}
                        className="rounded-lg border border-red-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10 hover:border-red-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                      >
                        {rejecting ? "Rejecting..." : "Reject"}
                      </button>
                    )}
                  <button
                    onClick={handleDeleteClick}
                    className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs tracking-[0.15em] text-slate-500 transition-colors hover:border-red-400/30 hover:text-red-300"
                  >
                    Delete
                  </button>
                </div>

                {sendingWelcome && (
                  <div className="flex items-center gap-2 mt-3">
                    <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-cyan-400/30 border-t-cyan-400" />
                    <p className="font-mono text-xs text-slate-400">
                      Sending welcome email...
                    </p>
                  </div>
                )}

                {applicant.emailStatus === "failed" && (
                  <div className="rounded-lg border border-red-400/10 bg-red-400/[0.02] p-3 space-y-2 mt-3">
                    <p className="font-mono text-xs text-red-300">
                      Welcome email failed to send. You can try again.
                    </p>
                    <button
                      onClick={handleResendWelcome}
                      disabled={sendingWelcome}
                      className="rounded-lg border border-red-400/30 px-4 py-2 font-mono text-[10px] tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10 hover:border-red-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                    >
                      {sendingWelcome ? "Sending..." : "Resend Welcome Email"}
                    </button>
                  </div>
                )}

                {applicant.emailStatus === "pending" &&
                  applicant.status === "approved" && (
                    <p className="font-mono text-xs text-amber-400 mt-3">
                      Welcome email pending...
                    </p>
                  )}

                {applicant.status === "active" && (
                  <p className="font-mono text-xs text-green-400">
                    &check; Applicant is active and ready for testing.
                  </p>
                )}
              </div>
            </div>
          )}

          <div className="border-t border-white/5 pt-6">
            <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
              Activity Timeline
            </p>
            <ActivityTimeline applicantEmail={applicant.email} />
          </div>

          <div className="border-t border-white/5 pt-6">
            <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
              Notes
            </p>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
              placeholder="Add internal notes about this applicant..."
              className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none resize-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
            />
            <div className="flex items-center gap-3 mt-3">
              <button
                onClick={handleSaveNotes}
                disabled={savingNotes}
                className="rounded-lg border border-cyan-400/30 px-5 py-2 font-mono text-xs tracking-[0.15em] text-cyan-300 transition-colors hover:bg-cyan-400/10 hover:border-cyan-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
              >
                {savingNotes ? "Saving..." : "Save Notes"}
              </button>
              {notesSaved && (
                <p className="font-mono text-[11px] text-green-400">Notes saved.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
