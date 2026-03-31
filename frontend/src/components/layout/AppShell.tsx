"use client";

import type { ReactNode } from "react";
import { useState } from "react";
import SidebarNav from "./SidebarNav";
import TopBar from "./TopBar";

export default function AppShell({ children }: { children: ReactNode }) {
  const [navOpen, setNavOpen] = useState(false);

  const handleToggleNav = () => {
    setNavOpen((value) => !value);
  };

  const handleCloseNav = () => {
    setNavOpen(false);
  };

  return (
    <div className={`app-shell${navOpen ? " is-nav-open" : ""}`}>
      <SidebarNav onNavigate={handleCloseNav} />
      <div className="shell-content">
        <TopBar onToggleNav={handleToggleNav} />
        <main className="main-content">{children}</main>
      </div>
      <button
        type="button"
        className="nav-overlay"
        aria-label="Close navigation"
        onClick={handleCloseNav}
      />
    </div>
  );
}
