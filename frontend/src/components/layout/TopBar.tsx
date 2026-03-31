"use client";

import { appConfig } from "../../lib/config/appConfig";
import { useAuth } from "../auth/AuthProvider";

type TopBarProps = {
  onToggleNav?: () => void;
};

export default function TopBar({ onToggleNav }: TopBarProps) {
  const { user, logout, status } = useAuth();

  const handleLogout = () => {
    void logout();
  };

  return (
    <header className="topbar">
      <div className="topbar-left">
        <button type="button" className="nav-toggle" onClick={onToggleNav} aria-label="Toggle navigation">
          Menu
        </button>
        <div className="topbar-title">PhoenixTask® Workspace</div>
      </div>
      <div className="topbar-meta">
        <span>Tenant: {appConfig.tenantCode}</span>
        {status === "authenticated" && user ? (
          <span className="topbar-user">
            {user.firstName} {user.lastName}
            <button type="button" className="link-button" onClick={handleLogout}>
              Sign out
            </button>
          </span>
        ) : null}
      </div>
    </header>
  );
}
