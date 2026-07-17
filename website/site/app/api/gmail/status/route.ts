import { NextResponse } from "next/server";

export async function GET() {
  try {
    const { doc, getDoc } = await import("firebase/firestore");
    const { getFirestore: initDb } = await import("firebase/firestore");
    const { initializeApp, getApps } = await import("firebase/app");

    const firebaseConfig = {
      apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
      authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
      projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
      storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
      messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
      appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
    };

    const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];
    const db = initDb(app);

    const { getFirestore } = await import("@/lib/firebase/config");
    const firestore = await getFirestore();

    const snap = await getDoc(doc(firestore, "gmailAuth", "tokens"));
    const authenticated = snap.exists() && !!snap.data()?.refreshToken;

    return NextResponse.json({ authenticated });
  } catch {
    return NextResponse.json({ authenticated: false });
  }
}
