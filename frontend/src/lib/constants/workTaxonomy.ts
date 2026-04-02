// TODO(Phase Y): replace hardcoded taxonomy with server-driven configuration.
export const STATUS_OPTIONS = [
  { value: "BACKLOG", label: "Backlog" },
  { value: "READY", label: "Ready to Do" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "IN_REVIEW", label: "In Review / QA" },
  { value: "BLOCKED", label: "Blocked" },
  { value: "DISCARDED", label: "Discarded / Rejected" },
  { value: "DONE", label: "Done / Closed" }
] as const;

export const PRIORITY_OPTIONS = [
  { value: "EPIC", label: "EPIC" },
  { value: "HIGH", label: "High" },
  { value: "MEDIUM", label: "Medium" },
  { value: "LOW", label: "Low" }
] as const;

export const CATEGORY_OPTIONS = [
  { value: "FEATURE", label: "Feature" },
  { value: "TASK", label: "Task" },
  { value: "IMPROVEMENT", label: "Improvement" },
  { value: "BUG", label: "Bug" },
  { value: "HOTFIX", label: "Hotfix" },
  { value: "TECHNICAL_DEBT", label: "Technical Debt" }
] as const;

const statusLabelMap = new Map(STATUS_OPTIONS.map((option) => [option.value, option.label]));
const priorityLabelMap = new Map(PRIORITY_OPTIONS.map((option) => [option.value, option.label]));
const categoryLabelMap = new Map(CATEGORY_OPTIONS.map((option) => [option.value, option.label]));

export function formatStatus(status: string) {
  return statusLabelMap.get(status) ?? status;
}

export function formatPriority(priority: string) {
  return priorityLabelMap.get(priority) ?? priority;
}

export function formatCategory(category: string) {
  return categoryLabelMap.get(category) ?? category;
}
