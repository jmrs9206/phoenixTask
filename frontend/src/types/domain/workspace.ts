export type Company = {
  id: number;
  code: string;
  name: string;
  status: string;
  ownerUserId: number;
  createdAt: string;
  updatedAt: string;
};

export type CompanySettings = {
  id: number;
  timezone: string;
  locale: string;
  weekStart: string;
  createdAt: string;
  updatedAt: string;
};

export type CompanyWithSettings = Company & {
  settings: CompanySettings;
};

export type User = {
  id: number;
  companyId: number;
  primaryRoleId: number;
  firstName: string;
  lastName: string;
  email: string;
  status: string;
  createdAt: string;
  updatedAt: string;
};

export type Team = {
  id: number;
  companyId: number;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Project = {
  id: number;
  companyId: number;
  projectKey: string;
  name: string;
  description: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
};

export type RoleOption = {
  id: number;
  code: string;
  name: string;
};

export type Member = {
  userId: number;
  fullName: string;
  email: string;
  roleCode: string;
  roleName: string;
  status: string;
};

export type Issue = {
  id: number;
  issueKey: string;
  title: string;
  projectKey: string;
  status: string;
  priority: string;
  assigneeName: string;
  createdAt: string;
};

export type IssueDetail = {
  id: number;
  issueKey: string;
  title: string;
  description: string | null;
  projectKey: string;
  status: string;
  priority: string;
  reporterName: string;
  assigneeName: string;
  dueDate: string | null;
  createdAt: string;
};

export type IssueComment = {
  id: number;
  issueId: number;
  authorId: number;
  authorName: string;
  body: string;
  createdAt: string;
};

export type IssueAttachment = {
  id: number;
  issueId: number;
  uploaderId: number;
  uploaderName: string;
  originalFilename: string;
  mimeType: string;
  sizeBytes: number;
  createdAt: string;
};

export type IssueActivity = {
  id: number;
  issueId: number;
  actorId: number;
  actorName: string;
  eventType: string;
  metadata: Record<string, unknown>;
  createdAt: string;
};

export type IssueCodeLink = {
  id: number;
  issueId: number;
  provider: string;
  artifactType: string;
  externalId: string;
  title: string | null;
  url: string | null;
  authorName: string | null;
  externalCreatedAt: string | null;
  createdAt: string;
  repoOwner: string;
  repoName: string;
};

export type IssueSummary = {
  id: number;
  issueKey: string;
  title: string;
  status: string;
  priority: string;
  assigneeName: string;
};

export type Sprint = {
  id: number;
  projectId: number;
  name: string;
  goal: string | null;
  status: string;
  startDate: string;
  endDate: string;
  createdAt: string;
  updatedAt: string;
};

export type SprintCommitment = {
  capturedAt: string;
  committedCount: number;
  completedCount: number;
  remainingCount: number;
};

export type SprintBurndownPoint = {
  date: string;
  committedCount: number;
  remainingCount: number;
};

export type SprintHealth = {
  sprintId: number;
  projectId: number;
  name: string;
  goal: string | null;
  status: string;
  startDate: string;
  endDate: string;
  healthStatus: string;
  sayDoPercent: number | null;
  commitment: SprintCommitment | null;
  burndown: SprintBurndownPoint[];
};

export type KanbanProjectSummary = {
  projectId: number;
  projectKey: string;
  projectName: string;
  issueCounts: Record<string, number>;
};

export type KanbanIssueCard = {
  issueId: number;
  issueKey: string;
  title: string;
  priority: string;
  assigneeName: string;
  ageDays: number;
};

export type KanbanColumnSummary = {
  status: string;
  title: string;
  wipLimit: number | null;
  policy: string | null;
  totalCount: number;
  overLimit: boolean;
};

export type KanbanLaneColumn = {
  status: string;
  issues: KanbanIssueCard[];
};

export type KanbanSwimlane = {
  laneId: string;
  label: string;
  columns: KanbanLaneColumn[];
};

export type KanbanFlowMetrics = {
  throughputLast7Days: number;
  averageWipAgeDays: number;
  oldestWipAgeDays: number;
};

export type KanbanBoard = {
  projectId: number;
  projectKey: string;
  projectName: string;
  columns: KanbanColumnSummary[];
  swimlanes: KanbanSwimlane[];
  metrics: KanbanFlowMetrics;
};

export type OkrObjective = {
  id: number;
  projectId: number;
  projectKey: string;
  projectName: string;
  title: string;
  description: string | null;
  status: string;
  periodStart: string;
  periodEnd: string;
  ownerUserId: number;
  ownerName: string;
  progress: number;
  confidenceLevel: "LOW" | "MEDIUM" | "HIGH" | null;
  finalScore: number | null;
  closedAt: string | null;
};

export type OkrKeyResult = {
  id: number;
  objectiveId: number;
  projectId: number;
  projectKey: string;
  projectName: string;
  title: string;
  targetValue: number;
  currentValue: number;
  unit: string;
  status: string;
};

export type OkrCheckin = {
  id: number;
  authorId: number;
  authorName: string;
  progressPercent: number | null;
  confidenceLevel: "LOW" | "MEDIUM" | "HIGH";
  note: string | null;
  createdAt: string;
};

export type OkrInitiative = {
  id: number;
  issueId: number | null;
  issueKey: string | null;
  issueTitle: string | null;
  projectId: number;
  projectKey: string;
  projectName: string;
};

export type OkrObjectiveDetail = OkrObjective & {
  keyResults: OkrKeyResult[];
  checkins: OkrCheckin[];
  initiatives: OkrInitiative[];
};

export type GanttProject = {
  projectId: number;
  projectKey: string;
  projectName: string;
  plannedStartDate: string | null;
  plannedEndDate: string | null;
};

export type GanttIssue = {
  issueId: number;
  issueKey: string;
  title: string;
  plannedStartDate: string | null;
  dueDate: string | null;
  status: string;
  baselineStartDate: string | null;
  baselineEndDate: string | null;
  assigneeUserId: number | null;
  assigneeName: string | null;
};

export type GanttBaseline = {
  baselineStartDate: string | null;
  baselineEndDate: string | null;
  capturedAt: string;
  capturedByUserId: number | null;
  capturedByName: string | null;
};

export type GanttDependency = {
  dependencyId: number;
  predecessorIssueId: number;
  predecessorIssueKey: string;
  successorIssueId: number;
  successorIssueKey: string;
  dependencyType: string;
};

export type GanttCriticalPathItem = {
  issueId: number;
  issueKey: string;
  title: string;
  plannedStartDate: string | null;
  dueDate: string | null;
  durationDays: number;
};

export type GanttResourceLoad = {
  assigneeUserId: number | null;
  assigneeName: string;
  issueCount: number;
  totalPlannedDays: number;
  windowStart: string | null;
  windowEnd: string | null;
};

export type GanttProjectDetail = GanttProject & {
  baseline: GanttBaseline | null;
  issues: GanttIssue[];
  dependencies: GanttDependency[];
  criticalPath: GanttCriticalPathItem[];
  resourceLoad: GanttResourceLoad[];
};

export type AnalyticsIssuesByProject = {
  projectId: number;
  projectKey: string;
  count: number;
};

export type AnalyticsSprintBacklog = {
  projectId: number;
  projectKey: string;
  backlog: number;
  activeSprint: number;
};

export type AnalyticsSummary = {
  issuesByStatus: Record<string, number>;
  issuesByPriority: Record<string, number>;
  issuesByProject: AnalyticsIssuesByProject[];
  sprintBacklogCounts: AnalyticsSprintBacklog[];
  teamCount: number;
  projectCount: number;
  userCount: number;
  messageCount: number;
};

export type AnalyticsCfdPoint = {
  date: string;
  open: number;
  inProgress: number;
  blocked: number;
  done: number;
  total: number;
};

export type AnalyticsCycleTimePoint = {
  issueKey: string;
  title: string;
  completedOn: string;
  cycleTimeDays: number;
};

export type AnalyticsFlowSummary = {
  medianDays: number;
  p85Days: number;
  sampleSize: number;
};

export type AnalyticsTechDebt = {
  debtCount: number;
  totalOpenCount: number;
  ratio: number;
};

export type AnalyticsAdvanced = {
  cfdSeries: AnalyticsCfdPoint[];
  cfdApproximate: boolean;
  cfdWindowDays: number;
  cycleTimeSeries: AnalyticsCycleTimePoint[];
  flowSummary: AnalyticsFlowSummary;
  mttrDays: number;
  techDebt: AnalyticsTechDebt;
};

export type TeamCreatePayload = {
  name: string;
  description?: string | null;
};

export type ProjectCreatePayload = {
  projectKey: string;
  name: string;
  description?: string | null;
};

export type MemberAddPayload = {
  userId: number;
  roleId: number;
};

export type IssueCreatePayload = {
  projectId: number;
  title: string;
  description?: string | null;
  reporterUserId: number;
  assigneeUserId?: number | null;
  status: string;
  priority: string;
};

export type SprintCreatePayload = {
  name: string;
  goal?: string | null;
  status: string;
  startDate: string;
  endDate: string;
};

export type SprintUpdatePayload = {
  goal?: string | null;
  status?: string;
};

export type SprintIssueAssignPayload = {
  issueId: number;
};

export type OkrObjectiveCreatePayload = {
  title: string;
  description?: string | null;
  ownerUserId: number;
  status: string;
  periodStart: string;
  periodEnd: string;
};

export type OkrKeyResultCreatePayload = {
  title: string;
  targetValue: number;
  currentValue: number;
  unit: string;
  status: string;
};

export type OkrCheckinCreatePayload = {
  progressPercent?: number | null;
  confidenceLevel: "LOW" | "MEDIUM" | "HIGH";
  note?: string | null;
};

export type OkrObjectiveClosePayload = {
  status: "COMPLETED" | "CANCELLED";
  finalScore: number;
};

export type OkrInitiativeCreatePayload = {
  issueId: number;
};

export type MessageThreadTeam = {
  threadId: number;
  threadType: "TEAM";
  teamId: number;
  teamName: string;
  lastMessageAt: string | null;
};

export type MessageThreadProject = {
  threadId: number;
  threadType: "PROJECT";
  projectId: number;
  projectKey: string;
  projectName: string;
  lastMessageAt: string | null;
};

export type MessageThreadDirect = {
  threadId: number;
  threadType: "DIRECT";
  otherUserId: number;
  otherUserFullName: string;
  otherUserEmail: string;
  lastMessageAt: string | null;
};

export type MessageItem = {
  messageId: number;
  authorUserId: number;
  authorFullName: string;
  body: string;
  createdAt: string;
};

export type DirectThreadResponse = {
  threadId: number;
  threadType: "DIRECT";
  directUserOneId: number;
  directUserTwoId: number;
  lastMessageAt: string | null;
};
