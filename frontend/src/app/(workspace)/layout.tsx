import type { ReactNode } from "react";
import AppShell from "../../components/layout/AppShell";
import AuthGate from "../../components/auth/AuthGate";

export default function WorkspaceLayout({ children }: { children: ReactNode }) {
  return (
    <AppShell>
      <AuthGate>{children}</AuthGate>
    </AppShell>
  );
}
