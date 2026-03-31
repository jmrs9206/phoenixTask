import { notFound } from "next/navigation";
import {
  getAssignableRoles,
  getProjectMembers,
  getProjects,
  getProjectIssues,
  getUsers
} from "../../../../lib/api/workspace.server";
import { ApiError } from "../../../../lib/api/client";
import type { Member, IssueSummary } from "../../../../types/domain/workspace";
import PageHeader from "../../../../components/ui/PageHeader";
import SimpleTable from "../../../../components/ui/SimpleTable";
import KeyValueList from "../../../../components/ui/KeyValueList";
import AddProjectMemberForm from "./projectMemberForm";
import Link from "next/link";

type ProjectPageProps = {
  params: Promise<{ projectId: string }>;
};

const memberColumns = [
  { key: "fullName", header: "Member", render: (row: Member) => row.fullName },
  { key: "email", header: "Email", render: (row: Member) => row.email },
  { key: "roleName", header: "Role", render: (row: Member) => row.roleName },
  { key: "status", header: "Status", render: (row: Member) => row.status }
];

const issueColumns = [
  {
    key: "issueKey",
    header: "Issue",
    render: (row: IssueSummary) => (
      <Link className="issue-link" href={`/issues/${row.id}`}>
        {row.issueKey}
      </Link>
    )
  },
  { key: "title", header: "Title", render: (row: IssueSummary) => row.title },
  { key: "status", header: "Status", render: (row: IssueSummary) => row.status },
  { key: "priority", header: "Priority", render: (row: IssueSummary) => row.priority },
  { key: "assigneeName", header: "Assignee", render: (row: IssueSummary) => row.assigneeName }
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

export default async function ProjectDetailPage({ params }: ProjectPageProps) {
  const { projectId } = await params;
  const projectIdNumber = Number(projectId);
  if (Number.isNaN(projectIdNumber)) {
    notFound();
  }

  const [projects, issuesResult, membersResult, rolesResult, usersResult] = await Promise.all([
    getProjects({ pageSize: 100 }),
    safeFetch(getProjectIssues(projectIdNumber), [] as IssueSummary[]),
    safeFetch(getProjectMembers(projectIdNumber), [] as Member[]),
    safeFetch(getAssignableRoles(), []),
    safeFetch(getUsers(), [])
  ]);

  const project = projects.items.find((item) => item.id === projectIdNumber);
  if (!project) {
    notFound();
  }

  const members = membersResult.data;
  const roles = rolesResult.data;
  const users = usersResult.data;
  const issues = issuesResult.data;
  const memberAccessRestricted = membersResult.restricted;
  const memberFormRestricted = rolesResult.restricted || usersResult.restricted;
  const memberRestrictionMessage = memberFormRestricted
    ? "Member management requires access to users and roles."
    : undefined;
  const membersEmptyTitle = memberAccessRestricted ? "Members restricted" : "No members found";
  const membersEmptyMessage = memberAccessRestricted
    ? "You do not have permission to view project members."
    : "No members are assigned to this project yet.";
  const issueAccessRestricted = issuesResult.restricted;
  const issuesEmptyTitle = issueAccessRestricted ? "Issues restricted" : "No issues found";
  const issuesEmptyMessage = issueAccessRestricted
    ? "You do not have permission to view project issues."
    : "No issues are linked to this project yet.";

  return (
    <div className="page">
      <PageHeader
        title={project.name}
        subtitle={`${project.projectKey} · ${project.status}`}
      />

      <section className="section">
        <h3 className="section-title">Project Details</h3>
        <KeyValueList
          items={[
            { label: "Project Key", value: project.projectKey },
            { label: "Name", value: project.name },
            { label: "Status", value: project.status },
            { label: "Description", value: project.description ?? "—" }
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
        <AddProjectMemberForm
          projectId={projectIdNumber}
          roles={roles}
          users={users}
          accessMessage={memberRestrictionMessage}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Issues</h3>
        <SimpleTable
          columns={issueColumns}
          rows={issues}
          emptyTitle={issuesEmptyTitle}
          emptyMessage={issuesEmptyMessage}
        />
      </section>
    </div>
  );
}
