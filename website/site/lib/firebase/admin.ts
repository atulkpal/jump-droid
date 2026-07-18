import { initializeApp, getApps, cert } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import type { Firestore } from "firebase-admin/firestore";

let db: Firestore | null = null;

export function getAdminFirestore(): Firestore {
  if (db) return db;
  const key = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;
  if (!key) throw new Error("FIREBASE_SERVICE_ACCOUNT_KEY not set");
  const parsed = JSON.parse(key.replace(/\n/g, "").replace(/\r/g, ""));
  if (getApps().length === 0) {
    initializeApp({
      credential: cert(parsed),
    });
  }
  db = getFirestore();
  return db;
}
