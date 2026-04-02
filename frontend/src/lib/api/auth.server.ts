import { cookies } from "next/headers";
import { apiFetch } from "./client";
import type { AuthMeResponse } from "../../types/api/auth";

async function withAuthCookie(headers: Record<string, string>) {
  const cookieStore = await cookies();
  const authCookie = cookieStore.get("phoenixtask_auth")?.value;
  if (!authCookie) {
    return headers;
  }
  return {
    ...headers,
    Cookie: `phoenixtask_auth=${authCookie}`
  };
}

async function apiFetchServer<T>(path: string, extraHeaders: Record<string, string> = {}) {
  const headers = await withAuthCookie(extraHeaders);
  return apiFetch<T>(path, headers);
}

export function getAuthMe(): Promise<AuthMeResponse> {
  return apiFetchServer<AuthMeResponse>("/api/auth/me");
}
