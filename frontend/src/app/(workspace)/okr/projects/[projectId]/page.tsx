import Link from "next/link";
import EmptyState from "../../../../../components/ui/EmptyState";
import ErrorState from "../../../../../components/ui/ErrorState";
import KeyValueList from "../../../../../components/ui/KeyValueList";
import PageHeader from "../../../../../components/ui/PageHeader";
import {
  getMyPermissions,
  getOkrObjectiveDetails,
  getOkrObjectives,
  getProjectIssues,
  getProjects,
  getUsers
} from "../../../../../lib/api/workspace.server";
import ObjectiveCreateForm from "../../objectiveCreateForm";
import KeyResultCreateForm from "../../keyResultCreateForm";
import ObjectiveCheckinForm from "../../objectiveCheckinForm";
import ObjectiveInitiativeForm from "../../objectiveInitiativeForm";
import ObjectiveCloseForm from "../../objectiveCloseForm";
import type { IssueSummary, OkrObjectiveDetail, Project, User } from "../../../../../types/domain/workspace";
import { PERMISSION_CODES } from "../../../../../lib/auth/permissions";

async function safeFetch<T>(promise: Promise<T>, fallback: T): Promise<T> {
  try {
    return await promise;
  } catch {
    return fallback;
  }
}

type PageProps = {
  params: Promise<{ projectId: string }>;
};

export default async function OkrProjectPage({ params }: PageProps) {
  const { projectId: rawProjectId } = await params;
  const projectId = Number(rawProjectId);
  if (!Number.isFinite(projectId)) {
    return <ErrorState />;
  }

  const permissions = await getMyPermissions();
  const permissionSet = new Set(permissions.permissions);
  const canViewOkr = permissionSet.has(PERMISSION_CODES.OKR_OBJECTIVE_VIEW);

  if (!canViewOkr) {
    throw new Error("Access denied");
  }

  let project: Project | undefined;
  let objectives: OkrObjectiveDetail[] = [];
  let users: User[] = [];
  let issues: IssueSummary[] = [];

  try {
    const projects = await getProjects({ pageSize: 100 });
    project = projects.items.find((item) => Number(item.id) === projectId);
    if (!project) {
      return <ErrorState />;
    }
    [objectives, users, issues] = await Promise.all([
      getOkrObjectiveDetails(projectId),
      safeFetch(getUsers(), []),
      safeFetch(getProjectIssues(projectId), [])
    ]);
  } catch {
    return <ErrorState />;
  }

  const objectiveSummaries = await getOkrObjectives(projectId);

  return (
    <div className="page">
      <PageHeader
        title={`${project.projectKey} · OKR`}
        subtitle="Objectives and key results scoped to this project."
      />

      <section className="section">
        <h3 className="section-title">Project Context</h3>
        <KeyValueList
          items={[
            { label: "Project", value: project.name },
            { label: "Key", value: project.projectKey },
            { label: "Status", value: project.status }
          ]}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Create Objective</h3>
        <ObjectiveCreateForm projectId={projectId} users={users} />
      </section>

      <section className="section">
        <h3 className="section-title">Create Key Result</h3>
        <KeyResultCreateForm objectives={objectiveSummaries} />
      </section>

      <section className="section">
        <h3 className="section-title">Objectives</h3>
        <div className="okr-list">
          {objectives.length === 0 ? (
            <EmptyState
              title="No objectives yet"
              message="Create the first project objective to start tracking outcomes."
            />
          ) : (
            objectives.map((objective) => (
              <div key={objective.id} className="okr-card">
                <div className="okr-header">
                  <div>
                    <h4>{objective.title}</h4>
                    <p>{objective.description ?? "No description"}</p>
                  </div>
                  <div className="okr-meta">
                    <span>{objective.status}</span>
                    <span>{objective.periodStart} → {objective.periodEnd}</span>
                    <span>Owner: {objective.ownerName}</span>
                  </div>
                </div>
                <div className="okr-tags">
                  <span className="okr-tag">Confidence: {objective.confidenceLevel ?? "Not set"}</span>
                  <span className="okr-tag">
                    Score: {objective.finalScore !== null ? `${objective.finalScore.toFixed(1)}%` : "Not scored"}
                  </span>
                  {objective.closedAt && (
                    <span className="okr-tag">Closed {new Date(objective.closedAt).toLocaleDateString()}</span>
                  )}
                </div>
                <div className="okr-progress">
                  <div className="okr-progress-bar">
                    <div style={{ width: `${Math.round(objective.progress * 100)}%` }} />
                  </div>
                  <span>{Math.round(objective.progress * 100)}%</span>
                </div>
                <div className="okr-key-results">
                  {objective.keyResults.length === 0 ? (
                    <EmptyState size="compact" />
                  ) : (
                    objective.keyResults.map((kr) => (
                      <div key={kr.id} className="okr-kr">
                        <div>
                          <strong>{kr.title}</strong>
                          <span className="muted">
                            {kr.projectKey} · {kr.projectName}
                          </span>
                        </div>
                        <div className="okr-kr-metrics">
                          <span>{kr.currentValue} / {kr.targetValue} {kr.unit}</span>
                          <span>{kr.status}</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
                <div className="okr-section">
                  <div className="okr-section-title">Check-ins</div>
                  {objective.checkins.length === 0 ? (
                    <p className="muted">No check-ins yet.</p>
                  ) : (
                    <div className="okr-checkins">
                      {objective.checkins.map((checkin) => (
                        <div key={checkin.id} className="okr-checkin">
                          <div>
                            <strong>{checkin.authorName}</strong>
                            <span className="muted"> · {new Date(checkin.createdAt).toLocaleDateString()}</span>
                          </div>
                          <div className="okr-checkin-meta">
                            <span>Confidence: {checkin.confidenceLevel}</span>
                            <span>
                              Progress: {checkin.progressPercent !== null ? `${checkin.progressPercent}%` : "—"}
                            </span>
                          </div>
                          {checkin.note && <p className="muted">{checkin.note}</p>}
                        </div>
                      ))}
                    </div>
                  )}
                  <ObjectiveCheckinForm objectiveId={objective.id} disabled={objective.status !== "ACTIVE"} />
                </div>
                <div className="okr-section">
                  <div className="okr-section-title">Initiatives</div>
                  {objective.initiatives.length === 0 ? (
                    <p className="muted">No initiatives linked yet.</p>
                  ) : (
                    <div className="okr-initiatives">
                      {objective.initiatives.map((initiative) => (
                        <Link
                          key={initiative.id}
                          href={initiative.issueId ? `/issues/${initiative.issueId}` : `/projects/${initiative.projectId}`}
                          className="okr-initiative"
                        >
                          {initiative.issueKey
                            ? `${initiative.issueKey} · ${initiative.issueTitle ?? "Issue"}`
                            : `${initiative.projectKey} · ${initiative.projectName}`}
                        </Link>
                      ))}
                    </div>
                  )}
                  <ObjectiveInitiativeForm
                    objectiveId={objective.id}
                    issues={issues}
                    disabled={objective.status !== "ACTIVE"}
                  />
                </div>
                <div className="okr-section">
                  <div className="okr-section-title">Close Objective</div>
                  <ObjectiveCloseForm
                    objectiveId={objective.id}
                    status={objective.status}
                    finalScore={objective.finalScore}
                    closedAt={objective.closedAt}
                  />
                </div>
              </div>
            ))
          )}
        </div>
      </section>
    </div>
  );
}
