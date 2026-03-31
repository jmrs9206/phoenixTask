import { apiFetch, apiPost } from "./client";
import type { AuthLoginResponse, AuthMeResponse } from "../../types/api/auth";

type LoginPayload = {
  tenantCode: string;
  email: string;
  password: string;
};

export function login(payload: LoginPayload): Promise<AuthLoginResponse> {
  return apiPost<AuthLoginResponse>("/api/auth/login", payload);
}

export function me(): Promise<AuthMeResponse> {
  return apiFetch<AuthMeResponse>("/api/auth/me");
}

export function logout(): Promise<void> {
  return apiPost<void>("/api/auth/logout", {});
}
