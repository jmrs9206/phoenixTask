import { apiDelete, apiFetch, apiFetchBlob, apiPost, apiUpload } from "./client";
import type {
  ApiCompanyResponse,
  ApiIssueAttachmentResponse,
  ApiIssueAttachmentsResponse,
  ApiIssueActivityResponse,
  ApiIssueCommentResponse,
  ApiIssueCommentsResponse,
  ApiIssueDetailResponse,
  ApiIssueSummaryResponse,
  ApiIssuesResponse,
  ApiKanbanBoardResponse,
  ApiKanbanProjectsResponse,
  ApiAnalyticsSummaryResponse,
  ApiGanttProjectDetailResponse,
  ApiGanttProjectsResponse,
  ApiGanttBaselineResponse,
  ApiGanttDependencyResponse,
  ApiOkrCheckinResponse,
  ApiOkrInitiativeResponse,
  ApiOkrObjectiveDetailResponse,
  ApiOkrObjectivesResponse,
  ApiOkrKeyResultResponse,
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
import type {
  DirectThreadResponse,
  IssueCreatePayload,
  MemberAddPayload,
  OkrCheckinCreatePayload,
  OkrInitiativeCreatePayload,
  OkrKeyResultCreatePayload,
  OkrObjectiveClosePayload,
  OkrObjectiveCreatePayload,
  ProjectCreatePayload,
  SprintCreatePayload,
  SprintUpdatePayload,
  SprintIssueAssignPayload,
  TeamCreatePayload
} from "../../types/domain/workspace";

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
  return apiFetch<ApiCompanyResponse>("/api/workspace/company");
}

export function getUsers(): Promise<ApiUsersResponse> {
  return apiFetch<ApiUsersResponse>("/api/workspace/users");
}

export function getTeams(params: ListParams = {}): Promise<ApiTeamsResponse> {
  const query = buildQuery(params);
  return apiFetch<ApiTeamsResponse>(`/api/workspace/teams${query}`);
}

export function getProjects(params: ListParams & { status?: string } = {}): Promise<ApiProjectsResponse> {
  const query = buildQuery(params);
  return apiFetch<ApiProjectsResponse>(`/api/workspace/projects${query}`);
}

export function getAssignableRoles(): Promise<ApiRoleOptionsResponse> {
  return apiFetch<ApiRoleOptionsResponse>("/api/workspace/users/roles");
}

export function createTeam(payload: TeamCreatePayload): Promise<{ id: number }> {
  return apiPost<{ id: number }>("/api/workspace/teams", payload);
}

export function getTeamMembers(teamId: number): Promise<ApiMembersResponse> {
  return apiFetch<ApiMembersResponse>(`/api/workspace/teams/${teamId}/members`);
}

export function addTeamMember(teamId: number, payload: MemberAddPayload): Promise<void> {
  return apiPost<void>(`/api/workspace/teams/${teamId}/members`, payload);
}

export function createProject(payload: ProjectCreatePayload): Promise<{ id: number }> {
  return apiPost<{ id: number }>("/api/workspace/projects", payload);
}

export function getProjectMembers(projectId: number): Promise<ApiMembersResponse> {
  return apiFetch<ApiMembersResponse>(`/api/workspace/projects/${projectId}/members`);
}

export function addProjectMember(projectId: number, payload: MemberAddPayload): Promise<void> {
  return apiPost<void>(`/api/workspace/projects/${projectId}/members`, payload);
}

export function getProjectIssues(projectId: number): Promise<ApiIssueSummaryResponse> {
  return apiFetch<ApiIssueSummaryResponse>(`/api/workspace/projects/${projectId}/issues`);
}

export function getIssues(params: IssueListParams = {}): Promise<ApiIssuesResponse> {
  const query = buildQuery(params);
  return apiFetch<ApiIssuesResponse>(`/api/workspace/issues${query}`);
}

export function createIssue(payload: IssueCreatePayload): Promise<{ id: number; issueKey: string }> {
  return apiPost<{ id: number; issueKey: string }>("/api/workspace/issues", payload);
}

export function getIssueDetail(issueId: number): Promise<ApiIssueDetailResponse> {
  return apiFetch<ApiIssueDetailResponse>(`/api/workspace/issues/${issueId}`);
}

export function getIssueComments(issueId: number): Promise<ApiIssueCommentsResponse> {
  return apiFetch<ApiIssueCommentsResponse>(`/api/workspace/issues/${issueId}/comments`);
}

