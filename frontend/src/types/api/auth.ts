export type AuthUser = {
  userId: number;
  companyId: number;
  primaryRoleId: number;
  firstName: string;
  lastName: string;
  email: string;
  status: string;
};

export type AuthLoginResponse = {
  tokenType: "Bearer";
  accessToken: string;
  expiresAt: string;
  tenantCode: string;
  user: AuthUser;
};

export type AuthMeResponse = {
  tenantCode: string;
  expiresAt: string;
  user: AuthUser;
};
