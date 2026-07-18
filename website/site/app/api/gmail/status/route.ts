import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  try {
    const email = req.nextUrl.searchParams.get("email");

    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();
    const { doc, getDoc } = await import("firebase/firestore");

    if (email) {
      const snap = await getDoc(doc(firestore, "emailAccounts", email.toLowerCase().trim()));
      if (!snap.exists()) return NextResponse.json({ authenticated: false });

      const data = snap.data();
      return NextResponse.json({
        authenticated: data.status === "connected" && !!data.refreshToken,
        status: data.status,
        email: data.email,
        displayName: data.displayName,
        isDefault: data.isDefault || false,
        errorMessage: data.errorMessage || null,
      });
    }

    const { collection, getDocs, query, where } = await import("firebase/firestore");

    const q = query(
      collection(firestore, "emailAccounts"),
      where("status", "==", "connected")
    );
    const snapshot = await getDocs(q);

    if (snapshot.empty) return NextResponse.json({ authenticated: false });

    const accounts = snapshot.docs.map((d: any) => ({
      email: d.data().email,
      displayName: d.data().displayName,
      isDefault: d.data().isDefault || false,
      status: d.data().status,
    }));

    return NextResponse.json({ authenticated: true, accounts });
  } catch {
    return NextResponse.json({ authenticated: false });
  }
}
