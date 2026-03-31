import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import EmptyState from "../../../components/ui/EmptyState";
import PaginationControls from "../../../components/ui/PaginationControls";
import { getIssues } from "../../../lib/api/workspace.server";
import type { Issue } from "../../../types/domain/workspace";
import IssueCreateForm from "./issueCreateForm";
import Link from "next/link";
import InsightsContextLink from "../../../components/insights/InsightsContextLink";

const columns = [
  {
    key: "issueKey",
    header: "Issue",
    render: (issue: Issue) => (
      <Link className="issue-link" href={`/issues/${issue.id}`}>
        {issue.issueKey}
      </Link>
    )
  },
  { key: "title", header: "Title", render: (issue: Issue) => issue.title },
  { key: "project", header: "Project", render: (issue: Issue) => issue.projectKey },
  { key: "status", header: "Status", render: (issue: Issue) => issue.status },
  { key: "priority", header: "Priority", render: (issue: Issue) => issue.priority },
  { key: "assignee", header: "Assignee", render: (issue: Issue) => issue.assigneeName },
  { key: "created", header: "Created", render: (issue: Issue) => new Date(issue.createdAt).toLocaleDateString() }
];

type SearchParams = Record<string, string | string[] | undefined>;

const STATUS_OPTIONS = ["OPEN", "IN_PROGRESS", "BLOCKED", "DONE"];
const PRIORITY_OPTIONS = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

function parseString(value: string | string[] | undefined) {
  if (Array.isArray(value)) {
    return value[0] ?? "";
  }
  return value ?? "";
}

function parseNumber(value: string | string[] | undefined, fallback: number) {
  const parsed = Number(parseString(value));
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return parsed;
}

export default async function IssuesPage({
  searchParams
}: {
  searchParams?: SearchParams | Promise<SearchParams>;
}) {
  const params = (await Promise.resolve(searchParams)) ?? {};
  const page = parseNumber(params.page, 1);
  const pageSize = Math.min(parseNumber(params.pageSize, 25), 100);
  const query = parseString(params.query);
  const status = parseString(params.status);
  const priority = parseString(params.priority);
  const projectIdParam = parseString(params.projectId);
  const projectId = projectIdParam ? Number(projectIdParam) : undefined;

  const issues = await getIssues({
    page,
    pageSize,
    query,
    status: status || undefined,
    priority: priority || undefined,
    projectId: Number.isFinite(projectId) ? projectId : undefined
  });

  const buildHref = (nextPage: number) => {
    const search = new URLSearchParams();
    search.set("page", String(nextPage));
    search.set("pageSize", String(pageSize));
    if (query) search.set("query", query);
    if (status) search.set("status", status);
    if (priority) search.set("priority", priority);
    if (projectIdParam) search.set("projectId", projectIdParam);
    return `/issues?${search.toString()}`;
  };

  return (
    <div className="page">
      <PageHeader title="Issues" subtitle="Track work across projects" />
      <InsightsContextLink
        href="/analytics#work-health"
        label="View issue flow insights"
        description="Status and priority distribution across active work."
      />
      <section className="section">
        <h3 className="section-title">Create Issue</h3>
        <IssueCreateForm />
      </section>
      <section className="section">
        <h3 className="section-title">All Issues</h3>
        <form className="list-filters" method="get">
          <input type="hidden" name="page" value="1" />
          <div className="filter-field">
            <label htmlFor="issues-query">Search</label>
            <input id="issues-query" name="query" placeholder="Search title or key" defaultValue={query} />
          </div>
          <div className="filter-field">
            <label htmlFor="issues-project">Project ID</label>
            <input
              id="issues-project"
              name="projectId"
              type="number"
              min="1"
              placeholder="e.g. 1"
              defaultValue={projectIdParam}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="issues-status">Status</label>
            <select id="issues-status" name="status" defaultValue={status}>
              <option value="">All</option>
              {STATUS_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="issues-priority">Priority</label>
            <select id="issues-priority" name="priority" defaultValue={priority}>
              <option value="">All</option>
              {PRIORITY_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="issues-page-size">Page size</label>
            <select id="issues-page-size" name="pageSize" defaultValue={String(pageSize)}>
              <option value="10">10</option>
              <option value="25">25</option>
              <option value="50">50</option>
            </select>
          </div>
          <div className="filter-actions">
            <button className="button-secondary" type="submit">
              Apply filters
            </button>
          </div>
        </form>
        {issues.items.length === 0 ? (
          <EmptyState
            title="No issues found"
            message="Try adjusting your filters or search to find relevant issues."
            size="compact"
          />
        ) : (
          <SimpleTable columns={columns} rows={issues.items} />
        )}
        <PaginationControls page={issues.page} totalPages={issues.totalPages} getHref={buildHref} />
      </section>
    </div>
  );
}
