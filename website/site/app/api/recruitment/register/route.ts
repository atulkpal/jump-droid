import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";

export async function POST(req: Request) {
  try {
    const { email, name, phone, codeJam, source, convertedFrom } = await req.json();

    if (!email) {
      return NextResponse.json({ error: "Email is required" }, { status: 400 });
    }

    const cleanEmail = email.toLowerCase().trim();
    const adminFirestore = getAdminFirestore();
    const { logEvent } = await import("@/lib/firebase/activityService");

    const existingSnap = await adminFirestore.collection("betaUsers").doc(cleanEmail).get();

    if (!existingSnap.exists) {
      await adminFirestore.collection("betaUsers").doc(cleanEmail).set({
        email: cleanEmail,
        name: name ?? "",
        phone: phone ?? "",
        codeJam: codeJam ?? false,
        status: "pending",
        source: source ?? "website",
        version: 1,
        notes: "",
        registeredFrom: "api",
        registeredAt: new Date(),
        acknowledgementSent: false,
        convertedFromCampaign: convertedFrom || null,
      });
    }

    // Auto-create recruitment contact for the applicant
    try {
      const contactRef = adminFirestore.collection("recruitmentContacts").doc(cleanEmail);
      const contactSnap = await contactRef.get();
      if (!contactSnap.exists) {
        await contactRef.set({
          name: name || "",
          email: cleanEmail,
          phone: phone || "",
          status: "pending",
          source: "website",
          importedBy: "Applicant",
          importedAt: new Date(),
          registeredAt: new Date(),
          lastInviteAt: null,
          notes: "",
          inviteCount: 0,
          nextEligibleAt: null,
          campaignId: "",
          campaigns: [],
          campaignData: {},
          emailStatus: "pending",
          stoppedReason: "",
        });
      } else {
        await contactRef.update({ status: "pending", registeredAt: new Date() });
      }
    } catch { /* non-critical */ }

    try { await logEvent(cleanEmail, "registered"); } catch { /* non-critical */ }

    let acknowledgementSent = false;
    const displayName = name || cleanEmail;
    try {
      const sendRes = await fetch(`${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/api/gmail/send`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          emails: [{ email: cleanEmail, name: displayName }],
          template: "acknowledgement",
          source: "registration",
        }),
      });

      if (sendRes.ok) {
        const results = await sendRes.json();
        const result = Array.isArray(results) ? results[0] : results;
        if (result?.success) {
          await logEvent(cleanEmail, "acknowledgement_sent");
          await adminFirestore.collection("betaUsers").doc(cleanEmail).update({
            acknowledgementSent: true,
          });
          acknowledgementSent = true;
        } else {
          const errMsg = result?.error || "Failed to send acknowledgement email";
          await adminFirestore.collection("betaUsers").doc(cleanEmail).update({
            acknowledgementSent: false,
            acknowledgementError: errMsg,
          }).catch(() => {});
        }
      } else {
        const errData = await sendRes.json().catch(() => ({ error: `HTTP ${sendRes.status}` }));
        const errMsg = errData?.error || `HTTP ${sendRes.status}`;
        await adminFirestore.collection("betaUsers").doc(cleanEmail).update({
          acknowledgementSent: false,
          acknowledgementError: errMsg,
        }).catch(() => {});
      }
    } catch (e: any) {
      const errMsg = e?.message || "Acknowledgement email failed";
      await adminFirestore.collection("betaUsers").doc(cleanEmail).update({
        acknowledgementSent: false,
        acknowledgementError: errMsg,
      }).catch(() => {});
    }

    return NextResponse.json({
      success: true,
      email: cleanEmail,
      acknowledgementSent,
    });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message ?? "Registration failed" },
      { status: 500 }
    );
  }
}
