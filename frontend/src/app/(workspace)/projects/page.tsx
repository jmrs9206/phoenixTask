import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import EmptyState from "../../../components/ui/EmptyState";
import PaginationControls from "../../../components/ui/PaginationControls";
import { getProjects } from "../../../lib/api/workspace.server";
import CreateProjectForm from "./projectCreateForm";
import type { Project } from "../../../types/domain/workspace";
import InsightsContextLink from "../../../components/insights/InsightsContextLink";

type SearchParams = Record<string, string | string[] | undefined>;

const STATUS_OPTIONS = ["ACTIVE", "ARCHIVED"];

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

export default async function ProjectsPage({
  searchParams
}: {
  searchParams?: SearchParams | Promise<SearchParams>;
}) {
  const params = (await Promise.resolve(searchParams)) ?? {};
  const page = parseNumber(params.page, 1);
  const pageSize = Math.min(parseNumber(params.pageSize, 25), 100);
  const query = parseString(params.query);
  const status = parseString(params.status);

  const projects = await getProjects({
    page,
    pageSize,
    query,
    status: status || undefined
  });

  const buildHref = (nextPage: number) => {
    const search = new URLSearchParams();
    search.set("page", String(nextPage));
    search.set("pageSize", String(pageSize));
    if (query) search.set("query", query);
    if (status) search.set("status", status);
    return `/projects?${search.toString()}`;
  };

  const columns = [
    {
      key: "projectKey",
      header: "Key",
      render: (project: Project) => project.projectKey,
      width: "120px"
    },
    {
      key: "name",
      header: "Name",
      render: (project: Project) => project.name
    },
    {
      key: "status",
      header: "Status",
      render: (project: Project) => project.status,
      width: "120px"
    }
  ];

  return (
    <div className="page">
      <PageHeader title="Projects" subtitle="Current projects for this workspace." />
      <InsightsContextLink
        href="/analytics#project-distribution"
        label="View project workload insights"
        description="Project-level issue distribution and sprint balance."
      />
      <section className="section">
        <h3 className="section-title">Create Project</h3>
        <CreateProjectForm />
      </section>
      <section className="section">
        <h3 className="section-title">All Projects</h3>
        <form className="list-filters" method="get">
          <input type="hidden" name="page" value="1" />
          <div className="filter-field">
            <label htmlFor="projects-query">Search</label>
            <input id="projects-query" name="query" placeholder="Search project name or key" defaultValue={query} />
          </div>
          <div className="filter-field">
            <label htmlFor="projects-status">Status</label>
            <select id="projects-status" name="status" defaultValue={status}>
              <option value="">All</option>
              {STATUS_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="projects-page-size">Page size</label>
            <select id="projects-page-size" name="pageSize" defaultValue={String(pageSize)}>
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
        {projects.items.length === 0 ? (
          <EmptyState
            title="No projects found"
            message="Try adjusting your filters or search to find active projects."
            size="compact"
          />
        ) : (
          <SimpleTable columns={columns} rows={projects.items} />
        )}
        <PaginationControls page={projects.page} totalPages={projects.totalPages} getHref={buildHref} />
      </section>
    </div>
  );
}
