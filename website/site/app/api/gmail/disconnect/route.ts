import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  try {
    const { email } = await req.json();

    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, updateDoc } = await import("firebase/firestore");

    const targetEmail = (email || "").toLowerCase().trim();

    if (targetEmail) {
      await updateDoc(doc(firestore, "emailAccounts", targetEmail), {
        status: "deleted",
      });
    } else {
      const { collection, getDocs } = await import("firebase/firestore");
      const snapshot = await getDocs(collection(firestore, "emailAccounts"));
      const batch = (await import("firebase/firestore")).writeBatch(firestore);
      snapshot.docs.forEach((d: any) => {
        batch.update(d.ref, { status: "deleted" });
      });
      await batch.commit();
    }

    const { logEmailAudit } = await import("@/lib/firebase/auditService");
    await logEmailAudit(
      "oauth_disconnected",
      targetEmail || "all",
      targetEmail ? `Account removed: ${targetEmail}` : "All accounts removed"
    );

    return NextResponse.json({ success: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to disconnect" }, { status: 500 });
  }
}
