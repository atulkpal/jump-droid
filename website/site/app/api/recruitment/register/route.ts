import { NextResponse } from "next/server";
import { getAccessToken, buildMimeMessage, logEmail, getSenderProfile } from "@/lib/emailService";
import { renderTemplate } from "@/lib/emailTemplates";

export async function POST(req: Request) {
  try {
    const { email, name, phone, codeJam, source } = await req.json();

    if (!email) {
      return NextResponse.json({ error: "Email is required" }, { status: 400 });
    }

    const cleanEmail = email.toLowerCase().trim();
    const sender = await getSenderProfile();

    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");
    const logEventModule = await import("@/lib/firebase/activityService");
    const logEvent = logEventModule.logEvent;

    const existingSnap = await getDoc(doc(firestore, "betaUsers", cleanEmail));
    if (existingSnap.exists()) {
      return NextResponse.json({ error: "Email already registered" }, { status: 409 });
    }

    await setDoc(doc(firestore, "betaUsers", cleanEmail), {
      email: cleanEmail,
      name: name ?? "",
      phone: phone ?? "",
      codeJam: codeJam ?? false,
      status: "pending",
      source: source ?? "website",
      version: 1,
      notes: "",
      registeredFrom: "api",
      registeredAt: serverTimestamp(),
      acknowledgementSent: false,
    });

    const outreachService = await import("@/lib/firebase/outreachService");
    try { await outreachService.matchRegistration(cleanEmail); } catch { /* non-critical */ }

    try { await logEvent(cleanEmail, "registered"); } catch { /* non-critical */ }

    let acknowledgementSent = false;
    try {
      const displayName = name || cleanEmail;
      const { html, subject, text } = renderTemplate("acknowledgement", displayName);

      const accessToken = await getAccessToken();
      const { raw } = buildMimeMessage(cleanEmail, displayName, subject, html, text, sender);

      const sendRes = await fetch(
        "https://gmail.googleapis.com/gmail/v1/users/me/messages/send",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ raw }),
        }
      );

      const responseBody = await sendRes.text();

      if (sendRes.ok) {
        let messageId = "";
        let threadId = "";
        try {
          const parsed = JSON.parse(responseBody);
          messageId = parsed.id || "";
          threadId = parsed.threadId || "";
        } catch {

        }

        await logEmail(cleanEmail, displayName, "acknowledgement", "", "registration", "sent", messageId, threadId, "");
        await logEvent(cleanEmail, "acknowledgement_sent");

        await setDoc(doc(firestore, "betaUsers", cleanEmail), {
          acknowledgementSent: true,
        }, { merge: true });

        acknowledgementSent = true;
      } else {
        await logEmail(cleanEmail, displayName, "acknowledgement", "", "registration", "failed", "", "", responseBody);
      }
    } catch {
      // Acknowledgement email failure should not block registration
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
