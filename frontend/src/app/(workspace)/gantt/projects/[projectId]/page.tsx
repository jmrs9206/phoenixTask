import PageHeader from "../../../../../components/ui/PageHeader";
import KeyValueList from "../../../../../components/ui/KeyValueList";
import SimpleTable from "../../../../../components/ui/SimpleTable";
import ErrorState from "../../../../../components/ui/ErrorState";
import { getGanttProject } from "../../../../../lib/api/workspace.server";
import type { GanttDependency, GanttIssue, GanttResourceLoad } from "../../../../../types/domain/workspace";
import Link from "next/link";
import GanttBaselineControls from "./ganttBaselineControls";
import GanttDependencyForm from "./ganttDependencyForm";

type PageProps = {
  params: Promise<{ projectId: string }>;
};

export default async function GanttProjectPage({ params }: PageProps) {
  const { projectId: rawProjectId } = await params;
  const projectId = Number(rawProjectId);
  if (!Number.isFinite(projectId)) {
    return <ErrorState />;
  }

  let project: Awaited<ReturnType<typeof getGanttProject>>;
  try {
    project = await getGanttProject(projectId);
  } catch {
    return <ErrorState />;
  }

  const dependsOnMap = new Map<number, string[]>();
  project.dependencies.forEach((dependency: GanttDependency) => {
    const list = dependsOnMap.get(dependency.successorIssueId) ?? [];
    list.push(dependency.predecessorIssueKey);
    dependsOnMap.set(dependency.successorIssueId, list);
  });

  const columns = [
    {
      key: "issueKey",
      header: "Key",
      render: (row: GanttIssue) => (
        <Link className="issue-link" href={`/issues/${row.issueId}`}>
          {row.issueKey}
        </Link>
      )
    },
    { key: "title", header: "Title", render: (row: GanttIssue) => row.title },
    {
      key: "plannedStartDate",
      header: "Planned start",
      render: (row: GanttIssue) => row.plannedStartDate ?? "—"
    },
    {
      key: "dueDate",
      header: "Due date",
      render: (row: GanttIssue) => row.dueDate ?? "—"
    },
    {
      key: "baselineStartDate",
      header: "Baseline start",
      render: (row: GanttIssue) => row.baselineStartDate ?? "—"
    },
    {
      key: "baselineEndDate",
      header: "Baseline end",
      render: (row: GanttIssue) => row.baselineEndDate ?? "—"
    },
    {
      key: "dependsOn",
      header: "Depends on",
      render: (row: GanttIssue) => {
        const deps = dependsOnMap.get(row.issueId) ?? [];
        return deps.length ? deps.join(", ") : "—";
      }
    },
    { key: "status", header: "Status", render: (row: GanttIssue) => row.status }
  ];

  const resourceColumns = [
    { key: "assignee", header: "Assignee", render: (row: GanttResourceLoad) => row.assigneeName },
    { key: "issueCount", header: "Issues", render: (row: GanttResourceLoad) => row.issueCount },
    { key: "totalPlannedDays", header: "Planned days", render: (row: GanttResourceLoad) => row.totalPlannedDays },
    {
      key: "window",
      header: "Date window",
      render: (row: GanttResourceLoad) => {
        if (!row.windowStart && !row.windowEnd) return "—";
        return `${row.windowStart ?? "—"} → ${row.windowEnd ?? "—"}`;
      }
    }
  ];

  return (
    <div className="page">
      <PageHeader
        title={`${project.projectKey} · Gantt`}
        subtitle="Planned timeline of issues with real dates."
      />

      <section className="section">
        <h3 className="section-title">Project Details</h3>
        <KeyValueList
          items={[
            { label: "Project", value: project.projectName },
            { label: "Key", value: project.projectKey },
            { label: "Planned start", value: project.plannedStartDate ?? "—" },
            { label: "Planned end", value: project.plannedEndDate ?? "—" }
          ]}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Baseline Snapshot</h3>
        <KeyValueList
          items={[
            {
              label: "Baseline start",
              value: project.baseline?.baselineStartDate ?? "Not captured"
            },
            {
              label: "Baseline end",
              value: project.baseline?.baselineEndDate ?? "Not captured"
            },
            {
              label: "Captured at",
              value: project.baseline?.capturedAt
                ? new Date(project.baseline.capturedAt).toLocaleString()
                : "Not captured"
            },
            {
              label: "Captured by",
              value: project.baseline?.capturedByName ?? "—"
            }
          ]}
        />
        <GanttBaselineControls projectId={project.projectId} baseline={project.baseline} />
      </section>

      <section className="section">
        <h3 className="section-title">Dependencies</h3>
        {project.dependencies.length === 0 ? (
          <p className="muted">No dependencies defined yet. Add finish-to-start links to unlock critical path.</p>
        ) : (
          <SimpleTable
            columns={[
              { key: "predecessor", header: "Predecessor", render: (row: GanttDependency) => row.predecessorIssueKey },
              { key: "successor", header: "Successor", render: (row: GanttDependency) => row.successorIssueKey },
              { key: "type", header: "Type", render: (row: GanttDependency) => row.dependencyType }
            ]}
            rows={project.dependencies}
          />
        )}
        <GanttDependencyForm projectId={project.projectId} issues={project.issues} />
      </section>

      <section className="section">
        <h3 className="section-title">Critical Path</h3>
        {project.criticalPath.length === 0 ? (
          <p className="muted">
            No critical path available yet. Add dependencies and planned dates to compute the blocking chain.
          </p>
        ) : (
          <ol className="stacked-list">
            {project.criticalPath.map((item) => (
              <li key={item.issueId} className="stacked-card">
                <div className="stacked-title">
                  <Link className="issue-link" href={`/issues/${item.issueId}`}>{item.issueKey}</Link>
                  <span>{item.title}</span>
                </div>
                <div className="stacked-meta">
                  <span>{item.plannedStartDate ?? "—"} → {item.dueDate ?? "—"}</span>
                  <span>{item.durationDays}d</span>
                </div>
              </li>
            ))}
          </ol>
        )}
        <p className="muted">
          Critical path uses finish-to-start dependencies and planned dates. Items without dates default to 1 day.
        </p>
      </section>

      <section className="section">
        <h3 className="section-title">Resource Load</h3>
        {project.resourceLoad.length === 0 ? (
          <p className="muted">No assigned work yet.</p>
        ) : (
          <SimpleTable columns={resourceColumns} rows={project.resourceLoad} />
        )}
        <p className="muted">
          Load is calculated from open issues with planned dates (sum of planned duration per assignee).
        </p>
      </section>

      <section className="section">
        <h3 className="section-title">Timeline</h3>
        <SimpleTable columns={columns} rows={project.issues} />
      </section>
    </div>
  );
}
