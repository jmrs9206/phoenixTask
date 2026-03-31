import { notFound } from "next/navigation";
import { getTeamMembers, getTeams, getAssignableRoles, getUsers } from "../../../../lib/api/workspace.server";
import { ApiError } from "../../../../lib/api/client";
import type { Member } from "../../../../types/domain/workspace";
import PageHeader from "../../../../components/ui/PageHeader";
import SimpleTable from "../../../../components/ui/SimpleTable";
import KeyValueList from "../../../../components/ui/KeyValueList";
import AddTeamMemberForm from "./teamMemberForm";

type TeamPageProps = {
  params: Promise<{ teamId: string }>;
};

const memberColumns = [
  { key: "fullName", header: "Member", render: (row: Member) => row.fullName },
  { key: "email", header: "Email", render: (row: Member) => row.email },
  { key: "roleName", header: "Role", render: (row: Member) => row.roleName },
  { key: "status", header: "Status", render: (row: Member) => row.status }
];

async function safeFetch<T>(promise: Promise<T>, fallback: T): Promise<{ data: T; restricted: boolean }> {
  try {
    return { data: await promise, restricted: false };
  } catch (error) {
    if (error instanceof ApiError && error.status === 403) {
      return { data: fallback, restricted: true };
    }
    throw error;
  }
}

export default async function TeamDetailPage({ params }: TeamPageProps) {
  const { teamId } = await params;
  const teamIdNumber = Number(teamId);
  if (Number.isNaN(teamIdNumber)) {
    notFound();
  }

  const [teams, membersResult, rolesResult, usersResult] = await Promise.all([
    getTeams({ pageSize: 100 }),
    safeFetch(getTeamMembers(teamIdNumber), [] as Member[]),
    safeFetch(getAssignableRoles(), []),
    safeFetch(getUsers(), [])
  ]);

  const team = teams.items.find((item) => item.id === teamIdNumber);
  if (!team) {
    notFound();
  }

  const members = membersResult.data;
  const roles = rolesResult.data;
  const users = usersResult.data;
  const memberAccessRestricted = membersResult.restricted;
  const memberFormRestricted = rolesResult.restricted || usersResult.restricted;
  const memberRestrictionMessage = memberFormRestricted
    ? "Member management requires access to users and roles."
    : undefined;
  const membersEmptyTitle = memberAccessRestricted ? "Members restricted" : "No members found";
  const membersEmptyMessage = memberAccessRestricted
    ? "You do not have permission to view team members."
    : "No members are assigned to this team yet.";

  return (
    <div className="page">
      <PageHeader
        title={team.name}
        subtitle={team.description ?? "Team details and membership"}
      />

      <section className="section">
        <h3 className="section-title">Team Details</h3>
        <KeyValueList
          items={[
            { label: "Name", value: team.name },
            { label: "Description", value: team.description ?? "—" },
            { label: "Total Members", value: String(members.length) }
          ]}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Members</h3>
        <SimpleTable
          columns={memberColumns}
          rows={members}
          emptyTitle={membersEmptyTitle}
          emptyMessage={membersEmptyMessage}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Add Member</h3>
        <AddTeamMemberForm
          teamId={teamIdNumber}
          roles={roles}
          users={users}
          accessMessage={memberRestrictionMessage}
        />
      </section>
    </div>
  );
}
