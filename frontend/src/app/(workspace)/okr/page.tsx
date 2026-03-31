import Link from "next/link";
import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import { getMyPermissions, getOkrObjectives, getProjects } from "../../../lib/api/workspace.server";
import type { Project } from "../../../types/domain/workspace";
import { ApiError } from "../../../lib/api/client";
import { PERMISSION_CODES } from "../../../lib/auth/permissions";

type OkrProjectSummary = {
  projectId: number;
  projectKey: string;
  projectName: string;
  total: number;
  active: number;
  completed: number;
};

export default async function OkrPage() {
  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  const canViewOkr = permissionSet.has(PERMISSION_CODES.OKR_OBJECTIVE_VIEW);
  const canViewProjects = permissionSet.has(PERMISSION_CODES.PROJECTS_VIEW);

  if (!canViewOkr) {
    throw new Error("Access denied");
  }

  const projects = canViewProjects
    ? await getProjects({ pageSize: 100 })
    : { items: [], page: 1, pageSize: 0, total: 0, totalPages: 0 };

  const loadSummary = async (project: Project): Promise<OkrProjectSummary | null> => {
    try {
      const objectives = await getOkrObjectives(project.id);
      const active = objectives.filter((objective) => objective.status === "ACTIVE").length;
      const completed = objectives.filter((objective) => objective.status === "COMPLETED").length;
      return {
        projectId: project.id,
        projectKey: project.projectKey,
        projectName: project.name,
        total: objectives.length,
        active,
        completed
      };
    } catch (error) {
      if (error instanceof ApiError && error.status === 403) {
        return null;
      }
      throw error;
    }
  };

  const summaries = (await Promise.all(projects.items.map(loadSummary))).filter(
    (row): row is OkrProjectSummary => row !== null
  );

  const columns = [
    {
      key: "project",
      header: "Project",
      render: (row: OkrProjectSummary) => (
        <Link href={`/okr/projects/${row.projectId}`}>{row.projectKey} · {row.projectName}</Link>
      )
    },
    { key: "total", header: "Objectives", render: (row: OkrProjectSummary) => row.total },
    { key: "active", header: "Active", render: (row: OkrProjectSummary) => row.active },
    { key: "completed", header: "Completed", render: (row: OkrProjectSummary) => row.completed }
  ];

  return (
    <div className="page">
      <PageHeader
        title="OKR"
        subtitle="Project-scoped objectives and key results."
      />
      <section className="section">
        <h3 className="section-title">Projects Overview</h3>
        {summaries.length > 0 ? (
          <SimpleTable columns={columns} rows={summaries} />
        ) : (
          <p className="muted">
            {canViewProjects
              ? "No accessible projects for OKR overview."
              : "Project access is required to view OKR overview."}
          </p>
        )}
      </section>
    </div>
  );
}