export function createIssueComment(issueId: number, body: string): Promise<ApiIssueCommentResponse> {
  return apiPost<ApiIssueCommentResponse>(`/api/workspace/issues/${issueId}/comments`, { body });
}

export function deleteIssueComment(issueId: number, commentId: number): Promise<void> {
  return apiDelete<void>(`/api/workspace/issues/${issueId}/comments/${commentId}`);
}

export function getIssueAttachments(issueId: number): Promise<ApiIssueAttachmentsResponse> {
  return apiFetch<ApiIssueAttachmentsResponse>(`/api/workspace/issues/${issueId}/attachments`);
}

export function getIssueActivity(issueId: number): Promise<ApiIssueActivityResponse> {
  return apiFetch<ApiIssueActivityResponse>(`/api/workspace/issues/${issueId}/activity`);
}

export function uploadIssueAttachment(issueId: number, file: File): Promise<ApiIssueAttachmentResponse> {
  const formData = new FormData();
  formData.append("file", file);
  return apiUpload<ApiIssueAttachmentResponse>(`/api/workspace/issues/${issueId}/attachments`, formData);
}

export function deleteIssueAttachment(issueId: number, attachmentId: number): Promise<void> {
  return apiDelete<void>(`/api/workspace/issues/${issueId}/attachments/${attachmentId}`);
}

export function fetchIssueAttachmentPreview(issueId: number, attachmentId: number): Promise<Blob> {
  return apiFetchBlob(`/api/workspace/issues/${issueId}/attachments/${attachmentId}/preview`);
}

export function fetchIssueAttachmentDownload(issueId: number, attachmentId: number): Promise<Blob> {
  return apiFetchBlob(`/api/workspace/issues/${issueId}/attachments/${attachmentId}/download`);
}

export function getSprints(projectId: number): Promise<ApiSprintsResponse> {
  return apiFetch<ApiSprintsResponse>(`/api/workspace/scrum/projects/${projectId}/sprints`);
}

export function updateSprint(sprintId: number, payload: SprintUpdatePayload): Promise<ApiSprintsResponse[number]> {
  return apiPost<ApiSprintsResponse[number]>(`/api/workspace/scrum/sprints/${sprintId}`, payload);
}

export function getSprintHealth(sprintId: number): Promise<ApiSprintHealthResponse> {
  return apiFetch<ApiSprintHealthResponse>(`/api/workspace/scrum/sprints/${sprintId}/health`);
}

export function createSprint(projectId: number, payload: SprintCreatePayload): Promise<ApiSprintsResponse[number]> {
  return apiPost<ApiSprintsResponse[number]>(`/api/workspace/scrum/projects/${projectId}/sprints`, payload);
}

export function getBacklog(projectId: number): Promise<ApiIssueSummaryResponse> {
  return apiFetch<ApiIssueSummaryResponse>(`/api/workspace/scrum/projects/${projectId}/backlog`);
}

export function getSprintIssues(sprintId: number): Promise<ApiIssueSummaryResponse> {
  return apiFetch<ApiIssueSummaryResponse>(`/api/workspace/scrum/sprints/${sprintId}/issues`);
}

export function assignIssueToSprint(sprintId: number, payload: SprintIssueAssignPayload): Promise<void> {
  return apiPost<void>(`/api/workspace/scrum/sprints/${sprintId}/issues`, payload);
}

export function moveIssueToBacklog(payload: SprintIssueAssignPayload): Promise<void> {
  return apiPost<void>("/api/workspace/scrum/sprints/backlog/issues", payload);
}

export function getKanbanProjects(): Promise<ApiKanbanProjectsResponse> {
  return apiFetch<ApiKanbanProjectsResponse>("/api/workspace/kanban/projects");
}

export function getKanbanBoard(projectId: number): Promise<ApiKanbanBoardResponse> {
  return apiFetch<ApiKanbanBoardResponse>(`/api/workspace/kanban/projects/${projectId}`);
}

export function getOkrObjectives(projectId: number): Promise<ApiOkrObjectivesResponse> {
  return apiFetch<ApiOkrObjectivesResponse>(`/api/workspace/okr/projects/${projectId}/objectives`);
}

export function createOkrObjective(
  projectId: number,
  payload: OkrObjectiveCreatePayload
): Promise<ApiOkrObjectivesResponse[number]> {
  return apiPost<ApiOkrObjectivesResponse[number]>(`/api/workspace/okr/projects/${projectId}/objectives`, payload);
}

