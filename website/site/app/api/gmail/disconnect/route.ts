import { NextResponse } from "next/server";

export async function POST() {
  try {
    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, deleteDoc } = await import("firebase/firestore");

    await deleteDoc(doc(firestore, "gmailAuth", "tokens"));

    return NextResponse.json({ success: true });
  } catch (e: any) {
    return NextResponse.json(
      { error: e?.message ?? "Disconnect failed" },
      { status: 500 }
    );
  }
}
