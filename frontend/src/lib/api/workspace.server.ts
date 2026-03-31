import { cookies } from "next/headers";
import { apiFetch } from "./client";
import type {
  ApiCompanyResponse,
  ApiIssueActivityResponse,
  ApiIssueAttachmentsResponse,
  ApiIssueCommentsResponse,
  ApiIssueCodeLinksResponse,
  ApiIssueDetailResponse,
  ApiIssueSummaryResponse,
  ApiIssuesResponse,
  ApiKanbanBoardResponse,
  ApiKanbanProjectsResponse,
  ApiAnalyticsSummaryResponse,
  ApiAnalyticsAdvancedResponse,
  ApiGanttProjectDetailResponse,
  ApiGanttProjectsResponse,
  ApiOkrObjectiveDetailResponse,
  ApiOkrObjectiveDetailsResponse,
  ApiOkrObjectivesResponse,
  ApiSprintsResponse,
  ApiSprintHealthResponse,
  ApiProjectThreadsResponse,
  ApiMembersResponse,
  ApiTeamThreadsResponse,
  ApiDirectThreadsResponse,
  ApiThreadMessagesResponse,
  ApiProjectsResponse,
  ApiRoleOptionsResponse,
  ApiTeamsResponse,
  ApiUsersResponse,
  ApiWorkspacePermissionsResponse
} from "../../types/api/workspace";

async function withAuthCookie(headers: Record<string, string>) {
  const cookieStore = await cookies();
  const authCookie = cookieStore.get("phoenixtask_auth")?.value;
  if (!authCookie) {
    return headers;
  }
  return {
    ...headers,
    Cookie: `phoenixtask_auth=${authCookie}`
  };
}

async function apiFetchServer<T>(path: string, extraHeaders: Record<string, string> = {}) {
  const headers = await withAuthCookie(extraHeaders);
  return apiFetch<T>(path, headers);
}

function buildQuery(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") {
      return;
    }
    search.set(key, String(value));
  });
  const queryString = search.toString();
  return queryString ? `?${queryString}` : "";
}

type ListParams = {
  page?: number;
  pageSize?: number;
  query?: string;
};

type IssueListParams = ListParams & {
  projectId?: number;
  status?: string;
  priority?: string;
  assigneeId?: number;
};

export function getCompany(): Promise<ApiCompanyResponse> {
  return apiFetchServer<ApiCompanyResponse>("/api/workspace/company");
}

export function getUsers(): Promise<ApiUsersResponse> {
  return apiFetchServer<ApiUsersResponse>("/api/workspace/users");
}

export function getTeams(params: ListParams = {}): Promise<ApiTeamsResponse> {
  const query = buildQuery(params);
  return apiFetchServer<ApiTeamsResponse>(`/api/workspace/teams${query}`);
}

export function getProjects(params: ListParams & { status?: string } = {}): Promise<ApiProjectsResponse> {
  const query = buildQuery(params);
  return apiFetchServer<ApiProjectsResponse>(`/api/workspace/projects${query}`);
}

export function getAssignableRoles(): Promise<ApiRoleOptionsResponse> {
  return apiFetchServer<ApiRoleOptionsResponse>("/api/workspace/users/roles");
}

export function getTeamMembers(teamId: number): Promise<ApiMembersResponse> {
  return apiFetchServer<ApiMembersResponse>(`/api/workspace/teams/${teamId}/members`);
}

export function getProjectMembers(projectId: number): Promise<ApiMembersResponse> {
  return apiFetchServer<ApiMembersResponse>(`/api/workspace/projects/${projectId}/members`);
}

export function getProjectIssues(projectId: number): Promise<ApiIssueSummaryResponse> {
  return apiFetchServer<ApiIssueSummaryResponse>(`/api/workspace/projects/${projectId}/issues`);
}

export function getIssues(params: IssueListParams = {}): Promise<ApiIssuesResponse> {
  const query = buildQuery(params);
  return apiFetchServer<ApiIssuesResponse>(`/api/workspace/issues${query}`);
}

export function getIssueDetail(issueId: number): Promise<ApiIssueDetailResponse> {
  return apiFetchServer<ApiIssueDetailResponse>(`/api/workspace/issues/${issueId}`);
}

export function getIssueComments(issueId: number): Promise<ApiIssueCommentsResponse> {
  return apiFetchServer<ApiIssueCommentsResponse>(`/api/workspace/issues/${issueId}/comments`);
}

export function getIssueAttachments(issueId: number): Promise<ApiIssueAttachmentsResponse> {
  return apiFetchServer<ApiIssueAttachmentsResponse>(`/api/workspace/issues/${issueId}/attachments`);
}

