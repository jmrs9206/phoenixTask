"use client";

import type { ReactNode } from "react";
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { AuthLoginResponse, AuthMeResponse, AuthUser } from "../../types/api/auth";
import type { PermissionCode, UiAction } from "../../lib/auth/permissions";
import { ApiError } from "../../lib/api/client";
import { login as loginRequest, logout as logoutRequest, me as meRequest } from "../../lib/api/auth";
import { getMyPermissions } from "../../lib/api/workspace";
import { UI_ACTION_MATRIX } from "../../lib/auth/permissions";
import { appConfig } from "../../lib/config/appConfig";

type AuthStatus = "loading" | "authenticated" | "unauthenticated";

type AuthContextValue = {
  status: AuthStatus;
  user: AuthUser | null;
  error: string | null;
  permissions: PermissionCode[];
  can: (action: UiAction) => boolean;
  hasPermission: (code: PermissionCode) => boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [user, setUser] = useState<AuthUser | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [permissions, setPermissions] = useState<PermissionCode[]>([]);

  const hydrateFromSession = useCallback(async () => {
    try {
      const response: AuthMeResponse = await meRequest();
      const permissionsResponse = await getMyPermissions();
      setUser(response.user);
      setPermissions(permissionsResponse.permissions as PermissionCode[]);
      setStatus("authenticated");
      setError(null);
    } catch (err) {
      setUser(null);
      setPermissions([]);
      setStatus("unauthenticated");
      if (err instanceof ApiError && err.status === 401) {
        if (err.message === "Missing Authorization header") {
          setError(null);
        } else {
          setError("Session expired. Please sign in again.");
        }
      } else {
        setError("Unable to restore session. Please sign in again.");
      }
    }
  }, []);

  const refresh = useCallback(async () => {
    setStatus("loading");
    await hydrateFromSession();
  }, [hydrateFromSession]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const login = useCallback(async (email: string, password: string) => {
    setStatus("loading");
    setError(null);
    try {
      const response: AuthLoginResponse = await loginRequest({
        tenantCode: appConfig.tenantCode,
        email,
        password
      });
      const permissionsResponse = await getMyPermissions();
      setUser(response.user);
      setPermissions(permissionsResponse.permissions as PermissionCode[]);
      setStatus("authenticated");
      setError(null);
    } catch (err) {
      setUser(null);
      setPermissions([]);
      setStatus("unauthenticated");
      if (err instanceof ApiError) {
        if (err.status === 401) {
          setError("Invalid credentials. Please try again.");
        } else {
          setError(err.message || "Authentication failed. Please try again.");
        }
      } else {
        setError("Authentication failed. Please try again.");
      }
      throw err;
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } finally {
      setUser(null);
      setPermissions([]);
      setStatus("unauthenticated");
    }
  }, []);

  const permissionSet = useMemo(() => new Set(permissions), [permissions]);

  const hasPermission = useCallback(
    (code: PermissionCode) => status === "authenticated" && permissionSet.has(code),
    [permissionSet, status]
  );

  const can = useCallback(
    (action: UiAction) => {
      if (status !== "authenticated") {
        return false;
      }
      const required = UI_ACTION_MATRIX[action];
      if (!required || required.length === 0) {
        return false;
      }
      return required.every((code) => permissionSet.has(code));
    },
    [permissionSet, status]
  );

  const value = useMemo(
    () => ({
      status,
      user,
      error,
      permissions,
      can,
      hasPermission,
      login,
      logout,
      refresh
    }),
    [status, user, error, permissions, can, hasPermission, login, logout, refresh]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
