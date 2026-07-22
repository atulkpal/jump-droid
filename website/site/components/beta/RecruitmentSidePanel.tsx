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
import { useRole } from "./AuthContext";
import StatusBadge from "./StatusBadge";
import ActivityTimeline from "./ActivityTimeline";

const REJECT_REASONS = [
  { label: "Not enough experience", value: "Not enough experience" },
  { label: "Region not supported", value: "Region not supported" },
  { label: "Application incomplete", value: "Application incomplete" },
  { label: "Device incompatible", value: "Device incompatible" },
  { label: "Too many applicants", value: "Too many applicants" },
  { label: "Other", value: "__other__" },
];

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
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [rejectCustomReason, setRejectCustomReason] = useState("");
  const [rejectSendEmail, setRejectSendEmail] = useState(true);
  const [rejectProcessing, setRejectProcessing] = useState(false);
  const [rejectError, setRejectError] = useState<string | null>(null);
  const { role, user: authUser } = useRole();
  const currentUserEmail = authUser?.email || "";
  const [campaigns, setCampaigns] = useState<{ id: string; name: string }[]>([]);
  const [campaignsLoading, setCampaignsLoading] = useState(true);
  const [selectedCampaignForAdd, setSelectedCampaignForAdd] = useState("");
  const [campaignAdding, setCampaignAdding] = useState(false);
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

  useEffect(() => {
    fetch("/api/campaigns")
      .then((r) => r.json())
      .then((list) => setCampaigns(list))
      .catch(() => {})
      .finally(() => setCampaignsLoading(false));
  }, []);

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

  const handleRejectClick = () => {
    setShowRejectModal(true);
    setRejectReason("");
    setRejectCustomReason("");
    setRejectSendEmail(true);
    setRejectError(null);
  };

  const handleRejectConfirm = async () => {
    const reason = rejectReason === "__other__" ? rejectCustomReason : rejectReason;
    setRejectProcessing(true);
    setRejectError(null);
    try {
      if (rejectSendEmail) {
        const result = await sendEmail(
          applicant.email,
          applicant.name || "Tester",
          "reject",
          "beta-onboarding",
          "recruitment"
        );
        if (!result.success) {
          setRejectError(`Email failed to send: ${result.error}`);
          setRejectProcessing(false);
          return;
        }
      }

      await updateApplicantStatus(applicant.email, "rejected");
      await logEvent(applicant.email, "rejected", reason || undefined);
      if (reason) {
        const timestamp = new Date().toLocaleString();
        const noteLine = `[${timestamp}] Rejected: ${reason}`;
        const updatedNotes = (applicant.notes || "") + "\n" + noteLine;
        await updateApplicantNotes(applicant.email, updatedNotes);
        onNotesSaved(applicant.email, updatedNotes);
      }
      onStatusChanged(applicant.email, "rejected");
      setShowRejectModal(false);
    } catch (e: any) {
      setRejectError(e?.message || "Failed to reject applicant.");
    } finally {
      setRejectProcessing(false);
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
            <div className="col-span-2">
              <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-1">
                Acknowledgement Email
              </p>
              {applicant.acknowledgementSent === true ? (
                <p className="font-mono text-xs text-green-400 flex items-center gap-1">
                  &#x2713; Sent
                </p>
              ) : applicant.acknowledgementError ? (
                <div>
                  <p className="font-mono text-xs text-red-400 flex items-center gap-1">
                    &#x2715; Failed
                  </p>
                  <p className="font-mono text-[10px] text-red-300/70 mt-0.5 break-all">
                    {applicant.acknowledgementError}
                  </p>
                </div>
              ) : (
                <p className="font-mono text-xs text-amber-400/60 flex items-center gap-1">
                  &#x25CB; Pending
                </p>
              )}
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
          ) : showRejectModal ? (
            <div className="rounded-lg border border-red-400/10 bg-red-400/[0.02] p-4 space-y-4">
              <p className="font-mono text-xs font-bold text-red-200">
                Reject Applicant
              </p>
              <p className="font-mono text-xs text-slate-400 leading-relaxed">
                This will reject{" "}
                <span className="text-cyan-100">{applicant.email}</span>.
                {rejectSendEmail && " A rejection email will be sent."}
              </p>

              <div>
                <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                  Reason
                </label>
                <select
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none transition focus:border-cyan-400/40"
                >
                  <option value="">— Select reason —</option>
                  {REJECT_REASONS.map((r) => (
                    <option key={r.value} value={r.value}>
                      {r.label}
                    </option>
                  ))}
                </select>
              </div>

              {rejectReason === "__other__" && (
                <div>
                  <label className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase block mb-2">
                    Custom Reason
                  </label>
                  <textarea
                    value={rejectCustomReason}
                    onChange={(e) => setRejectCustomReason(e.target.value)}
                    rows={3}
                    placeholder="Enter rejection reason..."
                    className="w-full rounded-lg border border-white/10 bg-black px-4 py-3 font-mono text-sm text-white outline-none resize-none transition focus:border-cyan-400/40 focus:ring-1 focus:ring-cyan-400/20"
                  />
                </div>
              )}

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="reject-send-email"
                  checked={rejectSendEmail}
                  onChange={(e) => setRejectSendEmail(e.target.checked)}
                  className="h-4 w-4 rounded border-white/10 bg-black text-cyan-400 focus:ring-cyan-400/20 focus:ring-1"
                />
                <label
                  htmlFor="reject-send-email"
                  className="font-mono text-xs text-slate-400"
                >
                  Send rejection email
                </label>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={handleRejectConfirm}
                  disabled={rejectProcessing}
                  className="rounded-lg border border-red-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10 hover:border-red-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  {rejectProcessing ? "Rejecting..." : "Confirm Rejection"}
                </button>
                <button
                  onClick={() => setShowRejectModal(false)}
                  disabled={rejectProcessing}
                  className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs tracking-[0.15em] text-slate-400 transition-colors hover:border-white/20 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  Cancel
                </button>
              </div>
              {rejectError && (
                <p className="font-mono text-xs text-red-400">{rejectError}</p>
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
                        onClick={handleRejectClick}
                        disabled={rejecting}
                        className="rounded-lg border border-red-400/30 px-5 py-3 font-mono text-xs tracking-[0.15em] text-red-300 transition-colors hover:bg-red-400/10 hover:border-red-400/50 disabled:opacity-30 disabled:cursor-not-allowed"
                      >
                        Reject
                      </button>
                    )}
                  {role === "owner" && (
                    <button
                      onClick={handleDeleteClick}
                      className="rounded-lg border border-white/10 px-5 py-3 font-mono text-xs tracking-[0.15em] text-slate-500 transition-colors hover:border-red-400/30 hover:text-red-300"
                    >
                      Delete
                    </button>
                  )}
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
              Campaigns
            </p>
            {campaignsLoading ? (
              <p className="font-mono text-xs text-slate-500">Loading...</p>
            ) : (
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <select
                    value={selectedCampaignForAdd}
                    onChange={(e) => setSelectedCampaignForAdd(e.target.value)}
                    className="flex-1 rounded-lg border border-white/10 bg-black px-3 py-2 font-mono text-xs text-white outline-none transition focus:border-cyan-400/40"
                  >
                    <option value="">— Select Campaign —</option>
                    {campaigns.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                  <button
                    onClick={async () => {
                      if (!selectedCampaignForAdd) return;
                      setCampaignAdding(true);
                      try {
                        const { createContactFromApplicant } = await import("@/lib/firebase/outreachService");
                        const { addContactToCampaign } = await import("@/lib/firebase/campaignService");
                        await createContactFromApplicant(applicant.email, applicant.name || applicant.email);
                        await addContactToCampaign(applicant.email, selectedCampaignForAdd, currentUserEmail);
                      } catch {
                        // silently fail
                      } finally {
                        setCampaignAdding(false);
                        setSelectedCampaignForAdd("");
                      }
                    }}
                    disabled={!selectedCampaignForAdd || campaignAdding}
                    className="rounded-lg border border-cyan-400/30 px-3 py-2 font-mono text-[11px] text-cyan-300 transition-colors hover:bg-cyan-400/10 disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    {campaignAdding ? "..." : "Add"}
                  </button>
                </div>
                {applicant.campaigns && applicant.campaigns.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {applicant.campaigns.map((cid) => {
                      const c = campaigns.find((x) => x.id === cid);
                      return (
                        <span
                          key={cid}
                          className="rounded border border-cyan-400/20 bg-cyan-400/5 px-2 py-1 font-mono text-[10px] text-cyan-300"
                        >
                          {c?.name || cid}
                        </span>
                      );
                    })}
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="border-t border-white/5 pt-6">
            <p className="font-mono text-[10px] tracking-[0.15em] text-slate-500 uppercase mb-3">
              Notes
            </p>
            {role !== "user" ? (
              <>
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
              </>
            ) : (
              <div className="rounded-lg border border-white/5 bg-white/[0.02] px-4 py-3">
                <p className="font-mono text-sm text-slate-400 whitespace-pre-wrap">
                  {applicant.notes || "\u2014"}
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
