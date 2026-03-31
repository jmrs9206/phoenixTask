import Link from "next/link";
import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import { getBacklog, getMyPermissions, getProjects, getSprints } from "../../../lib/api/workspace.server";
import type { Project } from "../../../types/domain/workspace";
import { ApiError } from "../../../lib/api/client";
import InsightsContextLink from "../../../components/insights/InsightsContextLink";
import { PERMISSION_CODES } from "../../../lib/auth/permissions";

type ScrumSummaryRow = {
  projectId: number;
  projectKey: string;
  projectName: string;
  active: number;
  planned: number;
  completed: number;
  backlog: number;
};

export default async function ScrumPage() {
  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  const canViewScrum = permissionSet.has(PERMISSION_CODES.SCRUM_SPRINT_VIEW)
    || permissionSet.has(PERMISSION_CODES.SCRUM_BACKLOG_VIEW);
  const canViewProjects = permissionSet.has(PERMISSION_CODES.PROJECTS_VIEW);

  if (!canViewScrum) {
    throw new Error("Access denied");
  }

  const projects = canViewProjects
    ? await getProjects({ pageSize: 100 })
    : { items: [], page: 1, pageSize: 0, total: 0, totalPages: 0 };

  const loadSummary = async (project: Project): Promise<ScrumSummaryRow | null> => {
    try {
      const [sprints, backlog] = await Promise.all([
        getSprints(project.id),
        getBacklog(project.id)
      ]);
      const active = sprints.filter((sprint) => sprint.status === "ACTIVE").length;
      const planned = sprints.filter((sprint) => sprint.status === "PLANNED").length;
      const completed = sprints.filter((sprint) => sprint.status === "COMPLETED").length;
      return {
        projectId: project.id,
        projectKey: project.projectKey,
        projectName: project.name,
        active,
        planned,
        completed,
        backlog: backlog.length
      };
    } catch (error) {
      if (error instanceof ApiError && error.status === 403) {
        return null;
      }
      throw error;
    }
  };

  const summaries = (await Promise.all(projects.items.map(loadSummary))).filter(
    (row): row is ScrumSummaryRow => row !== null
  );

  const columns = [
    {
      key: "project",
      header: "Project",
      render: (row: ScrumSummaryRow) => (
        <Link href={`/scrum/projects/${row.projectId}`}>{row.projectKey} · {row.projectName}</Link>
      )
    },
    { key: "active", header: "Active", render: (row: ScrumSummaryRow) => row.active },
    { key: "planned", header: "Planned", render: (row: ScrumSummaryRow) => row.planned },
    { key: "completed", header: "Completed", render: (row: ScrumSummaryRow) => row.completed },
    { key: "backlog", header: "Backlog", render: (row: ScrumSummaryRow) => row.backlog }
  ];

  return (
    <div className="page">
      <PageHeader
        title="Scrum"
        subtitle="Sprints and backlog overview by project."
      />
      <InsightsContextLink
        href="/analytics#delivery-flow"
        label="View sprint balance insights"
        description="Compare backlog and active sprint distribution."
      />
      <section className="section">
        <h3 className="section-title">Projects Overview</h3>
        {summaries.length > 0 ? (
          <SimpleTable columns={columns} rows={summaries} />
        ) : (
          <p className="muted">
            {canViewProjects
              ? "No accessible projects for Scrum overview."
              : "Project access is required to view Scrum overview."}
          </p>
        )}
      </section>
    </div>
  );
}