export function getIssueActivity(issueId: number): Promise<ApiIssueActivityResponse> {
  return apiFetchServer<ApiIssueActivityResponse>(`/api/workspace/issues/${issueId}/activity`);
}

export function getIssueCodeLinks(issueId: number): Promise<ApiIssueCodeLinksResponse> {
  return apiFetchServer<ApiIssueCodeLinksResponse>(`/api/workspace/issues/${issueId}/code-links`);
}

export function getSprints(projectId: number): Promise<ApiSprintsResponse> {
  return apiFetchServer<ApiSprintsResponse>(`/api/workspace/scrum/projects/${projectId}/sprints`);
}

export function getSprintHealth(sprintId: number): Promise<ApiSprintHealthResponse> {
  return apiFetchServer<ApiSprintHealthResponse>(`/api/workspace/scrum/sprints/${sprintId}/health`);
}

export function getBacklog(projectId: number): Promise<ApiIssueSummaryResponse> {
  return apiFetchServer<ApiIssueSummaryResponse>(`/api/workspace/scrum/projects/${projectId}/backlog`);
}

export function getSprintIssues(sprintId: number): Promise<ApiIssueSummaryResponse> {
  return apiFetchServer<ApiIssueSummaryResponse>(`/api/workspace/scrum/sprints/${sprintId}/issues`);
}

export function getKanbanProjects(): Promise<ApiKanbanProjectsResponse> {
  return apiFetchServer<ApiKanbanProjectsResponse>("/api/workspace/kanban/projects");
}

export function getKanbanBoard(projectId: number): Promise<ApiKanbanBoardResponse> {
  return apiFetchServer<ApiKanbanBoardResponse>(`/api/workspace/kanban/projects/${projectId}`);
}

export function getOkrObjectives(projectId: number): Promise<ApiOkrObjectivesResponse> {
  return apiFetchServer<ApiOkrObjectivesResponse>(`/api/workspace/okr/projects/${projectId}/objectives`);
}

export function getOkrObjectiveDetails(projectId: number): Promise<ApiOkrObjectiveDetailsResponse> {
  return apiFetchServer<ApiOkrObjectiveDetailsResponse>(`/api/workspace/okr/projects/${projectId}/objectives/details`);
}

export function getOkrObjectiveDetail(objectiveId: number): Promise<ApiOkrObjectiveDetailResponse> {
  return apiFetchServer<ApiOkrObjectiveDetailResponse>(`/api/workspace/okr/objectives/${objectiveId}`);
}

export function getGanttProjects(): Promise<ApiGanttProjectsResponse> {
  return apiFetchServer<ApiGanttProjectsResponse>("/api/workspace/gantt/projects");
}

export function getGanttProject(projectId: number): Promise<ApiGanttProjectDetailResponse> {
  return apiFetchServer<ApiGanttProjectDetailResponse>(`/api/workspace/gantt/projects/${projectId}`);
}

export function getAnalyticsSummary(): Promise<ApiAnalyticsSummaryResponse> {
  return apiFetchServer<ApiAnalyticsSummaryResponse>("/api/workspace/analytics/summary");
}

export function getAnalyticsAdvanced(): Promise<ApiAnalyticsAdvancedResponse> {
  return apiFetchServer<ApiAnalyticsAdvancedResponse>("/api/workspace/analytics/advanced");
}

export function getMyPermissions(): Promise<ApiWorkspacePermissionsResponse> {
  return apiFetchServer<ApiWorkspacePermissionsResponse>("/api/workspace/permissions/me");
}

export function getTeamThreads(userId: number): Promise<ApiTeamThreadsResponse> {
  return apiFetchServer<ApiTeamThreadsResponse>("/api/workspace/messages/teams", {
    "X-User-Id": String(userId)
  });
}

export function getProjectThreads(userId: number): Promise<ApiProjectThreadsResponse> {
  return apiFetchServer<ApiProjectThreadsResponse>("/api/workspace/messages/projects", {
    "X-User-Id": String(userId)
  });
}

export function getDirectThreads(userId: number): Promise<ApiDirectThreadsResponse> {
  return apiFetchServer<ApiDirectThreadsResponse>("/api/workspace/messages/direct", {
    "X-User-Id": String(userId)
  });
}

export function getThreadMessages(threadId: number, userId: number): Promise<ApiThreadMessagesResponse> {
  return apiFetchServer<ApiThreadMessagesResponse>(`/api/workspace/messages/threads/${threadId}/messages`, {
    "X-User-Id": String(userId)
  });
}