export function getOkrObjectiveDetail(objectiveId: number): Promise<ApiOkrObjectiveDetailResponse> {
  return apiFetch<ApiOkrObjectiveDetailResponse>(`/api/workspace/okr/objectives/${objectiveId}`);
}

export function createOkrKeyResult(objectiveId: number, payload: OkrKeyResultCreatePayload): Promise<ApiOkrKeyResultResponse> {
  return apiPost<ApiOkrKeyResultResponse>(`/api/workspace/okr/objectives/${objectiveId}/key-results`, payload);
}

export function createOkrCheckin(
  objectiveId: number,
  payload: OkrCheckinCreatePayload
): Promise<ApiOkrCheckinResponse> {
  return apiPost<ApiOkrCheckinResponse>(`/api/workspace/okr/objectives/${objectiveId}/check-ins`, payload);
}

export function closeOkrObjective(
  objectiveId: number,
  payload: OkrObjectiveClosePayload
): Promise<ApiOkrObjectivesResponse[number]> {
  return apiPost<ApiOkrObjectivesResponse[number]>(`/api/workspace/okr/objectives/${objectiveId}/close`, payload);
}

export function linkOkrInitiative(
  objectiveId: number,
  payload: OkrInitiativeCreatePayload
): Promise<ApiOkrInitiativeResponse> {
  return apiPost<ApiOkrInitiativeResponse>(`/api/workspace/okr/objectives/${objectiveId}/initiatives`, payload);
}

export function getGanttProjects(): Promise<ApiGanttProjectsResponse> {
  return apiFetch<ApiGanttProjectsResponse>("/api/workspace/gantt/projects");
}

export function getGanttProject(projectId: number): Promise<ApiGanttProjectDetailResponse> {
  return apiFetch<ApiGanttProjectDetailResponse>(`/api/workspace/gantt/projects/${projectId}`);
}

export function captureGanttBaseline(projectId: number): Promise<ApiGanttBaselineResponse> {
  return apiPost<ApiGanttBaselineResponse>(`/api/workspace/gantt/projects/${projectId}/baseline`, {});
}

export function createGanttDependency(
  projectId: number,
  payload: { predecessorIssueId: number; successorIssueId: number; dependencyType?: string }
): Promise<ApiGanttDependencyResponse> {
  return apiPost<ApiGanttDependencyResponse>(`/api/workspace/gantt/projects/${projectId}/dependencies`, payload);
}

export function getAnalyticsSummary(): Promise<ApiAnalyticsSummaryResponse> {
  return apiFetch<ApiAnalyticsSummaryResponse>("/api/workspace/analytics/summary");
}

export function getMyPermissions(): Promise<ApiWorkspacePermissionsResponse> {
  return apiFetch<ApiWorkspacePermissionsResponse>("/api/workspace/permissions/me");
}

export function getTeamThreads(userId: number): Promise<ApiTeamThreadsResponse> {
  return apiFetch<ApiTeamThreadsResponse>("/api/workspace/messages/teams", {
    "X-User-Id": String(userId)
  });
}

export function getProjectThreads(userId: number): Promise<ApiProjectThreadsResponse> {
  return apiFetch<ApiProjectThreadsResponse>("/api/workspace/messages/projects", {
    "X-User-Id": String(userId)
  });
}

export function getDirectThreads(userId: number): Promise<ApiDirectThreadsResponse> {
  return apiFetch<ApiDirectThreadsResponse>("/api/workspace/messages/direct", {
    "X-User-Id": String(userId)
  });
}

export function getThreadMessages(threadId: number, userId: number): Promise<ApiThreadMessagesResponse> {
  return apiFetch<ApiThreadMessagesResponse>(`/api/workspace/messages/threads/${threadId}/messages`, {
    "X-User-Id": String(userId)
  });
}

export function sendThreadMessage(threadId: number, userId: number, body: string): Promise<ApiThreadMessagesResponse[number]> {
  return apiPost<ApiThreadMessagesResponse[number]>(
    `/api/workspace/messages/threads/${threadId}/messages`,
    { body },
    { "X-User-Id": String(userId) }
  );
}

export function createDirectThread(userId: number, otherUserId: number): Promise<DirectThreadResponse> {
  return apiPost<DirectThreadResponse>(
    "/api/workspace/messages/direct",
    { userId: otherUserId },
    { "X-User-Id": String(userId) }
  );
}
