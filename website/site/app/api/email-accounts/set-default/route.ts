import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  try {
    const { email } = await req.json();
    if (!email) {
      return NextResponse.json({ error: "Email is required" }, { status: 400 });
    }

    const { setDefaultAccount, getEmailAccount } = await import(
      "@/lib/firebase/emailAccountService"
    );

    const account = await getEmailAccount(email);
    if (!account || account.status === "deleted") {
      return NextResponse.json({ error: "Account not found" }, { status: 404 });
    }

    await setDefaultAccount(email);

    const { logEmailAudit } = await import("@/lib/firebase/auditService");
    await logEmailAudit("default_sender_changed", email, `Default sender changed to ${email}`);

    return NextResponse.json({ success: true, defaultSender: email });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to set default" }, { status: 500 });
  }
}
