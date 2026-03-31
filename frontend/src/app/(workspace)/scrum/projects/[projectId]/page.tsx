import EmptyState from "../../../../../components/ui/EmptyState";
import PageHeader from "../../../../../components/ui/PageHeader";
import SimpleTable from "../../../../../components/ui/SimpleTable";
import KeyValueList from "../../../../../components/ui/KeyValueList";
import ErrorState from "../../../../../components/ui/ErrorState";
import {
  getBacklog,
  getMyPermissions,
  getProjects,
  getSprints,
  getSprintHealth,
  getSprintIssues
} from "../../../../../lib/api/workspace.server";
import SprintCreateForm from "./sprintCreateForm";
import SprintAssignForm from "./sprintAssignForm";
import BacklogMoveForm from "./backlogMoveForm";
import SprintHealthPanel from "./SprintHealthPanel";
import type { IssueSummary, Project, Sprint } from "../../../../../types/domain/workspace";
import Link from "next/link";
import { PERMISSION_CODES } from "../../../../../lib/auth/permissions";

type PageProps = {
  params: Promise<{ projectId: string }>;
};

export default async function ScrumProjectPage({ params }: PageProps) {
  const { projectId: rawProjectId } = await params;
  const projectId = Number(rawProjectId);
  if (!Number.isFinite(projectId)) {
    return <ErrorState />;
  }

  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  const canViewScrum = permissionSet.has(PERMISSION_CODES.SCRUM_SPRINT_VIEW)
    || permissionSet.has(PERMISSION_CODES.SCRUM_BACKLOG_VIEW);

  if (!canViewScrum) {
    throw new Error("Access denied");
  }

  let project: Project | undefined;
  let sprints: Sprint[] = [];
  let backlog: IssueSummary[] = [];
  try {
    const projects = await getProjects({ pageSize: 100 });
    project = projects.items.find((item) => Number(item.id) === projectId);
    if (!project) {
      return <ErrorState />;
    }
    [sprints, backlog] = await Promise.all([
      getSprints(projectId),
      getBacklog(projectId)
    ]);
  } catch {
    return <ErrorState />;
  }

  const activeSprint = sprints.find((sprint) => sprint.status === "ACTIVE") as Sprint | undefined;
  const sprintIssues: IssueSummary[] = activeSprint ? await getSprintIssues(activeSprint.id) : [];
  const sprintHealth = activeSprint ? await getSprintHealth(activeSprint.id).catch(() => null) : null;

  const sprintColumns = [
    { key: "name", header: "Name", render: (row: Sprint) => row.name },
    { key: "status", header: "Status", render: (row: Sprint) => row.status },
    { key: "dates", header: "Dates", render: (row: Sprint) => `${row.startDate} → ${row.endDate}` },
    { key: "goal", header: "Goal", render: (row: Sprint) => row.goal ?? "—" }
  ];

  const issueColumns = [
    {
      key: "issueKey",
      header: "Key",
      render: (row: IssueSummary) => (
        <Link className="issue-link" href={`/issues/${row.id}`}>
          {row.issueKey}
        </Link>
      )
    },
    { key: "title", header: "Title", render: (row: IssueSummary) => row.title },
    { key: "status", header: "Status", render: (row: IssueSummary) => row.status },
    { key: "priority", header: "Priority", render: (row: IssueSummary) => row.priority },
    { key: "assignee", header: "Assignee", render: (row: IssueSummary) => row.assigneeName }
  ];

  return (
    <div className="page">
      <PageHeader
        title={`${project.projectKey} · Scrum`}
        subtitle="Sprint planning and backlog management."
      />

      <section className="section">
        <h3 className="section-title">Project Details</h3>
        <KeyValueList
          items={[
            { label: "Project", value: project.name },
            { label: "Key", value: project.projectKey },
            { label: "Status", value: project.status }
          ]}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Create Sprint</h3>
        <SprintCreateForm projectId={projectId} />
      </section>

      <section className="section">
        <h3 className="section-title">Sprints</h3>
        <SimpleTable columns={sprintColumns} rows={sprints} />
      </section>

      <section className="section">
        {activeSprint ? (
          <SprintHealthPanel sprint={activeSprint} health={sprintHealth} />
        ) : (
          <EmptyState
            size="compact"
            title="No active sprint"
            message="Activate a sprint to start tracking commitment and burndown."
          />
        )}
      </section>

      <section className="section">
        <h3 className="section-title">Backlog</h3>
        <SprintAssignForm
          projectId={projectId}
          backlog={backlog}
          sprints={sprints}
        />
        <SimpleTable columns={issueColumns} rows={backlog} />
      </section>

      <section className="section">
        <h3 className="section-title">Active Sprint Backlog</h3>
        {activeSprint ? (
          <>
            <p className="description-text">
              Active sprint: <strong>{activeSprint.name}</strong> ({activeSprint.startDate} → {activeSprint.endDate})
            </p>
            <BacklogMoveForm sprintIssues={sprintIssues} />
            <SimpleTable columns={issueColumns} rows={sprintIssues} />
          </>
        ) : (
          <EmptyState size="compact" />
        )}
      </section>
    </div>
  );
}
