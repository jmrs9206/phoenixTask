import Link from "next/link";
import PageHeader from "../../../components/ui/PageHeader";
import { getMyPermissions } from "../../../lib/api/workspace.server";
import { PERMISSION_CODES } from "../../../lib/auth/permissions";

const QUICK_LINKS = [
  {
    title: "Issues",
    description: "Track and resolve the work that matters most.",
    href: "/issues",
    permissions: [PERMISSION_CODES.ISSUES_VIEW]
  },
  {
    title: "Projects",
    description: "Keep teams aligned on scope and delivery.",
    href: "/projects",
    permissions: [PERMISSION_CODES.PROJECTS_VIEW]
  },
  {
    title: "Teams",
    description: "See who is working together across initiatives.",
    href: "/teams",
    permissions: [PERMISSION_CODES.TEAMS_VIEW]
  },
  {
    title: "Messages",
    description: "Coordinate updates where work happens.",
    href: "/messages",
    permissions: [PERMISSION_CODES.MESSAGES_READ]
  },
  {
    title: "Scrum",
    description: "Plan sprints and manage active backlogs.",
    href: "/scrum",
    permissions: [PERMISSION_CODES.SCRUM_SPRINT_VIEW]
  },
  {
    title: "Kanban",
    description: "Visualize flow and manage in-progress work.",
    href: "/kanban",
    permissions: [PERMISSION_CODES.KANBAN_BOARD_VIEW]
  },
  {
    title: "Gantt",
    description: "Map dependencies and timelines by project.",
    href: "/gantt",
    permissions: [PERMISSION_CODES.GANTT_VIEW]
  },
  {
    title: "OKR",
    description: "Connect objectives with delivery outcomes.",
    href: "/okr",
    permissions: [PERMISSION_CODES.OKR_OBJECTIVE_VIEW]
  },
  {
    title: "Settings",
    description: "Manage workspace preferences and setup.",
    href: "/settings",
    permissions: [PERMISSION_CODES.COMPANY_SETTINGS_VIEW]
  }
];

export default async function HomePage() {
  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  const quickLinks = QUICK_LINKS.filter((item) =>
    item.permissions.every((code) => permissionSet.has(code))
  );

  return (
    <div className="page home-page">
      <PageHeader
        title="PhoenixTask®"
        subtitle="A structured work OS for focused teams and clear delivery."
      />

      <section className="section home-hero">
        <div className="home-hero-copy">
          <h2>Start with clarity, execute with confidence.</h2>
          <p className="description-text">
            PhoenixTask® keeps work, collaboration, and strategy organized without
            the clutter. Use Home to orient yourself, then jump straight into the
            modules that matter to your role.
          </p>
        </div>
        <div className="home-hero-panel">
          <div className="home-hero-card">
            <div className="home-hero-title">Today in PhoenixTask®</div>
            <ul>
              <li>Review issues that need attention.</li>
              <li>Open your active project workspaces.</li>
              <li>Coordinate updates with your team.</li>
            </ul>
          </div>
        </div>
      </section>

      <section className="section">
        <h3 className="section-title">Quick access</h3>
        {quickLinks.length === 0 ? (
          <p className="muted">No modules available for your current permissions.</p>
        ) : (
          <div className="home-quick-grid">
            {quickLinks.map((item) => (
              <Link key={item.href} href={item.href} className="home-quick-card">
                <div className="home-quick-title">{item.title}</div>
                <p className="muted">{item.description}</p>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="section home-guidance">
        <h3 className="section-title">How to navigate PhoenixTask®</h3>
        <div className="home-guidance-grid">
          <div className="home-guidance-card">
            <h4>Work</h4>
            <p className="muted">
              Issues, Projects, Scrum, Kanban, and Gantt live here. Track delivery
              and manage execution.
            </p>
          </div>
          <div className="home-guidance-card">
            <h4>Collaboration</h4>
            <p className="muted">
              Messages keep conversation connected to the work without mixing in
              analytics.
            </p>
          </div>
          <div className="home-guidance-card">
            <h4>Strategy</h4>
            <p className="muted">
              OKRs provide alignment between objectives and execution.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}
