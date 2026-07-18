import { NextResponse } from "next/server";
import { getAdminFirestore } from "@/lib/firebase/admin";

export async function GET() {
  try {
    const { listEmailAccountsAdmin, getDefaultSenderAccountAdmin } = await import(
      "@/lib/firebase/emailAccountService"
    );

    const adminFirestore = getAdminFirestore();
    const [accounts, defaultAccount] = await Promise.all([
      listEmailAccountsAdmin(adminFirestore),
      getDefaultSenderAccountAdmin(adminFirestore),
    ]);

    return NextResponse.json({
      accounts: accounts.map((a) => ({
        email: a.email,
        displayName: a.displayName,
        status: a.status,
        isDefault: a.isDefault,
        errorMessage: a.errorMessage,
        provider: a.provider,
        lastUsedAt: a.lastUsedAt,
      })),
      defaultSenderEmail: defaultAccount?.email || null,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to list accounts" }, { status: 500 });
  }
}
