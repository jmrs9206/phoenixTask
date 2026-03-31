import Link from "next/link";
import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import { getKanbanProjects, getMyPermissions } from "../../../lib/api/workspace.server";
import type { KanbanProjectSummary } from "../../../types/domain/workspace";
import InsightsContextLink from "../../../components/insights/InsightsContextLink";
import { PERMISSION_CODES } from "../../../lib/auth/permissions";

export default async function KanbanPage() {
  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  if (!permissionSet.has(PERMISSION_CODES.KANBAN_BOARD_VIEW)) {
    throw new Error("Access denied");
  }

  const projects = await getKanbanProjects();

  const columns = [
    {
      key: "project",
      header: "Project",
      render: (row: KanbanProjectSummary) => (
        <Link href={`/kanban/projects/${row.projectId}`}>{row.projectKey} · {row.projectName}</Link>
      )
    },
    { key: "open", header: "Open", render: (row: KanbanProjectSummary) => row.issueCounts.OPEN ?? 0 },
    { key: "inProgress", header: "In Progress", render: (row: KanbanProjectSummary) => row.issueCounts.IN_PROGRESS ?? 0 },
    { key: "blocked", header: "Blocked", render: (row: KanbanProjectSummary) => row.issueCounts.BLOCKED ?? 0 },
    { key: "done", header: "Done", render: (row: KanbanProjectSummary) => row.issueCounts.DONE ?? 0 }
  ];

  return (
    <div className="page">
      <PageHeader title="Kanban" subtitle="Issue status by project." />
      <InsightsContextLink
        href="/analytics#work-health"
        label="View flow insights"
        description="See status distribution across the workspace."
      />
      <section className="section">
        <h3 className="section-title">Projects</h3>
        <SimpleTable
          columns={columns}
          rows={projects}
          emptyTitle="No kanban data"
          emptyMessage="No accessible projects for this board yet."
        />
      </section>
    </div>
  );
}
