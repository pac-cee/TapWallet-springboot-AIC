"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { API_URL } from "@/lib/api";
import type { ApiError, Session } from "@/lib/types";

interface AuthContextValue {
  session: Session | null;
  ready: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);
const STORAGE_KEY = "tapwallet.session";

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [session, setSession] = useState<Session | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      if (stored) {
        try { setSession(JSON.parse(stored) as Session); } catch { window.localStorage.removeItem(STORAGE_KEY); }
      }
      setReady(true);
    });
    return () => window.clearTimeout(timer);
  }, []);

  useEffect(() => {
    function expireSession() {
      window.localStorage.removeItem(STORAGE_KEY);
      setSession(null);
      router.replace("/");
    }
    window.addEventListener("tapwallet:unauthorized", expireSession);
    return () => window.removeEventListener("tapwallet:unauthorized", expireSession);
  }, [router]);

  const login = useCallback(async (email: string, password: string) => {
    const response = await fetch(`${API_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!response.ok) {
      const error = (await response.json().catch(() => ({}))) as Partial<ApiError>;
      throw new Error(error.message ?? "Sign in failed. Check your details and try again.");
    }
    const nextSession = (await response.json()) as Session;
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession));
    setSession(nextSession);
    router.replace("/dashboard");
  }, [router]);

  const logout = useCallback(() => {
    window.localStorage.removeItem(STORAGE_KEY);
    setSession(null);
    router.replace("/");
  }, [router]);

  const value = useMemo(() => ({ session, ready, login, logout }), [session, ready, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used inside AuthProvider");
  return value;
}
