import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import EmptyState from "../../../components/ui/EmptyState";
import { getUsers } from "../../../lib/api/workspace.server";
import type { User } from "../../../types/domain/workspace";

const baseRoleLabels: Record<number, string> = {
  1: "Owner",
  2: "Team Leader",
  3: "Developer",
  4: "Support",
  5: "Quality Assurance"
};

type SearchParams = Record<string, string | string[] | undefined>;

function parseString(value: string | string[] | undefined) {
  if (Array.isArray(value)) {
    return value[0] ?? "";
  }
  return value ?? "";
}

export default async function UsersPage({
  searchParams
}: {
  searchParams?: SearchParams | Promise<SearchParams>;
}) {
  const params = (await Promise.resolve(searchParams)) ?? {};
  const query = parseString(params.query);
  const users = await getUsers();
  const normalizedQuery = query.trim().toLowerCase();
  const filteredUsers = normalizedQuery
    ? users.filter((user) => {
        const name = `${user.firstName} ${user.lastName}`.toLowerCase();
        return name.includes(normalizedQuery) || user.email.toLowerCase().includes(normalizedQuery);
      })
    : users;

  const columns = [
    {
      key: "name",
      header: "Name",
      render: (user: User) => `${user.firstName} ${user.lastName}`
    },
    {
      key: "email",
      header: "Email",
      render: (user: User) => user.email
    },
    {
      key: "role",
      header: "Base role",
      render: (user: User) => baseRoleLabels[user.primaryRoleId] ?? `Role ${user.primaryRoleId}`
    },
    {
      key: "status",
      header: "Status",
      render: (user: User) => user.status
    }
  ];

  return (
    <div className="page">
      <PageHeader title="Users" subtitle="People with access to this workspace." />
      <section className="section">
        <h3 className="section-title">All Users</h3>
        <form className="list-filters" method="get">
          <input type="hidden" name="page" value="1" />
          <div className="filter-field">
            <label htmlFor="users-query">Search</label>
            <input id="users-query" name="query" placeholder="Search by name or email" defaultValue={query} />
          </div>
          <div className="filter-actions">
            <button className="button-secondary" type="submit">
              Apply search
            </button>
          </div>
        </form>
        {filteredUsers.length === 0 ? (
          <EmptyState
            title="No users found"
            message="Try adjusting your search to find a workspace member."
            size="compact"
          />
        ) : (
          <SimpleTable columns={columns} rows={filteredUsers} />
        )}
      </section>
    </div>
  );
}
