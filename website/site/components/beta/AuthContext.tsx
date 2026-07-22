"use client";

import { createContext, useContext, useState, useEffect, useCallback, useRef } from "react";
import { getCurrentUser, fetchAdmin, onAuthChange } from "@/lib/firebase/authService";
import type { AdminRole } from "@/types/admin";

interface AuthUser {
  uid: string;
  email: string;
  displayName: string;
  role: AdminRole;
}

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  refresh: async () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const fetchingRef = useRef(false);

  const refresh = useCallback(async () => {
    if (fetchingRef.current) return;
    fetchingRef.current = true;
    try {
      const fbUser = await getCurrentUser();
      if (!fbUser) {
        setUser(null);
        return;
      }
      const admin = await fetchAdmin(fbUser.uid);
      if (!admin) {
        setUser(null);
        return;
      }
      setUser({
        uid: fbUser.uid,
        email: fbUser.email,
        displayName: fbUser.displayName,
        role: (admin.role as AdminRole) || "user",
      });
    } finally {
      fetchingRef.current = false;
    }
  }, []);

  useEffect(() => {
    refresh().then(() => setLoading(false));
    const unsub = onAuthChange(() => {
      refresh().then(() => setLoading(false));
    });
    return unsub;
  }, [refresh]);

  return (
    <AuthContext.Provider value={{ user, loading, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

export function useRole() {
  const { user, loading } = useAuth();
  return { role: user?.role || null, loading, user };
}
