import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import EmptyState from "../../../components/ui/EmptyState";
import PaginationControls from "../../../components/ui/PaginationControls";
import { getTeams } from "../../../lib/api/workspace.server";
import CreateTeamForm from "./teamCreateForm";
import type { Team } from "../../../types/domain/workspace";

type SearchParams = Record<string, string | string[] | undefined>;

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

export default async function TeamsPage({
  searchParams
}: {
  searchParams?: SearchParams | Promise<SearchParams>;
}) {
  const params = (await Promise.resolve(searchParams)) ?? {};
  const page = parseNumber(params.page, 1);
  const pageSize = Math.min(parseNumber(params.pageSize, 25), 100);
  const query = parseString(params.query);

  const teams = await getTeams({ page, pageSize, query });

  const buildHref = (nextPage: number) => {
    const search = new URLSearchParams();
    search.set("page", String(nextPage));
    search.set("pageSize", String(pageSize));
    if (query) search.set("query", query);
    return `/teams?${search.toString()}`;
  };

  const columns = [
    {
      key: "name",
      header: "Name",
      render: (team: Team) => team.name
    },
    {
      key: "description",
      header: "Description",
      render: (team: Team) => team.description ?? "—"
    }
  ];

  return (
    <div className="page">
      <PageHeader title="Teams" subtitle="Active teams in this workspace." />
      <section className="section">
        <h3 className="section-title">Create Team</h3>
        <CreateTeamForm />
      </section>
      <section className="section">
        <h3 className="section-title">All Teams</h3>
        <form className="list-filters" method="get">
          <input type="hidden" name="page" value="1" />
          <div className="filter-field">
            <label htmlFor="teams-query">Search</label>
            <input id="teams-query" name="query" placeholder="Search team name" defaultValue={query} />
          </div>
          <div className="filter-field">
            <label htmlFor="teams-page-size">Page size</label>
            <select id="teams-page-size" name="pageSize" defaultValue={String(pageSize)}>
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
        {teams.items.length === 0 ? (
          <EmptyState
            title="No teams found"
            message="Try adjusting your search or create a new team."
            size="compact"
          />
        ) : (
          <SimpleTable columns={columns} rows={teams.items} />
        )}
        <PaginationControls page={teams.page} totalPages={teams.totalPages} getHref={buildHref} />
      </section>
    </div>
  );
}
