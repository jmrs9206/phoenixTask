"use client";

import type { ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuth } from "./AuthProvider";
import type { UiAction } from "../../lib/auth/permissions";
import LoadingState from "../ui/LoadingState";

type PermissionGateProps = {
  action?: UiAction;
  children: ReactNode;
  fallback?: ReactNode;
  redirect?: boolean;
};

export default function PermissionGate({ action, children, fallback, redirect = true }: PermissionGateProps) {
  const { status, can } = useAuth();
  const router = useRouter();

  const isAllowed = action ? can(action) : true;

  useEffect(() => {
    if (status === "authenticated" && !isAllowed && redirect) {
      router.replace("/home?error=denied");
    }
  }, [status, isAllowed, redirect, router]);

  if (status === "loading") {
    return <LoadingState />;
  }

  if (status === "authenticated" && isAllowed) {
    return <>{children}</>;
  }

  return fallback ? <>{fallback}</> : null;
}
