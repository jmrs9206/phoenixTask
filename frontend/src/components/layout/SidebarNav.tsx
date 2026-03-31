"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "../auth/AuthProvider";
import type { UiAction } from "../../lib/auth/permissions";

type NavItem = { href: string; label: string; action?: UiAction };
type NavSection = { title: string; items: NavItem[] };

const navSections: NavSection[] = [
  {
    title: "Work",
    items: [
      { href: "/issues", label: "Issues", action: "issues.view" },
      { href: "/projects", label: "Projects", action: "projects.view" },
      { href: "/teams", label: "Teams", action: "teams.view" },
      { href: "/scrum", label: "Scrum", action: "scrum.sprint.view" },
      { href: "/kanban", label: "Kanban", action: "kanban.board.view" },
      { href: "/gantt", label: "Gantt", action: "gantt.view" }
    ]
  },
  {
    title: "Collaboration",
    items: [{ href: "/messages", label: "Messages", action: "messages.read" }]
  },
  {
    title: "Strategy",
    items: [{ href: "/okr", label: "OKR", action: "okr.objective.view" }]
  },
  {
    title: "Insights",
    items: [{ href: "/analytics", label: "Insights", action: "analytics.view" }]
  },
  {
    title: "Platform",
    items: [
      { href: "/users", label: "Users", action: "users.view" },
      { href: "/settings", label: "Settings", action: "company.settings.view" }
    ]
  }
];

type SidebarNavProps = {
  onNavigate?: () => void;
};

export default function SidebarNav({ onNavigate }: SidebarNavProps) {
  const pathname = usePathname();
  const { can } = useAuth();
  const visibleSections = navSections
    .map((section) => ({
      title: section.title,
      items: section.items.filter((item) => (item.action ? can(item.action) : true))
    }))
    .filter((section) => section.items.length > 0);

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <h1>PhoenixTask®</h1>
        <span>Workspace</span>
      </div>
      <nav className="nav-list">
        {visibleSections.map((section) => (
          <div key={section.title} className="nav-section">
            <div className="nav-section-title">{section.title}</div>
            {section.items.map((item) => {
              const isActive = pathname === item.href || pathname.startsWith(`${item.href}/`);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`nav-link${isActive ? " is-active" : ""}`}
                  onClick={onNavigate}
                >
                  {item.label}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>
    </aside>
  );
}
