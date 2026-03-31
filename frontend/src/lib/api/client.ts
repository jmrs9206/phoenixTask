import { appConfig } from "../config/appConfig";

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export async function apiFetch<T>(
  path: string,
  extraHeaders: Record<string, string> = {}
): Promise<T> {
  const url = new URL(path, appConfig.backendBaseUrl).toString();

  const response = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      "X-Tenant-Code": appConfig.tenantCode,
      ...extraHeaders
    },
    credentials: "include",
    cache: "no-store"
  });

  if (!response.ok) {
    let message = response.statusText || "Request failed";
    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      try {
        const payload = await response.json();
        if (payload?.message) {
          message = payload.message;
        }
      } catch {
        // ignore parse errors
      }
    }
    throw new ApiError(response.status, message);
  }

  return response.json() as Promise<T>;
}

export async function apiPost<T>(
  path: string,
  body: unknown,
  extraHeaders: Record<string, string> = {}
): Promise<T> {
  const url = new URL(path, appConfig.backendBaseUrl).toString();

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Tenant-Code": appConfig.tenantCode,
      ...extraHeaders
    },
    credentials: "include",
    body: JSON.stringify(body),
    cache: "no-store"
  });

  if (!response.ok) {
    let message = response.statusText || "Request failed";
    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      try {
        const payload = await response.json();
        if (payload?.message) {
          message = payload.message;
        }
      } catch {
        // ignore parse errors
      }
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function apiDelete<T>(
  path: string,
  extraHeaders: Record<string, string> = {}
): Promise<T> {
  const url = new URL(path, appConfig.backendBaseUrl).toString();

  const response = await fetch(url, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      "X-Tenant-Code": appConfig.tenantCode,
      ...extraHeaders
    },
    credentials: "include",
    cache: "no-store"
  });

  if (!response.ok) {
    let message = response.statusText || "Request failed";
    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      try {
        const payload = await response.json();
        if (payload?.message) {
          message = payload.message;
        }
      } catch {
        // ignore parse errors
      }
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function apiUpload<T>(
  path: string,
  body: FormData,
  extraHeaders: Record<string, string> = {}
): Promise<T> {
  const url = new URL(path, appConfig.backendBaseUrl).toString();

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "X-Tenant-Code": appConfig.tenantCode,
      ...extraHeaders
    },
    credentials: "include",
    body,
    cache: "no-store"
  });

  if (!response.ok) {
    let message = response.statusText || "Request failed";
    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      try {
        const payload = await response.json();
        if (payload?.message) {
          message = payload.message;
        }
      } catch {
        // ignore parse errors
      }
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function apiFetchBlob(
  path: string,
  extraHeaders: Record<string, string> = {}
): Promise<Blob> {
  const url = new URL(path, appConfig.backendBaseUrl).toString();

  const response = await fetch(url, {
    method: "GET",
    headers: {
      "X-Tenant-Code": appConfig.tenantCode,
      ...extraHeaders
    },
    credentials: "include",
    cache: "no-store"
  });

  if (!response.ok) {
    let message = response.statusText || "Request failed";
    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      try {
        const payload = await response.json();
        if (payload?.message) {
          message = payload.message;
        }
      } catch {
        // ignore parse errors
      }
    }
    throw new ApiError(response.status, message);
  }

  return response.blob();
}
