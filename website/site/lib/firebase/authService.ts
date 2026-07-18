import { getFirestore } from "./config";

let authInstance: any = null;

export async function getFirebaseAuth() {
  if (authInstance) return authInstance;
  await getFirestore();
  const { getApps } = await import("firebase/app");
  const { getAuth } = await import("firebase/auth");
  authInstance = getAuth(getApps()[0]);
  return authInstance;
}

export async function signInWithGoogle(): Promise<{ uid: string; email: string; displayName: string }> {
  const auth = await getFirebaseAuth();
  const { GoogleAuthProvider, signInWithPopup } = await import("firebase/auth");

  const provider = new GoogleAuthProvider();
  provider.setCustomParameters({ prompt: "select_account" });

  const result = await signInWithPopup(auth, provider);
  const user = result.user;

  if (!user.email) throw new Error("Email required for admin access");

  await ensureAdmin(user.uid, user.email, user.displayName || "");

  return {
    uid: user.uid,
    email: user.email,
    displayName: user.displayName || "",
  };
}

export async function signOut(): Promise<void> {
  const auth = await getFirebaseAuth();
  const { signOut: firebaseSignOut } = await import("firebase/auth");
  await firebaseSignOut(auth);
}

export function onAuthChange(callback: (user: { uid: string; email: string; displayName: string } | null) => void): () => void {
  let unsubscribe: (() => void) | null = null;

  getFirebaseAuth().then((auth) => {
    const { onAuthStateChanged } = require("firebase/auth");
    unsubscribe = onAuthStateChanged(auth, (user: any) => {
      if (user && user.email) {
        callback({ uid: user.uid, email: user.email, displayName: user.displayName || "" });
      } else {
        callback(null);
      }
    });
  });

  return () => {
    if (unsubscribe) unsubscribe();
  };
}

export async function getCurrentUser(): Promise<{ uid: string; email: string; displayName: string } | null> {
  const auth = await getFirebaseAuth();
  const user = auth.currentUser;
  if (user && user.email) {
    return { uid: user.uid, email: user.email, displayName: user.displayName || "" };
  }
  return null;
}

const DEFAULT_ALLOWED_EMAILS = ["ashwathai.dev@gmail.com", "atulkpal@gmail.com"];
const DEFAULT_OWNER_EMAILS = ["ashwathai.dev@gmail.com", "atulkpal@gmail.com"];

export async function ensureAdmin(uid: string, email: string, displayName: string): Promise<void> {
  const firestore = await getFirestore();
  const { doc, getDoc, setDoc, serverTimestamp } = await import("firebase/firestore");

  const adminRef = doc(firestore, "admins", uid);
  let existing;
  try {
    existing = await getDoc(adminRef);
  } catch {
    throw new Error("Could not verify admin status.");
  }
  if (existing.exists()) return;

  let allowedEmails: string[] = DEFAULT_ALLOWED_EMAILS;
  let ownerEmails: string[] = DEFAULT_OWNER_EMAILS;

  try {
    const allowedSnap = await getDoc(doc(firestore, "appConfig", "allowedAdmins"));
    if (allowedSnap.exists()) {
      const data = allowedSnap.data();
      if (data.emails && Array.isArray(data.emails)) allowedEmails = data.emails;
      if (data.owners && Array.isArray(data.owners)) ownerEmails = data.owners;
    }
  } catch {
    // Allowlist doc not readable — fallback to hardcoded values
  }

  if (!allowedEmails.includes(email)) {
    throw new Error("Your email is not authorized for admin access. Contact an owner to be added.");
  }

  const role = ownerEmails.includes(email) ? "owner" : "admin";

  try {
    await setDoc(adminRef, {
      uid,
      email,
      displayName,
      role,
      createdAt: serverTimestamp(),
    });
  } catch {
    throw new Error("Could not create admin record.");
  }
}

export async function fetchAdmin(uid: string): Promise<{ role: string; email?: string } | null> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");

  const snap = await getDoc(doc(firestore, "admins", uid));
  if (!snap.exists()) return null;
  const data = snap.data();
  return { role: data.role || "admin", email: data.email };
}

export interface AllowedAdminsConfig {
  emails: string[];
  owners: string[];
}

export async function getAllowedAdminsConfig(): Promise<AllowedAdminsConfig> {
  const firestore = await getFirestore();
  const { doc, getDoc } = await import("firebase/firestore");

  try {
    const snap = await getDoc(doc(firestore, "appConfig", "allowedAdmins"));
    if (snap.exists()) {
      const data = snap.data();
      return {
        emails: Array.isArray(data.emails) ? data.emails : [...DEFAULT_ALLOWED_EMAILS],
        owners: Array.isArray(data.owners) ? data.owners : [...DEFAULT_OWNER_EMAILS],
      };
    }
  } catch {
    // Allowlist doc not readable — return hardcoded defaults
  }

  return { emails: [...DEFAULT_ALLOWED_EMAILS], owners: [...DEFAULT_OWNER_EMAILS] };
}
