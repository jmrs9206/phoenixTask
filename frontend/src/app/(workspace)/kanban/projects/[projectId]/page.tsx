import PageHeader from "../../../../../components/ui/PageHeader";
import EmptyState from "../../../../../components/ui/EmptyState";
import ErrorState from "../../../../../components/ui/ErrorState";
import { getKanbanBoard, getMyPermissions } from "../../../../../lib/api/workspace.server";
import Link from "next/link";
import { PERMISSION_CODES } from "../../../../../lib/auth/permissions";

type PageProps = {
  params: Promise<{ projectId: string }>;
};

export default async function KanbanProjectPage({ params }: PageProps) {
  const { projectId: rawProjectId } = await params;
  const projectId = Number(rawProjectId);
  if (!Number.isFinite(projectId)) {
    return <ErrorState />;
  }

  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  if (!permissionSet.has(PERMISSION_CODES.KANBAN_BOARD_VIEW)) {
    throw new Error("Access denied");
  }

  let board: Awaited<ReturnType<typeof getKanbanBoard>>;
  try {
    board = await getKanbanBoard(projectId);
  } catch {
    return <ErrorState />;
  }

  return (
    <div className="page">
      <PageHeader
        title={`${board.projectKey} · Kanban`}
        subtitle="Live issue status board derived from project issues."
      />
      <section className="kanban-metrics">
        <div className="kanban-metric-card">
          <span className="kanban-metric-label">Throughput (done in last 7 days)</span>
          <strong className="kanban-metric-value">{board.metrics.throughputLast7Days}</strong>
          <span className="muted">Based on issues moved to DONE in the last 7 days.</span>
        </div>
        <div className="kanban-metric-card">
          <span className="kanban-metric-label">Average WIP age (days)</span>
          <strong className="kanban-metric-value">{board.metrics.averageWipAgeDays}</strong>
          <span className="muted">Uses last status update as the age signal.</span>
        </div>
        <div className="kanban-metric-card">
          <span className="kanban-metric-label">Oldest WIP item (days)</span>
          <strong className="kanban-metric-value">{board.metrics.oldestWipAgeDays}</strong>
          <span className="muted">Based on last updated date for in‑progress items.</span>
        </div>
      </section>
      <section className="kanban-board">
        <div className="kanban-columns-header">
          {board.columns.map((column) => (
            <div
              key={column.status}
              className={`kanban-column-header ${column.overLimit ? "is-over-limit" : ""}`}
            >
              <div className="kanban-column-title">
                <span>{column.title}</span>
                <span className="kanban-count">{column.totalCount}</span>
              </div>
              <div className="kanban-wip">
                WIP {column.totalCount}/{column.wipLimit ?? "—"}
              </div>
              <div className="kanban-policy">{column.policy}</div>
            </div>
          ))}
        </div>
        {board.swimlanes.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="kanban-swimlanes">
            {board.swimlanes.map((lane) => (
              <div key={lane.laneId} className="kanban-lane">
                <div className="kanban-lane-title">{lane.label}</div>
                <div className="kanban-lane-columns">
                  {lane.columns.map((column) => (
                    <div key={column.status} className="kanban-column">
                      <div className="kanban-cards">
                        {column.issues.length === 0 ? (
                          <EmptyState size="compact" />
                        ) : (
                          column.issues.map((issue) => (
                            <Link key={issue.issueId} href={`/issues/${issue.issueId}`} className="kanban-card">
                              <div className="kanban-card-header">
                                <strong>{issue.issueKey}</strong>
                                <span>{issue.priority}</span>
                              </div>
                              <p>{issue.title}</p>
                              <div className="kanban-card-footer">
                                <span className="muted">{issue.assigneeName}</span>
                                <span className="kanban-card-age">{issue.ageDays}d</span>
                              </div>
                            </Link>
                          ))
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
