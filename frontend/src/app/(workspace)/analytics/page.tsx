import EmptyState from "../../../components/ui/EmptyState";
import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import StatCard from "../../../components/ui/StatCard";
import { getAnalyticsAdvanced, getAnalyticsSummary } from "../../../lib/api/workspace.server";
import type {
  AnalyticsCfdPoint,
  AnalyticsCycleTimePoint,
  AnalyticsIssuesByProject,
  AnalyticsSprintBacklog
} from "../../../types/domain/workspace";

export default async function AnalyticsPage() {
  const [summary, advanced] = await Promise.all([
    getAnalyticsSummary(),
    getAnalyticsAdvanced()
  ]);

  const issuesByProjectColumns = [
    { key: "projectKey", header: "Project", render: (row: AnalyticsIssuesByProject) => row.projectKey },
    { key: "count", header: "Issues", render: (row: AnalyticsIssuesByProject) => row.count }
  ];

  const sprintBacklogColumns = [
    { key: "projectKey", header: "Project", render: (row: AnalyticsSprintBacklog) => row.projectKey },
    { key: "backlog", header: "Backlog", render: (row: AnalyticsSprintBacklog) => row.backlog },
    { key: "activeSprint", header: "Active sprint", render: (row: AnalyticsSprintBacklog) => row.activeSprint }
  ];

  const maxCfdTotal = Math.max(
    ...advanced.cfdSeries.map((point: AnalyticsCfdPoint) => point.total),
    1
  );

  const maxCycleTime = Math.max(
    ...advanced.cycleTimeSeries.map((point: AnalyticsCycleTimePoint) => point.cycleTimeDays),
    1
  );

  return (
    <div className="page">
      <PageHeader
        title="Insights"
        subtitle="Operational visibility derived from real workspace activity."
      />
      <div className="insights-nav">
        <a href="#overview">Overview</a>
        <a href="#work-health">Delivery flow</a>
        <a href="#project-distribution">Workload distribution</a>
        <a href="#delivery-flow">Sprint balance</a>
        <a href="#advanced-flow">Advanced flow</a>
        <a href="#reliability">Reliability</a>
        <a href="#tech-debt">Tech debt</a>
      </div>

      <section className="section insights-overview" id="overview">
        <div className="insights-overview-copy">
          <h3 className="section-title">Overview</h3>
          <p className="muted">
            Insights surfaces delivery signals and workload distribution based on
            live workspace data. It stays focused on execution instead of executive
            dashboards.
          </p>
        </div>
        <div className="card-grid">
          <StatCard label="Teams" value={summary.teamCount} />
          <StatCard label="Projects" value={summary.projectCount} />
          <StatCard label="Users" value={summary.userCount} />
          <StatCard label="Messages" value={summary.messageCount} />
        </div>
      </section>

      <section className="section" id="work-health">
        <div className="insights-section-header">
          <h3 className="section-title">Delivery Flow</h3>
          <span className="muted">Issue distribution by status and priority.</span>
        </div>
        <div className="insights-split">
          <div>
            <h4 className="section-subtitle">Issues by Status</h4>
            {Object.keys(summary.issuesByStatus).length === 0 ? (
              <EmptyState size="compact" />
            ) : (
              <div className="stat-list">
                {Object.entries(summary.issuesByStatus).map(([status, count]) => (
                  <div key={status} className="stat-row">
                    <span>{status}</span>
                    <strong>{count}</strong>
                  </div>
                ))}
              </div>
            )}
          </div>
          <div>
            <h4 className="section-subtitle">Issues by Priority</h4>
            {Object.keys(summary.issuesByPriority).length === 0 ? (
              <EmptyState size="compact" />
            ) : (
              <div className="stat-list">
                {Object.entries(summary.issuesByPriority).map(([priority, count]) => (
                  <div key={priority} className="stat-row">
                    <span>{priority}</span>
                    <strong>{count}</strong>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </section>

      <section className="section" id="project-distribution">
        <div className="insights-section-header">
          <h3 className="section-title">Workload Distribution</h3>
          <span className="muted">Project-level load and backlog balance.</span>
        </div>
        <div className="insights-table-stack">
          <div>
            <h4 className="section-subtitle">Issues by Project</h4>
            <SimpleTable columns={issuesByProjectColumns} rows={summary.issuesByProject} />
          </div>
          <div id="delivery-flow">
            <h4 className="section-subtitle">Sprint Backlog vs Active</h4>
            <SimpleTable columns={sprintBacklogColumns} rows={summary.sprintBacklogCounts} />
          </div>
        </div>
      </section>

      <section className="section" id="advanced-flow">
        <div className="insights-section-header">
          <h3 className="section-title">Advanced Flow</h3>
          <span className="muted">
            CFD and flow variability derived from current status and completion data (proxy signals).
          </span>
        </div>
        <div className="insights-stack">
          <div className="insights-card">
            <div className="insights-card-header">
              <h4 className="section-subtitle">Cumulative Flow (approx)</h4>
              <span className="muted">
                {advanced.cfdApproximate
                  ? `Approximation: assumes current status from creation until completion. Window: last ${advanced.cfdWindowDays} days.`
                  : `Window: last ${advanced.cfdWindowDays} days.`}
              </span>
            </div>
            {advanced.cfdSeries.length === 0 ? (
              <EmptyState size="compact" />
            ) : (
              <div className="cfd-chart">
                <div className="cfd-legend">
                  <span className="cfd-pill open">OPEN</span>
                  <span className="cfd-pill in-progress">IN_PROGRESS</span>
                  <span className="cfd-pill blocked">BLOCKED</span>
                  <span className="cfd-pill done">DONE</span>
                </div>
                {advanced.cfdSeries.map((point: AnalyticsCfdPoint) => (
                  <div key={point.date} className="cfd-row">
                    <span className="cfd-date">{point.date}</span>
                    <div className="cfd-bar">
                      <span
                        className="cfd-seg open"
                        style={{ width: `${(point.open / maxCfdTotal) * 100}%` }}
                      />
                      <span
                        className="cfd-seg in-progress"
                        style={{ width: `${(point.inProgress / maxCfdTotal) * 100}%` }}
                      />
                      <span
                        className="cfd-seg blocked"
                        style={{ width: `${(point.blocked / maxCfdTotal) * 100}%` }}
                      />
                      <span
                        className="cfd-seg done"
                        style={{ width: `${(point.done / maxCfdTotal) * 100}%` }}
                      />
                    </div>
                    <span className="cfd-total">{point.total}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="insights-card">
            <div className="insights-card-header">
              <h4 className="section-subtitle">Cycle Time Variability (proxy)</h4>
              <span className="muted">
                Equivalent to a control chart using completed issues (created → updated date).
              </span>
            </div>
            {advanced.cycleTimeSeries.length === 0 ? (
              <EmptyState size="compact" />
            ) : (
              <div className="flow-chart">
                <div className="flow-summary">
                  <div>
                    <strong>{advanced.flowSummary.medianDays}d</strong>
                    <span className="muted">Median</span>
                  </div>
                  <div>
                    <strong>{advanced.flowSummary.p85Days}d</strong>
                    <span className="muted">P85</span>
                  </div>
                  <div>
                    <strong>{advanced.flowSummary.sampleSize}</strong>
                    <span className="muted">Completed issues</span>
                  </div>
                </div>
                {advanced.cycleTimeSeries.map((point: AnalyticsCycleTimePoint) => (
                  <div key={`${point.issueKey}-${point.completedOn}`} className="flow-row">
                    <div className="flow-meta">
                      <strong>{point.issueKey}</strong>
                      <span className="muted">{point.completedOn}</span>
                    </div>
                    <div className="flow-bar-wrap">
                      <div
                        className="flow-bar"
                        style={{ width: `${(point.cycleTimeDays / maxCycleTime) * 100}%` }}
                      />
                    </div>
                    <span className="flow-value">{point.cycleTimeDays}d</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </section>

      <section className="section" id="reliability">
        <div className="insights-section-header">
          <h3 className="section-title">Reliability</h3>
          <span className="muted">
            MTTR proxy based on completed issues (created → updated date).
          </span>
        </div>
        <div className="card-grid">
          <StatCard label="MTTR (proxy, days)" value={`${advanced.mttrDays}d`} />
        </div>
      </section>

      <section className="section" id="tech-debt">
        <div className="insights-section-header">
          <h3 className="section-title">Tech Debt Ratio</h3>
          <span className="muted">
            Based on issues flagged as tech debt (internal marker). Ratio shown for open issues only.
          </span>
        </div>
        <div className="card-grid">
          <StatCard label="Debt ratio" value={`${advanced.techDebt.ratio}%`} />
          <StatCard label="Debt issues (open)" value={advanced.techDebt.debtCount} />
          <StatCard label="Total open issues" value={advanced.techDebt.totalOpenCount} />
        </div>
      </section>
    </div>
  );
}
