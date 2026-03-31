import type {
  CompanyWithSettings,
  IssueActivity,
  Issue,
  IssueAttachment,
  IssueComment,
  IssueCodeLink,
  IssueDetail,
  IssueSummary,
  KanbanBoard,
  KanbanProjectSummary,
  GanttProject,
  GanttProjectDetail,
  GanttDependency,
  GanttBaseline,
  MessageItem,
  MessageThreadDirect,
  MessageThreadProject,
  MessageThreadTeam,
  Member,
  AnalyticsSummary,
  AnalyticsAdvanced,
  OkrCheckin,
  OkrInitiative,
  OkrKeyResult,
  OkrObjective,
  OkrObjectiveDetail,
  Project,
  RoleOption,
  Sprint,
  SprintHealth,
  Team,
  User
} from "../domain/workspace";

export type ApiCompanyResponse = CompanyWithSettings;
export type ApiUsersResponse = User[];
export type ApiPaginated<T> = {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
};
export type ApiTeamsResponse = ApiPaginated<Team>;
export type ApiProjectsResponse = ApiPaginated<Project>;
export type ApiRoleOptionsResponse = RoleOption[];
export type ApiMembersResponse = Member[];
export type ApiIssuesResponse = ApiPaginated<Issue>;
export type ApiIssueDetailResponse = IssueDetail;
export type ApiIssueSummaryResponse = IssueSummary[];
export type ApiIssueCommentsResponse = IssueComment[];
export type ApiIssueCommentResponse = IssueComment;
export type ApiIssueAttachmentsResponse = IssueAttachment[];
export type ApiIssueAttachmentResponse = IssueAttachment;
export type ApiIssueActivityResponse = IssueActivity[];
export type ApiIssueCodeLinksResponse = IssueCodeLink[];
export type ApiSprintsResponse = Sprint[];
export type ApiSprintHealthResponse = SprintHealth;
export type ApiKanbanProjectsResponse = KanbanProjectSummary[];
export type ApiKanbanBoardResponse = KanbanBoard;
export type ApiOkrObjectivesResponse = OkrObjective[];
export type ApiOkrObjectiveDetailResponse = OkrObjectiveDetail;
export type ApiOkrObjectiveDetailsResponse = OkrObjectiveDetail[];
export type ApiOkrKeyResultResponse = OkrKeyResult;
export type ApiOkrCheckinResponse = OkrCheckin;
export type ApiOkrInitiativeResponse = OkrInitiative;
export type ApiGanttProjectsResponse = GanttProject[];
export type ApiGanttProjectDetailResponse = GanttProjectDetail;
export type ApiGanttDependencyResponse = GanttDependency;
export type ApiGanttBaselineResponse = GanttBaseline;
export type ApiAnalyticsSummaryResponse = AnalyticsSummary;
export type ApiAnalyticsAdvancedResponse = AnalyticsAdvanced;
export type ApiTeamThreadsResponse = MessageThreadTeam[];
export type ApiProjectThreadsResponse = MessageThreadProject[];
export type ApiDirectThreadsResponse = MessageThreadDirect[];
export type ApiThreadMessagesResponse = MessageItem[];
export type ApiWorkspacePermissionsResponse = {
  roleId: number;
  permissions: string[];
};
