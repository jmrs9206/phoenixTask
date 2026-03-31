const backendBaseUrl = process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";
const tenantCode = process.env.NEXT_PUBLIC_TENANT_CODE ?? "demo";

export const appConfig = {
  backendBaseUrl,
  tenantCode
};
