"use client";

import type { ReactNode } from "react";
import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import LoadingState from "../ui/LoadingState";
import { useAuth } from "./AuthProvider";

export default function AuthGate({ children }: { children: ReactNode }) {
  const { status } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (status === "unauthenticated") {
      const search = typeof window !== "undefined" ? window.location.search : "";
      const nextPath = search ? `${pathname}${search}` : pathname;
      router.replace(`/login?next=${encodeURIComponent(nextPath)}`);
    }
  }, [status, pathname, router]);

  if (status === "loading") {
    return (
      <div className="page">
        <LoadingState />
      </div>
    );
  }

  if (status === "unauthenticated") {
    return null;
  }

  return <>{children}</>;
}
